

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

import org.apache.hadoop.mapreduce.*;
import java.util.*;
import java.io.*;
import java.nio.*;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.conf.*;

/**
 * Reduce class for the VGWrapper.
 *
 * @author Luis.
 */
class VGWrapperReducer extends ReducerWithPostProcessing {

  // the VGWrapper
  private VGWrapper vgw = null;

  // set up the task
  public void setup(Context context) {
	
    // get the VG function and the types
    VGFunction vgf = new VGFunction(context.getConfiguration().getStrings("simsql.functionFile")[0]);

    // get the number of iterations
    int numMC = context.getConfiguration().getInt("simsql.numIterations", 1);

    // get the direct buffer size.
    int dataBufferSize = context.getConfiguration().getInt("simsql.dataBufferSize", 1024 * 1024 * 100);

    // start up the VGWrapper.
    vgw = new VGWrapper(dataBufferSize, vgf, numMC, new Writer(context), this);

    // load up the cross product relations.
    VGWrapperReducer.addCrossRelations(context.getConfiguration(), vgw);

    // load up the sorted relations.
    VGWrapperReducer.addSortedRelations(context.getConfiguration(), vgw, context.getConfiguration ().getInt ("mapred.task.partition", -1));
  }

  /** Loads up the cross product relations into a VGWrapper object. */
  public static void addCrossRelations(Configuration conf, VGWrapper vgw) {

    // get the properties with the file information.
    String[] fileNames = conf.getStrings("simsql.crossFiles", new String[0]);
    String[] typeCodes = conf.getStrings("simsql.crossTypeCodes", new String[0]);
    String[] attCounts = conf.getStrings("simsql.crossAttCounts", new String[0]);

    if (fileNames.length != typeCodes.length || typeCodes.length != attCounts.length) {
      throw new RuntimeException("Cross product relations incorrectly set up!");
    }

    // pass them all to the VGWrapper.
    for (int i=0;i<fileNames.length;i++) {
      vgw.addCrossRelation(fileNames[i], Integer.parseInt(attCounts[i]), Short.parseShort(typeCodes[i]));
    }    
  }

  /** Plugs in the sorted relations into a VGWrapper object. */
  public static void addSortedRelations(Configuration conf, VGWrapper vgw, int part) {

    // get the properties with the file information.
    String[] fileNames = conf.getStrings("simsql.sortedFiles", new String[0]);
    String[] typeCodes = conf.getStrings("simsql.sortedTypeCodes", new String[0]);
    String[] attCounts = conf.getStrings("simsql.sortedAttCounts", new String[0]);

    if (fileNames.length != typeCodes.length || typeCodes.length != attCounts.length) {
      throw new RuntimeException("Sorted relations incorrectly set up!");
    }

    // pass them all to the VGWrapper.
    for (int i=0;i<fileNames.length;i++) {
      vgw.addSortedRelation(fileNames[i], part, Integer.parseInt(attCounts[i]), Short.parseShort(typeCodes[i]));
    }
  }

  /** Adapter class for writing the output of the VGWrapper. */
  public static class Writer implements OutputAdapter {
    private TaskInputOutputContext <?, ?, WritableKey, WritableValue> context;
    private Nothing dummy = new Nothing();

    public Writer(TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
      this.context = context;
    }

    public void writeRecord(Record outRec) {
      try {
	context.write(dummy, outRec);
      } catch (IOException e) {
	throw new RuntimeException("Unable to write output record! (1)", e);
      } catch (InterruptedException e) {
	throw new RuntimeException("Unable to write output record! (2)", e);
      }
    }
  }

  // reduces
  public void reduce(RecordKey key, Iterable <RecordWrapper> values, Context context) {

    // load up the current group(s) into the VGWrapper.
    for (RecordWrapper rw : values) {
      VGRecord vx = (VGRecord)rw.getWrappedRecord();
      vgw.takeRecord(vx);
    }

    // read the current group from the sorted relations into the VGWrapper.
    vgw.loadSortedRecords(key.getKey());

    // do the sampling
    boolean keepGoing = false;
    do {
      keepGoing = vgw.run();
    } while (keepGoing);
  }

  public void cleanup(Context context) throws IOException, InterruptedException {

  }
}
