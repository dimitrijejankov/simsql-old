package simsql.functions.ud;

import simsql.runtime.*;

public class matrix_matrix_multiply extends UDFunction {
    private static VGFunction udf;
    private static UDWrapper udw;

    static {

        // the corresponding UDF
        udf = new VGFunction("/simsql/runtime/MatrixMatrixMultiply.ud.so");

        // only one UDWrapper related with each UDFunction
        udw = new UDWrapper(udf);
    };

    public matrix_matrix_multiply() {
        super(udf, udw);
    }

    @Override
    public String getName() {
        return "matrix_matrix_multiply";
    }
}
