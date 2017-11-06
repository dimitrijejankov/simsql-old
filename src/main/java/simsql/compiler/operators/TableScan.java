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
import simsql.compiler.expressions.MathExpression;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The class that represents the Selection in the logical query plan
 */
public class TableScan extends Operator {

    /**
     * the name of the table
     */
    @JsonProperty("table_name")
    private String tableName;

    /**
     * the list of attributes
     */
    @JsonProperty("attribute_list")
    private ArrayList<String> attributeList;

    /**
     * Here vgStatistics share the inputAttributeNameList and outputAttributeNameList
     */
    @JsonProperty("relation_statistics")
    private RelationStatistics relationStatistics;

    /**
     * a reference of the catalog
     */
    @JsonIgnore
    private Catalog catalog;

    /**
     * the hash map of (index, value) pairs
     */
    @JsonProperty("index-strings")
    private HashMap<String, Integer> indexStrings;

    /**
     * the hash map of (index, expression pairs)
     */
    @JsonProperty("index-math-expressions")
    private HashMap<String, MathExpression> indexMathExpressions;

    /**
     * the type of the table scan it can be : COMMON_TABLE, CONSTANT_INDEX_TABLE,
     * GENERAL_INDEX_TABLE, MULTIDIMENSIONAL_CONSTANT_INDEX_TABLE, MULTIDIMENSIONAL_GENERAL_INDEX_TABLE
     */
    @JsonProperty("type")
    private int type;

    /**
     * Add a data structure here for supporting simulation.
     */
    @JsonProperty("table-info")
    private PreviousTable tableInfo;

    /**
     * @param nodeName the name of the operator
     * @param children the children of the operator
     * @param parents  the parent operators
     */
    @JsonCreator
    public TableScan(@JsonProperty("node-name") String nodeName,
                     @JsonProperty("children") ArrayList<Operator> children,
                     @JsonProperty("parents") ArrayList<Operator> parents) {
        super(nodeName, children, parents);
        this.type = TableReference.COMMON_TABLE;
        this.tableInfo = null;
        this.indexStrings = new HashMap<>();
        this.indexMathExpressions = new HashMap<>();
        this.catalog = SimsqlCompiler.catalog;
    }

    @Override
    @JsonIgnore
    public ArrayList<String> getOutputAttributeNames() {
        return new ArrayList<>(attributeList);
    }

    /**
     * @param nodeName             the name of the operator
     * @param children             the children of the operator
     * @param parents              the parent operators
     * @param attributeList        the list of attributes for the table scan
     * @param relationStatistics   the relationship statistics
     * @param type                 the type of the table scan
     * @param indexMathExpressions the index map expression pairs
     */
    public TableScan(String nodeName,
                     ArrayList<Operator> children,
                     ArrayList<Operator> parents,
                     String tableName,
                     ArrayList<String> attributeList,
                     RelationStatistics relationStatistics,
                     int type,
                     HashMap<String, MathExpression> indexMathExpressions) {
        super(nodeName, children, parents);
        this.tableName = tableName;
        this.attributeList = attributeList;
        this.relationStatistics = relationStatistics;
        this.catalog = SimsqlCompiler.catalog;
        this.tableInfo = null;
        this.type = type;
        this.indexStrings = new HashMap<>();
        this.indexMathExpressions = indexMathExpressions;
    }

    /**
     * @param nodeName             the name of the operator
     * @param children             the children of the operator
     * @param parents              the parent operators
     * @param tableName            the name of the table
     * @param attributeList        the list of attributes for the table scan
     * @param relationStatistics   the relationship statistics
     * @param type                 the type of the table scan
     * @param indexMathExpressions the index map expression pairs
     */
    public TableScan(String nodeName,
                     ArrayList<Operator> children,
                     ArrayList<Operator> parents,
                     String tableName,
                     ArrayList<String> attributeList,
                     RelationStatistics relationStatistics,
                     HashMap<String, Integer> indexStrings,
                     int type,
                     HashMap<String, MathExpression> indexMathExpressions) {
        super(nodeName, children, parents);
        this.tableName = tableName;
        this.attributeList = attributeList;
        this.relationStatistics = relationStatistics;
        this.catalog = SimsqlCompiler.catalog;
        this.tableInfo = null;
        this.type = type;
        this.indexStrings = indexStrings;
        this.indexMathExpressions = indexMathExpressions;
    }

    /**
     * @return returns the name of the table
     */
    public String getTableName() {
        return tableName;
    }

    /**
     * @param tableName sets the table name
     */
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    /**
     * @return returns the attribute list
     */
    public ArrayList<String> getAttributeList() {
        return attributeList;
    }

