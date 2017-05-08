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
import simsql.compiler.math_operators.MathOperator;
import java.util.ArrayList;
import java.util.HashMap;


/**
 * The class that represents the Aggregate in the logical query plan
 */
public class Aggregate extends Operator {


    /**
     * The name of this operation in the graph
     */
    @JsonProperty("aggregate-name")
    private String aggregateName;


    /**
     * The group by list of the aggregate
     */
    @JsonProperty("group-by-list")
    private ArrayList<String> groupByList;


    /**
     * The expression list of the aggregate operation
     */
    @JsonProperty("aggregate-expression-list")
    private ArrayList<MathOperator> aggregateExpressionList;

    /**
     * The list of columns associated with it's math operator
     */
    @JsonIgnore
    private HashMap<MathOperator, ArrayList<String>> columnListMap;

    /**
     * TODO figure out what this is...
     */
    @JsonIgnore
    private HashMap<MathOperator, String> outputMap;

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents the parent operators
     */
    @JsonCreator
    public Aggregate(@JsonProperty("node-name") String nodeName, @JsonProperty("children") ArrayList<Operator> children, @JsonProperty("parents") ArrayList<Operator> parents) {
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
     * Returns the array list of column lists  - used for serialization
     * @return the array list column lists
     */
    @JsonGetter("column-lists")
    public ArrayList<ColumnList> getColumnLists() {

        ArrayList<ColumnList> ret = new ArrayList<ColumnList>();

        // convert to array list
        for(MathOperator m : columnListMap.keySet()) {
            ret.add(new ColumnList(m, columnListMap.get(m)));
        }

        return ret;
    }

    /**
     * Sets the column map from it's inverted form - used for serialization
     * @param invertedColumnMap the inverted column map
     */
    @JsonSetter("column-lists")
    public void setColumnLists(ArrayList<ColumnList> invertedColumnMap) {

        columnListMap = new HashMap<MathOperator, ArrayList<String>>();

        // invert the map and add it to to the empty output map
        for(ColumnList c : invertedColumnMap) {
            columnListMap.put(c.operator, c.columns);
        }
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
     * Returns the inverted output map - used for serialization
     * @return the inverted outputMap
     */
    @JsonGetter("inverted-output-map")
    public HashMap<String, MathOperator> getInvertedOutputMap() {

        HashMap<String, MathOperator> ret = new HashMap<String, MathOperator>();

        // invert the map
        for(MathOperator m : outputMap.keySet()) {
            ret.put(outputMap.get(m), m);
        }

        return ret;
    }

    /**
     * Sets the output map from it's inverted form - used for serialization
     * @param invertedOutputMap the inverted output map
     */
    @JsonSetter("inverted-output-map")
    public void setInvertedOutputMap(HashMap<String, MathOperator> invertedOutputMap) {

        outputMap = new HashMap<MathOperator, String>();

        // invert the map and add it to to the empty output map
        for(String m : invertedOutputMap.keySet()) {
            outputMap.put(invertedOutputMap.get(m), m);
        }
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

    @JsonIgnore
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
