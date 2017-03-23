package simsql.functions.ud;


import simsql.runtime.AttributeType;
import simsql.runtime.DoubleType;
import simsql.runtime.MatrixType;
import simsql.runtime.ReflectedUDFunction;

public class max_value extends ReflectedUDFunction {

    public static double max_ma(double[][] m) {

        double max_value = Float.MIN_VALUE;

        for (double[] row : m) {
            for (double value : row) {
                max_value = max_value < value ? value : max_value;
            }
        }

        return max_value;
    }

    public max_value() {
        super("simsql.functions.ud.max_value", "max_ma", new AttributeType(new DoubleType()), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")));
        setOutputType(new AttributeType(new DoubleType()));
    }

    @Override
    public String getName() {
        return "max_value";
    }

}
