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

package simsql.compiler.operators; // package mcdb.compiler.logicPlan.logicOperator.relationOperator;

import simsql.compiler.CommonContent;
import simsql.compiler.CopyHelper;

import java.util.ArrayList;


/**
 * The class that represents the Seed in the logical query plan
 */
public class Seed extends Operator {

	/**
	 * the name of the seed attribute in the logical query plan
	 */
	private String seedAttributeName;

	/**
	 * the name of the table
	 */
	private String tableName;

	/**
	 * @param nodeName the name of the operator
	 * @param children the children of the operator
	 * @param parents  the parent operators
	 */
	public Seed(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents) {
		super(nodeName, children, parents);
    }

    /**
     *
     * @param nodeName the name of the operator
     * @param tableName the name of the table
     * @param children the children of the operator
     * @param parents the parents of the operator
     * @param seedAttributeName the name of the seed attribute
     */
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


    /**
     * @return gets the name of the table
     */
    public String getTableName()
	{
		return tableName;
	}

    /**
     *
     * @return returns the name of the seed attribute
     */
    public String getSeedAttributeName() {
		return seedAttributeName;
	}

    /**
     *
     * @param seedAttributeName sets the name of the seed attribute
     */
	public void setSeedAttributeName(String seedAttributeName) {
		this.seedAttributeName = seedAttributeName;
	}


    /**
     * @return returns the string file representation of this operator
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

    /**
     * @param copyHelper an instance of the copy helper class
     * @return the deep copy of an operator
     * @throws Exception if the operation fails
     */
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
            for (Operator aChildren : children) {
                aChildren.addParent(seed);
            }
		}
		
		return seed;
	}
	
}
