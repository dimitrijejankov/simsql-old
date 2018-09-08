package simsql.functions.ud;

import simsql.runtime.*;

/**
 * Created by jacobgao on 7/24/17.
 */
public class scatter_update_two extends ReflectedUDFunction {

    public static double[][] update(double[][] mat, double[] indices1, double[][] delta1, double[] indices2, double[][] delta2) {

        // size of the chunk
        int row = indices1.length;
        int col = mat[0].length;

        for(int k = 0; k < row; k++) {
            for(int i = 0; i < col; i++) {
                mat[(int)indices1[k]][i] -= delta1[k][i];
            }
        }

        row = indices2.length;
        for(int k = 0; k < row; k++) {
            for(int i = 0; i < col; i++) {
                mat[(int)indices2[k]][i] -= delta2[k][i];
            }
        }

        // return the output...
        return mat;
    }

    public scatter_update_two() {
        super("simsql.functions.ud.scatter_update_two", "update", new AttributeType(new MatrixType()), double[][].class, double[].class, double[][].class, double[].class, double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new VectorType("vector[c]")), new AttributeType(new MatrixType("matrix[c][b]")),
                                                                         new AttributeType(new VectorType("vector[d]")), new AttributeType(new MatrixType("matrix[d][b]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
    }

    @Override
    public String getName() {
        return "scatter_update_two";
    }
}
