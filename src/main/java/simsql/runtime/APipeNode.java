

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

import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Constructor;
import java.lang.Thread;
import java.util.Set;
import java.lang.ClassLoader;

// a node in the pipe network
abstract class APipeNode implements PipeNode {

	private PipelinedOperator myOp;

	protected void getReadyToRun (String classToLoad) {

		try {
			classToLoad = classToLoad.substring (0, classToLoad.length () - 6);
			Class <?> myClass = Class.forName ("simsql.runtime." + classToLoad);
			Constructor <?> myConstructor = myClass.getConstructor ();
			myOp = (PipelinedOperator) myConstructor.newInstance ();
		} catch (NoSuchMethodException e) {
			throw new RuntimeException ("I could not find a valid contructor for the pipelined operator");
		} catch (InstantiationException e) {
			throw new RuntimeException ("I could not create a pipelined operator (1)");
		} catch (IllegalAccessException e) {
			throw new RuntimeException ("I could not create a pipelined operator (2)");
		} catch (InvocationTargetException e) {
			throw new RuntimeException (e.getCause ());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException ("I could not find the class to run the pipelined operator: " + classToLoad);
		}
				
	}

	public PipelinedOperator getOperator () {
		return myOp;
	}

        public Record cleanup () {
		return myOp.cleanup (); 
        }

	// default is to return nothing
	public Set <String> findPipelinedSortAtts (Set <Set <String>> currentAtts) {
		return null;
	}
} 
