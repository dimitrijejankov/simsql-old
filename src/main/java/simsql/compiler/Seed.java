

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
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;

/**
 * @author Bamboo
 *
 */
public class Seed extends Operator{

	private String seedAttributeName;
	private String tableName;
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public Seed(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
		
	}
	
	
	public Seed(String nodeName, 
				String tableName,
				ArrayList<Operator> children, 
				ArrayList<Operator> parents,
				String seedAttributeName) 
	{
		super(nodeName, children, parents);
		this.tableName = tableName;
		this.seedAttributeName = seedAttributeName;
	}
	
	
	public String getTableName()
	{
		return tableName;
	}

	public String getSeedAttributeName() {
		return seedAttributeName;
	}
	public void setSeedAttributeName(String seedAttributeName) {
		this.seedAttributeName = seedAttributeName;
	}


	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		result += this.getNodeStructureString();
		
		result += "seed(";
		result += this.getNodeName() + ", ";
		result += seedAttributeName;
		result += ").\r\n";
		
		return result;
	}

	public ArrayList<String> getGeneratedNameList()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		resultList.add(seedAttributeName);
		return resultList;
	}
	
	public Operator copy(CopyHelper copyHelper) throws Exception
	{
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		
		String c_nodeName =  commonContent.nodeName;
		String c_tableName = this.tableName;
		ArrayList<Operator> c_children = commonContent.children;
		ArrayList<Operator> c_parents = commonContent.parents;
		String c_seedAttributeName = this.seedAttributeName;
		
		Seed seed = new Seed(c_nodeName, c_tableName, c_children, c_parents, c_seedAttributeName);
		seed.setNameMap(commonContent.nameMap);
		seed.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		copyHelper.getCopiedMap().put(getNodeName(), seed);
		
		ArrayList<Operator> children = seed.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(seed);
			}
		}
		
		return seed;
	}
	
}
