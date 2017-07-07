

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
 * Implements the simple integer type, with the same value in every world.
 */

import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;

public class IntAttribute implements Attribute {

    private long myVal;

    public static final IntAttribute ONE = new IntAttribute(1);
    public static final IntAttribute ZERO = new IntAttribute(0);

    // a little pool
    private static int poolSize = 0;
    private static final IntAttribute[] pool = new IntAttribute[8];

    // tries to return a recycled instance.
    public static IntAttribute getInstance(long val) {

        if (poolSize == 0) {
            return new IntAttribute(val);
        }

        poolSize--;
        IntAttribute W = pool[poolSize];
        pool[poolSize] = null;

        W.myVal = val;
        W.recycled = false;
        return W;
    }

    private boolean recycled = false;

    public void recycle() {
        if (recycled || poolSize >= pool.length || this == IntAttribute.ONE || this == IntAttribute.ZERO)
            return;

        recycled = true;
        pool[poolSize] = this;
        poolSize++;
    }

    // this allows us to return the data as a byte array
    private static ByteBuffer b = ByteBuffer.allocate(8);

    static {
        b.order(ByteOrder.nativeOrder());
    }

    public IntAttribute() {
    }

    public Bitstring isNull() {
        return BitstringWithSingleValue.FALSE;
    }

    public Attribute removeNulls() {
        return this;
    }

    public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {
        String temp = Long.toString(myVal);
        writeToMe.write(temp, 0, temp.length());
        writeToMe.write("|");
    }

    public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {
        if (theseAreNull.getValue(0)) {
            writeToMe.write("null", 0, 4);
        } else {
            String temp = Long.toString(myVal);
            writeToMe.write(temp, 0, temp.length());
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
            return new IntArrayAttribute().readSelfFromTextStream(readFromMe);

            // see if we hit an EOF
        } else if (firstChar < 0) {
            return null;
        }

        // there was not a reading '<', so read in the single double
        // this is the space we'll use to do the parsing... we assume it is less than 64 chars
        char[] myArray = new char[64];

        // allows us to match the word "null"
        char[] nullString = {'n', 'u', 'l', 'l'};

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
            myArray[i] = (char) readFromMe.read();
        }

