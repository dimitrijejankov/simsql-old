package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Join operator.
 */
public class PrologJoinOp extends PrologQueryOperator {

    // cartinality types.
    enum Type {
        ONETOONE, MANYTOMANY, MANYTOONE, ONETOMANY, UNKNOWN
    }


    // the type
    private Type type;

    // the join expressions.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologJoinOp(TupleTerm t,
                        HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                        HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(3);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the expressions
        exps = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);


        // get the type.
        try {
            type = Type.valueOf(((AtomTerm) t.getTerm(2)).getValue().toUpperCase());
        } catch (Exception e) {

            // just in case we have one of those nasty underscores...
            type = Type.UNKNOWN;
        }
    }

    public PrologQueryOperator.Type getType() {
        return PrologQueryOperator.Type.JOIN;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
        return exps;
    }

    public Type getJoinType() {
        return type;
    }

    public String toString() {
        return getType() + " " + exps + " <" + type + ">";
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
