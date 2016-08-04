

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

import java.lang.reflect.InvocationTargetException;
import org.apache.hadoop.fs.Path;
import java.io.IOException;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.fs.FSDataInputStream;
import java.lang.reflect.Constructor;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.input.CombineFileSplit;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;

public class CombineInputFileDeserializer extends RecordReader <Nothing, Record> {

	// the delegate
	private InputFileDeserializer reader;

	// our input
	private CombineFileSplit split;
	private int currentPath;
	private boolean done;
	private TaskAttemptContext context;
	private long bigProgress;

	public CombineInputFileDeserializer() {
		reader = new InputFileDeserializer();
	}

	public void initialize(InputSplit genericSplit, TaskAttemptContext context) throws IOException {

		split = (CombineFileSplit) genericSplit;
		this.context = context;

		// ignore zero length splits
		if (split.getLength() == 0) {
			done = true;
			return;
		}

		// otherwise, initialize our delegate reader with the first sub-split.
		currentPath = 0;
		done = false;
		FileSplit subSplit = new FileSplit(split.getPath(0), split.getOffset(0), split.getLength(0), split.getLocations());
		reader.initialize(subSplit, context);
		bigProgress = 0;
	}

	public synchronized void close() throws IOException {
		reader.close();
	}

	public Nothing getCurrentKey() throws IOException {
		return reader.getCurrentKey();
	}

	public Record getCurrentValue() throws IOException {
		return reader.getCurrentValue();
	}

	public boolean nextKeyValue() throws IOException {

		// are we done for good?
		if (done || split == null)
			return false;

		// try to fetch the next one...
		boolean keepGoing = reader.nextKeyValue();

		// are we good?
		if (keepGoing)
			return true;

		// otherwise, try to advance...
		currentPath++;

		// are we past the last split?
		if (currentPath >= split.getNumPaths()) {

			// if so, it's over.
			done = true;
			return false;
		}
	 
		// otherwise, move on to the next one.
		reader.close();
		bigProgress += split.getLength(currentPath - 1);
		FileSplit subSplit = new FileSplit(split.getPath(currentPath), split.getOffset(currentPath), split.getLength(currentPath), split.getLocations());
		reader = new InputFileDeserializer();
		reader.initialize(subSplit, context);
		return nextKeyValue();
	}

	public float getProgress() {

		if (split == null)
			return 0.0f;
		/*
		long littleProgress = 0;
		if (currentPath < split.getNumPaths()) {
			littleProgress = (long)(reader.getProgress() * split.getLength(currentPath));
		}

		float prog = (bigProgress + littleProgress) / (float)(split.getLength());
		*/

		if (currentPath >= split.getNumPaths())
		  return 1.0f;

		float prog = bigProgress / (float)split.getLength();
		prog += reader.getProgress() * (split.getLength(currentPath) / (float)split.getLength());
		if (prog > 1.0)
			return 1.0f;

		return prog;
	}
}
