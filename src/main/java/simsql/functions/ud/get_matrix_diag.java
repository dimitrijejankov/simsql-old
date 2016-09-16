package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;


/**
 * A function for getting the diagonal vector from a matrix.
 *
 * @author Shangyu
 */
public class get_matrix_diag extends ReflectedUDFunction {

  public static double[] get_diag(double[][] mat) {

    double[] dia = new double[mat.length + 1];
    for (int i = 0; i < mat.length; i++)
      dia[i] = mat[i][i];
    dia[mat.length] = -1;

    return dia;
  }

  public get_matrix_diag() {
    super("simsql.functions.ud.get_matrix_diag", "get_diag", new AttributeType(new VectorType("vector[]")), double[][].class);
    setInputTypes(new AttributeType(new MatrixType("matrix[a][a]")));
    setOutputType(new AttributeType(new VectorType("vector[a]")));
    
  }

  @Override
  public String getName() {
    return "get_matrix_diag";
  }
     
}