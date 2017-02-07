package simsql.compiler.timetable;

import simsql.compiler.MultidimensionalTableSchema;

import java.util.HashMap;

public class TimeTableNode {

    private String tableName;
    private HashMap<String, Integer> indexStrings;

    public TimeTableNode(String tableName) {
        this.indexStrings = MultidimensionalTableSchema.getIndicesFromBracketsName(tableName);
        this.tableName = MultidimensionalTableSchema.getPrefixFromBracketsTableName(tableName);
    }

    public TimeTableNode(String tableName, HashMap<String, Integer> indexStrings) {
        this.tableName = tableName;
        this.indexStrings = indexStrings;
    }

    public String getBracketsTableName() {
        return MultidimensionalTableSchema.getBracketsTableNameFromIndices(tableName, indexStrings);
    }

    public String getTableName() {
        return tableName;
    }

    public HashMap<String, Integer> getIndexStrings() {
        return indexStrings;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof TimeTableNode && toString().equals(obj.toString());
    }

    @Override
    public int hashCode() {
        // the i index is added to get a more even distribution
        return tableName.hashCode() + indexStrings.get("i");
    }

    @Override
    public String toString() {
        return MultidimensionalTableSchema.getBracketsTableNameFromIndices(tableName, indexStrings);
    }
}
