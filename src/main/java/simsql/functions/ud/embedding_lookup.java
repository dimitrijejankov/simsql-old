package simsql.functions.ud;

import simsql.runtime.*;

/**
 * Created by jacobgao on 7/24/17.
 */
public class embedding_lookup extends ReflectedUDFunction {

    public static double[][] lookup(double[][] mat, double[] indices) {

        // size of the chunk
        int row = indices.length;
        int col = mat[0].length;

        double[][] res = new double[row][col];

        for(int k = 0; k < row; k++) {
            for(int i = 0; i < col; i++) {
                res[k][i] = mat[(int)indices[k]][i];
            }
        }

        // return the output...
        return res;
    }

    public embedding_lookup() {
        super("simsql.functions.ud.embedding_lookup", "lookup", new AttributeType(new MatrixType()), double[][].class, double[].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new VectorType("vector[c]")));
        setOutputType(new AttributeType(new MatrixType("matrix[c][b]")));
    }

    @Override
    public String getName() {
        return "embedding_lookup";
    }
}
