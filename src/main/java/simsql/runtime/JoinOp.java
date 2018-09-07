

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
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;

public class JoinOp extends RelOp {

    // types of join operators
    enum JoinType {
        NATURAL, SEMIJOIN, ANTIJOIN
    }

    ;

    private JoinType joinType;
    private boolean isSelfJoin;
    private boolean isCartesian;
    private short leftTypeCode;
    private short rightTypeCode;

    // set to true if we determine that the two input relations are already sorted in such a
    // way that we need only do a merge join on the two relations
    private boolean mergeJoin = false;

    // set to true if we can run the join by sorting only the left input file
    private boolean sortOnlyLeft = false;

    // set to true if we can run the join by sorting only the right input file
    private boolean sortOnlyRight = false;

    // these record the attributes that we are going to use to sort on the left and on the right
    Set<String> leftAtts;
    Set<String> rightAtts;

    // these store the left and right input file names... note that in the case of a pipeline, these might
    // not be our immediate inputs... they might be the files that feed the pipe on the left or the right
    String leftFile;
    String rightFile;

    private boolean materializedInputFileSpecified = false;
    private String materializedInputFile;

    // returns a set of necessary mappings.
    public Set<String> getNecessaryValues() {
        return new HashSet<String>(Arrays.asList
                ("operation", "leftInput", "leftInput.inFiles", "leftInput.inAtts", "leftInput.typeCode",
                        "leftInput.outAtts", "rightInput", "rightInput.inFiles", "rightInput.inAtts",
                        "rightInput.typeCode", "rightInput.outAtts", "output", "output.outFile",
                        "output.typeCode", "output.outAtts")
        );
    }

    // this allows us to decide to avoid mapping one or more of the input files
    public String[] excludeAnyWhoWillNotBeMapped(String[] inFiles) {

        // used to make sure we actually exclude one if we should
        boolean excludedOne = false;
        ArrayList<String> out = new ArrayList<String>();

        if (isSelfJoin) {
            for (String s : inFiles) {
                if (s.equals(leftFile))
                    out.add(s);
            }

            return out.toArray(new String[0]);
        }

        for (String s : inFiles) {

            // in the case that we are just going to merge this guy in, don't put him in the output
            if (((sortOnlyLeft) && s.equals(rightFile)) || ((sortOnlyRight || mergeJoin) && s.equals(leftFile))) {

                excludedOne = true;
                System.out.println("Excluded " + s);
                continue;
            }

            System.out.println("Kept " + s);
            out.add(s);
        }

        // sanity check
        if (!excludedOne && (sortOnlyLeft || mergeJoin || sortOnlyRight))
            throw new RuntimeException("I am doing a merge join, but I did not exclude any of the input files!");

        return out.toArray(new String[0]);
    }

    // ask the join for any additional sort atts that are induced, were we to run the join by pipelining
    // the file whichFileIsPipelined
    public Set<String> findPipelinedSortAtts(String fileName, Set<Set<String>> currentAtts) {

        if (isCartesian) {
            return null;
        }

        ArrayList<String> leftSet = getValue("leftInput.inFiles").getStringList();
        ArrayList<String> rightSet = getValue("rightInput.inFiles").getStringList();

        // try to match up the file name
        HashSet<String> myAttList, hisAttList;
        if (fileName.equals(leftSet.get(0))) {
            myAttList = new HashSet<String>(getValue("leftInput.hashAtts").getIdentifierList());
            hisAttList = new HashSet<String>(getValue("rightInput.hashAtts").getIdentifierList());
        } else if (fileName.equals(rightSet.get(0))) {
            hisAttList = new HashSet<String>(getValue("leftInput.hashAtts").getIdentifierList());
            myAttList = new HashSet<String>(getValue("rightInput.hashAtts").getIdentifierList());
        } else {
            throw new RuntimeException("Could not find the file that matches!!");
        }

        // see if we are sorted on the hash atts that are being pipelined through
        if (currentAtts.contains(myAttList)) {
            return new HashSet<String>(hisAttList);
        } else {
            return null;
        }
    }

