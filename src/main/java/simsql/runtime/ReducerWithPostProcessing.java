

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
import org.apache.hadoop.mapreduce.lib.reduce.*;
import org.apache.hadoop.mapreduce.lib.map.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import java.io.*;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.conf.Configuration;
import java.lang.reflect.*;

/**
 * This is a wrapped reducer that captures the output of write() in order to do things like:
 *  
 *  - Collecting statistics for certain relations.
 *  - Pushing the output records through the output pipe
 *  
 * Every reducer in the SimSQL runtime should be derived from this particular reducer
 *
 * @author Luis.
 */
class ReducerWithPostProcessing extends Reducer <RecordKey, RecordWrapper, WritableKey, WritableValue> implements ObjectWithPipeNetwork {

  // this is the pipe network that we contain... it is used to produce output records, and (optionally) it can
  // be used by reducers that need to access a pipe network in order process input records (like in a merge join)
  private ObjectWithPipeNetwork myNetwork;

  // allows access to the pipe network from within the reducer itself
  public void process (Record me) {
    myNetwork.process (me);
  }

  // this also allows such access
  public Record getResult () {
    return myNetwork.getResult ();
  }
  
  // as does this
  public Record cleanupNetwork () {
    throw new RuntimeException ("You probably should not be calling this operation externally!");
  }

  // needed for statistics collector
  HDFSTableStats coll = new HDFSTableStats();
  boolean collectStats = false;

  public void run(Context context) throws IOException, InterruptedException {

    int numReducers = context.getNumReduceTasks ();
    int whichReducer = context.getTaskAttemptID ().getTaskID ().getId ();

    // set up the pipe network
    myNetwork = new NetworkProcessor ("PipeNetwork.obj", whichReducer, numReducers);

    // set up our 'hook' context.
    Context myHookContext = new Wrapped<RecordKey, RecordWrapper, simsql.runtime.WritableKey, 
	simsql.runtime.WritableValue>().getReducerContext(context);

    // get ready to collect the stats
    collectStats = context.getConfiguration().getBoolean("simsql.collectStats", false);

    // do all of the reducing
    setup(myHookContext);
    while (context.nextKey()) {
      reduce (context.getCurrentKey(), context.getValues(), myHookContext);
    }

    // clean everything up 
    cleanup (myHookContext);

    // send any extra junk from the netowrk thru the reducer
    Nothing garbage = new Nothing ();
    Record r;
    while ((r = myNetwork.cleanupNetwork ()) != null) {
      context.write (garbage, r);
    }

    if (collectStats) {
      coll.save(FileOutputFormat.getPathForWorkFile(context, "part", ".stats").toUri().getPath());
    }
  }

  /** The internal class for wrapping the write() method  */
  @SuppressWarnings("unchecked")
  public class Wrapped <K1,V1,K2,V2> extends WrappedReducer<K1,V1,K2,V2> {

    @Override
    public Reducer<K1,V1,K2,V2>.Context getReducerContext(ReduceContext<K1,V1,K2,V2> context) {
      return new Context(context);
    }

    public class Context <K1,V1,K2,V2> extends WrappedReducer<K1,V1,K2,V2>.Context {

      public Context(ReduceContext<K1, V1, K2, V2> context) {
	super(context);
      }

      @Override
      public void write(Object key, Object value) throws IOException, InterruptedException {

        // run the output pipe
        process ((Record) value);
        
        // and push all of the data through it
        Record temp;
        while ((temp = getResult ()) != null) {
	    reduceContext.write((K2) key, (V2) temp);
	    
	    // collect stats...
	    if (collectStats) {
		coll.take((Record) temp);
	    }

	    temp.recycle();
	}
      }
    }
  }
}
