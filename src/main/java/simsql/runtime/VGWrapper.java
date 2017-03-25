

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


package simsql.runtime;

import java.nio.*;
import java.io.*;
import java.util.*;
import java.lang.reflect.*;
import java.lang.ref.*;
import java.util.concurrent.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.conf.*;
import java.text.NumberFormat;

/**
 * Encapsulates all the VGWrapper logic (originally, this was part of
 * VGWrapperReducer).
 *
 * @author Luis
 */
class VGWrapper {

  // the position exchange buffers
  private LongBuffer posBuffIn;
  private LongBuffer posBuffOut;
  
  // the data exchange buffers
  private ByteBuffer dataBuffIn;
  private ByteBuffer dataBuffOut;

  private int dataStartPosition = 0;  // for the cross products.
  private int posStartPosition = 0;   // same thing.

  // the tuple buffer
  private LongBuffer tupleBuf;

  // data types on the VGF
  private AttributeType[] inTypes;
  private AttributeType[] outTypes;
    
  // the VG function
  VGFunction vgf;

  // number of MC iterations
  private int numMC;

  // the adapter for writing output records
  private OutputAdapter writer;

  // the set of cross product records, serialized
  private ArrayList<VGIntermediateValue> crossRecs;
  private boolean crossAllEqual;

  // the current group's inner records.
  private VGIntermediateValue[] innerRecs;
  private int numInnerRecs;

  // the current group's outer record.
  private VGRecord outerRec;
  private boolean groupAllEqual;

  // intermediate values for the output of each group.
  private SoftReference<IntermediateValue[][]> intersR;
  private int numInters;
  private int lastNumInters;

  // cached cross product tuple buffer part.
  private long crossTup[][] = null;
  private int numCrossTup = 0;

  private SoftReference<long[]> innerTupR;
  private SoftReference<long[]> outTupR;

  // the mapper or reducer pipeline
  private ObjectWithPipeNetwork pipe;

  /** Simple constructor -- starts up the VGWrapper. */
  public VGWrapper(int bufferSize, VGFunction vgf, int numMC, OutputAdapter writer, ObjectWithPipeNetwork pipe) {

    System.out.println("Allocating buffer VG!!!!");
    System.out.flush();

    // copy the function, numMC, pipe and writer
    this.vgf = vgf;
    this.numMC = numMC;
    this.writer = writer;
    this.pipe = pipe;

    // get the input and output types.
    inTypes = vgf.getInputTypes();
    outTypes = vgf.getOutputTypes();

    // initialize some structures
    crossRecs = new ArrayList<VGIntermediateValue>();
    innerRecs = new VGIntermediateValue[32];
    numInnerRecs = 0;

    innerTupR = new SoftReference<long[]>(null);
    outTupR = new SoftReference<long[]>(null);

    for (int i=0;i<innerRecs.length;i++) {
      innerRecs[i] = new VGIntermediateValue(numMC);
    }

    outerRec = null;
    crossAllEqual = true;
    groupAllEqual = true;

    intersR = new SoftReference<IntermediateValue[][]>(null);
    numInters = 0;
    lastNumInters = 32;

    // declare the output position buffer.
    ByteBuffer pbo = ByteBuffer.allocateDirect(bufferSize / 10);
    pbo.order(ByteOrder.nativeOrder());
    posBuffOut = pbo.asLongBuffer();

    // and data output buffer.
    dataBuffOut = ByteBuffer.allocateDirect(bufferSize / 4);
    dataBuffOut.order(ByteOrder.nativeOrder());

    // create the input data exchange buffer.
    dataBuffIn = ByteBuffer.allocateDirect(bufferSize / 4);
    dataBuffIn.order(ByteOrder.nativeOrder());

    // and position buffer.
    ByteBuffer pbi = ByteBuffer.allocateDirect(bufferSize / 10);
    pbi.order(ByteOrder.nativeOrder());
    posBuffIn = pbi.asLongBuffer();

    // declare the tuple buffer.
    ByteBuffer tbi = ByteBuffer.allocateDirect(bufferSize / 10);
    tbi.order(ByteOrder.nativeOrder());
    tupleBuf = tbi.asLongBuffer();
    vgf.setBuffers(posBuffIn, posBuffOut, dataBuffIn, dataBuffOut, tupleBuf); 


    // set up the prototype records.
    Class<?> [] possibleRecordTypes = RecTypeList.getPossibleRecordTypes ();
    prototypeRecords = new Record[possibleRecordTypes.length];

    for (int i = 0; i < possibleRecordTypes.length; i++) {

      // extract the record's constructor and create a version of it
      try {
        Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
        prototypeRecords[i] = (Record) myConstructor.newInstance ();
      } catch (Exception e) {
	throw new RuntimeException("Could not obtain prototype records for deserialization!", e);
      }
    }
  }

