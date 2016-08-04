

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
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;



/**
 * @author Bamboo
 *
 */
public class ChainGeneration
{
	private Topologic topologic;
	/*
	 * The ruleMap records for each random table, which random tables are needed.
	 */
	private HashMap<String, HashSet<String>> ruleMap;
	/*
	 * The simulateTableMap saves for each time tick, the list of "TABLE" at this time, with the corresponding 
	 * tables generated by "TABLE".
	 */
	private HashMap<Integer, TableByTime> simulateTableMap;
	private ArrayList<String> startPointList;
	private HashMap<String, Integer> maxVersionTableMap;
	
	private int startTimeTick;
	private int maxLoop;
	
	public ChainGeneration(Topologic topologic,
			               int maxLoop)
	{
		this.topologic = topologic;
		ruleMap = new HashMap<String, HashSet<String>>();
		simulateTableMap = new HashMap<Integer, TableByTime>();
		startPointList = new ArrayList<String>();
		maxVersionTableMap = new HashMap<String, Integer>();
		instantiateChain(0, maxLoop);
		this.maxLoop = maxLoop;
	}
	
	public  boolean checkCircle()
	{
		int minIndex = this.minimumVersion(topologic.getForwardEdges());
		int maxIndex = this.maxBaseLineVersion(topologic.getForwardEdges());
		
		HashMap<Integer, TableByTime> chain = getChainByTime(minIndex, maxIndex);
		ArrayList<String> startPoint = this.getStartPoint();
		ArrayList<String> sortList = topologicalSort(chain, startPoint);
		return sortList.size() != 0;
	}
	
	public ArrayList<String> getTopologicalList(int start, int end)
	{
		HashMap<Integer, TableByTime> chain = getChainByTime(-1, end);
		ArrayList<String> startPoint = this.getStartPoint();
		ArrayList<String> sortList = topologicalSort(chain, startPoint);
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < sortList.size(); i++)
		{
			String table = sortList.get(i);
			if(this.getVersionFromConstantTable(table) >= start && 
					this.getVersionFromConstantTable(table) < end)
			{
				resultList.add(table);
			}
		}
		
