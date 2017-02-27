

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

import simsql.shell.RuntimeParameter;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;
import simsql.shell.PhysicalDatabase;

import static simsql.runtime.ReflectedFunction.isScalarFunction;
import static simsql.runtime.ReflectedFunction.isUDFunction;

/**
 * A general relational operator class.
 *
 * @author Luis.
 */
public abstract class RelOp {

    // every relational operation has two "pipe" networks... these are networks of additional,
    // pipelined operations that run on the input and on the output of the operation.
    private PipeNetwork myInputNetwork;
    private PipeNetwork myOutputNetwork;

    // used to allow access to the physical database
    private PhysicalDatabase myDB;

    // gets access to the input network
    protected PipeNetwork getNetworkOnInputSide() {
        return myInputNetwork;
    }

    // gets access to the output network
    protected PipeNetwork getNetworkOnOutputSide() {
        return myOutputNetwork;
    }

    // gets access to the physical database -- could be NULL
    protected PhysicalDatabase getDB() {
        return myDB;
    }

    // for replacements.
    protected void replaceNetworkOnInputSide(PipeNetwork newInputNetwork) {
        myInputNetwork = newInputNetwork;
    }

    // check to see if we can move part of this guy's operation over to be appended to run
    // on the output pipeline of another operation
    public boolean isAppendable(String[] inputFiles) {
        // the default is a no!
        return false;
    }

    // check to see if the current operation can accept another
    // operation on its output pipe.
    public boolean acceptsAppendable() {

        // the default is a yes
        return true;
    }


    // check to see if the current operation can accept another
    // operation on its input pipe.
    public boolean acceptsPipelineable() {

        // the default is a yes
        return true;
    }


    // this returns a pipe network that can be appended to an earlier operation in order to run part
    // of this operation at the same time that we run another operation.  For example, in the case of
    // an aggregate, the aggregate would create a pipe network that consisted of (a) it's own input
    // pipe network, and (b) a new APipeNode object at the end of the pipe network that could run its
    // mapper.  Note that after a call to getAppendableNetwork () has been made, the RelOp should never
    // again return "true" in response to a call to "isAppendable".
    public PipeNetwork getAppendableNetwork() {
        // default is to throw an exception
        throw new RuntimeException("This operation is not appendable!!");
    }

    // this appends the given pipe network to the output
    public void appendNetworkOnOutputSide(PipeNetwork appendMe) {
        appendMe.appendNetworkOnto(myOutputNetwork);
        myOutputNetwork = appendMe;
    }

    // returns the set of existing sorting attributes for our input
    // relations, as known from the database. this method is to be
    // invoked at the beginning of the scheduling, so as to "recover"
    // the sorting attributes of base relations and/or previous
    // iterations.
    public Map<String, Set<Set<String>>> getExistingSortAtts(PhysicalDatabase myDBase) {

        // default: do nothing.
        return new HashMap<String, Set<Set<String>>>();
    }

    // this is a helper for the above method -- just pass the keys to
    // get the list of existing sorting attributes.
    protected Map<String, Set<Set<String>>> getBaseSortAtts(PhysicalDatabase myDBase, String inFilesKey, String inAttsKey) {

        Map<String, Set<Set<String>>> ret = new HashMap<String, Set<Set<String>>>();

        // get the input file name.
        String inFile = getValue(inFilesKey).getStringList().get(0);

        // check if it is sorted.
        if (myDBase.isTableSorted(myDBase.getTableName(inFile))) {

            // if so, get the name from the list of input atts.
            int sortedAttPos = myDBase.getTableSortingAttribute(myDBase.getTableName(inFile));
            ArrayList<String> atts = getValue(inAttsKey).getIdentifierList();
            if (sortedAttPos < atts.size()) {
                Set<Set<String>> mx = new HashSet<Set<String>>();
                Set<String> kx = new HashSet<String>();
                kx.add(atts.get(sortedAttPos));
                mx.add(kx);

                ret.put(inFile, mx);
            }
        }

        return ret;
    }

    // this creates a simple pipe network taking exactly the files and type codes of this relational operation
    public final void setupPipeNetwork(PhysicalDatabase myDBase) {

        // first set up the input pipe network
        myDB = myDBase;
        String[] files = getInputs();
        Short[] typeCodes = new Short[files.length];
        for (int i = 0; i < files.length; i++) {
            typeCodes[i] = myDBase.getTypeCode(myDBase.getTableName(files[i]));
        }
        myInputNetwork = new PipeNetwork(files, typeCodes);

        // and now set up the output pipe network
        files = new String[1];
        files[0] = getOutput();
        typeCodes = new Short[1];
        typeCodes[0] = myDBase.getTypeCode(myDBase.getTableName(files[0]));
        myOutputNetwork = new PipeNetwork(files, typeCodes);
    }

