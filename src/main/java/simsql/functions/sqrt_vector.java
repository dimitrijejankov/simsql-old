package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for square root of a vector.
 *
 * @author Jacob
 */
public class sqrt_vector extends ReflectedUDFunction {

  public static double[] sqrt_vector(double[] val) {

    double [] vector = new double[val.length + 1];
    for (int i = 0; i < val.length; i++)
      vector[i] = Math.sqrt(val[i]);
    vector[val.length] = (double) -1;

    return vector;
  }

  public sqrt_vector() {
    super("simsql.functions.sqrt_vector", "sqrt_vector", new AttributeType(new VectorType("vector[a]")), double[].class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")));
    setOutputType(new AttributeType(new VectorType("vector[a]")));
  }

  @Override
  public String getName() {
    return "sqrt_vector";
  }
}