

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


package simsql.optimizer;

import simsql.shell.SimSQLCompiledQuery;
import simsql.shell.FileOutput;
import java.util.HashMap;
import java.util.ArrayList;

/**
 * A class for the output of an optimized query, including data on
 * materialized views that passes through the compiler.
 *
 * @author Luis
 */
public class SimSQLOptimizedQuery extends FileOutput {

  // materialized views and their attributes
  private HashMap<String, ArrayList<String>> materializedViewAtts = new HashMap<String, ArrayList<String>>();
  public SimSQLOptimizedQuery(String extension, SimSQLCompiledQuery cQuery) {
    super(extension);
    materializedViewAtts.putAll(cQuery.getMaterializedViews());
  }

  public ArrayList<String> getMaterializedViews() {
    ArrayList<String> materializedViews = new ArrayList<String>();
    materializedViews.addAll(materializedViewAtts.keySet());

    return materializedViews;
  }

  public ArrayList<String> getViewAtts(String viewName) {
    ArrayList<String> viewAtts = new ArrayList<String>();
    viewAtts.addAll(materializedViewAtts.get(viewName));

    return viewAtts;
  }
}
