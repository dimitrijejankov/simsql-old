

/*****************************************************************************
 * *
 * Copyright 2014 Rice University                                           *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                           *
 * *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 * *
 *****************************************************************************/


package simsql.runtime;

import simsql.compiler.ParallelExecutor;
import simsql.shell.*;
import simsql.code_generator.*;

import java.io.*;
import java.util.*;

import org.antlr.runtime.*;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;

/**
 * Hadoop-SimSQL runtime interface class.
 *
 * @author Luis.
 */
public class HadoopRuntime implements simsql.shell.Runtime<DataFlowQuery, HadoopResult> {

    private ExampleRuntimeParameter params = new ExampleRuntimeParameter();

    // this is here so that we can look up the typecode of reltions
    private MyPhysicalDatabase myDatabase;

    public HadoopRuntime(MyPhysicalDatabase myDBase) {
        myDatabase = myDBase;
    }

    private PrintStream savedOut = System.out;
    private PrintStream savedErr = System.err;

    // so we can stop printing to the screen
    private void turnOffOutput() {
        try {
            System.setOut(new PrintStream("/dev/null"));
            System.setErr(new PrintStream("/dev/null"));
        } catch (Exception c) {
            System.out.println("Died trying to turn off console output");
        }
    }

    // so we can resume printing to the screen
    private void turnOnOutput() {
        System.setOut(savedOut);
        System.setErr(savedErr);
    }


    private void saveTextCopy(String hdfsFile, String outFile1, String[] attNames, short typeCode) {

        String outFile = outFile1.replaceAll("/", "__");

        try {

            // write the schema file.
            BufferedWriter outSch = new BufferedWriter(new FileWriter(outFile + ".schema.txt"));

            for (String s : attNames) {
                outSch.write(s + "|");
            }

            outSch.flush();
            outSch.close();

            // get the output file
            BufferedWriter out = new BufferedWriter(new FileWriter(outFile + ".data.txt"));

            // set up a loader record
            LoaderRecord.setup(attNames.length, typeCode);

            // now we open up all of the files in the directory
            Configuration conf = new Configuration();
            FileSystem fs = FileSystem.get(conf);
            Path path = new Path(hdfsFile);
            FileStatus fstatus[] = fs.listStatus(path, new TableFileFilter());

            for (FileStatus fstatu : fstatus) {

                // at this point, we got it, so open up the file
                Class<?>[] temp = {LoaderRecord.class};
                InputFileDeserializer myRecordSource = new InputFileDeserializer(temp);
                myRecordSource.initialize(fstatu.getPath());

        /*-------------------for debugging------------------ zhuhua */
                out.write(fstatu.getPath().toString() + "\n");
                int num = 0;
                while (myRecordSource.nextKeyValue()) {
                    Record rec = myRecordSource.getCurrentValue();

                    for (int j = 0; j < rec.getNumAttributes(); j++) {
                        rec.getIthAttribute(j).writeSelfToTextStream(out);
                    }


                    out.newLine();

                    num++;
                    if (num >= 10000) {
                        break;
                    }
                }

	/*-----------------------end----------------*/
                myRecordSource.close();
            }

            // close everything and leave
            out.flush();
            out.close();
        } catch (Exception e) {
            System.out.println("Couldn't save text copy of file " + hdfsFile);
        }
    }

    // sort the relops on the size of the operation's input, from small to large
    private ArrayList<RelOp> sortBySize(ArrayList<RelOp> result) {
        ArrayList<RelOp> something = new ArrayList<RelOp>();
        something.addAll(result);

        Collections.sort(something, new Comparator<RelOp>() {

            public int compare(RelOp a, RelOp b) {
                Long t1 = 0L;
                for (String s : a.getNonPipelinedInputFiles()) {
                    t1 += myDatabase.getTableSize(myDatabase.getTableName(s));
                }

                for (String s : a.getPipelinedInputFiles()) {
                    Long ti = myDatabase.getTableSize(myDatabase.getTableName(s));
                    if (ti == 0) {
                        t1 = Long.MAX_VALUE;
                        break;
                    }
                    t1 += ti;
                }

                Long t2 = 0L;
                for (String s : b.getNonPipelinedInputFiles()) {
                    t2 += myDatabase.getTableSize(myDatabase.getTableName(s));
                }

                for (String s : b.getPipelinedInputFiles()) {
                    Long tj = myDatabase.getTableSize(myDatabase.getTableName(s));
                    if (tj == 0) {
                        t2 = Long.MAX_VALUE;
                        break;
                    }

                    t2 += tj;
                }

                return t1.compareTo(t2);
            }
        });

        return something;
    }

