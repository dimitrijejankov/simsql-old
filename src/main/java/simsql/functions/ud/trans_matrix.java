package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for transposing a matrix.
 *
 * @author Jacob
 */
public class trans_matrix extends ReflectedUDFunction {

  public static double[][] transpose(double[][] mat) {

    int row = mat.length;
    int col = mat[0].length;

    double[][] trans = new double[col][row];
    for (int i = 0; i < col; i++)
      for (int j = 0; j < row; j++)
      trans[i][j] = mat[j][i];

    return trans;
  }

  public trans_matrix() {
    super("simsql.functions.ud.trans_matrix", "transpose", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
    setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")));
    setOutputType(new AttributeType(new MatrixType("matrix[b][a]")));
  }

  @Override
  public String getName() {
    return "trans_matrix";
  }
}