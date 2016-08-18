package simsql.compiler;


import java.util.ArrayList;

public class ModuloRandomTableStatement extends GeneralRandomTableStatement {

    String multiplier;
    String offset;

    public ModuloRandomTableStatement(DefinedTableSchema schema,
                                      SQLExpression outerTable,
                                      ArrayList<WithStatement> withList,
                                      SelectStatement selectStatement) {
        super(schema, outerTable, withList, selectStatement);
    }

    public ModuloRandomTableStatement(String multiplier,
                                      String offset,
                                      DefinedTableSchema schema,
                                      SQLExpression outerTable,
                                      ArrayList<WithStatement> withList,
                                      SelectStatement selectStatement) {
        super(schema, outerTable, withList, selectStatement);
        this.multiplier = multiplier;
        this.offset = offset;
    }

    public boolean acceptVisitor(RandomModuloTableTypeChecker visitor)throws Exception
    {
        return visitor.visitModuloRandomTableStatement(this);
    }
}
