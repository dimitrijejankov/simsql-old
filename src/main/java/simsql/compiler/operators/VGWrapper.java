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
import java.util.ArrayList;


/**
 * The class that represents the VGWrapper in the logical query plan
 */
public class VGWrapper extends Operator {

    /**
     * The name of the vg wrapper
     */
    @JsonProperty("vg-wrapper-name")
    protected String vgWrapperName;

    /**
     * The name of the vg function
     */
    @JsonProperty("relation-statistics")
    protected String vgFunctionName;

    /**
     * The name of the input seed attribute
     */
    @JsonProperty("input-seed-attribute-name")
    protected String inputSeedAttributeName;

    /**
     * List of input attributes
     */
    @JsonProperty("input-attribute-name-list")
    protected ArrayList<String> inputAttributeNameList;

    /**
     * List of the output attributes
     */
    @JsonProperty("output-attribute-name-list")
    protected ArrayList<String> outputAttributeNameList;

    /**
     * The name of the output seed attribute
     */
    @JsonProperty("output-seed-attribute-name")
    protected String outputSeedAttributeName;

    /**
     * Outer relation operator
     */
    @JsonProperty("outer-relation-operator")
    protected Operator outerRelationOperator;

    /**
     * Here vgStatistics share the inputAttributeNameList and outputAttributeNameList
     */
    @JsonProperty("vg-statistics")
    protected VGFunctionStatistics vgStatistics;

    /**
     * A reference to the catalog
     */
    @JsonIgnore
    protected Catalog catalog = SimsqlCompiler.catalog;

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public VGWrapper(@JsonProperty("node-name") String nodeName,
                     @JsonProperty("children") ArrayList<Operator> children,
                     @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }


    /**
     * @param nodeName                the name of the operator
     * @param children                the children of the operator
     * @param parents                 the parent operators
     * @param vgWrapperName           the name of the vg wrapper
     * @param vgFunctionName          the name of the vg function
     * @param inputSeedAttributeName  the name of the input seed attribute
     * @param outputSeedAttributeName the name of the output seed attribute
     * @param outerRelationOperator   Outer relation operator
     * @param vgStatistics            vgStatistics share the inputAttributeNameList and outputAttributeNameList
     */
    public VGWrapper(String nodeName,
                     ArrayList<Operator> children,
                     ArrayList<Operator> parents,
                     String vgWrapperName,
                     String vgFunctionName,
                     String inputSeedAttributeName,
                     String outputSeedAttributeName,
                     Operator outerRelationOperator,
                     VGFunctionStatistics vgStatistics) {
        super(nodeName, children, parents);
        this.vgWrapperName = vgWrapperName;
        this.vgFunctionName = vgFunctionName;
        this.inputSeedAttributeName = inputSeedAttributeName;
        this.outputSeedAttributeName = outputSeedAttributeName;
        this.outerRelationOperator = outerRelationOperator;
        this.vgStatistics = vgStatistics;

        catalog = SimsqlCompiler.catalog;
    }

    /**
     * @ return the name of the VG Wrapper
     */
    public String getVgWrapperName() {
        return vgWrapperName;
    }

    /**
     * @param vgWrapperName sets the name of the vg wrapper
     */
    public void setVgWrapperName(String vgWrapperName) {
        this.vgWrapperName = vgWrapperName;
    }

    /**
     * @return the name of the vg function name
     */
    public String getVgFunctionName() {
        return vgFunctionName;
    }

    /**
     * @param vgFunctionName sets the vg function name
     */
    public void setVgFunctionName(String vgFunctionName) {
        this.vgFunctionName = vgFunctionName;
    }

    /**
     * @return the input seed attribute name
     */
    public String getInputSeedAttributeName() {
        return inputSeedAttributeName;
    }

    /**
     * @param inputSeedAttributeName sets the input seed variable name
     */
    public void setInputSeedAttributeName(String inputSeedAttributeName) {
        this.inputSeedAttributeName = inputSeedAttributeName;
    }

    /**
     * @return the attribute name list
     */
    public ArrayList<String> getInputAttributeNameList() {
        return inputAttributeNameList;
    }

    /**
     * @param inputAttributeNameList sets the attribute name list
     */
    public void setInputAttributeNameList(ArrayList<String> inputAttributeNameList) {
        this.inputAttributeNameList = inputAttributeNameList;
    }

    /**
     * @return returns the output attribute name list
     */
    public ArrayList<String> getOutputAttributeNameList() {
        return outputAttributeNameList;
    }

    /**
     * @param outputAttributeNameList sets the output attribute list
     */
    public void setOutputAttributeNameList(ArrayList<String> outputAttributeNameList) {
        this.outputAttributeNameList = outputAttributeNameList;
    }

    /**
     * @return outputSeedAttributeName the output attribute name list
     */
    public String getOutputSeedAttributeName() {
        return outputSeedAttributeName;
    }

    /**
     * @param outputSeedAttributeName sets the output attribute name list
     */
    public void setOutputSeedAttributeName(String outputSeedAttributeName) {
        this.outputSeedAttributeName = outputSeedAttributeName;
    }

    /**
     * @return outer relation operator
     */
    public Operator getOuterRelationOperator() {
        return outerRelationOperator;
    }

    /**
     * @param outerRelationOperator sets the outer relation operator
     */
    public void setOuterRelationOperator(Operator outerRelationOperator) {
        this.outerRelationOperator = outerRelationOperator;
    }

