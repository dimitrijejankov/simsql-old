package simsql.functions.ud;

import simsql.runtime.*;

/** A reflected function obtained from a java method.
 *
 * @author Jacob
 */

public class matrix_inverse extends UDFunction { 

  private static VGFunction udf;
  private static UDWrapper udw;

  static {

    // the corresponding UDF
    udf = new VGFunction("/simsql/runtime/MatrixInverse.ud.so");

    // only one UDWrapper related with each UDFunction
    udw = new UDWrapper(udf);
  };

  public matrix_inverse() {
    super(udf, udw);
  }

  @Override
	public String getName() {
		return "matrix_inverse";
	}
}