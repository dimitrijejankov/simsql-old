

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.relationOperator;

import java.util.ArrayList;
import java.util.HashMap;



// import mcdb.compiler.logicPlan.logicOperator.CommonContent;
// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.DateOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.NumberOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.StringOperator;
// import mcdb.compiler.logicPlan.translator.TranslatorHelper;

/**
 * @author Bamboo
 *
 */
public class ScalarFunction extends Operator{

	private ArrayList<MathOperator> scalarExpressionList;
	private HashMap<MathOperator, ArrayList<String>> columnListMap;
	private HashMap<MathOperator, String> outputMap;
	private TranslatorHelper translatorHelper;

	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public ScalarFunction(String nodeName, 
						  ArrayList<Operator> children, 
						  ArrayList<Operator> parents,
						  TranslatorHelper translatorHelper)
	{
		super(nodeName, children, parents);
		this.translatorHelper = translatorHelper;
	}

	public ArrayList<MathOperator> getScalarExpressionList() {
		return scalarExpressionList;
	}

	public void setScalarExpressionList(ArrayList<MathOperator> scalarExpressionList) {
		this.scalarExpressionList = scalarExpressionList;
	}

	public HashMap<MathOperator, ArrayList<String>> getColumnListMap() {
		return columnListMap;
	}

	public void setColumnListMap(
			HashMap<MathOperator, ArrayList<String>> columnListMap) {
		this.columnListMap = columnListMap;
	}

	public HashMap<MathOperator, String> getOutputMap() {
		return outputMap;
	}

	public void setOutputMap(HashMap<MathOperator, String> outputMap) {
		this.outputMap = outputMap;
	}
	
	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		ArrayList<String> constantArithList = new ArrayList<String>();
		String mathName;
		
		
		result += this.getNodeStructureString();
		
		result += "scalarfunc(" + this.getNodeName() + ", [";
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				MathOperator tempOperator = scalarExpressionList.get(i);
				
				if(tempOperator instanceof NumberOperator )
				{
					mathName = "arithExp" + translatorHelper.getArithExpIndex();
					constantArithList.add("verbatim(" + mathName + ", " + tempOperator.getNodeName() + ", isNumeric).\r\n");
					result += mathName;
				}
				else if(tempOperator instanceof DateOperator ||
						tempOperator instanceof StringOperator)
				{
					mathName = "arithExp" + translatorHelper.getArithExpIndex();
					constantArithList.add("verbatim(" + mathName + ", " + tempOperator.getNodeName() + ", isNotNumeric).\r\n");
					result += mathName;
				}
				else
				{
					result += tempOperator.getNodeName();
				}
				
				if(i != scalarExpressionList.size()-1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], [";
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				MathOperator mathOperator = scalarExpressionList.get(i);
				
				ArrayList<String> element = columnListMap.get(mathOperator);
				result += this.getListString(element);
				
				if(i != scalarExpressionList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], [";
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				MathOperator mathOperator = scalarExpressionList.get(i);
				
				String element = outputMap.get(mathOperator);
				result += "[";
				result += element;
				result += "]";
				
				if(i != scalarExpressionList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result +=  "]).\r\n";
		
		for(int i = 0; i < constantArithList.size(); i++)
		{
			result += constantArithList.get(i);
		}
			
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				MathOperator mathOperator = scalarExpressionList.get(i);
				
				result += mathOperator.visitNode();
			}
		}
		
		return result;
	}
	
	public ArrayList<String> getGeneratedNameList()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				MathOperator mathOperator = scalarExpressionList.get(i);
				resultList.add(outputMap.get(mathOperator));
			}
		}
		return resultList;
	}
	
	public Operator copy(CopyHelper copyHelper) throws Exception
	{
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		
		ArrayList<MathOperator> c_scalarExpressionList = copyHelper.copyMathOperatorList(scalarExpressionList);
		HashMap<MathOperator, ArrayList<String>> c_columnListMap = copyHelper.copyMathOperatorStringListMap(scalarExpressionList, c_scalarExpressionList, columnListMap);
		HashMap<MathOperator, String> c_outputMap = copyHelper.copyMathOperatorStringMap(scalarExpressionList, c_scalarExpressionList, outputMap);
		TranslatorHelper c_translatorHelper = this.translatorHelper;
		
		
		ScalarFunction scalarFunction = new ScalarFunction(commonContent.nodeName,
														   commonContent.children, 
														   commonContent.parents,
														   c_translatorHelper);
		
		scalarFunction.setNameMap(commonContent.nameMap);
		scalarFunction.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		scalarFunction.setScalarExpressionList(c_scalarExpressionList);
		scalarFunction.setColumnListMap(c_columnListMap);
		scalarFunction.setOutputMap(c_outputMap);
		
		copyHelper.getCopiedMap().put(getNodeName(), scalarFunction);
		ArrayList<Operator> children = scalarFunction.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(scalarFunction);
			}
		}
		return scalarFunction;
	}
	
}
