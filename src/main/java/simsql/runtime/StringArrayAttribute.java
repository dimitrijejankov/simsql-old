

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
 * This implements the String type, and allows for different values in
 * every possible world.
 */

import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class StringArrayAttribute implements Attribute {

    private String[] myVals;

    public StringArrayAttribute() {
    }

    public Bitstring isNull() {
        return BitstringWithSingleValue.FALSE;
    }

    public void recycle() {
    }


    public Attribute setNull(Bitstring theseOnes) {
        if (theseOnes.allAreTrue()) {
            return NullAttribute.NULL;
        } else {
            return new ArrayAttributeWithNulls(theseOnes, this);
        }
    }

    public int getSize() {
        return myVals.length;
    }

    public byte[] injectValue(int whichMC) {
        try {
            return ((myVals[whichMC]) + '\000').getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Unable to convert string", e);
        }
    }

    public void injectIntoBuffer(int whichMC, AttributeType castTo, LargeByteBuffer buffer) {
        if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) {
            byte[] data = injectValue(whichMC);
            buffer.put(data, 0, data.length);
        }

        throw new RuntimeException("Invalid cast when writing out value.");
    }

    public Attribute removeNulls() {
        return this;
    }

    public long getHashCode() {
        throw new RuntimeException("Can't hash an array attribute!");
    }

    public AttributeType getType(int whichMC) {
        return new AttributeType(new StringType());
    }

    public Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException {

        // this is the space we'll use to do the parsing... we assume it is less than 64 chars
        int curLen = 64;
        char[] myArray = new char[curLen];

        // allows us to match the word "null"
        char[] nullString = {'n', 'u', 'l', 'l'};

        // records the doubles and nulls we've read in
        ArrayList<String> myStrings = new ArrayList<String>(64);
        ArrayList<Boolean> myNulls = new ArrayList<Boolean>(64);

        // suck in the '<', but ignore a leading newline character
        int startChar = readFromMe.read();
        while (startChar == '\n')
            startChar = readFromMe.read();

        // if we got an eof, we are done
        if (startChar == -1) {
            return null;
        }

        if (startChar != '<') {
            throw new IOException("Read in a bad array start character when reading a String array; expected '<'");
        }

        // this tells us we did not see a null anywhere in the input
        boolean gotANull = false;

        // read in the first char
        myArray[0] = (char) readFromMe.read();

        // keep reading until we find the '>'
        while (myArray[0] != '>') {

            // this loop reads in until (and including) the '|'
            int i;
            boolean isNull = true;
            for (i = 1; myArray[i - 1] != '|'; i++) {

                // double the size of the buffer if needed
                if (i == curLen) {
                    char[] myNewArray = new char[curLen * 2];
                    for (int j = 0; j < curLen; j++) {
                        myNewArray[j] = myArray[j];
                    }
                    myArray = myNewArray;
                    curLen *= 2;
                }

                // isNull gets set to false if we find a char that does not match the string 'null'
                if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
                    isNull = false;
                }
                myArray[i] = (char) readFromMe.read();
            }

            // if we got here, we read in a '|'
            if (isNull == true && i == 5) {

                // this means we got a null!
                myStrings.add("");
                myNulls.add(true);
                gotANull = true;

            } else {

                // we did not get a null!
                try {
                    myStrings.add(new String(myArray, 0, i - 1));
                    myNulls.add(false);
                } catch (Exception e) {
                    throw new IOException("Error when I tried to read in a String array... an entry didn't parse to a string");
                }
            }

            // prime the parse of the next item in the array
            myArray[0] = (char) readFromMe.read();
        }

        // suck in the final '|'
        if (readFromMe.read() != '|') {
            throw new IOException("Error when I tried to read in a string array: didn't close with a '|'");
        }

        // at this point we've read the entire array, so make an attribute out of it
        String[] myStringArray = new String[myStrings.size()];
        for (int i = 0; i < myStrings.size(); i++) {
            myStringArray[i] = myStrings.get(i);
        }
        Attribute returnVal = new StringArrayAttribute(myStringArray);

        // return the final result
        if (gotANull == true) {
            boolean[] myBoolArray = new boolean[myNulls.size()];
            for (int i = 0; i < myNulls.size(); i++) {
                myBoolArray[i] = myNulls.get(i);
            }
            System.out.println("found a null!");
            return new ArrayAttributeWithNulls(new BitstringWithArray(myBoolArray), returnVal);
        } else {
            return returnVal;
        }
    }

    public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {

        // easy: just loop through and print out everything in the aray
        writeToMe.write("<");
        for (int i = 0; i < myVals.length; i++) {
            String temp = myVals[i];
            writeToMe.write(temp, 0, temp.length());
            writeToMe.write("|");
        }
        writeToMe.write(">");
        writeToMe.write("|");
    }

    public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

        // just loop through and print everything in the array, or null if testAreNull indicates a null in a position
        writeToMe.write("<");
        for (int i = 0; i < myVals.length; i++) {
            if (theseAreNull.getValue(i)) {
                writeToMe.write("null", 0, 4);
            } else {
                String temp = myVals[i];
                writeToMe.write(temp, 0, temp.length());
            }
            writeToMe.write("|");
        }
        writeToMe.write(">");
        writeToMe.write("|");
    }

    public long writeSelfToStream(DataOutputStream writeToMe) throws IOException {

        // first write the length
        int returnVal = 4;
        writeToMe.writeInt(myVals.length);

        // now write all of the individual strings
        for (int i = 0; i < myVals.length; i++) {

            // write the length of the string
            byte[] data = myVals[i].getBytes();
            writeToMe.writeInt(data.length);
            returnVal += (4 + data.length);

            // and write the actual string
            writeToMe.write(data);
        }

        return returnVal;
    }

    public long readSelfFromStream(DataInputStream readFromMe) throws IOException {

        // read the number of strings
        int returnVal = 4;
        int len = readFromMe.readInt();

        // allocate the array
        myVals = new String[len];

        // and read in each string
        for (int i = 0; i < myVals.length; i++) {

            // read the length of the string
            int strlen = readFromMe.readInt();
            byte[] tempChar = new byte[strlen];
            readFromMe.readFully(tempChar);
            myVals[i] = new String(tempChar);
            returnVal += (4 + strlen);
        }
        return returnVal;
    }

    public HashMap<Attribute, Bitstring> split() {
        HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
        Set<String> set = new HashSet<String>(Arrays.asList(myVals));
        for (String val : set) {
            boolean[] resArray = new boolean[myVals.length];
            for (int i = 0; i < myVals.length; i++) {
                resArray[i] = myVals[i].equals(val);
            }
            splits.put(new StringAttribute(val), new BitstringWithArray(resArray));
        }
        return splits;
    }

    public StringArrayAttribute(String[] fromMe) {

        for (int i = 0; i < fromMe.length; i++) {
            if (fromMe[i] == null) {
                throw new RuntimeException("You can't create a StringArrayAttribute using any null references; use empty strings instead.");
            }
        }
        myVals = fromMe;
    }

    public Attribute add(Attribute me) {
        return me.addR(myVals);
    }

    public Attribute add(long addThisIn) {

        // create the new array
        String[] newArray = new String[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(long addThisIn) {

        // because integer arithmatic is commutative
        return add(addThisIn);
    }

    public Attribute add(long[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        String[] newArray = new String[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(long[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        String[] newArray = new String[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = addThisIn[i] + myVals[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(String[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        String[] newArray = new String[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(String[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        String[] newArray = new String[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = addThisIn[i] + myVals[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(String addThisIn) {

        // create the new array
        String[] newArray = new String[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(String addThisIn) {

        // create the new array
        String[] newArray = new String[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = addThisIn + myVals[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(double[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        String[] newArray = new String[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(double[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        String[] newArray = new String[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = addThisIn[i] + myVals[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(double addThisIn) {

        String[] newArray = new String[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(double addThisIn) {

        String[] newArray = new String[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = addThisIn + myVals[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(int label, double addThisIn) {
        return add(addThisIn);
    }

    public Attribute addR(int label, double addThisIn) {
        return addR(addThisIn);
    }

    public Attribute add(int label, double[] addThisIn) {
        throw new RuntimeException("This method is not defined!");
    }

    public Attribute addR(int label, double[] addThisIn) {
        throw new RuntimeException("This method is not defined!");
    }

    public Attribute add(boolean ifRow, double[][] addThisIn) {
        throw new RuntimeException("This method is not defined!");
    }

    public Attribute addR(boolean ifRow, double[][] addThisIn) {
        throw new RuntimeException("This method is not defined!");
    }

    public Attribute subtract(Attribute me) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(long subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(long subtractFromMe) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(long[] subtractMeOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(long[] subtractFromMe) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(double[] subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(double[] subtractFromMe) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(double subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(double subtractFromThis) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(int label, double subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(int label, double subtractFromThis) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(int label, double[] subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(int label, double[] subtractFromThis) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtract(boolean ifRow, double[][] subtractThisOut) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute subtractR(boolean ifRow, double[][] subtractFromThis) {
        throw new RuntimeException("You can't have a subtraction involving a string.");
    }

    public Attribute multiply(Attribute byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(long byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(double byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(long[] byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(double[] byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(int label, double byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(int label, double[] byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute multiply(boolean ifRow, double[][] byMe) {
        throw new RuntimeException("You can't have a multiplication involving a string.");
    }

    public Attribute divide(Attribute byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(long byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(double byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(int label, double byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(int label, double[] byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(boolean ifRow, double[][] byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(long[] byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(double[] byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(long divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(double divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(long[] divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(double[] divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(int label, double divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(int label, double[] divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divideR(boolean ifRow, double[][] divideMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Bitstring equals(Attribute me) {
        return me.equals(myVals);
    }

    public Bitstring equals(long me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring equals(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].equals(me));
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring equals(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring equals(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].equals(me[i]));
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring equals(int label, double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a scalar?");
    }

    public Bitstring equals(int label, double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a vector?");
    }

    public Bitstring equals(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a matrix?");
    }

    public Bitstring notEqual(Attribute me) {
        return me.notEqual(myVals);
    }

    public Bitstring notEqual(long me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring notEqual(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (!myVals[i].equals(me));
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring notEqual(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring notEqual(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (!myVals[i].equals(me[i]));
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring notEqual(int label, double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a scalar?");
    }

    public Bitstring notEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a vector?");
    }

    public Bitstring notEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a matrix?");
    }

    public Bitstring lessThan(Attribute me) {
        return me.greaterThan(myVals);
    }

    public Bitstring lessThan(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThan(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThan(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me) < 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThan(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me[i]) < 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThan(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring lessThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring lessThan(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public Bitstring greaterThan(Attribute me) {
        return me.lessThan(myVals);
    }

    public Bitstring greaterThan(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThan(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThan(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me) > 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThan(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me[i]) > 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThan(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring greaterThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring greaterThan(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public Bitstring lessThanOrEqual(Attribute me) {
        return me.greaterThanOrEqual(myVals);
    }

    public Bitstring lessThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThanOrEqual(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me) <= 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThanOrEqual(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me[i]) <= 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThanOrEqual(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring lessThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring lessThanOrEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public Bitstring greaterThanOrEqual(Attribute me) {
        return me.lessThanOrEqual(myVals);
    }

    public Bitstring greaterThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThanOrEqual(String me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me) >= 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThanOrEqual(String[] me) {
        boolean[] resArray = new boolean[myVals.length];
        for (int i = 0; i < myVals.length; i++) {
            resArray[i] = (myVals[i].compareTo(me[i]) >= 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThanOrEqual(int lable, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring greaterThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring greaterThanOrEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public void injectSelf(Function f) {
        f.inject(myVals);
    }

    public boolean allAreEqual() {

        return myVals.length == 1;

        /***
         for (int i=1;i<myVals.length;i++) {
         if (!myVals[i].equals(myVals[0]))
         return false;
         }

         return true;
         ***/
    }

    public String print(int maxLen) {
        return print(maxLen, BitstringWithSingleValue.FALSE);
    }

    public String print(int maxLen, Bitstring theseAreNull) {
        String ret = "";
        for (int i = 0; i < myVals.length; i++) {
            if (i > 0) {
                ret += ", ";
            }

            if (theseAreNull.getValue(i)) {
                ret += "null";
            } else {
                ret += String.format("%s", myVals[i]);
            }

            if (ret.length() > maxLen && maxLen > 4) {
                return ret.substring(0, maxLen - 4) + "...";
            }
        }

        return ret;
    }

    public Attribute getSingleton() {
        return new StringAttribute(myVals[0]);
    }
}
