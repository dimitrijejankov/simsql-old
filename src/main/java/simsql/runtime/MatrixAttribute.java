

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
 * Implements the matrix type, with the same value in every world.
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

public class MatrixAttribute implements Attribute {

  // Indicate if the matrix is row based
  private boolean ifRow;
  
  // this is the actual data
  private double [][] myVals;
  
  // the number of elements in the second dimension
  // if myVals is row-based, the secondDimension will be the number of columns
  // if myVals is column-based, the secondDimension will be the number of rows
  private int secondDimension = -1;
  
  // all zeros vector in secondDimension
   // private double [] zeros;
  //private int size;
  
  //this allows us to return the data as a byte array
  private static ByteBuffer b = ByteBuffer.allocate (0);
  static {
    b.order(ByteOrder.nativeOrder());
  }
  
  /*
  public static int getTotSize() {
	  return myVals.length * calSecondDim(myVals);
  }
  */
  
  public void recycle() {
  }
  
  public MatrixAttribute () {}
    
  public Bitstring isNull () {
    return BitstringWithSingleValue.FALSE;
  }

  
  public Attribute setNull (Bitstring theseOnes) {
    if (theseOnes.allAreTrue ()) {
      return NullAttribute.NULL;
    } else {
      return new ArrayAttributeWithNulls (theseOnes, this);
    }
  }

  public Attribute readSelfFromTextStream (BufferedReader readFromMe) throws IOException {

    // this is the space we'll use to do the parsing... we assume it is less than 64 chars
    char [] myArray = new char[64];

    // allows us to match the word "null"
    char [] nullString = {'n', 'u', 'l', 'l'};

    // records the doubles and nulls we've read in
    ArrayList <ArrayList<Double>> myDoubles = new ArrayList <ArrayList<Double>> (64);
    // ArrayList <Boolean> myNulls = new ArrayList <Boolean> (64);
   
    // suck in the '[', but ignore a leading newline character
    int startChar = readFromMe.read ();
    while (startChar == '\n') 
      startChar = readFromMe.read ();

    // if we got an eof, we are done
    if (startChar == -1) {
      return null;
    }

    if (startChar != '[') {
      throw new IOException ("Read in a bad matrix start character when reading a vector; expected '['");
    }

    // this tells us we did not see a null anywhere in the input
    boolean gotANull = false;
    boolean isNull = true;

    // read in the first char
    myArray[0] = (char) startChar;

    while (myArray[0] == '['){
    	myArray[0] = (char) readFromMe.read ();
    	ArrayList <Double> myRow = new ArrayList <Double> (64);
    	boolean allZeros = true;
	    // keep reading until we find the ']'
	    while (myArray[0] != ']') {
	
	      // this loop reads in a row
	      int i;
	      for (i = 1; myArray[i - 1] != ',' && myArray[i - 1] != ']'; i++) {
	
	        // isNull gets set to false if we find a char that does not match the string 'null'
	        if (i - 1 <= 3 && myArray[i - 1] != nullString[i - 1]) {
	          isNull = false;  
	        }
	        myArray[i] = (char) readFromMe.read ();  
	      }
	
	      // if we got here, we read in a ','
	      if (isNull == true && i == 5) {
	
	        // this means we got a null!
	        // myDoubles.add (0.0);
	        // myNulls.add (true);
	        // gotANull = true;
	    	if (readFromMe.read () != '|') {
	    	    throw new IOException ("Error when I tried to read in a matrix: didn't close with a '|'");
	    	}
	    	return NullAttribute.NULL;
	
	      } else {
	
	        // we did not get a null!
	        try {
	          Double temp = Double.valueOf (new String (myArray, 0, i - 1));
	          myRow.add (temp);
	          if (temp.doubleValue() != 0.0 && allZeros == true)
	        	  allZeros = false;
	          // myNulls.add (false);
	        } catch (Exception e) {
	          throw new IOException ("Error when I tried to read in a matrix... an entry didn't parse to a double");
	        }
	      }
	
	      // prime the parse of the next item in the row
	      if(myArray[i-1] != ']')
	    	  myArray[0] = (char) readFromMe.read ();
	      else
	    	  break;
	    }
	    if (allZeros == true)
	    	myDoubles.add(null);
	    else
	    	myDoubles.add(myRow);
	    
	    // start to read the next row
	    myArray[0] = (char) readFromMe.read ();
    }

    // suck in the final '|'
    if (myArray[0] != '|') {
      throw new IOException ("Error when I tried to read in a matrix: didn't close with a '|'");
    }

    // at this point we've read the entire matrix, so make an attribute out of it
    double [][] myDoubleMatrix = new double[myDoubles.size ()][];
    for (int i = 0; i < myDoubles.size (); i++) {
    	if (myDoubles.get(i) != null) {
    		myDoubleMatrix[i] = new double[myDoubles.get(i).size()];
	    	for (int j = 0; j < myDoubles.get(i).size(); j++){
	    		myDoubleMatrix[i][j] = myDoubles.get(i).get(j);
	    	}
    	}
    }
    
    return new MatrixAttribute (true, myDoubleMatrix); 

  }

  
  // The matrix written in text will always be row-based
  // example: [1, 2, 3][0, 0, 0][4, 5, 6] |
  public void writeSelfToTextStream (BufferedWriter writeToMe) throws IOException {
	  // calculate the second dimension 
	  if (secondDimension == -1)
		  secondDimension = calSecondDim(myVals);
		
	  // depends on the value of ifRow	  
	  if (ifRow) {
		  for (int i = 0; i < myVals.length; i++) {
			  writeToMe.write ("[");
			  if (myVals[i] != null) {
				  for (int j = 0; j < secondDimension; j++) {
		    	      String temp = Double.toString (myVals[i][j]);
		    	      writeToMe.write (temp, 0, temp.length ());
		    	      if(j < secondDimension - 1)
		    	    	  writeToMe.write (",");
				  }
			  }
			  else {
				  for (int j = 0; j < secondDimension; j++) {
		    	      String temp = Double.toString (0.0);
		    	      writeToMe.write (temp, 0, temp.length ());
		    	      if(j < secondDimension - 1)
		    	    	  writeToMe.write (",");
				  }
			  }
			  writeToMe.write ("]");
		  }
	  }
	  else {
		  for (int j = 0; j < secondDimension; j++) {
			  writeToMe.write ("[");
			  for (int i = 0; i < myVals.length; i++) {
				  if (myVals[i] != null) {
		    	      String temp = Double.toString (myVals[i][j]);
		    	      writeToMe.write (temp, 0, temp.length ());
		    	      if(i < myVals.length - 1)
		    	    	  writeToMe.write (",");
				  }
				  else {
		    	      String temp = Double.toString (0.0);
		    	      writeToMe.write (temp, 0, temp.length ());
		    	      if(i < myVals.length - 1)
		    	    	  writeToMe.write (",");
				  }
			  }
			  writeToMe.write ("]");
		  }
	  }
      writeToMe.write ("|");
  }
  
