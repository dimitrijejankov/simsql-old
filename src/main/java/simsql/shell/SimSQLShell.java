

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


package simsql.shell;

import simsql.compiler.IndexReplacer;
import simsql.compiler.Relation;
import simsql.runtime.Record;

import java.io.*;
import java.util.zip.*;
import java.util.Enumeration;

import jline.*;

class SimSQLShell<MyCompiledQuery extends CompiledQuery, MyOptimizedQuery extends OptimizedQuery, MyExecutableQuery extends ExecutableQuery, MyRuntimeOutput extends RuntimeOutput, MyQueryProcessor extends QueryProcessor<MyCompiledQuery, MyOptimizedQuery, MyExecutableQuery, MyRuntimeOutput>> {

    // this is all of the machinery used to parse, compile, and execute queries
    private MyQueryProcessor myQueryProcessor;
    private Compiler<MyCompiledQuery> myCompiler;
    private Optimizer<MyCompiledQuery, MyOptimizedQuery> myOptimizer;
    private CodeGenerator<MyOptimizedQuery, MyExecutableQuery> myTranslator;
    private Runtime<MyExecutableQuery, MyRuntimeOutput> myRuntime;

    public SimSQLShell(MyQueryProcessor myQueryProcessorIn) {

        // this is used to deal with unexpected shutdowns in at least a rudimentary fashion
        ShutdownHook<MyCompiledQuery, MyOptimizedQuery, MyExecutableQuery, MyRuntimeOutput, MyQueryProcessor> myHook =
                new ShutdownHook<MyCompiledQuery, MyOptimizedQuery, MyExecutableQuery, MyRuntimeOutput, MyQueryProcessor>();
        myHook.attachShutDownHook(myQueryProcessorIn);

        System.out.print("Welcome to SimSQL v0.5!\n\n");
        // has simsql been run from this location?
        if (new File(".simsql").exists()) {
            if (new File(".simsql").isFile()) {
                System.out
                        .println("Error: \".simsql\" exists, but it is not a directory.  How did this happen?");
                throw new RuntimeException("error during startup");
            }

            if (new File(new File(".simsql"), "simsql.lck").exists()) {
                System.out
                        .print("Error: there is a lock file \"simsql.lck\" in the .simsql directory.");
                System.out
                        .print(" Perhaps you are running two shells simultanously?  This is a bad idea.");
                System.out
                        .println(" If not, then delete the lock file and restart the shell.");
                throw new RuntimeException("error during startup");
            }

            // simsql has not been run from this location before
        } else {

            ConsoleReader cr = null;
            try {
                cr = new ConsoleReader();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not create a console for reading!", e);
            }

            System.out
                    .println("Could not find the configuration directory \".simsql\". Please choose one of the following options: ");
            System.out.println();
            System.out.println("\t[1] Create a new configuration.");
            System.out.println("\t[2] Restore an existing configuration.");
            System.out.println("\t[3] Quit.");
            System.out.println();
            System.out.print("> ");

            char answer = '3';
            try {
                answer = (char) cr.readCharacter(new char[]{'1', '2', '3'});
                System.out.println(answer);
            } catch (Exception e) {
                throw new RuntimeException("Could not read from the console!",
                        e);
            }

            // leave?
            if (answer == '3') {
                System.exit(0);
            }

            // create the SimSQL directory.
            try {
                File newDir = new File(".simsql");
                newDir.mkdirs();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not create SimSQL directory!", e);
            }

            // create the lock file
            try {
                File tempFile = new File(new File(".simsql"), "simsql.lck");
                tempFile.createNewFile();
                tempFile.deleteOnExit();
            } catch (Exception e) {
                System.out
                        .println("Could not create the lock file in the \".simsql\" directory.");
                throw new RuntimeException("error during startup");
            }

            // load a configuration?
            if (answer == '2') {

                ArgumentCompletor ac = new ArgumentCompletor(
                        new FileNameCompletor());
                ac.setStrict(false);
                cr.addCompletor(ac);

                // get the file name
                File configFile = null;
                while (configFile == null) {
                    try {
                        configFile = new File(
                                cr.readLine(
                                        "Please enter the name of the existing configuration file: ")
                                        .replaceFirst("~",
                                                System.getProperty("user.home"))
                                        .trim());

                        if (!configFile.exists()) {
                            System.out.println("File not found!");
                            configFile = null;
                        }

                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Could not read from the console!", e);
                    }
                }

                // open it
                ZipFile zip = null;
                try {
                    zip = new ZipFile(configFile);
                } catch (Exception e) {
                    throw new RuntimeException(
                            "Could not open the configuration file!", e);
                }

                // make sure it has all the files we need.
                if (zip.getEntry("catalog") == null
                        || zip.getEntry("physicalDBdescription") == null
                        || zip.getEntry("runtimes") == null) {

                    throw new RuntimeException(
                            "The file does not contain all the necessary configuration parameters!");
                }

                // now, extract everything.
                Enumeration ee = zip.entries();

                while (ee.hasMoreElements()) {
                    ZipEntry en = (ZipEntry) ee.nextElement();

                    try {
                        FileOutputStream fos = new FileOutputStream(new File(
                                ".simsql", en.getName()));
                        InputStream is = zip.getInputStream(en);

                        while (is.available() > 0) {
                            fos.write(is.read());
                        }

                        fos.close();
                        is.close();
                    } catch (Exception e) {
                        throw new RuntimeException(
                                "Could not extract configuration file!", e);
                    }
                }

                // get the restoration path from the comments.
                String restorePath = zip.getEntry("physicalDBdescription")
                        .getComment();

                // load the physical database
                myQueryProcessorIn.getPhysicalDatabase().getFromFile(
                        new File(".simsql", "physicalDBdescription"));

                // and restore it
                myQueryProcessorIn.getPhysicalDatabase().restoreFrom(
                        restorePath);

                // and close the zip file
                try {
                    zip.close();
                } catch (Exception e) {
                }
            }
        }

        // remember the query processor
        myQueryProcessor = myQueryProcessorIn;

        // get the compiler, optimizer, translator, and execution engine from
        // the query processor
        myCompiler = myQueryProcessor.getCompiler();
        myOptimizer = myQueryProcessor.getOptimizer();
        myTranslator = myQueryProcessor.getTranslator();
        myRuntime = myQueryProcessor.getRuntime();

        // now, we set up the catalog, the physical database, and the runtime
        // parameters

        // first, we see if the catalog exists. If it does not, then create
        // it...
        if (!myQueryProcessor.getCatalog().getFromFile(
                new File(".simsql", "catalog"))) {

            System.out.print("Creating a new catalog. ");
            myQueryProcessor.getCatalog().setUp(new File(".simsql", "catalog"));
            System.out.println("OK!");
            System.out.println();
        }

        // now we see if the runtime parameters exist. If they do not, then
        // create them...
        if (!myQueryProcessor.getRuntimeParameter().getFromFile(
                new File(".simsql", "runtimes"))) {

            ConsoleReader cr = null;
            try {
                cr = new ConsoleReader();
            } catch (Exception e) {
                throw new RuntimeException(
                        "Could not create a console for reading!", e);
            }

            System.out
                    .println("Creating a new set of runtime parameter settings. Please choose one of the following options: ");
            System.out.println();
            System.out
                    .println("\t[1] Set up these parameters (requires answering some questions).");
            System.out
                    .println("\t[2] Use the default factory settings (might not be optimal).");
            System.out.println("\t[3] Quit.");
            System.out.println();
            System.out.println("\t(These parameters can be changed later!)");
            System.out.println();
            System.out.print("> ");

            char answer = '3';
            try {
                answer = (char) cr.readCharacter(new char[]{'1', '2', '3'});
                System.out.println(answer);
            } catch (Exception e) {
                throw new RuntimeException("Could not read from the console!",
                        e);
            }

            // leave?
            if (answer == '3') {
                System.exit(0);
            }

            // questionnaire?
            if (answer == '1') {
                myQueryProcessor.getRuntimeParameter().setUp(
                        new File(".simsql", "runtimes"));
            }

            // defaults
            if (answer == '2') {
                myQueryProcessor.getRuntimeParameter().setUpDefaults(
                        new File(".simsql", "runtimes"));
            }

            System.out.print("Loading default regular functions");
            for (String stat : myQueryProcessor.getRuntimeParameter()
                    .getFunctionCTs()) {
                try {
                    //System.out.println("I am parsing "+stat);
                    myCompiler.parseString(stat).cleanUp();
                    System.out.print(".");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load function.\n"
                            + stat, e);
                }
            }
            System.out.println(" OK!");

            System.out.print("Loading default VG functions");
            for (String stat : myQueryProcessor.getRuntimeParameter()
                    .getVGFunctionCTs()) {
                try {
                    myCompiler.parseString(stat).cleanUp();
                    System.out.print(".");
                } catch (Exception e) {
                    throw new RuntimeException("Failed to load VG function.\n"
                            + stat, e);
                }
            }

            System.out.println(" OK!");
            System.out.println();

            myQueryProcessor.getRuntimeParameter().save();
            System.out.println();
        }