    // this puts a new pipelined operation into the INPUT pipe network for the relational op, that will be run on
    // the relational op's mapper
    public final PipeNetwork addPipelinedOperationToInputPipeNetwork(PipeNode addMe) {
        myInputNetwork.addPipelinedOperation(addMe);
        return myInputNetwork;
    }

    // put add the specified pipe network into the INPUT pipe network for the relational op
    public final void suckUpNetworkOnInput(PipeNetwork addMe) {

        // myInputNetwork is the one after, who needs to have something pre-pended
        myInputNetwork.appendNetworkOnto(addMe);
    }

    // this gets the list of input files to the relational operation... taking into account all of
    // the pipelined operations that have been stuffed into the operation, and might require thier
    // own input files, or might be masking other input files because those files are now not materialized
    final public String[] getInputFiles() {
        return myInputNetwork.getInputFiles();
    }

    final public String[] getPipelinedInputFiles() {
        return myInputNetwork.getPipelinedInputFiles();
    }

    final public String[] getNonPipelinedInputFiles() {
        return myInputNetwork.getNonPipelinedInputFiles();
    }

    // returns true if this operation can be pipelined, given the specified amount of
    // memory... the default is no, since most operations cannot be pipelined
    public boolean isPipelineable(int numMegs) {
        return false;
    }

    // prunes out any input files that we do not actually want to be processed by the mapper of the
    // corresponding MapReduce job... this is used in the case that we know one inut file is already
    // sorted, so we can just merge it... the default is to not exclude anything... the default is to not exclude anything
    public String[] excludeAnyWhoWillNotBeMapped(String[] inFiles) {
        return inFiles;
    }

    // this returns a pipelined version of the relational operator... the default is to
    // just return a null, since it is not possible to pipeline most operations
    public PipeNetwork getPipelinedVersion() {
        return null;
    }

    // returns a set of necessary mappings.
    public abstract Set<String> getNecessaryValues();

    // returns the name of the operation
    public abstract String getOperatorName();

    // returns the set of inputs to the "bare" reltional operation, with no pipe network
    protected abstract String[] getInputs();

    // returns the output of this operation
    public abstract String getOutput();

    // this asks the relational operation to decide with algorithm it will use (for example, if
    // this is a join, it might decide just to do a merge of its two input files, assuming that
    // they are both sorted.  The inputs are as follows:
    //
    // votes: this gives all of the sets of sort attributes that another operation using this operation's
    //   output has indicated that it would like to see.  For example, say this guy produces the file "x".
    //   Two other ops use "x" as input.  One would like to see "x" sorted on att1, and another would like
    //   to see "x" sorted on att1 and att2.  In this case, "votes" would contain {{att1}, {att1, att2}}.
    //
    // fds: this is the list of all functional dependencies that hold among attributes.  It is a map from
    //   an attribute name to the set of all attributes that are functionally determined by that att
    //
    // sortingAtts: this is a map from a file name to the set of all atts that the file is sorted on.
    //
    public void determineWhatAlgorithmToRun(Set<Set<String>> votes, Map<String, Set<String>> fds,
                                            Map<String, Set<Set<String>>> sortingAtts) {
    }

    // this asks the relational operation for the set (or sets) of attributes that its output will be sorted
    // on (it just returns null if there is not a known sort order that this op will produce).  Note
    // that this method will always be called AFTER a call to determineWhatAlgorithmtoRun
    public Set<Set<String>> getSortedOutputAtts() {
        return null;
    }

    // returns the set of output attribute names
    public abstract String[] getOutputAttNames();

    // returns the output type code
    public abstract short getOutputTypeCode();

    // returns the set of macro replacements
    public abstract Map<String, String> buildMacroReplacements();

    // returns the name of the template Java source file.
    public abstract String getTemplateFile();

    // returns the mapper class.
    public abstract Class<? extends Mapper> getMapperClass();

    // returns the reducer class.
    public abstract Class<? extends Reducer> getReducerClass();

    // returns the set of functions
    // default -- override if necessary.
    public String[] getFunctions() {
        return new String[0];
    }

    // returns the set of vg functions
    // default -- override if necessary.
    public String[] getVGFunctions() {
        return new String[0];
    }

    // returns the number of reduce tasks
    // default -- override if necessary.
    public int getNumReducers(RuntimeParameter params) {

        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getNumCPUs();
    }

    // used to obtain the length of a set of paths
    // returns zero if not possible.
    public static long getPathsTotalSize(String[] paths) {
        try {

            // get a configuration and a fileSystem
            Configuration conf = new Configuration();
            FileSystem dfs = FileSystem.get(conf);

            long totalSize = 0;
            for (String s : paths) {
                Path path = new Path(s);
                if (dfs.exists(path)) {
                    totalSize += dfs.getContentSummary(path).getLength();
                }
            }

            // return
            return totalSize;
        } catch (Exception e) {
            return 0;
        }
    }

