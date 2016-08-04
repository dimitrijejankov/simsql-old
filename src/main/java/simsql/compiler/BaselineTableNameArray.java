

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


package simsql.compiler;


public class BaselineTableNameArray extends SQLExpression
{
	
	private String name;
	private String lowerBound;
	private String upBound;
	
	public BaselineTableNameArray(String name, String arrayString)
	{
		this.name = name;
		int start = arrayString.indexOf("..");
		lowerBound = arrayString.substring(0, start);
		upBound = arrayString.substring(start+2, arrayString.length());
	}
	
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitBaselineTableNameArray(this);	
	}

	@Override
	public String toString()
	{
		return name+"[" + lowerBound + ",...," + upBound + "]";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getLowerBoundValue()
	{
		return Integer.parseInt(lowerBound);
	}
	
	public int getUpBoundValue()
	{
		return Integer.parseInt(upBound);
	}

	public String getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(String lowerBound) {
		this.lowerBound = lowerBound;
	}

	public String getUpBound() {
		return upBound;
	}

	public void setUpBound(String upBound) {
		this.upBound = upBound;
	}

	
}
