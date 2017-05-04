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

package simsql.compiler.operators;

import simsql.compiler.CopyHelper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


/**
 * The class that represents the Operator in the logical query plan
 */
public abstract class Operator {

	/**
	 * the name of the node in the logical query plan
	 */
	private String nodeName;

	/**
	 * the children of the operators in the logical query plan
	 */
	private ArrayList<Operator> children;

	/**
	 * the parents of the operators in the logical query plan
	 */
	private ArrayList<Operator> parents;


	/**
	 * used to replace the names of the attributes
	 */
	private HashMap<String, String> nameMap;

	/**
	 * TODO figure out what this thing is.... (something related to attribute renaming)
	 */
	private HashSet<String> mapSpaceNameSet;

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents the parent operators
     */
	public Operator(String nodeName, 
					ArrayList<Operator> children,
	                ArrayList<Operator> parents)
	{
		this.nodeName = nodeName;
		this.children = children;
		this.parents = parents;

		// Initialization of nameMap
		nameMap = new HashMap<String, String>();
		mapSpaceNameSet = new HashSet<String>();
	}


    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents the parent operators
     * @param nameMap the name map to be used
     * @param mapSpaceNameSet the map space name set to be used...
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


    /**
     * @return gets the node name
     */
    public String getNodeName() {
		return nodeName;
	}

    /**
     * @param nodeName sets the node name
     */
    public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}

    /**
     * @return returns the list of children associated with this node
     */
    public ArrayList<Operator> getChildren() {
		return children;
	}

    /**
     * @param children sets the list of children associated with this node
     */
    public void setChildren(ArrayList<Operator> children) {
		this.children = children;
	}

    /**
     * @return gets the list of parents associated with this node
     */
    public ArrayList<Operator> getParents() {
		return parents;
	}

    /**
     *
     * @param parents sets the list of parents associated with this node
     */
	public void setParents(ArrayList<Operator> parents) {
		this.parents = parents;
	}

    /**
     *
     * @param operator adds a new parent
     */
	public void addParent(Operator operator)
	{
		parents.add(operator);
	}

    /**
     * Adds a new child at a certain position
     * @param index the position of child in the list of children
     * @param operator the child operator
     */
	public void addChild(int index, Operator operator)
	{
		children.add(index, operator);
	}


    /**
     * Replaces a child operator in the child list with the given operator
     * @param op1 the child operator to be replaced
     * @param op2 the new child operator
     */
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

    /**
     * Adds a child operator to the children list
     * @param operator the new operator
     */
    public void addChild(Operator operator)
	{
		children.add(operator);
	}

    /**
     * remove a parent from the list of parent operators
     * @param operator the parent operator to be removed
     */
	public void removeParent(Operator operator)
	{
		parents.remove(operator);
	}

    /**
     * remove a child from the child operator list
     * @param operator the child to be removed
     */
	public void removeChild(Operator operator)
	{
		children.remove(operator);
	}

    /**
     * removes all the parents and children from this node
     */
	public void clearLinks()
	{
		parents.clear();
		children.clear();
	}

    /**
     *
     * @return the string that has the node structure
     */
	public String getNodeStructureString()
	{
		String result = "";
		ArrayList<Operator> temp = getChildren();
		if(temp != null)
		{
            for (Operator aTemp : temp) {
                result += "parent(" + getNodeName() + ", " + aTemp.getNodeName() + ").\r\n";
            }
		}
		return result;
	}

    /**
     *
     * @param list
     * @return
     */
	String getListString(ArrayList<String> list)
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

    /**
     *
     * @param s
     * @return
     */
	public boolean isMapped(String s)
	{
		return nameMap.containsKey(s);
	}

    /**
     *
     * @param s1
     * @param s2
     */
	public void putNameMap(String s1, String s2)
	{
		nameMap.put(s1, s2);
		mapSpaceNameSet.add(s2);
	}

    /**
     *
     * @param s2
     * @return
     */
	public boolean isSpaceNamein(String s2)
	{
		return mapSpaceNameSet.contains(s2);
	}

    /**
     *
     * @return
     */
	public HashMap<String, String> getNameMap() {
		return nameMap;
	}

    /**
     *
     * @param nameMap
     */
	public void setNameMap(HashMap<String, String> nameMap) {
		this.nameMap = nameMap;
	}

    /**
     *
     * @return
     */
	public HashSet<String> getMapSpaceNameSet() {
		return mapSpaceNameSet;
	}

    /**
     *
     * @param mapSpaceNameSet
     */
	public void setMapSpaceNameSet(HashSet<String> mapSpaceNameSet) {
		this.mapSpaceNameSet = mapSpaceNameSet;
	}

    /**
     *
     * @return
     */
	public String toString()
	{
		String result;
		try{
			result = nodeName + ".\r\n" + visitNode();
		}
		catch(Exception e)
		{
			result = nodeName;
		}
		
		return result;
	}

    /**
     *
     */
    public void clearRenamingInfo()
	{
		clearNameMap();
		clearMapSpaceNameSet();
	}

    /**
     *
     */
	void clearNameMap()
	{
		nameMap.clear();
	}

    /**
     *
     */
	void clearMapSpaceNameSet()
	{
		mapSpaceNameSet.clear();
	}

    /**
     * @return returns the string file representation of this operator
     */
	public abstract String visitNode() throws Exception;

    /**
     * @param copyHelper an instance of the copy helper class
     * @return the deep copy of an operator
     * @throws Exception if the operation fails
     */
	public abstract Operator copy(CopyHelper copyHelper) throws Exception;
	
}
