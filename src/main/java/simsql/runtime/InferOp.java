

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

import java.util.*;
import java.io.*;
import org.antlr.runtime.*;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.filecache.DistributedCache;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;

public class InferOp extends RelOp {

    // returns a set of necessary mappings.   
    public Set<String> getNecessaryValues() {
	return new HashSet<String>(Arrays.asList
				   ("operation", "input", "input.inFiles", "input.inAtts", "input.typeCode", "input.hashAtts", 
				    "output", "output.outFile", "output.typeCode", "output.outAtts")
				   );
    }

    // returns the name of the operation
    public String getOperatorName() {
	return "Inference";
    }

    // returns the set of inputs
    public String[] getInputs() {
	ArrayList<String> inputSet = new ArrayList<String>();
	inputSet.addAll(getValue("input.inFiles").getStringList());
	String[] foo = {""};
	return inputSet.toArray(foo);
    }
   
    // returns the output of this operation
    public String getOutput() {
	return getValue("output.outFile").getStringLiteral();
    }

    public short getOutputTypeCode() {
      return getValue("output.typeCode").getInteger().shortValue();
    }


    // returns the set of output attribute names
    public String[] getOutputAttNames() {

	ArrayList<String> out = new ArrayList<String>();
	for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
	    out.add(a.getIdentifier());
	}

	return out.toArray(new String[0]);
    }


    // returns the set of macro replacements
    public Map<String, String> buildMacroReplacements() {

	Map<String, String> replacements = new HashMap<String, String>();

	// build the input record replacements
	Map<String, String> inAtts = buildAttributeReplacements("input.inAtts", "");
	replacements.put("inputNumAtts", "" + inAtts.size());
	replacements.put("inputTypeCode", "" + getValue("input.typeCode").getInteger());
	replacements.put("inputSelection", buildSelectionReplacement("input.selection", inAtts));

	// build the hashable record replacements.
	ParsedRHS inputAttsRHS = getValue("input.hashAtts");
	int counter = 0;
	String inStr = "";
    String hhStr = "";
    String coStr = "";
    for (String att: inputAttsRHS.getIdentifierList()) {
    	String newName = "atts[" + counter + "]";

    	inStr += "returnVal." + newName + " = " + inAtts.get(att) + ";\n    ";
    	hhStr += " ^ " + newName + ".getHashCode()";
		coStr += " && " + newName + ".equals(rec." + newName + ").allAreTrue()";
    	counter++;
    }

    replacements.put("inputAssignments", inStr);
    replacements.put("inputAttsComparisons", coStr);
    replacements.put("inferHash", hhStr);
	replacements.put("numInferAtts", "" + counter);

	// build the output record replacements.
	Map<String, String> allAtts = buildAttributeAssignReplacements("output.outAtts", "input.", "input.");

	replacements.put("outputTypeCode", "" + getValue("output.typeCode").getInteger());
	replacements.put("outputNumAtts", "" + getValue("output.outAtts").getAssignmentList().size());
	replacements.put("outputSelection", buildSelectionReplacement("output.selection", allAtts));
	replacements.put("outputAssignments", buildAssignmentReplacements("output.outAtts", "output.", allAtts));

	// we're done
	return replacements;
    }

    // returns the name of the template Java source file.
    public String getTemplateFile() {
	return "InferRecords.javat";
    }

    // returns the mapper class.
    public Class<? extends Mapper> getMapperClass() {
	return InferMapper.class;
    }

    // returns the reducer class.
    public Class<? extends Reducer> getReducerClass() {
	return InferReducer.class;
    }

    public InferOp(Map<String, ParsedRHS> valuesIn) {
	super(valuesIn);
    }
    
}
