

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

import org.antlr.runtime.*;
import java.io.*;
import java.util.*;

public class Interpreter {
  
  public static void main (String [] args) throws Exception {
    
    int splitSize = 1024 * 1024 * 128;
    int numRed = 1;
    String fileToRun = null;
    
    // now, parse the input string
    for (int i = 0; i < args.length; i++) {
      
      if (args[i].equals ("-splitSize")) {
        i++;
        splitSize = Integer.decode (args[i]);
        
      } else if (args[i].equals ("-numRed")) {
        i++;
        numRed = Integer.decode (args[i]);
 
      } else if (args[i].equals ("-fileToRun")) {
        i++;
        fileToRun = args[i];
      } else {
        System.out.println ("Flags: -splitSize -numRed -fileToRun");
        return;
      }
    }
    
    if (fileToRun == null) {
      System.out.println ("-fileToRun flag is mandatory");
      return;
    }
    
    try {
      
      // read the file into a string
      File file = new File (fileToRun);
      FileReader converter = new FileReader (file);
      int fileSize = (int) file.length ();
      char [] buf = new char[fileSize + 10];
      fileSize = converter.read (buf);
      String toParse = String.valueOf (buf, 0, fileSize);
      
      // then parse the string and get a set of relational operations to run
      ANTLRStringStream parserIn = new ANTLRStringStream (toParse);
      DataFlowLexer lexer = new DataFlowLexer (parserIn);
      CommonTokenStream tokens = new CommonTokenStream (lexer);
      DataFlowParser parser = new DataFlowParser (tokens);
      ArrayList <RelOp> result = parser.parse ();
      
      // first we collect all of the files that will be produced by some operation
      ArrayList <String> filesIAmWaitingOn = new ArrayList <String> ();
      for (RelOp r : result) {
        filesIAmWaitingOn.add (r.getOutput ());
      }
      
      // now we keep on running the various operations, repeatedly choosing one that does not have a dependence
      for (int i = 0; i < result.size (); i++) {
        
        // get the files this guy needs
        ArrayList <String> filesHeNeeds = new ArrayList <String> (Arrays.asList (result.get (i).getInputs ()));
        
        // see if we are waiting on any of them
        boolean ok = true;
        for (String s1 : filesHeNeeds) {
          for (String s2: filesIAmWaitingOn) {
            if (s1.equals (s2)) {
              ok = false;
              break;
            }
          }
          if (!ok) {
            break;
          }
        }
        
        // see if we can run this guy
        if (ok) {
          
          // run the operation
          System.out.println ("Running operation to produce " + result.get (i).getOutput ());
          // result.get (i).run (numRed, splitSize);
          
          // remove him from the list of ones we are waiting on
          result.remove (i);
          
          // and re-do the strings we are waiting on
          filesIAmWaitingOn = new ArrayList <String> ();
          for (RelOp r : result) {
            filesIAmWaitingOn.add (r.getOutput ());
          }
          
          // and start over!
          i = -1;
        }
      }
      
      // if we got here and there are still operations left, we have a problem
      if (result.size () != 0) {
        throw new RuntimeException ("One or more operations could not be run!!!  How could this happen!?!?"); 
      }
    } catch (Exception e) {
      e.printStackTrace ();
    } 
  }
}
