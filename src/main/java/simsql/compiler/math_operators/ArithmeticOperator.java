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
import simsql.compiler.CopyHelper;
import simsql.compiler.FinalVariable;
import simsql.compiler.TranslatorHelper;

import java.util.HashMap;

/**
 * Represents an arithmetic math operator
 */
public class ArithmeticOperator implements MathOperator {

    /**
     * The name of this operator as a node
     */
    @JsonProperty("name")
    private String name;

    /**
     * the type of this arithmetic operator
     * can be : FinalVariable.PLUS FinalVariable.MINUS FinalVariable.DIVIDE FinalVariable.TIMES
     */
    @JsonProperty("type")
    private int type;

    /**
     * the left inputs to this operator
     */
    @JsonProperty("left")
    private MathOperator left;

    /**
     * the right input to this operator
     */
    @JsonProperty("right")
    private MathOperator right;

    /**
     * the type of the left input to this operator
     */
    @JsonProperty("left-type")
    private String leftType;

    /**
     * the type of the right input to this operator
     */
    @JsonProperty("right-type")
    private String rightType;

    @JsonCreator
    public ArithmeticOperator(@JsonProperty("name") String name,
                              @JsonProperty("type")int type,
                              @JsonProperty("left") MathOperator left,
                              @JsonProperty("right") MathOperator right,
                              @JsonProperty("left-type") String leftType,
                              @JsonProperty("right-type") String rightType) {
        super();
        this.name = name;
        this.type = type;
        this.left = left;
        this.right = right;
        this.leftType = leftType;
        this.rightType = rightType;
    }

    /**
     * returns the type of the arithmetic operator
     * @return can be : FinalVariable.PLUS or FinalVariable.MINUS or FinalVariable.DIVIDE or FinalVariable.TIMES
     */
    public int getType() {
        return type;
    }

    /**
     * sets the type of this operator
     * @param type - can be : FinalVariable.PLUS or FinalVariable.MINUS or FinalVariable.DIVIDE or FinalVariable.TIMES
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * returns the left operand of this operator
     * @return the operand
     */
    public MathOperator getLeft() {
        return left;
    }

    /**
     * sets the left operand of this arithmetic operator
     * @param left the new operand
     */
    public void setLeft(MathOperator left) {
        this.left = left;
    }

    /**
     * returns the right operand of this operator
     * @return the operand
     */
    public MathOperator getRight() {
        return right;
    }

    /**
     * sets the right operand of this arithmetic operator
     * @param right the new operand
     */
    public void setRight(MathOperator right) {
        this.right = right;
    }

    /**
     * returns the type of the left operand
     * @return the type in string form
     */
    public String getLeftType() {
        return leftType;
    }

    /**
     * sets the type of the left operand
     * @param leftType the type in string form
     */
    public void setLeftType(String leftType) {
        this.leftType = leftType;
    }

    /**
     * returns the type of the right operand
     * @return the type in string form
     */
    public String getRightType() {
        return rightType;
    }

    /**
     * sets the type of the right operand
     * @param rightType the type in string form
     */
    public void setRightType(String rightType) {
        this.rightType = rightType;
    }

    /**
     * returns the name of the arithmetic math operator
     * @return the name as set by the constructor or the setName method...
     */
    @Override
    public String getNodeName() {
        return name;
    }

    /**
     * sets the name of the arithmetic math operator
     * @param name the name string
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * translate the type to it's string representation
     *
     * for FinalVariable.PLUS => "plus"
     * for FinalVariable.MINUS => "minus"
     * for FinalVariable.DIVIDE => "divide"
     * for FinalVariable.TIMES => "times"
     *
     * @param type can be : FinalVariable.PLUS or FinalVariable.MINUS or FinalVariable.DIVIDE or FinalVariable.TIMES
     * @return the string representation
     */
    private String translateArithmeticOperation(int type) {
        String result = "";

        switch (type) {
            case FinalVariable.PLUS:
                result = "plus";
                break;

            case FinalVariable.MINUS:
                result = "minus";
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

    /**
     * @see simsql.compiler.math_operators.MathOperator#changeProperty(HashMap, TranslatorHelper)
     */
    @Override
    public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        // set the name
        this.setName("arithExp" + translatorHelper.getArithExpIndex());

        // change the properties of the left side
        left.changeProperty(indices, translatorHelper);

        // change the properties of the right side
        right.changeProperty(indices, translatorHelper);
    }

    /**
     * Returns the string representation of the arithmetic operator for the relation statistics
     * @return the string representation
     */
    @Override
    public String visitNode() {
        String result = "";
        result += "arithExp(" + name;
        result += ", ";

        result += translateArithmeticOperation(type);
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

    /**
     * Copies the arithmetic math operator
     * @param copyHelper the operator to be copied
     * @return the copy
     */
    public MathOperator copy(CopyHelper copyHelper) {
        String c_name = name;
        int c_type = this.type;
        MathOperator c_left = this.left.copy(copyHelper);
        MathOperator c_right = this.right.copy(copyHelper);
        String c_leftType = this.leftType;
        String c_rightType = this.rightType;

        return new ArithmeticOperator(c_name, c_type, c_left, c_right, c_leftType, c_rightType);
    }
}
