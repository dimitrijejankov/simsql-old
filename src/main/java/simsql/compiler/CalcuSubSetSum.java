

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
import java.util.Stack;

/**
 * @author Bamboo
 *
 */
public class CalcuSubSetSum 
{
	private ArrayList<String> nodeList;
	private HashMap<String, Long> costMap;
	private long sum;
	
	private Stack<Integer> stack;
	/**
	 * @param nodeList
	 * @param costMap
	 * @param sum
	 */
	public CalcuSubSetSum(ArrayList<String> nodeList,
						  HashMap<String, Long> costMap,
						  long sum)
	{
		super();
		this.nodeList = nodeList;
		this.costMap = costMap;
		this.sum = sum;
		this.stack = new Stack<Integer>();
	}
	
	/*
	 * This function tries to find the subSet of nodeList, of which the sum cost equals the "sum".
	 */
	public void calc(ArrayList<ArrayList<String>> resultList, 
			long currentSum, 
			int from, 
			int to)
	{
		if(currentSum == sum)
		{
			ArrayList<String> currentResultList = new ArrayList<String>();
			for(Integer index: stack)
			{
				currentResultList.add(nodeList.get(index));
			}
			
			resultList.add(currentResultList);
		}
		
		
		for(int i = from; i < to; i++)
		{
			String currentNode = nodeList.get(i);
			long cost = costMap.get(currentNode);
			if(currentSum + cost <= sum)
			{
				currentSum += cost;
				stack.push(i);
				calc(resultList, currentSum, i+1, to);
				
				int index = stack.pop();
				String node = nodeList.get(index);
				cost = costMap.get(node);
				currentSum -= cost;
			}
		}
	}
	
	/*
	 * Testcase
	 * public static void main(String []args)
		{
			ArrayList<String> list = new ArrayList<String>();
			list.add("num8");
			list.add("num5");
			list.add("num4");
			list.add("num3");
			list.add("num2");
			list.add("num1");
			list.add("num0");
			
			HashMap<String, Integer> costMap = new HashMap<String, Integer>();
			costMap.put("num8", 8);
			costMap.put("num5", 5);
			costMap.put("num4", 4);
			costMap.put("num3", 3);
			costMap.put("num2", 2);
			costMap.put("num1", 1);
			costMap.put("num0", 0);
			
			CalcuSubSetSum sum = new CalcuSubSetSum(list, costMap, 10);
			ArrayList<ArrayList<String>> resultlist = new ArrayList<ArrayList<String>>();
			sum.calc(resultlist, 0, 0, 7);
			System.out.println(resultlist.size());
		}
	 */
}
