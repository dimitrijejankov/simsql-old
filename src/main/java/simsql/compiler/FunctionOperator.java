

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
public class FunctionOperator implements MathOperator{
	private String name;
	private String functionName;
	private ArrayList<MathOperator>  parameterList;
	private ArrayList<String> typeList;
	
	public FunctionOperator(String name, 
			                String functionName,
			                ArrayList<MathOperator> parameterList,
			                ArrayList<String> typeList)
   {
		super();
		this.name = name;
		this.functionName = functionName;
		this.parameterList = parameterList;
		this.typeList = typeList;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public ArrayList<MathOperator> getParameterList() {
		return parameterList;
	}

	public void setParameterList(ArrayList<MathOperator> parameterList) {
		this.parameterList = parameterList;
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += "function(";
		result += name;
		result += ", ";
		result += functionName;
		result += ", [";
		
		if(parameterList != null)
		{
			for(int i = 0; i < parameterList.size(); i++)
			{
				result += "[" + parameterList.get(i).getNodeName()+ ", " + typeList.get(i) + "]";
				if(i != parameterList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "]).\r\n";
		
		if(parameterList != null)
		{
			for(int i = 0; i < parameterList.size(); i++)
			{
				result += parameterList.get(i).visitNode();
			}
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

	/**
	 * @return the typeList
	 */
	public ArrayList<String> getTypeList() {
		return typeList;
	}
	
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_name = new String(name);
		String c_functionName = new String(functionName);
		ArrayList<MathOperator>  c_parameterList = copyHelper.copyMathOperatorList(parameterList);
		ArrayList<String> c_typeList = copyHelper.copyStringList(typeList);
		
		FunctionOperator functionOperator = new FunctionOperator(c_name, 
															c_functionName, 
															c_parameterList,
															c_typeList);
		
		return functionOperator;
	}

}
