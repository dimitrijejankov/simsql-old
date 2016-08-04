

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

import java.util.HashMap;


// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;
/**
 * @author Bamboo
 *
 */
public class RenameNode{

	public Operator child;
	public Operator parent;
	
	public HashMap<String, String> attributeMap;
	
	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	public RenameNode(Operator child,
					  Operator parent)
	{
		this.child = child;
		this.parent = parent;
		attributeMap = new HashMap<String, String>();
	}
	
	public void addStringMap(String original, String mappedString) throws Exception
	{
		if(!this.attributeMap.containsKey(original))
		{
			attributeMap.put(original, mappedString);
		}
		else
		{
			String temp = attributeMap.get(original);
			if(!temp.equals(mappedString))
			{
				System.err.println("error during mapping!");
				throw new Exception("error during mapping!");
			}
		}
	}
	
	public int getMapsize()
	{
		return attributeMap.size();
	}
}
