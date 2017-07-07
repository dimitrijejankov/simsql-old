

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


package simsql.runtime;

/**
 * Implements the double type, with the same value in every world.
 */

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class DoubleAttribute extends AbstractDoubleAttribute {

    // the actual data value
    // private double myVal;

    public static final DoubleAttribute ONE = new DoubleAttribute(1.0);
    public static final DoubleAttribute ZERO = new DoubleAttribute(0.0);

    // a little pool
    private static int poolSize = 0;
    private static final DoubleAttribute[] pool = new DoubleAttribute[8];

    // tries to return a recycled instance.
    public static DoubleAttribute getInstance(double val) {

        if (poolSize == 0) {
            return new DoubleAttribute(val);
        }

        poolSize--;
        DoubleAttribute W = pool[poolSize];
        pool[poolSize] = null;

        W.setVal(val);
        W.recycled = false;
        return W;
    }

    private boolean recycled = false;

    public void recycle() {
        if (recycled || poolSize >= pool.length || this == DoubleAttribute.ONE || this == DoubleAttribute.ZERO)
            return;

        recycled = true;
        pool[poolSize] = this;
        poolSize++;
    }


    // this allows us to return the data as a byte array
    private static ByteBuffer b = ByteBuffer.allocate(8);

    static {
        b.order(ByteOrder.nativeOrder());
    }

    public DoubleAttribute() {
    }

    public DoubleAttribute(double fromMe) {
        setVal(fromMe);
    }

    public byte[] injectValue(int whichMC) {
        b.putDouble(0, getVal());
        return b.array();
    }

    public void injectIntoBuffer(int whichMC, AttributeType castTo, LargeByteBuffer buffer) {
        if (castTo.getTypeCode() == getType(whichMC).getTypeCode()) {
            buffer.putDouble(0, getVal());
        }

        if (castTo.getTypeCode() == TypeCode.INT) {
            buffer.putLong(0, (long) getVal());
        } else {
            throw new RuntimeException("Invalid cast when writing out value.");
        }
    }

    public long getHashCode() {
        return Hash.hashMe(injectValue(0));
    }

    public AttributeType getType(int whichMC) {
        return new AttributeType(new DoubleType());
    }

}
