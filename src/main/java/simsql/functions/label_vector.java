package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for labeling vectors.
 *
 * @author Jacob
 */
public class label_vector extends ReflectedUDFunction {

  public static double[] label(double[] val, int label) {

    double [] vector = new double[val.length + 1];
    for (int i = 0; i < val.length; i++)
      vector[i] = val[i];
    vector[val.length] = (double) label;

    return vector;
  }

  public label_vector() {
    super("simsql.functions.label_vector", "label", new AttributeType(new VectorType("vector[]")), double[].class, int.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[a]")));
  }

  @Override
  public String getName() {
    return "label_vector";
  }

  public static void main (String[] args) {

    label_vector os = new label_vector();

    double [] vec = new double[]{0, 0.5, 1};

    Attribute out = os.apply(new VectorAttribute(1, vec), new IntAttribute(2));

    System.out.println(out.print(200));

  }
}