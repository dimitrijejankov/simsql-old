

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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;

/**
 * This is lke the RecordKeyGroupingComparator, except that it also
 * takes into account the typeCode of the underlying record to
 * make sure that records of one type come before records of 
 * another type in the reducer
 */

public class RecordKeySortComparator implements RawComparator <RecordKey> {
 
  // this is an array of codes, sorted so that records with the first
  // typeCode should all come first, record with the second typeCode
  // should all come second, and so on
  private short [] orderedTypecodes = null;
  
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
    
    // if we have no typecode info, do not give them an order
    if (orderedTypecodes == null)
      return 0;
    
    // next we will compare the typecodes
    short codeLHS = 0, codeRHS = 0;
    for (int i = 0; i < 2; i++) {
      codeLHS +=  ((short) (keyLHS[i + startLHS + 8] & 0xFF)) << ((1 - i) * 8);
      codeRHS +=  ((short) (keyRHS[i + startRHS + 8] & 0xFF)) << ((1 - i) * 8);
    }
    
    // if they are the same, teh records are equal
    if (codeLHS == codeRHS) 
      return 0;
    
    for (int i = 0; i < orderedTypecodes.length; i++) {
      if (orderedTypecodes[i] == codeLHS)
        return -1;
      if (orderedTypecodes[i] == codeRHS)
        return 1;
    }
  
    // getting here means that we have two equal keys
    return 0;
  }
  
  public int compare (RecordKey keyLHS, RecordKey keyRHS) {
    
    // here, just check the key
    if (keyRHS.getKey () > keyLHS.getKey ())
      return -1;
    else if (keyRHS.getKey () < keyLHS.getKey ())
      return 1;  
      
      // if they do match, then move on to the type codes
    short codeLHS = keyLHS.getTypeCode ();
    short codeRHS = keyRHS.getTypeCode ();
    
    if (codeLHS == codeRHS) 
      return 0;
    
    // look and see if one of the type codes is preferred
    for (int i = 0; i < orderedTypecodes.length; i++) {
      if  (orderedTypecodes[i] == codeLHS)
        return -1;
      if  (orderedTypecodes[i] == codeRHS)
        return 1;
    }
  
    // getting here means that we have two equal keys
    return 0;
  }
  
  /** 
   * The constructor builds an array of all of the different type codes that can be
   * seen by the comparator, given in the order in which they should be grouped.  Thus,
   * records having the first type in possibleRecordTypes will always appear first
   * in the list of records given to a mapper; records having the second will always
   * be given after all of the records having the first, and so on.
   * 
   * If a record is encountered that has none of these types, it will always come last.
   */
  public RecordKeySortComparator () {
    
    // if for some reasin this is already set, get outta here
    if (orderedTypecodes != null)
      return;
    
    Class<?> [] possibleRecordTypes = Ordering.getOrdering ();
    
    orderedTypecodes = new short [possibleRecordTypes.length];
    
    // Go through and create dummy records of each type, asking each for their code
    for (int i = 0; i < possibleRecordTypes.length; i++) {
      // extract the record's constructor and create a version of it
      try {
        Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
        Record tempRecord = (Record) myConstructor.newInstance ();
        orderedTypecodes[i] = tempRecord.getTypeCode ();
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
}
