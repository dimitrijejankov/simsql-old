

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
import simsql.compiler.FileOperation;
import simsql.compiler.FrameOutput;
import simsql.compiler.Operator;
import simsql.compiler.PlanHelper;
import simsql.compiler.PlanStatistics;
import simsql.compiler.PreviousTable;
import simsql.compiler.CompilerProcessor;
import simsql.compiler.Projection;
import simsql.shell.Optimizer;
import simsql.shell.FileOutput;
import simsql.shell.SimSQLCompiledQuery;
import simsql.shell.RuntimeParameter;
import simsql.runtime.ExampleRuntimeParameter;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.jar.*;
import java.net.URL;
import java.security.CodeSource;


public class FoulaOptimizer implements Optimizer <SimSQLCompiledQuery, SimSQLOptimizedQuery> {
  
    // This helper method "gobbles" a stream -- i.e. eats its output until there is nothing 
    private void gobbleStream(InputStream toGobble) {
    
	final BufferedReader reader = new BufferedReader(new InputStreamReader(toGobble));
    
	// create a thread that gobbles the input.
	new Thread() {
      
	    @Override public void run() {
		String line;
		try {
          
		    // eat each line
		    while ((line = reader.readLine()) != null) {
			/* do nothing with the output */;
		    }
		} catch (Exception e) { 
		    e.printStackTrace ();
		}
        
		// flush and close those streams.
		try {
		    reader.close();
		} catch (IOException e) {
		    e.printStackTrace ();
		}
	    }
	}.start();
    }
  
    // this private method is used to extract the Prolog source from the jar file
    private void extractFromJar (String jarPath, String whichFile) {
    
	try {
	    InputStream is;

	    // first, make sure that we have a jar... if not, just copy the thing over
	    if (!jarPath.endsWith (".jar")) {
		is = new BufferedInputStream (new FileInputStream (new File (jarPath, whichFile)));

		// in this case we have a jar
	    } else {
		JarFile myJar = new JarFile (jarPath);
		JarEntry myFile = myJar.getJarEntry (whichFile);
		is = myJar.getInputStream (myFile);
	    }

	    // do the copy
	    FileOutputStream fos = new FileOutputStream (new File (new File (whichFile).getName ()));
	    while (is.available() > 0) { 
		fos.write (is.read ());
	    }
	    fos.close();
	    is.close();
	} catch (Exception e) {
	    e.printStackTrace ();
	    System.out.println ("Error when trying to extract Prolog code from the jar"); 
	}
    }
  
