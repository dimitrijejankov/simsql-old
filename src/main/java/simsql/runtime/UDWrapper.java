package simsql.runtime;

import java.nio.*;

/**
 * Encapsulates all the UDWrapper logic.
 *
 * @author Jacob
 */
public class UDWrapper {

    // data types on the UDF
    private AttributeType[] inTypes;
    private AttributeType outType;

    // the UD function
    VGFunction udf;

    /** Simple constructor -- starts up the UDWrapper. */
    public UDWrapper(VGFunction udf) {

        // copy the function
        this.udf = udf;

        // get the input and output types.
        inTypes = udf.getInputTypes();
        outType = udf.getOutputTypes()[0];

    }

    /** Runs the UDWrapper. */
    public Attribute run(IntermediateValue[] inParams, int numMC) {

        // System.out.println("The number of numMC is: " + numMC);

        IntermediateValue outVal = new IntermediateValue(outType, numMC);

        ByteBuffer dataBuffIn = UDFunction.getDataBuffIn();
        ByteBuffer dataBuffOut = UDFunction.getDataBuffIn();
        LongBuffer posBuffIn = UDFunction.getPosBuffIn();
        LongBuffer posBuffOut = UDFunction.getPosBuffOut();
        LongBuffer tupleBuf = UDFunction.getTupleBuf();

        if (posBuffIn == null || posBuffOut == null || dataBuffIn == null || dataBuffOut == null || tupleBuf == null) {
            // allocate buffers
            System.out.println("Allocating buffer only once!!!!");
            System.out.flush();
            int bufferSize = Integer.MAX_VALUE/10;

            // declare the output position buffer.
            ByteBuffer pbo = ByteBuffer.allocateDirect(bufferSize / 10);
            pbo.order(ByteOrder.nativeOrder());
            posBuffOut = pbo.asLongBuffer();
            UDFunction.setPosBuffOut(posBuffOut);

            // and data output buffer.
            dataBuffOut = ByteBuffer.allocateDirect(bufferSize / 4);
            dataBuffOut.order(ByteOrder.nativeOrder());
            UDFunction.setDataBuffOut(dataBuffOut);

            // create the input data exchange buffer.
            dataBuffIn = ByteBuffer.allocateDirect(bufferSize / 4);
            dataBuffIn.order(ByteOrder.nativeOrder());
            UDFunction.setDataBuffIn(dataBuffIn);

            // and position buffer.
            ByteBuffer pbi = ByteBuffer.allocateDirect(bufferSize / 10);
            pbi.order(ByteOrder.nativeOrder());
            posBuffIn = pbi.asLongBuffer();
            UDFunction.setPosBuffIn(posBuffIn);

            // declare the tuple buffer.
            ByteBuffer tbi = ByteBuffer.allocateDirect(bufferSize / 10);
            tbi.order(ByteOrder.nativeOrder());
            tupleBuf = tbi.asLongBuffer();
            UDFunction.setTupleBuf(tupleBuf);
        }
        udf.setBuffers(posBuffIn, posBuffOut, dataBuffIn, dataBuffOut, tupleBuf);

//    calculateSize(inParams, numMC);
        dataBuffIn.position(0);
        dataBuffOut.position(0);
        posBuffIn.position(0);
        posBuffOut.position(0);

        for (int i=0;i<numMC;i++) {

            tupleBuf.position(0);
            tupleBuf.put(posBuffIn.position());

            // go through each attribute value.
            for (int j=0;j<inTypes.length;j++) {

                // if it's null, put -1 in the position buffer and move on.
                if (IntermediateValue.getAttributes(inParams)[j].isNull().getValue(i)) {
                    posBuffIn.put(-1);
                    continue;
                }

                // otherwise, buffer the data and advance the position.
                posBuffIn.put(dataBuffIn.position());
                byte[] bx = IntermediateValue.getAttributes(inParams)[j].getValue(i, inTypes[j]);
                dataBuffIn.put(bx);
            }

            udf.clearParams();

            udf.takeParams(1);  /** num of input tuples */

            // call the UDF for samples
            int outVals = (int)udf.outputVals();

            long[] outTup = new long[1];
            if (outVals == 1) {  /** success */
                posBuffOut.position(0);
                posBuffOut.get(outTup, 0, 1);
                dataBuffOut.position(0);
            }

            // update the intermediates...
            // deserialize the output values.

            int kVal = (int)outTup[0];
            if (kVal >= 0) {

                switch(outType.getTypeCode()) {
                    case INT: {
                        outVal.set(dataBuffOut.getLong(kVal), i);
                    }
                    break;

                    case DOUBLE: {
                        outVal.set(dataBuffOut.getDouble(kVal), i);
                    }
                    break;

                    case STRING: {

                        // get to the position
                        dataBuffOut.position(kVal);

                        // read the length of the string
                        int len = (int)(dataBuffOut.getLong());

                        // read those chars and make a string.
                        byte[] btr = new byte[len];
                        dataBuffOut.get(btr, 0, len);

                        try {
                            outVal.set(new String(btr, "UTF-8"), i);
                        } catch (Exception e) {
                            throw new RuntimeException("Failed to read string from UD function!", e);
                        }
                    }
                    break;

                    case SCALAR: {
                        outVal.set(dataBuffOut.getDouble(kVal), -1, i);
                    }
                    break;

                    case VECTOR: {

                        // get to the position
                        dataBuffOut.position(kVal);

                        // read the length of the vector
                        int len = (int)(dataBuffOut.getLong());

                        // read those doubles and make an array.
                        double[] vec = new double[len];
                        for (int j = 0; j < len; j++)
                            vec[j] = dataBuffOut.getDouble();

                        outVal.set(vec, -1, i);
                    }
                    break;

                    case MATRIX: {

                        // get to the position
                        dataBuffOut.position(kVal);

                        // read the row and column numbers of the matrix
                        int row = (int)(dataBuffOut.getLong());
                        int col = (int)(dataBuffOut.getLong());

                        // read those doubles and make an array.
                        double[][] mat = new double[row][col];
                        for (int j = 0; j < row; j++)
                            for (int k = 0; k < col; k++)
                                mat[j][k] = dataBuffOut.getDouble();

                        outVal.set(mat, true, i);
                    }
                    break;
                }
            }
        }

        return outVal.getAttribute();
    }

