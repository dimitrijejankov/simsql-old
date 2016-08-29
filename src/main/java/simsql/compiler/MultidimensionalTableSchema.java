package simsql.compiler;

import java.util.ArrayList;

public class MultidimensionalTableSchema extends DefinedTableSchema  {

    public MultidimensionalSchemaIndices multidimensionalSchemaIndices;


    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, ArrayList<String> tableAttributeList, boolean isAligned) {
        super(viewName, tableAttributeList, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;

    }

    public MultidimensionalTableSchema(String viewName, MultidimensionalSchemaIndices multidimensionalSchemaIndices, boolean isAligned) {
        super(viewName, isAligned);
        this.multidimensionalSchemaIndices = multidimensionalSchemaIndices;
    }

    @Override
    public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
        return true;
    }
}
