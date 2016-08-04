

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
import java.io.*;
import java.util.*;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;

/**
 * The map class for the aggregate operator. Does pre-aggregation.
 *
 * @author Luis.
 */
class AggregateMapper extends MapperWithPipeNetwork {

  // adapter class for writing -- this one is used only when we have
  // an entire map/reduce job.
  public static class Writer implements OutputAdapter {

    private TaskInputOutputContext <?, ?, WritableKey, WritableValue> context;
    private RecordKey rk = new RecordKey();
    private RecordWrapper rw = new RecordWrapper();

    public Writer(TaskInputOutputContext <?, ?, WritableKey, WritableValue> context) {
      this.context = context;
    }

    public void writeRecord(Record writeMe) {
      try {
	AggregatorRecord output = (AggregatorRecord)writeMe;
	output.setSortAttribute(output.getHashKey());

	rk.set(output.getHashKey(), output.getTypeCode());
	rw.set(output);

	context.write(rk, rw);
	output.recycle();
      } catch (Exception e) {
	throw new RuntimeException("Failed to write out record!", e);
      }
    }
  }

  // our structure for pre-aggregation
  private Aggregator agg;

  public AggregateMapper() {
  }

  // set up the mapper...
  public void setup(Context context) throws IOException, InterruptedException {

    // get our configuration params.
    long preAggregationBufferSize = context.getConfiguration().getLong("simsql.preAggregationBufferSize", 1024L * 1024L * 16);
    boolean finalizeAggregate = context.getConfiguration().getBoolean("simsql.finalizeAggregate", false);
    agg = new Aggregator(preAggregationBufferSize, new Writer(context));
  }

  public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {

    // is this from a chain?
    InputRecord inputRec = (InputRecord) value;

    // we run the selection and projection.
    HashableRecord hashedRec = inputRec.runSelectionAndProjection();
    inputRec.recycle();

    // did we get something?
    if (hashedRec == null) {

      // if not, then leave.
      return;
    }

    // now, we split on the group-by attributes
    HashableRecord[] splits = hashedRec.splitSome();

    // give the splits to the aggregator.
    for (HashableRecord inRec : splits) {
      agg.take(inRec);
    }
  }

  public void cleanup(Context context) throws IOException, InterruptedException {

    // we flush those aggregates that remain in our pre-aggregation structure
    agg.flush();
  }
}


/**
 * This is just a pass-through aggregate.
 */
class ChainedAggregateMapper extends Mapper<Nothing, Record, WritableKey, WritableValue> {
    
    Aggregator agg;

    public ChainedAggregateMapper() {
    }
    
    public void setup(Context context) throws IOException, InterruptedException {
      long preAggregationBufferSize = context.getConfiguration().getLong("simsql.preAggregationBufferSize", 1024L * 1024L * 16);
      agg = new Aggregator(preAggregationBufferSize, new AggregateMapper.Writer(context));
    }

    public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {
	AggregatorRecord myRec = (AggregatorRecord)value;
	agg.take(myRec);
    }

    public void cleanup(Context context) throws IOException, InterruptedException {
	
	// we flush those aggregates that remain in our pre-aggregation structure
	agg.flush();
    }
}
