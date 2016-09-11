

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

import simsql.compiler.timetable.TimeTableNode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Stack;
import java.util.concurrent.LinkedBlockingDeque;

import static simsql.compiler.MultidimensionalTableSchema.getTablePrefixFromGeneralName;


public class PlanHelper {
	public static ArrayList<TableScan> findReferencedRandomTable(ArrayList<Operator> nodeList)
	{
		ArrayList<TableScan> resultList = new ArrayList<TableScan>();
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and save them in 
		 * the result list.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();
		
		for(int i = 0; i < nodeList.size(); i++)
		{
			availableQueue.add(nodeList.get(i));
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
			if(currentElement instanceof TableScan)
			{
				TableScan tableScan = (TableScan)currentElement;
				
				if(tableScan.getType() != TableReference.COMMON_TABLE)
				{
					String tableName = tableScan.getTableName();
					
					if(!resultList.contains(tableName))
					{
						resultList.add(tableScan);
					}
				}
			}
			
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
	
	public static ArrayList<TableScan> findReferencedRandomTable(Operator sink)
	{
		ArrayList<Operator> nodeList = new ArrayList<Operator>();
		nodeList.add(sink);
		return findReferencedRandomTable(nodeList);
	}
	
	public static ArrayList<Integer> findReferencedRandomTableTimeTicks(Operator sink, ChainGeneration chain)
	{
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and save them in 
		 * the result list.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();
		
		availableQueue.add(sink);
		
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
			if(currentElement instanceof TableScan)
			{
				TableScan tableScan = (TableScan)currentElement;
				
				if(tableScan.getType() != TableReference.COMMON_TABLE)
				{
				    String bracketsTableName = MultidimensionalTableSchema.getBracketsTableNameFromTableName(tableScan.getTableName());
					int timeTick = chain.getTickForTable(bracketsTableName);
					if(!resultList.contains(timeTick))
					{
						resultList.add(timeTick);
					}
				}
			}
			
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
	
	public static int findMaxTimeTick(ArrayList<TableScan> indexedTableList)
	{
		int timeTick = -1;
		for(int i = 0; i < indexedTableList.size(); i++)
		{
			if(timeTick < indexedTableList.get(i).getIndexStrings().get("i"))
			{
				timeTick = indexedTableList.get(i).getIndexStrings().get("i");
			}
		}
		
		return timeTick;
	}

	public static LinkedList<TimeTableNode> getRequiredTables(ArrayList<TableScan> indexedTableList) {
        LinkedList<TimeTableNode> requiredTableList = new LinkedList<TimeTableNode>();

        for(TableScan ts : indexedTableList) {

        	String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(ts.getTableName());
            String tableName = MultidimensionalTableSchema.getBracketsTableNameFromIndices(prefix, ts.getIndexStrings());
            requiredTableList.add(new TimeTableNode(tableName, ts.getIndexStrings()));
        }

        return requiredTableList;
    }
	
	/*
	 * Example: select * from A[50]; Here indexedTableList [A_50], then we need to find
	 * all the random tables in the chain. However, we just need to find the template here. 
	 */
	public static ArrayList<String> findModelTableList(ArrayList<TableScan> indexedTableList)
	{
		Catalog catalog = SimsqlCompiler.catalog;
		HashSet<String> resultSet = new HashSet<String>();
		
		TableScan tempTableScan;
		String tableName;
		View view;
		String viewName;
		for(int i = 0; i < indexedTableList.size(); i++)
		{
			tempTableScan = indexedTableList.get(i);
			tableName = tempTableScan.getTableName();
			try
			{
				view = catalog.getView(tableName);
				//view.view_name can be general template or the same with tableName.
				viewName = view.getName();
				resultSet.add(viewName);
				
				if(viewName.endsWith("_i"))
				{
					String tablePrefix = getTablePrefixUnderscore(viewName);
					ArrayList<String> tempList = catalog.getIndexTableList(tablePrefix);
					if(tempList != null)
					{
						for(String s: tempList)
						{
							resultSet.add(s);
						}
					}
				}
				else if (viewName.matches("^[^_]+(_[0-9]+to[0-9]+|_[0-9]+to|_[0-9]+)+$")) {
				    String tablePrefix = getTablePrefixFromGeneralName(viewName);

                    ArrayList<String> tempList = catalog.getIndexTableList(tablePrefix);

                    if(tempList != null)
                    {
                        for(String s: tempList)
                        {
                            resultSet.add(s);
                        }
                    }
                }
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		/*
		 * 2. Use the BFS to search all the template tables.
		 */
		LinkedBlockingDeque<String> availableQueue = new LinkedBlockingDeque<String>();
		availableQueue.addAll(resultSet);
		ArrayList<String> dependedList;
		String tempTable;
		
		while(!availableQueue.isEmpty())
		{
			String currentTable = availableQueue.poll();
			try
			{
				dependedList = catalog.getMCDependedTables(currentTable);
				for(int i = 0; i < dependedList.size(); i++)
				{
					tempTable = dependedList.get(i);
					if(!resultSet.contains(tempTable))
					{
						resultSet.add(tempTable);
						availableQueue.add(tempTable);
					}
					
					try
					{
						view = catalog.getView(tempTable);
						//view.view_name can be general template or the same with tableName.
						viewName = view.getName();
						
						if(!resultSet.contains(viewName))
						{
							resultSet.add(viewName);
							availableQueue.add(viewName);
						}
						
						if(viewName.endsWith("_i"))
						{
							String tablePrefix = getTablePrefixUnderscore(viewName);
							ArrayList<String> tempList = catalog.getIndexTableList(tablePrefix);
							if(tempList != null)
							{
								for(String s: tempList)
								{
									if(!resultSet.contains(s))
									{
										resultSet.add(s);
										availableQueue.add(s);
									}
								}
							}
						}
					}
					catch(Exception e)
					{
						e.printStackTrace();
					}
				}
			}
			catch(Exception e)
			{
				e.printStackTrace();
			}
		}
		
		
		ArrayList<String> resultList = new ArrayList<String>();
		resultList.addAll(resultSet);
		return resultList;
	}
	
	public static String BFS(ArrayList<Operator> elementList)throws Exception
	{
		//create the root
		String result = "";
		
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and output all 
		 * of such sentences.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();
		
		for(int i = 0; i < elementList.size(); i++)
		{
			Operator element = elementList.get(i);
			availableQueue.add(element);
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
			result += "\r\n";
			result += currentElement.visitNode();
			
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
		return result;
	}
	
	public static String DFS(ArrayList<Operator> elementList,
			ArrayList<String> sqlList)throws Exception
	{
		//create the root
		String result = "";
		
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and output all 
		 * of such sentences.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		Stack<Operator> availableQueue = new Stack<Operator>();
		
		for(int i = elementList.size()-1; i >= 0; i--)
		{
			result += "\r\n%------------------------------------- plan " + i + "-----------------------------------";
			result += sqlList.get(i).replaceAll("\n", "\n%\t");
			result += "\r\n%";
			Operator element = elementList.get(i);
			availableQueue.add(element);
			
			while(!availableQueue.isEmpty())
			{
				Operator currentElement = availableQueue.pop();
				
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
				result += "\r\n";
				result += currentElement.visitNode();
				
				ArrayList<Operator> children = currentElement.getChildren();
				
				if(children != null)
				{
					for(int j = 0; j < children.size(); j++)
					{
						Operator temp = children.get(j);
						
						if(!finishedQueue.contains(temp))
						{
							availableQueue.push(temp);
						}
					}
				}
			}
		}
		
		
		return result;
	}
	
	public static String DFS(ArrayList<Operator> elementList)throws Exception
	{
		//create the root
		String result = "";
		
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and output all 
		 * of such sentences.
		 */
		HashSet<Operator> finishedQueue = new HashSet<Operator>();
		Stack<Operator> availableQueue = new Stack<Operator>();
		
		for(int i = elementList.size()-1; i >= 0; i--)
		{
			result += "\r\n%------------------------------------- plan " + i + "-----------------------------------";
			Operator element = elementList.get(i);
			availableQueue.add(element);
			
			while(!availableQueue.isEmpty())
			{
				Operator currentElement = availableQueue.pop();
				
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
				result += "\r\n";
				result += currentElement.visitNode();
				
				ArrayList<Operator> children = currentElement.getChildren();
				
				if(children != null)
				{
					for(int j = 0; j < children.size(); j++)
					{
						Operator temp = children.get(j);
						
						if(!finishedQueue.contains(temp))
						{
							availableQueue.push(temp);
						}
					}
				}
			}
		}
		
		
		return result;
	}
	
	private static String getTablePrefixUnderscore(String table)
	{
		int start = table.lastIndexOf("_");
		
		if(start < 0)
			return table;
		else
			return table.substring(0, start);
	}
	
}
