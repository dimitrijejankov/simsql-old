package simsql.compiler.operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIdentityReference;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.math_operators.MathOperator;

import java.util.ArrayList;

/**
 * Helper class to link the columns with the math operator they correspond to...
 */
public class ColumnList {

    /**
     * The math operator
     */
    @JsonProperty("operator")
    @JsonIdentityReference(alwaysAsId = true)
    public MathOperator operator;

    /**
     * The columns
     */
    @JsonProperty("columns")
    public ArrayList<String> columns;

    @JsonCreator
    ColumnList(@JsonProperty("operator") MathOperator operator, @JsonProperty("columns") ArrayList<String> columns) {
        this.operator = operator;
        this.columns = columns;
    }
}
