

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

import javax.tools.JavaCompiler; 
import javax.tools.ToolProvider;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector; 
import javax.tools.JavaFileObject; 
import javax.tools.StandardJavaFileManager; 
import java.util.Arrays;
import java.io.*;
import java.util.jar.*;
import java.util.zip.*; 
import java.util.*;

// this class encapsultes the functionality needed to compile a java code and add it to a jar
class Compiler {
  
    private JavaCompiler compiler = ToolProvider.getSystemJavaCompiler ();
 
    /**
     * This compiles some java codes and then adds them to a jar. The params are as follows:
     * 
     * workingDir is the name of the directory where all of the magic is going to happen.  It
     * should not currently have any .class files in it, just to ensure that we don't end up
     * deleting them (if there are some .class files, they will be deleted).
     * 
     * jarToAddTo is a .jar file that is going to have the result of the compilation added to
     * it.  It must reside in workingDir.
     * 
     * newJarName is the .jar file that will come out of this whole process.  It will be put into
     * the workingDir directory.
     * 
     * compilationJars is a list of .jar files that will be used to do the compilation.  It will
     * be used to form the classpath arg for the compiler.  Again, these should all be located
     * in the workingDir directory.
     * 
     * filesToCompile is a list of .java files that should be compiled and added to the new .jar.
     * Again, they should reside in the workingDir directory.
     *
     * A true is returned on success.  A RuntimeException is thrown if there are any problems.
     * 
     */
    public boolean getNewJar (String workingDir, String jarToAddTo, 
			      String newJarName, String [] compilationJars, String [] filesToCompile) {
   
	// do we have a compiler?
	if (compiler == null)
	    throw new RuntimeException ("Could not create the compiler.");

	// now, we need to inflate the jar -- but first, we have to
	// check if it's a jar or a directory
	if (new File(jarToAddTo).isDirectory()) {

	    // in this case, we copy everything in the simsql/runtime path
	    for (File ff : new File(jarToAddTo + "/simsql/runtime/").listFiles()) {

		try {
		    BufferedInputStream bis = new BufferedInputStream(new FileInputStream(ff));
		    FileOutputStream fos = new FileOutputStream(new File(workingDir + "/simsql/runtime/", ff.getName()));

		    while (bis.available() > 0) {
			fos.write(bis.read());
		    }

		    bis.close();
		    fos.close();
		} catch (Exception e) {
		    throw new RuntimeException("Could not copy source directory files for compilation.", e);
		}
	    }
	}
	else {

	    // otherwise, it's an actual jar file.

	    // so now we will inflate the jar into the working dir... then the compilation will effectively
	    // replace any .class files that are already there
	    
	    try {
		JarFile input = new JarFile (new File(jarToAddTo), false, ZipFile.OPEN_READ);
		Enumeration <JarEntry> enumeration = input.entries();
		while (enumeration.hasMoreElements()) {
        
		    // get the next entry from the jar
		    JarEntry entry = enumeration.nextElement();
		    if (!entry.isDirectory()) {
          
			// do not copy anything from the package cache
			if (entry.getName().indexOf("package cache") != -1)
			    continue;

			// copy only things from the simsql/runtime package
			if (entry.getName().indexOf("simsql/runtime") == -1)
			    continue;
          
			File file = new File (workingDir + "/" + entry.getName ());
          
			// if the directory is not there, then make it
			if (!file.getParentFile().exists()) {
			    file.getParentFile().mkdirs();
			}
          
			// and finally write the file
			FileOutputStream out = new FileOutputStream (file);
			BufferedInputStream in = new BufferedInputStream(input.getInputStream (entry));
			int data;
			while (in.available() > 0) {
			    out.write(in.read());
			}

			in.close();
			out.close();
		    }
		}
		input.close ();
	    } catch (Exception e) {
		throw new RuntimeException ("When trying to unzip the jar to add to it, got an I/O problem", e); 
	    }
	}

	// NOW it is time to actually compile the Java source files... we begin by setting up the compiler args
	DiagnosticCollector <JavaFileObject> diagnostics = new DiagnosticCollector <JavaFileObject>();
	StandardJavaFileManager fileManager = compiler.getStandardFileManager (diagnostics, null, null);
	ArrayList <String> fileList = new ArrayList <String> ();
	for (int i = 0; i < filesToCompile.length; i++) {
	    fileList.add (workingDir + "/" + filesToCompile[i]);
	}
	Iterable <? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromStrings (fileList);
    
	// now we build up the classpath arg and use it to create the task
	JavaCompiler.CompilationTask task;

	// in this case, we need to build up the classpath
	if (compilationJars.length != 0) {
	    String [] options = new String [2];
	    options[0] = "-cp";
	    String classPath = "";
	    classPath = compilationJars[0];
	    for (int i = 1; i < compilationJars.length; i++) {
		classPath += ":" + workingDir + "/" + compilationJars[i];
	    }
	    options[1] = classPath;
	    task = compiler.getTask (null, fileManager, diagnostics, Arrays.asList (options), null, compilationUnits);
      
	    // in this case, there were no jars to include in the classpath
	} else {
	    task = compiler.getTask (null, fileManager, diagnostics, null, null, compilationUnits);
	}
     
	// run the compiler
	try {
	    boolean success = task.call();
	    if (!success) {
			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				// read error dertails from the diagnostic object
				System.out.println(diagnostic.getMessage(null));
			}
			throw new RuntimeException("Error.");
		}
	} catch (Exception e) {
            e.printStackTrace ();
	    throw new RuntimeException("I could not compile the java source I created.", e);
	}

