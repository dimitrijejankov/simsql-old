

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

import org.apache.hadoop.yarn.webapp.hamlet.Hamlet;
import simsql.compiler.math_operators.EFunction;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.*;

import java.util.*;


// import mcdb.compiler.logicPlan.logicOperator.mathOperator.EFunction;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.*;
// import mcdb.compiler.logicPlan.translator.TranslatorHelper;

/**
 * @author Bamboo
 *
 */
public class PostProcessor
{
	
	private ArrayList<Operator> operatorList;
	private TranslatorHelper translatorHelper;
	
	public PostProcessor(ArrayList<Operator> operatorList, TranslatorHelper translatorHelper)
	{
		this.operatorList = operatorList;
		this.translatorHelper = translatorHelper;
	}

	public ArrayList<Operator> getOperator() {
		return operatorList;
	}

	public void setOperatorList(ArrayList<Operator> operatorList) {
		this.operatorList = operatorList;
	}
	
	public void renaming() throws Exception
	{
		/*
		 * 1. Find all the nodes.
		 */
		ArrayList<Operator> nodeList = PostProcessorHelper.findAllNode(operatorList);
		nodeList = PostProcessorHelper.topologicalSort(nodeList);
		ArrayList<RenameNode> addedNodeList = new ArrayList<RenameNode>();
		
		/*
		 * 2. Rename all the nodes
		 */
		for (Operator element : nodeList) {
			renameNode(element, addedNodeList);
		}
		
		/*
		 * 3. Rename the attributes according to the result of step2
		 */
		for (Operator element : nodeList) {
			LogicOperatorReplacer.replaceAttribute(element);
		}
		
		/*
		 * 4. hack here for self-join, and find the the place that potential has self-join
		 */
		ArrayList<RenameNode> selfJoinRenameList = findSelfJoinRenanmeList(addedNodeList);
		
		/*
		 * 5. Merge the result of step 3, and add nodes accordingly.
		 */
		merge(addedNodeList);
		
		/*
		 * 6. Deal with self join
		 */
		if(addedNodeList.size() > 0)
		{
			nodeList = PostProcessorHelper.findAllNode(operatorList);
			
			/*
			 * 5.1 Clear the renaming information.
			 */
			for (Operator aNodeList : nodeList) {
				aNodeList.clearRenamingInfo();
			}
			
			/*
			 * 5.2 For self-join, we add the node according to the selfJoinRenameList.
			 */
			selfJoinmerge(selfJoinRenameList);
			
			/*
			 * 5.3 Rename all the ancester nodes according to the renaming attribute.
			 * The difference between this function and the renameNode() is that renameNode() tries to keep the 
			 * lineage, where A->B->C along the path will use only A as the attribute. B and C will be transferred to A.
			 * Where here A->B->C the path, only change the path that A exists if we want to change A to D.
			 * renameNode: A->B->C (where A <- D) ===> D->D->D
			 * here: A->B-> (where A <- D) ===> D->B->C
			 */
			LogicOperatorReplacer.renameSelfJoinAttributeListAlongTheDAG(selfJoinRenameList);
		}
		
		/*
		 * 6. Remove the redundant nodes.
		 */
		removeRedundant(nodeList);
	}

public ArrayList<RenameNode> findSelfJoinRenanmeList(ArrayList<RenameNode> addedNodeList) throws Exception
	{
		HashMap<Operator, ArrayList<Operator>> addOperatorMap = new HashMap<Operator, ArrayList<Operator>>();
		HashSet<String> conflictedAttributeNameSet = new HashSet<String>();
		ArrayList<RenameNode> selfJoinRenameList = new ArrayList<RenameNode>();
		
		if(addedNodeList.size() > 0)
		{
			/*
			 * 4.1 First, find the potential edges, that we need to add another renaming.
			 */
			// potentialSelfJoinOperatorMap 
			HashMap<Operator, ArrayList<Operator>> potentialSelfJoinOperatorMap = new HashMap<Operator, ArrayList<Operator>>();
			for(RenameNode renameNode: addedNodeList)
			{
				Operator tempParent = renameNode.parent;
				Operator tempChild = renameNode.child;
				if(potentialSelfJoinOperatorMap.containsKey(tempParent))
				{
					ArrayList<Operator> tempList = potentialSelfJoinOperatorMap.get(tempParent);
					if(!tempList.contains(tempChild))
					{
						tempList.add(tempChild);
					}
				}
				else
				{
					ArrayList<Operator> tempList = new ArrayList<Operator>();
					tempList.add(tempChild);
					potentialSelfJoinOperatorMap.put(tempParent, tempList);
				}
				
				conflictedAttributeNameSet.addAll(renameNode.attributeMap.keySet());
			}
			
			for(Operator tempParent: potentialSelfJoinOperatorMap.keySet())
			{
				ArrayList<Operator> tempList = potentialSelfJoinOperatorMap.get(tempParent);
				ArrayList<Operator> childrenList = tempParent.getChildren();
				ArrayList<Operator> remainList = this.subtractList(childrenList, tempList);
				if(remainList != null && remainList.size() > 0)
				{
					addOperatorMap.put(tempParent, remainList);
				}
			}
			
			/*
			 * 4.2 Now the "addOperatorMap" contains every edge that we may need to add "renaming" operator.
		     * the "conflictedAttributeNameSet" has all the potential target name.
			 */
			for(Operator tempParent: addOperatorMap.keySet())
			{
				ArrayList<Operator> tempList = addOperatorMap.get(tempParent);
				for(Operator tempChild: tempList)
				{
					for(String attribute: conflictedAttributeNameSet)
					{
						if(tempChild.getMapSpaceNameSet().contains(attribute))
						{
							addRenameNode(selfJoinRenameList, tempChild, tempParent, attribute, AttributeNameSpace.getString(attribute));
						}
					}
				}
			}
		}
		
		return selfJoinRenameList;
	}
	
