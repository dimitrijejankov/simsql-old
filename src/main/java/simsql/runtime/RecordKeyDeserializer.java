

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

import org.apache.hadoop.io.serializer.Deserializer;
import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.FileNotFoundException;

/**
 * This is used to deserialize record keys so thay can be processed
 * by a reducer
 */
class RecordKeyDeserializer implements Deserializer <WritableKey> {
  
  // this is the stream we deserialize from
  private InputStream getFromMe;
  
  // this is where we store the data
  byte [] dataHere = new byte [10];
  
  public void open (InputStream in) throws IOException {
    getFromMe = in;
  }
  
  public void open (String pathToWriteTo) {
    try {
      FileInputStream inFileStream = new FileInputStream (pathToWriteTo);
      getFromMe = new BufferedInputStream (inFileStream, 32768);
    } catch (FileNotFoundException e) {
      throw new RuntimeException ("for some reason I could not create the file you asked for"); 
    }
  }
  
  public void close () throws IOException {
    if (getFromMe != null)
      getFromMe.close ();
  }
  
  public WritableKey deserialize (WritableKey input) throws IOException {

      /***
      if (input instanceof Nothing)
	  return input;
      ***/

    // do the read
    int returnVal = getFromMe.read (dataHere);
    
    // if we didn't get 10 bytes, we have a problem
    if (returnVal != 10) {
      throw new IOException (); 
    }
    
    // get the key
    long newKey = 0;
    for (int i = 0; i < 8; i++) {
      newKey += ((long) (dataHere[i] & 0xFF)) << ((7 - i) * 8);
    }
    
    // get the code
    short code = 0;
    for (int i = 0; i < 2; i++) {
      code += ((short) (dataHere[i + 8] & 0xFF)) << ((1 - i) * 8);
    }
    
    // and create the object
    if (input == null) {
      return new RecordKey (newKey, code); 
    } else {
      input.set (newKey, code);
      return input;
    }
  }
  
}
