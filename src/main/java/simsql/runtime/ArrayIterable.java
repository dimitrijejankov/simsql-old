

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
import java.lang.*;


public class ArrayIterable<T> implements Iterable<T>{

    // this defines the sizes of the first k bins.
    private static final int[] FAST_BINS = {16, 112, 896, 3072, 12288, 49152, 65536, 131072};
    private static final int   FAST_LAST = 262144;
    
    // lookup table for powers of 2, based on the above array.
    private static final int[] BIN_LOOKUP = {0, 0, 0, 0,  // 0-15
					     1, 1, 1,     // 16-127
					     2, 2, 2,     // 128-1023
					     3, 3,        // 1024-4095
					     4, 4,        // 4096-16383
					     5, 5,        // 16384-65535
					     6,           // 65536-131071
					     7};          // 131072-262143

    // for lookups.
    private static final int[] BIN_INDEX = {0, 0, 0, 0,
					    16, 16, 16,
					    128, 128, 128,
					    1024, 1024,
					    4096, 4096,
					    16384, 16384,
					    65536, 131072,
					    262144};

    // the bins.
    private Object[][] bins;

    private int currentBin = 0;
    private int numElements = 0;
    private int totalElements = 0;

    // default constructor
    public ArrayIterable() {
	bins = new Object[FAST_BINS.length][];
	bins[0] = new Object[FAST_BINS[0]];
    }

    // copy constructor
    public ArrayIterable(T[] inArray) {

	this();
	for (T t: inArray)
	    add(t);
    }

    // add a value
    public void add(T value) {
	
	// do we have to resize?
	if (numElements >= bins[currentBin].length) {

	    // go to the next bin
	    currentBin++;
	    numElements = 0;

	    // is it one of the fast ones?
	    if (currentBin < FAST_BINS.length && bins[currentBin] == null) {
		bins[currentBin] = new Object[FAST_BINS[currentBin]];
	    } else {


		// if not, check if there's space
		if (currentBin >= bins.length) {
		    Object[][] newBins = new Object[bins.length + (bins.length >> 1)][];
		    System.arraycopy(bins, 0, newBins, 0, bins.length);
		    bins = newBins;
		}

		// and create a new bin at 1.5
		if (bins[currentBin] == null)
		    bins[currentBin] = new Object[bins[currentBin-1].length + (bins[currentBin-1].length >> 1)];
	    }
	}

	bins[currentBin][numElements++] = value;
	totalElements++;
    }

    // retrieve the i^th value
    @SuppressWarnings("unchecked")
    public T get(int i) {

	// special cases first...
	if (i == 0)
	    return (T)(bins[0][0]);

	// get the log.
	int log = 31 - Integer.numberOfLeadingZeros(i);

	// we're lucky if it's from one of the fast bins.
	if (log < BIN_LOOKUP.length) {

	    return (T)(bins[BIN_LOOKUP[log]][i - BIN_INDEX[log]]);
	}

	// otherwise it will take a bit of work.
	int bin = FAST_BINS.length;
	int mx = i - FAST_LAST;
       
	while (bin <= currentBin && mx >= bins[bin].length) { 
	    mx -= bins[bin].length;
	    bin++;
	} 
	
	return (T)bins[bin][mx];
    }

    // returns the number of elements
    public int size() {
	return totalElements;
    }

    // clears the structure
    public void clear() {
	currentBin = 0;
	numElements = 0;
	totalElements = 0;

	bins = new Object[FAST_BINS.length][];
	bins[0] = new Object[FAST_BINS[0]];
    }


    // make a nice iterator
    @SuppressWarnings("unchecked")
    public Iterator<T> iterator() {

	return new Iterator<T>() {

	    private int bin = 0;
	    private int elem = 0;
	    private int tot = 0;
	    
	    public boolean hasNext() {
		return tot < totalElements;
	    }

	    public T next() {
		T obj = (T)bins[bin][elem++];
		if (elem >= bins[bin].length) {
		    bin++; 
		    elem = 0;
		}

		tot++;
		return obj;
	    }

	    public void remove() {
		throw new RuntimeException("Operation not supported!");
	    }
	};
    }
}
