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

package simsql.compiler.operators;

import com.fasterxml.jackson.annotation.*;
import simsql.compiler.*;
import simsql.compiler.boolean_operator.AndOperator;
import simsql.compiler.boolean_operator.BooleanOperator;

import java.util.ArrayList;


/**
 * The class that represents the Selection in the logical query plan
 */
public class Selection extends Operator {

    /**
     * the boolean operator of the selection
     */
    @JsonProperty("boolean-operator")
    private BooleanOperator booleanOperator;

    /**
     * This is used for JSON deserialization
     *
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public Selection(@JsonProperty("node-name") String nodeName,
                     @JsonProperty("children") ArrayList<Operator> children,
                     @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @param nodeName        the name of the operator
     * @param children        the children of the operator
     * @param parents         the parent operators
     * @param booleanOperator the boolean operator of the selection
     */
    public Selection(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents, BooleanOperator booleanOperator) {
        super(nodeName, children, parents);
        this.booleanOperator = booleanOperator;
    }

    /**
     * @return returns the boolean operator of the selection
     */
    public BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }

    /**
     * @param booleanOperator sets the boolean operator of the selection
     */
    public void setBooleanOperator(BooleanOperator booleanOperator) {
        this.booleanOperator = booleanOperator;
    }

    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() {
        String result = "";

        result += this.getNodeStructureString();

        result += "selection(" + this.getNodeName() + ", [";

        if (booleanOperator instanceof AndOperator) {
            /*
             * case 4.1: AndOperator
			 */
            ArrayList<BooleanOperator> andList = ((AndOperator) booleanOperator).getOperatorList();

            for (int i = 0; i < andList.size(); i++) {
                result += andList.get(i).getName();

                if (i != andList.size() - 1) {
                    result += ", ";
                }
            }
        } else {
            result += booleanOperator.getName();

        }

        result += "]).\r\n";

        if (booleanOperator instanceof AndOperator) {
            /*
			 * case 4.1: AndOperator
			 */
            ArrayList<BooleanOperator> andList = ((AndOperator) booleanOperator).getOperatorList();

            for (BooleanOperator anAndList : andList) {
                result += anAndList.visitNode();
            }
        } else {
            result += booleanOperator.visitNode();

        }

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
        BooleanOperator c_booleanOperator = copyHelper.copyBooleanPredicate(this.booleanOperator);
        Selection c_selection = new Selection(c_nodeName, c_children, c_parents, c_booleanOperator);
        c_selection.setNameMap(commonContent.nameMap);
        c_selection.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        copyHelper.getCopiedMap().put(getNodeName(), c_selection);

        ArrayList<Operator> children = c_selection.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(c_selection);
            }
        }
        return c_selection;
    }
}
