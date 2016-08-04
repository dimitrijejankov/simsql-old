

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
import java.util.Set;
import java.util.ArrayList;

interface PipeNode extends java.io.Serializable {

	// this creates a .java file containing a code (which extends the PipelinedOperator class)
	// that will actually execute the pipenode suring the MR job... returns the name of the java
        // code (or null if no code is created)
	String createJavaCode (String className, String path);

	// this gets a list of the names of any input record classes associated with the pipenode... this is done
	// so that the mapper can figure out how to deserialize records from disk into the pipenode
	String getInputRecordClasses ();

	// this gets the file name for the file that can possibly be pipelined into the PipeNode
	String getPipelinedInputFile ();

	// this gets a list of files that the PipeNode depends upon, but that cannot be pipelined into the node
	String [] getNonPipelinedInputFileNames ();

	// this gets the name of the output file (though it might be the case that this file is never materialized)
	String getOutputFileName ();

	// when running SimSQL, we have occasionally run into problems where the file to be pipelined is
	// tiny---this happens because the system will ask an operation whether it can pipeline itself before
	// all of the input files are materialized.  If the operation has multiple input files and the size of
	// one input is unknown but all of the others are small enough to be buffered in RAM, the operation 
	// will decide to pipeline itself, choosing the file with unknown size as input into the pipeline.  
	// This is bad if, when it is time to run the operation, that file turns out to be tiny so that its
	// processing cannot be parallelized.  If that happens, then you cannot ask various machines to run
	// parts of the computation accross the cluster and performance can be terrible.  
	//
	// To address this, right before an operation with pipelined input is going to be executed, we ask
	// all of the operations at the bottom of the pipe network to re-evlauate what input is going to be
	// pipelined.  Since at this point, all of the input files MUST be materialized, it gives us the
	// chance to choose to pipeline the larger file.  Note that this won't help if ALL of the input files
	// are tiny, but it will work in the case where there is at least one file that is big enough to be
	// pipelined, because it gives us one last chance to make it the big one.
	void reEvaluateWhichFileIsPipelined ();

	// there are two incarnations of a PipeNode... there is the incarnation of the PipeNode that is created
	// by the execution engine as it sets up a MapReduce operation, and then there is the incarnation of the
	// PipeNode that is reconstituted later on, as it is time for the MR operation to actually run.  By
	// default, the PipeNode it not executable.  Calling this method causes the PipeNode to create an
	// instance of its PipelinedOperator object, so that it can make itself run.
	//
	// Note that the two params tell us which partition we are running in (for example, which mapper) and
	// and the overall number of partitions.  This is useful in the case that we have to partition out some
	// work to each of the mappers/reducers that are running a particular pipelined operation
	void getReadyToRun (int whichPartition, int numPartitions);	

	// this allows us to ask whether the node induces any additional sorting atttributes... it takes
	// as input the current set of sorting attributes, and then sees if, after the operation, there
	// are any other ats that the output file is also sorted on (example: if A is aorted on att1, then
	// you pipeline it into a join with B on att1 = att2, the output will be sorted on both att1 and
	// att2)... this should return any additional sorting attributes that are induced
	Set <String> findPipelinedSortAtts (Set <Set <String>> currentAtts);

	// this gets the approximate amount of memory that the PipeNode will take to execute (in MB)
	long getRAMToRun ();

	// this gets a reference to the PipelinedOperator that is created by GetReadyToRun
	PipelinedOperator getOperator ();

        // get ready to die... return any records that need to be flushed out (useful, for example, when you
	// are pipelining into an aggregate and there are a bunch of records that you need to flush from a hash)
        // this is called repeatedly until we get a null, at which point we know that there are no more cleanup
	// records to be produced
        Record cleanup ();
}
