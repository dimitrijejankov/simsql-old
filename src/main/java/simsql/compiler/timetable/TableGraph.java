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
     * the pair of an backward edge and a dependent table
     */
    class pair {

        /**
         * the backward edge
         */
        BackwardEdge edge;

        /**
         * the dependent table
         */
        HashSet<String> dependentTables;

        public pair(BackwardEdge backwardEdge, HashSet<String> dependentTables) {
            this.edge = backwardEdge;
            this.dependentTables = dependentTables;
        }
    }

    /**
     * The nodes of the table graph
     */
    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;

    /**
     * The the (table, set of dependent tables) pairs in general index form
     */
    private HashMap<String, pair> generalIndexBackwardEdges;

    /**
     * The the (table, set of dependent tables) pairs in multidimensional general index form
     */
    private HashMap<BackwardEdge, HashSet<String>> multidimensionalBackwardEdges;


    TableGraph(HashMap<String, HashSet<String>> backwardEdges, LinkedList<TimeTableNode> requiredTables) {

        // initializes the nodes
        this.nodes = new HashMap<>();

        // process the backward edges so we do this faster
        processBackwardEdges(backwardEdges);

        // generate the table graph
        generateTableGraph(requiredTables);
    }

    private void processBackwardEdges(HashMap<String, HashSet<String>> backwardEdges) {

        // initialize the general index backward edges
        generalIndexBackwardEdges = new HashMap<>();

        // initialize the multidimensional edges
        multidimensionalBackwardEdges = new HashMap<>();

        for (String edge : backwardEdges.keySet()) {

            String tablePrefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(edge);

            // if this is a general index table
            if(edge.equals(tablePrefix + "[i]")) {
                generalIndexBackwardEdges.put(tablePrefix + "[i]", new pair(new BackwardEdge(edge), backwardEdges.get(edge)));
            }
            // if this is a multidimensional index table
            else if (edge.matches("^[^_]+((\\[[0-9]+to[0-9]])+|(\\[[0-9]+to])|(\\[[0-9]+]))+$")) {
                multidimensionalBackwardEdges.put(new BackwardEdge(edge), backwardEdges.get(edge));
            }
        }
    }

    /**
     * Generates the table dependency graph for the tables that are in the final nodes list
     *
     * @param finalNodes the list of the final nodes
     */
    private void generateTableGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<>(finalNodes);
        HashSet<TimeTableNode> visitedNodes = new HashSet<>();

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

        if(multidimensionalBackwardEdges.containsKey(node.getBracketsTableName())) {
            return multidimensionalBackwardEdges.get(node.getBracketsTableName());
        }

        if(node.isGeneralIndexTable()) {
            return generalIndexBackwardEdges.get(node.getGeneralIndexName()).dependentTables;
        }

        for (BackwardEdge edge : multidimensionalBackwardEdges.keySet()) {

            MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(edge.getQualifiedName());
            String prefix = edge.getPrefix();

            if (indices.areIndicesForThisTable(node.getIndexStrings()) && prefix.equals(node.getTableName())) {
                return multidimensionalBackwardEdges.get(edge);
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

        HashSet<TimeTableNode> retValue = new HashSet<>();

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

    /**
     * Generate the rule map (what table depends on what tables
     * @return the rule map
     */
    public HashMap<String, HashSet<String>> generateRuleMap() {

        HashMap<String, HashSet<String>> ret = new HashMap<>();

        for(TimeTableNode key : nodes.keySet()) {

            HashSet<String> tmp = new HashSet<>();

            for(TimeTableNode node : nodes.get(key)) {
                tmp.add(node.getBracketsTableName());
            }

            ret.put(key.getBracketsTableName(), tmp);
        }

        return ret;
    }

}