    // this is called in order to give the pipelined operations that are attached to this
    // op one last chance to fix up problems that can occur, and are associated with pipelining
    // tiny input files
    public void reEvaluateWhichFilesArePipelined() {
        myInputNetwork.reEvaluateWhichFilesArePipelined();
    }

    // this tries to use the physicalDatabase to obtain the *uncompressed* size of a relation.
    public long getPathsActualSize(String[] paths) {

        // use the HDFS one if we fail....
        if (myDB == null) {
            return getPathsTotalSize(paths) * 2;
        }

        long count = 0;
        for (int i = 0; i < paths.length; i++) {
            count += myDB.getTableSize(myDB.getTableName(paths[i]));
        }

        return count;
    }

    // returns the max split size, in bytes.
    // default -- override if necessary.
    public long getSplitSize(RuntimeParameter params) {

        // default value = fileSize / numProcessors
        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        Configuration conf = new Configuration();
        long dfsBlockSize = (long) conf.getInt("dfs.blocksize", 128 * 1024 * 1024);

        try {

            // get a configuration and a fileSystem
            FileSystem dfs = FileSystem.get(conf);

            long totalSize = 0;
            for (String s : myInputNetwork.getPipelinedInputFiles()) {
                Path path = new Path(s);
                if (dfs.exists(path)) {
                    totalSize += dfs.getContentSummary(path).getLength();
                }
            }

            // if it's too small, just use a block.
            if (totalSize < dfsBlockSize)
                return dfsBlockSize;

            // otherwise, divide
            return totalSize / p.getNumCPUs();

        } catch (Exception e) {

            // if we fail, just return the DFS block size!!!
            return (long) dfsBlockSize;
        }
    }

    // returns the amount of memory that we can afford to allocate to the pipeline
    // the default assumes that the RelOp uses 1/2 of the RAM for its own stuff
    public long getRemainingMemForPipelinePerMapper(RuntimeParameter params) {
        return (getMemPerMapper(params) / 2) - (myInputNetwork.getRAMToRun());
    }

    // returns the memory to allocate per map task, in MB.
    // default -- override if necessary.
    public int getMemPerMapper(RuntimeParameter params) {

        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getMemoryPerCPUInMB();
    }

    // returns the memory to allocate per reduce task, in MB.
    // default -- override if necessary.
    public int getMemPerReducer(RuntimeParameter params) {

        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getMemoryPerCPUInMB();
    }

    // sets up additional configuration parameters.
    // default -- override if necessary.
    public void setConfigurations(Configuration conf, RuntimeParameter params) {
        // do nothing.
    }

    // returns the serializations string.
    // default value -- override if necessary.
    public String getSerializations() {
        return "simsql.runtime.RecordSerialization,simsql.runtime.RecordKeySerialization,org.apache.hadoop.io.serializer.WritableSerialization";
    }

    // returns the map output key class.
    // default values -- override if necessary
    public Class getMapOutputKeyClass() {
        return RecordKey.class;
    }

    // returns the map output value class.
    // default value -- override if necessary.
    public Class getMapOutputValueClass() {
        return RecordWrapper.class;
    }

    // returns the output key class.
    // default value -- override if necessary.
    public Class getOutputKeyClass() {
        return Nothing.class;
    }

    // returns the output value class.
    // default value -- override if necessary.
    public Class getOutputValueClass() {
        return Record.class;
    }

    // returns the input format class.
    // default value -- override if necessary.
    public Class<? extends InputFormat> getInputFormatClass() {
        return RecordInputFormat.class;
    }

    // returns the output format class.
    // default value -- override if necessary.
    public Class<? extends OutputFormat> getOutputFormatClass() {
        return RecordOutputFormat.class;
    }

    // returns the grouping comparator class
    // default value -- override if necessary.
    public Class<? extends RawComparator> getGroupingComparatorClass() {
        return RecordKeyGroupingComparator.class;
    }

    // returns the partitioner class.
    // default value -- override if necessary.
    public Class<? extends Partitioner> getPartitionerClass() {
        return RecordPartitioner.class;
    }

    // returns the sort comparator class.
    // default value -- override if necessary.
    public Class<? extends RawComparator> getSortComparatorClass() {
        return RecordKeySortComparator.class;
    }

    // retrieves a given value
    public ParsedRHS getValue(String mappingName) {

        String curMapping = mappingName;
        Map<String, ParsedRHS> topRHS = values;

        // keep on moving down until we get the last one.
        while (curMapping.indexOf('.') >= 0) {

            String nextMapping = curMapping.substring(0, curMapping.indexOf('.'));
            topRHS = topRHS.get(nextMapping).getVarList();

            // check validity.
            if (topRHS == null)
                return null;

            curMapping = curMapping.substring(curMapping.indexOf('.') + 1);
        }

        // and then, get the final RHS.
        return topRHS.get(curMapping);
    }

