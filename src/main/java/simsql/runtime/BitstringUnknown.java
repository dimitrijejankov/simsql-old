

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
 * This class implements a boolean whose value is unknown because it
 * has resulted from a comparison with an NULL value.  This particular
 * class implements a boolean whose value is unknown in all worlds.
 */

import java.io.DataOutputStream;
import java.io.DataInputStream;

public class BitstringUnknown implements Bitstring {

  public static void main (String [] args) {

	Attribute rightAtts0 = new NullAttribute ();
	Attribute leftAtts0 = new NullAttribute ();
        Attribute leftAtts4 = new StringAttribute ("Sawmill Mountain");
	Bitstring leftIsPresent = new BitstringWithSingleValue (true);
	Bitstring rightIsPresent = new BitstringWithSingleValue (true);

	Bitstring predResult = rightAtts0.equals (leftAtts0).not ();
	System.out.println(predResult.print ());
        predResult = predResult.and (leftAtts4.equals ((new StringAttribute ("Sawmill Mountain")))).not ();
	System.out.println(predResult.print ());
	predResult = predResult.and (leftIsPresent).and (rightIsPresent);
	System.out.println(predResult.print ());
        Bitstring antiPred = predResult.theseAreTrue ().not ();
	System.out.println(antiPred.print ());
        System.out.println(!antiPred.allAreFalseOrUnknown ());

  }

  public static final BitstringUnknown UNKNOWN = new BitstringUnknown();
  public BitstringUnknown () {}
  
  public String print () {
    return "<all U>"; 
  }
  
  public int writeSelfToStream (DataOutputStream writeToMe) {
    // do nothing
    return 0;
  }
  
  public boolean getValue (int whichMCIter) {
    throw new RuntimeException ("this guy has an unknown value!!");  
  }
  
  public int readSelfFromStream (DataInputStream readFromMe) {
    // do nothing
    return 0;
  }
  
  public boolean allAreTrue () {
    return false;
  }
  
  public Attribute toInt () {
    return NullAttribute.NULL;
  }
  
  public double fractionOfTrue () {
    return 0.0;  
  }
    
  public boolean allAreFalseOrUnknown () {
    return true;
  }
  
  public Bitstring isUnknown () {
    return BitstringWithSingleValue.TRUE;
  }
  
  public Bitstring setUnknown (Bitstring setThese) {
    return this;
  }
  
  public Bitstring not () {
    return this;
  }
  
  public Bitstring theseAreTrue () {
    return BitstringWithSingleValue.FALSE;
  }

  public Bitstring and (Bitstring withMe) {
    return withMe.setUnknown (withMe.theseAreTrue ());
  }
  
  public Bitstring or (Bitstring withMe) {
    return withMe.setUnknown (withMe.theseAreTrue ().not ());
  }
  
  public Bitstring xor (Bitstring withMe) {
    return this;
  }
  
  public Bitstring equals (Bitstring withMe) {
    return this;
  }
  
  public Bitstring and (boolean withMe) {
    if (!withMe)
      return BitstringWithSingleValue.FALSE;
    return this;
  }
  
  public Bitstring or (boolean withMe) {
    if (withMe)
      return BitstringWithSingleValue.TRUE;
    return this;
  }
  
  public Bitstring xor (boolean withMe) {
    return this;
  }
  
  public Bitstring equals (boolean withMe) {
    return this;
  }
  
  public Bitstring and (boolean [] withMe) {
    return this;
  }
  
  public Bitstring or (boolean [] withMe) {
    return this;
  }
  
  public Bitstring xor (boolean [] withMe) {
    return this;
  }
  
  public Bitstring equals (boolean [] withMe) {
    return this;
  }
  
}
