package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the sigmoid of a vector.
 *
 * @author Jacob
 */
public class sigmoid extends ReflectedUDFunction {

    public static double[] sigmoid(double[] vec) {

        double sig[] = new double[vec.length + 1];
        for (int i = 0; i < vec.length; i++)
            sig[i] = 1.0 / (1.0 + Math.exp(-vec[i]));

        return sig;
    }

    public sigmoid() {
        super("simsql.functions.ud.sigmoid", "sigmoid", new AttributeType(new VectorType()), double[].class);
        setInputTypes(new AttributeType(new VectorType("vector[a]")));
        setOutputType(new AttributeType(new VectorType("vector[a]")));
    }

    @Override
    public String getName() {
        return "sigmoid";
    }
}