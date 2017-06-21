package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class cross_entropy_derivative extends UDFunction {

    public cross_entropy_derivative() {
        super("/simsql/runtime/CrossEntropyDerivative.ud.so");
    }
}
