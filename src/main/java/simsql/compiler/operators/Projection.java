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
import simsql.compiler.CommonContent;
import simsql.compiler.CopyHelper;

import java.util.ArrayList;


/**
 * The class that represents the Projection in the logical query plan
 */
public class Projection extends Operator {


    /**
     * Names of the projected attributes
     */
    @JsonProperty("projected-name-list")
    private ArrayList<String> projectedNameList;

    /**
     * This is used for JSON deserialization
     *
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public Projection(@JsonProperty("node-name") String nodeName,
                      @JsonProperty("children") ArrayList<Operator> children,
                      @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @param nodeName          the name of the operator
     * @param children          the children of the operator
     * @param parents           the parent operators
     * @param projectedNameList the list of the projected attributes..
     */
    public Projection(String nodeName,
                      ArrayList<Operator> children,
                      ArrayList<Operator> parents,
                      ArrayList<String> projectedNameList) {
        super(nodeName, children, parents);
        this.projectedNameList = projectedNameList;
    }


    /**
     * @return the list of projected attributes
     */
    public ArrayList<String> getProjectedNameList() {
        return projectedNameList;
    }


    /**
     * @param projectedNameList sets the list of projected attributes
     */
    public void setProjectedNameList(ArrayList<String> projectedNameList) {
        this.projectedNameList = projectedNameList;
    }


    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() {
        String result = "";

        result += this.getNodeStructureString();

        result += "projection(" + this.getNodeName();
        result += ", [";

        if (projectedNameList != null) {
            for (int i = 0; i < projectedNameList.size(); i++) {
                result += projectedNameList.get(i);

                if (i != projectedNameList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "]";
        result += ").\r\n";

        return result;
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
        ArrayList<String> c_projectedNameList = copyHelper.copyStringList(this.projectedNameList);

        String c_nodeName = commonContent.nodeName;
        ArrayList<Operator> c_children = commonContent.children;
        ArrayList<Operator> c_parents = commonContent.parents;

        Projection projection = new Projection(c_nodeName,
                c_children,
                c_parents,
                c_projectedNameList);

        projection.setNameMap(commonContent.nameMap);
        projection.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        copyHelper.getCopiedMap().put(getNodeName(), projection);

        ArrayList<Operator> children = projection.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(projection);
            }
        }
        return projection;
    }
}
