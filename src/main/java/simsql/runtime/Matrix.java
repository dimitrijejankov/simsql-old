package simsql.runtime;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.nio.ByteBuffer;


public class Matrix {

    /**
     * Indicate if the matrix is row based
     */
    private boolean ifRow;

    /**
     * this is the actual memory of the matrix
     */
    private MatrixMemory memory;

    /**
     * A native method that returns the size of the matrix header in memory
     *
     * @return size of the matrix header
     */
    public static native long getMatrixHeaderSize();

    /**
     * return the size of a double on the native machine
     *
     * @return the size
     */
    public static native long getNativeDoubleSize();

    /**
     * Allocate a matrix of size1 x size2
     */
    private static native void initMatrix(long size1 , long size2, long data);

    /**
     * Reshape the matrix to new dimensions
     * @param size1 the new size1 of the matrix
     * @param size2 the new size2 of the matrix
     * @param data the address of the matrix in memory
     */
    private static native void nativeReshape(long size1 , long size2, long data);

    /**
     * Returns the size of the allocated matrix
     * @param data the address of the matrix in memory
     * @return returns actual size
     */
    private static native long allocatedMatrixSize(long data);

    /**
     * Returns the number of rows from the matrix header
     *
     * @return the number of rows
     */
    public static native long getMatrixSize1(long data);

    /**
     * Returns the number of columns from the matrix header
     *
     * @return the number of columns
     */
    public static native long getMatrixSize2(long data);

    /**
     * Performs the native matrix add operation more specific
     * scale * matrix + double
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param sa  the scale
     * @param v   scalar value to add
     * @param out where to put the new matrix
     */
    static native void nativeAddDouble(long a, double sa, double v, long out);

    /**
     * Preforms the native matrix add operation more specific
     * ta * a + tb * b
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param ta  true if matrix a is transposed, false otherwise
     * @param sa  the multiplier for the matrix a
     * @param b   the vector to add
     * @param sb  the multiplier for the vector b
     * @param out where to put the new matrix
     */
    static native void nativeAddVector(long a, boolean ta, double sa, double[] b, double sb, long out);

    /**
     * Preforms the native matrix add operation more specific
     * sa * a + sb * b
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param sa  the multiplier for the matrix a
     * @param ta  true if matrix a is transposed, false otherwise
     * @param b   the address of the matrix allocated in unsafe memory
     * @param tb  true if matrix b is transposed, false otherwise
     * @param sb  the multiplier for the matrix b
     * @param out where to put the new matrix
     */
    static native void nativeAddMatrix(long a, boolean ta, double sa, long b, boolean tb, double sb, long out);

    /**
     * Performs the native matrix scale by double.
     * a * v
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param v   the scaling double
     * @param out where to put the new matrix
     */
    static native void nativeMultiplyDouble(long a, double v, long out);

    /**
     * Performs the native matrix vector multiply
     * v * m
     *
     * @param v   the vector as double[]
     * @param m   the address of the matrix allocated in unsafe memory
     * @param tv  true if matrix v is transposed, false otherwise
     * @param out where to put the new matrix
     */
    static native void nativeMultiplyVector(double[] v, long m, boolean tv, long out);

    /**
     * Performs matrix matrix multiply
     * a * b
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param ta  true if matrix a is transposed, false otherwise
     * @param b   the address of the matrix allocated in unsafe memory
     * @param tb  true if matrix b is transposed, false otherwise
     * @param out where to put the new matrix
     */
    static native void nativeMultiplyMatrix(long a, boolean ta, long b, boolean tb, long out);

    /**
     * Perform matrix double division
     * m ./ v
     *
     * @param m   the address of the matrix allocated in unsafe memory
     * @param v   value we want to divide with
     * @param out where to put the new matrix
     */
    static native void nativeDivideMatrixDouble(long m, double v, long out);

    /**
     * Preform double matrix division
     * v ./ m
     *
     * @param v   value we want to divide with
     * @param m   the address of the matrix allocated in unsafe memory
     * @param out where to put the new matrix
     */
    static native void nativeDivideDoubleMatrix(double v, long m, long out);

    /**
     * Preform matrix vector element-wise division
     * m ./ v
     *
     * @param m   the address of the matrix allocated in unsafe memory
     * @param tm  true if matrix m is transposed, false otherwise
     * @param v   vector to be used
     * @param out where to put the new matrix
     */
    static native void nativeDivideVectorMatrix(long m, boolean tm, double[] v, long out);

    /**
     * Preform matrix vector element-wise division
     * m ./ v
     *
     * @param m   the address of the matrix allocated in unsafe memory
     * @param tm  true if matrix m is transposed, false otherwise
     * @param v   vector to be used
     * @param out where to put the new matrix
     */
    static native void nativeDivideMatrixVector(long m, boolean tm, double[] v, long out);

    /**
     * Do element-wise division of two matrices
     * a ./ b
     *
     * @param a   the address of the matrix allocated in unsafe memory
     * @param ta  true if matrix a is transposed, false otherwise
     * @param b   the address of the matrix allocated in unsafe memory
     * @param tb  true if matrix b is transposed, false otherwise
     * @param out where to put the new matrix
     */
    static native void nativeDivideMatrixMatrix(long a, boolean ta, long b, boolean tb, long out);

    /**
     * Returns true if matrix a and b are equal
     * a == b
     *
     * @param a  the address of the matrix allocated in unsafe memory
     * @param ta true if matrix a is transposed, false otherwise
     * @param b  the address of the matrix allocated in unsafe memory
     * @param tb true if matrix a is transposed, false otherwise
     * @return true if a and b are not equal
     */
    static native boolean nativeEqual(long a, boolean ta, long b, boolean tb);

