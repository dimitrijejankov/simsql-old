package simsql.code_generator.translator.terms;

import simsql.code_generator.translator.PrologTerm;
import simsql.code_generator.translator.TermType;
import simsql.code_generator.translator.terms.AtomTerm;

import java.util.ArrayList;

/**
 * A tuple term -- composed of an atom and a set of terms.
 */
public class TupleTerm implements PrologTerm {

    // the name
    private AtomTerm atom;

    // the set of attributes
    private ArrayList<PrologTerm> terms;

    // constructor
    public TupleTerm(AtomTerm atomIn, ArrayList<PrologTerm> termsIn) {
        atom = atomIn;
        terms = termsIn;
    }

    // type
    public TermType getType() {
        return TermType.TUPLE;
    }

    // helper methods.
    public AtomTerm getAtom() {
        return atom;
    }

    public int getArity() {
        return terms.size();
    }

    public void checkArity(int arity) {
        if (terms.size() != arity) {
            throw new RuntimeException("Tuple " + atom.getValue() + " has incorrect arity!");
        }
    }

    public PrologTerm getTerm(int i) {
        return terms.get(i);
    }

    // string representation.
    public String toString() {

        String termStr = terms.get(0).toString();
        for (int i = 1; i < terms.size(); i++) {
            termStr += ", " + terms.get(i).toString();
        }

        return atom.toString() + "(" + termStr + ")";
    }

}
