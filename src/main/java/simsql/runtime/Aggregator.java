

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


/* Encapsulates all the aggregation and pre-aggregation of
 * records. Essentially, the "innards" of the Aggregate Op.
 *
 * @author Luis.
 */
class Aggregator {
  
  // estimator for the size of our hash table.
  private CollectionSizeEstimator cse;
  private RecordHashTable table;

  // adapter
  private OutputAdapter writer;

  // latest key.
  private long lastKey = -1;
  
  /** Simple constructor. */
  public Aggregator(long memory, OutputAdapter writer) {

    table = new RecordHashTable(0.25, 1024);
    cse = new CollectionSizeEstimator(table.getCollectionWrapper(), memory);
    this.writer = writer;
  }

  /** Takes an input record for aggregation. */
  public void take(Record inRec) {

    // cast to aggregation record
    AggregatorRecord aggRec = null;
    try {
      aggRec = (AggregatorRecord) inRec;
    } catch (Exception e) {
	throw new RuntimeException("Could not cast selected record to the relevant type!", e);
    }

    Record[] preAggRecs = table.find (aggRec.getSecondaryHashKey());
    boolean foundIt = false;
    if (preAggRecs != null) {
      
      // if so, find the one that matches
      for (int i=0;i<preAggRecs.length;i++) {
	AggregatorRecord preAggRec = (AggregatorRecord)preAggRecs[i];
	
	if (preAggRec.isFromSameGroup(aggRec)) {

	  // if we found it, then consume the current record.
	  preAggRec.consume(aggRec);
	  foundIt = true;
	  break;
	}
      }
    }

    // did we not find it? then put it in the table.
    if (!foundIt) {      

      // but, first, check if a flush is due.
      if (cse.capacityExceeded() && lastKey != aggRec.getHashKey()) {
	
	// if so, flush -- this is OK
	flush();
      }

      table.add(aggRec, aggRec.getSecondaryHashKey());
      cse.updateEstimate();      

      // update the key.
      lastKey = aggRec.getHashKey();
    } else {

	// otherwise, recycle the consumed aggregator.
	lastKey = aggRec.getHashKey();
	aggRec.recycle();
    }

  }

  /** Flushes all the aggregated records we have. */
  public void flush() {

    // go through all the records in the table.
    for (Record xrec: table) {

      // give them to the adapter; it knows what to do.
      writer.writeRecord(xrec);
    }

    // clear the table
    table.clear();
  }
}
