

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
  * Implements the integer type, allowing for different
  * values in every world
  */
import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.FileReader;

public class IntArrayAttribute implements Attribute {

  // the actual data
  private long [] myVals;
  
  // this allows us to return the data as a byte array
  private static ByteBuffer b = ByteBuffer.allocate (8);
  static {
    b.order(ByteOrder.nativeOrder());
  }

  public void recycle() {
  }

  public Bitstring isNull () {
    return BitstringWithSingleValue.FALSE;
  }

  public Attribute removeNulls () {
    return this;  
  }

  public int getSize () {
  	return myVals.length;
  }
  
  public byte [] getValue (int whichMC) {
    b.putLong (0, myVals[whichMC]);
    return b.array ();
  }

  public byte [] getValue (int whichMC, AttributeType castTo) {
    if (castTo.getTypeCode() == getType(whichMC).getTypeCode())
      return getValue(whichMC);

    if (castTo.getTypeCode() == TypeCode.DOUBLE) {
      b.putDouble(0, (double)myVals[whichMC]);
      return b.array();
    }

    else throw new RuntimeException("Invalid cast when writing out value.");
  }
  
  public AttributeType getType (int whichMC) {
    return new AttributeType(new IntType());  
  }

  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {

    // this is the space we'll use to do the parsing... we assume it is less than 64 chars
    char [] myArray = new char[64];

    // allows us to match the word "null"
    char [] nullString = {'n', 'u', 'l', 'l'};

    // records the ints and nulls we've read in
    ArrayList <Long> myIntegers = new ArrayList <Long> (64);
    ArrayList <Boolean> myNulls = new ArrayList <Boolean> (64);

    // suck in the '<', but ignore a leading newline character
    int startChar = readFromMe.read ();
    while (startChar == '\n')
      startChar = readFromMe.read ();

    // if we got an eof, we are done
    if (startChar == -1) {
      return null;
    }

    if (startChar != '<') {
      throw new IOException ("Read in a bad array start character when reading an Int  array; expected '<'");
    }

    // this tells us we did not see a null anywhere in the input
    boolean gotANull = false;

    // read in the first char
    myArray[0] = (char) readFromMe.read ();

    // keep reading until we find the '>'
    while (myArray[0] != '>') {

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
        myIntegers.add ((long) 0);
        myNulls.add (true);
        gotANull = true;

      } else {
        // we did not get a null!
        try {
          myIntegers.add (Long.valueOf (new String (myArray, 0, i - 1)));
          myNulls.add (false);
        } catch (Exception e) {
          throw new IOException ("Error when I tried to read in a Integer array... an entry didn't parse to a int");
        }
      }

      // prime the parse of the next item in the array
      myArray[0] = (char) readFromMe.read ();
    }

    // suck in the final '|'
    if (readFromMe.read () != '|') {
      throw new IOException ("Error when I tried to read in a Integer array: didn't close with a '|'");
    }

    // at this point we've read the entire array, so make an attribute out of it
    long [] myIntegerArray = new long[myIntegers.size ()];
    for (int i = 0; i < myIntegers.size (); i++) {
      myIntegerArray[i] = myIntegers.get (i);
    }
    Attribute returnVal = new IntArrayAttribute (myIntegerArray);

