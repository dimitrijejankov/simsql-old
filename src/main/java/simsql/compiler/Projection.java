

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
public class Projection extends Operator{
	
	private ArrayList<String> projectedNameList;
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public Projection(String nodeName,
			ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
	}
	
	
	public Projection(String nodeName, 
			ArrayList<Operator> children, 
			ArrayList<Operator> parents,
			ArrayList<String> projectedNameList) {
		super(nodeName, children, parents);
		this.projectedNameList = projectedNameList;
	}


	public ArrayList<String> getProjectedNameList() {
		return projectedNameList;
	}


	public void setProjectedNameList(ArrayList<String> projectedNameList) {
		this.projectedNameList = projectedNameList;
	}


	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += this.getNodeStructureString(); 
		
		result += "projection(" + this.getNodeName();
		result += ", [";
		
		if(projectedNameList != null)
		{
			for(int i = 0; i < projectedNameList.size(); i ++)
			{
				result += projectedNameList.get(i);
				
				if(i != projectedNameList.size()-1)
				{
					result += ", ";
				}
			}
		}
		
		result += "]";
		result += ").\r\n";
		
		return result;
	}
	
	public Operator copy(CopyHelper copyHelper) throws Exception
	{
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		ArrayList<String>  c_projectedNameList = copyHelper.copyStringList(this.projectedNameList);
		
		String c_nodeName = commonContent.nodeName;
		ArrayList<Operator> c_children = commonContent.children;
		ArrayList<Operator> c_parents = commonContent.parents;
		
		Projection projection = new Projection(c_nodeName, 
				c_children, 
				c_parents,
				c_projectedNameList);
		
		projection.setNameMap(commonContent.nameMap);
		projection.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), projection);
		
		ArrayList<Operator> children = projection.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(projection);
			}
		}
		return projection;
	}
}
