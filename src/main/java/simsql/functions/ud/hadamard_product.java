package simsql.functions.ud;

import simsql.runtime.AttributeType;
import simsql.runtime.MatrixType;
import simsql.runtime.ReflectedUDFunction;

/**
 * A function for transposing a matrix.
 *
 * @author Jacob
 */
public class hadamard_product extends ReflectedUDFunction {

  public static double[][] execute(double[][] a, double[][] b) {

    int row = a.length;
    int col = a[0].length;

    double[][] trans = new double[row][col];

    for (int i = 0; i < row; i++) {
      for (int j = 0; j < col; j++) {
        trans[i][j] = a[i][j] * b[i][j];
      }
    }

    return trans;
  }

  public hadamard_product() {
    super("simsql.functions.ud.hadamard_product", "execute", new AttributeType(new MatrixType("matrix[][]")), double[][].class, double[][].class);
    setInputTypes(new AttributeType(new MatrixType("matrix[a][b]")), new AttributeType(new MatrixType("matrix[a][b]")));
    setOutputType(new AttributeType(new MatrixType("matrix[b][a]")));
  }

  @Override
  public String getName() {
    return "hadamard_product";
  }
}