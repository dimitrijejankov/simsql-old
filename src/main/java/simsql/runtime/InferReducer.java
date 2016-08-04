

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


class InferReducer extends ReducerWithPostProcessing /* <RecordKey, RecordWrapper, Nothing, Record> */ {

    // dummy object for the output key.
    private Nothing dummy = new Nothing();
    private boolean debug = false;

    public void reduce(RecordKey key, Iterable <RecordWrapper> values, Context context) {

	// iterate through the input values.
	for (RecordWrapper rw: values) {

	    // cast to the appropriate type.
	    ConstantRecord infRec = (ConstantRecord) rw.getWrappedRecord();

	    // gets the final record -- this also does selections and projections.
	    Record outRec = InferResult.inference(infRec);

	    // did we get something?
	    if (outRec != null) {

	    if (debug) {
		    System.err.print("{" + outRec.getIthAttribute(0).print(20));
		    for (int i=1;i<outRec.getNumAttributes() + 1;i++) {
			System.err.print(", " + outRec.getIthAttribute(i).print(20));
		    }
		    System.err.println("}");
		}

		// if so, write it out.
		try {
		    context.write(dummy, outRec);
		} catch (InterruptedException e) {
		    throw new RuntimeException("Unable to write an inference output record to the stream! (1)", e);
		} catch (IOException e) {
		    throw new RuntimeException("Unable to write an inference output record to the stream! (2)", e);
		}
	    }
	    
	}

    }
}