  public void writeSelfToTextStream (Bitstring theseAreNull, BufferedWriter writeToMe) throws IOException {

	// if the value in thsesAreNull[0] is null, we regard the whole matrix as null
	if (theseAreNull.getValue (0)){
		writeToMe.write ("[");
	    writeToMe.write ("null", 0, 4);
	    writeToMe.write ("]");
	}
	else {
		writeSelfToTextStream (writeToMe);
    }

  }

  public long getHashCode () {
	  return Hash.hashMe (getValue (0));  
  }

  // calculate the size of myVals
  public int getSize(){
	  return 1;
  }
  
  // the matrix in getValue will always be row-based
  public byte [] getValue (int whichMC) {
	// double [][] completeVals = transferAndComplete (ifRow, myVals, secondDimension);
	  if (secondDimension == -1)
		  secondDimension = calSecondDim(myVals);
	  
	  if (b.capacity() <  8 * (myVals.length * secondDimension + 2)) {
		  b = ByteBuffer.allocate(8 * (myVals.length * secondDimension + 2));
		  b.order(ByteOrder.nativeOrder());
	  }	  
	  
	  b.position(0);
	
	  if (ifRow) {
		  b.putDouble(myVals.length);
		  b.putDouble(secondDimension);
		  for (int i = 0; i < myVals.length; i++) {
			  if (myVals[i] != null) {
				  for (int j = 0; j < secondDimension; j++) {
					  b.putDouble (myVals[i][j]);
				  }
			  }
			  else {
				  for (int j = 0; j < secondDimension; j++) {
					  b.putDouble (0.0);
				  }
			  }
		  }
	  }
	  else {
		  b.putDouble(secondDimension);
		  b.putDouble(myVals.length);
		  for (int j = 0; j < secondDimension; j++) {
			  for (int i = 0; i < myVals.length; i++) {
				  if (myVals[i] != null) {
					  b.putDouble (myVals[i][j]);
				  }
				  else {
					  b.putDouble (0.0);
				  }
			  }
		  }
	  }
	  
      return b.array ();
  }

  public byte [] getValue (int whichMC, AttributeType castTo) {
    if (castTo.getTypeCode() == getType(whichMC).getTypeCode())
      return getValue(whichMC);

    else throw new RuntimeException("Invalid cast when writing out value.");
  }
  
  public Attribute removeNulls () {
    return this;  
  }
  
  public AttributeType getType (int whichMC) {
    return new AttributeType(new MatrixType());  
  }
  
  /** format: 
    *  ifRow(boolean), numOfRow/numOfColumn, numOfColumn/numOfRow, 
    *  numOfNonZeroRow/numOfNonZeroColumn, nonZeroRow/nonZeroColumn, data
    *  e.g., True, 5, 2, 2, 2, 4, 1, 2, 3, 4 for the matrix | 0, 0 |
                                                            | 0, 0 |
                                                            | 1, 2 |
                                                            | 0, 0 |
                                                            | 3, 4 |
    */
  public int writeSelfToStream (DataOutputStream writeToMe) throws IOException {	  
    if (secondDimension == -1)
          secondDimension = calSecondDim(myVals);
    int returnVal = 0;
    ArrayList <Integer> nonZeros = new ArrayList <Integer>();
    writeToMe.writeBoolean(ifRow);
    returnVal += 1;
    // here we assume the matrix has the same number of entries in each row/column
    writeToMe.writeInt (myVals.length);
    writeToMe.writeInt (secondDimension);
    returnVal += 8;
       
	for (int i = 0; i < myVals.length; i++) {
		if (myVals[i] != null){
			nonZeros.add(i);
		}
	}
    
	writeToMe.writeInt (nonZeros.size());
	returnVal += 4;
	for (int i:nonZeros) {
		writeToMe.writeInt (i);
	}
	returnVal += 4 * nonZeros.size();
	
    for (int i:nonZeros) {
		for (int j = 0; j < myVals[i].length; j++){
	      writeToMe.writeDouble (myVals[i][j]);
		}
    }
    returnVal += 8 * nonZeros.size() * secondDimension;
    return returnVal;
  }
  
