package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.HashMap;

/**
 * An aggregate expression.
 */
public class PrologAggExp extends PrologQueryExpression {

    // aggregate type.
    public enum Type {
        AVG, SUM, COUNT, COUNTALL, MIN, MAX, VAR, STDEV, VECTOR, ROWMATRIX, COLMATRIX
    }

    // the type.
    private Type type;

    // the expression being aggregated.
    private PrologQueryExpression exp;

    public PrologAggExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                        HashMap<AtomTerm, TupleTerm> exprTuples) {

        t.checkArity(4);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the type
        type = Type.valueOf(((AtomTerm) t.getTerm(1)).getValue().toUpperCase());

        // get the child expression.
        exp = PrologQueryExpression.fromChildTuple((AtomTerm) t.getTerm(2), (AtomTerm) t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

        // if it's an unknown attribute, then we have a COUNTALL
        if (exp.getType() == PrologQueryExpression.Type.ATTRIBUTE && ((PrologAttributeExp) exp).getAttType().equals("unknown")) {
            type = Type.COUNTALL;
        }
    }

    public Type getAggType() {
        return type;
    }

    public PrologQueryExpression.Type getType() {
        return PrologQueryExpression.Type.AGGEXP;
    }

    public PrologQueryExpression getExp() {
        return exp;
    }

    public String toString() {
        return type + "(" + exp + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
