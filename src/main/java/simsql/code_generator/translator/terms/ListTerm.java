package simsql.code_generator.translator.terms;

import simsql.code_generator.translator.PrologTerm;
import simsql.code_generator.translator.TermType;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A list term, with a sequence of values that can be iterated.
 */
public class ListTerm implements PrologTerm, Iterable<PrologTerm> {

    private ArrayList<PrologTerm> values;

    // constructor
    public ListTerm(ArrayList<PrologTerm> valuesIn) {
        values = valuesIn;
    }

    // type getter
    public TermType getType() {
        return TermType.LIST;
    }

    // iterator
    public Iterator<PrologTerm> iterator() {
        return values.iterator();
    }

    // element getter
    public PrologTerm get(int i) {
        return values.get(i);
    }

    // make a string.
    public String toString() {

        if (values.isEmpty())
            return "[]";

        String outStr = "[" + values.get(0).toString();
        for (int i = 1; i < values.size(); i++) {
            outStr += ", " + values.get(i);
        }

        return outStr + "]";
    }
}
