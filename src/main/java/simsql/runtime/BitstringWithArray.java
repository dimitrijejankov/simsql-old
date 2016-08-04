

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
 * This class implements the bitstring, allowing for each possible world 
 * to have a different boolean value
 */
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BitstringWithArray implements Bitstring {
  
  // this is the value stored in the bitstring
  private boolean [] myVal;
  
  public boolean getValue (int whichIter) {
    return myVal[whichIter];  
  }
  
  public Bitstring theseAreTrue () {
    return this;
  }

  public BitstringWithArray () {}
  
  public String print () {
    String res = "<";
    for (int i = 0; i < myVal.length; i++) {
      if (myVal[i] == false) {
        res += "F";
      } else {
        res += "T";
      }
    }
    return res + ">";
  }
  
  // writes a boolean array to the output stream
  private int writeBooleans (DataOutputStream out) throws IOException {
    int returnVal = 0;
    for (int i = 0; i < myVal.length; i += 8) {
      int b = 0;
      for (int j = Math.min (i + 7, myVal.length-1); j >= i; j--) {
        b = (b << 1) | (myVal[j] ? 1 : 0);
      }
      returnVal++;
      out.write(b);
    }
    return returnVal;
  }
  
  // reads a boolean array from an output stream
  private int readBooleans (DataInputStream in) throws IOException {
    int returnVal = 0;
    for (int i = 0; i < myVal.length; i += 8) {
      returnVal++;
      int b = in.read ();
      for (int j = i; j < i + 8 && j < myVal.length; j++) {
        myVal[j] = (b & 1) != 0;
        b >>>= 1;
      }
    }
    return returnVal;
  }
  
  public Attribute toInt () {
    long [] returnVal = new long [myVal.length];
    for (int i = 0; i < myVal.length; i++) {
      if (myVal[i] == false)
        returnVal[i] = 0;
      else
        returnVal[i] = 1;
    }
    return new IntArrayAttribute (returnVal);
  }  
  
  public double fractionOfTrue () {
	double sum = 0.0;
	for (int i = 0; i < myVal.length; i++)
		if (myVal[i] == true)
			sum += 1.0;
	return sum / myVal.length;
  }
  
  public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {
    
    // first, write the length
    writeToMe.writeInt (myVal.length);
    
    // and write the data
    return 4 + writeBooleans (writeToMe);
  }
  
  public int readSelfFromStream (DataInputStream readFromMe) throws IOException {
    
    // first read the length
    int arrayLen;
    arrayLen = readFromMe.readInt ();
    
    // allocate the space
    myVal = new boolean [arrayLen];
    
    // and read the data
    return 4 + readBooleans (readFromMe);
  }
  
  // create a bitstring with the indicated value
  public BitstringWithArray (boolean [] inVal) {
    myVal = inVal;
  }
    
  public boolean allAreTrue () {
    for (int i = 0; i < myVal.length; i++) {
      if (myVal[i] == false) {
        return false;
      }
    }
    return true;
  }
  
  public boolean allAreFalseOrUnknown () {
    for (int i = 0; i < myVal.length; i++) {
      if (myVal[i] == true) {
        return false;
      }
    }
    return true;
  }
  
  public Bitstring setUnknown (Bitstring setThese) {
    return new BitstringWithSomeUnknown (this, setThese);
  }


  public Bitstring isUnknown () {
      return BitstringWithSingleValue.FALSE;
  }
  
  public Bitstring not () {
    boolean [] retVal = new boolean [myVal.length];
    for (int i = 0; i < myVal.length; i++) {
      retVal[i] = !myVal[i];
    }
    return new BitstringWithArray (retVal); 
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

    if (!withMe)
      return BitstringWithSingleValue.FALSE;

    return this;
  }
  
  public Bitstring or (boolean withMe) {

    if (!withMe)
      return this;

    return BitstringWithSingleValue.TRUE;
  }
  
  public Bitstring xor (boolean withMe) {
    boolean [] retVal = new boolean [myVal.length];
    for (int i = 0; i < myVal.length; i++) {
      retVal[i] = (myVal[i] || withMe) && !(myVal[i] && withMe);
    }
    return new BitstringWithArray (retVal); 
  }
  
  public Bitstring equals (boolean withMe) {
    boolean [] retVal = new boolean [myVal.length];
    for (int i = 0; i < myVal.length; i++) {
      retVal[i] = (myVal[i] == withMe);
    }
    return new BitstringWithArray (retVal); 
  }
  
  public Bitstring and (boolean [] withMe) {
    if (withMe.length != myVal.length)
      throw new RuntimeException ("can't do a boolean operation on two arrays of different lengths.");
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = myVal[i] && withMe[i];
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring or (boolean [] withMe) {
    if (withMe.length != myVal.length)
      throw new RuntimeException ("can't do a boolean operation on two arrays of different lengths.");
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = myVal[i] || withMe[i];
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring xor (boolean [] withMe) {
    if (withMe.length != myVal.length)
      throw new RuntimeException ("can't do a boolean operation on two arrays of different lengths.");
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = (myVal[i] || withMe[i]) && !(myVal[i] && withMe[i]);
    }
    return new BitstringWithArray (retVal);
  }
  
  public Bitstring equals (boolean [] withMe) {
    if (withMe.length != myVal.length)
      throw new RuntimeException ("can't do a boolean operation on two arrays of different lengths.");
    boolean [] retVal = new boolean [withMe.length];
    for (int i = 0; i < withMe.length; i++) {
      retVal[i] = (myVal[i] == withMe[i]);
    }
    return new BitstringWithArray (retVal);
  }
}
