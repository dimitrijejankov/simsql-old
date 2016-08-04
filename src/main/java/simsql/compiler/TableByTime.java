

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

/**
 * @author Bamboo
 *
 */
public class TableByTime {
	private int time;
	private HashSet<String> tableSet;
	private HashMap<String, HashSet<String>> timeMap;
	
	public TableByTime(int time)
	{
		this.time = time;
		this.tableSet = new HashSet<String>();
		this.timeMap = new HashMap<String, HashSet<String>>();
	}
	
	public TableByTime(int time, HashSet<String> tableSet, HashMap<String, HashSet<String>> timeMap)
	{
		this.time = time;
		this.tableSet = tableSet;
		this.timeMap = timeMap;
	}
	
	public void addTable(String table)
	{
		if(!tableSet.contains(table))
			tableSet.add(table);
	}
	
	public void addEdge(String origin, String generatedTable)
	{
		HashSet<String> generatedList;
		
		if(!timeMap.containsKey(origin))
		{
			generatedList = new HashSet<String>();
			timeMap.put(origin, generatedList);
		}
		
		generatedList = timeMap.get(origin);
		
		if(!generatedList.contains(generatedTable))
		{
			generatedList.add(generatedTable);
		}
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}

	/**
	 * @param time the time to set
	 */
	public void setTime(int time) {
		this.time = time;
	}

	/**
	 * @return the tableList
	 */
	public HashSet<String> getTableSet() {
		return tableSet;
	}

	/**
	 * @param tableList the tableList to set
	 */
	public void setTableSet(HashSet<String> tableList) {
		this.tableSet = tableList;
	}

	/**
	 * @return the timeMap
	 */
	public HashMap<String, HashSet<String>> getTimeMap() {
		return timeMap;
	}

	/**
	 * @param timeMap the timeMap to set
	 */
	public void setTimeMap(HashMap<String, HashSet<String>> timeMap) {
		this.timeMap = timeMap;
	}
	
	public TableByTime copy()
	{
		int c_time = time;
		HashSet<String> c_tableList = copy(tableSet);
		HashMap<String, HashSet<String>> c_timeMap = new HashMap<String, HashSet<String>>();
		
		if(c_timeMap != null)
		{
			for(String s: timeMap.keySet())
			{
				c_timeMap.put(s, copy(timeMap.get(s)));
			}
		}
		
		return new TableByTime(c_time, c_tableList, c_timeMap);
	}
	
	public HashSet<String> copy(HashSet<String> set)
	{
		HashSet<String> resultSet = new HashSet<String>();
		if(set != null)
		{
			for(String temp: set)
			{
				resultSet.add(new String(temp));
			}
		}
		
		return resultSet;
	}
	
	
	public String toString()
	{
		String result = "time " + time;
		result += "\n{";
		result += "\n \t tablelist: {";
		
		int i = 0;
		for(String key: tableSet)
		{
			result += key;
			i++;
			if(i != tableSet.size())
			{
				result += ", ";
			}
		}
		result += "}";
		
		result += "\n \t timeMap: {";
		for(String s: timeMap.keySet())
		{
			HashSet<String> resultList = timeMap.get(s);
			result += "\n\t\t" + s + "->{";
			
			i = 0;
			for(String key: resultList)
			{
				result += key;
				
				i++;
				if(i != resultList.size())
				{
					result += ", ";
				}
			}
			
			result += "}";
		}
		result += "\n}";
		
		return result;
	}
	
	public ArrayList<String> souceListInThisCycle()
	{
		HashSet<String> tempSet = new HashSet<String>();
		
		for(String s: timeMap.keySet())
		{
			HashSet<String> value = timeMap.get(s);
			
			for(String tempS: value)
			{
				if(!tempSet.contains(tempS))
				{
					tempSet.add(tempS);
				}
			}
		}
		
		ArrayList<String> resultList = new ArrayList<String>();
		for(String tempS: tableSet)
		{
			if(!tempSet.contains(tempS))
			{
				resultList.add(tempS);
			}
		}
		
		return resultList;
	}
}
