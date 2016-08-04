package simsql.functions;

import simsql.runtime.*;

/** A reflected function obtained from a java method.
 *
 * @author Jacob
 */

public class matrix_vector_multiply extends UDFunction { 

  private static VGFunction udf;
  private static UDWrapper udw;

  static {

    // the corresponding UDF
    udf = new VGFunction("/simsql/runtime/MatrixVectorMultiply.ud.so");

    // only one UDWrapper related with each UDFunction
    udw = new UDWrapper(udf);
  };

  public matrix_vector_multiply() {
    super(udf, udw);
  }

  @Override
	public String getName() {
		return "matrix_vector_multiply";
	}

  public static void main (String[] args) {

    matrix_vector_multiply os = new matrix_vector_multiply();

    double[][] mat = new double[][]{{1, 2, 3}, {4, 5, 6}};
    double[] vec = new double[]{7, 8, 9};

    Attribute out = os.apply(new MatrixAttribute(mat), new VectorAttribute(vec));

    System.out.println(out.print(200));

  }
}