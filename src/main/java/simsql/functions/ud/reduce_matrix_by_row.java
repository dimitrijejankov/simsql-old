package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class reduce_matrix_by_row extends UDFunction {

    public reduce_matrix_by_row() {
        super("/simsql/runtime/ReduceMatrixByRow.ud.so");
    }
}
