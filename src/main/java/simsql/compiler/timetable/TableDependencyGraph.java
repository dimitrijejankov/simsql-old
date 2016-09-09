package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalSchemaExpressions;
import simsql.compiler.MultidimensionalTableSchema;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

import static simsql.compiler.MultidimensionalTableSchema.getTableNameFromIndices;
import static simsql.compiler.MultidimensionalTableSchema.getTableNameFromBracketsName;

public class TableDependencyGraph {

    private HashMap<String, LinkedList<TimeTableNode>> nodes;
    private HashMap<String, HashSet<String>> backwardEdges;

    public TableDependencyGraph(LinkedList<TimeTableNode> finalNodes, HashMap<String, HashSet<String>> backwardEdges) {

        this.backwardEdges = backwardEdges;
        this.nodes = new HashMap<String, LinkedList<TimeTableNode>>();

        generateGraph(finalNodes);
    }

    void generateGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<TimeTableNode>(finalNodes);
        HashSet<String> visitedNodes = new HashSet<String>();

        while (processingList.size() != 0) {

            TimeTableNode activeNode = processingList.getFirst();

            HashSet<String> edges = findBackwardEdge(activeNode);
            LinkedList<TimeTableNode> tables = evaluateDependentTables(edges, activeNode.indexStrings);

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

    LinkedList<TimeTableNode> evaluateDependentTables(HashSet<String> edges, HashMap<String, Integer> indexStrings) {

        LinkedList<TimeTableNode> retValue = new LinkedList<TimeTableNode>();

        for (String edge : edges) {

            String prefix = getTableNameFromBracketsName(edge);
            MultidimensionalSchemaExpressions expressions = new MultidimensionalSchemaExpressions(edge.substring(edge.indexOf("[")));
            HashMap<String, Integer> edgeIndex = expressions.evaluateExpressions(indexStrings);
            TimeTableNode node = new TimeTableNode(getTableNameFromIndices(prefix, edgeIndex), edgeIndex);

            retValue.add(node);
        }

        return retValue;
    }

    HashSet<String> findBackwardEdge(TimeTableNode node) {
        String tableName = MultidimensionalTableSchema.getTableNameFromQualifiedName(node.tableName);
        String bracketsTableName = tableName + "[" + node.indexStrings.get("i") + "]";

        // If this is a general index table
        if(node.indexStrings.size() == 1) {
            if(backwardEdges.containsKey(bracketsTableName)) {
                return backwardEdges.get(bracketsTableName);
            }
            else {
                return backwardEdges.get(tableName + "[i]");
            }
        }

        throw new RuntimeException("Failed to find an appropriate edge.");
    }

}
