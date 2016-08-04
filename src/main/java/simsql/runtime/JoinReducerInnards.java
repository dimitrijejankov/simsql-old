

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

import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapred.FileSplit;
import java.util.*;
import java.lang.ref.*;
import java.io.IOException;
import java.text.NumberFormat;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

// this is the "innards" or core functionality of the Join's reduce operation.  It is not actually
// housed inside of the Join's reducer, because it can actually appear on the map side as well (during
// a pure merge join)

class JoinReducerInnards {
  
  // this is the dummy object we return with every output record
  private Nothing dummy = new Nothing ();
    
  // this is where we pull records from in the case that we only sorted one of the input relations 
  private Iterator <Record> smallFile;

  // this is the guy that will allow us to put records through a pipeline
  private ObjectWithPipeNetwork myRecordProcessor;

  // this is the greatest join key that we've processed from the small file
  private long processedThruKey = -1;

  // this is the hash table used to store records from the small file
  private RecordHashTable myTable = null;

  // used to store recs that has not yet been added to the hash table
  private Record [] tabledRecs = new Record[10];
  private int numTabledRecs = 0;

  // this is the suffix of the file that we are supposed to join with
  String suffix;

  // the join type
  private JoinOp.JoinType joinType;

  // constructor... just remembers the RecordProcessor, so we can put records through the pipeline
  public JoinReducerInnards (ObjectWithPipeNetwork myRecordProcessorIn) {
    myRecordProcessor = myRecordProcessorIn;
  }

  // double the amount of storage for the list of tabled recs
  private void doubleTabledRecs () {
    Record [] newRecs = new Record [tabledRecs.length * 2];
    for (int i = 0; i < numTabledRecs; i++) {
      newRecs[i] = tabledRecs[i];
    }
    tabledRecs = newRecs;
  }

  // get another rec that has been "tabled" or saved
  private Record peekAtNextTabledRec () {
    return (tabledRecs[numTabledRecs - 1]); 
  }

  // remove one of the tabled recs
  private void forgetLastTabledRec () {
    numTabledRecs--;
    tabledRecs[numTabledRecs] = null;
  }

  // save another rec
  private void addTabledRec (Record addMe) {
    tabledRecs[numTabledRecs] = addMe;
    numTabledRecs++;
    if (numTabledRecs == tabledRecs.length)
      doubleTabledRecs ();
  }

  // see if there are saved recs
  private boolean areTabledRecs () {
    return numTabledRecs > 0;
  }
  
  // sets up the iterator over the file to be merged, if applicable... first arg is the context, the 
  // second is the mapper that this is running as part of (or -1 if it is the reducer)
  public void setup (TaskInputOutputContext <?, ?, WritableKey, WritableValue> context, 
	int whichMapper) throws IOException, InterruptedException {
    

    // get the join type
    String myJoinType = context.getConfiguration().get("simsql.joinType", "natural").toUpperCase();
    joinType = JoinOp.JoinType.valueOf(myJoinType);
    
    // if necessary, we set up the iterator over the sorted file here... see if we have a sorted file 
    String whichFile = context.getConfiguration ().get ("simsql.sortedFileName", ""); 
    if (!whichFile.equals ("") || whichMapper >= 0) {

      // this will let the join know that we have a merge on one side
      myTable = new RecordHashTable (0.60);

      // now we need to figure out the suffix of the file that we are going to process
      // if we are on the mapper, then we will get the name of the file that is being mapped...
      // by extracting the suffix of that file, we can figure out the suffix of the file we need
      // to merge with
      int whichOne;
      if (whichMapper >= 0) {
        whichOne = whichMapper;
      } else {
        whichOne = context.getConfiguration ().getInt ("mapred.task.partition", -1);
      }

      // get the other info about the file to proess
      short whichTypeCode = (short) context.getConfiguration ().getInt ("simsql.sortedFileTypeCode", -1);
      int howManyAtts = context.getConfiguration ().getInt ("simsql.sortedFileNumAtts", -1); 

      // now we need ro find the corresponding input flie
      FileSystem fs = FileSystem.get (context.getConfiguration ());
      Path path = new Path (whichFile);
      FileStatus [] fstatus = fs.listStatus (path, new TableFileFilter());

      // this is the file suffix we are trying to find
      String names = "";
      String suffix = RecordOutputFormat.getFileNumber(whichOne); 
      for (FileStatus f : fstatus) {

        // first we see if this thing ends with the correct suffix
        names += f.getPath ().getName () + ";";
        if (f.getPath ().getName ().contains (suffix)) {

          // we found the file to open, so go ahead and do it
          IteratorFactory myFactory = new IteratorFactory ();
          smallFile = myFactory.getIterableForFile (f.getPath ().toString (), howManyAtts, whichTypeCode).iterator ();
          return;
        }
      }

      smallFile = new EmptyRecordIterator ();
    }

  }

