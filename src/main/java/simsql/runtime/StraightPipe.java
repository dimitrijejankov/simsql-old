

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

import java.io.File;
import java.util.ArrayList;

// this class implements a very simple PipeNode that just outputs all of the data that it takes in
class StraightPipe extends APipeNode {

	// the input file that this guy accepts
	private String fName;

	// this is just a dummy operator
	private PipelinedOperator myOp;

	// the type code that this guy accepts
	private short typeCode;

	public StraightPipe (String fNameIn, short typeCodeIn) {
		fName = fNameIn;
		typeCode = typeCodeIn;	
	}

	public String createJavaCode (String className, String path) {
		return null;
	}

	public String getInputRecordClasses () {
		return null; 
	}

	// this gets the file name for the file that can possibly be pipelined into the PipeNode
	public String getPipelinedInputFile () {
		return fName;	
	}

	// this gets a list of files that the PipeNode depends upon, but that cannot be pipelined into the node
	public String [] getNonPipelinedInputFileNames () {
		return new String [0];
	}

	// this gets the name of the output file (though it might be the case that this file is never materialized)
	public String getOutputFileName () {
		return fName;
	}

	public void getReadyToRun (int whichPartition, int numPartitions) {
		myOp = new PipelinedOperator (typeCode);
	}

	// this gets the approximate amount of memory that the PipeNode will take to execute (in MB)
	public long getRAMToRun () {
		return 0;
	}

	// this gets a reference to the PipelinedOperator that is created by GetReadyToRun
	public PipelinedOperator getOperator () {
		return myOp;
	}

	public Record cleanup () {
		return null;
	}

	public void reEvaluateWhichFileIsPipelined () {}
}
