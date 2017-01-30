package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for summing up all elements for a vector.
 *
 * @author Shangyu
 */
public class sum_vector extends ReflectedUDFunction {

  public static double get_sum(double vec[]) {

    double sum = 0;
    for (int i = 0; i < vec.length; i++)
	sum += vec[i];

    return sum;
  }

  public sum_vector() {
    super("simsql.functions.ud.sum_vector", "get_sum", new AttributeType(new DoubleType()), double[].class);
    //setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()));
    //setOutputType(new AttributeType(new ScalarType()));
  }

  @Override
  public String getName() {
    return "sum_vector";
  }
}
