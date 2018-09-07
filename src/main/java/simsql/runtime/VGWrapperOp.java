

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

/**
 * The VGWrapper operator.
 *
 * @author Luis.
 */
public class VGWrapperOp extends RelOp {

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
        return "VGWrapper";
    }

    // returns the set of inputs
    public String[] getInputs() {

        ArrayList<String> inputSet = new ArrayList<String>();
        inputSet.addAll(getValue("outerInput.inFiles").getStringList());

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

        Map<String, Set<Set<String>>> ret = new HashMap<String, Set<Set<String>>>();

        // add the ones for the outer input.
        ret.putAll(getBaseSortAtts(myDBase, "outerInput.inFiles", "outerInput.inAtts"));

        // now, do the same with all the inners.
        for (String innerName : getInnerInputs()) {
            ret.putAll(getBaseSortAtts(myDBase, "innerInputs." + innerName + ".inFiles", "innerInputs." + innerName + ".inAtts"));
        }

        return ret;
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

        // outer selections
        ParsedRHS outerSelRHS = getValue("outerInput.selection");
        if (outerSelRHS != null) {
            one.addAll(outerSelRHS.getExpression().getAllFunctions());
        }

        // outer output
        for (Assignment a : getValue("outerInput.outAtts").getAssignmentList()) {
            one.addAll(a.getExpression().getAllFunctions());
        }

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

    // returns the set of vg functions
    public String[] getVGFunctions() {
        return new String[]{getValue("function.functionName").getStringLiteral()};
    }

    // number of reducers -- zero if we got the sort orders correctly.
    public int getNumReducers(RuntimeParameter params) {

        // otherwise, use reducers.
        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getNumCPUs();
    }

    // sets up additional configuration parameters.
    public void setConfigurations(Configuration conf, RuntimeParameter params) {

        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;

        /**
         conf.setBoolean("mapred.task.profile", true);
         conf.set("mapred.task.profile.params", "-agentlib:hprof=cpu=samples," +
         "heap=sites,depth=10,force=n,thread=y,verbose=n,file=%s");
         **/
        // set the number of iterations
        conf.setInt("simsql.numIterations", p.getNumIterations());

        // set the file name of the VG function.
        conf.setStrings("simsql.functionFile", new String[]{"/simsql/functions/" + getVGFunctions()[0] + ".so"});

        // set the buffer size for data exchange -- 2GB is the maximum because it is a long.
        int bSize = 0;
        if (((p.getMemoryPerCPUInMB() / 2) * 1024L * 1024L) > (long) Integer.MAX_VALUE) {
            bSize = Integer.MAX_VALUE;
        } else {
            bSize = (p.getMemoryPerCPUInMB() / 2) * 1024 * 1024;
        }

        conf.setInt("simsql.dataBufferSize", bSize);

        // set the cross product relations.
        if (crossFiles.size() > 0) {
            conf.setStrings("simsql.crossFiles", crossFiles.toArray(new String[0]));
            conf.setStrings("simsql.crossTypeCodes", crossTypeCodes.toArray(new String[0]));
            conf.setStrings("simsql.crossAttCounts", crossAttCounts.toArray(new String[0]));
        }
        // set the sorted input relations.
        if (sortedInnerFiles.size() > 0) {
            conf.setStrings("simsql.sortedFiles", sortedInnerFiles.toArray(new String[0]));
            conf.setStrings("simsql.sortedTypeCodes", sortedTypeCodes.toArray(new String[0]));
            conf.setStrings("simsql.sortedAttCounts", sortedAttCounts.toArray(new String[0]));
        }

        conf.setBoolean("simsql.runVGWrapperReducer", runVGWrapperReducer);
    }

    // returns the set of macro replacements
    public Map<String, String> buildMacroReplacements() {

        HashMap<String, String> replacements = new HashMap<String, String>();

        // get the replacements for the 'merged input' (a.k.a. vgrecord)
        Map<String, String> mergedAtts = buildAttributeReplacements("inAtts", "");
        replacements.put("numAttsVG", "" + mergedAtts.size());
        replacements.put("vgSeedAtt", mergedAtts.get(getValue("seedAtt").getIdentifier()));

        // and the order of the vg call attributes
        String vgInputAtts = "";
        boolean first = true;
        for (String vgInAtt : getValue("function.vgInAtts").getIdentifierList()) {

            if (!first) {
                vgInputAtts += ", ";
            }

            vgInputAtts += mergedAtts.get(vgInAtt);
            first = false;
        }
        replacements.put("vgInputAtts", vgInputAtts);


        // now, let's do the outer input record.
        Map<String, String> outerAtts = buildAttributeReplacements("outerInput.inAtts", "");
        replacements.put("numAttsOuter", "" + outerAtts.size());
        replacements.put("outerTypeCode", "" + getValue("outerInput.typeCode").getInteger());
        replacements.put("outerInputSelection", buildSelectionReplacement("outerInput.selection", outerAtts));
        replacements.put("outerInputAssignments", buildMappedAssignmentReplacements("outerInput.outAtts", "outRec.", "", mergedAtts, outerAtts));

        // get the primary hash for the outer input record.
        String hh = "";

        if (outerAtts.keySet().containsAll(sortedOutputAtts) && !sortedOutputAtts.isEmpty()) {
            for (String sa : sortedOutputAtts) {
                hh += " ^ " + outerAtts.get(sa) + ".getHashCode()";
            }
        } else {
            hh = " ^ getSortAttribute()";
        }

        replacements.put("outerPrimaryHash", hh);

        // and then, the final output record.
        replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());

        // the attributes available to this guy are: all of inAtts + the output of the VGF.
        HashMap<String, String> finalAtts = new HashMap<String, String>();
        finalAtts.putAll(buildAttributeReplacements("function.vgOutAtts", "input_"));
        finalAtts.putAll(buildAttributeReplacements("inAtts", "seedRec."));

        replacements.put("outputAssignments", buildAssignmentReplacements("output.outAtts", "", finalAtts));
        replacements.put("numAttsOutput", "" + getValue("output.outAtts").getAssignmentList().size());
        replacements.put("outputSelection", buildSelectionReplacement("output.selection", finalAtts));
        replacements.put("functionDeclarations", "" + buildFunctionDeclarations());

        // then, finally, the names of all the VG inner input classes.
        ParsedRHS innerInputs = getValue("innerInputs");
        if (innerInputs != null) {

            String innerNames = "";
            for (String s : innerInputs.getVarList().keySet()) {
                innerNames += ", VGInnerInput_" + s + ".class";
            }

            replacements.put("innerInputClassNames", innerNames);
        }

        return replacements;
    }

    // returns the set of macro replacements for a given inner query
    public Map<String, String> buildInnerMacroReplacements(String innerInputName, int innerInputID) {

        HashMap<String, String> replacements = new HashMap<String, String>();

        Map<String, ParsedRHS> input = getValue("innerInputs." + innerInputName).getVarList();

        if (!input.containsKey("inFiles") || !input.containsKey("inAtts") || !input.containsKey("typeCode") || !input.containsKey("outAtts"))
            throw new RuntimeException("Incomplete inner input description!");

        // now, fill the replacements
        replacements.put("innerInputName", innerInputName);
        replacements.put("innerInputTypeCode", "" + input.get("typeCode").getInteger());
        Map<String, String> mergedAtts = buildAttributeReplacements("inAtts", "");
        Map<String, String> innerAtts = buildAttributeReplacements("innerInputs." + innerInputName + ".inAtts", "");
        replacements.put("innerInputSelection", buildSelectionReplacement("innerInputs." + innerInputName + ".selection", innerAtts));
        replacements.put("innerInputAssignments", buildMappedAssignmentReplacements("innerInputs." + innerInputName + ".outAtts", "outRec.", "", mergedAtts, innerAtts));
        replacements.put("numInnerInputAtts", "" + innerAtts.size());
        replacements.put("functionDeclarations", "" + buildFunctionDeclarations());
        replacements.put("innerInputID", "" + innerInputID);

        // get the primary hash for the inner input record.
        String hh = "";

        if (innerAtts.keySet().containsAll(sortedOutputAtts) && !sortedOutputAtts.isEmpty()) {
            for (String sa : sortedOutputAtts) {
                hh += " ^ " + innerAtts.get(sa) + ".getHashCode()";
            }
        } else {
            hh = " ^ getSortAttribute()";
        }

        replacements.put("innerPrimaryHash", hh);


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
        return "VGWrapperRecords.javat";
    }

    public String getInnerTemplateFile() {
        return "VGWrapperInnerRecord.javat";
    }

    // returns the mapper class.
    public Class<? extends Mapper> getMapperClass() {
        return VGWrapperMapper.class;
    }

    // returns the reducer class.
    public Class<? extends Reducer> getReducerClass() {
        return VGWrapperReducer.class;
    }

    public Class getOutputValueClass() {
        return VGOutputRecord.class;
    }


    public VGWrapperOp(Map<String, ParsedRHS> inValues) {
        super(inValues);

        // get the seed/merge attribute name
        seedAtt = getValue("seedAtt").getIdentifier();

        // the default sorting is on the seed.
        sortedOutputAtts.add(seedAtt);
        allSortedOutputAtts.add(sortedOutputAtts);
    }

    // seed attribute name
    String seedAtt;

    // set of cross product relations.
    private ArrayList<String> crossFiles = new ArrayList<String>();
    private ArrayList<String> crossTypeCodes = new ArrayList<String>();
    private ArrayList<String> crossAttCounts = new ArrayList<String>();

    // set of inner relations.
    private ArrayList<String> sortedInnerFiles = new ArrayList<String>();
    private ArrayList<String> sortedTypeCodes = new ArrayList<String>();
    private ArrayList<String> sortedAttCounts = new ArrayList<String>();

    // set of sorted output attributes. by default, our seed!
    private Set<String> sortedOutputAtts = new HashSet<String>();
    private Set<Set<String>> allSortedOutputAtts = new HashSet<Set<String>>();

    // false means "run a map-side VGW."
    private boolean runVGWrapperReducer = true;

    public Set<Set<String>> getAllSortAtts(String fName, Map<String, Set<Set<String>>> sortingAttsIn) {
        Set<Set<String>> ret = new HashSet<Set<String>>();

        String feeder = getNetworkOnInputSide().getFeeder(fName);
        if (sortingAttsIn.containsKey(feeder)) {
            ret.addAll(sortingAttsIn.get(feeder));
        }

        ret.addAll(getNetworkOnInputSide().findAnyAdditionalSortAttributes(feeder, ret));

        return ret;
    }

    // selects which inner inputs will be read directly from the pipeline.
    public void determineWhatAlgorithmToRun(Set<Set<String>> votes, Map<String, Set<String>> fds,
                                            Map<String, Set<Set<String>>> sortingAttsIn) {


        System.out.println("-------------------------------------------");
        System.out.println("VGWRAPPER DECIDING ON WHAT ALGORITHM TO USE");

        // first, we'll get all the sorting attributes from the outer
        // input and walk through the pipeline.
        Set<Set<String>> outerSortAtts = getAllSortAtts(getOuterFile(), sortingAttsIn);
        System.out.println("OUTER INPUT SORTED ON: " + outerSortAtts);

        // then, we will examine the inner inputs and exclude those that are cross products.
        Map<String, Set<Set<String>>> mergedInputs = new HashMap<String, Set<Set<String>>>();
        System.out.println("INNER INPUTS: ");

        for (String s : getInnerInputs()) {
            String innerF = getInnerFile(s);
            System.out.print("    " + s + " [" + innerF + "]  ");
            String feederF = getNetworkOnInputSide().getFeeder(innerF);

            if (isCrossProduct(s)) {
                System.out.println("cross product, excluded.");
                if (!crossFiles.contains(feederF)) {
                    crossFiles.add(getNetworkOnInputSide().getFeeder(innerF));
                    crossTypeCodes.add(getInnerTypeCode(s));
                    crossAttCounts.add(getInnerAttCount(s));
                }
            } else {

                if (feederF.equals(getOuterFile())) {
                    System.out.println("stems from outer, excluded");
                } else {
                    Set<Set<String>> innerSortAtts = sortedFDs(seedAtt, fds, getAllSortAtts(innerF, sortingAttsIn));
                    mergedInputs.put(s, innerSortAtts);
                    System.out.println("merged on " + innerSortAtts + " " + getAllSortAtts(innerF, sortingAttsIn));
                }
            }
        }

        // then, we will group the merged inputs into equivalence classes
        // based on their compatible sorting atts.
        // map from [sort orders] -> [set of inputs]
        Map<Set<Set<String>>, Set<String>> eqClasses = new HashMap<Set<Set<String>>, Set<String>>();

        for (String s : mergedInputs.keySet()) {

            // first, add them all to the map.
            Set<Set<String>> innerOrders = mergedInputs.get(s);
            if (eqClasses.containsKey(innerOrders)) {

                // update sets.
                eqClasses.get(innerOrders).add(s);
            } else {

                // add singleton
                Set<String> ss = new HashSet<String>();
                ss.add(s);
                eqClasses.put(innerOrders, ss);
            }
        }

        // now, continuously update the map
        boolean changedMap = true;
        while (changedMap) {
            changedMap = false;

            // maybe we'll have to break.
            if (eqClasses.size() < 2) {
                break;
            }

            // try a pair of sort orders
            for (Set<Set<String>> sx : eqClasses.keySet()) {

                for (Set<Set<String>> sy : eqClasses.keySet()) {

                    // don't do equals.
                    if (sx == sy) {
                        continue;
                    }

                    // check if the sorts are compatible.
                    if (equalSorts(sx, sy) != null) {

                        // excellent! merge them.
                        Set<Set<String>> newKey = new HashSet<Set<String>>();
                        newKey.addAll(sx);
                        newKey.addAll(sy);

                        Set<String> newValue = new HashSet<String>();
                        newValue.addAll(eqClasses.get(sx));
                        newValue.addAll(eqClasses.get(sy));

                        // change the map
                        eqClasses.remove(sx);
                        eqClasses.remove(sy);
                        eqClasses.put(newKey, newValue);
                        changedMap = true;

                        // break out
                        break;
                    }
                }

                // did we find a match? then let's loop back.
                if (changedMap) {
                    break;
                }
            }
        }

        System.out.println("Merged input equivalence classes: " + eqClasses);

        // at this point, we can check for the possibility of a pure.
        // map-side vgwrapper.
        //
        // first, check if there are no merged inputs.
        if (mergedInputs.isEmpty()) {
            System.out.println("No merged inner inputs to exclude! Map-side VGWrapper.");

            // set the sorting.
            if (!outerSortAtts.isEmpty()) {

                Set<String> someSort = usableSort(outerSortAtts, getValue("outerInput.inAtts").getIdentifierList());
                if (someSort != null) {
                    sortedOutputAtts = someSort;
                    allSortedOutputAtts = outerSortAtts;
                }
            }

            runVGWrapperReducer = true;
            return;
        }

        // then, check if there is a sort order that covers every merged
        // inner input, and that the outer input is sorted like that too.
        // look at the map
        for (Set<Set<String>> ss : eqClasses.keySet()) {

            // check the two conditions.
            if (eqClasses.get(ss).containsAll(mergedInputs.keySet()) && equalSorts(ss, outerSortAtts) != null) {

                // bingo! generate our sorting attributes.
                allSortedOutputAtts.clear();
                allSortedOutputAtts.addAll(outerSortAtts);
                allSortedOutputAtts.addAll(ss);
                sortedOutputAtts = usableSort(allSortedOutputAtts, getValue("outerInput.inAtts").getIdentifierList());

                System.out.println("Compatible sorts across all merged inner inputs! Map-side VGWrapper.");


                // see if we want to swap the outer input for something larger
                String largestInput = null;
                long largestInputSize = -1;
                for (String s : mergedInputs.keySet()) {
                    String feederF = getNetworkOnInputSide().getFeeder(getInnerFile(s));
                    long mySize = getPathsActualSize(new String[]{feederF});
                    if (mySize > largestInputSize) {
                        largestInput = s;
                        largestInputSize = mySize;
                    }
                }

                long outerInputSize = getPathsActualSize(new String[]{getOuterFile()});

                /*** DEBUG: I'm disabling this feature because it interferes with the GMM with imputation. */
                largestInputSize = -1;
                if (outerInputSize > 0 && largestInputSize > 0 && largestInputSize > outerInputSize) {

                    System.out.println("Excluding the outer input to make " + largestInput + " the main branch.");
                    sortedInnerFiles.add(getOuterFile());
                    sortedTypeCodes.add("" + getValue("outerInput.typeCode").getInteger());
                    sortedAttCounts.add("" + getValue("outerInput.inAtts").getIdentifierList().size());
                    largestInput = getNetworkOnInputSide().getFeeder(getInnerFile(largestInput));
                } else {
                    largestInput = "<nix>";
                }

                // exclude all the inputs.
                for (String s : mergedInputs.keySet()) {
                    String feederF = getNetworkOnInputSide().getFeeder(getInnerFile(s));
                    if (!sortedInnerFiles.contains(feederF) && !feederF.equals(largestInput)) {
                        sortedInnerFiles.add(feederF);
                        sortedTypeCodes.add(getInnerTypeCode(s));
                        sortedAttCounts.add(getInnerAttCount(s));
                    }
                }

                // we're done.
                runVGWrapperReducer = true;
                return;
            }
        }

        // if we get here, then we have to run a reducer.
        System.out.println("VGWrapper has to reduce. Trying to exclude as many inner inputs as possible.");

        // we'll look at the map and find the sort with the most inputs.
        Set<Set<String>> biggest = null;
        int biggestSize = 0;

        // note: this cannot be done if the empty equivalence class is
        // present.  that basically means there is at least one relation
        // that had no sorts with a functional dependency compatible with
        // the outer input, e.g. a little join.

        if (!eqClasses.containsKey(new HashSet<String>())) {
            for (Set<Set<String>> ss : eqClasses.keySet()) {

                // check the conditions
                Set<String> ssInners = eqClasses.get(ss);
                if (usableSort(ss, getValue("outerInput.inAtts").getIdentifierList()) != null &&
                        biggestSize < ssInners.size()) {

                    biggest = ss;
                    biggestSize = ssInners.size();
                }
            }
        }

        // did we find one? good! exclude, then.
        if (biggest != null) {

            System.out.println("VGWrapper sorting on " + biggest + " with excluded inputs.");
            for (String s : eqClasses.get(biggest)) {
                String feederF = getNetworkOnInputSide().getFeeder(getInnerFile(s));

                if (!sortedInnerFiles.contains(feederF)) {
                    sortedInnerFiles.add(feederF);
                    sortedTypeCodes.add(getInnerTypeCode(s));
                    sortedAttCounts.add(getInnerAttCount(s));
                }
            }

            allSortedOutputAtts.clear();
            allSortedOutputAtts.addAll(biggest);
            sortedOutputAtts = usableSort(allSortedOutputAtts, getValue("outerInput.inAtts").getIdentifierList());

            return;
        }

        // if we could not find such a sort, then we'll have to use the
        // seed to merge. this is the worst case and default behavior.
        System.out.println("VGWrapper could not find any optimized sort orders. Using default seed-based merging.");
    }

    // returns the name of the outer input.
    private String getOuterFile() {

        String[] ofArr = getValue("outerInput.inFiles").getStringList().toArray(new String[0]);
        if (ofArr.length > 1) {
            throw new RuntimeException("Not supporting more than one outer file!");
        }

        return getNetworkOnInputSide().getFeeder(ofArr[0]);
    }

    // true if a file is the outer.
    private boolean isFileOuter(String fName) {
        return getOuterFile().equals(fName);
    }

    // returns true if a given input file is pipelined
    private boolean isFilePipelined(String fName) {
        return !getNetworkOnInputSide().getFeeder(fName).equals(fName);
    }

    // gets the file of an inner input by name
    private String getInnerFile(String innerName) {
        ParsedRHS innerInputs = getValue("innerInputs");

        if (innerInputs != null) {
            String[] ofArr = getValue("innerInputs." + innerName + ".inFiles").getStringList().toArray(new String[0]);
            if (ofArr.length > 1) {
                throw new RuntimeException("Not supporting more than one inner file in the array!");
            }

            return ofArr[0];
        }

        return null;
    }

    // returns the type code of a given inner input.
    private String getInnerTypeCode(String innerName) {

        return "" + getDB().getTypeCode(getDB().getTableName(getNetworkOnInputSide().getFeeder(getInnerFile(innerName))));
    }

    // returns the number of atts of a given inner input.
    private String getInnerAttCount(String innerName) {

        return "" + getDB().getNumAtts(getDB().getTableName(getNetworkOnInputSide().getFeeder(getInnerFile(innerName))));
    }

    // gets the name of an inner input by file.
    private String getInnerName(String fName) {
        ParsedRHS innerInputs = getValue("innerInputs");

        if (innerInputs != null) {
            for (String innerName : getInnerInputs()) {
                if (getInnerFile(innerName).equals(fName)) {
                    return innerName;
                }
            }
        }

        return null;

    }

    // returns true if a given inner input is a cross product
    private boolean isCrossProduct(String innerName) {
        ParsedRHS innerInputs = getValue("innerInputs");

        if (innerInputs != null && innerName != null) {
            return !getValue("innerInputs." + innerName + ".inAtts").getIdentifierList().contains(seedAtt);
        }

        return false;
    }

    // returns the set of all inner input names.
    private Set<String> getInnerInputs() {
        ParsedRHS innerInputs = getValue("innerInputs");

        if (innerInputs != null) {
            HashSet<String> kx = new HashSet<String>();
            kx.addAll(innerInputs.getVarList().keySet());
            return kx;
        } else return new HashSet<String>();
    }

    // do we actually have inner inputs?
    private boolean haveInnerInputs() {
        return getValue("innerInputs") != null;
    }

    // returns the subset of sort orders that are functionally
    // determined by whichAtts
    private Set<Set<String>> sortedFDs(String whichAtt, Map<String, Set<String>> fds, Set<Set<String>> sortOrder) {

        Set<Set<String>> sortKeys = new HashSet<Set<String>>();

        if (fds.containsKey(whichAtt) && sortOrder != null) {

            Set<String> allFDs = fds.get(whichAtt);
            for (Set<String> ss : sortOrder) {

                if (allFDs.containsAll(ss) || (ss.size() == 1 && ss.contains(whichAtt))) {
                    sortKeys.add(ss);
                }
            }
        }

        return sortKeys;
    }

    // returns non-null if two sets of sorting atts are equivalent.
    private Set<String> equalSorts(Set<Set<String>> set1, Set<Set<String>> set2) {

        if (set1 == null || set2 == null) {
            return null;
        }

        for (Set<String> ss : set1) {
            if (set2.contains(ss)) {
                return ss;
            }
        }

        return null;
    }

    // returns a sort order that can be obtained from a set of attributes. otherwise, null.
    private Set<String> usableSort(Set<Set<String>> set1, Collection<String> set2) {

        for (Set<String> ss : set1) {
            if (set2.containsAll(ss)) {
                return ss;
            }
        }

        return null;
    }

    public Set<Set<String>> getSortedOutputAtts() {
        HashSet<Set<String>> ret = new HashSet<Set<String>>();
        ret.addAll(allSortedOutputAtts);
        return ret;
    }


    // this operator doesn't really have a "possible" set of sorting
    // atts to offer the next operator. we just give whatever we get.
    public Set<Set<String>> getAllPossibleSortingAtts() {
        return new HashSet<Set<String>>();
    }

    // our set of preferred attributes for each input is always dependent on whatever the
    // seed FDs can give us and on the type of input that we have.
    private Set<Set<String>> pastVotes = new LinkedHashSet<Set<String>>();

    public Set<String> getPreferredSortOrder(String fName, Map<String, Set<String>> fds,
                                             Set<Set<String>> allPossibleSortingAtts) {

        System.out.println("==> VOTING ON PREFERRED SORT ORDERS THROUGH THE VGWRAPPER.");

        // is this the outer input?
        if (isFileOuter(fName)) {

            System.out.println("VGW OUTER INPUT: VOTING null");
            return null;
        }

        // is this a cross product inner input?
        if (isCrossProduct(getInnerName(fName))) {
            System.out.println("VGW CROSS PRODUCT INPUT: VOTING null");
            return null;
        }

        // if we're here, it's a merged inner input.
        Set<Set<String>> usefulSortingAtts = sortedFDs(seedAtt, fds, allPossibleSortingAtts);

        // did we find something? if not, then we can't do much.
        if (usefulSortingAtts.isEmpty()) {
            System.out.println("VGW: INNER INPUT. NO USEFUL SORTS FOUND! VOTING null");
            return null;
        }

        // if we did, then check if there are previous votes.
        if (pastVotes.isEmpty()) {

            // if not, then we'll vote for the most likely...
            System.out.println("VGW: INNER INPUT. NO PREVIOUS VOTES, VOTING FOR MOST LIKELY... ");
            Set<String> mostLikely = findMostLikelyVote(usefulSortingAtts);
            System.out.println("...VOTED FOR: " + mostLikely);
            pastVotes.add(mostLikely);

            return mostLikely;
        }


        // if we did, check if we have a previous vote.
        for (Set<String> s : usefulSortingAtts) {
            if (pastVotes.contains(s)) {
                System.out.println("VGW: INNER INPUT. FOUND PREVIOUS VOTE ON " + s + ". RETURNING");
                return s;
            }
        }

        // if we are here, then it means that none of the sort orders have
        // been seen before and that there will have to be a reduce.
        System.out.println("VGW: INNER INPUT. PREVIOUS VOTES NOT USEFUL, VOTING FOR MOST LIKELY...");
        Set<String> mostLikely = findMostLikelyVote(usefulSortingAtts);
        System.out.println("...VOTED FOR: " + mostLikely);
        pastVotes.add(mostLikely);

        return mostLikely;
    }

    private Set<String> findMostLikelyVote(Set<Set<String>> possibleVotes) {

        // is the outer input already sorted?
        String outerInFile = getOuterFile();
        System.out.println("PHIL: " + outerInFile);
        if (outerInFile != null && getDB() != null && getDB().isTableSorted(getDB().getTableName(outerInFile))) {

            // get the sorting attribute and check.
            String outerSortAtt = getValue("outerInput.inAtts").getIdentifierList().get(getDB().getTableSortingAttribute(getDB().getTableName(outerInFile)));
            System.out.println("ATTE: " + outerSortAtt);
            for (Set<String> ss : possibleVotes) {

                if (ss.size() == 1 && ss.contains(outerSortAtt)) {
                    System.out.println("OUTER SORT IS COMPATIBLE!");
                    return ss;
                }
            }
        }

        // otherwise, get the first one.
        // is there only one possible vote? if so, return it.
        if (possibleVotes.size() == 1) {
            System.out.println("SINGLE OPTION!");
        }

        for (Set<String> ss : possibleVotes) {
            return ss;
        }

        return null;
    }

    // exclude the cross product and sorted input files.
    public String[] excludeAnyWhoWillNotBeMapped(String[] inFiles) {
        LinkedHashSet<String> files = new LinkedHashSet<String>();

        for (int i = 0; i < inFiles.length; i++) {
            if (crossFiles.contains(inFiles[i]) || sortedInnerFiles.contains(inFiles[i])) {
                continue;
            }

            files.add(inFiles[i]);
        }

        System.out.println("excluded cross product: " + crossFiles);
        System.out.println("excluded merged: " + sortedInnerFiles);
        System.out.println("actual input: " + files);
        return files.toArray(new String[0]);
    }
}