  // set of prototype records for deserialization.
  private Record [] prototypeRecords;
  private RecordPool outputPool = new RecordPool(8192, new VGOutputRecord());

  /** Takes a bare input record, obtains its correct type, runs the selection and projection and returns a VGRecord. */
  private VGRecord processRecord(Record r) {

    // get the corresponding type.
    InputRecord inR = null;
    for (int i=0;i<prototypeRecords.length;i++) {
      if (r.getTypeCode() == prototypeRecords[i].getTypeCode()) {
	inR = (InputRecord)prototypeRecords[i] /*.buildRecordOfSameType()*/ ;
	inR.copyMyselfFrom(r);
	break;
      }
    }

    // check if it matched.
    if (inR == null) {
      throw new RuntimeException("Unable to match record with typeCode " + r.getTypeCode());
    }

    // now, run the selection and projection.
    VGRecord hashRec = (VGRecord)inR.runSelectionAndProjection();
    return hashRec;
  }

  /** Adds an entire cross product relation. */
  public void addCrossRelation(String inputFile, int numAtts, short typeCode) {

    IteratorFactory myFactory = new IteratorFactory ();
    for (Record r : myFactory.getIterableForFile(inputFile, numAtts, typeCode)) {

      // give the record to the pipeline
      pipe.process(r);

      // get all the output records and process them.
      Record temp;
      while ((temp = pipe.getResult()) != null) {

	// get the record
	VGRecord hashRec = processRecord(temp);
	
	// did it pass? good, serialize it.
	if (hashRec != null) {
	  takeCrossRecord(hashRec);
	}

	temp.recycle();
      }
    }
  }

  // queue of outer records waiting to be passed.
  private Queue<VGRecord> outerQueue = new ArrayDeque<VGRecord>();

  // hash table with inner records from relevant groups.
  private RecordHashTable innerHash = new RecordHashTable(0.60);

  // iterators for the sorted straight records.
  ArrayList<Iterator<Record>> sortedFiles = new ArrayList<Iterator<Record>>();

  /** Plugs in a sorted relation. */
  public void addSortedRelation(String fileName, int part, int numAtts, short typeCode) {

    // this is the file suffix we are trying to find
    String names = "";
    String suffix = RecordOutputFormat.getFileNumber(part);

    try {
      FileSystem fs = FileSystem.get(new Configuration());
      Path path = new Path (fileName);
      FileStatus[] fstatus = fs.listStatus (path, new TableFileFilter());
      for (FileStatus f : fstatus) {

	// first we see if this thing ends with the correct suffix      
	names += f.getPath ().getName () + ";";
	if (f.getPath ().getName ().contains (suffix)) {
	
	  // we found the file to open, so go ahead and do it
	  IteratorFactory myFactory = new IteratorFactory ();
	  Iterator<Record> someFile = myFactory.getIterableForFile (f.getPath ().toString (), numAtts, typeCode).iterator ();
	  sortedFiles.add(someFile);
	  tabledRecs.add(new ArrayDeque<VGRecord>()); // create its queues.
	  return;
	}
      }
    } catch (Exception e) {
      System.out.println("Something happened while reading file " + e + " ... maybe it is empty?" + fileName + " / " + part + " / " + suffix);
    }

    System.out.println("For some reason, I could not find the proper file to read." + fileName + " / " + part + " / " + suffix);
  }

  // "tabled" recs that remain when searching for the next key.
  ArrayList<ArrayDeque<VGRecord>> tabledRecs = new ArrayList<ArrayDeque<VGRecord>>();