    public SimSQLOptimizedQuery optimize (SimSQLCompiledQuery planToOptimize, RuntimeParameter param1) {

	ExampleRuntimeParameter param = (ExampleRuntimeParameter)param1;
	boolean DEBUG = param.getDebug();
	int OPT_MAX_ITERATIONS = param.getOptIterations();
	int OPT_MAX_PLANS = param.getOptPlans();

	if (DEBUG) {
	    System.out.println("It starts to optimize: " + planToOptimize.getFName());
	}
		 
	// first off, we extract all of the optimizer source from our jar...
	// get the .jar file that we are running
	String jarPath = null;
	try {
	    CodeSource src = FoulaOptimizer.class.getProtectionDomain().getCodeSource();
	    URL jar = src.getLocation();
	    jarPath = jar.toURI ().getPath ();
	} catch (Exception e) {
	    e.printStackTrace ();
	    throw new RuntimeException ("Got a problem when trying to figure out what jar was being used, to extract the Prolog code"); 
	}

	SimSQLOptimizedQuery tempQueryFile = new SimSQLOptimizedQuery (".pl", planToOptimize);
    
	/*
	// now, we extract all of the Prolog source for the preoptimizer 
	extractFromJar (jarPath, "simsql/optimizer/preoptimizer.pl");
    
	// this is the return file
    
	// run the Prolog code
	try {
	java.lang.Process p = java.lang.Runtime.getRuntime().exec(new String[]{"swipl", "-q"});      
	gobbleStream(p.getInputStream());
	gobbleStream(p.getErrorStream());
      
	DataOutputStream wri = new DataOutputStream(p.getOutputStream());
	wri.writeBytes("consult(['preoptimizer.pl', '" + planToOptimize.getFName () + "']).\n");
	wri.writeBytes("preoptimize(optimize, 'Q_preopt.pl').\n");
	wri.writeBytes("halt.\n");
	wri.flush();
	wri.close();
      
	p.waitFor();
      
	} catch (Exception e) {
	tempQueryFile.setError ("pre-optimizer exception: " + e);
	return tempQueryFile; 
	}
    
	// check if the preoptimizer produced the output file
	if (!(new File("Q_preopt.pl")).exists()) {
	tempQueryFile.setError ("for some reason, the output of the pre-optimizer was empty");
	return tempQueryFile; 
	}
	// delete the pre-optimizer code
	new File ("preoptimizer.pl").delete ();
	*/
    
	// extract the source code for the optimizer
	extractFromJar (jarPath, "simsql/optimizer/control_transformations.pl");
	extractFromJar (jarPath, "simsql/optimizer/graphoperations.pl");
	extractFromJar (jarPath, "simsql/optimizer/lib.pl");
	extractFromJar (jarPath, "simsql/optimizer/transformations.pl");
	extractFromJar (jarPath, "simsql/optimizer/plancost.pl");
	extractFromJar (jarPath, "simsql/optimizer/optimizer.pl");
        
	// now, call the optimizer and send the script.
	try {
      
	    java.lang.Process p2 = java.lang.Runtime.getRuntime().exec(new String[]{"swipl"});
      
	    gobbleStream(p2.getInputStream());
	    gobbleStream(p2.getErrorStream());
      
	    DataOutputStream wro = new DataOutputStream(p2.getOutputStream());
      
	    wro.writeBytes("consult(['control_transformations', 'graphoperations', 'lib', 'transformations', 'plancost', 'optimizer']).\n");
	    wro.writeBytes("consult('" + planToOptimize.getFName () + "').\n");
	    wro.writeBytes("optimize(optimize, " + OPT_MAX_ITERATIONS + ", " + OPT_MAX_PLANS + ", A,B,C,D, '" + tempQueryFile.getFName() + "').\n");
	    wro.writeBytes("halt.\n");
	    wro.flush();
      
	    // close stream
	    wro.close();
      
	    // and wait
	    p2.waitFor();
      
	} catch (Exception e) {
	    tempQueryFile.setError ("I got an exception while running the optimizer...");
	    return tempQueryFile; 
	}
    
	// check if the optimizer produced the desired output
	if (!(new File (tempQueryFile.getFName ())).exists()) {
	    tempQueryFile.setError ("The optimizer did not seem to produce any output...");
	    return tempQueryFile; 
	}
    
	// delete the output of the pre-optimizer
	new File("Q_preopt.pl").delete();
    
	// delete all of the optimizer source code
	new File("control_transformations.pl").delete ();
	new File("graphoperations.pl").delete ();
	new File("lib.pl").delete ();
	new File("transformations.pl").delete ();
	new File("plancost.pl").delete ();
	new File("optimizer.pl").delete ();
    
	if (DEBUG) {
	    System.out.println("It outputs the optimized result: " + tempQueryFile.getFName());
	}

	// and get outta here
	return tempQueryFile;
    
    }
  
  
    public void estimateStatistics(SimSQLCompiledQuery fileToEstimateStats,
				   String currentRandomTable, 
				   HashMap<String, Long> cutCostMap,
				   HashMap<String, Long> noCutCostMap,
				   ExampleRuntimeParameter params)
    {
	boolean DEBUG = params.getDebug();
	int OPT_MAX_ITERATIONS = params.getOptIterations();
	int OPT_MAX_PLANS = params.getOptPlans();

	if (DEBUG) {
	    System.out.println("It starts to estimiates the statistics of table: " + currentRandomTable);
	}

	// first off, we extract all of the optimizer source from our jar...
	// get the .jar file that we are running
	String jarPath = null;
	try {
	    CodeSource src = FoulaOptimizer.class.getProtectionDomain().getCodeSource();
	    URL jar = src.getLocation();
	    jarPath = jar.toURI().getPath();
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new RuntimeException(
				       "Got a problem when trying to figure out what jar was being used, to extract the Prolog code");
	}
		
	String queryFile = fileToEstimateStats.getFName();

	extractFromJar (jarPath, "simsql/optimizer/preoptimizer.pl");
	extractFromJar (jarPath, "simsql/optimizer/control_transformations.pl");
	extractFromJar (jarPath, "simsql/optimizer/graphoperations.pl");
	extractFromJar (jarPath, "simsql/optimizer/lib.pl");
	extractFromJar (jarPath, "simsql/optimizer/transformations.pl");
	extractFromJar (jarPath, "simsql/optimizer/plancost.pl");
	extractFromJar (jarPath, "simsql/optimizer/optimizer.pl");
	    
	/*
	 * 1. call the optimizer to estimate the statistics.
	 */
	try
	    {
		java.lang.Process p2 = Runtime.getRuntime().exec(new String[] {"swipl"});
	
		gobbleStream(p2.getInputStream());
		gobbleStream(p2.getErrorStream());
	
		DataOutputStream wro = new DataOutputStream(p2.getOutputStream());
		wro.writeBytes("consult(['control_transformations', 'graphoperations', 'lib', 'transformations', 'plancost', 'optimizer']).\n");
		wro.writeBytes("consult('" + queryFile + "').\n");
		wro.writeBytes("optimize(cut, " + OPT_MAX_ITERATIONS + ", " + OPT_MAX_PLANS + ",A,B,C,D, 'Q_cut.pl').\n");
		wro.flush();
	
		// close stream
		wro.close();
	
		// and wait
		p2.waitFor();
	    }
	catch(Exception e)
	    {
	    	e.printStackTrace();
	    	throw new RuntimeException("exception happens in estimating Statistics for finding the cut. ");
	    }

	// check if the optimizer produced the desired output
	if (!(new File("Q_cut_info.pl")).exists()) {
	    throw new RuntimeException("Could not execute the optimizer!");
	}

	String lines[] = FileOperation.getFile_content("Q_cut_info.pl");
	/*
	 * 2. Put the result statistics to both the cutCostMap and noCutCostMap.
	 */
	int start, end;
	double value;

	for (int i = 0; i < lines.length; i++) {
	    if (lines[i].startsWith("END")) {
		start = lines[i].indexOf(":");
		value = Double.parseDouble(lines[i].substring(start + 1,
							      lines[i].length()).trim());
		cutCostMap.put(currentRandomTable, (long) value);
	    } else {
		end = lines[i].indexOf(":");
		start = 0;
		String table = lines[i].substring(0, end);
		table = convertTableName(table);
		value = Double.parseDouble(lines[i].substring(end + 1,
							      lines[i].length()).trim());
		if (noCutCostMap.containsKey(table)) {
		    noCutCostMap.put(table, (long)-1);
		} else {
		    noCutCostMap.put(table, (long) value);
		}
	    }
	}
		
	new File("preoptimizer.pl").delete();
	new File("Q_cut_info.pl").delete();
	new File("control_transformations.pl").delete ();
	new File("graphoperations.pl").delete ();
	new File("lib.pl").delete ();
	new File("transformations.pl").delete ();
	new File("plancost.pl").delete ();
	new File("optimizer.pl").delete ();
	    
	if (DEBUG) {
	    System.out.println("It ends to estimiates the statistics of table: " + currentRandomTable);
	}
		
    }
	
