

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
package simsql.compiler; // package mcdb.compiler.logicPlan.postProcessor;

import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Aggregate;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.DuplicateRemove;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Join;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Projection;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.ScalarFunction;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Seed;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Selection;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.TableScan;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.VGWrapper;

/**
 * @author Bamboo
 *
 */
public class LogicOperatorReplacer
{	
	public static void replaceAttribute(Operator element)
	{
		switch(PostProcessorHelper.getNodeType(element))
		{
			case PostProcessorHelper.AGGREGATE:
				replaceAttributeInAggregate((Aggregate)element);
				break;
				
			case PostProcessorHelper.DUPLICATEREMOVE:
				replaceAttributeInDuplicateRemove((DuplicateRemove)element);
				break;
				
			case PostProcessorHelper.JOIN:
				replaceAttributeInJoin((Join)element);
				break;
				
			case PostProcessorHelper.PROJECTION:
				replaceAttributeInProjection((Projection)element);
				break;
					
			case PostProcessorHelper.SCALARFUNCTION:
				replaceAttributeInScalarFunction((ScalarFunction)element);
				break; 
				
			case PostProcessorHelper.SEED:
				replaceAttributeInSeed((Seed)element);
				break;
				
			case PostProcessorHelper.SELECTION:
				replaceAttributeInSelection((Selection)element);
				break;
				
			case PostProcessorHelper.TABLESCAN:
				replaceAttributeInTableScan((TableScan)element);
				break;
				
			case PostProcessorHelper.VGWRAPPER:
				replaceAttributeInVGWrapper((VGWrapper)element);
				break;
			 
		}
	}
	
	public static void replaceAttributeInAggregate(Aggregate element)
	{
		ArrayList<String> groupbyList = element.getGroupByList();
		ArrayList<MathOperator> aggregateExpressionList = element.getAggregateExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		HashMap<String, String> nameMap = element.getNameMap();
		/*
		 * 1. Change the groupBy attribute list.
		 */
		replaceList(groupbyList, nameMap);
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				/*
				 * 2. Change the attribute name in the columnListMap
				 */
				
				MathOperator mathOperator = aggregateExpressionList.get(i);
				ArrayList<String> list = columnListMap.get(mathOperator);
				replaceList(list, nameMap);
				
				/*
				 * 3. Change the attribute name in output
				 */
				String output = outputMap.get(mathOperator);
				String mappedOutput = nameMap.get(output);
				outputMap.put(mathOperator, mappedOutput);
			}
			
