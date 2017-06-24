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

import org.apache.hadoop.yarn.webapp.hamlet.HamletSpec;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Implements the matrix type, with the same value in every world.
 */
public class MatrixAttribute implements Attribute, Serializable {

	/**
	 * this allows us to return the address as a byte arraycolumnmatrix
	 */
	private static ByteBuffer b = ByteBuffer.allocate(9);

	static {
		b.order(ByteOrder.nativeOrder());
	}

	/**
	 * Default constructor
	 */
	public MatrixAttribute() {}

	/**
	 * The matrix address
	 */
	private transient Matrix matrix;

	/**
	 * constructs an instance of the matrix attribute from a double[][]
	 *
	 * @param fromMe the address
	 */
	public MatrixAttribute(double[][] fromMe) {
		// initialize a new matrix
		this.matrix = new Matrix(fromMe.length, fromMe[0].length, true, fromMe);
	}

	/**
	 * Create a matrix from address
	 *
	 * @param indicateIfRow is true if this is a row matrix
	 * @param fromMe        the raw address to initialize
	 */
	public MatrixAttribute(boolean indicateIfRow, double[][] fromMe) {
		// initialize a new matrix
		this.matrix = new Matrix(fromMe.length, fromMe[0].length, indicateIfRow, fromMe);
	}

	/**
	 * Create a matrix from address
	 *
	 * @param indicateIfRow is true if this is a row matrix
	 * @param fromMe        the raw address to initialize
	 * @param dim2          the second dimension of the matrix
	 */
	public MatrixAttribute(boolean indicateIfRow, double[][] fromMe, int dim2) {
		// initialize a new matrix
		this.matrix = new Matrix(fromMe.length, dim2, indicateIfRow, fromMe);
	}

	/**
	 * Creates a shallow copy of the address
	 *
	 * @param matrix a reference of the matrix
	 */
	public MatrixAttribute(Matrix matrix) {
		this.matrix = matrix;
	}

	/**
	 * We don't do anything in the recycle method
	 */
	public void recycle() {
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

	/**
	 * Reads a MatrixAttribute from a bufferedReader (string)
	 *
	 * @param readFromMe the buffer
	 * @return the matrix attribute we just have read
	 * @throws IOException when the parsing format is not right
	 */
	public Attribute readSelfFromTextStream(BufferedReader readFromMe) throws IOException {

		// this is the space we'll use to do the parsing... we assume it is less than 64 chars
		char[] myArray = new char[64];

		// allows us to match the word "null"
		char[] nullString = {'n', 'u', 'l', 'l'};

		// records the doubles and nulls we've read in
		ArrayList<ArrayList<Double>> myDoubles = new ArrayList<>(64);

		// suck in the '[', but ignore a leading newline character
		int startChar = readFromMe.read();
		while (startChar == '\n')
			startChar = readFromMe.read();

		// if we got an eof, we are done
		if (startChar == -1) {
			return null;
		}

		// check if the matrix begins with a '[' character
		if (startChar != '[') {
			throw new IOException("Read in a bad matrix start character when reading a vector; expected '['");
		}

		// this tells us we did not see a null anywhere in the input
		boolean isNull = true;

		// read in the first char
		myArray[0] = (char) startChar;

		while (myArray[0] == '[') {
			myArray[0] = (char) readFromMe.read();
			ArrayList<Double> myRow = new ArrayList<>(64);

			// keep reading until we find the ']'
			while (myArray[0] != ']') {

				// this loop reads in a row
				int i;
				for (i = 1; myArray[i - 1] != ',' && myArray[i - 1] != ']'; i++) {

					// isNull gets set to false if we find a char that does not match the string 'null'
					if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
						isNull = false;
					}
					myArray[i] = (char) readFromMe.read();
				}

				// if we got here, we read in a ','
				if (isNull && i == 5) {
					if (readFromMe.read() != '|') {
						throw new IOException("Error when I tried to read in a matrix: didn't close with a '|'");
					}
					return NullAttribute.NULL;
				} else {

					// we did not get a null!
					try {
						Double temp = Double.valueOf(new String(myArray, 0, i - 1));
						myRow.add(temp);
					} catch (Exception e) {
						throw new IOException("Error when I tried to read in a matrix... an entry didn't parse to a double");
					}
				}

				// prime the parse of the next item in the row
				if (myArray[i - 1] != ']')
					myArray[0] = (char) readFromMe.read();
				else
					break;
			}

			myDoubles.add(myRow);

			// start to read the next row
			myArray[0] = (char) readFromMe.read();
		}

		// suck in the final '|'
		if (myArray[0] != '|') {
			throw new IOException("Error when I tried to read in a matrix: didn't close with a '|'");
		}

		// at this point we've read the entire matrix, so make an attribute out of it
		double[][] myDoubleMatrix = new double[myDoubles.size()][];
		for (int i = 0; i < myDoubles.size(); i++) {
			if (myDoubles.get(i) != null) {
				myDoubleMatrix[i] = new double[myDoubles.get(i).size()];
				for (int j = 0; j < myDoubles.get(i).size(); j++) {
					myDoubleMatrix[i][j] = myDoubles.get(i).get(j);
				}
			}
		}

		return new MatrixAttribute(true, myDoubleMatrix);
	}