    /**
     * @param attributeList sets the attribute list
     */
    public void setAttributeList(ArrayList<String> attributeList) {
        this.attributeList = attributeList;
    }

    /**
     * @return returns the type
     */
    public int getType() {
        return type;
    }


    /**
     * @param type sets the type of the table scan
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * @return returns the index value pairs of the table
     */
    public HashMap<String, Integer> getIndexStrings() {
        return indexStrings;
    }

    /**
     * @param indexStrings sets the index value pairs of the table
     */
    public void setIndexStrings(HashMap<String, Integer> indexStrings) {
        this.indexStrings = indexStrings;
    }

    /**
     * @return returns the index expression pairs of the table (if the table is a general index table)
     */
    public HashMap<String, MathExpression> getIndexMathExpressions() {
        return indexMathExpressions;
    }

    /**
     * @param indexMathExpressions sets the index expression pairs
     */
    public void setIndexMathExpressions(HashMap<String, MathExpression> indexMathExpressions) {
        this.indexMathExpressions = indexMathExpressions;
    }

    /**
     * @return returns expression for the first index.. (used to support the legacy general index table)
     */
    @JsonIgnore
    public MathExpression getIndexMathExp() {
        return indexMathExpressions.get("i");
    }

    /**
     * @return the tableInfo
     */
    @JsonIgnore
    public PreviousTable getTableInfo() {
        return tableInfo;
    }

    /**
     * @param tableInfo the tableInfo to set
     */
    public void setTableInfo(PreviousTable tableInfo) {
        this.tableInfo = tableInfo;
    }

    /**
     * @param a left value
     * @param b right value
     * @return minimum of both values
     */
    public long min(long a, long b) {
        if (a < b)
            return a;
        else
            return b;
    }

    @JsonIgnore
    public ArrayList<String> getGeneratedNameList() {
        ArrayList<String> resultList = new ArrayList<String>();

        if (attributeList != null) {
            for (String attributeName : attributeList) {
                resultList.add(attributeName);
            }
        }
        return resultList;
    }


    public RelationStatistics getRelationStatistics() {
        return relationStatistics;
    }


    public void setRelationStatistics(RelationStatistics relationStatistics) {
        this.relationStatistics = relationStatistics;
    }

    /**
     * Returns the type enumeration of the operator
     * @return returns the type
     */
    @JsonIgnore
    public OperatorType getOperatorType() {
        return OperatorType.TABLE_SCAN;
    }