    // returns the set of function declarations
    public String buildFunctionDeclarations() {

        String outStr = "";
        for (String s : getFunctions()) {

            if(isScalarFunction(s)) {
                outStr += "public static simsql.functions.scalar." + s + " func_" + s + " = new simsql.functions.scalar." + s + "();\n  ";
            }
            else if(isUDFunction(s)) {
                outStr += "public static simsql.functions.ud." + s + " func_" + s + " = new simsql.functions.ud." + s + "();\n  ";
            }
            else {
                throw new RuntimeException(s + " is not a scalar nor a ud function!");
            }
        }

        return outStr;
    }


    // generic macro builder for a list of attributes (inAtts,
    // groupByAtts, etc.). transform the attribute names sequentially
    // into atts[i] corresponding to an AbstractRecord type, and
    // returns the mappings.
    // call like this: buildAttributeReplacements("leftInput.inAtts", "this.");
    // returns something like: {("o_orderkey", "atts[0]"), ...}
    public Map<String, String> buildAttributeReplacements(String valueKey, String prefix) {

        ArrayList<String> atts = getValue(valueKey).getIdentifierList();
        Map<String, String> attReplacements = new HashMap<String, String>();
        int counter = 0;
        for (String s : atts) {
            attReplacements.put(s, prefix + "atts[" + counter + "]");
            counter++;
        }

        return attReplacements;
    }

    // generic macro builder for a selection predicate.
    // call it like this: buildSelectionReplacement("leftInput.selection", inAttReplacements);
    public String buildSelectionReplacement(String valueKey, Map<String, String> inAttReplacements) {

        // check if it's around
        ParsedRHS selRHS = getValue(valueKey);
        if (selRHS == null) {
            return "BitstringWithSingleValue.TRUE";
        }

        return selRHS.getExpression().print(inAttReplacements);
    }

    // generic macro builder for a list of assignments (outAtts). it
    // uses existing attribute replacement names to transform things like
    // "output.o_orderkey = o_orderkey" into "output.atts[0] = atts[1]".
    // call like this:
    // buildAssignmentReplacements("leftInput.outAtts", "returnVal.",
    // inputAttReplacements);
    //
    // returns a String with a bunch of Java assignment statements.
    public String buildAssignmentReplacements(String valueKey, String prefix, Map<String, String> inAttReplacements) {

        String outStr = "";
        int counter = 0;
        for (Assignment a : getValue(valueKey).getAssignmentList()) {
            outStr += prefix + "atts[" + counter + "] = " +
                    a.getExpression().print(inAttReplacements) + ";\n    ";
            counter++;
        }
        return outStr;
    }

    // same as above, but looks up the LHS attribute in a map instead of generating the counters.
    public String buildMappedAssignmentReplacements(String valueKey, String lhsPrefix, String rhsPrefix, Map<String, String> lhsMap, Map<String, String> rhsMap) {

        String outStr = "";
        for (Assignment a : getValue(valueKey).getAssignmentList()) {
            outStr += lhsPrefix + lhsMap.get(a.getIdentifier()) + " = " + rhsPrefix + a.getExpression().print(rhsMap) + ";\n    ";
        }

        return outStr;
    }


    // this returns the set of all possible sorting attribute combinations that this operation could produce
    public Set<Set<String>> getAllPossibleSortingAtts() {

        // this is the output
        Set<Set<String>> retVal = new HashSet<Set<String>>();

        // we look for everything that has a "hashAtts" tag, then produce all subsets of the hashAtts
        for (Map.Entry<String, ParsedRHS> i : values.entrySet()) {
            ParsedRHS temp = null;
            if (i.getKey().equals("hashAtts"))
                temp = i.getValue();
            else if (i.getValue().getVarList() != null) {
                temp = i.getValue().getVarList().get("hashAtts");
            }

            if (temp != null) {
                if (temp.getIdentifierList() == null)
                    continue;

                // we do, so get all possible subsets of the hash atts
                retVal.addAll(SubsetMachine.getAllSubsets(temp.getIdentifierList()));
            }
        }

        return retVal;
    }

