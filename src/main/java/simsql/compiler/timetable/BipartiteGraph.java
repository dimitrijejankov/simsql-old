package simsql.compiler.timetable;

import simsql.compiler.*;
import simsql.compiler.boolean_operator.*;
import simsql.compiler.expressions.MathExpression;
import simsql.compiler.math_operators.*;
import simsql.compiler.operators.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;
import static simsql.compiler.MultidimensionalTableSchema.isGeneralTable;

public class BipartiteGraph {

    /**
     * The (general index name, relational operations) pairs for general index tables.
     */
    private HashMap<String, Operator> tableOperationMap;

    /**
     * The nodes of the table graph
     */
    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;

    /**
     * The the (table, set of dependent tables) pairs in general index form
     */
    private HashMap<String, HashSet<String>> backwardEdges;

    /**
     * An instance of the TranslatorHelper class
     */
    private TranslatorHelper translatorHelper;

    public BipartiteGraph(LinkedList<TimeTableNode> requiredTables,
                          HashMap<String, HashSet<String>> backwardEdges,
                          HashMap<String, Operator> tableOperationMap) throws Exception {

        // store these values
        this.tableOperationMap = tableOperationMap;
        this.backwardEdges = backwardEdges;

        // init stuff
        this.translatorHelper = new TranslatorHelper();
        this.nodes = new HashMap<TimeTableNode, HashSet<TimeTableNode>>();

        // generate the table graph
        generateTableGraph(requiredTables);

        // generate the bipartite graph
        generateBipartiteGraph();
    }


    /**
     * Generates the table dependency graph for the tables that are in the final nodes list
     *
     * @param finalNodes the list of the final nodes
     */
    private void generateTableGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<TimeTableNode>(finalNodes);
        HashSet<TimeTableNode> visitedNodes = new HashSet<TimeTableNode>();

