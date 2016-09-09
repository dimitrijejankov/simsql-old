package simsql.compiler.timetable;

import java.util.HashMap;

public class TimeTableNode {

    String tableName;
    HashMap<String, Integer> indexStrings;

    public TimeTableNode(String tableName, HashMap<String, Integer> indexStrings) {
        this.tableName = tableName;
        this.indexStrings = indexStrings;
    }
}
