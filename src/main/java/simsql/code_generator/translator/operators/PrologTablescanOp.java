package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Tablescan operator.
 */
public class PrologTablescanOp extends PrologQueryOperator {

    // the set of output attributes
    private PrologQueryRelation relation;
    private ArrayList<PrologAttributeExp> attributes = new ArrayList<PrologAttributeExp>();


    public PrologTablescanOp(TupleTerm t,
                             HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                             HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(3);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the relation
        relation = new PrologQueryRelation(relationTuples.get(t.getTerm(1)), attributeTuples);

        // get the attributes
        attributes = PrologAttributeExp.fromList((ListTerm) t.getTerm(2), attributeTuples, false);
    }

    public Type getType() {
        return Type.TABLESCAN;
    }

    public PrologQueryRelation getRelation() {
        return relation;
    }

    public ArrayList<PrologAttributeExp> getAttributes() {
        return attributes;
    }

    public String toString() {
        return getType() + " " + relation + " " + attributes;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
