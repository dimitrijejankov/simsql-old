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

package simsql.compiler.operators;

import com.fasterxml.jackson.annotation.*;
import simsql.compiler.*;
import simsql.compiler.math_operators.DateOperator;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.math_operators.NumberOperator;
import simsql.compiler.math_operators.StringOperator;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The class that represents the ScalarFunction in the logical query plan
 */
public class ScalarFunction extends Operator {

    /**
     * List of scalar expressions
     */
    @JsonProperty("scalar-expression-list")
    private ArrayList<MathOperator> scalarExpressionList;

    /**
     *
     */
    @JsonIgnore
    private HashMap<MathOperator, ArrayList<String>> columnListMap;

    /**
     *
     */
    @JsonIgnore
    private HashMap<MathOperator, String> outputMap;

    /**
     * an instance of the translator helper function
     */
    @JsonIgnore
    private TranslatorHelper translatorHelper;

    /**
     * This is used for JSON deserialization
     *
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public ScalarFunction(@JsonProperty("node-name") String nodeName,
                          @JsonProperty("children") ArrayList<Operator> children,
                          @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @param nodeName         the name of the operator
     * @param children         the children of the operator
     * @param parents          the parent operators
     * @param translatorHelper an instance of the translator helper class
     */
    public ScalarFunction(String nodeName,
                          ArrayList<Operator> children,
                          ArrayList<Operator> parents,
                          TranslatorHelper translatorHelper) {
        super(nodeName, children, parents);
        this.translatorHelper = translatorHelper;
    }


    /**
     * @return gets the scalar expression list
     */
    public ArrayList<MathOperator> getScalarExpressionList() {
        return scalarExpressionList;
    }

    /**
     * @param scalarExpressionList sets the scalar expression list
     */
    public void setScalarExpressionList(ArrayList<MathOperator> scalarExpressionList) {
        this.scalarExpressionList = scalarExpressionList;
    }

    /**
     * @return returns the column list map
     */
    public HashMap<MathOperator, ArrayList<String>> getColumnListMap() {
        return columnListMap;
    }

    /**
     * @param columnListMap sets the column list map
     */
    public void setColumnListMap(
            HashMap<MathOperator, ArrayList<String>> columnListMap) {
        this.columnListMap = columnListMap;
    }

    /**
     * @return gets the output map
     */
    public HashMap<MathOperator, String> getOutputMap() {
        return outputMap;
    }

