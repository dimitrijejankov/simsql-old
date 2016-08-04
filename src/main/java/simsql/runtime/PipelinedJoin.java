

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

import java.util.Map;
import java.util.Set;
import java.util.HashMap;

class PipelinedJoin extends APipeNode {

	// the actual join we are running
	private JoinOp joinToRun;

	// used implement "getInputRecordClasses"
	private String inputRecordClass;

	// this remembers the name of the class that we created
	private String className;
	
	// remember the various input and output files
	private String outputFile;
	private String leftInputFile; // this is the one that is materialized (the small one)
	private String rightInputFile;  // this is the one that is pipelined (the big one)

	// this tells us that we need to reverse the left and the right in the macro replacements
	private boolean mustReverse;

	public String getInputRecordClasses () {
		return inputRecordClass;
	}

	public long getRAMToRun () {
		return joinToRun.numMegsToPipeline ();
        }

	// ask the join for any additional sort atts that are induced
	public Set <String> findPipelinedSortAtts (Set <Set <String>> currentAtts) {
		return joinToRun.findPipelinedSortAtts (rightInputFile, currentAtts);
	}

	public String getPipelinedInputFile () {
		return rightInputFile;
	}

	public String [] getNonPipelinedInputFileNames () {
		String [] retVal = new String [1];
		retVal[0] = leftInputFile;
		return retVal;
	}

	public String getOutputFileName () {
		return outputFile;
	}

	public void getReadyToRun (int whichPartition, int numPartitions) {
		getReadyToRun (className + ".class");
	}

        public void reEvaluateWhichFileIsPipelined () {
		leftInputFile = joinToRun.getSmallInput ();
		rightInputFile = joinToRun.getLargeInput ();
		mustReverse = joinToRun.needToReverseInputs ();
	}

	public PipelinedJoin (JoinOp useMe) {
		joinToRun = useMe;	
		outputFile = useMe.getOutput ();
		leftInputFile = useMe.getSmallInput ();
		rightInputFile = useMe.getLargeInput ();
		mustReverse = useMe.needToReverseInputs ();
	}

	public String createJavaCode (String classNameIn, String workDirectory) {

		className = classNameIn;

		// first, get the macros we are gonna use
		Map <String, String> macros = joinToRun.buildMacroReplacements ();
		macros.put ("pipeName", className);
		macros.put ("smallRelName", leftInputFile);

		// swap all occurrences of "left" and "right" in the list of macros, if needed
		if (mustReverse) {
			Map <String, String> newMap = new HashMap <String, String> ();
			for (Map.Entry <String, String> m : macros.entrySet ()) {
				String key = m.getKey ();
				String newKey = key.replace ("left", "TEMP");
				newKey = newKey.replace ("right", "left");
				newKey = newKey.replace ("TEMP", "right");
				newMap.put (newKey, m.getValue ());
			}
			macros = newMap;
			macros.put ("swappedLeftAndRight", "true");
		} else {
			macros.put ("swappedLeftAndRight", "false");
		}

		// do the macro replacement on the file
		MacroReplacer myReplacer = new MacroReplacer ("PipelinedJoinOperator.javat", 
			workDirectory + "/simsql/runtime/" + className + ".java", macros);

		// remember the name of the input record class
		inputRecordClass = className + "Input.class";	
		
		// and kill our relational op (so we do not serialize it)
		joinToRun = null;		

		// outta here!
		return "/simsql/runtime/" + className + ".java";
	}	
}
