package simsql.runtime;

import java.lang.*;
import java.nio.*;

/**
 * A user-defined function type that can be constructed from a libFile
 * that specifies the UDF to run.
 *
 * @author Dimitrije.
 */
public class UDFunction extends Function {

    /**
     * the position buffer, contains the positions of each input attribute value
     */
    private static LongBuffer posBuffIn;

    /**
     * the data exchange buffers, contains the values of the buffer
     */
    private static ByteBuffer dataBuffIn;

    static {
        // the size of the buffer
        int bufferSize = Integer.MAX_VALUE / 10;

        // allocate the data buffer in
        dataBuffIn = ByteBuffer.allocateDirect(bufferSize / 4).order(ByteOrder.nativeOrder());

        // allocate the position buffer
        posBuffIn = ByteBuffer.allocateDirect(bufferSize / 10).order(ByteOrder.nativeOrder()).asLongBuffer();

        // get the library file
        String file = Function.extractFile("/simsql/runtime/UDFunction.jni.so");
        System.load(file);
    }

    /**
     * The type of the output attribute
     */
    private AttributeType outType;

    /**
     * The address of the loaded function
     */
    private long instance;

    /**
     * If our output is a matrix this where the output will be...
     */
    private Matrix matrix;

    /**
     * Constructs a UD Function from the file
     * @param fileName the filename of the library
     */
    public UDFunction(String fileName) {

        // extract the library file
        String file = Function.extractFile(fileName);
        instance = load(file);

        // set the buffers to the loaded
        setBuffers(posBuffIn, dataBuffIn);

        // create the parameters array
        inTypes = getInputTypes();
        inParams = IntermediateValue.getIntermediates(1, inTypes);

        // set the start.
        numMC = 1;
        curParam = 0;
        isNull = BitstringWithSingleValue.FALSE;

        // set
        outType = getOutputType();
    }

    /**
     * Loads an UD function from a library
     * @param fileName the file of the function
     * @return the address of the created function
     */
    private native long load(String fileName);

    /**
     * Returns the name of the loaded function
     * @param instance the loaded function instance address
     * @return the name of the function
     */
    private native String getLoadedName(long instance);

    /**
     * Returns the output type
     * @param instance the address of the loaded function
     * @return the string representation of the type
     */
    private native String getLoadedOutputType(long instance);

    /**
     * Returns the input types as an array of strings
     * @param instance the address of the loaded function
     * @return the types in their string representation
     */
    private native String[] getLoadedInputTypes(long instance);

    /**
     * Sets the buffers for the input attributes
     * @param instancePtr address of the loaded function
     * @param inBuff the buffer that contains the data
     * @param positionsIn the buffer that contains the positions of individual attributes
     */
    private native void setBuffers(long instancePtr, ByteBuffer inBuff, LongBuffer positionsIn);

    /**
     * A wrapper for setBuffers
     * @param dataBufferIn the buffer that contains the data
     * @param posBufferIn the buffer that contains the positions of individual attributes
     */
    private void setBuffers(LongBuffer posBufferIn, ByteBuffer dataBufferIn) {
        setBuffers(instance, dataBufferIn, posBufferIn);
    }

    /**
     * Returns the result of the function as double
     * @param instance address of the loaded function
     * @return the value
     */
    private native double getDoubleResult(long instance);

    /**
     * Returns the result of the function as long
     * @param instance address of the loaded function
     * @return the value
     */
    private native long getIntResult(long instance);

    /**
     * Returns the result of the function as vector
     * @param instance address of the loaded function
     * @return the value
     */
    private native double[] getVectorResult(long instance);

    /**
     * Returns the result of the function as matrix
     * @param instance address of the loaded function
     * @return address of the matrix
     */
    private native long getMatrixResult(long instance);

    /**
     * Returns the result of the function as String
     * @param instance address of the loaded function
     * @return address of the matrix
     */
    private native String getStringResult(long instance);

    /**
     * Returns the name of the UD function
     * @return the name
     */
    public String getName() {
        return getLoadedName(instance);
    }

    /**
     * Returns the output type of this function
     * @return the output attribute type
     */
    @Override
    public AttributeType getOutputType() {

        // if we have the type set return it
        if(outType != null) {
            return outType;
        }

        // otherwise get it from the native function
        return new AttributeType(TypeMachine.fromString(getLoadedOutputType(instance)));
    }

    /**
     * Returns the array of input parameter types
     * @return the type array
     */
    @Override
    public AttributeType[] getInputTypes() {

        // if we have set the input types return them
        if(inTypes != null) {
            return inTypes;
        }

        //otherwise load them from the native function
        String[] typesString = getLoadedInputTypes(instance);
        AttributeType[] ret = new AttributeType[typesString.length];

        // use TypeMachine and DataType to do the parsing
        for (int i=0; i<typesString.length; i++) {
            ret[i] = new AttributeType(TypeMachine.fromString(typesString[i]));
        }

        return ret;
    }

    // universal evaluation method.
    protected Attribute eval() {

        // deal with NULL singletons
        if (getNumMC() == 1 && !isNull.allAreFalseOrUnknown())
            return new NullAttribute();

        // grab the return value
        return run();
    }

    /**
     * Invoked from native code, allocates a Matrix object of specified size or reuse an existing one
     * @param size1 the size 1 of the matrix
     * @param size2 the size 2 of the matrix
     * @return return the address of the matrix in the memory
     */
    @SuppressWarnings("unused")
    private long getOutputMatrix(long size1, long size2) {

        // check if we already have a matrix
        if(matrix != null) {

            // the matrix is big enough we can reuse it
            if(matrix.reshape(size1, size2)) {
                return matrix.getAddress();
            }
        }

        // create a new matrix
        matrix = new Matrix(size1, size2, true);

        // return the address of matrix
        return matrix.getAddress();
    }

    /**
     * Runs the loaded UDFunction
     * @return the outputted attribute
     */
    private Attribute run() {

        // set the buffers to position
        dataBuffIn.position(0);
        posBuffIn.position(0);

        // go through each attribute value.
        for (int j = 0; j < inTypes.length; j++) {

            // if it's null, put -1 in the position buffer and move on.
            if (IntermediateValue.getAttributes(inParams)[j].isNull().getValue(0)) {
                posBuffIn.put(-1);
                continue;
            }

            // otherwise, buffer the data and advance the position.
            posBuffIn.put(dataBuffIn.position());
            byte[] bx = IntermediateValue.getAttributes(inParams)[j].getValue(0, inTypes[j]);
            dataBuffIn.put(bx);
        }

        // the output type string
        switch (getOutputType().getTypeCode()){
            case INT: {
                return new IntAttribute(getIntResult(instance));
            }
            case DOUBLE: {
                return new DoubleAttribute(getDoubleResult(instance));
            }
            case SCALAR: {
                return new ScalarAttribute(getDoubleResult(instance));
            }
            case STRING: {
                return new StringAttribute(getStringResult(instance));
            }
            case VECTOR: {
                return new VectorAttribute(getVectorResult(instance));
            }
            case MATRIX: {
                // run the function
                long retAddress = getMatrixResult(instance);

                // check if we actually returned the requested matrix
                if(matrix.getAddress() != retAddress) {
                    throw new RuntimeException("The returned value is not the requested output matrix");
                }

                // return the matrix
                return new MatrixAttribute(matrix);
            }
            default: {
                throw new RuntimeException("Unsupported return type in UD function!");
            }
        }
    }
}
