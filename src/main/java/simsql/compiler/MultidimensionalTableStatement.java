package simsql.compiler;

import java.util.ArrayList;

public class MultidimensionalTableStatement extends RandomTableStatement {

    public MultidimensionalTableStatement(MultidimensionalTableSchema schema,
                                          SQLExpression outerTable,
                                          ArrayList<WithStatement> withList,
                                          SelectStatement selectStatement) {
        super(schema, outerTable, withList, selectStatement);
    }

    public boolean acceptVisitor(MultidimensionalTableTypeChecker astVisitor) throws Exception {
        return astVisitor.visitMultidimensionalTableStatement(this);
    }
}