	/**
	 * The matrix written in text will always be row-based
	 * example: [1, 2, 3][0, 0, 0][4, 5, 6] |
	 *
	 * @param writeToMe the buffer where we write the matrix in serialized form
	 */
	public void writeSelfToTextStream(BufferedWriter writeToMe) throws IOException {

		// grab the sizes from the matrix header
		long size1 = matrix.getSize1();
		long size2 = matrix.getSize2();

		// grab the header size
		long headerSize = Matrix.getMatrixHeaderSize();

		// grab the address from the matrix
		long data = matrix.getAddress();

		// depends on the value of ifRow
		if (matrix.getIfRow()) {
			for (int i = 0; i < size1; i++) {
				writeToMe.write("[");

				// write out the values to the
				for (int j = 0; j < size2; j++) {
					String temp = Double.toString(Matrix.unsafe.getDouble(data + headerSize + (i * size2 + j) * Double.BYTES));
					writeToMe.write(temp, 0, temp.length());
					if (j < size2 - 1)
						writeToMe.write(",");
				}

				writeToMe.write("]");
			}
		} else {
			for (int j = 0; j < size2; j++) {
				writeToMe.write("[");

				for (int i = 0; i < size1; i++) {
					String temp = Double.toString(Matrix.unsafe.getDouble(data + headerSize + (i * size1 + j) * Double.BYTES));
					writeToMe.write(temp, 0, temp.length());
					if (i < size1 - 1)
						writeToMe.write(",");
				}

				writeToMe.write("]");
			}
		}
		writeToMe.write("|");
	}

	/**
	 * if the value in theseAreNull[0] is null, we regard the whole matrix
	 * as null otherwise we write the whole thing to string
	 *
	 * @param theseAreNull a bit string whose only value [0] is important
	 * @param writeToMe    the buffer we are writing text
	 * @throws IOException if something happens during writing...
	 */
	public void writeSelfToTextStream(Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

		// if the value in theseAreNull[0] is null, we regard the whole matrix as null
		if (theseAreNull.getValue(0)) {
			writeToMe.write("[");
			writeToMe.write("null", 0, 4);
			writeToMe.write("]");
		} else {
			writeSelfToTextStream(writeToMe);
		}
	}

	/**
	 * Returns the hash code
	 *
	 * @return the hash
	 */
	public long getHashCode() {

		// grab the sizes from the matrix header
		long size1 = matrix.getSize1();
		long size2 = matrix.getSize2();

		// grab the header size
		long headerSize = Matrix.getMatrixHeaderSize();

		// grab the address from the matrix
		long data = matrix.getAddress();

		// long sum of all values
		long sum = 0;

		for (int i = 0; i < size1; i++) {
			// write out the values to the
			for (int j = 0; j < size2; j++) {
				sum = (sum + Matrix.unsafe.getLong(data + headerSize + (i * size2 + j) * Double.BYTES)) % Long.MAX_VALUE;
			}
		}

		// convert the long to a byte array
		ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
		buffer.putLong(sum);

		// calculate the hash
		return Hash.hashMe(buffer.array());
	}

