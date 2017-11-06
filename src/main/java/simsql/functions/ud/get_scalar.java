package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for fetching scalars from a vector.
 *
 * @author Jacob
 */
public class get_scalar extends ReflectedUDFunction {

  public static double[] get(double vec[], int label) {

    double [] scalar = new double[2];
    scalar[0] = vec[label];
    scalar[1] = (double) label;

    return scalar;
  }

  public get_scalar() {
    super("simsql.functions.ud.get_scalar", "get", new AttributeType(new ScalarType()), double[].class, int.class);
    //setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()));
    //setOutputType(new AttributeType(new ScalarType()));
  }

  @Override
  public String getName() {
    return "get_scalar";
  }
}