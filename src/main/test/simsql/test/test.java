package simsql.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import simsql.compiler.operators.Operator;
import simsql.compiler.timetable.BipartiteGraph;
import simsql.compiler.timetable.TableDependencyGraph;
import simsql.compiler.timetable.TimeTableNode;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;


public class test {
    public static void main(String[] args) throws Exception {

        // JSON object loader
        ObjectMapper mapper = new ObjectMapper();

        // Make a class loader
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // opens the table operation map
        File tableOperationMapFile = new File(classLoader.getResource("simsql/test/table-operation-map.json").getFile());

        // deserialize the table operation map
        HashMap<String, Operator> tableOperationMap = mapper.readValue(tableOperationMapFile, new TypeReference<HashMap<String, Operator>>() {});

        // open the required tables file
        File requiredTablesFile = new File(classLoader.getResource("simsql/test/required-tables.json").getFile());

        // deserialize the required tables
        LinkedList<TimeTableNode> requiredTables = mapper.readValue(requiredTablesFile, new TypeReference<LinkedList<TimeTableNode>>() {});

        // open the required tables file
        File backwardEdgesFile = new File(classLoader.getResource("simsql/test/backward-edges.json").getFile());

        // deserialize the backward edges
        HashMap<String, HashSet<String>> backwardEdges = mapper.readValue(backwardEdgesFile, new TypeReference<HashMap<String, HashSet<String>>>() {});


        BipartiteGraph bp = new BipartiteGraph(requiredTables, backwardEdges, tableOperationMap);

    }
}
