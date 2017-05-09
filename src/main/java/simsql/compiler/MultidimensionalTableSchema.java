package simsql.compiler;

import simsql.compiler.expressions.MathExpression;

import java.util.ArrayList;
import java.util.HashMap;

import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;

public class MultidimensionalTableSchema extends DefinedTableSchema {

    public MultidimensionalSchemaIndices multidimensionalSchemaIndices;


    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, ArrayList<String> tableAttributeList, boolean isAligned) {
        super(viewName, tableAttributeList, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;

        if (!multidimensionalSchemaIndices.checkLabelingOrder())
            throw new RuntimeException("Wrong index order!");
    }

    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, boolean isAligned) {
        super(viewName, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;
    }

    @Override
    public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitMultidimensionalTableSchemaExpression(this);
    }

    @Override
    public String getViewName() {
        return super.getViewName() + multidimensionalSchemaIndices.getSuffix();
    }

    /**
     * This method extracts the table prefix from it's qualified name.
     * For example, if the qualified name is "md_1_2_3 the table prefix would be md
     **/
    public static String getTablePrefixFromQualifiedName(String qualifiedName) {
        int offset = qualifiedName.indexOf('_');
        return qualifiedName.substring(0, offset);
    }

    /**
     * This method extracts the table prefix from it's general name name.
     * For example, if the general table name is "md_1to_2to5_3 the table prefix would be md
     **/
    public static String getTablePrefixFromGeneralName(String generalName) {
        return getTablePrefixFromQualifiedName(generalName);
    }

    /**
     * This method extracts the table name from it's brackets name.
     * For example, if the brackets name is "md[1][2][3] the table name would be md
     **/
    public static String getPrefixFromBracketsTableName(String qualifiedName) {
        int offset = qualifiedName.indexOf('[');
        return qualifiedName.substring(0, offset);
    }

    public static String getBracketsTableNameFromQualifiedTableName(String tableName) {

        int offset = tableName.indexOf('_');
        String prefix = tableName.substring(0, offset);
        String[] parts = tableName.split("_");

        for(int i = 1; i < parts.length; ++i) {
            prefix += "[" + parts[i] + "]";
        }

        return prefix;
    }

    /**
     * Extracts the indices from a table qualified name.
     * @param qualifiedName For example, if the qualified name is "md_1_2_3" the indices would be 1, 2, 3
     * @return hash map with indices and their integer values.
     */
    public static HashMap<String, Integer> getIndicesFromQualifiedName(String qualifiedName) {
        HashMap<String, Integer> ret = new HashMap<String, Integer>();

        int offset = qualifiedName.indexOf('_');
        String[] parts = qualifiedName.substring(offset + 1).split("_");

        for (int i = 0; i < parts.length; ++i) {
            ret.put(labelingOrder[i], Integer.parseInt(parts[i]));
        }

        return ret;
    }

    public static HashMap<String, Integer> getIndicesFromBracketsName(String bracketsName) {

        String qualifiedName = getQualifiedTableNameFromBracketsTableName(bracketsName);
        HashMap<String, Integer> ret = new HashMap<String, Integer>();

        int offset = qualifiedName.indexOf('_');
        String[] parts = qualifiedName.substring(offset + 1).split("_");

        for (int i = 0; i < parts.length; ++i) {
            ret.put(labelingOrder[i], Integer.parseInt(parts[i]));
        }

        return ret;
    }


    /**
     * Generate qualified table name from prefix indices.
     * @param prefix the table prefix
     * @param indices a HashMap with key value pairs, for example {i : 1, j : 2, k : 4}
     * @return qualified table name for example tablePrefix_1_2_4
     */
    public static String getQualifiedTableNameFromIndices(String prefix, HashMap<String, Integer> indices) {

        for (int i = 0; i < indices.size(); ++i) {
            if (!indices.containsKey(labelingOrder[i]))
                throw new RuntimeException("Wrong indices order!");

            prefix += "_" + indices.get(labelingOrder[i]);
        }

        return prefix;
    }

    /**
     * Generate brackets table name from prefix indices.
     * @param prefix the table prefix
     * @param indices a HashMap with key value pairs, for example {i : 1, j : 2, k : 4}
     * @return brackets table name for example tablePrefix[1][2][4]
     */
    public static String getBracketsTableNameFromIndices(String prefix, HashMap<String, Integer> indices) {

        for (int i = 0; i < indices.size(); ++i) {
            if (!indices.containsKey(labelingOrder[i]))
                throw new RuntimeException("Wrong indices order!");

            prefix += "[" + indices.get(labelingOrder[i]) + "]";
        }

        return prefix;
    }

    /**
     * Generate general index table name from expressions
     * @param prefix the table name prefix
     * @param expressions a HashMap with key value pairs with index string as key and expression as value
     * @return general index table name for example tablePrefix_i_j_k
     */
    public static String getGeneralIndexTableNameFromExpressions(String prefix, HashMap<String, MathExpression> expressions) {
        for (int i = 0; i < expressions.size(); ++i) {
            if (!expressions.containsKey(labelingOrder[i]))
                throw new RuntimeException("Wrong indices order!");

            prefix += "_" + labelingOrder[i];
        }

        return prefix;
    }

    public static String getBracketsTableNameFromEvaluatedExpressions(String prefix,
                                                                      HashMap<String, MathExpression> indexMathExpressions,
                                                                      HashMap<String, Integer> indices) {
        HashMap<String, Integer> newIndices = evaluateExpressions(indexMathExpressions, indices);
        return getBracketsTableNameFromIndices(prefix, newIndices);
    }

    public static String getQualifiedTableNameFromEvaluatedExpressions(String prefix,
                                                                       HashMap<String, MathExpression> indexMathExpressions,
                                                                       HashMap<String, Integer> indices) {
        HashMap<String, Integer> newIndices = evaluateExpressions(indexMathExpressions, indices);
        return getQualifiedTableNameFromIndices(prefix, newIndices);
    }

    public static HashMap<String, Integer> evaluateExpressions(HashMap<String, MathExpression> expressions,
                                                               HashMap<String, Integer> indices) {
        HashMap<String, Integer> newIndices = new HashMap<String, Integer>();

        for(String key : expressions.keySet()) {
            MathExpression e = expressions.get(key);
            MPNGenerator generator = new MPNGenerator(e);
            newIndices.put(key, generator.initializeTime(indices));
        }

        return newIndices;
    }

    /**
     * Converts the brackets table name into a qualified table name
     * @param bracketsName brackets table name ex. table[1][2][3] or table[i+1][j+5]
     * @return qualified table name like ex. table_1_2_3 or table_i+1_j+5
     */
    public static String getQualifiedTableNameFromBracketsTableName(String bracketsName) {
        bracketsName = bracketsName.replace("][", "_");
        bracketsName = bracketsName.replace("[", "_");
        return bracketsName.replace("]", "");
    }

    /**
     * Generate general index table name from the index specification
     * @param prefix the table name prefix
     * @param indices a MultidimensionalSchemaIndices object that has the index specification for the table schema
     * @return general index table name for example tablePrefix_2to_5to6
     */
    public static String getGeneralIndexTableNameFromIndices(String prefix, MultidimensionalSchemaIndices indices) {
        return prefix + indices.getSuffix();
    }

    /**
     * Generate a brackets labeled general index table name from the index specification
     * @param tableName the table name in it's general index form, for example tablePrefix_2to_5to6
     * @return general index table name for example tablePrefix[i:2...][j:5...6]
     */
    public static String getLabeledBracketsGeneralIndexTableNameFromGeneralIndexTableName(String tableName) {

        String dots = tableName.replace("to", "..."); // tablePrefix_2..._5...6
        String[] splits = dots.split("_"); // ["tablePrefix", "2...", 5...6"]

        String ret = splits[0]; // "tablePrefix"

        for(int i = 1; i < splits.length; i++) {
            ret +=  "[" + labelingOrder[i-1] + ":" + splits[i] + "]";
        }

        return ret;
    }

    /**
     * Checks if the table with the name provided is a general index table ex. "tablename_i_j_k"
     * @param table the table name
     * @return true if it is false otherwise
     */
    public static boolean isGeneralTable(String table) {
        return table.matches("^[^_]+(_[a-z])+$") || table.matches("^[^_]+(\\[[a-z]])+$");
    }
}
