

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

import simsql.shell.RuntimeParameter;
import simsql.shell.PhysicalDatabase;
import java.util.*;
import java.io.*;
import org.antlr.runtime.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;


class AggregateOp extends RelOp {

  // returns a set of necessary mappings.   
  public Set<String> getNecessaryValues() {
    return new HashSet<String>(Arrays.asList
			       ("operation", "input", "input.inFiles", "input.inAtts", "input.typeCode",
				"aggregates", "output", "output.outFile", "output.typeCode", "output.outAtts"));
  }
    
  // returns the name of the operation
  public String getOperatorName() {
    return "Aggregate" + (isChained ? "Chained" : "");
  }

  // returns the set of inputs
  public String[] getInputs() {
    ArrayList<String> inputSet = new ArrayList<String>();
    inputSet.addAll(getValue("input.inFiles").getStringList());
    String[] foo = {""};
    return inputSet.toArray(foo);
  }


  public Map<String, Set<Set<String>>> getExistingSortAtts(PhysicalDatabase myDBase) {
    return getBaseSortAtts(myDBase, "input.inFiles", "input.inAtts");
  }

  // returns the output of this operation
  public String getOutput() {
    return getValue("output.outFile").getStringLiteral();
  }

  // returns the set of output attribute names
  public String[] getOutputAttNames() {

    ArrayList<String> out = new ArrayList<String>();
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      out.add(a.getIdentifier());
    }

