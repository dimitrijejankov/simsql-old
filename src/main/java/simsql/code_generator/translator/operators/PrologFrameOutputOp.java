package simsql.code_generator.translator.operators;

import simsql.code_generator.translator.*;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The SimSQL frame output operator.
 */
public class PrologFrameOutputOp extends PrologQueryOperator {

    // set of operators to keep, only the node names.
    private ArrayList<String> sourceNames = new ArrayList<String>();

    // set of output files for those operators.
    private ArrayList<String> outputFiles = new ArrayList<String>();

    public PrologFrameOutputOp(TupleTerm t,
                               HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
                               HashMap<AtomTerm, TupleTerm> exprTuples) {

        // verify the arity
        t.checkArity(3);

        // get the name.
        name = ((AtomTerm) t.getTerm(0)).getValue();

        // get the set of sources
        for (PrologTerm tt : (ListTerm) t.getTerm(1)) {
            sourceNames.add(((AtomTerm) tt).getValue());
        }

        // get the set of output files
        for (PrologTerm tt : (ListTerm) t.getTerm(2)) {
            outputFiles.add(((AtomTerm) tt).getValue());
        }

    }

    public Type getType() {
        return Type.FRAMEOUTPUT;
    }

    public ArrayList<String> getSources() {
        return sourceNames;
    }

    public ArrayList<String> getOutputFiles() {
        return outputFiles;
    }

    public String toString() {
        return getType() + " " + sourceNames + " -> " + outputFiles;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
        return visitor.visit(this);
    }
}
