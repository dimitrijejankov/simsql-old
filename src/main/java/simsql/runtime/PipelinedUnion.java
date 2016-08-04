

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
import java.util.HashSet;
import java.io.InputStream;
import java.io.FileInputStream;


class PipelinedUnion extends APipeNode {

	// the actual union we are running
	private UnionOp unionToRun;

	// used implement "getInputRecordClasses"
	private String inputRecordClass;

	// this remembers the name of the class that we created
	private String className;

	// the input and output info
	String outputFile;
	String pipelinedFile;
	String nonPipelinedFiles;
	
	public String getInputRecordClasses () {
		return inputRecordClass;
	}

	public long getRAMToRun () {
		return 0;
        }

	// ask the join for any additional sort atts that are induced
	public Set <String> findPipelinedSortAtts (Set <Set <String>> currentAtts) {
		return new HashSet <String> ();
	}

	public String getPipelinedInputFile () {
		return pipelinedFile;
	}

	public String [] getNonPipelinedInputFileNames () {
		
		if (nonPipelinedFiles == null)
			return new String [0];

		return nonPipelinedFiles.split (",");
	}

	public String getOutputFileName () {
		return outputFile;
	}

	public void getReadyToRun (int whichPartition, int numPartitions) {
		getReadyToRun (className + ".class");
		getOperator ().takeParam ("whichPartition", Integer.toString (whichPartition));
		getOperator ().takeParam ("numPartitions", Integer.toString (numPartitions));
	}

        public void reEvaluateWhichFileIsPipelined () {
		nonPipelinedFiles = unionToRun.getNonPipelinedFiles ();
		pipelinedFile = unionToRun.getPipelinedFile ();
		outputFile = unionToRun.getOutput ();
	}

	public PipelinedUnion (UnionOp useMe) {
		unionToRun = useMe;	
		reEvaluateWhichFileIsPipelined ();
	}

	public String createJavaCode (String classNameIn, String workDirectory) {

		className = classNameIn;

		// the basic tactic is as follows.  We are going to build an input record type 
		// for each of the inner input files... these will all be inner classes.
		// first, get the macros we are gonna use
		Map <String, String> macros = unionToRun.buildMacroReplacements ();

		// build the macro for the inner inputs
		ParsedRHS innerInputs = unionToRun.getValue("innerInputs");
		StringBuilder builder = new StringBuilder();

		// this lists all of the typecodes for the inner files
		String innerTypeCodes = null;

		// this lists "new" statements for all of the inner record classes
		String newStatements = "";

		if (innerInputs != null) {
			int yx = 1; 

			// this is going to hold all of the inner classes
			for (String innerName: innerInputs.getVarList().keySet()) {

				// build the inner set of macros
				Map <String, String> innerMacros = unionToRun.buildInnerMacroReplacements(innerName, yx);

				// extract the type code
				if (innerTypeCodes == null) {
					innerTypeCodes = "";
				} else {
					innerTypeCodes += ", ";
				}

				// add the type name
				innerTypeCodes += innerMacros.get ("innerInputTypeCode");

				// add the new statement to create an instance of the record
				newStatements += "allRecs[" + (yx - 1) + "] = new PipelinedUnionInnerRecord_" +
					innerMacros.get ("innerInputName") + "();\n";

				// apply the matcros
				String tempFile = workDirectory + "/simsql/runtime/" + className + "Union" + yx + ".tmp";
				MacroReplacer innerReplacer = new MacroReplacer(unionToRun.getPipelinedInnerTemplateFile(), 
					tempFile, innerMacros);
				yx++;

				// read the file into a string
				try {
					InputStream reader = new FileInputStream (tempFile);
					while (reader.available() > 0) {
						builder.append ((char) reader.read ());
					}
					reader.close();
				} catch (Exception e) {
					throw new RuntimeException("Could not obtain pipelined union file " + tempFile, e);
				}
			}

		}

                // record info on the input data
                macros.put ("inputNumAtts", unionToRun.getPipelinedNumAtts () + "");
                macros.put ("inputTypeCode", unionToRun.getPipelinedTypeCode () + "");

		// put the list of classes into the map
		macros.put ("innerInputClasses", builder.toString());

		// put the name of the pipe in there
		macros.put ("pipeName", className);

		// these are the files we'll just read from disk
		String files = "";
		String [] allFiles = unionToRun.getNonPipelinedFiles ().split (",");
		for (String file : allFiles) {
			if (!files.equals (""))
				files = files + ", ";
			files = files + "\"" + file + "\"";
		}
		macros.put ("filesToReadDirectly", files);
                macros.put ("directTypeCodes", unionToRun.getNonPipelinedTypeCodes ());

		// now put the corresponding list of type codes in
		macros.put ("inputTypeCodes", innerTypeCodes);

		// and put the list of record creation statments in
		macros.put ("setupListOfRecs", newStatements);

		// do the macro replacement on the file
		MacroReplacer myReplacer = new MacroReplacer ("PipelinedUnionOperator.javat", 
			workDirectory + "/simsql/runtime/" + className + ".java", macros);

		// remember the name of the input record class
		inputRecordClass = className + "Input.class";	
		
		// and kill our relational op (so we do not serialize it)
		unionToRun = null;		

		// outta here!
		return "/simsql/runtime/" + className + ".java";
	}	
}
