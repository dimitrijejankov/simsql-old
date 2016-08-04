

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

import java.util.*;
import java.io.*;
import java.nio.*;

/*
 * Encapsulates a seed. Internally, this is represented as a number,
 * but it cannot be operated on arithmetically.
 *
 * @author Luis
 */ 

public class SeedAttribute implements Attribute { 
    
    // the seed value
    private long myVal;
    
    // this allows us to return the data as a byte array
    private static ByteBuffer b = ByteBuffer.allocate (8);
    static {
	b.order(ByteOrder.nativeOrder());
    }
    
    // constructors
    public SeedAttribute() { }
    public SeedAttribute(long value) {
	myVal = value;
    }

    public void recycle() {
    }
    
    public AttributeType getType(int mcIteration) {
	return new AttributeType(new SeedType());
    }

    public int getSize () {
  	return 1;
    }
    
    public byte [] getValue (int mcIteration) {
	b.putLong(0, myVal);
	return b.array ();
    }

    public byte [] getValue (int whichMC, AttributeType castTo) {
	if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) 
	    return getValue(whichMC);
	else throw new RuntimeException("Invalid cast when writing out value.");
    }


    public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {
	writeToMe.writeLong(myVal);
	return 8;
    }
    
  public void writeSelfToTextStream (BufferedWriter writeToMe) throws IOException {
    String temp = Long.toString (myVal);
    writeToMe.write (temp, 0, temp.length ());
    writeToMe.write ("|");
  }

  public void writeSelfToTextStream (Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {
    if (theseAreNull.getValue (0)) {
      writeToMe.write ("null", 0, 4);
    } else {
      String temp = Long.toString (myVal);
      writeToMe.write (temp, 0, temp.length ());
    }
    writeToMe.write ("|");
  }

  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {

    int firstChar = readFromMe.read ();
    while (firstChar == '\n')
      firstChar = readFromMe.read ();

    // see if we hit an EOF
    if (firstChar < 0) {
      return null;
    }

    // this is the space we'll use to do the parsing... we assume it is less than 64 chars
    char [] myArray = new char[64];

    // allows us to match the word "null"
    char [] nullString = {'n', 'u', 'l', 'l'};

    // read in the first char
    myArray[0] = (char) firstChar;

    // this loop reads in until (and including) the '|'
    int i;
    boolean isNull = true;
    for (i = 1; myArray[i - 1] != '|'; i++) {

      // isNull gets set to false if we find a char that does not match the string 'null'
      if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
        isNull = false;
      }
      myArray[i] = (char) readFromMe.read ();
    }

    // if we got here, we read in a '|'
    if (isNull == true && i == 5) {

      // this means we got a null!
      return NullAttribute.NULL;

    } else {

      // we did not get a null!
      try {
        return new SeedAttribute (Long.valueOf (new String (myArray, 0, i - 1)));
      } catch (Exception e) {
        throw new IOException ("Error when I tried to read in a Int att... didn't parse to an int");
      }
    }
  }

    public int readSelfFromStream (DataInputStream readFromMe)  throws IOException {
	myVal = readFromMe.readLong ();
	return 8;
    }

    public Attribute removeNulls() {
	return this;
    }

    public Bitstring isNull () {
	return BitstringWithSingleValue.FALSE;
    }

    public Attribute setNull(Bitstring theseOnes) {
	if (theseOnes.allAreTrue ()) {
	  return NullAttribute.NULL;
	} else {
	  return new ArrayAttributeWithNulls (theseOnes, this);
	}
    }

    public long getHashCode () {
	return Hash.hashMe (getValue (0));
    }

    public HashMap<Attribute, Bitstring> split () {
  	  HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
  	  splits.put(new SeedAttribute(myVal), BitstringWithSingleValue.trueIf(true));
  	  return splits;
    }

    public Attribute add (Attribute me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (long me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (double me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (String me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (long [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (double [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute add (String [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute add (int label, double me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute add (int label, double [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute add (boolean ifRow, double [][] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (long me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (double me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (String me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (long [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (double [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute addR (String [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute addR (int label, double me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute addR (int label, double [] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute addR (boolean ifRow, double [][] me) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtract (Attribute subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtract (long subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtract (double subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtract (long [] subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtract (double [] subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtract (int label, double subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtract (int label, double [] subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtract (boolean ifRow, double [][] subtractThisOut) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtractR (long subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtractR (double subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtractR (long [] subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute subtractR (double [] subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtractR (int label, double subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtractR (int label, double [] subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute subtractR (boolean ifRow, double [][] subtractFromMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute multiply (Attribute byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute multiply (long byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute multiply (double byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute multiply (long [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute multiply (double [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute multiply (int label, double byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute multiply (int label, double [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute multiply (boolean ifRow, double [][] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divide (Attribute byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divide (long byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divide (double byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divide (long [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divide (double [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divide (int label, double byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divide (int label, double [] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divide (boolean ifRow, double [][] byMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divideR (long divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divideR (double divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divideR (long [] divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Attribute divideR (double [] divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divideR (int label, double divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divideR (int label, double [] divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }
    
    public Attribute divideR (boolean ifRow, double [][] divideMe) {
	throw new RuntimeException("The seed type does not support arithmetic operations.");
    }

    public Bitstring equals (Attribute me) {
	return me.equals (myVal);
    }
    
    public Bitstring equals (long me) {
	return BitstringWithSingleValue.trueIf (myVal == me);
    }
    
    public Bitstring equals (double me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring equals (String me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring equals (long [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring equals (double [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring equals (String [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring equals (int label, double me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring equals (int label, double [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring equals (boolean ifRow, double [][] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring notEqual (Attribute me) {
	return me.notEqual (myVal);
    }
    
    public Bitstring notEqual (long me) {
	return BitstringWithSingleValue.trueIf (myVal != me);
    }

    public Bitstring notEqual (double me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring notEqual (String me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring notEqual (long [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring notEqual (double [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring notEqual (String [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring notEqual (int label, double me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring notEqual (int label, double [] me)  {
	throw new RuntimeException("Invalid equality check.");
    }
    
    public Bitstring notEqual (boolean ifRow, double [][] me)  {
	throw new RuntimeException("Invalid equality check.");
    }

    public Bitstring greaterThan (Attribute me) {
	return me.lessThan (myVal);
    }
    
    public Bitstring greaterThan (long me) {
	return BitstringWithSingleValue.trueIf (myVal > me);
    }

    public Bitstring greaterThan (double me)  {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThan (String me)  {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThan (long [] me)  {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThan (double [] me)  {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThan (String [] me)  {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThan (int label, double me)  {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThan (int label, double [] me)  {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThan (boolean ifRow, double [][] me)  {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThan (Attribute me) {
	return me.greaterThan (myVal);
    }
    
    public Bitstring lessThan (long me) {
	return BitstringWithSingleValue.trueIf (myVal < me);
    }

    public Bitstring lessThan (double me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThan (String me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThan (long [] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThan (double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThan (String [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThan (int label, double me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThan (int label, double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThan (boolean ifRow, double [][] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThanOrEqual (Attribute me) {
	return me.lessThanOrEqual (myVal);
    }
    
    public Bitstring greaterThanOrEqual (long me) {
	return BitstringWithSingleValue.trueIf (myVal >= me);
    }
    
    public Bitstring greaterThanOrEqual (double me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThanOrEqual (String me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThanOrEqual (long [] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThanOrEqual (double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring greaterThanOrEqual (String [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThanOrEqual (int label, double me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThanOrEqual (int label, double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring greaterThanOrEqual (boolean ifRow, double [][] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThanOrEqual (Attribute me) {
	return me.greaterThanOrEqual (myVal);
    }
    
    public Bitstring lessThanOrEqual (long me) {
	return BitstringWithSingleValue.trueIf (myVal <= me);
    }

    public Bitstring lessThanOrEqual (double me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThanOrEqual (String me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThanOrEqual (long [] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public Bitstring lessThanOrEqual (double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThanOrEqual (String [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThanOrEqual (int label, double me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThanOrEqual (int label, double [] me) {
	throw new RuntimeException("Invalid comparison.");
    }
    
    public Bitstring lessThanOrEqual (boolean ifRow, double [][] me) {
	throw new RuntimeException("Invalid comparison.");
    }

    public void injectSelf(Function f) {
	throw new RuntimeException("Seed types cannot be injected into scalar functions.");
    }


    // injection into a VG function for initialization
    public void initializeVG(VGFunction f, int numIter) {
	byte[] someStuff = {(byte)numIter, (byte)(numIter >>> 8), (byte)(numIter >>> 16)};
	f.initializeSeed(myVal + Hash.hashMe(someStuff));
    }

    public boolean allAreEqual() {
	return true;
    }

  public String print(int maxLen) {
      String s = String.format("%d", myVal);
      if (s.length() > maxLen && maxLen > 4) {
	  return s.substring(0, maxLen-4) + "...";
      }

      return s;
  }

  public String print(int maxLen, Bitstring theseAreNull) {
      if (theseAreNull.getValue(0))
	  return "null";
      else
	  return print(maxLen);
  }
    
  public Attribute getSingleton() {
    return this;
  }
}
