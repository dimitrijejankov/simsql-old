

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
import simsql.compiler.Relation;

// encapsultes an SQL compiler
public interface Compiler <Output extends CompiledQuery> {
  
  // this uses the parser to parse the contents of the string "parseMe"... 
  Output parseString (String parseMe);
  
  // this uses the parser to parse the contents of the file whose name is "parseMe"...
  Output parseFile (String parseMe);

}
