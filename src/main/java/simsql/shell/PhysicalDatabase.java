

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

import simsql.compiler.Relation;
import simsql.runtime.Record;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.File;

// this interface encapsulates the physical database.  It records metadata about the database 
// (for our current runtime, this would include at a minimum the typecode, as well as the
// directory in HDFS that actually stores the file) and also allows the system (outside of
// the runtime) to interact with the database.

public interface PhysicalDatabase <DataFlowProgram extends ExecutableQuery>  {

  // this sets up a phystical database, saving the required info to the indcated location
  void setUp (File where);

  // backs up and restores the contents in HDFS
  void backupTo (String where);
  void restoreFrom (String where);

  // cleans up the physical database by removing all of the zero-byte relations whose name containts the given string
  public void removeZeroSizeRelations (String containing);

  // this attempts to load the physical database description from the file indicated...
  // if there is problem or the indicated file does not exist, then false is returned
  boolean getFromFile (File file);

  // this saves all of the info associated with this object... a call to this method must happen
  // before the system shuts down, or else all of the info will be lost
  void save ();

  // this changes the name of a relation in the physical database
  void rename (String oldName, String newName);

  // this registers a relation with the given name to the physical database (this might reserve
  // a physical file name for it, as well as a type code, for example)
  boolean registerTable (String relName);

  // this de-registers the relation, but does not delete the underlying storage 
  boolean unregisterTable (String relName);

  // this de-registers the relation, AND deletes the underlying storage
  void deleteTable (String relName);

  // returns the name of the table based on its file name.
  String getTableName (String fileName);

  // this bulk loads a file, using the simsql.runtime.Attribute.readSelfFromTextStream () method
  // on the text stream that is passed into the method
  TableStatistics<Record> loadTable (Relation loadMe, String localFilePath, RuntimeParameter params, String sortAtt) throws IOException;

  // pretty prints a given relation to the standard output.
  public void printRelation (Relation rel);

  // writes a given relation into a text file
  public void saveRelation (Relation rel, String outputFile);

  // this creates an iterator factory for the indicated relation.  In other words, you can
  // get back an object iteratorFactory from this method, and then say:
  // 
  // for (Record r : iteratorFactory) {...}
  //
  // note that to make sure we don't have resource leaks, this method can only be used to
  // create a SINGLE iterator factory at a given instant (if more are created, then the
  // older ones are deallocated), and that iterator factory can only be used to iterate
  // over the relation ONE time simulatanously (though the factory can be used to create
  // many iterators; they just can't be used at the same time) 
  Iterable <Record> getIteratorFactoryForRelation (Relation forMe);

  // is a given table sorted?
  public boolean isTableSorted (String table);

  // returns the position of the sorting atribute for this table
  public int getTableSortingAttribute (String table);

  public void setNumAtts(String table, int numAtts);
  public int getNumAtts(String table);


  // returns the type code associated with this table
  public short getTypeCode (String table); 

  // returns the size of this table
  public long getTableSize (String table);

  // remember the size of this table
  public void setTableSize (String table, long sizeToSetTo);

  //
}