    // this remembers any functional dependencies that are created by the relation
    public void addAnyNewFDs(Map<String, Set<String>> fds, Map<String, Set<Set<String>>> esa) {

        // the default implementation just looks for any seed () operations, since this creates FDs

        // we look for everything that has a "outAtts" tag
        for (String s : getNecessaryValues()) {

            if (!s.contains("outAtts"))
                continue;


            ParsedRHS temp = getValue(s);
            if (temp != null) {

                // see if we have an identifier list in here
                if (temp.getIdentifierList() != null)
                    continue;

                // we do, so look for a "seed" operation
                ArrayList<Assignment> myList = temp.getAssignmentList();
                String lhs = null;
                Set<String> rhs = new HashSet<String>();

                // loop through all of the assignments in the "outAtts" list
                for (Assignment a : myList) {

                    // see if we got an assignment from a "seed" function
                    Expression e = a.getExpression();
                    if (e.getType().equals("func") && e.getValue().contains("seed")) {

                        // we did, so remember whast we are assigning to
                        lhs = a.getIdentifier();
                    } else {

                        // we did not, so there is a functional dependency from the seed
                        rhs.add(a.getIdentifier());
                    }
                }

                // if we got a seed operation, then add this to the output
                if (lhs != null) {

                    // also, check if this comes with sorting attributes so that
                    // we can set the equivalence.
                    /***
                     for (String ff : esa.keySet()) {
                     boolean gotThem = false;
                     for (Set<String> ss : esa.get(ff)) {
                     if (rhs.containsAll(ss)) {
                     gotThem = true;
                     break;
                     }
                     }

                     if (gotThem) {
                     for (Set<String> ss : esa.get(ff)) {
                     rhs.addAll(ss);
                     }
                     }
                     }
                     ***/

                    // add output
                    fds.put(lhs, rhs);
                }
            }
        }
    }

    // this asks the relational operation whether it has a preferred sort order for the input atts
    public Set<String> getPreferredSortOrder(String fName, Map<String, Set<String>> fds,
                                             Set<Set<String>> allPossibleSortingAtts) {

        // the default implementation gets all possible sorting atts... it then tries to find
        // the largest set of sorting atts that matches up with a set in allPossibleSortingAtts
        Set<Set<String>> myPossibleSortingAtts = getAllPossibleSortingAtts();

        System.out.println("possible sorting atts: " + myPossibleSortingAtts);

        Set<String> best = null;
        for (Set<String> s : myPossibleSortingAtts) {
            if (allPossibleSortingAtts.contains(s)) {
                if (best == null || s.size() > best.size()) {
                    best = s;
                }
            }
        }

        return best;
    }

    // generic macro builder for a list of hash attributes (hashAtts)
    // that is used to build a HashableRecord.
    // call it like this:
    // buildHashReplacements("leftInput.hashAtts", "leftInput.outAtts", "this.");
    // returns a string with the hash loop.
    public String buildHashReplacements(String valueKey, String outAttsValueKey, String prefix) {

        // check
        if (getValue(valueKey) == null)
            return "";

        String outStr = "";

        // for each of the hash atts, we have to find the position in the atts array.
        for (String h : getValue(valueKey).getIdentifierList()) {
            int counter = 0;
            for (Assignment a : getValue(outAttsValueKey).getAssignmentList()) {
                if (h.equals(a.getIdentifier())) {
                    outStr += " ^ " + prefix + "atts[" + counter + "].getHashCode ()";
                }
                counter++;
            }
        }

        return outStr;
    }

    public String buildHashReplacements(Set<String> values, String outAttsValueKey, String prefix) {

        // check
        if (values.size() == 0)
            return "";

        String outStr = "";

        // for each of the hash atts, we have to find the position in the atts array.
        for (String h : values) {
            int counter = 0;
            for (Assignment a : getValue(outAttsValueKey).getAssignmentList()) {
                if (h.equals(a.getIdentifier())) {
                    outStr += " ^ " + prefix + "atts[" + counter + "].getHashCode ()";
                }
                counter++;
            }
        }
        System.out.println(outStr);
        return outStr;
    }

    // similar to the previous one, but returns the positions of the
    // respective attributes as an array.
    public String buildAttPositionsArray(String valueKey, String attKey) {

        // check for nulls
        if (getValue(valueKey) == null)
            return "";

        String outStr = "";
        boolean first = true;
        for (String h : getValue(valueKey).getIdentifierList()) {

            int ct = 0;
            for (Assignment a : getValue(attKey).getAssignmentList()) {
                if (h.equals(a.getIdentifier())) {

                    if (!first) {
                        outStr += ", " + ct;
                    } else {
                        outStr += ct;
                        first = false;
                    }
                }
            }

            ct++;
        }

        return outStr;
    }

    public String buildAttPositionsArray(Set<String> values, String attKey) {

        String outStr = "";
        boolean first = true;
        for (String h : values) {

            int ct = 0;
            for (Assignment a : getValue(attKey).getAssignmentList()) {
                if (h.equals(a.getIdentifier())) {

                    if (!first) {
                        outStr += ", " + ct;
                    } else {
                        outStr += ct;
                        first = false;
                    }
                }
                ct++;
            }

        }

        return outStr;
    }

