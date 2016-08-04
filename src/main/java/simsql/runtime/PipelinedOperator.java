

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

class PipelinedOperator {

	private Record [] data = new Record [10];
	private int numUsed = 0;	
	private short typeCode = -1;

	// in the case of a generic Pipelined op that does nothing, we might want to accept a specific type code
	public PipelinedOperator (short typeCodeToAccept) {
		typeCode = typeCodeToAccept;	
	}

	public PipelinedOperator () {}

	// this accepts a parameter from the caller... we give it the name as well as the value
	public void takeParam (String paramName, String paramVal) {
		throw new RuntimeException ("Why is a generic pipelined operator getting a parameter?");
	}

	// each particular PipelinedOperator has a process method that the PipelinedOperator actually
	// uses to do its work.  This is the method that the outside world uses to ask the operator 
	// to process a record
	protected void process (Record me) {
		emit (me);
	}

	// returns true if this pipelined operator accepts the specified record type.
	public boolean accepts (Record me) {
	  return me.getTypeCode () == typeCode;
	}

	// this is called by the outside world to get a result from the operator.  A null is returned
	// if there is not a result
	public Record getResult () {

		if (numUsed == 0)
			return null;

		numUsed--;
		Record ret = data[numUsed];
		data[numUsed] = null; // for the gc -- holding the reference causes leaks.
		return ret;
	}

	// this is called repetedly until a null is returned, as we are about to shut down
        public Record cleanup () {
		return null;
	}

	// this is called by the process operator to write its output
	protected void emit (Record me) {

		if (numUsed == data.length) {
			Record [] newData = new Record [data.length * 2];
			System.arraycopy(data, 0, newData, 0, data.length); // faster
			data = newData;
		}

		data[numUsed] = me;
		numUsed++;
	}
}
