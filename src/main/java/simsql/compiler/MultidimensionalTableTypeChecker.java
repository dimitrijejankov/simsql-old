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
        ArrayList<Attribute> schema = saveAttributes(tableAttributes, gottenAttributeTypeList);
        String viewName = tableAttributes.getViewName();

        View view = new View(viewName, sql, schema, DataAccess.OBJ_MULRELATION);
        catalog.addView(view);

        int end = viewName.indexOf("_");
        String realViewName = viewName.substring(0, end);
        catalog.addIndexTable(realViewName, viewName);

        catalog.addMCDependecy(viewName, this.getIndexedRandomTableList());
    }
}
