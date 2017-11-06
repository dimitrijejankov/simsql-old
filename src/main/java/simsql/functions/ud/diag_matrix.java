package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;


/**
 * A function for generating a diagonal matrix from a vector.
 *
 * @author Jacob
 */
public class diag_matrix extends ReflectedUDFunction {

  public static double[][] diag(double[] vec) {

    double[][] sum = new double[vec.length][vec.length];
    for (int i = 0; i < vec.length; i++)
      sum[i][i] = vec[i];

    return sum;
  }

  public diag_matrix() {
    super("simsql.functions.ud.diag_matrix", "diag", new AttributeType(new MatrixType("matrix[][]")), double[].class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")));
    setOutputType(new AttributeType(new MatrixType("matrix[a][a]")));
  }

  @Override
  public String getName() {
    return "diag_matrix";
  }
     
}