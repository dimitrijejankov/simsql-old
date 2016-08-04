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

public abstract class AbstractDataType implements DataType{
	
	private boolean isStoch = false;
	
	public void parseStoch(String parseMe) {
		//isStoch = false;
		for (String a: parseMe.split(" ")) {
			if (a.toLowerCase().equals("random")) {
				isStoch = true;
				return;
			}
		}
		
	}
	
	public boolean ifStoch() {
		return isStoch;
	}
	
	public void setStoch(boolean fromMe) {
		isStoch = fromMe;
	}
	
	public int getFirstDim() {
		return -1;
	}
	
	public int getSecondDim() {
		return -1;
	}
	
	public String getFirstDimVar() {
		return null;
	}
	
	public String getSecondDimVar() {
		return null;
	}


}
