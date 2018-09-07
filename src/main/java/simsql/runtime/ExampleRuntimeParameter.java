

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

import java.io.*;

import jline.*;

import java.util.*;
import java.util.jar.*;
import java.lang.reflect.Method;
import java.net.URL;

import org.apache.log4j.LogManager;
import org.apache.log4j.Level;

import java.lang.reflect.*;

import static simsql.runtime.ReflectedFunction.isScalarFunction;
import static simsql.runtime.ReflectedFunction.isUDFunction;

public class ExampleRuntimeParameter implements RuntimeParameter {

    // runtime parameter data
    private Integer numCPUs;
    private Integer memoryPerCPUInMB;
    private Integer numIterations;
    private Integer optIterations;
    private Integer optPlans;
    private Boolean debug;
    private Boolean speculativeExecution = true;
    private long defaultSpeculativeSize = 1024L*1024L*1024*6L;

    // this is where we save ourselves to
    private File whereILive;

    // We also have two files that store all of the compiled binary code for VG functions and "regular" functions.
    // The following structure tells us where in the first file each of the VG functions are located
    HashMap<String, FileInfoPair> vgFunctionLocations = null;

    // And the following structure tells us where in the second file each of the VG functions are located
    HashMap<String, FileInfoPair> regularFunctionLocations = null;

    // sets the logging level according to the value of 'debug'
    public void setLogging() {
        Level newLevel = debug ? Level.INFO : Level.ERROR;
        LogManager.getRootLogger().setLevel(newLevel);
    }

    public int getNumCPUs() {
        return numCPUs;
    }

    public int getMemoryPerCPUInMB() {
        return memoryPerCPUInMB;
    }

    public int getNumIterations() {
        return numIterations;
    }

    public boolean getDebug() {
        return debug;
    }

    public boolean getSpeculativeExecution() {
        return speculativeExecution;
    }

    public long getDefaultSpeculativeSize() {
        return defaultSpeculativeSize;
    }

    public int getOptIterations() {
        return optIterations;
    }

    public int getOptPlans() {
        return optPlans;
    }

    public void listParams() {

        // header
        System.out.format("%-25s | %-50s%n", "parameter name", "current value");
        System.out.format("%s", new String(new char[75]).replace("\0", "-"));
        System.out.format("%n");

        // parameters
        System.out.format("%-25s | %-50d%n", "numCPUs", numCPUs);
        System.out.format("%-25s | %-50d%n", "memoryPerCPUInMB", memoryPerCPUInMB);
        System.out.format("%-25s | %-50d%n", "numIterations", numIterations);
        System.out.format("%-25s | %-50d%n", "optIterations", optIterations);
        System.out.format("%-25s | %-50d%n", "optPlans", optPlans);
        System.out.format("%-25s | %-50s%n", "debug", debug.toString());
        System.out.format("%-25s | %-50s%n", "speculativeExecution", speculativeExecution.toString());
        System.out.format("%n");
    }

    public boolean setParam(String paramName, String paramValue) {

        // save the originals, in case of exception.
        Integer curNumCPUs = numCPUs;
        Integer curMemoryPerCPUInMB = memoryPerCPUInMB;
        Integer curNumIterations = numIterations;
        Integer curOptIterations = optIterations;
        Integer curOptPlans = optPlans;
        Boolean curDebug = debug;

        try {
            if (paramName.equalsIgnoreCase("numCPUs")) {
                numCPUs = Integer.parseInt(paramValue);
            } else if (paramName.equalsIgnoreCase("memoryPerCPUInMB")) {
                memoryPerCPUInMB = Integer.parseInt(paramValue);
            } else if (paramName.equalsIgnoreCase("numIterations")) {
                numIterations = Integer.parseInt(paramValue);
            } else if (paramName.equalsIgnoreCase("optIterations")) {
                optIterations = Integer.parseInt(paramValue);
            } else if (paramName.equalsIgnoreCase("optPlans")) {
                optPlans = Integer.parseInt(paramValue);
            } else if (paramName.equalsIgnoreCase("debug")) {
                debug = Boolean.parseBoolean(paramValue);
                setLogging();
            } else if (paramName.equalsIgnoreCase("speculativeExecution")) {
                speculativeExecution = Boolean.parseBoolean(paramValue);

                if(speculativeExecution) {
                    System.out.println("Speculative execution is an experimental feature and should be used with caution!");
                    System.out.println("It assumes that all the relations are of size " + defaultSpeculativeSize + " bytes.");
                }

            } else {
                System.out.format("Unknown parameter %s%n", paramName);
            }
        } catch (Exception e) {
            System.out.format("Could not set value for parameter %s%n", paramName);

            // restore the defaults.
            numCPUs = curNumCPUs;
            memoryPerCPUInMB = curMemoryPerCPUInMB;
            numIterations = curNumIterations;
            optIterations = curOptIterations;
            optPlans = curOptPlans;
            debug = curDebug;
            setLogging();

            return false;
        }

        return true;
    }

