

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



package simsql.shell;
import java.io.File;

/**
 * The QueryProcessor class is used by the system to process a query from beginning
 * to end.  To process a query using a QueryProcessor object myObj, the following should
 * be done:
 * 
 * 1) a) ask myObj for a parser, an optimizer, a translator, and a runtime
 *    b) ask myObj for i) the catalog (which stores semantic info about the system)
 *                    ii) the physical database info (which stores info about the tables physically present in the DB)
                     iii) the runtime parameters (which stores info about how to run queries)
         make sure these exist and are all set up properly
 * 2) call myObj.reset () to wipe any state from the QueryProcessor
 * 3) use the parser you got to get a CompiledQuery object
 * 4) give that CompiledQuery object back to myObj for post-processing by calling myObj.doneParsing ()
 * 5) start the next iteration: call myObj.nextIter () to get the next plan to process
 * 6) give the CompiledQuery that you get from myObj.nextIter () to the optimizer
 * 7) give the OptimizedQuery that you get from the optimizer to the translator
 * 8) give the ExecutbleQuery that you get from the translator to the runtime
 * 9) give the RuntimeOutput that you get from the runtime back to myObj via myObj.doneExecuting ();
 * 10) repeat from step 5 as long as myObj.nextIter () gives you another plan to process
 * 11) call myObj.close () to get it to write any unsaved info to disk
 *
 */

public interface QueryProcessor <MyCompiledQuery extends CompiledQuery, 
                          MyOptimizedQuery extends OptimizedQuery,
                          MyExecutableQuery extends ExecutableQuery, 
                          MyRuntimeOutput extends RuntimeOutput> {
  
  // this gets the system catalog
  Catalog getCatalog ();

  // this gets the system's physical database
  PhysicalDatabase <MyExecutableQuery> getPhysicalDatabase ();

  // this gets the system's set of runtime parameters
  RuntimeParameter getRuntimeParameter ();

  // this returns a parser for the shell to use
  Compiler <MyCompiledQuery> getCompiler ();
  
  // this returns an optimizer for the shell to use
  Optimizer <MyCompiledQuery, MyOptimizedQuery> getOptimizer ();
    
  // this returns a translator for the shell to use
  CodeGenerator <MyOptimizedQuery, MyExecutableQuery> getTranslator ();
  
  // this returns a runtime for the shell to use to actually execute the query
  Runtime <MyExecutableQuery, MyRuntimeOutput> getRuntime ();

  // this tells it to save its last output 
  void saveOutputsFromLastIter ();

  // this tells it to delete its last output 
  void deleteOutputsFromLastIter ();

  // this wipes any state out of the QueryProcessor, so it forgets about any query
  // that it is currently being used to process
  void reset ();
  
  // once the user of this object parses a plan, the plan should be given back to
  // the object for post-processing via a call to this method
  void doneParsing (MyCompiledQuery result);
  
  // this is called to obtain the query that should be processed in this iteration;
  // a null result means that we are totally done processing the query
  MyCompiledQuery nextIter ();
  
  // this is called when the query has been optimized, translated, and executed
  void doneExecuting (MyRuntimeOutput result, String outputFile);

  // this is called to get the current iteration number; a -1 is returned if no iterations are happening
  int getIteration ();

  // this is called to kill the runtime and attempt to do any reasonable cleanup... used in the case of a
  // control-C or an unexpected shutdown of some type
  void killRuntime ();
  
}


