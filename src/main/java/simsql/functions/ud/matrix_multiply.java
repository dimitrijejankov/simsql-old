package simsql.functions.ud;

import simsql.runtime.*;

/** A reflected function obtained from a java method.
 *
 * @author Jacob
 */

public class matrix_multiply extends UDFunction { 

  private static VGFunction udf;
  private static UDWrapper udw;

  static {

    // the corresponding UDF
    udf = new VGFunction("/simsql/runtime/MatrixMultiply.ud.so");

    // only one UDWrapper related with each UDFunction
    udw = new UDWrapper(udf);
  };

  public matrix_multiply() {
    super(udf, udw);
  }

  @Override
	public String getName() {
		return "matrix_multiply";
	}

  public static void main (String[] args) {

    matrix_multiply os = new matrix_multiply();

    double[][] mat1 = new double[][]{{1, 2, 3}, {4, 5, 6}};
    double[][] mat2 = new double[][]{{7, 8}, {9, 10}, {11, 12}};

    Attribute out = os.apply(new MatrixAttribute(mat1), new MatrixAttribute(mat2));

    System.out.println(out.print(200));

  }
}