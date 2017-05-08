package simsql.compiler.timetable;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;


import simsql.compiler.operators.Operator;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;

public class BipartiteGraph {

    private HashMap<String, Operator> tableOperationMap;

    public BipartiteGraph(LinkedList<TimeTableNode> requiredTables, HashMap<String, HashSet<String>> backwardEdges, HashMap<Operator, String> planTableMap) {

        tableOperationMap = new HashMap<String, Operator>();

        ObjectMapper mapper = new ObjectMapper();

        for(Operator o : planTableMap.keySet()) {
            tableOperationMap.put(planTableMap.get(o), o);
            try {
                String jsonInString = mapper.writeValueAsString(o);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        String x = "";
        try {
            x = mapper.writerFor(new TypeReference<HashMap<String, Operator>>() {}).writeValueAsString(this.tableOperationMap);

        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        String t = x + "!@3";
    }
}
