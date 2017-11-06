
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

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import simsql.shell.PhysicalDatabase;
import simsql.shell.RuntimeParameter;

import java.io.File;
import java.util.*;

// The way that the union operation works is that the largest input relation is used to "fuel" the mappers.
// Each mapper is assigned one of the partitions from the large input relation, which it maps.  It also
// takes a partiion from each of the other input relations, and maps it as well.  In the case where the
// largest has a single partition, the one mapper maps everyone.  In the case where one or more of the other
// input relations has a single partition, the very first mapper maps those.

public class UnionOp extends RelOp {

    // returns a set of necessary mappings.
    public Set<String> getNecessaryValues() {
        return new HashSet<String>(Arrays.asList
                ("operation", "inAtts", "seedAtt", "outerInput", "function", "output",
                        "outerInput.inFiles", "outerInput.typeCode", "outerInput.outAtts",
                        "function.functionName", "function.vgInAtts", "function.vgOutAtts",
                        "output.outFile", "output.typeCode", "output.outAtts"));

    }

    // returns the name of the operation
    public String getOperatorName() {
        return "Union";
    }

    // returns the set of inputs
    public String[] getInputs() {

        ArrayList<String> inputSet = new ArrayList<String>();

        // add the inner inputs
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String innerName : innerInputs.getVarList().keySet()) {
                inputSet.addAll(getValue("innerInputs." + innerName + ".inFiles").getStringList());
            }
        }

        String[] foo = {""};
        return inputSet.toArray(foo);
    }


    public Map<String, Set<Set<String>>> getExistingSortAtts(PhysicalDatabase myDBase) {

        // there are no existing sort atts for the union
        return new HashMap<>();
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
        for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
            out.add(a.getIdentifier());
        }

        return out.toArray(new String[0]);
    }

    // returns the functions -- from the selections and output expressions
    public String[] getFunctions() {

        HashSet<String> one = new HashSet<String>();

        // go through all the inner inputs.
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String innerName : innerInputs.getVarList().keySet()) {

                ParsedRHS innerSelRHS = getValue("innerInputs." + innerName + ".selection");
                if (innerSelRHS != null) {
                    one.addAll(innerSelRHS.getExpression().getAllFunctions());
                }

                for (Assignment a : getValue("innerInputs." + innerName + ".outAtts").getAssignmentList()) {
                    one.addAll(a.getExpression().getAllFunctions());
                }
            }
        }

        // output selection
        ParsedRHS outputSelRHS = getValue("output.selection");
        if (outputSelRHS != null) {
            one.addAll(outputSelRHS.getExpression().getAllFunctions());
        }

        // output assignments
        for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
            one.addAll(a.getExpression().getAllFunctions());
        }

        return one.toArray(new String[0]);
    }

    // always run as a map-only job
    public int getNumReducers(RuntimeParameter params) {

        // check map-only job
        if (!runUnionReducer) {
            return 0;
        }

        // otherwise, use reducers
        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getNumCPUs();

    }

    // this looks up the size of a file
    private long getSizeFromName(String inputName) {
        String[] bar = new String[1];
        bar[0] = inputName;
        return getPathsActualSize(bar);
    }

    // this gets the size in bytes of the given input... fName should be something like "outerInput.inFiles"
    private long getSize(String fName) {
        ArrayList<String> inputSet = getValue(fName).getStringList();
        System.out.println("getting size for " + inputSet);
        String[] foo = {""};
        String[] bar = inputSet.toArray(foo);
        return getPathsActualSize(bar);
    }

    // the basic tactic here is to read an input record, and then do a runSelectionAndProjection on it.
    //
    //
    // returns the set of macro replacements
    public Map<String, String> buildMacroReplacements() {

        HashMap<String, String> replacements = new HashMap<String, String>();

        // add in the output type code
        replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());

        // and the number of output atts
        replacements.put("numOutputAtts", "" + (getValue("output.outAtts").getAssignmentList().size()));

        // now do the output selection
        if (getValue("output.selection") != null)
            throw new RuntimeException("Problem!  I found a selection in the union output");

        // and the funciotns
        replacements.put("functionDeclarations", "" + buildFunctionDeclarations());

        for (Set<String> s : allSortingAtts) {
            // and the hash positions for splitting
            replacements.put("hashPositions", buildAttPositionsArray(s, "output.outAtts"));

            // and the primary hash
            replacements.put("primaryHash", buildHashReplacements(s, "output.outAtts", ""));
        }

        // then, finally, the names of all the VG inner input classes.
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {

            String innerNames = "";
            for (String s : innerInputs.getVarList().keySet()) {
                innerNames += ", UnionInnerRecord_" + s + ".class";
            }

            replacements.put("innerInputClassNames", innerNames);
        }

        return replacements;
    }

    // returns the set of macro replacements for a given inner query
    public Map<String, String> buildInnerMacroReplacements(String innerInputName, int innerInputID) {

        HashMap<String, String> replacements = new HashMap<String, String>();

        Map<String, ParsedRHS> input = getValue("innerInputs." + innerInputName).getVarList();

        if (!input.containsKey("inFiles") || !input.containsKey("inAtts") ||
                !input.containsKey("typeCode") || !input.containsKey("outAtts"))
            throw new RuntimeException("Incomplete inner input description!");

        // first, get the list of inner input attributes
        Map<String, String> innerAtts = buildAttributeReplacements("innerInputs." + innerInputName + ".inAtts", "");

        // now we build a string that is like:
        //    Attribute temp1 = atts[1].add (atts[2]);
        //    Attribute temp2 = atts[2];
        //
        // at the same time, we build a map from the VG function output attribute name to temp1, temp2
        String assignmentString = "\n";
        Map<String, String> mapFromVGAttNameToTempVar = new HashMap<String, String>();
        int counter = 0;
        for (Assignment a : getValue("innerInputs." + innerInputName + ".outAtts").getAssignmentList()) {
            assignmentString += "\tAttribute temp" + counter + " = " + a.getExpression().print(innerAtts) + ";\n";

            // for the seed, we take the seed att
            if (counter == 0) {
                mapFromVGAttNameToTempVar.put(getValue("seedAtt").getIdentifier(), "temp" + counter);

                // otherwise, take the other atts
            } else {
                mapFromVGAttNameToTempVar.put(getValue("function.vgOutAtts").getIdentifierList().get(counter - 1), "temp" + counter);
            }
            counter++;
        }

        // now we build the final assignment string

        // the first one should always be the seed!
        assignmentString += "\n";
        counter = 0;
        for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
            assignmentString += "\toutRec.atts[" + counter + "] = " + a.getExpression().print(mapFromVGAttNameToTempVar) + ";\n";
            counter++;
        }

        // now, fill the replacements
        replacements.put("innerInputName", innerInputName);
        replacements.put("innerInputTypeCode", "" + getValue("innerInputs." + innerInputName + ".typeCode").getInteger());
        replacements.put("innerInputSelection", buildSelectionReplacement("innerInputs." + innerInputName +
                ".selection", mapFromVGAttNameToTempVar));
        replacements.put("innerInputAssignments", assignmentString);
        replacements.put("numInnerInputAtts", "" + innerAtts.size());
        replacements.put("functionDeclarations", "" + buildFunctionDeclarations());
        replacements.put("innerInputID", "" + innerInputID);

        return replacements;
    }

    // we override the jar building operation because we are dealing with multiple files.
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

        // get the macro replacements for the inner.
        ArrayList<String> javaFileNames = new ArrayList<String>();
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            int yx = 1; // id=0 for the outer input.
            for (String innerName : innerInputs.getVarList().keySet()) {
                String newFileName = "/simsql/runtime/Macro" + getOperatorName() + RelOp.COUNT_OP + innerName + ".java";
                MacroReplacer innerReplacer = new MacroReplacer(getInnerTemplateFile(), workDirectory + "/" + newFileName, buildInnerMacroReplacements(innerName, yx));
                javaFileNames.add(newFileName);
                yx++;
            }
        }

        javaFileNames.add(javaFileName);

        return buildJarFile(paramsIn, newJarFileName, workDirectory, javaFileName, javaFileNames.toArray(new String[0]));
    }

    // returns the name of the template Java source file.
    public String getTemplateFile() {
        return "UnionOutputRecord.javat";
    }

    public String getPipelinedInnerTemplateFile() {
        return "PipelinedUnionInnerRecord.javat";
    }

    public String getInnerTemplateFile() {
        return "UnionInnerRecord.javat";
    }

    // returns the mapper class.
    public Class<? extends Mapper> getMapperClass() {
        return UnionMapper.class;
    }

    // returns the reducer class.
    public Class<? extends Reducer> getReducerClass() {
        return UnionReducer.class;
    }

    public Class getOutputValueClass() {
        return UnionOutputRecord.class;
    }

    // this corresponds to the file that we are going to actual map... it is the biggest input file
    String bigFile = null;

    public String[] excludeAnyWhoWillNotBeMapped(String[] inFiles) {

        // if bigFile is null, it means that the configs have not yet been set
        if (bigFile == null)
            throw new RuntimeException("bigFile was null... perhaps setConfigurations was not called?");

        // verify that bigFile is in there
        boolean foundIt = false;
        for (String s : inFiles)
            if (s.equals(bigFile))
                foundIt = true;

        if (!foundIt)
            throw new RuntimeException("this is strange... I could not find the file that *should* be mapped");

        // the only one we do MapReduce over is the big one
        inFiles = new String[1];
        inFiles[0] = bigFile;
        return inFiles;
    }

    private short pipelinedTypeCode = -1;
    private int pipelinedNumAtts = -1;

    public String getPipelinedFile() {

        // the biggest file
        String bigFile = null;
        long bigSize = 0;

        // now check all of the inner inputs
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String s : innerInputs.getVarList().keySet()) {

                // see if this next guy is the biggest
                ArrayList<String> fileSet = getValue("innerInputs." + s + ".inFiles").getStringList();
                String inFile = getNetworkOnInputSide().getFeeder(fileSet.get(0));
                if (getSizeFromName(inFile) > bigSize) {
                    bigSize = getSizeFromName(inFile);
                    bigFile = inFile;
                    pipelinedTypeCode = getValue("innerInputs." + s + ".typeCode").getInteger().shortValue();
                    pipelinedNumAtts = getValue("innerInputs." + s + ".inAtts").getIdentifierList().size();
                }
            }
        }

        System.out.println("bigFile was " + bigFile);
        return bigFile;

    }

    public int getPipelinedTypeCode() {
        return pipelinedTypeCode;
    }

    public int getPipelinedNumAtts() {
        return pipelinedNumAtts;
    }

    String allTypeCodes = null;

    public String getNonPipelinedTypeCodes() {
        if (allTypeCodes == null)
            getNonPipelinedFiles();

        return allTypeCodes;
    }

    public String getNonPipelinedFiles() {

        // the biggest file
        String bigFile = getPipelinedFile();

        // this is the String of all of the files to process
        String allFilesToProcess = null;

        // now check all of the inner inputs
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String s : innerInputs.getVarList().keySet()) {

                // now, add him to the string all of the input files
                ArrayList<String> fileSet = getValue("innerInputs." + s + ".inFiles").getStringList();
                String inFile = getNetworkOnInputSide().getFeeder(fileSet.get(0));

                boolean useThisOne = false;
                if (inFile == null)
                    useThisOne = true;
                else if (!inFile.equals(getPipelinedFile()))
                    useThisOne = true;

                if (useThisOne && allFilesToProcess == null) {
                    allFilesToProcess = fileSet.get(0);
                    allTypeCodes = getValue("innerInputs." + s + ".typeCode").getInteger() + "";
                } else if (useThisOne && allFilesToProcess != null) {
                    allFilesToProcess = allFilesToProcess + "," + fileSet.get(0);
                    allTypeCodes = allTypeCodes + "," + getValue("innerInputs." + s + ".typeCode").getInteger();
                }
            }
        }

        return allFilesToProcess;

    }

    // configure the job...
    public void setConfigurations(Configuration conf, RuntimeParameter params) {

        // we will be running a reducer if we want to produce a particular sort order
        conf.setBoolean("simsql.runUnionReducer", runUnionReducer);

        // OK, we need to figure out all of the files that will simply be read from disk, and not processed
        // via MapReduce.

        // this is the String of all of the files to process
        bigFile = getPipelinedFile();
        String allFilesToProcess = getNonPipelinedFiles();

        System.out.println("pipielined file is " + bigFile);
        System.out.println("non-pipelined file is " + allFilesToProcess);

        // now, take out the large one
        String[] allFiles = allFilesToProcess.split(",");
        for (String s : allFiles) {
            // and add information to the config file... we need the typecode and the number of atts
            // this will allow the mapper to read them in
            conf.setInt("simsql." + s + ".typeCode", getDB().getTypeCode(getDB().getTableName(s)));
            conf.setInt("simsql." + s + ".numAtts", getDB().getNumAtts(getDB().getTableName(s)));
        }

        // and add in the configuration... this is the file to directy read
        conf.set("simsql.filesToDirectlyRead", allFilesToProcess);

    }

    public UnionOp(Map<String, ParsedRHS> valuesIn) {
        super(valuesIn);

        // here we add the seed for each of the inners
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String innerName : innerInputs.getVarList().keySet()) {
                Expression myExp = new Expression("literal int");
                myExp.setValue("0");
                getValue("innerInputs." + innerName + ".outAtts").getAssignmentList().add(0, new Assignment("foo", myExp));
            }
        }

        // put the outerInput into the inner inputs
        getValue("innerInputs").getVarList().put("outerInput", getValue("outerInput"));

        // get the set of all output atts
        outputAtts = new HashSet<String>();
        for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
            outputAtts.add(a.getIdentifier());
        }

        // set up the sorting atts
        allSortingAtts = new HashSet<Set<String>>();
    }

    // do we run a reducer on this?
    private boolean runUnionReducer = false;

    // our set of sorting and output attributes.
    private Set<Set<String>> allSortingAtts;
    private Set<String> outputAtts;

    public boolean acceptsPipelineable() {

        // we only allow a pipeline of one of the inputs

        // this variable will count the number of items to be pipelines
        int numThatAreNotThere = 0;

        // now check all of the inner inputs
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {
            for (String s : innerInputs.getVarList().keySet()) {
                long returnedSize = getSize("innerInputs." + s + ".inFiles");
                System.out.println("Size was " + returnedSize);
                if (returnedSize == 0)
                    numThatAreNotThere++;
            }
        }

        System.out.println("Found that " + numThatAreNotThere + " inputs are not there");
        // if there is less than one that is materialized, we can accept a pipeline
        return numThatAreNotThere <= 1;
    }

    public boolean isPipelineable(int megabytesIn) {

        return false;
    }

    public boolean isAppendable(String[] inx) {

        // we are not appendable
        return false;
    }

    public PipeNetwork getAppendableNetwork() {
        throw new RuntimeException("We are not appendable!");
    }

    public PipeNetwork getPipelinedVersion() {
        return addPipelinedOperationToInputPipeNetwork(new PipelinedUnion(this));
    }

    // here, we determine if we are going to do a map-side-only union, which is equivalent to
    // deciding if we are going to provide a sort order on the output
    public void determineWhatAlgorithmToRun(Set<Set<String>> votes, Map<String, Set<String>> fds,
                                            Map<String, Set<Set<String>>> sortingAttsIn) {

        // if we have sort order requests, we'll try to honor them.
        if (votes.size() > 0) {

            // find the largest of the requested sort orders.
            Set<String> biggest = null;
            for (Set<String> s : votes) {
                if (biggest == null || s.size() > biggest.size()) {
                    biggest = s;
                }
            }

            System.out.println("Sorting selection output on " + biggest);
            runUnionReducer = true;
            allSortingAtts.clear();
            allSortingAtts.add(biggest);
            return;
        }

        // final case: just give out these input sorting atts
        System.out.println("No sorting");
    }

    // simply returns the sorting atts we have decided.
    public Set<Set<String>> getSortedOutputAtts() {
        return allSortingAtts;
    }

    // we can sort on anything we output
    public Set<Set<String>> getAllPossibleSortingAtts() {
        return SubsetMachine.getAllSubsets(outputAtts);
    }

}
