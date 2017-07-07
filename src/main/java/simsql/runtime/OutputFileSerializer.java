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

import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.BufferedOutputStream;
import org.apache.hadoop.mapreduce.RecordWriter;
import java.io.IOException;
import org.apache.hadoop.mapreduce.TaskAttemptContext;

public class OutputFileSerializer extends RecordWriter <WritableKey, WritableValue> {

  // this is where we actually write the result of the serialization
  private DataOutputStream writeToMe;
  public static enum Counters { BYTES_WRITTEN };
  
  // this is is the pattern that we write to mark the start of a record.  Not all records
  // start with this pattern, but the pattern will appear every 100KB or so in the file.
  // A reader that finds this pattern can be sure that immediately after, there will be the
  // start of a record
  byte [] startPattern = {0, 0, 34, 56, 23, 46, 45, 21, -34, -78, -23, 56, 12, -12, -10, 99};
  
  // this is the number of bytes of data to write before we insert the startPattern sequence
  int numBytesToWrite = 1000000;
  
  // this is the current location in the file
  long pos;
  long totalBytesWritten;
  
  /**
   * Create a new RecordSerializer that writes to the given DataOutputStream
   */
  public OutputFileSerializer (DataOutputStream out) {

    writeToMe = RecordCompression.getOutputStream(out);
    pos = numBytesToWrite + 1;
    totalBytesWritten = 0;
  }
  
  /**
   * Create a new RecordSerializer that writes to the given file
   */
  public OutputFileSerializer (String pathToWriteTo) {
    
    try {
      FileOutputStream outFileStream = new FileOutputStream (pathToWriteTo);
      BufferedOutputStream outBufStream = new BufferedOutputStream (outFileStream, 32768);

      // writeToMe = new DataOutputStream (outBufStream);
      writeToMe = RecordCompression.getOutputStream(outBufStream);
      pos = numBytesToWrite + 1;
      totalBytesWritten = 0;
    } catch (Exception e) {
      throw new RuntimeException ("for some reason I could not create the file you asked for"); 
    }
  }
  
  public void write (WritableKey nothing, WritableValue value) throws IOException {
      if (value instanceof RecordWrapper) {
	  write(null, ((RecordWrapper)value).getWrappedRecord());
	  return;
      }

      write(null, (Record)value);
  }


  public synchronized void write (Nothing nothing, Record value) throws IOException {
   
    // if we have written enough bytes, then write the startPattern
    if (pos > numBytesToWrite) {
      writeToMe.write (startPattern, 0, startPattern.length);
      pos = 0;
      totalBytesWritten += startPattern.length;
    }
    
    // since "key" and "value" should defintely be equal here, we only need to 
    // write one of them
    long total = value.writeSelfToStream (writeToMe);
    pos += total;
    totalBytesWritten += total;    
  }

  public long totalBytesWritten() {
    return totalBytesWritten;
  }
  
  public synchronized void close () throws IOException {
    if (writeToMe != null)
      writeToMe.close();
  }
  
  public synchronized void close (TaskAttemptContext context) throws IOException {      
    if (writeToMe != null)
      writeToMe.close();

    // add up the number of bytes written to our special counter...
    context.getCounter(Counters.BYTES_WRITTEN).increment(totalBytesWritten);
  }
}
