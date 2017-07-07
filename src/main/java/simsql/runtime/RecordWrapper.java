

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
import java.io.*;


/**
 * Due to stupidity in hadoop, we can't just send generic "Record" objects on the output
 * stream from the Mapper.  The problem is that Record is an interface.  If we do send
 * object that implement this interface, it will error out with seomthing like:
 * 
 * Type mismatch in value from map: expected Record, recieved OutOrders
 * 
 * Even though OutOrders implements Record!  To get around this, the mapper needs to wrap
 * the record inside of a RecordWrapper object, and then send THAT along
 * 
 */ 
public class RecordWrapper implements WritableValue {
  
  private Record holdMe;
  
  public Record getWrappedRecord () {
    return holdMe;
  }
  
  public RecordWrapper (Record useMe) {
    holdMe = useMe;
  }
  
  public RecordWrapper () {
    holdMe = null; 
  }

  // a 'set' method so that we can recycle instances of this class.
  public void set(Record useMe) {
    holdMe = useMe;
  }


  public long writeSelfToStream (DataOutputStream writeToMe) throws IOException {
    return holdMe.writeSelfToStream(writeToMe);
  }

  public int readSelfFromStream (DataInputStream readFromMe) throws IOException {
    return holdMe.readSelfFromStream(readFromMe);
  }

}
