package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Scalar function operator.
 */
public class PrologScalarFunctionOp extends PrologQueryOperator {

    // expressions, input and output attributes.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();

    public PrologScalarFunctionOp(TupleTerm t,
                                  HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                                  HashMap<AtomTerm, TupleTerm> exprTuples) {


        // check the arity.
        t.checkArity(4);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the set of expressions
        exps = PrologQueryExpression.fromListOfTuples((ListTerm) t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);

        // get assignment expressions
        while (true) {

            // identify them by their UNKNOWN atribute.
            int ix = exps.indexOf(new PrologAttributeExp());
            if (ix < 0)
                break;

            // set it.
            exps.set(ix, PrologAttributeExp.fromList((ListTerm) ((ListTerm) t.getTerm(2)).get(ix), attributeTuples, true).get(0));
        }

        // get the set of input attributes
        inputAtts = PrologAttributeExp.fromListOfLists((ListTerm) t.getTerm(2), attributeTuples, true);
        outputAtts = PrologAttributeExp.fromListOfLists((ListTerm) t.getTerm(3), attributeTuples, true);
    }

    public Type getType() {
        return Type.SCALARFUNC;
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

    public String toString() {
        return getType() + " " + exps + " " + inputAtts + " -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
