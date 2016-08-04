

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
import java.util.ArrayList;

class InferMapper extends MapperWithPipeNetwork {
  
  public void map (Nothing key, Record value, Context context) throws IOException, InterruptedException {
   
    // first, cast "value" to the InputRecord type
    InputRecord input;
    try {
      input = (InputRecord) value;
    } catch (Exception e) {
      throw new RuntimeException ("For some reason, the cast of the input record in the mapper failed!"); 
    }

    // run the selection, projection as well as splitting

    ConstantRecord interRecord = (ConstantRecord) input.runSelectionAndProjection ();
    ConstantRecord[] writeMe = interRecord.runSplit ();
    
    // if we got nothing back, the record was killed by the selection
    if (writeMe == null) {
      return;  
    }
    
    for (int i = 0; i < writeMe.length; i++) {

      ConstantRecord rec = writeMe[i];

    	// create the RecordKey object that will be used to partition and order the records during the inference
    	RecordKey outKey = new RecordKey (rec.getHashKey (), rec.getTypeCode ());
    
    	// and send it on!
    	context.write (outKey, new RecordWrapper (rec));
    }
  }
}
