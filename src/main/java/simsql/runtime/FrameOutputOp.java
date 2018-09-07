

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

import simsql.compiler.ParallelExecutor;
import simsql.shell.RuntimeParameter;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.*;

/**
 * Frame Output op.
 */
public class FrameOutputOp extends RelOp {

  // returns a set of necessary mappings.   
  public Set<String> getNecessaryValues() {
    return new HashSet<String>(Arrays.asList
			       ("operation", "inFiles", "output", "output.outFile", "output.typeCode", "output.outAtts", "output.isFinal"));
  }

  // returns the name of the operation
  public String getOperatorName() {
    return "FrameOutput";
  }

  public boolean acceptsAppendable() {
    return false;
  }

  public boolean acceptsPipelineable() {
    return false;
  }

  // returns the set of inputs
  public String[] getInputs() {
    ArrayList<String> inputSet = new ArrayList<String>();
    inputSet.addAll(getValue("inFiles").getStringList());
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
    /***
    for (Assignment a: getValue("output.outAtts").getAssignmentList()) {
      out.add(a.getIdentifier());
    }

    return out.toArray(new String[0]);
    ***/
    return new String[]{"output_table_name"};
  }

  // no macro replacements.
  public Map<String, String> buildMacroReplacements() {
    return null;
  }

  // no template file either
  public String getTemplateFile() {
    return null;
  }

  // no mapper class.
  public Class<? extends Mapper> getMapperClass() {
    return null;
  }

  // no reducer class.
  public Class<? extends Reducer> getReducerClass() {
    return null;
  }

  @Override
    public boolean run(RuntimeParameter params, boolean verbose, ParallelExecutor parent) {

    // first, try getting an output file
    Configuration conf = new Configuration ();
    try {
      FileSystem dfs = FileSystem.get (conf);

      // check the directories...
      Path path = new Path(getOutput());
      if (!dfs.exists(path)) {
	try {
	  dfs.mkdirs(path);
	} catch (Exception e) {
	  throw new RuntimeException ("Could not create temporary table file.");
	}
      }

      // make an output file.
      String newPath = getOutput() + "/" + RecordOutputFormat.getFileNumber(0);

      // open the output file
      OutputFileSerializer myOutput = new OutputFileSerializer(dfs.create(new Path(newPath)));

      // create the loader record
      AbstractRecord outRec = new AbstractRecord() {
	  public short getTypeCode() {
	    return FrameOutputOp.this.typeCode;
	  }

	  public int getNumAttributes() {
	    return 1;
	  }
	};

      outRec.setIsPresent(new BitstringWithSingleValue(true));

      // now, load all the records.
      Nothing nothing = new Nothing();
      RowListRHS rows = (RowListRHS)getValue("rows");

      for (String ss : getInputs()) {
	  outRec.setIthAttribute(0, new StringAttribute(ss));
	  myOutput.write(nothing, outRec);
      }

      // close the output file.
      myOutput.close();

    } catch (Exception e) {
      throw new RuntimeException("Could not create temporary table.", e);			
    }

    // set the number of attributes here...
    if (getDB () != null) {
	getDB().setTableSize(getDB().getTableName(getOutput()), 100L);
	getDB().setNumAtts(getDB().getTableName(getOutput()), getOutputAttNames().length);
    }		
    // do nothing
    return true;
  }
    
  private short typeCode;

  public FrameOutputOp(Map<String, ParsedRHS> valuesIn) {
      super(valuesIn);
      typeCode = getValue("output.typeCode").getInteger().shortValue();
  }
}
