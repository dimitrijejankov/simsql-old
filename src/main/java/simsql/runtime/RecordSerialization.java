

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

import org.apache.hadoop.io.serializer.Serialization;

/**
 * Because hadoop is a bit convoluted in how you specify a record serializer/deserializer,
 * you need to have these two classes that serve as a factory for record and recordkey
 * serializers and deserializers
 */

class RecordSerialization implements Serialization <WritableValue> {
  
  public boolean accept (Class <?> c) {
    return c.equals (RecordWrapper.class) || WritableValue.class.isAssignableFrom(c);
  }
  
  public RecordSerializer getSerializer (Class <WritableValue> c) {
    return new RecordSerializer ();
  }
    
  public RecordDeserializer getDeserializer (Class <WritableValue> c) {
    return new RecordDeserializer ();
  }
  
}

class RecordKeySerialization implements Serialization <WritableKey> {
  
  public boolean accept (Class <?> c) {
    return c.equals (RecordKey.class) || WritableKey.class.isAssignableFrom(c);
  }
  
  public RecordKeySerializer getSerializer (Class <WritableKey> c) {
    return new RecordKeySerializer ();
  }
  
  public RecordKeyDeserializer getDeserializer (Class <WritableKey> c) {
    return new RecordKeyDeserializer ();
  }
  
}
