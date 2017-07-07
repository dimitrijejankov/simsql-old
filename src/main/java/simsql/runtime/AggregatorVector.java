package simsql.runtime;

/**
 * Implements the vector type during aggregation, with the same value in every world.
 *
 * @author Jacob
 */
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class AggregatorVector extends VectorAttribute {

	private int curDim;
	private ScalarAttribute[] myScalars;

	public AggregatorVector () {}

	public AggregatorVector (ScalarAttribute fromMe) {
		myScalars = new ScalarAttribute[1];
		myScalars[0] = fromMe;
		curDim = fromMe.getLabel();
	}

	public int getCurDim () {
		return curDim;
	}

	public ScalarAttribute[] getScalars () {
		return myScalars;
	}

	public AggregatorVector combine (AggregatorVector me) {
		ScalarAttribute[] otherScalars = me.getScalars();
		int otherCurDim = me.getCurDim();
		if (myScalars.length == 1 && curDim > 0) {
			ScalarAttribute[] newScalars = new ScalarAttribute[curDim + 1];
			newScalars[curDim] = myScalars[0];
			myScalars = newScalars;
		}
		if (otherCurDim >= myScalars.length) {
			ScalarAttribute[] newScalars = new ScalarAttribute[2 * otherCurDim];
			System.arraycopy(myScalars, 0, newScalars, 0, myScalars.length);
			myScalars = newScalars;
		}
		if (otherCurDim > curDim)
			curDim = otherCurDim;
		for (int i = 0; i < otherScalars.length; i++) {
			if (otherScalars[i] != null) {
				int label = otherScalars[i].getLabel();
				if (myScalars[label] != null) {
					throw new RuntimeException ("duplicate values for one dimension!");
				}
				else {
					myScalars[label] = otherScalars[i];
				}
			}
		}
		return this;
	}

	public VectorAttribute condense () {
		double[] newVals = new double[curDim + 1];
		if (myScalars.length == 1) {
			newVals[curDim] = myScalars[0].getVal();
			for (int i = 0; i < newVals.length - 1; i++) {
					newVals[i] = 0;
			}	
		}
		else {
			for (int i = 0; i < newVals.length; i++) {
				if (myScalars[i] != null)
					newVals[i] = myScalars[i].getVal();
				else
					newVals[i] = 0;
			}
		}
		return new VectorAttribute(newVals);
	}

  	public long writeSelfToStream (DataOutputStream writeToMe) throws IOException {
	    int returnVal = 0;
	    writeToMe.writeInt(curDim);
	    writeToMe.writeInt(myScalars.length);
	    returnVal += 8;
	    for (int i = 0; i < myScalars.length; i++) {
	    	if (myScalars[i] != null) {
	    		writeToMe.writeBoolean(true);
	    		returnVal += 1;
	    		returnVal += myScalars[i].writeSelfToStream(writeToMe);
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
	    curDim = readFromMe.readInt();
	    int len = readFromMe.readInt();
	    returnVal += 8;
	    myScalars = new ScalarAttribute[len];
	    boolean hasValue;
	    for (int i = 0; i < len; i++) {
	    	hasValue = readFromMe.readBoolean();
	    	returnVal += 1;
	    	if (hasValue) {
		    	myScalars[i] = new ScalarAttribute();
		    	returnVal += myScalars[i].readSelfFromStream(readFromMe);
		    }
		    else
		    	myScalars[i] = null;
	    }
	    return returnVal;
  	}

}