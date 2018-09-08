package simsql.functions.ud;

import simsql.runtime.*;

/**
 * Created by jacobgao on 7/24/17.
 */
public class multiply_by_row extends ReflectedUDFunction {

    public static double[][] multiply(double[][] mat1, double[][] mat2) {

        // size of the chunk
        int row = mat2.length;
        int col = mat2[0].length;

        double[][] res = new double[row][col];

        for(int k = 0; k < row; k++) {
            for(int i = 0; i < col; i++) {
                res[k][i] = mat1[k][0] * mat2[k][i];
            }
        }

        // return the output...
        return res;
    }

    public multiply_by_row() {
        super("simsql.functions.ud.multiply_by_row", "multiply", new AttributeType(new MatrixType()), double[][].class, double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][c]")), new AttributeType(new MatrixType("matrix[a][b]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
    }

    @Override
    public String getName() {
        return "multiply_by_row";
    }
}
