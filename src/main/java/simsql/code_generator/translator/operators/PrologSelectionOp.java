package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Selection operator.
 */
public class PrologSelectionOp extends PrologQueryOperator {

    // selection expressions
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologSelectionOp(TupleTerm t,
                             HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                             HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(2);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the expressions
        exps = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public Type getType() {
        return Type.SELECTION;
    }


    public ArrayList<PrologQueryExpression> getExpressions() {
        return exps;
    }

    public String toString() {
        return getType() + " " + exps;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
