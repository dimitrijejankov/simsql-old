

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.booleanOperator;

import java.util.ArrayList;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public class OrOperator extends BooleanOperator {
	private ArrayList<BooleanOperator> operatorList;
	/**
	 * @param name
	 */
	public OrOperator(String name) {
		super(name);
	}
	
	public OrOperator(String name, ArrayList<BooleanOperator> operatorList)
	{
		this(name);
		this.operatorList = operatorList;
	}

	public ArrayList<BooleanOperator> getOperatorList() {
		return operatorList;
	}

	public void setOperatorList(ArrayList<BooleanOperator> operatorList) {
		this.operatorList = operatorList;
	}

	/* (non-Javadoc)
	 * @see logicOperator.booleanOperator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "boolOr(" + this.getName() + ", [";
		
		if(operatorList != null)
		{
			for(int i = 0 ; i < operatorList.size(); i++)
			{
				result += operatorList.get(i).getName();
				
				if(i != operatorList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "]).\r\n";
		
		if(operatorList != null)
		{
			for(int i = 0 ; i < operatorList.size(); i++)
			{
				result += operatorList.get(i).visitNode();
			}
		}
		return result;
	}
	
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.getName());
		ArrayList<BooleanOperator> c_operatorList = copyHelper.copyBooleanOperatorList(operatorList);
		
		OrOperator c_operator = new OrOperator(c_name, c_operatorList);
		return c_operator;
	}
}