	/**
	 * returns the number of possible worlds (1 for constant attributes)
	 */
	public int getSize() {
		return 1;
	}

	/**
	 * the matrix in getValue will always be row-based
	 *
	 * @param whichMC MC iteration
	 * @return the byte array where the first 8 bytes are the address of the header
	 * and the last byte indicates whether it's a row or column based matrix
	 */
	public byte[] getValue(int whichMC) {

		// set the byte buffer to the begining
		b.position(0);

		// store the address of the matrix header
		b.putLong(matrix.getAddress());

		// store the information if its a row or column matrix
		b.put((byte) (matrix.getIfRow() ? 1 : 0));

		return b.array();
	}

	/**
	 * returns a binary version of the value associated with this attribute...
	 * throws an exception if the attribute has a null value in the given MC iteration.
	 * Throws an exception if the type of the attribute for that particular MC iteration
	 * is NULL. The version with an AttributeType attribute performs casting.
	 */
	public byte[] getValue(int whichMC, AttributeType castTo) {
		if (castTo.getTypeCode() == getType(whichMC).getTypeCode())
			return getValue(whichMC);

		else throw new RuntimeException("Invalid cast when writing out value.");
	}

	public Attribute removeNulls() {
		return this;
	}

	public AttributeType getType(int whichMC) {
		return new AttributeType(new MatrixType());
	}

	/**
	 * TODO the method signature needs to be changed for this method
	 * write the matrix representation to the address output stream
	 * format:
	 * struct {
	 * <p>
	 * boolean ifRow,
	 * long size1,
	 * long size2,
	 * double address[size1][size2]
	 * }
	 * ifRow(boolean), numOfRow/numOfColumn, numOfColumn/numOfRow,
	 * numOfNonZeroRow/numOfNonZeroColumn, nonZeroRow/nonZeroColumn, address
	 * e.g., True, 5, 2, for the matrix
	 * | 0, 0 |
	 * | 0, 0 |
	 * | 1, 2 |
	 * | 0, 0 |
	 * | 3, 4 |
	 *
	 * @param writeToMe the address output stream we write to
	 * @return the size of the address written to the DataOutputStream
	 */
	public int writeSelfToStream(DataOutputStream writeToMe) throws IOException {

		// grab the sizes from the matrix header
		long size1 = matrix.getSize1();
		long size2 = matrix.getSize2();

		// grab the header size
		long headerSize = Matrix.getMatrixHeaderSize();

		// grab the address from the matrix
		long data = matrix.getAddress();

		// the size of the the outputted address
		long returnVal = 0;

		// write boolean to the stream
		writeToMe.writeBoolean(matrix.getIfRow());
		returnVal += 1;

		// here we assume the matrix has the same number of entries in each row/column
		writeToMe.writeLong(matrix.getSize1());
		writeToMe.writeLong(matrix.getSize2());
		returnVal += 16;

		for (int i = 0; i < size1; i++) {
			// write out the values to the
			for (int j = 0; j < size2; j++) {
				writeToMe.writeDouble(Matrix.unsafe.getDouble(data + headerSize + (i * size2 + j) * Double.BYTES));
			}
		}

		returnVal += Double.BYTES * size1 * size2;

		return (int) returnVal;
	}

	/**
	 * @param readFromMe input stream from which we read in the matrix
	 * @return the size of the address we read in
	 * @throws IOException if something goes wrong an IOException is thrown
	 */
	public int readSelfFromStream(DataInputStream readFromMe) throws IOException {

		int returnVal = 0;

		// get if the the matrix is a row matrix
		boolean ifRow = readFromMe.readBoolean();
		returnVal += 1;

		// grab the matrix size
		long size1 = readFromMe.readLong();
		long size2 = readFromMe.readLong();

		// add 2 * 8 bytes to the output
        returnVal += 16;

        // grab the header size
		long headerSize = Matrix.getMatrixHeaderSize();

		// allocate a new matrix
		if (matrix == null || !matrix.reshape(size1, size2)) {
			matrix = new Matrix(size1, size2, ifRow);
		}

		// the pointer of the new matrix
		long data = matrix.getAddress();

		for (int i = 0; i < size1; i++) {
			// write out the values to the
			for (int j = 0; j < size2; j++) {
				Matrix.unsafe.putDouble(data + headerSize + (i * size2 + j) * Double.BYTES, readFromMe.readDouble());
			}
		}

		returnVal += 8 * size1 * size2;

		return returnVal;
	}

