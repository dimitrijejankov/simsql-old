package simsql.functions.ud;

import simsql.runtime.*;

/**
 * Created by jacobgao on 7/24/17.
 */
public class inner_product_by_row extends ReflectedUDFunction {

    public static double[][] inner(double[][] mat1, double[][] mat2) {

        // size of the chunk
        int row = mat1.length;
        int col = mat1[0].length;

        double[][] res = new double[row][1];

        for(int k = 0; k < row; k++) {
            double sum = 0.0;
            for(int i = 0; i < col; i++) {
                sum += mat1[k][i] * mat2[k][i];
            }
            res[k][0] = sum;
        }

        // return the output...
        return res;
    }

    public inner_product_by_row() {
        super("simsql.functions.ud.inner_product_by_row", "inner", new AttributeType(new MatrixType()), double[][].class, double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new MatrixType("matrix[a][b]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][c]")));
    }

    @Override
    public String getName() {
        return "inner_product_by_row";
    }
}
