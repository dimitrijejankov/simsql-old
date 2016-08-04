
package simsql.runtime;

import java.lang.*;
import java.lang.reflect.*;

/**
 * A user-defined function type that can be constructed from a libFile
 * that specifies the UDF to run.
 * 
 * @author Jacob.
 */
public class UDFunction extends Function {

    private VGFunction udf;
    private UDWrapper udw;
    private AttributeType outType;

    // default constructor
    public UDFunction(VGFunction f, UDWrapper w) {

      // call Function's constructor
      super(f.getInputTypes());

      // the corresponding UDF
      udf = f;

      // the corresponding UDW
      udw = w;

      // set
      outType = udf.getOutputTypes()[0];
    }

    // name.
    public String getName() {
      return udf.getName();
    }

    // output type.
    public AttributeType getOutputType() {
      return outType;
    }

    // universal evaluation method.
    protected Attribute eval() {

      // deal with NULL singletons
      if (getNumMC() == 1 && !isNull.allAreFalseOrUnknown())
          return new NullAttribute();

      return udw.run(inParams, getNumMC());
    }
}
