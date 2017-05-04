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

import simsql.compiler.CommonContent;
import simsql.compiler.CopyHelper;
import simsql.compiler.MathOperator;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * The class that represents the Aggregate in the logical query plan
 */
public class Aggregate extends Operator {


    /**
     * The name of this operation in the graph
     */
    private String aggregateName;


    /**
     * The group by list of the aggregate
     */
    private ArrayList<String> groupByList;


    /**
     * The expression list of the aggregate operation
     */
    private ArrayList<MathOperator> aggregateExpressionList;

    /**
     * The list of columns associated with it's math operator
     */
    private HashMap<MathOperator, ArrayList<String>> columnListMap;


    private HashMap<MathOperator, String> outputMap;


    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents the parent operators
     */
    public Aggregate(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents) {
        super(nodeName, children, parents);
    }

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents the parent operators
     * @param aggregateName the name of the aggregate operation in the graph
     */
    public Aggregate(String nodeName, ArrayList<Operator> children, ArrayList<Operator> parents, String aggregateName) {
        super(nodeName, children, parents);

        // set the aggregate name
        this.aggregateName = aggregateName;
    }

    /**
     * @return returns the name of the aggregate operator
     */
    public String getAggregateName() {
        return aggregateName;
    }


    /**
     * @param aggregateName sets the name of the aggregate operator
     */
    public void setAggregateName(String aggregateName) {
        this.aggregateName = aggregateName;
    }

    /**
     * @return gets the group by list of the aggregate
     */
    public ArrayList<String> getGroupByList() {
        return groupByList;
    }

    /**
     * @param groupByList set the group by list
     */
    public void setGroupByList(ArrayList<String> groupByList) {
        this.groupByList = groupByList;
    }

    /**
     * @return return the list of aggregate expressions
     */
    public ArrayList<MathOperator> getAggregateExpressionList() {
        return aggregateExpressionList;
    }

    public void setAggregateExpressionList(
            ArrayList<MathOperator> aggregateExpressionList) {
        this.aggregateExpressionList = aggregateExpressionList;
    }

    /**
     *
     * @return the the list of columns
     */
    public HashMap<MathOperator, ArrayList<String>> getColumnListMap() {
        return columnListMap;
    }


    /**
     *
     * @param columnListMap sets the list of columns
     */
    public void setColumnListMap(
            HashMap<MathOperator, ArrayList<String>> columnListMap) {
        this.columnListMap = columnListMap;
    }

    /**
     *
     * @return returns the output map
     */
    public HashMap<MathOperator, String> getOutputMap() {
        return outputMap;
    }

    public void setOutputMap(HashMap<MathOperator, String> outputMap) {
        this.outputMap = outputMap;
    }


    /**
     *
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() {
        String result = "";
        result += this.getNodeStructureString();

        result += "genagg(" + this.getNodeName() + ", ";
        result += aggregateName + ", [";

        if (groupByList != null) {
            for (int i = 0; i < groupByList.size(); i++) {
                result += groupByList.get(i);

                if (i != groupByList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";

        if (aggregateExpressionList != null) {
            for (int i = 0; i < aggregateExpressionList.size(); i++) {
                result += aggregateExpressionList.get(i).getNodeName();

                if (i != aggregateExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";

        if (aggregateExpressionList != null) {
            for (int i = 0; i < aggregateExpressionList.size(); i++) {
                MathOperator mathOperator = aggregateExpressionList.get(i);

                ArrayList<String> element = columnListMap.get(mathOperator);
                result += this.getListString(element);

                if (i != aggregateExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "], [";

        if (aggregateExpressionList != null) {
            for (int i = 0; i < aggregateExpressionList.size(); i++) {
                MathOperator mathOperator = aggregateExpressionList.get(i);

                String element = outputMap.get(mathOperator);
                result += "[";
                result += element;
                result += "]";

                if (i != aggregateExpressionList.size() - 1) {
                    result += ", ";
                }
            }
        }

        result += "]).\r\n";

        if (aggregateExpressionList != null) {
            for (MathOperator mathOperator : aggregateExpressionList) {
                result += mathOperator.visitNode();
            }
        }

        return result;
    }


    public ArrayList<String> getGeneratedNameList() {
        ArrayList<String> resultList = new ArrayList<String>();

        if (aggregateExpressionList != null) {
            for (MathOperator mathOperator : aggregateExpressionList) {
                resultList.add(outputMap.get(mathOperator));
            }
        }
        return resultList;
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
        String c_aggregateName = aggregateName;
        ArrayList<String> c_groupbyList = copyHelper.copyStringList(groupByList);
        ArrayList<MathOperator> c_aggregateExpressionList = copyHelper.copyMathOperatorList(aggregateExpressionList);
        HashMap<MathOperator, ArrayList<String>> c_columnListMap = copyHelper.copyMathOperatorStringListMap(aggregateExpressionList, c_aggregateExpressionList, columnListMap);
        HashMap<MathOperator, String> c_outputMap = copyHelper.copyMathOperatorStringMap(aggregateExpressionList, c_aggregateExpressionList, outputMap);

        Aggregate aggregate = new Aggregate(commonContent.nodeName,
                commonContent.children,
                commonContent.parents,
                c_aggregateName);

        aggregate.setNameMap(commonContent.nameMap);
        aggregate.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        aggregate.setGroupByList(c_groupbyList);
        aggregate.setAggregateExpressionList(c_aggregateExpressionList);
        aggregate.setColumnListMap(c_columnListMap);
        aggregate.setOutputMap(c_outputMap);

        copyHelper.getCopiedMap().put(getNodeName(), aggregate);
        /*
		 * add parent for its children.
		 */
        ArrayList<Operator> children = aggregate.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(aggregate);
            }
        }
        return aggregate;
    }

}
