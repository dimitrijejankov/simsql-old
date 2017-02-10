package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;


/**
 * A function for building a matrix from a vector.
 * The input vector is a column in the output matrix.
 * Its position is indicated by the second input (starting from 0).
 * The last input is the column dimension of the output matrix. 
 * The other entries of the matrix are 0.
 * 
 * @author Shangyu
 */
public class build_matrix_from_col extends ReflectedUDFunction {

  public static double[][] build(double[] vec, int pos, int col_dim) {

    double[][] m = new double[vec.length][col_dim];
    for (int i = 0; i < vec.length; i++)
      m[i][pos] = vec[i];

    return m;
  }

  public build_matrix_from_col() {
    super("simsql.functions.ud.build_matrix_from_col", "build", new AttributeType(new MatrixType("matrix[][]")), double[].class, int.class, int.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
  }

  @Override
  public String getName() {
    return "build_matrix_from_col";
  }
     
}