  public int readSelfFromStream (DataInputStream readFromMe) throws IOException {
    int returnVal, numNonZeros, len, currentIndex = 0;
    ArrayList <Integer> nonZeros = new ArrayList <Integer>();
    ifRow = readFromMe.readBoolean ();
    len = readFromMe.readInt ();
    secondDimension = readFromMe.readInt ();
    myVals = new double[len][secondDimension];
    numNonZeros = readFromMe.readInt ();
    returnVal = 13;
    for (int i = 0; i < numNonZeros; i++) {
    	nonZeros.add(readFromMe.readInt ());
    }    
    returnVal += 4 * numNonZeros;
    for (int j:nonZeros){
    	while (currentIndex < j) {
    		myVals[currentIndex] = null;
    		currentIndex ++;
    	}
    	for (int k = 0; k < secondDimension; k++) {
    		myVals[currentIndex][k] = readFromMe.readDouble ();
    	}
    	currentIndex ++;
    }
    returnVal += 8 * numNonZeros * secondDimension;
    return returnVal;
  }

  public HashMap<Attribute, Bitstring> split () {
  	HashMap<Attribute, Bitstring> splits = new HashMap<Attribute, Bitstring>();
  	splits.put(new MatrixAttribute(ifRow, myVals), BitstringWithSingleValue.trueIf(true));
  	return splits;
  }
  
  // Transfer the myVals to row-based matrix and complete the null row/column with 0s
  public double[][] transferAndComplete (boolean localIfRow, double [][] localVals, int localSecDim) {
	  double [][] transferedMatrix;
	 // double [] zeros = new double [secondDimension];
	  if (localIfRow) {
		  transferedMatrix = new double [localVals.length][localSecDim];
		  for (int i = 0; i < localVals.length; i++) {
				  if (localVals[i] != null){
					  for (int j = 0; j < localSecDim; j++)
						  transferedMatrix[i][j] = localVals[i][j];
				  }
		  }
	  }
	  else {
		  transferedMatrix = new double [localSecDim][localVals.length];
		  for (int i = 0; i < localVals.length; i++) {
				  if (localVals[i] != null) {
					  for (int j = 0; j < localSecDim; j++)
						  transferedMatrix[j][i] = localVals[i][j];
				  }
		  }
	  }
	  return transferedMatrix;
  }
  
  // This function will calculate the second dimension for this matrix
  public static int calSecondDim(double [][] thisMatrix) {
	  for (int i = 0; i < thisMatrix.length; i++) {
		  if (thisMatrix[i] != null)
			  return thisMatrix[i].length;
	  }
	  return 0;
  }
  
  public MatrixAttribute (double [][] fromMe) {
	ifRow = true;
    myVals = fromMe;
    /*
    for (int i = 0; i < fromMe.length; i++) {
    	if (fromMe[i] != null){
    		secondDimension = fromMe[i].length;
    		break;
    	}
    }
    */
  }
  
  public MatrixAttribute (boolean indicateIfRow, double [][] fromMe) {
	ifRow = indicateIfRow;
	myVals = fromMe;
  }
  
  public MatrixAttribute (boolean indicateIfRow, double [][] fromMe, int dim2) {
	ifRow = indicateIfRow;
	myVals = fromMe;
	secondDimension = dim2;
	  
  }

  protected double[][] getVal () {
    return myVals;
  }

  protected boolean getIfRow () {
    return ifRow;
  }

  protected int getSecondDimension () {
    return secondDimension;
  }
  
  protected void setVal (double[][] val) {
    myVals = val;
  }

  protected void setVal (double[] val, int dim) {
    myVals[dim] = val;
  }

  protected void setVal (double val, int dim1, int dim2) {
    myVals[dim1][dim2] = val;
  }

  protected void setIfRow (boolean ifr) {
    ifRow = ifr;
  }

  protected void setSecondDimension (int sd) {
    secondDimension = sd;
  }
  
  public Attribute add (Attribute me) {
	    return me.addR (ifRow, myVals);
  }
	  
