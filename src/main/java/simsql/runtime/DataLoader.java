

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

import simsql.compiler.Relation;
import java.io.*;

/**
 * This is a general class for loading up data.
 *
 * @author Luis.
 */
public abstract class DataLoader {

  private Attribute[] atts;
  private PipedReader pr;
  private PipedWriter pw;
  private BufferedReader br;
  private LoaderRecord myRec;

  public DataLoader() {

    atts = null;
    myRec = null;

    // set up the streams
    try {
      //pr = new PipedReader();
      pw = new PipedWriter();
      //pw.connect(pr);
      pr = new PipedReader(pw, 1024 * 1024);
      br = new BufferedReader(pr);
    } catch (Exception e) {
      throw new RuntimeException("Failed to connect streams for bulk loading!");
    }
  }

  // sets up the loader record.
  protected void setup(short typeCode, Attribute[] inAtts) {
    LoaderRecord.setup(inAtts.length, typeCode);
    atts = inAtts;

    myRec = new LoaderRecord();
    myRec.setIsPresent(new BitstringWithSingleValue(true));
  }

  // translates a record from its text representation
  protected Record getRecord(String inRec) {

    // write the text representation into the pipe
    try {
      pw.write(inRec);
      pw.write((int)'\n');
      
      // parse all the attributes
      for (int i=0;i<atts.length;i++) {
	Attribute temp = atts[i].readSelfFromTextStream(br);
	myRec.setIthAttribute(i, temp);
      }
    } catch (Exception e) {
      throw new RuntimeException("Could not load record!", e);
    }

    return myRec;
  }

  // runs the bulk loader. returns the uncompressed size of the
  // relation on HDFS, otherwise zero.
  public abstract long run(String inputPath, String outputPath, short typeCode, Relation r, int sortAtt);
}