	private void writeObject(ObjectOutputStream stream) throws IOException {

		// get the sizes of the matrix
		long size1 = matrix.getSize1();
		long size2 = matrix.getSize2();

		// grab the indicator if the matrix is a row matrix
		boolean ifRow = matrix.getIfRow();

		// write header
		stream.writeLong(size1);
		stream.writeLong(size2);
		stream.writeBoolean(ifRow);

		// write data
		for(long i = 0; i < size1; ++i) {
			for(long j = 0; j < size2; ++j) {
				stream.writeDouble(Matrix.unsafe.getDouble(matrix.getAddress() + Matrix.getMatrixHeaderSize() + (i * size2 + j) * Double.BYTES));
			}
		}
	}

	private void readObject(ObjectInputStream stream) throws ClassNotFoundException, IOException {

		// get the sizes of the matrix
		long size1 = stream.readLong();
		long size2 = stream.readLong();

		// grab ifRow
		boolean ifRow = stream.readBoolean();

		// allocate a new matrix
		matrix = new Matrix(size1, size2, ifRow);

		// the pointer of the new matrix
		long data = matrix.getAddress();

		for (int i = 0; i < size1; i++) {
			// write out the values to the
			for (int j = 0; j < size2; j++) {
				Matrix.unsafe.putDouble(data + Matrix.getMatrixHeaderSize() + (i * size2 + j) * Double.BYTES, stream.readDouble());
			}
		}
	}

	/**
	 * splits an attribute into a map between constant attributes and their
	 * corresponding Bitstring
	 */
	public HashMap<Attribute, Bitstring> split() {
		HashMap<Attribute, Bitstring> splits = new HashMap<>();
		splits.put(new MatrixAttribute(matrix), BitstringWithSingleValue.trueIf(true));
		return splits;
	}

	/**
	 * TODO fix this in the AggregatorMatrix
	 */
	protected Matrix getVal() {
		return matrix;
	}

	/**
	 * Returns true if the matrix attribute is a row based matrix false otherwise
	 *
	 * @return the boolean value
	 */
	protected boolean getIfRow() {
		return matrix.getIfRow();
	}

	/**
	 * marks the matrix as a row or column matrix
	 *
	 * @param ifr true if the matrix attribute is a row based matrix false otherwise
	 */
	protected void setIfRow(boolean ifr) {
		matrix.setIfRow(ifr);
	}

	public Attribute add(Attribute me) {
		return me.addR(matrix);
	}

	public Attribute add(long addThisIn) {
		return add((double)addThisIn);
	}

	public Attribute addR(long addThisIn) {
		return add(addThisIn);
	}

	public Attribute add(long[] addThisIn) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute addR(long[] addThisIn) {
		return add(addThisIn);
	}

	public Attribute add(String[] addThisIn) {
		throw new RuntimeException("You can't add a matrix and an array of string.");
	}

	public Attribute addR(String[] addThisIn) {
		throw new RuntimeException("You can't add a matrix and an array of string.");
	}

	public Attribute add(String addThisIn) {
		throw new RuntimeException("You can't add a matrix and a string.");
	}

	public Attribute addR(String addThisIn) {
		throw new RuntimeException("You can't add a matrix and a string.");
	}

	public Attribute add(double[] addThisIn) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute addR(double[] addThisIn) {
		return add(addThisIn);
	}

