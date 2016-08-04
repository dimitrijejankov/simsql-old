

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

import java.lang.ref.*;
import java.lang.Iterable;
import java.util.Iterator;
import java.util.ArrayList;
import java.io.*;
import java.lang.reflect.*;


class SimSQLHashTable <T> implements Iterable <T> {

  protected class HashRecord implements Serializable {

    private long hashKey;
    private T myRecord;

    public long getHashKey () {
      return hashKey;	
    }

    public T getRecord () {
      return myRecord;
    }

    public HashRecord (T recIn, long hashIn) {
      myRecord = recIn;
      hashKey = hashIn;	
    }
  }

  // This is a small addition for the CollectionSizeEstimator
  private class CollectionWrapper <E> extends ArrayList <E> {
    private SimSQLHashTable theHashRec = SimSQLHashTable.this;

    public int size() {
      return theHashRec.numUsed;
    }

    public CollectionWrapper() {
      super();
    }
  }

  // Another addition: for iteration
  private class SimSQLHashTableIterator implements Iterator <T> {

    int curPos = 0;

    public boolean hasNext() {

      while (true) {
	if (curPos >= myArray.length)
	  return false;

	if (myArray[curPos] != null)
	  return true;

	curPos++;
      }
    }

    public T next() {
      T outRec = myArray[curPos].getRecord();
      curPos ++;

      return outRec;
    }

    public void remove() {
      throw new RuntimeException("Unsupported operation!");
    }
  }

  public CollectionWrapper<T> getCollectionWrapper() {
    return new CollectionWrapper<T>();
  }

  public Iterator<T> iterator() {
    return new SimSQLHashTableIterator();
  }

  @SuppressWarnings({"unchecked"})
  public void clear() {
    for (int i=0;i<myArray.length;i++) {
      myArray[i] = null;
    }

    numUsed = 0;    
    lastPos = lastSlot = -1;
    lastFoundKey = -1;
    lastFoundRecs.clear();
  }

  public String print () {
    String outVal = "";
    for (HashRecord h : myArray) {
      if (h == null)
	continue;
      outVal += h.hashKey + " ";
    }
    return outVal;
  }

  protected transient HashRecord [] myArray;
  protected transient long mask;
  protected transient int whenToDouble;
  protected transient int numUsed;
  protected transient double maxFillRate;
  protected transient Class<?> clazz;

  public SimSQLHashTable () {
      this(0.65, 1024, Object.class);
  }

  @SuppressWarnings({"unchecked"})
  public SimSQLHashTable (double maxFillRateIn, int initialCapacity, Class<?> clazz) {

    this.clazz = clazz;
    myArray = (HashRecord[])Array.newInstance(HashRecord.class, 1024);
    maxFillRate = maxFillRateIn;
    mask = 1023L;
    whenToDouble = (int) Math.round (1024 * maxFillRate);
    numUsed = 0;
  }

  @SuppressWarnings({"unchecked"})
  private void doubleTable () {

    // forget about the positions we have remembered
    lastPos = lastSlot = -1;

    // create the new table
    HashRecord [] temp = myArray;
    myArray = (HashRecord[])Array.newInstance(HashRecord.class, myArray.length * 2);
    mask = (mask << 1) ^ 1L;

    // re-hash all of the records
    for (int i = 0; i < temp.length; i++) {
      if (temp[i] != null)
	add (temp[i]);
    }

    // and re-set the upper limit
    whenToDouble = (int) Math.round (myArray.length * maxFillRate);
  }

  private transient long lastPos = -1;
  private transient long lastSlot = -1;

  private void add (HashRecord me) {

    // put the record in the hash table
    long pos = mask & me.getHashKey ();	

    // if we've done this one before, then jump ahead
    if (pos == lastPos) {
      pos = lastSlot;

    // otherwise, remember it
    } else {
      lastPos = pos;
    }

    // first, find an empty slot
    while (myArray[(int) pos] != null) {
      pos++;
      if (pos == myArray.length)
	pos = 0;
    }	

    // and write it in
    myArray[(int) pos] = me;

    // remembering where we wrote it
    lastSlot = pos;

    // L: nullify the last found info
    lastFoundKey = -1;
    lastFoundRecs.clear();
  }

  public int size () {
    return numUsed;
  }