  // this loads up the record hash table in case we are doing just a merge
  private void loadUpRecordHashTable () {

    // these are the two prototype records
    LeftIn left = new LeftIn ();
    RightIn right = new RightIn ();

    // create the new hash table
    if (!areTabledRecs()) {
	int k = 0;
	for (Record r : myTable) {

	    AbstractRecord rr = (AbstractRecord)r;
	    rr.recycle();
	    k++;
	    if (k > 16384) {
		break;
	    }
	}
    }
    myTable.clear();
    
    // load up at least 10,000 records into it
    int i = 0;
    while (true) {
      
      // get the next record
      HashableRecord next;
      if (areTabledRecs ()) {

	next = (HashableRecord) peekAtNextTabledRec();
        Record temp = peekAtNextTabledRec ();

      // here, the pipe network does not have anything, but the file does
      } else if (smallFile.hasNext ()) {

        // process the next record and remember all of the recs that come through
        Record temp = smallFile.next ();
        myRecordProcessor.process (temp);
        while ((temp = myRecordProcessor.getResult ()) != null) {

	    HashableRecord nnext = null;
	    if (temp.getTypeCode () == left.getTypeCode ()) {
		left.copyMyselfFrom (temp);
		nnext = left.runSelectionAndProjection ();
	    } else if (temp.getTypeCode () == right.getTypeCode ()) {
		right.copyMyselfFrom (temp);
		nnext = right.runSelectionAndProjection ();
	    } else {
		throw new RuntimeException ("Oh no!  I got a record type that I do not recognize!");
	    }

	    if (nnext != null) {
		addTabledRec (nnext);
	    }

	    temp.recycle();
        }

        // and keep going
        continue;

      // there is no more data!
      } else {
        processedThruKey = Long.MAX_VALUE;
        break;
      }

      // remember the next key

      // LL: this is an issue, have to check with JoinOp.java
      long nextKey = next.getSortAttribute ();
      /***      long nextKey = next.getHashKey(); ***/

      // and make sure they are not out of order!
      if (nextKey < processedThruKey)
        throw new RuntimeException ("Got hash keys out of order!!");

      // if it has a different key, then see if we have added enough
      if (nextKey != processedThruKey && i > 10000) {

	// sanity check
	if (areTabledRecs()) {

	    // add the tabled records, without forgetting them
	    // or marking them as "processed"
	    for (int j=0;j<numTabledRecs;j++) {
		myTable.add(tabledRecs[j], ((HashableRecord)tabledRecs[j]).getSecondaryHashKey());
	    }
        }

        break;

      // if we get here, it means that we are processing the rec, so note that we have processed it
      } else {
        forgetLastTabledRec ();
      }

      // add it here!
      processedThruKey = nextKey;
      i++;
      myTable.add (next, next.getSecondaryHashKey ());
    }

  }

  private String print (AbstractRecord me) {
    String retVal = "";
    for (int i = 0; i < me.getNumAttributes (); i++) {
      retVal += me.getIthAttribute (i).print (33434); 
    }
    return retVal;
  }

  private SoftReference<RecordHashTable> tempTableR = new SoftReference<RecordHashTable>(null);

