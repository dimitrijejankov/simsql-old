package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalSchemaExpressions;
import simsql.compiler.MultidimensionalSchemaIndices;
import simsql.compiler.MultidimensionalTableSchema;
import simsql.compiler.TableByTime;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;

public class TableDependencyGraph {

    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;
    private HashMap<String, HashSet<String>> backwardEdges;

    private Integer time;

    public TableDependencyGraph(LinkedList<TimeTableNode> finalNodes, HashMap<String, HashSet<String>> backwardEdges) {

        this.backwardEdges = backwardEdges;
        this.nodes = new HashMap<>();
        this.time = 0;

        generateGraph(finalNodes);
    }

    private void generateGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<>(finalNodes);
        HashSet<TimeTableNode> visitedNodes = new HashSet<>();

        while (processingList.size() != 0) {

            long l1 = System.nanoTime();

            // set the fist element from the HashSet
            TimeTableNode activeNode = processingList.iterator().next();

            long l2 = System.nanoTime();

            HashSet<String> edges = findBackwardEdge(activeNode);

            long l3 = System.nanoTime();

            HashSet<TimeTableNode> tables = evaluateDependentTables(edges, activeNode.getIndexStrings());

            long l4 = System.nanoTime();

            // only adds not previously visited tables to the processing list
            for (TimeTableNode table : tables) {
                if(!visitedNodes.contains(table)) {
                    processingList.add(table);
                }
            }

            long l5 = System.nanoTime();

            // adds the dependencies for the table
            nodes.put(activeNode, tables);

            long l6 = System.nanoTime();

            processingList.remove(activeNode);
            visitedNodes.add(activeNode);

            long l7 = System.nanoTime();

            System.out.println("t1 = " + (l2 - l1));
            System.out.println("t2 = " + (l3 - l2));
            System.out.println("t3 = " + (l4 - l3));
            System.out.println("t4 = " + (l5 - l4));
            System.out.println("t5 = " + (l6 - l5));
            System.out.println("t6 = " + (l7 - l6));

        }

    }

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

        if(backwardEdges.containsKey(tableName)) {
            return backwardEdges.get(tableName);
        }

        if(backwardEdges.containsKey(tablePrefix + "[i]")){
            return backwardEdges.get(tablePrefix + "[i]");
        }

        for(String edge : backwardEdges.keySet()) {

            if(edge.matches("^[^_]+((\\[[0-9]+to[0-9]])+|(\\[[0-9]+to])|(\\[[0-9]+]))+$")) {
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


    private TableByTime getNextTick() {

        TableByTime tableByTime = new TableByTime(time);

        // TODO MAKE THIS GRAPH CUTTING ALGORITHM SMART

        // Get the fist layer of independent tables
        for(TimeTableNode table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                tableByTime.addTable(table.getBracketsTableName());
            }
        }
        // Get the layer that just became independent
        for(TimeTableNode table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                tableByTime.addTable(table.getBracketsTableName());
            }
        }

        for(String table : tableByTime.getTableSet()) {
            updateEdges(tableByTime, table);
        }

        time++;
        return tableByTime;
    }

    private void updateEdges(TableByTime tableByTime, String table) {

        nodes.remove(new TimeTableNode(table));
        TimeTableNode node = new TimeTableNode(table);

        for(TimeTableNode tmp : nodes.keySet()) {
            if(nodes.get(tmp).remove(node)){
                tableByTime.addEdge(table, tmp.getBracketsTableName());
            }
        }
    }

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

    private int findMinimumFirstIndex() {

        int min = Integer.MAX_VALUE;

        for(TimeTableNode key : nodes.keySet()) {
            if(nodes.get(key).isEmpty()) {
                min = Math.min(key.getIndexStrings().get("i"), min);
            }
        }

        return min;
    }

    public HashMap<Integer, TableByTime> extractSimulateTableMap() {

        HashMap<Integer, TableByTime> ret = new HashMap<Integer, TableByTime>();

        while (nodes.size() != 0) {

            TableByTime tt = getNextTick();

            if(tt.getTableSet().isEmpty()) {
                throw new RuntimeException("Could not extract simulateTableMap, circles in the graph!");
            }

            ret.put(time - 1, tt);
        }

        return ret;
    }

}
