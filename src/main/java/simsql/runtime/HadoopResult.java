

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

import simsql.shell.RuntimeOutput;
import simsql.shell.PhysicalDatabase;
import simsql.compiler.*;
import java.util.*;
/**
 * Encapsulates the result of a query that was executed by the runtime.
 *
 * @author Luis.
 */
public class HadoopResult implements RuntimeOutput {

  // an error message.
  private String errorMessage;
  private String outputRelationFile;
  private ArrayList<String> outputRelationAtts;
  private ArrayList<Relation> finalRelations;
  private ArrayList<String> filesToUnlink;

  public HadoopResult() {

    // no errors = null
    errorMessage = null;
    outputRelationFile = null;
    outputRelationAtts = null;
    finalRelations = new ArrayList<Relation>();
    filesToUnlink = new ArrayList<String>();
  }

  // record an error
  public void setError(String err) {
    errorMessage = err;
  }

  // returns the error message
  public String isError() {
    return errorMessage;
  }

  // returns the final relations
  public ArrayList<Relation> getFinalRelations() {
    return finalRelations;
  }

  // adds a new final relation
  public void addFinalRelation(String name, String path, ArrayList<simsql.compiler.Attribute> atts, long tupleNum) {

    for (simsql.compiler.Attribute a: atts) {

      long val = a.getUniqueValue();
      a.setUniqueValue(val);
    }

    Relation rel = new Relation(name, path, atts);

    rel.setTupleNum(tupleNum);
    finalRelations.add(rel);
  }


  // sets the output relation
  public void setOutputRelation(String file, ArrayList<String> atts) {
    outputRelationFile = file;
    outputRelationAtts = atts;
  }

  // returns the root, if there is any
  public Relation getOutputRelation(PhysicalDatabase db) {

    // null if there's nothing...
    if (outputRelationFile == null)
      return null;

    // otherwise, create.
    ArrayList<simsql.compiler.Attribute> atts = new ArrayList<simsql.compiler.Attribute>();
    for (String ss: outputRelationAtts) {
      // simsql.compiler.Attribute newAtt = new simsql.compiler.Attribute(ss, simsql.compiler.Attribute.getStringType("unknown"), db.getTableName(outputRelationFile), false);
      simsql.compiler.Attribute newAtt = new simsql.compiler.Attribute(ss, new VersType(), db.getTableName(outputRelationFile));         // TO-DO

      // THIS IS TEMPORARY
      newAtt.setUniqueValue(10);

      atts.add(newAtt);
    }

    Relation rel = new Relation(db.getTableName(outputRelationFile), outputRelationFile, atts);
    rel.setTupleNum(10);
    return rel;
  }

  public void addFileToUnlink(String file) {
    filesToUnlink.add(file);
  }

  public ArrayList<String> getFilesToUnlink() {
    return filesToUnlink;
  }

}
