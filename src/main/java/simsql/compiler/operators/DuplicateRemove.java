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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CommonContent;
import simsql.compiler.CopyHelper;

import java.util.ArrayList;


/**
 * A class that represents the duplicate removal in the logical plan
 */
public class DuplicateRemove extends Operator {

    /**
     * This is used for JSON deserialization
     *
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public DuplicateRemove(@JsonProperty("node-name") String nodeName,
                           @JsonProperty("children") ArrayList<Operator> children,
                           @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() {
        String result = "";

        result += this.getNodeStructureString();
        result += "dedup(" + this.getNodeName() + ", []). \r\n";
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
        String c_nodeName = commonContent.nodeName;
        ArrayList<Operator> c_children = commonContent.children;
        ArrayList<Operator> c_parents = commonContent.parents;

        DuplicateRemove dr = new DuplicateRemove(c_nodeName, c_children, c_parents);
        dr.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
        dr.setNameMap(commonContent.nameMap);

        copyHelper.getCopiedMap().put(getNodeName(), dr);

        ArrayList<Operator> children = dr.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(dr);
            }
        }

        return dr;
    }
}