    // return the list of relational operators that accepts the input of me
    private ArrayList<RelOp> getFollows(ArrayList<RelOp> candidates, RelOp me) {

        // now, we find all of the guys who come after him
        ArrayList<RelOp> nextOnes = new ArrayList<RelOp>();
        for (RelOp r : candidates) {

            // see if this rel op takes our output as input
            boolean follows = false;
            for (String s : r.getInputFiles()) {

                // we found the guy coming after this operation!
                if (me.getOutput().equals(s)) {
                    follows = true;
                }
            }

            if (follows) {
                nextOnes.add(r);
            }
        }

        return nextOnes;
    }

    // this makes a very rough attempt at cleaning up all of the files that are sitting around...
    // note that bad stuff can happen because this is not actually thread safe...
    private HashSet<String> outFiles = new HashSet<String>();

    public void killRuntime() {
        turnOnOutput();
        for (String s : outFiles) {
            try {
                myDatabase.deleteTable(myDatabase.getTableName(s));
            } catch (Exception e) {
                System.out.println("Little issue when I tried to clean up " + s);
            }
        }
        myDatabase.removeZeroSizeRelations("query_block");
    }

    public HadoopResult run(DataFlowQuery queryPlan) {

        boolean verbose = params.getDebug();

        if (!verbose)
            turnOffOutput();

        // get an output result
        HadoopResult res = new HadoopResult();

        // first, we will read up the input file into an array of
        // relational operators.
        ArrayList<RelOp> result;

        try {
            String toParse = queryPlan.getQueryStr();

            // then parse the string and get a set of relational operations to run
            ANTLRStringStream parserIn = new ANTLRStringStream(toParse);
            DataFlowLexer lexer = new DataFlowLexer(parserIn);
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            DataFlowParser parser = new DataFlowParser(tokens);
            result = parser.parse();

        } catch (Exception e) {
            if (!verbose)
                turnOnOutput();

            StringWriter sw = new StringWriter();
            e.printStackTrace(new PrintWriter(sw));
            res.setError("Could not obtain translated input.\n" + sw.toString());
            return res;
        }

        long t = System.currentTimeMillis();

        // this will store all of the functional dependencies that we are
        // aware of in the query plan... they are possibly used by the relational
        // operations to speed query processing... the key names a LHS attribute,
        // and the value (the set) names all of the RHS attributes
        Map<String, Set<String>> fds = new HashMap<String, Set<String>>();

        // this stores the sort order for all of the intermediate tables that are
        // created... note that it might be the case that some of these attributes
        // don't even exist in the table... the key is the name of a file and the
        // value is the list of attributes that the file is sorted on
        Map<String, Set<Set<String>>> sortingAtts = new HashMap<String, Set<Set<String>>>();

        // get all the output files.
        outFiles = new HashSet<String>();
        HashSet<String> finalFilesSet = new HashSet<String>();

        if (!verbose) {
            turnOnOutput();
            System.out.print("Executing the plan. Number of operations to go is...\n\t");
            turnOffOutput();
        }

        for (RelOp r : result) {

            // get the output files
            outFiles.add(r.getOutput());

            // set up finals...
            if (r.isFinal()) {
                finalFilesSet.add(r.getOutput());
            }

            // set up the pipeline network that is within the relational op
            r.setupPipeNetwork(myDatabase);

            // set the sorting attributes based on the input files
            Map<String, Set<Set<String>>> mySortingAtts = r.getExistingSortAtts(myDatabase);
            for (String s : mySortingAtts.keySet()) {
                if (sortingAtts.containsKey(s)) {
                    sortingAtts.get(s).addAll(mySortingAtts.get(s));
                } else {
                    sortingAtts.put(s, mySortingAtts.get(s));
                }
            }
        }

        if (verbose) {
            System.out.println("SORTING ATTRIBUTES FROM PREVIOUS RUNS / BASE TABLES: " + sortingAtts);
        }

        // and our waiting set.
        HashSet<String> waitFiles = new HashSet<String>();
        waitFiles.addAll(outFiles);

        // now we keep on running the various operations, repeatedly
        // choosing one that does not have a dependency
        String[] outputAtts = null;
        String outputFile = null;

        // heuristically run the ops with the small inputs first
        result = sortBySize(result);

        // operations that are either piplined or not yet executed.
        ArrayList<RelOp> unexecutedOps = new ArrayList<RelOp>(result);

        HashMap<Pair<String, String>, Integer> pipelineStats = new HashMap<>();
        HashMap<Pair<String, String>, Integer> totalPipelineStats = new HashMap<>();
        HashSet<Pair<Integer, Integer>> visitedPairs = new HashSet<>();

        // we just started executing
        ParallelExecutor parallelExecutor = null;

        for (int i = 0; i < result.size(); i++) {

            // tells if we found a guy to pipeline
            boolean gotOne = false;

            // find a guy that we can pipeline
            for (int pos = 0; pos < result.size(); pos++) {

                // first, find the dude who comes after this operation....
                RelOp r1 = result.get(pos);

                // don't pipeline final guys.
                if (r1.isFinal())
                    continue;

                ArrayList<RelOp> follows = getFollows(unexecutedOps, r1);

                if (follows.size() > 0) {

                    // store the result quick and dirty
                    Integer op1 = System.identityHashCode(r1);
                    Integer op2 = System.identityHashCode(follows.get(0));

                    Pair<Integer, Integer> key = new Pair<>(op1, op2);

                    if(!visitedPairs.contains(key)) {

                        // store the result quick and dirty
                        Pair<String, String> k = new Pair<>(r1.getClass().getSimpleName(),
                                                               follows.get(0).getClass().getSimpleName());

                        totalPipelineStats.putIfAbsent(k, 0);

                        // increase the current value
                        totalPipelineStats.put(k, totalPipelineStats.get(k) + 1);

                        visitedPairs.add(key);
                    }
                }


                // if one guy comes after, then perhaps we can pipeline
                if (follows.size() == 1 && r1.isPipelineable((int) (follows.get(0).getRemainingMemForPipelinePerMapper (getRuntimeParameters()))) && follows.get(0).acceptsPipelineable()) {

                    follows.get(0).suckUpNetworkOnInput(r1.getPipelinedVersion());

                    // store the result quick and dirty
                    String op1 = r1.getClass().getSimpleName();
                    String op2 = follows.get(0).getClass().getSimpleName();

                    Pair<String, String> key = new Pair<>(op1, op2);

                    // if we did not initialize
                    pipelineStats.putIfAbsent(key, 0);

                    // increase the current value
                    pipelineStats.put(key, pipelineStats.get(key) + 1);


			/*
		                    boolean foundPipelinedInputFile = false;

                    if (r1 instanceof JoinOp && follows.get(0) instanceof JoinOp) {
                        if (verbose) {
                            System.out.println("All the sorting atts: " + sortingAtts);
                            System.out.println("The sorting atts of r1: " + r1.getAllPossibleSortingAtts());
                            System.out.println("The sorting atts of next: " + follows.get(0).getAllPossibleSortingAtts());
                            // System.out.println("The preferred sort order of r1 to next: " + follows.get(0).getPreferredSortOrder(r1.getOutput(), fds, r1.getAllPossibleSortingAtts()));
                        }

                        String leftFile1 = r1.getNetworkOnInputSide().getFeeder(r1.getValue("leftInput.inFiles").getStringList().get(0));
                        String rightFile1 = r1.getNetworkOnInputSide().getFeeder(r1.getValue("rightInput.inFiles").getStringList().get(0));

                        if (sortingAtts.containsKey(leftFile1) && sortingAtts.containsKey(rightFile1)) {
                            // String leftSortingAtt1 = sortingAtts.get(leftFile1).iterator().next().iterator().next();
                            // String rightSortingAtt1 = sortingAtts.get(rightFile1).iterator().next().iterator().next();
                            if (verbose) {
                                System.out.println("The left file of r1: " + leftFile1 + ", sorted on: " + sortingAtts.get(leftFile1));
                                System.out.println("The right file of r1: " + rightFile1 + ", sorted on: " + sortingAtts.get(rightFile1));
                            }

                            String leftFile2 = follows.get(0).getNetworkOnInputSide().getFeeder(follows.get(0).getValue("leftInput.inFiles").getStringList().get(0));
                            String rightFile2 = follows.get(0).getNetworkOnInputSide().getFeeder(follows.get(0).getValue("rightInput.inFiles").getStringList().get(0));
                            if (sortingAtts.containsKey(leftFile2)) {
                                // String leftSortingAtt2 = sortingAtts.get(leftFile2).iterator().next().iterator().next();
                                // String rightSortingAtt2 = sortingAtts.get(rightFile2).iterator().next().iterator().next();
                                if (verbose) {
                                    System.out.println("The left file of next: " + leftFile2 + ", sorted on: " + sortingAtts.get(leftFile2));
                                    // System.out.println("The right file of next: " + rightFile2 + ", sorted on: " + sortingAtts.get(rightFile2));
                                    System.out.println("The right file of next: " + rightFile2);
                                }
                                String leftSortingAtt1 = "";
                                String rightSortingAtt1 = "";
                                String determiningSortingAtt = "";
                                // if (follows.get(0).getValue ("leftInput.hashAtts").getIdentifierList ().contains(leftSortingAtt1)
                                //     && follows.get(0).getValue ("leftInput.hashAtts").getIdentifierList ().contains(rightSortingAtt1)) {
                                //     determiningSortingAtt = rightSortingAtt2;
                                // }
                                ArrayList<String> leftHashAtts = follows.get(0).getValue("leftInput.hashAtts").getIdentifierList();
                                ArrayList<String> rightHashAtts = follows.get(0).getValue("rightInput.hashAtts").getIdentifierList();
                                if (rightHashAtts.size() == 2) {
                                    if (sortingAtts.get(leftFile1).contains(Collections.singleton(rightHashAtts.get(0)))) {
                                        leftSortingAtt1 = rightHashAtts.get(0);
                                        rightSortingAtt1 = rightHashAtts.get(1);
                                    } else {
                                        leftSortingAtt1 = rightHashAtts.get(1);
                                        rightSortingAtt1 = rightHashAtts.get(0);
                                    }
                                    if (leftHashAtts.size() == 2) {
                                        if (sortingAtts.get(leftFile2).contains(Collections.singleton(leftHashAtts.get(0)))) {
                                            determiningSortingAtt = leftHashAtts.get(0);
                                        } else {
                                            determiningSortingAtt = leftHashAtts.get(1);
                                        }
                                    }
                                }
                                // if (follows.get(0).getValue ("rightInput.hashAtts").getIdentifierList ().contains(leftSortingAtt1)
                                //         && follows.get(0).getValue ("rightInput.hashAtts").getIdentifierList ().contains(rightSortingAtt1)) {
                                //     determiningSortingAtt = leftSortingAtt2;
                                // }
                                if (determiningSortingAtt.length() > 0) {
                                    if (verbose) {
                                        System.out.println("The determining sort att: " + determiningSortingAtt);
                                    }
                                    String pipelinedInputFile = "", materializedInputFile = "";
                                    String pipelinedSortingAtt = ((JoinOp) follows.get(0)).getEqualityCheckWith("any." + determiningSortingAtt);
                                    if (leftSortingAtt1.equals(pipelinedSortingAtt)) {
                                        pipelinedInputFile = leftFile1;
                                        materializedInputFile = "right";
                                    }
                                    if (rightSortingAtt1.equals(pipelinedSortingAtt)) {
                                        pipelinedInputFile = rightFile1;
                                        materializedInputFile = "left";
                                    }
                                    if (pipelinedInputFile.length() > 0) {
                                        foundPipelinedInputFile = true;
                                        if (verbose) {
                                            System.out.println("The pipelined file should be: " + pipelinedInputFile + ", sorted on: " + pipelinedSortingAtt);
                                            System.out.println("The materialized file should be: " + materializedInputFile);
                                        }
                                        // follows.get(0) will now take r1 with pipelinedInputFile pipelined and the other table materialized
                                        follows.get(0).suckUpNetworkOnInput(((JoinOp) r1).getPipelinedVersion(materializedInputFile));
                                    }
                                }
                            }
                        }
                    }

                    if (!foundPipelinedInputFile) {
                        follows.get(0).suckUpNetworkOnInput(r1.getPipelinedVersion());
                    }
*/

                    // let the user know how we are doing
                    if (!verbose) {
                        turnOnOutput();
                        System.out.print(result.size() + "->");
                        turnOffOutput();
                    }

                    result.remove(pos);
                    gotOne = true;
                    break;
                }

                // if we can't pipeline, then perhaps we can append the second one onto the first one
                if (follows.size() == 1 && follows.get(0).isAppendable(r1.getInputFiles()) && r1.acceptsAppendable()) {
                    r1.appendNetworkOnOutputSide(follows.get(0).getAppendableNetwork());
                    gotOne = true;

                    // store the result quick and dirty
                    String op1 = r1.getClass().getSimpleName();
                    String op2 = follows.get(0).getClass().getSimpleName();

                    Pair<String, String> key = new Pair<>(op1, op2);

                    // if we did not initialize
                    pipelineStats.putIfAbsent(key, 0);

                    // increase the current value
                    pipelineStats.put(key, pipelineStats.get(key) + 1);

                    // set stats collection (for the finals that get some pre-processing work moved but won't run a mapper).
                    r1.setCollectStats(follows.get(0).collectStats());
                    break;
                }
            }

            // if we found a dude to pipeline in this last pass, then try again
            if (gotOne) {
                i = -1;
                continue;
            }

            // now we check to see if we can append part of one operation onto the end of another one, so
            // at least part of the second operation can run concurrently with the first one

            // get the files this operation needs
            HashSet<String> filesNeeded = new HashSet<String>(Arrays.asList(result.get(i).getInputFiles()));

            // see if we are waiting on any of them
            boolean okToRun = true;
            for (String ff : filesNeeded) {
                okToRun &= !waitFiles.contains(ff);
            }

            // can we run this OP?
            if (okToRun) {

                // get the next op to run, and then ask it to re-evaluate it's input files (the goal is to
                // avoid a problem that we observed where pipelined ops can choose to pipeline a tiny input file)
                RelOp nextOp = result.get(i);
                nextOp.reEvaluateWhichFilesArePipelined();

                if (verbose) {
                    System.out.println("Running operation to produce " + nextOp.getOutput());
                    System.out.println(nextOp.getOperatorName());
                    System.out.print("INPUT SET: [");
                    for (String s : nextOp.getInputs()) {
                        System.out.print(s + ", ");
                    }
                    System.out.println("]");
                }

                // next we ask this guy to add any functional dependencies that he might create
                nextOp.addAnyNewFDs(fds, sortingAtts);
                if (verbose) {
                    System.out.println("FUNC. DEPENDENCIES: " + fds + "\n");
                }

                // and we ask this guy for the various possible lists of sorting attributes
                // that he might produce... each item in the set is a set of possible sorting atts
                Set<Set<String>> allPossibleSortingAtts = nextOp.getAllPossibleSortingAtts();

                Set<Set<String>> votes = new HashSet<Set<String>>();

                if (verbose) {
                    System.out.println("All possible sorting atts is " + allPossibleSortingAtts);
                }

                // get all of the guys who use his output
                ArrayList<RelOp> follows = getFollows(result, nextOp);

                // ask each of them if they have a preference for the sorting attributes
                for (RelOp r : follows) {
                    Set<String> preferredSort = r.getPreferredSortOrder(nextOp.getOutput(), fds, allPossibleSortingAtts);
                    if (verbose) {
                        System.out.println("Voted for " + preferredSort);
                    }
                    if (preferredSort != null)
                        votes.add(preferredSort);
                }

                // now give those preferences to the relational op, and ask it to plan what it is going to do
                nextOp.determineWhatAlgorithmToRun(votes, fds, sortingAtts);

                // and ask it what sort order it is going to provide on output
                Set<Set<String>> sortedOutput = nextOp.getSortedOutputAtts();
                if (sortedOutput != null) {
                    if (verbose) {
                        System.out.println("adding " + sortedOutput + " for " + nextOp.getOutput());
                    }
                    sortingAtts.put(nextOp.getOutput(), sortedOutput);
                }

                // run the job
                if(params.getSpeculativeExecution()) {

                    // if we are doing speculative execution
                    ParallelExecutor parent = parallelExecutor;

                    parallelExecutor = new ParallelExecutor(nextOp, verbose, getRuntimeParameters(), parent);
                    parallelExecutor.start();

                    // check if there is an executor running
                    if(parent != null) {

                        // if it is wait for it
                        parent.waitToFinish();

                        // ok we got the thing check for error
                        if(parent.hasError()) {

                            // finish this then and set the error
                            res.setError(parent.getError());
                            return res;
                        }
                    }

                    // speculate the statistics
                    nextOp.speculateStatistics((ExampleRuntimeParameter) getRuntimeParameters());
                }
                else {

                    // just run the job and wait for it to finish
                    parallelExecutor = new ParallelExecutor(nextOp, verbose, getRuntimeParameters(), null);
                    parallelExecutor.start();
                    parallelExecutor.waitToFinish();
                }

                if (!verbose) {
                    turnOnOutput();
                    System.out.print(result.size() + " (running " + nextOp.getOperatorName() + " as Hadoop job...)\n\t");
                    turnOffOutput();
                }

                // we record its output file for the end...
                // ...
                // Note: this is sort of a hack here, added by chris... when someone issues a straight query over a time
                // tick table (or tables) things didn't work because there is a dummy file at the very end of the plan.
                // It was this dummy table that was being recorded as "outputFile".  I changed this so that a regular
                // query at the end of a Markov chain will always be called "file_to_print_i_j".  So now the
                // way that it works is that such a file, if found, is always choen is the output file
                if (outputFile == null || !outputFile.contains("file_to_print_")) {
                    outputAtts = result.get(i).getOutputAttNames();
                    outputFile = result.get(i).getOutput();
                }

                // add the sorted output atts to the physical database.
                if (sortedOutput != null) {
                    for (Set<String> sq : sortedOutput) {

                        if (sq.size() > 1) {
                            continue;
                        }

                        for (String sk : sq) {

                            for (int k = 0; k < outputAtts.length; k++) {
                                if (outputAtts[k].equals(sk)) {

                                    // set that thing sorted
                                    myDatabase.setTableSortingAtt(myDatabase.getTableName(outputFile), k);
                                    break;
                                }
                            }
                        }
                    }
                }

                if (verbose) {

                    // UNCOMMENTING THE FOLLOWING LINE WILL CREATE A LOCAL TEXT FILE FOR THE OUTPUT OF EACH OPERATOR.
                    // BE CAREFUL WHEN YOU USE IT, AND COMMENT IT BACK WHEN YOU'RE DONE.
                    saveTextCopy(outputFile, "save_" + outputFile, outputAtts, result.get(i).getOutputTypeCode());
                }

                // remove the executed operation from the unexecuted operations list.
                unexecutedOps.remove(result.get(i));

                // remove from the queue.
                result.remove(i);

                // recreate the wait list.
                waitFiles = new HashSet<>();
                for (RelOp r : result) {
                    r.reEvaluateWhichFilesArePipelined();
                    waitFiles.add(r.getOutput());
                }

                result = sortBySize(result);

                // and start over
                i = -1;
            }
        }

        try(PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter("/tmp/stats.txt", true)))) {

            // output everything
            out.println("<<<<<<<<<<<<<<<Total>>>>>>>>>>>>>>");
            for(Pair<String, String> key : totalPipelineStats.keySet()) {
                out.println("(" + key.getFirst() + ", " + key.getSecond() + ") => " + totalPipelineStats.get(key).toString());
            }

            // output everything
            out.println("<<<<<<<<<<<<<<<Pipelinable>>>>>>>>>>>>>>");
            for(Pair<String, String> key : pipelineStats.keySet()) {
                out.println("(" + key.getFirst() + ", " + key.getSecond() + ") => " + pipelineStats.get(key).toString());
            }

        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

        t = System.currentTimeMillis() - t;
        if (!verbose) {
	    turnOnOutput();
            System.out.println("Query finished. Execution took " + t + " milliseconds.");
	    turnOffOutput();
        }

        // since everything in outFiles will be destroyed, don't remove the final stuff
         outFiles.removeAll(finalFilesSet);

        // obtain info on the final relations.
        for (String s : queryPlan.getFinalRelations()) {

            // get the path...
            String sPath = queryPlan.getFinalPath(s);

            // try getting the collected statistics...
            HDFSTableStats coll = new HDFSTableStats();
            try {
                coll.load(sPath);
            } catch (Exception e) {
                System.out.println("Failed to obtain statistics for table " + s);
            }

            ArrayList<simsql.compiler.Attribute> newAtts = new ArrayList<simsql.compiler.Attribute>();

            int ix = 0;
            for (String a : queryPlan.getFinalAttributes(s)) {
                // newAtts.add(new simsql.compiler.Attribute
                // 	    (a,
                // 	     simsql.compiler.Attribute.getStringType(queryPlan.getFinalType(a)),
                // 	     s,
                // 	     queryPlan.getFinalRandomAttribute(a)));
                newAtts.add(new simsql.compiler.Attribute
                        (a,
                                TypeMachine.fromString(queryPlan.getFinalType(a)),         // TO-DO
                                s));

                // set the uniques for that attribute.
                if (coll.numAttributes() > ix) {
                    newAtts.get(ix).setUniqueValue(coll.numUniques(ix));
                }

                ix++;
            }

            res.addFinalRelation(s, sPath, newAtts, coll.numTuples());
        }

        if (outputFile != null) {
            outFiles.remove(outputFile);
            res.setOutputRelation(outputFile, new ArrayList<>(Arrays.asList(outputAtts)));
        }

        // unlink those files...
        for (String ff : outFiles) {
            res.addFileToUnlink(ff);
        }

        if (verbose) {
            System.out.println("Finals : " + res.getFinalRelations() + "\n");
            System.out.println("Files to unlink : " + res.getFilesToUnlink() + "\n");
        }

        // start printing output again
        if (!verbose) {
            turnOnOutput();
            System.out.println("");
        }
        return res;
    }

    public RuntimeParameter getRuntimeParameters() {
        return params;
    }

    public static void main(String args[]) {

        if (args.length != 1) {
            System.exit(-1);
        }

        String fileToRun = args[0];
        try {
            // read the DataFlow input file
            File file = new File(fileToRun);
            FileReader converter = new FileReader(file);
            int fileSize = (int) file.length();
            char[] buf = new char[fileSize + 10];
            fileSize = converter.read(buf);
            String toParse = String.valueOf(buf, 0, fileSize);

            DataFlowQuery qq = new DataFlowQuery();
            qq.setQueryStr(toParse);
            MyPhysicalDatabase mdb = new MyPhysicalDatabase();
            System.out.println(mdb.getFromFile(new File(".simsql", "physicalDBdescription")));
            HadoopRuntime rt = new HadoopRuntime(mdb);
            System.out.println(rt.params.getFromFile(new File(".simsql", "runtimes")));
            HadoopResult res = rt.run(qq);

            System.out.println(res.isError());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
