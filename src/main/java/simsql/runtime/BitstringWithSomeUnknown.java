

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

public class BitstringWithSomeUnknown implements Bitstring {
  
  // these are the actual bits that indicate if our value is true or false
  private Bitstring myVals;
  
  // these indicate whether or not the values in each dimension are unknown
  private Bitstring theseAreUnknown;
  
  // this is the machine to read/write the above two members
  static BitstringIOMachine bitstringMachine = new BitstringIOMachine ();
    
  public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {
    int returnVal = bitstringMachine.writeToStream (myVals, writeToMe);
    return returnVal + bitstringMachine.writeToStream (theseAreUnknown, writeToMe);
  }
  
  public Attribute toInt () {
    return myVals.toInt ().multiply (theseAreUnknown.toInt ());
  }  

  public Bitstring theseAreTrue () {

        // the ones that are true are those where myVals is true, and which are not unknown
	return myVals.theseAreTrue ().and (theseAreUnknown.not ());
  }

  public double fractionOfTrue () {
	return myVals.fractionOfTrue ();  
  }
    
  public boolean getValue (int whichMC) {
    if (theseAreUnknown.getValue (whichMC))
      throw new RuntimeException ("you can't call getValue when the value is unknown!!");
    else
      return myVals.getValue (whichMC);
  }
  
  public BitstringWithSomeUnknown () {}
  
  public int readSelfFromStream (DataInputStream readFromMe) throws IOException {
    WriteableInt numBytes = new WriteableInt ();
    myVals = bitstringMachine.readFromStream (readFromMe, numBytes);
    int returnVal = (int) numBytes.get ();
    theseAreUnknown = bitstringMachine.readFromStream (readFromMe, numBytes);
    return returnVal + (int) numBytes.get ();
  }
  
  /**
   * Create a btstring that has some unknown values
   */
  public BitstringWithSomeUnknown (Bitstring useMe, Bitstring unknowns) {
    myVals = useMe;
    theseAreUnknown = unknowns;
  }
  
  public String print () {
    String returnVal = "<truth val: " + myVals.print () + "; unknown: " + theseAreUnknown.print () + ">";
    return returnVal;
  }
  
  public boolean allAreTrue () {
    return myVals.and (theseAreUnknown.not ()).allAreTrue ();
  }
  
  public boolean allAreFalseOrUnknown () {
    return myVals.not ().or (theseAreUnknown).allAreTrue ();
  }
  
  public Bitstring isUnknown () {
    return theseAreUnknown.or (myVals.isUnknown ());
  }
  
  public Bitstring setUnknown (Bitstring setThese) {
    return new BitstringWithSomeUnknown (myVals, theseAreUnknown.or (setThese));
  }
  
  public Bitstring not () {
    return myVals.not ().setUnknown (theseAreUnknown);
  }
  
  public Bitstring and (Bitstring withMe) {

    // we just do an "and", but set to an unknown if we are unknwn and the other guy is true
    return myVals.and (withMe).setUnknown (theseAreUnknown.and (withMe.theseAreTrue ()));
  }
  
  public Bitstring or (Bitstring withMe) {

    // we just do an "or", but set to an unknown if we are unknwn and the other guy is false 
    return myVals.and (withMe).setUnknown (theseAreUnknown.and (withMe.theseAreTrue ().not ()));
  }
  
  public Bitstring xor (Bitstring withMe) {
    return myVals.xor (withMe).setUnknown (theseAreUnknown);
  }
  
  public Bitstring equals (Bitstring withMe) {
    return myVals.equals (withMe).setUnknown (theseAreUnknown);
  }
  
  public Bitstring and (boolean withMe) {
    if (withMe)
	return this;
    else
	return BitstringWithSingleValue.FALSE;
  }
  
  public Bitstring or (boolean withMe) {
    if (withMe)
	return BitstringWithSingleValue.TRUE;
    else
	return this;
  }
  
  public Bitstring xor (boolean withMe) {
    return myVals.xor (withMe).setUnknown (theseAreUnknown);
  }
  
  public Bitstring equals (boolean withMe) {
    return myVals.equals (withMe).setUnknown (theseAreUnknown);
  }
  
  public Bitstring and (boolean [] withMe) {
    return and (new BitstringWithArray (withMe));
  }
  
  public Bitstring or (boolean [] withMe) {
    return or (new BitstringWithArray (withMe));
  }
  
  public Bitstring xor (boolean [] withMe) {
    return myVals.xor (withMe).setUnknown (theseAreUnknown);
  }
  
  public Bitstring equals (boolean [] withMe) {
    return myVals.equals (withMe).setUnknown (theseAreUnknown);
  }
  
}
