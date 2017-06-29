

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
import java.io.*;
import org.antlr.runtime.*;
import simsql.shell.RuntimeParameter;
import simsql.shell.PhysicalDatabase;
import simsql.code_generator.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;

public class SelectionOp extends RelOp {


  // returns a set of necessary mappings.   
  public Set<String> getNecessaryValues() {
    return new HashSet<>(Arrays.asList
			("operation", "input", "input.inFiles", "input.typeCode", "input.inAtts", "output", "output.outFile",
			"output.typeCode", "output.outAtts"));
  }

  // returns the name of the operation
  public String getOperatorName() {
    return "Selection" + (isChained ? "Chained" : "");
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

  public short getOutputTypeCode() {
    return getValue("output.typeCode").getInteger().shortValue();
  }

  // returns the set of output attribute names
  public String[] getOutputAttNames() {

    ArrayList<String> out = new ArrayList<String>();
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      out.add(a.getIdentifier());
    }

    return out.toArray(new String[0]);
  }

  // returns the functions -- from the selections and output expressions
  public String[] getFunctions() {

    HashSet<String> one = new HashSet<String>();

    // go through the selections
    ParsedRHS selRHS = getValue("selection");
    if (selRHS != null) {
      one.addAll(selRHS.getExpression().getAllFunctions());
    }

    // and through the output attributes
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      one.addAll(a.getExpression().getAllFunctions());
    }

