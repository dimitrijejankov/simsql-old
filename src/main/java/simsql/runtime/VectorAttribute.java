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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the vector type, with the same value in every world.
 */
public class VectorAttribute implements Attribute {

    // label, default is -1
    private int myLabel = -1;
    // this is the actual address
    private double[] myVals;

    // this allows us to return the address as a byte array
    private static ByteBuffer b = ByteBuffer.allocate(0);

    static {
        b.order(ByteOrder.nativeOrder());
    }

    public void recycle() {
    }

    public VectorAttribute() {
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

    public Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException {

        // this is the space we'll use to do the parsing... we assume it is less than 64 chars
        char[] myArray = new char[64];

        // allows us to match the word "null"
        char[] nullString = {'n', 'u', 'l', 'l'};

        // records the doubles and nulls we've read in
        ArrayList<Double> myDoubles = new ArrayList<Double>(64);
        // ArrayList <Boolean> myNulls = new ArrayList <Boolean> (64);

        boolean isNull = true;

        // suck in the '[', but ignore a leading newline character
        int startChar = readFromMe.read();
        while (startChar == '\n')
            startChar = readFromMe.read();

        // if we got an eof, we are done
        if (startChar == -1) {
            return null;
        }

        if (startChar != '[') {
            throw new IOException("Read in a bad vector start character when reading a vector; expected '['");
        }

        // this tells us we did not see a null anywhere in the input
        boolean gotANull = false;

        // read in the first char
        myArray[0] = (char) readFromMe.read();

        // keep reading until we find the ']'
        while (myArray[0] != ']') {

            // this loop reads in until (and including) the '|'
            int i;
            for (i = 1; myArray[i - 1] != ',' && myArray[i - 1] != ']'; i++) {

                if (isNull) {
                    // isNull gets set to false if we find a char that does not match the string 'null'
                    if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
                        isNull = false;
                    }
                }
                myArray[i] = (char) readFromMe.read();
            }

            // if we got here, we read in a ','
            if (isNull == true && i == 5) {

                // this means we got a null!
                // myDoubles.add (0.0);
                // gotANull = true;
                // suck in the final '|'
                if (readFromMe.read() != '|') {
                    throw new IOException("Error when I tried to read in a vector: didn't close with a '|'");
                }
                return NullAttribute.NULL;

            } else {

                // we did not get a null!
                try {
                    myDoubles.add(Double.valueOf(new String(myArray, 0, i - 1)));
                } catch (Exception e) {
                    throw new IOException("Error when I tried to read in a vector... an entry didn't parse to a double");
                }
            }

            // prime the parse of the next item in the vector
            if (myArray[i - 1] != ']')
                myArray[0] = (char) readFromMe.read();
            else
                break;
        }

        // suck in the final '|'
        if (readFromMe.read() != '|') {
            throw new IOException("Error when I tried to read in a vector: didn't close with a '|'");
        }

        // at this point we've read the entire vector, so make an attribute out of it
        double[] myDoubleArray = new double[myDoubles.size()];
        for (int i = 0; i < myDoubles.size(); i++) {
            myDoubleArray[i] = myDoubles.get(i);
        }
        return new VectorAttribute(myDoubleArray);
    }


    public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {

        // easy: just loop through and print out everything in the aray
        writeToMe.write("[");
        for (int i = 0; i < myVals.length; i++) {
            String temp = Double.toString(myVals[i]);
            writeToMe.write(temp, 0, temp.length());
            if (i < myVals.length - 1)
                writeToMe.write(",");
        }
        writeToMe.write("]");
        writeToMe.write("|");
    }

    public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

