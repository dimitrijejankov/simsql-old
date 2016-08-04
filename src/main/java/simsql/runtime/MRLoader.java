

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
 * MapReduce data loader for larger files.
 *
 * @author Luis.
 */

public class MRLoader extends DataLoader {

  // a filter to avoid reading logs and hidden files in Hadoop.
  private static final PathFilter hiddenFileFilter = new PathFilter() {
      public boolean accept(Path p){
	String name = p.getName();
	return !name.startsWith("_") && !name.startsWith(".");
      }
    };


  private int numTasks = 1;

  public MRLoader() {
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
      // if it exists, destroy it.
      Path path = new Path (outputPath);
      if (dfs.exists (path)) {
	dfs.delete (path, true);
      }
    } catch (Exception e) {
      throw new RuntimeException ("Could not create the file to bulk load to!", e);
    }

    // find a file name 
    String tempPath = null;
    if (inputPath.startsWith("hdfs:")) {
      tempPath = inputPath.replace("hdfs:", "");
    }
    else {
      tempPath = "/tempDataFile_" + r.getName();
      try {
	dfs.delete(new Path(tempPath), true);
      } catch (Exception e) {
	// ignore this.
      }

      // upload the text file
      try {   
	dfs.copyFromLocalFile(false, true, new Path(inputPath), new Path(tempPath));
	dfs.deleteOnExit(new Path(tempPath));
      } catch (Exception e) {
	throw new RuntimeException("Failed to upload text file " + inputPath + " to HDFS!", e);
      }
    }

    // set up the new job's parameters.
    conf.setBoolean ("mapred.compress.map.output", true);
    conf.set ("mapred.map.output.compression.codec", RecordCompression.getCodecClass());

    conf.set("io.serializations", "simsql.runtime.RecordSerialization,simsql.runtime.RecordKeySerialization,org.apache.hadoop.io.serializer.WritableSerialization");
    conf.setInt("simsql.loader.numAtts", r.getAttributes().size());
    conf.setInt("simsql.loader.typeCode", (int)typeCode);
    conf.setInt("simsql.loader.sortAtt", sortAtt);
    
    String[] myStrings = new String[r.getAttributes().size()];
    int j=0;
    for (simsql.compiler.Attribute a: r.getAttributes()) {
      myStrings[j++] = a.getPhysicalRealization().getClass().getName();
    }

    conf.setStrings("simsql.loader.types", myStrings);

    // create a job
    Job job;
    try {
      job = new Job(conf);
    } catch (Exception e) {
      throw new RuntimeException("Unable to create bulk loading job!", e);
    }

    // set the split size (number of mappers)
    long fSize = 0;
    if (inputPath.startsWith("hdfs")) {
      fSize = RelOp.getPathsTotalSize(new String[]{tempPath});
    } else {
      fSize = new File(inputPath).length();
    }

    FileInputFormat.setMinInputSplitSize(job, fSize / (long)numTasks);
    FileInputFormat.setMaxInputSplitSize(job, fSize / (long)numTasks);

    // and the number of reducers
    job.setNumReduceTasks(numTasks);

    // the mapper/reducer/jar
    job.setMapperClass(MRLoaderMapper.class);
    job.setReducerClass(MRLoaderReducer.class);
    job.setJarByClass(MRLoader.class);

    // I/O settings.
    job.setOutputFormatClass(RecordOutputFormat.class);

    job.setMapOutputKeyClass(RecordKey.class);
    job.setMapOutputValueClass(RecordWrapper.class);
    job.setOutputKeyClass(Nothing.class);
    job.setOutputValueClass(Record.class);
    try {
      FileInputFormat.setInputPaths (job, new Path(tempPath));
      FileOutputFormat.setOutputPath (job, new Path(outputPath));
    } catch (Exception e) {
      throw new RuntimeException("Could not set job inputs/outputs", e);
    }
    job.setGroupingComparatorClass(RecordKeyGroupingComparator.class);
    job.setPartitionerClass(RecordPartitioner.class);
    job.setSortComparatorClass(RecordKeySortComparator.class);

    job.setJobName("MRLoader: " + inputPath + " ==> " + outputPath);

    // run it
    Counters counters;
    try {
      job.waitForCompletion(true);
      counters = job.getCounters();
    } catch (Exception e) {
      throw new RuntimeException("Could not set up bulk loader job!", e);
    }

