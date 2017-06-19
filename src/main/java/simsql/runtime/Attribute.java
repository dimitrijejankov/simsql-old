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
import java.util.HashMap;

/**
 * This class is the basic attribute type.  Specific, concrete classes that
 * are implemented support ints, doubles, strings, and NULL values.
 * <p>
 * Note that this is an immutable class; all of the operations should return
 * a new Attribute object with the result of the computation, as oppsed to a
 * modified Attribute object
 */
public interface Attribute extends Serializable {

  /**
   * Returns the attribute in singleton form -- if this is an array
   * with multiple MC iterations, return the first one.
   */
  Attribute getSingleton();

  /**
   * returns the type of the attriubte in the ith MC iteration; This always returns
   * NULL if there is no valid address for the specified mcIteration
   */
  AttributeType getType(int mcIteration);

  /**
   * returns a binary version of the value associated with this attribute...
   * throws an exception if the attribute has a null value in the given MC iteration.
   * Throws an exception if the type of the attribute for that particular MC iteration
   * is NULL. The version with an AttributeType attribute performs casting.
   */
  byte[] getValue(int mcIteration);

  byte[] getValue(int mcIteration, AttributeType castTo);
  // byte [] getAll (int mcIteration);

  /**
   * returns the number of possible worlds (1 for constant attributes)
   */
  int getSize();

  /**
   * Serializes the attribute to the given stream, returning the number of bytes written
   */
  int writeSelfToStream(DataOutputStream writeToMe) throws IOException;

  /**
   * This creates a version of the Attribute that has no NULL vlaues... the values that
   * are left behind (where the NULLs used to be) are arbitrary
   */
  Attribute removeNulls();

  /**
   * This deserializes the attribute from the specified input stream, returning the number
   * of bytes read.
   */
  int readSelfFromStream(DataInputStream readFromMe) throws IOException;

  /**
   * This reads the attribute (as text) from a BufferedReader; it is assumed that the reading
   * starts at the current byte in the reader, and continues up to (and including) a pipe
   * character '|' which marks the end of the attribute value.  The attribute is read into
   * a brand new Attribute object (which is created by the method) and returned to the caller.
   * A null is returned at an EOF, and an exception is thrown if there is any error.  Array
   * attributes should be written as <val|val|val|val|>.  So (for example) say that you had
   * a file describing tuples where each tuple had an int, a string, and an array of ints.
   * The file might look like:
   * <p>
   * 123|This is my string!|<1|null|3|4|>|
   * 124|This is another string!|<5|6|null|8|>|
   */
  Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException;

  /**
   * This is basically the reverse of the above operation, and it writes the attribute to
   * a text stream, ending the attribute value with a pipe character '|'
   */
  void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException;

  /**
   * This writes the attribute to a text stream, writing a null as specified by the thisOneIsNull
   * attribute, ending the attribute value with a pipe character '|'
   */
  void writeSelfToTextStream(Bitstring theseOnesAreNull, BufferedWriter writeToMe) throws IOException;

  /**
   * returns a bitstring with a "T" in every position corresponding to a
   * possible world where this particular attribute value is NULL
   */
  Bitstring isNull();

  /**
   * sets the attribute value to NULL in every possible world where there
   * is a "T" in theseOnes.  If there are already any worlds for which the
   * value is null, it will remain NULL after the call.
   */
  Attribute setNull(Bitstring theseOnes);

  /**
   * hashes the attribute; this should never be called on array/stochastic/NULL attributes (if
   * it is, an exception will result)
   */
  long getHashCode();

  /**
   * splits an attribute into a map between constant attributes and their
   * corresponding bitstrings
   */
  HashMap<Attribute, Bitstring> split();

  /**
   * Computes "this + me" and returns the result.  The computation is performed
   * simultanously in all of the possible worlds.  If a singleton constant value
   * is given (such as "String me") then this value is assumed to be constant in
   * all of the possible worlds.  Note that the first four versions of the op
   * will be used the vast majority of the time in the execution engine
   */
  Attribute add(Attribute me);

  Attribute add(long me);

  Attribute add(double me);

  Attribute add(String me);

  Attribute add(int label, double me);

  Attribute add(int label, double[] me);

  Attribute add(Matrix me);

  Attribute add(long[] me);

  Attribute add(double[] me);

  Attribute add(String[] me);

  /**
   * Computes "me + this" and returns the result (note this is the reverse of
   * "this + me", hence the "R" in the method name). Note that addition is not
   * necessarily commutative (eg. over strings), which is why we need this version
   * of the operation.  There is no need for an "addR" version that works on two
   * "Attribute" objects, because you can just switch the order of the objects
   * to get the desired result.
   */
  Attribute addR(long me);

  Attribute addR(double me);

  Attribute addR(String me);

  Attribute addR(int label, double me);

  Attribute addR(int label, double[] me);

  Attribute addR(Matrix me);

  Attribute addR(long[] me);

  Attribute addR(double[] me);

  Attribute addR(String[] me);

  /**
   * Computes "this - me" and returns the result
   */
  Attribute subtract(Attribute subtractThisOut);

  Attribute subtract(long subtractThisOut);

  Attribute subtract(double subtractThisOut);

  Attribute subtract(int label, double me);

  Attribute subtract(int label, double[] me);

  Attribute subtract(Matrix me);

  Attribute subtract(long[] subtractThisOut);

  Attribute subtract(double[] subtractThisOut);

  /**
   * Computes "me - this" and returns the result
   */
  Attribute subtractR(long subtractFromMe);

  Attribute subtractR(double subtractFromMe);

