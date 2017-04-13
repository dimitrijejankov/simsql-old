

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
package simsql.compiler;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;

import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;


/**
 * @author Bamboo
 *
 */
public class Topologic 
{
	/* The operatorList saves the root nodes of three kinds of sentences.
	 * 1. select statement.
	 * 2. 
	 */
	private ArrayList<Operator> operatorList;
	private HashMap<Operator, String> randomTableMap;

	private HashMap<String, HashSet<String>> forwardEdge;
	private HashMap<String, HashSet<String>> backwardEdge;

	private HashMap<String, Double> tableCosts;
	
	public Topologic(ArrayList<Operator> operatorList, HashMap<Operator, String> randomTableMap)
	{
		this.operatorList = operatorList;
		this.randomTableMap = randomTableMap;

		this.backwardEdge = new HashMap<String, HashSet<String>>();	

		this.tableCosts = new HashMap<String, Double>();

		generateBackwardEdges();
//		generateForwardEdges();
	}

	/**
	 * @return the operatorList
	 */
	public ArrayList<Operator> getOperatorList() {
		return operatorList;
	}

	public ArrayList<String> getRandomTables()
	{
		HashSet<String> resultSet = new HashSet<String>();
		ArrayList<String> resultList = new ArrayList<String>();
		
		/*
		 * 1. random referenced by definition.
		 */
		ArrayList<Operator> allOperators = findAllNode(operatorList);
		ArrayList<Operator> topLogicalList = topologicalSort(allOperators);
		for(int i = 0; i < topLogicalList.size(); i++)
		{
			Operator operator = topLogicalList.get(i);
			
			if(operator instanceof TableScan)
			{
				TableScan tableScan = (TableScan)operator;
				
				if(tableScan.getType() != TableReference.COMMON_TABLE)
				{
					String tableName = tableScan.getTableName();
					
					if(tableName.endsWith("_i"))
					{
						MathExpression indexExpression = tableScan.getIndexMathExp();
						MPNGenerator generator = new MPNGenerator(indexExpression);
						tableName = tableName.substring(0, tableName.length()-2);
						tableName += "[" + generator.convertToMPN() + "]";
					}
					else
					{
						int end = tableName.lastIndexOf("_");
						String prefix = tableName.substring(0, end);
						String suffix = tableName.substring(end+1, tableName.length());
						
						tableName = prefix + "[" +suffix + "]"; 
					}
					
					if(!resultSet.contains(tableName))
					{
						resultSet.add(tableName);
						resultList.add(tableName);
					}
				}
			}
		}
		
		/*
		 * 2. random table defined in the schema.
		 */
		for(Object o: randomTableMap.keySet())
		{
			String tableName = randomTableMap.get(o);
			
			int end = tableName.lastIndexOf("_");
			String prefix = tableName.substring(0, end);
			String suffix = tableName.substring(end+1, tableName.length());
			
			tableName = prefix + "[" +suffix + "]"; 
			
			if(!resultSet.contains(tableName))
			{
				resultSet.add(tableName);
				resultList.add(tableName);
			}
		}
		return resultList;
	}

	public void generateForwardEdges()
	{
		for(Object o: backwardEdge.keySet())
		{
			String table = (String)o;

			HashSet<String> referencedTableSet = backwardEdge.get(o);
			for(String referencedTable: referencedTableSet)
			{
				HashSet<String> forwardSet;
				if(forwardEdge.containsKey(referencedTable))
				{
					forwardSet = forwardEdge.get(referencedTable);
				}
				else
				{
					forwardSet = new HashSet<String>();
					forwardEdge.put(referencedTable, forwardSet);
				}

				forwardSet.add(table);
			}
		}

	}
	
	public void generateBackwardEdges()
	{
		for(Operator operator : randomTableMap.keySet())
		{
			String tableName = randomTableMap.get(operator);
			tableName = getTableName(tableName);

			HashSet<String> referencedTables = getReferencedRandomTables(operator, tableName);
            referencedTables = findRangeTables(referencedTables);
			backwardEdge.put(tableName, referencedTables);
		}
	}


