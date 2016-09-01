package simsql.compiler;

import java.util.ArrayList;
import java.util.HashMap;
import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;

public class MultidimensionalTableSchema extends DefinedTableSchema  {

    public MultidimensionalSchemaIndices multidimensionalSchemaIndices;


    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, ArrayList<String> tableAttributeList, boolean isAligned) {
        super(viewName, tableAttributeList, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;

        if(!multidimensionalSchemaIndices.checkLabelingOrder())
            throw new RuntimeException("Wrong index order!");
    }

    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, boolean isAligned) {
        super(viewName, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;
    }

    @Override
    public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
        return astVisitor.visitMultidimensionalTableSchemaExpression(this);
    }

    @Override
    public String getViewName() {
        return super.getViewName() + multidimensionalSchemaIndices.getSuffix();
    }

    /**
    This method extracts the table name from it's qualified name.
    For example, if the qualified name is "md_1_2_3 the table name would be md
     **/
    public static String getTableNameFromQualifiedName(String qualifiedName){
        int offset = qualifiedName.indexOf('_');
        return qualifiedName.substring(0, offset);
    }

    /**
     * Extracts the indices from a table qualified name
     * @param qualifiedName For example, if the qualified name is "md_1_2_3" the indices would be 1, 2, 3
     * @return hash map with indices and their integer values.
     */
    public static HashMap<String, Integer> getIndicesFromQualifiedName(String qualifiedName){
        HashMap<String, Integer> ret = new HashMap<String, Integer>();

        int offset = qualifiedName.indexOf('_');
        String[] parts = qualifiedName.substring(offset+1).split("_");

        for (int i = 0; i < parts.length; ++i) {
            ret.put(labelingOrder[i], Integer.parseInt(parts[i]));
        }

        return ret;
    }
}
