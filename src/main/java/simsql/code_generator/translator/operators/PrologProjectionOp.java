package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Projection operator.
 */
public class PrologProjectionOp extends PrologQueryOperator {

    // the set of output attributes
    private ArrayList<PrologAttributeExp> attributes = new ArrayList<PrologAttributeExp>();

    public PrologProjectionOp(TupleTerm t,
                              HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                              HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(2);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the attributes
        attributes = PrologAttributeExp.fromList((ListTerm) t.getTerm(1), attributeTuples, true);
    }

    public Type getType() {
        return Type.PROJECTION;
    }

    public ArrayList<PrologAttributeExp> getAttributes() {
        return attributes;
    }

    public void updateAttributes(ArrayList<String> attsIn) {

        // rearrange
        ArrayList<PrologAttributeExp> newAtts = new ArrayList<PrologAttributeExp>();
        for (String att : attsIn) {
            boolean found = false;

            for (PrologAttributeExp attExp : getAttributes()) {
                if (attExp.getName().equals(att)) {
                    newAtts.add(attExp);
                    found = true;
                    break;
                }
            }

            if (!found) {
                throw new RuntimeException("Could not find attribute " + att + " while updating materialized views!");
            }
        }

        // set anew.
        attributes = newAtts;
    }

    public String toString() {
        return getType() + " " + attributes;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
