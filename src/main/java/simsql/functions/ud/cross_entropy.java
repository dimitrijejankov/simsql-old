package simsql.functions.ud;

import simsql.runtime.*;

public class cross_entropy extends ReflectedUDFunction {

    public static double cx(double[][] mat, int columnID, double[] category) {

        // size of the chunk
        int row = mat.length;
        int col = mat[0].length;

        // allocate the output
        double sum = 0.0;

        for(int k = 0; k < row; k++) {
            // figure out the derivative
            for (int i = 0; i < col; i++) {
                if (columnID == (((int)category[k]) / col) && i == ((int)category[k]) % col) {
                    sum += Math.log(mat[k][i] + 1e-5);
                }
            }
        }

        // return the output...
        return -sum;
    }

    public cross_entropy() {
        super("simsql.functions.ud.cross_entropy", "cx", new AttributeType(new DoubleType()), double[][].class, int.class, double[].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new IntType()), new AttributeType(new VectorType("vector[a]")));
        setOutputType(new AttributeType(new DoubleType()));
    }

    @Override
    public String getName() {
        return "cross_entropy";
    }
}
