package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the exp for each element in a vector.
 *
 * @author Shangyu
 */
public class exp_vector extends ReflectedUDFunction {

    public static double[] execute(double[] vec) {

        double t[] = new double[vec.length + 1];
        for (int i = 0; i < vec.length; i++)
            t[i] = Math.exp(vec[i]);
	t[vec.length] = -1;
        return t;
    }

    public exp_vector() {
        super("simsql.functions.ud.exp_vector", "execute", new AttributeType(new VectorType()), double[].class);
        setInputTypes(new AttributeType(new VectorType("vector[a]")));
        setOutputType(new AttributeType(new VectorType("vector[a]")));
    }

    @Override
    public String getName() {
        return "exp_vector";
    }
}
