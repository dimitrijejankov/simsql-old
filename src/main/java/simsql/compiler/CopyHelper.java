

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.Operator;
import simsql.runtime.TypeMachine;
import simsql.runtime.DataType;


// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;

/**
 * @author Bamboo
 *
 */
public class CopyHelper {
	private HashMap<String, Operator> copiedMap;
	
	/**
	 * @param copiedMap
	 * @param operator
	 */
	public CopyHelper() {
		super();
		this.copiedMap = new HashMap<String, Operator>();
	}
	
	/**
	 * @return the copiedMap
	 */
	public HashMap<String, Operator> getCopiedMap() {
		return copiedMap;
	}

	/**
	 * @param copiedMap the copiedMap to set
	 */
	public void setCopiedMap(HashMap<String, Operator> copiedMap) {
		this.copiedMap = copiedMap;
	}


	public CommonContent copyBasicOperator(Operator operator) throws Exception
	{
		/* original content */
		String nodeName = operator.getNodeName();
		ArrayList<Operator> children = operator.getChildren();
		ArrayList<Operator> parents = operator.getParents();
		
		/*
		 * Additional data structure
		 */
		HashMap<String, String> nameMap = operator.getNameMap();
		HashSet<String> mapSpaceNameSet = operator.getMapSpaceNameSet();
		
		
		/*
		 * -------------------------------begin copy-------------------------------------
		 */
		String c_nodeName = new String(nodeName);
		ArrayList<Operator> c_children = new ArrayList<Operator>(children.size());
		for(int i = 0; i < children.size(); i++)
		{
			c_children.add(children.get(i).copy(this));
		}
		
		ArrayList<Operator> c_parents = new ArrayList<Operator>(parents.size());
		
		/*
		 * Copy of parents should be left to future.
		 */
		
		/*
		 * Additional data structure
		 */
		HashMap<String, String> c_nameMap = new HashMap<String, String>();
		for(String s: nameMap.keySet())
		{
			c_nameMap.put(new String(s), new String(nameMap.get(s)));
		}
		
		HashSet<String> c_mapSpaceNameSet = new HashSet<String>();
		for(Object s: mapSpaceNameSet.toArray())
		{
			c_mapSpaceNameSet.add((String)s);
		}
		
		return new CommonContent(c_nodeName, 
								c_children,
								c_parents, 
								c_nameMap,
								c_mapSpaceNameSet);
	}
	
	public ArrayList<String> copyStringList(ArrayList<String> list)
	{
		ArrayList<String> c_list= new ArrayList<String>(list.size());
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				c_list.add(new String(list.get(i)));
			}
		}
		return c_list;
	}
	
	public ArrayList<Integer> copyIntegerList(ArrayList<Integer> list)
	{
		ArrayList<Integer> c_list= new ArrayList<Integer>(list.size());
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				c_list.add(list.get(i).intValue());
			}
		}
		return c_list;
	}

	public ArrayList<DataType> copyDataTypeList(ArrayList<DataType> list)
	{
		ArrayList<DataType> c_list= new ArrayList<DataType>(list.size());
		if(list != null)
		{
			for(int i = 0; i < list.size(); i++)
			{
				c_list.add(TypeMachine.fromString(list.get(i).writeOut()));
			}
		}
		return c_list;
	}
	
	public HashMap<String, String> copyStringMap(HashMap<String, String> map)
	{
		HashMap<String, String> c_Map = new HashMap<String, String>();
		if(map != null)
		{
			for(String s: map.keySet())
			{
				c_Map.put(new String(s), new String(c_Map.get(s)));
			}
		}
		
		return c_Map;
	}
	
	public ArrayList<MathOperator> copyMathOperatorList(ArrayList<MathOperator> operatorList)
	{
		ArrayList<MathOperator> resultList = new ArrayList<MathOperator>();
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				resultList.add(operatorList.get(i).copy(this));
			}
		}
		
		return resultList;
	}
	
	public ArrayList<BooleanOperator> copyBooleanOperatorList(ArrayList<BooleanOperator> operatorList)
	{
		ArrayList<BooleanOperator> resultList = new ArrayList<BooleanOperator>();
		
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				resultList.add(operatorList.get(i).copy(this));
			}
		}
				
		return resultList;
	}
	
	public HashMap<MathOperator, ArrayList<String>> copyMathOperatorStringListMap(
			ArrayList<MathOperator> originalMathOperatorList,
			ArrayList<MathOperator> c_OperatorList,
			HashMap<MathOperator, ArrayList<String>> map)
	{
		HashMap<MathOperator, ArrayList<String>> resultMap = new HashMap<MathOperator, ArrayList<String>>();
		
		for(MathOperator operator: map.keySet())
		{
			ArrayList<String> list = map.get(operator);
			int index = originalMathOperatorList.indexOf(operator);
			resultMap.put(c_OperatorList.get(index), copyStringList(list));
		}
		return resultMap;
	}
	
	public HashMap<MathOperator, String> copyMathOperatorStringMap(
			ArrayList<MathOperator> originalMathOperatorList,
			ArrayList<MathOperator> c_OperatorList,
			HashMap<MathOperator, String> map)
	{
		HashMap<MathOperator, String> resultMap = new HashMap<MathOperator, String>();
		
		for(MathOperator operator: map.keySet())
		{
			int index = originalMathOperatorList.indexOf(operator);
			
			String value = new String(map.get(operator));
			resultMap.put(c_OperatorList.get(index), value);
		}
		return resultMap;
	}
	
	public BooleanOperator copyBooleanPredicate(BooleanOperator predicate)
	{
		if(predicate == null)
			return null;
		else
			return predicate.copy(this);
	}
}
