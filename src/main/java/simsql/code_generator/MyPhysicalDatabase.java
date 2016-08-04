

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



package simsql.code_generator;

import jline.*;
import simsql.shell.*;
import simsql.runtime.*;
import java.io.*;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.mapreduce.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import simsql.compiler.Relation;
import org.apache.hadoop.conf.Configuration;
import java.util.*;

// this is going to be a simple implementation of the PhysicalDatabase interface...
// note that in addition to the standard interface, it also has some methods that
// are used internally by the translator/code generator: getTypeCode and getPhysicalFile
public class MyPhysicalDatabase implements PhysicalDatabase <DataFlowQuery> {

  // this is the Hadoop directory where this database is stored
  private String myDir;

  // this is where we save ourselves
  private File whereIAm;

  // this tells us the physical information for each of the database files
  private HashMap <String, DatabaseFile> physicalInfo = new HashMap <String, DatabaseFile> ();

  // stores the last type code that has been used
  private Short lastUsedTypeCode = 0;

  // renames the given relation
  public void rename (String oldName, String newName) {
    physicalInfo.put (newName, physicalInfo.remove (oldName));
  }

  // cleans up the physical database by removing all of the zero-byte relations whose name containts the given string
  public void removeZeroSizeRelations (String containing) {

    // first we compute everyone to clean up
    ArrayList <String> killMe = new ArrayList <String> ();
    for (String s: physicalInfo.keySet()) {
      if (s.contains (containing)) {
	// check the size
        DatabaseFile temp = physicalInfo.get (s);
        if (temp.getSizeInBytes () == 0)
          killMe.add (s);
      }
    }

    // next we clean them up
    for (String s : killMe) {
      deleteTable (s);
    }
    
  }

  public boolean registerTable (String relName) {
  
    lastUsedTypeCode++;
    if (lastUsedTypeCode >= Short.MAX_VALUE) 
      throw new RuntimeException ("Ran out of type codes... guess we should implement re-use!");

    DatabaseFile temp = new DatabaseFile (relName + "_" + lastUsedTypeCode, lastUsedTypeCode);
    
    if (physicalInfo.put (relName, temp) != null) {
      return false;
    }

    return true;
  }

  public boolean registerTable (String relName, String physicalTable, short typeCode) {
    DatabaseFile temp = new DatabaseFile (physicalTable, typeCode);
    
    if (physicalInfo.put (relName, temp) != null) {
      System.out.println ("When trying to associated a new relation with a physical table, I found that" +
			  " this relation already exists!");
      return false;
    }

    return true;
  }

  public boolean unregisterTable (String relName) {

    // if the file does not exist, get outta here
    if (getFileName (relName) == null) {
      System.out.println ("trying to kill file " + relName + " that I've never seen!");
      return false;
    }

    physicalInfo.remove (relName);
    return true;
  } 

  // this returns the type code that has been assigned to the records in the specified relation
  public short getTypeCode (String relationName) {

    // find the corresponding database file
    DatabaseFile temp = physicalInfo.get (relationName);
    if (temp == null)
      return -1;

    return temp.getTypeCode ();
  }

  // this returns the physical file that actually stores the records in this particular relation
  public String getFileName (String relation) {

    // find the corresponding database file
    DatabaseFile temp = physicalInfo.get (relation);
    if (temp == null)
      return null;

    return myDir + "/" + temp.getDirName ();
  }

  // this returns a particular relation given the name of its physical file
  public String getTableName (String fileName) {

    for (String s: physicalInfo.keySet()) {
      if ((myDir + "/" + physicalInfo.get(s).getDirName()).equals(fileName)) {
	return s;
      }
    }

    return null;
  }

  // and this attempts to load this particular physical database description from a file 
  @SuppressWarnings("unchecked") 
    public boolean getFromFile (File file) {

    whereIAm = file;

    FileInputStream fis = null;
    ObjectInputStream in = null;
    try {
      fis = new FileInputStream (file);
      in = new ObjectInputStream (fis);
      myDir = (String) in.readObject ();
      physicalInfo = (HashMap <String, DatabaseFile>) in.readObject ();
      lastUsedTypeCode = (Short) in.readObject ();
      in.close ();
    } catch (Exception e) {
      return false;
    }

    return true;
  }

