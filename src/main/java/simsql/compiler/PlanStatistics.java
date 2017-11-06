

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
package simsql.compiler; // package mcdb.compiler.logicPlan;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import simsql.compiler.math_operators.EFunction;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.*;
import simsql.runtime.DataType;
import simsql.runtime.SeedType;


/**
 * @author Bamboo
 *
 *	Since the tables and vgFunctions can be used multiple times, but in the plan, we only explicitly
 *	state them for only one time. So we traverse all the code by one time, and output each Relation
 *	-Statistics and VGFunctionStatistics for only one time.
 */
public class PlanStatistics {
	
	private ArrayList<Operator> sinkList;
	private Catalog catalog;
	
	
	public PlanStatistics(ArrayList<Operator> sinkList) {
		super();
		this.sinkList = sinkList;
		catalog = SimsqlCompiler.catalog;
	}
	
	public ArrayList<Operator> getOperatorList() {
		return sinkList;
	}

	public void setOperatorList(ArrayList<Operator> sinkList) {
		this.sinkList = sinkList;
	}

	public String getStatistics()throws Exception
	{
		String result = "";
		ArrayList<StatisticsOperator> list = getStatisticsOperatorList();

		for (StatisticsOperator operator : list) {
			result += operator.visitNode();
		}
		
		return result;
	}
	
