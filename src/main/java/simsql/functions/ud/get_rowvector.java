package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for fetching row vectors from a matrix.
 *
 * @author Jacob
 */
public class get_rowvector extends ReflectedUDFunction {

  public static double[] get(double[][] mat, int label) {

    double [] vector = new double[mat[label].length + 1];
    for (int i = 0; i < mat[label].length; i++)
      vector[i] = mat[label][i];
    vector[mat[label].length] = (double) label;

    return vector;
  }

  public get_rowvector() {
    super("simsql.functions.ud.get_rowvector", "get", new AttributeType(new VectorType("vector[]")), double[][].class, int.class);
    setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[b]")));
  }

  @Override
  public String getName() {
    return "get_rowvector";
  }
}