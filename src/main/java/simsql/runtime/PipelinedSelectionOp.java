

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
 * Stub of the appendable selection operator. Template classes derive from this one
 * and implement the passInput(), and accepts() methods.
 *
 * @author Luis.
 */
abstract class PipelinedSelectionOp extends PipelinedOperator {

  // our writer adapter class for the duplicate remover.
  private class Writer implements OutputAdapter {
    public Writer() {
    }

    public void writeRecord(Record writeMe) {
      HashableRecord r = (HashableRecord)writeMe;
      if (r.hasSortAttribute()) {
	  r.setSortAttribute(r.getSortAttribute());
      }
      emit(r);
    }
  }

  private Deduper dedup;
  private Writer myWriter;

  public PipelinedSelectionOp(boolean removeDuplicates) {
    myWriter = new Writer();
    dedup = new Deduper(16L * (1024L * 1024L), myWriter, removeDuplicates);
  }

  // this is for the generated classes to implement.  it will just
  // cast the input record and run the selection and projection.
  protected abstract HashableRecord passInput(Record rec);

  public void process (Record me) {

    // run selection and production.
    HashableRecord hashedRec = passInput(me);
    if (hashedRec == null) {
      return;
    }

    // give it to the deduper.
    dedup.take(hashedRec);
  }
}