  Attribute subtractR(int label, double me);

  Attribute subtractR(int label, double[] me);

  Attribute subtractR(Matrix me);

  Attribute subtractR(long[] subtractFromMe);

  Attribute subtractR(double[] subtractFromMe);

  /**
   * Computes "this * me" and returns the result.  Since it is assumed that
   * equality always commutes, there are no "R" versions of these ops, because
   * it does not matter in which order you provide the parameters
   */
  Attribute multiply(Attribute byMe);

  Attribute multiply(long byMe);

  Attribute multiply(double byMe);

  Attribute multiply(int label, double me);

  Attribute multiply(int label, double[] me);

  Attribute multiply(Matrix me);

  Attribute multiply(long[] byMe);

  Attribute multiply(double[] byMe);

  /**
   * Computes "this / me" and returns the result.
   */
  Attribute divide(Attribute byMe);

  Attribute divide(long byMe);

  Attribute divide(double byMe);

  Attribute divide(int label, double me);

  Attribute divide(int label, double[] me);

  Attribute divide(Matrix me);

  Attribute divide(long[] byMe);

  Attribute divide(double[] byMe);

  /**
   * Computes "me / this" and returns the result.
   */
  Attribute divideR(long divideMe);

  Attribute divideR(double divideMe);

  Attribute divideR(int label, double me);

  Attribute divideR(int label, double[] me);

  Attribute divideR(Matrix me);

  Attribute divideR(long[] divideMe);

  Attribute divideR(double[] divideMe);

  /**
   * Compute "this == me" and returns the result.  Since it is assumed that
   * equality always commutes, there is no "R" version of the various ops.
   */
  Bitstring equals(Attribute me);

  Bitstring equals(long me);

  Bitstring equals(double me);

  Bitstring equals(String me);

  Bitstring equals(int label, double me);

  Bitstring equals(int label, double[] me);

  Bitstring equals(Matrix me);

  Bitstring equals(long[] me);

  Bitstring equals(double[] me);

  Bitstring equals(String[] me);

  /**
   * Compute "this != me" and returns the result.  Since it is assumed that
   * equality always commutes, there is no "R" version of the various ops.
   */
  Bitstring notEqual(Attribute me);

  Bitstring notEqual(long me);

  Bitstring notEqual(double me);

  Bitstring notEqual(String me);

  Bitstring notEqual(int label, double me);

  Bitstring notEqual(int label, double[] me);

  Bitstring notEqual(Matrix me);

  Bitstring notEqual(long[] me);

  Bitstring notEqual(double[] me);

  Bitstring notEqual(String[] me);

  /**
   * Compute "this > me" and return the result.  Note that there is no need
   * for the "R" version of this op, because doing "me > this" is simply a call
   * to "this.lessThan (me)".
   */
  Bitstring greaterThan(Attribute me);

  Bitstring greaterThan(long me);

  Bitstring greaterThan(double me);

  Bitstring greaterThan(String me);

  Bitstring greaterThan(int label, double me);

  Bitstring greaterThan(int label, double[] me);

  Bitstring greaterThan(Matrix me);

  Bitstring greaterThan(long[] me);

  Bitstring greaterThan(double[] me);

  Bitstring greaterThan(String[] me);

  /**
   * Compute "this < me" and return the result.  Note that again there is no need
   * for the "R" version of this op.
   */
  Bitstring lessThan(Attribute me);

  Bitstring lessThan(long me);

  Bitstring lessThan(double me);

  Bitstring lessThan(String me);

  Bitstring lessThan(int label, double me);

  Bitstring lessThan(int label, double[] me);

  Bitstring lessThan(Matrix me);

  Bitstring lessThan(long[] me);

  Bitstring lessThan(double[] me);

  Bitstring lessThan(String[] me);

  /**
   * Compute "this >= me" and return the result.  Note that again there is no need
   * for the "R" version of this op.
   */
  Bitstring greaterThanOrEqual(Attribute me);

  Bitstring greaterThanOrEqual(long me);

  Bitstring greaterThanOrEqual(double me);

  Bitstring greaterThanOrEqual(String me);

  Bitstring greaterThanOrEqual(int label, double me);

  Bitstring greaterThanOrEqual(int label, double[] me);

  Bitstring greaterThanOrEqual(Matrix me);

  Bitstring greaterThanOrEqual(long[] me);

  Bitstring greaterThanOrEqual(double[] me);

  Bitstring greaterThanOrEqual(String[] me);

  /**
   * Compute "this >= me" and return the result.  Note that again there is no need
   * for the "R" version of this op.
   */
  Bitstring lessThanOrEqual(Attribute me);

  Bitstring lessThanOrEqual(long me);

  Bitstring lessThanOrEqual(double me);

  Bitstring lessThanOrEqual(String me);

  Bitstring lessThanOrEqual(int label, double me);

  Bitstring lessThanOrEqual(int label, double[] me);

  Bitstring lessThanOrEqual(Matrix me);

  Bitstring lessThanOrEqual(long[] me);

  Bitstring lessThanOrEqual(double[] me);

  Bitstring lessThanOrEqual(String[] me);

  /**
   * Injects the contents of the attribute into a given function.
   */
  void injectSelf(Function f);

  /**
   * Returns true if all the iterations have the same value.
   * Defaults true for singleton attributes.
   */

  boolean allAreEqual();

  /**
   * Returns a pretty-printed representation, with a maximum length.
   */
  String print(int maxLen);

  String print(int maxLen, Bitstring theseAreNull);

  /**
   * Semi-optional operation to recycle an attribute instance for future re-use.
   */
  void recycle();

}
