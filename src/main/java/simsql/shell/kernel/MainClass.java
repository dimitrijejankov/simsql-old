

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


package simsql.shell.kernel;

import simsql.runtime.HadoopResult;
import simsql.code_generator.DataFlowQuery;
import simsql.optimizer.SimSQLOptimizedQuery;
import simsql.shell.query_processor.MCMCQueryProcessor;
import simsql.shell.query_processor.SimSQLCompiledQuery;


// this is where it all happens!
public class MainClass {
  
  public static void main (String [] args) {
    MCMCQueryProcessor myQueryProcessor = new MCMCQueryProcessor ();
    
    SimSQLShell<SimSQLCompiledQuery, SimSQLOptimizedQuery, DataFlowQuery, HadoopResult, MCMCQueryProcessor> myShell =
      new SimSQLShell <SimSQLCompiledQuery, SimSQLOptimizedQuery, DataFlowQuery, HadoopResult, MCMCQueryProcessor> (myQueryProcessor);
    
    myShell.promptLoop ();
  }
}

