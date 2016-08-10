

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

                // if one guy comes after, then perhaps we can pipeline
                if (follows.size() == 1 && r1.isPipelineable((int) (follows.get(0).getRemainingMemForPipelinePerMapper
                        (getRuntimeParameters()))) && follows.get(0).acceptsPipelineable()) {
                    follows.get(0).suckUpNetworkOnInput(r1.getPipelinedVersion());

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
                try {
                    if (!verbose) {
                        turnOnOutput();
                        System.out.print(result.size() + " (running " + nextOp.getOperatorName() + " as Hadoop job...)\n\t");
                        turnOffOutput();
                    }

                    nextOp.run(getRuntimeParameters(), verbose);
                } catch (Exception e) {
                    if (!verbose)
                        turnOnOutput();
                    StringWriter sw = new StringWriter();
                    e.printStackTrace(new PrintWriter(sw));
                    res.setError("Unable to execute query!\n" + sw.toString());
                    return res;
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
                waitFiles = new HashSet<String>();
                for (RelOp r : result) {
                    r.reEvaluateWhichFilesArePipelined();
                    waitFiles.add(r.getOutput());
                }

                result = sortBySize(result);

                // and start over
                i = -1;
            }
        }

        t = System.currentTimeMillis() - t;
        if (verbose) {
            System.out.println("Query finished. Execution took " + t + " milliseconds.");
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
            res.setOutputRelation(outputFile, new ArrayList<String>(Arrays.asList(outputAtts)));
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