    // this determines whether the atts we are already sorted on for either the left or right
    // input relation are useful, and if so, it returns the set of useful sort atts
    private Set<String> getSortAttsWeCanUse(String leftOrRight, Set<Set<String>> sortingAtts) {

        // if the input file is not actually sorted, then get outta here
        if (sortingAtts == null) {
            return new HashSet<String>();
        }

        // get the possible sets of acceptable sort attributes
        ArrayList<String> inputSet = getValue(leftOrRight + "Input.hashAtts").getIdentifierList();
        Set<Set<String>> allSets = SubsetMachine.getAllSubsets(inputSet);

        // see if one of the actual sets of sorting atts matches
        for (Set<String> s : sortingAtts) {

            if (allSets.contains(s)) {
                return s;
            }
        }

        // if we got here, then there were not any useful sorting atts
        return new HashSet<String>();
    }

    // this looks through the final selection pred associated with the join, and tries to find an
    // attribute participates in an equality check with the attribute referred to by "attName"
    public String getEqualityCheckWith(String attName) {

        ArrayList<String> leftList = getValue("leftInput.hashAtts").getIdentifierList();
        ArrayList<String> rightList = getValue("rightInput.hashAtts").getIdentifierList();
        if (leftList.size() != rightList.size()) {
            throw new RuntimeException("Bad: why don't the hash atts match up??");
        }
        attName = (attName.split("\\."))[1];
        for (int i = 0; i < leftList.size(); i++) {
            if (attName.equals(leftList.get(i)))
                return rightList.get(i);
            if (attName.equals(rightList.get(i)))
                return leftList.get(i);
        }
        throw new RuntimeException("Bad: could not find matching hash att!");
    }

    public Set<Set<String>> getSortedOutputAtts() {

        Set<Set<String>> returnVal = new HashSet<Set<String>>();
        if (leftAtts.size() != 0)
            returnVal.add(leftAtts);
        if (rightAtts.size() != 0)
            returnVal.add(rightAtts);

        return returnVal;
    }

