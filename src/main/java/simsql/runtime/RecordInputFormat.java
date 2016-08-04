

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
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
  
/**
 * This silly little class exists to create intances of the
 * InputFileDeserializer class, and also get soem information on the
 * input split names for the RecordOutputFormat in the case of a
 * map-side job.
 */
public class RecordInputFormat extends FileInputFormat <Nothing, Record> {

  // our files are compressed, so they can't be split.
  public boolean isSplitable(JobContext context, Path filename) {
    return false;
  }

  public RecordReader <Nothing, Record> createRecordReader(InputSplit split, TaskAttemptContext context) throws IOException, InterruptedException {

    // get the input file name and give it to the RecordOutputFormat
    RecordOutputFormat.lastInputSplit = ((FileSplit)split).getPath().getName();
    return new InputFileDeserializer();
  }
}
