package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for outer product.
 *
 * @author Jacob
 */
public class outer_product extends ReflectedUDFunction {

  public static double[][] outer(double[] vec1, double[] vec2) {

    double[][] sum = new double[vec1.length][vec2.length];
    for (int i = 0; i < vec1.length; i++)
      for (int j = 0; j < vec2.length; j++)
      sum[i][j] = vec1[i] * vec2[j];

    return sum;
  }

  public outer_product() {
    super("simsql.functions.ud.outer_product", "outer", new AttributeType(new MatrixType("matrix[][]")), double[].class, double[].class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new VectorType("vector[b]")));
    setOutputType(new AttributeType(new MatrixType("matrix[a][b]")));
  }

  @Override
  public String getName() {
    return "outer_product";
  }
}