  public Attribute add (long addThisIn) {

    if (addThisIn == 0)
      return this;
    
    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn + myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  public Attribute addR (long addThisIn) {

    // because integer arithmatic is commutative
    return add (addThisIn);
  }
  
  // TODO
  public Attribute add (long [] addThisIn) {
    
	  /*
    if (addThisIn.length != myVals.length) {
      throw new RuntimeException ("adding an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] + addThisIn[i];
    }
    
    // and get outta here!
    return new VectorAttribute(newArray);
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute addR (long [] addThisIn) {
    
    // because ordering does not matter with addition of ints
    return add (addThisIn);
  }
  
  public Attribute add (String [] addThisIn) {
	  throw new RuntimeException ("You can't add a matrix and an array of string.");
  }
  
  public Attribute addR (String [] addThisIn) {
	  throw new RuntimeException ("You can't add a matrix and an array of string.");
  }
  
  public Attribute add (String addThisIn) {
	  throw new RuntimeException ("You can't add a matrix and a string."); 
  }
  
  public Attribute addR (String addThisIn) {
	  throw new RuntimeException ("You can't add a matrix and a string."); 
  }
  
  // TODO
  public Attribute add (double [] addThisIn) {
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute addR (double [] addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (double addThisIn) {

    if (addThisIn == 0.0)
      return this;
    
    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn + myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix); 
  }
  
  public Attribute addR (double addThisIn) {
    
    // since addition on numbers is commutative
    return add (addThisIn);
  }
  
  public Attribute add (int label, double addThisIn) {
    if (addThisIn == 0.0)
        return this;
      
	  if (secondDimension == -1)
	  	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn + myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = addThisIn;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  public Attribute addR (int label, double addThisIn) {
    
    // since addition on numbers is commutative
    return add (label, addThisIn);
  }
  
  public Attribute add (int label, double [] addThisIn) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != addThisIn.length) {
	      throw new RuntimeException ("adding a vector and a matrix with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = addThisIn[j] + myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = addThisIn[j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != addThisIn.length)
		  throw new RuntimeException ("adding a vector and a matrix with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = addThisIn[i] + myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = addThisIn[i];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	} 
  }
  
  public Attribute addR (int label, double [] addThisIn) {
    
    // since addition on numbers is commutative
    return add (label, addThisIn);
  }
  
  // vector + matrix will be always row-based addition
  public Attribute add (boolean addIfRow, double [][] addThisIn) {
    
	if (ifRow == addIfRow) {  		

		if (myVals.length != addThisIn.length) {
		  throw new RuntimeException ("adding two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (addThisIn)) {
	      throw new RuntimeException ("adding two matrices with different dimensions: " + myVals.length + "," + secondDimension + " " + addThisIn.length + "," + calSecondDim(addThisIn));
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && addThisIn[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] + addThisIn[i][j];
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j];
	    	}
	    	else if (addThisIn[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = addThisIn[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (addThisIn) || secondDimension != addThisIn.length) {
			throw new RuntimeException ("adding two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (addThisIn[j] != null)
	    				newMatrix[i][j] = myVals[i][j] + addThisIn[j][i];
	    			else
	    				newMatrix[i][j] = myVals[i][j];
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (addThisIn[j] != null)
	    				newMatrix[i][j] = addThisIn[j][i];
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
  }
  
  // The value of ifRow for the result matrix will depend on the value of the ifRow of 
  // the frist matrix in addition
  public Attribute addR (boolean otherIfRow, double [][] addThisIn) {
	    
	if (ifRow == otherIfRow) {  		
	    return add (otherIfRow, addThisIn); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (addThisIn) || secondDimension != addThisIn.length) {
			throw new RuntimeException ("adding two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [addThisIn.length][myVals.length];
	    for (int i = 0; i < addThisIn.length; i++) {
	    	if (addThisIn[i] != null) {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] != null)
	    				newMatrix[i][j] = addThisIn[i][j] + myVals[j][i];
	    			else
	    				newMatrix[i][j] = addThisIn[i][j];
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] != null)
	    				newMatrix[i][j] = myVals[j][i];
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (otherIfRow, newMatrix); 
	}
  }
  
  public Attribute subtract (Attribute me) {
    return me.subtractR (ifRow, myVals);
  }
  
  public Attribute subtract (long subtractThisOut) {
    
    if (subtractThisOut == 0)
      return this;
    
    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = myVals[i][j] - subtractThisOut;
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = - subtractThisOut;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  public Attribute subtractR (long subtractFromMe) {

    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = subtractFromMe - myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = subtractFromMe;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  // TODO
  public Attribute subtract (long [] subtractMeOut) {
    
	  /*
    if (subtractMeOut.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] - subtractMeOut[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute subtractR (long [] subtractFromMe) {
	  
	  /*
    
    if (subtractFromMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = subtractFromMe[i] - myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute subtract (double [] subtractThisOut) {
	  
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute subtractR (double [] subtractFromMe) {
	  
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute subtract (double subtractThisOut) {
    if (subtractThisOut == 0.0)
        return this;
      
	  if (secondDimension == -1)
	  	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = myVals[i][j] - subtractThisOut;
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = - subtractThisOut;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  public Attribute subtractR (double subtractFromThis) {
    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = subtractFromThis - myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = subtractFromThis;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix); 
  }
  
  public Attribute subtract (int label, double subtractThisOut) {
	  return subtract (subtractThisOut);
  }
  
  public Attribute subtractR (int label, double subtractFromThis) {
	  return subtractR (subtractFromThis);
  }
  
  public Attribute subtract (int label, double [] subtractThisOut) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != subtractThisOut.length) {
	      throw new RuntimeException ("subtracting a vector from a matrix with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] - subtractThisOut[j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = - subtractThisOut[j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != subtractThisOut.length)
		  throw new RuntimeException ("subtracting a vector from a matrix with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] - subtractThisOut[i];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = - subtractThisOut[i];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	} 
  }
  
  public Attribute subtractR (int label, double [] subtractFromMe) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != subtractFromMe.length) {
	      throw new RuntimeException ("subtracting a matrix from a vector with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[j] - myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != subtractFromMe.length)
		  throw new RuntimeException ("subtracting a matrix from a vector with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[i] - myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[i];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	} 
  }
  
  public Attribute subtract (boolean otherIfRow, double [][] subtractThisOut) {
    
	if (ifRow == otherIfRow) {  		

		if (myVals.length != subtractThisOut.length) {
		  throw new RuntimeException ("subtracting two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (subtractThisOut)) {
	      throw new RuntimeException ("subtracting two matrices with different dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && subtractThisOut[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] - subtractThisOut[i][j];
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j];
	    	}
	    	else if (subtractThisOut[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = - subtractThisOut[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (subtractThisOut) || secondDimension != subtractThisOut.length) {
			throw new RuntimeException ("subtracting two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (subtractThisOut[j] != null)
	    				newMatrix[i][j] = myVals[i][j] - subtractThisOut[j][i];
	    			else
	    				newMatrix[i][j] = myVals[i][j];
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (subtractThisOut[j] != null)
	    				newMatrix[i][j] = - subtractThisOut[j][i];
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
  }
  
  public Attribute subtractR (boolean otherIfRow, double [][] subtractFromMe) {
	    
	if (ifRow == otherIfRow) {  		

		if (myVals.length != subtractFromMe.length) {
		  throw new RuntimeException ("subtracting two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (subtractFromMe)) {
	      throw new RuntimeException ("subtracting two matrices with different dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && subtractFromMe[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[i][j] - myVals[i][j];
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = - myVals[i][j];
	    	}
	    	else if (subtractFromMe[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = subtractFromMe[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (otherIfRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (subtractFromMe) || secondDimension != subtractFromMe.length) {
			throw new RuntimeException ("subtracting two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [subtractFromMe.length][myVals.length];
	    for (int i = 0; i < subtractFromMe.length; i++) {
	    	if (subtractFromMe[i] != null) {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] != null)
	    				newMatrix[i][j] = subtractFromMe[i][j] - myVals[j][i];
	    			else
	    				newMatrix[i][j] = subtractFromMe[i][j];
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] != null)
	    				newMatrix[i][j] = - myVals[j][i];
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (otherIfRow, newMatrix); 
	}
  }
  
  public Attribute multiply (Attribute byMe) {
     return byMe.multiply (ifRow, myVals);
  }
  
  public Attribute multiply (long byMe) {

    if (byMe == 1)
      return this;
    else {
        if (secondDimension == -1)
        	secondDimension = calSecondDim(myVals);
  	    double [][] newMatrix = new double [myVals.length][];
        if (byMe == 0) {   		  
    		newMatrix[0] = new double [secondDimension];
    		for (int i=0; i < secondDimension; i++) {
    			newMatrix[0][i] = 0;
    		}
        }
        else {       	  
        	  for (int i = 0; i < myVals.length; i++) {
        		  if (myVals[i] != null) {
        			  newMatrix[i] = new double [secondDimension];
        			  for (int j = 0; j < secondDimension; j++) 
        				  newMatrix[i][j] = byMe * myVals[i][j];
        		  }
        	  }
        }
        return new MatrixAttribute (ifRow, newMatrix);
    }    
  }
  
  public Attribute multiply (double byMe) {
    if (byMe == 1.0)
        return this;
      else {
          if (secondDimension == -1)
          	secondDimension = calSecondDim(myVals);
          double [][] newMatrix = new double [myVals.length][];
          if (byMe == 0.0) {   		        		
        	  newMatrix[0] = new double [secondDimension];
        	  for (int i = 0; i < secondDimension; i++)
        		  newMatrix[0][i] = 0;
          }
          else {       	  
	      	  for (int i = 0; i < myVals.length; i++) {
	      		  if (myVals[i] != null) {
	      			  newMatrix[i] = new double [secondDimension];
	      			  for (int j = 0; j < secondDimension; j++) 
	      				  newMatrix[i][j] = byMe * myVals[i][j];
	      		  }
	      	  }
          }
          return new MatrixAttribute (ifRow, newMatrix);
      }
  }
  
  // TODO
  public Attribute multiply (long [] byMe) {
	  
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute multiply (double [] byMe) {
	  
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute multiply (int label, double byMe) {
	return multiply (byMe);
  }
  
  public Attribute multiply (int label, double [] byMe) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != byMe.length) {
	      throw new RuntimeException ("multiplying a vector and a matrix with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		newMatrix[i] = new double [secondDimension];
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = byMe[j] * myVals[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != byMe.length)
		  throw new RuntimeException ("multiplying a vector and a matrix with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		newMatrix[i] = new double [secondDimension];
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = byMe[i] * myVals[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
  }
  
  public Attribute multiply (boolean otherIfRow, double [][] byMe) {
    
	if (ifRow == otherIfRow) {  		

		if (myVals.length != byMe.length) {
		  throw new RuntimeException ("multiplying two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (byMe)) {
	      throw new RuntimeException ("multiplying two matrices with different dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && byMe[i] != null) {
	    		newMatrix[i] = new double [secondDimension];
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] * byMe[i][j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (byMe) || secondDimension != byMe.length) {
			throw new RuntimeException ("multiplying two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [myVals.length][];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		newMatrix[i] = new double [secondDimension];
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (byMe[j] != null)
	    				newMatrix[i][j] = myVals[i][j] * byMe[j][i];
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
  }
  
  public Attribute divide (Attribute byMe) {
    return byMe.divideR (ifRow, myVals);
  }
  
  public Attribute divide (long byMe) {

    if (byMe == 1)
      return this;

    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  newMatrix[i] = new double [secondDimension];		  
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = myVals[i][j] / byMe;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  public Attribute divideR (long divideMe) {

    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = divideMe / myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = divideMe / 0.0;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix);
  }
  
  // TODO
  public Attribute divide (long [] byMe) {
	  
	  /*
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i] / byMe[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute divideR (long [] divideMe) {
	  
	  /*
    
    if (divideMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    // create the new array
    double [] newArray  = new double [myVals.length];
    
    // put the stuff to add in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = divideMe[i] / myVals[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray);
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute divide (double [] byMe) {
	  
	  /*
    
    if (byMe.length != myVals.length) {
      throw new RuntimeException ("subtracting an array of values with the wrong number of possible worlds");
    }
    
    double [] newArray  = new double [myVals.length];
    
    // now add ourselves in
    for (int i = 0; i < myVals.length; i++) {
      newArray[i] = myVals[i]/ byMe[i];
    }
    
    // and get outta here!
    return new DoubleArrayAttribute (newArray); 
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute divideR (double [] divideMe) {
	  
	  /*
    
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
    */
	throw new RuntimeException ("This method is not defined!");
  }
  
  public Attribute divide (double byMe) {

    if (byMe == 1.0)
      return this;

    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  newMatrix[i] = new double [secondDimension];		  
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = myVals[i][j] / byMe;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix); 
  }
  