  public void reduce (RecordKey key, Iterable <RecordWrapper> values, TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
   
    // tells us we are joining
    boolean joining = false;
    
    // used to store all of the records from the first relation
    RecordHashTable tempTable = tempTableR.get();
    if (tempTable == null) {
      tempTable = new RecordHashTable(0.60);
      tempTableR = new SoftReference<RecordHashTable>(tempTable);
    } else {
      tempTable.clear();
    }
 
    // now we loop through all of the values
    Record firstRecord = null;
    Record firstFromRight = new RightOut();


    for (RecordWrapper r : values) {

      // remember the very first record so we can check its typecode
      if (firstRecord == null) {
	if (joinType == JoinOp.JoinType.NATURAL) {
	  firstRecord = r.getWrappedRecord (); 
	}
	else {
	  firstRecord = firstFromRight; 
	}
      }
      
      // if we have a record with a diffreent typeCode than the first one, it means that
      // we have moved on to the records from the 2nd relation... so we just scan all of 
      // the records in the first relation and output any hits
      if (!joining && (r.getWrappedRecord ().getTypeCode () != firstRecord.getTypeCode ())) {
        joining = true; 
      }
      
      // if we are currently joining, then compare the current record with the ones from the other relation
      if (joining || myTable != null) {

        // this is where we try to find matches for r1
        Record [] guyToPullFrom;
        HashableRecord next = (HashableRecord) r.getWrappedRecord ();

        // there are two cases: either we are doing a full-on, MapReduce join, in which case we will match
        // with everyone who came through the mapper
        if (joining && myTable != null) {
          throw new RuntimeException ("How is joining true?");
        } else if (joining) {
          guyToPullFrom = tempTable.find (next.getSecondaryHashKey ());
        } else {
          while (key.getKey () > processedThruKey) {
            loadUpRecordHashTable ();  
          }
          guyToPullFrom = myTable.find (next.getSecondaryHashKey ());
        }
    
	// is this a natural join?
	if (joinType == JoinOp.JoinType.NATURAL) {

	  // loop through all of the matches and run the join
	  if (guyToPullFrom != null) {
	    for (Record r1 : guyToPullFrom) {
          
	      // join the two records
	      Record output = Result.join ((AbstractRecord) r1, (AbstractRecord) r.getWrappedRecord ());
	      if (output != null) {
		try {
  
		  // remember the sort key and send it on its way
		  output.setSortAttribute (key.getKey ());
		  context.write (dummy, output);

		  output.recycle();
  
		} catch (InterruptedException e) {
		  throw new RuntimeException ("died when trying to write a join output rec to the output stream (1)"); 
		} catch (IOException e) {
		  throw new RuntimeException ("died when trying to write a join output rec to the output stream (2)"); 
		}
	      }
	    }
	  }
	}

	// is this a semijoin/antijoin?
	else {

	  boolean someonePassed = false;
          boolean testedOne = false;

	  Bitstring pred = BitstringWithSingleValue.FALSE;

	  if (guyToPullFrom != null) {

	    // not null? then traverse.
	    for (Record r1 : guyToPullFrom) {

	      // test
              testedOne = true;
	      pred = pred.or( Result.test((AbstractRecord)r.getWrappedRecord(), (AbstractRecord)r1) );

	      if (!pred.allAreFalseOrUnknown() && joinType == JoinOp.JoinType.SEMIJOIN) {

		// if we are a running a semijoin, we write out and break out of the loop.
		Record output = Result.compose((AbstractRecord)r.getWrappedRecord(), (AbstractRecord)r1, pred);
		output.setSortAttribute(key.getKey());
		try {
		  context.write(dummy, output);	
		  output.recycle();
		} catch (Exception e) { throw new RuntimeException("Failed (1) ", e); }
		
	        break;
	      }
	    }
          }

          // if we are doing an antijoin and pred is false, it means that we could not exclude
          // the current record and it will be included

          // pred could be unknown here, if the current record had no match... 

          // if it is an antijoin, then we see if we need to produce an output
	  if (joinType == JoinOp.JoinType.ANTIJOIN) {

            // note that we let a record through if the truth value of the NOT EXISTS at this point is UNKNOWN, or
	    // if it is TRUE.  Strictly speaking, this is not correct.  If the NOT EXSITS is UNKNOWN, the record should
	    // not make its way through.  But this was a semi-necessary hack.  The way we deal with anti-join predicates
            // when the LHS record has no potential match is to create a record with all NULLs and run the anti-join predicate
            // on the pair.  If we come back with UNKNOWN, then the record survived the predicate.
            if (!testedOne) {

              // get a null record and test against it
              pred = pred.or (Result.test((AbstractRecord)r.getWrappedRecord (), 
	  	(AbstractRecord) RightOut.getNull ()));
            }

            if (!pred.allAreTrue ()) {
	      Record output = Result.compose((AbstractRecord)r.getWrappedRecord(), 
		(AbstractRecord)r.getWrappedRecord(), pred.theseAreTrue ().not ());
	      output.setSortAttribute(key.getKey());
	      try {
	        context.write(dummy, output);
	        output.recycle();
	      } catch (Exception e) { throw new RuntimeException("Failed (2)", e); }
	    }
	  }
	}
        
        // remember that we just did a join
        firstRecord = null;
	r.getWrappedRecord().recycle();

      // if we are not currently joining, then add the record into the list
      } else {
        HashableRecord next = (HashableRecord) r.getWrappedRecord ();
        tempTable.add (next, next.getSecondaryHashKey ()); 
      }
    }
  }
}