	/*
	 * Return the list = list1 - list2 (set operation).
	 */
	public ArrayList<Operator> subtractList(ArrayList<Operator> list1, ArrayList<Operator> list2)
	{
		ArrayList<Operator> resultList = new ArrayList<Operator>();
		for(Operator operator: list1)
		{
			if(!list2.contains(operator))
			{
				resultList.add(operator);
			}
		}
		
		return resultList;
	}
	
	public void removeRedundant(ArrayList<Operator> operatorList)
	{
		/*
		 * 1. remove redundant scalar function
		 */
		for(int i = 0; i < operatorList.size(); i++)
		{
			Operator element = operatorList.get(i);
			if(PostProcessorHelper.getNodeType(element) == PostProcessorHelper.SCALARFUNCTION)
			{
				removeRedundantScalarFunction((ScalarFunction) element);
				
			}
		}
		
		/*
		 * 2. remove redundant projection
		 */
		for(int i = 0; i < operatorList.size(); i++)
		{
			Operator element = operatorList.get(i);
			if(PostProcessorHelper.getNodeType(element) == PostProcessorHelper.PROJECTION)
			{
				removeRedundantProjection((Projection) element);
			}
		}	
	}
	
	public void removeRedundantProjection(Projection projection)
	{
		ArrayList<Operator> parents = projection.getParents();
		ArrayList<Operator> children = projection.getChildren();
		
		boolean removeForParents, removeForChildren;
		removeForParents = true;
		removeForChildren = true;
		
		if(parents == null || parents.size() == 0)
		{
			removeForParents = false;
		}
		
		if(children == null || children.size() == 0)
		{
			removeForChildren = false;
		}
		
		if(parents != null)
		{
			for (Operator parent : parents) {
				if (!(parent instanceof Projection) ||
						!same((Projection) parent, projection)) {
					removeForParents = false;
				}
			}
		}
		
		if(children != null)
		{
			for (Operator child : children) {
				if (!(child instanceof Projection) ||
						!same((Projection) child, projection)) {
					removeForChildren = false;
				}
			}
		}
		
		if(removeForParents || removeForChildren)
		{
			//remove current projection
			/*
			 * children remove parent "element"
			 */
			for (Operator child : children) {
				child.removeParent(projection);
			}
			
			/*
			 * parents remove child "element"
			 */
			for (Operator parent : parents) {
				int index = parent.getChildren().indexOf(projection);
				parent.removeChild(projection);

				for (int j = 0; j < children.size(); j++) //In fact projection has only one child here.
				{
					Operator child = children.get(j);

					//parent adds child as "child" and child adds parent as "parent"
					parent.addChild(index, child);
					child.addParent(parent);
				}

				// change the outer_relation
				if (parent instanceof VGWrapper &&
						projection.equals(((VGWrapper) parent).getOuterRelationOperator())) {
					((VGWrapper) parent).setOuterRelationOperator(projection.getChildren().get(0));
				}

				// change the leftTable
				else if (parent instanceof Join) {
					if (((Join) parent).getLeftTable().equals(projection.getNodeName())) {
						((Join) parent).setLeftTable(projection.getChildren().get(0).getNodeName());
					}
				}
			}
			
			projection.clearLinks();
		}
		
	}
	