	private HashSet<String> findRangeTables(HashSet<String> tables){

		HashSet<String> toAdd = new HashSet<String>();
		HashSet<String> toRemove = new HashSet<String>();

		for(String table : tables) {

			if(table.matches("^[^\\[\\]]+\\[i-[0-9]+:i]")) {

				int offset = table.indexOf("[");
//				String expression = table.substring(offset);
//				expression = table.substring(offset + 1, expression.length() - 1);
				String expression = table.substring(offset + 1, table.length() - 1);

				int value = Integer.parseInt(expression.split(":")[0].substring(2));
                String prefix = table.substring(0, offset);

                for(int i = value; i > 0; --i) {
                    toAdd.add(prefix + "[i-" + i + "]");
                }

                toAdd.add(prefix + "[i]");
                toRemove.add(table);
			}
		}

		tables.removeAll(toRemove);
        tables.addAll(toAdd);

		return tables;
	}
	
	public static ArrayList<Operator> topologicalSort(ArrayList<Operator> nodeList)
	{
		ArrayList<Operator> resultList = new ArrayList<Operator>();
		HashMap<Operator, Integer> childrenNumMap = new HashMap<Operator, Integer>();
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			Operator temp = nodeList.get(i);
			ArrayList<Operator> children = temp.getChildren();
			if(children == null)
			{
				childrenNumMap.put(temp, 0);
			}
			else
			{
				childrenNumMap.put(temp, children.size());
			}
		}
		
		boolean accessed[] = new boolean[nodeList.size()];
		for(int i = 0; i < nodeList.size(); i++)
		{
			accessed[i] = false;
		}
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			//find the node with no parents
			int j = 0;
			for(; j < nodeList.size(); j++)
			{
				Operator temp = nodeList.get(j);
				
				if(childrenNumMap.get(temp) == 0 && !accessed[j])
				{
					break;
				}
			}
			