  /** Loads up a bunch of sorted records into the queue and hash table, for a certain key value. */
  public void loadSortedRecords(long key) {

    // go through each stream
    for (int i=0;i<sortedFiles.size();i++) {

      // pass the tabled records first.
      ArrayDeque<VGRecord> tr = tabledRecs.get(i);
      
      boolean readFromDisk = true;
      while (true) {

	// peek the next record
	VGRecord rx = tr.peek();

	// empty queue? leave, and read more from the disk.
	if (rx == null) {
	  break;
	}

	// larger? leave -- but, the queue 'tr' *is* sorted on the
	// key. so we can skip reading from the disk for this
	// particular stream.
	if (rx.getSortAttribute() > key) {
	  readFromDisk = false;
	  break;
	}

	// if we get here, we got a useful record. so take it out.
	takeRecord(rx);
	tr.remove();	
      }

      // see if we have to read disk records.
      if (!readFromDisk) {
	continue;
      }

      // read until the next group is reached and we have enough
      // records.
      int k = tr.size();

      Iterator<Record> file = sortedFiles.get(i);
      while (true) {

	// if we reached EOF, leave.
	if (!file.hasNext()) {
	  break;
	}

	// got one record -- read it and process it.
	Record temp = file.next();
	pipe.process(temp);

	boolean passedKey = false;
	while ((temp = pipe.getResult()) != null) {

	  VGRecord hashRec = processRecord(temp);

	  // count the number of tuples.
	  k++;

	  if (hashRec != null) {

	    // check the key -- if it's above our expected key, put it as tabled.
	    if (hashRec.getSortAttribute() > key) {
	      tr.offer(hashRec);
	      passedKey = true;
	    }
	    else {
	      takeRecord(hashRec);
	    }
	  }

	  temp.recycle();
	}

	// buffer up to 10K tuples/stream in advance.
	if (passedKey && k >= 10000) {
	  break;
	}
      }
    }   
  }

  /** Takes in a record from the map or reduce function. */
  public void takeRecord(VGRecord rec) {

    // is it an outer record? to the queue.
    if (rec.isOuterRecord()) {
      outerQueue.offer(rec);
      return;
    }

    // is it a cross product record?
    if (!rec.containsSeed()) {
      takeCrossRecord(rec);
      return;
    }

    // otherwise, to the hash table on its secondary hash.
    innerHash.add(rec, rec.getSecondaryHashKey());
  }

  /** Takes a cross product record -- these will be maintained and replicated for all groups. */
  public void takeCrossRecord(VGRecord crossRec) {

    // update the all-equals value.
    crossAllEqual &= crossRec.allAreEqual();

    // get a new intermediate value for the record.
    VGIntermediateValue crossVal = new VGIntermediateValue(numMC);
    crossVal.readFrom(crossRec, inTypes, numMC, dataBuffIn, posBuffIn);

    // put it in the array.
    crossRecs.add(crossVal);
    dataStartPosition = dataBuffIn.position();
    posStartPosition = posBuffIn.position();
  }

  /** Starts a new VG group by taking the outer record. */
  public void takeNewGroup(VGRecord outerRec) {

    // get the record.
    this.outerRec = outerRec;

    // start up the all-equals value.
    groupAllEqual = crossAllEqual;
    
    // clear any previous groups.
    numInnerRecs = 0;

    // set the position of the data buffer to the point after the
    // cross product records, so that they don't get overwritten.
    dataBuffIn.position(dataStartPosition);
    posBuffIn.position(posStartPosition);

    // pass it as an inner rec
    takeInnerRec(outerRec);
  }

  /** Takes an inner record for the current VG group. */
  public void takeInnerRec(VGRecord innerRec) {

    // update the all-equals value.
    groupAllEqual &= innerRec.allAreEqual();

    // get a new intermediate value for the record.
    innerRecs[numInnerRecs].readFrom(innerRec, inTypes, numMC, dataBuffIn, posBuffIn);
    numInnerRecs++;

    // expand the array, if necessary.
    if (numInnerRecs >= innerRecs.length) {
      VGIntermediateValue[] newInnerRecs = new VGIntermediateValue[innerRecs.length * 2];

      // copy the existing values.
      for (int i=0;i<numInnerRecs;i++) {
	newInnerRecs[i] = innerRecs[i];
      }

      // create the new ones.
      for (int i=numInnerRecs;i<newInnerRecs.length;i++) {
	newInnerRecs[i] = new VGIntermediateValue(numMC);
      }

      innerRecs = newInnerRecs;
    }
  }

