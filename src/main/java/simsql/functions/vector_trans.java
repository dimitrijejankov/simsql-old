package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * This function takes a vector v, a number w and a number k so that
 * any v[i] < k, and returns a vector of size k with counts
 * corresponding to the entries (v[i],v[i+1]) as long as v[i]=w.
 *
 * @author Luis
 */

public class vector_trans extends ReflectedUDFunction {

  public static double[] trans(double[] v, int k, int w) {

    double[] out = new double[k];
    for (int i=0;i<k;i++) {
      out[i] = 0.0;
    }

    for (int i=1;i<v.length;i++) {
      int pos1 = (int)v[i-1];
      int pos2 = (int)v[i];

      if (pos1 == w) {
        out[pos2] += 1.0;
      }
    }

    return out;
  }

  public vector_trans() {
    super("simsql.functions.vector_trans", "trans", new AttributeType(new VectorType("vector[]")), double[].class, int.class, int.class);
    setInputTypes(new AttributeType(new VectorType("vector[a]")), new AttributeType(new IntType()), new AttributeType(new IntType()));
    setOutputType(new AttributeType(new VectorType("vector[b]")));
  }

  @Override
  public String getName() {
    return "vector_trans";
  }
}
