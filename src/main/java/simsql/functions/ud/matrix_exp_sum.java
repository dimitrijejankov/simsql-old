package simsql.functions.ud;

import simsql.runtime.*;

public class matrix_exp_sum extends ReflectedUDFunction {

    public static double inner(double[][] m, double max_value) {

        double sum = 0.0;
        for (double[] aM : m) {
            for (int j = 0; j < m[0].length; j++) {
                sum += Math.exp(aM[j] - max_value);
            }
        }

        return sum;
    }

    public matrix_exp_sum() {
        super("simsql.functions.ud.matrix_exp_sum", "inner", new AttributeType(new DoubleType()), double[][].class, double.class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new DoubleType()));
        setOutputType(new AttributeType(new DoubleType()));
    }

    @Override
    public String getName() {
        return "matrix_exp_sum";
    }

}