    /**
     * @param outputMap sets the output map
     */
    public void setOutputMap(HashMap<MathOperator, String> outputMap) {
        this.outputMap = outputMap;
    }

    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() {
        String result = "";

        ArrayList<String> constantArithList = new ArrayList<String>();
        String mathName;


        result += this.getNodeStructureString();

        result += "scalarfunc(" + this.getNodeName() + ", [";

        if (scalarExpressionList != null) {
            for (int i = 0; i < scalarExpressionList.size(); i++) {
                MathOperator tempOperator = scalarExpressionList.get(i);

                if (tempOperator instanceof NumberOperator) {
                    mathName = "arithExp" + translatorHelper.getArithExpIndex();
                    constantArithList.add("verbatim(" + mathName + ", " + tempOperator.getNodeName() + ", isNumeric).\r\n");
                    result += mathName;
                } else if (tempOperator instanceof DateOperator ||
                        tempOperator instanceof StringOperator) {
                    mathName = "arithExp" + translatorHelper.getArithExpIndex();
                    constantArithList.add("verbatim(" + mathName + ", " + tempOperator.getNodeName() + ", isNotNumeric).\r\n");
                    result += mathName;
                } else {
                    result += tempOperator.getNodeName();
                }

                if (i != scalarExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";

        if (scalarExpressionList != null) {
            for (int i = 0; i < scalarExpressionList.size(); i++) {
                MathOperator mathOperator = scalarExpressionList.get(i);

                ArrayList<String> element = columnListMap.get(mathOperator);
                result += this.getListString(element);

                if (i != scalarExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";

        if (scalarExpressionList != null) {
            for (int i = 0; i < scalarExpressionList.size(); i++) {
                MathOperator mathOperator = scalarExpressionList.get(i);

                String element = outputMap.get(mathOperator);
                result += "[";
                result += element;
                result += "]";

                if (i != scalarExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "]).\r\n";

        for (String aConstantArithList : constantArithList) {
            result += aConstantArithList;
        }

        if (scalarExpressionList != null) {
            for (MathOperator mathOperator : scalarExpressionList) {
                result += mathOperator.visitNode();
            }
        }

        return result;
    }

    @JsonIgnore
    public ArrayList<String> getGeneratedNameList() {
        ArrayList<String> resultList = new ArrayList<String>();

        if (scalarExpressionList != null) {
            for (MathOperator mathOperator : scalarExpressionList) {
                resultList.add(outputMap.get(mathOperator));
            }
        }
        return resultList;
    }

    /**
     * Returns the array list of column lists  - used for serialization
     *
     * @return the array list column lists
     */
    @JsonGetter("column-lists")
    public ArrayList<ColumnList> getColumnLists() {

        ArrayList<ColumnList> ret = new ArrayList<ColumnList>();

        // convert to array list
        for (MathOperator m : columnListMap.keySet()) {
            ret.add(new ColumnList(m, columnListMap.get(m)));
        }

        return ret;
    }

    /**
     * Sets the column map from it's inverted form - used for serialization
     *
     * @param invertedColumnMap the inverted column map
     */
    @JsonSetter("column-lists")
    public void setColumnLists(ArrayList<ColumnList> invertedColumnMap) {

        columnListMap = new HashMap<MathOperator, ArrayList<String>>();

        // invert the map and add it to to the empty output map
        for (ColumnList c : invertedColumnMap) {
            columnListMap.put(c.operator, c.columns);
        }
    }

    /**
     * Returns the inverted output map - used for serialization
     *
     * @return the inverted outputMap
     */
    @JsonGetter("inverted-output-map")
    public HashMap<String, MathOperator> getInvertedOutputMap() {

        HashMap<String, MathOperator> ret = new HashMap<String, MathOperator>();

        // invert the map
        for (MathOperator m : outputMap.keySet()) {
            ret.put(outputMap.get(m), m);
        }

        return ret;
    }

    /**
     * Sets the output map from it's inverted form - used for serialization
     *
     * @param invertedOutputMap the inverted output map
     */
    @JsonSetter("inverted-output-map")
    public void setInvertedOutputMap(HashMap<String, MathOperator> invertedOutputMap) {

        outputMap = new HashMap<MathOperator, String>();

        // invert the map and add it to to the empty output map
        for (String m : invertedOutputMap.keySet()) {
            outputMap.put(invertedOutputMap.get(m), m);
        }
    }

    /**
     * @see simsql.compiler.operators.Operator#changeNodeProperty(HashMap, TranslatorHelper)
     */
    public void changeNodeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        super.changeNodeProperty(indices, translatorHelper);

        // replace the indices with their respective values for each expression
        for (MathOperator tempOperator : scalarExpressionList) {
            tempOperator.changeProperty(indices, translatorHelper);
        }
    }

    /**
     * @param copyHelper an instance of the copy helper class
     * @return the deep copy of an operator
     * @throws Exception if the operation fails
     */
    public Operator copy(CopyHelper copyHelper) throws Exception {
        if (copyHelper.getCopiedMap().containsKey(getNodeName())) {
            return copyHelper.getCopiedMap().get(getNodeName());
        }

        CommonContent commonContent = copyHelper.copyBasicOperator(this);

        ArrayList<MathOperator> c_scalarExpressionList = copyHelper.copyMathOperatorList(scalarExpressionList);
        HashMap<MathOperator, ArrayList<String>> c_columnListMap = copyHelper.copyMathOperatorStringListMap(scalarExpressionList, c_scalarExpressionList, columnListMap);
        HashMap<MathOperator, String> c_outputMap = copyHelper.copyMathOperatorStringMap(scalarExpressionList, c_scalarExpressionList, outputMap);
        TranslatorHelper c_translatorHelper = this.translatorHelper;


        ScalarFunction scalarFunction = new ScalarFunction(commonContent.nodeName,
                commonContent.children,
                commonContent.parents,
                c_translatorHelper);

        scalarFunction.setNameMap(commonContent.nameMap);
        scalarFunction.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
        scalarFunction.setScalarExpressionList(c_scalarExpressionList);
        scalarFunction.setColumnListMap(c_columnListMap);
        scalarFunction.setOutputMap(c_outputMap);

        copyHelper.getCopiedMap().put(getNodeName(), scalarFunction);
        ArrayList<Operator> children = scalarFunction.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(scalarFunction);
            }
        }
        return scalarFunction;
    }

}
