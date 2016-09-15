package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.PrologTerm;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * A single attribute, with its data type.
 */
public class PrologAttributeExp extends PrologQueryExpression {

    // possible types.
    //    public enum Type {
    // INTEGER, DOUBLE, STRING, BOTTOM, SEED, UNKNOWN
    //    }

    private String type;

    //private simsql.runtime.DataType type;				// TO-DO. What about BOTTOM and UNKNOWN?

    // unknown att constructor
    public PrologAttributeExp() {
        name = "";
        type = "unknown";
    }

    public PrologAttributeExp(TupleTerm tup) {
        tup.checkArity(2);
        AtomTerm t0 = (AtomTerm) tup.getTerm(0);
        AtomTerm t1 = (AtomTerm) tup.getTerm(1);

        name = t0.getValue();
        type = t1.getValue();
    }

    public String getName() {
        return name;
    }

    public String getAttType() {
        return type;
    }

    public Type getType() {
        return Type.ATTRIBUTE;
    }

    public String toString() {
        return name;
    }

    // we use this to remove duplicates from sets.
    public boolean equals(Object o) {

        if (this == o)
            return true;

        if (o == null)
            return false;

        if (!(o instanceof PrologAttributeExp))
            return false;

        return ((PrologAttributeExp) o).getName().equals(name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    // method for working with lists of attribute names
    public static ArrayList<PrologAttributeExp> fromList(ListTerm t, HashMap<AtomTerm, TupleTerm> attributeTuples, boolean removeDuplicates) {

        ArrayList<PrologAttributeExp> outAtts = new ArrayList<PrologAttributeExp>();
        for (PrologTerm tx : t) {

            PrologAttributeExp tt = new PrologAttributeExp(attributeTuples.get(tx));
            if (!(removeDuplicates && outAtts.contains(tt))) {
                outAtts.add(tt);
            }
        }

        return outAtts;
    }

    // method for working with lists of lists of attribute names
    public static ArrayList<PrologAttributeExp> fromListOfLists(ListTerm t, HashMap<AtomTerm, TupleTerm> attributeTuples, boolean removeDuplicates) {
        ArrayList<PrologAttributeExp> outAtts = new ArrayList<PrologAttributeExp>();

        for (PrologTerm tx : t) {

            for (PrologTerm ty : (ListTerm) tx) {
                PrologAttributeExp tt = new PrologAttributeExp(attributeTuples.get(ty));
                if (!(removeDuplicates && outAtts.contains(tt))) {
                    outAtts.add(tt);
                }
            }
        }

        return outAtts;
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }

}
