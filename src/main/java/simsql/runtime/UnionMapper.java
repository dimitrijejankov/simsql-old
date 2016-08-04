

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

import org.apache.hadoop.mapreduce.Mapper;
import java.io.IOException;
import org.apache.hadoop.mapreduce.lib.input.FileSplit; 
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import java.util.HashSet;
import java.lang.reflect.Constructor;
import java.util.Iterator;
import org.apache.hadoop.mapreduce.TaskInputOutputContext;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

/*
 * The mapper for the union operator.
 */
class UnionMapper extends MapperWithPipeNetwork {

  // mutables, because memory is precious.
  RecordKey rk = new RecordKey();
  RecordWrapper rw = new RecordWrapper();

  // the stupid Nothing type
  Nothing nothing = new Nothing ();

  // record whether or not we need to be sorting
  boolean sorting;

  // this is an array of records that the mapper will use to create the
  // records it produces as it sends them along to the map function
  Record [] prototypeRecords;

  // this basically runs a map over all of the files that are not directly processed by Hadoop MapReduce
  public void setup(Context context) throws IOException, InterruptedException {

    // get the list of prototype records
    Class<?> [] possibleRecordTypes = RecTypeList.getPossibleRecordTypes ();

    // go through and create dummy records of each type
    prototypeRecords = new Record [possibleRecordTypes.length];
    for (int i = 0; i < possibleRecordTypes.length; i++) {
      // extract the record's constructor and create a version of it
      try {
        Constructor <?> myConstructor = possibleRecordTypes[i].getConstructor ();
        prototypeRecords[i] = (Record) myConstructor.newInstance ();
      } catch (Exception e) {
        throw new RuntimeException ("I could not find a valid contructor for one of the record types I was passed");
      }
    }

    String taskId = context.getConfiguration ().get("mapred.task.id");
    String[] parts = taskId.split("_");
    int whichMapper = Integer.parseInt (parts[4]);

    /*// first we figure out which mapper this is
    FileSplit fileSplit = (FileSplit) context.getInputSplit();
    String filename = fileSplit.getPath().getName();
    int whichMapper = RecordOutputFormat.getPartNumber(filename);*/
    
    // now we figure out the number of mappers that there are gonna be
    FileSystem fs = FileSystem.get (context.getConfiguration ());
    Path path = new Path (context.getConfiguration ().get ("simsql.fileToMap"));
    FileStatus [] fstatus = fs.listStatus (path, new TableFileFilter());
    int numMappers = fstatus.length;

    // see if we need to be sorting
    sorting = context.getConfiguration ().getBoolean("simsql.runUnionReducer", false);

    // and now, we go ahead and go through all of the files that need to be unioned, and see
    // if they hash to us.  If they do, then we need to iterate through them
    String filesNotMapped = context.getConfiguration ().get ("simsql.filesToDirectlyRead", "");
    String [] filesToDirectlyRead = filesNotMapped.split (",");

    for (String s : filesToDirectlyRead) {

      // OK, we now have a directory to process
      try {
        path = new Path (s);
      } catch (Exception e) {
        throw new RuntimeException ("filesToDirectlyRead was " + filesNotMapped + " and I got " + s);
      }

      fstatus = fs.listStatus (path, new TableFileFilter());

      if (fstatus.length == 0)
	throw new RuntimeException ("Got no files");

      // look through each file, and process a random subset of them
      String allFiles = "";
      for (FileStatus f : fstatus) {

        // check if we are tasked with processing this guy
        int whichOne = 0;
        if (f.getPath ().toString ().hashCode () < 0)
          whichOne = (-f.getPath ().toString ().hashCode ()) % numMappers;
        else
          whichOne = f.getPath ().toString ().hashCode () % numMappers;
        allFiles += whichOne + " ";
        if (whichOne == whichMapper) {

          // get an iterator for this file
	  IteratorFactory myFactory = new IteratorFactory ();
          Iterator <Record> smallFile = myFactory.getIterableForFile (f.getPath ().toString (), 
		context.getConfiguration ().getInt ("simsql." + s + ".numAtts", -1),
		(short) context.getConfiguration ().getInt ("simsql." + s + ".typeCode", -1)).iterator ();

          // and process all of the records in there
          while (smallFile.hasNext ()) {

            // put the record into the pipe network
            Record temp = smallFile.next ();
            process (temp);

            // and get all of the records that come out of the pipe network
            Record nextTemp;
            while ((nextTemp = getResult ()) != null) {

	      // get the correct type for the record
              boolean gotOne = false;
              for (Record r : prototypeRecords) {

                if (r.getTypeCode () == nextTemp.getTypeCode ()) {
                  r.copyMyselfFrom (nextTemp);
                  map (nothing, r, context);
                  nextTemp.recycle ();
                  gotOne = true;
                  break;
                }
              }

              if (!gotOne) 
                throw new RuntimeException ("Didn't find a match!");
            }

            temp.recycle ();
          }

	} 
      }
       //   throw new RuntimeException (allFiles + " " + whichMapper);
    }
  }

  public void map(Nothing key, Record value, Context context) throws IOException, InterruptedException {

    // first, cast "value" to the InputRecord type
    InputRecord input;
    try {
      input = (InputRecord) value;
    } catch (Exception e) {
      throw new RuntimeException ("For some reason, the cast of the input record in the mapper failed! " + value.getClass ().toString ()); 
    }

    // run the selection and projection
    HashableRecord myRec = input.runSelectionAndProjection ();

    // if it survived the selection
    if (myRec != null) {

      
      // if we are doing a sort, then we need to split
      if (sorting) {
 
        HashableRecord[] splits = myRec.splitSome();
        
	// go through all of the pslit results
        for (HashableRecord rec: splits) {
      
          if (rec.getIsPresent ().allAreFalseOrUnknown ())
            continue;

          // send it to the reducer
          rk.set(rec.getHashKey (), rec.getTypeCode ());
          rw.set(rec);
          context.write (rk, rw);
          rec.recycle (); 

        }

      // we are not sorting; just write the output
      } else {
        if (!myRec.getIsPresent ().allAreFalseOrUnknown ())
          context.write (nothing, myRec);
      } 

      // and save the RAM
      myRec.recycle ();
     
    }
  }
}

