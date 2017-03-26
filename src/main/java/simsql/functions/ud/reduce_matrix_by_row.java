package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for summing up all rows of a matrix.
 *
 * @author Jacob
 */
public class reduce_matrix_by_row extends ReflectedUDFunction {

    public static double[][] get_sum(double mat[][]) {

        int row = mat.length;
        int col = mat[0].length;

        double[][] out = new double[1][col];

        for(int k = 0; k < row; k++) {
            for (int i = 0; i < col; i++) {
                out[0][i] += mat[k][i];
            }
        }

        return out;
    }

    public reduce_matrix_by_row() {
        super("simsql.functions.ud.reduce_matrix_by_row", "get_sum", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[][]")));
        setOutputType(new AttributeType(new MatrixType("matrix[][]")));
    }

    @Override
    public String getName() {
        return "reduce_matrix_by_row";
    }
}
