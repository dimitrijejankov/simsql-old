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

public class MatrixType extends AbstractDataType {
	
	
	private int firstDim = -1;
	private int secondDim = -1;
	private String firstDimVar = null;
	private String secondDimVar = null;
	
	public int getFirstDim() {
		return firstDim;
	}
	
	public int getSecondDim() {
		return secondDim;
	}
	
	public String getFirstDimVar() {
		return firstDimVar;
	}
	
	public String getSecondDimVar() {
		return secondDimVar;
	}
	
	public MatrixType() {
		
	}
	
	public MatrixType(String fromMe) {
		Parse(fromMe);
	}
	
	// this takes the DataType, written as a string, and parses it to get any additional info
	public boolean Parse (String fromMe) {
		
		try {
			
			parseStoch(fromMe);
			
			String[] temp = fromMe.split("(\\[|\\])+");
			// check if first dimension is unknown
			if (fromMe.indexOf("[") != fromMe.indexOf("]") - 1) {
				if (temp[1].matches("\\d+"))
					firstDim = Integer.parseInt(temp[1]);
				else
					firstDimVar = temp[1];
			}
			// check if second dimension is unknown
			if (fromMe.lastIndexOf("[") != fromMe.lastIndexOf("]") - 1) {
				if (temp[2].matches("\\d+"))
					secondDim = Integer.parseInt(temp[2]);
				else
					secondDimVar = temp[2];
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
		
		if (firstDim != -1)
			result += "matrix[" + firstDim + "]";
		else if (firstDimVar != null)
			result += "matrix[" + firstDimVar + "]";
		else
			result += "matrix[]";
		
		if (secondDim != -1)
			result += "[" + secondDim + "]";
		else if (secondDimVar != null)
			result += "[" + secondDimVar + "]";
		else
			result += "[]";
		if (ifStoch())
			result += " random";
		return result;
	}

	// gets the size, in bytes, of this particular data type
	public int getSizeInBytes () {

		if (firstDim != -1 && secondDim != -1)
			return 8 * firstDim * secondDim;
		else
			return 800;
	}

	// gets the definite value for a dimension (or a -1, if it has no definite value).  For example,
	// calling (Matrix[10][]).getDefiniteValue (0) will return a 10, but calling 
	// (Matrix[10][]).getDefiniteValue (1) will return a -1, and calling String.getDefiniteValue (1)
	// will give you a -1, as will (Matrix[10][N]).getDefiniteValue (1)
	public int getDefiniteValue (int whichDim) {
		if (whichDim == 0 && firstDim != -1)
			return firstDim;
		else if (whichDim == 1 && secondDim != -1)
			return secondDim;
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
		
		int input1Dim = withMe.getFirstDim();
		int input2Dim = withMe.getSecondDim();
		String input1DimVar = withMe.getFirstDimVar();
		String input2DimVar = withMe.getSecondDimVar();
		String inputType = withMe.getTypeName();
		
		Compatibility success = Compatibility.OK;
		
		// input DataType withMe contains variable or it is not matrix type
		if (input1DimVar !=null || input2DimVar !=null ||
				! (inputType.equals("matrix") || inputType.equals("matrix random"))) {
			// throw new RuntimeException("input DataType cannot parameterize this MatrixType!");
			return Compatibility.BAD;
		}
			
		// For the first dimension of this MatrixType
		// check if this the first dimension contains variable
		if (this.firstDimVar != null) {
			if (input1Dim == -1)
				success = Compatibility.PROMOTABLE;			
			else if (addToMe.containsKey(this.firstDimVar)) {
				if (addToMe.get(this.firstDimVar) != input1Dim) {
					// throw new RuntimeException("The variable "+ this.firstDimVar + " has different values!");
					//System.out.println("The variable "+ this.firstDimVar + " has different values!");
					return Compatibility.BAD;
				}
				else if (this.getTypeName().equals(inputType)) {
					success = Compatibility.OK;
				}
				else
					success = Compatibility.PROMOTABLE;
			}
			else {
				addToMe.put(this.firstDimVar, input1Dim);
				if (this.getTypeName().equals(inputType)) {
					success = Compatibility.OK;
				}
				else
					success = Compatibility.PROMOTABLE;
			}
			
		}
		else if (this.firstDim != -1) {
			if (this.firstDim == input1Dim) {
				if (this.getTypeName().equals(inputType)) {
					success = Compatibility.OK;
				}
				else
					success = Compatibility.PROMOTABLE;
			}
			// withMe's first dimension size is unknown
			else if (input1Dim == -1)
				success = Compatibility.PROMOTABLE;
			else {
				return Compatibility.BAD;
				//throw new RuntimeException("The first dimension of input DataType has wrong length!");
			}
		}
		
		// For the second dimension of this MatrixType
		// check if the second dimension contains variable
		if (this.secondDimVar != null) {
			if (input2Dim == -1)
				return Compatibility.PROMOTABLE;
			else if (addToMe.containsKey(this.secondDimVar)) {
				if (addToMe.get(this.secondDimVar) != input2Dim) {
					//throw new RuntimeException("The variable "+ this.secondDimVar + " has different values!");
					//System.out.println("The variable "+ this.secondDimVar + " has different values!");
					return Compatibility.BAD;
				}
				else if (this.getTypeName().equals(inputType)) {
					if (success.equals(Compatibility.OK))
						return Compatibility.OK;
					else
						return Compatibility.PROMOTABLE;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			else {
				addToMe.put(this.secondDimVar, input2Dim);
				if (this.getTypeName().equals(inputType)) {
					if (success.equals(Compatibility.OK))
						return Compatibility.OK;
					else
						return Compatibility.PROMOTABLE;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			
		}
		else if (this.secondDim != -1) {
			if (this.secondDim == input2Dim) {
				if (this.getTypeName().equals(inputType)) {
					if (success.equals(Compatibility.OK))
						return Compatibility.OK;
					else
						return Compatibility.PROMOTABLE;
				}
				else
					return Compatibility.PROMOTABLE;
			}
			// withMe's second dimension size is unknown
			else if (input2Dim == -1)
				return Compatibility.PROMOTABLE;
			else {
				return Compatibility.BAD;
				//throw new RuntimeException("The second dimension of input DataType has wrong length!");
			}
		}
		
		// matrix[][]
		if (input1Dim == -1 && input2Dim == -1) {
			return Compatibility.OK;
		}
		else
			return Compatibility.PROMOTABLE;
	}

	// this takes a map such as {(n,10),(p,100)}, and applies it to get a new data type.  For example,
	// say that this object is Matrix[n,p].  If we call applyMapping ({(n,10),(p,100)}), we will return
	// a Matrix[10][100].
	public DataType applyMapping (Map <String, Integer> useMe) {
		
		String result = "";
		//boolean success = false;
		
		// apply mapping for first dimension
		if (firstDim != -1) {
			result += "matrix[" + firstDim + "]";
			//System.out.println("The first dimension of this MatrixType cannot use applyMapping!");
		}
		//else if (!useMe.containsKey(firstDimVar)) {
			//result += "matrix[" + firstDimVar + "]";
		//	throw new RuntimeException("The map does not have value for the first dimension of this MatrixType!");
			//System.out.println("The map does not have value for the first dimension of this MatrixType!");			
		//}				
		else {
			if (useMe.containsKey(firstDimVar))
				result += "matrix[" + useMe.get(firstDimVar) + "]";
			else
				result += "matrix[]";
			//success = true;
		}
		
		// apply mapping for second dimension
		if (secondDim != -1) {
			result += "[" + secondDim + "]";
		}
		//else if (!useMe.containsKey(secondDimVar)) {
		//	throw new RuntimeException("The map does not have value for the second dimension of this MatrixType!");
		//}				
		else {
			if (useMe.containsKey(secondDimVar))
				result += "[" + useMe.get(secondDimVar) + "]";
			else
				result += "[]";
			//success = true;
		}
		
		if (ifStoch())
			result += " random";
		
		/*
		if (!success) {
			System.out.println("This MatrixType does not apply mapping successfully!");		
			return this;
		}*/
		return new MatrixType(result);
	}
	
	public simsql.runtime.Attribute getPhysicalRealization() {
		// TODO currently we do not have MatrixArrayAttribute
	//	if (ifStoch())
	//		return new simsql.runtime.MatrixArrayAttribute();
	//	else
		if (firstDim != -1 && secondDim != -1)
			return new simsql.runtime.MatrixAttributeWithLength(firstDim, secondDim);
		else
			return new simsql.runtime.MatrixAttribute();
	}
	
	public String getTypeName() {
		if (ifStoch())
			return "matrix random";
		return "matrix";
	}



}
