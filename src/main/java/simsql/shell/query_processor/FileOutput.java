

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



package simsql.shell.query_processor;

import simsql.shell.CompiledQuery;
import simsql.shell.ExecutableQuery;
import simsql.shell.OptimizedQuery;

import java.io.File;
public class FileOutput implements CompiledQuery, OptimizedQuery, ExecutableQuery {
  
  // this is the name of the file
  private String fName = null;
  
  // this is the error message (if there)
  private String errMsg = null;
  
  // tells if there is nothing to process
  private boolean isEmp = false;
  
  // record the fact that there is nothig to process in the file
  public void setEmpty () {
    isEmp = true;
  }
  
  // see if the thing is empty and there is nothing to process
  public boolean isEmpty () {
    return isEmp;
  }
  
  // this gets the name of the file
  public String getFName () {
    return fName;
  } 
  
  // this constructor creates a new (empty) file with the given extension
  public FileOutput (String extension) {
    
    // loop through all of the file names until we sucessfully create one
    try {
      for (int i = 0; true; i++) {
        fName = "Temp_file_" + i + extension;
        if (new File (fName).createNewFile ()) {
          break; 
        }
      }
    } catch (Exception e) {
      e.printStackTrace ();
      throw new RuntimeException ("Problem occurred when I was trying to create a temp file");
    }
  }
  
  // this should be called to record an error
  public void setError (String setToMe) {
    errMsg = setToMe;
  }
  
  // this is called to get the error if it is there
  public String isError () {
    return errMsg;
  }
  
  // and this deletes the underlying file
  public void cleanUp () {
    new File (fName).delete ();
  }
}