    // return the final result
    if (gotANull == true) {
      boolean [] myBoolArray = new boolean[myNulls.size ()];
      for (int i = 0; i < myNulls.size (); i++) {
        myBoolArray[i] = myNulls.get (i);
      }
      return new ArrayAttributeWithNulls (new BitstringWithArray (myBoolArray), returnVal);
    } else {
      return returnVal;
    }
  }

  public void writeSelfToTextStream (BufferedWriter writeToMe) throws IOException {

    // easy: just loop through and print out everything in the aray
    writeToMe.write ("<");
    for (int i = 0; i < myVals.length; i++) {
      String temp = Long.toString (myVals[i]);
      writeToMe.write (temp, 0, temp.length ());
      writeToMe.write ("|");
    }
    writeToMe.write (">");
    writeToMe.write ("|");
  }

  public void writeSelfToTextStream (Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

    // just loop through and print everything in the array, or null if testAreNull indicates a null in a position
    writeToMe.write ("<");
    for (int i = 0; i < myVals.length; i++) {
      if (theseAreNull.getValue (i)) {
        writeToMe.write ("null", 0, 4);
      } else {
        String temp = Long.toString (myVals[i]);
        writeToMe.write (temp, 0, temp.length ());
      }
      writeToMe.write ("|");
    }
    writeToMe.write (">");
    writeToMe.write ("|");

  }

  public long getHashCode () {
    throw new RuntimeException ("Can't hash an array attribute!");  
  }  
  
  public IntArrayAttribute () {}
  
  public int writeSelfToStream (DataOutputStream writeToMe)  throws IOException  {
    int returnVal = 4;
    writeToMe.writeInt (myVals.length);
    for (int i = 0; i < myVals.length; i++) {
      writeToMe.writeLong (myVals[i]);
      returnVal += 8;
    }
    return returnVal;
  }
  
  public int readSelfFromStream (DataInputStream readFromMe)  throws IOException {
    int returnVal = 4;
    int len = readFromMe.readInt ();
    myVals = new long[len];
    for (int i = 0; i < myVals.length; i++) {
      myVals[i] = readFromMe.readLong ();
      returnVal += 8;
    }
    return returnVal;
  }
  
  public Attribute setNull (Bitstring theseOnes) {
    if (theseOnes.allAreTrue ()) {
      return NullAttribute.NULL;
    } else {
      return new ArrayAttributeWithNulls (theseOnes, this);
    }
  }

  public HashMap<Attribute, Bitstring> split () {
  	HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
  	List<Long> list = new ArrayList<Long>();
  	for (int i = 0; i < myVals.length; i++)
  		list.add(myVals[i]);
	Set<Long> set = new HashSet<Long>(list);
	for (Long val : set) {
		boolean [] resArray = new boolean [myVals.length];
	  	for (int i = 0; i < myVals.length; i++) {
	  	  resArray[i] = (myVals[i] == val.longValue()); 
	    }
	  	splits.put(new IntAttribute(val.longValue()), new BitstringWithArray(resArray));
    }
  	return splits;
  }
  
  public IntArrayAttribute (long [] fromMe) {
    myVals = fromMe;
  }
  
  public Attribute add (Attribute me) {
    return me.addR (myVals);
  }
  
  public Attribute add (long addThisIn) {

    if (addThisIn == 0)
      return this;
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn;
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute addR (long addThisIn) {
    
    // because integer arithmatic is commutative
    if (addThisIn == 0)
      return this;

    return add (addThisIn);
  }
  
  public Attribute add (long [] addThisIn) {
    
    if (addThisIn.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn[i];
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute addR (long [] addThisIn) {
    
    // because ordering does not matter with addition of ints
    return add (addThisIn);
  }
  
  public Attribute add (String [] addThisIn) {
    
    if (addThisIn.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    String [] newArray  = new String [myVals.length];
    
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn[i];
    }
    
    // and get outta here!
    return new StringArrayAttribute (newArray); 
  }
  
  public Attribute addR (String [] addThisIn) {
    if (addThisIn.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    String [] newArray  = new String [myVals.length];
    
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = addThisIn[i] + myVals[i];
    }
    
    // and get outta here!
    return new StringArrayAttribute (newArray); 
  }
  
  public Attribute add (String addThisIn) {
    
    // create the new array
    String [] newArray  = new String [myVals.length];
    
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn;
    }
    
    // and get outta here!
    return new StringArrayAttribute (newArray); 
  }
  
  public Attribute addR (String addThisIn) {
    
    // create the new array
    String [] newArray  = new String [myVals.length];
    
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = addThisIn + myVals[i];
    }
    
    // and get outta here!
    return new StringArrayAttribute (newArray); 
  }
  
  public Attribute add (double [] addThisIn) {
    
    if (addThisIn.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute addR (double [] addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (double addThisIn) {
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn;
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute addR (double addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (int label, double addThisIn) {
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn;
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute addR (int label, double addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (int label, double [] addThisIn) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Attribute addR (int lable, double [] addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (boolean ifRow, double [][] addThisIn) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Attribute addR (boolean ifRow, double [][] addThisIn) {
    
    // since addition on numbers is commutative
    return add (ifRow, addThisIn);
  }
  
 public Attribute subtract (Attribute me) {
    return me.subtractR (myVals);
  }
  
  public Attribute subtract (long subtractThisOut) {

    if (subtractThisOut == 0)
      return this;
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] - subtractThisOut;
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute subtractR (long subtractFromMe) {
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = subtractFromMe - myVals[i];
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute subtract (long [] subtractMeOut) {
    
    if (subtractMeOut.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] - subtractMeOut[i];
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute subtractR (long [] subtractFromMe) {
    
    if (subtractFromMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = subtractFromMe[i] - myVals[i];
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute subtract (double [] subtractThisOut) {
    
    if (subtractThisOut.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] - subtractThisOut[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute subtractR (double [] subtractFromMe) {
    
    if (subtractFromMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = subtractFromMe[i] - myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute subtract (double subtractThisOut) {
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] - subtractThisOut;
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute subtractR (double subtractFromThis) {
    
   double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = subtractFromThis - myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute subtract (int label, double subtractThisOut) {
	return subtract (subtractThisOut);
  }
  
  public Attribute subtractR (int label, double subtractFromThis) {
	return subtractR (subtractFromThis); 
  }
  
  public Attribute subtract (int label, double [] subtractThisOut) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Attribute subtractR (int label, double [] subtractFromThis) {
	throw new RuntimeException("This method is not defined!"); 
  }
  
  public Attribute subtract (boolean ifRow, double [][] subtractThisOut) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Attribute subtractR (boolean ifRow, double [][] subtractFromThis) {
	throw new RuntimeException("This method is not defined!"); 
  }
	  
  public Attribute multiply (Attribute byMe) {
     return byMe.multiply (myVals);
  }
  
  public Attribute multiply (long byMe) {

    if (byMe == 1)
      return this;

    if (byMe == 0)
      return IntAttribute.ZERO;
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] * byMe;
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute multiply (double byMe) {

    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] * byMe;
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
  }
  
  public Attribute multiply (long [] byMe) {
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] * byMe[i];
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute multiply (double [] byMe) {
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] * byMe[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
  }
  
  public Attribute multiply (int label, double byMe) {
	return multiply (byMe);
  }
  
  public Attribute multiply (int label, double [] byMe) {
	throw new RuntimeException("This method is not defined!"); 
  }
  
  public Attribute multiply (boolean ifRow, double [][] byMe) {
	throw new RuntimeException("This method is not defined!"); 
  }
  
   public Attribute divide (Attribute byMe) {
    return byMe.divideR (myVals);
  }
  
  public Attribute divide (long byMe) {

    if (byMe == 1)
      return this;

    // division by zero -> null
    if (byMe == 0)
      return NullAttribute.NULL;

    // create the new array
    long [] newArray  = new long [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] / byMe;
    }
    
    // and get outta here!
    return new IntArrayAttribute (newArray);
  }
  
  public Attribute divideR (long divideMe) {

    if (divideMe == 0)
      return IntAttribute.ZERO;
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    boolean [] nulls = new boolean[myVals.length];
    boolean haveNulls = false;
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      nulls[i] = (myVals[i] == 0);
      haveNulls |= nulls[i];
      newArray[i] = nulls[i] ? 0 : (divideMe / myVals[i]);
    }
    
    // and get outta here!
    if (haveNulls) {
      return new ArrayAttributeWithNulls(new BitstringWithArray(nulls), new IntArrayAttribute(newArray));
    }
    else {
      return new IntArrayAttribute (newArray);
    }
  }
  
  public Attribute divide (long [] byMe) {
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("dividing an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    boolean [] nulls = new boolean[byMe.length];
    boolean haveNulls = false;
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      nulls[i] = (byMe[i] == 0);
      haveNulls |= nulls[i];
      newArray[i] = nulls[i] ? 0 : (myVals[i] / byMe[i]);
    }

    // and get outta here!
    if (haveNulls) {
      return new ArrayAttributeWithNulls(new BitstringWithArray(nulls), new IntArrayAttribute(newArray));
    }
    else {
      return new IntArrayAttribute (newArray);
    } 
  }
  
  public Attribute divideR (long [] divideMe) {
    
    if (divideMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    long [] newArray  = new long [myVals.length];
    boolean [] nulls = new boolean[myVals.length];
    boolean haveNulls = false;
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      nulls[i] = (myVals[i] == 0);
      haveNulls |= nulls[i];
      newArray[i] = nulls[i] ? 0 : (divideMe[i] / myVals[i]);
    }
    
    // and get outta here!
    if (haveNulls) {
      return new ArrayAttributeWithNulls(new BitstringWithArray(nulls), new IntArrayAttribute(newArray));
    }
    else {
      return new IntArrayAttribute (newArray);
    } 
  }
  
  public Attribute divide (double [] byMe) {
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] / byMe[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute divideR (double [] divideMe) {
    
    if (divideMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = divideMe[i] / myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute divide (double byMe) {
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] / byMe;
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute divideR (double divideMe) {

    if (divideMe == 0.0)
      return DoubleAttribute.ZERO;
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = divideMe / myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
  }
  
  public Attribute divide (int label, double byMe) {
	return divide (byMe);
  }
	  
  public Attribute divideR (int label, double divideMe) {
	return divideR (divideMe);
  }
  
  public Attribute divide (int label, double [] byMe) {
	throw new RuntimeException("This method is not defined!");
  }
	  
  public Attribute divideR (int label, double [] divideMe) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Attribute divide (boolean ifRow, double [][] byMe) {
	throw new RuntimeException("This method is not defined!");
  }
	  
  public Attribute divideR (boolean ifRow, double [][] divideMe) {
	throw new RuntimeException("This method is not defined!");
  }
  
  public Bitstring equals (Attribute me) {
    return me.equals (myVals);
  }
  
  public Bitstring equals (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] == me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring equals (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] == me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring equals (String me)  {
    throw new RuntimeException ("Why are you doing an equality check on a String and an int?");
  }
  
  public Bitstring equals (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] == me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring equals (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] == me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring equals (String [] me)  {
    throw new RuntimeException ("Why are you doing an equality check on a String and an int?");
  }
  
  public Bitstring equals (int label, double me)  {
	return equals (me);
  }
  
  public Bitstring equals (int label, double [] me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a vector and an int?");
  }
  
  public Bitstring equals (boolean ifRow, double [][] me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a matrix and an int?");
  }
  
  public Bitstring notEqual (Attribute me) {
    return me.notEqual (myVals);
  }
  
  public Bitstring notEqual (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] != me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring notEqual (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] != me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring notEqual (String me)  {
    throw new RuntimeException ("Why are you doing an equality check on a String and an int?");
  }
  
  public Bitstring notEqual (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] != me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring notEqual (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] != me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring notEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing an equality check on a String and an int?");
  }
  
  public Bitstring notEqual (int label, double me)  {
	return notEqual (me);
  }
  
  public Bitstring notEqual (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a vector and an int?");
  }
  
  public Bitstring notEqual (boolean ifRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and an int?");
  }
  
  public Bitstring lessThan (Attribute me) {
    return me.greaterThan (myVals);
  }
  
  public Bitstring lessThan (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] < me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThan (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] < me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThan (String me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring lessThan (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] < me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThan (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] < me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThan (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring lessThan (int label, double me)  {
	return lessThan (me);
  }
  
  public Bitstring lessThan (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison on a vector and an int?");
  }
  
  public Bitstring lessThan (boolean ifRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison on a matrix and an int?");
  }
  
  public Bitstring greaterThan (Attribute me) {
    return me.lessThan (myVals);
  }
  
  public Bitstring greaterThan (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] > me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThan (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] > me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThan (String me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring greaterThan (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] > me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThan (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] > me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThan (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring greaterThan (int label, double me)  {
	return greaterThan (me);
  }
  
  public Bitstring greaterThan (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison on a vector and an int?");
  }
  
  public Bitstring greaterThan (boolean label, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison on a matrix and an int?");
  }
  
  public Bitstring lessThanOrEqual (Attribute me) {
    return me.greaterThanOrEqual (myVals);
  }
  
  public Bitstring lessThanOrEqual (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] <= me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThanOrEqual (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] <= me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThanOrEqual (String me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring lessThanOrEqual (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] <= me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThanOrEqual (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] <= me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring lessThanOrEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring lessThanOrEqual (int label, double me)  {
	return lessThanOrEqual (me);
  }
  
  public Bitstring lessThanOrEqual (int label, double [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a vector and an int?");
  }
  
  public Bitstring lessThanOrEqual (boolean ifRow, double [][] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a matrix and an int?");
  }
  
  public Bitstring greaterThanOrEqual (Attribute me) {
    return me.lessThanOrEqual (myVals);
  }
  
  public Bitstring greaterThanOrEqual (long me) {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] >= me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThanOrEqual (double me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] >= me); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThanOrEqual (String me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring greaterThanOrEqual (long [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] >= me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThanOrEqual (double [] me)  {
    boolean [] resArray = new boolean [myVals.length];
    for (int i = 0; i < myVals.length; i++) {
      resArray[i] = (myVals[i] >= me[i]); 
    }
    return new BitstringWithArray (resArray);
  }
  
  public Bitstring greaterThanOrEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a String and an int?");
  }
  
  public Bitstring greaterThanOrEqual (int label, double me)  {
	return greaterThanOrEqual (me);
  }
  
  public Bitstring greaterThanOrEqual (int label, double [] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a vector and an int?");
  }
  
  public Bitstring greaterThanOrEqual (boolean ifRow, double [][] me)  {
    throw new RuntimeException ("Why are you doing a comparison on a matrix and an int?");
  }

  public void injectSelf(Function f) {
    f.inject(myVals);
  }

  public boolean allAreEqual() {

    for (int i=1;i<myVals.length;i++) {
      if (myVals[i] != myVals[0])
	return false;
    }
    
    return true;
  }

  public String print(int maxLen) {
      return print(maxLen, BitstringWithSingleValue.FALSE);
   }

  public String print(int maxLen, Bitstring theseAreNull) {
      String ret = "";
      for (int i=0;i<myVals.length;i++) {
	  if (i > 0) {
	      ret += ", ";
	  }

	  if (theseAreNull.getValue(i)) {
	      ret += "null";
	  } 
	  else {
	      ret += String.format("%d", myVals[i]);
	  }

	  if (ret.length() > maxLen && maxLen > 4) {
	      return ret.substring(0, maxLen-4) + "...";
	  }
      }

      return ret;
  }

    
  public Attribute getSingleton() {
    return new IntAttribute(myVals[0]);
  }

}
