

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

import java.util.*;
import java.lang.ref.*;

/**
 * A late addition to the system: the ability to "recycle" records of
 * some type by marking them after they are not necessary anymore, so
 * that operators can avoid reallocating them and triggering the gc.
 * <p>
 * Operations obtain new instances through this class, and then use the
 * recycle() method to bring them back here.
 *
 * @author Luis.
 */

class RecordPool {

    // to avoid circular references and object loitering, we use soft
    // references to our records.
    private SoftReference<ArrayDeque<AbstractRecord>> recs;
    private int capacity;
    private AbstractRecord prototype;

    // constructor with a defined capacity.
    public RecordPool(int capacity, AbstractRecord prototype) {
        recs = new SoftReference<>(null);
        this.capacity = capacity;
        this.prototype = prototype;
    }

    // constructor with default capacity.
    public RecordPool(AbstractRecord prototype) {
        this(1024, prototype);
    }

    // produces an instance.
    public AbstractRecord get() {

        // did we get recalled?
        ArrayDeque<AbstractRecord> Q = recs.get();
        if (Q == null) {

            Q = new ArrayDeque<>(capacity);
            recs = new SoftReference<>(Q);
        }

        // are we empty?
        if (Q.isEmpty()) {

            // then return an old record.
            try {
                AbstractRecord myRec = (AbstractRecord) prototype.buildRecordOfSameType();
                myRec.setPoolForRecycling(this);
                return myRec;
            } catch (Exception e) {
                e.printStackTrace();
                throw new RuntimeException("Pool failed!", e);
            }
        }

        // otherwise, pull a value from the queue.
        AbstractRecord r = Q.remove();
        r.setPoolForRecycling(this);
        return r;
    }

    // returns an instance to the pool
    public void put(AbstractRecord myRec) {

        // did we get recalled?
        ArrayDeque<AbstractRecord> Q = recs.get();
        if (Q == null) {
            Q = new ArrayDeque<>(capacity);
            recs = new SoftReference<>(Q);
        }

        // take it only if we have the capacity.
        if (Q.size() < capacity && myRec != null) {
            Q.offer(myRec);
        }
    }

}
