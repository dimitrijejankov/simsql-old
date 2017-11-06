package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the tanh of a vector.
 *
 * @author Shangyu
 */
public class tanh_vector extends ReflectedUDFunction {

    public static double[] tanh(double[] vec) {

        double t[] = new double[vec.length + 1];
        for (int i = 0; i < vec.length; i++)
            t[i] = Math.tanh(vec[i]);
	t[vec.length] = -1;
        return t;
    }

    public tanh_vector() {
        super("simsql.functions.ud.tanh_vector", "tanh", new AttributeType(new VectorType()), double[].class);
        setInputTypes(new AttributeType(new VectorType("vector[a]")));
        setOutputType(new AttributeType(new VectorType("vector[a]")));
    }

    @Override
    public String getName() {
        return "tanh_vector";
    }
}