  public void backupTo (String toHere) {

    ConsoleReader cr = null;
    try {
      cr = new ConsoleReader();
    } catch (Exception e) {
      throw new RuntimeException("Could not create a console for reading!", e);
    }

    try {

      // first, we see if the directory that we are writing to exists
      Configuration conf = new Configuration ();
      FileSystem dfs = FileSystem.get (conf);

      Path pathTo = new Path (toHere);			
      if (dfs.exists(pathTo)) {
	System.out.format("The specified path already exists. Would you like to overwrite it? [Y/N] ");

	char answer = (char)cr.readCharacter(new char[]{'Y', 'N', 'y', 'n'});
	System.out.println(answer);

	if (answer == 'Y' || answer == 'y') {
	  dfs.delete(pathTo, true);
	} else {

	  // otherwise, we don't proceed.
	  pathTo = null;
	}
      }

      // do we continue?
      if (pathTo != null) {

	// make it a directory
	// dfs.mkdirs(pathTo);

	// get our current directory.
	Path pathFrom = new Path (myDir);

	// and all the paths of the in our directory
	Path[] sourcePaths = FileUtil.stat2Paths(dfs.globStatus(pathFrom), pathFrom);
	for (Path sp: sourcePaths) {
					
	  // copy all of it.
	  FileUtil.copy(dfs, sp, dfs, pathTo, false, conf);
	}
      }

    } catch (Exception e) {
      throw new RuntimeException("Could not back up data into directory " + toHere, e);
    }
  }

  public void restoreFrom (String fromHere) {

    try {

      // first, we see if the directory that we are reading from
      // exists and is a directory.
      Configuration conf = new Configuration ();
      FileSystem dfs = FileSystem.get (conf);

      Path pathFrom = new Path (fromHere);
      if (!dfs.exists(pathFrom) || !dfs.isDirectory(pathFrom)) {
	System.out.println("The specified restoration path does not exist or is not a directory!");
	return;
      }

      // now, get the destination path.
      Path pathTo = new Path (myDir);
      if (dfs.exists(pathTo)) {

	// destroy it, if it's there.
	dfs.delete(pathTo, true);
      }

      // make the directory
      // dfs.mkdirs(pathTo);

      // and all the paths we will be copying
      Path[] sourcePaths = FileUtil.stat2Paths(dfs.globStatus(pathFrom), pathFrom);
      for (Path sp: sourcePaths) {

	// restore all of it.
	FileUtil.copy(dfs, sp, dfs, pathTo, false, conf);
      }
			
    } catch (Exception e) {
      throw new RuntimeException("Could not restore data from directory " + fromHere, e);
    }		
  }

  /** Easy setup stuff for the BUDS translator -- added by Luis. */
  public void setUp (File toHere, String someDir) {
    whereIAm = toHere;
    myDir = someDir;
  }

  public void setUp (File toHere) {
    
    whereIAm = toHere;

    ConsoleReader cr = null;
    try {
      cr = new ConsoleReader();
    } catch (Exception e) {
      throw new RuntimeException("Could not create a console for reading!", e);
    }

    myDir = null;
    while (myDir == null) {

      try {

	myDir = cr.readLine("In what Hadoop directory do you want to store all of your database files? ");
	if (myDir.charAt (0) != '/') {
	  myDir = "/" + myDir;
	}
				
	// first, we see if the directory that we are writing to exists
	Configuration conf = new Configuration ();
	FileSystem dfs = FileSystem.get (conf);

	// it we can't find it in the configuration, then create it
	Path path = new Path (myDir);
	if (dfs.exists(path)) {
	  System.out.format("The specified path already exists. Would you like to overwrite it? [Y/N] ");
	  char answer = (char)cr.readCharacter(new char[]{'Y', 'N', 'y', 'n'});
	  System.out.println(answer);

	  if (answer == 'N' || answer == 'n') {
	    myDir = null;
	  }
	  else {
	    dfs.delete(path, true);
	  }
	}

	if (myDir != null) {
	  dfs.mkdirs(path);
	}
      } catch (Exception e) {
	throw new RuntimeException("Could not create the Hadoop directory for your database!", e);
      }
    }
  }

