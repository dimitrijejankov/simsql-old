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

package simsql.compiler.math_operators;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.TranslatorHelper;
import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.compiler.CopyHelper;

import java.util.HashMap;


/**
 * Wraps a boolean operator to a math operator
 */
public class PredicateToMathWrapper implements MathOperator {
    /**
     * The boolean operator to wrap
     */
    @JsonProperty("operator")
    public BooleanOperator operator;

    @JsonCreator
    public PredicateToMathWrapper(@JsonProperty("operator") BooleanOperator operator) {
        super();
        this.operator = operator;
    }

    /**
     * @see simsql.compiler.math_operators.MathOperator#changeProperty(HashMap, TranslatorHelper)
     */
    @Override
    public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {

        // if by any chance the operator is null
        if(operator == null) {
            return;
        }

        // change the property of the operator
        operator.changeProperty(indices, translatorHelper);
    }

    /**
     * returns the boolean operator string representation for relationship statistics
     *
     * @return the string representation
     */
    @Override
    public String visitNode() {
        return operator.visitNode();
    }


    /**
     * The name of the node - same as the wrapped boolean operator
     *
     * @return the name of the node as string
     */
    @Override
    public String getNodeName() {
        return operator.getName();
    }


    /**
     * returns the wrapped boolean operator
     *
     * @return the operator
     */
    public BooleanOperator getBooleanOperator() {
        return operator;
    }

    /**
     * set a new boolean operator to wrap
     *
     * @param operator the operator to set
     */
    public void setBooleanOperator(BooleanOperator operator) {
        this.operator = operator;
    }

    /**
     * Copies the boolean operator
     *
     * @param copyHelper the operator to be copied
     * @return the copy
     */
    @Override
    public MathOperator copy(CopyHelper copyHelper) {
        return new PredicateToMathWrapper(operator.copy(copyHelper));
    }

}
