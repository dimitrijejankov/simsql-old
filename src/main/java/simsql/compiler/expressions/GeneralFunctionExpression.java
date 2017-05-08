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

import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;


/**
 * Represents a general function expression in the AST
 */
public class GeneralFunctionExpression extends MathExpression {

    /**
     * The name of the function
     */
    @JsonProperty("function-name")
    public String functionName;

    /**
     * The list of parameters
     */
    @JsonProperty("paras-list")
    public ArrayList<MathExpression> parasList;

    /**
     * The list of output types
     */
    @JsonProperty("out-type-list")
    private ArrayList<DataType> outTypeList;

    /**
     * If this is a VGFunction this flag is true, false otherwise
     */
    @JsonProperty("is-vg")
    private boolean isVGFunctionVal = false;

    public GeneralFunctionExpression(@JsonProperty("function-name") String functionName,
                                     @JsonProperty("paras-list") ArrayList<MathExpression> parasList) {
        super();
        this.functionName = functionName.toLowerCase();
        this.parasList = parasList;
    }

    /**
     * Checks if this is a VGFunction
     *
     * @return true if it is, false otherwise
     */
    public boolean isVGFunction() {
        return isVGFunctionVal;
    }

    /**
     * Marks if this is a VGFunction
     *
     * @param toMe true if it is, false otherwise
     */
    public void setVGFunctionCall(boolean toMe) {
        isVGFunctionVal = toMe;
    }

    /**
     * Adds a parameter to the function
     *
     * @param expression the math expression that is the new parameter
     */
    public void addParameter(MathExpression expression) {
        parasList.add(expression);
    }

    /**
     * @param astVisitor Type checker
     * @return the types of the function expression in order
     * @throws Exception if the expression is not valid throws an exception
     */
    @Override
    public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitGeneralFunctionExpression(this);
    }

    /**
     * sets the output type list
     *
     * @param typeList the typeList to set
     */
    public void setOutTypeList(ArrayList<DataType> typeList) {
        this.outTypeList = typeList;
    }
}