	try {
	    fileManager.close ();
	} catch (Exception e) {
	    throw new RuntimeException ("Strange error closing the compiler output"); 
	}
        
	// finally, it is time to manufacture the new jar
	try {
	    FileOutputStream out = new FileOutputStream (newJarName);
	    JarOutputStream jarOut = new JarOutputStream (out);
	    CRC32 crc = new CRC32();
	    File workDir = new File(workingDir);
	    makeJar (workDir, jarOut, crc, workDir.getAbsolutePath().length() + 1, new byte[1024 * 1024]);
	    jarOut.close ();
	} catch (Exception e) {
	    throw new RuntimeException ("Got some sort of IO exception when manufacturing the new jar");
	}
    
	// get outta here: we are done!
	return true;
    }
  
    // this recursively adds all of the .class files in the directory to the specified JAR, and THEN IT
    // DELETES THEM
    private void makeJar (File directory, JarOutputStream out, CRC32 crc, int sourceDirLength, byte [] buffer) 
	throws IOException {
    
	File [] files = directory.listFiles ();
	for (int i = 0; i < files.length; i++) {
      
	    // if it is a directory, then recursively add it
	    if (files[i].isDirectory ()) {
        
		makeJar (files[i], out, crc, sourceDirLength, buffer);
        
		// otherwise we write it
	    } else {
        
		// only jar class, .obj, and C library files
		if (files[i].getPath ().indexOf (".class") == -1 && 
                    files[i].getPath ().indexOf (".obj") == -1 &&
                    files[i].getPath ().indexOf (".so") == -1) {
		    continue;
		}
        
		// get the name of the entry by chopping off the prefix
		String entryName = files[i].getAbsolutePath ().substring (sourceDirLength);
		JarEntry entry = new JarEntry (entryName);
        
		// now do the compression
		out.putNextEntry (entry);
		FileInputStream in = new FileInputStream (files[i]);
		int data = 0, size = 0;
        
		// copy the bytes over
		while ((data = in.read (buffer)) != -1) {
		    crc.update (buffer, 0, data);
		    out.write (buffer, 0, data);
		    size += data;
		}
        
		// and close everything up
		entry.setCrc (crc.getValue());
		entry.setSize (size);
		in.close ();
		out.closeEntry ();
		crc.reset ();
        
		// now delete the class file
		files[i].delete ();
	    }
	}
    }
}
