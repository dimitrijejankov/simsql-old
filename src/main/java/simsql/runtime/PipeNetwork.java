

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

import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;

class PipeNetwork implements java.io.Serializable, ObjectWithPipeNetwork {

	// the basic building block of a pipe network
	private class PipeLink implements java.io.Serializable {
		PipeNode in;
		PipeLink out;

		public PipeLink (PipeNode myNode, PipeLink myOutput) {
			in = myNode;
			out = myOutput;
		}

		public void process (Record me) {
			
			// feed the record in
			in.getOperator ().process (me);

			// now, keep going through the network
			PipeNode inN = in;
			PipeLink outN = out;

			// this just follows the links through the network
			while (outN != null) {
					
				// pull all of the output records out, and put 'em into the next op
				Record next;
				while ((next = inN.getOperator ().getResult ()) != null) {

					// send the record along
					outN.in.getOperator ().process (next);	
				}

				// and move to the next operation
				inN = outN.in;
				outN = outN.out;
			}	
		}

		// basically, this loops repeatedly while records are still coming out of the operation... if a record
		// comes out, the network is asked to process the record, and if a result comes out, it is returned.
		// We keep doing this until (a) no results come out, or (b) the operation does not produce any more records
		public Record cleanup () {

			// just process all of the records that come out of the cleanup
			Record rr;
			if ((rr = getResult ()) != null)
				return rr;
	
			for (Record r = in.cleanup (); r != null; r = in.cleanup ()) {

				// if we are not at the top of the pipe...
			        if (out != null)
					// have the next guy process the record
					out.process (r);

				// otherwise, just return it
                                else
					return r;	

				// see if the cleanup produced a record
				if ((rr = getResult ()) != null)
					return rr;
			}
			return null;
		}
	}
	
	// this lists all of the ways that records can make it into the network
	private ArrayList <PipeLink> leaves;
	
	// this is the top-level node in the network
	private ArrayList <PipeLink> outputs;

	// the list of all of the nodes in the network
	private ArrayList <PipeNode> allNodes;

	// creates a pipe network by combining the two inputs
	public PipeNetwork (PipeNetwork one, PipeNetwork two) {

		leaves = new ArrayList <PipeLink> ();
		leaves.addAll (one.leaves);
		leaves.addAll (two.leaves);

		outputs = new ArrayList <PipeLink> ();
		outputs.addAll (one.outputs);
		outputs.addAll (two.outputs);

		allNodes = new ArrayList <PipeNode> ();
		allNodes.addAll (one.allNodes);
		allNodes.addAll (two.allNodes);
	}

	// this takes as input all of the sort attributes that are present on the table "file" and
	// it figures out and returns any additional sort attributes that are created
	// by the network (for example, if we are pipelining R1 into (R1 join R2 on R1.a = R2.b), then
	// if R1 is sorted on a, then the output will also be sorted on R2.b.
	public Set <Set <String>> findAnyAdditionalSortAttributes (String file, Set <Set <String>> currentAtts) {
		
		Set <Set <String>> returnVal = new HashSet <Set <String>> ();

		// loop through all of leave, trying to find the feeder
		for (PipeLink l : leaves) {

			// if we found the input that will take 
			if (l.in.getPipelinedInputFile ().equals (file)) {
				
				// chase all of the pointers through the network 
				PipeLink current = l;
				while (current.out != null) {

					// see if this op induces any new sort atts
					Set <String> newVal = current.in.findPipelinedSortAtts (currentAtts);
					if (newVal != null)
						returnVal.add (newVal);

					// and advance through the network
					current = current.out;
				}
			}
		}

		return returnVal;
	} 

        // tell all of the nodes they are shutting down, and get any additional records they spit out
	// basically, this words by just looping through all of the nodes in the network... for each, we
	// ask them to cleanup, which returns any records that come out of the top of the network as a
	// result of the cleanup.  Once all nodes have been cleaned up, we are done.
        public Record cleanupNetwork () {

		// see if there is anyone handling out there
		Record returnMe = getResult ();
		if (returnMe != null)
			return returnMe;

		// loop through each of the leaves, and work our way up the network, cleaning as we go
	        for (int whichLeafToClean = 0; whichLeafToClean < leaves.size(); whichLeafToClean++) {

		        PipeLink l = leaves.get(whichLeafToClean);

			// this just follows the links through the network
			while (l != null) {
					
				// ask this guy to cleanup
				while ((returnMe = l.cleanup ()) != null) {
					return returnMe;
				}

				// and move to the next operation
				l = l.out;
			}
		}

		// if we got here, it means nothing to clean
		return null;
	
        }

