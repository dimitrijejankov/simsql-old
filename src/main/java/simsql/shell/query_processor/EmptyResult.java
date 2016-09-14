

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
import simsql.shell.RuntimeOutput;

import java.io.PrintStream;

// this class encapsulates a result that never holds anything 
public class EmptyResult implements RuntimeOutput {
  
  private String errMsg = null;
  
  // this should be called to record an error
  public void setError (String setToMe) {
    errMsg = setToMe;
  }
  
  // this is called to get the error if it is there
  public String isError () {
    return errMsg;
  }
}
