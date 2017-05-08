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

public class ScalarType extends DataType {

	
	// this takes the DataType, written as a string, and parses it to get any additional info
	public boolean Parse (String fromMe) {
		if (fromMe.length() <= 0) {
			throw new RuntimeException("The length of input type string is not positive!");
		}
		parseStoch(fromMe);
		return true;
	}

	// this is the reverse of the above operation... creating a string that encodes the data type
	// this can be used, for example, to serialze the data type and write it to the catalog
	public String writeOut () {
		if (ifStoch() == true)
			return "scalar random";
		return "scalar";
	}

	// gets the size, in bytes, of this particular data type
	public int getSizeInBytes () {
		return 8;
	}

	public int getDefiniteValue (int whichDim) {
		return -1;
	}

	public Compatibility parameterize (DataType withMe, Map <String, Integer> addToMe) {
		if (this.getTypeName().equals(withMe.getTypeName()))
			return Compatibility.OK;
		else if (withMe.getTypeName().equals("integer") || withMe.getTypeName().equals("integer random")
				|| withMe.getTypeName().equals("double") || withMe.getTypeName().equals("double random")
				|| withMe.getTypeName().equals("scalar") || withMe.getTypeName().equals("scalar random"))
			return Compatibility.PROMOTABLE;
		else
			return Compatibility.BAD;
	}

	public DataType applyMapping (Map <String, Integer> useMe) {
		return this;
	}
	
	public simsql.runtime.Attribute getPhysicalRealization() {
		// TODO currently we do not have ScalarArrayAttribute
	//	if (ifStoch())
	//		return new simsql.runtime.attributes.ScalarArrayAttribute();
	//	else
			return new simsql.runtime.ScalarAttribute();
	}
	
	public String getTypeName() {
		if (ifStoch())
			return "scalar random";
		return "scalar";
	}

}