    // now, delete all the empty part files
    try {
      
      // get a filesystem
      FileSystem ddfs = FileSystem.get(conf);
      Path outPath = new Path(outputPath);
      if (ddfs.exists(outPath) && ddfs.isDirectory(outPath)) {
	FileStatus fstatus[] = ddfs.listStatus(outPath, new TableFileFilter());
	for (FileStatus ff: fstatus) {
	  if (ddfs.getContentSummary(ff.getPath()).getLength() <= 4) { // snappy leaves 4-byte long files around...
	    ddfs.delete(ff.getPath(), true);
	  }
	}
      }
    } catch (Exception e) { // this isn't disastrous 
    }			

    // get the counter for the output of the mapper.
    Counter bytesCounter = counters.findCounter(OutputFileSerializer.Counters.BYTES_WRITTEN);
    return bytesCounter.getValue();
  }

  public void setNumTasks(int numTasks) {
    this.numTasks = numTasks;
  }


  // ---- MAPPER CLASS FOR THE BULK LOADER ----
  public static class MRLoaderMapper extends Mapper<LongWritable, Text, RecordKey, RecordWrapper> {
    
    private MRLoader loader;
    private long count;
    private short typeCode;
    private int sortAtt;

    // build the mapper with our configurations...
    public void setup(Context context) {

      int numAtts = context.getConfiguration().getInt("simsql.loader.numAtts", 0);
      typeCode = (short)context.getConfiguration().getInt("simsql.loader.typeCode", 0);
      sortAtt = context.getConfiguration().getInt("simsql.loader.sortAtt", -1);

      String[] attTypes = context.getConfiguration().getStrings("simsql.loader.types");
      Attribute[] actualAtts = new Attribute[attTypes.length];
      for (int i=0;i<attTypes.length;i++) {
	try {
	  Class cx = Class.forName(attTypes[i]);
	  actualAtts[i] = (Attribute) cx.newInstance();
	} catch (Exception e) {
	  throw new RuntimeException(e);
	}
      }

      // set up.
      loader = new MRLoader();
      loader.setup(typeCode, actualAtts);
      count = 0;
    }

    private HDFSTableStats coll = new HDFSTableStats();

    public void map(LongWritable key, Text value, Context context) throws IOException, InterruptedException {

      // get a the string
      String inRec = value.toString();

      // parse the record
      Record outRec = loader.getRecord(inRec);

      // send it out
      RecordKey rk = null;
      if (sortAtt == -1) {

	// not sorted.
	rk = new RecordKey(count++, typeCode);
      } else {

	// sorted
	rk = new RecordKey(outRec.getIthAttribute(sortAtt).getHashCode(), typeCode);
      }

      coll.take(outRec);
      outRec.setSortAttribute(rk.getKey());
      context.write(rk, new RecordWrapper(outRec));
    }


    public void cleanup(Context context) throws IOException, InterruptedException {
      
      // write out the stats collector.
      coll.save(FileOutputFormat.getPathForWorkFile(context, "part", ".stats").toUri().getPath());
    }

  }

  // ---- REDUCER CLASS FOR THE BULK LOADER ----
  public static class MRLoaderReducer extends Reducer<RecordKey, RecordWrapper, Nothing, Record> {

    private MRLoader loader;
    private Nothing dummy = new Nothing();

    public void setup(Context context) {

      // same thing -- set up the loader.
      int numAtts = context.getConfiguration().getInt("simsql.loader.numAtts", 0);
      short typeCode = (short)context.getConfiguration().getInt("simsql.loader.typeCode", 0);

      String[] attTypes = context.getConfiguration().getStrings("simsql.loader.types");
      Attribute[] actualAtts = new Attribute[attTypes.length];
      for (int i=0;i<attTypes.length;i++) {
	try {
	  Class cx = Class.forName(attTypes[i]);
	  actualAtts[i] = (Attribute) cx.newInstance();
	} catch (Exception e) {
	  throw new RuntimeException("", e);
	}
      }

      // set up.
      loader = new MRLoader();
      loader.setup(typeCode, actualAtts);
    }

    public void reduce(RecordKey key, Iterable<RecordWrapper> values, Context context) throws IOException, InterruptedException {

      // write them out
      for (RecordWrapper rw: values) {
	context.write(dummy, rw.getWrappedRecord());
      }
    }
  }
}
