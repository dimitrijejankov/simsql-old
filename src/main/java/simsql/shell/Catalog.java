

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
import java.io.File;
import java.util.List;

import simsql.compiler.Relation;
import simsql.compiler.View;

// this encapsulates the system catalog.  Note that the "catalog" here stores semantic and statistical
// information about the database tables and functions present in the system.  It does not store any
// information about the physical tables present in the system (that is the job of the PhysicalDatabase
// interface), nor does it store any information about the runtime parameters that should be used (that
// is the job of the RuntimeParameters interface).
public interface Catalog {
  
  // this builds the default catalog
  void setUp (File where);
  
  // this attemps to read the catalog from the file indicated; if there is a problem
  // or if the file does not exist, a false is returned; otherwise, a true comes back
  boolean getFromFile (File which);
  
  // this saves the catalog to a file... a call to this method is guaranteed before the system shuts down
  // will only be called once as the system dies
  void save ();

  // this gets information about a particular relation in the catalog
  Relation getRelation (String relName);	
  View getView (String viewName);

  // this gets all the modulo relations
  List<View> getModuloRelations(String baseRel);

  // prints all the table names
  void listTables();
  
  // prints all the view names
  void listViews();

  // adds a relation to the catalog
  void addRelation (Relation addMe);

  // updates the statistics of a given relation.
  void updateTableStatistics(Relation rel, TableStatistics stats);
  void updateTableStatistics(Relation rel, Relation rel2);

}
