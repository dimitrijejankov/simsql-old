

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


// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;

/**
 * @author Bamboo
 *
 */
public class CommonContent {
	public String nodeName;
	public ArrayList<Operator> children;
	public ArrayList<Operator> parents; 
	
	/*
	 * Additional data structure
	 */
	public HashMap<String, String> nameMap;
	public HashSet<String> mapSpaceNameSet;
	
	
	/**
	 * @param nodeName
	 * @param children
	 * @param parents
	 * @param nameMap
	 * @param mapSpaceNameSet
	 */
	public CommonContent(String nodeName, ArrayList<Operator> children,
			ArrayList<Operator> parents, HashMap<String, String> nameMap,
			HashSet<String> mapSpaceNameSet) {
		super();
		this.nodeName = nodeName;
		this.children = children;
		this.parents = parents;
		this.nameMap = nameMap;
		this.mapSpaceNameSet = mapSpaceNameSet;
	}
}
