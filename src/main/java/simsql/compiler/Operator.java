

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.relationOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public abstract class Operator {
	private String nodeName;
	private ArrayList<Operator> children;
	private ArrayList<Operator> parents; 
	
	/*
	 * Additional data structure
	 */
	private HashMap<String, String> nameMap;
	private HashSet<String> mapSpaceNameSet;
	
	public Operator(String nodeName, 
					ArrayList<Operator> children,
	                ArrayList<Operator> parents)
	{
		this.nodeName = nodeName;
		this.children = children;
		this.parents = parents;
		
		/*
		 * Initialization of nameMap
		 */
		nameMap = new HashMap<String, String>();
		mapSpaceNameSet = new HashSet<String>();
	}
	
	
	
	/**
	 * @param nodeName
	 * @param children
	 * @param parents
	 * @param nameMap
	 * @param mapSpaceNameSet
	 */
	public Operator(String nodeName, ArrayList<Operator> children,
			ArrayList<Operator> parents, HashMap<String, String> nameMap,
			HashSet<String> mapSpaceNameSet) {
		super();
		this.nodeName = nodeName;
		this.children = children;
		this.parents = parents;
		this.nameMap = nameMap;
		this.mapSpaceNameSet = mapSpaceNameSet;
	}



	public String getNodeName() {
		return nodeName;
	}

	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

	public ArrayList<Operator> getChildren() {
		return children;
	}

	public void setChildren(ArrayList<Operator> children) {
		this.children = children;
	}

	public ArrayList<Operator> getParents() {
		return parents;
	}

	public void setParents(ArrayList<Operator> parents) {
		this.parents = parents;
	}
	
	public void addParent(Operator operator)
	{
		parents.add(operator);
	}
	
	public void addChild(int index, Operator operator)
	{
		children.add(index, operator);
	}
	
	public void replaceChild(Operator op1, Operator op2)
	{
		int index = children.indexOf(op1);
		if(index >= 0)
		{
			children.remove(op1);
			children.add(index, op2);
		}
		else
		{
			children.add(op2);
		}
	}
	
	public void addChild(Operator operator)
	{
		children.add(operator);
	}
	
	public void removeParent(Operator operator)
	{
		parents.remove(operator);
	}
	
	public void removeChild(Operator operator)
	{
		children.remove(operator);
	}
	
	public void clearLinks()
	{
		parents.clear();
		children.clear();
	}
	
	public String getNodeStructureString()
	{
		String result = "";
		ArrayList<Operator> temp = getChildren();
		if(temp != null)
		{
			for(int i = 0; i < temp.size(); i++)
			{
				result += "parent(" + getNodeName() + ", " + temp.get(i).getNodeName() + ").\r\n";
			}
		}
		return result;
	}
	
	public String getListString(ArrayList<String> list)
	{
		String result = "[";
		for(int i = 0; i < list.size(); i++)
		{
			result += list.get(i);
			
			if(i != list.size() -1)
			{
				result += ", ";
			}
		}
		
		result += "]";
		return result;
	}
	
	public boolean isMapped(String s)
	{
		return nameMap.containsKey(s);
	}
	
	public void putNameMap(String s1, String s2)
	{
		nameMap.put(s1, s2);
		mapSpaceNameSet.add(s2);
	}
	
	public boolean isSpaceNamein(String s2)
	{
		return mapSpaceNameSet.contains(s2);
	}
	
	public HashMap<String, String> getNameMap() {
		return nameMap;
	}

	public void setNameMap(HashMap<String, String> nameMap) {
		this.nameMap = nameMap;
	}

	public HashSet<String> getMapSpaceNameSet() {
		return mapSpaceNameSet;
	}

	public void setMapSpaceNameSet(HashSet<String> mapSpaceNameSet) {
		this.mapSpaceNameSet = mapSpaceNameSet;
	}
	
	public String toString()
	{
		String result = "";
		try{
			result = nodeName + ".\r\n" + visitNode();
		}
		catch(Exception e)
		{
			result = nodeName;
		}
		
		return result;
	}
	
	public void clearRenamingInfo()
	{
		clearNameMap();
		clearMapSpaceNameSet();
	}
	
	public void clearNameMap()
	{
		nameMap.clear();
	}
	
	public void clearMapSpaceNameSet()
	{
		mapSpaceNameSet.clear();
	}

	public abstract String visitNode() throws Exception;
	public abstract Operator copy(CopyHelper copyHelper) throws Exception;
	
}
