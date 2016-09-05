package simsql.compiler;


import simsql.runtime.DataType;
import java.util.ArrayList;

public class MultidimensionalTableTypeChecker extends RandomTableTypeChecker {

    public MultidimensionalTableTypeChecker(boolean allowDuplicatedAttributeAlias) throws Exception {
        super(allowDuplicatedAttributeAlias);
    }

    public boolean visitMultidimensionalTableStatement(
            MultidimensionalTableStatement multidimensionalTableStatement) throws Exception
    {
        return super.visitRandomTableStatement(multidimensionalTableStatement);
    }

    @Override
    public void saveView(DefinedTableSchema tableAttributes, ArrayList<DataType> gottenAttributeTypeList, String sql) throws Exception {

        if(!(tableAttributes instanceof MultidimensionalTableSchema))
            throw new RuntimeException("Schema has to be a MultidimensionalTableSchema");

        MultidimensionalTableSchema schema = (MultidimensionalTableSchema)tableAttributes;

        ArrayList<Attribute> schemaAttributes = saveAttributes(schema, gottenAttributeTypeList);
        String viewName = schema.getViewName();

        View view = new View(viewName, sql, schemaAttributes, DataAccess.OBJ_MULRELATION);
        catalog.addView(view);

        String realViewName = viewName.substring(0, viewName.indexOf("_"));

        catalog.addIndexTable(realViewName, viewName);
        catalog.addMCDependecy(viewName, this.getIndexedRandomTableList());
    }
}
