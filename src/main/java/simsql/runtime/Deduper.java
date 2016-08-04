

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


/* Similar to the Aggregator, encapsulates the duplicate removal of
 * records in a SelectionOp. 
 *
 * @author Luis.
 */
class Deduper {

  // estimator for the size of our hash table.
  private CollectionSizeEstimator cse;
  private RecordHashTable table;

  // adapter
  private OutputAdapter writer;

  // are we actually removing duplicates or just passing records
  // through?
  private boolean removeDuplicates;

  // last hash key processed
  private long lastKey = -1;

  /** Simple constructor. */
  public Deduper(long memory, OutputAdapter writer, boolean removeDuplicates) {
    table = new RecordHashTable(0.5, 1024);
    cse = new CollectionSizeEstimator(table.getCollectionWrapper(), memory);
    this.writer = writer;
    this.removeDuplicates = removeDuplicates;
  }

  public void take(Record inRec) {

    // just pass the records around if we're not removing duplicates
    if (!removeDuplicates) {
      writer.writeRecord(inRec);
      return;
    }

    // otherwise, use the hash table.
    boolean isThere = false;
    for (HashableRecord hRec : ((HashableRecord)inRec).splitAll()) {

      Record[] dups = table.find(hRec.getSecondaryHashKey());

      // find it
      if (dups != null) {
	for (Record xrec: dups) {
	  if (xrec.equals(hRec)) {
	    isThere = true;
	    break;
	  }
	}
      }

      // if it's not there, we'll have to add it.
      if (!isThere) {

	// before doing so, check if a flush is due.
	if (cse.capacityExceeded() && lastKey != hRec.getHashKey()) {

	  // if so, flush
	  table.clear();
	}

	// put it in the table
	table.add(hRec, hRec.getSecondaryHashKey());
	cse.updateEstimate();
      
	// and set it off.
	writer.writeRecord(hRec);
      }

      lastKey = hRec.getHashKey();
    }
  }
}
