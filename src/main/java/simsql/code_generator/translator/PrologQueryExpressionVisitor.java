package simsql.code_generator.translator;

import simsql.code_generator.translator.expressions.*;

/**
 * Visitor interface for expressions.
 */
public interface PrologQueryExpressionVisitor<E> {

    E visit(PrologAttributeExp exp);

    E visit(PrologLiteralExp exp);

    E visit(PrologCompExp exp);

    E visit(PrologBoolExp exp);

    E visit(PrologAggExp exp);

    E visit(PrologArithExp exp);

    E visit(PrologFunctionExp exp);
}
