package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for fetching column vectors from a matrix.
 *
 * @author Jacob
 */
public class get_colvector extends ReflectedUDFunction {

  public static double[] get(double[][] mat, int label) {

    double [] vector = new double[mat.length + 1];
    for (int i = 0; i < mat.length; i++)
      vector[i] = mat[i][label];
    vector[mat.length] = (double) label;

    return vector;
  }

  public get_colvector() {
    super("simsql.functions.get_colvector", "get", new AttributeType(new VectorType("vector[]")), double[][].class, int.class);
    setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[a]")));
  }

  @Override
  public String getName() {
    return "get_colvector";
  }
}