    /**
     * @return returns the string file representation of this operator
     */
    @Override
    public String visitNode() throws Exception {
        String result = "";
        if (indexStrings.isEmpty() && type == TableReference.COMMON_TABLE) {

            result += this.getNodeStructureString();

            result += "tablescan(" + this.getNodeName() + ", '" + tableName;
            result += "', [";

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    result += attributeList.get(i);

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "]).\r\n";

			/* add the statistics of this table and its attributes */
            result += "stats(" + this.getNodeName() + ", [";

            // the real attributes of the relation to be scanned
            ArrayList<Attribute> realAttributeList;

            // the number of records in this relation
            long tupleNumber;

            // the directory in the HDFS of the relation
            String directory;

            // the primary key of the relation
            ArrayList<String> primaryKey;

            // if we don't have the information about this relation
            if(tableInfo == null) {
                Relation relation = catalog.getRelation(tableName);

                // grab the attributes
                realAttributeList = relation.getAttributes();

                // grab the tuple number
                tupleNumber = relation.getTupleNum();

                // grab the directory
                directory = relation.getFileName();

                // grab the primary key
                primaryKey = relation.getPrimaryKey();
            }
            else {

                // grab it from table info
                realAttributeList = tableInfo.getRealAttributeList();

                // grab the tuple number
                tupleNumber = tableInfo.getTupleNum();

                // grab the directory
                directory = tableInfo.getFileDirectory();

                // grab the primary key from the table info
                primaryKey = tableInfo.getPrimaryKey();
            }

            String attributeAlias;
            Attribute attribute;
            int attributeSize;
            long uniqueValue;

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    attributeAlias = attributeList.get(i);
                    attribute = realAttributeList.get(i);

                    uniqueValue = attribute.getUniqueValue();
                    result += "uniqueValues(" + attributeAlias + ", " + uniqueValue + ")";

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "], [], \r\n\t\t[";

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    attributeAlias = attributeList.get(i);
                    attribute = realAttributeList.get(i);

                    attributeSize = attribute.getAttributeSize();
                    result += "attributeSize(" + attributeAlias + ", " + attributeSize + ")";

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "], " + tupleNumber + ", _).\r\n";

            if (primaryKey != null && primaryKey.size() >= 1) {
                int keyIndex;

                HashMap<String, Integer> attributeIndexMap = new HashMap<>();
                if (realAttributeList != null) {
                    for (int i = 0; i < realAttributeList.size(); i++) {
                        attribute = realAttributeList.get(i);
                        attributeIndexMap.put(attribute.getName(), i);
                    }
                }

                result += "candidateKey(" + this.getNodeName();
                result += ", [";

                for (int i = 0; i < primaryKey.size(); i++) {
                    keyIndex = attributeIndexMap.get(primaryKey.get(i));

                    result += attributeList.get(keyIndex);

                    if (i != primaryKey.size() - 1) {
                        result += ", ";
                    }
                }

                result += "]).\r\n";
            }

            // if we don't have the relation statistics time to make some...
            if(relationStatistics == null) {

                ArrayList<String> attributeNames = new ArrayList<>();

                if(realAttributeList == null) {
                    throw new RuntimeException("The real attribute list is missing, something went wrong!");
                }

                // copy the attribute names
                for (Attribute a : realAttributeList) {
                    attributeNames.add(a.getName());
                }

                this.relationStatistics = new RelationStatistics(tableName,
                        directory,
                        attributeNames,
                        TableReference.COMMON_TABLE);

                this.relationStatistics.setTableInfo(tableInfo);
            }

        } else if (tableName.matches("^[^_]+(_[a-z])+$")) {
            result += this.getNodeStructureString();

            String indexString = "";
            for (int i = 0; i < this.getIndexMathExpressions().size(); ++i) {
                indexString += "_" + new MPNGenerator(this.indexMathExpressions.get(MultidimensionalSchemaIndices.labelingOrder[i])).convertToMPN();
            }

            result += "tablescan(" + this.getNodeName() + ", '" + MultidimensionalTableSchema.getTablePrefixFromGeneralName(tableName) + "_" + indexString;

            result += "', [";

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    result += attributeList.get(i);

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "]).\r\n";

            /* add the statistics of this table and its attributes */
            result += "stats(" + this.getNodeName() + ", [";

            View view = catalog.getView(tableName);
            ArrayList<Attribute> realAttributeList = view.getAttributes();
            String attributeAlias;
            Attribute attribute;
            int attributeSize;
            long uniqueValue;

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    attributeAlias = attributeList.get(i);
                    attribute = realAttributeList.get(i);

                    uniqueValue = attribute.getUniqueValue();
                    result += "uniqueValues(" + attributeAlias + ", " + uniqueValue + ")";

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "], [], \r\n\t\t[";

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    attributeAlias = attributeList.get(i);
                    attribute = realAttributeList.get(i);

                    attributeSize = attribute.getAttributeSize();
                    result += "attributeSize(" + attributeAlias + ", " + attributeSize + ")";

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "], " + "_" + ", _).\r\n";
        } else {
            result += this.getNodeStructureString();

            result += "tablescan(" + this.getNodeName() + ", '" + tableName;
            result += "', [";

            if (attributeList != null) {
                for (int i = 0; i < attributeList.size(); i++) {
                    result += attributeList.get(i);

                    if (i != attributeList.size() - 1) {
                        result += ", ";
                    }
                }
            }

            result += "]).\r\n";

            /* add the statistics of this table and its attributes */
            result += "stats(" + this.getNodeName() + ", [";

            if (tableInfo == null) {
                View view = catalog.getView(tableName);
                ArrayList<Attribute> realAttributeList = view.getAttributes();
                String attributeAlias;
                Attribute attribute;
                int attributeSize;
                long uniqueValue;

                if (attributeList != null) {
                    for (int i = 0; i < attributeList.size(); i++) {
                        attributeAlias = attributeList.get(i);
                        attribute = realAttributeList.get(i);

                        uniqueValue = attribute.getUniqueValue();
                        result += "uniqueValues(" + attributeAlias + ", " + uniqueValue + ")";

                        if (i != attributeList.size() - 1) {
                            result += ", ";
                        }
                    }
                }

                result += "], [], \r\n\t\t[";

                if (attributeList != null) {
                    for (int i = 0; i < attributeList.size(); i++) {
                        attributeAlias = attributeList.get(i);
                        attribute = realAttributeList.get(i);

                        attributeSize = attribute.getAttributeSize();
                        result += "attributeSize(" + attributeAlias + ", " + attributeSize + ")";

                        if (i != attributeList.size() - 1) {
                            result += ", ";
                        }
                    }
                }

                result += "], " + "_" + ", _).\r\n";
            } else //tableInfo != null
            {
                String attributeAlias;
                String attribute;
                int attributeSize;
                long uniqueValue;

                View view = catalog.getView(tableName);
                ArrayList<Attribute> realAttributeList = view.getAttributes();

                String fileDirectory = tableInfo.getFileDirectory();
                ArrayList<String> hdfsAttributeList = tableInfo.getAttributeList();
                HashMap<String, String> hdfsAttributeTypeMap = tableInfo.getAttributeMap();
                ArrayList<String> hdfsRandamAttributeList = tableInfo.getRandamAttributeList();
                HashMap<String, Long> hdfsUniqueValueNumMap = tableInfo.getUniqueValueNumMap();
                long tupleNum = tableInfo.getTupleNum();

                if (attributeList != null) {
                    for (int i = 0; i < attributeList.size(); i++) {
                        attributeAlias = attributeList.get(i);
                        attribute = hdfsAttributeList.get(i);

                        uniqueValue = hdfsUniqueValueNumMap.get(attribute);
                        result += "uniqueValues(" + attributeAlias + ", " + uniqueValue + ")";

                        if (i != attributeList.size() - 1) {
                            result += ", ";
                        }
                    }
                }

                result += "], [";

                if (hdfsRandamAttributeList != null && hdfsRandamAttributeList.size() > 0) {
                    for (int i = 0; i < hdfsRandamAttributeList.size(); i++) {
                        String randomAttributeName = hdfsRandamAttributeList.get(i);
                        int indexForAttribute = hdfsAttributeList.indexOf(randomAttributeName);
                        result += "uniqueValuesPerTupleBundle(";
                        result += attributeList.get(indexForAttribute);
                        result += ", ";
                        long monteCarloIteration = Long.parseLong(catalog.getMonteCarloIterations());
                        result += min(hdfsUniqueValueNumMap.get(hdfsAttributeList.get(indexForAttribute)), monteCarloIteration);
                        result += ")";

                        if (i != hdfsRandamAttributeList.size() - 1) {
                            result += ",";
                        }
                    }
                }

                result += "], \r\n\t\t[";

                if (attributeList != null) {
                    for (int i = 0; i < attributeList.size(); i++) {
                        attributeAlias = attributeList.get(i);
                        attribute = hdfsAttributeList.get(i);
                        Attribute viewAttribute = realAttributeList.get(i);

                        attributeSize = viewAttribute.getAttributeSize();
                        result += "attributeSize(" + attributeAlias + ", " + attributeSize + ")";

                        if (i != attributeList.size() - 1) {
                            result += ", ";
                        }
                    }
                }

                result += "], " + tupleNum + ", _).\r\n";

                /*
                 * random Attribute;
                 */
                result += "randomAttrsRelation(";
                result += this.getNodeName() + ", [";
                for (int i = 0; i < hdfsRandamAttributeList.size(); i++) {
                    String randomAttributeName = hdfsRandamAttributeList.get(i);
                    int indexForAttribute = hdfsAttributeList.indexOf(randomAttributeName);
                    result += attributeList.get(indexForAttribute);

                    if (i != hdfsRandamAttributeList.size() - 1) {
                        result += ",";
                    }
                }

                result += "]).\r\n";

                relationStatistics.setTableInfo(tableInfo);
            }
        }

        return result;
    }

