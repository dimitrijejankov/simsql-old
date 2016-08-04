package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * This function takes the following: 
 * - a vector v1
 * - a vector v2
 * - a number k
 * - a number w.
 *
 * both vectors have the same length.
 * 
 * returns a vector of size k with counts corresponding to the
 * entries (v1[i],v2[i]) as long as v1[i]=w.
 *
 * @author Luis
 */

public class vector_emits extends ReflectedUDFunction {

  public static double[] emits(double[] v1, double[] v2, int k2, int w) {

    double[] out = new double[k2];
    for (int j=0;j<k2;j++) {
      out[j] = 0.0;
    }

    for (int i=0;i<v1.length;i++) {
      int pos1 = (int)v1[i];
      int pos2 = (int)v2[i];

      if (pos1 == w) {
        out[pos2] += 1.0;
      }
    }

    return out;
  }

  public vector_emits() {
    super("simsql.functions.vector_emits", "emits", new AttributeType(new VectorType("vector[]")), double[].class, double[].class, int.class, int.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[b]")));
  }

  @Override
  public String getName() {
    return "vector_emits";
  }
}
