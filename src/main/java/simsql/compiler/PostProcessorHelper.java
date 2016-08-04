

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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;







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
public class PostProcessorHelper {
	public static final int AGGREGATE  = 0;
	public static final int DUPLICATEREMOVE = 1;
	public static final int JOIN = 2;
	public static final int PROJECTION = 3;
	public static final int SCALARFUNCTION  = 4;
	public static final int SEED  = 5;
	public static final int SELECTION = 6;
	public static final int TABLESCAN  = 7;
	public static final int VGWRAPPER  = 8;
	public static final int ERROR  = 9;
	
	public static int getNodeType(Operator o)
	{
		if(o instanceof Aggregate)
			return AGGREGATE;
		else if(o instanceof DuplicateRemove)
			return DUPLICATEREMOVE;
		else if(o instanceof Join)
			return JOIN;
		else if(o instanceof Projection)
			return PROJECTION;
		else if(o instanceof ScalarFunction)
			return SCALARFUNCTION;
		else if(o instanceof Seed)
			return SEED;
		else if(o instanceof Selection)
			return SELECTION;
		else if(o instanceof TableScan)
			return TABLESCAN;
		else if(o instanceof VGWrapper)
			return VGWRAPPER;
		else
			return ERROR;
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
						if(temp2 == null || childrenNumMap.get(temp2) == null)
						{
							System.out.println("adsfdsa");
						}
						int num = childrenNumMap.get(temp2);
						childrenNumMap.put(temp2, num-1);
					}
				}
			}
		}
		
		return resultList;
	}
}
