

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
 * This class implements an Attribute that is stored as an array,
 * where the value in one more more of the worlds is NULL
 */

import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.BufferedWriter;


public class ArrayAttributeWithNulls implements Attribute {

    // there is a one in every position that has a null
    private Bitstring theseOnesAreNull;

    // this is the array of values
    private Attribute myAttribute;

    // these are the machines to read/write the above two members
    static BitstringIOMachine bitstringMachine = new BitstringIOMachine();
    static AttributeIOMachine attributeMachine = new AttributeIOMachine();

    public void recycle() {
    }

    public ArrayAttributeWithNulls() {
    }

    public Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException {
        throw new IOException("It makes no sense to try to read text into an ArrayAttributeWithNulls object");
    }

    public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {
        myAttribute.writeSelfToTextStream(theseOnesAreNull, writeToMe);
    }

    public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

        // in this case, we write a null if indicated by either theseAreNull or theseOnesAreNull
        myAttribute.writeSelfToTextStream(theseOnesAreNull.or(theseAreNull), writeToMe);
    }


    public long writeSelfToStream(DataOutputStream writeToMe) throws IOException {
        long returnVal = bitstringMachine.writeToStream(theseOnesAreNull, writeToMe);
        return returnVal + AttributeIOMachine.writeToStream(myAttribute, writeToMe);
    }

    public Attribute removeNulls() {
        return myAttribute;
    }

    public long getHashCode() {
        throw new RuntimeException("Can't hash an array attribute!");
    }

    public int getSize() {
        return myAttribute.getSize();
    }

    public byte[] injectValue(int whichMCIter) {
        if (!theseOnesAreNull.getValue(whichMCIter)) {
            return myAttribute.injectValue(whichMCIter);
        } else {
            throw new RuntimeException("you tried to get a value that was NULL");
        }
    }

    public void injectIntoBuffer(int whichMCIter, AttributeType castTo, LargeByteBuffer buffer) {
        if (!theseOnesAreNull.getValue(whichMCIter)) {
            myAttribute.injectIntoBuffer(whichMCIter, castTo, buffer);
        } else {
            throw new RuntimeException("you tried to get a value that was NULL");
        }
    }

    public AttributeType getType(int whichMCIter) {
        if (!theseOnesAreNull.getValue(whichMCIter)) {
            return myAttribute.getType(whichMCIter);
        } else {
            return new AttributeType(new NullType());
        }
    }

    public long readSelfFromStream(DataInputStream readFromMe) throws IOException {
        WriteableInt numBytes = new WriteableInt();
        theseOnesAreNull = bitstringMachine.readFromStream(readFromMe, numBytes);
        int totRead = (int) numBytes.get();
        myAttribute = attributeMachine.readFromStream(readFromMe, numBytes, numBytes);
        return totRead + (int) numBytes.get();
    }

    public ArrayAttributeWithNulls(Bitstring nullOnes, Attribute fromMe) {
        theseOnesAreNull = nullOnes.or(fromMe.isNull());
        myAttribute = fromMe;
    }


    public Bitstring isNull() {
        return theseOnesAreNull;
    }

    public Attribute setNull(Bitstring theseOnes) {
        Bitstring temp = theseOnes.or(theseOnesAreNull);
        if (temp.allAreTrue()) {
            return NullAttribute.NULL;
        } else {
            return new ArrayAttributeWithNulls(temp, this);
        }
    }

    public HashMap<Attribute, Bitstring> split() {
        HashMap<Attribute, Bitstring> splits = myAttribute.split();
        for (Attribute att : splits.keySet()) {
            splits.put(att, splits.get(att).and(theseOnesAreNull.not()));
        }
        return splits;
    }

    public Attribute add(Attribute me) {
        Attribute res = myAttribute.add(me);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(long addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(long addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(long[] addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(long[] addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(String[] addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(String[] addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(String addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(String addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(double[] addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(double[] addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(double addThisIn) {
        Attribute res = myAttribute.add(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(double addThisIn) {
        Attribute res = myAttribute.addR(addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(int label, double addThisIn) {
        Attribute res = myAttribute.add(label, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(int label, double addThisIn) {
        Attribute res = myAttribute.addR(label, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(int label, double[] addThisIn) {
        Attribute res = myAttribute.add(label, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(int label, double[] addThisIn) {
        Attribute res = myAttribute.addR(label, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute add(boolean ifRow, double[][] addThisIn) {
        Attribute res = myAttribute.add(ifRow, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute addR(boolean ifRow, double[][] addThisIn) {
        Attribute res = myAttribute.addR(ifRow, addThisIn);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(Attribute subtractThisOut) {
        Attribute res = myAttribute.subtract(subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(long subtractThisOut) {
        Attribute res = myAttribute.subtract(subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(long subtractFromMe) {
        Attribute res = myAttribute.subtractR(subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(long[] subtractMeOut) {
        Attribute res = myAttribute.subtract(subtractMeOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(long[] subtractFromMe) {
        Attribute res = myAttribute.subtractR(subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(double[] subtractThisOut) {
        Attribute res = myAttribute.subtract(subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(double[] subtractFromMe) {
        Attribute res = myAttribute.subtractR(subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(double subtractThisOut) {
        Attribute res = myAttribute.subtract(subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(double subtractFromThis) {
        Attribute res = myAttribute.subtractR(subtractFromThis);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(int label, double subtractThisOut) {
        Attribute res = myAttribute.subtract(label, subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(int label, double subtractFromMe) {
        Attribute res = myAttribute.subtractR(label, subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(int label, double[] subtractThisOut) {
        Attribute res = myAttribute.subtract(label, subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(int label, double[] subtractFromMe) {
        Attribute res = myAttribute.subtractR(label, subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtract(boolean ifRow, double[][] subtractThisOut) {
        Attribute res = myAttribute.subtract(ifRow, subtractThisOut);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute subtractR(boolean ifRow, double[][] subtractFromMe) {
        Attribute res = myAttribute.subtractR(ifRow, subtractFromMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(Attribute byMe) {
        Attribute res = myAttribute.multiply(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(long byMe) {
        Attribute res = myAttribute.multiply(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(double byMe) {
        Attribute res = myAttribute.multiply(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(long[] byMe) {
        Attribute res = myAttribute.multiply(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(double[] byMe) {
        Attribute res = myAttribute.multiply(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(int label, double byMe) {
        Attribute res = myAttribute.multiply(label, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(int label, double[] byMe) {
        Attribute res = myAttribute.multiply(label, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute multiply(boolean ifRow, double[][] byMe) {
        Attribute res = myAttribute.multiply(ifRow, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(Attribute byMe) {
        Attribute res = myAttribute.divide(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(long byMe) {
        Attribute res = myAttribute.divide(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(double byMe) {
        Attribute res = myAttribute.divide(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(long[] byMe) {
        Attribute res = myAttribute.divide(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(double[] byMe) {
        Attribute res = myAttribute.divide(byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(int label, double byMe) {
        Attribute res = myAttribute.divide(label, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(int label, double[] byMe) {
        Attribute res = myAttribute.divide(label, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divide(boolean ifRow, double[][] byMe) {
        Attribute res = myAttribute.divide(ifRow, byMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(long divideMe) {
        Attribute res = myAttribute.divideR(divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(double divideMe) {
        Attribute res = myAttribute.divideR(divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(long[] divideMe) {
        Attribute res = myAttribute.divideR(divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(double[] divideMe) {
        Attribute res = myAttribute.divideR(divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(int label, double divideMe) {
        Attribute res = myAttribute.divideR(label, divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(int label, double[] divideMe) {
        Attribute res = myAttribute.divideR(label, divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Attribute divideR(boolean ifRow, double[][] divideMe) {
        Attribute res = myAttribute.divideR(ifRow, divideMe);
        return res.setNull(theseOnesAreNull);
    }

    public Bitstring equals(Attribute me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(long me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(double me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(String me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(long[] me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(double[] me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(String[] me) {
        Bitstring res = myAttribute.equals(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(int label, double me) {
        Bitstring res = myAttribute.equals(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(int label, double[] me) {
        Bitstring res = myAttribute.equals(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring equals(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.equals(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(Attribute me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(long me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(double me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(String me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(long[] me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(double[] me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(String[] me) {
        Bitstring res = myAttribute.notEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(int label, double me) {
        Bitstring res = myAttribute.notEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(int label, double[] me) {
        Bitstring res = myAttribute.notEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring notEqual(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.notEqual(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(Attribute me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(long me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(double me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(String me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(long[] me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(double[] me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(String[] me) {
        Bitstring res = myAttribute.greaterThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(int label, double me) {
        Bitstring res = myAttribute.greaterThan(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(int label, double[] me) {
        Bitstring res = myAttribute.greaterThan(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThan(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.greaterThan(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(Attribute me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(long me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(double me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(String me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(long[] me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(double[] me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(String[] me) {
        Bitstring res = myAttribute.lessThan(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(int label, double me) {
        Bitstring res = myAttribute.lessThan(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(int label, double[] me) {
        Bitstring res = myAttribute.lessThan(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThan(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.lessThan(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(Attribute me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(long me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(double me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(String me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(long[] me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(double[] me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(String[] me) {
        Bitstring res = myAttribute.greaterThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(int label, double me) {
        Bitstring res = myAttribute.greaterThanOrEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(int label, double[] me) {
        Bitstring res = myAttribute.greaterThanOrEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring greaterThanOrEqual(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.greaterThanOrEqual(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(Attribute me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(long me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(double me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(String me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(long[] me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(double[] me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(String[] me) {
        Bitstring res = myAttribute.lessThanOrEqual(me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(int label, double me) {
        Bitstring res = myAttribute.lessThanOrEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(int label, double[] me) {
        Bitstring res = myAttribute.lessThanOrEqual(label, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public Bitstring lessThanOrEqual(boolean ifRow, double[][] me) {
        Bitstring res = myAttribute.lessThanOrEqual(ifRow, me);
        return res.setUnknown(theseOnesAreNull);
    }

    public void injectSelf(Function f) {

        // inject the attribute contents
        myAttribute.injectSelf(f);

        // and the bitstring.
        f.injectBitstring(theseOnesAreNull);
    }

    public boolean allAreEqual() {
        return myAttribute.allAreEqual();
    }

    public String print(int maxLen) {
        return print(maxLen, theseOnesAreNull);
    }

    public String print(int maxLen, Bitstring theseAreNull) {
        return myAttribute.print(maxLen, theseAreNull.or(theseOnesAreNull));
    }

    public Attribute getSingleton() {
        return myAttribute.getSingleton();
    }
}
