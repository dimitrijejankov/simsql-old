package simsql.functions.ud;

import simsql.runtime.AttributeType;
import simsql.runtime.MatrixType;
import simsql.runtime.ReflectedUDFunction;

/**
 * A function for calculating the relu derivative.
 *
 * @author Jacob
 */
public class relu_derivative extends ReflectedUDFunction {

    public static double[][] execute(double[][] mat) {

        int row = mat.length;
        int col = mat[0].length;

        double[][] out = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                out[i][j] = (mat[i][j] > 0) ? 1.0 : 0.0;
            }
        }

        return out;
    }

    public relu_derivative() {
        super("simsql.functions.ud.relu_derivative", "execute", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")));
        setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
    }

    @Override
    public String getName() {
        return "relu_derivative";
    }
}