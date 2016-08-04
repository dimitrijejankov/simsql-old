

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
 * This is a list of all of the types that an attribute can take
 */

public class AttributeType {

    private TypeCode myCode;
    private DataType myType;
    
    public AttributeType(DataType inType) {
		myType = inType;
		if (inType.getTypeName().contains("integer"))
			myCode = TypeCode.INT;
		else if (inType.getTypeName().contains("double"))
			myCode = TypeCode.DOUBLE;
		else if (inType.getTypeName().contains("string"))
			myCode = TypeCode.STRING;		
		else if (inType.getTypeName().contains("scalar"))
				myCode = TypeCode.SCALAR;
		else if (inType.getTypeName().contains("vector"))
			myCode = TypeCode.VECTOR;
		else if (inType.getTypeName().contains("matrix"))
			myCode = TypeCode.MATRIX;
		else if (inType.getTypeName().contains("seed"))
			myCode = TypeCode.SEED;
		else if (inType.getTypeName().contains("null"))
			myCode = TypeCode.NULL;
    }

    public DataType getType() {
    	return myType;
    }
    
    public TypeCode getTypeCode() {
    	return myCode;
    }
}

