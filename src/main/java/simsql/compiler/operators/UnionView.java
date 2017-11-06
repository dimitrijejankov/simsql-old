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

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CommonContent;
import simsql.compiler.CopyHelper;
import simsql.compiler.VGFunctionStatistics;
import simsql.compiler.expressions.MathExpression;
import simsql.compiler.math_operators.MathOperator;
import simsql.runtime.DataType;


/**
 * This union supports union the tables in different time ticks.
 * Since the Optimizer written by Foula does not support the union operator, here we use VGWrapper to simulate
 * the existence of union operator.
 * The structure of the UnionView follows the following structure:
 * ***************************************************************************
 * parent(nodeName, elementNodeName0).
 * parent(nodeName, elementNodeName1).
 * parent(nodeName, elementNodeName2).
 * ...
 * parent(nodeName, elementNodeName_n).
 * node(nodeName, unionVGWrapper, unionVGFunction, element0_seed,
 * [element0.attribute0, .. element_n.attribute0], [],
 * [element0.attribute0, .. element_n.attribute0],
 * [union.outAttributeList],
 * unionVGWrapper_seed2).
 * ***************************************************************************
 * The first table provides the seed and one link to the VGWrapper,
 * while the others are linked to the VGWrapper directly.
 */
public class UnionView extends VGWrapper {
    //For intelligently handle the dynamic GeneralTableNameArray.
    /*
     * The first three parameters is used to record the schema. After the UnionViewTypeChecker, the
	 * tableAttributeList should not be null (empty).
	 */
    @JsonProperty("view-name")
    private String viewName;

    // The inputName list and outputName list exists in the VGWrapper.
    @JsonProperty("input-attribute-type-list")
    private ArrayList<DataType> inputAttributeTypeList;

    @JsonProperty("output-attribute-type-list")
    private ArrayList<DataType> outputAttributeTypeList;

    @JsonProperty("is-aligned")
    private boolean isAligned;

    /**
     * elementName: used to record the prefix of the element name.
     * constantTableList: used to record the constant table name.
     * individualGeneralIndexList: This is used to record the GeneralNameTable.
     * generalTableBoundList: (value0, value1), (value2, value3)..... The total number of elements in
     * this arrayList should be even, that every two elements define an interval.
     * interval.
     */
    @JsonProperty("element-name")
    private String elementName;

    @JsonProperty("constant-table-list")
    private ArrayList<String> constantTableList;

    @JsonProperty("individual-general-index-operator-list")
    private ArrayList<MathOperator> individualGeneralIndexOperatorList;

    @JsonProperty("individual-general-index-expression-list")
    private ArrayList<MathExpression> individualGeneralIndexExpressionList;

    @JsonProperty("general-table-bound-operator-list")
    private ArrayList<MathOperator> generalTableBoundOperatorList;

    @JsonProperty("general-table-bound-expression-list")
    private ArrayList<MathExpression> generalTableBoundExpressionList;

    /**
     * This is used to deserialize an UnionView
     */
    @JsonCreator
    public UnionView(@JsonProperty("node-name") String nodeName,
                     @JsonProperty("children") ArrayList<Operator> children,
                     @JsonProperty("parents") ArrayList<Operator> parents,
                     @JsonProperty("view-name") String viewName,
                     @JsonProperty("input-attribute-type-list") ArrayList<DataType> inputAttributeTypeList,
                     @JsonProperty("output-attribute-type-list") ArrayList<DataType> outputAttributeTypeList,
                     @JsonProperty("is-aligned") boolean isAligned,
                     @JsonProperty("element-name") String elementName,
                     @JsonProperty("constant-table-list") ArrayList<String> constantTableList,
                     @JsonProperty("individual-general-index-operator-list") ArrayList<MathOperator> individualGeneralIndexOperatorList,
                     @JsonProperty("individual-general-index-expression-list") ArrayList<MathExpression> individualGeneralIndexExpressionList,
                     @JsonProperty("general-table-bound-operator-list") ArrayList<MathOperator> generalTableBoundOperatorList,
                     @JsonProperty("general-table-bound-expression-list") ArrayList<MathExpression> generalTableBoundExpressionList) {
        super(nodeName, children, parents);
        this.viewName = viewName;
        this.inputAttributeTypeList = inputAttributeTypeList;
        this.outputAttributeTypeList = outputAttributeTypeList;
        this.isAligned = isAligned;
        this.elementName = elementName;
        this.constantTableList = constantTableList;
        this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
        this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
        this.generalTableBoundOperatorList = generalTableBoundOperatorList;
        this.generalTableBoundExpressionList = generalTableBoundExpressionList;
    }

