

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
 * These empty interfaces are used to enforce correctness on the
 * generics for operators that might have map-only jobs (like
 * selections and joins) and could call context.write() on different
 * object types, depending on the situation.
 *
 * To use them, just put <WritableKey, WritableValue> on the
 * declaration of your mapper class.
 * 
 */
interface WritableKey {
  long getKey();
  short getTypeCode();
  void set (long key, short code);
}

interface WritableValue {
  long writeSelfToStream (DataOutputStream writeToMe) throws IOException;
  int readSelfFromStream (DataInputStream readFromMe) throws IOException;
}

