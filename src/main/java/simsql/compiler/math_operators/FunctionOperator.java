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
import simsql.compiler.TranslatorHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Represents the function operator
 */
public class FunctionOperator implements MathOperator {

    /**
     * The name of the function operator
     */
    @JsonProperty("name")
    private String name;

    /**
     * the name of the function
     */
    @JsonProperty("function-name")
    private String functionName;

    /**
     * the list of parameters
     */
    @JsonProperty("parameter-list")
    private ArrayList<MathOperator> parameterList;

    /**
     * the list of types
     */
    @JsonProperty("type-list")
    private ArrayList<String> typeList;

    @JsonCreator
    public FunctionOperator(@JsonProperty("name") String name,
                            @JsonProperty("function-name") String functionName,
                            @JsonProperty("parameter-list") ArrayList<MathOperator> parameterList,
                            @JsonProperty("type-list") ArrayList<String> typeList) {
        super();
        this.name = name;
        this.functionName = functionName;
        this.parameterList = parameterList;
        this.typeList = typeList;
    }

    /**
     * returns the name of the operator
     * @return name of the operator
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the operator
     * @param name the new name of the operator
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * gets the name of the function from the operator
     * @return the name of the function this operator uses
     */
    public String getFunctionName() {
        return functionName;
    }

    /**
     * sets the name of the function this operator is usign
     * @param functionName the new name of the function
     */
    public void setFunctionName(String functionName) {
        this.functionName = functionName;
    }

    /**
     * returns the list of parameters
     * @return the list of parameters
     */
    public ArrayList<MathOperator> getParameterList() {
        return parameterList;
    }

    /**
     * sets the parameter list of this function
     * @param parameterList the new parameter list
     */
    public void setParameterList(ArrayList<MathOperator> parameterList) {
        this.parameterList = parameterList;
    }


    /**
     * @see simsql.compiler.math_operators.MathOperator#changeProperty(HashMap, TranslatorHelper)
     */
    @Override
    public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        // set the name
        this.setName("arithExp" + translatorHelper.getArithExpIndex());

        // change the properties of each parameter
        for (MathOperator tempOperator : parameterList) {
            tempOperator.changeProperty(indices, translatorHelper);
        }
    }

    /**
     * Returns the string representation of this operation for the relationship statistics
     * @return the string representation
     */
    @Override
    public String visitNode() {
        String result = "";

        result += "function(";
        result += name;
        result += ", ";
        result += functionName;
        result += ", [";

        if (parameterList != null) {
            for (int i = 0; i < parameterList.size(); i++) {
                result += "[" + parameterList.get(i).getNodeName() + ", " + typeList.get(i) + "]";
                if (i != parameterList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "]).\r\n";

        if (parameterList != null) {
            for (MathOperator aParameterList : parameterList) {
                result += aParameterList.visitNode();
            }
        }

        return result;
    }

    /**
     * Returns the name of this node (set in the constructor)
     * @return the name of the node
     */
    @Override
    public String getNodeName() {
        return name;
    }

    /**
     * returns the list of the types for the function parameters
     * @return the typeList the list of the types
     */
    public ArrayList<String> getTypeList() {
        return typeList;
    }

    /**
     * Copies the function operator
     * @param copyHelper the operator to be copied
     * @return the copy
     */
    public MathOperator copy(CopyHelper copyHelper) {
        String c_name = new String(name);
        String c_functionName = new String(functionName);
        ArrayList<MathOperator> c_parameterList = copyHelper.copyMathOperatorList(parameterList);
        ArrayList<String> c_typeList = copyHelper.copyStringList(typeList);

        FunctionOperator functionOperator = new FunctionOperator(c_name,
                c_functionName,
                c_parameterList,
                c_typeList);

        return functionOperator;
    }

}
