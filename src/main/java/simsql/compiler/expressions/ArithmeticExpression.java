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

package simsql.compiler.expressions; // package mcdb.compiler.parser.expression.mathExpression;


import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;


/**
 * Represents the ArithmeticExpression in an AST
 */
public class ArithmeticExpression extends MathExpression {

    /**
     * The list of operands in the arithmetic expression
     */
    @JsonProperty("operand-list")
    public ArrayList<MathExpression> operandList;

    /**
     * The list of operators in the arithmetic expression
     */
    @JsonProperty("operator-list")
    public ArrayList<Integer> operatorList;

    @JsonCreator
    public ArithmeticExpression(@JsonProperty("operand-list") ArrayList<MathExpression> operandList,
                                @JsonProperty("operator-list") ArrayList<Integer> operatorList) {
        super();
        this.operandList = operandList;
        this.operatorList = operatorList;
    }

    /**
     * Inserts a new operator to the arithmetic expression
     *
     * @param operator the operator to be added
     */
    public void addOperator(int operator) {
        operatorList.add(operator);
    }

    /**
     * Adds a math expression to the arithmetic expression
     *
     * @param expression the expression to be added
     */
    public void addMathExpression(MathExpression expression) {
        operandList.add(expression);
    }

    /**
     * @param astVisitor Type checker
     * @return the type of the arithmetic expression
     * @throws Exception if the expression is not valid throws an exception
     */
    public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitArithmeticExpression(this);
    }
}