			//get j
			if(j == nodeList.size())
			{
				System.err.println("Circles in the graph");
			}
			else
			{
				Operator temp = nodeList.get(j);
				resultList.add(temp);
				
				accessed[j] = true;
				
				ArrayList<Operator> parents = temp.getParents();
				if(parents != null)
				{
					for(int k = 0; k < parents.size(); k++)
					{
						Operator temp2 = parents.get(k);
						if(childrenNumMap.containsKey(temp2))
						{
							int num = childrenNumMap.get(temp2);
							childrenNumMap.put(temp2, num-1);
						}
					}
				}
			}
		}
		
		return resultList;
	}
	
	public static ArrayList<Operator> findAllNode(ArrayList<Operator> o)
	{
		ArrayList<Operator> resultList = new ArrayList<Operator>();
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and save them in 
		 * the result list.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();
		
		for(int i = 0; i < o.size(); i++)
		{
			availableQueue.add(o.get(i));
		}
		
		while(!availableQueue.isEmpty())
		{
			Operator currentElement = availableQueue.poll();
			
			if(finishedQueue.contains(currentElement))
			{
				continue;
			}
			else
			{
				finishedQueue.add(currentElement);
			}
			/*
			 * deal with the current elment
			 */
			resultList.add(currentElement);
			
			ArrayList<Operator> children = currentElement.getChildren();
			
			if(children != null)
			{
				for(int i = 0; i < children.size(); i++)
				{
					Operator temp = children.get(i);
					
					if(!finishedQueue.contains(temp))
					{
						availableQueue.add(temp);
					}
				}
			}
		}
		return resultList;
	}
	
	private HashSet<String> getReferencedRandomTables(Operator sink, String sinkName)
	{
		HashSet<String> resultSet = new HashSet<String>();
		
		/*
		 * 1. random referenced by definition.
		 */
		ArrayList<Operator> sinkList = new ArrayList<Operator>();
		sinkList.add(sink);
		
		ArrayList<Operator> allOperators = findAllNode(sinkList);
		UnionView firstUnionViewOperator = findUnionVIew(allOperators);
		
		//we need to tackle the union view.
		if(firstUnionViewOperator == null)
		{
			ArrayList<Operator> topLogicalList = topologicalSort(allOperators);

			double sumCost = 0.0;
			for(int i = 0; i < topLogicalList.size(); i++)
			{
				Operator operator = topLogicalList.get(i);

				if(operator instanceof TableScan)
				{
					TableScan tableScan = (TableScan)operator;
					
					if(tableScan.getType() != TableReference.COMMON_TABLE)
					{
						String tableName = tableScan.getTableName();
						
						if(tableScan.getIndexMathExpressions().size() != 0)
						{
                            HashMap<String, MathExpression> indexExpressions = tableScan.getIndexMathExpressions();
							tableName = MultidimensionalTableSchema.getTablePrefixFromGeneralName(tableScan.getTableName());

                            for(int idx = 0; idx < indexExpressions.size(); ++idx) {
                                MPNGenerator generator = new MPNGenerator(indexExpressions.get(labelingOrder[idx]));
                                tableName += "[" + generator.convertToMPN() + "]";
                            }
						}
						else
						{
							int end = tableName.lastIndexOf("_");
							String prefix = tableName.substring(0, end);
							String suffix = tableName.substring(end+1, tableName.length());
							
							tableName = prefix + "[" +suffix + "]"; 
						}
						
						if(!resultSet.contains(tableName))
						{
							resultSet.add(tableName);
						}
					}
				} else if (operator instanceof Aggregate || operator instanceof DuplicateRemove || operator instanceof Seed) {
					sumCost += 1.0;
				} else if (operator instanceof Selection) {
					sumCost += 0.0;
				} else if (operator instanceof VGWrapper) {
//					ArrayList<Operator> projections = operator.getChildren();
//					for (Operator c : projections) {
//						if (c.getChildren().get(0) instanceof Join &&
//								((Join) c.getChildren().get(0)).getBooleanOperator() != null) {
//							sumCost += 0.5;
//							break;
//						}
//					}
					sumCost += 0.5;
				} else if (operator instanceof Join) {
//					ArrayList<Operator> children = o.getChildren();
//					// Estimate children statistics through TableScan?
					sumCost += 0.5;
				}
			}
			tableCosts.put(sinkName, sumCost);
		}
		else
		{
			String elementName = firstUnionViewOperator.getElementName();
			ArrayList<String> constantTableList = firstUnionViewOperator.getConstantTableList();
			ArrayList<MathExpression> individualGeneralIndexExpressionList = firstUnionViewOperator.getIndividualGeneralIndexExpressionList();
			ArrayList<MathExpression>  generalTableBoundExpressionList = firstUnionViewOperator.getGeneralTableBoundExpressionList();
			
			resultSet.addAll(constantTableList);
			
			if(individualGeneralIndexExpressionList != null)
			{
				for(int i = 0; i < individualGeneralIndexExpressionList.size(); i++)
				{
					MPNGenerator generator = new MPNGenerator(individualGeneralIndexExpressionList.get(i));
					resultSet.add(elementName + "[" + generator.convertToMPN() + "]");
				}
			}
			
			if(generalTableBoundExpressionList != null)
			{
				for(int i = 0; i < generalTableBoundExpressionList.size(); i+=2)
				{
					MPNGenerator lowerGenerator = new MPNGenerator(generalTableBoundExpressionList.get(i));
					MPNGenerator upGenerator = new MPNGenerator(generalTableBoundExpressionList.get(i+1));
					
					resultSet.add(elementName + "[" + lowerGenerator.convertToMPN()  + ":" + upGenerator.convertToMPN()
							+ "]");
				}
			}
			tableCosts.put(sinkName, 0.0);
		}
		
		return resultSet;
	}
	
	private String getTableName(String name)
	{
		int end = name.indexOf("_");
		String prefix = name.substring(0, end);
        String suffix;

        if(name.matches("^[^_]+(_[0-9]+to[0-9]+|_[0-9]+to|_[0-9]+)+$")) {
            MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(name);
            suffix = indices.getBracketsSuffix();
        }
        else {
            suffix =  "[" + name.substring(end+1, name.length()) + "]";
        }
		name = prefix + suffix;
		return name;
	}

	public HashMap<String, HashSet<String>> getBackwardEdges() {
		return backwardEdge;
	}

	public HashMap<String, HashSet<String>> getForwardEdges() {
		return forwardEdge;
	}

	public HashMap<String, Double> getTableCosts() {
		return tableCosts;
	}
	
	public static UnionView findUnionVIew(ArrayList<Operator> topLogicalList)
	{
		Operator operator;
		
		for(int i = 0; i < topLogicalList.size(); i++)
		{
			operator = topLogicalList.get(i);
			if(operator instanceof UnionView)
			{
				return (UnionView)operator;
			}
		}
		
		return null;
	}
}
