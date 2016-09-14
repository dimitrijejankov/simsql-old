

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



package simsql.code_generator;

import simsql.shell.RuntimeParameter;
import simsql.shell.query_processor.FileOutput;
import simsql.optimizer.SimSQLOptimizedQuery;
import simsql.shell.CodeGenerator;
import java.io.*;

/* 
 * Wraps the Prolog query parser and GraphViz/DataFlow translators.
 *
 * @author Luis
 */
public class WrappedTranslator implements CodeGenerator<SimSQLOptimizedQuery, DataFlowQuery> {

  // physical database
  private MyPhysicalDatabase myDB = new MyPhysicalDatabase ();

  public DataFlowQuery translate (SimSQLOptimizedQuery translateMe, RuntimeParameter params) {
	
    boolean DEBUG = true; /*((ExampleRuntimeParameter)params).getDebug();*/

    // first, get a string with the query
    DataFlowQuery dfq = null;
    String toParse = null;

    try {
      File file = new File(translateMe.getFName());
      FileReader converter = new FileReader (file);
      int fileSize = (int) file.length ();
      char [] buf = new char[fileSize + 10];
      fileSize = converter.read (buf);
      toParse = String.valueOf (buf, 0, fileSize);
      converter.close();
    } catch (Exception e) {
      dfq = new DataFlowQuery();
      dfq.setError("Unable to open input file for translation!");
      return dfq;
    }

    // now, obtain the parsed prolog query
    PrologQuery pq = new PrologQuery(toParse);

    // update the order of the output attributes in the frameOutput operator
    if (pq.getRoot().getType() == PrologQueryOperator.Type.FRAMEOUTPUT) {

      PrologFrameOutputOp opF = (PrologFrameOutputOp)pq.getRoot();

      for (int i=0;i<opF.getSources().size();i++) {

	// make sure it's a projection
	if (pq.getOperator(opF.getSources().get(i)).getType() == PrologQueryOperator.Type.PROJECTION) {
	  PrologProjectionOp opP = (PrologProjectionOp)pq.getOperator(opF.getSources().get(i));
	  /* System.out.println("updating projection " + opP.getName() + " for table " + opF.getOutputFiles().get(i) + ": " + translateMe.getViewAtts(opF.getOutputFiles().get(i))); */


	  // update its attribute names.
	  opP.updateAttributes(translateMe.getViewAtts(opF.getOutputFiles().get(i)));
	}
      }
    }

    // get a translator.
    DataFlowTranslator dft = new DataFlowTranslator(myDB);

    // do the translation.
    try {
      dfq = dft.translate(pq);
    } catch (Exception e) {
      StringWriter sw = new StringWriter();
      e.printStackTrace(new PrintWriter(sw));
      dfq = new DataFlowQuery();
      dfq.setError("Failed to translate the prolog -> dataflow query.\n" + sw.toString());
      return dfq;
    }

    // write the graphviz and DataFlow
    if (DEBUG) {
      try {
	FileOutput outFile = new FileOutput(".dataflow");
	PrintWriter of = new PrintWriter(outFile.getFName());
	of.println(dfq.getQueryStr());
	of.close();

	FileOutput gf = new FileOutput(".dot");
	PrintWriter off = new PrintWriter(gf.getFName());
	off.println(dfq.getGraphStr());
	off.close();
      } catch (Exception e) {
	// doesn't matter.
      }
    }
	
    return dfq;
  }

  // return the physical database
  public MyPhysicalDatabase getPhysicalDatabase () {
    return myDB;
  }
}
