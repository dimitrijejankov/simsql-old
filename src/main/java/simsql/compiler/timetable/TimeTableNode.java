package simsql.compiler.timetable;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.MultidimensionalTableSchema;

import java.util.HashMap;

import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;

public class TimeTableNode {

    @JsonProperty("table-name")
    private String tableName;

    @JsonProperty("index-strings")
    private HashMap<String, Integer> indexStrings;

    @JsonCreator
    public TimeTableNode(@JsonProperty("table-name") String tableName, @JsonProperty("index-strings") HashMap<String, Integer> indexStrings) {
        this.tableName = tableName;
        this.indexStrings = indexStrings;
    }

    public TimeTableNode(String tableName) {
        this.indexStrings = MultidimensionalTableSchema.getIndicesFromBracketsName(tableName);
        this.tableName = MultidimensionalTableSchema.getPrefixFromBracketsTableName(tableName);
    }

    public String getTableName() {
        return tableName;
    }

    public HashMap<String, Integer> getIndexStrings() {
        return indexStrings;
    }

    @JsonIgnore
    public String getBracketsTableName() {
        return MultidimensionalTableSchema.getBracketsTableNameFromIndices(tableName, indexStrings);
    }

    @JsonIgnore
    public String getQualifiedTableName() {
        return MultidimensionalTableSchema.getQualifiedTableNameFromIndices(tableName, indexStrings);
    }

    @JsonIgnore
    public String getGeneralIndexName() {
        String ret = tableName;

        for(int i = 0; i < indexStrings.size(); ++i) {
            ret += "[" + labelingOrder[i] + "]";
        }

        return ret;
    }

    @JsonIgnore
    public boolean isGeneralIndexTable() {
        return indexStrings != null && indexStrings.size() == 1;
    }

    @JsonIgnore
    public boolean isMultidmensionalIndexTable() {
        return indexStrings != null && indexStrings.size() == 0;
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
