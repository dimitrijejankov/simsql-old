

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
 * This is a simple boolean with the same value in 
 * every world.
 */

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BitstringWithSingleValue implements Bitstring {

  // this is the value stored in the bitstring
  private boolean myVal;

  public static final BitstringWithSingleValue TRUE = new BitstringWithSingleValue(true);
  public static final BitstringWithSingleValue FALSE = new BitstringWithSingleValue(false);
  
  public BitstringWithSingleValue () {}
  
  public boolean getValue (int whichMC) {
    return myVal;  
  }
  
  public Bitstring theseAreTrue () {
    return this;
  }

  public String print () {
    if (myVal)
      return "<all T>";
    else
      return "<all F>";
  }
  
  public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {
    
    // first, write the length
    writeToMe.writeBoolean (myVal);
    return 1;
  }
  
  public int readSelfFromStream (DataInputStream readFromMe) throws IOException {
    
    // read the value
    myVal = readFromMe.readBoolean ();
    return 1;
  }

  public Attribute toInt () {
      return myVal ? IntAttribute.ONE : IntAttribute.ZERO; 
  }  
  
  public double fractionOfTrue () {
	if (myVal == false)
	  return 0.0;
	else
	  return 1.0;  
  }
  
  public BitstringWithSingleValue (boolean useMe) {
    myVal = useMe;
  }
  
  public boolean allAreFalseOrUnknown () {
    return !myVal;  
  }
  
  public boolean allAreTrue () {
    return myVal;
  }
  
  public Bitstring setUnknown (Bitstring setThese) {
    return new BitstringWithSomeUnknown (this, setThese);
  }
  
  public Bitstring isUnknown () {
    return BitstringWithSingleValue.FALSE;
  }
  
  public static Bitstring trueIf(boolean expr) {
    return expr ? BitstringWithSingleValue.TRUE : BitstringWithSingleValue.FALSE;
  }

  public Bitstring not () {
    return trueIf(!myVal);
  }
  
  public Bitstring and (Bitstring withMe) {
    return withMe.and (myVal);
  }
  
  public Bitstring or (Bitstring withMe) {
    return withMe.or (myVal);
  }
  
  public Bitstring xor (Bitstring withMe) {
    return withMe.xor (myVal);
  }
  
  public Bitstring equals (Bitstring withMe) {
    return withMe.equals (myVal);
  }
  
  public Bitstring and (boolean withMe) {
    return trueIf(myVal && withMe);
  }
  
  public Bitstring or (boolean withMe) {
    return trueIf(myVal || withMe);
  }
  
  public Bitstring xor (boolean withMe) {
    return trueIf((myVal || withMe) && !(myVal && withMe));
  }
  
  public Bitstring equals (boolean withMe) {
    return trueIf(myVal == withMe);
  }
  
  public Bitstring and (boolean [] withMe) {

    if (!myVal)
      return BitstringWithSingleValue.FALSE;

    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = myVal && withMe[i];
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring or (boolean [] withMe) {

    if (myVal)
      return BitstringWithSingleValue.TRUE;

    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = myVal || withMe[i];
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring xor (boolean [] withMe) {
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = (myVal || withMe[i]) && !(myVal && withMe[i]);
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring equals (boolean [] withMe) {
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = (myVal == withMe[i]);
    }
    return new BitstringWithArray (retVal);
  }
  
}
