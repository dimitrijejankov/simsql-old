package simsql.runtime;

import sun.misc.Cleaner;
import sun.misc.Unsafe;
import sun.misc.VM;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicLong;


/**
 * This class is used to allocate memory for the Matrix class
 */
public class MatrixMemory {

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

        } catch (Exception ignored) {
            // this has to exist to for this to work
            System.exit(-1);
        }
    }

    /**
     * This class defalcates the memory of the matrix
     */
    private static class Deallocator implements Runnable {
        private static Unsafe unsafe = MatrixMemory.unsafe;
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

        System.gc();
        try {
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
     * Creates an new instance of the MatrixMemory class that allocates the specified amount of memory
     * This constructor allocates the memory in the same manner as DirectByteBuffer
     *
     * @param cap the amount of memory we want to allocate
     */
    public MatrixMemory(long cap) {

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

    public long getAddress() {
        return address;
    }

    public long getCapacity() {
        return capacity;
    }
}
