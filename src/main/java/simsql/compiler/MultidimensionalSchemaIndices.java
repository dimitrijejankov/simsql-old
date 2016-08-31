package simsql.compiler;

import java.util.LinkedHashMap;

public class MultidimensionalSchemaIndices {

    private final static String[] labelingOrder = { "i", "j", "k", "l", "m", "n", "o",
                                                    "p", "q", "r", "s", "t", "u", "v",
                                                    "w", "x", "y", "z", "a", "b", "c",
                                                    "d", "e", "f", "g", "h" };

    LinkedHashMap<String, MultidimensionalSchemaIndexSpecification> indices;

    public MultidimensionalSchemaIndices() {
        indices = new LinkedHashMap<String, MultidimensionalSchemaIndexSpecification>();
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
            ret += indices.get(key).getStringValue();
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
}
