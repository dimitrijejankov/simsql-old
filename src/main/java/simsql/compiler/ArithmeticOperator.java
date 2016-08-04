

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
// import mcdb.compiler.logicPlan.translator.TranslatorHelper;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;
// import mcdb.compiler.parser.expression.util.FinalVariable;




/**
 * @author Bamboo
 *
 */
public class ArithmeticOperator implements MathOperator{
	private String name;
	private int type;
	private MathOperator left, right;
	private String leftType, rightType;
	
	public ArithmeticOperator(String name,
							  int type, 
							  MathOperator left, 
							  MathOperator right,
							  String leftType, 
							  String rightType)
	{
		super();
		this.name = name;
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
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		result += "arithExp(" + name;
		result += ", ";
		
		result += translaterArithmeticOperation(type);
		result += ", ";
		
		result += left.getNodeName();
		result += ", ";
		
		result += leftType + ", ";
		
		result += right.getNodeName();
		result += ", ";
		
		result += rightType;
		result += ").\r\n";
		
		result += left.visitNode();
		result += right.visitNode();
		return result;
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return name;
	}
	
	public void setName(String name)
	{
		this.name = name;
	}
	
	public String translaterArithmeticOperation(int type)
	{
		String result = "";
		
		switch(type)
		{
			case FinalVariable.PLUS:
				result = "plus";
				break;
				
			case FinalVariable.MINUS:
				result ="minus";
				break;
				
			case FinalVariable.TIMES:
				result = "times";
				break;
				
			case FinalVariable.DIVIDE:
				result = "divide";
				break;
		}
		
		return result;
	}
	
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_name = new String(name);
		int c_type = this.type;
		MathOperator c_left = this.left.copy(copyHelper);
		MathOperator c_right = this.right.copy(copyHelper);
		String c_leftType = new String(this.leftType);
		String c_rightType = new String(this.rightType);
		
		ArithmeticOperator c_operator = new ArithmeticOperator(c_name,
																c_type, 
																c_left, 
																c_right,
																c_leftType, 
																c_rightType);
		return c_operator;
	}
}
