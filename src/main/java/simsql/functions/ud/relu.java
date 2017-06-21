package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class relu extends UDFunction {

    public relu() {
        super("/simsql/runtime/ReLU.ud.so");
    }
}
