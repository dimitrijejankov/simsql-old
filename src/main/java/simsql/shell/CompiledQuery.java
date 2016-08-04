

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

/**
 * This encapsultes a compiled query
 */
public interface CompiledQuery {

  // If there was an error encountered when compiling the query, this function
  // should return a nice text that describes the error.  If there was no error,
  // then this returns a "null"
  String isError ();
  
  // see if the thing is empty and there is nothing to process... this might happen
  // if we are given a CREATE TABLE statement, for example
  public boolean isEmpty ();

  // this "cleans up" the compiled query, by (for example) removing any files that
  // were created to store the contents of the query
  void cleanUp ();
  
}
