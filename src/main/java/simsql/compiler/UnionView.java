

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

import java.util.ArrayList;

import simsql.compiler.operators.Operator;
import simsql.compiler.operators.VGWrapper;
import simsql.runtime.DataType;


/*
 * This union supports union the tables in different time ticks.
 * Since the Optimizer written by Foula does not support the union operator, here we use VGWrapper to simulate 
 * the existence of union operator. 
 * The structure of the UnionView follows the following structure:
 * ***************************************************************************
 * parent(nodeName, elementNodeName0).
 * parent(nodeName, elementNodeName1).
 * parent(nodeName, elementNodeName2).
 * ...
 * parent(nodeName, elementNodeName_n).
 * node(nodeName, unionVGWrapper, unionVGFunction, element0_seed, 
 * 			[element0.attribute0, .. element_n.attibute0], [],
 * 			[element0.attribute0, .. element_n.attibute0],
 * 			[union.outAttributeList],
 * 			unionVGWrapper_seed2).
 * ***************************************************************************
 * The first table provides the seed and one link to the VGWrapper,
 *  while the others are linked to the VGWrapper directly.
 */
public class UnionView extends VGWrapper
{
	//For intelligently handle the dynamic GeneralTableNameArray.
	/*
	 * The first three parameters is used to record the schema. After the UnionViewTypeChecker, the 
	 * tableAttributeList should not be null (empty).
	 */
	private String viewName;

	// The inputName list and outputName list exists in the VGWrapper.
	private ArrayList<DataType> inputAttributeTypeList;
	private ArrayList<DataType> outputAttributeTypeList;
	
	private boolean isAlignd;
	/*
	 * elementName: used to record the prefix of the element name.
	 * constantTableList: used to record the constant table name.
	 * individualGeneralIndexList: This is used to record the GeneralNameTable.
	 * generalTableBoundList: (value0, value1), (value2, value3)..... The total number of elements in 
	 *    this arrayList should be even, that every two elements define an interval.
	 *    interval.
	 */
	String elementName;
	private ArrayList<String> constantTableList;
	private ArrayList<MathOperator> individualGeneralIndexOperatorList;
	private ArrayList<MathExpression> individualGeneralIndexExpressionList;
	private ArrayList<MathOperator> generalTableBoundOperatorList;
	private ArrayList<MathExpression>  generalTableBoundExpressionList;
	
	public UnionView(String nodeName, 
					 ArrayList<Operator> children,
					 ArrayList<Operator> parents,
					 String viewName,
					 ArrayList<DataType> inputAttributeTypeList,
					 ArrayList<DataType> outputAttributeTypeList,
					 boolean isAligned,
					 String elementName,
					 ArrayList<String> constantTableList,
					 ArrayList<MathOperator> individualGeneralIndexOperatorList,
					 ArrayList<MathExpression> individualGeneralIndexExpressionList,
					 ArrayList<MathOperator> generalTableBoundOperatorList,
					 ArrayList<MathExpression>  generalTableBoundExpressionList)
	{
					 super(nodeName, children, parents);
					 this.viewName = viewName;
					 this.inputAttributeTypeList = inputAttributeTypeList;
					 this.outputAttributeTypeList = outputAttributeTypeList;
					 this.isAlignd = isAligned;
					 this.elementName = elementName;
					 this.constantTableList = constantTableList;
					 this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
					 this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
					 this.generalTableBoundOperatorList = generalTableBoundOperatorList;
					 this.generalTableBoundExpressionList = generalTableBoundExpressionList;
	}
	
	public UnionView(String nodeName, 
					ArrayList<Operator> children, 
					ArrayList<Operator> parents,
					String vgWrapperName, 
					String vgFunctionName,
					String inputSeedAttributeName, 
					String outputSeedAttributeName,
					Operator outerRelationOperator,
					VGFunctionStatistics vgStatistics,
					String viewName,
					ArrayList<DataType> inputAttributeTypeList,
					ArrayList<DataType> outputAttributeTypeList,
					boolean isAligned,
					String elementName,
					ArrayList<String> constantTableList,
					ArrayList<MathOperator> individualGeneralIndexOperatorList,
					ArrayList<MathExpression> individualGeneralIndexExpressionList,
					ArrayList<MathOperator> generalTableBoundOperatorList,
				    ArrayList<MathExpression>  generalTableBoundExpressionList)
	{
		super(nodeName,
				children,
				parents,
				vgWrapperName,
				vgFunctionName,
				inputSeedAttributeName,
				outputSeedAttributeName,
				outerRelationOperator,
				vgStatistics);
		this.viewName = viewName;
		this.inputAttributeTypeList = inputAttributeTypeList;
		this.outputAttributeTypeList = outputAttributeTypeList;
		this.isAlignd = isAligned;
		this.elementName = elementName;
		this.constantTableList = constantTableList;
		this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
		this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
		this.generalTableBoundOperatorList = generalTableBoundOperatorList;
		this.generalTableBoundExpressionList = generalTableBoundExpressionList;
	}
	
