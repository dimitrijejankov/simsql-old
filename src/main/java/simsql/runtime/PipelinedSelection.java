

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
 * An appendable version of the selection operator. Technically, this
 * should happen rarely, as the DataFlow translator takes care of
 * moving a lot of these selections. However, there are two cases where
 * it might be necessary to do this; essentially:
 *
 * 1. Duplicate removals -- it is a good idea to do pre-remove some
 *    duplicates.
 *
 * 2. The frameoutput operation, which is translated into multiple selections
 *    sometimes.
 *
 * @author Luis.
 */

class PipelinedSelection extends APipeNode {


  // the name of the input record class
  private String inputRecordClass;

  // the name of the input file
  private String pipelinedInputFile;

  // the name of the output file.
  private String outputFile;

  // the name of the class that runs this guy.
  private String className;

  // are we removing duplicates?
  private boolean removeDuplicates;

  // the operator itself
  private SelectionOp selToRun;


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

    // we'll ask for a minimum of 32MB to do the duplicate removal
    // (which is more than enough!)
    return 32;
  }

  public PipelinedSelection(SelectionOp useMe, boolean append) {
    selToRun = useMe;
    if (append) {
	outputFile = useMe.getInputs()[0];
    } else {
	outputFile = useMe.getOutput();
    }

    pipelinedInputFile = useMe.getInputs()[0];
  }

  public String createJavaCode (String classNameIn, String workDirectory) {

    className = classNameIn;

    // get the macros for the selection records.
    Map<String, String> macros = selToRun.buildMacroReplacements();

    // get the macros for the pipelined operator class.
    Map<String, String> opMacros = new HashMap<String, String>();
    opMacros.put("pipeName", className);
    opMacros.put("pipeTypeCode", macros.get("inputTypeCode"));
    opMacros.put("removeDuplicates", selToRun.removeDuplicates() ? "true" : "false");

    // generate it as a temporary file.
    String tempFile = workDirectory + "/simsql/runtime/" + className + "Selection.tmp";
    MacroReplacer opReplacer = new MacroReplacer("PipelinedSelectionOperator.javat", tempFile, opMacros);


    // now, read that file into a string
    String preSelectionStub = null;
    try {
      InputStream reader = new FileInputStream(tempFile);
      StringBuilder builder = new StringBuilder();
      while (reader.available() > 0) {
	builder.append((char)reader.read());
      }
      
      reader.close();
      preSelectionStub = builder.toString();
    } catch (Exception e) {
      throw new RuntimeException("Could not obtain pipelined selection file " + tempFile, e);
    }

    // set the special macros for this pipelined selection.
    macros.put ("pipeName", className);
    macros.put ("skipOrderingsStart", "/****");
    macros.put ("skipOrderingsEnd", "****/");
    macros.put ("preSelectionStub", preSelectionStub);

    // do the macro replacement
    MacroReplacer myReplacer = new MacroReplacer ("SelectionRecords.javat", workDirectory + "/simsql/runtime/" + className + "Records.java", macros);

    inputRecordClass = className + "SelectionIn.class";
    selToRun = null;

    // outta here.
    return "/simsql/runtime/" + className + "Records.java";
  }

}
