

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



package simsql.shell;
import java.io.File;

// this encapsulates the set of parameters (number of CPUs, memory, and any other configs) that
// the runtime needs in order to be able to run the query 
public interface RuntimeParameter {

    // lists all runtime parameters
    void listParams();

    // sets a given parameter value
    boolean setParam(String paramName, String paramValue);

    // lists all functions
    void listFunctions();

    // lists all vg functions
    void listVGFunctions();

    // this prompts the user for all of the necessary parameters
    void setUp (File where);

    // or uses a bunch of default factory settings
    void setUpDefaults (File where);

    // returns the CREATE statements for the default functions
    String[] getFunctionCTs();
    String[] getVGFunctionCTs();
    
    // this attemps to read all of the parameters from the file indicated; if there is a problem
    // or if the file does not exist, a false is returned; otherwise, a true comes back
    boolean getFromFile (File which);
  
    // this saves the runtime params to a file... a call to this method must happen before the system shuts down
    void save ();

    // this takes an executable VG function (as a file) and writes it into this RuntimeParameter object
    public boolean readVGFunctionFromFile (String vgName, File whereToReadFrom);
  
    // this takes an executable function (as a file) and writes it into this RuntimeParameter object
    public boolean readFunctionFromFile (String funcName, File whereToReadFrom);
}