		return resultList;
	}
	
	/*
	 * Such random tables which are generated without referring to other random tables
	 */
	public ArrayList<String> getStartPoint()
	{
		return startPointList;
	}
	
	public HashMap<Integer, TableByTime> getChainByTime(int start, int end)
	{
		HashMap<Integer, TableByTime> resultChain = new HashMap<Integer, TableByTime>();
		for(int i = start; i <= end; i++)
		{
			if(simulateTableMap.containsKey(i))
			{
				TableByTime tempTable = simulateTableMap.get(i);
				resultChain.put(i, tempTable.copy());
			}
		}
		
		return resultChain;
	}
	
	public TableByTime getTableByTime(int time)
	{
		if(maxLoop < time)
		{
			return null;
		}
		else
		{
			return simulateTableMap.get(time);
		}
	}
	
	private void instantiateChain(int start, int end)
	{
		HashMap<String, HashSet<String>> forwardEdge = topologic.getForwardEdges();
		HashMap<String, HashSet<String>> backwardEdge = topologic.getBackwardEdges();
		
		/*
		 * 1. chain
		 */
		putBaseline(ruleMap, backwardEdge, simulateTableMap);
		
		int minimumVersion = minimumVersion(backwardEdge);
		startTimeTick = minimumVersion;
		HashSet<String> randomTableList = randomTableList(forwardEdge);
		
		if(start < minimumVersion)
		{
			start = minimumVersion;
		}
		
		for(int index = start; index <= end; index++)
		{
			generateTimeTable(randomTableList, 
							  forwardEdge, 
							  backwardEdge, 
							  simulateTableMap,
							  ruleMap,
							  index);
		}
		
		/*
		 * 2. starting point
		 */
		for(String s: backwardEdge.keySet())
		{
			if(!isGeneralTable(s))
			{
				if(backwardEdge.get(s).size() == 0)
				{
					startPointList.add(s);
				}
			}
		}
		
		/*
		 * 3. trune the simulateTableMap and the ruleMap.
		 */
		boolean remove;
		while(true)
		{
			remove = false;
			
			TableByTime tempTableByTime;
			HashSet<String> tempTableSet;
			HashMap<String, HashSet<String>> tempTimeMap;
			HashMap<String, HashSet<String>> backEdgeMap = topologic.getBackwardEdges();
			HashSet<String> removedTableSet = new HashSet<String>();
			//clear the rulemap key.
			for(int index = start; index <= end; index++)
			{
				tempTableByTime = simulateTableMap.get(index);
				if(tempTableByTime != null)
				{
					tempTableSet = tempTableByTime.getTableSet();
					tempTimeMap = tempTableByTime.getTimeMap();
					
					if(tempTableSet != null)
					{
						Iterator<String> iterator = tempTableSet.iterator();
						while(iterator.hasNext())
						{
							String temptableName = iterator.next();
							if(!tempTimeMap.containsKey(temptableName) &&
									!backEdgeMap.containsKey(temptableName) && !hasVersionedTable(temptableName))
							{
								ruleMap.remove(temptableName);
								iterator.remove();
								removedTableSet.add(temptableName);
								remove = true;
							}
						}
						
						//if we find all the tables in the time tick i are deleted, then we delete the TimeTable.
						if(tempTableSet.size() == 0)
						{
							simulateTableMap.remove(index);
						}
					}
				}
			}
			
			//remove the rule map values.
			for(int index = start; index <= end; index++)
			{
				tempTableByTime = simulateTableMap.get(index);
				
				if(tempTableByTime != null)
				{
					tempTimeMap = tempTableByTime.getTimeMap();
					
					if(tempTimeMap != null)
					{
						for(String key: tempTimeMap.keySet())
						{
							tempTableSet = tempTimeMap.get(key);
							Iterator<String> iterator = tempTableSet.iterator();
							String temptableName;
							while(iterator.hasNext())
							{
								temptableName = iterator.next();
								if(removedTableSet.contains(temptableName))
								{
									iterator.remove();
									remove = true;
								}
							}
						}
						
						 Iterator<Map.Entry<String, HashSet<String>>> it = tempTimeMap.entrySet().iterator(); 
						 while(it.hasNext())
						 {
							 
						      Map.Entry<String, HashSet<String>> entry = it.next();
						      if(entry.getValue() == null ||entry.getValue().size() == 0)
						      {
						    	  it.remove();
						      }
						 }
					}
				}
			}
			
			if(!remove)
				break;
		}
	}
	
	public void generateTimeTable(HashSet<String> randomTableList, 
										 HashMap<String, HashSet<String>> forwardEdge, 
										 HashMap<String, HashSet<String>> backwardEdge, 
										 HashMap<Integer, TableByTime> simulateTableMap,
										 HashMap<String, HashSet<String>> ruleMap,
										 int index)
	{
		for(String origin: forwardEdge.keySet())
		{
			if(isGeneralTable(origin))
			{
				if(!isGeneralTableArray(origin))
				{
					int originVersion = getVersionFromGeneralTable(origin, index);
					HashSet<String> tempList = forwardEdge.get(origin);
					for(String target: tempList)
					{
						if(isGeneralTable(target))
						{
							int targetVersion = getVersionFromGeneralTable(target, index);
							
							if(originVersion < startTimeTick || targetVersion < startTimeTick)
							{
								break;
							}
							
							String originPrefix = getTablePrefix(origin);
							String targetPrefix = getTablePrefix(target);
							
							String instantiatedOrigin = originPrefix + "[" + originVersion + "]";
							String instantiatedTarget = targetPrefix + "[" + targetVersion + "]";
							putMaxVersionMap(originPrefix, originVersion);
							putMaxVersionMap(targetPrefix, targetVersion);
							
							if(!backwardEdge.containsKey(instantiatedTarget))
							{
								putContent(ruleMap, instantiatedTarget, instantiatedOrigin);
							}
							
							TableByTime sourceTable;
							
							if(simulateTableMap.containsKey(originVersion))
							{
								sourceTable = simulateTableMap.get(originVersion);
							}
							else
							{
								sourceTable = new TableByTime(originVersion);
								simulateTableMap.put(originVersion, sourceTable);
							}
							
							TableByTime targetTable;
							if(simulateTableMap.containsKey(targetVersion))
							{
								targetTable = simulateTableMap.get(targetVersion);
							}
							else
							{
								targetTable = new TableByTime(targetVersion);
								simulateTableMap.put(targetVersion, targetTable);
							}
							
							sourceTable.addTable(instantiatedOrigin);
							targetTable.addTable(instantiatedTarget);
							
							if(!backwardEdge.containsKey(instantiatedTarget))
							{
								sourceTable.addEdge(instantiatedOrigin, instantiatedTarget);
							}
						}
					}
				}
				else //general table array
				{
					int originVersionArray[] = getVersionFromGeneralTableArray(origin, index);
					HashSet<String> tempList = forwardEdge.get(origin);
					for(String target: tempList)
					{
						if(isGeneralTable(target))
						{
							int targetVersion = getVersionFromGeneralTable(target, index);
							
							for(int originVersion = originVersionArray[0]; 
									originVersion <= originVersionArray[1];
									originVersion ++)
							{
								if(originVersion < startTimeTick || targetVersion < startTimeTick)
								{
									continue;
								}
								
								String originPrefix = getTablePrefix(origin);
								String targetPrefix = getTablePrefix(target);
								
								String instantiatedOrigin = originPrefix + "[" + originVersion + "]";
								String instantiatedTarget = targetPrefix + "[" + targetVersion + "]";
								putMaxVersionMap(originPrefix, originVersion);
								putMaxVersionMap(targetPrefix, targetVersion);
								
								if(!backwardEdge.containsKey(instantiatedTarget))
								{
									putContent(ruleMap, instantiatedTarget, instantiatedOrigin);
								}
								
								TableByTime sourceTable;
								
								if(simulateTableMap.containsKey(originVersion))
								{
									sourceTable = simulateTableMap.get(originVersion);
								}
								else
								{
									sourceTable = new TableByTime(originVersion);
									simulateTableMap.put(originVersion, sourceTable);
								}
								
								sourceTable.addTable(instantiatedOrigin);
								
								TableByTime targetTable;
								if(simulateTableMap.containsKey(targetVersion))
								{
									targetTable = simulateTableMap.get(targetVersion);
								}
								else
								{
									targetTable = new TableByTime(targetVersion);
									simulateTableMap.put(targetVersion, targetTable);
								}
								targetTable.addTable(instantiatedTarget);
								
								if(!backwardEdge.containsKey(instantiatedTarget))
								{
									sourceTable.addEdge(instantiatedOrigin, instantiatedTarget);
								}
							}
						}
					}
				}
			}
		}
	}
	
	public void putMaxVersionMap(String table, int version)
	{
		int value;
		if(maxVersionTableMap.containsKey(table))
		{
			value = maxVersionTableMap.get(table);
			if(value < version)
			{
				maxVersionTableMap.put(table, version);
			}
		}
		else
		{
			maxVersionTableMap.put(table, version);
		}
	}
	
	private boolean hasVersionedTable(String table)
	{
		String prefix = this.getTablePrefix(table);
		int version = this.getVersionFromConstantTable(table);
		
		if(maxVersionTableMap.containsKey(prefix) && maxVersionTableMap.get(prefix) == version)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void putContent(HashMap<String, HashSet<String>> resultMap,
			        String source, 
			        String target)
	{
		HashSet<String> tempList;
		
		if(resultMap.containsKey(source))
		{
			tempList = resultMap.get(source);
		}
		else
		{
			resultMap.put(source, new HashSet<String>());
			tempList = resultMap.get(source);
		}
		
		if(!tempList.contains(target))
		{
			tempList.add(target);
		}
	}
	
	public void putBaseline(HashMap<String, HashSet<String>> ruleMap,
							HashMap<String, HashSet<String>> backwardEdge,
							HashMap<Integer, TableByTime> simulateTableMap)
	{
		/*
		 * 1. Add the basic rule to the ruleMap.
		 */
		for(String s: backwardEdge.keySet())
		{
			if(!isGeneralTable(s))
			{
				ruleMap.put(s, copySet(backwardEdge.get(s)));
			}
		}
		
		/*
		 * 2. Generate the table in the chain according to the baseline rules.
		 */
		for(String target: backwardEdge.keySet())
		{
			if(!isGeneralTable(target))
			{
				/*
				 * 2.1 Generate the target indexTable.
				 */
				int version = this.getVersionFromConstantTable(target);
				String prefix = getTablePrefix(target);
				putMaxVersionMap(prefix, version);
				
				TableByTime targetTableByTime;
				
				if(simulateTableMap.containsKey(version))
				{
					targetTableByTime =  simulateTableMap.get(version);
				}
				else
				{
					targetTableByTime = new TableByTime(version);
					simulateTableMap.put(version, targetTableByTime);
				}
				
				targetTableByTime.addTable(target);
				
				/*
				 * 2.2 Generate the source indexTable.
				 */
				HashSet<String> sourceSet = backwardEdge.get(target);
				for(String source: sourceSet)
				{
					if(!isGeneralTable(source))
					{
						version = this.getVersionFromConstantTable(source);
						prefix = getTablePrefix(source);
						putMaxVersionMap(prefix, version);
						
						TableByTime sourceTableByTime;
						
						if(simulateTableMap.containsKey(version))
						{
							sourceTableByTime =  simulateTableMap.get(version);
						}
						else
						{
							sourceTableByTime = new TableByTime(version);
							simulateTableMap.put(version, sourceTableByTime);
						}
						
						sourceTableByTime.addTable(source);
						/*
						 * 2.3 generate the edges in the chain
						 */
						sourceTableByTime.addEdge(source, target);
					}
				}
			}
		}
	}
	
	public HashSet<String> copySet(HashSet<String> set)
	{
		 HashSet<String> resultSet = new HashSet<String>();
		
		if(set != null)
		{
			for(String key: set)
			{
				resultSet.add(key);
			}
		}
		return resultSet;
	}
	
	public HashSet<String> randomTableList(HashMap<String, HashSet<String>> forwardEdge)
	{
		HashSet<String> resultSet = new HashSet<String>();
		for(String s: forwardEdge.keySet())
		{
			int start = s.indexOf("[");
			String index = s.substring(0, start);
			
			if(!resultSet.contains(index))
				resultSet.add(index);
			
			HashSet<String> tempList = forwardEdge.get(s);
			for(String tempTable: tempList)
			{
				start = tempTable.indexOf("[");
				index = tempTable.substring(0, start);
				
				if(!resultSet.contains(index))
					resultSet.add(index);
			}
		}
		
		return resultSet;
	}
	
	public int minimumVersion(HashMap<String, HashSet<String>> backwardEdge)
	{
		ArrayList<String> baselineList = new ArrayList<String>();
		
		for(String s: backwardEdge.keySet())
		{
			if(!isGeneralTable(s))
			{
				if(!baselineList.contains(s))
					baselineList.add(s);
			}
			
			HashSet<String> tempSet = backwardEdge.get(s);
			
			for(String temp: tempSet)
			{
				if(!isGeneralTable(temp))
				{
					if(!baselineList.contains(temp))
						baselineList.add(temp);
				}
			}
		}
		
		int minTime = Integer.MAX_VALUE;
		for(int i = 0; i < baselineList.size(); i++)
		{
			String baselineTable = baselineList.get(i);
			
			int version = getVersionFromConstantTable(baselineTable);
			if(minTime > version)
			{
				minTime = version;
			}
		}
		
		return minTime;
	}
	
	public int maxBaseLineVersion(HashMap<String, HashSet<String>> forwardEdge)
	{
		ArrayList<String> baselineList = new ArrayList<String>();
		
		for(String s: forwardEdge.keySet())
		{
			if(!isGeneralTable(s))
			{
				if(!baselineList.contains(s))
					baselineList.add(s);
			}
			
			HashSet<String> tempList = forwardEdge.get(s);
			
			for(String temp: tempList)
			{
				if(!isGeneralTable(temp))
				{
					if(!baselineList.contains(temp))
						baselineList.add(temp);
				}
			}
		}
		
		int maxTime = Integer.MIN_VALUE;
		for(int i = 0; i < baselineList.size(); i++)
		{
			String baselineTable = baselineList.get(i);
			
			int version = getVersionFromConstantTable(baselineTable);
			if(maxTime < version)
			{
				maxTime = version;
			}
		}
		
		return maxTime;
	}
	
	public ArrayList<String> getBaselineTables(HashMap<String, ArrayList<String>> forwardEdge)
	{
		ArrayList<String> baselineList = new ArrayList<String>();
		
		for(String s: forwardEdge.keySet())
		{
			if(!isGeneralTable(s))
			{
				if(!baselineList.contains(s))
					baselineList.add(s);
			}
			
			ArrayList<String> tempList = forwardEdge.get(s);
			
			for(int i = 0; i < tempList.size(); i++)
			{
				String temp = tempList.get(i);
				if(!isGeneralTable(temp))
				{
					if(!baselineList.contains(temp))
						baselineList.add(temp);
				}
			}
		}
		
		return baselineList;
	}
	
	public boolean isGeneralTable(String table)
	{
		int start = table.indexOf("[");
		int end = table.indexOf("]");
		
		String index = table.substring(start+1, end);
		if(isNumeric(index))
			return false;
		else
			return true;
	}
	
	public boolean isGeneralTableArray(String table)
	{
		int start = table.indexOf("[");
		int end = table.indexOf("]");
		
		if(start < 0 || end < 0)
			return false;
		
		String index = table.substring(start+1, end);
		
		start = index.indexOf(":");
		
		if(start < 0)
			return false;
		
		String prefix, suffix;
		
		prefix = index.substring(0, start);
		suffix = index.substring(start+1, index.length());
		
		if(isNumeric(prefix) || isNumeric(suffix))
			return false;
		else
			return true;
	}
	
	public boolean isNumeric(String s)
	{
		if(s != null)
		{
			char letter;
			for(int i = 0; i < s.length(); i++)
			{
				letter = s.charAt(i);
				
				if(letter < '0' || letter > '9')
				{
					return false;
				}
			}
			
			if(s.length() <= 0)
			{
				return false;
			}
			
			if(s.length() > 1)
			{
				letter = s.charAt(0);
				if(letter == '0')
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	/*
	 * If it is the baseline table, then it returns the version of this table;
	 */
	public int getVersionFromConstantTable(String table)
	{
		int start = table.indexOf("[");
		int end = table.indexOf("]");
		String index = table.substring(start+1, end);
		
		return Integer.parseInt(index);
	}
	
	/*
	 * If it is the general table, then it returns the version of this table with the current time tick;
	 */
	public int getVersionFromGeneralTable(String table, int index)
	{
		int start = table.indexOf("[");
		int end = table.indexOf("]");
		String indexString = table.substring(start+1, end);
		
		double result = new MPNGenerator().compute(indexString, index);
		return (int)result;
	}
	
	public int[] getVersionFromGeneralTableArray(String table, int index)
	{
		int start = table.indexOf("[");
		int end = table.indexOf("]");
		String indexString = table.substring(start+1, end);
		
		start = indexString.indexOf(":");
		
		String prefix = indexString.substring(0, start);
		String suffix = indexString.substring(start+1, indexString.length());
		
		int result[] = new int[2];
		result[0] = (int)(new MPNGenerator().compute(prefix, index));
		result[1] = (int)(new MPNGenerator().compute(suffix, index));
		
		return result;
	}
	
	public HashMap<Integer, TableByTime> getSimulateTableMap() {
		return simulateTableMap;
	}

	public void setSimulateTableMap(HashMap<Integer, TableByTime> simulateTableMap) {
		this.simulateTableMap = simulateTableMap;
	}

	public String getTablePrefix(String table)
	{
		int start = table.indexOf("[");
		
		if(start < 0)
			return table;
		else
			return table.substring(0, start);
	}
	
	/* Given the cutPrePoint and the cutSufPoint, this function aims to find all the tables between these two list */
	public ArrayList<String> findParamtersBetweenTwoBlocks(HashMap<Integer, TableByTime> chain, 
														   int start_timeTick,
														   int end_timeTick) throws InterruptedException
	{
		ArrayList<String> resultList = new ArrayList<String>();
		HashMap<String, HashSet<String>> rule = new HashMap<String, HashSet<String>>();
		
		for(Object o: chain.keySet())
		{
			TableByTime tempTable = chain.get(o);
			HashMap<String, HashSet<String>> timeMap = tempTable.getTimeMap();
			
			for(String s: timeMap.keySet())
			{
				rule.put(s, timeMap.get(s));
			}
		}
		
		return resultList;
	}
	
	private ArrayList<String> topologicalSort(HashMap<Integer, TableByTime> chain, ArrayList<String> startPoint)
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		ArrayList<String> nodeList = new ArrayList<String>();
		HashMap<String, Integer> childrenNumMap = new HashMap<String, Integer>();
		
		Integer []index = new Integer[chain.size()];
		chain.keySet().toArray(index);
		Arrays.sort(index);
		
		/*
		 * 1. start point
		 */
		for(int i = 0; i < startPoint.size(); i++)
		{
			String starttable = startPoint.get(i);
			childrenNumMap.put(starttable, 0);
		}
		
		/*
		 * 2. Get all the table and its parents number in topological order.
		 */
		for(int i = 0; i < index.length; i++)
		{
			TableByTime tableByTime = chain.get(index[i]);
			
			HashSet<String> tableList = tableByTime.getTableSet();
			HashMap<String, HashSet<String>> timeMap = tableByTime.getTimeMap();
			
			for(String tempTable: tableList)
			{
				if(!nodeList.contains(tempTable))
				{
					nodeList.add(tempTable);
				}
			}
			
			for(String s: timeMap.keySet())
			{
				HashSet<String> tempList = timeMap.get(s);
				
				for(String value: tempList)
				{
					addOne(childrenNumMap, value);
				}
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
			HashMap<String, Integer> potentialStringMap = new HashMap<String, Integer>();
			
			for(int j = 0; j < nodeList.size(); j++)
			{
				String temp = nodeList.get(j);
				
				if(childrenNumMap.containsKey(temp)&& childrenNumMap.get(temp) == 0 && !accessed[j])
				{
					potentialStringMap.put(temp, j);
				}
			}
			
			//get j
			if(potentialStringMap.isEmpty())
			{
				resultList.clear();
				System.err.println("Circles in the graph");
				break;
			}
			else
			{
				int min = Integer.MAX_VALUE;
				String minTable = null;
				
				for(String s: potentialStringMap.keySet())
				{
					if(min > this.getVersionFromConstantTable(s))
					{
						min = this.getVersionFromConstantTable(s);
						minTable = s;
					}
				}
				
				resultList.add(minTable);
				accessed[potentialStringMap.get(minTable)] = true;
				
				int time = min;
				
				TableByTime tableByTime = chain.get(time);
				if(tableByTime != null)
				{
					HashMap<String, HashSet<String>> timeMap = tableByTime.getTimeMap();
					if(timeMap.containsKey(minTable))
					{
						HashSet<String> targetList = timeMap.get(minTable);
						for(String element: targetList)
						{
							reduceOne(childrenNumMap, element);
						}
					}
				}
			}
		}
		
		return resultList;
	}
	
	public void addOne(HashMap<String, Integer> childrenNumMap, String key)
	{
		if(childrenNumMap.containsKey(key))
		{
			int value = childrenNumMap.get(key);
			childrenNumMap.put(key, value + 1);
		}
		else
		{
			childrenNumMap.put(key, 1);
		}
	}
	
	public void reduceOne(HashMap<String, Integer> childrenNumMap, String key)
	{
		if(childrenNumMap.containsKey(key))
		{
			int value = childrenNumMap.get(key);
			childrenNumMap.put(key, value - 1);
		}
	}
	
	public int getMinimumTimeTick()
	{
		return startTimeTick;
	}

	/**
	 * @return the ruleMap
	 */
	public HashMap<String, HashSet<String>> getRuleMap() {
		return ruleMap;
	}

}
