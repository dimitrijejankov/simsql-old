

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

import simsql.shell.TableStatistics;
import java.io.*;

/**
 * Silly wrapper class around HDFSTableStats to interact with the
 * rest of the system...
 */
public class TableStatisticsWrapper implements TableStatistics<Record> {

  HDFSTableStats me;

  public TableStatisticsWrapper() {
    me = new HDFSTableStats();
  }

  public TableStatisticsWrapper(HDFSTableStats me) {
    this.me = me;
  }

  public void clear() {
    me.clear();
  }

  public void take(Record inRec) {
    me.take(inRec);
  }

  @SuppressWarnings("unchecked")
  public void consume(TableStatistics<Record> inCol) {
    me.consume((HDFSTableStats) inCol);
  }

  public void load(String path) throws IOException, ClassNotFoundException {
    me.load(path);
  }

  public void save(String path) throws IOException {
    me.save(path);
  }

  public long numTuples() {
    return me.numTuples();
  }

  public int numAttributes() {
    return me.numAttributes();
  }

  public long numUniques(int att) {
    return me.numUniques(att);
  }

  public String toString() {
    return me.toString();
  }

}
