

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

class JoinReducer extends ReducerWithPostProcessing {
  
  private JoinReducerInnards myInnards;

  public void setup (Context context) throws IOException, InterruptedException {
    myInnards = new JoinReducerInnards (this);
    myInnards.setup (context, -1);  
  }

  public void reduce (RecordKey key, Iterable <RecordWrapper> values, Context context) {
    myInnards.reduce (key, values, context); 
  }
}
