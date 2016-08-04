

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

import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

public interface Record extends WritableValue {

  /** 
   * Every record has a short that encodes the type of the record.  This makes it possible
   * to determine which records came from which relation when implementing a join, for
   * exmample.  It also makes it possible to figure out what type of record we have when
   * we are deserializing a record.  Any value is OK, BUT ZERO SHOULD NOT BE USED!!
   */
  short getTypeCode ();
  
  /**
   * This gets the isPresent attribute associated with the record
   */
  Bitstring getIsPresent ();
    
  /**
   * This sets the bitstring associated with the record
   */
  void setIsPresent (Bitstring toMe);
  
  /**
   * This this gets the number of Attributes
   */
  int getNumAttributes ();
  
  /**
   * This returns the ith attribute in the record, considering the declaration order...
   */
  Attribute getIthAttribute (int i);
  
  /**
   * This sets the ith attribute in the record, considering the declaration order
   */
  void setIthAttribute (int i, Attribute setToMe);
  
  /**
   * This serializes the record to the specified output stream, returning the number of
   * bytes that were written (including the TypeCode)
   */
  int writeSelfToStream (DataOutputStream writeToMe) throws IOException;
  
  /**
   * This deserializes the record from the specified input stream, returning the number
   * of bytes read.  It is assumed that the first 2 bytes in the serialized record (the
   * typecode) have already been read, and so the only thing yet to read are all of the
   * attributes
   */
  int readSelfFromStream (DataInputStream readFromMe) throws IOException;
  
  /**
   * Returns an empty Record of the same type as this one
   */
  Record buildRecordOfSameType ();


  /** "Recycles" the record by sending it into a RecordPool. The
   * operation is optional, but should still clear the contents of the
   * record.
   */
  void recycle();

  /**
   * puts all of the attributes from me into this... fails if the typecodes or the number of attss don't match
   */
  void copyMyselfFrom (Record me);

  /**
   * this returns true if there is a sort value associated with the record
   */
  boolean hasSortAttribute ();

  /** 
   * this sets the value of the sort attribute
   */
  void setSortAttribute (long toMe);

  /** 
   * this gets the value of the sort attribute
   */
  long getSortAttribute ();

}
