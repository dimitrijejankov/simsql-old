
package simsql.runtime;

import java.lang.*;
import java.nio.*;

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

    // the position exchange buffers
    private static LongBuffer posBuffIn;
    private static LongBuffer posBuffOut;

    // the data exchange buffers
    private static LargeByteBuffer dataBuffIn;
    private static LargeByteBuffer dataBuffOut;

    // the tuple buffer
    private static LongBuffer tupleBuf;

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

    public static LongBuffer getPosBuffIn() {
        return posBuffIn;
    }

    public static LongBuffer getPosBuffOut() {
        return posBuffOut;
    }

    public static LargeByteBuffer getDataBuffIn() {
        return dataBuffIn;
    }

    public static LargeByteBuffer getDataBuffOut() {
        return dataBuffOut;
    }

    public static LongBuffer getTupleBuf() {
        return tupleBuf;
    }

    public static void setPosBuffIn(LongBuffer otherPosBuffIn) {
        posBuffIn = otherPosBuffIn;
    }

    public static void setPosBuffOut(LongBuffer otherPosBuffOut) {
        posBuffOut = otherPosBuffOut;
    }

    public static void setDataBuffIn(LargeByteBuffer otherDataBuffIn) {
        dataBuffIn = otherDataBuffIn;
    }

    public static void setDataBuffOut(LargeByteBuffer otherDataBuffOut) {
        dataBuffOut = otherDataBuffOut;
    }

    public static void setTupleBuf(LongBuffer otherTupleBuf) {
        tupleBuf = otherTupleBuf;
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
