package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The semi-join operator.
 */
public class PrologSemiJoinOp extends PrologQueryOperator {

    // the source operator, only the node name -- users must look it up.
    private String sourceName;

    // join predicates
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologSemiJoinOp(TupleTerm t,
                            HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                            HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(3);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the source
        sourceName = ((AtomTerm) t.getTerm(1)).getValue();

        // get the expressions
        exps = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(2), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public Type getType() {
        return Type.SEMIJOIN;
    }

    public String getSource() {
        return sourceName;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
        return exps;
    }

    public String toString() {
        return getType() + " " + sourceName + " " + exps;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
