

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


// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.VGFunction;
// import mcdb.compiler.FileOperation;
// import mcdb.compiler.logicPlan.logicOperator.CommonContent;
// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
// import mcdb.compiler.logicPlan.logicOperator.statisticsOperator.VGFunctionStatistics;
// import mcdb.compiler.parser.astVisitor.TypeChecker;



/**
 * @author Bamboo
 *
 */
public class VGWrapper extends Operator{

	protected String vgWrapperName;
	protected String vgFunctionName;
	protected String inputSeedAttributeName;
	protected ArrayList<String> inputAttributeNameList;
	protected ArrayList<String> outputAttributeNameList;
	protected String outputSeedAttributeName;
	protected Operator outerRelationOperator;
	/*
	 * Here vgStatistics share the inputAttributeNameList and outputAttributeNameList
	 */
	protected VGFunctionStatistics vgStatistics;
	
	protected Catalog catalog = SimsqlCompiler.catalog;
	
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public VGWrapper(String nodeName, 
			ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
	}

	public VGWrapper(String nodeName, 
			ArrayList<Operator> children, 
			ArrayList<Operator> parents,
			String vgWrapperName, 
			String vgFunctionName,
			String inputSeedAttributeName, 
			String outputSeedAttributeName,
			Operator outerRelationOperator,
			VGFunctionStatistics vgStatistics)
	{
		super(nodeName, children, parents);
		this.vgWrapperName = vgWrapperName;
		this.vgFunctionName = vgFunctionName;
		this.inputSeedAttributeName = inputSeedAttributeName;
		this.outputSeedAttributeName = outputSeedAttributeName;
		this.outerRelationOperator = outerRelationOperator;
		this.vgStatistics = vgStatistics;
		
		catalog = SimsqlCompiler.catalog;
	}

	public String getVgWrapperName() {
		return vgWrapperName;
	}

	public void setVgWrapperName(String vgWrapperName) {
		this.vgWrapperName = vgWrapperName;
	}

	public String getVgFunctionName() {
		return vgFunctionName;
	}

	public void setVgFunctionName(String vgFunctionName) {
		this.vgFunctionName = vgFunctionName;
	}

	public String getInputSeedAttributeName() {
		return inputSeedAttributeName;
	}

	public void setInputSeedAttributeName(String inputSeedAttributeName) {
		this.inputSeedAttributeName = inputSeedAttributeName;
	}

	public ArrayList<String> getInputAttributeNameList() {
		return inputAttributeNameList;
	}

	public void setInputAttributeNameList(ArrayList<String> inputAttributeNameList) {
		this.inputAttributeNameList = inputAttributeNameList;
	}

	public ArrayList<String> getOutputAttributeNameList() {
		return outputAttributeNameList;
	}

	public void setOutputAttributeNameList(ArrayList<String> outputAttributeNameList) {
		this.outputAttributeNameList = outputAttributeNameList;
	}

	public String getOutputSeedAttributeName() {
		return outputSeedAttributeName;
	}

	public void setOutputSeedAttributeName(String outputSeedAttributeName) {
		this.outputSeedAttributeName = outputSeedAttributeName;
	}

	public Operator getOuterRelationOperator() {
		return outerRelationOperator;
	}

	public void setOuterRelationOperator(Operator outerRelationOperator) {
		this.outerRelationOperator = outerRelationOperator;
	}
	
	

	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode()  throws Exception{

		/******************* REMOVED BY CHRIS... random att info is now in the catalog **********************
		HashMap<String, ArrayList<String>> vgFunctionRandomAttributeMap = getRandomAttributes("/var/tmp/jaql/VGFunctionAttributeProperties");
		*****************************************************************************************************/

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
		result += vgStatistics.getBundlesPerTuple();
		result += "), [";
		
		String temp;

		/**************** Removed by chris... there is now no reson to append _num to each VG function *********
		int start = vgFunctionName.lastIndexOf("_");
		String realVGFunctionName = vgFunctionName.substring(0, start);
		********************************************************************************************************/

		VGFunction vgf = catalog.getVGFunction(vgFunctionName);
		ArrayList<Attribute> attributeList = vgf.getOutputAtts();
		Attribute tempAttribute;
		
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
		ArrayList<String> domainTypeList = new ArrayList<String>();
		ArrayList<String> domainSizeList = new ArrayList<String>();
		
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			tempAttribute = attributeList.get(i);

			// note that now we just ask the attribute if it is random
			if(tempAttribute.getIsRandom ())
			{
				outputRandomNameList.add(outputAttributeNameList.get(i));
				domainTypeList.add(tempAttribute.getDomainType());
				domainSizeList.add(tempAttribute.getDomainSize());
			}
		}
		
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			tempAttribute = attributeList.get(i);
			