    return out.toArray(new String[0]);
  }

  public short getOutputTypeCode() {
    return getValue("output.typeCode").getInteger().shortValue();
  }

  // set some configurations
  public void setConfigurations(Configuration conf, RuntimeParameter params) {

    ExampleRuntimeParameter p = (ExampleRuntimeParameter)params;
    conf.setLong("simsql.preAggregationBufferSize", (p.getMemoryPerCPUInMB() / 8) * 1024 * 1024);
  }

  // number of reducers.
  public int getNumReducers(RuntimeParameter params) {

    // empty set of grouping attributes? one reducer in one tuple
    if (sortingAtts.isEmpty()) {
      return 1;
    }

    // otherwise, use all the available CPUs as reducers.
    ExampleRuntimeParameter p = (ExampleRuntimeParameter)params;
    return p.getNumCPUs();
  }

  // returns the functions -- from the selection, aggregate and output expressions
  public String[] getFunctions() {
    HashSet<String> one = new HashSet<String>();

    // go through the input selections
    ParsedRHS selRHS = getValue("input.selection");
    if (selRHS != null) {
      one.addAll(selRHS.getExpression().getAllFunctions());
    }

    // go through the aggregate expressions
    for (AggregateExp agg: getValue("aggregates").getAggregateList()) {
      if (agg.getExpression() != null) {
	one.addAll(agg.getExpression().getAllFunctions());
      }
    }

    // go through the output selections
    ParsedRHS oSelRHS = getValue("output.selection");
    if (oSelRHS != null) {
      one.addAll(oSelRHS.getExpression().getAllFunctions());
    }

    // and through the output attributes
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      one.addAll(a.getExpression().getAllFunctions());
    }

    return one.toArray(new String[0]);
  }


  // returns the set of macro replacements
  public Map<String, String> buildMacroReplacements() {

    HashMap<String, String> replacements = new HashMap<String, String>();

    // get the basic replacements for the input.
    Map<String, String> inAtts = buildAttributeReplacements("input.inAtts", "");
    replacements.put("inputNumAtts", "" + inAtts.size());

    replacements.put("inputTypeCode", "" + getValue("input.typeCode").getInteger());
    replacements.put("inputSelection", buildSelectionReplacement("input.selection", inAtts));
    replacements.put("functionDeclarations", "" + buildFunctionDeclarations());

    // are there any grouping attributes?
    // "aggregatorHash" is the secondary hash.
    ParsedRHS groupByAttsRHS = getValue("input.groupByAtts");
    HashMap<String, String> aggAtts = new HashMap<String, String>();
    int counter = 0;
    if (groupByAttsRHS == null) {

      // if it is null, then everyone will hash/belong to the same group.
      replacements.put("groupingAttsAssignments", "");
      replacements.put("groupingAttsComparisons", "");
      replacements.put("groupingAttsPositions", "");
      replacements.put("aggregatorHash", "");
      replacements.put("primaryHash", "");
    } else {

      // otherwise, the first attributes are for the groupBy.
      // and the hash is computed for them.
      String gbStr = "";
      String hhStr = "";
      String coStr = "";
      String poStr = "";
      String primhhStr = "";
      boolean firstPos = true;
      for (String att: groupByAttsRHS.getIdentifierList()) {
	String newName = "atts[" + counter + "]";

	gbStr += "aggRec." + newName + " = " + inAtts.get(att) + ";\n    ";
	hhStr += " ^ " + newName + ".getHashCode()";
	if (sortingAtts.contains(att)) {
	  primhhStr += " ^ " + newName + ".getHashCode()";
	}

	coStr += " && " + newName + ".equals(rec." + newName + ").allAreTrue()";

	if (firstPos) {
	  poStr += counter;
	  firstPos = false;
	} else {
	  poStr += ", " + counter;
	}

	aggAtts.put("input." + att, newName);
	counter++;
      }

      replacements.put("groupingAttsAssignments", gbStr);
      replacements.put("groupingAttsComparisons", coStr);
      replacements.put("groupingAttsPositions", poStr);
      replacements.put("aggregatorHash", hhStr);
      replacements.put("primaryHash", primhhStr);
    }

    // now, we'll create the aggregator attributes and their initializers/consumers/finalizers.
    String initStr = ""; // initializers
    String consStr = ""; // consumers. finalizers are put in the HashMap of the attributes.


    for (AggregateExp agg: getValue("aggregates").getAggregateList()) {


      String finStr = "";
      String funcStr = "lessThan"; // for the max/min

      switch(agg.getType()) {

      case COUNTALL: {
	String attCount = "atts[" + counter + "]";
	counter++;

	initStr += "aggRec." + attCount + " = aggRec.isPresent";
	for (String s: inAtts.keySet()) {
	  initStr += ".and(" + inAtts.get(s) + ".isNull().not())";
	}
	initStr += ".toInt();\n\t";

	consStr += attCount + " = " + attCount + ".add(me." + attCount + ");\n\t";
	aggAtts.put("aggregates." + agg.getIdentifier(), attCount);
      }		
	break;

      case COUNT: {
	String attCount = "atts[" + counter + "]";
	String tempAttCount = "temp_" + counter;
	counter++;

	initStr += "aggRec." + attCount + " = " + agg.getExpression().print(inAtts) + ".isNull().not().and(aggRec.isPresent).toInt();\n\t";

	
	// add recycling stuff
	consStr += "Attribute " + tempAttCount + " = " + attCount + ";\n\t";	
	consStr += attCount + " = " + attCount + ".add(me." + attCount + ");\n\t";

	consStr += tempAttCount + ".recycle();\n\t";
	consStr += "me." + attCount + ".recycle();\n\t";

	aggAtts.put("aggregates." + agg.getIdentifier(), attCount);
      }
	break;

      case SUM: {
	String attSum = "atts[" + counter + "]";
	String tempAttSum = "temp_" + counter;

	counter++;

	initStr += "aggRec." + attSum + " = aggRec.isPresent.toInt().multiply(" + agg.getExpression().print(inAtts) + ");\n\t";

	// add recycling stuff.
	consStr += "Attribute " + tempAttSum + " = " + attSum + ";\n\t";
	consStr += attSum + " = " + attSum + ".add(me." + attSum + ");\n\t";

	consStr += tempAttSum + ".recycle();\n\t";
	consStr += "me." + attSum + ".recycle();\n\t";

	aggAtts.put("aggregates." + agg.getIdentifier(), attSum);
      }
	break;

      case AVG: {
	String attCount = "atts[" + counter + "]";
	String tempAttCount = "temp_" + counter;
	counter++;

	String attSum = "atts[" + counter + "]";
	String tempAttSum = "temp_" + counter;
	counter++;

	initStr += "aggRec." + attCount + " = " + agg.getExpression().print(inAtts) + ".isNull().not().and(aggRec.isPresent).toInt();\n\t";
	initStr += "aggRec." + attSum + " = aggRec.isPresent.toInt().multiply(" + agg.getExpression().print(inAtts) + ");\n\t";

	// add recycling stuff.
	consStr += "Attribute " + tempAttCount + " = " + attCount + ";\n\t";
	consStr += "Attribute " + tempAttSum + " = " + attSum + ";\n\t";

	consStr += attCount + " = " + attCount + ".add(me." + attCount + ");\n\t";
	consStr += attSum + " = " + attSum + ".add(me." + attSum + ");\n\t";

	consStr += tempAttCount + ".recycle();\n\t";
	consStr += tempAttSum + ".recycle();\n\t";

	consStr += "me." + attCount + ".recycle();\n\t";
	consStr += "me." + attSum + ".recycle();\n\t";
       	
	aggAtts.put("aggregates." + agg.getIdentifier(), attSum + ".divide(" + attCount + ")");
      }
	break;

      case STDEV:
	finStr = "_agg_sqrt.apply";

      case VAR: {
	String attCount = "atts[" + counter + "]";
	counter++;

	String attSum = "atts[" + counter + "]";
	counter++;

	String attSumSq = "atts[" + counter + "]";
	counter++;

	initStr += "aggRec." + attCount + " = " + agg.getExpression().print(inAtts) + ".isNull().not().and(aggRec.isPresent).toInt();\n\t";
	initStr += "aggRec." + attSum + " = aggRec.isPresent.toInt().multiply(" + agg.getExpression().print(inAtts) + ");\n\t";
	initStr += "aggRec." + attSumSq + " = aggRec.isPresent.toInt().multiply(" + agg.getExpression().print(inAtts) + ".multiply(" + agg.getExpression().print(inAtts) + "));\n\t";

	consStr += attCount + " = " + attCount + ".add(me." + attCount + ");\n\t";
	consStr += attSum + " = " + attSum + ".add(me." + attSum + ");\n\t";
	consStr += attSumSq + " = " + attSumSq + ".add(me." + attSumSq + ");\n\t";

	finStr += "((" + attSumSq + ".divide(" + attCount + ")).subtract((" 
	  + attSum + ".divide(" + attCount + ")).multiply(" + attSum + ".divide(" + attCount + "))))";

	aggAtts.put("aggregates." + agg.getIdentifier(), finStr);

      }
	break;

      case MAX:
	funcStr = "greaterThan";

      case MIN: {
	String attValue = "atts[" + counter + "]";
	String valueBit = "max_" + counter;
	counter++;

	initStr += "aggRec." + attValue + " = " + agg.getExpression().print(inAtts) + ";\n\t";
	consStr += "Bitstring " + valueBit + " = " + attValue + "." + funcStr + "(me." + attValue + ").and(me.isPresent);\n\t";
	consStr += attValue + " = " + attValue + ".multiply(" + valueBit + ".toInt()).add(me." + attValue + ".multiply(" + valueBit + ".not().toInt()));\n\t";
		
	aggAtts.put("aggregates." + agg.getIdentifier(), attValue);
      }
	break;

      case VECTOR: {
        String attValue = "atts[" + counter + "]";
        counter++;
        initStr += "aggRec." + attValue + " = new AggregatorVector((ScalarAttribute)" + agg.getExpression().print(inAtts) + ");\n\t";
        consStr += attValue + " = " + "((AggregatorVector)" + attValue + ").combine((AggregatorVector)me." + attValue + ");\n\t";
        aggAtts.put("aggregates." + agg.getIdentifier(), "((AggregatorVector)" + attValue + ").condense()");
      }
      break;

      case ROWMATRIX: {
        String attValue = "atts[" + counter + "]";
        counter++;
        initStr += "aggRec." + attValue + " = new AggregatorMatrix((VectorAttribute)" + agg.getExpression().print(inAtts) + ", true);\n\t";
        consStr += attValue + " = " + "((AggregatorMatrix)" + attValue + ").combine((AggregatorMatrix)me." + attValue + ");\n\t";
        aggAtts.put("aggregates." + agg.getIdentifier(), "((AggregatorMatrix)" + attValue + ").condense()");
      }
      break;

      case COLMATRIX: {
        String attValue = "atts[" + counter + "]";
        counter++;
        initStr += "aggRec." + attValue + " = new AggregatorMatrix((VectorAttribute)" + agg.getExpression().print(inAtts) + ", false);\n\t";
        consStr += attValue + " = " + "((AggregatorMatrix)" + attValue + ").combine((AggregatorMatrix)me." + attValue + ");\n\t";
        aggAtts.put("aggregates." + agg.getIdentifier(), "((AggregatorMatrix)" + attValue + ").condense()");
      }
      break;

      default:
	throw new RuntimeException("Undefined aggregate type " + agg.getType().toString());
      }
    }
    replacements.put("aggregateInitializers", initStr);
    replacements.put("aggregateConsumers", consStr);
    replacements.put("numAggregatorAtts", "" + counter);

    // now, get the output parts.
    Map<String, String> allAtts = buildAttributeAssignReplacements("output.outAtts", "", "finalRec.");
    allAtts.putAll(aggAtts);
    replacements.put("outputSelection", buildSelectionReplacement("output.selection", allAtts));
    replacements.put("outputAssignments", buildAssignmentReplacements("output.outAtts", "finalRec.", allAtts));
    replacements.put("outputNumAtts", "" + getValue("output.outAtts").getAssignmentList().size());
    replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());

    // to-do
    return replacements;
  }

  // returns the name of the template Java source file.
  public String getTemplateFile() {
    return "AggregateRecords.javat";
  }

  // returns a straight pipe if the aggregate is chained
  private boolean isChained = false;
  private PipeNetwork chainedNetwork = null;
  
  public boolean acceptsAppendable() {
    return false;
  }

  public boolean acceptsPipelineable() {
    return !isChained;
  }


  public boolean isAppendable(String[] inx) {
      
    // if we are already chained, we cannot append.
    if (isChained) {
	return false;
    }

    // to avoid over-optimistically pushing aggregates over long
    // chains of joins and ruining the potential for reducing the number
    // of join jobs, we won't let this operation be appendable unless all of the
    // non-pipelined inputs are still not materialized, because they could be
    // re-evaluated. but if we append before they are re-evaluated, we lose the
    // opportunity to pipeline the preceding operator!
    for (int i=0;i<inx.length;i++) {
	if (getPathsActualSize(new String[]{inx[i]}) == 0) {
	    return false;
	}
    }

    return true;
  }

  public PipeNetwork getAppendableNetwork() {

    if (isChained) {
      throw new RuntimeException("Aggregate operation is already chained/appended!");
    }

    // build the new input network
    PipeNetwork chainedNetwork = new PipeNetwork(getInputFiles(), new Short[]{7747}); // 7747 is the typecode for the aggregators.

    // get the current input network
    PipeNetwork currentInput = getPipelinedVersion();

    // set the flag.
    isChained = true;

    // replace the network.
    replaceNetworkOnInputSide(chainedNetwork);

    return currentInput;
  }

  public PipeNetwork getPipelinedVersion() {
    return addPipelinedOperationToInputPipeNetwork(new PipelinedAggregate(this));
  }


  // returns the mapper class.
  // if this aggregate is chained, use a different mapper class.
  public Class<? extends Mapper> getMapperClass() {
    if (isChained) 
      return ChainedAggregateMapper.class;

    return AggregateMapper.class;
  }

  // returns the reducer class.
  public Class<? extends Reducer> getReducerClass() {
    return AggregateReducer.class;
  }
    
  public AggregateOp(Map<String, ParsedRHS> inValues) {
    super(inValues);

    // set up the default algorithm to run.
    ParsedRHS gbRHS = getValue("input.groupByAtts");
    sortingAtts = new HashSet<String>();
    groupingAtts = new HashSet<String>();
    if (gbRHS != null && gbRHS.getIdentifierList().size() > 0) {
      sortingAtts.addAll(gbRHS.getIdentifierList());
      groupingAtts.addAll(gbRHS.getIdentifierList());
    }

    allSortingAtts = new HashSet<Set<String>>();
    allSortingAtts.add(sortingAtts);
  }

  // sets of sorting and grouping attributes to use.
  private Set<Set<String>> allSortingAtts;
  private Set<String> sortingAtts;
  private Set<String> groupingAtts;

  // here, we determine if we are going to do a map-side aggregate, and which
  // sort order we'll be following.
  public void determineWhatAlgorithmToRun (Set <Set <String>> votes, Map <String, Set <String>> fds, 
					   Map <String, Set <Set <String>>> sortingAttsIn) {

    // get the set of input sorts.
    String feederFile = getNetworkOnInputSide().getFeeder(getInputs()[0]);
    Set<Set<String>> inSortingAtts = sortingAttsIn.get(feederFile);
    if (inSortingAtts == null) {
	inSortingAtts = new HashSet<Set<String>>();
    }

    inSortingAtts.addAll(getNetworkOnInputSide().findAnyAdditionalSortAttributes(feederFile, inSortingAtts));

    // check if we have an empty set of grouping/sorting atts. there is nothing
    // we can do in that case because the output is a single tuple.
    if (groupingAtts.isEmpty()) {
      return;
    }

    // we'll try to do a map-side aggregate first, which can only be done if
    // the relation is not final and if we have not previously chained the aggregate.
    if (!isFinal && !isChained) {
            
      // get all the subsets to our grouping atts.
      Set<Set<String>> allGroupings = SubsetMachine.getAllSubsets(groupingAtts);

      // if any of those subsets are contained by our input sort
      // attributes, we'll do a map-side aggregate on that, and
      // preserve the order.
      for (Set<String> ss : allGroupings) {

	if (inSortingAtts.contains(ss)) {
	  System.out.println("Matched aggregate sorting on " + ss);
	  sortingAtts = ss;	  
	  allSortingAtts = inSortingAtts;
	  return;
	}
      }            
    }

    // if we got here, we have to reduce. then, we'll see if we can
    // honor any of the requested sort orders.
    if (votes.size() > 0) {

      // of all the requested orders that are subsets of our grouping
      // atts, find the largest.
      Set<Set<String>> allGroupings = SubsetMachine.getAllSubsets(groupingAtts);
	
      Set<String> biggest = null;
      for (Set<String> s : votes) {
	if (allGroupings.contains(s) && (biggest == null || s.size() > biggest.size())) {
	  biggest = s;
	}
      }

      // did we find something?
      if (biggest != null) {
	System.out.println("Sorting aggregate groups on " + biggest + " (requested)");
	sortingAtts = biggest;
	allSortingAtts.clear();
	allSortingAtts.add(biggest);
	return;
      }

      System.out.println("Sorting aggregate groups on " + sortingAtts + " (original)");
    }
  }

  // simply returns the sorting atts we have decided.
  public Set <Set <String>> getSortedOutputAtts () {
    return allSortingAtts;
  }

  // our possible sorting atts are: all subsets of the set of grouping atts.
  public Set <Set <String>> getAllPossibleSortingAtts () {

    if (groupingAtts.size() > 0)
      return SubsetMachine.getAllSubsets(groupingAtts);
    else 
      return new HashSet<Set<String>>();
  }

  // we *do* have a preferred sort order: our grouping attributes.
  public Set <String> getPreferredSortOrder (String fName, Map <String, Set <String>> fds, 
	Set <Set <String>> allPossibleSortingAtts) {

    // get the largest possible sort order that can help us do our aggregate.
    if (groupingAtts.isEmpty()) {

      // though, null if we don't need anything in particular.
      return null;
    }

    Set<Set<String>> allGroupings = SubsetMachine.getAllSubsets(groupingAtts);
    Set<String> biggest = null;
    for (Set<String> ss : allGroupings) {
      if (allPossibleSortingAtts.contains(ss) && (biggest == null || ss.size() > biggest.size())) {
	biggest = ss;
      }
    }

    // return it -- null if we found nothing.
    return biggest;
  }
    
}