        // if we got here, we read in a '|'
        if (isNull == true && i == 5) {

            // this means we got a null!
            return NullAttribute.NULL;

        } else {

            // we did not get a null!
            try {
                return new IntAttribute(Long.valueOf(new String(myArray, 0, i - 1)));
            } catch (Exception e) {
                throw new IOException("Error when I tried to read in a Int att... didn't parse to an int");
            }
        }
    }

    public Attribute setNull(Bitstring theseOnes) {
        if (theseOnes.allAreTrue()) {
            return NullAttribute.NULL;
        } else {
            return new ArrayAttributeWithNulls(theseOnes, this);
        }
    }

    public long getHashCode() {
        return Hash.hashMe(injectValue(0));
    }

    public byte[] injectValue(int whichMC) {
        b.putLong(0, myVal);
        return b.array();
    }

    public void injectIntoBuffer(int whichMC, AttributeType castTo, LargeByteBuffer buffer) {
        if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) {
            buffer.putLong(myVal);
        }

        if (castTo.getTypeCode() == TypeCode.DOUBLE) {
            buffer.putDouble((double) myVal);
        } else throw new RuntimeException("Invalid cast when writing out value.");
    }

    public int getSize() {
        return 1;
    }

    public AttributeType getType(int whichMC) {
        return new AttributeType(new IntType());
    }

    public long writeSelfToStream(DataOutputStream writeToMe) throws IOException {
        writeToMe.writeLong(myVal);
        return 8;
    }

    public long readSelfFromStream(DataInputStream readFromMe) throws IOException {
        myVal = readFromMe.readLong();
        return 8;
    }

    public HashMap<Attribute, Bitstring> split() {
        HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
        splits.put(new IntAttribute(myVal), BitstringWithSingleValue.trueIf(true));
        return splits;
    }

    public IntAttribute(long fromMe) {
        myVal = fromMe;
    }

    public Attribute add(Attribute me) {
        return me.addR(myVal);
    }

    public Attribute add(long addThisIn) {
        if (addThisIn == 0)
            return this;

        //    return new IntAttribute (myVal + addThisIn);
        return getInstance(myVal + addThisIn);
    }

    public Attribute addR(long addThisIn) {
        if (addThisIn == 0)
            return this;

        //    return new IntAttribute (myVal + addThisIn);
        return getInstance(myVal + addThisIn);
    }

    public Attribute add(long[] addThisIn) {

        // create the new array
        long[] newArray = new long[addThisIn.length];

        // put the stuff to add in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = addThisIn[i] + myVal;
        }

        // and get outta here!
        return new IntArrayAttribute(newArray);
    }

    public Attribute addR(long[] addThisIn) {

        // because ordering does not matter with addition of ints
        return add(addThisIn);
    }

    public Attribute add(String[] addThisIn) {
        // create the new array
        String[] newArray = new String[addThisIn.length];

        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = myVal + addThisIn[i];
        }

        // and get outta here!
        return new StringArrayAttribute(newArray);
    }

    public Attribute addR(String[] addThisIn) {
        // create the new array
        String[] newArray = new String[addThisIn.length];

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

        double[] newArray = new double[addThisIn.length];

        // now add ourselves in
        for (int i = 0; i < addThisIn.length; i++) {
            newArray[i] = myVal + addThisIn[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute addR(double[] addThisIn) {

        // since addition on numbers is commutative
        return add(addThisIn);
    }

    public Attribute add(double addThisIn) {
        //    return new DoubleAttribute (addThisIn + myVal);
        return DoubleAttribute.getInstance(addThisIn + myVal);
    }

    public Attribute addR(double addThisIn) {
        //    return new DoubleAttribute (addThisIn + myVal);
        return DoubleAttribute.getInstance(addThisIn + myVal);
    }

    public Attribute add(int label, double addThisIn) {
        return DoubleAttribute.getInstance(addThisIn + myVal);
    }

    public Attribute addR(int label, double addThisIn) {
        return DoubleAttribute.getInstance(addThisIn + myVal);
    }

    public Attribute add(int label, double[] addThisIn) {
        double[] newVector = new double[addThisIn.length];
        for (int i = 0; i < addThisIn.length; i++) {
            newVector[i] = myVal + addThisIn[i];
        }
        return new VectorAttribute(newVector);
    }

    public Attribute addR(int label, double[] addThisIn) {
        return add(label, addThisIn);
    }

    public Attribute add(boolean ifRow, double[][] addThisIn) {

        if (myVal == 0) {
            return new MatrixAttribute(ifRow, addThisIn);
        }

        // find the second dimension
        int secondDim = 0;
        for (int i = 0; i < addThisIn.length; i++) {
            if (addThisIn[i] != null) {
                secondDim = addThisIn[i].length;
                break;
            }
        }

        double[][] newMatrix = new double[addThisIn.length][secondDim];
        for (int i = 0; i < addThisIn.length; i++) {
            if (addThisIn[i] != null) {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal + addThisIn[i][j];
            } else {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal;
            }
        }
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Attribute addR(boolean ifRow, double[][] addThisIn) {
        return add(ifRow, addThisIn);
    }

    public Attribute subtract(Attribute me) {
        return me.subtractR(myVal);
    }

    public Attribute subtract(long subtractThisOut) {
        if (subtractThisOut == 0)
            return this;

        //    return new IntAttribute (myVal - subtractThisOut);
        return getInstance(myVal - subtractThisOut);
    }

    public Attribute subtractR(long subtractFromMe) {
        return getInstance(subtractFromMe - myVal);
        //    return new IntAttribute (subtractFromMe - myVal);
    }

    public Attribute subtract(long[] subtractMeOut) {

        // create the new array
        long[] newArray = new long[subtractMeOut.length];

        // put the stuff to add in
        for (int i = 0; i < subtractMeOut.length; i++) {
            newArray[i] = myVal - subtractMeOut[i];
        }

        // and get outta here!
        return new IntArrayAttribute(newArray);
    }

    public Attribute subtractR(long[] subtractFromMe) {

        // create the new array
        long[] newArray = new long[subtractFromMe.length];

        // put the stuff to add in
        for (int i = 0; i < subtractFromMe.length; i++) {
            newArray[i] = subtractFromMe[i] - myVal;
        }

        // and get outta here!
        return new IntArrayAttribute(newArray);
    }

    public Attribute subtract(double[] subtractThisOut) {

        double[] newArray = new double[subtractThisOut.length];

        // now add ourselves in
        for (int i = 0; i < subtractThisOut.length; i++) {
            newArray[i] = myVal - subtractThisOut[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtractR(double[] subtractFromMe) {

        double[] newArray = new double[subtractFromMe.length];

        // now add ourselves in
        for (int i = 0; i < subtractFromMe.length; i++) {
            newArray[i] = subtractFromMe[i] - myVal;
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtract(double subtractThisOut) {
        //    return new DoubleAttribute (myVal - subtractThisOut);
        return DoubleAttribute.getInstance(myVal - subtractThisOut);
    }

    public Attribute subtractR(double subtractFromThis) {
        //    return new DoubleAttribute (subtractFromThis - myVal);
        return DoubleAttribute.getInstance(subtractFromThis - myVal);
    }

    public Attribute subtract(int label, double subtractThisOut) {

        if (myVal == subtractThisOut)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (myVal - subtractThisOut);
        return DoubleAttribute.getInstance(myVal - subtractThisOut);
    }

    public Attribute subtractR(int label, double subtractFromThis) {
        if (myVal == subtractFromThis)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (subtractFromThis - myVal);
        return DoubleAttribute.getInstance(subtractFromThis - myVal);
    }

    public Attribute subtract(int label, double[] subtractThisOut) {

        double[] newArray = new double[subtractThisOut.length];

        // now add ourselves in
        for (int i = 0; i < subtractThisOut.length; i++) {
            newArray[i] = myVal - subtractThisOut[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtractR(int label, double[] subtractFromMe) {

        double[] newArray = new double[subtractFromMe.length];

        // now add ourselves in
        for (int i = 0; i < subtractFromMe.length; i++) {
            newArray[i] = subtractFromMe[i] - myVal;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtract(boolean ifRow, double[][] subtractThisOut) {

        // find the second dimension
        int secondDim = 0;
        for (int i = 0; i < subtractThisOut.length; i++) {
            if (subtractThisOut[i] != null) {
                secondDim = subtractThisOut[i].length;
                break;
            }
        }

        double[][] newMatrix = new double[subtractThisOut.length][secondDim];

        // now add ourselves in
        for (int i = 0; i < subtractThisOut.length; i++) {
            if (subtractThisOut[i] != null) {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal - subtractThisOut[i][j];
            } else {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal;
            }
        }

        // and get outta here!
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Attribute subtractR(boolean ifRow, double[][] subtractFromMe) {

        if (myVal == 0)
            return new MatrixAttribute(ifRow, subtractFromMe);

        // find the second dimension
        int secondDim = 0;
        for (int i = 0; i < subtractFromMe.length; i++) {
            if (subtractFromMe[i] != null) {
                secondDim = subtractFromMe[i].length;
                break;
            }
        }

        double[][] newMatrix = new double[subtractFromMe.length][secondDim];

        // now add ourselves in
        for (int i = 0; i < subtractFromMe.length; i++) {
            if (subtractFromMe[i] != null) {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = subtractFromMe[i][j] - myVal;
            } else {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = -myVal;
            }
        }

        // and get outta here!
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Attribute multiply(Attribute byMe) {
        return byMe.multiply(myVal);
    }

    public Attribute multiply(long byMe) {
        if (byMe == 0)
            return IntAttribute.ZERO;

        if (byMe == 1)
            return this;

        return getInstance(myVal * byMe);
        //    return new IntAttribute (myVal * byMe);
    }

    public Attribute multiply(double byMe) {
        //    return new DoubleAttribute (myVal * byMe);
        return DoubleAttribute.getInstance(myVal * byMe);
    }

    public Attribute multiply(long[] byMe) {

        // create the new array
        long[] newArray = new long[byMe.length];

        // put the stuff to add in
        for (int i = 0; i < byMe.length; i++) {
            newArray[i] = myVal * byMe[i];
        }

        // and get outta here!
        return new IntArrayAttribute(newArray);
    }

    public Attribute multiply(double[] byMe) {

        // create the new array
        double[] newArray = new double[byMe.length];

        // put the stuff to add in
        for (int i = 0; i < byMe.length; i++) {
            newArray[i] = myVal * byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute multiply(int label, double byMe) {

        if (byMe == 0.0)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (myVal * byMe);
        return DoubleAttribute.getInstance(myVal * byMe);
    }

    public Attribute multiply(int label, double[] byMe) {

        // create the new array
        double[] newArray = new double[byMe.length];

        // put the stuff to add in
        for (int i = 0; i < byMe.length; i++) {
            newArray[i] = myVal * byMe[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute multiply(boolean ifRow, double[][] byMe) {

        double[][] newMatrix = new double[byMe.length][];
        for (int i = 0; i < byMe.length; i++) {
            if (byMe[i] != null) {
                newMatrix[i] = new double[byMe[i].length];
                for (int j = 0; j < byMe[i].length; j++)
                    newMatrix[i][j] = myVal * byMe[i][j];
            }
        }
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Attribute divide(Attribute byMe) {
        return byMe.divideR(myVal);
    }

    public Attribute divide(long byMe) {

        if (byMe == 1)
            return this;

        if (byMe == myVal)
            return IntAttribute.ONE;

        if (myVal == 0)
            return IntAttribute.ZERO;

        try {
            //      return new IntAttribute (myVal / byMe);

            return getInstance(myVal / byMe);

        } catch (ArithmeticException e) {

            // division by zero -> null.
            return NullAttribute.NULL;
        }
    }

    public Attribute divideR(long divideMe) {

        if (divideMe == 0)
            return IntAttribute.ZERO;

        if (divideMe == myVal)
            return IntAttribute.ONE;

        try {
            //      return new IntAttribute (divideMe / myVal);
            return getInstance(divideMe / myVal);
        } catch (ArithmeticException e) {

            // division by zero -> null.
            return NullAttribute.NULL;
        }
    }

    public Attribute divide(long[] byMe) {

        if (myVal == 0)
            return IntAttribute.ZERO;

        // create the new array
        long[] newArray = new long[byMe.length];
        boolean[] nulls = new boolean[byMe.length];
        boolean haveNulls = false;

        // put the stuff to add in
        for (int i = 0; i < byMe.length; i++) {

            nulls[i] = (byMe[i] == 0);
            haveNulls |= nulls[i];
            newArray[i] = nulls[i] ? 0 : (myVal / byMe[i]);
        }

        // and get outta here!
        if (haveNulls) {
            return new ArrayAttributeWithNulls(new BitstringWithArray(nulls), new IntArrayAttribute(newArray));
        } else {
            return new IntArrayAttribute(newArray);
        }
    }

    public Attribute divideR(long[] divideMe) {

        if (myVal == 0)
            return NullAttribute.NULL;

        // create the new array
        long[] newArray = new long[divideMe.length];

        // put the stuff to add in
        for (int i = 0; i < divideMe.length; i++) {
            newArray[i] = divideMe[i] / myVal;
        }

        // and get outta here!
        return new IntArrayAttribute(newArray);
    }

    public Attribute divide(double[] byMe) {

        double[] newArray = new double[byMe.length];

        // now add ourselves in
        for (int i = 0; i < byMe.length; i++) {
            newArray[i] = myVal / byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divideR(double[] divideMe) {

        double[] newArray = new double[divideMe.length];

        // now add ourselves in
        for (int i = 0; i < divideMe.length; i++) {
            newArray[i] = divideMe[i] / myVal;
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divide(double byMe) {
        if (myVal == 0)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (myVal / byMe);
        return DoubleAttribute.getInstance(myVal / byMe);
    }

    public Attribute divideR(double divideMe) {
        if (divideMe == 0.0)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (divideMe / myVal);
        return DoubleAttribute.getInstance(divideMe / myVal);
    }

    public Attribute divide(int label, double byMe) {

        if (myVal == 0.0)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (myVal / byMe);
        return DoubleAttribute.getInstance(myVal / byMe);
    }

    public Attribute divideR(int label, double divideMe) {
        if (divideMe == 0.0)
            return DoubleAttribute.ZERO;

        //    return new DoubleAttribute (divideMe / myVal);
        return DoubleAttribute.getInstance(divideMe / myVal);
    }

    public Attribute divide(int label, double[] byMe) {

        double[] newArray = new double[byMe.length];

        // now add ourselves in
        for (int i = 0; i < byMe.length; i++) {
            newArray[i] = myVal / byMe[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divideR(int label, double[] divideMe) {

        double[] newArray = new double[divideMe.length];

        // now add ourselves in
        for (int i = 0; i < divideMe.length; i++) {
            newArray[i] = divideMe[i] / myVal;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divide(boolean ifRow, double[][] byMe) {

        // find the second dimension
        int secondDim = 0;
        for (int i = 0; i < byMe.length; i++) {
            if (byMe[i] != null) {
                secondDim = byMe[i].length;
                break;
            }
        }

        double[][] newMatrix = new double[byMe.length][secondDim];
        for (int i = 0; i < byMe.length; i++) {
            if (byMe[i] != null) {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal / byMe[i][j];
            } else {
                for (int j = 0; j < secondDim; j++)
                    newMatrix[i][j] = myVal / 0.0;
            }
        }
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Attribute divideR(boolean ifRow, double[][] divideMe) {
        if (myVal == 1)
            return new MatrixAttribute(ifRow, divideMe);

        double[][] newMatrix = new double[divideMe.length][];
        for (int i = 0; i < divideMe.length; i++) {
            if (divideMe[i] != null) {
                newMatrix[i] = new double[divideMe[i].length];
                for (int j = 0; j < divideMe[i].length; j++)
                    newMatrix[i][j] = divideMe[i][j] / myVal;
            }
        }
        return new MatrixAttribute(ifRow, newMatrix);
    }

    public Bitstring equals(Attribute me) {
        return me.equals(myVal);
    }

    public Bitstring equals(long me) {
        return BitstringWithSingleValue.trueIf(myVal == me);
    }

    public Bitstring equals(double me) {
        return BitstringWithSingleValue.trueIf(myVal == me);
    }

    public Bitstring equals(String me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal == me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring equals(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal == me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring equals(String[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring equals(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal == me);
    }

    public Bitstring equals(int label, double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a int?");
    }

    public Bitstring equals(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing an equality check on a matrix and a int?");
    }

    public Bitstring notEqual(Attribute me) {
        return me.notEqual(myVal);
    }

    public Bitstring notEqual(long me) {
        return BitstringWithSingleValue.trueIf(myVal != me);
    }

    public Bitstring notEqual(double me) {
        return BitstringWithSingleValue.trueIf(myVal != me);
    }

    public Bitstring notEqual(String me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal != me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring notEqual(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal != me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring notEqual(String[] me) {
        throw new RuntimeException("Why are you doing an equality check on a String and an int?");
    }

    public Bitstring notEqual(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal != me);
    }

    public Bitstring notEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a int?");
    }

    public Bitstring notEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing an equality check on a matrix and a int?");
    }

    public Bitstring greaterThan(Attribute me) {
        return me.lessThan(myVal);
    }

    public Bitstring greaterThan(long me) {
        return BitstringWithSingleValue.trueIf(myVal > me);
    }

    public Bitstring greaterThan(double me) {
        return BitstringWithSingleValue.trueIf(myVal > me);
    }

    public Bitstring greaterThan(String me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThan(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal > me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThan(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal > me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThan(String[] me) {
        throw new RuntimeException("Why are you doinga comparison on a String and an int?");
    }

    public Bitstring greaterThan(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal > me);
    }

    public Bitstring greaterThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a vector and a int?");
    }

    public Bitstring greaterThan(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a matrix and a int?");
    }

    public Bitstring lessThan(Attribute me) {
        return me.greaterThan(myVal);
    }

    public Bitstring lessThan(long me) {
        return BitstringWithSingleValue.trueIf(myVal < me);
    }

    public Bitstring lessThan(double me) {
        return BitstringWithSingleValue.trueIf(myVal < me);
    }

    public Bitstring lessThan(String me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThan(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal < me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThan(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal < me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThan(String[] me) {
        throw new RuntimeException("Why are you doinga comparison on a String and an int?");
    }

    public Bitstring lessThan(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal < me);
    }

    public Bitstring lessThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a vector and a int?");
    }

    public Bitstring lessThan(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a matrix and a int?");
    }

    public Bitstring greaterThanOrEqual(Attribute me) {
        return me.lessThanOrEqual(myVal);
    }

    public Bitstring greaterThanOrEqual(long me) {
        return BitstringWithSingleValue.trueIf(myVal >= me);
    }

    public Bitstring greaterThanOrEqual(double me) {
        return BitstringWithSingleValue.trueIf(myVal >= me);
    }

    public Bitstring greaterThanOrEqual(String me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal >= me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThanOrEqual(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal >= me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring greaterThanOrEqual(String[] me) {
        throw new RuntimeException("Why are you doinga comparison on a String and an int?");
    }

    public Bitstring greaterThanOrEqual(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal >= me);
    }

    public Bitstring greaterThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a vector and a int?");
    }

    public Bitstring greaterThanOrEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a matrix and a int?");
    }

    public Bitstring lessThanOrEqual(Attribute me) {
        return me.greaterThanOrEqual(myVal);
    }

    public Bitstring lessThanOrEqual(long me) {
        return BitstringWithSingleValue.trueIf(myVal <= me);
    }

    public Bitstring lessThanOrEqual(double me) {
        return BitstringWithSingleValue.trueIf(myVal <= me);
    }

    public Bitstring lessThanOrEqual(String me) {
        throw new RuntimeException("Why are you doing a comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(long[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal <= me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThanOrEqual(double[] me) {
        boolean[] resArray = new boolean[me.length];
        for (int i = 0; i < me.length; i++) {
            resArray[i] = (myVal <= me[i]);
        }
        return new BitstringWithArray(resArray);
    }

    public Bitstring lessThanOrEqual(String[] me) {
        throw new RuntimeException("Why are you doinga comparison on a String and an int?");
    }

    public Bitstring lessThanOrEqual(int label, double me) {
        return BitstringWithSingleValue.trueIf(myVal <= me);
    }

    public Bitstring lessThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison on a vector and a int?");
    }

    public Bitstring lessThanOrEqual(boolean ifRow, double[][] me) {
        throw new RuntimeException("Why are you doing a comparison on a matrix and a int?");
    }

    public void injectSelf(Function f) {
        f.inject(myVal);
    }

    public boolean allAreEqual() {
        return true;
    }

    public String print(int maxLen) {
        String s = String.format("%d", myVal);
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