    /*
    public void calculateSize(IntermediateValue[] inParams, int numMC) {

        long size = 0;

        for (int i=0;i<numMC;i++) {

            // go through each attribute value.
            for (int j=0;j<inTypes.length;j++) {

                // if it's null, move on.
                if (IntermediateValue.getAttributes(inParams)[j].isNull().getValue(i)) {
                    continue;
                }

                byte[] bx = IntermediateValue.getAttributes(inParams)[j].getValue(i, inTypes[j]);
                size += bx.length;
            }
        }

        long bufferSize = 20 * size;

        // declare the output position buffer.
        int memAmount;
        if (bufferSize / 10 > Integer.MAX_VALUE)
            memAmount = Integer.MAX_VALUE;
        else
            memAmount = (int) (bufferSize / 10);
        ByteBuffer pbo = ByteBuffer.allocateDirect(memAmount);
        pbo.order(ByteOrder.nativeOrder());
        posBuffOut = pbo.asLongBuffer();

        // and data output buffer.
        dataBuffOut = ByteBuffer.allocateDirect(memAmount);
        dataBuffOut.order(ByteOrder.nativeOrder());

        // create the input data exchange buffer.
        if (bufferSize / 4 > Integer.MAX_VALUE)
            memAmount = Integer.MAX_VALUE;
        else
            memAmount = (int) (bufferSize / 4);
        dataBuffIn = ByteBuffer.allocateDirect(memAmount);
        dataBuffIn.order(ByteOrder.nativeOrder());

        // and position buffer.
        ByteBuffer pbi = ByteBuffer.allocateDirect(memAmount);
        pbi.order(ByteOrder.nativeOrder());
        posBuffIn = pbi.asLongBuffer();

        // declare the tuple buffer.
        ByteBuffer tbi = ByteBuffer.allocateDirect(memAmount);
        tbi.order(ByteOrder.nativeOrder());
        tupleBuf = tbi.asLongBuffer();
        udf.setBuffers(posBuffIn, posBuffOut, dataBuffIn, dataBuffOut, tupleBuf);
    }
    */

}