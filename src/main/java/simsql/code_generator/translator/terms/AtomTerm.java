package simsql.code_generator.translator.terms;

import simsql.code_generator.translator.PrologTerm;
import simsql.code_generator.translator.TermType;

/**
 * A numeric or string or atomic term.
 */
public class AtomTerm implements PrologTerm {

    // the value itself, as a string.
    private String value;

    // constructor
    public AtomTerm(String valueIn) {
        value = valueIn;
    }

    // type identifier.
    public TermType getType() {
        return TermType.ATOM;
    }

    // helper method
    public String getValue() {
        return value;
    }

    // make it a string
    public String toString() {
        return value;
    }

    // these methods are for the HashMap...
    public boolean equals(Object o) {
        if (this == o)
            return true;

        if (o == null)
            return false;

        if (!(o instanceof AtomTerm))
            return false;

        return ((AtomTerm) o).getValue().equals(value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
