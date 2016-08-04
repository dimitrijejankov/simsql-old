

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
package simsql.compiler;

import java.util.ArrayList;

/*
import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AndOperator;
import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
import mcdb.compiler.logicPlan.logicOperator.relationOperator.*;
*/
/**
 * @author Bamboo
 *
 */
public class FrameOutput extends Operator{

	private ArrayList<String> tableList;
	/**
	 * @param nodeName
	 * @param children
	 * @param parents
	 */
	public FrameOutput(String nodeName, 
					   ArrayList<Operator> children,
					   ArrayList<Operator> parents,
					   ArrayList<String> tableList)
	{
		super(nodeName, children, parents);
		this.tableList = tableList;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode() throws Exception {
		String result = "";
		
		result += this.getNodeStructureString(); 
		
		result += "frameoutput(" + getNodeName() + ", [";
		
		for(int i = 0; i < this.getChildren().size(); i++)
		{
			Operator child = this.getChildren().get(i);
			result += child.getNodeName();
			
			if(i != this.getChildren().size()-1)
			{
				result += ", ";
			}
		}
		
		result += "], [";
		for(int i = 0; i < tableList.size(); i++)
		{
			//result += TempScanHelper.filePrefix + tableList.get(i);
			result += tableList.get(i);
			
			if(i != tableList.size()-1)
			{
				result += ", ";
			}
		}
		
		result += "]). \r\n";
		
		return result;
	}
	


	public ArrayList<String> getTableList() {
		return tableList;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator#copy(mcdb.compiler.logicPlan.logicOperator.CopyHelper)
	 * This function should be never used!
	 */
	@Override
	public Operator copy(CopyHelper copyHelper) throws Exception {
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		ArrayList<String>  c_tableList = copyHelper.copyStringList(this.tableList);
		
		String c_nodeName = commonContent.nodeName;
		ArrayList<Operator> c_children = commonContent.children;
		ArrayList<Operator> c_parents = commonContent.parents;
		
		FrameOutput frameOutput = new FrameOutput(c_nodeName, 
				c_children, 
				c_parents,
				c_tableList);
		
		frameOutput.setNameMap(commonContent.nameMap);
		frameOutput.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), frameOutput);
		
		ArrayList<Operator> children = frameOutput.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(frameOutput);
			}
		}
		return frameOutput;
	}

}
