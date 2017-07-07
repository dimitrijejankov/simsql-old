package simsql.runtime;

import sun.misc.Cleaner;
import sun.misc.SharedSecrets;
import sun.misc.Unsafe;
import sun.misc.VM;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implements a byte buffer that can allocate more memory then two gb
 */
public class LargeByteBuffer {

    private static Class bits;

    /**
     * Reference to the filed from java.nio.Bits reservedMemory
     */
    private static AtomicLong reservedMemory;

    /**
     * Reference to the filed from java.nio.Bits totalCapacity
     */
    private static AtomicLong totalCapacity;

    /**
     * Reference to the filed from java.nio.Bits count
     */
    private static AtomicLong count;

    /**
     * Reference to the filed from java.nio.Bits maxMemory
     */
    private static Long maxMemory;

    /**
     * Reference to the filed from java.nio.Bits memoryLimitSet
     */
    private static Boolean memoryLimitSet;

    /**
     * A reference to the unsafe object for allocation of the native memory
     */
    private static Unsafe unsafe;

    /**
     * True is the memory access is unaligned
     */
    private static boolean unaligned;

    /**
     * True if big endian false otherwise
     */
    private static boolean _nativeByteOrder;

    /**
     * This number limits the number of bytes to copy per call to Unsafe's
     * copyMemory method. A limit is imposed to allow for safepoint polling
     * during a large copy
     */
    private static final long UNSAFE_COPY_THRESHOLD = 1024L * 1024L;

    // Cached array base offset
    private static long arrayBaseOffset;

    static {
        try {

            // grab a reference to an instance of the java.nio.Bits class
            bits = Class.forName("java.nio.Bits");

            // grab a reference to some fields we need
            Field reservedMemoryField = bits.getDeclaredField("reservedMemory");
            Field totalCapacityField = bits.getDeclaredField("totalCapacity");
            Field countField = bits.getDeclaredField("count");
            Field memoryLimitSetField = bits.getDeclaredField("memoryLimitSet");
            Field maxMemoryField = bits.getDeclaredField("maxMemory");

            // get the unaligned flag from nio.bits
            Method method = bits.getDeclaredMethod("unaligned");
            method.setAccessible(true);
            unaligned = (Boolean) method.invoke(null);

            // get the byte order from nio.bits
            method = bits.getDeclaredMethod("byteOrder");
            method.setAccessible(true);
            _nativeByteOrder = method.invoke(null) == ByteOrder.BIG_ENDIAN;

            // set the to accessible
            reservedMemoryField.setAccessible(true);
            totalCapacityField.setAccessible(true);
            countField.setAccessible(true);
            memoryLimitSetField.setAccessible(true);
            maxMemoryField.setAccessible(true);

            // get a reference to the fields..
            reservedMemory = (AtomicLong) reservedMemoryField.get(null);
            totalCapacity = (AtomicLong) totalCapacityField.get(null);
            count = (AtomicLong) countField.get(null);
            maxMemory = (Long) maxMemoryField.get(null);
            memoryLimitSet = (Boolean) memoryLimitSetField.get(null);

            // grab an Unsafe instance class
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            unsafe = unsafeConstructor.newInstance();

            // grab the base offset of the array
            arrayBaseOffset = (long)unsafe.arrayBaseOffset(byte[].class);

        } catch (Exception ignored) {
            // this has to exist to for this to work
            System.exit(-1);
        }
    }

    /**
     * This class defalcates the memory of the matrix
     */
    private static class Deallocator implements Runnable {
        private static Unsafe unsafe = LargeByteBuffer.unsafe;
        private long address;
        private long size;
        private long capacity;

        private Deallocator(long address, long size, long capacity) {
            assert (address != 0);
            this.address = address;
            this.size = size;
            this.capacity = capacity;
        }

        public void run() {
            if (address == 0) {
                // Paranoia
                return;
            }
            unsafe.freeMemory(address);
            address = 0;
            unreserveMemory(size, capacity);
        }
    }

    /**
     * an instance of the cleaner class associated with the matrix memory
     */
    private final Cleaner cleaner;

    public Cleaner cleaner() {
        return cleaner;
    }

    /**
     * A direct copy of the java.nio.Bits.unreserveMemory method just with long as a parameters
     */
    static void unreserveMemory(long size, long cap) {

        synchronized (bits) {

            if (reservedMemory.get() > 0) {

                reservedMemory.addAndGet(-size); // reservedMemory -= size;
                totalCapacity.getAndAdd(-cap); //totalCapacity -= cap;
                count.decrementAndGet(); // count--

                assert (reservedMemory.get() > -1);
            }
        }
    }

