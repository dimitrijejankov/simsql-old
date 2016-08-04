

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

/**
 * This class is used to facilitate fast sorting and comparing of records...
 * that way, the records themselves do not have to be deserialized during
 * sorting... instead, the keys can be looked at directly
 */
public class RecordKey implements WritableKey {
  
  // this is generally going to be the hash key of a record
  private long key;
  
  // this is generally going to be the file key of a record... it can
  // be used to make sure the records from one file come first to the
  // reducer
  private short typeCode;
  
  // "getters" for the two values
  public long getKey () {
    return key;
  }
  
  public short getTypeCode () {
    return typeCode;
  }
  
  // sets all of the fields
  public void set (long keyIn, short typeCodeIn) {
        
    if (keyIn < 0)
      throw new RuntimeException ("hash value of " + keyIn + " typecode is " + typeCodeIn);
    
    key = keyIn;
    typeCode = typeCodeIn;
  }
  
  // simple constructor
  public RecordKey (long keyIn, short typeCodeIn) {
    
    if (keyIn < 0)
      throw new RuntimeException ("hash value of " + keyIn + " typecode is " + typeCodeIn);
    
    key = keyIn;
    typeCode = typeCodeIn;
  }
  
  // no param constructor
  public RecordKey () {
    key = -1;
    typeCode = -1;
  }
}