    public UnionView(String nodeName,
                     ArrayList<Operator> children,
                     ArrayList<Operator> parents,
                     String vgWrapperName,
                     String vgFunctionName,
                     String inputSeedAttributeName,
                     String outputSeedAttributeName,
                     Operator outerRelationOperator,
                     VGFunctionStatistics vgStatistics,
                     String viewName,
                     ArrayList<DataType> inputAttributeTypeList,
                     ArrayList<DataType> outputAttributeTypeList,
                     boolean isAligned,
                     String elementName,
                     ArrayList<String> constantTableList,
                     ArrayList<MathOperator> individualGeneralIndexOperatorList,
                     ArrayList<MathExpression> individualGeneralIndexExpressionList,
                     ArrayList<MathOperator> generalTableBoundOperatorList,
                     ArrayList<MathExpression> generalTableBoundExpressionList) {
        super(nodeName,
                children,
                parents,
                vgWrapperName,
                vgFunctionName,
                inputSeedAttributeName,
                outputSeedAttributeName,
                outerRelationOperator,
                vgStatistics);
        this.viewName = viewName;
        this.inputAttributeTypeList = inputAttributeTypeList;
        this.outputAttributeTypeList = outputAttributeTypeList;
        this.isAligned = isAligned;
        this.elementName = elementName;
        this.constantTableList = constantTableList;
        this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
        this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
        this.generalTableBoundOperatorList = generalTableBoundOperatorList;
        this.generalTableBoundExpressionList = generalTableBoundExpressionList;
    }

    public ArrayList<DataType> getOutputAttributeTypeList() {
        return outputAttributeTypeList;
    }

    public void setOutputAttributeTypeList(ArrayList<DataType> outputAttributeTypeList) {
        this.outputAttributeTypeList = outputAttributeTypeList;
    }


    public ArrayList<DataType> getInputAttributeTypeList() {
        return inputAttributeTypeList;
    }

    public void setInputAttributeTypeList(ArrayList<DataType> inputAttributeTypeList) {
        this.inputAttributeTypeList = inputAttributeTypeList;
    }

    public String getElementName() {
        return elementName;
    }

    public void setElementName(String elementName) {
        this.elementName = elementName;
    }

    public ArrayList<String> getConstantTableList() {
        return constantTableList;
    }

    public void setConstantTableList(ArrayList<String> constantTableList) {
        this.constantTableList = constantTableList;
    }

    public String getViewName() {
        return viewName;
    }

    public void setViewName(String viewName) {
        this.viewName = viewName;
    }

    public boolean isAligned() {
        return isAligned;
    }

    public void setAligned(boolean isAlignd) {
        this.isAligned = isAlignd;
    }


    public ArrayList<MathOperator> getIndividualGeneralIndexOperatorList() {
        return individualGeneralIndexOperatorList;
    }

    public void setIndividualGeneralIndexOperatorList(
            ArrayList<MathOperator> individualGeneralIndexOperatorList) {
        this.individualGeneralIndexOperatorList = individualGeneralIndexOperatorList;
    }

    public ArrayList<MathExpression> getIndividualGeneralIndexExpressionList() {
        return individualGeneralIndexExpressionList;
    }

