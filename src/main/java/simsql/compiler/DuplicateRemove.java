

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


// import mcdb.compiler.logicPlan.logicOperator.CommonContent;
// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public class DuplicateRemove extends Operator {

	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public DuplicateRemove(String nodeName, 
			ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += this.getNodeStructureString();
		result += "dedup(" + this.getNodeName() + ", []). \r\n";
		return result;
	}
	
	public Operator copy(CopyHelper copyHelper) throws Exception
	{
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		String c_nodeName = commonContent.nodeName;
		ArrayList<Operator> c_children = commonContent.children;
		ArrayList<Operator> c_parents = commonContent.parents;
		
		DuplicateRemove dr = new DuplicateRemove(c_nodeName, c_children, c_parents);
		dr.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		dr.setNameMap(commonContent.nameMap);
		
		copyHelper.getCopiedMap().put(getNodeName(), dr);
		
		ArrayList<Operator> children = dr.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(dr);
			}
		}
		
		return dr;
	}
}
