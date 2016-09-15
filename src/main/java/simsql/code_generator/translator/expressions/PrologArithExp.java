package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.HashMap;

/**
 * An arithmetic expression, with its operator.
 */
public class PrologArithExp extends PrologQueryExpression {

    // arithmetic operator type
    public enum Op {
        PLUS, MINUS, TIMES, DIVIDE
    }

    // operator
    private Op op;

    // expression on the LHS
    private PrologQueryExpression left;

    // expression on the RHS
    private PrologQueryExpression right;

    public PrologArithExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                          HashMap<AtomTerm, TupleTerm> exprTuples) {


        // ensure that it has the right arity.
        t.checkArity(6);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the operator
        op = Op.valueOf(((AtomTerm) t.getTerm(1)).getValue().toUpperCase());

        // get the attribute on the left.
        left = PrologQueryExpression.fromChildTuple((AtomTerm) t.getTerm(2), (AtomTerm) t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

        // get the expression on the right.
        right = PrologQueryExpression.fromChildTuple((AtomTerm) t.getTerm(4), (AtomTerm) t.getTerm(5), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public Type getType() {
        return Type.ARITHEXP;
    }

    public Op getOp() {
        return op;
    }

    public PrologQueryExpression getLeft() {
        return left;
    }

    public PrologQueryExpression getRight() {
        return right;
    }

    public String toString() {
        switch (op) {
            case PLUS:
                return "(" + left + " + " + right + ")";
            case MINUS:
                return "(" + left + " - " + right + ")";
            case TIMES:
                return "(" + left + " - " + right + ")";
            case DIVIDE:
                return "(" + left + " / " + right + ")";
        }

        return "";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