    public void setIndividualGeneralIndexExpressionList(
            ArrayList<MathExpression> individualGeneralIndexExpressionList) {
        this.individualGeneralIndexExpressionList = individualGeneralIndexExpressionList;
    }

    public ArrayList<MathOperator> getGeneralTableBoundOperatorList() {
        return generalTableBoundOperatorList;
    }

    public void setGeneralTableBoundOperatorList(
            ArrayList<MathOperator> generalTableBoundOperatorList) {
        this.generalTableBoundOperatorList = generalTableBoundOperatorList;
    }

    public ArrayList<MathExpression> getGeneralTableBoundExpressionList() {
        return generalTableBoundExpressionList;
    }

    public void setGeneralTableBoundExpressionList(
            ArrayList<MathExpression> generalTableBoundExpressionList) {
        this.generalTableBoundExpressionList = generalTableBoundExpressionList;
    }

    @Override
    @JsonIgnore
    public ArrayList<String> getOutputAttributeNames() {
        ArrayList<String> outputAttributes = new ArrayList<>(outputAttributeNameList);
        outputAttributes.add(outputSeedAttributeName);
        return outputAttributes;
    }

    /**
     * Returns the type enumeration of the operator
     * @return returns the type
     */
    @JsonIgnore
    public OperatorType getOperatorType() {
        return OperatorType.UNION_VIEW;
    }


    @Override
    public String visitNode() throws Exception {

        StringBuilder result = new StringBuilder();
        result.append(this.getNodeStructureString());

        result.append("vgwrapper(");
        //nodeName
        result.append(this.getNodeName());
        result.append(", ");
        //vgWrapperName
        result.append(vgWrapperName);
        result.append(", ");
        //vgFunctionName
        result.append(vgFunctionName.toLowerCase());
        result.append(", ");
        //inputSeeds
        result.append(inputSeedAttributeName);
        result.append(", ");

        //?
        result.append("[");

        if (inputAttributeNameList != null) {
            for (int i = 0; i < inputAttributeNameList.size(); i++) {
                result.append(inputAttributeNameList.get(i));

                if (i != inputAttributeNameList.size() - 1) {
                    result.append(", ");
                }
            }
        }

        result.append("], ");

        //?
        result.append("[]");
        result.append(", ");

        result.append("[");
        if (inputAttributeNameList != null) {
            for (int i = 0; i < inputAttributeNameList.size(); i++) {
                result.append(inputAttributeNameList.get(i));

                if (i != inputAttributeNameList.size() - 1) {
                    result.append(", ");
                }
            }
        }
        result.append("], [");

        if (outputAttributeNameList != null) {
            for (int i = 0; i < outputAttributeNameList.size(); i++) {
                result.append(outputAttributeNameList.get(i));

                if (i != outputAttributeNameList.size() - 1) {
                    result.append(", ");
                }
            }
        }
        result.append("], ");

        result.append(outputSeedAttributeName);
        result.append(").\r\n");

        result.append("outerRelation(");
        result.append(this.getNodeName()).append(", ").append(outerRelationOperator.getNodeName());
        result.append(").\r\n");

		/*
		 * ------------------------vgWrapper properties----------------------------
		 */
        result.append("vgwrapperproperties(");
        result.append(vgWrapperName);
        result.append(", bundlesPerTuple(");
        result.append(1);
        result.append("), [");

        String temp;

        ArrayList<String> outputRandomNameList = new ArrayList<>();

        for (int i = 0; i < outputAttributeNameList.size(); i++) {
            temp = "domain(" + outputAttributeNameList.get(i);
            temp += ", ";
            temp += "nothing";
            temp += ", ";
            temp += "infinite";
            temp += ")";

            result.append(temp);

            if (i != outputAttributeNameList.size() - 1) {
                result.append(", ");
            }
        }

        result.append("], [");
        int attributeSize;
        DataType type;
        for (int i = 0; i < outputAttributeNameList.size(); i++) {
            type = outputAttributeTypeList.get(i);
            attributeSize = type.getSizeInBytes();

            temp = "attributeSize(" + outputAttributeNameList.get(i);
            temp += ", ";
            temp += attributeSize;
            temp += ")";

            result.append(temp);

            if (i != outputAttributeNameList.size() - 1) {
                result.append(", ");
            }
        }
        result.append("]).\r\n");

        //-------------add the deterministic attribute

        if (outputRandomNameList.size() >= 1) {
            result.append("randomAttrsRelation(");
            result.append(this.getNodeName());
            result.append(", [");

            for (int i = 0; i < outputRandomNameList.size(); i++) {
                result.append(outputRandomNameList.get(i));

                if (i != outputRandomNameList.size() - 1) {
                    result.append(", ");
                }
            }

            result.append("]).\r\n");
        }

        return result.toString();
    }