  public void save () {
    FileOutputStream fos = null;
    ObjectOutputStream out = null;
    try {
      fos = new FileOutputStream (whereIAm);
      out = new ObjectOutputStream (fos);
      out.writeObject (myDir);
      out.writeObject (physicalInfo);
      out.writeObject (lastUsedTypeCode);
      out.close ();
    } catch (Exception e) {
      e.printStackTrace ();
      System.out.println ("Got some sort of error writing the physical database description to a file");
    } 
  }

  // this removes the table from HDFS, as well as our memory
  public void deleteTable (String relName) {

    if (relName == null)	
      return;

    // if the file does not exist, get outta here
    if (getFileName (relName) == null)
      return;

    try {
      // first, we see if the directory that we are deleting exists
      Configuration conf = new Configuration ();
      FileSystem dfs = FileSystem.get (conf);

      // if it does exist then we kill it
      Path path = new Path (getFileName (relName));
      if (dfs.exists(path) && dfs.getFileStatus (path) != null) {
        dfs.delete (path, true);
      }
    } catch (Exception e) {
      throw new RuntimeException ("Error when trying to delete the physical table " + relName, e);
    }


    // and remove it from our map
    unregisterTable (relName);
  }

  public TableStatistics<Record> loadTable (Relation loadMe, String inputFile, RuntimeParameter params, String sortAtt) throws IOException {

    // get the params.
    ExampleRuntimeParameter pp = (ExampleRuntimeParameter)params;
    Configuration conf = new Configuration ();

    // get the DFS block size
    long dfsBlockSize = conf.getLong("dfs.blocksize", 1024 * 1024 * 128);

    // get the filesize and check.
    long fSize = inputFile.startsWith("hdfs:") ? Long.MAX_VALUE : inputFile.length();
    DataLoader dl;

    // get the position of the sorting attribute.
    int sortAttPos = -1;
    if (sortAtt != null) {
      int i = 0;
      for (simsql.compiler.Attribute a : loadMe.getAttributes()) {
	if (a.getName().equalsIgnoreCase(sortAtt)) {
	  sortAttPos = i;
	  break;
	}
	i++;
      }
    }

    // straight loader if (1) small relation; (2) not sorted and (3) not from HDFS.
    if (fSize < dfsBlockSize && sortAttPos == -1 && !inputFile.startsWith("hdfs:")) {
      dl = new HDFSLoader();
    } else {
      dl = new MRLoader();
      ((MRLoader) dl).setNumTasks(pp.getNumCPUs());
    }

    long newSize = dl.run(inputFile, getFileName(loadMe.getName()), getTypeCode(loadMe.getName()), loadMe, sortAttPos);
    System.out.println("Done! Loaded " + newSize + " raw bytes.");

    // save the file size.
    physicalInfo.get(loadMe.getName()).setSizeInBytes(newSize);

    // save the sorting attribute.
    physicalInfo.get(loadMe.getName()).setSortingAtt(sortAttPos);

    // set the num. atts
    physicalInfo.get(loadMe.getName()).setNumAtts(loadMe.getAttributes().size());

    // get the stats.
    try {
      HDFSTableStats coll = new HDFSTableStats();
      coll.load(getFileName(loadMe.getName()));

      return new TableStatisticsWrapper(coll);
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    }
  }

