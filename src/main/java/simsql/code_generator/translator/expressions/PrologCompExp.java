package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.HashMap;

/**
 * A comparison expression.
 */
public class PrologCompExp extends PrologQueryExpression {

    // comparison operator types.
    public enum Op {
        EQUALS, NOTEQUALS, LESSTHAN, GREATERTHAN, LESSEQUALS, GREATEREQUALS
    }

    // the operator
    private Op op;

    // expression on the left -- always an attribute
    private PrologAttributeExp left;

    // expression on the right.
    private PrologQueryExpression right;

    public PrologCompExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                         HashMap<AtomTerm, TupleTerm> exprTuples) {

        // ensure that it has the right arity.
        t.checkArity(5);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the operator
        op = Op.valueOf(((AtomTerm) t.getTerm(1)).getValue().toUpperCase());

        // get the attribute on the left.
        left = new PrologAttributeExp(attributeTuples.get(t.getTerm(2)));

        // get the expression on the right.
        right = PrologQueryExpression.fromChildTuple((AtomTerm) t.getTerm(3), (AtomTerm) t.getTerm(4), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public Type getType() {
        return Type.COMPEXP;
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
            case EQUALS:
                return "(" + left + " = " + right + ")";
            case NOTEQUALS:
                return "(" + left + " != " + right + ")";
            case LESSTHAN:
                return "(" + left + " < " + right + ")";
            case GREATERTHAN:
                return "(" + left + " > " + right + ")";
            case LESSEQUALS:
                return "(" + left + " <= " + right + ")";
            case GREATEREQUALS:
                return "(" + left + " >= " + right + ")";
        }

        return "";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
