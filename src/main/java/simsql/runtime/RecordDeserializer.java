

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

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.io.DataInputStream;
import org.apache.hadoop.io.serializer.Deserializer;

class RecordDeserializer implements Deserializer <WritableValue> {
  
  // this is an array of records that the RecordDeserializer will use to create the
  // records it produces as it deserializes
  Record [] prototypeRecords;
  RecordPool [] pools;
  
  // this is the file that we are reading from
  DataInputStream fileIn = null;
  
  /** 
   * The constructor builds an array of all of the different record types that can be
   * produced by the deserializer (if the input stream has three record types in
   * it, for example, then an array containing these three types would be passed
   * as "possibleRecordTypes".  What the deserializer does when deserializing is look at the
   * TypeCode at the start of the record; it then tries to find someone in "possibleRecordTypes"
   * that has the specified TypeCode... if it does, it then creates a record of that
   * type, derializes into it, and then returns the record.
   */
  public RecordDeserializer () {
    
    // if for some reasin this is already set, get outta here
    if (prototypeRecords != null)
      return;
    
    Class<?> [] possibleRecordTypes = RecTypeList.getPossibleRecordTypes ();
    
    // first, go through and create dummy records of each type
    prototypeRecords = new Record [possibleRecordTypes.length];
    pools = new RecordPool[possibleRecordTypes.length];
    for (int i = 0; i < possibleRecordTypes.length; i++) {
      // extract the record's constructor and create a version of it
      try {
        Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
        prototypeRecords[i] = (Record) myConstructor.newInstance ();
	pools[i] = new RecordPool((AbstractRecord)prototypeRecords[i]);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException ("I could not find a valid contructor for one of the record types I was passed"); 
      } catch (InstantiationException e) {
        throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (1)");
      } catch (IllegalAccessException e) {
        throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (2)");
      } catch (InvocationTargetException e) {
        throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (3)");
      }
    }
  }
  
  public WritableValue deserialize (WritableValue input) throws IOException {
    
    // first we read in the code for the next record
    short typeCode;
    typeCode = fileIn.readShort (); 
    
    if (prototypeRecords == null)
      throw new RuntimeException ("Did you forget to set up the record deserializer?");
    
    for (int i = 0; i < prototypeRecords.length; i++) {
      
      // if we found a record to match the typecode...
      if (typeCode == prototypeRecords[i].getTypeCode ()) {
        
        // deserialize it and put it into both the key and the value
        //RecordWrapper returnVal = new RecordWrapper (prototypeRecords[i].buildRecordOfSameType ());

	RecordWrapper returnVal = new RecordWrapper (pools[i].get ());
        int kx = returnVal.readSelfFromStream (fileIn);

	/***
 	if (input instanceof Record) {
	    ((Record)input).copyMyselfFrom(returnVal.getWrappedRecord());
	    return returnVal.getWrappedRecord();
	}
	***/

        return returnVal;
      }
    }
    
    // if we made it here if means we got a bad typecode
    throw new RuntimeException ("I could not find a record with typeCode " + typeCode);
  }
  
  public synchronized void close () throws IOException {
    if (fileIn != null) {
      fileIn.close(); 
    }
  }
  
  public void open (InputStream in) {
    fileIn = new DataInputStream (in);
  }
}
