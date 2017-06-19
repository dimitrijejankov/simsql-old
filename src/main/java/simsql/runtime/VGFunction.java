

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

import java.nio.*;
import java.util.*;
import java.io.*;

/** 
 * Interacts with the native C++ VG function code.
 *
 * @author Luis
 */
public class VGFunction {

  // static loader
  static {

    // get the library file
    String file = Function.extractFile("/simsql/runtime/VGFunction.jni.so");
    System.load(file);
  }

  // pointer to the actual VG function.
  private long instancePtr;

  // native methods
  private native long load(String functionFile);
  private native void unload(long instancePtr);
  private native void initializeSeed(long instancePtr, long seedValue);
  private native void clearParams(long instancePtr);
  private native void setBuffers(long instancePtr, ByteBuffer inBuff, ByteBuffer outBuff, LongBuffer positionsIn, LongBuffer positionsOut, LongBuffer tuplesIn);
  private native void takeParams(long instancePtr, long numTuples);
  private native long outputVals(long instancePtr);
  private native void finalizeTrial(long instancePtr);
  private native String[] getInputSchema(long instancePtr);
  private native String[] getOutputSchema(long instancePtr);
  private native String[] getInputAttNames(long instancePtr);
  private native String[] getOutputAttNames(long instancePtr);
  private native long[] getRandomOutputAtts(long instancePtr);
  private native String getName(long instancePtr);

  // public methods -- they call the natives with the instance from
  // the function that we created.
  public void initializeSeed(long seed) {
    initializeSeed(instancePtr, seed);
  }

  public void setBuffers(LongBuffer posBufferIn, LongBuffer posBufferOut, ByteBuffer dataBufferIn, ByteBuffer dataBufferOut, LongBuffer tuplesIn) {
    setBuffers(instancePtr, dataBufferIn, dataBufferOut, posBufferIn, posBufferOut, tuplesIn);
  }

  public void takeParams(long num) {
    takeParams(instancePtr, num);
  }

  public long outputVals() {
    return(outputVals(instancePtr));
  }

  public void finalizeTrial() {
    finalizeTrial(instancePtr);
  }

  public void clearParams() {
    clearParams(instancePtr);
  }

  public String getName() {
    return getName(instancePtr);
  }

  public String[] getOutputAttNames() {
    return getOutputAttNames(instancePtr);
  }

  public String[] getInputAttNames() {
    return getInputAttNames(instancePtr);
  }

  public AttributeType[] getInputTypes() {

    // get the types.
    String[] tStr = getInputSchema(instancePtr);
    AttributeType[] ret = new AttributeType[tStr.length];
    TypeMachine tm = new TypeMachine();

    // use TypeMachine and DataType to do the parsing
    for (int i=0;i<tStr.length;i++) {
      ret[i] = new AttributeType(tm.fromString(tStr[i]));
    }
	
    return ret;
  }

  public AttributeType[] getOutputTypes() {

    // get the types.
    String[] tStr = getOutputSchema(instancePtr);
    AttributeType[] ret = new AttributeType[tStr.length];
    TypeMachine tm = new TypeMachine();

    // use TypeMachine and DataType to do the parsing
    for (int i=0;i<tStr.length;i++) {
      ret[i] = new AttributeType(tm.fromString(tStr[i]));
    }
	
    return ret;
  }

  public long[] getRandomOutputAtts() {
    return getRandomOutputAtts(instancePtr);
  }

  // SQL string
  public String getCreateSQL(String sourceFile) {
    String outStr = "create vgfunction " + getName().toLowerCase() + "(";

    if (getInputTypes().length > 0) {

      outStr += getInputAttNames()[0] + " " + getInputTypes()[0].getType().writeOut();
      for (int i=1;i<getInputTypes().length;i++) {
	outStr += ", " + getInputAttNames()[i] + " " + getInputTypes()[i].getType().writeOut();
      }
    }

    outStr += ") returns (";

    if (getOutputTypes().length > 0) {
      boolean first = true;

      for (int i=0;i<getOutputTypes().length;i++) {

	if (!first) {
	  outStr += ", ";
	} else {
	  first = false;
	}

	outStr += getOutputAttNames()[i] + " " + getOutputTypes()[i].getType().writeOut();
      }
    }

    outStr += ") source '" + sourceFile + "';";
    return outStr;
  }

  // default constructor
  public VGFunction(String libFile) {
    String f = Function.extractFile(libFile);
    instancePtr = load(f);
  }

  // another constructor
  public VGFunction(File libFile) {
    instancePtr = load(libFile.getAbsolutePath());
  }
}
