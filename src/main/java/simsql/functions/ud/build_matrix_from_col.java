package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;


/**
 * A function for building a matrix from a vector.
 * The input vector is put at the column indicated by the input position. 
 * The other entries of the matrix are 0.
 * 
 * @author Shangyu
 */
public class build_matrix_from_col extends ReflectedUDFunction {

  public static double[][] build(double[] vec, double pos) {

    double[][] m = new double[vec.length][vec.length];
    int mypos = (int)pos;
    for (int i = 0; i < vec.length; i++)
      m[i][mypos] = vec[i];

    return m;
  }

  public build_matrix_from_col() {
    super("simsql.functions.ud.diag_matrix", "build", new AttributeType(new MatrixType("matrix[][]")), double[].class, double.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new DoubleType()));
    setOutputType(new AttributeType(new MatrixType("matrix[a][a]")));
  }

  @Override
  public String getName() {
    return "build_matrix_from_col";
  }
     
}