    /**
     * Returns the fist size of the matrix
     * @return the size1
     */
    public long getSize1() {
        return getMatrixSize1(memory.getAddress());
    }

    /**
     * Returns the second size of the matrix
     * @return the seconds size
     */
    public long getSize2() {
        return getMatrixSize2(memory.getAddress());
    }

    /**
     * The memory address where the address is
     * @return the address
     */
    public long getAddress() {
        return memory.getAddress();
    }

    /**
     * Return true if this is a row matrix, false otherwise
     * @return the boolean value
     */
    public boolean getIfRow() {
        return ifRow;
    }

    /**
     * Sets the indicator if this is a row matrix
     * @param ifRow true if it's a row matrix false otherwise
     */
    public void setIfRow(boolean ifRow) {
        this.ifRow = ifRow;
    }

    /**
     * an reference to the instance of the Unsafe class used for memory allocation within the matrix attribute
     */
    static Unsafe unsafe;

    static {
        try {
            // grab an Unsafe instance class
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            unsafe = unsafeConstructor.newInstance();

            // load the matrix allocation library
            String file = Function.extractFile("/simsql/runtime/Matrix.so");
            System.load(file);
        }
        catch (Exception ignore) {}
    }

    boolean reshape(long size1, long size2) {

        // check if we have enough memory to reshape
        if(allocatedMatrixSize(memory.getAddress()) >= size1 * size2 * Double.BYTES) {
            nativeReshape(size1, size2, memory.getAddress());
            return true;
        }

        return false;
    }

    /**
     * Checks if the dimensions of two matrices match throws R
     * @param m the matrix to compare
     */
    void checkDimensions(Matrix m) {

        long m1_row = m.getIfRow() ? m.getSize1() : m.getSize2();
        long m1_col = m.getIfRow() ? m.getSize2() : m.getSize1();
        long m2_row = getIfRow() ? getSize1() : getSize2();
        long m2_col = getIfRow() ? getSize2() : getSize1();

        if(m1_row != m2_row || m1_col != m2_col) {
            throw new RuntimeException("The dimensions don't match!");
        }
    }

    /**
     * Check if the length of the vector is the same the number of rows in the matrix, throws exception if it doesn't
     * @param x the vector as double array
     */
    void checkDimensions(double[] x) {

        long row = getIfRow() ? getSize1() : getSize2();

        if(row == x.length) {
            throw new RuntimeException("The dimensions don't match!");
        }
    }

    /**
     * Allocate the matrix of specified size, it's a matrix size1 x size2
     * if it's a row matrix size2 x size1 if column matrix
     * @param size1 first size of the matrix
     * @param size2 second size fo the matrix
     * @param ifRow true if a row matrix false otherwise
     */
    public Matrix(long size1, long size2, boolean ifRow) {

        // allocate the memory for the matrix
        this.memory = new MatrixMemory(getMatrixHeaderSize() + (size1 * size2 * getNativeDoubleSize()));

        // set the indicator for the row
        this.ifRow = ifRow;

        // initialize the memory of the matrix
        initMatrix(size1, size2, memory.getAddress());
    }

    /**
     * Allocate the matrix of specified size, it's a matrix size1 x size2
     * if it's a row matrix size2 x size1 if column matrix
     * @param size1 first size of the matrix
     * @param size2 second size fo the matrix
     * @param ifRow true if a row matrix false otherwise
     * @param raw the address to put in the matrix
     */
    public Matrix(long size1, long size2, boolean ifRow, double[][] raw) {

        // allocate the memory for the matrix
        this.memory = new MatrixMemory(getMatrixHeaderSize() + (size1 * size2 * getNativeDoubleSize()));

        // set the indicator for the row
        this.ifRow = ifRow;

        // initialize the memory of the matrix
        initMatrix(size1, size2, memory.getAddress());

        // if we have some address
        if(raw != null) {
            for(int i = 0; i < size1; ++i) {
                for(int j = 0; j < size2; ++j) {
                    unsafe.putDouble(memory.getAddress() + getMatrixHeaderSize() + (i * size2 + j) * getNativeDoubleSize(), raw[i][j]);
                }
            }
        }
    }

    /**
     * Creates a matrix from a ByteBuffer
     * The data in bytes has the following order
     * 8 bytes - row number
     * 8 bytes - col number
     * 8 bytes - ifRow (0 is false everything else is true)
     * row * column * 8 bytes of the matrix data as doubles
     * @param dataBuffOut byte buffer
     */
    public Matrix(ByteBuffer dataBuffOut) {
        // read the row and column numbers of the matrix
        long row = dataBuffOut.getLong();
        long col = dataBuffOut.getLong();
        long ifRow = dataBuffOut.getLong();

        // set the indicator for the row
        this.ifRow = (ifRow != 0);

        // allocate the memory for the matrix
        this.memory = new MatrixMemory(getMatrixHeaderSize() + (row * col * getNativeDoubleSize()));

        // initialize the memory of the matrix
        initMatrix(row, col, memory.getAddress());

        // store the doubles
        for(int i = 0; i < row; ++i) {
            for(int j = 0; j < col; ++j) {
                unsafe.putDouble(memory.getAddress() + getMatrixHeaderSize() + (i * col + j) * getNativeDoubleSize(), dataBuffOut.getDouble());
            }
        }
    }


    @Override
    protected void finalize() throws Throwable {
        // call the super method of the Object
        super.finalize();
    }
}
