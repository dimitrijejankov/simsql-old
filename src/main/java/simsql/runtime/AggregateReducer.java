

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
import java.util.*;
import java.io.*;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

/** 
 * Reducer class for the Aggregate operator.
 *
 * @author Luis.
 */
class AggregateReducer extends ReducerWithPostProcessing {

  // adapter class for writing -- can be used by the mapper.
  // this one finalizes the aggregated records.
  public static class Writer implements OutputAdapter {

    private TaskInputOutputContext <?, ?, WritableKey, WritableValue> context;
    private Nothing dummy = new Nothing();

    public Writer(TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
      this.context = context;
    }

    public void writeRecord(Record writeMe) {

      try {
	AggregatorRecord aggRec = (AggregatorRecord)writeMe;
	Record output = aggRec.getFinal();

	if (output != null) {
	  context.write(dummy, output);
	  output.recycle();
	  aggRec.recycle();
	}
      } catch (Exception e) {
	throw new RuntimeException("Failed to write out record!", e);
      }
    }
  }

  // aggregator object
  private Aggregator agg;

  public void setup(Context context) {
    agg = new Aggregator(1024L * 1024L * 16, new Writer(context));
  }

  public void reduce(RecordKey key, Iterable <RecordWrapper> values, Context context) {

    // iterate through the input values and give them to the aggregator.
    for (RecordWrapper rw: values) {
      agg.take(rw.getWrappedRecord());
    }

    // flush it right away to maintain the sort on the hash key.
    agg.flush();
  }
}
