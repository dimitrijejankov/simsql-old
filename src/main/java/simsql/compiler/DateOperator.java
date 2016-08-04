

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
public class DateOperator implements MathOperator{
	private String string;
	
	public DateOperator(String string)
	{
		this.string = string;
	}

	public String getDateString() {
		return string;
	}

	public void setDateString(String string) {
		this.string = string;
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
		return "'" + string + "'";
	}
	
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_string = new String(string);
		DateOperator c_operator = new DateOperator(c_string);
		
		return c_operator;
	}
}
