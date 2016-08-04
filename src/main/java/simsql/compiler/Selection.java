

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
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AndOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;



/**
 * @author Bamboo
 *
 */
public class Selection extends Operator{

	private BooleanOperator booleanOperator;
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public Selection(String nodeName, 
			ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
		// TODO Auto-generated constructor stub
	}

	public Selection(String nodeName, 
					ArrayList<Operator> children, 
					ArrayList<Operator> parents,
					BooleanOperator booleanOperator
			) 
	{
		super(nodeName, children, parents);
		this.booleanOperator = booleanOperator;
	}

	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}

	public void setBooleanOperator(BooleanOperator booleanOperator) {
		this.booleanOperator = booleanOperator;
	}

	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += this.getNodeStructureString();
		
		result += "selection(" + this.getNodeName() + ", [";
		
		if(booleanOperator instanceof AndOperator)
		{
			/*
			 * case 4.1: AndOperator
			 */
			ArrayList<BooleanOperator> andList = ((AndOperator) booleanOperator).getOperatorList();
			
			for(int i = 0 ; i < andList.size(); i++)
			{
				result += andList.get(i).getName();
				
				if(i != andList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		else
		{
			result += booleanOperator.getName();
			
		}
		
		result +=  "]).\r\n";
		
		if(booleanOperator instanceof AndOperator)
		{
			/*
			 * case 4.1: AndOperator
			 */
			ArrayList<BooleanOperator> andList = ((AndOperator) booleanOperator).getOperatorList();
			
			for(int i = 0 ; i < andList.size(); i++)
			{
				result += andList.get(i).visitNode();
				
			}
		}
		else
		{
			result += booleanOperator.visitNode();
			
		}
		
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
		BooleanOperator c_booleanOperator = copyHelper.copyBooleanPredicate(this.booleanOperator);
		Selection c_selection = new Selection(c_nodeName, c_children, c_parents, c_booleanOperator);
		c_selection.setNameMap(commonContent.nameMap);
		c_selection.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), c_selection);
		
		ArrayList<Operator> children = c_selection.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(c_selection);
			}
		}
		return c_selection;
	}
}
