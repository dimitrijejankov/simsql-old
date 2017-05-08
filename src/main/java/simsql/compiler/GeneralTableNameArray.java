

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


package simsql.compiler;


import simsql.compiler.expressions.MathExpression;

public class GeneralTableNameArray extends SQLExpression{

	private String name;
	private MathExpression lowerBoundExp;
	private MathExpression upBoundExp;
	
	public GeneralTableNameArray(String name, MathExpression lowerBoundExp,
			MathExpression upBoundExp) {
		super();
		this.name = name;
		this.lowerBoundExp = lowerBoundExp;
		this.upBoundExp = upBoundExp;
	}


	public MathExpression getLowerBoundExp() {
		return lowerBoundExp;
	}


	public void setLowerBoundExp(MathExpression lowerBoundExp) {
		this.lowerBoundExp = lowerBoundExp;
	}


	public MathExpression getUpBoundExp() {
		return upBoundExp;
	}


	public void setUpBoundExp(MathExpression upBoundExp) {
		this.upBoundExp = upBoundExp;
	}


	public String getName() {
		return name;
	}


	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersionUpName()
	{
		String indexString = new MPNGenerator(upBoundExp).convertToMPN();
		return name + "_" + indexString;
	}

	public String getVersionLowerName()
	{
		String indexString = new MPNGenerator(lowerBoundExp).convertToMPN();
		return name + "_" + indexString;
	}
	
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitGeneralTableNameArray(this);	
	}

}
