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

package simsql.compiler.boolean_operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CopyHelper;
import simsql.compiler.FinalVariable;
import simsql.compiler.math_operators.MathOperator;

/**
 * The comparator operator
 */
public class CompOperator extends BooleanOperator {

    /**
     * The type of the comparator
     * FinalVariable.EQUALS
     * FinalVariable.NOTEQUALS
     * FinalVariable.LESSTHAN
     * FinalVariable.GREATERTHAN
     * FinalVariable.LESSEQUAL
     * FinalVariable.GREATEREQUAL
     */
    @JsonProperty("type")
    private int type;

    /**
     * the left side of the comparator
     */
    @JsonProperty("left")
    private MathOperator left;

    /**
     * the right side of the comparator
     */
    @JsonProperty("right")
    private MathOperator right;

    /**
     * the type of the left side in string
     */
    @JsonProperty("left-type")
    private String leftType;

    /**
     * the type of the right side in string
     */
    @JsonProperty("right-type")
    private String rightType;

    public CompOperator(String name) {
        super(name);
    }

    @JsonCreator
    public CompOperator(@JsonProperty("name") String name,
                        @JsonProperty("type") int type,
                        @JsonProperty("left") MathOperator left,
                        @JsonProperty("right") MathOperator right,
                        @JsonProperty("left-type") String leftType,
                        @JsonProperty("right-type") String rightType) {
        super(name);
        this.type = type;
        this.left = left;
        this.right = right;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    /**
     * Returns the type of the comparator
     * @return type of the comparator
     */
    public int getType() {
        return type;
    }

    /**
     * Sets the type of the the comparator
     * @param type the new type of the comparator
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * Gets the left operator
     * @return the left operator
     */
    public MathOperator getLeft() {
        return left;
    }

    /**
     * Sets the left operator
     * @param left the new left operator
     */
    public void setLeft(MathOperator left) {
        this.left = left;
    }

    /**
     * Gets the right operator
     * @return the right operator
     */
    public MathOperator getRight() {
        return right;
    }

    /**
     * Sets the right operator
     * @param right the new right operator
     */
    public void setRight(MathOperator right) {
        this.right = right;
    }

    /**
     * Returns the type of the left operator
     * @return the type in string form
     */
    public String getLeftType() {
        return leftType;
    }

    /**
     * Sets the type of the left operator
     * @param leftType the new left operator
     */
    public void setLeftType(String leftType) {
        this.leftType = leftType;
    }

    /**
     * Returns the type of the right operator
     * @return the type in string form
     */
    public String getRightType() {
        return rightType;
    }

    /**
     * Sets the type of the right operator
     * @param rightType the new right operator
     */
    public void setRightType(String rightType) {
        this.rightType = rightType;
    }

    /**
     * Transforms the comparison operator type to it's string format
     * @param type ca be one of the following and yields:
     *             FinalVariable.EQUALS => "equal"
     *             FinalVariable.NOTEQUALS => "notequal"
     *             FinalVariable.LESSTHAN => "lessthan"
     *             FinalVariable.GREATERTHAN => "greaterthan'
     *             FinalVariable.LESSEQUAL => "lessequal"
     *             FinalVariable.GREATEREQUAL => "greaterequal"
     * @return the string
     */
    private String translateComparisonOperation(int type) {
        String result = "";
        switch (type) {
            case FinalVariable.EQUALS:
                result = "equals";
                break;

            case FinalVariable.NOTEQUALS:
                result = "notEquals";
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

    /**
     * @see simsql.compiler.boolean_operator.BooleanOperator#visitNode()
     */
    @Override
    public String visitNode() {
        String result = "compExp(" + this.getName();

        result += ", ";

        //2. Deal with the operation type
        result += translateComparisonOperation(type);
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

    /**
     * @see simsql.compiler.boolean_operator.BooleanOperator#copy(CopyHelper)
     */
    @Override
    public BooleanOperator copy(CopyHelper copyHelper) {
        String c_name = this.getName();
        int c_type = this.type;
        MathOperator c_left = left.copy(copyHelper);
        MathOperator c_right = right.copy(copyHelper);
        String c_leftType = leftType;
        String c_rightType = rightType;

        return new CompOperator(c_name, c_type, c_left, c_right, c_leftType, c_rightType);
    }

}
