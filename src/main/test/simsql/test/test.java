package simsql.test;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import simsql.compiler.operators.Operator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;


public class test {
    public static void main(String[] args) throws IOException {

        // JSON object loader
        ObjectMapper mapper = new ObjectMapper();

        // Make a class loader
        ClassLoader classLoader = ClassLoader.getSystemClassLoader();

        // opens the table operation map
        File tableOperationMapFile = new File(classLoader.getResource("simsql/test/table-operation-map.json").getFile());

        // deserialize the table operation map
        HashMap<String, Operator> tableOperationMap = mapper.readValue(tableOperationMapFile, new TypeReference<HashMap<String, Operator>>() {});

    }
}