    public void listFunctions() {
        System.out.format("%-50s%n", "function name");
        System.out.format("%s", new String(new char[50]).replace("\0", "-"));
        System.out.format("%n");

        for (String s : regularFunctionLocations.keySet()) {
            System.out.format("%-50s%n", s);
        }

        System.out.format("%n");
    }

    public void listVGFunctions() {
        System.out.format("%-50s%n", "vg function name");
        System.out.format("%s", new String(new char[50]).replace("\0", "-"));
        System.out.format("%n");

        for (String s : vgFunctionLocations.keySet()) {
            System.out.format("%-50s%n", s);
        }

        System.out.format("%n");
    }


    private File extractTemporaryFile(InputStream in) {

        File fx = null;
        try {
            fx = File.createTempFile("simsql_", null);

            // NOTE: commented this because Linux tends to crash the JVM if we delete a .so file
            // that has been loaded previously.
            // fx.deleteOnExit();
            FileOutputStream fos = new FileOutputStream(fx);
            while (in.available() > 0) {
                fos.write(in.read());
            }
            in.close();
            fos.close();

        } catch (Exception e) {
            throw new RuntimeException("Failed to create temporary files.", e);
        }

        return fx;
    }

    // extracts all the file names in a given directory that conform to the filter,
    // making them temporary files.
    private File[] getAllFilesInDirectory(String directory, String filter) {

        ArrayList<File> outVals = new ArrayList<File>();

        // get the base directory
        try {
            URL rootDir = ExampleRuntimeParameter.class.getProtectionDomain().getCodeSource().getLocation();

            // is it a normal directory?
            if (rootDir != null && new File(rootDir.toURI()).isDirectory()) {

                // if so, just list all the files and apply the filter
                for (String f : new File(rootDir.toURI().getPath(), directory).list()) {
                    if (f.contains(filter)) {
                        outVals.add(extractTemporaryFile(ExampleRuntimeParameter.class.getResourceAsStream("/" + directory + "/" + f)));
                    }
                }
            }

            // is it a jar?
            else if (rootDir != null && rootDir.toURI().getPath().endsWith(".jar")) {

                // get it
                JarFile jar = new JarFile(rootDir.toURI().getPath());

                // and go through all the entries
                for (Enumeration<JarEntry> e = jar.entries(); e.hasMoreElements(); ) {
                    String eName = e.nextElement().getName();

                    // filter
                    if (eName.startsWith(directory) && eName.contains(filter)) {
                        outVals.add(extractTemporaryFile(ExampleRuntimeParameter.class.getResourceAsStream("/" + eName)));
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not extract temporary files.", e);
        }

        // return the outVals.
        return outVals.toArray(new File[0]);
    }

    // returns the CREATE statements for the default functions
    @SuppressWarnings("unchecked")
    public String[] getFunctionCTs() {

        ArrayList<String> ret = new ArrayList<String>();

        HashSet<File> files = new HashSet<File>();

        // the ud functions
        files.addAll(Arrays.asList(getAllFilesInDirectory("simsql/functions/ud", ".class")));

        // the scalar functions
        files.addAll(Arrays.asList(getAllFilesInDirectory("simsql/functions/scalar", ".class")));

        for (File s : files) {

            // read the file
            byte[] bytes = null;

            try {
                FileInputStream fis = new FileInputStream(s);
                bytes = new byte[fis.available()];
                int read = fis.read(bytes);
                fis.close();
            } catch (Exception e) {
                throw new RuntimeException("Could not load regular functions...", e);
            }

            // load the class from the bytes
            Class<Function> clazz = new ClassLoader(ExampleRuntimeParameter.class.getClassLoader()) {
                public Class<Function> loadFromBytes(byte[] bytes) {
                    Class<Function> cx = (Class<Function>) defineClass(null, bytes, 0, bytes.length);
                    resolveClass(cx);
                    return (cx);
                }
            }.loadFromBytes(bytes);

            // get an instance
            Function obj = null;
            try {
                obj = clazz.newInstance();
            } catch (Exception e) {
                throw new RuntimeException("Could not load regular function " + clazz.getName(), e);
            }

            // get its creation statement
            ret.add(obj.getCreateSQL(s.getAbsolutePath()));
        }

        return ret.toArray(new String[0]);
    }

    // returns the CREATE statements for the default VG functions
    public String[] getVGFunctionCTs() {

        ArrayList<String> ret = new ArrayList<String>();

        for (File s : getAllFilesInDirectory("simsql/functions", ".vg.so")) {
            VGFunction f = new VGFunction(s);
            ret.add(f.getCreateSQL(s.getAbsolutePath()));
        }

        return ret.toArray(new String[0]);
    }

    private boolean writeToFile(File whereToReadFrom, int location, int numBytes, File whereToWriteTo) {

        try {

            // get the source
            RandomAccessFile input = new RandomAccessFile(whereToReadFrom, "r");
            input.seek(location);

            // get the destination
            FileOutputStream dest = new FileOutputStream(whereToWriteTo);

            // this info will control the transfer
            int increment = 1024;
            byte[] b = new byte[increment];

            for (int bytesSoFar = 0; bytesSoFar < numBytes; bytesSoFar += increment) {

                // read in and write the next few bytes
                if (numBytes - bytesSoFar >= increment) {
                    input.read(b, 0, increment);
                    dest.write(b);

                } else {
                    input.read(b, 0, numBytes - bytesSoFar);
                    dest.write(b, 0, numBytes - bytesSoFar);
                }
            }

            // outta here!
            input.close();
            dest.close();

        } catch (Exception e) {
            System.out.println("error when extracting the code for " + whereToWriteTo.getPath());
            return false;
        }

        return true;
    }

    // this extracts a VG function from the database of VG functions, writing it to the location that is indicated
    public boolean writeVGFunctionToFile(String vgName, File whereToWrite) {

        File whereToReadFrom = new File(whereILive.getPath() + "_vgFunctions");

        if (vgFunctionLocations.get(vgName) == null) {
            System.out.println("Could not find the VG function " + vgName + " in the database.");
            return false;
        }

        int location = (int) vgFunctionLocations.get(vgName).getStart();
        int len = (int) vgFunctionLocations.get(vgName).getLen();

        // and extract!
        return writeToFile(whereToReadFrom, location, len, whereToWrite);
    }

    public void writeAllFunctionsToDirectory(String whereToWrite) {

        for (String ff : regularFunctionLocations.keySet()) {

            if(isScalarFunction(ff)) {
                writeFunctionToFile(ff, new File(whereToWrite + "/scalar", ff + ".class"));
            }
            else if(isUDFunction(ff)){
                writeFunctionToFile(ff, new File(whereToWrite + "/ud", ff + ".class"));
            }
            else {
                throw new RuntimeException(ff + " is not a Scalar nor a UD function!");
            }

        }
    }

    public boolean writeFunctionToFile(String funcName, File whereToWrite) {

        File whereToReadFrom = new File(whereILive.getPath() + "_functions");

        if (regularFunctionLocations.get(funcName) == null) {
            System.out.println("Could not find the function " + funcName + " in the database.");
            return false;
        }

        int location = (int) regularFunctionLocations.get(funcName).getStart();
        int len = (int) regularFunctionLocations.get(funcName).getLen();

        // and extract!
        return writeToFile(whereToReadFrom, location, len, whereToWrite);
    }

    private boolean loadFromFile(String nameOfObject, File whereToReadFrom, File outFile, HashMap<String, FileInfoPair> posMap) {

        try {

            // get the source
            FileInputStream fin = new FileInputStream(whereToReadFrom);

            // get the destination, and open it to write at the end
            posMap.put(nameOfObject, new FileInfoPair(outFile.length(), whereToReadFrom.length()));
            RandomAccessFile output = new RandomAccessFile(outFile, "rw");
            output.seek(outFile.length());

            // create a buffer
            byte[] b = new byte[1024];
            int noOfBytes = 0;

            // read bytes from source file and write to destination file
            while ((noOfBytes = fin.read(b)) != -1) {
                output.write(b, 0, noOfBytes);
            }

            // and close up the infile and the outfile
            fin.close();
            output.close();

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error when trying to load up the specified file");
            return false;
        }

        return true;
    }

    // this takes a VG function from a file and puts it into the database
    public boolean readVGFunctionFromFile(String vgName, File whereToReadFrom) {

        if (vgFunctionLocations == null) {
            System.out.println("The map telling me where I saved VG functions was null. Was the runtime param object initialized?");
            return false;
        }

        return loadFromFile(vgName, whereToReadFrom, new File(whereILive.getPath() + "_vgFunctions"), vgFunctionLocations);
    }

    public boolean readFunctionFromFile(String funcName, File whereToReadFrom) {

        if (regularFunctionLocations == null) {
            System.out.println("The map telling me where I saved reg functions was null. Was the runtime param object initialized?");
            return false;
        }

        return loadFromFile(funcName, whereToReadFrom, new File(whereILive.getPath() + "_functions"), regularFunctionLocations);
    }

    public void setUpDefaults(File here) {
        whereILive = here;
        vgFunctionLocations = new HashMap<String, FileInfoPair>();
        regularFunctionLocations = new HashMap<String, FileInfoPair>();

        // get the number of CPUs
        numCPUs = Runtime.getRuntime().availableProcessors();

        // get the total memory per CPUs
        memoryPerCPUInMB = (int) (Runtime.getRuntime().totalMemory() / (1024 * 1024));

        // default number of iterations
        numIterations = 1;

        // default number of optimizer iterations
        optIterations = 150;

        // default number of optimizer plans
        optPlans = 1;

        // default debugging level
        debug = false;
        setLogging();
    }

    public void setUp(File here) {
        setUpDefaults(here);

        // get a console for reading
        ConsoleReader cr = null;
        try {
            cr = new ConsoleReader();
        } catch (Exception e) {
            throw new RuntimeException("Could not create a console for reading!", e);
        }

        // get the number of CPUs
        boolean keepAsking = true;
        while (keepAsking) {
            try {
                String s = cr.readLine("[numCPUs]\t\t How many CPU cores are available for execution? ");
                keepAsking = !setParam("numCPUs", s);
            } catch (Exception e) {
                keepAsking = true;
            }
        }

        // get the amount of RAM.
        keepAsking = true;
        while (keepAsking) {
            try {
                String s = cr.readLine("[memoryPerCPUInMB]\t How many MBs of RAM are available per CPU core? ");
                keepAsking = !setParam("memoryPerCPUInMB", s);
            } catch (Exception e) {
                keepAsking = true;
            }
        }

        keepAsking = true;
        while (keepAsking) {
            try {
                String s = cr.readLine("[numIterations]\t\t How many possible worlds will be considered during simulation? ");
                keepAsking = !setParam("numIterations", s);
            } catch (Exception e) {
                keepAsking = true;
            }
        }

        keepAsking = true;
        while (keepAsking) {
            try {
                String s = cr.readLine("[speculativeExecution]\t\t Do you plan on using speculative execution? ");
                keepAsking = !setParam("speculativeExecution", s);
            } catch (Exception e) {
                keepAsking = true;
            }
        }
    }

    @SuppressWarnings("unchecked")
    public boolean getFromFile(File myFile) {

        whereILive = myFile;

        FileInputStream fis = null;
        ObjectInputStream in = null;
        try {
            fis = new FileInputStream(myFile);
            in = new ObjectInputStream(fis);
            numCPUs = (Integer) in.readObject();
            memoryPerCPUInMB = (Integer) in.readObject();
            numIterations = (Integer) in.readObject();
            optIterations = (Integer) in.readObject();
            optPlans = (Integer) in.readObject();
            debug = (Boolean) in.readObject();
            speculativeExecution = (Boolean) in.readObject();
            setLogging();

            vgFunctionLocations = (HashMap<String, FileInfoPair>) in.readObject();
            regularFunctionLocations = (HashMap<String, FileInfoPair>) in.readObject();
            in.close();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public void save() {
        FileOutputStream fos = null;
        ObjectOutputStream out = null;
        try {
            fos = new FileOutputStream(whereILive);
            out = new ObjectOutputStream(fos);
            out.writeObject(numCPUs);
            out.writeObject(memoryPerCPUInMB);
            out.writeObject(numIterations);
            out.writeObject(optIterations);
            out.writeObject(optPlans);
            out.writeObject(debug);
            out.writeObject(speculativeExecution);
            out.writeObject(vgFunctionLocations);
            out.writeObject(regularFunctionLocations);
            out.close();
        } catch (Exception e) {
            System.out.println("Got some sort of error writing runtime params to a file");
        }
    }
}
