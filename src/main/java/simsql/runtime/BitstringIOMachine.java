

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

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

class BitstringIOMachine {
  
  /**
   * serializes the Bitstring object to the given stream
   */
  public int writeToStream (Bitstring writeMe, DataOutputStream writeToMe) throws IOException {
  
    Class <? extends Bitstring> classToWrite = writeMe.getClass ();
    if (classToWrite.equals (BitstringUnknown.class)) {
      writeToMe.writeByte (34);
    } else if (classToWrite.equals (BitstringWithArray.class)) {
      writeToMe.writeByte (23);
    } else if (classToWrite.equals (BitstringWithSingleValue.class)) {
      writeToMe.writeByte (19);
    } else if (classToWrite.equals (BitstringWithSomeUnknown.class)) {
      writeToMe.writeByte (46);
    } else {
      throw new RuntimeException ("trying to serialize an unknwn Bitstring class"); 
    }
    
    return 1 + writeMe.writeSelfToStream (writeToMe);
  }
  
  /**
   * read in the next Bitstring object
   */
  public Bitstring readFromStream (DataInputStream readFromMe, WriteableInt bytesRead) throws IOException {
   
    // first, read the type code
    int code = readFromMe.readByte ();
    
    // now, create an object
    Bitstring lastRead;
    if (code == 34) {
      lastRead = new BitstringUnknown ();
    } else if (code == 23) {
      lastRead = new BitstringWithArray ();
    } else if (code == 19) {
      lastRead = new BitstringWithSingleValue ();
    } else if (code == 46) {
      lastRead = new BitstringWithSomeUnknown ();
    } else {
      throw new RuntimeException ("found a bitstring type that I did not recognize");
    }
   
    bytesRead.set (1 + lastRead.readSelfFromStream (readFromMe));
    return lastRead;
  }
}
