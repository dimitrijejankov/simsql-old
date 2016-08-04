

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


package simsql.compiler; // package mcdb.catalog;


/**
 * Represents a foreign key, composed of two mapped attributes.
 *
 * @author Luis, Bamboo
 */

public class ForeignKey {

  /** Set of source attributes */
  private Attribute srcAtt;

  /** Set of destination attributes */
  private Attribute destAtt;

  /** Public constructor */
  public ForeignKey(Attribute _srcAtt, Attribute _destAtt) {
    srcAtt = _srcAtt;
    destAtt = _destAtt;
  }

  /** Returns the set of source attributes */
  public Attribute getSrcAtt() {
    return(srcAtt);
  }

  /** Returns the set of destination attributes */
  public Attribute getDestAtt() {
    return(destAtt);
  }

  public String toString() {
    return(srcAtt + "->" + destAtt);
  }
}