	public void removeRedundantScalarFunction(ScalarFunction element)
	{
		ArrayList<MathOperator> expressionList = element.getScalarExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = element.getColumnListMap();
		HashMap<MathOperator, String> outputMap = element.getOutputMap();
		
		boolean remove = true;
		
		if(expressionList != null)
		{
            for (MathOperator temp : expressionList) {
                if (temp instanceof EFunction) //empty Operator
                {
                    ArrayList<String> associatedAttributeList = columnListMap.get(temp);
                    String output = outputMap.get(temp);

                    if (associatedAttributeList.size() == 1) {
                        String associateAttribute = associatedAttributeList.get(0);
                        if (!associateAttribute.equals(output)) {
                            remove = false;
                            break;
                        }
                    } else {
                        remove = false;
                        break;
                    }
                } else {
                    remove = false;
                    break;
                }
            }
		}
		
		if(remove) // we should remove the current
		{
			ArrayList<Operator> parents = element.getParents();
			ArrayList<Operator> children = element.getChildren();
			
			/*
			 * parents remove child "element"
			 */
            for (Operator parent : parents) {
                parent.removeChild(element);

                // change the outer_relation
                if (parent instanceof VGWrapper &&
                        element.equals(((VGWrapper) parent).getOuterRelationOperator())) {
                    ((VGWrapper) parent).setOuterRelationOperator(element.getChildren().get(0));
                }
                // change the leftTable
                else if (parent instanceof Join) {
                    if (((Join) parent).getLeftTable().equals(element.getNodeName())) {
                        ((Join) parent).setLeftTable(element.getChildren().get(0).getNodeName());
                    }
                }
            }
			
			/*
			 * children remove parent "element"
			 */
            for (Operator child : children) {
                child.removeParent(element);
            }

            for (Operator parent : parents) {
                for (Operator child : children) {
                    //parent adds child as "child" and child adds parent as "parent"
                    parent.addChild(child);
                    child.addParent(parent);
                }
            }
			
			element.clearLinks();
		}
		else //see if we need to remove some attributes
		{
            for(int i = 0; i < expressionList.size(); i++)
            {
                MathOperator temp = expressionList.get(i);
                if(temp instanceof EFunction) //empty Operator
                {
                    ArrayList<String> associatedAttributeList = columnListMap.get(temp);
                    String output = outputMap.get(temp);

                    if(associatedAttributeList.size() == 1)
                    {
                        String associateAttribute = associatedAttributeList.get(0);
                        if(associateAttribute.equals(output))
                        {
                            expressionList.remove(temp);
                            columnListMap.remove(temp);
                            outputMap.remove(temp);

                            //if we remove one element, then the i should decrease, or else
                            //we ignore the next one.
                            i--;
                        }
                    }
                }
            }
		}
	}
	
	public void merge(ArrayList<RenameNode> addedNodeList)
	{
        for (RenameNode anAddedNodeList : addedNodeList) {
            renameFunction(anAddedNodeList);
        }
	}
	
	public void renameFunction(RenameNode renameNode)
	{
		if(renameNode.getMapsize() == 0)
			return;
		/*
		 * Scalar function followed by projection
		 */
		Operator child = renameNode.child;
		Operator parent = renameNode.parent;
	    HashMap<String, String> stringMap = renameNode.attributeMap;
	    
		/*
		 * 1. Scalar function on the new defined attribute due to the
		 * definition of the schema.
		 */

		/*
		 * The data structure in the ScalarFunction node.
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
		HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
		HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
		
		String []keys = new String[stringMap.size()];
		stringMap.keySet().toArray(keys);
		/*
		 * 1.1. Fill the translatedStatement in the ScalarFunction.
		 */
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.2. Fill in the scalarFunction with the concrete
			 * MathFunction Here it should be EFunction.
			 */
			scalarExpressionList.add(new EFunction());
		}

		/*
		 * It comes to the attribute set of each function. Since one scalar
		 * function can have multiple functions, with each function can have
		 * multiple involved attributes. However, since the scalar function
		 * only plays the role of renaming, each scalar function has only
		 * one attribute.
		 */

