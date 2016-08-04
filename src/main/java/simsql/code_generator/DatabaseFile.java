

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



package simsql.code_generator;

import java.io.Serializable;

// this is the structure that tells us where the database files are actually stored
public class DatabaseFile implements Serializable {

  private String dirName;
  private Short typeCode;
  private Long sizeInBytes;  // uncompressed size.
  private Integer sortingAtt;  // sorted att. position
  private Integer numAtts; // known no. of atts.

  public DatabaseFile (String dirNameIn, Short typeCodeIn) {
    dirName = dirNameIn;
    typeCode = typeCodeIn;
    sizeInBytes = 0L;
    sortingAtt = -1;
    numAtts = 0;
  }

  public String getDirName () {
    return dirName;
  }

  public Short getTypeCode () {
    return typeCode;
  }

  public Long getSizeInBytes() {
    return sizeInBytes;
  }

  public void setSizeInBytes(Long sizeIn) {
    sizeInBytes = sizeIn;
  }

  public Integer getSortingAtt() {
    return sortingAtt;
  }

  public void setSortingAtt(Integer whichAtt) {
    sortingAtt = whichAtt;
  }

  public void setNumAtts(Integer val) {
    numAtts = val;
  }
    
  public Integer getNumAtts() {
    return numAtts;
  }
}
