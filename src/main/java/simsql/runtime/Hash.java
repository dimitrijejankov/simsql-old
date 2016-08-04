

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

// this class computes the hash of a bit array... the code is nasty and lacks comments...
// perhaps we should replace it in the future with something better?

public class Hash {
 
  private static int newHash (int key) {
    
    key = ~key + (key << 15); // key = (key << 15) - key - 1;
    key = key ^ (key >>> 12);
    key = key + (key << 2);
    key = key ^ (key >>> 4);
    key = key * 2057; // key = (key + (key << 3)) + (key << 11);
    key = key ^ (key >>> 16);
    return key;
  }
  
  public static long hashMe (byte [] me) {
    
    
    long code = 0;
    
    for (int i = 0; i < me.length; i += 8) {
      
      int low = 0;
      int high = 0;
      
      for (int j = 0; j < 4; j++) {
        if (i + j < me.length) {
          low += (me[i + j] & 0xFF) << (8 * j);
        } else {
          low += 123 << (8 * j); 
        }
      }
      
      if (me.length <= 4) 
        high = low;
      else {
        for (int j = 4; j < 8; j++) {
          if (i + j < me.length) {
            high += (me[i + j] & 0xFF) << (8 * (j - 3));
          } else {
            high += 97 << (8 * (j - 3)); 
          }
        } 
      }
      
      long returnVal = ((long) newHash (high)) << 32;
      returnVal += newHash (low) & 0xFFFFFFFF;
      code = code ^ returnVal;
    }
    
    if (code < 0)
      code = Long.MAX_VALUE + code;
    
    return code;
  }
}
