

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

/**
 * This class is used to estimate and control the capacity of a
 * Collection object with a given memory budget.
 *
 * @author Luis
 */
public class CollectionSizeEstimator {

  // maximum no. of estimates to have.
  private static final int MAX_ESTIMATES = 10;

  // some attributes...
  private Collection collection;
  private Map collectionMap;
  private long budget;
  private int lastCount;
  private int numEstimates;
  private int[] counts;
  private long[] sizes;
  private Integer capacityEstimate;

  /** 
   * Creates an instance of this class with a given collection and
   * memory budget.
   */
  public CollectionSizeEstimator(Collection collection, long budget) {
    this(budget);
    this.collection = collection;
    this.lastCount = collection.size();
    this.collectionMap = null;
  }

  /** 
   * Creates an instance of this class with a given map and
   * memory budget.
   */
  public CollectionSizeEstimator(Map collectionMap, long budget) {
    this(budget);
    this.collectionMap = collectionMap;
    this.lastCount = collectionMap.size();
    this.collection = null;
  }

  private CollectionSizeEstimator(long budget) {
    this.budget = budget;
    this.numEstimates = 0;
    this.counts = new int[MAX_ESTIMATES];
    this.sizes = new long[MAX_ESTIMATES];
    this.capacityEstimate = null;
  }

  /** 
   * This is supposed to be called every time we add elements to the
   * collection.
   */
  public void updateEstimate() {

    // ignore if the collection decreased or did not change in size.
    // or if we exceeded the number of necessary estimates.
    int colSize = (collection != null) ? collection.size() : collectionMap.size();
    if (capacityEstimate != null || colSize <= lastCount)
      return;

    counts[numEstimates] = colSize;
    sizes[numEstimates] = collection != null ? ObjectSizeEstimator.getInstanceSize(collection) : ObjectSizeEstimator.getInstanceSize(collectionMap);
    numEstimates++;
    lastCount = colSize;

    // do we have enough estimates to compute this?
    if (numEstimates >= MAX_ESTIMATES) {

      // get the means
      double meanCounts = 0.0;
      double meanSizes = 0.0;

      for (int i=0;i<MAX_ESTIMATES;i++) {
	      meanCounts += counts[i];
	      meanSizes += sizes[i];
      }

      meanCounts /= MAX_ESTIMATES;
      meanSizes /= MAX_ESTIMATES;

      // get the variances
      double varCounts = 0.0;
      double varSizes = 0.0;
      double covar = 0.0;
      for (int i=0;i<MAX_ESTIMATES;i++) {
	      varCounts += (counts[i] - meanCounts) * (counts[i] - meanCounts);
	      varSizes += (sizes[i] - meanSizes) * (sizes[i] - meanSizes);
	      covar += (counts[i] - meanCounts) * (sizes[i] - meanSizes);
      }

      // compute the slope and intercept
      double slope = covar / varSizes;
      double intercept = meanCounts - (slope * meanSizes);

      // now, obtain our capacity estimate with a simple linear regression
      capacityEstimate = (int)Math.rint(((double)budget * slope) + intercept);
      if (capacityEstimate <= 0)
	      capacityEstimate = new Integer(1);
      
      capacityEstimate += MAX_ESTIMATES;
    }

  }

  /**
   * Returns true if we have a valid number of estimates for the capacity.
   */
  public boolean haveCapacity() {
    return capacityEstimate != null;
  }

  /**
   * Returns the estimate for the max. capacity
   */
  public int getCapacity() {
    return capacityEstimate;
  }

  /**
   * Returns true if we have exceeded the estimated capacity.
   */
  public boolean capacityExceeded() {
    int colSize = (collection != null) ? collection.size() : collectionMap.size();
    return haveCapacity() && (colSize >= capacityEstimate);
  }
}
