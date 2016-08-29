package simsql.compiler;

import java.util.LinkedHashMap;

public class MultidimensionalSchemaIndices {

    LinkedHashMap<String, MultidimensionalSchemaIndexSpecification> indices;

    public MultidimensionalSchemaIndices() {
        indices = new LinkedHashMap<String, MultidimensionalSchemaIndexSpecification>();
    }

    public void add(String idx, MultidimensionalSchemaIndexSpecification spec) {
        indices.put(idx, spec);
    }
}
