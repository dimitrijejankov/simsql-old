

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

/**
 * An appendable version of the aggregate. This can be added onto the
 * output pipe network of an operator to pre-aggregate some groups and
 * decrease the size of the output written into HDFS.
 *
 * @author Luis
 */

class PipelinedAggregate extends APipeNode {

  // the name of the input record class
  private String inputRecordClass;

  // the name of the input file
  private String pipelinedInputFile;

  // the name of the output file.
  private String outputFile;

  // the name of the class that runs this guy.
  private String className;

  // the operator itself
  private AggregateOp aggToRun;

  public String getInputRecordClasses() {
    return inputRecordClass;
  }

  public String getPipelinedInputFile() {
    return pipelinedInputFile;
  }

  public String[] getNonPipelinedInputFileNames() {

    // there are no non-pipelined files for us
    return new String[0];
  }

  public String getOutputFileName() {
    return outputFile;
  }

  public void reEvaluateWhichFileIsPipelined() {
    // nothing to do here!
  }

  public void getReadyToRun(int whichPartition, int numPartitions) {
    getReadyToRun(className + ".class");
  }

  public Set<String> findPipelinedSortAtts(Set<Set<String>> currentAtts) {

    // temporary
      return null;
  }

  public long getRAMToRun() {

    // we'll ask for a minimum of 32MB to do this.
    return 32;
  }

  public PipelinedAggregate(AggregateOp useMe) {
    aggToRun = useMe;
    outputFile = useMe.getInputs()[0];
    pipelinedInputFile = useMe.getInputs()[0];    
  }

  public String createJavaCode (String classNameIn, String workDirectory) {

    className = classNameIn;

    // get the macros for the aggregate records.
    Map<String, String> macros = aggToRun.buildMacroReplacements();

    // get the macros for the operator class.
    Map<String, String> opMacros = new HashMap<String, String>();
    opMacros.put("pipeName", className);
    opMacros.put("pipeTypeCode", macros.get("inputTypeCode"));

    // generate it as a temporary file.
    String tempFile = workDirectory + "/simsql/runtime/" + className + ".tmp";
    MacroReplacer opReplacer = new MacroReplacer("PipelinedAggregateOperator.javat", tempFile, opMacros);

    // now, read that file into a string
    String preAggregateStub = null;
    try {
      InputStream reader = new FileInputStream(tempFile);
      StringBuilder builder = new StringBuilder();
      while (reader.available() > 0) {
	builder.append((char)reader.read());
      }
      
      reader.close();
      preAggregateStub = builder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Could not obtain pipelined aggregate file " + tempFile, e);
    }

    // set the special macros for this pipelined aggregate.
    macros.put ("pipeName", className);
    macros.put ("skipOrderingsStart", "/****");
    macros.put ("skipOrderingsEnd", "****/");
    macros.put ("preAggregateStub", preAggregateStub);

    // do the macro replacement
    MacroReplacer myReplacer = new MacroReplacer ("AggregateRecords.javat", workDirectory + "/simsql/runtime/" + className + "Records.java", macros);

    inputRecordClass = className + "AggregationInput.class";
    aggToRun = null;

    // outta here.
    return "/simsql/runtime/" + className + "Records.java";
  }
}