    public void determineWhatAlgorithmToRun(Set<Set<String>> votes, Map<String, Set<String>> fds,
                                            Map<String, Set<Set<String>>> sortingAtts) {

        // get all of the input files for the left and the right sides
        ArrayList<String> leftSet = getValue("leftInput.inFiles").getStringList();
        String[] leftFiles = new String[1];
        leftFile = getNetworkOnInputSide().getFeeder(leftSet.get(0));
        leftFiles[0] = leftFile;

        ArrayList<String> rightSet = getValue("rightInput.inFiles").getStringList();
        String[] rightFiles = new String[1];
        rightFile = getNetworkOnInputSide().getFeeder(rightSet.get(0));
        rightFiles[0] = rightFile;

        // sanity check
        if (rightSet.size() > 1 || leftSet.size() > 1) {
            throw new RuntimeException("While the data flow supports > 1 input to join, the join can't handle it!");
        }

        // get the sizes.
        long leftSize = getPathsActualSize(leftFiles);
        long rightSize = getPathsActualSize(rightFiles);

        // check for cartesian products
        if (isCartesian) {
            leftAtts = new HashSet<String>();
            rightAtts = new HashSet<String>();
            return;
        }

        // check for self-joins and semi-joins.
        if (isSelfJoin || joinType != JoinType.NATURAL || leftFile.equals(rightFile)) {
            leftAtts = new HashSet<String>();
            rightAtts = new HashSet<String>();
            leftAtts.addAll(getValue("leftInput.hashAtts").getIdentifierList());
            rightAtts.addAll(getValue("rightInput.hashAtts").getIdentifierList());
            return;
        }


        // at a very high level, there are two cases... we got a request for a specific sort order, or
        // we did not.  The convention is that if we got a request for a specific sort order, our first
        // goal is to honor it no matter what.  This is what we do now.
        if (votes.size() > 0) {

            // first, find the largest set of atts that have been voted for
            Set<String> biggest = null;
            for (Set<String> s : votes) {
                if (biggest == null || s.size() > biggest.size()) {
                    biggest = s;
                }
            }

            // now we have the set of atts that we want to sort on.  So determine if it is in the left or the right
            boolean isFromLeft = false;
            ArrayList<String> inputSet = getValue("leftInput.hashAtts").getIdentifierList();
            Set<Set<String>> allSets = SubsetMachine.getAllSubsets(inputSet);
            if (allSets.contains(biggest)) {
                isFromLeft = true;
            }

            // if it is from the left, then we need to get the matching sorting atts on the RHS
            if (isFromLeft) {

                System.out.println("Sort atts are from left!");
                leftAtts = biggest;
                rightAtts = new HashSet<String>();
                for (String s : biggest) {
                    rightAtts.add(getEqualityCheckWith("left." + s));
                }
                System.out.println("Matched with " + rightAtts);

                // likewise, if it is from the right, then we need to get the matching sorting atts on the LHS
            } else {

                System.out.println("Sort atts are from right!");
                rightAtts = biggest;
                leftAtts = new HashSet<String>();
                for (String s : biggest) {
                    leftAtts.add(getEqualityCheckWith("right." + s));
                }
                System.out.println("Matched with " + leftAtts);
            }

            // at this point, we have the left and the right sort atts; so, see if they match the existing ones
            Set<Set<String>> leftSortAtts = sortingAtts.get(leftFile);
            Set<Set<String>> rightSortAtts = sortingAtts.get(rightFile);

            if (leftSortAtts != null && leftSortAtts.contains(leftAtts)) {

                // getting here means that we don't have to sort on the left
                if (rightSortAtts != null && rightSortAtts.contains(rightAtts)) {

                    // here we can just do a merge; everyone is sorted!
                    mergeJoin = true;

                } else {

                    // here we just sort the right... the left is sorted!
                    sortOnlyRight = true;
                }

                // getting here means we have to sort on the left
            } else {

                // getting here means that we don't have to sort on the left
                if (rightSortAtts != null && rightSortAtts.contains(rightAtts)) {

                    // here we need to sort just on the left
                    sortOnlyLeft = true;

                }
            }

            // that is it!
            return;
        }

        // if we get here, it means that we had no vote from above, and so we see if we can use a sorting to help us
        System.out.println("No votes!");

        // first we get the size of the left relation, as well as the hash atts from the LHS that the
        // input is already sorted on... do the same for the right
        leftAtts = getSortAttsWeCanUse("left", sortingAtts.get(leftFile));
        rightAtts = getSortAttsWeCanUse("right", sortingAtts.get(rightFile));

        System.out.println("leftAtts " + leftAtts + " rightAtts " + rightAtts);
        if ((leftSize > rightSize || rightAtts.size() == 0) && leftAtts.size() > 0) {

            // get all of the matching atts on the right side
            rightAtts = new HashSet<String>();
            for (String s : leftAtts) {
                rightAtts.add(getEqualityCheckWith("left." + s));
            }

            // if the atts we check equality with match the sort atts, we will only do a merge join
            if (rightAtts.equals(getSortAttsWeCanUse("right", sortingAtts.get(rightFiles[0])))) {
                System.out.println("Merge Join");
                mergeJoin = true;
            } else {
                System.out.println("Only sorting on right");
                sortOnlyRight = true;
            }

            return;
        }

        // if we are here, then the right is larger... if there are no sort atts on the right, then
        // we can't do a special join here, either
        if (rightAtts.size() == 0) {
            leftAtts.addAll(getValue("leftInput.hashAtts").getIdentifierList());
            rightAtts.addAll(getValue("rightInput.hashAtts").getIdentifierList());
            return;
        }

        // get all of the matching atts on the left side
        leftAtts = new HashSet<String>();
        for (String s : rightAtts) {
            leftAtts.add(getEqualityCheckWith("right." + s));
        }

        // if the atts we check equality with match the sort atts, we will only do a merge join
        if (leftAtts.equals(getSortAttsWeCanUse("left", sortingAtts.get(leftFile)))) {
            System.out.println("Merge Join");
            mergeJoin = true;
        } else {
            sortOnlyLeft = true;
            System.out.println("Only sorting on left");
        }
    }

