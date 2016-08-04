

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
 * Stub of the appendable pipelined aggregate operator.  Template
 * classes derive from this one and implement the passInput() and
 * accepts() methods.
 *
 * @author Luis.
 */
abstract class PipelinedAggregateOp extends PipelinedOperator {

  // our writer adapter class for the aggregator.
  private class Writer implements OutputAdapter {

    // this is for the last batch of records that the aggregate has to
    // flush.
    private boolean cleanup = false;
    private ArrayList<Record> cleanupArray = null;

    public Writer() {
    }

    public void writeRecord(Record writeMe) {

      AggregatorRecord output = (AggregatorRecord)writeMe;
      output.setSortAttribute(output.getHashKey());

      if (cleanup) {
	cleanupArray.add(output);
      } else {
	emit(output);
      }
    }

    public void startCleanup() {
      cleanup = true;
      cleanupArray = new ArrayList<Record>();
    }

    public ArrayList<Record> finishCleanup() {
      cleanup = false;
      ArrayList<Record> ret = cleanupArray;
      cleanupArray = null;

      return ret;
    }
  }

  private Aggregator agg;
  private Writer myWriter;

  public PipelinedAggregateOp() {
    myWriter = new Writer();
    agg = new Aggregator(128L * (1024L * 1024L), myWriter);
  }

  // this is for the generated classes to implement.  it will just
  // cast the input record and run the selection and projection.
  protected abstract HashableRecord passInput(Record rec);

  // does the last flush after the operator has finished.
  ArrayList <Record> cleanupRes = null; 
  public Record cleanup() {
    if (cleanupRes != null && cleanupRes.size () == 0)
      return null;

    // set up the return array if it is not set up
    if (cleanupRes == null) {
      myWriter.startCleanup();
      agg.flush();
      cleanupRes = myWriter.finishCleanup();
      return cleanup ();
    }

    // return a cleanup record
    return cleanupRes.remove (0);
  }

  public void process (Record me) {

    // run selection and projection
    HashableRecord hashedRec = passInput(me);
    if (hashedRec == null) {
      return;
    }

    // split on the group-by attributes
    HashableRecord[] splits = hashedRec.splitSome();
    for (HashableRecord rec : splits) {
      agg.take(rec);
    }
  }
}
