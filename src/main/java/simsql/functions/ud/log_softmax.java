package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class log_softmax extends UDFunction {

    public log_softmax() {
        super("/simsql/runtime/LogSoftmax.ud.so");
    }
}
