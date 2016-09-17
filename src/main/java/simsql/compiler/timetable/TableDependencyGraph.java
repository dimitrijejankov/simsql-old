package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalSchemaExpressions;
import simsql.compiler.MultidimensionalSchemaIndices;
import simsql.compiler.MultidimensionalTableSchema;
import simsql.compiler.TableByTime;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;

public class TableDependencyGraph {

    private HashMap<String, HashSet<TimeTableNode>> nodes;
    private HashMap<String, HashSet<String>> backwardEdges;

    private Integer time;

    public TableDependencyGraph(LinkedList<TimeTableNode> finalNodes, HashMap<String, HashSet<String>> backwardEdges) {

        this.backwardEdges = backwardEdges;
        this.nodes = new HashMap<String, HashSet<TimeTableNode>>();
        this.time = 0;

        generateGraph(finalNodes);
    }

    private void generateGraph(LinkedList<TimeTableNode> finalNodes) {

        HashSet<TimeTableNode> processingList = new HashSet<TimeTableNode>(finalNodes);
        HashSet<String> visitedNodes = new HashSet<String>();

        while (processingList.size() != 0) {

            // set the fist element from the HashSet
            TimeTableNode activeNode = processingList.iterator().next();

            HashSet<String> edges = findBackwardEdge(activeNode);
            HashSet<TimeTableNode> tables = evaluateDependentTables(edges, activeNode.indexStrings);

            // only adds not previously visited tables to the processing list
            for (TimeTableNode table : tables) {
                if(!visitedNodes.contains(table.tableName)) {
                    processingList.add(table);
                }
            }

            // adds the dependencies for the table
            nodes.put(activeNode.tableName, tables);

            processingList.remove(activeNode);
            visitedNodes.add(activeNode.tableName);
        }

    }

    private HashSet<TimeTableNode> evaluateDependentTables(HashSet<String> edges, HashMap<String, Integer> indexStrings) {

        HashSet<TimeTableNode> retValue = new HashSet<TimeTableNode>();

        for (String edge : edges) {

            String prefix = getPrefixFromBracketsTableName(edge);
            MultidimensionalSchemaExpressions expressions = new MultidimensionalSchemaExpressions(edge.substring(edge.indexOf("[")));
            HashMap<String, Integer> edgeIndex = expressions.evaluateExpressions(indexStrings);
            TimeTableNode node = new TimeTableNode(MultidimensionalTableSchema.getBracketsTableNameFromIndices(prefix, edgeIndex), edgeIndex);

            retValue.add(node);
        }

        return retValue;
    }

    private HashSet<String> findBackwardEdge(TimeTableNode node) {
        String tableName = node.tableName;
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

                if (indices.areIndicesForThisTable(node.indexStrings)) {
                    return backwardEdges.get(edge);
                }
            }
        }

        throw new RuntimeException("Failed to find an appropriate edge.");
    }

    public TableByTime getNextTick() {

        TableByTime tableByTime = new TableByTime(time);

        for(String table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                tableByTime.addTable(table);
            }
        }

        for(String table : tableByTime.getTableSet()) {
            updateEdges(tableByTime, table);
        }

        time++;
        return tableByTime;
    }

    private void updateEdges(TableByTime tableByTime, String table) {

        nodes.remove(table);
        TimeTableNode node = new TimeTableNode(table, new HashMap<String, Integer>());

        for(String tmp : nodes.keySet()) {
            if(nodes.get(tmp).remove(node)){
                tableByTime.addEdge(table, tmp);
            }
        }
    }

    public ArrayList<String> getStartingPoint(){
        ArrayList<String> ret = new ArrayList<String>();

        for(String table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                ret.add(table);
            }
        }

        return ret;
    }

    public HashMap<String, HashSet<String>> generateRuleMap() {

        HashMap<String, HashSet<String>> ret = new HashMap<String, HashSet<String>>();

        for(String key : nodes.keySet()) {

            HashSet<String> tmp = new HashSet<String>();

            for(TimeTableNode node : nodes.get(key)) {
                tmp.add(node.tableName);
            }

            ret.put(key, tmp);
        }

        return ret;
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
