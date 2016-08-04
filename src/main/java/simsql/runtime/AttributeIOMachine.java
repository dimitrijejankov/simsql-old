

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

class AttributeIOMachine {
  
  
  /**
   * serializes the attribute object to the given stream
   */
  public static int writeToStream (Attribute writeMe, DataOutputStream writeToMe) throws IOException {
  
    Class <? extends Attribute> classToWrite = writeMe.getClass ();
    if (classToWrite.equals (DoubleArrayAttribute.class)) {
      writeToMe.writeByte (45);
    } else if (classToWrite.equals (DoubleAttribute.class)) {
      writeToMe.writeByte (30);
    } else if (classToWrite.equals (IntArrayAttribute.class)) {
      writeToMe.writeByte (83);
    } else if (classToWrite.equals (IntAttribute.class)) {
      writeToMe.writeByte (34);
    } else if (classToWrite.equals (ArrayAttributeWithNulls.class)) {
      writeToMe.writeByte (57);
    } else if (classToWrite.equals (NullAttribute.class)) {
      writeToMe.writeByte (93);
    } else if (classToWrite.equals (StringArrayAttribute.class)) {
      writeToMe.writeByte (84);
    } else if (classToWrite.equals (StringAttribute.class)) {
      writeToMe.writeByte (12);
    } else if (classToWrite.equals (ScalarAttribute.class)) {
      writeToMe.writeByte (33);
    } else if (classToWrite.equals (VectorAttribute.class)) {
      writeToMe.writeByte (61);
    } else if (classToWrite.equals (MatrixAttribute.class)) {
      writeToMe.writeByte (72);
    } else if (classToWrite.equals (SeedAttribute.class)) {
      writeToMe.writeByte (26);
    } else if (classToWrite.equals (AggregatorVector.class)) {
        writeToMe.writeByte (21);
    } else if (classToWrite.equals (AggregatorMatrix.class)) {
        writeToMe.writeByte (51);
    } else {
      throw new RuntimeException ("trying to serialize an unknwn Attribute class"); 
    }
    
    return 1 + writeMe.writeSelfToStream (writeToMe);
  }
  
  /** 
   * searialize the long to the given stream
   */
  public static int writeToStream (long writeMe, DataOutputStream writeToMe) throws IOException {
    writeToMe.writeByte (81);
    writeToMe.writeLong (writeMe);
    return 9;
  }
 
  /**
   * read in the next Attribute object... returns a null if an Attribute was not found, but a long was found instead
   * (in this case, the long is returned via the WriteableInt
   */
  public static Attribute readFromStream (DataInputStream readFromMe, WriteableInt bytesRead, WriteableInt longRead) throws IOException {
   
    // first, read the type code
    int code = readFromMe.readByte ();
    
    // now, create an object
    Attribute lastRead;
    if (code == 45) {
      lastRead = new DoubleArrayAttribute ();
    } else if (code == 30) {
      lastRead = new DoubleAttribute ();
    } else if (code == 83) {
      lastRead = new IntArrayAttribute ();
    } else if (code == 34) {
      lastRead = new IntAttribute ();
    } else if (code == 57) {
      lastRead = new ArrayAttributeWithNulls ();
    } else if (code == 93) {
      lastRead = new NullAttribute ();
    } else if (code == 84) {
      lastRead = new StringArrayAttribute ();
    } else if (code == 12) {
      lastRead = new StringAttribute ();
    } else if (code == 33) {
      lastRead = new ScalarAttribute ();
    } else if (code == 61) {
      lastRead = new VectorAttribute ();
    } else if (code == 72) {
      lastRead = new MatrixAttribute ();
    } else if (code == 26) {
      lastRead = new SeedAttribute();
    } else if (code == 21) {
        lastRead = new AggregatorVector();
    } else if (code == 51) {
        lastRead = new AggregatorMatrix();
    } else if (code == 81) {
      longRead.set (readFromMe.readLong ());
      bytesRead.set (1 + 9);
      return null;
    } else {
      throw new RuntimeException ("trying to serialize an unknwn Attribute class"); 
    }
    
    bytesRead.set (1 + lastRead.readSelfFromStream (readFromMe));
    return lastRead;
  }
  
}
