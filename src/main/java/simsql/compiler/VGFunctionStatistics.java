

/**
 * *
 * Copyright 2014 Rice University                                           *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                           *
 * *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 * *
 */


/**
 *
 */
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.statisticsOperator;

import java.util.ArrayList;

/**
 * @author Bamboo
 *
 */
public class VGFunctionStatistics implements StatisticsOperator {
    private String vgFunctionName;
    private String directory;
    private int bundlesPerTuple;
    private ArrayList<String> inputAttributes;
    private ArrayList<String> outputAttributes;

    public VGFunctionStatistics(String vgFunctionName,
                                String directory,
                                int bundlesPerTuple,
                                ArrayList<String> inputAttributes,
                                ArrayList<String> outputAttributes) {
        this.vgFunctionName = vgFunctionName;
        this.directory = directory;
        this.bundlesPerTuple = bundlesPerTuple;
        this.inputAttributes = inputAttributes;
        this.outputAttributes = outputAttributes;
    }

    public String getVfFunctionName() {
        return vgFunctionName;
    }

    public void setVfFunctionName(String vgFunctionName) {
        this.vgFunctionName = vgFunctionName;
    }

    public String getDirectory() {
        return directory;
    }

    public void setDirectory(String directory) {
        this.directory = directory;
    }

    public ArrayList<String> getInputAttributes() {
        return inputAttributes;
    }

    public void setInputAttributes(ArrayList<String> inputAttributes) {
        this.inputAttributes = inputAttributes;
    }

    public ArrayList<String> getOutputAttributes() {
        return outputAttributes;
    }

    public void setOutputAttributes(ArrayList<String> outputAttributes) {
        this.outputAttributes = outputAttributes;
    }


    /**
     * @return the bundlesPerTuple
     */
    public int getBundlesPerTuple() {
        return bundlesPerTuple;
    }

    /**
     * @param bundlesPerTuple the bundlesPerTuple to set
     */
    public void setBundlesPerTuple(int bundlesPerTuple) {
        this.bundlesPerTuple = bundlesPerTuple;
    }

    /* (non-Javadoc)
     * @see logicOperator.statisticsOperator.StatisticsOperator#visitNode()
     */
    @Override
    public String visitNode() {
        String result = "";
        result += "vgfunction('";
        result += vgFunctionName;
        result += "', ";
        result += directory;
        result += ", [";
        ;

        if (inputAttributes != null) {
            for (int i = 0; i < inputAttributes.size(); i++) {
                result += inputAttributes.get(i);

                if (i != inputAttributes.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";
        ;
        if (outputAttributes != null) {
            for (int i = 0; i < outputAttributes.size(); i++) {
                result += outputAttributes.get(i);

                if (i != outputAttributes.size() - 1) {
                    result += ", ";
                }
            }
        }
        result += "]).\r\n";

        return result;
    }

}
