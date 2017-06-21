package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class cross_entropy extends UDFunction {

    public cross_entropy() {
        super("/simsql/runtime/CrossEntropy.ud.so");
    }
}
