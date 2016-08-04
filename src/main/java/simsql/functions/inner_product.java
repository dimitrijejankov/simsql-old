package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for inner product.
 *
 * @author Jacob
 */
public class inner_product extends ReflectedUDFunction {

  public static double inner(double[] vec1, double[] vec2) {

    double sum = 0.0;
    for (int i = 0; i < vec1.length; i++)
      sum += vec1[i] * vec2[i];

    return sum;
  }

  public inner_product() {
    super("simsql.functions.inner_product", "inner", new AttributeType(new DoubleType()), double[].class, double[].class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new VectorType("vector[a]")));
    setOutputType(new AttributeType(new DoubleType()));
  }

  @Override
  public String getName() {
    return "inner_product";
  }
}