

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



package simsql.shell.kernel;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashSet;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Date;

// this class goes through the current directory and cleans up the mess that the last query left
public class Cleaner {

	// this stores the complete list of files that were there before query execution
	private HashSet <String> wereThereToBegin = new HashSet <String> ();

	// this is where the cleaned up results go
	private File theDir;

	// the Cleaner object should be created **before** the query is run...
	// it will put all of the results in the file indicated
	public Cleaner (String whereToPutTheMess) {

		// create the directory to stick everything in, if needed
		theDir = new File(whereToPutTheMess);

		// if the directory does not exist, create it
		if (!theDir.exists()) {
    			theDir.mkdir();  
		}

		// remember all of the files that are now here
		File dir = new File(".");
		File[] filesList = dir.listFiles();
		for (File file : filesList) {
			if (file.isFile() || file.isDirectory ()) {
				wereThereToBegin.add (file.getName());
			}
		}
	}

	// this is called to clean everything up
	public void cleanUp () {

		// get the date time
		DateFormat dateFormat = new SimpleDateFormat("yyyy_MM_dd_HH:mm:ss");
		Date date = new Date();
		String dirName = dateFormat.format(date);

		try {
			// create a new directory for all of this stuff
			File newDir = new File (theDir, dirName);
	
			// create the target directory
			if (!newDir.exists ()) {
				newDir.mkdir ();
			}
	
			// now check all of the files, and move them if necessary
			File dir = new File(".");
			File[] filesList = dir.listFiles();
			for (File file : filesList) {
				if ((file.isFile() || file.isDirectory ()) && 
				     !wereThereToBegin.contains (file.getName ()) &&
				     (file.getName ().contains ("Aggregate_") ||
				      file.getName ().contains ("Selection_") ||
				      file.getName ().contains ("VGWrapper_") ||
				      file.getName ().contains ("Join_") ||
				      file.getName ().contains ("Temp_file_") ||
				      file.getName ().contains ("work_"))) {
					Files.move (file.toPath (), new File (newDir, file.getName ()).toPath ());
				}
			}
		} catch (Exception e) {
			e.printStackTrace ();
		}
	}

}
