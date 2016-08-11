package simsql.compiler;


import java.util.ArrayList;

public class BaseLineModuloRandomTableStatement extends RandomTableStatement {

    String multiplier;
    String offset;

    public BaseLineModuloRandomTableStatement(DefinedTableSchema schema,
                                              SQLExpression outerTable,
                                              ArrayList<WithStatement> withList,
                                              SelectStatement selectStatement) {
        super(schema, outerTable, withList, selectStatement);
    }

    public BaseLineModuloRandomTableStatement(String multiplier,
                                              String offset,
                                              DefinedTableSchema schema,
                                              SQLExpression outerTable,
                                              ArrayList<WithStatement> withList,
                                              SelectStatement selectStatement) {
        super(schema, outerTable, withList, selectStatement);
        this.multiplier = multiplier;
        this.offset = offset;
    }
}
