

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
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import java.io.EOFException;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.BufferedInputStream;
import java.io.File;
import org.apache.hadoop.fs.Seekable;



public class InputFileDeserializer extends RecordReader <Nothing, Record> {
  
  // records where we are currently in the file that we are reading from, as well as the
  // location where we are supposed to start reading, as well as the last byte assigned to us
  private long start;
  private long pos;
  private long end;

  // for compressed streams
  private Seekable posCompressed;
  private boolean isCompressed;
  
  // set to true when we are done processing the split
  private boolean done = false;
  private String fName = "";
  
  // this is the record we are returning
  Record returnVal = null;
  
  // this is a special pattern that marks dead space in the input file... right after the
  // end of this pattern, a new record will start... in general, the RecordDeserializer
  // looks for the first one of these that occurs fully within its split, and then keeps
  // reading until it finds another one that starts past the end of its split
  byte [] startPattern = {0, 0, 34, 56, 23, 46, 45, 21, -34, -78, -23, 56, 12, -12, -10, 99};
      
  // this is an array of records that the RecordDeserializer will use to create the
  // records it produces as it deserializes
  private Record [] prototypeRecords;
  private RecordPool [] pools;
  
  // this is the file that we are reading from
  DataInputStream fileIn;
  
  // keep track of hoe many patters we have completed; for debugging
  int numPatternsSeen = 0;
 
  /** 
   * The constructor builds up a lost of all of the different record types that can be 
   * produced by the deserializer (if the input stream has three record types in
   * it, for example, then an array containing these three types would be passed
   * as "possibleRecordTypes".  What the deserializer does when deserializing is look at the
   * TypeCode at the start of the record; it then tries to find someone in "possibleRecordTypes"
   * that has the specified TypeCode... if it does, it then creates a record of that
   * type, derializes into it, and then returns the record.
   */
  public InputFileDeserializer () {
    Class<?> [] possibleRecordTypes = RecTypeList.getPossibleRecordTypes ();
    Init (possibleRecordTypes);
  }
  
  public InputFileDeserializer (Class<?> [] possibleRecordTypes) {
    Init (possibleRecordTypes);
  }
  
  private void Init (Class<?> [] possibleRecordTypes) {
    
    // if for some reasin this is already set, get outta here
    if (prototypeRecords != null)
      return;
    
    // first, go through and create dummy records of each type
    prototypeRecords = new Record [possibleRecordTypes.length];
    pools = new RecordPool [possibleRecordTypes.length];

    for (int i = 0; i < possibleRecordTypes.length; i++) {
      // extract the record's constructor and create a version of it
      try {
        Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
        prototypeRecords[i] = (Record) myConstructor.newInstance ();
	pools[i] = new RecordPool((AbstractRecord)prototypeRecords[i]);
      } catch (NoSuchMethodException e) {
        throw new RuntimeException ("I could not find a valid contructor for record type " + possibleRecordTypes[i].getName ()); 
      } catch (InstantiationException e) {
	  throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (1)", e);
      } catch (IllegalAccessException e) {
	  throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (2)", e);
      } catch (InvocationTargetException e) {
	  throw new RuntimeException ("I could not create a prototype record for one of the record types I was passed (3)", e);
      }
    }
  }  
  
  /**
   * finds the next start marker in the file
   */
  private boolean findStart () throws IOException {
    
    // we will keep reading until we see the start pattern, which has been put in the 
    // file to indicate that a record is just about to start.  If we cannot find this pattern
    // within the area we have been asked to read from, then we are done and there is not
    // any data for us to return
    int matchedSoFar = 0;
    while (true) {
      
      byte nextByte;
      try {
        // get another character
        nextByte = fileIn.readByte ();
        pos++;
        
      // if we got an exception, it means we hit an end of file
      } catch (EOFException eof) {
        done = true;
        return false;
      }
      
      // if our character matches, then record this
      if (nextByte == startPattern [matchedSoFar]) {
        matchedSoFar++;
      } else {
        
        // if we are past the end, then we are done
        if (!isCompressed && pos > end) {
          done = true;
          return false;
        }
        
        // no match, so start over (assuming we didn't just see a zero)
        if (nextByte == 0) {
          matchedSoFar = 1;
        } else {
          matchedSoFar = 0;
        }
      }
      
      // if we matched te entire sequence, we are ready to start deserializing
      if (matchedSoFar == startPattern.length) {
        numPatternsSeen++;
        return true;
      }
    }
  }
  
  /** 
   * Initializes the RecordDeserializer for use in Hadoop
   */
  public void initialize (InputSplit genericSplit, TaskAttemptContext context) throws IOException {
    
    FileSplit split = (FileSplit) genericSplit;
    Configuration job = context.getConfiguration ();
    
    // open the file and seek to the start of the split; also remember the start, end, and current position
    Path file = split.getPath ();
    FileSystem fs = file.getFileSystem (job);
    FSDataInputStream fileInLocal = fs.open (split.getPath());
    
    // do not do anything with a zero length split
    if (split.getLength () == 0) {
      done = true;
      return;
    }
    
    // not a non-zero length split
    start = split.getStart ();
    end = split.getStart () + split.getLength () - 1;       
    posCompressed = fileInLocal;
    fileInLocal.seek (start);


    // compression
    isCompressed = true;
    fileIn = RecordCompression.getInputStream(fileInLocal);
    pos = start;
    
    // now, find the start marker in our split
    fName = file.getName();
    findStart ();
  }
  
