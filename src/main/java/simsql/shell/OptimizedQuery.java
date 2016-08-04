

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

// this wraps up an opimtized query
public interface OptimizedQuery {
  
  // If there was an error encountered when optimizing the query, this function
  // should return a nice text that describes the error.  If there was no error,
  // then this returns a "null"
  String isError ();
  
  // If the thing is empty so that there is nothing to do, then this returns true
  boolean isEmpty ();

  // this "cleans up" the optimized query, by (for example) removing any files that
  // were created to store the result of the optimization
  void cleanUp ();
}