    static void checkBounds(long var0, long var1, long var2) {
        if ((var0 | var1 | var0 + var1 | var2 - (var0 + var1)) < 0) {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * A direct copy of the java.nio.Bits.reserveMemory method just with long as a parameters
     */
    private static void reserveMemory(long size, long cap) {

        synchronized (bits) {

            if (!memoryLimitSet && VM.isBooted()) {
                maxMemory = VM.maxDirectMemory();
                memoryLimitSet = true;
            }

            // -XX:MaxDirectMemorySize limits the total capacity rather than the
            // actual memory usage, which will differ when buffers are page
            // aligned.
            if (cap <= maxMemory - totalCapacity.get()) {
                reservedMemory.addAndGet(size); //reservedMemory += size;
                totalCapacity.getAndAdd(cap); //totalCapacity += cap;
                count.incrementAndGet(); //count++;
                return;
            }
        }

        try {

            // try to garbage collect
            System.gc();

            // sleep for a few minutes
            Thread.sleep(100);

        } catch (InterruptedException x) {
            // Restore interrupt status
            Thread.currentThread().interrupt();
        }

        synchronized (bits) {
            if (totalCapacity.get() + cap > maxMemory) {
                throw new OutOfMemoryError("Direct buffer memory");
            }

            reservedMemory.addAndGet(size); //reservedMemory += size;
            totalCapacity.getAndAdd(cap); //totalCapacity += cap;
            count.incrementAndGet(); //count++;
        }
    }

    /**
     * Address of the allocated memory
     */
    private final long address;

    /**
     * The capacity of the allocated memory
     */
    private final long capacity;

    /**
     * The the current position of the buffer
     */
    private long position = 0;

    /**
     * True if big endian false otherwise
     */
    private boolean bigEndian;

    /**
     * True if the native order is the same as the order of the buffer
     */
    private boolean nativeByteOrder;

    /**
     * Creates an new instance of the MatrixMemory class that allocates the specified amount of memory
     * This constructor allocates the memory in the same manner as DirectByteBuffer
     *
     * @param cap the amount of memory we want to allocate
     */
    private LargeByteBuffer(long cap) {

        // java is big endian
        bigEndian = true;

        // set the native byte order
        nativeByteOrder = _nativeByteOrder;

        capacity = cap;
        boolean pa = VM.isDirectMemoryPageAligned();
        int ps = unsafe.pageSize();
        long size = Math.max(1L, cap + (pa ? ps : 0));
        reserveMemory(size, cap);
        long base;
        try {
            base = unsafe.allocateMemory(size);
        } catch (OutOfMemoryError x) {
            unreserveMemory(size, cap);
            throw x;
        }
        unsafe.setMemory(base, size, (byte) 0);
        if (pa && (base % ps != 0)) {
            // Round up to page boundary
            address = base + ps - (base & (ps - 1));
        } else {
            address = base;
        }
        cleaner = Cleaner.create(this, new Deallocator(base, size, cap));
    }

    /**
     * Returns the current position of the byte buffer
     *
     * @return the position
     */
    public long position() {
        return position;
    }

    /**
     * Returns the current position of the byte buffer
     */
    public void position(long pos) {
        position = pos;
    }


    /**
     * Returns the capacity of the large buffer
     * @return the capacity
     */
    public long capacity() {
        return capacity;
    }

    /**
     * Returns the address
     * @return the address of the native memory
     */
    public long getAddress() {
        return address;
    }

    /**
     * Set the byte order of the buffer
     *
     * @param order the order we want
     */
    public void order(ByteOrder order) {
        this.bigEndian = order == ByteOrder.BIG_ENDIAN;
        this.nativeByteOrder = this.bigEndian == _nativeByteOrder;
    }

    /**
     * Return the actual address of the postition
     *
     * @param position the position in buffer
     * @return the actual address
     */
    private long ix(long position) {
        return this.address + position;
    }

    /**
     * Check if the index is out of bounds
     *
     * @param index the index to check
     * @return the index if everything is fine
     */
    final long checkIndex(long index) {
        if (index >= 0 && index < this.capacity) {
            return index;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Check if an incremented address will be out of bounds
     *
     * @param address   the address
     * @param increment the increment of the address
     * @return the address if it will not
     */
    final long checkIndex(long address, long increment) {
        if (address >= 0 && increment <= this.capacity - address) {
            return address;
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    /**
     * Figure out the next position and sets it
     * throws exception if it exceeds the limit
     *
     * @param size the size of the step
     * @return the old position
     */
    final long nextGetIndex(int size) {
        if (this.capacity - this.position < size) {
            throw new BufferUnderflowException();
        } else {
            long old = this.position;
            this.position += size;
            return old;
        }
    }

    /**
     * Returns the next position where to put the value of a certain size
     *
     * @param size
     * @return the address where to put it
     */
    final long nextPutIndex(long size) {
        if (this.capacity - this.position < size) {
            throw new BufferOverflowException();
        } else {
            long var2 = this.position;
            this.position += size;
            return var2;
        }
    }

    static void copyToArray(long srcAddr, Object dst, long dstBaseOffset, long dstPos,
                            long length) {
        long offset = dstBaseOffset + dstPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(null, srcAddr, dst, offset, size);
            length -= size;
            srcAddr += size;
            offset += size;
        }
    }

    static void copyFromArray(Object src, long srcBaseOffset, long srcPos,
                              long dstAddr, long length)
    {
        long offset = srcBaseOffset + srcPos;
        while (length > 0) {
            long size = (length > UNSAFE_COPY_THRESHOLD) ? UNSAFE_COPY_THRESHOLD : length;
            unsafe.copyMemory(src, offset, null, dstAddr, size);
            length -= size;
            offset += size;
            dstAddr += size;
        }
    }

    public byte get() {
        return unsafe.getByte(this.ix(this.nextGetIndex(1)));
    }

    public byte get(int var1) {
        return unsafe.getByte(this.ix(this.checkIndex(var1)));
    }

    LargeByteBuffer _get(byte[] var1, int var2, int var3) {
        checkBounds(var2, var3, var1.length);
        if(var3 > (capacity - position)) {
            throw new BufferUnderflowException();
        } else {
            int var4 = var2 + var3;

            for(int var5 = var2; var5 < var4; ++var5) {
                var1[var5] = this.get();
            }

            return this;
        }
    }

    public LargeByteBuffer get(byte[] data, long var2, long size) {
        if (size > 6L) {
            checkBounds(var2, size, data.length);
            long var4 = this.position();
            long var5 = this.capacity;

            assert var4 <= var5;

            long var6 = var4 <= var5 ? var5 - var4 : 0;
            if (size > var6) {
                throw new BufferUnderflowException();
            }

            copyToArray(this.ix(var4), data, arrayBaseOffset, var2, size);
            this.position(var4 + size);
        } else {
            _get(data, (int)var2, (int)size);
        }

        return this;
    }

    private LargeByteBuffer _put(byte[] bytes, int position, int size) {
        checkBounds(position, size, bytes.length);
        if(size > (capacity - this.position)) {
            throw new BufferOverflowException();
        } else {
            int endPosition = position + size;

            for(int i = position; i < endPosition; ++i) {
                this.put(bytes[i]);
            }

            return this;
        }
    }

    public LargeByteBuffer put(byte var1) {
        unsafe.putByte(this.ix(this.nextPutIndex(1)), var1);
        return this;
    }

    public LargeByteBuffer put(long var1, byte var2) {
        unsafe.putByte(this.ix(this.checkIndex(var1)), var2);
        return this;
    }

    public LargeByteBuffer put(byte[] bytes, long position, long length) {
        if(length > 6L) {
            checkBounds(position, length, bytes.length);
            long currentPosition = this.position();
            long capacity = this.capacity;

            assert currentPosition <= capacity;

            long var6 = currentPosition <= capacity ? capacity - currentPosition : 0;
            if(length > var6) {
                throw new BufferOverflowException();
            }

            copyFromArray(bytes, arrayBaseOffset, position, this.ix(currentPosition), length);
            this.position(currentPosition + length);
        } else {
            // size less then six safe to convert it to an integer
            _put(bytes, (int)position, (int)length);
        }

        return this;
    }

    private char _getChar(long position) {
        if (unaligned) {
            char var3 = unsafe.getChar(position);
            return this.nativeByteOrder ? var3 : Character.reverseBytes(var3);
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public char getChar() {
        return this.getChar(this.ix(this.nextGetIndex(2)));
    }

    public char getChar(long index) {
        return this._getChar(this.ix(this.checkIndex(index, 2)));
    }

    private LargeByteBuffer _putChar(long address, char value) {
        if (unaligned) {
            unsafe.putChar(address, this.nativeByteOrder ? value : Character.reverseBytes(value));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putChar(char value) {
        this._putChar(this.ix(this.nextPutIndex(2)), value);
        return this;
    }

    public LargeByteBuffer putChar(long var1, char value) {
        this.putChar(this.ix(this.checkIndex(var1, 2)), value);
        return this;
    }

    private short _getShort(long address) {
        if (unaligned) {
            short value = unsafe.getShort(address);
            return this.nativeByteOrder ? value : Short.reverseBytes(value);
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public short getShort() {
        return this._getShort(this.ix(this.nextGetIndex(2)));
    }

    public short getShort(int var1) {
        return this._getShort(this.ix(this.checkIndex(var1, 2)));
    }

    private LargeByteBuffer _putShort(long var1, short var3) {
        if (unaligned) {
            unsafe.putShort(var1, this.nativeByteOrder ? var3 : Short.reverseBytes(var3));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putShort(short var1) {
        this.putShort(this.ix(this.nextPutIndex(2)), var1);
        return this;
    }

    public LargeByteBuffer putShort(long var1, short var2) {
        this._putShort(this.ix(this.checkIndex(var1, 2)), var2);
        return this;
    }

    private int getInt(long var1) {
        if (unaligned) {
            int var3 = unsafe.getInt(var1);
            return this.nativeByteOrder ? var3 : Integer.reverseBytes(var3);
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public int getInt() {
        return this.getInt(this.ix(this.nextGetIndex(4)));
    }

    public int getInt(int var1) {
        return this.getInt(this.ix(this.checkIndex(var1, 4)));
    }

    private LargeByteBuffer putInt(long var1, int var3) {
        if (unaligned) {
            unsafe.putInt(var1, this.nativeByteOrder ? var3 : Integer.reverseBytes(var3));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putInt(int var1) {
        this.putInt(this.ix(this.nextPutIndex(4)), var1);
        return this;
    }

    public LargeByteBuffer putInt(int var1, int var2) {
        this.putInt(this.ix(this.checkIndex(var1, 4)), var2);
        return this;
    }

    private long getLong(long var1) {
        if (unaligned) {
            long var3 = unsafe.getLong(var1);
            return this.nativeByteOrder ? var3 : Long.reverseBytes(var3);
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public long getLong() {
        return this.getLong(this.ix(this.nextGetIndex(8)));
    }

    public long getLong(int var1) {
        return this.getLong(this.ix(this.checkIndex(var1, 8)));
    }

    private LargeByteBuffer putLong(long var1, long var3) {
        if (unaligned) {
            unsafe.putLong(var1, this.nativeByteOrder ? var3 : Long.reverseBytes(var3));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putLong(long var1) {
        this.putLong(this.ix(this.nextPutIndex(8)), var1);
        return this;
    }

    public LargeByteBuffer putLong(int var1, long var2) {
        this.putLong(this.ix(this.checkIndex(var1, 8)), var2);
        return this;
    }

    private float getFloat(long var1) {
        if (unaligned) {
            int var3 = unsafe.getInt(var1);
            return Float.intBitsToFloat(this.nativeByteOrder ? var3 : Integer.reverseBytes(var3));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public float getFloat() {
        return this.getFloat(this.ix(this.nextGetIndex(4)));
    }

    public float getFloat(int var1) {
        return this.getFloat(this.ix(this.checkIndex(var1, 4)));
    }

    private LargeByteBuffer putFloat(long var1, float var3) {
        if (unaligned) {
            int var4 = Float.floatToRawIntBits(var3);
            unsafe.putInt(var1, this.nativeByteOrder ? var4 : Integer.reverseBytes(var4));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putFloat(float var1) {
        this.putFloat(this.ix(this.nextPutIndex(4)), var1);
        return this;
    }

    public LargeByteBuffer putFloat(int var1, float var2) {
        this.putFloat(this.ix(this.checkIndex(var1, 4)), var2);
        return this;
    }

    private double getDouble(long var1) {
        if (unaligned) {
            long var3 = unsafe.getLong(var1);
            return Double.longBitsToDouble(this.nativeByteOrder ? var3 : Long.reverseBytes(var3));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }
    }

    public double getDouble() {
        return this.getDouble(this.ix(this.nextGetIndex(8)));
    }

    public double getDouble(int var1) {
        return this.getDouble(this.ix(this.checkIndex(var1, 8)));
    }

    private LargeByteBuffer putDouble(long var1, double var3) {
        if (unaligned) {
            long var5 = Double.doubleToRawLongBits(var3);
            unsafe.putLong(var1, this.nativeByteOrder ? var5 : Long.reverseBytes(var5));
        } else {
            throw new RuntimeException("The system doesn't support unaligned memory access, this is not implemented!");
        }

        return this;
    }

    public LargeByteBuffer putDouble(double var1) {
        this.putDouble(this.ix(this.nextPutIndex(8)), var1);
        return this;
    }

    public LargeByteBuffer putDouble(int var1, double var2) {
        this.putDouble(this.ix(this.checkIndex(var1, 8)), var2);
        return this;
    }

    /**
     * Allocates a buffer of the specified capacity
     *
     * @param capacity the capacity in MB
     * @return the instance of the byte buffer
     */
    public static LargeByteBuffer allocateDirect(long capacity) {
        return new LargeByteBuffer(capacity);
    }

    public String toString() {
        return this.getClass().getName() +
                "[pos=" +
                this.position +
                " cap=" +
                this.capacity +
                "]";
    }
}