        while (processingList.size() != 0) {

            // this is the current table we are processing
            TimeTableNode activeNode = processingList.iterator().next();

            // find the backward edges for the current table
            HashSet<String> edges = findBackwardEdge(activeNode);

            // evaluate the indices of the backward edges we just found
            HashSet<TimeTableNode> tables = evaluateDependentTables(edges, activeNode.getIndexStrings());

            // only adds not previously visited tables to the processing list
            for (TimeTableNode table : tables) {
                if (!visitedNodes.contains(table)) {
                    processingList.add(table);
                }
            }

            // adds the dependencies for the table
            nodes.put(activeNode, tables);

            // remove the current table from the list of unprocessed tables
            processingList.remove(activeNode);

            // add the current table to the list of visited nodes so we don't check it out twice...
            visitedNodes.add(activeNode);
        }
    }

    /**
     * For a given set of edges and the specified index strings calculates the index strings of the tables in the set of edges
     *
     * @param edges        the set of edges given in brackets general index form with index formulas
     * @param indexStrings the the index strings
     * @return the evaluated time table nodes
     */
    private HashSet<TimeTableNode> evaluateDependentTables(HashSet<String> edges, HashMap<String, Integer> indexStrings) {

        HashSet<TimeTableNode> retValue = new HashSet<TimeTableNode>();

        for (String edge : edges) {

            String prefix = getPrefixFromBracketsTableName(edge);
            MultidimensionalSchemaExpressions expressions = new MultidimensionalSchemaExpressions(edge.substring(edge.indexOf("[")));
            HashMap<String, Integer> edgeIndex = expressions.evaluateExpressions(indexStrings);
            TimeTableNode node = new TimeTableNode(prefix, edgeIndex);

            retValue.add(node);
        }

        return retValue;
    }

    private HashSet<String> findBackwardEdge(TimeTableNode node) {
        String tableName = node.getBracketsTableName();
        String tablePrefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(tableName);

        if (backwardEdges.containsKey(tableName)) {
            return backwardEdges.get(tableName);
        }

        if (backwardEdges.containsKey(tablePrefix + "[i]")) {
            return backwardEdges.get(tablePrefix + "[i]");
        }

        for (String edge : backwardEdges.keySet()) {

            if (edge.matches("^[^_]+((\\[[0-9]+to[0-9]])+|(\\[[0-9]+to])|(\\[[0-9]+]))+$")) {
                MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(edge));
                String prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(edge);

                if (indices.areIndicesForThisTable(node.getIndexStrings()) &&
                        prefix.equals(node.getTableName())) {
                    return backwardEdges.get(edge);
                }
            }
        }

        throw new RuntimeException("Failed to find an appropriate edge.");
    }

    private void generateBipartiteGraph() throws Exception {

        // Each random table (string) corresponds to an operator.
        HashMap<String, Operator> generatedPlanMap = new HashMap<String, Operator>();

        // Each random table (string) has a list of tableScan which should be replaced.
        HashMap<String, ArrayList<TableScan>> replacedPlanMap = new HashMap<String, ArrayList<TableScan>>();

        // while we haven't used up all the tables to instantiate their operators
        while(!this.nodes.isEmpty()) {

            // find a leaf node
            String table = getLeafNode();

            Operator operator;

            // figure out the operator
            if (tableOperationMap.containsKey(table)) {
                operator = tableOperationMap.get(table);
                generatedPlanMap.put(table, operator);
            } else {
                String randomTable = findMatchingGeneralIndexTable(table);
                operator = tableOperationMap.get(randomTable);
            }

            CopyHelper copyHelper = new CopyHelper();
            operator = operator.copy(copyHelper);

            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);
            instantiateOperator(table, operator, indices, generatedPlanMap, replacedPlanMap);
        }

    }

    /**
     * Extracts a leaf node from the table graph
     * @return a leaf node name
     */
    private String getLeafNode() {

        // go through the graph and find a node
        for(TimeTableNode table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                return MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(table.getBracketsTableName());
            }
        }

        // something went wrong this should not happen!
        throw new RuntimeException("Can't find a leaf!");
    }

    /**
     * Instantiates an operator to he specified indices
     * TODO handle union view
     *
     * @param rootTable
     * @param operator the operator we ant to instantiate
     * @param indices
     * @param generatedPlanMap
     * @param replacedPlanMap
     * @return
     */
    private Operator instantiateOperator(String rootTable,
                                         Operator operator,
                                         HashMap<String, Integer> indices,
                                         HashMap<String, Operator> generatedPlanMap,
                                         HashMap<String, ArrayList<TableScan>> replacedPlanMap) {

		// the set of all the operators we already have visited
        HashSet<Operator> finishedQueue = new HashSet<Operator>();

        // the list of all the operators that are still unprocessed
        LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();

        // add the operator we want to instantiate
        availableQueue.add(operator);

        // while there are no more operators that are unprocessed
        while (!availableQueue.isEmpty()) {

            // grab the first available operator
            Operator currentElement = availableQueue.poll();

            // check if we have processed it already, if we have just skip it
            if (!finishedQueue.contains(currentElement)) {

                // change the indices of the current operator
                currentElement.changeNodeProperty(indices, translatorHelper);

                // add the current operator to the set of processed operators so we don't visit him again
                finishedQueue.add(currentElement);

                // if the current operator is a table scan operator
                if (currentElement instanceof TableScan) {

                    // if this is not a normal table
                    if (((TableScan) currentElement).getType() != TableReference.COMMON_TABLE) {

                        // grab it's table name
                        String tableName = ((TableScan) currentElement).getTableName();

                        // check if it's a general index table
                        if (isGeneralTable(tableName)) {
                            HashMap<String, MathExpression> expressions = ((TableScan) currentElement).getIndexMathExpressions();
                            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(tableName);

                            HashMap<String, Integer> newIndices = MultidimensionalTableSchema.evaluateExpressions(expressions, indices);

                            ((TableScan) currentElement).setTableName(MultidimensionalTableSchema.getQualifiedTableNameFromIndices(prefix, newIndices));
                            ((TableScan) currentElement).setIndexStrings(newIndices);

                            /*
                             * Keep the state of current table scan, which should be replaced in the integrated plan.
                             */
                            putIntoMap(replacedPlanMap, rootTable, (TableScan) currentElement);
                        } else {
                            putIntoMap(replacedPlanMap, rootTable, (TableScan) currentElement);
                        }
                    }
                }

                // get the children of the current operator
                ArrayList<Operator> children = currentElement.getChildren();

                // if it has some children add them to the available queue
                if (children != null) {
                    for (Operator temp : children) {
                        if (!finishedQueue.contains(temp)) {
                            availableQueue.add(temp);
                        }
                    }
                }
            }
        }

        generatedPlanMap.put(rootTable, operator);
        return operator;
    }

    private String findMatchingGeneralIndexTable(String table) {

        if(table.matches("^[^_]+((\\[[0-9]+])+){2,}$")) {
            String prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(table);
            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);

            for(String randomTable : tableOperationMap.keySet()) {
                if(randomTable.startsWith(prefix)) {
                    MultidimensionalSchemaIndices indexSpecification = new MultidimensionalSchemaIndices(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(randomTable));

                    if (indexSpecification.areIndicesForThisTable(indices)){
                        return randomTable;
                    }
                }
            }

            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        String randomTableName = getTablePrefix(table) + "[i]";

        if(!tableOperationMap.containsKey(randomTableName)) {
            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        return randomTableName;
    }

    private void putIntoMap(HashMap<String, ArrayList<TableScan>> replacedPlanMap, String key, TableScan value) {
        if (replacedPlanMap.containsKey(key)) {
            ArrayList<TableScan> tempList = replacedPlanMap.get(key);
            if (!tempList.contains(value)) {
                tempList.add(value);
            }
        } else {
            ArrayList<TableScan> tempList = new ArrayList<TableScan>();
            tempList.add(value);
            replacedPlanMap.put(key, tempList);
        }
    }

    /**
     * Gets the prefix of the table
     * TODO move this somewhere
     * @param table the full name of the table in brackets form
     * @return the prefix
     */
    private String getTablePrefix(String table) {
        // find hte '[' character
        int start = table.indexOf("[");

        // if there brackets the prefix is the same as the name
        if (start < 0) {
            return table;
        }

        // return the prefix
        return table.substring(0, start);
    }

}
