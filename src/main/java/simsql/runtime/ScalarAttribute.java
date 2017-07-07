
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
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.BufferedReader;

public class ScalarAttribute extends AbstractDoubleAttribute {
	// private double myVal;
	// private int myLabel = -1;
	
	// tries to return a recycled instance.
	  public static ScalarAttribute getInstance(int label, double val) {	    
		 
		// We will not use the cycle pool at this point
		/*
	    if (poolSize == 0) {
	      return new DoubleAttribute(val);
	    }

	    poolSize --;
	    DoubleAttribute W = pool[poolSize];
	    pool[poolSize] = null;

	    W.myVal = val;
	    W.recycled = false;
	    return W;
	    */
		return new ScalarAttribute(label, val);
	  }

	  /*
	  private boolean recycled = false;
	  public void recycle() {
	    if (recycled || poolSize >= pool.length || this == DoubleAttribute.ONE || this == DoubleAttribute.ZERO)
	      return;

	    recycled = true;
	    pool[poolSize] = this;
	    poolSize ++;
	  }
	  */
	  
	  public void recycle () {
		  
	  }

	  
	  // this allows us to return the data as a byte array
	  private static ByteBuffer b = ByteBuffer.allocate (8);
	  static {
	    b.order(ByteOrder.nativeOrder());
	  }

	  public ScalarAttribute () {}
	  
	  public long writeSelfToStream (DataOutputStream writeToMe) throws IOException {
		writeToMe.writeInt(getLabel());
	    writeToMe.writeDouble (getVal());
	    return 12;
	  }
	  
	  // currently same with DoubleAttribute
	  /*
	  public void writeSelfToTextStream (BufferedWriter writeToMe) throws IOException {
	    String temp = Double.toString (myVal);
	    writeToMe.write (temp, 0, temp.length ());
	    writeToMe.write ("|");
	  }
	  */

	  // currently same with DoubleAttribute
	  /*
	  public void writeSelfToTextStream (Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {
	    if (theseAreNull.injectValue (0)) {
	      writeToMe.write ("null", 0, 4);
	    } else {
	      String temp = Double.toString (myVal);
	      writeToMe.write (temp, 0, temp.length ());
	    }
	    writeToMe.write ("|");
	  }
	  */

	  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {
	    
	    // check if there is an array, signified by a leaing '<'
	    readFromMe.mark (1);
	    int firstChar = readFromMe.read ();
	    while (firstChar == '\n')
	      firstChar = readFromMe.read ();

	    
	    if (firstChar == '<') {
	      //readFromMe.reset ();
	      //return new ScalarArrayAttribute ().readSelfFromTextStream (readFromMe);
	    	throw new RuntimeException ("we do not have scalar array attribute yet");

	    // see if we hit an EOF
	    } else if (firstChar < 0) {
	      return null;
	    }
	    

	    // there was not a reading '<', so read in the single label and double
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
	        return new ScalarAttribute (Double.valueOf (new String (myArray, 0, i - 1)));
	      } catch (Exception e) {
	        throw new IOException ("Error when I tried to read in a Scalar... didn't parse to a double");
	      }
	    }
	  }

	  public long readSelfFromStream (DataInputStream readFromMe)  throws IOException {
		setLabel (readFromMe.readInt ());
		setVal (readFromMe.readDouble ());
	    return 12;
	  }
	    
	  public byte [] injectValue(int whichMC) {
	    b.putDouble (0, getVal());
	    return b.array ();
	  }

	  public void injectIntoBuffer(int whichMC, AttributeType castTo, LargeByteBuffer buffer) {
	    if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) {
			buffer.putDouble (getVal());
		}
	    else if (castTo.getTypeCode() == TypeCode.INT) {
			buffer.putLong((long)getVal());
	    }
	    else if (castTo.getTypeCode() == TypeCode.DOUBLE) {
			buffer.putDouble(getVal());
	    }

	    else throw new RuntimeException("Invalid cast when writing out value.");
	  }
	  
	  public byte [] getAll (int whichMC) {
		    b.putInt(0, getLabel());
		    b.putDouble (getVal());
		    return b.array ();
	  }

	  public long getHashCode () {
	    return Hash.hashMe (injectValue(0));
	  }
	    
	  public AttributeType getType (int whichMC) {
	    return new AttributeType(new ScalarType());  
	  }

	  public HashMap<Attribute, Bitstring> split () {
	  	HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
	  	splits.put(this, BitstringWithSingleValue.trueIf(true));
	  	return splits;
	  }
	  
	  public ScalarAttribute (double fromMe) {
	    setVal (fromMe);
	  }
	  
	  public ScalarAttribute (int label, double fromMe) {
		setLabel (label);
		setVal (fromMe);
	  }
	  
	  public Attribute multiply (long byMe) {

	    if (byMe == 0)
	      return DoubleAttribute.ZERO;

	    //    return new DoubleAttribute (myVal * byMe);
	    return DoubleAttribute.getInstance (getVal() * byMe);
	  }
	  
	  public Attribute multiply (double byMe) {

	    if (byMe == 0.0)
	      return DoubleAttribute.ZERO;

	    //    return new DoubleAttribute (myVal * byMe);
	    return DoubleAttribute.getInstance (getVal() * byMe);
	  }	  
	  
	  public Attribute divide (long byMe) {

	    return DoubleAttribute.getInstance (getVal() / byMe);
	  }
	  	  
	  public Attribute divide (double byMe) {

	    if (getVal() == 0.0)
	      return DoubleAttribute.ZERO;

	    //    return new DoubleAttribute (myVal / byMe); 
	    return DoubleAttribute.getInstance (getVal() / byMe); 
	  }
	  	  
	  public Bitstring equals (String me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a String and a Scalar?");
	  }
	  	  
	  public Bitstring equals (String [] me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a String and a Scalar?");
	  }

	  public Bitstring notEqual (String me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a String and a Scalar?");
	  }
	  
	  public Bitstring notEqual (String [] me)  {
	    throw new RuntimeException ("Why are you doing an equality check on a String and a Scalar?");
	  }
	  
	  public Bitstring lessThan (String me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring lessThan (String [] me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring greaterThan (String me)  {
	    throw new RuntimeException ("Why are you doinga comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring greaterThan (String [] me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring lessThanOrEqual (String me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring lessThanOrEqual (String [] me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring greaterThanOrEqual (String me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }
	  
	  public Bitstring greaterThanOrEqual (String [] me)  {
	    throw new RuntimeException ("Why are you doing a comparison on a String and a Scalar?");
	  }

	  public void injectSelf(Function f) {
		f.inject(getVal(), getLabel());
	    //f.inject(myVal);
	  }

	  public String print(int maxLen) {
		  String s = "";
		  s += "Label: ";
		  s += String.format("%d", getLabel());
		  s += ", ";
		  s += "Value: ";
	      s += String.format("%.2f", getVal());
	      if (s.length() > maxLen && maxLen > 4) {
		  return s.substring(0, maxLen-4) + "...";
	      }

	      return s;
	  }

}
