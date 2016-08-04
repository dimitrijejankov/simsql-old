

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

/**
 * This is the basic boolean type in the system.  It is called
 * a "bistring" because the boolean value can take different values
 * in different worlds.  In addition to the standard T/F values, the
 * whole thing also supports "unknown" values.  You will get these,
 * for example, if you try to do a comparison with a null value.
 */

public interface Bitstring {
  
  /**
   * Prints the bitstring to a string and returns the result
   */
  String print ();
  
  /**
   * returns a true if and only if all of the entries are true
   */
  boolean allAreTrue ();
  
  /**
   * returns a true if and only if all of the entries are not true;
   * note that it is possible that some entries are not true or false,
   * and are "unknown".  These result from comparisons with NULL values.
   */
  boolean allAreFalseOrUnknown ();
  
  /**
   * returns all a Bitstring indicating which entries are definitely true 
   * (not false and not unknown)
   */
  Bitstring theseAreTrue ();

  /**
   * returns true if the ith value is true
   */
  boolean getValue (int whichOne);
  
  /**
   * Returns a version of the bitstring where all T values are represented
   * as the integer one.  Any F values (or unknown values) become a zero
   */
  Attribute toInt ();
  
  /**
   * returns the fraction of true values in all of the entries
   */
  double fractionOfTrue ();
  
  /**
   * returns true for every possible world where the truth value is
   * unknown (you get an unknown from a comparison involving a NULL)
   */
  Bitstring isUnknown ();
  
  /**
   * sets all of the indicated possible worlds to take the "unknown" value
   * and returns the result
   */
  Bitstring setUnknown (Bitstring theseAreUnknown);
  
  /**
   * flips all of the bits in the bitstring
   */
  Bitstring not ();
  
  /**
   * Serializes the bitstring to the given stream, returning the number of bytes written
   */
  int writeSelfToStream (DataOutputStream writeToMe) throws IOException;
  
  /**
   * This deserializes the bitstring from the specified input stream, returning the number
   * of bytes read.
   */
  int readSelfFromStream (DataInputStream readFromMe) throws IOException;
  
  /**
   * boolean operations on pairs of bitstrings
   */
  Bitstring and (Bitstring withMe);
  Bitstring or (Bitstring withMe);
  Bitstring xor (Bitstring withMe);
  Bitstring equals (Bitstring me);
  
  /**
   * boolean operations on a bitstring and a single boolean
   */
  Bitstring and (boolean withMe);
  Bitstring or (boolean withMe);
  Bitstring xor (boolean withMe);
  Bitstring equals (boolean me);
  
  /**
   * boolean operations on a bitstring and an array og booleans
   */
  Bitstring and (boolean [] withMe);
  Bitstring or (boolean [] withMe);
  Bitstring xor (boolean [] withMe);
  Bitstring equals (boolean [] me);
}
