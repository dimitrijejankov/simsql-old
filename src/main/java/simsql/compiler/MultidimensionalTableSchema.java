package simsql.compiler;

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
     * This method extracts the table name from it's qualified name.
     * For example, if the qualified name is "md_1_2_3 the table name would be md
     **/
    public static String getTableNameFromQualifiedName(String qualifiedName) {
        int offset = qualifiedName.indexOf('_');
        return qualifiedName.substring(0, offset);
    }

    /**
     * This method extracts the table name from it's general name name.
     * For example, if the general name name is "md_1to_2to5_3 the table name would be md
     **/
    public static String getTableNameFromGeneralName(String generalName) {
        return getTableNameFromQualifiedName(generalName);
    }

    /**
     * This method extracts the table name from it's brackets name.
     * For example, if the brackets name is "md[1][2][3] the table name would be md
     **/
    public static String getTableNameFromBracketsName(String qualifiedName) {
        int offset = qualifiedName.indexOf('[');
        return qualifiedName.substring(0, offset);
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


    /**
     * Generate qualified table name from prefix an indices.
     * @param prefix the table prefix
     * @param indices a HashMap with key value pairs, for example {i : 1, j : 2, k : 4}
     * @return qualified table name for example tablePrefix_1_2_4
     */
    public static String getTableNameFromIndices(String prefix, HashMap<String, Integer> indices) {

        for (int i = 0; i < indices.size(); ++i) {
            if (!indices.containsKey(labelingOrder[i]))
                throw new RuntimeException("Wrong indices order!");

            prefix += "_" + indices.get(labelingOrder[i]);
        }

        return prefix;
    }

    /**
     * Generate brackets table name from prefix an indices.
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

    public static String bracketsToQualifiedTableName(String bracketsName) {
        bracketsName = bracketsName.replace("][", "_");
        bracketsName = bracketsName.replace("[", "_");
        return bracketsName.replace("]", "_");
    }

    public static String getGeneralIndexTableNameFromIndices(String prefix, MultidimensionalSchemaIndices indices) {
        return prefix + indices.getSuffix();
    }
}
