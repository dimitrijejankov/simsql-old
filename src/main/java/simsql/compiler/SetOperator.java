

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


/**
 * 
 */
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.mathOperator;

import java.util.ArrayList;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public class SetOperator implements MathOperator{
	public String name;
	public ArrayList<MathOperator> elementList;
	
	
	public SetOperator(String name, ArrayList<MathOperator> elmentList) {
		super();
		this.name = name;
		this.elementList = elmentList;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public ArrayList<MathOperator> getElmentList() {
		return elementList;
	}
	public void setElmentList(ArrayList<MathOperator> elementList) {
		this.elementList = elementList;
	}
	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += "set(";
		result += name;
		result += ", ";
		result += "[";
		
		for(int i = 0; i < elementList.size(); i++)
		{
			result += elementList.get(i).getNodeName();
			if(i != elementList.size() - 1)
			{
				result += ", ";
			}
		}
		result += "]).\r\n";
		
		for(int i = 0; i < elementList.size(); i++)
		{
			result += elementList.get(i).visitNode();
		}
		
		return result;
	}
	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return name;
	}
	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator#copy()
	 */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.name);
		ArrayList<MathOperator> c_elementList = copyHelper.copyMathOperatorList(elementList);
		
		SetOperator setOperator = new SetOperator(c_name,c_elementList);
		return setOperator;
	}
}
