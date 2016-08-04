

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

import java.util.*;
import java.io.*;
import java.lang.*;

/**
 * This class is used to do a macro replace in the file "inFile", writing the results
 * to "outFile".  The map "replacements" has as its keys the strings that are to be 
 * replaced, and as its values, the replacement strings.  It is assumed that the keys
 * in the file are demarcated using the string "<<<<key>>>>".  For each key of this form,
 * we look up the key in "replacements", and then replace the  "<<<<key>>>>" with the
 * vlue that is found.
 */
class MacroReplacer {
 
  public MacroReplacer (String inFile, String outFile, Map <String, String> replacements) {
    
    // read the file into a string
    try {
    /*** CHANGED BY LUIS -- using the resource InputStream now.
    File file = new File (inFile);
    FileReader converter = new FileReader (file);

    
    int fileSize = (int) file.length ();
    char [] buf = new char[fileSize + 10];
    fileSize = converter.read (buf);
    String fileText = String.valueOf (buf, 0, fileSize);
    */

    InputStream converter = MacroReplacer.class.getResourceAsStream(inFile);
    StringBuilder builder = new StringBuilder();
    while (converter.available() > 0) {
	builder.append((char)converter.read());
    }

    String fileText = builder.toString();
    
    // set up the output file
    FileWriter myWriter = new FileWriter (outFile);
    BufferedWriter out = new BufferedWriter (myWriter);
    
    // first we split up the file on every instance of "<<<<"
    String [] results = fileText.split ("<<<<");
    out.write (results[0]);
    
    // now we go through, and further split up the string, getting the key, 
    for (int i = 1; i < results.length; i++) {
      String [] smallResult = results[i].split (">>>>");
    
      // make sure the replacement is there
      String replacement = replacements.get (smallResult[0]);
      if (replacement != null) {
        out.write (replacement);
      }
      
      // write all of the stuff that comes after it
      out.write (smallResult[1]);
    }
    
    // now close up the file
    out.close ();
    } catch (Exception e) {
      e.printStackTrace ();
      throw new RuntimeException ("error when I was performing the macro substitution");
    }
  }
}
