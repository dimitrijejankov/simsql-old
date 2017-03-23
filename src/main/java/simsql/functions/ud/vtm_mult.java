package simsql.functions.ud;

import simsql.runtime.UDFunction;
import simsql.runtime.UDWrapper;
import simsql.runtime.VGFunction;

public class vtm_mult extends UDFunction {
    private static VGFunction udf;
    private static UDWrapper udw;

    static {

        // the corresponding UDF
        udf = new VGFunction("/simsql/runtime/VectorTransposeMatrixMultiply.ud.so");

        // only one UDWrapper related with each UDFunction
        udw = new UDWrapper(udf);
    };

    public vtm_mult() {
        super(udf, udw);
    }

    @Override
    public String getName() {
        return "vtm_mult";
    }
}