        // this simply takes as input a pipe network and adds it into the this one
        public void addAll (PipeNetwork addMe) {
          leaves.addAll (addMe.leaves);
          outputs.addAll (addMe.outputs);
          allNodes.addAll (addMe.allNodes);
        }

	// this gives all of the leaves in the pipe network a chance to re-evaluate which files are
	// really going to be pipelined (see PipeNode.reEvaluateWhichFileIsPipelined ())
	public void reEvaluateWhichFilesArePipelined () {
		for (PipeLink p : leaves) {
			p.in.reEvaluateWhichFileIsPipelined ();
		}
	}

	// this gets the name of the file that will ultimately be used to feed tuples into the specified file
	public String getFeeder (String inputFileName) {

		// loop through all of leave, trying to find the feeder
		for (PipeLink l : leaves) {

			// find the ultimate output of the network
			PipeLink current = l;

			// if the output of this leaf is what we want, we are good
			if (current.in.getOutputFileName ().equals (inputFileName) || current.in.getPipelinedInputFile ().equals (inputFileName))
				return l.in.getPipelinedInputFile ();

			while (current.out != null) {
				current = current.out;
				
				// if the ultimate output is what we want, we are done!
				if (current.in.getOutputFileName ().equals (inputFileName))
					return l.in.getPipelinedInputFile ();
			}
		}

		// if we got here, then we could not find the input, so throw an exception
		// [LL : we cannot throw an exception anymore, because the findPipelinedSortAtts() method in the join expects something
		//  there.]
		/* throw new RuntimeException ("Error!  I could not find the file that eventually pipelines into " + inputFileName); */
		return null;
		   
	}

	// this returns the list of all files that provide input to this pipe network
	public String [] getInputFiles () {
	
		// first thing is to see how many input files there are
		int count = leaves.size ();
		for (PipeNode p : allNodes) {
			count += p.getNonPipelinedInputFileNames ().length;
		}
		String [] output = new String [count];
		count = 0;
		
		// first, get all of the (possibly) pipelined input files
		for (PipeLink l : leaves) {
			output[count++] = l.in.getPipelinedInputFile ();
		}

		// now, get all of the ones throughout the network that cannot be pipelined
		for (PipeNode p : allNodes) {
			for (String s : p.getNonPipelinedInputFileNames ()) {
				output[count++] = s;
			}
		}

		return output;
	}

	public String [] getNonPipelinedInputFiles () {
		ArrayList <String> output = new ArrayList <String> ();


		// now, get all of the ones throughout the network that cannot be pipelined
		for (PipeNode p : allNodes) {
			for (String s : p.getNonPipelinedInputFileNames ()) {
			     output.add(s);
			}
		}

		return output.toArray (new String[output.size ()]);
	}

	// this returns the list of all of the files that will be pipelined into the pipe network
	public String [] getPipelinedInputFiles () {
		
		// go through all of the leaves in the network and collect the input files... BUT we
		// need to make sure not to add the same input file twice, or else we'll get twice
		// the number of tuples that we need from that particular input file (if the network
		// runs a self-join, we don't need to scan an input twice, since there will be two
		// input pipes that accept a particular record)
		ArrayList <String> output = new ArrayList <String> ();

		for (PipeLink l : leaves) {

			boolean alreadyThere = false;
			for (String s : output) {
				if (l.in.getPipelinedInputFile ().equals (s)) {
					alreadyThere = true;
					break;
				}
			}

			if (!alreadyThere)
				output.add (l.in.getPipelinedInputFile ());
		}
	
		return output.toArray (new String[output.size ()]);
	}

	// this is called to get a record from the output of the PipeNetwork... returns
	// a null if there is not any output
	public Record getResult () {

		// loop through and see if any of the outputs have any data
	        for (int i=0;i<outputs.size();i++) {
			Record temp;
			if ((temp = outputs.get(i).in.getOperator ().getResult ()) != null)
				return temp;
		}
		return null;
	}

	// this creates a pipe network that simply accepts records from the indicated
	// input files, and then emits them without doing anything else
	public PipeNetwork (String [] inputFiles, Short [] inputTypeCodes) {
		leaves = new ArrayList <PipeLink> ();
		outputs = new ArrayList <PipeLink> ();
		allNodes = new ArrayList <PipeNode> ();

		int i = 0;
		for (String s : inputFiles) {
			PipeNode temp1 = new StraightPipe (s, inputTypeCodes[i++]);
			PipeLink temp2 = new PipeLink (temp1, null);
			leaves.add (temp2);
			outputs.add (temp2);
			allNodes.add (temp1);
		}
	}

