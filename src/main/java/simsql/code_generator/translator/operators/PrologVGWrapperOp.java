package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * VGWrapper operator.
 */
public class PrologVGWrapperOp extends PrologQueryOperator {

    // name of the VG wrapper (not the same as the node name!)
    private String id;

    // the VG function it calls.
    private PrologQueryVGFunction function;

    // the input seed attribute.
    private PrologAttributeExp inputSeedAtt;

    // the output seed attribute.
    private PrologAttributeExp outputSeedAtt;

    // list of input attributes.
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();

    // list of output attributes.
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();

    public PrologVGWrapperOp(TupleTerm t,
                             HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                             HashMap<AtomTerm, TupleTerm> exprTuples) {

        // check the arity.
        t.checkArity(9);

        // get the name
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the ID
        id = ((AtomTerm) t.getTerm(1)).getValue();

        // get the vg function
        function = new PrologQueryVGFunction(vgTuples.get(t.getTerm(2)), attributeTuples);

        // get the input seed attribute
        inputSeedAtt = new PrologAttributeExp(attributeTuples.get(t.getTerm(3)));

        // get the input attributes
        inputAtts = PrologAttributeExp.fromList((ListTerm) t.getTerm(4), attributeTuples, false);
        outputAtts = PrologAttributeExp.fromList((ListTerm) t.getTerm(7), attributeTuples, true);

        // get the output seed attribute
        outputSeedAtt = new PrologAttributeExp(attributeTuples.get(t.getTerm(8)));
    }

    public Type getType() {
        return Type.VGWRAPPER;
    }

    public String getID() {
        return id;
    }

    public PrologQueryVGFunction getFunction() {
        return function;
    }

    public PrologAttributeExp getInputSeed() {
        return inputSeedAtt;
    }

    public PrologAttributeExp getOutputSeed() {
        return outputSeedAtt;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
        return inputAtts;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
        return outputAtts;
    }

    public String toString() {
        return getType() + " " + function + "(" + inputAtts + ") -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
