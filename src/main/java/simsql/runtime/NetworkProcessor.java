

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

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.lang.reflect.InvocationTargetException;

// this class encapsultes a PipeNetwork object that has been created by de-serializing a 
// previously serialized PipeNetwork
class NetworkProcessor implements ObjectWithPipeNetwork {

	// the PipeNetwork that we are supposed to be running on the input to the mapper
	private PipeNetwork myNetwork;

	// useful when we need to know which mapper/reducer we are, and how many there are
	private int whichPartition;
	private int numPartitions;

	// this is an array of records that the mapper will use to create the
	// records it produces as it sends them along to the map function
	Record [] prototypeRecords;

	// allows access to the pipe network--from ObjectWithPipeNetwork
	public void process (Record me) {
		myNetwork.process (me);
	}

	// this also allows such access--from ObjectWithPipeNetwork
	public Record getResult () {
		return myNetwork.getResult ();
	}

	public Record cleanupNetwork () {
		return myNetwork.cleanupNetwork ();
	}

        // the first param is the name of the file where the pipe network has been serialized to
        // the second param is the partition (mapper/reducer) number, and the last param is the 
        // number of workers (mappers/reducers).  This is useful in the case that we need to divide
        // work among the various mappers and reducers; we can see what work we need to do
	// Note that for numParitionsIn to be calculated, the MapReduce job must have the entry
	// "simsql.fileToMap" in the configurations
	public NetworkProcessor (String objFile, int whichPartitionIn, int numPartitionsIn) throws IOException, InterruptedException {

		// remember which partition this is 
		whichPartition = whichPartitionIn;

		// remember the number of partitions
		numPartitions = numPartitionsIn;

		// get the list of prototype records
		Class<?> [] possibleRecordTypes = RecTypeList.getPossibleRecordTypes ();

		// first, go through and create dummy records of each type
		prototypeRecords = new Record [possibleRecordTypes.length];
		for (int i = 0; i < possibleRecordTypes.length; i++) {

			// extract the record's constructor and create a version of it
			try {
				Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
				prototypeRecords[i] = (Record) myConstructor.newInstance ();
			} catch (NoSuchMethodException e) {
				throw new RuntimeException ("I could not find a valid contructor for one of the record types I was passed");
			} catch (InstantiationException e) {
				throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (1)");
			} catch (IllegalAccessException e) {
				throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (2)");
			} catch (InvocationTargetException e) {
				throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (3)");
			}
		}

		// deserialize the pipe network
		try {
			InputStream fileIn = VGFunction.class.getResourceAsStream (objFile);	
			ObjectInputStream in = new ObjectInputStream (fileIn);
			myNetwork = (PipeNetwork) in.readObject ();
			in.close ();
			fileIn.close ();
		} catch (IOException i) {
			i.printStackTrace ();
			throw new RuntimeException ("problem deserializing the network");
		} catch (ClassNotFoundException c) {
			c.printStackTrace ();
			throw new RuntimeException ("derialized into a class I do not know!");
		}

		// now we are ready to go!
		myNetwork.getReadyToRun (whichPartition, numPartitions);		
	}

    public Record buildFromPrototype(Record inRec) {
	for (int i=prototypeRecords.length - 1;i>=0;i--) {
	    if (inRec.getTypeCode() == prototypeRecords[i].getTypeCode()) {
		Record outRec = prototypeRecords[i]/*.buildRecordOfSameType()*/;
		outRec.copyMyselfFrom(inRec);

		return outRec;
	    }
	}

	String expected = "" + prototypeRecords[0].getTypeCode();
	for (int i=1;i<prototypeRecords.length;i++) {
	    expected += ", " + prototypeRecords[i].getTypeCode();
	}

	throw new RuntimeException("Unknown TypeCode from record " + inRec.getTypeCode() + "expected one of: " + expected);
    }
}
