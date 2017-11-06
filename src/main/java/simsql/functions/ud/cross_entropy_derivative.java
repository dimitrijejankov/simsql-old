package simsql.functions.ud;

import simsql.runtime.AttributeType;
import simsql.runtime.IntType;
import simsql.runtime.MatrixType;
import simsql.runtime.ReflectedUDFunction;

public class cross_entropy_derivative extends ReflectedUDFunction {

    public static double[][] cx(double[][] vec, int columnID, int category) {

        // size of the chunk
        int chunkSize = vec[0].length;

        // allocate the output
        double[][] out = new double[1][chunkSize];

        // figure out the derivative
        for(int i = 0; i < chunkSize; i++){
            if(columnID == (category / chunkSize) && i == category % chunkSize) {
                out[0][i] = vec[0][i] - 1;
            }
            else {
                out[0][i] = vec[0][i];
            }
        }

        // return the output...
        return out;
    }

    public cross_entropy_derivative() {
        super("simsql.functions.ud.cross_entropy_derivative", "cx", new AttributeType(new MatrixType("matrix[][]")), double[][].class, int.class, int.class);
        setInputTypes(new AttributeType(new MatrixType("matrix[][]")), new AttributeType(new IntType()), new AttributeType(new IntType()));
        setOutputType(new AttributeType(new MatrixType("matrix[][]")));
    }

    @Override
    public String getName() {
        return "cross_entropy_derivative";
    }
}
