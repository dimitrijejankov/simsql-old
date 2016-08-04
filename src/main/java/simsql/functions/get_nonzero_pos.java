package simsql.functions;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for finding the first non-zero position in a vector.
 *
 * @author Shangyu
 */
public class get_nonzero_pos extends ReflectedUDFunction {

  public static int nonzero_pos(double[] val) {

    for (int i = 0; i < val.length; i++){
      if(val[i] != 0)
      return i;
    }
    return 0;
  }

  public get_nonzero_pos() {
    super("simsql.functions.get_nonzero_pos", "nonzero_pos", new AttributeType(new IntType()), double[].class);
  }

  @Override
  public String getName() {
    return "get_nonzero_pos";
  }
}
