

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
package simsql.compiler; // package mcdb.compiler.parser.expression.mathExpression;

import java.util.ArrayList;
import simsql.runtime.DataType;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;




/**
 * @author Bamboo
 *
 */
public class SetExpression extends MathExpression{

	public ArrayList<MathExpression> expressionList;
	
	
	public SetExpression(ArrayList<MathExpression> expressionList) {
		super();
		this.expressionList = expressionList;
	}

	public void addMathExpression(MathExpression expression)
	{
		expressionList.add(expression);
	}

	/* (non-Javadoc)
	 * @see component.expression.Expression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitSetExpression(this);	
	}
	
}
