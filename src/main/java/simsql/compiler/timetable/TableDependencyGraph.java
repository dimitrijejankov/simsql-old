package simsql.compiler.timetable;

import simsql.compiler.*;

import java.sql.Array;
import java.sql.Time;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ArrayList;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;

public class TableDependencyGraph {

    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;
    private HashMap<String, HashSet<String>> backwardEdges;
    private HashMap<String, Double> tableOperatorCostSum;
    private HashMap<TimeTableNode, Double> tableCosts;


    private Integer time;

    public TableDependencyGraph(LinkedList<TimeTableNode> finalNodes, HashMap<String, HashSet<String>> backwardEdges, HashMap<String, Double> tableOperatorCostSum) {

        this.backwardEdges = backwardEdges;
        this.nodes = new HashMap<TimeTableNode, HashSet<TimeTableNode>>();
        this.time = 0;
        this.tableOperatorCostSum = tableOperatorCostSum;
        this.tableCosts = new HashMap<TimeTableNode, Double>();

        generateGraph(finalNodes);
    }

    private void generateGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<TimeTableNode>(finalNodes);
        HashSet<TimeTableNode> visitedNodes = new HashSet<TimeTableNode>();

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
            tableCosts.put(node, tableOperatorCostSum.get(tableName));
            return backwardEdges.get(tableName);
        }

        if(backwardEdges.containsKey(tablePrefix + "[i]")){
            tableCosts.put(node, tableOperatorCostSum.get(tablePrefix + "[i]"));
            return backwardEdges.get(tablePrefix + "[i]");
        }

        for(String edge : backwardEdges.keySet()) {

            if(edge.matches("^[^_]+((\\[[0-9]+to[0-9]])+|(\\[[0-9]+to])|(\\[[0-9]+]))+$")) {
                MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(edge));
                String prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(edge);

                if (indices.areIndicesForThisTable(node.getIndexStrings()) &&
                        prefix.equals(node.getTableName())) {
                    tableCosts.put(node, tableOperatorCostSum.get(edge));
                    return backwardEdges.get(edge);
                }
            }
        }

        throw new RuntimeException("Failed to find an appropriate edge.");
    }


    private TableByTime getNextTick() {

        TableByTime tableByTime = new TableByTime(time);

        // TODO MAKE THIS GRAPH CUTTING ALGORITHM SMART
        int nLower = 3, nHigher = 10;

        if (nodes.size() <= nLower) {
            for (TimeTableNode table : nodes.keySet()) {
                tableByTime.addTable(table.getBracketsTableName());
            }
        } else {
            // Get all source nodes as starting points
            HashSet<TimeTableNode> sourceTables = new HashSet<TimeTableNode>();
            for (TimeTableNode table : nodes.keySet()) {
                if (nodes.get(table).isEmpty()) {
                    sourceTables.add(table);
                }
            }

            HashMap<TimeTableNode, HashSet<TimeTableNode>> forwardNodes = generateForwardNodes();

            HashMap<TimeTableNode, Integer> nodeCounts = new HashMap<TimeTableNode, Integer>();
            HashMap<TimeTableNode, Double> nodeCosts = new HashMap<TimeTableNode, Double>();
            HashSet<TimeTableNode> sinkTables = new HashSet<TimeTableNode>(nodes.keySet());
            sinkTables.removeAll(forwardNodes.keySet());
            for (TimeTableNode sink : sinkTables) {
                buildNodeCountsAndCosts(sink, nodeCounts, nodeCosts);
            }

            HashMap<TimeTableNode, Integer> sourceCounts = new HashMap<TimeTableNode, Integer>();
            HashMap<TimeTableNode, Double> sourceCosts = new HashMap<TimeTableNode, Double>();
            HashMap<TimeTableNode, TimeTableNode> sourcePaths = new HashMap<TimeTableNode, TimeTableNode>();
            for (TimeTableNode source : sourceTables) {
                int sourceCount = 1;
                double sourceCost = tableCosts.get(source);
                TimeTableNode sourcePath = source;
                TimeTableNode currentTable = source;
                while (forwardNodes.containsKey(currentTable) && sourceCount <= nHigher) {
                    HashSet<TimeTableNode> parentTables = forwardNodes.get(currentTable);
                    double minCost = Double.MAX_VALUE;
                    for (TimeTableNode parent : parentTables) {
                        if (nodeCosts.get(parent) < minCost && nodeCounts.get(parent) <= nHigher) {      // greedy
                            minCost = nodeCosts.get(parent);
                            currentTable = parent;
                        }
                    }
                    if (minCost == Double.MAX_VALUE) {
                        break;
                    } else {
                        sourceCount = nodeCounts.get(currentTable);
                        sourceCost = minCost;
                        sourcePath = currentTable;
                    }
                }
                sourceCounts.put(source, sourceCount);
                sourceCosts.put(source, sourceCost);
                sourcePaths.put(source, sourcePath);
            }

            boolean foundResult = false;
            // 1. sort sourceCosts by cost in ascending order
            LinkedList<Map.Entry<TimeTableNode, Double>> sourceCostsList =
                    new LinkedList<Map.Entry<TimeTableNode, Double>>(sourceCosts.entrySet());

            Collections.sort(sourceCostsList, new Comparator<Map.Entry<TimeTableNode, Double>>() {
                public int compare(Map.Entry<TimeTableNode, Double> o1, Map.Entry<TimeTableNode, Double> o2) {
                    return (o1.getValue()).compareTo(o2.getValue());
                }
            });

            // 2.1. starting from the smallest cost, pick the one whose corresponding count is not smaller than nLower
            for (Map.Entry<TimeTableNode, Double> entry : sourceCostsList) {
                if (sourceCounts.get(entry.getKey()) >= nLower) {
                    foundResult = true;
                    // 2.2. recover all the nodes from its path and add to tableByTime
                    traverseChildTables(tableByTime, sourcePaths.get(entry.getKey()));
                    break;
                }
            }

            // 3.1. if all counts are smaller than nLower, sort sourceCounts by count in descending order
            if (!foundResult) {
                LinkedList<Map.Entry<TimeTableNode, Integer>> sourceCountsList =
                        new LinkedList<Map.Entry<TimeTableNode, Integer>>(sourceCounts.entrySet());

                Collections.sort(sourceCountsList, new Comparator<Map.Entry<TimeTableNode, Integer>>() {
                    public int compare(Map.Entry<TimeTableNode, Integer> o1, Map.Entry<TimeTableNode, Integer> o2) {
                        return (o2.getValue()).compareTo(o1.getValue());
                    }
                });
                // 3.2. add from the largest count to tableByTime until nLower is met
                int countToZero = nLower;
                for (Map.Entry<TimeTableNode, Integer> entry : sourceCountsList) {
                    if (entry.getValue() <= countToZero) {
                        countToZero -= entry.getValue();
                        traverseChildTables(tableByTime, sourcePaths.get(entry.getKey()));
                        if (countToZero == 0) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
        }

        /**
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
         } */

        for(String table : tableByTime.getTableSet()) {
            updateEdges(tableByTime, table);
        }

        // Update forwardEdges after removing those tables!

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

    private HashMap<TimeTableNode, HashSet<TimeTableNode>> generateForwardNodes()
    {
        HashMap<TimeTableNode, HashSet<TimeTableNode>> forwardNodes = new HashMap<TimeTableNode, HashSet<TimeTableNode>>();
        for(Object o: nodes.keySet())
        {
            TimeTableNode table = (TimeTableNode)o;

            HashSet<TimeTableNode> referencedTableSet = nodes.get(o);
            for(TimeTableNode referencedTable: referencedTableSet)
            {
                HashSet<TimeTableNode> forwardSet;
                if(forwardNodes.containsKey(referencedTable))
                {
                    forwardSet = forwardNodes.get(referencedTable);
                }
                else
                {
                    forwardSet = new HashSet<TimeTableNode>();
                    forwardNodes.put(referencedTable, forwardSet);
                }

                forwardSet.add(table);
            }
        }
        return forwardNodes;
    }

    private void traverseChildTables(TableByTime tableByTime, TimeTableNode parent) {
        tableByTime.addTable(parent.getBracketsTableName());
        for (TimeTableNode child : nodes.get(parent)) {
            traverseChildTables(tableByTime, child);
        }
    }

    private void buildNodeCountsAndCosts(TimeTableNode sink,
                                         HashMap<TimeTableNode, Integer> nodeCounts,
                                         HashMap<TimeTableNode, Double> nodeCosts) {
        if (nodes.get(sink).isEmpty()) {
            if (!nodeCounts.containsKey(sink)) {
                nodeCounts.put(sink, 1);
                nodeCosts.put(sink, tableCosts.get(sink));
            }
        } else {
            for (TimeTableNode child : nodes.get(sink)) {
                buildNodeCountsAndCosts(child, nodeCounts, nodeCosts);
            }
            if (!nodeCounts.containsKey(sink)) {
                int sinkCount = 1;
                double sinkCost = tableCosts.get(sink);
                for (TimeTableNode child : nodes.get(sink)) {
                    sinkCount += nodeCounts.get(child);
                    sinkCost += nodeCosts.get(child);
                }
                nodeCounts.put(sink, sinkCount);
                nodeCosts.put(sink, sinkCost);
            }
        }
    }

    public HashMap<String, HashSet<String>> generateRuleMap() {

        HashMap<String, HashSet<String>> ret = new HashMap<String, HashSet<String>>();

        for(TimeTableNode key : nodes.keySet()) {

            HashSet<String> tmp = new HashSet<String>();

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
