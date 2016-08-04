

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
import org.apache.hadoop.mapreduce.lib.map.*;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileStatus;
import java.util.Iterator;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import java.util.ArrayList;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

// this class implements a mapper that has a pipe network attached to it, so that it can run a bunch of pipelined operations
class MapperWithPipeNetwork extends Mapper <Nothing, Record, WritableKey, WritableValue> 
	implements ObjectWithPipeNetwork {

	// the PipeNetwork that we are supposed to be running on the input to the mapper
	private NetworkProcessor myNetwork;

        // dummy, used because we have to deal with the silly Nothing type
	Nothing nothing = new Nothing ();

	// needed for statistics collector
	HDFSTableStats coll = new HDFSTableStats();
	boolean collectStats = false;

	// allows access to the pipe network from within the mapper itself
	public void process (Record me) {
		myNetwork.process (me);
	}

	// this also allows such access
	public Record getResult () {
		return myNetwork.getResult ();
	}

        // as does this
        public Record cleanupNetwork () {
		throw new RuntimeException ("Probably should not be cleaning up the network in ObjectWithPipeNetwork via external call");
        }

	public void run (Context context) throws IOException, InterruptedException {

	    // first we figure out which mapper this is
            String taskId = context.getConfiguration ().get("mapred.task.id");
            String[] parts = taskId.split("_");
            int whichMapper = Integer.parseInt (parts[4]);

	    // now we figure out the number of mappers that there are gonna be
	    String fileToMap = context.getConfiguration ().get ("simsql.fileToMap", "noFile");
	    int numMappers;
	    if (fileToMap.equals ("noFile"))
		numMappers = -1;
	    else {
	    	Path path = new Path (fileToMap);
	        FileSystem fs = FileSystem.get (context.getConfiguration ());
	    	FileStatus [] fstatus = fs.listStatus (path, new TableFileFilter());
	    	numMappers = fstatus.length;
	    }

            // set up the pipe network
            myNetwork = new NetworkProcessor ("PipeNetwork.obj", whichMapper, numMappers);

	    // now, check if this is a map-only job.
	    Context myHookContext = context;
	    if (context.getNumReduceTasks() == 0) {

	      // if so, then we need to put some hooks on our context.
	      myHookContext = new Wrapped<Nothing, Record, WritableKey, WritableValue>().getMapContext(context);
	      
	      // and check if there's statistics collection to do here.
	      collectStats = context.getConfiguration().getBoolean("simsql.collectStats", false);
	    }

            // set things up for the context
	    setup(myHookContext);

	    // loop through and process all of the input records
	    while (context.nextKeyValue()) {
		pushOneRecord(myHookContext);
	    }

	    // clean everything up
	    cleanup (myHookContext);

            // cleanup the network and send everyone on
            Record r;
	    Nothing garbage = new Nothing();
            while ((r = myNetwork.cleanupNetwork ()) != null) {
		context.write(garbage, r);
            }
		
	    if (collectStats) {
	      coll.save(FileOutputFormat.getPathForWorkFile(context, "part", ".stats").toUri().getPath());
	    }	    
	}

	private ArrayList <Record> myBuffer = new ArrayList <Record> ();

	public void pushOneRecord (Context context) throws IOException, InterruptedException {

	    // get the input record and add it to the network
	    context.getCurrentKey ();
	    myNetwork.process (context.getCurrentValue ());

            // suck out all of the result records and buffer them... we do this so that we can 
            // be sure that there won't be any conflict between the records being put into the 
            // pipe by Hadoop MapReduce, and records that are being put into the pipe by a specific
	    // database operation (for example, a merge join might use the pipe as well.  If MapReduce
	    // puts a Record in, and then the merge join puts a record in before all of the MapReduce
	    // records have been removed, they are going to be interleaved, which is bad)
            int lastPos = 0;
            Record next = null;
            while ((next = myNetwork.getResult ()) != null) {
              if (lastPos >= myBuffer.size ())
                myBuffer.add (next);
              else
                myBuffer.set (lastPos, next);
              lastPos++;
            }
	    
	    // and push all of the output records into the network
            for (int i = 0; i < lastPos; i++) {
                next = myBuffer.get (i);
		map(nothing, myNetwork.buildFromPrototype(next), context);
		next.recycle();
	    }
	}


	/** The internal class for wrapping the write() method on the output context */
	@SuppressWarnings("unchecked")
	public class Wrapped <K1, V1, K2, V2> extends WrappedMapper<K1, V1, K2, V2> {

	  @Override
	  public Mapper<K1,V1,K2,V2>.Context getMapContext(MapContext<K1, V1, K2, V2> context) {
	    return new Context(context);
	  }

	  public class Context <K1, V1, K2, V2> extends WrappedMapper<K1, V1, K2, V2>.Context {

	    public Context(MapContext<K1, V1, K2, V2> context) {
	      super(context);
	    }

	    @Override
	    public void write(Object key, Object value) throws IOException, InterruptedException {

	      // run the output pipe
	      process ((Record) value);
	      	      
	      // and push all of the data through it
	      Record temp;
	      while ((temp = getResult ()) != null) {
		  mapContext.write((K2) key, (V2) temp);	     
		  
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
