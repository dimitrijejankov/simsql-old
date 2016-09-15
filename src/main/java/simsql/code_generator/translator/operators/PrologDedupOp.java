package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The duplicate removal operator.
 */
public class PrologDedupOp extends PrologQueryOperator {

    // set of attributes
    private ArrayList<PrologAttributeExp> atts = new ArrayList<PrologAttributeExp>();

    public PrologDedupOp(TupleTerm t,
                         HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                         HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(2);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the attributes
        atts = PrologAttributeExp.fromList((ListTerm) t.getTerm(1), attributeTuples, true);
    }

    public Type getType() {
        return Type.DEDUP;
    }

    public ArrayList<PrologAttributeExp> getAtts() {
        return atts;
    }

    public String toString() {
        return getType() + " " + atts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
