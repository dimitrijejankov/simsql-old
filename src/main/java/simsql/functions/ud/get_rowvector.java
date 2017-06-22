package simsql.functions.ud;

import simsql.runtime.UDFunction;


public class get_rowvector extends UDFunction {

    public get_rowvector() {
        super("/simsql/runtime/GetRowvector.ud.so");
    }
}
