package simsql.runtime;

/**
 * Implements the matrix type during aggregation, with the same value in every world.
 *
 * @author Jacob
 */
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AggregatorMatrix extends MatrixAttribute {

	private int curDim;
	private VectorAttribute[] myVectors;
	private boolean ifRow;

	public AggregatorMatrix () {}

	public AggregatorMatrix (VectorAttribute fromMe, boolean ifr) {
		myVectors = new VectorAttribute[1];
		myVectors[0] = fromMe;
		curDim = fromMe.getLabel();
		ifRow = ifr;
	}

	public int getCurDim () {
		return curDim;
	}

	public VectorAttribute[] getVectors () {
		return myVectors;
	}

	public AggregatorMatrix combine (AggregatorMatrix me) {
		VectorAttribute[] otherVectors = me.getVectors();
		int otherCurDim = me.getCurDim();
		if (myVectors.length == 1 && curDim > 0) {
			VectorAttribute[] newVectors = new VectorAttribute[curDim + 1];
			newVectors[curDim] = myVectors[0];
			myVectors = newVectors;
		}
		if (otherCurDim >= myVectors.length) {
			VectorAttribute[] newVectors = new VectorAttribute[2 * otherCurDim];
			System.arraycopy(myVectors, 0, newVectors, 0, myVectors.length);
			myVectors = newVectors;
		}
		if (otherCurDim > curDim)
			curDim = otherCurDim;
		for (int i = 0; i < otherVectors.length; i++) {
			if (otherVectors[i] != null) {
				int label = otherVectors[i].getLabel();
				if (myVectors[label] != null) {
					throw new RuntimeException ("duplicate values for one dimension!");
				}
				else {
					myVectors[label] = otherVectors[i];
				}
			}
		}
		return this;
	}

	public MatrixAttribute condense () {
		int secondDimension = 0;
		for (int i = 0; i < curDim + 1; i++) {
			if (myVectors[i] != null) {
				secondDimension = myVectors[i].getVal().length;
				break;
			}
		}
		double[][] newVals = new double[curDim + 1][secondDimension];
		if (myVectors.length == 1) {
			newVals[curDim] = myVectors[0].getVal();
			for (int i = 0; i < newVals.length - 1; i++) {
				newVals[i] = new double[secondDimension];
			}	
		}
		else {
			for (int i = 0; i < newVals.length; i++) {
				if (myVectors[i] != null)
					newVals[i] = myVectors[i].getVal();
				else
					newVals[i] = new double[secondDimension];
			}
		}
		return new MatrixAttribute(ifRow, newVals, secondDimension);
	}

	public long writeSelfToStream (DataOutputStream writeToMe) throws IOException {
	    int returnVal = 0;
	    writeToMe.writeBoolean(ifRow);
	    returnVal += 1;
	    writeToMe.writeInt(curDim);
	    writeToMe.writeInt(myVectors.length);
	    returnVal += 8;
	    for (int i = 0; i < myVectors.length; i++) {
	    	if (myVectors[i] != null) {
	    		writeToMe.writeBoolean(true);
	    		returnVal += 1;
	    		returnVal += myVectors[i].writeSelfToStream(writeToMe);
	    	}
	    	else {
	    		writeToMe.writeBoolean(false);
	    		returnVal += 1;
	    	}

	    }
	    return returnVal;
	}
  
	public long readSelfFromStream (DataInputStream readFromMe) throws IOException {
		long returnVal = 0;
	    ifRow = readFromMe.readBoolean();
	    returnVal += 1;
	    curDim = readFromMe.readInt();
	    int len = readFromMe.readInt();
	    returnVal += 8;
	    myVectors = new VectorAttribute[len];
	    boolean hasValue;
	    for (int i = 0; i < len; i++) {
	    	hasValue = readFromMe.readBoolean();
	    	returnVal += 1;
	    	if (hasValue) {
		    	myVectors[i] = new VectorAttribute();
		    	returnVal += myVectors[i].readSelfFromStream(readFromMe);
		    }
		    else
		    	myVectors[i] = null;
	    }
	    return returnVal;
	}

}