  /** Runs the VGWrapper for the next group. */
  public boolean run() {

    // if the queue of outer records is empty, we're done with our cached groups.
    if (outerQueue.isEmpty()) {
      return false;
    }

    // create the fixed array for the cross product records
    if (crossTup == null && crossRecs.size() > 0) {

      int numValues = crossAllEqual ? 1 : numMC;
      crossTup = new long[numValues][];

      for (int i=0;i<numValues;i++) {
	crossTup[i] = new long[crossRecs.size()];

	for (int j=0;j<crossRecs.size();j++) {
	  crossTup[i][j] = crossRecs.get(j).getStride(i);
	}
      }
      
      numCrossTup = crossRecs.size();

      // we can get ride of the original stuff.
      crossRecs.clear();
    }

    // pull a record from the queue, make it the outer.
    takeNewGroup(outerQueue.poll());

    // now, pull all the records from the hash table that match its
    // seed.
    Record[] inns = innerHash.find (outerRec.getSecondaryHashKey());
    if (inns != null) {
      for (int i=0;i<inns.length;i++) {
	
	VGRecord innsRec = (VGRecord)inns[i];
	if (innsRec.equals(outerRec)) {
	  takeInnerRec(innsRec);
	  innsRec.recycle(); // yes, definitely.
	}
      }
    }
    
    // now, we can go ahead and sample.
    boolean alreadyParameterized = false;
    numInters = 0;

    // see if we have the "inters" to do it.
    IntermediateValue[][] inters = intersR.get();
    if (inters == null) {

      // if not, then we'll create some.
      inters = new IntermediateValue[lastNumInters][];
      for (int i=0;i<inters.length;i++) {
	inters[i] = IntermediateValue.getIntermediates(numMC, outTypes);
      }
    }

    // see if we have the "innerTups" for the buffers.
    long[] innerTup = innerTupR.get();
    if (innerTup == null) {
      innerTup = new long[numInnerRecs];
    }

    // and the "outTups" too.
    long[] outTup = outTupR.get();
    if (outTup == null) {
      outTup = new long[lastNumInters * outTypes.length];
    }

    // go through all iterations
    for (int i=0;i<numMC;i++) {

      // is this iteration valid?
      if (outerRec.getIsPresent().getValue(i)) {

	// initialize the VG group.
	outerRec.initializeVG(vgf, i);

	// pass all the parameters, if necessary.
	if (!alreadyParameterized || !groupAllEqual) {

	  // clear the set of parameters.
	  vgf.clearParams();

	  // pass all the cross product records
	  if (numCrossTup > 0) {
	    tupleBuf.position(0);

	    if (crossAllEqual) {
	      tupleBuf.put(crossTup[0]);
	    } else {
	      tupleBuf.put(crossTup[i]);
	    }

	    vgf.takeParams(numCrossTup);
	  }
	  
	  // pass all the inner records.
	  if (innerTup.length < numInnerRecs) {
	    innerTup = new long[numInnerRecs];
	  }

	  for (int w=0;w<numInnerRecs;w++) {
	    VGIntermediateValue vgiv = innerRecs[w];
	    innerTup[w] = vgiv.getStride(i);
	  }

	  if (numInnerRecs > 0) {
	    tupleBuf.position(0);
	    tupleBuf.put(innerTup, 0, numInnerRecs);
	    vgf.takeParams(numInnerRecs);
	  }

	  alreadyParameterized = true;
	}

	// call the VGF for samples
	int outVals = (int)vgf.outputVals();

	// do we have enough intermediates for it?
	if (numInters < outVals) {
	  numInters = outVals;

	  if (numInters >= inters.length) {
	    IntermediateValue[][] intersNew = new IntermediateValue[numInters + 1][];
	    for (int q=0;q<inters.length;q++) {
	      intersNew[q] = inters[q];
	    }

	    for (int q=inters.length;q<intersNew.length;q++) {
	      intersNew[q] = IntermediateValue.getIntermediates(numMC, outTypes);
	    }
	    
	    inters = intersNew;
	  }
	}

	// read in all of the position buffer.	
	if (outTup.length < (outVals * outTypes.length)) {
	  outTup = new long[outVals * outTypes.length];
	}

	if (outVals > 0) {
	  posBuffOut.position(0);
	  posBuffOut.get(outTup, 0, outVals * outTypes.length);
	  dataBuffOut.position(0);
	}

	for (int curInter=0;curInter<outVals;curInter++) {
       
	  // update the intermediates...
	  // deserialize the output values.
	  for (int k=0;k<outTypes.length;k++) {
	      
	    int kVal = (int)outTup[(outTypes.length * curInter) + k];
	    if (kVal >= 0) {
		
	      switch(outTypes[k].getTypeCode()) {
	      case INT: {
		inters[curInter][k].set(dataBuffOut.getLong(kVal), i);
	      }
		break;
		
	      case DOUBLE: {
		inters[curInter][k].set(dataBuffOut.getDouble(kVal), i);
	      }
		break;

	      case STRING: {
		
		// get to the position
		dataBuffOut.position(kVal);
		
		// read the length of the string
		int len = (int)(dataBuffOut.getLong());
		
		// read those chars and make a string.
		byte[] btr = new byte[len];
		dataBuffOut.get(btr, 0, len);
		
		try {
		  inters[curInter][k].set(new String(btr, "UTF-8"), i);
		} catch (Exception e) {
		  throw new RuntimeException("Failed to read string from VG function!", e);
		}
	      }
		break;

          case SCALAR: {
            inters[curInter][k].set(dataBuffOut.getDouble(kVal), -1, i);
          }
          break;

          case VECTOR: {

            // get to the position
            dataBuffOut.position(kVal);

            // read the length of the vector
            int len = (int)(dataBuffOut.getLong());

            // read those doubles and make an array.
            double[] vec = new double[len];
            for (int j = 0; j < len; j++)
              vec[j] = dataBuffOut.getDouble();

            inters[curInter][k].set(vec, -1, i);
          }
          break;

          case MATRIX: {
            
            // get to the position
            dataBuffOut.position(kVal);
            
            // read the row and column numbers of the matrix
            int row = (int)(dataBuffOut.getLong());
            int col = (int)(dataBuffOut.getLong());

            // read those doubles and make an array.
            double[][] mat = new double[row][col];
            for (int j = 0; j < row; j++)
              for (int l = 0; l < col; l++)
                mat[j][l] = dataBuffOut.getDouble();

            inters[curInter][k].set(mat, true, i);
          }
          break;
	      }
	    }
	  }	  
	}
	
	// finalize this iteration
	vgf.finalizeTrial();
      }
    }
    
    // at this point, we have all the intermediates ready, so we create the 
    // output records with them
    for (int k=0;k<numInters;k++) {
      
      // build the record.
      // VGOutputRecord outRec = new VGOutputRecord(outerRec.getIsPresent(), outerRec, IntermediateValue.getAttributes(inters[k]));
      VGOutputRecord outRec = (VGOutputRecord) outputPool.get();
      outRec.setMeUp(outerRec.getIsPresent(), outerRec, IntermediateValue.getAttributes(inters[k]));

      // run the selection
      if (outRec.runSelection()) {

	// if the selection passes, write out the record using the adapter.
	// and give it the sorting of the outer input.
	outRec.setSortAttribute (outerRec.getSortAttribute());
	writer.writeRecord(outRec);
	outRec.recycle();
      }
    }

    outerRec.recycle();

    // save the inters as a soft reference for the next group to work on.
    if (numInters > 0) {
      lastNumInters = numInters;
    }

    intersR = new SoftReference<IntermediateValue[][]>(inters);
    innerTupR = new SoftReference<long[]>(innerTup);
    outTupR = new SoftReference<long[]>(outTup);

    // we're done. if we have exhausted all the outer records, then
    // we can clear up the hash table of inner records. 
    if (outerQueue.isEmpty()) {
      innerHash.clear();
    }
    return true;
  }

}