	// this writes out all of the Java codes that will be needed to run the pipe network
	// they are written to the indicated working directory
        static int classCount = 0;
	public ArrayList <String> writeJavaCodes (String workingDirectory) {
		ArrayList <String> fNames = new ArrayList <String> ();
		for (PipeNode p : allNodes) {
			String res = p.createJavaCode ("Class" + (classCount++), workingDirectory);
			if (res != null)
				fNames.add (res);
		}
		return fNames;
	}

	// this gets a string that contains the names of all of the .class objects that
	// are associated with the leaves of the pipe network... the relational op that
	// will run this pipe network will prepend this string to the result of the
	// RecTypeList.getPossibleRecordTypes operation, so that those records can be
	// deserialized by the mapper	
	public String getPossibleRecordTypesString () {
		String retVal = "";
		for (PipeLink p : leaves) {
			String next = p.in.getInputRecordClasses ();
			if (next != null)
				if (retVal.trim ().equals (""))
					retVal = next;
				else
					retVal = retVal + ", " + next;
		}
		return retVal;
	}

	// tell all of the ops to get ready to go... whichPartition gives the ID of this mapper (0, 1, 2, ..., up to 
	// numParitions).  Useful in the case that the PipeNode needs to know what mapper it is running in.  This
	// happens in the pipelined version of the UnionOp, which needs to read several of its input files from disk
	public void getReadyToRun (int whichPartition, int numPartitions) {
		for (PipeNode p : allNodes) {
			p.getReadyToRun (whichPartition, numPartitions);
		}
	}

	// see how much RAM we need to execute this network
	public long getRAMToRun () {
		long total = 0;
		for (PipeNode p : allNodes) {
			total += p.getRAMToRun ();
		}
		return total;
	}

	// this adds the given pipelined node at the top of this network... this is called when we are converting a node so
	// that it is going to be pipelined
	public void addPipelinedOperation (PipeNode addMe) {

		// by definition, there can only be one single input that constitutes a "real" leaf, and is not a straight pipe,
		// otherwise, there is no way that we could pipeline... so first thing is to find this "real" leaf
		PipeLink realLeaf = null;
		for (PipeLink p : leaves) {
			if (!p.in.getPipelinedInputFile ().equals (p.in.getOutputFileName ())) {
				if (realLeaf != null)
					throw new RuntimeException ("How did I find > 1 real pipes?");
				realLeaf = p;
			}
		}

		// if the "realLeaf" is null, then we will have a super-simple network, with only this node
		PipeLink output = new PipeLink (addMe, null);
		leaves = new ArrayList <PipeLink> ();
		if (realLeaf == null) {
			
			allNodes = new ArrayList <PipeNode> ();
			leaves.add (output);

		} else {

			leaves.add (realLeaf);

			// now, we look for the one output that pipes into this node
			for (PipeLink p : outputs) {
				if (p.in.getOutputFileName ().equals (addMe.getPipelinedInputFile ())) {
					p.out = output;
				}
			}	

		}
		
		outputs = new ArrayList <PipeLink> ();
		outputs.add (output);	
		allNodes.add (addMe);
	}

	// this combines two pipe networks
	public void appendNetworkOnto (PipeNetwork appendOntoMe) {
		
		// first we need to find the particular leaf in the network that 
		// will take this guy's output
		int i = 0;
		boolean foundIt = false;

		if (appendOntoMe.outputs.size () != 1)
			throw new RuntimeException ("how can a pipe network to be integrated have <> 1 outputs??");
		if (appendOntoMe.leaves.size () != 1)
			throw new RuntimeException ("how can a pipe network to be integrated have <> 1 leaves??");
		PipeNode output = appendOntoMe.outputs.get (0).in;

		while (!foundIt) {

			if (i == leaves.size ()) {
				throw new RuntimeException ("Error: I could not find a node in the pipe network taking this guy as input");
			}

			PipeLink p = leaves.get (i);	
			String s = p.in.getPipelinedInputFile ();

			// if we found the one that we are looking for, add him into the network
			if (s.equals (output.getOutputFileName ())) {
				
				leaves.remove (i);
				appendOntoMe.outputs.get (0).out = p;
				allNodes.addAll (appendOntoMe.allNodes);
				leaves.addAll (appendOntoMe.leaves);
				foundIt = true;
			}	

			i++;
		}
	}

	// move a record through the pipe network
	public void process (Record me) {

		// first, find the leaf-level PipeLink that accepts this record
	        for (int i=0;i<leaves.size();i++) {
		        PipeLink l = leaves.get(i);
			if (l.in.getOperator ().accepts (me)) {
				l.process (me);
			}
		}
	}
}
