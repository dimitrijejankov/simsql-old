

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
import java.io.IOException;
import java.util.HashSet;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;


/*
 * The mapper for the selection/projection/scalarFunc operator.
 */
class SelectionMapper extends MapperWithPipeNetwork {

  // set with duplicates -- we do a "pre-removal" to take stress from the reducers.
  private Deduper dedup;

  // writer class for duplicate removal and sort ordering -- send all records
  // to a particular reducer, based on hash.
  public static class WriterSort implements OutputAdapter {

    private TaskInputOutputContext <?, ?, WritableKey, WritableValue> context;
    private RecordKey rk = new RecordKey();
    private RecordWrapper rw = new RecordWrapper();

    public WriterSort(TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
      this.context = context;
    }

    public void writeRecord(Record writeMe) {
      try {
	HashableRecord r = (HashableRecord)writeMe;
	r.setSortAttribute(r.getHashKey());
	rk.set(r.getHashKey(), r.getTypeCode());
	rw.set(r);

	context.write(rk, rw);
	r.recycle();

      } catch (Exception e) {
	throw new RuntimeException("Failed to write out record!", e);
      }
    }
  }

  // set up the mapper...
  public void setup(Context context) throws IOException, InterruptedException {

    // get our configuration params.
    boolean removeDuplicates = context.getConfiguration().getBoolean("simsql.removeDuplicates", false);
    boolean runSelectionReducer = context.getConfiguration().getBoolean("simsql.runSelectionReducer", false);
    boolean collectStats = context.getConfiguration().getBoolean("simsql.reducer.collectStats", false);
    long duplicatesBufferSize = context.getConfiguration().getLong("simsql.duplicatesBufferSize", 1024L * 1024L * 10);  


    // case 1: we don't have to run a reducer.
    if (!runSelectionReducer) {
      dedup = new Deduper(duplicatesBufferSize, new SelectionReducer.Writer(context), removeDuplicates);
      return;
    }

    // case 2: we have to run a reducer, AND we have to get a specific sort order.
    dedup = new Deduper(duplicatesBufferSize, new WriterSort(context), removeDuplicates);
  }

  public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {

    // first, cast "value" to the InputRecord type
    InputRecord input;
    try {
      input = (InputRecord) value;
    } catch (Exception e) {
      throw new RuntimeException ("For some reason, the cast of the input record in the mapper failed!"); 
    }

    // run the selection and projection and assign everything.
    HashableRecord writeMe = (HashableRecord)input.runSelectionAndProjection ();
    
    // if we got nothing back, the record was killed by the selection
    input.recycle();
    if (writeMe == null) {
      return;  
    }

    // give it to the deduper.
    dedup.take(writeMe);
  }
}

class ChainedSelectionMapper extends /* MapperWithPipeNetwork */ Mapper<Nothing, Record, WritableKey, WritableValue> {

  public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {

    HashableRecord r = (HashableRecord)value;
    r.setSortAttribute(r.getHashKey());
    context.write(new RecordKey(r.getHashKey(), r.getTypeCode()), new RecordWrapper(r));
  }

}
