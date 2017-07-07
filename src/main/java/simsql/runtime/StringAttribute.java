

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
 * This implements the String type, assuming that there is a single
 * value for the string accross all possible worlds.
 */

import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class StringAttribute implements Attribute {

    private String myVal;

    public StringAttribute() {
    }

    public void recycle() {
    }

    public long writeSelfToStream(DataOutputStream writeToMe) throws IOException {

        // write the length of the string
        writeToMe.writeInt(myVal.length());
        int returnVal = 4;

        // and write the actual string
        for (int j = 0; j < myVal.length(); j++) {
            returnVal += 2;
            writeToMe.writeChar(myVal.charAt(j));
        }
        return returnVal;
    }

    public long getHashCode() {
        return Hash.hashMe(injectValue(0));
    }

    public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {
        writeToMe.write(myVal, 0, myVal.length());
        writeToMe.write("|");
    }

    public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {
        if (theseAreNull.getValue(0)) {
            writeToMe.write("null", 0, 4);
        } else {
            writeToMe.write(myVal, 0, myVal.length());
        }
        writeToMe.write("|");
    }

    public Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException {

        // check if there is an array, signified by a leaing '<'
        readFromMe.mark(1);
        int firstChar = readFromMe.read();
        while (firstChar == '\n')
            firstChar = readFromMe.read();

        if (firstChar == '<') {
            readFromMe.reset();
            return new StringArrayAttribute().readSelfFromTextStream(readFromMe);

            // see if we hit an EOF
        } else if (firstChar < 0) {
            return null;
        }

        // there was not a reading '<', so read in the single double
        // this is the space we'll use to do the parsing... we assume it is less than 64 chars
        int curLen = 64;
        char[] myArray = new char[curLen];

        // allows us to match the word "null"
        char[] nullString = {'n', 'u', 'l', 'l'};

        // read in the first char
        myArray[0] = (char) firstChar;

        // this loop reads in until (and including) the '|'
        int i;
        boolean isNull = true;
        for (i = 1; myArray[i - 1] != '|'; i++) {

            // double the size of the buffer if needed
            if (i == curLen) {
                char[] myNewArray = new char[curLen * 2];
                System.arraycopy(myArray, 0, myNewArray, 0, curLen);
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
        if (isNull && i == 5) {

            // this means we got a null!
            return NullAttribute.NULL;

        } else {

            // we did not get a null!
            try {
                return new StringAttribute(new String(myArray, 0, i - 1));
            } catch (Exception e) {
                throw new IOException("Error when I tried to read in a Int att... didn't parse to an int");
            }
        }
    }


    public long readSelfFromStream(DataInputStream readFromMe) throws IOException {

        // read the string len
        int returnVal = 4;
        int len = readFromMe.readInt();

        char[] tempChar = new char[len];
        for (int j = 0; j < len; j++) {
            tempChar[j] = readFromMe.readChar();
            returnVal += 2;
        }
        myVal = new String(tempChar);
        return returnVal;
    }

    public int getSize() {
        return 1;
    }

    public byte[] injectValue(int whichMC) {
        try {
            return (myVal + '\000').getBytes("UTF-8");
        } catch (Exception e) {
            throw new RuntimeException("Unable to convert string", e);
        }
    }

    public void injectIntoBuffer(int whichMC, AttributeType castTo, LargeByteBuffer buffer) {
        if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) {
            byte[] bytes = injectValue(whichMC);
            buffer.put(bytes, 0, bytes.length);
        }

        throw new RuntimeException("Invalid cast when writing out value.");
    }


    public AttributeType getType(int whichMC) {
        return new AttributeType(new StringType());
    }

    public Bitstring isNull() {
        return BitstringWithSingleValue.FALSE;
    }

    public Attribute setNull(Bitstring theseOnes) {
        if (theseOnes.allAreTrue()) {
            return NullAttribute.NULL;
        } else {
            return new ArrayAttributeWithNulls(theseOnes, this);
        }
    }

    public HashMap<Attribute, Bitstring> split() {
        HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
        splits.put(new StringAttribute(myVal), BitstringWithSingleValue.trueIf(true));
        return splits;
    }

    public StringAttribute(String fromMe) {
        myVal = fromMe;
    }

    public Attribute add(Attribute me) {
        return me.addR(myVal);
    }

    public Attribute add(long addThisIn) {
        return new StringAttribute(myVal + addThisIn);
    }

    public Attribute addR(long addThisIn) {
        return new StringAttribute(addThisIn + myVal);
    }

    public Attribute add(long[] addThisIn) {

        // create the new array
        String[] newArray = new String[addThisIn.length];

        // put the stuff to add in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = myVal + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(long[] addThisIn) {

        // create the new array
        String[] newArray = new String[addThisIn.length];

        // put the stuff to add in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = addThisIn[i] + myVal;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(String[] addThisIn) {

        // create the new array
        String[] newArray = new String[addThisIn.length];

        // put the stuff to add in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = myVal + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(String[] addThisIn) {

        // create the new array
        String[] newArray = new String[addThisIn.length];

        // put the stuff to add in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = addThisIn[i] + myVal;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(String addThisIn) {
        return new StringAttribute(myVal + addThisIn);
    }

    public Attribute addR(String addThisIn) {
        return new StringAttribute(addThisIn + myVal);
    }

    public Attribute add(double[] addThisIn) {

        if (addThisIn.length != addThisIn.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        String[] newArray = new String[addThisIn.length];

        // now add ourselves in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = myVal + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute removeNulls() {
        return this;
    }

    public Attribute addR(double[] addThisIn) {

        String[] newArray = new String[addThisIn.length];

        // now add ourselves in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = addThisIn[i] + myVal;
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute add(double addThisIn) {
        return new StringAttribute(myVal + addThisIn);
    }

    public Attribute addR(double addThisIn) {
        return new StringAttribute(addThisIn + myVal);
    }

    public Attribute add(int label, double addThisIn) {
        return new StringAttribute(myVal + addThisIn);
    }

    public Attribute addR(int label, double addThisIn) {
        return new StringAttribute(addThisIn + myVal);
    }

    public Attribute add(int label, double[] addThisIn) {
        throw new RuntimeException("You can't add a string and a vector.");
    }

    public Attribute addR(int label, double[] addThisIn) {
        throw new RuntimeException("You can't add a string and a vector.");
    }

    public Attribute add(boolean ifRow, double[][] addThisIn) {
        throw new RuntimeException("You can't add a string and a matrix.");
    }

    public Attribute addR(boolean ifRow, double[][] addThisIn) {
        throw new RuntimeException("You can't add a string and a matrix.");
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

    public Attribute divide(long[] byMe) {
        throw new RuntimeException("You can't have a division involving a string.");
    }

    public Attribute divide(double[] byMe) {
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
        return me.equals(myVal);
    }

    public Bitstring equals(long me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring equals(String me) {
        return BitstringWithSingleValue.trueIf(myVal.equals(me));
    }

    public Bitstring equals(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring equals(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.equals(me[i]));
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
        return me.notEqual(myVal);
    }

    public Bitstring notEqual(long me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(double me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring notEqual(String me) {
        return BitstringWithSingleValue.trueIf(!myVal.equals(me));
    }

    public Bitstring notEqual(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and a double?");
    }

    public Bitstring notEqual(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.equals(me[i]));
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
        return me.greaterThan(myVal);
    }

    public Bitstring lessThan(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThan(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThan(String me) {
        return BitstringWithSingleValue.trueIf(myVal.compareTo(me) < 0);
    }

    public Bitstring lessThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThan(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.compareTo(me[i]) < 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThan(int lable, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring lessThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring lessThan(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public Bitstring greaterThan(Attribute me) {
        return me.lessThan(myVal);
    }

    public Bitstring greaterThan(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThan(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThan(String me) {
        return BitstringWithSingleValue.trueIf(myVal.compareTo(me) > 0);
    }

    public Bitstring greaterThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThan(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.compareTo(me[i]) > 0);
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
        return me.greaterThanOrEqual(myVal);
    }

    public Bitstring lessThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThanOrEqual(String me) {
        return BitstringWithSingleValue.trueIf(myVal.compareTo(me) <= 0);
    }

    public Bitstring lessThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring lessThanOrEqual(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.compareTo(me[i]) <= 0);
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
        return me.lessThanOrEqual(myVal);
    }

    public Bitstring greaterThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThanOrEqual(String me) {
        return BitstringWithSingleValue.trueIf(myVal.compareTo(me) >= 0);
    }

    public Bitstring greaterThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a double?");
    }

    public Bitstring greaterThanOrEqual(String[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal.compareTo(me[i]) >= 0);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThanOrEqual(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a scalar?");
    }

    public Bitstring greaterThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a vector?");
    }

    public Bitstring greaterThanOrEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a String and a matrix?");
    }

    public void injectSelf(Function f) {
        f.inject(myVal);
    }

    public boolean allAreEqual() {
        return true;
    }

    public String print(int maxLen) {
        String s = String.format("%s", myVal);
        if (s.length() > maxLen && maxLen > 4) {
            return s.substring(0, maxLen - 4) + "...";
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