    return one.toArray(new String[0]);
  }

  // number of reducers -- depends on duplicate removal.
  public int getNumReducers(RuntimeParameter params) {

    // map-only job if there is no duplicate removal / finals.
    if (!runSelectionReducer) {
      return 0;
    }

    // otherwise, use reducers.
    ExampleRuntimeParameter p = (ExampleRuntimeParameter)params;
    return p.getNumCPUs();
  }

  // returns the set of macro replacements
  public Map<String, String> buildMacroReplacements() {

    Map<String, String> replacements = new HashMap<String, String>();
    Map<String, String> inAtts = buildAttributeReplacements("input.inAtts", "");

    replacements.put("inputNumAtts", "" + inAtts.size());
    replacements.put("inputTypeCode", "" + getValue("input.typeCode").getInteger());
    replacements.put("inputSelection", buildSelectionReplacement("selection", inAtts));
    replacements.put("inputAssignments", buildAssignmentReplacements("output.outAtts", "returnVal.", inAtts));
    replacements.put("outputNumAtts", "" + getValue("output.outAtts").getAssignmentList().size());
    replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());
    replacements.put("functionDeclarations", "" + buildFunctionDeclarations());

    // make a hash on all the output attributes.
    String hhStr = "";
    String phStr = "";
    for (int i=0;i<getValue("output.outAtts").getAssignmentList().size();i++) {
      hhStr += " ^ atts[" + i + "].getHashCode()";

      if (sortingAtts.contains(getValue("output.outAtts").getAssignmentList().get(i).getIdentifier())) {
	phStr += " ^ atts[" + i + "].getHashCode()";
      }
    }

    replacements.put("selectionHash", hhStr);
    replacements.put("primaryHash", phStr);

    return replacements;
  }    

  // returns the name of the template Java source file.
  public String getTemplateFile() {
    return "SelectionRecords.javat";
  }

  // returns the mapper class.
  public Class<? extends Mapper> getMapperClass() {
    return isChained ? ChainedSelectionMapper.class : SelectionMapper.class;
  }

  // returns the reducer class.
  public Class<? extends Reducer> getReducerClass() {
    return SelectionReducer.class;
  }

  public Class getOutputValueClass() {
    return SelectionOut.class;
  }


  // configure the job...
  public void setConfigurations(Configuration conf, RuntimeParameter params) {

    // set for removing duplicates
    conf.setBoolean("simsql.removeDuplicates", removeDuplicates);

    // set for reducer thing.
    conf.setBoolean("simsql.runSelectionReducer", runSelectionReducer);

    // set the pre-dedup buffer size: 1/8 of available memory.
    ExampleRuntimeParameter p = (ExampleRuntimeParameter)params;
    conf.setLong("simsql.duplicatesBufferSize", (p.getMemoryPerCPUInMB() / 8) * 1024 * 1024);
  }

  public SelectionOp(Map<String, ParsedRHS> valuesIn) {
    super(valuesIn);
    
    // get the set of all output atts.
    outputAtts = new HashSet<String>();
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      outputAtts.add(a.getIdentifier());
    }

    // are we removing duplicates?
    ParsedRHS removeDup = getValue("removeDuplicates");
    removeDuplicates = (removeDup != null && removeDup.getStringLiteral().equals("true"));

    // if we remove duplicates, our output attributes are our default sorting attributes.
    sortingAtts = new HashSet<String>();
    if (removeDuplicates) {
      sortingAtts.addAll(outputAtts);
    }

    allSortingAtts = new HashSet<Set<String>>();
    allSortingAtts.add(sortingAtts);

    // by default, run a reducer if we are removing duplicates.
    runSelectionReducer = (removeDuplicates);
  }
    
  // do we remove duplicate records?
  private boolean removeDuplicates;

  // do we run a reducer on this?
  private boolean runSelectionReducer;

  // our set of sorting and output attributes.
  private Set<String> sortingAtts;
  private Set<Set<String>> allSortingAtts;
  private Set<String> outputAtts;

  public boolean removeDuplicates() {
    return removeDuplicates;
  }


  // are we chained already?
  private boolean isChained = false;

  public boolean acceptsAppendable() {
    return false;
  }

  public boolean acceptsPipelineable() {
    return !isChained;
  }

  public boolean isPipelineable (int megabytesIn) {

      // can't pipeline if already chained. though that doesn't make
      // sense but it's good to check for sanity!
      if (isChained) {
	  return false;
      }

      // pipelined duplicate removals not supported yet (requires sort).
      if (removeDuplicates) {
	  return false;
      }

      // i had to disable this for seeds...
      for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
	  
	  Expression e = a.getExpression();
	  if (e.getType().equals("func") && e.getValue().contains("seed")) {
	      return false;
	  }
      }

      // take some 16MB for all the data structures and processing and
      // whatnot.
      return 16L < megabytesIn;
  }

  public boolean isAppendable(String[] inx) {

    // TO-DO: cannot append if already chained.
    return !isChained;
  }

  public PipeNetwork getAppendableNetwork() {

    if (isChained) {
      throw new RuntimeException("Selection operation is already chained/appended!");
    }

    // build the new input network
    PipeNetwork chainedNetwork = new PipeNetwork(getInputFiles(), new Short[]{-1}); // 7747 is the typecode for the aggregators.
    
    // get the current input network
    PipeNetwork currentInput = addPipelinedOperationToInputPipeNetwork(new PipelinedSelection(this, true));

    // set the flag.
    isChained = true;

    // replace the network.
    replaceNetworkOnInputSide(chainedNetwork);
    return currentInput;
  }

  public PipeNetwork getPipelinedVersion() {
    return addPipelinedOperationToInputPipeNetwork(new PipelinedSelection(this, false));
  }

  // execution -- if we're chained and don't have to reduce, we skip
  // all of this.
  public boolean run(RuntimeParameter params, boolean verbose) {

    // if not chained OR if we need to run a reducer, run normally.
    if (!isChained || runSelectionReducer) {
      return super.run(params, verbose);
    }

    // otherwise, just rename.
    System.out.println("This is a chained map-only selection that does not need to run a job.");
    System.out.println("Renaming the input/output paths and copying the stats.");

    String[] inDirs = getNetworkOnInputSide().getPipelinedInputFiles();
    if (inDirs.length > 1) {
      throw new RuntimeException("Why is this selection using more than one input?");
    }
    
    // set the size.
    if (getDB() != null) {
      long inSize = getDB().getTableSize(getDB().getTableName(inDirs[0]));
      getDB().setTableSize(getDB().getTableName(getOutput()), inSize);
      getDB().setNumAtts(getDB().getTableName(getOutput()), getOutputAttNames().length);
    }

    // do the renaming
    try {
      Configuration conf = new Configuration();
      FileSystem dfs = FileSystem.get(conf);
      Path src = new Path(inDirs[0]);
      Path dest = new Path(getOutput());

      return dfs.rename(src, dest);
    } catch (Exception e) {
      throw new RuntimeException("Could not rename directories!" + e);
    }
  }


  // here, we determine if we are going to do a map-side selection, and which
  // sort orders we'll be following.
  public void determineWhatAlgorithmToRun (Set <Set <String>> votes, Map <String, Set <String>> fds, 
					   Map <String, Set <Set <String>>> sortingAttsIn) {


    // first off, get the set of sorting atts that we have from the input.
    String feeder = getNetworkOnInputSide().getFeeder(getInputs()[0]);
    Set<Set<String>> inSortAtts = new HashSet<Set<String>>();

    sortingAttsIn.get(feeder);
    if (sortingAttsIn.containsKey(feeder)) {
	inSortAtts.addAll(sortingAttsIn.get(feeder));
    }

    // update it with the rest.
    inSortAtts.addAll(getNetworkOnInputSide().findAnyAdditionalSortAttributes(feeder, inSortAtts));

    // check if we are removing duplicates or if this is a final
    // relation. in the case of finals -- we just pass on our stuff,
    // and in the case of duplicate removals, we do the sorting we
    // want to do.
    if (isFinal || removeDuplicates) {

      if (!removeDuplicates) {
	allSortingAtts = inSortAtts;
      }

      System.out.println("No change due to finals/removed duplicates: " + allSortingAtts);
      return;
    }

    // if we have sort order requests, we'll try to honor them.
    if (votes.size() > 0) {

      // if our input sort is among those sort requests, we'll just keep it and don't do anything.
      for (Set<String> s : inSortAtts) {
	if (votes.contains(s)) {
	  allSortingAtts = inSortAtts;
	  System.out.println("Maintaining selection orders on " + allSortingAtts);
	  return;
	}
      }

      // if not, then see if we have some statistics on the input
      // attributes, so as to choose a single one.
      try {
	  HDFSTableStats stats = new HDFSTableStats();
	  stats.load(getInputs()[0]);
	  if (stats.numTuples() > 0) {
	      
	      // get the array of input attributes.
	      ArrayList<String> inputAtts = new ArrayList<String>();
	      inputAtts.addAll(getValue("input.inAtts").getIdentifierList());
	      
	      String largestAtt = null;
	      long largestSize = -1;
	      for (Set<String> sw : votes) {
		  for (String s: sw) {
		      
		      // get the position onf the attribute in the array.
		      int attPos = inputAtts.indexOf(s);
		      
		      // and get its stats
		      long itsSize = stats.numUniques(attPos);
		      if (largestSize < itsSize) {
			  largestSize = itsSize;
			  largestAtt = s;
		      }
		  }
	      }
	      
	      
	      // the attribute with most distinct values is the one we choose.
	      Set<String> wow = new HashSet<String>();
	      wow.add(largestAtt);
	      System.out.println("Obtaining sorting attributes from statistics: " + wow);
	      sortingAtts = wow;
	      allSortingAtts.clear();
	      allSortingAtts.add(wow);
	      runSelectionReducer = true;
	      return;
	  }
      } catch (Exception e) {
	  // nothing here, in fact.
      }
      
      // otherwise, we'll find the largest of the requested sort orders.
      Set<String> biggest = null;
      for (Set <String> s: votes) {
	if (biggest == null || s.size() > biggest.size()) {
	  biggest = s;
	}
      }

      System.out.println("Sorting selection output on " + biggest);
      runSelectionReducer = true;
      sortingAtts = biggest;
      allSortingAtts.clear();
      allSortingAtts.add(biggest);
      return;
    }


    // final case: just give out these input sorting atts
    System.out.println("Maintaining selection orders without any matching: " + inSortAtts);
    allSortingAtts = inSortAtts;
  }

  // simply returns the sorting atts we have decided.
  public Set <Set <String>> getSortedOutputAtts () {
    return allSortingAtts;
  }

  
  // we can sort on anything we output, except in the case of a strict
  // duplicate removal.
  public Set <Set <String>> getAllPossibleSortingAtts () {
    if (removeDuplicates) {
      Set <Set <String>> outSet = new HashSet<Set<String>>();
      outSet.addAll(allSortingAtts);
      return outSet;
    }

    return SubsetMachine.getAllSubsets(outputAtts);
  }

   // this asks the relational operation whether it has a preferred sort order for the input atts
  public Set <String> getPreferredSortOrder (String fName, Map <String, Set <String>> fds,
        Set <Set <String>> allPossibleSortingAtts) {
    return null; 
  }

}
