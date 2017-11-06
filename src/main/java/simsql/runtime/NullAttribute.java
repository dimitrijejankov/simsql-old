

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
 * This implements an attribute whose value is null
 */
import java.util.*;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.PrintStream;
import java.io.IOException;

public class NullAttribute implements Attribute {

  // a single instance.
  public static final NullAttribute NULL = new NullAttribute();

  public NullAttribute () {}

  public int getSize () {
  	return 0;
  }

  public void recycle() {
  }
  
  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {
    throw new IOException ("It makes no sense to try to read text into a NullAttribute object");
  }

  public void writeSelfToTextStream (BufferedWriter writeToMe) throws IOException {
    writeToMe.write ("null|");
  }

  public void writeSelfToTextStream (Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {
    writeToMe.write ("null|");
  }

  public byte [] getValue (int whichMC) {
    throw new RuntimeException ("you tried to get the value assoiated with a NULL");
  }

  public byte [] getValue (int whichMC, AttributeType castTo) {
    if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) 
      return getValue(whichMC);
    else throw new RuntimeException("Invalid cast when writing out value.");
  }

  
  public AttributeType getType (int whichMC) {
    return new AttributeType(new NullType());  
  }
  
  public Bitstring isNull () {
    return BitstringWithSingleValue.TRUE;
  }
  
  public Attribute removeNulls () {
    return IntAttribute.ZERO;
  }
    
  public int writeSelfToStream (DataOutputStream writeToMe) {
    // do nothing!
    return 0;
  }

  public long getHashCode () {
    return 0; 
  }

  public HashMap<Attribute, Bitstring> split () {
    return null;
  }
    
  public int readSelfFromStream (DataInputStream readFromMe) {
    // do nothing!
    return 0;
  }
  
  public Attribute setNull (Bitstring theseOnes) {
    return this;
  }
  
  public Attribute add (Attribute me) {
    return this;
  }
  
  public Attribute add (long addThisIn) {
    return this;
  }
  
  public Attribute addR (long addThisIn) {
    return this;
  }
  
  public Attribute add (long [] addThisIn) {
    return this;
  }
  
  public Attribute addR (long [] addThisIn) {
    return this;
  }
  
  public Attribute add (String [] addThisIn) {
    return this;
  }
  
  public Attribute addR (String [] addThisIn) {
    return this;
  }
  
  public Attribute add (String addThisIn) {
    return this;
  }
  
  public Attribute addR (String addThisIn) {
    return this;
  }
  
  public Attribute add (double [] addThisIn) {
    return this;
  }
  
  public Attribute addR (double [] addThisIn) {
    return this;
  }
  
  public Attribute add (double addThisIn) {
    return this;
  }
  
  public Attribute addR (double addThisIn) {
    return this;
  }
  
  public Attribute add (int label, double addThisIn) {
    return this;
  }
  
  public Attribute addR (int label, double addThisIn) {
    return this;
  }
  
  public Attribute add (int label, double [] addThisIn) {
    return this;
  }
  
  public Attribute addR (int label, double [] addThisIn) {
    return this;
  }
  
  public Attribute add (boolean ifRow, double [][] addThisIn) {
    return this;
  }
  
  public Attribute addR (boolean ifRow, double [][] addThisIn) {
    return this;
  }
  
  public Attribute subtract (Attribute me) {
    return this;
  }
  
  public Attribute subtract (long subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (long subtractFromMe) {
    return this;
  }
  
  public Attribute subtract (long [] subtractMeOut) {
    return this;
  }
  
  public Attribute subtractR (long [] subtractFromMe) {
    return this;
  }
  
  public Attribute subtract (double [] subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (double [] subtractFromMe) {
    return this;
  }
  
  public Attribute subtract (double subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (double subtractFromThis) {
    return this;
  }
  
  public Attribute subtract (int label, double subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (int label, double subtractFromThis) {
    return this;
  }
  
  public Attribute subtract (int label, double [] subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (int label, double [] subtractFromThis) {
    return this;
  }
  
  public Attribute subtract (boolean ifRow, double [][] subtractThisOut) {
    return this;
  }
  
  public Attribute subtractR (boolean ifRow, double [][] subtractFromThis) {
    return this;
  }
  
  public Attribute multiply (Attribute byMe) {
    return this;
  }
  
  public Attribute multiply (long byMe) {
    return this;
  }
  
  public Attribute multiply (double byMe) {
    return this;
  }
  
  public Attribute multiply (long [] byMe) {
    return this;
  }
  
  public Attribute multiply (double [] byMe) {
    return this;
  }
  
  public Attribute multiply (int label, double byMe) {
    return this;
  }
  
  public Attribute multiply (int label, double [] byMe) {
    return this;
  }
  
  public Attribute multiply (boolean ifRow, double [][] byMe) {
    return this;
  }
  
  public Attribute divide (Attribute byMe) {
    return this;
  }
  
  public Attribute divide (long byMe) {
    return this;
  }
  
  public Attribute divide (double byMe) {
    return this;
  }
  
  public Attribute divide (long [] byMe) {
    return this;
  }
  
  public Attribute divide (double [] byMe) {
    return this;
  }
  
  public Attribute divide (int label, double byMe) {
    return this;
  }
  
  public Attribute divide (int label, double [] byMe) {
    return this;
  }
  
  public Attribute divide (boolean ifRow, double [][] byMe) {
    return this;
  }
  
  public Attribute divideR (long divideMe) {
    return this;
  }
  
  public Attribute divideR (double divideMe) {
    return this;
  }
  
  public Attribute divideR (long [] divideMe) {
    return this;
  }
  
  public Attribute divideR (double [] divideMe) {
    return this;
  }
  
  public Attribute divideR (int label, double divideMe) {
    return this;
  }
  
  public Attribute divideR (int label, double [] divideMe) {
    return this;
  }
  
  public Attribute divideR (boolean ifRow, double [][] divideMe) {
    return this;
  }
  
  public Bitstring equals (Attribute me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring equals (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (Attribute me)  {
    return BitstringUnknown.UNKNOWN;
  }
   
  public Bitstring notEqual (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring notEqual (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (Attribute me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThan (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }

  public Bitstring lessThan (Attribute me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  }  
  
  public Bitstring lessThan (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThan (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (Attribute me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring greaterThanOrEqual (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }

  public Bitstring lessThanOrEqual (Attribute me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (long me) {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (String me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (long [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (String [] me)  {
    return BitstringUnknown.UNKNOWN;
  } 
  
  public Bitstring lessThanOrEqual (int label, double me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (int label, double [] me)  {
    return BitstringUnknown.UNKNOWN;
  }
  
  public Bitstring lessThanOrEqual (boolean ifRow, double [][] me)  {
    return BitstringUnknown.UNKNOWN;
  }

  public void injectSelf(Function f) {

      // doesn't inject any value, but puts an "unknown."
      f.injectBitstring(BitstringWithSingleValue.TRUE);
  }

  public boolean allAreEqual() {
      return true;
  }

  public String print(int maxLen) {
    return "null";
  }

  public String print(int maxLen, Bitstring theseAreNull) {
    return "null";
  }

  public Attribute getSingleton() {
    return this;
  }
}
