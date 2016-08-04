

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
// import mcdb.compiler.logicPlan.translator.BooleanPredicateTranslator;
// import mcdb.compiler.parser.expression.boolExpression.AndPredicate;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;




/**
 * @author Bamboo
 *
 */
public class Join extends Operator{

	public static final int JOIN = 0;
	public static final int SEMIJOIN = 1;
	public static final int ANTIJOIN = 2;
	
	/*
	 * type can be:
	 * 	1. join
	 *  2. semijoin
	 *  3. antijoin
	 */
	private int type;
	private String leftTable;
	private BooleanOperator booleanOperator;
	
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public Join(String nodeName, 
			ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
		// TODO Auto-generated constructor stub
	}

	public Join(String nodeName, 
			ArrayList<Operator> children, 
			ArrayList<Operator> parents,
			int type, 
			String leftTable, 
			BooleanOperator booleanOperator) {
		super(nodeName, children, parents);
		this.type = type;
		this.leftTable = leftTable;
		this.booleanOperator = booleanOperator;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getLeftTable() {
		return leftTable;
	}

	public void setLeftTable(String leftTable) {
		this.leftTable = leftTable;
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
		
		switch(type)
		{
			case SEMIJOIN:	
				result += "semijoin(" + getNodeName() + ", ";
				result += leftTable;
				result += ", [";
				
				if(booleanOperator != null)
				{
					if(booleanOperator instanceof AndOperator)
					{
						ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
						for(int i = 0 ; i < subBooleanOperatorList.size(); i++)
						{
							if(i != subBooleanOperatorList.size() - 1)
							{
								result += subBooleanOperatorList.get(i).getName();
								result += ", ";
							}
							else
							{
								result += subBooleanOperatorList.get(i).getName();
							}	
						}
					}
					else
					{
						result += booleanOperator.getName();
					}
				}
				
				result += "]). \r\n";
				break;
				
			case ANTIJOIN:
				result += "antijoin(" + getNodeName() + ", ";
				result += leftTable;
				result += ", [";
				
				if(booleanOperator != null)
				{
					if(booleanOperator instanceof AndOperator)
					{
						ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
						for(int i = 0 ; i < subBooleanOperatorList.size(); i++)
						{
							if(i != subBooleanOperatorList.size() - 1)
							{
								result += subBooleanOperatorList.get(i).getName();
								result += ", ";
							}
							else
							{
								result += subBooleanOperatorList.get(i).getName();
							}	
						}
					}
					else
					{
						result += booleanOperator.getName();
					}
				}
				
				
				result += "]). \r\n";
				break;
				
			default:
				result += "join(" + getNodeName() + ", [";
				
				if(booleanOperator != null)
				{
					if(booleanOperator instanceof AndOperator)
					{
						ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
						for(int i = 0 ; i < subBooleanOperatorList.size(); i++)
						{
							if(i != subBooleanOperatorList.size() - 1)
							{
								result += subBooleanOperatorList.get(i).getName();
								result += ", ";
							}
							else
							{
								result += subBooleanOperatorList.get(i).getName();
							}	
						}
					}
					else
					{
						result += booleanOperator.getName();
					}
				}
				
				
				result += "], _). \r\n";
				break;
		}
		
		if(booleanOperator != null)
		{
			if(booleanOperator instanceof AndOperator)
			{
				ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
				for(int i = 0 ; i < subBooleanOperatorList.size(); i++)
				{
					result += subBooleanOperatorList.get(i).visitNode();
				}
			}
			else
			{
				result += booleanOperator.visitNode();
			}
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
		
		int c_type = this.type;
		String c_leftTable = new String(this.leftTable);
		BooleanOperator c_booleanOperator = copyHelper.copyBooleanPredicate(this.booleanOperator);
		
		Join c_jion = new Join(c_nodeName, c_children, c_parents, c_type, c_leftTable, c_booleanOperator);
		c_jion.setNameMap(commonContent.nameMap);
		c_jion.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), c_jion);
		
		ArrayList<Operator> children = c_jion.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(c_jion);
			}
		}
		return c_jion;
	}
	
}