	public Attribute add(double addThisIn) {

		// if we are adding a zero there is no need to do anything...
		if(addThisIn == 0) {
			return this;
		}

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddDouble(matrix.getAddress(), 1.0, addThisIn, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute addR(double addThisIn) {
		return add(addThisIn);
	}

	public Attribute add(int label, double addThisIn) {
		return add(addThisIn);
	}

	public Attribute addR(int label, double addThisIn) {
		return add(addThisIn);
	}

	public Attribute add(int label, double[] addThisIn) {

		// add the vector
		matrix.checkDimensions(addThisIn);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddVector(matrix.getAddress(), matrix.getIfRow(), 1.0, addThisIn, 1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute addR(int label, double[] addThisIn) {
		return add(addThisIn);
	}

	// vector + matrix will be always row-based addition
	public Attribute add(Matrix me) {

		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add matrix in native
		Matrix.nativeAddMatrix(matrix.getAddress(), matrix.getIfRow(), 1.0, me.getAddress(), me.getIfRow(), 1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	// The value of ifRow for the result matrix will depend on the value of the ifRow of
	// the frist matrix in addition
	public Attribute addR(Matrix me) {
		return add(me);
	}

	public Attribute subtract(Attribute me) {
		return me.subtractR(matrix);
	}

	public Attribute subtract(long subtractThisOut) {
		return subtract((double)subtractThisOut);
	}

	public Attribute subtractR(long subtractFromMe) {
		return subtractR((double) subtractFromMe);
	}

	public Attribute subtract(long[] subtractMeOut) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute subtractR(long[] subtractFromMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute subtract(double[] subtractThisOut) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute subtractR(double[] subtractFromMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute subtract(double subtractThisOut) {

		// if we are adding a zero there is no need to do anything...
		if(subtractThisOut == 0) {
			return this;
		}

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddDouble(matrix.getAddress(), 1.0, -subtractThisOut, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute subtractR(double subtractFromThis) {

		// if we are adding a zero there is no need to do anything...
		if(subtractFromThis == 0) {
			return this;
		}

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddDouble(matrix.getAddress(), -1.0, subtractFromThis, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute subtract(int label, double subtractThisOut) {
		return subtract(subtractThisOut);
	}

	public Attribute subtractR(int label, double subtractFromThis) {
		return subtractR(subtractFromThis);
	}

	public Attribute subtract(int label, double[] subtractThisOut) {
		// add the vector
		matrix.checkDimensions(subtractThisOut);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddVector(matrix.getAddress(), matrix.getIfRow(), -1.0, subtractThisOut, 1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute subtractR(int label, double[] subtractFromMe) {
		// add the vector
		matrix.checkDimensions(subtractFromMe);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeAddVector(matrix.getAddress(), matrix.getIfRow(), 1.0, subtractFromMe, -1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute subtract(Matrix me) {

		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add matrix in native
		Matrix.nativeAddMatrix(matrix.getAddress(), matrix.getIfRow(), 1.0, me.getAddress(), me.getIfRow(), -1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute subtractR(Matrix me) {
		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add matrix in native
		Matrix.nativeAddMatrix(matrix.getAddress(), matrix.getIfRow(), -1.0, me.getAddress(), me.getIfRow(), 1.0, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute multiply(Attribute byMe) {
		return byMe.multiply(matrix);
	}

	public Attribute multiply(long byMe) {
		return multiply((double)byMe);
	}

	public Attribute multiply(double byMe) {
		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeMultiplyDouble(matrix.getAddress(), byMe, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute multiply(long[] byMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute multiply(double[] byMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute multiply(int label, double byMe) {
		return multiply(byMe);
	}

	public Attribute multiply(int label, double[] byMe) {
		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(byMe);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// multiply matrix in native
		Matrix.nativeMultiplyVector(byMe, matrix.getAddress(), matrix.getIfRow(), m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute multiply(Matrix me) {
		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add matrix in native
		Matrix.nativeMultiplyMatrix(matrix.getAddress(), matrix.getIfRow(), me.getAddress(), me.getIfRow(), m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divide(Attribute byMe) {
		return byMe.divideR(matrix);
	}

	public Attribute divide(long byMe) {
		return divide((double)byMe);
	}

	public Attribute divideR(long divideMe) {
		return divideR((double)divideMe);
	}

	public Attribute divide(long[] byMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute divideR(long[] divideMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute divide(double[] byMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute divideR(double[] divideMe) {
		throw new RuntimeException("This method is not defined!");
	}

	public Attribute divide(double byMe) {
		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeDivideMatrixDouble(matrix.getAddress(), byMe, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divideR(double divideMe) {
		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeDivideDoubleMatrix(divideMe, matrix.getAddress(), m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divide(int label, double byMe) {
		return divide(byMe);
	}

	public Attribute divideR(int label, double divideMe) {
		return divideR(divideMe);
	}

	public Attribute divide(int label, double[] byMe) {
		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(byMe);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// multiply matrix in native
		Matrix.nativeDivideMatrixVector(matrix.getAddress(), matrix.getIfRow(), byMe, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divideR(int label, double[] divideMe) {
		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(divideMe);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// multiply matrix in native
		Matrix.nativeDivideVectorMatrix(matrix.getAddress(), matrix.getIfRow(), divideMe, m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divide(Matrix me) {

		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeDivideMatrixMatrix(matrix.getAddress(), matrix.getIfRow(), me.getAddress(), me.getIfRow(), m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Attribute divideR(Matrix me) {

		// check if we can perform this operation throw runtime exception otherwise
		matrix.checkDimensions(me);

		// allocate matrix
		Matrix m = new Matrix(matrix.getSize1(), matrix.getSize2(), matrix.getIfRow());

		// add double in native
		Matrix.nativeDivideMatrixMatrix(me.getAddress(), me.getIfRow(), matrix.getAddress(), matrix.getIfRow(), m.getAddress());

		// return the matrix attribute
		return new MatrixAttribute(m);
	}

	public Bitstring equals(Attribute me) {
		return me.equals(matrix);
	}

	public Bitstring equals(long me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a long?");
	}

	public Bitstring equals(double me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a double?");
	}

	public Bitstring equals(String me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a string?");
	}

	public Bitstring equals(long[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of long?");
	}

	public Bitstring equals(double[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of double?");
	}

	public Bitstring equals(String[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of string?");
	}

	public Bitstring equals(int label, double me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a scalar?");
	}

	public Bitstring equals(int label, double[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a vector?");
	}

	public Bitstring equals(Matrix me) {
		return BitstringWithSingleValue.trueIf(Matrix.nativeEqual(matrix.getAddress(), matrix.getIfRow(),
				me.getAddress(), me.getIfRow()));
	}

	public Bitstring notEqual(Attribute me) {
		return me.notEqual(matrix);
	}

	public Bitstring notEqual(long me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a long?");
	}

	public Bitstring notEqual(double me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a double?");
	}

	public Bitstring notEqual(String me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a string?");
	}

	public Bitstring notEqual(long[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of long?");
	}

	public Bitstring notEqual(double[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of double?");
	}

	public Bitstring notEqual(String[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and an array of string?");
	}

	public Bitstring notEqual(int label, double me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a scalar?");
	}

	public Bitstring notEqual(int label, double[] me) {
		throw new RuntimeException("Why are you doing an equality check on a matrix and a vector?");
	}

	public Bitstring notEqual(Matrix me) {
		return BitstringWithSingleValue.trueIf(!Matrix.nativeEqual(matrix.getAddress(), matrix.getIfRow(),
				me.getAddress(), me.getIfRow()));
	}

	public Bitstring greaterThan(Attribute me) {
		return me.lessThan(matrix);
	}

	public Bitstring greaterThan(long me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a long?");
	}

	public Bitstring greaterThan(double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a double?");
	}

	public Bitstring greaterThan(String me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a string?");
	}

	public Bitstring greaterThan(long[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of long?");
	}

	public Bitstring greaterThan(double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of double?");
	}

	public Bitstring greaterThan(String[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of string?");
	}

	public Bitstring greaterThan(int label, double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a scalar?");
	}

	public Bitstring greaterThan(int label, double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a vector?");
	}

	public Bitstring greaterThan(Matrix me) {
		throw new RuntimeException("Why are you doing a comparison of tow matrices?");
	}

	public Bitstring lessThan(Attribute me) {
		return me.greaterThan(matrix);
	}

	public Bitstring lessThan(long me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a long?");
	}

	public Bitstring lessThan(double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a double?");
	}

	public Bitstring lessThan(String me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a string?");
	}

	public Bitstring lessThan(long[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of long?");
	}

	public Bitstring lessThan(double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of double?");
	}

	public Bitstring lessThan(String[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of string?");
	}

	public Bitstring lessThan(int label, double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a scalar?");
	}

	public Bitstring lessThan(int label, double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a vector?");
	}

	public Bitstring lessThan(Matrix me) {
		throw new RuntimeException("Why are you doing a comparison between two matrices?");
	}

	public Bitstring greaterThanOrEqual(Attribute me) {
		return me.lessThanOrEqual(matrix);
	}

	public Bitstring greaterThanOrEqual(long me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a long?");
	}

	public Bitstring greaterThanOrEqual(double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a double?");
	}

	public Bitstring greaterThanOrEqual(String me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a string?");
	}

	public Bitstring greaterThanOrEqual(long[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of long?");
	}

	public Bitstring greaterThanOrEqual(double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of double?");
	}

	public Bitstring greaterThanOrEqual(String[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of string?");
	}

	public Bitstring greaterThanOrEqual(int label, double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a scalar?");
	}

	public Bitstring greaterThanOrEqual(int label, double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a vector?");
	}

	public Bitstring greaterThanOrEqual(Matrix me) {
		throw new RuntimeException("Why are you doing a comparison between two matrices?");
	}

	public Bitstring lessThanOrEqual(Attribute me) {
		return me.greaterThanOrEqual(matrix);
	}

	public Bitstring lessThanOrEqual(long me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a long?");
	}

	public Bitstring lessThanOrEqual(double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a double?");
	}

	public Bitstring lessThanOrEqual(String me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a string?");
	}

	public Bitstring lessThanOrEqual(long[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of long?");
	}

	public Bitstring lessThanOrEqual(double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of double?");
	}

	public Bitstring lessThanOrEqual(String[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and an array of string?");
	}

	public Bitstring lessThanOrEqual(int label, double me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a scalar?");
	}

	public Bitstring lessThanOrEqual(int label, double[] me) {
		throw new RuntimeException("Why are you doing a comparison of a matrix and a vector?");
	}

	public Bitstring lessThanOrEqual(Matrix me) {
		throw new RuntimeException("Why are you doing a comparison between two matrices?");
	}

	public void injectSelf(Function f)  {
		f.inject(matrix);
	}

	public boolean allAreEqual() {
		return true;
	}

	// we will always print the matrix in row-based manner
	// example
	public String print(int maxLen) {
		String ret = "";

		// grab the sizes from the matrix header
		long size1 = matrix.getSize1();
		long size2 = matrix.getSize2();

		// grab the header size
		long headerSize = Matrix.getMatrixHeaderSize();

		// grab the address from the matrix
		long data = matrix.getAddress();

		// get if row
		boolean ifRow = matrix.getIfRow();

		if (ifRow) {
			for (int i = 0; i < size1; i++) {
				ret += "[";

				for (int j = 0; j < size2; j++) {
					ret += String.format("%.2f", Matrix.unsafe.getDouble(data + headerSize + (i * size2 + j) * Double.BYTES));
					if (ret.length() > maxLen && maxLen > 4) {
						return ret.substring(0, maxLen - 4) + "...";
					}
					if (j < size2 - 1)
						ret += ", ";
				}

				ret += "]\n";
			}
		} else {
			for (int j = 0; j < size2; j++) {
				ret += "[";
				for (int i = 0; i < size1; i++) {
					ret += String.format("%.2f", Matrix.unsafe.getDouble(data + headerSize + (i * size2 + j) * Double.BYTES));
					if (ret.length() > maxLen && maxLen > 4) {
						return ret.substring(0, maxLen - 4) + "...";
					}
					if (i < size1 - 1)
						ret += ", ";
				}
				ret += "]\n";
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

	private static double counter = 0.01;

	public Attribute getSingleton() {
		double[][] data = new double[1][1];
		data[0][0] = counter;
		counter += 0.01;
		return new MatrixAttribute(true, data);
	}

}
