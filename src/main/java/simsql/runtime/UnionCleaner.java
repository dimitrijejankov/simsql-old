
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
import java.util.Iterator;
import java.util.ArrayList;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;

public class UnionCleaner {

  // allows us to iterate through the records in the current file we are processing
  private Iterator <Record> smallFile = null; 

  // tells us the list of files that we need to process
  private ArrayList <FileStatus> allFiles = null;
  private ArrayList <Integer> whichFiles = null;
  private short [] typeCodes;
  private InputRecord [] allRecs;
  private int numPartitions;
  private int whichPartition;
 
  public UnionCleaner (String [] fileNames, short [] typeCodesIn, InputRecord [] allRecsIn, int numPartitionsIn,
	int whichPartitionIn) {

    // put all of the relecant recs into allRecs; we might have extras in allRecsIn
    allRecs = new InputRecord [typeCodesIn.length];
    for (int i = 0; i < typeCodesIn.length; i++) {
      for (InputRecord ir : allRecsIn) {
        if (typeCodesIn[i] == ir.getTypeCode ())
          allRecs[i] = ir;
      }
    }

    // remember the input params
    typeCodes = typeCodesIn;
    numPartitions = numPartitionsIn;
    whichPartition = whichPartitionIn;

    // get access to the filesystem
    Configuration conf = new Configuration ();
    FileSystem fs;
    try {
      fs = FileSystem.get (conf);
    } catch (Exception e) {
      throw new RuntimeException (e);
    }

    // loop through all of the directories/files that we are supposed to process and remember them
    allFiles = new ArrayList <FileStatus> ();
    whichFiles = new ArrayList <Integer> ();
    for (int i = 0; i < fileNames.length; i++) {

      // list all of the subfiles or files in the directory
      Path path = new Path (fileNames[i]);

      try {
        // and add them to the list
        for (FileStatus fstat : fs.listStatus (path, new TableFileFilter())) {
          allFiles.add (fstat);
        }

        // remember which file they came from
        for (FileStatus fstat : fs.listStatus (path, new TableFileFilter())) {
          whichFiles.add (i);
        }
      } catch (Exception e) {
        throw new RuntimeException (e);
      }
    }
  }

  public Record cleanup () {

    // if we have a record queued up, process it
    while (smallFile != null && smallFile.hasNext ()) {

      // read in the record
      Record temp = smallFile.next ();
      int i;
      for (i = 0; i < allRecs.length; i++) {
        if (typeCodes[i] == temp.getTypeCode ()) {
          allRecs[i].copyMyselfFrom (temp); 
          break;
        }
      }

      // process and return the result
      HashableRecord myRec = allRecs[i].runSelectionAndProjection ();
      if (myRec != null) {
        if (!myRec.getIsPresent ().allAreFalseOrUnknown ())
          return myRec;
      } 
    }

    // if we got here, then it is time to move to the next file
    // repeatedly see if we need to process the next file
    while (allFiles.size () != 0) {

      // get the first file
      FileStatus f = allFiles.remove (0);

      // get the identify of the record contained in the file
      Integer i = whichFiles.remove (0);

      // see if it is ours to process
      int hashVal = f.getPath ().toString ().hashCode ();
      if (hashVal < 0)
        hashVal = -hashVal;
      if (hashVal % numPartitions == whichPartition) {

	// if it is, build an iterator for it
        IteratorFactory myFactory = new IteratorFactory ();
        smallFile = myFactory.getIterableForFile (f.getPath ().toString (), 
		allRecs[i].getNumAttributes (), allRecs[i].getTypeCode ()).iterator ();

        // and we are outta here
        return cleanup ();
      }
    }
    
    // if there are no files to process, then get outta here
    return null; 
  }

}