        // finally, get the physical file info to be used when
        // storing/retreiving data
        if (!myQueryProcessor.getPhysicalDatabase().getFromFile(
                new File(".simsql", "physicalDBdescription"))) {

            System.out.println("Creating a new physical database. ");
            myQueryProcessor.getPhysicalDatabase().setUp(
                    new File(".simsql", "physicalDBdescription"));
            System.out.println();

            // save it
            myQueryProcessor.getPhysicalDatabase().save();
        }
    }

    private String outputFile = null;

    public void promptLoop() {

        ConsoleReader br = null;
        try {
            br = new ConsoleReader();
        } catch (Exception e) {
            throw new RuntimeException("Failed to get a console reader.", e);
        }

        ArgumentCompletor ac = new ArgumentCompletor(new FileNameCompletor());
        ac.setStrict(false);
        br.addCompletor(ac);


        String qstr = null;

        while (true) {


            // prompt and read a line
            String line = null;
            try {
                line = br.readLine(qstr == null ? "SimSQL> " : "   > ");
            } catch (IOException e) {
                System.out.println("Cannot read from stdin!");
                return;
            }


            // if there is only whitespace here, then just try again
            if (line.trim().length() == 0)
                continue;

            // determine the command
            try {
                qstr = parseLine(line, qstr);
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println();
            }

            if (leave) {

                // we quit.
                break;
            }
        }

        return;
    }

    public String parseLine(String line, String queryStr) {

        // are we in the middle of a query?
        if (queryStr == null) {

            // no? try parsing a command.
            if (parseCommand(line)) {

                // true means it wasn't a command...
                // could be a query.
                queryStr = "";
            }
        }

        // amidst a query...
        if (queryStr != null) {

            // add our line.
            queryStr += "\n" + line;

            // does it end with ';' ?
            if (queryStr.trim().endsWith(";")) {

                // if so, we have something to run.

                // the cleaner is responsible for cleaning up the mess in the current directory
                // that the query execution will create... all of the mess is saved to "simsql_exec_files"
                Cleaner myCleaner = new Cleaner("simsql_exec_files");

                String myQuery = queryStr;
                queryStr = null;

                MyCompiledQuery myCompiledQuery = null;
                try {
                    // parse it.
                    IndexReplacer myReplacer = new IndexReplacer(myQuery, myQueryProcessor.getCatalog());
                    myQuery = myReplacer.getOutput();
                    myCompiledQuery = myCompiler.parseString(myQuery);
                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println();
                    myCleaner.cleanUp();
                    return null;
                }

                if (myCompiledQuery == null) {
                    myCleaner.cleanUp();
                    return null;
                }

                // make sure that the parsed query was valid
                if (myCompiledQuery.isError() != null) {
                    System.out.println("Error parsing query:"
                            + myCompiledQuery.isError());
                    myCompiledQuery.cleanUp();
                    myCleaner.cleanUp();
                    return null;
                }

                // make sure that it's a SELECT
                if (myCompiledQuery.isEmpty()) {
                    myCompiledQuery.cleanUp();
                    myCleaner.cleanUp();
                    return null;
                }

                // and execute it
                try {
                    execute(myCompiledQuery, outputFile);
                    outputFile = null;
                } catch (Exception e) {
                    System.out.println("ERROR!");
                    e.printStackTrace();
                }
                myCleaner.cleanUp();
            }
        }

        return queryStr;
    }

    private boolean leave = false;

    public boolean parseCommand(String line) {

        // simply ignore lines that start with '--' those are comments.
        if (line.trim().length() == 0 || line.startsWith("--"))
            return false;

        String[] splits = line.split("\\s+");

        // check the splits...
        if (splits == null || splits.length == 0)
            return false;

        // go with 1-value commands
        String p1 = splits[0].replaceAll(";", "").trim();

        // only QUIT/EXIT.
        if (p1.equalsIgnoreCase("QUIT") || p1.equalsIgnoreCase("EXIT")) {
            leave = true;
            return false;
        }

        // and HELP
        if (p1.equalsIgnoreCase("HELP")) {
            help();
            return false;
        }

        // no more 1-value commands.
        if (splits.length < 2) {
            return true;
        }

        // now with 2-value commands
        String p2 = splits[1].replaceAll(";", "").trim();

        // SHOW PARAMS
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("PARAMS")) {
            showParams();
            return false;
        }

        // SHOW FUNCTIONS
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("FUNCTIONS")) {
            showFunctions();
            return false;
        }


        // SHOW VGFUNCTIONS
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("VGFUNCTIONS")) {
            showVGFunctions();
            return false;
        }

        // SHOW TABLES
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("TABLES")) {
            showTables();
            return false;
        }

        // SHOW VIEWS
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("VIEWS")) {
            showViews();
            return false;
        }

        // SHOW PHYSDB
        if (p1.equalsIgnoreCase("SHOW") && p2.equalsIgnoreCase("PHYSDB")) {
            showPhysicalDB();
            return false;
        }

        // DISPLAY x
        if (p1.equalsIgnoreCase("DISPLAY")) {
            IndexReplacer myReplacer = new IndexReplacer(p2, myQueryProcessor.getCatalog());
            p2 = myReplacer.getOutput();
            display(p2);
            return false;
        }

        // OUTPUT x
        if (p1.equalsIgnoreCase("OUTPUT")) {
            output(p2);
            return false;
        }

        // RUN x
        if (p1.equalsIgnoreCase("RUN")) {
            run(p2);
            return false;
        }

        // TABLE x
        if (p1.equalsIgnoreCase("TABLE")) {
            IndexReplacer myReplacer = new IndexReplacer(p2, myQueryProcessor.getCatalog());
            p2 = myReplacer.getOutput();
            table(p2);
            return false;
        }

        // done with 2-splits...
        if (splits.length < 3) {
            return true;
        }

        String p3 = splits[2].replaceAll(";", "").trim();

        // SET p v
        if (p1.equalsIgnoreCase("SET")) {
            setParam(p2, p3);
            return false;
        }

        // BACKUP f d
        if (p1.equalsIgnoreCase("BACKUP")) {
            backup(p2, p3);
            return false;
        }

        // at this point, the only possibilities are LOAD or a query.
        if (splits.length < 4) {
            return true;
        }

        String p4 = splits[3].replaceAll(";", "").trim();

        if (p1.equalsIgnoreCase("LOAD") && p3.equalsIgnoreCase("FROM")) {

            // sorted by?!
            String p7 = null;
            if (splits.length >= 7) {

                String p5 = splits[4].replaceAll(";", "").trim();
                String p6 = splits[5].replaceAll(";", "").trim();

                if (p5.equalsIgnoreCase("SORT") && p6.equalsIgnoreCase("BY")) {
                    p7 = splits[6].replaceAll(";", "").trim();
                }
            }

            load(p2, p4, p7);
            return false;
        }

        // everything else must be a query...
        return true;
    }

    public void help() {

        // header
        System.out.format("%-25s | %-50s%n", "command", "description");
        System.out.format("%s",
                new String(new char[75]).replace("\0", "-"));
        System.out.format("%n");

        // descriptions
        System.out.format("%-25s | %-50s%n", "SHOW PARAMS",
                "Displays the current values of runtime parameters");

        System.out.format("%-25s | %-50s%n", "SET param value",
                "Sets runtime parameter <param> to <value>");

        System.out.format("%-25s | %-50s%n", "SHOW FUNCTIONS",
                "Lists all the available scalar functions");

        System.out.format("%-25s | %-50s%n", "SHOW VGFUNCTIONS",
                "Lists all the available VG functions");

        System.out.format("%-25s | %-50s%n", "SHOW TABLES",
                "Lists all the existing database tables");

        System.out.format("%-25s | %-50s%n", "SHOW VIEWS",
                "Lists all the existing database views");

        System.out.format("%-25s | %-50s%n", "SHOW PHYSDB",
                "Show information on the physical database");

        System.out.format("%-25s | %-50s%n", "DISPLAY relation",
                "Prints the contents of table 'relation'");

        System.out.format("%-25s | %-50s%n", "TABLE relation",
                "Prints attribute information on table 'relation'");

        System.out
                .format("%-25s | %-50s%n", "LOAD r FROM f [SORT BY att]",
                        "Bulk loads table 'r' with the records from local file 'f'");

        System.out
                .format("%-25s | %-50s%n", "OUTPUT file",
                        "Writes the output of the next query to local file 'file'");

        System.out.format("%-25s | %-50s%n", "RUN file",
                "Executes the commands in 'file'");
        System.out
                .format("%-25s | %-50s%n",
                        "BACKUP file dir",
                        "Exports the catalog and runtime information to local 'file' and the data to 'dir'");
        System.out.format("%-25s | %-50s%n", "QUIT",
                "Closes the SimSQL shell");
        System.out.format("%n");

    }

    public void showParams() {
        myQueryProcessor.getRuntimeParameter().listParams();
    }

    public void showVGFunctions() {
        myQueryProcessor.getRuntimeParameter().listVGFunctions();
    }

    public void showFunctions() {
        myQueryProcessor.getRuntimeParameter().listFunctions();
    }

    public void showTables() {
        myQueryProcessor.getCatalog().listTables();
    }

    public void showViews() {
        myQueryProcessor.getCatalog().listViews();
    }

    public void showPhysicalDB() {
        System.out.println(myQueryProcessor.getPhysicalDatabase());
    }

    public void display(String relation) {
        Relation myRel = myQueryProcessor.getCatalog().getRelation(relation);

        // if we didn't get a valid relation back, give an error
        if (myRel == null) {
            System.out.println("Could not find relation " + relation);
            return;
        }

        // are we printing to a file or to the console
        if (outputFile == null) {

            // to the console
            myQueryProcessor.getPhysicalDatabase().printRelation(myRel);
        } else {

            // to a file
            try {
                System.out.print("Writing to file " + outputFile
                        + " ... ");

                myQueryProcessor.getPhysicalDatabase().saveRelation(myRel, outputFile);

                System.out.println("OK!");

                // set back to NULL
                outputFile = null;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void output(String output) {

        File ff = new File(output);

        if (ff.isDirectory()) {
            System.out.format("The path " + outputFile
                    + " is a directory!");

            outputFile = null;
            return;
        } else if (ff.exists()) {
            System.out
                    .format("The file "
                            + outputFile
                            + " already exists. It will be overwritten unless OUTPUT is called again.");
        }

        outputFile = output;
    }

    private boolean runningFile = false;

    public void run(String fileIn) {

        // get the home...
        String file = fileIn.replaceFirst("~", System.getProperty("user.home"));

        if (runningFile) {
            System.out.println("Sorry! no nested RUN commands!");
            return;
        }

        runningFile = true;

        // parse that file.
        try {

            BufferedReader br = new BufferedReader(new FileReader(file));

            String line = "";
            while (true) {
                String ll = br.readLine();
                if (ll == null) {
                    break;
                }

                // line = parseLine(ll, line);
                if (parseCommand(ll)) {
                    line += "\n" + ll;
                }
            }

            br.close();

            // attempt to execute the big thing.
            parseLine("", line);

        } catch (Exception e) {

            System.out.println("Could not read file " + file);
            e.printStackTrace();
            runningFile = false;
            return;
        }

        runningFile = false;
    }

    public void table(String table) {

        Relation myRel = myQueryProcessor.getCatalog().getRelation(table);

        // if we didn't get a valid relation back, give an error
        if (myRel == null) {
            System.out.println("Could not find relation " + table);
            return;
        }

        // pretty printing
        System.out.println(myRel.formatString());
    }


    public void setParam(String param, String value) {

        // set the param
        myQueryProcessor.getRuntimeParameter().setParam(param,
                value);

        // and save the params
        myQueryProcessor.getRuntimeParameter().save();
    }

    public void backup(String exportFileIn, String exportPath) {

        // check if the file exists.
        String exportFile = exportFileIn.replaceFirst("~", System.getProperty("user.home"));
        File ff = new File(exportFile);

        boolean isOK = true;
        if (ff.isDirectory()) {
            System.out.format("The path " + exportFile
                    + " is a directory!");

            return;

        } else if (ff.exists()) {
            System.out
                    .format("The file "
                            + exportFile
                            + " already exists! ");

            return;
        }

        try {

            // save the phys. database and runtime parameters first
            myQueryProcessor.getPhysicalDatabase().save();
            myQueryProcessor.getRuntimeParameter().save();

            // create a new zip file
            ZipOutputStream ous = new ZipOutputStream(
                    new FileOutputStream(ff));

            for (File inF : new File(".simsql").listFiles()) {

                // read each file in the .simsql directory
                FileInputStream fis = new FileInputStream(inF);

                // compress in...
                ZipEntry zen = new ZipEntry(inF.getName());
                zen.setComment(exportPath);
                ous.putNextEntry(zen);
                while (fis.available() > 0) {
                    ous.write(fis.read());
                }

                // close the entry.
                ous.closeEntry();

                // close the input file
                fis.close();
            }

            // close the output file
            ous.close();
        } catch (Exception e) {
            System.out
                    .println("Could not create configuration file "
                            + ff + "!");
            e.printStackTrace();
            System.out.println();
        }

        // now, back everything up.
        myQueryProcessor.getPhysicalDatabase().backupTo(exportPath);
    }

    public void load(String relation, String fileIn, String sortAtt) {

        String localFile = null;
        if (fileIn.startsWith("hdfs:")) {
            localFile = fileIn;
        } else {

            localFile = fileIn.replaceFirst("~", System.getProperty("user.home"));

            File fx = new File(localFile);
            if (!fx.exists()) {
                System.out.println("File " + fx + " not found!");
                return;
            }
        }

        // how do we load it? depends...
        Relation myRel = myQueryProcessor.getCatalog().getRelation(
                relation);
        if (myRel == null) {
            System.out
                    .println("Could not find the relation " + relation);
            return;
        }

        TableStatistics<Record> stats = null;
        try {
            stats = myQueryProcessor.getPhysicalDatabase().loadTable(myRel, localFile,
                    myQueryProcessor.getRuntimeParameter(), sortAtt);
        } catch (Exception e) {
            e.printStackTrace();
            System.out
                    .println("Error: IO problem when loading the file "
                            + localFile);
        }

        // update the stats
        myQueryProcessor.getCatalog().updateTableStatistics(myRel, stats);
    }


    private void execute(MyCompiledQuery myCompiledQuery, String outputFile) {

        // now we have parsed the query, give it back to the query processor for
        // post-processing
        myQueryProcessor.reset();
        myQueryProcessor.doneParsing(myCompiledQuery);

        // this repeatedly optimizes, translates, and runs the query
        while ((myCompiledQuery = myQueryProcessor.nextIteration()) != null) {

            // optimize the query
            MyOptimizedQuery myOptimizedQuery = myOptimizer.optimize(myCompiledQuery, myQueryProcessor.getRuntimeParameter());

            // see if we got an error
            if (myOptimizedQuery.isError() != null) {
                System.out.println("Error optimizing query: "
                        + myOptimizedQuery.isError());
                myOptimizedQuery.cleanUp();
                break;
            }

            // translate the query into an executable plan
            MyExecutableQuery myExecutableQuery = myTranslator.translate(myOptimizedQuery, myQueryProcessor.getRuntimeParameter());

            // see if we got an error
            if (myExecutableQuery.isError() != null) {
                System.out.println("Error translating the query to get an executable plan: "
                        + myExecutableQuery.isError());
                break;
            }

            // run the query
            MyRuntimeOutput myRuntimeOutput = myRuntime.run(myExecutableQuery);

            // see if we got an error
            if (myRuntimeOutput.isError() != null) {
                System.out.println("Error executing the query: " + myRuntimeOutput.isError());
                break;
            }

            // and give it back to the query processor for post-processing
            myQueryProcessor.doneExecuting(myRuntimeOutput, outputFile);

            //myQueryProcessor.saveRequiredRelations();
        }

        // save the outputs so they can be queried
        myQueryProcessor.saveRequiredRelations();

    }
}
