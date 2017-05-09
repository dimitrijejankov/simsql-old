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
import simsql.compiler.boolean_operator.AndOperator;
import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.compiler.math_operators.MathOperator;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The class that represents the Join in the logical query plan
 */
public class Join extends Operator {

    public static final int JOIN = 0;
    public static final int SEMIJOIN = 1;
    public static final int ANTIJOIN = 2;

    /**
     * type can be:
     * 1. JOIN
     * 2. SEMIJOIN
     * 3. ANTIJOIN
     */
    @JsonProperty("type")
    private int type;

    /**
     * The name of the left table
     */
    @JsonProperty("left-table")
    private String leftTable;

    /**
     * The boolean operator of the join
     */
    @JsonProperty("boolean-operator")
    private BooleanOperator booleanOperator;

    /**
     * This is used for JSON deserialization
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public Join(@JsonProperty("node-name") String nodeName,
                @JsonProperty("children") ArrayList<Operator> children,
                @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @param nodeName        the name of the operator
     * @param children        the children of the operator
     * @param parents         the parent operators
     * @param type            the type of the join (join, semi-join, anti-join)
     * @param leftTable       the left table name
     * @param booleanOperator the boolean operator
     */
    public Join(String nodeName,
                ArrayList<Operator> children,
                ArrayList<Operator> parents,
                int type,
                String leftTable,
                BooleanOperator booleanOperator) {
        super(nodeName, children, parents);
        this.type = type;
        this.leftTable = leftTable;
        this.booleanOperator = booleanOperator;
    }

    /**
     * @return returns the type of the join
     */
    public int getType() {
        return type;
    }

    /**
     * @param type the new type of the join
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return the left table name
     */
    public String getLeftTable() {
        return leftTable;
    }

    /**
     * @param leftTable sets the left table name
     */
    public void setLeftTable(String leftTable) {
        this.leftTable = leftTable;
    }

    /**
     * @return returns the boolean operator
     */
    public BooleanOperator getBooleanOperator() {
        return booleanOperator;
    }

    /**
     * @param booleanOperator sets the boolean operator
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

        switch (type) {
            case SEMIJOIN:
                result += "semijoin(" + getNodeName() + ", ";
                result += leftTable;
                result += ", [";

                if (booleanOperator != null) {
                    if (booleanOperator instanceof AndOperator) {
                        ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
                        for (int i = 0; i < subBooleanOperatorList.size(); i++) {
                            if (i != subBooleanOperatorList.size() - 1) {
                                result += subBooleanOperatorList.get(i).getName();
                                result += ", ";
                            } else {
                                result += subBooleanOperatorList.get(i).getName();
                            }
                        }
                    } else {
                        result += booleanOperator.getName();
                    }
                }

                result += "]). \r\n";
                break;

            case ANTIJOIN:
                result += "antijoin(" + getNodeName() + ", ";
                result += leftTable;
                result += ", [";

                if (booleanOperator != null) {
                    if (booleanOperator instanceof AndOperator) {
                        ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
                        for (int i = 0; i < subBooleanOperatorList.size(); i++) {
                            if (i != subBooleanOperatorList.size() - 1) {
                                result += subBooleanOperatorList.get(i).getName();
                                result += ", ";
                            } else {
                                result += subBooleanOperatorList.get(i).getName();
                            }
                        }
                    } else {
                        result += booleanOperator.getName();
                    }
                }


                result += "]). \r\n";
                break;

            default:
                result += "join(" + getNodeName() + ", [";

                if (booleanOperator != null) {
                    if (booleanOperator instanceof AndOperator) {
                        ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
                        for (int i = 0; i < subBooleanOperatorList.size(); i++) {
                            if (i != subBooleanOperatorList.size() - 1) {
                                result += subBooleanOperatorList.get(i).getName();
                                result += ", ";
                            } else {
                                result += subBooleanOperatorList.get(i).getName();
                            }
                        }
                    } else {
                        result += booleanOperator.getName();
                    }
                }


                result += "], _). \r\n";
                break;
        }

        if (booleanOperator != null) {
            if (booleanOperator instanceof AndOperator) {
                ArrayList<BooleanOperator> subBooleanOperatorList = ((AndOperator) booleanOperator).getOperatorList();
                for (BooleanOperator aSubBooleanOperatorList : subBooleanOperatorList) {
                    result += aSubBooleanOperatorList.visitNode();
                }
            } else {
                result += booleanOperator.visitNode();
            }
        }

        return result;
    }

    /**
     * @see simsql.compiler.operators.Operator#changeNodeProperty(HashMap, TranslatorHelper)
     */
    public void changeNodeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        super.changeNodeProperty(indices, translatorHelper);

        // replace the indices in it...
        booleanOperator.changeProperty(indices, translatorHelper);
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

        int c_type = this.type;
        String c_leftTable = this.leftTable;
        BooleanOperator c_booleanOperator = copyHelper.copyBooleanPredicate(this.booleanOperator);

        Join c_jion = new Join(c_nodeName, c_children, c_parents, c_type, c_leftTable, c_booleanOperator);
        c_jion.setNameMap(commonContent.nameMap);
        c_jion.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        copyHelper.getCopiedMap().put(getNodeName(), c_jion);

        ArrayList<Operator> children = c_jion.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(c_jion);
            }
        }
        return c_jion;
    }

}
