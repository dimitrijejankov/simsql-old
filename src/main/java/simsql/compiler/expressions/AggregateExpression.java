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
import simsql.compiler.FinalVariable;
import simsql.runtime.DataType;


/**
 * Models the AggregateExpression in the AST.
 */
public class AggregateExpression extends MathExpression {

    /**
     * The type of the aggregate can be one of the following values from the FinalVariable class.
     * AVG, SUM, COUNT, MIN, MAX, VARIANCE, STDEV, VECTOR, ROWMATRIX, COLUMNMATRIX
     */
    @JsonProperty("agg-type")
    public int aggType;

    /**
     * ALL or DISTINCT from the FinalVariable class
     */
    @JsonProperty("quantifier")
    public int setQuantifier;

    /**
     * The math expression of the aggregate
     */
    @JsonProperty("expression")
    public MathExpression expression;

    public AggregateExpression(int aggType) {
        this(aggType, FinalVariable.ALL, null);
    }

    @JsonCreator
    public AggregateExpression(@JsonProperty("agg-type") int aggType,
                               @JsonProperty("quantifier") int setQuantifier,
                               @JsonProperty("expression") MathExpression expression) {
        super();
        this.aggType = aggType;
        this.setQuantifier = setQuantifier;
        this.expression = expression;
    }

    /**
     * @param astVisitor Type checker
     * @return the type of the aggregate expression
     * @throws Exception if the expression is not valid throws an exception
     */
    public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitAggregateExpression(this);
    }

}