  public Attribute divideR (double divideMe) {
    if (secondDimension == -1)
    	secondDimension = calSecondDim(myVals);
	  
	  double [][] newMatrix = new double [myVals.length][secondDimension];
	  for (int i = 0; i < myVals.length; i++) {
		  if (myVals[i] != null) {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = divideMe / myVals[i][j];
		  }
		  else {
			  for (int j = 0; j < secondDimension; j++) 
				  newMatrix[i][j] = divideMe / 0.0;
		  }
	  }
	  return new MatrixAttribute (ifRow, newMatrix); 
  }
  
  public Attribute divide (int label, double byMe) {
	  if (byMe == 1.0)
	      return this;
	  
	  return divide (byMe);
  }
  
  public Attribute divideR (int label, double divideMe) {
	  return divideR (divideMe);
  }
  
  public Attribute divide (int label, double [] byMe) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != byMe.length) {
	      throw new RuntimeException ("dividing a matrix by a vector with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] / byMe[j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = 0.0 / byMe[j];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != byMe.length)
		  throw new RuntimeException ("dividing a matrix by a vector with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] / byMe[i];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = 0.0 / byMe[i];
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}  
  }
  
  public Attribute divideR (int label, double [] divideMe) {
	if (ifRow) {  
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
	    if (secondDimension != divideMe.length) {
	      throw new RuntimeException ("dividing a vector by a matrix with different row dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[j] / myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[j] / 0.0;
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {
	    if (myVals.length != divideMe.length)
		  throw new RuntimeException ("dividing a vector by a matrix with different row dimensions!");
		if (secondDimension == -1)
			secondDimension = calSecondDim(myVals);
		
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[i] / myVals[i][j];
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[i] / 0.0;
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	} 
  }
  
  public Attribute divide (boolean otherIfRow, double [][] byMe) {
    
	if (ifRow == otherIfRow) {  		

		if (myVals.length != byMe.length) {
		  throw new RuntimeException ("division between two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (byMe)) {
	      throw new RuntimeException ("division between two matrices with different dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && byMe[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] / byMe[i][j];
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = myVals[i][j] / 0.0;
	    	}
	    	else if (byMe[i] == null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = 0.0 / 0.0;
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (byMe) || secondDimension != byMe.length) {
			throw new RuntimeException ("division between two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [myVals.length][secondDimension];
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (byMe[j] != null)
	    				newMatrix[i][j] = myVals[i][j] / byMe[j][i];
	    			else
	    				newMatrix[i][j] = myVals[i][j] / 0.0;
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (byMe[j] == null)
	    				newMatrix[i][j] = 0.0 / 0.0;
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (ifRow, newMatrix); 
	}
  }
  
  public Attribute divideR (boolean otherIfRow, double [][] divideMe) {
	    
	if (ifRow == otherIfRow) {  		

		if (myVals.length != divideMe.length) {
		  throw new RuntimeException ("division between two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (divideMe)) {
	      throw new RuntimeException ("division between two matrices with different dimensions!");
	    }
	    
	    double [][] newMatrix  = new double [myVals.length][secondDimension];
	    
	    // now add ourselves in
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && divideMe[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[i][j] / myVals[i][j];
	    	}
	    	else if (divideMe[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = divideMe[i][j] / 0.0;
	    	}
	    	else if (myVals[i] == null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			newMatrix[i][j] = 0.0 / 0.0;
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (otherIfRow, newMatrix); 
	}
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (divideMe) || secondDimension != divideMe.length) {
			throw new RuntimeException ("division between two matrices with different dimensions!");
		}	
	    
		double [][] newMatrix  = new double [divideMe.length][myVals.length];
	    for (int i = 0; i < divideMe.length; i++) {
	    	if (divideMe[i] != null) {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] != null)
	    				newMatrix[i][j] = divideMe[i][j] / myVals[j][i];
	    			else
	    				newMatrix[i][j] = divideMe[i][j] / 0.0;
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < myVals.length; j++) {
	    			if (myVals[j] == null)
	    				newMatrix[i][j] = 0.0 / 0.0;
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return new MatrixAttribute (otherIfRow, newMatrix); 
	}
  }
  
  public Bitstring equals (Attribute me) {
    return me.equals (ifRow, myVals);
  }
  
  public Bitstring equals (long me) {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a long?");
  }
  
  public Bitstring equals (double me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a double?");
  }
  
  public Bitstring equals (String me)  {
    throw new RuntimeException ("Why are you doing an equality check on a matrix and a string?");
  }
  
  public Bitstring equals (long [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of long?");
  }
  
  public Bitstring equals (double [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of double?");
  }
  
  public Bitstring equals (String [] me)  {
    throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of string?");
  }
  
  public Bitstring equals (int label, double me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a scalar?");
  }
  
  public Bitstring equals (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a vector?");
  }
  
  public Bitstring equals (boolean otherIfRow, double [][] me)  {
	 
	if (ifRow == otherIfRow) {		

		if (myVals.length != me.length) {
		  throw new RuntimeException ("Equality check on two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (me)) {
	      throw new RuntimeException ("Equality check on two matrices with different dimensions!");
	    }
	    
	    boolean ifEqual = true;
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && me[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			ifEqual = ifEqual && (myVals[i][j] == me[i][j]);
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			ifEqual = ifEqual && (myVals[i][j] == 0.0);
	    	}
	    	else if (me[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			ifEqual = ifEqual && (me[i][j] == 0.0);
	    	}
	    }
	    return BitstringWithSingleValue.trueIf(ifEqual);
	}
	
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (me) || secondDimension != me.length) {
			throw new RuntimeException ("Equality check on two matrices with different dimensions!");
		}	
	    
		boolean ifEqual = true;
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (me[j] != null)
	    				ifEqual = ifEqual && (myVals[i][j] == me[j][i]);
	    			else
	    				ifEqual = ifEqual && (myVals[i][j] == 0.0);
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (me[j] != null)
	    				ifEqual = ifEqual && (me[j][i] == 0.0);
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return BitstringWithSingleValue.trueIf(ifEqual);
	}
  }
  
  public Bitstring notEqual (Attribute me) {
    return me.notEqual (ifRow, myVals);
  }
  
  public Bitstring notEqual (long me) {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a long?");
  }
  
  public Bitstring notEqual (double me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a double?");
  }
  
  public Bitstring notEqual (String me)  {
    throw new RuntimeException ("Why are you doing an equality check on a matrix and a string?");
  }
  
  public Bitstring notEqual (long [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of long?");
  }
  
  public Bitstring notEqual (double [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of double?");
  }
  
  public Bitstring notEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing an equality check on a matrix and an array of string?");
  }
  
  public Bitstring notEqual (int label, double me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a scalar?");
  }
  
  public Bitstring notEqual (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing an equality check on a matrix and a vector?");
  }
  
  public Bitstring notEqual (boolean otherIfRow, double [][] me)  {
 
	if (ifRow == otherIfRow) {		

		if (myVals.length != me.length) {
		  throw new RuntimeException ("Equality check on two matrices with different dimensions!");
		}
		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
	    if (secondDimension != calSecondDim (me)) {
	      throw new RuntimeException ("Equality check on two matrices with different dimensions!");
	    }
	    
	    boolean notEqual = false;
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null && me[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			notEqual = notEqual || (myVals[i][j] != me[i][j]);
	    	}
	    	else if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			notEqual = notEqual || (myVals[i][j] != 0.0);
	    	}
	    	else if (me[i] != null) {
	    		for (int j = 0; j < secondDimension; j++)
	    			notEqual = notEqual || (me[i][j] != 0.0);
	    	}
	    }
	    return BitstringWithSingleValue.trueIf(notEqual);
	}
	
	else {		
		if (secondDimension == -1)
			secondDimension = calSecondDim (myVals);
		
		if (myVals.length != calSecondDim (me) || secondDimension != me.length) {
			throw new RuntimeException ("Equality check on two matrices with different dimensions!");
		}	
	    
		boolean notEqual = false;
	    for (int i = 0; i < myVals.length; i++) {
	    	if (myVals[i] != null) {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (me[j] != null)
	    				notEqual = notEqual || (myVals[i][j] != me[j][i]);
	    			else
	    				notEqual = notEqual || (myVals[i][j] != 0.0);
	    		}
	    	}
	    	else {
	    		for (int j = 0; j < secondDimension; j++) {
	    			if (me[j] != null)
	    				notEqual = notEqual || (me[j][i] != 0.0);
	    		}
	    	}
	    }	    
	    // and get outta here!
	    return BitstringWithSingleValue.trueIf(notEqual);
	}
  }
  
  public Bitstring greaterThan (Attribute me) {
    return me.lessThan (ifRow, myVals);
  }
  
  public Bitstring greaterThan (long me) {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a long?");
  }
  
  public Bitstring greaterThan (double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a double?");
  }
  
  public Bitstring greaterThan (String me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and a string?");
  }
  
  public Bitstring greaterThan (long [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of long?");
  }
  
  public Bitstring greaterThan (double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of double?");
  }
  
  public Bitstring greaterThan (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of string?");
  }
  
  public Bitstring greaterThan (int label, double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a scalar?");
  }
  
  public Bitstring greaterThan (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a vector?");
  }
  
  public Bitstring greaterThan (boolean otherIfRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison of tow matrices?");
  }
  
  public Bitstring lessThan (Attribute me) {
    return me.greaterThan (ifRow, myVals);
  }
  
  public Bitstring lessThan (long me) {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a long?");
  }
  
  public Bitstring lessThan (double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a double?");
  }
  
  public Bitstring lessThan (String me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and a string?");
  }
  
  public Bitstring lessThan (long [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of long?");
  }
  
  public Bitstring lessThan (double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of double?");
  }
  
  public Bitstring lessThan (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of string?");
  }
  
  public Bitstring lessThan (int label, double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a scalar?");
  }
  
  public Bitstring lessThan (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a vector?");
  }
  
  public Bitstring lessThan (boolean otherIfRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison between two matrices?");
  }
  
  public Bitstring greaterThanOrEqual (Attribute me) {
    return me.lessThanOrEqual (ifRow, myVals);
  }
  
  public Bitstring greaterThanOrEqual (long me) {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a long?");
  }
  
  public Bitstring greaterThanOrEqual (double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a double?");
  }
  
  public Bitstring greaterThanOrEqual (String me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and a string?");
  }
  
  public Bitstring greaterThanOrEqual (long [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of long?");
  }
  
  public Bitstring greaterThanOrEqual (double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of double?");
  }
  
  public Bitstring greaterThanOrEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of string?");
  }
  
  public Bitstring greaterThanOrEqual (int label, double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a scalar?");
  }
  
  public Bitstring greaterThanOrEqual (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a vector?");
  }
  
  public Bitstring greaterThanOrEqual (boolean otehrIfRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison between two matrices?");
  }
  
  public Bitstring lessThanOrEqual (Attribute me) {
    return me.greaterThanOrEqual (ifRow, myVals);
  }
  
  public Bitstring lessThanOrEqual (long me) {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a long?");
  }
  
  public Bitstring lessThanOrEqual (double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a double?");
  }
  
  public Bitstring lessThanOrEqual (String me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and a string?");
  }
  
  public Bitstring lessThanOrEqual (long [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of long?");
  }
  
  public Bitstring lessThanOrEqual (double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of double?");
  }
  
  public Bitstring lessThanOrEqual (String [] me)  {
    throw new RuntimeException ("Why are you doing a comparison of a matrix and an array of string?");
  }
  
  public Bitstring lessThanOrEqual (int label, double me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a scalar?");
  }
  
  public Bitstring lessThanOrEqual (int label, double [] me)  {
	throw new RuntimeException ("Why are you doing a comparison of a matrix and a vector?");
  }
  
  public Bitstring lessThanOrEqual (boolean otherIfRow, double [][] me)  {
	throw new RuntimeException ("Why are you doing a comparison between two matrices?");
  }
  
  public void injectSelf(Function f) {
    f.inject(myVals, ifRow);
  }  

  public boolean allAreEqual() {
	  return true;
  }

  // we will always print the matrix in row-based manner
  // example 
  public String print(int maxLen) {
      String ret = "";
      
	  if (secondDimension == -1)
		  secondDimension = calSecondDim(myVals);
      
	  if (ifRow) {
		  for (int i = 0; i < myVals.length; i++) {			  
			  ret += "[";
			  if (myVals[i] != null) {
				  for (int j = 0; j < secondDimension; j++) {
					  ret += String.format("%.2f", myVals[i][j]);
					  if (ret.length() > maxLen && maxLen > 4) {
					      return ret.substring(0, maxLen-4) + "...";
					  }
		    	      if(j < secondDimension - 1)
		    	    	  ret += ", ";
				  }
			  }
			  else {
				  for (int j = 0; j < secondDimension; j++) {
					  ret += String.format("%.2f", 0.0);
					  if (ret.length() > maxLen && maxLen > 4) {
					      return ret.substring(0, maxLen-4) + "...";
					  }
		    	      if(j < secondDimension - 1)
		    	    	  ret += ", ";
				  }
			  }
			  ret += "]\n";
		  }
	  }
	  else {
		  for (int j = 0; j < secondDimension; j++) {
			  ret += "[";
			  for (int i = 0; i < myVals.length; i++) {
				  if (myVals[i] != null) {
					  ret += String.format("%.2f", myVals[i][j]);
					  if (ret.length() > maxLen && maxLen > 4) {
					      return ret.substring(0, maxLen-4) + "...";
					  }
		    	      if(i < myVals.length - 1)
		    	    	  ret += ", ";
				  }
				  else {
					  ret += String.format("%.2f", 0.0);
					  if (ret.length() > maxLen && maxLen > 4) {
					      return ret.substring(0, maxLen-4) + "...";
					  }
		    	      if(i < myVals.length - 1)
		    	    	  ret += ", ";
				  }
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

  static double counter = 0.01;
  public Attribute getSingleton() {
        double [][] data = new double [1][1];
        data[0][0] = counter;
        counter += 0.01;
        return new MatrixAttribute (true, data);          
  }

}
