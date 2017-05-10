package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalSchemaExpressions;
import simsql.compiler.MultidimensionalSchemaIndices;
import simsql.compiler.MultidimensionalTableSchema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;

/**
 * Figures out all the dependencies between tables
 */
class TableGraph {

    /**
     * The nodes of the table graph
     */
    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;

    /**
     * The the (table, set of dependent tables) pairs in general index form
     */
    private HashMap<String, HashSet<String>> backwardEdges;


    TableGraph(HashMap<String, HashSet<String>> backwardEdges,
                      LinkedList<TimeTableNode> requiredTables) {


        // initializes the nodes
        this.nodes = new HashMap<TimeTableNode, HashSet<TimeTableNode>>();

        // stores the backward edges
        this.backwardEdges = backwardEdges;

        // generate the table graph
        generateTableGraph(requiredTables);
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
     * Finds the backward edges for a given table
     * @param node the table to find the backward edges for
     * @return the set of backward edges
     */
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

    /**
     * Extracts a leaf node from the table graph
     * @return a leaf node name
     */
    TimeTableNode getNextTable() {

        // go through the graph and find a node
        for(TimeTableNode table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {

                // remove the table from the graph and update dependencies
                updateTableGraph(table);

                // return the table
                return table;
            }
        }

        // something went wrong this should not happen!
        throw new RuntimeException("Can't find a leaf!");
    }

    // returns true if it has more tables in the graph
    boolean hasNextTable() {
        return !this.nodes.isEmpty();
    }

    /**
     * Removes the node from the graph and updates the dependencies
     * @param node the node to be removed
     */
    private void updateTableGraph(TimeTableNode node) {

        // remove the node from the graph
        nodes.remove(node);

        // remove the dependency to this node from every node
        for(HashSet<TimeTableNode> n : nodes.values()) {
            n.remove(node);
        }
    }

}
