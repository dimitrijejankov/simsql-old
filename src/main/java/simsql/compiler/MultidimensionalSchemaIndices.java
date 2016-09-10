package simsql.compiler;

import java.util.HashMap;
import java.util.LinkedHashMap;

public class MultidimensionalSchemaIndices {

    public final static String[] labelingOrder = { "i", "j", "k", "l", "m", "n", "o",
                                                    "p", "q", "r", "s", "t", "u", "v",
                                                    "w", "x", "y", "z", "a", "b", "c",
                                                    "d", "e", "f", "g", "h" };

    LinkedHashMap<String, MultidimensionalSchemaIndexSpecification> indices;

    public MultidimensionalSchemaIndices() {
        indices = new LinkedHashMap<String, MultidimensionalSchemaIndexSpecification>();
    }

    /**
     * Creates a new instance of MultidimensionalSchemaIndices from a general multidimensional table name.
     * @param generalTableName For example the input string resembles the following format : md_2_2to3_3to.
     */
    public MultidimensionalSchemaIndices(String generalTableName) {
        indices = new LinkedHashMap<String, MultidimensionalSchemaIndexSpecification>();

        int offset = generalTableName.indexOf("_");
        String tableName = generalTableName.substring(offset + 1);

        String parts[] = tableName.split("_");

        for(int i = 0; i < parts.length; ++i) {
            indices.put(labelingOrder[i], new MultidimensionalSchemaIndexSpecification(parts[i]));
        }
    }

    public void add(String idx, MultidimensionalSchemaIndexSpecification spec) {
        if(indices.containsKey(idx)){
            throw new RuntimeException("Can't use the index " + idx + " twice");
        }

        indices.put(idx, spec);
    }

    public String getSuffix() {

        String ret = "";

        for(String key : indices.keySet()) {
            ret += "_" + indices.get(key).getStringValue();
        }

        return ret;
    }

    public String getBracketsSuffix() {
        String ret = "";

        for(String key : indices.keySet()) {
            ret += "[" + indices.get(key).getStringValue() + "]";
        }

        return ret;
    }


    Boolean checkLabelingOrder() {

        int i = 0;

        for(String key : indices.keySet()) {
            if(!key.equals(labelingOrder[i]))
                return false;

            i++;
        }

        return true;
    }

    public Boolean areIndicesForThisTable(HashMap<String, Integer> ids) {

        if(ids.size() != indices.size())
            return false;

        for(String id : ids.keySet()) {
            if(!indices.get(id).checkRange(ids.get(id))) {
                return false;
            }
        }

        return true;
    }
}
