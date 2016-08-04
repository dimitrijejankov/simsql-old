

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.mathOperator;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public class ColumnOperator implements MathOperator
{
	private String columnName;

	public ColumnOperator(String columnName) {
		super();
		this.columnName = columnName;
	}

	public String getColumnName() {
		return columnName;
	}

	public void setColumnName(String columnName) {
		this.columnName = columnName;
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		/*
		 * Such atomic operator should return "". Since it only provides
		 * the name of the operator, and we do not need to provide new lines.
		 */
		return "";
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return columnName;
	}
	
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_columnName = new String(columnName);
		ColumnOperator c_operator = new ColumnOperator(c_columnName);
		
		return c_operator;
	}
	
}
