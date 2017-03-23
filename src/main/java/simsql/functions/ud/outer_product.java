package simsql.functions.ud;

import simsql.runtime.*;
import java.lang.*;

/**
 * A function for outer product.
 *
 * @author Jacob
 */
public class outer_product extends ReflectedUDFunction {

  public static double[][] outer(double[][] a, double[][] b) {

    double[] vec1 = a[0];
    double[] vec2 = b[0];

    System.out.println(vec1.length, vec2.length);

    double[][] sum = new double[vec1.length][vec2.length];
    for (int i = 0; i < vec1.length; i++)
      for (int j = 0; j < vec2.length; j++)
        sum[i][j] = vec1[i] * vec2[j];

    return sum;
  }

  public outer_product() {
    super("simsql.functions.ud.outer_product", "outer", new AttributeType(new MatrixType("matrix[][]")), double[][].class, double[][].class);
    setInputTypes(new AttributeType(new MatrixType("matrix[][]")), new AttributeType(new MatrixType("matrix[][]")));
    setOutputType(new AttributeType(new MatrixType("matrix[][]")));
  }

  @Override
  public String getName() {
    return "outer_product";
  }
}