    /**
     * @see simsql.compiler.operators.Operator#changeNodeProperty(HashMap, TranslatorHelper)
     */
    public void changeNodeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
        super.changeNodeProperty(indices, translatorHelper);

        // if this table scan is a general table
        if (MultidimensionalTableSchema.isGeneralTable(tableName)) {


            // grab the prefix from it's name
            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(tableName);

            // update the relation statistics
            relationStatistics.setRelation(MultidimensionalTableSchema.getQualifiedTableNameFromEvaluatedExpressions(prefix, indexMathExpressions, indices));
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

        String c_tableName = this.tableName;
        ArrayList<String> c_attributeList = copyHelper.copyStringList(attributeList);
        RelationStatistics c_relationStatistics = this.relationStatistics.copy();

        HashMap<String, Integer> c_indexStrings = new HashMap<String, Integer>(indexStrings);

        TableScan tablescan = new TableScan(commonContent.nodeName,
                commonContent.children,
                commonContent.parents,
                c_tableName,
                c_attributeList,
                c_relationStatistics,
                c_indexStrings,
                this.type,
                this.indexMathExpressions);

        tablescan.setNameMap(commonContent.nameMap);
        tablescan.setMapSpaceNameSet(commonContent.mapSpaceNameSet);

        copyHelper.getCopiedMap().put(getNodeName(), tablescan);

        ArrayList<Operator> children = tablescan.getChildren();
        if (children != null) {
            for (Operator aChildren : children) {
                aChildren.addParent(tablescan);
            }
        }
        return tablescan;
    }

}
