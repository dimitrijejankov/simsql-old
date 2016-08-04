

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

import java.io.OutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import org.apache.hadoop.io.serializer.Serializer;
import java.io.IOException;
import java.io.FileNotFoundException;

/**
 * This is used to serialize RecordKey objects so they can be sorted
 * after they are created by the mapper
 */
class RecordKeySerializer implements Serializer <WritableKey> {
  
  private OutputStream writeToMe;
  
  public void close () throws IOException {
    if (writeToMe != null)
      writeToMe.close ();
  }
  
  public void open (OutputStream out) throws IOException {
    writeToMe = out; 
  }
  
  public void open (String pathToWriteTo) {
    try {
      FileOutputStream outFileStream = new FileOutputStream (pathToWriteTo);
      writeToMe = new BufferedOutputStream (outFileStream, 32768);
    } catch (FileNotFoundException e) {
      throw new RuntimeException ("for some reason I could not create the file you asked for"); 
    }
  }
    
  public void serialize (WritableKey serializeMe) throws IOException {

      /**
	 if (serializeMe instanceof Nothing)
	 return;
      **/

    // this is where we write the output
    byte [] outputArray = new byte [10];
    
    // serialize the key
    long key = serializeMe.getKey ();
    for (int i = 0; i < 8; i++) {
       outputArray[i] = (byte) ((key >>> ((7 - i) * 8)) & 0xFF);
    }
    
    // serialize the code
    short code = serializeMe.getTypeCode ();
    outputArray[8] = (byte) ((code >>> 8) & 0xFF);
    outputArray[9] = (byte) (code & 0xFF);
    
    // and do the write
    writeToMe.write (outputArray);
  }
}
