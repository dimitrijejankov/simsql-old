
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

// since all this is doing is sorting the output, this is pretty simple
class UnionReducer extends ReducerWithPostProcessing {

  private Nothing dummy = new Nothing();

  public void reduce (RecordKey key, Iterable <RecordWrapper> values, Context context) {

    // set the sort key and then write the record!
    for (RecordWrapper r : values) {
      Record record = r.getWrappedRecord ();
      record.setSortAttribute(key.getKey());
      try {
        context.write(dummy, record);
      } catch (Exception e) {
        throw new RuntimeException (e);
      }
      record.recycle ();
    }
  }
}


