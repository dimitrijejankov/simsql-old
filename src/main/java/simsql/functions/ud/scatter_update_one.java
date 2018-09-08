package simsql.functions.ud;

import simsql.runtime.*;

/**
 * Created by jacobgao on 7/24/17.
 */
public class scatter_update_one extends ReflectedUDFunction {

    public static double[][] update(double[][] mat, double[] indices, double[][] delta) {

        // size of the chunk
        int row = indices.length;
        int col = mat[0].length;

        for(int k = 0; k < row; k++) {
            for(int i = 0; i < col; i++) {
                mat[(int)indices[k]][i] -= delta[k][i];
            }
        }

        // return the output...
        return mat;
    }

    public scatter_update_one() {
        super("simsql.functions.ud.scatter_update_one", "update", new AttributeType(new MatrixType()), double[][].class, double[].class, double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new VectorType("vector[c]")), new AttributeType(new MatrixType("matrix[c][b]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
    }

    @Override
    public String getName() {
        return "scatter_update_one";
    }
}