	public String getAttributeTypeStatistics()throws Exception
	{
		ArrayList<AttributeType> resultList = new ArrayList<AttributeType>();
		HashMap<String, DataType> attributeTypeMap = new HashMap<String, DataType>();
		
		ArrayList<Operator> operatorList = PostProcessorHelper.findAllNode(sinkList);
		operatorList = PostProcessorHelper.topologicalSort(operatorList);
		
		for(int i = 0; i < operatorList.size(); i++)
		{
			Operator element = operatorList.get(i);
			
			switch(PostProcessorHelper.getNodeType(element))
			{
				case PostProcessorHelper.AGGREGATE:
					getAttributeFromAggregate((Aggregate)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.DUPLICATEREMOVE:
					getAttributeFromDuplicateRemove((DuplicateRemove)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.JOIN:
					getAttributeFromJoin((Join)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.PROJECTION:
					getAttributeFromProjection((Projection)element, attributeTypeMap);
					break;
						
				case PostProcessorHelper.SCALARFUNCTION:
					getAttributeFromScalarFunction((ScalarFunction)element, attributeTypeMap);
					break; 
					
				case PostProcessorHelper.SEED:
					getAttributeFromSeed((Seed)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.SELECTION:
					getAttributeFromSelection((Selection)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.TABLESCAN:
					getAttributeFromTableScan((TableScan)element, attributeTypeMap);
					break;
					
				case PostProcessorHelper.VGWRAPPER:
					getAttributeFromVGWrapper((VGWrapper)element, attributeTypeMap);
					break;
			}	
		}
		
		String []names = new String[attributeTypeMap.size()];
		attributeTypeMap.keySet().toArray(names);
		
		for(int i = 0; i < names.length; i++)
		{
			DataType type = attributeTypeMap.get(names[i]);
			AttributeType attributeType = null;
			attributeType = new AttributeType(names[i], type);
			// switch(type)
			// {
			// 	case Attribute.INTEGER:
			// 		attributeType = new AttributeType(names[i], "integer");
			// 		break;
					
			// 	case Attribute.DOUBLE:
			// 		attributeType = new AttributeType(names[i], "double");
			// 		break;
					
			// 	case Attribute.STRING:
			// 		attributeType = new AttributeType(names[i], "string");
			// 		break;
					
			// 	case Attribute.DATE:
			// 		attributeType = new AttributeType(names[i], "date");
			// 		break;
					
			// 	case Attribute.STOCHINT:
			// 		attributeType = new AttributeType(names[i], "integer");
			// 		break;
					
			// 	case Attribute.STOCHDBL:
			// 		attributeType = new AttributeType(names[i], "double");
			// 		break;
					
			// 	case Attribute.STOCHDAT:
			// 		attributeType = new AttributeType(names[i], "date");
			// 		break;
					
			// 	case Attribute.SEED:
			// 		attributeType = new AttributeType(names[i], "seed");
			// 		break;	
			// }
			
			if(attributeType != null)
				resultList.add(attributeType);
		}
		
		String result = "";
		
		for(int i = 0; i < resultList.size(); i++)
		{
			result += resultList.get(i).visitNode();
		}
		
		return result;
	}
	
	
	
	
	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromVGWrapper(VGWrapper element,
			HashMap<String, DataType> attributeTypeMap) throws Exception {
		
		/*
		 * Hack up for the UnionView Operator, since UnionView is a subclass of VGWrapper.
		 */
		
		if(element instanceof UnionView)
		{
			getAttributeFromUnionView((UnionView)element, attributeTypeMap);
			return;
		}
		else
		{
		
			String vgFunctionName = element.getVgFunctionName();
			
			ArrayList<String> inputAttributeNameList = element.getInputAttributeNameList();
			ArrayList<String> outputAttributeNameList = element.getOutputAttributeNameList();
			String outputSeedAttributeName = element.getOutputSeedAttributeName();
			String inputSeedAttributeName = element.getInputSeedAttributeName();
			
	                /************ Removed by chris: we no longer append the VG function name with _number *******
			int start = vgFunctionName.lastIndexOf("_");
			String realVGName = vgFunctionName.substring(0, start);
			********************************************************************************************/
	
			VGFunction vgf = catalog.getVGFunction(vgFunctionName);
			
			ArrayList<Attribute> outAttributeList = vgf.getOutputAtts();
			ArrayList<Attribute> inAttributeList = vgf.getInputAtts();
			
			for(int i = 0; i < inputAttributeNameList.size(); i++)
			{
				String attributeName = inputAttributeNameList.get(i);
				Attribute attribute = inAttributeList.get(i);
				
				attributeTypeMap.put(attributeName, attribute.getType());
			}
			
			for(int i = 0; i < outAttributeList.size(); i++)
			{
				String attributeName = outputAttributeNameList.get(i);
				Attribute attribute = outAttributeList.get(i);
				
				attributeTypeMap.put(attributeName, attribute.getType());
			}
			
			attributeTypeMap.put(outputSeedAttributeName, new SeedType());
			attributeTypeMap.put(inputSeedAttributeName, new SeedType());
		}
	}
	
	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromUnionView(UnionView element,
			HashMap<String, DataType> attributeTypeMap) throws Exception {
		
		/*
		 * The 
		 */
		ArrayList<String> inputAttributeNameList = element.getInputAttributeNameList();
		ArrayList<DataType> inputAttributeTypeList = element.getInputAttributeTypeList();
		ArrayList<String> outputAttributeNameList = element.getOutputAttributeNameList();
		ArrayList<DataType> outputAttributeTypeList = element.getOutputAttributeTypeList();
		
		String outputSeedAttributeName = element.getOutputSeedAttributeName();
		String inputSeedAttributeName = element.getInputSeedAttributeName();
		
		for(int i = 0; i < inputAttributeNameList.size(); i++)
		{
			String attributeName = inputAttributeNameList.get(i);
			DataType attributeType = inputAttributeTypeList.get(i);
			attributeTypeMap.put(attributeName, attributeType);
		}
		
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			String attributeName = outputAttributeNameList.get(i);
			DataType attributeType = outputAttributeTypeList.get(i);
			
			attributeTypeMap.put(attributeName, attributeType);
		}
		
		attributeTypeMap.put(outputSeedAttributeName, new SeedType());
		attributeTypeMap.put(inputSeedAttributeName, new SeedType());
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromTableScan(TableScan element,
			HashMap<String, DataType> attributeTypeMap) throws Exception{
		String tableName = element.getTableName();
		ArrayList<String> tableScanList = element.getAttributeList();
		
		ArrayList<Attribute> attributeList;
		if(element.getIndexStrings().isEmpty() && element.getType() == TableReference.COMMON_TABLE)
		{
		    // if we have the table info we don't need to fetch the relation from the catalog
			if(element.getTableInfo() != null) {
			    attributeList = element.getTableInfo().getRealAttributeList();
            }
            else {
                attributeList = catalog.getRelation(tableName).getAttributes();
            }
		}
		else
		{
			View view = catalog.getView(tableName);
			attributeList = view.getAttributes();
		}
		
		for(int i = 0; i < tableScanList.size(); i++)
		{
			String attributeName = tableScanList.get(i);
			Attribute attribute = attributeList.get(i);
			
			attributeTypeMap.put(attributeName, attribute.getType());
		}
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromSelection(Selection element,
			HashMap<String, DataType> attributeTypeMap) {
		/*
		 * selection do not generate the new attributes: nothing to do..
		 */
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromSeed(Seed element,
			HashMap<String, DataType> attributeTypeMap) {
		String seedAttributeName = element.getSeedAttributeName();
		attributeTypeMap.put(seedAttributeName, new SeedType());
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromScalarFunction(ScalarFunction element,
			HashMap<String, DataType> attributeTypeMap)  throws Exception{
		ArrayList<MathOperator> scalarExpressionList = element.getScalarExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		
		for(int i = 0; i < scalarExpressionList.size(); i++)
		{
			MathOperator tempOperator = scalarExpressionList.get(i);
			ArrayList<String> inputAttributeList = columnListMap.get(tempOperator);
			String output = outputMap.get(tempOperator);
			
			DataType type;
			
			if(tempOperator instanceof EFunction)
			{
				String firstInput = inputAttributeList.get(0);
				
				type = attributeTypeMap.get(firstInput);
			}
			else
			{
				type = new TypeInference(attributeTypeMap, tempOperator).getType();
				//type = null;
			}
			attributeTypeMap.put(output, type);
		}
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromProjection(Projection element,
			HashMap<String, DataType> attributeTypeMap) {
		/*
		 * selection do not generate the new attributes: nothing to do..
		 */
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromJoin(Join element,
			HashMap<String, DataType> attributeTypeMap) {
		/*
		 * selection do not generate the new attributes: nothing to do..
		 */
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromDuplicateRemove(DuplicateRemove element,
			HashMap<String, DataType> attributeTypeMap) {
		/*
		 * selection do not generate the new attributes: nothing to do..
		 */
	}

	/**
	 * @param element
	 * @param attributeTypeMap
	 */
	private void getAttributeFromAggregate(Aggregate element,
			HashMap<String, DataType> attributeTypeMap)  throws Exception
	{
		ArrayList<MathOperator> aggregateExpressionList = element.getAggregateExpressionList();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		
		for(int i = 0; i < aggregateExpressionList.size(); i++)
		{
			MathOperator mathOperator = aggregateExpressionList.get(i);
			DataType type = new TypeInference(attributeTypeMap, mathOperator).getType();
			String output = outputMap.get(mathOperator);
			attributeTypeMap.put(output, type);
		}
	}

	private ArrayList<StatisticsOperator> getStatisticsOperatorList()
	{
		ArrayList<StatisticsOperator> resultList = new ArrayList<StatisticsOperator>();
		
		HashSet<String> tableSet = new HashSet<String>();
		HashSet<String> vgFunctionSet = new HashSet<String>();
		/*
		 * 1. System statistics
		 */
		SystemParameters parameters = getSystemParameters();
		resultList.add(parameters);
		
		ArrayList<Operator> operatorList = PostProcessorHelper.findAllNode(sinkList);
		
		for(int i = 0; i < operatorList.size(); i++)
		{
			Operator temp = operatorList.get(i);
			if(temp instanceof TableScan)
			{
				RelationStatistics tempStatiscs = ((TableScan) temp).getRelationStatistics();
				String tableName = tempStatiscs.getRelation();
				
				if(!tableSet.contains(tableName))
				{
					tableSet.add(tableName);
					
					resultList.add(tempStatiscs);
				}
			}
			else if(temp instanceof VGWrapper)
			{
				VGFunctionStatistics tempStatiscs = ((VGWrapper) temp).getVgStatistics();
				String vgFunctionName = tempStatiscs.getVfFunctionName();
				
				if(!vgFunctionSet.contains(vgFunctionName))
				{
					vgFunctionSet.add(vgFunctionName);
					
					resultList.add(tempStatiscs);
				}
			}
		}
		
		return resultList;
	}

	public SystemParameters getSystemParameters()
	{
		Catalog  catalog = SimsqlCompiler.catalog;
		
		int monteCarloIterations = new Integer(catalog.getMonteCarloIterations()).intValue();
		int vgThreads = new Integer(catalog.getVGFunctionThreads()).intValue();
		int vgTuples = new Integer(catalog.getVGTuples()).intValue();
		int vgPipeSize = new Integer(catalog.getPipeSize()).intValue();
		double defaultSelectivity = new Double(catalog.getDefaultSelectivity()).doubleValue();
		int isPreSize = new Integer(catalog.getIsPresentSize()).intValue();
		int seedSize = new Integer(catalog.getSeedSize()).intValue();
		
		double selectionCostWeight = new Double(catalog.getSelectionCostWeight()).doubleValue();
		double tablescanCostWeight = new Double(catalog.getTablescanCostWeight()).doubleValue();
		double seedCostWeight = new Double(catalog.getSeedCostWeight()).doubleValue();
		double splitCostWeight = new Double(catalog.getSplitCostWeight()).doubleValue();
		double joinCostWeight = new Double(catalog.getJoinCostWeight()).doubleValue();
		double antijoinCostWeight = new Double(catalog.getAntijoinCostWeight()).doubleValue();
		double semijoinCostWeight = new Double(catalog.getSemijoinCostWeight()).doubleValue();
		double dedupCostWeight = new Double(catalog.getDedupCostWeight()).doubleValue();
		double projectionCostWeight = new Double(catalog.getProjectionCostWeight()).doubleValue();
		double genaggCostWeight = new Double(catalog.getGenaggCostWeight()).doubleValue();
		double vgwrapperCostWeight = new Double(catalog.getVgWrapperCostWeight()).doubleValue();
		double scalarfuncCostWeight = new Double(catalog.getScalarCostWeight()).doubleValue();
		
		SystemParameters parameters = new SystemParameters(monteCarloIterations,
														   vgThreads,
														   vgTuples, 
														   vgPipeSize,
														   defaultSelectivity,
														   isPreSize,
														   seedSize,
														   selectionCostWeight,
														   tablescanCostWeight,
														   seedCostWeight,
														   splitCostWeight,
														   joinCostWeight,
														   antijoinCostWeight,
														   semijoinCostWeight,
														   dedupCostWeight,
														   projectionCostWeight,
														   genaggCostWeight,
														   vgwrapperCostWeight,
														   scalarfuncCostWeight
														   );
		
		return parameters;
	}
}