    private PreviousTable parseResultTableMetaDataForEstimator(String line, Operator operator) {
	int start, end;
	start = line.indexOf("(");
	end = line.lastIndexOf(")");

	String info = line.substring(start + 1, end).trim();
	start = info.indexOf(",");
	String file = info.substring(0, start);
	info = info.substring(start + 1, info.length());

	start = 0;
	String tableName = file.substring(start, file.length());

	start = info.indexOf("[");
	end = info.indexOf("]");
	String attributeListStr = info.substring(start + 1, end);
	info = info.substring(end + 1, info.length());

	start = info.indexOf("[");
	end = info.indexOf("]");
	String attributeTypeListStr = info.substring(start + 1, end);
	info = info.substring(end + 1, info.length());

	start = info.indexOf("[");
	end = info.indexOf("]");
	String randAttriListStr = info.substring(start + 1, end);

	String[] attributes = parseToken(attributeListStr);
	String[] attributeTypes = parseToken(attributeTypeListStr);
	String[] randAttibutes = parseToken(randAttriListStr);

	ArrayList<String> attributeList = new ArrayList<String>();
	HashMap<String, String> attributeMap = new HashMap<String, String>();
	ArrayList<String> randamAttributeList = new ArrayList<String>();
		
	ArrayList<String> projectionList;
	if(operator instanceof Projection)
	    {
		projectionList = ((Projection) operator).getProjectedNameList();
	    }
	else
	    {
		projectionList = new ArrayList<String>();
	    }

	for (int i = 0; i < attributes.length; i++) {
	    //here we put a filter to help Foula.
	    if(projectionList.contains(attributes[i]))
		{
		    attributeList.add(attributes[i]);
		    attributeMap.put(attributes[i], attributeTypes[i]);
		}
	}

	for (int i = 0; i < randAttibutes.length; i++) {
	    if(projectionList.contains(attributes[i]))
		{
		    randamAttributeList.add(randAttibutes[i]);
		}
	}

	PreviousTable resultTable = new PreviousTable(file,
						      tableName, attributeList, attributeMap, randamAttributeList);
	return resultTable;
    }
	
    private String[] parseToken(String line) {
	ArrayList<String> list = new ArrayList<String>();
	StringTokenizer token = new StringTokenizer(line, ", ");
	while (token.hasMoreElements()) {
	    list.add((String) token.nextElement());
	}

	String result[] = new String[list.size()];
	list.toArray(result);
	return result;
    }
	
    private String reverseTableName(String name)
    {
	/*&
	 * transfer from xx[i] to xx_i.
	 */
	int start = name.lastIndexOf("[");
	String prefix = name.substring(0, start);
		
	String suffix = name.substring(start + 1, name.length()-1);
	return prefix + "_" + suffix;
    }
	
    public String convertTableName(String name)
    {
	/*&
	 * transfer from xx_i to xx[i].
	 */
	int start = name.lastIndexOf("_");
	String prefix = name.substring(0, start);
	String suffix = name.substring(start + 1, name.length());
		
	return prefix + "[" + suffix + "]";
    }


    public java.math.BigDecimal computeCost(SimSQLCompiledQuery q, RuntimeParameter p) {
      return new java.math.BigDecimal(-1.0);
    }
}