    // returns the name of the operation
    public String getOperatorName() {
        return "Join";
    }

    // this returns the size of either the left or the right input
    private long getSize(String leftOrRight) {
        ArrayList<String> inputSet = getValue(leftOrRight + "Input.inFiles").getStringList();
        String[] foo = {""};
        String[] bar = inputSet.toArray(foo);
        return getPathsActualSize(bar);
    }


    // do we accept an input in the pipeline?
    public boolean acceptsPipelineable() {

        // not if semijoin.
        if (joinType != JoinType.NATURAL) {
            return false;
        }

        long leftSize = getSize("left");
        long rightSize = getSize("right");

        // only if either side is materialized.
        return leftSize != 0 || rightSize != 0;
    }

    // determines whether we can pipeline the join... for this to happen, either the left or the
    // right must be materialized, and the left or the right must be small enough to buffer in RAM
    public boolean isPipelineable(int megabytesIn) {

        if (joinType != JoinType.NATURAL) {
            return false;
        }

        long leftSize = getSize("left") * 10;
        long rightSize = getSize("right") * 10;

        long megabytes = (megabytesIn * 1024L * 1024L);

        if (leftSize == 0 && rightSize == 0)
            return false;
        else if (leftSize != 0 && leftSize < megabytes) {
            return true;
        } else if (rightSize != 0 && rightSize < megabytes) {
            return true;
        } else
            return false;
    }

    // returns a pipelined version of the join
    public PipeNetwork getPipelinedVersion() {
        PipeNode temp = new PipelinedJoin(this);
        return addPipelinedOperationToInputPipeNetwork(temp);
    }

    public PipeNetwork getPipelinedVersion(String materializedInputFile) {
        materializedInputFileSpecified = true;
        this.materializedInputFile = materializedInputFile;
        PipeNode temp = new PipelinedJoin(this);
        return addPipelinedOperationToInputPipeNetwork(temp);
    }

    // returns the number of megabytes that we think will be needed to pipeline this thing
    public long numMegsToPipeline() {

        long leftSize = getSize("left") * 10;
        long rightSize = getSize("right") * 10;
        if (leftSize == 0 && rightSize == 0)
            return 10000 * 10000;

        if (rightSize == 0)
            return leftSize / (1024L * 1024L);

        return Math.min(leftSize, rightSize) / (1024L * 1024L);
    }

    // returns the larger of the two inputs
    public String getLargeInput() {
        String small = getSmallInput();
        if (small == null)
            return null;
        else if (small.equals(getValue("rightInput.inFiles").getStringList().get(0)))
            return getValue("leftInput.inFiles").getStringList().get(0);
        else
            return getValue("rightInput.inFiles").getStringList().get(0);
    }

    // the "left" input file is supposed to be the larger one... so if it is not, then we need to reverse
    public boolean needToReverseInputs() {
        String large = getLargeInput();
        return (large != getValue("rightInput.inFiles").getStringList().get(0));
    }

