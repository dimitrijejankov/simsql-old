

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

import org.apache.hadoop.mapreduce.Reducer;
import java.util.*;
import java.io.IOException;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

/**
 * The reducer for the selection/projection/scalarFunc/union operator.
 * To be invoked only when removing duplicates or when sorting.
 */
class SelectionReducer extends ReducerWithPostProcessing {

  // adapter class for writing -- this one can be used by the mapper.
  public static class Writer implements OutputAdapter {

    private TaskInputOutputContext <?, ?, WritableKey, WritableValue> context;
    private Nothing dummy = new Nothing();

    public Writer(TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
      this.context = context;
    }

    public void writeRecord(Record writeMe) {
      try {
	if (writeMe != null) {
	  context.write(dummy, writeMe);
	}
      } catch (Exception e) {
	throw new RuntimeException("Failed to write out record!", e);
      }
    }
  }

  // set with duplicates
  private Deduper dedup;

  // set up the reducer...
  public void setup(Context context) throws IOException, InterruptedException {

    // determine if we are removing duplicates.
    boolean removeDuplicates = context.getConfiguration().getBoolean("simsql.removeDuplicates", false);
    dedup = new Deduper(1024L * 1024L * 10, new Writer(context), removeDuplicates);
  }
    
  public void reduce (RecordKey key, Iterable <RecordWrapper> values, Context context) {

    // go through all the input records -- that's it.
    for (RecordWrapper rw: values) {

      Record rec = (Record)rw.getWrappedRecord();
      dedup.take(rec);
    }
  }
}
