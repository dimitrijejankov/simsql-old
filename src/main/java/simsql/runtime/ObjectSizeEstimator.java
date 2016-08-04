

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
import java.lang.reflect.*;

/**
 * This is used to estimate the size of a given object type with
 * variable instance sizes, particularly record sizes.
 * 
 * @author Luis.
 */
public class ObjectSizeEstimator {

  // max. no of estimates to have
  private static final long MAX_ESTIMATES = 250;

  // some attributes...
  private long finalNumEstimates;
  private IdentityHashMap<Class, Long> sumEstimates;
  private IdentityHashMap<Class, Long> countEstimates;
  private Long finalSizeEstimate;

  /** Default constructor. */
  public ObjectSizeEstimator() {
    finalSizeEstimate = null;
    finalNumEstimates = 0;
    sumEstimates = new IdentityHashMap<Class, Long>();
    countEstimates = new IdentityHashMap<Class, Long>();
  }

  /**
   * Update the estimates with a new instance.
   */
  public void updateEstimate(Object obj) {

    // ignore if we already have the necessary estimates
    // and/or if it is null
    if (finalNumEstimates >= MAX_ESTIMATES || obj == null)
      return;

    // do we have it in our collections?
    if (!sumEstimates.containsKey(obj.getClass())) {
      sumEstimates.put(obj.getClass(), new Long(0));
    }

    if (!countEstimates.containsKey(obj.getClass())) {
      countEstimates.put(obj.getClass(), new Long(0));
    }

    // update the collections
    sumEstimates.put(obj.getClass(), sumEstimates.get(obj.getClass()) + getInstanceSize(obj));
    countEstimates.put(obj.getClass(), countEstimates.get(obj.getClass()) + 1);
    finalNumEstimates++;
    
    // with a weighted average
    long fes = 0;
    for (Class cx: sumEstimates.keySet()) {
      fes += (sumEstimates.get(cx) / countEstimates.get(cx)) * (countEstimates.get(cx) / (double)finalNumEstimates);
    }

    
    finalSizeEstimate = new Long(fes);
  }

  /**
   * Returns the size estimate.
   */
  public long getSizeEstimate() {
    return finalSizeEstimate;
  }
  
  /**
   * Returns an estimate of the capacity of an array with a memory budget.
   */
  public int getArrayCapacity(long budget) {

    // 20 = base Java array size in memory.

    // just in case...
    if (budget < finalSizeEstimate + 20)
      return 1;

    return (int)(budget / (finalSizeEstimate + 20));
  }



  // hash maps for object references.
  private static IdentityHashMap<Object,Object> seenBeforeInParent = new IdentityHashMap<Object, Object>();

  /**
   * Returns the actual size in memory of a given instance.
   */
  public static long getInstanceSize(Object obj) {

    seenBeforeInParent.clear();
    return getInstanceSizeAux(obj);
  }

  private static long getInstanceSizeAux(Object obj) {

    // null?
    if (obj == null)
      return 0;

    // to avoid infinite loops...
    if (seenBeforeInParent.containsKey(obj))
      return 0;


    // get its class
    Class clazz = obj.getClass();
    
    // is it a primitive value?
    if (clazz.isPrimitive()) {
      return getPrimitiveSize(clazz);
    }

    // is it an array?
    if (clazz.isArray()) {
      long lx = 16;
      int len = Array.getLength(obj);
      if (len > 0) {
	Class arrayClazz = clazz.getComponentType();
	if (arrayClazz.isPrimitive()) {
	  lx += len * getPrimitiveSize(arrayClazz);
	} else {

	  for (int i=0;i<len;i++) {
	    try {
        // System.out.println("Recursive call on array " + clazz + " position " + i + " ref: " + obj);
        // System.out.println("Whose components are of type " + arrayClazz);
	      lx += 4 + getInstanceSizeAux(Array.get(obj, i));
	    } catch (Exception e) { }
	  }
	}
      }

      return lx;
    }

    // otherwise, we have a regular object 
    long lx = 8;
    while (true) {

      // get the fields
      for (Field f: clazz.getDeclaredFields()) {

	// ignore statics
	if (java.lang.reflect.Modifier.isStatic(f.getModifiers())) {
	  continue;
	}

	try {
	  boolean fx = f.isAccessible();
	  f.setAccessible(true);
	  if (f.getType().isPrimitive()) {
	    lx += getPrimitiveSize(f.getType());
	  }
	  else {
      // System.out.println("Recursive call on object of type " + clazz + " field " + f + " ref: " + obj);
    	    seenBeforeInParent.put(obj, obj);
	    lx += getInstanceSizeAux(f.get(obj));
            seenBeforeInParent.remove (obj);
	  }
	  f.setAccessible(fx);
	} catch (Exception e) { e.printStackTrace(); }
      }

      // go with the superclass
      clazz = clazz.getSuperclass();
      if (clazz == null) {
	break;
      }
    }
    
    return lx;
  }

  private static long getPrimitiveSize(Class prim) {
    if (prim == boolean.class)
      return 1;

    if (prim == byte.class)
      return 1;

    if (prim == char.class)
      return 2;

    if (prim == short.class)
      return 2;

    if (prim == int.class)
      return 4;

    if (prim == float.class)
      return 4;

    if (prim == double.class)
      return 8;

    if (prim == long.class)
      return 8;

    return 0;
  }
}
