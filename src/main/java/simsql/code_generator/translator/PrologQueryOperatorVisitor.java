package simsql.code_generator.translator;

import simsql.code_generator.translator.operators.*;

/**
 * Visitor interface for operators.
 */
public interface PrologQueryOperatorVisitor<E> {

    E visit(PrologSelectionOp op);

    E visit(PrologProjectionOp op);

    E visit(PrologJoinOp op);

    E visit(PrologTablescanOp op);

    E visit(PrologSeedOp op);

    E visit(PrologVGWrapperOp op);

    E visit(PrologScalarFunctionOp op);

    E visit(PrologGeneralAggregateOp op);

    E visit(PrologSplitOp op);

    E visit(PrologAntiJoinOp op);

    E visit(PrologSemiJoinOp op);

    E visit(PrologDedupOp op);

    E visit(PrologFrameOutputOp op);
}
