package simsql.functions.ud;

import simsql.runtime.Attribute;
import simsql.runtime.MatrixAttribute;
import simsql.runtime.UDFunction;


public class matrix_transpose_matrix_multiply extends UDFunction {

    public matrix_transpose_matrix_multiply() {
        super("/simsql/runtime/MatrixTransposeMatrixMultiply.ud.so");
    }
}