  public void add (T addMe, long hashKey) {

    // make the hash table bigger, if needed
    numUsed++;
    if (numUsed == whenToDouble) {
      doubleTable ();
    }

    // create a holder for the record
    HashRecord recordToAdd = new HashRecord (addMe, hashKey);

    // and add him
    add (recordToAdd);
  }

  // L: a little optimization for operations that do more find() than add().
  private transient long lastFoundKey = -1;
  private transient SoftReference<T[]> lastFoundRecs = new SoftReference<T[]>(null);
  private transient Object[] keptInstances = new Object[1000];

  @SuppressWarnings({"unchecked"})
  public T [] find (long hashKey) {

    // L: found it before?
    if (lastFoundKey == hashKey && lastFoundRecs.get() != null) {
      return lastFoundRecs.get();
    }

    int counter = 0;
    long pos = mask & hashKey;
    while (myArray[(int) pos] != null) {

      // if we found a match, count it
      if (myArray[(int) pos].getHashKey () == hashKey) {
	counter++;
      }

      pos++;
      if (pos == myArray.length)
	pos = 0;
    }

    // if we don't find a match, return a null
    if (counter == 0)
      return null;

    // try recycling an array instance.
    T [] retVal = null; // (T[]) Array.newInstance(clazz, counter);
    if (counter <= keptInstances.length) {

      if (keptInstances[counter - 1] == null) {
	keptInstances[counter - 1] = new SoftReference<Object[]>(null);
      }

      if (((SoftReference<Object[]>)keptInstances[counter - 1]).get() == null) {
	keptInstances[counter - 1] = new SoftReference<Object[]>((Object[])Array.newInstance(clazz, counter));
      }

      retVal = (T[]) ((SoftReference<Object[]>)keptInstances[counter - 1]).get();
    } else {
      retVal = (T[]) Array.newInstance(clazz, counter);
    }

    counter = 0;
    pos = mask & hashKey;
    while (myArray[(int) pos] != null) {

      // if we found a match, return it
      if (myArray[(int) pos].getHashKey () == hashKey) {
	retVal[(int) counter] = myArray[(int) pos].getRecord ();
	counter++;
      }

      pos++;
      if (pos == myArray.length)
	pos = 0;
    }

    // cache this for subsequent calls.
    lastFoundKey = hashKey;
    lastFoundRecs = new SoftReference(retVal);

    return retVal;
  }
}

class RecordHashTable extends SimSQLHashTable<Record> {

  public RecordHashTable (double maxFillRateIn) {
    super(maxFillRateIn, 16, Record.class);
  }

  public RecordHashTable (double maxFillRateIn, int initialCapacity) {
    super(maxFillRateIn, initialCapacity, Record.class);
  }
}

class VGHashTable extends SimSQLHashTable<VGIntermediateValue> {

  public VGHashTable (double maxFillRateIn) {
    super(maxFillRateIn, 16, VGIntermediateValue.class);
  }
}

class AttributeHashTable extends SimSQLHashTable<Attribute> implements Serializable {

  private void writeObject(java.io.ObjectOutputStream out) throws IOException {
    out.writeObject(myArray);
    out.writeLong(mask);
    out.writeInt(whenToDouble);
    out.writeInt(numUsed);
    out.writeDouble(maxFillRate);
    out.writeObject(clazz);
  }

  @SuppressWarnings({"unchecked"})
  private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
    myArray = (HashRecord[])in.readObject();
    mask = in.readLong();
    whenToDouble = in.readInt();
    numUsed = in.readInt();
    maxFillRate = in.readDouble();
    clazz = (Class<?>)in.readObject();
  }

  public AttributeHashTable (double maxFillRateIn) {
    super(maxFillRateIn, 16, Attribute.class);
  }

  public AttributeHashTable (double maxFillRateIn, int initialCapacity) {
    super(maxFillRateIn, initialCapacity, Attribute.class);
  }

  public boolean contains(Attribute addMe, long hashKey) {
    long pos = mask & hashKey;
    while (myArray[(int) pos] != null) {

      // if we found a match, count it
      if (myArray[(int) pos].getHashKey() == hashKey &&
	  myArray[(int) pos].getRecord().equals(addMe).allAreTrue()) {
	return true;
      }

      pos++;
      if (pos == myArray.length)
	pos = 0;
    }
		
    return false;
  }
}
