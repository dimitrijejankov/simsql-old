package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The split operator.
 */
public class PrologSplitOp extends PrologQueryOperator {

    // the list of attributes to split with
    private ArrayList<PrologAttributeExp> splitAtts = new ArrayList<PrologAttributeExp>();

    // simple constructor.
    public PrologSplitOp(TupleTerm t,
                         HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                         HashMap<AtomTerm, TupleTerm> exprTuples) {

        // check the arity.
        t.checkArity(2);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the attributes
        splitAtts = PrologAttributeExp.fromList((ListTerm) t.getTerm(1), attributeTuples, true);
    }

    public Type getType() {
        return Type.SPLIT;
    }

    public ArrayList<PrologAttributeExp> getSplitAtts() {
        return splitAtts;
    }

    public String toString() {
        return getType() + " " + splitAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
