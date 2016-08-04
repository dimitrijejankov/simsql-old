

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

import java.io.DataOutputStream;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputCommitter;
import java.io.IOException;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.mapreduce.TaskID;
import org.apache.hadoop.mapreduce.TaskType;
import java.text.NumberFormat;

/**
 * This silly little class is here primarily to return an
 * OutputFileSerializer object
 */

public class RecordOutputFormat extends FileOutputFormat <WritableKey, WritableValue> {

  // name of the last input split given to us. if it is non-null,
  // we'll get its
  public static String lastInputSplit = null;

  public RecordWriter <WritableKey, WritableValue> getRecordWriter (TaskAttemptContext job) throws IOException, InterruptedException {
    
    Configuration conf = job.getConfiguration();

    // here's what we do -- if we have a map-only job and a value for
    // lastInputSplit as given to us by RecordInputFormat, then we
    // will get our part number from that file. otherwise, we'll use
    // the one we get from the job.

    // get the part from the job.
    TaskID taskId = job.getTaskAttemptID().getTaskID();
    int part = taskId.getId();
    if (RecordOutputFormat.lastInputSplit != null && taskId.getTaskType() == TaskType.MAP) {

      part = RecordOutputFormat.getPartNumber(RecordOutputFormat.lastInputSplit);
      System.out.println("MAP-ONLY JOB: USING PART NUMBER " + part + " FROM INPUT SPLIT");

      // set it back to null
      RecordOutputFormat.lastInputSplit = null;
    }

    FileOutputCommitter committer = (FileOutputCommitter) getOutputCommitter(job);
    Path file = new Path(committer.getWorkPath(), RecordOutputFormat.getFileNumber(part));

    /* Path file = getDefaultWorkFile (job, ".tbl"); */
    FileSystem fs = file.getFileSystem (conf);
    FSDataOutputStream fileOut = fs.create(file, false); 
    return new OutputFileSerializer (fileOut);
  }

  // part-00025.tbl --> 25
  public static int getPartNumber(String fileName) {
    return Integer.parseInt((fileName.substring(fileName.length() - 9).split("\\."))[0]);
  }

  // 25 --> part-00025.tbl
  public static String getFileNumber(int part) {
    NumberFormat nf = NumberFormat.getInstance();
    nf.setMinimumIntegerDigits(5);
    nf.setGroupingUsed(false);

    return "part-" + nf.format(part) + ".tbl";
  }

}
