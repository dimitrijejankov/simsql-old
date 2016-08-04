

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
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;

/**
 * @author Bamboo
 *
 */
public class Aggregate extends Operator{

	private String aggregateName;
	private ArrayList<String> groupbyList;
	private ArrayList<MathOperator> aggregateExpressionList;
	private HashMap<MathOperator, ArrayList<String>> columnListMap;
	private HashMap<MathOperator, String> outputMap;
	
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	
	
	public Aggregate(String nodeName, 
			ArrayList<Operator> children, 
			ArrayList<Operator> parents) {
		super(nodeName, children, parents);
	}
	
	

	public Aggregate(String nodeName,
			ArrayList<Operator> children, 
			ArrayList<Operator> parents,
			String aggregateName) {
		super(nodeName, children, parents);
		this.aggregateName = aggregateName;
	}
	
	


	public String getAggregateName() {
		return aggregateName;
	}

	public void setAggregateName(String aggregateName) {
		this.aggregateName = aggregateName;
	}

	public ArrayList<String> getGroupbyList() {
		return groupbyList;
	}

	public void setGroupbyList(ArrayList<String> groupbyList) {
		this.groupbyList = groupbyList;
	}

	public ArrayList<MathOperator> getAggregateExpressionList() {
		return aggregateExpressionList;
	}

	public void setAggregateExpressionList(
			ArrayList<MathOperator> aggregateExpressionList) {
		this.aggregateExpressionList = aggregateExpressionList;
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
		result += this.getNodeStructureString();
		
		result += "genagg(" + this.getNodeName() + ", ";
		result += aggregateName + ", [";
		
		if(groupbyList != null)
		{
			for(int i = 0; i < groupbyList.size(); i++)
			{
				result += groupbyList.get(i);
				
				if(i != groupbyList.size()-1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], [";
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				result += aggregateExpressionList.get(i).getNodeName();
				
				if(i != aggregateExpressionList.size()-1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], [";
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				MathOperator mathOperator = aggregateExpressionList.get(i);
				
				ArrayList<String> element = columnListMap.get(mathOperator);
				result += this.getListString(element);
				
				if(i != aggregateExpressionList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "], [";
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				MathOperator mathOperator = aggregateExpressionList.get(i);
				
				String element = outputMap.get(mathOperator);
				result += "[";
				result += element;
				result += "]";
				
				if(i != aggregateExpressionList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result +=  "]).\r\n";
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				MathOperator mathOperator = aggregateExpressionList.get(i);
				
				result += mathOperator.visitNode();
			}
		}
		
		return result;
	}

	
	public ArrayList<String> getGeneratedNameList()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				MathOperator mathOperator = aggregateExpressionList.get(i);
				resultList.add(outputMap.get(mathOperator));
			}
		}
		return resultList;
	}



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
		String c_aggregateName = new String(aggregateName);
		ArrayList<String> c_groupbyList = copyHelper.copyStringList(groupbyList);
		ArrayList<MathOperator> c_aggregateExpressionList = copyHelper.copyMathOperatorList(aggregateExpressionList);
		HashMap<MathOperator, ArrayList<String>> c_columnListMap = copyHelper.copyMathOperatorStringListMap(aggregateExpressionList, c_aggregateExpressionList, columnListMap);
		HashMap<MathOperator, String> c_outputMap = copyHelper.copyMathOperatorStringMap(aggregateExpressionList, c_aggregateExpressionList, outputMap);
		
		Aggregate aggregate = new Aggregate(commonContent.nodeName,
											commonContent.children,
											commonContent.parents,
											c_aggregateName);
		
		aggregate.setNameMap(commonContent.nameMap);
		aggregate.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		aggregate.setGroupbyList(c_groupbyList);
		aggregate.setAggregateExpressionList(c_aggregateExpressionList);
		aggregate.setColumnListMap(c_columnListMap);
		aggregate.setOutputMap(c_outputMap);
		
		copyHelper.getCopiedMap().put(getNodeName(), aggregate);
		/*
		 * add parent for its children.
		 */
		ArrayList<Operator> children = aggregate.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(aggregate);
			}
		}
		return aggregate;
	}
	
}