			/*
			 * 4. change the attribute in the aggregateExpressionList
			 */
			replaceMathOperatorList(element, aggregateExpressionList);
		}
		
	}
	public static void replaceAttributeInDuplicateRemove(DuplicateRemove element)
	{
		return;
	}
	
	public static void replaceAttributeInJoin(Join element)
	{
		BooleanOperator booleanOperator = element.getBooleanOperator();
		if(booleanOperator != null)
		{
			BooleanOperatorReplacer booleanOperatorReplacer = new BooleanOperatorReplacer(element);
			booleanOperatorReplacer.replaceAttributeInBooleanOperator(booleanOperator);
		}
	}
	
	public static void replaceAttributeInProjection(Projection element)
	{
		ArrayList<String> projectedNameList = element.getProjectedNameList();
		HashMap<String, String> nameMap = element.getNameMap();
		
		if(projectedNameList != null)
		{
			replaceList(projectedNameList, nameMap);
		}
	}
	
	public static void replaceAttributeInScalarFunction(ScalarFunction element)
	{
		ArrayList<MathOperator> scalarExpressionList = element.getScalarExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		
		HashMap<String, String> nameMap = element.getNameMap();
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				/*
				 * 1. Change the attribute name in the columnListMap
				 */
				
				MathOperator mathOperator = scalarExpressionList.get(i);
				ArrayList<String> list = columnListMap.get(mathOperator);
				replaceList(list, nameMap);
				
				/*
				 * 2. Change the attribute name in output
				 */
				String output = outputMap.get(mathOperator);
				String mappedOutput = nameMap.get(output);
				outputMap.put(mathOperator, mappedOutput);
			}
			
			/*
			 * 3. change the attribute in the scalarExpressionList
			 */
			replaceMathOperatorList(element, scalarExpressionList);
		}
	}
	
	public static void replaceAttributeInSeed(Seed element)
	{
		HashMap<String, String> nameMap = element.getNameMap();
		String seedAttributeName = element.getSeedAttributeName();
		
		if(nameMap.containsKey(seedAttributeName))
		{
			String mappedName = nameMap.get(seedAttributeName);
			element.setSeedAttributeName(mappedName);
		}
	}
	
	public static void replaceAttributeInSelection(Selection element)
	{
		BooleanOperator booleanOperator = element.getBooleanOperator();
		if(booleanOperator != null)
		{
			BooleanOperatorReplacer booleanOperatorReplacer = new BooleanOperatorReplacer(element);
			booleanOperatorReplacer.replaceAttributeInBooleanOperator(booleanOperator);
		}
	}
	
	public static void replaceAttributeInTableScan(TableScan element)
	{
		ArrayList<String> attributeList = element.getAttributeList();
		HashMap<String, String> nameMap = element.getNameMap();
		
		if(attributeList != null)
		{
			replaceList(attributeList, nameMap);
		}
	}
	
	public static void replaceAttributeInVGWrapper(VGWrapper element)
	{
		HashMap<String, String> nameMap = element.getNameMap();
		String inputSeedAttributeName = element.getInputSeedAttributeName();
		ArrayList<String> inputAttributeNameList = element.getInputAttributeNameList();
		ArrayList<String> outputAttributeNameList = element.getOutputAttributeNameList();
		String outputSeedAttributeName = element.getOutputSeedAttributeName();
		/*
		 * 1. inputSeedAttributeName
		 */
		if(nameMap.containsKey(inputSeedAttributeName))
		{
			String mappedName = nameMap.get(inputSeedAttributeName);
			element.setInputSeedAttributeName(mappedName);
		}
		
		/*
		 * 2. inputAttributeNameList
		 */
		if(inputAttributeNameList != null)
		{
			replaceList(inputAttributeNameList, nameMap);
		}
		
		/*
		 * 3. outputAttributeNameList
		 */
		if(outputAttributeNameList != null)
		{
			replaceList(outputAttributeNameList, nameMap);
		}
		
		/*
		 * 4. outputSeedAttributeName
		 */
		if(nameMap.containsKey(outputSeedAttributeName))
		{
			String mappedName = nameMap.get(outputSeedAttributeName);
			element.setOutputSeedAttributeName(mappedName);
		}
	}
	
	private static void replaceList(ArrayList<String> list, HashMap<String, String> nameMap)
	{
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				String originalString = list.get(i);
				if(nameMap.containsKey(originalString))
				{
					String mapString = nameMap.get(originalString);
					if(!list.contains(mapString))
					{
						list.remove(i);
						list.add(i, mapString);
					}
					else
					{
						list.remove(i);
						i--;
					}
				}
			}
		}
	}
	
	private static void replaceMathOperatorList(Operator operator,
			                                    ArrayList<MathOperator> list)
	{
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				MathOperatorReplacer mathReplacer = new MathOperatorReplacer(operator);
				mathReplacer.replaceAttributeInMathOperator(list.get(i));
			}
		}
	}
	
