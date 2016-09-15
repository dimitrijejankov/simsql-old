package simsql.code_generator.translator.expressions;

import simsql.code_generator.translator.PrologQueryExpression;
import simsql.code_generator.translator.PrologQueryExpressionVisitor;
import simsql.code_generator.translator.terms.AtomTerm;

/**
 * A simple literal value. No type information.
 */
public class PrologLiteralExp extends PrologQueryExpression {

    private String value;

    public PrologLiteralExp(AtomTerm t) {
        name = "<literal>";
        value = t.getValue();
    }

    public String getValue() {
        return value;
    }

    public Type getType() {
        return Type.LITERAL;
    }

    public String toString() {
        return value;
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
        return visitor.visit(this);
    }

    public boolean equals(Object o) {
        if (o == this)
            return true;

        if (!(o instanceof PrologLiteralExp))
            return false;

        return ((PrologLiteralExp) o).getValue().equals(value);
    }

    public int hashCode() {
        return value.hashCode();
    }
}