    /**
     * @return returns all the output attributes wit the addition of the output seed attribute
     */
    @JsonIgnore
    public ArrayList<String> getGeneratedNameList() {
        ArrayList<String> resultList = new ArrayList<String>();

        if (outputAttributeNameList != null) {
            for (String attributeName : outputAttributeNameList) {
                resultList.add(attributeName);
            }
        }

        resultList.add(outputSeedAttributeName);
        return resultList;
    }

    /**
     * @return returns the vg function statistics
     */
    public VGFunctionStatistics getVgStatistics() {
        return vgStatistics;
    }

    /**
     * @param vgStatistics sets the vg function statistics
     */
    public void setVgStatistics(VGFunctionStatistics vgStatistics) {
        this.vgStatistics = vgStatistics;
    }

    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() throws Exception {

        String result = "";
        result += this.getNodeStructureString();

        result += "vgwrapper(";
        //nodeName
        result += this.getNodeName();
        result += ", ";
        //vgWrapperName
        result += vgWrapperName;
        result += ", ";
        //vgFunctionName
        result += vgFunctionName.toLowerCase();
        result += ", ";
        //inputSeeds
        result += inputSeedAttributeName;
        result += ", ";

        //?
        result += "[";

        if (inputAttributeNameList != null) {
            for (int i = 0; i < inputAttributeNameList.size(); i++) {
                result += inputAttributeNameList.get(i);

                if (i != inputAttributeNameList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], ";

        //?
        result += "[]";
        result += ", ";

        result += "[";
        if (inputAttributeNameList != null) {
            for (int i = 0; i < inputAttributeNameList.size(); i++) {
                result += inputAttributeNameList.get(i);

                if (i != inputAttributeNameList.size() - 1) {
                    result += ", ";
                }
            }
        }
        result += "], [";

        if (outputAttributeNameList != null) {
            for (int i = 0; i < outputAttributeNameList.size(); i++) {
                result += outputAttributeNameList.get(i);

                if (i != outputAttributeNameList.size() - 1) {
                    result += ", ";
                }
            }
        }
        result += "], ";

        result += outputSeedAttributeName;
        result += ").\r\n";

        result += "outerRelation(";
        result += this.getNodeName() + ", " + outerRelationOperator.getNodeName();
        result += ").\r\n";

		/*
         * ------------------------vgWrapper properties----------------------------
		 */
        result += "vgwrapperproperties(";
        result += vgWrapperName;
        result += ", bundlesPerTuple(";
        result += vgStatistics.getBundlesPerTuple();
        result += "), [";

        String temp;

        VGFunction vgf = catalog.getVGFunction(vgFunctionName);
        ArrayList<Attribute> attributeList = vgf.getOutputAtts();
        Attribute tempAttribute;

        ArrayList<String> outputRandomNameList = new ArrayList<String>();

        for (int i = 0; i < outputAttributeNameList.size(); i++) {
            tempAttribute = attributeList.get(i);

            // note that now we just ask the attribute if it is random
            if (tempAttribute.getIsRandom()) {
                outputRandomNameList.add(outputAttributeNameList.get(i));
            }
        }

        for (int i = 0; i < outputAttributeNameList.size(); i++) {
            tempAttribute = attributeList.get(i);

            temp = "domain(" + outputAttributeNameList.get(i);
            temp += ", ";
            temp += tempAttribute.getDomainType();
            temp += ", ";
            temp += tempAttribute.getDomainSize();
            temp += ")";

            result += temp;

            if (i != outputAttributeNameList.size() - 1) {
                result += ", ";
            }
        }

        result += "], [";
        int attributeSize;
        for (int i = 0; i < outputAttributeNameList.size(); i++) {
            tempAttribute = attributeList.get(i);
            attributeSize = tempAttribute.getAttributeSize();

            temp = "attributeSize(" + outputAttributeNameList.get(i);
            temp += ", ";
            temp += attributeSize;
            temp += ")";

            result += temp;

            if (i != outputAttributeNameList.size() - 1) {
                result += ", ";
            }
        }
        result += "]).\r\n";

        //-------------add the deterministic attribute

        if (outputRandomNameList.size() >= 1) {
            result += "randomAttrsRelation(";
            result += this.getNodeName();
            result += ", [";

            for (int i = 0; i < outputRandomNameList.size(); i++) {
                result += outputRandomNameList.get(i);

                if (i != outputRandomNameList.size() - 1) {
                    result += ", ";
                }
            }

            result += "]).\r\n";
        }

        return result;
    }

    /**
     * @param copyHelper an instance of the copy helper class
     * @return the deep copy of an operator
     * @throws Exception if the operation fails
     */
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

        VGWrapper vgWrapper = new VGWrapper(commonContent.nodeName,
                commonContent.children,
                commonContent.parents,
                c_vgWrapperName,
                c_vgFunctionName,
                c_inputSeedAttributeName,
                c_outputSeedAttributeName,
                c_outerRelationOperator,
                c_vgStatistics);

        vgWrapper.setInputAttributeNameList(c_inputAttributeNameList);
        vgWrapper.setOutputAttributeNameList(c_outputAttributeNameList);
        vgWrapper.setNameMap(commonContent.nameMap);
        vgWrapper.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        copyHelper.getCopiedMap().put(getNodeName(), vgWrapper);

        ArrayList<Operator> children = vgWrapper.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(vgWrapper);
            }
        }
        return vgWrapper;
    }

}
