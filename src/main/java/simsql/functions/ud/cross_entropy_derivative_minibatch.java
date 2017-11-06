package simsql.functions.ud;

import simsql.runtime.*;

public class cross_entropy_derivative_minibatch extends ReflectedUDFunction {

    public static double[][] cx(double[][] mat, int columnID, double[] category) {

        // size of the chunk
        int row = mat.length;
        int col = mat[0].length;

        // allocate the output
        double[][] out = new double[row][col];

        for(int k = 0; k < row; k++) {
            // figure out the derivative
            for (int i = 0; i < col; i++) {
                if (columnID == (((int)category[k]) / col) && i == ((int)category[k]) % col) {
                    out[k][i] = mat[k][i] - 1;
                } else {
                    out[k][i] = mat[k][i];
                }
            }
        }

        // return the output...
        return out;
    }

    public cross_entropy_derivative_minibatch() {
        super("simsql.functions.ud.cross_entropy_derivative_minibatch", "cx", new AttributeType(new MatrixType("matrix[][]")), double[][].class, int.class, double[].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new IntType()), new AttributeType(new VectorType("vector[a]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
    }

    @Override
    public String getName() {
        return "cross_entropy_derivative_minibatch";
    }
}
