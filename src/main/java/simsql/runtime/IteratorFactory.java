

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

import simsql.runtime.OutputFileSerializer;
import simsql.runtime.ExampleRuntimeParameter;
import simsql.runtime.BitstringWithSingleValue;
import simsql.runtime.Attribute;
import simsql.runtime.Nothing;
import simsql.runtime.Record;
import simsql.runtime.InputFileDeserializer;
import simsql.runtime.RecordOutputFormat;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.BufferedWriter;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.Serializable;
import java.util.HashMap;
import java.util.ArrayList;
import java.lang.Iterable;
import java.util.Iterator;
import simsql.runtime.AbstractRecord;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import java.util.concurrent.LinkedBlockingQueue;

// this class is used to support iteration over physical relations
public class IteratorFactory {


  private class RecordIterator implements Iterator <Record> {

     // this is used to store the files we are iterating over
     private FileStatus [] fstatus;
  
     // this is used to deserialize the input file
     InputFileDeserializer myRecordSource;

     // this is the current file we are iterating over
     int curFile;

     // this is the current Record to return
     Record curRec = null;

     // number of attributes
     int numAtts;

     // typeCode
     short typeCode;

     public synchronized void close () throws IOException {
	     if (myRecordSource != null) {
		 myRecordSource.close ();
	     }
     }

     public RecordIterator (String fileName, int numAtts, short typeCode) {

	 synchronized(RecordIterator.class) {
	     // we start out by setting up the prototype recor
	     this.numAtts = numAtts;
	     this.typeCode = typeCode;
	     LoaderRecord.setMultiple(true);
	     LoaderRecord.setup (numAtts, typeCode);
   
	     try {
		 // now we open up all of the files in the directory
		 Configuration conf = new Configuration ();
		 FileSystem fs = FileSystem.get (conf); 
		 Path path = new Path (fileName); 
		 fstatus = fs.listStatus (path, new TableFileFilter());

		 // if we got no files, we are basically done!
		 if (fstatus.length == 0) {
		     curFile = -1;
		     return;
		 } else {
		     curFile = 0;
		 }

		 // at this point, we got it, so open up the file
		 Class<?> [] temp = {LoaderRecord.class};
		 myRecordSource = new InputFileDeserializer (temp);
		 myRecordSource.initialize (fstatus[curFile].getPath()); 
 
		 // and get the next record
		 getNextReady ();
	     } catch (Exception e) {
		 throw new RuntimeException ("Problem when setting up the iterator for a database file " +
					     fileName + " " + e.getMessage ());
	     }
	 }
     }

 
     // this does nothing; not supported
     public void remove () {}

     // this finds the next record in the sequence
     private void getNextReady () throws IOException {
 
       if (myRecordSource.nextKeyValue ()) {
         return; 
       }

       myRecordSource.close();
       myRecordSource = null;
       curFile++; 
       if (curFile == fstatus.length) {
         curFile = -1;
         return;
       }

       // and open it
       synchronized(RecordIterator.class) {
	   LoaderRecord.setMultiple(true);
	   LoaderRecord.setup(numAtts, typeCode);

	   Class<?> [] temp = {LoaderRecord.class};
	   myRecordSource = new InputFileDeserializer (temp);
	   myRecordSource.initialize (fstatus[curFile].getPath()); 
       }

       // and get the next record
       getNextReady ();
     }

     public boolean hasNext () {
       return (curFile != -1); 
     }

     public Record next () {
       try {
         Record returnVal = myRecordSource.getCurrentValue ();
         getNextReady ();
         return returnVal;
       } catch (Exception e) {
         e.printStackTrace ();
         throw new RuntimeException ("Problems when trying to iterate through a database file");
       }
     }
  }

  // this private class is also used to support iteration over physical relations
  private class RecordIterable implements Iterable <Record> {

    // this records the last iterator we were used to build... if another one is constructed,
    // then we will close the curent one
    private RecordIterator lastOne = null;

    private String fileNameToIterate = null;
    private Integer numAtts = null;
    private Short typeCode = null; 

    public RecordIterator iterator () {

      if (lastOne != null) {
        try {
          lastOne.close ();
        } catch (Exception e) {
          e.printStackTrace ();
          throw new RuntimeException ("problem when closing the Record iterator");
        }
      }

      lastOne = new RecordIterator (fileNameToIterate, numAtts, typeCode);
    
      return lastOne;
    }

    public RecordIterable (String fileNameIn, int numAttsIn, short typeCodeIn) {
      fileNameToIterate = fileNameIn;
      numAtts = numAttsIn;
      typeCode = typeCodeIn; 
    }

    public synchronized void close () {
      try {
        lastOne.close ();
      } catch (Exception e) {
        e.printStackTrace ();
        throw new RuntimeException ("problem when closing the Record iterator");
      }
      lastOne = null;
    } 
  }

  private RecordIterable lastOne = null;

  public Iterable <Record> getIterableForFile (String fileNameIn, int numAttsIn, short typeCodeIn) {

      synchronized(IteratorFactory.class) {

	  // we allow only one RecordIterator at a time... if a second one is opened, kill the last one
	  if (lastOne != null) {
	      // lastOne.close ();
	  }
	  lastOne = new RecordIterable (fileNameIn, numAttsIn, typeCodeIn);
	  
	  return lastOne; 
      }
  }
}