		ArrayList<String> tempList;
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.3. Fill each functions in the ScalarFunction with involved
			 * attributes.
			 */
			tempList = new ArrayList<String>();
			tempList.add(keys[i]);
			columnListMap.put(scalarExpressionList.get(i), tempList);
		}
		
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.4. Fill each functions in the ScalarFunction with an output
			 */
			outputMap.put(scalarExpressionList.get(i), stringMap.get(keys[i]));
		}

		/*
		 * 1.5. Fill in the children
		 */
		children.add(child);

		/*
		 * 1.6 Create the current scalar function node.
		 */
		ScalarFunction scalarFunction = new ScalarFunction(nodeName,
				children, parents, translatorHelper);
		scalarFunction.setScalarExpressionList(scalarExpressionList);
		scalarFunction.setColumnListMap(columnListMap);
		scalarFunction.setOutputMap(outputMap);

		/*
		 * 1.7 This translatedElement add current Node as parent
		 */
		child.addParent(scalarFunction);
		child.removeParent(parent);

		/*
		 * 2. Projection on the result attribute
		 */
		/*
		 * 2.1 Create the data structure of the Projection
		 */
		Projection projection;
		nodeName = "node" + translatorHelper.getNodeIndex();
		children = new ArrayList<Operator>();
		parents = new ArrayList<Operator>();
		ArrayList<String> projectedNameList = new ArrayList<String>();

		/*
		 * 2.2 Fill the tranlsatedResult.
		 */
		if(child instanceof Projection)
		{
			ArrayList<String> childrenProjectedList = ((Projection)child).getProjectedNameList();
			for (int i = 0; i < childrenProjectedList.size(); i++) {
				String attributeName = childrenProjectedList.get(i); //the mapped name in the children.
				/*
				 * 2.3 Fill the projectedNameList
				 */
				if(stringMap.containsKey(attributeName))
				{
					if(!projectedNameList.contains(stringMap.get(attributeName)))
					{
						projectedNameList.add(stringMap.get(attributeName));
					}
				}
				else
				{
					if(!projectedNameList.contains(attributeName))
					{
						projectedNameList.add(attributeName);
					}
				}
			}
		}
		else
		{
			for (int i = 0; i < keys.length; i++) {
				/*
				 * 2.3 Fill the projectedNameList
				 */
				if(!projectedNameList.contains(stringMap.get(keys[i])))
				{
					projectedNameList.add(stringMap.get(keys[i]));
				}
			}
			
			HashMap<String, String> childStringMap = child.getNameMap();
		    /*
			 * 1.2.1 Consider the attribute in its child, which do not need renaming.
			 */
			String childkeys[] = new String[childStringMap.size()];
			childStringMap.keySet().toArray(childkeys);
			
			for (int i = 0; i < childkeys.length; i++)
			{
				/*
				 * 1.2. Fill in the scalarFunction with the concrete
				 * MathFunction Here it should be EFunction.
				 */
				String mappedName = childStringMap.get(childkeys[i]);
				
				if(!stringMap.containsKey(mappedName)) //it could not be a projection
				{
					if(!projectedNameList.contains(mappedName))
					{
						projectedNameList.add(mappedName);
					}
				}
			}
		}

		/*
		 * 2.4 Fill the children
		 */
		children.add(scalarFunction);

		/*
		 * 2.5 Create the current projection node.
		 */
		projection = new Projection(nodeName, children, parents, projectedNameList);
		/*
		 * 2.6 "sclarFunction" fills it parents with the projection.
		 */
		scalarFunction.addParent(projection);
		
		/*
		 * 3. Parent adds "projection" as the child, and remove the child "child".
		 */
		parent.replaceChild(child, projection);
		
		/*
		 * 4. projection add "parent" as the parent.
		 */
		projection.addParent(parent);
		
		/*
		 * 5. Take care the leftTable in join and the outerOperator in VGWrapper.
		 */
		if(parent instanceof VGWrapper &&
				child.equals(((VGWrapper) parent).getOuterRelationOperator()))
		{
			((VGWrapper) parent).setOuterRelationOperator(projection);
		}
		// change the leftTable
		else if(parent instanceof Join && 
				child.getNodeName().equals(((Join) parent).getLeftTable()))
		{
			((Join) parent).setLeftTable(projection.getNodeName());
		}
	}
	
