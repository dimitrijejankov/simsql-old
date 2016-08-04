

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


package simsql.shell;
import java.io.*;
import java.lang.*;

// this interface defines a container for the statistics of a given
// table
public interface TableStatistics <RecordIn> {

  // clears the structure, back to its default 'empty' values.
  void clear();

  // takes an input record and updates the statistics.
  void take(RecordIn inRec);

  // consumes another set of statistics from the same table, associated
  // with another partition.
  void consume(TableStatistics<RecordIn> inTable);

  // loads the statistics from a file.
  void load(String path) throws IOException, ClassNotFoundException;

  // saves the contents to a file.
  void save(String path) throws IOException;

  // returns the total number of tuples in a given table.
  long numTuples();

  // returns the number of attributes.
  int numAttributes();

  // returns the number of unique values for any given attribute.
  long numUniques(int att);
}
