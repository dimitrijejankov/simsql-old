package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * This function takes a vector v and a number k so that any v[i] < k,
 * and returns a vector of length k with zeroes except in the position
 * corresponding to the label v[0].
 *
 * @author Luis
 */

public class vector_start extends ReflectedUDFunction {

  public static double[] starts(double[] v, int k) {

    double[] out = new double[k];
    for (int i=0;i<k;i++) {
      out[i] = 0.0;
    }

    int pos = (int)v[0];
    out[pos] = 1;

    return out;
  }

  public vector_start() {
    super("simsql.functions.vector_start", "starts", new AttributeType(new VectorType("vector[]")), double[].class, int.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[b]")));
  }

  @Override
  public String getName() {
    return "vector_start";
  }
}