        // just loop through and print everything in the array, or null if testAreNull indicates a null in a position
        writeToMe.write("[");
        if (theseAreNull.getValue(0))
            writeToMe.write("null", 0, 4);
        else {
            writeSelfToTextStream(writeToMe);
        }
        writeToMe.write("]");
        writeToMe.write("|");

    }

    public long getHashCode() {
        //throw new RuntimeException ("Can't hash a vector attribute!");
        return Hash.hashMe(getValue(0));
    }

    public int getSize() {
        return 1;
    }

    public byte[] getValue(int whichMC) {
        if (b.capacity() < 8 * (myVals.length + 1)) {
            b = ByteBuffer.allocate(8 * (myVals.length + 1));
            b.order(ByteOrder.nativeOrder());
        }

        b.position(0);
        b.putDouble((double) myVals.length);
        for (double myVal : myVals) {
            b.putDouble(myVal);
        }
        return b.array();
    }

    public byte[] getValue(int whichMC, AttributeType castTo) {
        if (castTo.getTypeCode() == getType(whichMC).getTypeCode())
            return getValue(whichMC);

        else throw new RuntimeException("Invalid cast when writing out value.");
    }

    public Attribute removeNulls() {
        return this;
    }

    public AttributeType getType(int whichMC) {
        return new AttributeType(new VectorType());
    }

    // format: label, length of address, address
    public int writeSelfToStream(DataOutputStream writeToMe) throws IOException {
        int returnVal = 8;
        writeToMe.writeInt(myLabel);
        writeToMe.writeInt(myVals.length);
        for (int i = 0; i < myVals.length; i++) {
            writeToMe.writeDouble(myVals[i]);
        }
        returnVal += 8 * myVals.length;
        return returnVal;
    }

    public int readSelfFromStream(DataInputStream readFromMe) throws IOException {
        int returnVal = 8;
        myLabel = readFromMe.readInt();
        int len = readFromMe.readInt();
        myVals = new double[len];
        for (int i = 0; i < myVals.length; i++) {
            myVals[i] = readFromMe.readDouble();
        }
        returnVal += 8 * myVals.length;
        return returnVal;
    }

    public HashMap<Attribute, Bitstring> split() {
        HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
        splits.put(this, BitstringWithSingleValue.trueIf(true));
        return splits;
    }

    public VectorAttribute(double[] fromMe) {
        myLabel = -1;
        myVals = fromMe;
    }

    public VectorAttribute(int label, double[] fromMe) {
        myLabel = label;
        myVals = fromMe;
    }

    protected double[] getVal() {
        return myVals;
    }

    protected int getLabel() {
        return myLabel;
    }

    protected void setVal(double[] val) {
        myVals = val;
    }

    protected void setVal(double val, int dim) {
        myVals[dim] = val;
    }

    protected void setLabel(int label) {
        myLabel = label;
    }

    public Attribute add(Attribute me) {
        return me.addR(myLabel, myVals);
    }

    public Attribute add(long addThisIn) {

        if (addThisIn == 0)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute addR(long addThisIn) {

        // because integer arithmatic is commutative
        return add(addThisIn);
    }

    // TODO
    public Attribute add(long[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute addR(long[] addThisIn) {

        // because ordering does not matter with addition of ints
        return add(addThisIn);
    }

    public Attribute add(String[] addThisIn) {
        throw new RuntimeException("You can't add a vector and an array of string.");
    }

    public Attribute addR(String[] addThisIn) {
        throw new RuntimeException("You can't add a vector and an array of string.");
    }

    public Attribute add(String addThisIn) {
        throw new RuntimeException("You can't add a vector and a string.");
    }

    public Attribute addR(String addThisIn) {
        throw new RuntimeException("You can't add a vector and a string.");
    }

    // TODO
    public Attribute add(double[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute addR(double[] addThisIn) {

        // since addition on numbers is commutative
        return add(addThisIn);
    }

    public Attribute add(double addThisIn) {

        if (addThisIn == 0.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute addR(double addThisIn) {

        // since addition on numbers is commutative
        return add(addThisIn);
    }

    public Attribute add(int label, double addThisIn) {

        if (addThisIn == 0.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute addR(int label, double addThisIn) {

        // since addition on numbers is commutative
        return add(label, addThisIn);
    }

    public Attribute add(int label, double[] addThisIn) {

        if (addThisIn.length != myVals.length) {
            throw new RuntimeException("adding two vectors with different lengths!");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] + addThisIn[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute addR(int label, double[] addThisIn) {

        // since addition on numbers is commutative
        return add(label, addThisIn);
    }

    // vector + matrix will be always row-based addition
    public Attribute add(Matrix addThisIn) {
        // add the vector
        addThisIn.checkDimensions(myVals);

        // allocate matrix
        Matrix m = new Matrix(addThisIn.getSize1(), addThisIn.getSize2(), addThisIn.getIfRow());

        // add double in native
        Matrix.nativeAddVector(addThisIn.getAddress(), addThisIn.getIfRow(), 1.0, myVals, 1.0, m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Attribute addR(Matrix addThisIn) {

        // since addition on numbers is commutative
        return add(addThisIn);
    }

    public Attribute subtract(Attribute me) {
        return me.subtractR(myLabel, myVals);
    }

    public Attribute subtract(long subtractThisOut) {

        if (subtractThisOut == 0)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractThisOut;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtractR(long subtractFromMe) {

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromMe - myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    // TODO
    public Attribute subtract(long[] subtractMeOut) {

        if (subtractMeOut.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractMeOut[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtractR(long[] subtractFromMe) {

        if (subtractFromMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromMe[i] - myVals[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtract(double[] subtractThisOut) {

        if (subtractThisOut.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractThisOut[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtractR(double[] subtractFromMe) {

        if (subtractFromMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromMe[i] - myVals[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute subtract(double subtractThisOut) {

        if (subtractThisOut == 0.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractThisOut;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtractR(double subtractFromThis) {

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromThis - myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtract(int label, double subtractThisOut) {

        if (subtractThisOut == 0.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractThisOut;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtractR(int label, double subtractFromThis) {

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromThis - myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtract(int label, double[] subtractThisOut) {

        if (subtractThisOut.length != myVals.length) {
            throw new RuntimeException("subtracting two vectors with different lengths!");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] - subtractThisOut[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtractR(int label, double[] subtractFromMe) {

        if (subtractFromMe.length != myVals.length) {
            throw new RuntimeException("subtracting two vectors with different lengths!");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = subtractFromMe[i] - myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute subtract(Matrix subtractThisOut) {

        // add the vector
        subtractThisOut.checkDimensions(myVals);

        // allocate matrix
        Matrix m = new Matrix(subtractThisOut.getSize1(), subtractThisOut.getSize2(), subtractThisOut.getIfRow());

        // add double in native
        Matrix.nativeAddVector(subtractThisOut.getAddress(), subtractThisOut.getIfRow(), -1.0, myVals, 1.0, m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Attribute subtractR(Matrix subtractFromMe) {

        // add the vector
        subtractFromMe.checkDimensions(myVals);

        // allocate matrix
        Matrix m = new Matrix(subtractFromMe.getSize1(), subtractFromMe.getSize2(), subtractFromMe.getIfRow());

        // add double in native
        Matrix.nativeAddVector(subtractFromMe.getAddress(), subtractFromMe.getIfRow(), 1.0, myVals, -1.0, m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Attribute multiply(Attribute byMe) {
        return byMe.multiply(myLabel, myVals);
    }

    public Attribute multiply(long byMe) {

        if (byMe == 1)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute multiply(double byMe) {

        if (byMe == 1.0)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    // TODO
    public Attribute multiply(long[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute multiply(double[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("adding an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute multiply(int label, double byMe) {

        if (byMe == 1.0)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute multiply(int label, double[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("multiplying two vectors with different lengths");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] * byMe[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute multiply(Matrix byMe) {

        // check if we can perform this operation throw runtime exception otherwise
        byMe.checkDimensions(byMe);

        // allocate matrix
        Matrix m = new Matrix(byMe.getSize1(), byMe.getSize2(), byMe.getIfRow());

        // multiply matrix in native
        Matrix.nativeMultiplyVector(myVals, byMe.getAddress(), byMe.getIfRow(), m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Attribute divide(Attribute byMe) {
        return byMe.divideR(myLabel, myVals);
    }

    public Attribute divide(long byMe) {

        if (byMe == 1)
            return new VectorAttribute(myVals);

        // create the new array
        double[] newArray = new double[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divideR(long divideMe) {

        // create the new array
        double[] newArray = new double[myVals.length];

        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe / myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    // TODO
    public Attribute divide(long[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divideR(long[] divideMe) {

        if (divideMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        // create the new array
        double[] newArray = new double[myVals.length];

        // put the stuff to add in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe[i] / myVals[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divide(double[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divideR(double[] divideMe) {

        if (divideMe.length != myVals.length) {
            throw new RuntimeException("subtracting an array of values with the wrong number of possible worlds");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe[i] / myVals[i];
        }

        // and get outta here!
        return new DoubleArrayAttribute(newArray);
    }

    public Attribute divide(double byMe) {

        if (byMe == 1.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divideR(double divideMe) {

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe / myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divide(int label, double byMe) {

        if (byMe == 1.0)
            return new VectorAttribute(myVals);

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe;
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divideR(int label, double divideMe) {

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe / myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divide(int label, double[] byMe) {

        if (byMe.length != myVals.length) {
            throw new RuntimeException("division between two vectors with different lengths");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = myVals[i] / byMe[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divideR(int label, double[] divideMe) {

        if (divideMe.length != myVals.length) {
            throw new RuntimeException("division between two vectors with different lengths");
        }

        double[] newArray = new double[myVals.length];

        // now add ourselves in
        for (int i = 0; i < myVals.length; i++) {
            newArray[i] = divideMe[i] / myVals[i];
        }

        // and get outta here!
        return new VectorAttribute(newArray);
    }

    public Attribute divide(Matrix byMe) {

        // check if we can perform this operation throw runtime exception otherwise
        byMe.checkDimensions(byMe);

        // allocate matrix
        Matrix m = new Matrix(byMe.getSize1(), byMe.getSize2(), byMe.getIfRow());

        // multiply matrix in native
        Matrix.nativeDivideVectorMatrix(byMe.getAddress(), byMe.getIfRow(), myVals, m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Attribute divideR(Matrix divideMe) {

        // check if we can perform this operation throw runtime exception otherwise
        divideMe.checkDimensions(divideMe);

        // allocate matrix
        Matrix m = new Matrix(divideMe.getSize1(), divideMe.getSize2(), divideMe.getIfRow());

        // multiply matrix in native
        Matrix.nativeDivideMatrixVector(divideMe.getAddress(), divideMe.getIfRow(), myVals, m.getAddress());

        // return the matrix attribute
        return new MatrixAttribute(m);
    }

    public Bitstring equals(Attribute me) {
        return me.equals(myLabel, myVals);
    }

    public Bitstring equals(long me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a long?");
    }

    public Bitstring equals(double me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a double?");
    }

    public Bitstring equals(String me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a string?");
    }

    public Bitstring equals(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of long?");
    }

    public Bitstring equals(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of double?");
    }

    public Bitstring equals(String[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of string?");
    }

    public Bitstring equals(int label, double me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a scalar?");
    }

    public Bitstring equals(int label, double[] me) {

        if (me.length != myVals.length) {
            //throw new RuntimeException ("Equality check on two vectors with different lengths");
            return BitstringWithSingleValue.trueIf(false);
        }

        boolean ifEqual = true;
        for (int i = 0; i < myVals.length; i++) {
            ifEqual = ifEqual && (myVals[i] == me[i]);
        }
        return BitstringWithSingleValue.trueIf(ifEqual);
    }

    public Bitstring equals(Matrix me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a matrix?");
    }

    public Bitstring notEqual(Attribute me) {
        return me.notEqual(myLabel, myVals);
    }

    public Bitstring notEqual(long me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a long?");
    }

    public Bitstring notEqual(double me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a double?");
    }

    public Bitstring notEqual(String me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a string?");
    }

    public Bitstring notEqual(long[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of long?");
    }

    public Bitstring notEqual(double[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of double?");
    }

    public Bitstring notEqual(String[] me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and an array of string?");
    }

    public Bitstring notEqual(int label, double me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a scalar?");
    }

    public Bitstring notEqual(int label, double[] me) {

        if (me.length != myVals.length) {
            //throw new RuntimeException ("Equality check on two vectors with different lengths");
            return BitstringWithSingleValue.trueIf(true);
        }

        boolean notEqual = false;
        for (int i = 0; i < myVals.length; i++) {
            notEqual = notEqual || (myVals[i] != me[i]);
        }
        return BitstringWithSingleValue.trueIf(notEqual);
    }

    public Bitstring notEqual(Matrix me) {
        throw new RuntimeException("Why are you doing an equality check on a vector and a matrix?");
    }

    public Bitstring greaterThan(Attribute me) {
        return me.lessThan(myLabel, myVals);
    }

    public Bitstring greaterThan(long me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a long?");
    }

    public Bitstring greaterThan(double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a double?");
    }

    public Bitstring greaterThan(String me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a string?");
    }

    public Bitstring greaterThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of long?");
    }

    public Bitstring greaterThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of double?");
    }

    public Bitstring greaterThan(String[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of string?");
    }

    public Bitstring greaterThan(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a scalar?");
    }

    public Bitstring greaterThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison between two vectors?");
    }

    public Bitstring greaterThan(Matrix me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a matrix?");
    }

    public Bitstring lessThan(Attribute me) {
        return me.greaterThan(myLabel, myVals);
    }

    public Bitstring lessThan(long me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a long?");
    }

    public Bitstring lessThan(double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a double?");
    }

    public Bitstring lessThan(String me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a string?");
    }

    public Bitstring lessThan(long[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of long?");
    }

    public Bitstring lessThan(double[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of double?");
    }

    public Bitstring lessThan(String[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of string?");
    }

    public Bitstring lessThan(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a scalar?");
    }

    public Bitstring lessThan(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison between two vectors?");
    }

    public Bitstring lessThan(Matrix me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a matrix?");
    }

    public Bitstring greaterThanOrEqual(Attribute me) {
        return me.lessThanOrEqual(myLabel, myVals);
    }

    public Bitstring greaterThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a long?");
    }

    public Bitstring greaterThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a double?");
    }

    public Bitstring greaterThanOrEqual(String me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a string?");
    }

    public Bitstring greaterThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of long?");
    }

    public Bitstring greaterThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of double?");
    }

    public Bitstring greaterThanOrEqual(String[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of string?");
    }

    public Bitstring greaterThanOrEqual(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a scalar?");
    }

    public Bitstring greaterThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison between two vectors?");
    }

    public Bitstring greaterThanOrEqual(Matrix me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a matrix?");
    }

    public Bitstring lessThanOrEqual(Attribute me) {
        return me.greaterThanOrEqual(myLabel, myVals);
    }

    public Bitstring lessThanOrEqual(long me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a long?");
    }

    public Bitstring lessThanOrEqual(double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a double?");
    }

    public Bitstring lessThanOrEqual(String me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a string?");
    }

    public Bitstring lessThanOrEqual(long[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of long?");
    }

    public Bitstring lessThanOrEqual(double[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of double?");
    }

    public Bitstring lessThanOrEqual(String[] me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and an array of string?");
    }

    public Bitstring lessThanOrEqual(int label, double me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a scalar?");
    }

    public Bitstring lessThanOrEqual(int label, double[] me) {
        throw new RuntimeException("Why are you doing a comparison between two vectors?");
    }

    public Bitstring lessThanOrEqual(Matrix me) {
        throw new RuntimeException("Why are you doing a comparison of a vector and a matrix?");
    }

    public void injectSelf(Function f) {
        f.inject(myVals, myLabel);
    }

    public boolean allAreEqual() {

        return true;
        // return myVals.length == 1;
        /***
         for (int i=1;i<myVals.length;i++) {
         if (myVals[i] != myVals[0])
         return false;
         }

         return true;
         ***/
    }

    public String print(int maxLen) {
        String ret = "";
        ret += "Label: ";
        ret += String.format("%d", myLabel);
        ret += ", ";
        ret += "Value: ";
        for (int i = 0; i < myVals.length; i++) {
            if (i > 0) {
                ret += ", ";
            }
            ret += String.format("%.2f", myVals[i]);

            if (ret.length() > maxLen && maxLen > 4) {
                return ret.substring(0, maxLen - 4) + "...";
            }
        }

        return ret;
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