package simsql.functions.ud;

import simsql.runtime.*;

import java.lang.*;

/**
 * A function for labeling scalars.
 *
 * @author Jacob
 */
public class label_scalar extends ReflectedUDFunction {

  public static double[] label(double val, int label) {

    double [] scalar = new double[2];
    scalar[0] = val;
    scalar[1] = (double) label;

    return scalar;
  }

  public label_scalar() {
    super("simsql.functions.ud.label_scalar", "label", new AttributeType(new ScalarType()), double.class, int.class);
  }

  @Override
  public String getName() {
    return "label_scalar";
  }

  public static void main (String[] args) {

    label_scalar os = new label_scalar();

    Attribute out = os.apply(new ScalarAttribute(0.5), new IntAttribute(1));

    System.out.println(out.print(200));

  }
}