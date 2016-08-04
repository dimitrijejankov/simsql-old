

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
 * This is a dummy class that we use as the output of the initial RecordReader
 * and the input to the final RecordWriter (because we input and ouput Record
 * objects, we have no use for the keys, and we use the Nothing class instead)
 */

public class Nothing implements WritableKey {

  public void set(long key, short typeCode) {
  }

  public long getKey() {
    return 0;
  }

  public short getTypeCode() {
    return -1;
  }
}