	public ArrayList<DataType> getOutputAttributeTypeList() {
		return outputAttributeTypeList;
	}

	public void setOutputAttributeTypeList(ArrayList<DataType> outputAttributeTypeList) {
		this.outputAttributeTypeList = outputAttributeTypeList;
	}
	
	

	public ArrayList<DataType> getInputAttributeTypeList() {
		return inputAttributeTypeList;
	}

	public void setInputAttributeTypeList(ArrayList<DataType> inputAttributeTypeList) {
		this.inputAttributeTypeList = inputAttributeTypeList;
	}

	public String getElementName() {
		return elementName;
	}

	public void setElementName(String elementName) {
		this.elementName = elementName;
	}

	public ArrayList<String> getConstantTableList() {
		return constantTableList;
	}

	public void setConstantTableList(ArrayList<String> constantTableList) {
		this.constantTableList = constantTableList;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public boolean isAlignd() {
		return isAlignd;
	}

	public void setAlignd(boolean isAlignd) {
		this.isAlignd = isAlignd;
	}
	
	

	public ArrayList<MathOperator> getIndividualGeneralIndexOperatorList() {
		return individualGeneralIndexOperatorList;
	}

	public void setIndividualGeneralIndexOperatorList(
			ArrayList<MathOperator> individualGeneralIndexOperatorList) {
		this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
	}

	public ArrayList<MathExpression> getIndividualGeneralIndexExpressionList() {
		return individualGeneralIndexExpressionList;
	}

	public void setIndividualGeneralIndexExpressionList(
			ArrayList<MathExpression> individualGeneralIndexExpressionList) {
		this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
	}

	public ArrayList<MathOperator> getGeneralTableBoundOperatorList() {
		return generalTableBoundOperatorList;
	}

	public void setGeneralTableBoundOperatorList(
			ArrayList<MathOperator> generalTableBoundOperatorList) {
		this.generalTableBoundOperatorList = generalTableBoundOperatorList;
	}

	public ArrayList<MathExpression> getGeneralTableBoundExpressionList() {
		return generalTableBoundExpressionList;
	}

	public void setGeneralTableBoundExpressionList(
			ArrayList<MathExpression> generalTableBoundExpressionList) {
		this.generalTableBoundExpressionList = generalTableBoundExpressionList;
	}

	@Override
	public String visitNode() throws Exception {

		String result = "";
		result += this.getNodeStructureString();
		
		result += "vgwrapper(";
		//nodeName
		result += this.getNodeName();
		result += ", ";
		//vgWrapperName
		result += vgWrapperName;
		result += ", ";
		//vgFunctionName
		result += vgFunctionName.toLowerCase();
		result += ", ";
		//inputSeeds
		result += inputSeedAttributeName;
		result += ", ";
		
		//?
		result += "[";
		
		if(inputAttributeNameList != null)
		{
			for(int i = 0; i < inputAttributeNameList.size(); i++)
			{
				result += inputAttributeNameList.get(i);
				
				if(i != inputAttributeNameList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], ";
		
		//?
		result += "[]";
		result += ", ";
		
		result += "[";
		if(inputAttributeNameList != null)
		{
			for(int i = 0; i < inputAttributeNameList.size(); i++)
			{
				result += inputAttributeNameList.get(i);
				
				if(i != inputAttributeNameList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		result += "], [";
		
		if(outputAttributeNameList != null)
		{
			for(int i = 0; i < outputAttributeNameList.size(); i++)
			{
				result += outputAttributeNameList.get(i);
				
				if(i != outputAttributeNameList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		result += "], ";
		
		result += outputSeedAttributeName;
		result += ").\r\n";
		
		result += "outerRelation(";
		result += this.getNodeName() + ", " + outerRelationOperator.getNodeName();
		result += ").\r\n";
		
		/*
		 * ------------------------vgWrapper properties----------------------------
		 */
		result += "vgwrapperproperties(";
		result += vgWrapperName;
		result += ", bundlesPerTuple(";
		result += 1;
		result += "), [";
		
		String temp;

		/**************** Removed by chris... there is now no reson to append _num to each VG function *********
		int start = vgFunctionName.lastIndexOf("_");
		String realVGFunctionName = vgFunctionName.substring(0, start);
		********************************************************************************************************/

		/***************** Removed by chris.... whether or not an attribute is random is stored in the catalog ********
		ArrayList<String> randomAttributeList;
		if(vgFunctionRandomAttributeMap.containsKey(realVGFunctionName))
		{
			randomAttributeList = vgFunctionRandomAttributeMap.get(realVGFunctionName);
		}
		else
		{
			randomAttributeList = new ArrayList<String>();
		}
		***************************************************************************************************************/
		
		ArrayList<String> outputRandomNameList = new ArrayList<String>();
		
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			temp = "domain(" + outputAttributeNameList.get(i);
			temp += ", ";
			temp += "nothing";
			temp += ", ";
			temp += "infinite";
			temp += ")";
			
			result += temp;
			
			if(i != outputAttributeNameList.size() -1)
			{
				result += ", ";
			}
		}
		
		result += "], [";
		int attributeSize;
		DataType type;
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			type = outputAttributeTypeList.get(i);
			attributeSize = type.getSizeInBytes();
			
			temp = "attributeSize(" + outputAttributeNameList.get(i);
			temp += ", ";
			temp += attributeSize;
			temp += ")";
			
			result += temp;
			
			if(i != outputAttributeNameList.size() -1)
			{
				result += ", ";
			}
		}
		result += "]).\r\n";
		
		//-------------add the deterministic attribute
		
		if(outputRandomNameList.size() >= 1)
		{
			result += "randomAttrsRelation(";
			result += this.getNodeName();
			result += ", [";
			
			for(int i = 0; i < outputRandomNameList.size(); i++)
			{
				result += outputRandomNameList.get(i);
				
				if(i != outputRandomNameList.size() - 1)
				{
					result += ", ";
				}
			}
			
			result += "]).\r\n";
		}
		
		return result;
	}

	@Override
	public Operator copy(CopyHelper copyHelper) throws Exception {
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		
		String c_vgWrapperName = new String(vgWrapperName);
		String c_vgFunctionName = new String(vgFunctionName);
		String c_inputSeedAttributeName = new String(inputSeedAttributeName);
		ArrayList<String> c_inputAttributeNameList = copyHelper.copyStringList(inputAttributeNameList);
		ArrayList<String> c_outputAttributeNameList = copyHelper.copyStringList(outputAttributeNameList);
		String c_outputSeedAttributeName = new String(outputSeedAttributeName);
		
		int outIndex = getChildren().indexOf(outerRelationOperator);
		
		Operator c_outerRelationOperator = commonContent.children.get(outIndex);
		
		String vgFunctionName = new String(vgStatistics.getVfFunctionName());
		String directory = new String(vgStatistics.getDirectory());
		int bundlesPerTuple = vgStatistics.getBundlesPerTuple();
		ArrayList<String> inputAttributes = c_inputAttributeNameList;
		ArrayList<String> outputAttributes = c_outputAttributeNameList;
		
		VGFunctionStatistics c_vgStatistics = new VGFunctionStatistics(vgFunctionName,
				                                                       directory,
				                                                       bundlesPerTuple,
				                                                       inputAttributes,
				                                                       outputAttributes);
		
		String c_viewName = new String(viewName);
		
		ArrayList<DataType> c_inputAttributeTypeList = copyHelper.copyDataTypeList(inputAttributeTypeList);
		ArrayList<DataType> c_outputAttributeTypeList = copyHelper.copyDataTypeList(outputAttributeTypeList);
		
		boolean c_isAlignd = isAlignd;
		String c_elementName = new String(elementName);
		ArrayList<String> c_constantTableList = copyHelper.copyStringList(constantTableList);
		ArrayList<MathOperator> c_individualGeneralIndexList = copyHelper.copyMathOperatorList(individualGeneralIndexOperatorList);
		ArrayList<MathOperator> c_generalTableBoundList = copyHelper.copyMathOperatorList(generalTableBoundOperatorList);
		
		UnionView uionViewOperator =  new UnionView(commonContent.nodeName, 
				commonContent.children, 
				commonContent.parents,
				c_vgWrapperName, 
				c_vgFunctionName,
				c_inputSeedAttributeName, 
				c_outputSeedAttributeName,
				c_outerRelationOperator,
				c_vgStatistics,
				c_viewName,
				c_inputAttributeTypeList,
				c_outputAttributeTypeList,
				c_isAlignd,
				c_elementName,
				c_constantTableList,
				c_individualGeneralIndexList,
				individualGeneralIndexExpressionList,
				c_generalTableBoundList,
				generalTableBoundExpressionList);
		
		uionViewOperator.setInputAttributeNameList(inputAttributes);
		uionViewOperator.setOutputAttributeNameList(outputAttributes);
		
		uionViewOperator.setNameMap(commonContent.nameMap);
		uionViewOperator.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		copyHelper.getCopiedMap().put(getNodeName(), uionViewOperator);
		
		ArrayList<Operator> children = uionViewOperator.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(uionViewOperator);
			}
		}
		
		return uionViewOperator;
		
	}

}
