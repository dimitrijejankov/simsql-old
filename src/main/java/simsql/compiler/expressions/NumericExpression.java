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
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;
import simsql.runtime.DoubleType;
import simsql.runtime.IntType;


/**
 * Represents a numeric expression in the AST
 */
public class NumericExpression extends MathExpression {

    /**
     * The value of the numeric expression
     */
    @JsonProperty("value")
    public double value;

    /**
     * true if the numeric expression is an integer, false otherwise
     */
    @JsonProperty("is-integer")
    private boolean isInteger;

    @JsonCreator
    public NumericExpression(@JsonProperty("value") String valueString) {
        value = new Double(valueString);
        isInteger = !valueString.contains(".");
    }

    @Override
    public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitNumericExpression(this);
    }

    /**
     * Returns the data type of the numeric expression
     * @return the data type (IntType or DoubleType)
     */
    @JsonIgnore
    public DataType getType() {
        return isInteger ? new IntType() : new DoubleType();
    }

    /**
     * Returns the string representation of the numeric expression
     * @return integer form if integer double otherwise
     */
    @Override
    public String toString() {
        return isInteger ?  Integer.toString((int)value) : Double.toString(value);
    }
}
