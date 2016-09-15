package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A generalized aggregate operator
 */
public class PrologGeneralAggregateOp extends PrologQueryOperator {

    // the aggregate id -- not the same as the node name!
    private String id;

    // grouping attributes.
    private ArrayList<PrologAttributeExp> groupByAtts = new ArrayList<PrologAttributeExp>();

    // aggregate expressions.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    // all the input attributes.
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();

    // all the output attributes.
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();

    public PrologGeneralAggregateOp(TupleTerm t,
                                    HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                                    HashMap<AtomTerm, TupleTerm> exprTuples) {

        // check the arity.
        t.checkArity(6);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the ID
        id = ((AtomTerm) t.getTerm(1)).getValue();

        // get the group-by attributes
        groupByAtts = PrologAttributeExp.fromList((ListTerm) t.getTerm(2), attributeTuples, true);

        // get the expressions
        exps = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

        // get the input attributes
        inputAtts = PrologAttributeExp.fromListOfLists((ListTerm) t.getTerm(4), attributeTuples, true);
        outputAtts = PrologAttributeExp.fromListOfLists((ListTerm) t.getTerm(5), attributeTuples, true);
    }

    public Type getType() {
        return Type.GENAGG;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
        return exps;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
        return inputAtts;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
        return outputAtts;
    }

    public ArrayList<PrologAttributeExp> getGroupByAtts() {
        return groupByAtts;
    }

    public String getID() {
        return id;
    }

    public String toString() {
        return getType() + " " + exps + " BY " + groupByAtts + "; " + inputAtts + " -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
