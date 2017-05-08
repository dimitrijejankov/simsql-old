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

package simsql.compiler.expressions;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;


/**
 * Represents the set expression in the AST
 */
public class SetExpression extends MathExpression {


    /**
     * the list of expressions in the set expression
     */
    @JsonProperty("expression-list")
    public ArrayList<MathExpression> expressionList;

	@JsonCreator
	public SetExpression(@JsonProperty("expression-list") ArrayList<MathExpression> expressionList) {
		super();
		this.expressionList = expressionList;
	}

    /**
     * Adds one math expression to the set expression
     * @param expression the expression to be added
     */
	public void addMathExpression(MathExpression expression)
	{
		expressionList.add(expression);
	}

	/**
	 * @param astVisitor Type checker
	 * @return the type of the set expression
	 * @throws Exception if the expression is not valid throws an exception
	 */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitSetExpression(this);	
	}
	
}
