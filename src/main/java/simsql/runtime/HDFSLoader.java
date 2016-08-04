

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

import java.io.*;
import java.util.*;
import simsql.compiler.Relation;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.conf.Configuration;

/**
 * Direct data loader for small files, writes to HDFS.
 *
 * @author Luis.
 */

public class HDFSLoader extends DataLoader {


  // replication factor for these small tables.
  private static short REPLICATION_FACTOR = 5;

  public HDFSLoader() {
    super();
  }

  public long run(String inputPath, String outputPath, short typeCode, Relation r, int sortAtt) {

    // make a directory for the relation
    Configuration conf = new Configuration ();
    FileSystem dfs = null;
    
    try {
      dfs = FileSystem.get (conf);
    } catch (Exception e) {
      throw new RuntimeException("Cannot access HDFS!", e);
    }
    
    try {
      // it we can't find it in the configuration, then create it
      Path path = new Path (outputPath);
      if (dfs.exists (path)) {
	dfs.delete (path, true);
	dfs.mkdirs (path);
      }
    } catch (Exception e) {
      throw new RuntimeException ("Could not create the file to bulk load to!", e);
    }
    
    
    
    // set up the attributes
    ArrayList<simsql.compiler.Attribute> allAtts = r.getAttributes();
    Attribute[] someAtts = new Attribute[allAtts.size()];
    for (int i=0;i<allAtts.size();i++) {
      someAtts[i] = allAtts.get(i).getPhysicalRealization();
    }

    setup(typeCode, someAtts);
    long count = 0;

    // open the input file.
    BufferedReader inputBuff = null;
    try {
      inputBuff = new BufferedReader(new FileReader(inputPath));
    } catch (Exception e) {
      throw new RuntimeException("Could not open file to bulk load from!", e);
    }

    // open the output file.
    DataOutputStream outputBuff = null;
    try {
      outputBuff = dfs.create(new Path(outputPath + "/" + RecordOutputFormat.getFileNumber(0)), REPLICATION_FACTOR);
    } catch (Exception e) {
      throw new RuntimeException("Could not create HDFS output file!", e);
    }

    OutputFileSerializer myOutput = new OutputFileSerializer(outputBuff);
    Nothing nothing = new Nothing();

    // stats loader
    HDFSTableStats coll = new HDFSTableStats();

    while (true) {

      // read one line.
      String inRec = null;
      try {
	inRec = inputBuff.readLine();
      } catch (Exception e) {
	// ignore... it will break.
      }

      // eof?
      if (inRec == null) {
	break;
      }

      Record outRec = getRecord(inRec);
      outRec.setSortAttribute(count++);
      try {
	myOutput.write(nothing, outRec);
	coll.take(outRec);
      } catch (Exception e) {
	throw new RuntimeException("Could not write to HDFS file!", e);
      }
    }

    // close the files
    try {
      myOutput.close();
      inputBuff.close();
    } catch (Exception e) {
      throw new RuntimeException("Could not close output files!", e);
    }

    try {
      coll.save(outputPath + "/dataFile.stats");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return myOutput.totalBytesWritten();
  }
}
