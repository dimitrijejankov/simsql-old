package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.HashMap;

/**
 * Seed operator.
 */
public class PrologSeedOp extends PrologQueryOperator {

    // the one output attribute it appends.
    private PrologAttributeExp attribute;

    public PrologSeedOp(TupleTerm t,
                        HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                        HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(2);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the attribute
        attribute = new PrologAttributeExp(attributeTuples.get(t.getTerm(1)));
    }

    public Type getType() {
        return Type.SEED;
    }

    public PrologAttributeExp getAttribute() {
        return attribute;
    }

    public String toString() {
        return getType() + " " + attribute;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
