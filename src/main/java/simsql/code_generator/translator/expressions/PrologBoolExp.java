package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A boolean junction expression.
 */
public class PrologBoolExp extends PrologQueryExpression {

    // junction type.
    public enum Type {
        AND, OR, NOT, SET
    }

    // the type
    private Type type;

    // the list of children expressions
    private ArrayList<PrologQueryExpression> children = new ArrayList<PrologQueryExpression>();

    public PrologBoolExp(TupleTerm t, Type inType, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                         HashMap<AtomTerm, TupleTerm> exprTuples) {

        // arity
        t.checkArity(2);

        // set the type
        type = inType;

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the children expressions
        children = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryExpression.Type getType() {
        return PrologQueryExpression.Type.BOOLEXP;
    }

    public Type getBoolType() {
        return type;
    }

    public ArrayList<PrologQueryExpression> getChildren() {
        return children;
    }

    public String toString() {
        return type + "(" + children + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
