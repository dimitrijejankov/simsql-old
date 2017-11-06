package simsql.functions.ud;

import simsql.runtime.AttributeType;
import simsql.runtime.MatrixType;
import simsql.runtime.ReflectedUDFunction;

public class exp_matrix extends ReflectedUDFunction {

    public static double[][] execute(double[][] mat) {

        int rows = mat.length;
        int columns = mat[0].length;

        double t[][] = new double[rows][columns];

        for (int i = 0; i < rows; i++) {
            for(int j = 0; j < columns; j++) {
                t[i][j] = Math.exp(mat[i][j]);
            }
        }

        return t;
    }

    public exp_matrix() {
        super("simsql.functions.ud.exp_matrix", "execute", new AttributeType(new MatrixType()), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("vector[a][b]")));
        setOutputType(new AttributeType(new MatrixType("vector[a][b]")));
    }

    @Override
    public String getName() {
        return "exp_matrix";
    }
}