			temp = "domain(" + outputAttributeNameList.get(i);
			temp += ", ";
			temp += tempAttribute.getDomainType();
			temp += ", ";
			temp += tempAttribute.getDomainSize();
			temp += ")";
			
			result += temp;
			
			if(i != outputAttributeNameList.size() -1)
			{
				result += ", ";
			}
		}
		
		result += "], [";
		int attributeSize;
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			tempAttribute = attributeList.get(i);
			attributeSize = tempAttribute.getAttributeSize();
			
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

	
	public ArrayList<String> getGeneratedNameList()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		if(outputAttributeNameList != null)
		{
			for(int i = 0; i < outputAttributeNameList.size(); i++)
			{
				String attributeName = outputAttributeNameList.get(i);
				resultList.add(attributeName);
			}
		}
		
		resultList.add(outputSeedAttributeName);
		return resultList;
	}

	public VGFunctionStatistics getVgStatistics() {
		return vgStatistics;
	}

	public void setVgStatistics(VGFunctionStatistics vgStatistics) {
		this.vgStatistics = vgStatistics;
	}
	
	// public String getTypeSizeString(int type)
	// {
	// 	String result = null;							// TO-DO
	// 	switch(type)
	// 	{
	// 		case Attribute.INTEGER:
	// 			result = "integerSize";
	// 			break;
				
	// 		case Attribute.DOUBLE:
	// 			result = "doubleSize";
	// 			break;
				
	// 		case Attribute.STRING:
	// 			result = "stringSize";
	// 			break;
				
	// 		case Attribute.DATE:
	// 			result = "dateSize";
	// 			break;
				
	// 		case Attribute.STOCHINT:
	// 			result ="stochdateintegerSize";
	// 			break;
				
	// 		case Attribute.STOCHDBL:
	// 			result = "stochdoubleSize";
	// 			break;
				
	// 		case Attribute.STOCHDAT:
	// 			result = "stochdateSize";
	// 			break;
				
	// 		case Attribute.SEED:
	// 			result = "seedSize";
	// 			break;	
	// 	}
	// 	return result;
	// }

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator#copy()
	 */
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
		
		VGWrapper vgWrapper = new VGWrapper(commonContent.nodeName, 
				commonContent.children, 
				commonContent.parents,
				c_vgWrapperName, 
				c_vgFunctionName,
				c_inputSeedAttributeName, 
				c_outputSeedAttributeName,
				c_outerRelationOperator,
				c_vgStatistics);
		
		vgWrapper.setInputAttributeNameList(c_inputAttributeNameList);
		vgWrapper.setOutputAttributeNameList(c_outputAttributeNameList);
		vgWrapper.setNameMap(commonContent.nameMap);
		vgWrapper.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), vgWrapper);
		
		ArrayList<Operator> children = vgWrapper.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(vgWrapper);
			}
		}
		return vgWrapper;
	}
	
	public HashMap<String, ArrayList<String>> getRandomAttributes(String filepath)
	{
		HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();

		String []lines = FileOperation.getFile_content(filepath);
		for(int i = 0; i < lines.length; i++)
		{
			String tokens[] = lines[i].split(" ");
			ArrayList<String> tempList = new ArrayList<String>();
			for(int j = 1; j < tokens.length; j++)
			{
				tempList.add(tokens[j]);
			}
			resultMap.put(tokens[0], tempList);
		}
		
		return resultMap;	
	}
	
}