    // returns the smaller of the two inputs
    public String getSmallInput() {

        if (materializedInputFileSpecified) {
            return getValue(materializedInputFile + "Input.inFiles").getStringList().get(0);
        }

        long leftSize = getSize("left");
        long rightSize = getSize("right");

        // if both the left and the right are zero sized, then neither has been materialized
        if (leftSize == 0 && rightSize == 0) {
            return null;
        }

        // if the left has not been materialized, or if the right is smaller, return the right
        if (leftSize == 0 || (rightSize != 0 && rightSize < leftSize)) {
            return getValue("rightInput.inFiles").getStringList().get(0);
        }

        // just return the left
        return getValue("leftInput.inFiles").getStringList().get(0);
    }

    // returns the set of inputs
    public String[] getInputs() {
        ArrayList<String> inputSet = new ArrayList<String>();
        inputSet.addAll(getValue("leftInput.inFiles").getStringList());

        if (!isSelfJoin) {
            inputSet.addAll(getValue("rightInput.inFiles").getStringList());
        }

        String[] foo = {""};
        return inputSet.toArray(foo);
    }

    public Map<String, Set<Set<String>>> getExistingSortAtts(PhysicalDatabase myDBase) {

        Map<String, Set<Set<String>>> ret = new HashMap<String, Set<Set<String>>>();

        // get the input file name.
        String inFile = getValue("rightInput.inFiles").getStringList().get(0);

        // check if it is sorted.
        if (myDBase.isTableSorted(myDBase.getTableName(inFile))) {

            // if so, get the name from the list of input atts.
            int sortedAttPos = myDBase.getTableSortingAttribute(myDBase.getTableName(inFile));
            ArrayList<String> atts = getValue("rightInput.inAtts").getIdentifierList();
            if (sortedAttPos < atts.size()) {
                Set<Set<String>> mx = new HashSet<Set<String>>();
                Set<String> kx = new HashSet<String>();
                kx.add(atts.get(sortedAttPos));
                mx.add(kx);

                ret.put(inFile, mx);
            }
        }

        // get the input file name.
        inFile = getValue("leftInput.inFiles").getStringList().get(0);

        // check if it is sorted.
        if (myDBase.isTableSorted(myDBase.getTableName(inFile))) {

            // if so, get the name from the list of input atts.
            int sortedAttPos = myDBase.getTableSortingAttribute(myDBase.getTableName(inFile));
            ArrayList<String> atts = getValue("leftInput.inAtts").getIdentifierList();
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

    // returns the functions -- from the selections, join and output expressions
    public String[] getFunctions() {

        HashSet<String> one = new HashSet<String>();

        // left input selection
        ParsedRHS leftInputSelRHS = getValue("leftInput.selection");
        if (leftInputSelRHS != null) {
            one.addAll(leftInputSelRHS.getExpression().getAllFunctions());
        }

        // left input output attributes
        for (Assignment a : getValue("leftInput.outAtts").getAssignmentList()) {
            one.addAll(a.getExpression().getAllFunctions());
        }

        // right input selection
        ParsedRHS rightInputSelRHS = getValue("rightInput.selection");
        if (rightInputSelRHS != null) {
            one.addAll(rightInputSelRHS.getExpression().getAllFunctions());
        }

        // right input output attributes
        for (Assignment a : getValue("rightInput.outAtts").getAssignmentList()) {
            one.addAll(a.getExpression().getAllFunctions());
        }

        // join selection
        ParsedRHS outputSelRHS = getValue("output.selection");
        if (outputSelRHS != null) {
            one.addAll(outputSelRHS.getExpression().getAllFunctions());
        }

        // join output attributes
        for (Assignment a : getValue("output.outAtts").getAssignmentList()) {
            one.addAll(a.getExpression().getAllFunctions());
        }

        return one.toArray(new String[0]);
    }

    // returns the set of macro replacements
    public Map<String, String> buildMacroReplacements() {

        Map<String, String> replacements = new HashMap<String, String>();

        // build the left input record replacements
        Map<String, String> leftInAtts = buildAttributeReplacements("leftInput.inAtts", "");
        replacements.put("leftNumAtts", "" + leftInAtts.size());
        replacements.put("leftNumOutAtts", "" + getValue("leftInput.outAtts").getAssignmentList().size());
        replacements.put("leftTypeCode", "" + leftTypeCode);
        replacements.put("leftSelection", buildSelectionReplacement("leftInput.selection", leftInAtts));
        replacements.put("leftAssignments", buildAssignmentReplacements("leftInput.outAtts", "returnVal.", leftInAtts));

        // do the same with the right input record.
        Map<String, String> rightInAtts = buildAttributeReplacements("rightInput.inAtts", "");
        replacements.put("rightNumAtts", "" + rightInAtts.size());
        replacements.put("rightNumOutAtts", "" + getValue("rightInput.outAtts").getAssignmentList().size());
        replacements.put("rightTypeCode", "" + rightTypeCode);
        replacements.put("rightSelection", buildSelectionReplacement("rightInput.selection", rightInAtts));
        replacements.put("rightAssignments", buildAssignmentReplacements("rightInput.outAtts", "returnVal.", rightInAtts));

        // build the hashable record replacements.
        // leftAtts could be null if we are pipelining
        if (leftAtts != null) {
            replacements.put("leftHash", buildHashReplacements(leftAtts, "leftInput.outAtts", ""));
            replacements.put("leftHashPositions", buildAttPositionsArray(leftAtts, "leftInput.outAtts"));
        } else {
            replacements.put("leftHash", buildHashReplacements("leftInput.hashAtts", "leftInput.outAtts", ""));
            replacements.put("leftHashPositions", buildAttPositionsArray("leftInput.hashAtts", "leftInput.outAtts"));
        }

        // likewise, rightAtts could be null if we are pipelinining
        if (rightAtts != null) {
            replacements.put("rightHash", buildHashReplacements(rightAtts, "rightInput.outAtts", ""));
            replacements.put("rightHashPositions", buildAttPositionsArray(rightAtts, "rightInput.outAtts"));
        } else {
            replacements.put("rightHash", buildHashReplacements("rightInput.hashAtts", "rightInput.outAtts", ""));
            replacements.put("rightHashPositions", buildAttPositionsArray("rightInput.hashAtts", "rightInput.outAtts"));
        }

        replacements.put("leftSecondaryHash", buildHashReplacements("leftInput.hashAtts", "leftInput.outAtts", ""));
        replacements.put("rightSecondaryHash", buildHashReplacements("rightInput.hashAtts", "rightInput.outAtts", ""));

        // build the output record replacements.
        Map<String, String> allAtts = buildAttributeAssignReplacements("leftInput.outAtts", "left.", "left.");
        allAtts.putAll(buildAttributeAssignReplacements("rightInput.outAtts", "right.", "right."));

        replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());
        replacements.put("outputNumOutAtts", "" + getValue("output.outAtts").getAssignmentList().size());
        replacements.put("outputSelection", buildSelectionReplacement("output.selection", allAtts));
        replacements.put("outputAssignments", buildAssignmentReplacements("output.outAtts", "output.", allAtts));
        replacements.put("functionDeclarations", "" + buildFunctionDeclarations());

        // build the reducer ordering replacement -- smallest relation goes first
        long leftSize = getPathsActualSize(getValue("leftInput.inFiles").getStringList().toArray(new String[0]));
        long rightSize = getPathsActualSize(getValue("rightInput.inFiles").getStringList().toArray(new String[0]));
        if (rightSize > leftSize && joinType == JoinType.NATURAL) {
            replacements.put("reducerOrdering", "leftFirst");
        } else {
            replacements.put("reducerOrdering", "rightFirst");
        }

        // we're done
        return replacements;
    }


    // set some configurations
    public void setConfigurations(Configuration conf, RuntimeParameter params) {

        // first, send out the type of join
        conf.setStrings("simsql.joinType", new String[]{joinType.toString().toLowerCase()});

        // set the self-join value
        conf.setBoolean("simsql.isSelfJoin", isSelfJoin);

        // see if we have a Cartesian product
        conf.setBoolean("simsql.joinCartesian", isCartesian);

        // see if we have a pure, map-only merge join
        conf.setBoolean("simsql.isMergeJoin", mergeJoin);

        // if we are able to avoid a sort of the left or of the right, then we need some extra configs that will allow the merge
        if (mergeJoin || sortOnlyRight) {
            conf.setInt("simsql.sortedFileTypeCode", getDB().getTypeCode(getDB().getTableName(leftFile)));
            conf.set("simsql.sortedFileName", leftFile);
            conf.setInt("simsql.sortedFileNumAtts", getDB().getNumAtts(getDB().getTableName(leftFile)));
        } else if (sortOnlyLeft) {
            conf.setInt("simsql.sortedFileTypeCode", getDB().getTypeCode(getDB().getTableName(rightFile)));
            conf.set("simsql.sortedFileName", rightFile);
            conf.setInt("simsql.sortedFileNumAtts", getDB().getNumAtts(getDB().getTableName(rightFile)));
        }

        // find out which relation is the largest.
        long leftSize = getPathsActualSize(getValue("leftInput.inFiles").getStringList().toArray(new String[0]));
        long rightSize = getPathsActualSize(getValue("rightInput.inFiles").getStringList().toArray(new String[0]));
        long smallerSize = 0;
        long largerSize = 0;
        int smallerTypeCode = -1;
        int largerTypeCode = -1;

        if (leftSize < rightSize) {
            smallerSize = leftSize;
            largerSize = rightSize;
            smallerTypeCode = leftTypeCode;
            largerTypeCode = rightTypeCode;
        } else {
            smallerSize = rightSize;
            largerSize = leftSize;
            smallerTypeCode = rightTypeCode;
            largerTypeCode = leftTypeCode;
        }

        // and pass the typecode and size of those relations.
        conf.setInt("simsql.smallerRelation.typeCode", smallerTypeCode);
        conf.setInt("simsql.largerRelation.typeCode", largerTypeCode);
        conf.setLong("simsql.smallerRelation.size", smallerSize);
        conf.setLong("simsql.largerRelation.size", largerSize);
    }

    // returns the name of the template Java source file.
    public String getTemplateFile() {
        return "JoinRecords.javat";
    }

    // returns the mapper class.
    public Class<? extends Mapper> getMapperClass() {
        return JoinMapper.class;
    }

    // returns the reducer class.
    public Class<? extends Reducer> getReducerClass() {
        return JoinReducer.class;
    }

    public Class getOutputValueClass() {
        return Result.class;
    }


    // number of reducers -- depends on whether we are doing a pure merge join
    public int getNumReducers(RuntimeParameter params) {

        // map-only job if we are running a pure merge join
        if (mergeJoin)
            return 0;

        // otherwise, use reducers
        ExampleRuntimeParameter p = (ExampleRuntimeParameter) params;
        return p.getNumCPUs();
    }

    public JoinOp(Map<String, ParsedRHS> valuesIn) {
        super(valuesIn);

        // check the join type
        joinType = JoinType.NATURAL;
        if (getValue("joinType") != null) {

            String s = getValue("joinType").getIdentifier();
            joinType = JoinType.valueOf(s.toUpperCase());
            if (joinType == null) {
                throw new RuntimeException("Unrecognized join type " + s);
            }
        }

        // get the type codes
        leftTypeCode = getValue("leftInput.typeCode").getInteger().shortValue();
        rightTypeCode = getValue("rightInput.typeCode").getInteger().shortValue();

        // determine if we have a self-join
        isSelfJoin = (leftTypeCode == rightTypeCode);
        isCartesian = getValue("leftInput.hashAtts") == null || getValue("rightInput.hashAtts") == null;
    }

}
