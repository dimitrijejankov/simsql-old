

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

/**
 * This class is used to store assignments of the form
 * 
 * var = expression
 * 
 * where "var" is an identifier, and "expression" is a parsed expression.
 */
class Assignment {
  
  private String identifier;
  private Expression exp;
  
  public Assignment (String idIn, Expression expIn) {
    identifier = idIn;
    exp = expIn;
  }
  
  public String getIdentifier () {
    return identifier; 
  }
  
  public Expression getExpression () {
    return exp;
  }
  
  public String print () {
    return identifier + ": " + exp.print (); 
  }
}
