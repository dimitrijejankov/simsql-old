

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
 * Temporary table operation.
 */
public class TempTableOp extends RelOp {

  // returns a set of necessary mappings.   
  public Set<String> getNecessaryValues() {
    return new HashSet<String>(Arrays.asList
			       ("operation", "outFile", "typeCode", "rows"));
  }

  // returns the name of the operation
  public String getOperatorName() {
    return "TempTable";
  }

  public boolean acceptsAppendable() {
    return false;
  }

  public boolean acceptsPipelineable() {
      return false;
  }

  // returns the set of inputs
  public String[] getInputs() {
    return new String[0];
  }

  // returns the output of this operation
  public String getOutput() {
    return getValue("outFile").getStringLiteral();
  }

  public short getOutputTypeCode() {
    return getValue("typeCode").getInteger().shortValue();
  }


  // returns the set of output attribute names
  public String[] getOutputAttNames() {

    String[] out = new String[typesOut.length];

    for (int i=0;i<out.length;i++) {
      out[i] = "temp_att_" + i;
    }

    return out;
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
	    return TempTableOp.this.typeCode;
	  }

	  public int getNumAttributes() {
	    return TempTableOp.this.typesOut.length;
	  }
	};

      outRec.setIsPresent(new BitstringWithSingleValue(true));

      // now, load all the records.
      Nothing nothing = new Nothing();
      RowListRHS rows = (RowListRHS)getValue("rows");

      for (int i=0;i<rows.getNumRows();i++) {

	for (int j=0;j<typesOut.length;j++) {
	  outRec.setIthAttribute(j, rows.getValue(i, j));
	}

	outRec.setSortAttribute(i);
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

    return true;
  }

  private AttributeType[] typesOut;
  private short typeCode;

  public TempTableOp(Map<String, ParsedRHS> valuesIn) {
    super(valuesIn);

    // check/get the types.
    typesOut = ((RowListRHS)getValue("rows")).getCheckTypes();
    typeCode = getValue("typeCode").getInteger().shortValue();
  }
}
