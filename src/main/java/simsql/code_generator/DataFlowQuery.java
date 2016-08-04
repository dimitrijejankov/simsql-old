

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


package simsql.code_generator;
import java.util.*;
import simsql.shell.ExecutableQuery;

/**
 * Encapsulates the result of the translation from the translation
 * between Prolog and DataFlow.
 *
 * @author Luis.
 */
public class DataFlowQuery implements ExecutableQuery {

  // A string containing a translated query that can be given to the
  // DataFlow interpreter.
  String queryStr;

  // A string containing a GraphViz representation of the query graph.
  String graphStr;

  // Errors go here...
  String errorStr;

  // Map between the final relations that have been created and their
  // corresponding attribute names.
  HashMap<String, ArrayList<String>> finalAttributes;

  // Map between the final relation attributes and a string
  // representation of their data types.
  HashMap<String, String> finalTypes;

  // Map between the final relation attributes and a boolean that denotes
  // if they are random.
  HashMap<String, Boolean> finalRandomAttributes;

  // Map between the final relation name and its file path.
  HashMap<String, String> finalPaths;


  // counts the nodes...
  int numNodes;

  // Default constructor.
  public DataFlowQuery() {
    numNodes = 0;
    errorStr = null;
    queryStr = "";
    graphStr = "";
    finalAttributes = new HashMap<String, ArrayList<String>>();
    finalTypes = new HashMap<String, String>();
    finalRandomAttributes = new HashMap<String, Boolean>();
    finalPaths = new HashMap<String, String>();
  }


  // sets the DataFlow string
  public void setQueryStr(String queryStr) {
    this.queryStr = queryStr;
  }

  // returns the DataFlow string
  public String getQueryStr() {
    return queryStr;
  }

  // adds a new edge to the graph
  public void addGraphEdge(String parent, String child) {
    graphStr += parent + " -- " + child + ";\n";
  }

  // adds a new node to the graph
  public void addGraphNode(String node, String kind, ArrayList<String> planNodes, boolean isFinal) {
    graphStr += "subgraph cluster_" + (numNodes++) + "{\nstyle=filled\n;color=";
    graphStr += isFinal ? "lightblue;\n" : "lightgrey;\n";
    graphStr += "label=\"" + kind + " (" + node + ")\";\n";

    for (String ss: planNodes) {
      graphStr += ss + "; ";
    }

    graphStr += "\n}\n";      
  }

  // returns the complete graph string.
  public String getGraphStr() {
    return "strict graph {\nnode [fontname=\"Helvetica\"];\n" + graphStr + "}\n";
  }

  // adds a final relation with its set of attributes.
  public void setFinalAttributes(String relation, ArrayList<String> attributes) {
    finalAttributes.put(relation, attributes);
  }

  // adds a final attribute with its type.
  public void setFinalType(String attribute, String type) {
    finalTypes.put(attribute, type);
  }

  // adds a random attribute.
  public void setFinalRandomAttribute(String attribute, boolean isRandom) {
    finalRandomAttributes.put(attribute, isRandom);
  }

  public void setFinalPath(String relation, String path) {
    finalPaths.put(relation, path);
  }

  // gets the filename for a final relation
  public String getFinalPath(String relation) {
    return finalPaths.get(relation);
  }

  // gets the set of relations
  public ArrayList<String> getFinalRelations() {
    return new ArrayList<String>(finalAttributes.keySet());
  }

  // gets the set of final attributes for a given relation
  public ArrayList<String> getFinalAttributes(String relation) {
    return finalAttributes.get(relation);
  }

  // gets the type of a given attribute
  public String getFinalType(String attribute) {
    return finalTypes.get(attribute);
  }

  // returns true if a given attribute is random
  public boolean getFinalRandomAttribute(String attribute) {
    return finalRandomAttributes.get(attribute);
  }

  // returns a possible error.
  public String isError() {
    return errorStr;
  }

  // sets the error string.
  public void setError(String str) {
    errorStr = str;
  }

  // cleans up the debris...
  public void cleanUp() {
    // nothing to do here.
  }
}