/*
	 * ================================================= hack for the self-join =======================================
	 */
	public static void renameSelfJoinAttributeListAlongTheDAG(ArrayList<RenameNode> selfJoinRenameList)
	{
		for(int i = 0; i < selfJoinRenameList.size(); i++)
		{
			RenameNode tempRenameNode = selfJoinRenameList.get(i);
			renameSelfJoinAttributeListAlongTheDAG(tempRenameNode.parent, tempRenameNode.attributeMap);
		}
	}
	
	private static void renameSelfJoinAttributeListAlongTheDAG(Operator element, HashMap<String, String> attributeMap)
	{
		/*
		 * We adopt a downToTop algorithm, try to rename each node one by one according each attribute in the attributeMap.
		 */
		for(String originalName: attributeMap.keySet())
		{
			String mappedName = attributeMap.get(originalName);
			renameSelfJoinAttribute(element, originalName, mappedName);
		}
		
	}
	
	public static void renameSelfJoinAttribute(Operator element, String originalName, String mappedName)
	{
		/*
		 * 1. record the mapped name
		 */
		HashSet<Operator> nodeSet = new HashSet<Operator>();
		renameSelfJoinAttribute(element, originalName, mappedName, nodeSet);
		
		/*
		 * 2. For all the nodes in the nodeSet, change their name.
		 */
		for(Operator tempOperator: nodeSet)
		{
			replaceAttributeForSelfJoin(tempOperator, originalName, mappedName);
		}
	}
	
	public static void renameSelfJoinAttribute(Operator element, String originalName, String mappedName, HashSet<Operator> nodeSet)
	{
		if(!element.getNameMap().containsKey(originalName))
		{
			element.putNameMap(originalName, mappedName);
		}
		else
		{
			return;
		}
		
		switch(PostProcessorHelper.getNodeType(element))
		{
			case PostProcessorHelper.AGGREGATE:
				ArrayList<String> groupbyList = ((Aggregate) element).getGroupByList();
				ArrayList<String> outputList = ((Aggregate) element).getGeneratedNameList();
				if (groupbyList.contains(originalName) || outputList.contains(originalName)) {
					renameAttributeForSelfJoin(element, element.getParents(), originalName, mappedName, nodeSet);
				}
				break;
				
			case PostProcessorHelper.DUPLICATEREMOVE:
			case PostProcessorHelper.JOIN:
			case PostProcessorHelper.SEED:
			case PostProcessorHelper.SELECTION:
			case PostProcessorHelper.SCALARFUNCTION:
				renameAttributeForSelfJoin(element, element.getParents(), originalName, mappedName, nodeSet);
				break;
	
			case PostProcessorHelper.PROJECTION:
				ArrayList<String> projectedNameList = ((Projection)element).getProjectedNameList();
				if(projectedNameList.contains(originalName))
				{
					renameAttributeForSelfJoin(element, element.getParents(), originalName, mappedName, nodeSet);
				}
				break;
				
			case PostProcessorHelper.TABLESCAN: //nothing to do, since this case never happens.
				break;
				
			case PostProcessorHelper.VGWRAPPER:
				ArrayList<String> outputNameList = ((VGWrapper)element).getGeneratedNameList();
				if(outputNameList.contains(originalName))
				{
					renameAttributeForSelfJoin(element, element.getParents(), originalName, mappedName, nodeSet);
				}
				break;			 
		}
		
		/*
		 * change the state
		 */
		
		if(!nodeSet.contains(element))
			nodeSet.add(element);
	}
	
	public static void renameAttributeForSelfJoin(Operator currentNode,
												  ArrayList<Operator> parentList, 
												  String originalName, 
												  String mappedName, 
												  HashSet<Operator> nodeSet)
	{
		if(parentList != null)
		{
			for(int i = 0; i < parentList.size(); i++)
			{
				Operator parent = parentList.get(i);
				if(!nodeSet.contains(parent))
				{
					renameSelfJoinAttribute(parent, originalName, mappedName, nodeSet);
				}
			}
		}
	}


	private static void replaceAttributeForSelfJoin(Operator element, String originalName, String mappedName)
	{
		switch(PostProcessorHelper.getNodeType(element))
		{
			case PostProcessorHelper.AGGREGATE:
				replaceAttributeInAggregateForSelfJoin((Aggregate)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.DUPLICATEREMOVE:
				replaceAttributeInDuplicateRemoveForSelfJoin((DuplicateRemove)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.JOIN:
				replaceAttributeInJoinForSelfJoin((Join)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.PROJECTION:
				replaceAttributeInProjectionForSelfJoin((Projection)element, originalName, mappedName);
				break;
					
			case PostProcessorHelper.SCALARFUNCTION:
				replaceAttributeInScalarFunctionForSelfJoin((ScalarFunction)element, originalName, mappedName);
				break; 
				
			case PostProcessorHelper.SEED:
				replaceAttributeInSeedForSelfJoin((Seed)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.SELECTION:
				replaceAttributeInSelectionForSelfJoin((Selection)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.TABLESCAN:
				replaceAttributeInTableScanForSelfJoin((TableScan)element, originalName, mappedName);
				break;
				
			case PostProcessorHelper.VGWRAPPER:
				replaceAttributeInVGWrapperForSelfJoin((VGWrapper)element, originalName, mappedName);
				break;
			 
		}
	}
	
	public static void replaceAttributeInAggregateForSelfJoin(Aggregate element, String originalName, String mappedName)
	{
		ArrayList<String> groupbyList = element.getGroupByList();
		ArrayList<MathOperator> aggregateExpressionList = element.getAggregateExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		/*
		 * 1. Change the groupBy attribute list.
		 */
		replaceListForSelfJoin(groupbyList, originalName, mappedName);
		
		if(aggregateExpressionList != null)
		{
			for(int i = 0; i < aggregateExpressionList.size(); i++)
			{
				/*
				 * 2. Change the attribute name in the columnListMap
				 */
				
				MathOperator mathOperator = aggregateExpressionList.get(i);
				ArrayList<String> list = columnListMap.get(mathOperator);
				replaceListForSelfJoin(list, originalName, mappedName);
				
				/*
				 * 3. Change the attribute name in output
				 */
				String output = outputMap.get(mathOperator);
				if(output.equals(originalName))
				{
					outputMap.put(mathOperator, mappedName);
				}
			}
			
			/*
			 * 4. change the attribute in the aggregateExpressionList
			 */
			replaceMathOperatorListForSelfJoin(element, aggregateExpressionList, originalName, mappedName);
		}
		
	}
	
	public static void replaceAttributeInDuplicateRemoveForSelfJoin(DuplicateRemove element, String originalName, String mappedName)
	{
		return;
	}
	
	public static void replaceAttributeInJoinForSelfJoin(Join element, String originalName, String mappedName)
	{
		BooleanOperator booleanOperator = element.getBooleanOperator();
		if(booleanOperator != null)
		{
			BooleanOperatorReplacerForSelfJoin booleanOperatorReplacer = new BooleanOperatorReplacerForSelfJoin(originalName, mappedName);
			booleanOperatorReplacer.replaceAttributeInBooleanOperator(booleanOperator);
		}
	}
	
	public static void replaceAttributeInProjectionForSelfJoin(Projection element, String originalName, String mappedName)
	{
		ArrayList<String> projectedNameList = element.getProjectedNameList();
		
		if(projectedNameList != null)
		{
			replaceListForSelfJoin(projectedNameList, originalName, mappedName);
		}
	}
	
	public static void replaceAttributeInScalarFunctionForSelfJoin(ScalarFunction element, String originalName, String mappedName)
	{
		ArrayList<MathOperator> scalarExpressionList = element.getScalarExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		
		if(scalarExpressionList != null)
		{
			for(int i = 0; i < scalarExpressionList.size(); i++)
			{
				/*
				 * 1. Change the attribute name in the columnListMap
				 */
				
				MathOperator mathOperator = scalarExpressionList.get(i);
				ArrayList<String> list = columnListMap.get(mathOperator);
				replaceListForSelfJoin(list, originalName, mappedName);
				
				/*
				 * 2. Change the attribute name in output
				 */
				String output = outputMap.get(mathOperator);
				if(output.equals(originalName))
				{
					outputMap.put(mathOperator, mappedName);
				}
			}
			
			/*
			 * 3. change the attribute in the scalarExpressionList
			 */
			replaceMathOperatorListForSelfJoin(element, scalarExpressionList, originalName, mappedName);
		}
	}
	
	public static void replaceAttributeInSeedForSelfJoin(Seed element, String originalName, String mappedName)
	{
		String seedAttributeName = element.getSeedAttributeName();
		
		if(originalName.equals(seedAttributeName))
		{
			element.setSeedAttributeName(mappedName);
		}
	}
	
	public static void replaceAttributeInSelectionForSelfJoin(Selection element, String originalName, String mappedName)
	{
		BooleanOperator booleanOperator = element.getBooleanOperator();
		if(booleanOperator != null)
		{
			BooleanOperatorReplacerForSelfJoin booleanOperatorReplacer = new BooleanOperatorReplacerForSelfJoin(originalName, mappedName);
			booleanOperatorReplacer.replaceAttributeInBooleanOperator(booleanOperator);
		}
	}
	
	public static void replaceAttributeInTableScanForSelfJoin(TableScan element, String originalName, String mappedName)
	{
		ArrayList<String> attributeList = element.getAttributeList();
		if(attributeList != null)
		{
			replaceListForSelfJoin(attributeList, originalName, mappedName);
		}
	}
	
	public static void replaceAttributeInVGWrapperForSelfJoin(VGWrapper element, String originalName, String mappedName)
	{
		String inputSeedAttributeName = element.getInputSeedAttributeName();
		ArrayList<String> inputAttributeNameList = element.getInputAttributeNameList();
		ArrayList<String> outputAttributeNameList = element.getOutputAttributeNameList();
		String outputSeedAttributeName = element.getOutputSeedAttributeName();
		/*
		 * 1. inputSeedAttributeName
		 */
		if(originalName.equals(inputSeedAttributeName))
		{
			element.setInputSeedAttributeName(mappedName);
		}
		
		/*
		 * 2. inputAttributeNameList
		 */
		if(inputAttributeNameList != null)
		{
			replaceListForSelfJoin(inputAttributeNameList, originalName, mappedName);
		}
		
		/*
		 * 3. outputAttributeNameList
		 */
		if(outputAttributeNameList != null)
		{
			replaceListForSelfJoin(outputAttributeNameList, originalName, mappedName);
		}
		
		/*
		 * 4. outputSeedAttributeName
		 */
		if(originalName.equals(outputSeedAttributeName))
		{
			element.setOutputSeedAttributeName(mappedName);
		}
	}
	
	private static void replaceListForSelfJoin(ArrayList<String> list, String originalName, String mappedName)
	{
		if(list != null)
		{
			if(list.contains(originalName))
			{
				int i = list.indexOf(originalName);
				list.remove(i);
				list.add(i, mappedName);
			}
		}
	}
	
	private static void replaceMathOperatorListForSelfJoin(Operator operator,
			                                    ArrayList<MathOperator> list,
			                                    String originalName, 
			                                    String mappedName)
	{
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				MathOperatorReplacerForSelfJoin mathReplacer = new MathOperatorReplacerForSelfJoin(originalName, mappedName);
				mathReplacer.replaceAttributeInMathOperator(list.get(i));
			}
		}
	}
}

/*
 * ================================================ end of self-join ==============================================
 */
