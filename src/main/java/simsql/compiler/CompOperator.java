

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.booleanOperator;

import java.util.ArrayList;



// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.translator.MathExpressionTranslator;
// import mcdb.compiler.logicPlan.translator.TranslatorHelper;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.util.FinalVariable;

/**
 * @author Bamboo
 *
 */
public class CompOperator extends BooleanOperator{

	private int type;
	private MathOperator left, right;
	private String leftType, rightType;
	/**
	 * @param name
	 */
	public CompOperator(String name)
	{
		super(name);
	}
	
	public CompOperator(String name, 
						int type, 
						MathOperator left,
						MathOperator right,
						String leftType,
						String rightType)
	{
		super(name);
		this.type = type;
		this.left = left;
		this.right = right;
		this.leftType = leftType;
		this.rightType = rightType;
	}
	
	public int getType() {
		return type;
	}
	
	public void setType(int type) {
		this.type = type;
	}
	
	public MathOperator getLeft() {
		return left;
	}
	
	public void setLeft(MathOperator left) {
		this.left = left;
	}
	
	public MathOperator getRight() {
		return right;
	}
	
	public void setRight(MathOperator right) {
		this.right = right;
	}

	public String getLeftType() {
		return leftType;
	}

	public void setLeftType(String leftType) {
		this.leftType = leftType;
	}

	public String getRightType() {
		return rightType;
	}

	public void setRightType(String rightType) {
		this.rightType = rightType;
	}

	/* (non-Javadoc)
	 * @see logicOperator.booleanOperator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "compExp(" + this.getName();
		
		result += ", ";
		
		//2. Deal with the operation type
		result += translaterComparisonOperation(type);
		result += ", ";
		
		result += left.getNodeName();
		result += ", ";
		result += right.getNodeName();
		result += ", ";
		
		result += rightType;
		result += ").\r\n";
		
		result += left.visitNode();
		result += right.visitNode();
		
		return result;
	}
	
	public String translaterComparisonOperation(int type)
	{
		String result = "";
		switch(type)
		{
			case FinalVariable.EQUALS:
				result = "equals";
				break;
				
			case FinalVariable.NOTEQUALS:
				result ="notEquals";
				break;
				
			case FinalVariable.LESSTHAN:
				result = "lessThan";
				break;
				
			case FinalVariable.GREATERTHAN:
				result = "greaterThan";
				break;
				
			case FinalVariable.LESSEQUAL:
				result = "lessEquals";
				break;
				
			case FinalVariable.GREATEREQUAL:
				result = "greaterEquals";
				break;
		}
		
		return result;
	}

	@Override
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.getName());
		int c_type = this.type;
		MathOperator c_left = left.copy(copyHelper);
		MathOperator c_right = right.copy(copyHelper);
		String c_leftType = new String(leftType);
		String c_rightType = new String(rightType);
		
		return new CompOperator(c_name, c_type, c_left, c_right, c_leftType, c_rightType);
	}
	
}
