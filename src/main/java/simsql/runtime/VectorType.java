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

import java.util.Map;

public class VectorType extends AbstractDataType {
	
	private int mySize = -1;
	private String mySizeVar = null;
	
	public int getFirstDim() {
		return mySize;
	}
	
	public String getFirstDimVar() {
		return mySizeVar;
	}
	
	public VectorType() {
		
	}
	
	public VectorType(String fromMe) {
		Parse(fromMe);
	}
	
	// this takes the DataType, written as a string, and parses it to get any additional info
	public boolean Parse (String fromMe) {
		
		try {
			
			parseStoch(fromMe);
			
			String[] temp = fromMe.split("\\[|\\]");
				// check if vector[]
			if (fromMe.indexOf("[") != fromMe.indexOf("]") - 1) {
				if (temp[1].matches("\\d+"))
					mySize = Integer.parseInt(temp[1]);
				else
					mySizeVar = temp[1];
			}
			return true;
		} catch (RuntimeException e) {
			System.out.println("Parsing error: " + e);
			return false;
		}
	}

	// this is the reverse of the above operation... creating a string that encodes the data type
	// this can be used, for example, to serialze the data type and write it to the catalog
	public String writeOut () {
		String result = "";
		if (mySize != -1)
			result = "vector[" + mySize + "]";
		else if (mySizeVar != null)
			result = "vector[" + mySizeVar + "]";
		else
			result = "vector[]";
		if (ifStoch())
			result += " random";
		return result;
	}

	// gets the size, in bytes, of this particular data type
	public int getSizeInBytes () {
		if (mySize != -1)
			return 8 * mySize;
		else
			return 80;
	}

	// gets the definite value for a dimension (or a -1, if it has no definite value).  For example,
	// calling (Matrix[10][]).getDefiniteValue (0) will return a 10, but calling 
	// (Matrix[10][]).getDefiniteValue (1) will return a -1, and calling String.getDefiniteValue (1)
	// will give you a -1, as will (Matrix[10][N]).getDefiniteValue (1)
	public int getDefiniteValue (int whichDim) {
		if (whichDim == 0 && mySize != -1)
			return mySize;
		else
			return -1;
	}

	// this takes as input a mapping from parameter names to sizes, as well as a type that is
	// being used to parameterize this type, and adds to the mappings, as is needed.  This will
	// be used as follows.  Let's say that we have a UDF MatrixMultiply (Matrix[n][m], Matrix[m,p]) ->
	// Matrix[n,p].  We want to call this with the call MatrixMultiply (Matrix[10][], Matrix[][100]).
	// First, we would have (Matrix[n][m]).parameterize (Matrix[10][], {}), which would modify
	// the map so we have {(n,10)}.  We would than have (Matrix[m,p]).parameterize (Matrix[][100], {(n,10)})
	// which would then mdify the map so we have {(n,10),(p,100)}.  
	// 
	// If the method finds that a particular parameter has already been parameterized differently, it
	// returns a false, indicating that there is a type mismatch.  Otherwise, it returns a true
	//
	public Compatibility parameterize (DataType withMe, Map <String, Integer> addToMe) {
		
		int sizeValue = withMe.getFirstDim();
		String sizeVar = withMe.getFirstDimVar();
		String inputType = withMe.getTypeName();
		
		// input DataType withMe contains variable or it is not vector type
		if (sizeVar !=null || 
				! (inputType.equals("vector") || inputType.equals("vector random"))) {
			// throw new RuntimeException("input DataType cannot parameterize this VectorType!");
			return Compatibility.BAD;
		}
			
		// check if this VectorType contains variable
		if (mySizeVar != null) {
			if (sizeValue == -1)
				return Compatibility.PROMOTABLE;
			else if (addToMe.containsKey(mySizeVar)) {
				if (addToMe.get(mySizeVar) != sizeValue) {
					// throw new RuntimeException("The variable "+ mySizeVar + " has different values!");
					return Compatibility.BAD;
				}
				else if (this.getTypeName().equals(inputType)) {
					return Compatibility.OK;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			else {
				addToMe.put(mySizeVar, sizeValue);
				if (this.getTypeName().equals(inputType)) {
					return Compatibility.OK;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			
		}
		else if (mySize != -1) {
			if (mySize == sizeValue) {
				if (this.getTypeName().equals(inputType)) {
					return Compatibility.OK;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			// withMe is Vector[]
			else if (sizeValue == -1)
				return Compatibility.PROMOTABLE;
			else {
				//throw new RuntimeException("Input DataType has wrong length!");
				return Compatibility.BAD;
			}
		}
		else if (sizeValue == -1)
			return Compatibility.OK;
		else		
			return Compatibility.PROMOTABLE;
	}

	// this takes a map such as {(n,10),(p,100)}, and applies it to get a new data type.  For example,
	// say that this object is Matrix[n,p].  If we call applyMapping ({(n,10),(p,100)}), we will return
	// a Matrix[10][100].
	public DataType applyMapping (Map <String, Integer> useMe) {
		if (mySizeVar == null) {
			System.err.println("This VectorType cannot use applyMapping!");
			return this;
		}
		//else if (!useMe.containsKey(mySizeVar)) {
		//	System.err.println("The map does not have value for this VectorType!");
		//	return this;			
		//}
		else {
			String result = "";
			if (useMe.containsKey(mySizeVar))
				result = "vector[" + useMe.get(mySizeVar) + "]";
			else
				result = "vector[]";
			if (ifStoch())
				result += " random";
			return new VectorType(result);
		}
	}
	
	public simsql.runtime.Attribute getPhysicalRealization() {
		// TODO currently we do not have VectorArrayAttribute
	//	if (ifStoch())
	//		return new simsql.runtime.VectorArrayAttribute();
	//	else
		if (mySize != -1)
			return new simsql.runtime.VectorAttributeWithLength(mySize);
		else
			return new simsql.runtime.VectorAttribute();
	}
	
	public String getTypeName() {
		if (ifStoch())
			return "vector random";
		return "vector";
	}


}
