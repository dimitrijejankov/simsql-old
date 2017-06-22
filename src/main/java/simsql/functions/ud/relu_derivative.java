package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class relu_derivative extends UDFunction {

    public relu_derivative() {
        super("/simsql/runtime/ReLUDerivative.ud.so");
    }
}