public void selfJoinmerge(ArrayList<RenameNode> addedNodeList)
	{
		for(int i = 0; i < addedNodeList.size(); i++)
		{
			renameFunctionSelfJoin(addedNodeList.get(i));
		}
	}
	
	public void renameFunctionSelfJoin(RenameNode renameNode)
	{
		if(renameNode.getMapsize() == 0)
			return;
		/*
		 * Scalar function followed by projection
		 */
		Operator child = renameNode.child;
		Operator parent = renameNode.parent;
	    HashMap<String, String> stringMap = renameNode.attributeMap;
	    
		/*
		 * 1. Scalar function on the new defined attribute due to the
		 * definition of the schema.
		 */

		/*
		 * The data structure in the ScalarFunction node.
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
		HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
		HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
		
		String []keys = new String[stringMap.size()];
		stringMap.keySet().toArray(keys);
		/*
		 * 1.1. Fill the translatedStatement in the ScalarFunction.
		 */
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.2. Fill in the scalarFunction with the concrete
			 * MathFunction Here it should be EFunction.
			 */
			scalarExpressionList.add(new EFunction());
		}

		/*
		 * It comes to the attribute set of each function. Since one scalar
		 * function can have multiple functions, with each function can have
		 * multiple involved attributes. However, since the scalar function
		 * only plays the role of renaming, each scalar function has only
		 * one attribute.
		 */

		ArrayList<String> tempList;
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.3. Fill each functions in the ScalarFunction with involved
			 * attributes.
			 */
			tempList = new ArrayList<String>();
			tempList.add(keys[i]);
			columnListMap.put(scalarExpressionList.get(i), tempList);
		}
		
		for (int i = 0; i < keys.length; i++) {
			/*
			 * 1.4. Fill each functions in the ScalarFunction with an output
			 */
			outputMap.put(scalarExpressionList.get(i), stringMap.get(keys[i]));
		}

		/*
		 * 1.5. Fill in the children
		 */
		children.add(child);

		/*
		 * 1.6 Create the current scalar function node.
		 */
		ScalarFunction scalarFunction = new ScalarFunction(nodeName,
				children, parents, translatorHelper);
		scalarFunction.setScalarExpressionList(scalarExpressionList);
		scalarFunction.setColumnListMap(columnListMap);
		scalarFunction.setOutputMap(outputMap);

		/*
		 * 1.7 This translatedElement add current Node as parent
		 */
		child.addParent(scalarFunction);
		child.removeParent(parent);

		/*
		 * 2. Projection on the result attribute
		 */
		/*
		 * 2.2 Fill the tranlsatedResult.
		 */
		if(child instanceof Projection)
		{
			/*
			 * 2.1 Create the data structure of the Projection
			 */
			Projection projection;
			nodeName = "node" + translatorHelper.getNodeIndex();
			children = new ArrayList<Operator>();
			parents = new ArrayList<Operator>();
			ArrayList<String> projectedNameList = new ArrayList<String>();

			ArrayList<String> childrenProjectedList = ((Projection)child).getProjectedNameList();
			for (int i = 0; i < childrenProjectedList.size(); i++) {
				String attributeName = childrenProjectedList.get(i); //the mapped name in the children.
				/*
				 * 2.3 Fill the projectedNameList
				 */
				if(stringMap.containsKey(attributeName))
				{
					if(!projectedNameList.contains(stringMap.get(attributeName)))
					{
						projectedNameList.add(stringMap.get(attributeName));
					}
				}
				else
				{
					if(!projectedNameList.contains(attributeName))
					{
						projectedNameList.add(attributeName);
					}
				}
			}
			
			/*
			 * 2.4 Fill the children
			 */
			children.add(scalarFunction);

			/*
			 * 2.5 Create the current projection node.
			 */
			projection = new Projection(nodeName, children, parents, projectedNameList);
			/*
			 * 2.6 "sclarFunction" fills it parents with the projection.
			 */
			scalarFunction.addParent(projection);
			
			/*
			 * 3. Parent adds "projection" as the child, and remove the child "child".
			 */
			parent.replaceChild(child, projection);
			
			/*
			 * 4. projection add "parent" as the parent.
			 */
			projection.addParent(parent);
			
			/*
			 * 5. Take care the leftTable in join and the outerOperator in VGWrapper.
			 */
			if(parent instanceof VGWrapper &&
					child.equals(((VGWrapper) parent).getOuterRelationOperator()))
			{
				((VGWrapper) parent).setOuterRelationOperator(projection);
			}
			// change the leftTable
			else if(parent instanceof Join && 
					child.getNodeName().equals(((Join) parent).getLeftTable()))
			{
				((Join) parent).setLeftTable(projection.getNodeName());
			}
		}
		else
		{
			scalarFunction.addParent(parent);
			parent.replaceChild(child, scalarFunction);
			
			/*
			 * 5. Take care the leftTable in join and the outerOperator in VGWrapper.
			 */
			if(parent instanceof VGWrapper &&
					child.equals(((VGWrapper) parent).getOuterRelationOperator()))
			{
				throw new RuntimeException("The final operator of the left most children of VGWrapper should be projection!");
			}
			// change the leftTable
			else if(parent instanceof Join && 
					child.getNodeName().equals(((Join) parent).getLeftTable()))
			{
				((Join) parent).setLeftTable(scalarFunction.getNodeName());
			}
		}
	}
	private void renameNode(Operator element, 
			                ArrayList<RenameNode> addedNodeList) throws Exception
	{
		if(isAttributeGenerationNode(element))
		{
			ArrayList<String> attributeList;
			
			switch(PostProcessorHelper.getNodeType(element))
			{
				case PostProcessorHelper.AGGREGATE:
					attributeList = ((Aggregate)element).getGeneratedNameList();
					break;
					
				case PostProcessorHelper.SCALARFUNCTION:
					attributeList = ((ScalarFunction)element).getGeneratedNameList();
					break;
					
				case PostProcessorHelper.SEED:
					attributeList = ((Seed)element).getGeneratedNameList();
					break;
					
				case PostProcessorHelper.TABLESCAN:
					attributeList = ((TableScan)element).getGeneratedNameList();
					break;
					
				case PostProcessorHelper.VGWRAPPER:
					attributeList = ((VGWrapper)element).getGeneratedNameList();
					break;
					
				default:
					attributeList = new ArrayList<String>();
					break;
			}
			
			for(int j = 0; j < attributeList.size(); j++)
			{
				String originalName = attributeList.get(j);
				renameAttribute(element, originalName, addedNodeList);
			}
		}
	}
	
	public void renameAttribute(Operator element, 
								String originalName, 
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		
		HashMap<String, String> nameMap = element.getNameMap();
		
		if(nameMap.containsKey(originalName))
		{
			/*
			 * The original name has been mapped.
			 */
			return;
		}
		else
		{
			String mappedName = AttributeNameSpace.getString(originalName);
			HashSet<Operator> nodeSet = new HashSet<Operator>();
			HashMap<Operator, ArrayList<Operator>> edgeSet = new HashMap<Operator, ArrayList<Operator>>();
			
			renameAttribute(element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
		}
	}
	
	public void renameAttribute(Operator element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * The access state should be an edge (S, T)
		 * which meets following conditions, then we should add new renaming node.
		 * 	1). The parent node is a join/vgwrapper
		 * 	2). The edge has never been visited.
		 * 	3). T has been visited.
		 *  4). original name in child node appears in the nameMap of the parent node.
		 *  5). Pay attention that here edge (S, T), S can appears two times in the children list of T, and then 
		 *  	we consider that different items in the children are different, even they correspond to the same S. 
		 *  	It seems that S could not appear more than two times in its children list.
		 */
		if(!element.getNameMap().containsKey(originalName))
		{
			element.putNameMap(originalName, mappedName);
		}
		else
		{
			return;
		}
		
		/*
		if(element.getNodeName().equals("node18") &&
				originalName.equals("supplier.s_nationkey"))
		{
			System.out.println("testing");
		}
		*/
		
		switch(PostProcessorHelper.getNodeType(element))
		{
			case PostProcessorHelper.AGGREGATE:
				renameAggregate((Aggregate)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.DUPLICATEREMOVE:
				renameDuplicateRemove((DuplicateRemove)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.JOIN:
				renameJoin((Join)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.PROJECTION:
				renameProjection((Projection)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
					
			case PostProcessorHelper.SCALARFUNCTION:
				renameScalarFunction((ScalarFunction)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break; 
				
			case PostProcessorHelper.SEED:
				renameSeed((Seed)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.SELECTION:
				renameSelection((Selection)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.TABLESCAN:
				renameTableScan((TableScan)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
				
			case PostProcessorHelper.VGWRAPPER:
				renameVGWrapper((VGWrapper)element, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				break;
			 
		}
		
		/*
		 * change the state
		 */
		
		if(!nodeSet.contains(element))
			nodeSet.add(element);
	

	}
	
	public void renameVGWrapper(VGWrapper element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * consider its parents
		 */
		ArrayList<String> outputNameList = element.getGeneratedNameList();
		if(outputNameList.contains(originalName))
		{
			ArrayList<Operator> parents = element.getParents();
			renameAttribute(element, 
							parents, 
							originalName, 
							mappedName, 
							nodeSet, 
							edgeSet,
							addedNodeList);
		}
		/*
		
		*/
		//nothing to do..Since vgWrapper generally do not transfer the input attributes.
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameTableScan(TableScan element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * consider its parents
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet,
						addedNodeList);
		
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameSelection(Selection element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * consider its parents
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet, 
						addedNodeList);
		
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameSeed(Seed element, 
							String originalName, 
							String mappedName, 
							HashSet<Operator> nodeSet,
							HashMap<Operator, ArrayList<Operator>> edgeSet,
							ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * consider its parents
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet,
						addedNodeList);
		
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameProjection(Projection element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * consider its parents
		 */
		ArrayList<String> projectedNameList = element.getProjectedNameList();
		if(projectedNameList.contains(originalName))
		{
			ArrayList<Operator> parents = element.getParents();
			renameAttribute(element, 
							parents, 
							originalName, 
							mappedName, 
							nodeSet, 
							edgeSet, 
							addedNodeList);
		}
		
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameJoin(Join element, 
							String originalName, 
							String mappedName, 
							HashSet<Operator> nodeSet,
							HashMap<Operator, ArrayList<Operator>> edgeSet,
							ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * 2. Consider its parents
		 * The stop condition:
		 * A. This node is root.
		 * B. The attribute does not appear in the parents.
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet, 
						addedNodeList);
		
		/*
		 * 3. deal with the state
		 */
		
	}
	
	public void renameDuplicateRemove(DuplicateRemove element, 
										String originalName, 
										String mappedName, 
										HashSet<Operator> nodeSet,
										HashMap<Operator, ArrayList<Operator>> edgeSet,
										ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * 2. Consider its parents
		 * The stop condition:
		 * A. This node is root.
		 * B. The attribute does not appear in the parents.
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet, 
						addedNodeList);
		
		/*
		 * 3. deal with the state
		 */
	}
	
	public void renameScalarFunction(ScalarFunction element, 
									String originalName, 
									String mappedName, 
									HashSet<Operator> nodeSet,
									HashMap<Operator, ArrayList<Operator>> edgeSet,
									ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * The scalarfunction does create some new attributes, but for such EFunction(x->y), we do need to map
		 * both x, y to a name in the target name space.
		 * However, if there are multiple EFunction(x->y1, x->y2..x->y_k), we need to map them to different name
		 * in the target name space. (x->_n_1, x->_n_2..x->_n_k).
		 */
		ArrayList<Operator> parents = element.getParents();
		renameAttribute(element, 
						parents, 
						originalName, 
						mappedName, 
						nodeSet, 
						edgeSet, 
						addedNodeList);
		/*
		 * 2. Find the output attributes that are renamed by the original name.
		 * 		tempList: the list for the output attributes that are renamed by the original name.
		 */
		ArrayList<String>  tempList = getEqualMappedStringInScalar(element, originalName);
		/*
		 * 3. Map the name
		 */
		if(tempList.size() != 0)
		{
			/*
			 * 3.1 For the first generated name which is renamed by current original name.
			 * 3.1.1 Map the current name
			 */
			String fistName = tempList.get(0);
			if(!element.getNameMap().containsKey(fistName))
			{
				element.putNameMap(fistName, mappedName);
			
			
				/*
				 * 3.1.2 Deal with its parents
				 */
				renameAttribute(element, 
								parents, 
								fistName, 
								mappedName, 
								nodeSet, 
								edgeSet, 
								addedNodeList);
			}
			/*
			 * 3.2 For the following name, we need some new mapped name in the name space.
			 * 
			 */
			for(int i = 1; i < tempList.size(); i++)
			{
				/*
				 * 3.2.1 Map the current following name.
				 */
				String followingName = tempList.get(i);
				String mappedFollowingName = AttributeNameSpace.getString(followingName);
				if(!element.getNameMap().containsKey(followingName))
				{
					element.putNameMap(followingName, mappedFollowingName);
					
					/*
					 * 3.2.2 Map its parents.
					 */
					HashSet<Operator> newNodeSet = new HashSet<Operator>();
					HashMap<Operator, ArrayList<Operator>> newEdgeSet = new HashMap<Operator, ArrayList<Operator>>();
					
					renameAttribute(element, 
							parents, 
							followingName, 
							mappedFollowingName, 
							newNodeSet, 
							newEdgeSet, 
							addedNodeList);
				}
			}
		}
		
		/*
		 * 3. deal with the state
		 */
		
	}
	
	public void renameAggregate(Aggregate element, 
								String originalName, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		/*
		 * 1. The aggregate does create the new attribute, but we do not need to remove the aggregate operator, so
		 * we do not need to care the attributes which are generated by the current originalName.
		 */
		/*
		 * 2. Consider its parents
		 * The stop condition:
		 * A. This node is root.
		 * B. The attribute does not appear in the parents.
		 */
		ArrayList<String> groupbyList = ((Aggregate)element).getGroupByList();
		ArrayList<String> outputList = element.getGeneratedNameList();
		if(groupbyList.contains(originalName) ||
				outputList.contains(originalName))
		{
			ArrayList<Operator> parents = element.getParents();
			
			renameAttribute(element, 
							parents, 
							originalName, 
							mappedName, 
							nodeSet, 
							edgeSet, 
							addedNodeList);
		}
		
		/*
		 * 3. deal with the state
		 */
	}
	
	/*
	 * This function aims to compare two projections, if they output the same string list,
	 * then we consider them the same
	 */
	public boolean same(Projection projection1, Projection projection2)
	{
		ArrayList<String> list1 = projection1.getProjectedNameList();
		ArrayList<String> list2 = projection2.getProjectedNameList();
		
		if(list1 == null && list2 == null)
		{
			return true;
		}
		else if(list1 == null || list2 == null)
		{
			return false;
		}
		else if(list1.size() != list2.size())
		{
			return false;
		}
		else
		{
			String s1[] = new String[list1.size()];
			String s2[] = new String[list1.size()];
			
			list1.toArray(s1);
			list2.toArray(s2);
			
			for(int i = 0; i < s1.length; i++)
			{
				if(!s1[i].equals(s2[i]))
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	public ArrayList<String> getEqualMappedStringInScalar(ScalarFunction element, String originalName)
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		ArrayList<MathOperator> expressionList = element.getScalarExpressionList();
		if(expressionList != null)
		{
			for(int i = 0; i < expressionList.size(); i++)
			{
				MathOperator temp = expressionList.get(i);
				if(temp instanceof EFunction) //empty Operator
				{
					ArrayList<String> associatedAttributeList = element.getColumnListMap().get(temp);
					String output = element.getOutputMap().get(temp);
					
					if(associatedAttributeList.size() == 1)
					{
						String associateAttribute = associatedAttributeList.get(0);
						if(associateAttribute.equals(originalName) && !resultList.contains(output) &&
								!output.equals(originalName))
						{
							resultList.add(output);
						}
					}
				}
			}
		}
		
		return resultList;
	}

	public void renameAttribute(ArrayList<Operator> parentList, 
								ArrayList<String> equalMappedAttributeList, 
								String mappedName, 
								HashSet<Operator> nodeSet,
								HashMap<Operator, ArrayList<Operator>> edgeSet,
								ArrayList<RenameNode> addedNodeList) throws Exception
	{
		if(parentList != null)
		{
			for(int i = 0; i < equalMappedAttributeList.size(); i++)
			{
				for(int j = 0; j < parentList.size(); j++)
				{
					renameAttribute(parentList.get(j), 
									equalMappedAttributeList.get(i), 
									mappedName, 
									nodeSet, 
									edgeSet, 
									addedNodeList);
				}
			}
		}
	}
	
	public void renameAttribute(Operator currentNode,
			ArrayList<Operator> parentList, 
			String originalName, 
			String mappedName, 
			HashSet<Operator> nodeSet,
			HashMap<Operator, ArrayList<Operator>> edgeSet,
			ArrayList<RenameNode> addedNodeList) throws Exception
	{
		if(parentList != null)
		{
			for(int i = 0; i < parentList.size(); i++)
			{
				/*
				 * The access state should be an edge (S, T)
				 * which meets following conditions, then we should add new renaming node.
				 * 	1). The parent node is a join/vgwrapper
				 * 	2). The edge has never been visited.
				 * 	3). T has been visited.
				 *  4). original name in child node appears in the nameMap of the parent node.
				 *  5). Pay attention that here edge (S, T), S can appears two times in the children list of T, and then 
				 *  	we consider that different items in the children are different, even they correspond to the same S. 
				 *  	It seems that S could not appear more than two times in its children list.
				 */
				Operator parent = parentList.get(i);
				
				
				if(!nodeSet.contains(parent))
				{
					renameAttribute(parent, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				}
				else if(contains(edgeSet, currentNode, parent))
				{
					renameAttribute(parent, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
				}
				else
				{
					HashSet<String> mappedNameSet = parent.getMapSpaceNameSet();
					if(!mappedNameSet.contains(mappedName))
					{
						renameAttribute(parent, originalName, mappedName, nodeSet, edgeSet, addedNodeList);
					}
					else
					{
						/*
						 * Meets the condition
						 */
						//add the RenameNode, and then for the parents on, do another renaming.
						String mappedName2 = AttributeNameSpace.getString(originalName);
						addRenameNode(addedNodeList, currentNode, parent, mappedName, mappedName2);
						
						HashSet<Operator> newNodeSet = new HashSet<Operator>();
						HashMap<Operator, ArrayList<Operator>> newEdgeSet = new HashMap<Operator, ArrayList<Operator>>();
						
						renameAttribute(parent, 
										originalName, 
										mappedName2, 
										newNodeSet, 
										newEdgeSet, 
										addedNodeList);
						
						//Something may be needed to do..
					}
				}
				
				/*
				 * change the state
				 */
				put(edgeSet, currentNode, parent);
			}
		}
	}
	
	public boolean contains(HashMap<Operator, ArrayList<Operator>> edgeSet,
							Operator child,
							Operator parent)
	{
		ArrayList<Operator> list = edgeSet.get(child);
		
		if(list != null && list.contains(parent))
			return true;
		else
			return false;
	}
	
	public void put(HashMap<Operator, ArrayList<Operator>> edgeSet,
			Operator child,
			Operator parent)
	{
		if(!edgeSet.containsKey(child))
		{
			ArrayList<Operator> list = new ArrayList<Operator>();
			edgeSet.put(child, list);
		}
		
		ArrayList<Operator> list = edgeSet.get(child);
		list.add(parent);
	}
	
	
	
	
	
	private boolean isAttributeGenerationNode(Operator o)
	{
		if(o instanceof TableScan ||
				o instanceof Aggregate ||
				o instanceof ScalarFunction ||
				o instanceof VGWrapper ||
				o instanceof Seed)
		{
			return true;
		}
		else
			return false;
	}
	
	public void addRenameNode(ArrayList<RenameNode> addedNodeList, 
							  Operator currentNode, 
							  Operator parent, 
							  String originalName, 
							  String mappedName) throws Exception
	{
        for (RenameNode tempNode : addedNodeList) {
            if (tempNode.child.equals(currentNode) &&
                    tempNode.parent.equals(parent)) {
                tempNode.addStringMap(originalName, mappedName);
                return;
            }
        }
		//We could find the associated node in the list, and we add one.
		RenameNode node = new RenameNode(currentNode, parent);
		node.addStringMap(originalName, mappedName);
		addedNodeList.add(node);
	}

    /**
     * Merges all the operators that are forwarded to the frame output as a
     * @param frameOutput
     * @param sourceOperators
     */
    public void mergeDuplicates(FrameOutput frameOutput, LinkedList<Operator> sourceOperators) {

        Hashtable<Operator, String> uniques = new Hashtable<>();

        int idx = 0;

        // scan through all the children of the frameOutput
	    for(Operator c : new ArrayList<>(frameOutput.getChildren())){

	        if(!uniques.keySet().contains(c)) {
	            // add it to the unique operators
	            uniques.put(c, frameOutput.getTableList().get(idx));
            }
            else {

	            // remove the duplicate from the frameOutput
                frameOutput.getChildren().remove(idx);
                String uniqueTable = uniques.get(c);
                String removedTable = frameOutput.getTableList().remove(idx);

                // update the sources so that the scan from the right table name
                for(Operator o : sourceOperators) {
                    TableScan s  = (TableScan) o;

                    if(s.getTableName().equals(removedTable)) {
                        s.setTableName(uniqueTable);
                    }
                }

                // remove one link to the frameOutput
                c.removeParent(frameOutput);

                idx--;
            }

            idx++;
        }
    }
}
