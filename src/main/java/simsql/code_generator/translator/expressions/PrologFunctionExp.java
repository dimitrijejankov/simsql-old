package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.PrologTerm;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A function expression, with name and parameters.
 */
public class PrologFunctionExp extends PrologQueryExpression {

    // function name (not the same as the expression name!)
    private String function;

    // all the parameters
    private ArrayList<PrologQueryExpression> params = new ArrayList<PrologQueryExpression>();

    public PrologFunctionExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                             HashMap<AtomTerm, TupleTerm> exprTuples) {

        // check the arity...
        t.checkArity(3);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the function
        function = ((AtomTerm) t.getTerm(1)).getValue();

        // now, go through the list of parameters...
        for (PrologTerm tt : (ListTerm) t.getTerm(2)) {

            // each one is, by itself, a list.
            ListTerm tl = (ListTerm) tt;

            // i.e. a pair
            params.add(PrologQueryExpression.fromChildTuple((AtomTerm) tl.get(0), (AtomTerm) tl.get(1), vgTuples, attributeTuples, relationTuples, exprTuples));
        }
    }

    public Type getType() {
        return Type.FUNCTION;
    }

    public String getFunction() {
        return function;
    }

    public ArrayList<PrologQueryExpression> getParams() {
        return params;
    }

    public String toString() {
        return function + "(" + params + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
