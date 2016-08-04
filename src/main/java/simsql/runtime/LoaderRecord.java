

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

import simsql.runtime.AbstractRecord;

// this little class is used to builk load a database file
public class LoaderRecord extends AbstractRecord {

  static private int numAttsLR;
  static private short typeCodeLR;

  // [updated to make it possible to have multiple RecordIterators at the same time]
  static private boolean haveMultiple = false;
  private int numAttsMult;
  private short typeCodeMult;

  static public void setup (int numAttsLRIn, short typeCodeLRIn) {
    numAttsLR = numAttsLRIn;
    typeCodeLR = typeCodeLRIn;
  }

  static public void setMultiple(boolean mult) {
    haveMultiple = mult;
  }

  public LoaderRecord () {
    this(numAttsLR, typeCodeLR);
  }

  public LoaderRecord(int numAttsIn, short typeCodeIn) {
    numAttsMult = numAttsIn;
    typeCodeMult = typeCodeIn;
    atts = new Attribute [getNumAttributes()];
  }

  @Override
  public Record buildRecordOfSameType() {
    return haveMultiple ? new LoaderRecord(numAttsMult, typeCodeMult) : new LoaderRecord(numAttsLR, typeCodeLR);
  }
  
  public short getTypeCode () {
    return haveMultiple ? typeCodeMult : typeCodeLR;
  }

  public int getNumAttributes () {
    return haveMultiple ? numAttsMult : numAttsLR;
  }
}