/** A serialized intermediate input record, to be written into the buffers. */
class VGIntermediateValue {

  // contains the stride in the position buffer to locate the i^th MC
  // iteration.
  private long[] strides = null;
  private int numVals;


  public VGIntermediateValue(int numMC) {

    // set everything up.
    strides = new long[numMC];
    numVals = 0;
  }

  public void readFrom(VGRecord inputRec, AttributeType[] inTypes, int numMC, ByteBuffer dataBuffIn, LongBuffer posBuffIn) {

    // get the number of iterations.
    numVals = inputRec.allAreEqual() ? 1 : numMC;

    // go through each iteration and build it.
    Attribute[] atts = inputRec.getVGInputAtts();

    for (int i=0;i<numVals;i++) {

      strides[i] = posBuffIn.position();

      // go through each attribute value.
      for (int j=0;j<inTypes.length;j++) {

	// if it's null, put -1 in the position buffer and move on.
	if (atts[j].isNull().getValue(i)) {
	  posBuffIn.put(-1);
	  continue;
	}

	// otherwise, buffer the data and advance the position.
	posBuffIn.put(dataBuffIn.position());
	byte[] bx = atts[j].getValue(i, inTypes[j]);
	dataBuffIn.put(bx);
      }
    }
  }

  public long getStride(int iteration) {

    if (numVals == 0)
      return -1;

    // get the iteration that matters to us.
    int whichIter = (numVals == 1) ? 0 : iteration;

    return strides[whichIter];
  }
}
