

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

import org.apache.hadoop.io.RawComparator;
 
/**
 * This is used to group records so that all records in the same 
 * group are given to the same reducer.  In our case, this means
 * that all records with the same hashcode
 */
public class RecordKeyGroupingComparator implements RawComparator <RecordKey> {
 
  public int compare(byte [] keyLHS, int startLHS, int lenLHS, byte [] keyRHS, int startRHS, int lenRHS) {
    
    // first, make sure we have full RecordKey objects!!
    if (lenLHS < 10 || lenRHS < 10)
      throw new RuntimeException ("Why did I get small RecordKeys to compare??");
    
    // go through and compare the high order bits down to the low order bits
    for (int i = 0; i < 8; i++) {
      int lhs = keyLHS[i + startLHS] & 0xFF;
      int rhs = keyRHS[i + startRHS] & 0xFF;
      
      if (lhs < rhs) 
        return -1;
      if (lhs > rhs)
        return 1;
    }
    
    // getting here means that we have two equal keys
    return 0;
  }
 
  // here, just check the keys
  public int compare (RecordKey keyLHS, RecordKey keyRHS) {
   if (keyRHS.getKey () > keyLHS.getKey ())
     return -1;
   else if  (keyRHS.getKey () < keyLHS.getKey ())
     return 1;
   else
     return 0;
  }
 
}
