

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

// this class is used to deal with an unexpected shutdown... there are a lot of race
// condintions in here, so it can potentially fail, but it is better than nothing...

import simsql.shell.*;

public class ShutdownHook <MyCompiledQuery extends CompiledQuery, MyOptimizedQuery extends OptimizedQuery,
	MyExecutableQuery extends ExecutableQuery, MyRuntimeOutput extends RuntimeOutput,
	MyQueryProcessor extends QueryProcessor<MyCompiledQuery, MyOptimizedQuery, MyExecutableQuery, MyRuntimeOutput>> {

	// this is the query processor
	MyQueryProcessor myQueryProcessor;

	// called to set up at least some rudimentary shutdown robustness
	public void attachShutDownHook(MyQueryProcessor processorIn) {
		
		myQueryProcessor = processorIn;

		java.lang.Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {

				// if we are currently running a simulation, then we attempt to save its state 
				if (myQueryProcessor.getIteration () != -1) { 
					myQueryProcessor.killRuntime ();
					System.out.println ("Killed on iteration " + (myQueryProcessor.getIteration () - 1) + ".");
					if (myQueryProcessor.getIteration () >= 2) {
						System.out.println ("Saving results of iteration " + (myQueryProcessor.getIteration () - 2) + ".");
						myQueryProcessor.saveRequiredRelations();
					}
				} 
			
				// save the catalog and the physical database
				myQueryProcessor.getCatalog().save();
				myQueryProcessor.getRuntimeParameter().save();
				myQueryProcessor.getPhysicalDatabase().save();
			}
		});
	}
}