    // generic macro builder for a list of attributes based on their
    // LHS assignment identifier (e.g. outAtts).
    // call it like this:
    // buildAttributeAssignReplacements("leftInput.outAtts", "left.", "leftRec.");
    // returns something like: {("left.o_orderkey", "leftRec.atts[0]"), ...}.
    public Map<String, String> buildAttributeAssignReplacements(String valueKey, String lhsPrefix, String rhsPrefix) {

        Map<String, String> retMap = new HashMap<String, String>();
        int counter = 0;
        for (Assignment a : getValue(valueKey).getAssignmentList()) {
            retMap.put(lhsPrefix + a.getIdentifier(), rhsPrefix + "atts[" + counter + "]");
            counter++;
        }

        return retMap;
    }


    // prints information about this operator.
    public String print() {
        String returnVal = getOperatorName();
        for (Map.Entry<String, ParsedRHS> i : values.entrySet()) {
            returnVal += "<" + i.getKey();
            returnVal += ": " + i.getValue().print() + ">\n";
        }
        return returnVal;
    }

    // used to unlink an entire subdirectory structure
    protected void rmdir(File f) {

        // does it exist?
        if (!f.exists())
            return;

        // is it a directory?
        if (f.isDirectory()) {

            // then, recursively delete its contents
            for (File ff : f.listFiles()) {
                rmdir(ff);
            }
        }

        // destroy it
        if (!f.delete()) {
            throw new RuntimeException("Could not prepare/delete the work directories for macro replacement");
        }
    }


    // builds the jar file for this operation.
    public String buildJarFile(RuntimeParameter paramsIn) {

        // cast the parameters.
        ExampleRuntimeParameter params = (ExampleRuntimeParameter) paramsIn;

        // get a name for the jar file, java template names and work directory
        String newJarFileName = getOperatorName() + "_" + RelOp.COUNT_OP + ".jar";
        String javaFileName = "/simsql/runtime/Macro" + getOperatorName() + RelOp.COUNT_OP + ".java";
        String workDirectory = "work_" + getOperatorName() + "_" + RelOp.COUNT_OP;
        RelOp.COUNT_OP++;

        // destroy the temp directory, if it's there
        rmdir(new File(workDirectory));

        // make the directories with their runtime/functions substructure
        if (!(new File(workDirectory + "/simsql/runtime").mkdirs()) ||
            !(new File(workDirectory + "/simsql/functions").mkdirs()) ||
            !(new File(workDirectory + "/simsql/functions/scalar").mkdirs()) ||
            !(new File(workDirectory + "/simsql/functions/ud").mkdirs())) {
            throw new RuntimeException("Could not prepare/create the work directories for macro replacement");
        }

        return buildJarFile(paramsIn, newJarFileName, workDirectory, javaFileName, new String[]{javaFileName});
    }