  /**
   * Initializes the RecordDeserializer for use with a particular HDFS file
   */
  public void initialize (Path file) throws IOException {
    
    Configuration conf = new Configuration ();
    FileSystem fs = file.getFileSystem (conf);
    FSDataInputStream fileInLocal = fs.open (file);

    // fileIn = fs.open (file);
    fileIn = RecordCompression.getInputStream(fileInLocal);
    posCompressed = fileInLocal;
    isCompressed = true;

    // remember the various positions
    start = 0;
    pos = 0;
    end = fs.getFileStatus(file).getLen() - 1;
 
    // now, find the start marker
    fName = file.getName();
    findStart ();
  }

  /**
   * Initializes the RecordDeserializer for use in a "regular" file system
   */
  public void initialize (String fileToProcess) throws IOException {
    
    // create the input stream
    FileInputStream fis = new FileInputStream (fileToProcess);
    BufferedInputStream inBufStream = new BufferedInputStream (fis, 32768);

    // fileIn = new DataInputStream (inBufStream);
    fileIn = RecordCompression.getInputStream(inBufStream);

    isCompressed = false;

    // remember the various positions
    start = 0;
    pos = 0;
    File file = new File (fileToProcess);
    end = file.length () - 1;
    
    // now, find the start marker
    fName = fileToProcess;
    findStart ();
  }
  
  public boolean nextKeyValue () throws IOException {
      
      // see if we are done with our split
      if (done)
        return false;
      
      // first we read in the code for the next record
      short typeCode;
      try {
        typeCode = fileIn.readShort ();
        
      // if we get an exception here it means we are all done
      } catch (EOFException allDone) {
        done = true;
        returnVal = null;
        return false; 
      }
      
      // we sucked up two bytes
      pos += 2;
              
      // if the typeCode is a zero, it means that we have hit a record boundary marker
      if (typeCode == 0) {
        
        // if we are at (or past) the end of our range, then a record boundary marker means we are done
        if (!isCompressed && pos >= end) {
          done = true;
          returnVal = null;
          return false;
        }
        
        // if we are not at the end of our range, then suck up the marker
        for (int i = 2; i < startPattern.length; i++) {
          
          byte nextByte = fileIn.readByte ();

	  pos++;

          if (nextByte != startPattern[i]) {
            throw new RuntimeException ("I was sucking up a marker, but byte " + i + " did not match"); 
          }
          numPatternsSeen++;
        }
        
        // this means we sucked up a marker, so try again
        return nextKeyValue ();
        
        // in this case, we got a different typecode, so deserialize the record
      } else {
       
        if (prototypeRecords == null) {
          throw new RuntimeException ("no one set up the list of prototype records!!!");  
        }
        
        for (int i = 0; i < prototypeRecords.length; i++) {
          
          // if we found a record to match the typecode...
          if (typeCode == prototypeRecords[i].getTypeCode ()) {
            
            // deserialize it and put it into both the key and the value
	      //            returnVal = prototypeRecords[i].buildRecordOfSameType ();
	    returnVal = pools[i].get();
	    int kx = returnVal.readSelfFromStream (fileIn);
	    pos += kx;

            return true;
          }
        }
        
        // if we made it here if means we got a bad typecode
	String pTypeCodes = "" + prototypeRecords[0].getTypeCode();
	for (int i=0;i<prototypeRecords.length;i++) {
	  pTypeCodes += ", " + prototypeRecords[i].getTypeCode();
	}

        throw new RuntimeException (fName + " -- At pos " + pos + " of " + end + " (starting at " + start + 
                                    ") I could not find a record with typeCode " + typeCode +
                                    "; saw " + numPatternsSeen + " start markers to date. Possible typeCodes are: " + pTypeCodes);
      }
      
    }
    
    public Nothing getCurrentKey ()  throws IOException {
      if (returnVal == null)
        throw new IOException ();
      
      return null;  
    }
    
    public Record getCurrentValue ()  throws IOException {
      if (returnVal == null)
        throw new IOException ();
      
      return returnVal;  
    }   

    public synchronized void close() throws IOException {
      if (fileIn != null) {
        fileIn.close(); 
      }
    }

    public float getProgress () {
      
      float returnVal;
      long pos = this.pos;

      if (isCompressed) {
	try {
	  pos = posCompressed.getPos();
	} catch (Exception e) { }
      }
      
      if (start == end)
        returnVal = (float) 1.0;
      else
        returnVal = (pos - start) / (float) (end - start);
      
      if (returnVal >= 1.0)
        returnVal = (float) 1.0;
      
      return returnVal;
    }
}