  public void printRelation(Relation rel) {

    // get the column length
    int nx = (jline.Terminal.setupTerminal().getTerminalWidth() - (rel.getAttributes().size() * 2)) / rel.getAttributes().size();
    if (nx < 10)
      nx = 30;
		
    // print the attribute names
    boolean first = true;
    for (simsql.compiler.Attribute a: rel.getAttributes()) {
      String s = a.getName();
      if (s.length() > nx - 2) {
	s = s.substring(0, nx - 2);
      }

      if (first) {
	System.out.format("%-" + nx + "s", s);
	first = false;
      }
      else {
	System.out.format("| %-" + nx + "s", s);
      }
    }
		
    System.out.format("%n");
    System.out.format("%s%n", new String(new char[nx * rel.getAttributes().size() - 1]).replace("\0", "-"));

    // now, we will print each attribute value
    try {
      /* ----------------------------modified by Bamboo ----------------------------------*/
      int num = 0;
      for (Record rec: getIteratorFactoryForRelation(rel)) {
	for (int i=0;i<rec.getNumAttributes();i++) {
	  if (i > 0) {
	    System.out.print("| ");
	  }

	  System.out.format("%-" + nx + "s", rec.getIthAttribute(i).print(nx));
	}
				
	System.out.format("%n");
	num ++;
        if(num >= 1000)
	  {
	    break;
	  }
      }
      /*------------------------------*/
    } catch (Exception e) {
      throw new RuntimeException("Could not print the relation " + rel.getName(), e);
    }
		
    System.out.format("%n");
  }
	
  public void saveRelation(Relation rel, String outputFile) {
		
    // create the output file
    BufferedWriter out = null;
    try {
      out = new BufferedWriter(new FileWriter(outputFile));
    } catch (Exception e) {
      throw new RuntimeException("Could not create output file " + outputFile, e);
    }

    try {

      // write each record
      // LDA ANNOYANCE
      int n = 0;
      for (Record r: getIteratorFactoryForRelation(rel)) {

	// attribute by attribute.
	for (int i=0;i<r.getNumAttributes();i++) {
	  r.getIthAttribute(i).writeSelfToTextStream(out);
	}

	// separate by newline
	out.newLine();

	n++;
	if (n > 10000) {
	  out.write("----- SNIP -----");
	  break;
	}
      }

      // flush out.
      out.flush();
      out.close();
    } catch (Exception e) {
      throw new RuntimeException("Could not write relation " + rel.getName(), e);
    }
  }

  public Iterable <Record> getIteratorFactoryForRelation (Relation forMe) {
    IteratorFactory temp = new IteratorFactory ();
    return temp.getIterableForFile (getFileName(forMe.getName()), forMe.getAttributes().size(), getTypeCode(forMe.getName()));
  }

  public boolean isTableSorted(String table) {

    DatabaseFile df = physicalInfo.get(table);
    if (df == null)
      return false;

    if (df.getSortingAtt() == -1)
      return false;

    return true;
  }

  public int getTableSortingAttribute(String table) {
    return physicalInfo.get(table).getSortingAtt();
  }

  public long getTableSize(String table) {

    DatabaseFile df = physicalInfo.get(table);
    if (df == null) {
      System.out.println("Unknown relation " + table);
      return 0L;
    }

    return df.getSizeInBytes();
  }

  public void setTableSize(String table, long size) {
    DatabaseFile df = physicalInfo.get(table);
    if (df != null) {
      df.setSizeInBytes(size);
    }
  }

  public void setTableSortingAtt(String table, int sortAttPos) {
    DatabaseFile df = physicalInfo.get(table);
    if (df != null) {
      df.setSortingAtt(sortAttPos);
    }
  }

  public void setNumAtts(String table, int numAtts) {
    DatabaseFile df = physicalInfo.get(table);
    if (df != null) {
      df.setNumAtts(numAtts);
    }    
  }

  public int getNumAtts(String table) {
    return physicalInfo.get(table).getNumAtts();
  }


  // pretty printing for the shell.
  public String toString() {

    String relStr = "\n";
    for (String s: physicalInfo.keySet()) {
      relStr += String.format
	("  - %s%n" +
	 "    location: %s%n" +
	 "    typeCode: %s%n" +
	 "    sizeInBytes: %s%n" +
	 "    sorting attribute: %s%n%n", s, getFileName(s), getTypeCode(s), getTableSize(s), isTableSorted(s) ? getTableSortingAttribute(s) : "n/a");	 
    }
    return String.format
      (
       "HDFS directory: %s%n" +
       "Latest typeCode: %d%n" + 
       "Registered relations: %n" +
       "%s" +
       "%n%n", myDir, lastUsedTypeCode, relStr);       
  }
}
