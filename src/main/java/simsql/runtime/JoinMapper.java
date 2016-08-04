

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

import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import java.io.IOException;
import java.util.*;

class JoinMapper extends MapperWithPipeNetwork {
  
  // configuration parameters
  private boolean joinCartesian = false;
  private boolean isSelfJoin = false;
  private boolean isMergeJoin = false;
  private short smallTypeCode = -1;
  private short largeTypeCode = -1;
  private long numRec = 0;

  // used in the case that we are doing a pure merge join
  private JoinReducerInnards myLittleReducer = null;
  
  // this is used to send stuff along to the little reducer in the case of a pure merge
  private ArrayList <RecordWrapper> myRecs = new ArrayList <RecordWrapper> ();

  // mutables, because memory is precious.
  RecordKey rk = new RecordKey();
  RecordWrapper rw = new RecordWrapper();

  // set up the mapper...
  public void setup(Context context) throws IOException, InterruptedException {
    
    // get our configuration parameters.
    joinCartesian = context.getConfiguration().getBoolean("simsql.joinCartesian", false);
    smallTypeCode = (short)context.getConfiguration().getInt("simsql.smallerRelation.typeCode", -1);
    largeTypeCode = (short)context.getConfiguration().getInt("simsql.largerRelation.typeCode", -1);
    isSelfJoin = context.getConfiguration().getBoolean("simsql.isSelfJoin", false);
    isMergeJoin = context.getConfiguration().getBoolean("simsql.isMergeJoin", false);

    isSelfJoin &= !isMergeJoin;
    isSelfJoin &= context.getConfiguration ().get ("simsql.sortedFileName", "").equals("");

    // if this is actually a merge join, then we need to set up the reducer
    if (isMergeJoin) {
      myLittleReducer = new JoinReducerInnards (this);
      FileSplit fileSplit = (FileSplit) context.getInputSplit();
      String filename = fileSplit.getPath().getName();
      int whichMapper = RecordOutputFormat.getPartNumber(filename);
      myLittleReducer.setup (context, whichMapper);
      myRecs.add (new RecordWrapper ());
    }
  }

  public void map (Nothing key, Record value, Context context) throws IOException, InterruptedException {

    // first, cast "value" to the InputRecord type
    InputRecord input;
    try {
      input = (InputRecord) value;
    } catch (Exception e) {
      throw new RuntimeException ("For some reason, the cast of the input record in the mapper failed!"); 
    }

    // check if we have a self-join.
    if (isSelfJoin && key != null) {

      // prepare a record just like this one
      Record nextRec = null;
      if (input instanceof LeftIn) {
	nextRec = new RightIn();
      } else {
	nextRec = new LeftIn();
      }
      nextRec.copyMyselfFrom (input);

      // call "map" for that record.
      map(null, nextRec, context);
    }
    
    // run the selection and projection
    HashableRecord writeMe = input.runSelectionAndProjection ();
    
    // if we got nothing back, the record was killed by the selection
    if (writeMe == null) {
	input.recycle();
	return;  
    }

    // is this a Cartesian product?
    if (joinCartesian) {

      rw.set(writeMe);
      boolean sendEverywhere = (isSelfJoin && key == null) || (!isSelfJoin && input.getTypeCode() == smallTypeCode);

      // is this record from the smaller relation?
      if (sendEverywhere) {
	
	// if so, then send a copy to each reducer.
	for (int i=0;i<context.getNumReduceTasks();i++) {
	  rk.set((long) i, writeMe.getTypeCode());
	  context.write(rk, rw);
	}
      }

      // otherwise, it goes to a single reducer
      else {
	rk.set(numRec % context.getNumReduceTasks(), writeMe.getTypeCode());
	context.write(rk, rw);
	numRec++;
      }

      writeMe.recycle();
      input.recycle();
      return;
    }

    input.recycle();

    // not a Cartesian product...
    // split on the join attributes
    HashableRecord[] splits = writeMe.splitSome();
    for (HashableRecord rec: splits) {

	// create the RecordKey object that will be used to partition and order the records during the join
        rk.set(rec.getHashKey (), rec.getTypeCode ());
	rw.set(rec);

	// and send it on!
        if (!isMergeJoin) {
	  context.write (rk, rw);
	  rec.recycle();
        } else {
          myRecs.set (0, rw);
          myLittleReducer.reduce (rk, myRecs, context); 
        }
          
    }
  }
}
