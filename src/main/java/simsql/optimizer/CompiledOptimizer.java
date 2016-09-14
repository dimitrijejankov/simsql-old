

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


package simsql.optimizer;
import simsql.shell.Optimizer;
import simsql.shell.query_processor.FileOutput;
import simsql.shell.query_processor.SimSQLCompiledQuery;
import simsql.shell.RuntimeParameter;
import simsql.runtime.ExampleRuntimeParameter;

import java.io.*;
import java.nio.file.Files;
import java.math.BigDecimal;


/**
 * This version of the optimizer uses the compiled version of the optimizer.
 *
 * @author Luis
 */
public class CompiledOptimizer implements Optimizer <SimSQLCompiledQuery, SimSQLOptimizedQuery> {

  // location of the optimizer's temp executable.
  File eFile;

  /** Constructor -- extracts the optimizer for the first time. */
  public CompiledOptimizer() {
    eFile = null;
    try {
	    extractMe();
    } catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException("Failed to write out optimizer! " + e);
    }
  }

  private String extractMe() throws IOException {

    // do we have it?
    if (eFile == null || !eFile.exists()) {	    

	    File fDir = Files.createTempDirectory(null).toFile();
	    fDir.deleteOnExit();
	    eFile = new File(fDir, "optimizer");

	    // open the jar                                                                                                                                                    
 	    InputStream fis = CompiledOptimizer.class.getResourceAsStream("prolog/optimizer.pro");

	    // open the output stream                                                                                                                                          
 	    FileOutputStream fos = new FileOutputStream(eFile);

	    // write out                                                                                                                                                       
 	    while (fis.available() > 0) {
        fos.write(fis.read());
	    }

	    // close these files.                                                                                                                                              
 	    fis.close();
	    fos.close();

	    eFile.setExecutable(true);
    }

    return eFile.getAbsolutePath();
  }

  public SimSQLOptimizedQuery optimize (SimSQLCompiledQuery planToOptimize, RuntimeParameter param1) {

    // get the params
    ExampleRuntimeParameter param = (ExampleRuntimeParameter)param1;
    boolean DEBUG = param.getDebug();
    int OPT_MAX_ITERATIONS = param.getOptIterations();
    int OPT_MAX_PLANS = param.getOptPlans();


    SimSQLOptimizedQuery tempQueryFile = new SimSQLOptimizedQuery (".pl", planToOptimize);

    // write out the optimizer...
    String optFile = null;
    try {
	    optFile = extractMe();
    } catch  (Exception e) {
	    tempQueryFile.setError("There was an error while executing the optimizer...");
	    return tempQueryFile;
    }

    System.out.println("Invoking the optimizer...");

    // time to call it.
    try {
	    Process p = Runtime.getRuntime().exec(new String[]{optFile, "optimize", planToOptimize.getFName(), "" + OPT_MAX_ITERATIONS, "" + OPT_MAX_PLANS, tempQueryFile.getFName()});

	    // wait for it
	    int ret = p.waitFor();

	    if (DEBUG) {
        System.out.println("Optimizer DONE");
	    }
	    if (ret != 0) {
        tempQueryFile.setError("There was an error while executing the optimizer! (1)");		
	    }
    } catch (Exception e) {
	    tempQueryFile.setError("There was an error while executing the optimizer! (2)");
    }

    return tempQueryFile;

  }

  /** 
   *  Computes the cost of a given plan after it has been
   *  optimized. Returns -1 if there was an error. 
   */
  public BigDecimal computeCost(SimSQLCompiledQuery planToCost, RuntimeParameter param1) {
    // get the params
    ExampleRuntimeParameter param = (ExampleRuntimeParameter)param1;
    boolean DEBUG = param.getDebug();
    int OPT_MAX_ITERATIONS = param.getOptIterations();
    int OPT_MAX_PLANS = param.getOptPlans();

    FileOutput f = new FileOutput(".cost");

    // write out the optimizer...
    String optFile = null;
    try {
	    optFile = extractMe();
    } catch  (Exception e) {
      return new BigDecimal(-1.0);
    }


    if (DEBUG) {
      System.out.println("Invoking the optimizer to cost...");
    }

    // time to call it.

    try {
	    Process p = Runtime.getRuntime().exec(new String[]{optFile, "computeCost", planToCost.getFName(), "" + OPT_MAX_ITERATIONS, "" + OPT_MAX_PLANS, f.getFName()});

	    // wait for it
	    int ret = p.waitFor();

      if (DEBUG) {
        System.out.println("Optimizer DONE");
      }
	    if (ret != 0) {
        return new BigDecimal(-1.0);
	    }
    } catch (Exception e) {
      return new BigDecimal(-1.0);
    }

    // at this point, we can read the file.
    try {
      BufferedReader br = new BufferedReader(new FileReader(f.getFName()));
      String line = br.readLine();
      br.close();

      return new BigDecimal(line);
    } catch (Exception e) {
      return new BigDecimal(-1.0);
    }
  }
}
