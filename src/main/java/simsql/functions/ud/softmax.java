package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class softmax extends UDFunction {

    public softmax() {
        super("/simsql/runtime/Softmax.ud.so");
    }
}
