

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

import org.apache.hadoop.mapreduce.*;
import java.io.*;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

/** Map class for the VGWrapper.
 *
 * @author Luis.
 */
public class VGWrapperMapper extends MapperWithPipeNetwork {

  private boolean runVGWrapperReducer = true;
  private VGWrapper vgw = null;
  private long lastKey = -1;

  private RecordKey rk = new RecordKey();
  private RecordWrapper rw = new RecordWrapper();

  public void setup(Context context) throws IOException, InterruptedException {

    // check if we have a map-only VGWrapper.
    runVGWrapperReducer = context.getConfiguration().getBoolean("simsql.runVGWrapperReducer", true);

    if (!runVGWrapperReducer) {

      // if we don't, then we'll have to create our own here.
      // get the VG function and the types
      VGFunction vgf = new VGFunction(context.getConfiguration().getStrings("simsql.functionFile")[0]);

      // get the number of iterations
      int numMC = context.getConfiguration().getInt("simsql.numIterations", 1);

      // get the direct buffer size.
      int dataBufferSize = context.getConfiguration().getInt("simsql.dataBufferSize", 1024 * 1024 * 100);

      // start up the VGWrapper.
      vgw = new VGWrapper(dataBufferSize, vgf, numMC, new VGWrapperReducer.Writer(context), this);

      // load up the cross product relations.
      VGWrapperReducer.addCrossRelations(context.getConfiguration(), vgw);

      // load up the sorted relations.
      FileSplit fileSplit = (FileSplit) context.getInputSplit();
      String filename = fileSplit.getPath().getName();
      int whichMapper = RecordOutputFormat.getPartNumber(filename);

      VGWrapperReducer.addSortedRelations(context.getConfiguration(), vgw, whichMapper);
    }
  }


  public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {

    // first, cast our record
    InputRecord inputRec = null;
    try {
      inputRec = (InputRecord) value;
    } catch (Exception e) {
      throw new RuntimeException("Could not cast input record to relevant type!");
    }

    // run our selection and projection
    VGRecord hashRec = (VGRecord)inputRec.runSelectionAndProjection();
    inputRec.recycle();
	
    // leave if the record didn't pass the selection.
    if (hashRec == null) {
      return;
    }

    // are we doing an entire map-reduce job?
    if (runVGWrapperReducer) {

      // make a holder object.
      rw.set(hashRec);

      // is the record seeded?
      if (hashRec.containsSeed()) {

	// in that case, create key object and send the holder to its respective reducer.
	rk.set(hashRec.getHashKey(), hashRec.getTypeCode());
	context.write(rk, rw);
      } else {

	// otherwise, we will have to make keys that send a copy of the record to each reducer.
	for (int i=0;i<context.getNumReduceTasks();i++) {
	  rk.set((long)i, hashRec.getTypeCode());
	  context.write(rk, rw);
	}	    
      }

      return;
    }

    // if we got here, we have a map-side VG Wrapper, so we'll pass
    // the latest tuple and get to grouping.

    // cross products -- pass them always
    if (!hashRec.containsSeed()) {
      vgw.takeRecord(hashRec);
      return;
    }

    // is this the first time we pass a record?
    if (lastKey == -1) {
      lastKey = hashRec.getHashKey();
    }

    // did we finish our reading of this group?
    else if (hashRec.getHashKey() != lastKey) {

      // if so, load up everything for its key
      vgw.loadSortedRecords(lastKey);

      // and sample -- note that this is always one group behind what
      // the mapper is reading.
      boolean keepGoing = false;
      do {
	keepGoing = vgw.run();
      } while (keepGoing);      
    }

    // then, just pass the record and move on.
    vgw.takeRecord(hashRec);
    lastKey = hashRec.getHashKey();
  }

  public void cleanup(Context context) throws IOException, InterruptedException {

    // if we missed one last group, do it here.
    if (lastKey != -1) {

      // if so, load up everything for its key
      vgw.loadSortedRecords(lastKey);

      // and sample.
      boolean keepGoing = false;
      do {
	keepGoing = vgw.run();
      } while (keepGoing);      
    }

  }
}