    @Override
    public Operator copy(CopyHelper copyHelper) throws Exception {
        if (copyHelper.getCopiedMap().containsKey(getNodeName())) {
            return copyHelper.getCopiedMap().get(getNodeName());
        }

        CommonContent commonContent = copyHelper.copyBasicOperator(this);

        String c_vgWrapperName = vgWrapperName;
        String c_vgFunctionName = vgFunctionName;
        String c_inputSeedAttributeName = inputSeedAttributeName;
        ArrayList<String> c_inputAttributeNameList = copyHelper.copyStringList(inputAttributeNameList);
        ArrayList<String> c_outputAttributeNameList = copyHelper.copyStringList(outputAttributeNameList);
        String c_outputSeedAttributeName = outputSeedAttributeName;

        int outIndex = getChildren().indexOf(outerRelationOperator);

        Operator c_outerRelationOperator = commonContent.children.get(outIndex);

        String vgFunctionName = vgStatistics.getVfFunctionName();
        String directory = vgStatistics.getDirectory();
        int bundlesPerTuple = vgStatistics.getBundlesPerTuple();

        VGFunctionStatistics c_vgStatistics = new VGFunctionStatistics(vgFunctionName,
                directory,
                bundlesPerTuple,
                c_inputAttributeNameList,
                c_outputAttributeNameList);

        String c_viewName = viewName;

        ArrayList<DataType> c_inputAttributeTypeList = copyHelper.copyDataTypeList(inputAttributeTypeList);
        ArrayList<DataType> c_outputAttributeTypeList = copyHelper.copyDataTypeList(outputAttributeTypeList);

        boolean c_isAlignd = isAligned;
        String c_elementName = elementName;
        ArrayList<String> c_constantTableList = copyHelper.copyStringList(constantTableList);
        ArrayList<MathOperator> c_individualGeneralIndexList = copyHelper.copyMathOperatorList(individualGeneralIndexOperatorList);
        ArrayList<MathOperator> c_generalTableBoundList = copyHelper.copyMathOperatorList(generalTableBoundOperatorList);

        UnionView uionViewOperator = new UnionView(commonContent.nodeName,
                commonContent.children,
                commonContent.parents,
                c_vgWrapperName,
                c_vgFunctionName,
                c_inputSeedAttributeName,
                c_outputSeedAttributeName,
                c_outerRelationOperator,
                c_vgStatistics,
                c_viewName,
                c_inputAttributeTypeList,
                c_outputAttributeTypeList,
                c_isAlignd,
                c_elementName,
                c_constantTableList,
                c_individualGeneralIndexList,
                individualGeneralIndexExpressionList,
                c_generalTableBoundList,
                generalTableBoundExpressionList);

        uionViewOperator.setInputAttributeNameList(c_inputAttributeNameList);
        uionViewOperator.setOutputAttributeNameList(c_outputAttributeNameList);

        uionViewOperator.setNameMap(commonContent.nameMap);
        uionViewOperator.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
        copyHelper.getCopiedMap().put(getNodeName(), uionViewOperator);

        ArrayList<Operator> children = uionViewOperator.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(uionViewOperator);
            }
        }

        return uionViewOperator;

    }

}
