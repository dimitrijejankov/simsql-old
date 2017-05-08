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

public class NullType extends DataType {

	
	// this takes the DataType, written as a string, and parses it to get any additional info
	public boolean Parse (String fromMe) {
		return true;
	}

	// this is the reverse of the above operation... creating a string that encodes the data type
	// this can be used, for example, to serialze the data type and write it to the catalog
	public String writeOut () {
		return "null";
	}

	// gets the size, in bytes, of this particular data type
	public int getSizeInBytes () {
		return 0;
	}

	public int getDefiniteValue (int whichDim) {
		return -1;
	}

	public Compatibility parameterize (DataType withMe, Map <String, Integer> addToMe) {
		return Compatibility.BAD;				// TO-DO, don't change this!
	}

	public DataType applyMapping (Map <String, Integer> useMe) {
		return this;
	}

	public simsql.runtime.Attribute getPhysicalRealization() {
		return new simsql.runtime.NullAttribute();
	}
	
	public String getTypeName() {
		return "null";
	}
}


