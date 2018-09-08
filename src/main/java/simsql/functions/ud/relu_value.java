package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the (point-wise) relu of a matrix = max(0,x).
 *
 * @author Shangyu
 */
public class relu_value extends ReflectedUDFunction {


    public static double relu_fun(double val) {

	return Math.max(0, val);
    }

    public relu_value() {
        super("simsql.functions.ud.relu_value", "relu_fun", new AttributeType(new DoubleType()), double.class);
        setInputTypes(new AttributeType(new DoubleType()));
        setOutputType(new AttributeType(new DoubleType()));
    }

    @Override
    public String getName() {
        return "relu_value";
    }
}