    // helper for building jars
    final protected String buildJarFile(RuntimeParameter paramsIn, String newJarFileName,
                                        String workDirectory, String javaFileName, String[] filesToCompile) {

        // first, we combine the input and output networks into the same network
        PipeNetwork myNetwork = new PipeNetwork(myInputNetwork, myOutputNetwork);

        // cast the parameters.
        ExampleRuntimeParameter params = (ExampleRuntimeParameter) paramsIn;

        // first, write out all of the Java source codes associated with the network of pipelined ops that will run in the mapper
        ArrayList<String> fNames = myNetwork.writeJavaCodes(workDirectory);

        // get the macro replacements and write out the java files
        Map<String, String> macroReplacements = buildMacroReplacements();

        // this will allow us to deserialize any record types that will be used as input to a pipelined op
        String recTypesString = myNetwork.getPossibleRecordTypesString();
        if (!recTypesString.trim().equals(""))
            recTypesString = recTypesString + ", ";

        macroReplacements.put("possRecordTypesString", recTypesString);

        // now we write out the jave source file for acual mapper
        MacroReplacer myReplacer = new MacroReplacer(getTemplateFile(), workDirectory + "/" + javaFileName, macroReplacements);

        // write the functions
        params.writeAllFunctionsToDirectory(workDirectory + "/simsql/functions");

        // write the VG functions
        for (String vf : getVGFunctions()) {
            params.writeVGFunctionToFile(vf, new File(workDirectory + "/simsql/functions", vf + ".so"));
        }

        // now we write out the PipeNetwork object
        try {
            FileOutputStream fileOut = new FileOutputStream(workDirectory + "/simsql/runtime/PipeNetwork.obj");
            ObjectOutputStream out = new ObjectOutputStream(fileOut);
            out.writeObject(myNetwork);
            out.close();
            fileOut.close();
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Got an error when I was trying to serialize the network of pipelined operations");
        }

        // get the current jar/resource path
        String jarPath = null;
        try {
            jarPath = RelOp.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (Exception e) {
            throw new RuntimeException("Unable to figure out the jar file being used when creating the operators: " + e);
        }

        // and compile the resulting file.
        String[] files = new String[filesToCompile.length + fNames.size()];
        for (int i = 0; i < files.length; i++) {
            if (i < filesToCompile.length)
                files[i] = filesToCompile[i];
            else
                files[i] = fNames.get(i - filesToCompile.length);
        }
        Compiler temp = new Compiler();
        String[] compilationJars = {jarPath};
        temp.getNewJar(workDirectory, jarPath, newJarFileName, compilationJars, files);

        // destroy the work directory.
        //rmdir(new File(workDirectory));

        // return the file name.
        return newJarFileName;
    }

    // runs this operation.
    public boolean run(RuntimeParameter params, boolean verbose) {

        ExampleRuntimeParameter pp = (ExampleRuntimeParameter) params;

        // build the jar.
        String jarFile = buildJarFile(params);

        // Get the default configuration object
        Configuration conf = new Configuration();

        // set quite mode on/off
        conf.setQuietMode(!verbose);


        /***
         conf.setBoolean("mapred.task.profile", true);
         conf.set("mapred.task.profile.params", "-agentlib:hprof=cpu=samples," +
         "heap=sites,depth=8,force=n,thread=y,verbose=n,file=%s");
         ***/

        // tell it how to serialize and deserialize records and recordkeys
        conf.set("io.serializations", getSerializations());
        conf.setBoolean("mapred.compress.map.output", true);

        int ioSortMB = conf.getInt("io.sort.mb", 256);
        conf.set("mapred.map.child.java.opts", "-Xmx" + (getMemPerMapper(params) + ioSortMB) + "m -Xms" + (getMemPerMapper(params)) + "m -Duser.timezone='America/Chicago' -Djava.net.preferIPv4Stack=true -XX:CompileThreshold=10000 -XX:+DoEscapeAnalysis -XX:+UseNUMA -XX:-EliminateLocks -XX:+UseBiasedLocking -XX:+OptimizeStringConcat -XX:+UseFastAccessorMethods -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:+UseCompressedOops -XX:+AggressiveOpts -XX:-UseStringCache -XX:ErrorFile=/tmp/hs_err_pid%p.log");

        conf.set("mapred.reduce.child.java.opts", "-Xmx" + (getMemPerReducer(params) + ioSortMB) + "m -Xms" + (getMemPerMapper(params)) + "m -Duser.timezone='America/Chicago' -Djava.net.preferIPv4Stack=true -XX:CompileThreshold=10000 -XX:+DoEscapeAnalysis -XX:+UseNUMA -XX:-EliminateLocks -XX:+UseBiasedLocking -XX:+OptimizeStringConcat -XX:+UseFastAccessorMethods -XX:+UseConcMarkSweepGC -XX:+CMSIncrementalMode -XX:+CMSIncrementalPacing -XX:CMSIncrementalDutyCycleMin=0 -XX:+UseCompressedOops -XX:+AggressiveOpts -XX:-UseStringCache -XX:ErrorFile=/tmp/hs_err_pid%p.log");

        conf.setInt("simsql.input.numSplits", pp.getNumCPUs());
        conf.setInt("mapred.job.reuse.jvm.num.tasks", 1);
        // conf.setBoolean ("mapred.map.tasks.speculative.execution", false);
        // conf.setBoolean ("mapred.reduce.tasks.speculative.execution", false);

        // tell it to use the jar that we just created
        conf.set("mapred.jar", jarFile);

        // conf.set("tmpjars", "file:///usr/lib/hadoop-mapreduce/hadoop-mapreduce-client-core.jar");

        conf.setBoolean("mapred.output.compress", true);
        conf.setStrings("mapred.output.compression.type", new String[]{"RECORD"});

        // use snappy for the intermediate stuff
        conf.set("mapred.map.output.compression.codec", RecordCompression.getCodecClass());

        // do some additional operator-specific configurations
        setConfigurations(conf, params);

        // collect statistics for final relations always
        conf.setBoolean("simsql.collectStats", isFinal || collectStats);

        // figure out what file to map
        String[] inDirs = myInputNetwork.getPipelinedInputFiles();
        inDirs = excludeAnyWhoWillNotBeMapped(inDirs);
        String inSingleString = inDirs[0];
        conf.set("simsql.fileToMap", inSingleString);
        for (int i = 1; i < inDirs.length; i++) {
            inSingleString += "," + inDirs[i];
        }

        // create and name the job
        Job job;
        try {
            job = new Job(conf);
        } catch (Exception e) {
            throw new RuntimeException("Unable to create a new job!", e);
        }

        job.setJobName(getJobName());

        // set the map-reduce input and output types
        job.setMapOutputKeyClass(getMapOutputKeyClass());
        job.setMapOutputValueClass(getMapOutputValueClass());
        job.setOutputKeyClass(getOutputKeyClass());
        job.setOutputValueClass(getOutputValueClass());

        int numReducers = getNumReducers(params);

        job.setMapperClass(getMapperClass());
        job.setReducerClass(getReducerClass());

        // set the number of reducers
        job.setNumReduceTasks(numReducers);

        // set the input and the output formats... these extend FileInputFormat and FileOutputFormat
        job.setInputFormatClass(getInputFormatClass());
        job.setOutputFormatClass(getOutputFormatClass());

        // set the input and output paths
        try {
            System.out.println("input file: " + inSingleString);
            FileInputFormat.setInputPaths(job, inSingleString);
            FileInputFormat.setInputPathFilter(job, TableFileFilter.class);
            FileOutputFormat.setOutputPath(job, new Path(getOutput()));
        } catch (Exception e) {
            throw new RuntimeException("Unable to set up the input/output path for the job.", e);
        }

        // set the split size
        FileInputFormat.setMinInputSplitSize(job, getSplitSize(params));
        FileInputFormat.setMaxInputSplitSize(job, getSplitSize(params));

        // set the various sorting/grouping/mapping classes
        job.setGroupingComparatorClass(getGroupingComparatorClass());
        job.setPartitionerClass(getPartitionerClass());
        job.setSortComparatorClass(getSortComparatorClass());

        // and now, submit the job and wait for things to finish
        int exitCode;
        try {
            exitCode = job.waitForCompletion(verbose) ? 0 : 1;

            // get the output bytes counter.
            Counters c = job.getCounters();
            Counter mx = c.findCounter(OutputFileSerializer.Counters.BYTES_WRITTEN);

            // and use them to set the size of the output relation.
            if (myDB != null) {
                myDB.setTableSize(myDB.getTableName(getOutput()), mx.getValue());
                myDB.setNumAtts(myDB.getTableName(getOutput()), getOutputAttNames().length);
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Unable to run the job", e);
        }

        // now, delete all the empty part files
        try {

            // get a filesystem
            FileSystem dfs = FileSystem.get(conf);
            Path outPath = new Path(getOutput());
            if (dfs.exists(outPath) && dfs.isDirectory(outPath)) {
                FileStatus fstatus[] = dfs.listStatus(outPath, new TableFileFilter());
                for (FileStatus ff : fstatus) {
                    if (dfs.getContentSummary(ff.getPath()).getLength() <= 4) { // snappy leaves 4-byte long files around...
                        dfs.delete(ff.getPath(), true);
                    }
                }
            }
        } catch (Exception e) { // this isn't disastrous
        }
        return (exitCode == 0);
    }

    // returns the name of the job in Hadoop
    public String getJobName() {

        String jobName = getOperatorName() + " {";
        boolean first = true;
        for (String i : myInputNetwork.getPipelinedInputFiles()) {

            if (first)
                jobName += "'" + i + "'";
            else
                jobName += ", '" + i + "'";

            first = false;
        }

        jobName += "} || [";

        first = true;
        for (String i : myInputNetwork.getNonPipelinedInputFiles()) {
            if (first)
                jobName += "'" + i + "'";
            else
                jobName += ", '" + i + "'";

            first = false;
        }

        jobName += "] => ";

        jobName += "'" + getOutput() + "'";
        return jobName;
    }

    // returns true if the relation is final
    protected boolean isFinal = false;

    public boolean isFinal() {
        return isFinal;
    }

    protected boolean collectStats = false;

    public void setCollectStats(boolean value) {

        // this cannot be set back to false -- we need it for pipelined
        // operations on the output.
        collectStats |= value;
    }

    // returns true if we are collecting statistics.
    public boolean collectStats() {
        return isFinal || collectStats;
    }

    // builds the operator from all the value mappings.
    public RelOp(Map<String, ParsedRHS> valuesIn) {

        // make sure that all the necessary values are represented here.
        values = valuesIn;
        for (String val : getNecessaryValues()) {
            if (getValue(val) == null)
                throw new RuntimeException("Value " + val + " not found in " + getOperatorName() + " description!");
        }

        // see if we have an "isFinal" here...
        ParsedRHS outputVal = getValue("output");
        if (outputVal != null) {
            ParsedRHS finalVal = getValue("output.isFinal");
            isFinal = (finalVal != null && finalVal.getStringLiteral().equals("true"));
        }
    }

    // counter for the jars
    protected static int COUNT_OP = 1;

    // the mappings.
    private Map<String, ParsedRHS> values;
}
