

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


#include "VGFunction.jni.h"
#include "VGFunction.h"
#include <dlfcn.h>
#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>

#ifdef LINUX
#include <malloc.h>
#endif // LINUX

using namespace std;

/** 
 * This file contains the implementation of the native methods in the
 * VGFunction class. They do marshalling/unmarshalling of data and
 * schema information between the actual VG functions and the engine.
 *
 * @author Luis
 */


static const char *methodNames[] = {
  "load", 
  "unload",
  "initializeSeed",
  "clearParams",
  "takeParams",
  "outputVals",
  "finalizeTrial",
  "getInputSchema",
  "getOutputSchema",
  "getInputAttNames",
  "getOutputAttNames",
  "getRandomOutputAtts",
  "getName",
  "setBuffers", 
  "<?>"
};

enum __currentMethod {
  LOAD = 0,
  UNLOAD = 1,
  INITIALIZESEED = 2,
  CLEARPARAMS = 3,
  TAKEPARAMS = 4,
  OUTPUTVALS = 5,
  FINALIZETRIAL = 6,
  GETINPUTSCHEMA = 7,
  GETOUTPUTSCHEMA = 8,
  GETINPUTATTNAMES = 9,
  GETOUTPUTATTNAMES = 10,
  GETRANDOMOUTPUTATTS = 11,
  GETNAME = 12,
  SETBUFFERS = 13,
  UNKNOWN = 14,
} currentMethod;

// this is for saving time from strstr()
enum __vgType {
  _INTEGER,
  _DOUBLE,
  _STRING,
  _SCALAR,
  _VECTOR,
  _MATRIX,
  _UNKNOWN
};

struct instance {
  void *module;
  VGFunction *func;
  VGSchema inSchema;
  __vgType inTypes[16384];
  VGSchema outSchema;
  __vgType outTypes[16384];
  void **inRec;
  void **outRec;
  Vector *inVec;
  Matrix *inMat;
  void *dataBufIn;
  void *dataBufOut;
  long *posBufIn;
  long *posBufOut;
  long *tupBuf;
  int dataBufInCapacity;
  int dataBufOutCapacity;
};

/**
 * Error printing helper method: prints an error message that includes
 * the method on top of the call stack.
 */
void printError(const char *message) {
  fprintf(stderr, "[VGFunction.jni::%s]: %s\n", methodNames[currentMethod], message);
}

/**
 * Exception printing method: prints an error message that includes
 * the method on top of the call stack, and produces a Java exception.
 */
void throwException(JNIEnv *env, const char *message) {

  printError(message);
  
  jclass newExcCls;
  env->ExceptionDescribe();
  env->ExceptionClear();

  // exception type.
  newExcCls = env->FindClass("java/lang/RuntimeException");
  if (newExcCls != NULL) {
    env->ThrowNew(newExcCls, message);
  }
}


/** 
 * Signal handler: in case of a certain fatal event, we try to exit
 *  gracefully and produce error messages before the JVM catches it.
 */
void signalHandler(int signum) {
	if (currentMethod != UNKNOWN) {
		printError("Caught fatal signal. Aborting."); 
		printError(strsignal(signum));
		exit(signum);
	}
}

/**
 * Exit handler: in case someone calls exit(), we produce error
 * messages.
 */
void exitHandler() {
  if (currentMethod != UNKNOWN) {
    printError("Forced call to exit()");
  }
}


/**
 * Loads a given VG function file, returning a pointer to its instance
 * structure.
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_VGFunction_load
(JNIEnv *env, jobject, jstring _libFile) {

  // set the method
  currentMethod = LOAD;

  // register the exit and signal handlers

	/*
    atexit(exitHandler);
    signal(SIGBUS, signalHandler);
    signal(SIGFPE, signalHandler);
    signal(SIGILL, signalHandler);
    signal(SIGPIPE, signalHandler);
    signal(SIGSEGV, signalHandler);
  */

  // load the library 
  const char *libFile = env->GetStringUTFChars(_libFile, NULL);
  if (libFile == NULL) {
    throwException(env, "Invalid library filename.");
    return 0;
  }

  void *module = dlopen(libFile, RTLD_NOW);
  if (module == NULL) {
    string outStr = "Unable to load VG function library file ";
    outStr += libFile;
    outStr += " ";
    outStr += dlerror();
    throwException(env, outStr.c_str());
    return 0;
  }

  // load the create() method
  void *fhandle = dlsym(module, "create");
  if (fhandle == NULL) {
    string outStr = "Unable to load create() function from VG function library file ";
    outStr += libFile;
    outStr += " ";
    outStr += dlerror();
    throwException(env, outStr.c_str());
    return 0;
  }

  // cast
  VGFunction *(*create)() = (VGFunction *(*)())fhandle;

  // create the function
  VGFunction *func = create();
  if (func == NULL) {
    throwException(env, "Unable to instantiate VG function.");
    return 0;
  }

  // create the exchange records from the schemas.
  VGSchema inSchema = func->inputSchema();
  void **inRec = new void*[inSchema.numAtts];
  VGSchema outSchema = func->outputSchema();
  void **outRec = new void*[outSchema.numAtts];

  Vector *inVec = new Vector[inSchema.numAtts];
  Matrix *inMat = new Matrix[inSchema.numAtts];

  // make the instance type
  instance *inst = new instance;
  inst->module = module;
  inst->func = func;
  inst->inSchema = inSchema;
  inst->outSchema = outSchema;
  inst->inRec = inRec;
  inst->outRec = outRec;
  inst->inVec = inVec;
  inst->inMat = inMat;
  inst->dataBufIn = NULL;
  inst->dataBufOut = NULL;
  inst->posBufIn = NULL;
  inst->posBufOut = NULL;
  inst->dataBufInCapacity = 0;
  inst->dataBufOutCapacity = 0;

  for (int i=0;i<inSchema.numAtts;i++) {
    if (strstr(inst->inSchema.attTypes[i], "integer") != NULL) {
      inst->inTypes[i] = _INTEGER;
    }
    else if (strstr(inst->inSchema.attTypes[i], "double") != NULL) {
      inst->inTypes[i] = _DOUBLE;
    }
    else if (strstr(inst->inSchema.attTypes[i], "string") != NULL) {
      inst->inTypes[i] = _STRING;
    }
    else if (strstr(inst->inSchema.attTypes[i], "scalar") != NULL) {
      inst->inTypes[i] = _SCALAR;
    }
    else if (strstr(inst->inSchema.attTypes[i], "vector") != NULL) {
      inst->inTypes[i] = _VECTOR;
    }
    else if (strstr(inst->inSchema.attTypes[i], "matrix") != NULL) {
      inst->inTypes[i] = _MATRIX;
    } else {
      inst->inTypes[i] = _UNKNOWN;
    }
  }

  for (int i=0;i<outSchema.numAtts;i++) {
    if (strstr(inst->outSchema.attTypes[i], "integer") != NULL) {
      inst->outTypes[i] = _INTEGER;
    }
    else if (strstr(inst->outSchema.attTypes[i], "double") != NULL) {
      inst->outTypes[i] = _DOUBLE;
    }
    else if (strstr(inst->outSchema.attTypes[i], "string") != NULL) {
      inst->outTypes[i] = _STRING;
    }
    else if (strstr(inst->outSchema.attTypes[i], "scalar") != NULL) {
      inst->outTypes[i] = _SCALAR;
    }
    else if (strstr(inst->outSchema.attTypes[i], "vector") != NULL) {
      inst->outTypes[i] = _VECTOR;
    }
    else if (strstr(inst->outSchema.attTypes[i], "matrix") != NULL) {
      inst->outTypes[i] = _MATRIX;
    } else {
      inst->outTypes[i] = _UNKNOWN;
    }
  }

  // return its pointer
  currentMethod = UNKNOWN;
  return (jlong)inst;
}

/**
 * Unloads a given VG function instance.
 */
JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_unload
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = UNLOAD;

  // get the instance from the pointer
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }

  // get the destroy method
  void *fhandle = dlsym(inst->module, "destroy");
  if (fhandle == NULL) {
    throwException(env, "Unable to load destroy() function from VG function library.");
  }

  // cast and call
  void *(*destroy)(VGFunction *) = (void *(*)(VGFunction *))fhandle;
  destroy(inst->func);

  // now, unload the module
  dlclose(inst->module);

  // destroy the exchange records
  delete inst->inRec;
  delete inst->outRec;

  delete inst->inVec;
  delete inst->inMat;
  // and destroy the instance
  delete inst;
  currentMethod = UNKNOWN;
}

/** 
 * Initializes the VG function's seed.
 */
JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_initializeSeed
(JNIEnv *env, jobject, jlong _inst, jlong _seed) {

  // set the method
  currentMethod = INITIALIZESEED;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }

  // call the function
  inst->func->initializeSeed(_seed);
  currentMethod = UNKNOWN;
}

/**
 * Clears the VG function's parameters for the next initialization
 */
JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_clearParams
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = CLEARPARAMS;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }

  // call the function
  inst->func->clearParams();  
  currentMethod = UNKNOWN;
}

JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_setBuffers
(JNIEnv *env, jobject, jlong _inst, jobject _dataBufIn, jobject _dataBufOut, jobject _posBufIn, jobject _posBufOut, jobject _tupBuf) {

  // set the method
  currentMethod = SETBUFFERS;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }

  // get the buffers
  inst->dataBufIn = env->GetDirectBufferAddress(_dataBufIn);
  inst->dataBufOut = env->GetDirectBufferAddress(_dataBufOut);
  inst->posBufIn = (long *)env->GetDirectBufferAddress(_posBufIn);
  inst->posBufOut = (long *)env->GetDirectBufferAddress(_posBufOut);
  inst->tupBuf = (long *)env->GetDirectBufferAddress(_tupBuf);

  if (inst->dataBufIn == NULL || inst->dataBufOut == NULL || inst->posBufIn == NULL || inst->posBufOut == NULL || inst->tupBuf == NULL) {
    throwException(env, "Unable to access direct VG buffers for data.");
    return;
  }

  // get the capacity of the DataBuffer
  inst->dataBufInCapacity = env->GetDirectBufferCapacity(_dataBufIn);
  inst->dataBufOutCapacity = env->GetDirectBufferCapacity(_dataBufOut);


  currentMethod = UNKNOWN;
}

/** 
 * Initializes the VG function's parameters.
 */
JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_takeParams
(JNIEnv *env, jobject, jlong _inst, jlong _num) {

  // set the method
  currentMethod = TAKEPARAMS;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }

  // get the buffers
  void *dataBuf = inst->dataBufIn;
  long *posBuf = inst->posBufIn;
  long *tupBuf = inst->tupBuf;

  // get the capacity of the DataBuffer
  long capacity = inst->dataBufInCapacity;

  // go for all the tuples in the buffer
  for (long k=0;k<_num;k++) {

    // add the stride to get to the right tuple.
    posBuf = inst->posBufIn + tupBuf[k];

    /**
    struct Vector *vec[inst->inSchema.numAtts];
    struct Matrix *mat[inst->inSchema.numAtts];
    **/

    // set the pointers for the input record using the position buffer.
    for (int i=0;i<inst->inSchema.numAtts;i++) {

      /**
      vec[i] = NULL;
      mat[i] = NULL;
      **/

      // check for correctness
      if (posBuf[i] >= capacity) {
      	throwException(env, "Data buffer overrun.");
      	return;
      }

      // a negative position implies a NULL pointer.
      if (posBuf[i] >= 0) {

        
        //        if (strstr(inst->inSchema.attTypes[i], "vector") !=NULL ) {
        if (inst->inTypes[i] == _VECTOR) {
          /**
          inst->inRec[i] = (void *)((long)dataBuf + posBuf[i]);
          vec[i] = (struct Vector *) malloc(sizeof(struct Vector));
          vec[i]->length = *((double*)inst->inRec[i]);
          vec[i]->value = ((double*)inst->inRec[i]) + 1;
          inst->inRec[i] = vec[i];
          **/
          void *myRec = (void *)((long)dataBuf + posBuf[i]);
          Vector *myVec = &(inst->inVec[i]);
          myVec->length = *((double *)myRec);
          myVec->value = ((double *)myRec) + 1;
          inst->inRec[i] = myVec;
        }
        //        else if (strstr(inst->inSchema.attTypes[i], "matrix") !=NULL) {
        else if (inst->inTypes[i] == _MATRIX) {
          /**
          inst->inRec[i] = (void *)((long)dataBuf + posBuf[i]);
          mat[i] = (struct Matrix *) malloc(sizeof(struct Matrix));
          mat[i]->numRow = *((double*)inst->inRec[i]);
          mat[i]->numCol = *(((double*)inst->inRec[i]) + 1);
          mat[i]->value = ((double*)inst->inRec[i]) + 2;
          inst->inRec[i] = mat[i];
          **/
          void *myRec = (void *)((long)dataBuf + posBuf[i]);
          Matrix *myMat = &(inst->inMat[i]);
          myMat->numRow = *((double*)myRec);
          myMat->numCol = *(((double*)myRec) + 1);
          myMat->value = ((double*)myRec) + 2;
          inst->inRec[i] = myMat;
        }

        else {
          inst->inRec[i] = (void *)((long)dataBuf + posBuf[i]);
        }
      }
      else {
        inst->inRec[i] = NULL;
      }
    }

    // call the function
    inst->func->takeParams((RecordIn &)*inst->inRec);

    /**
    for (int i=0;i<inst->inSchema.numAtts;i++) {


      //      if(strstr(inst->inSchema.attTypes[i], "vector") != NULL) {
      if (inst->inTypes[i] == _VECTOR) {
        if(vec[i] != NULL) {
          free(vec[i]);
          vec[i] = NULL;
        }
      }
      //      else if (strstr(inst->inSchema.attTypes[i], "matrix") != NULL) {
      else if (inst->inTypes[i] == _MATRIX) {
        if(mat[i] != NULL) {
          free(mat[i]);
          mat[i] = NULL;
        }
      }
    }
    **/
  }

  currentMethod = UNKNOWN;
}

/** 
 * Obtains samples from the VG function.
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_VGFunction_outputVals
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = OUTPUTVALS;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return 0;
  }

  // get the buffers
  void *dataBuf = inst->dataBufOut;
  long *posBuf = inst->posBufOut;
  long curPosition = 0;

  if (dataBuf == NULL || posBuf == NULL) {
    throwException(env, "Unable to access direct VG buffers for data.");
    return 0;
  }

  // get the capacity of the DataBuffer
  long capacity = inst->dataBufOutCapacity;
  int numTuples = 0; 

  // call the function
  while (inst->func->outputVals((RecordOut &)*inst->outRec)) {

    // update the position buffer.
    posBuf = (inst->posBufOut + (numTuples * inst->outSchema.numAtts));

    // process the output record
    for (int i=0;i<inst->outSchema.numAtts;i++) {

      // is it a NULL value?
      if (inst->outRec[i] == NULL) {
	
        // if so, we just set the current position as a negative number
        posBuf[i] = -1;
      } else {
	
        // otherwise, we move the values
        //    if (strstr(inst->outSchema.attTypes[i], "integer") != NULL) {
        if (inst->outTypes[i] == _INTEGER) {
          if (capacity < (long) sizeof(long)) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), inst->outRec[i], sizeof(long));
          posBuf[i] = curPosition;
          curPosition += sizeof(long);
          capacity -= sizeof(long);
        }
        //        else if (strstr(inst->outSchema.attTypes[i], "double") != NULL) {
        else if (inst->outTypes[i] == _DOUBLE) {
          if (capacity < (long) sizeof(double)) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), inst->outRec[i], sizeof(double));
          posBuf[i] = curPosition;
          curPosition += sizeof(double);
          capacity -= sizeof(double);
        }
        //        else if (strstr(inst->outSchema.attTypes[i], "string") != NULL) {
        else if (inst->outTypes[i] == _STRING) {
          long len = (long)strlen((char *)inst->outRec[i]);

          if (capacity < (long) (sizeof(long) + len)) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), &len, sizeof(long));
          memmove((void *) ((long)dataBuf + curPosition + sizeof(long)), inst->outRec[i], len);
          posBuf[i] = curPosition;
          curPosition += len + sizeof(long);
          capacity -= len + sizeof(long);
        }

        //        else if (strstr(inst->outSchema.attTypes[i], "scalar") != NULL) {
        else if (inst->outTypes[i] == _SCALAR) {


          if (capacity < (long) sizeof(double)) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), inst->outRec[i], sizeof(double));
          posBuf[i] = curPosition;
          curPosition += sizeof(double);
          capacity -= sizeof(double);
        }

        //        else if (strstr(inst->outSchema.attTypes[i], "vector") != NULL) {

        else if (inst->outTypes[i] == _VECTOR) {

          long len = (long) ((Vector *)inst->outRec[i])->length;

          if (capacity < (long) (len * sizeof(double) + sizeof(long))) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), &len, sizeof(long));
          memmove((void *) ((long)dataBuf + curPosition + sizeof(long)), ((Vector *)inst->outRec[i])->value, len * sizeof(double));
          posBuf[i] = curPosition;
          curPosition += len * sizeof(double) + sizeof(long);
          capacity -= len * sizeof(double) + sizeof(long);
    
          free(((Vector *)inst->outRec[i])->value);

          // TEMPORARY: OPTIMIZE LATER.
          free((Vector *)inst->outRec[i]);
        }

        //        else if (strstr(inst->outSchema.attTypes[i], "matrix") != NULL) {

        else if (inst->outTypes[i] == _MATRIX) {
          long row = (long) ((Matrix *)inst->outRec[i])->numRow;
          long col = (long) ((Matrix *)inst->outRec[i])->numCol;

          if (capacity < (long) (row * col * sizeof(double) + 2 * sizeof(long))) {
            throwException(env, "Data buffer overrun");
            return 0;
          }

          memmove((void *) ((long)dataBuf + curPosition), &row, sizeof(long));
          memmove((void *) ((long)dataBuf + curPosition + sizeof(long)), &col, sizeof(long));
          memmove((void *) ((long)dataBuf + curPosition + 2 * sizeof(long)), ((Matrix *)inst->outRec[i])->value, row * col * sizeof(double));
          posBuf[i] = curPosition;
          curPosition += row * col * sizeof(double) + 2 * sizeof(long);
          capacity -= row * col * sizeof(double) + 2 * sizeof(long);

          free(((Matrix *)inst->outRec[i])->value);

          // TEMPORARY: OPTIMIZE LATER.
          free((Matrix *)inst->outRec[i]);
        }
        else {

          // just put a NULL position
          posBuf[i] = -1;
        }
      }
    }
    
    numTuples++;
  }

  // return the number of tuples.
  currentMethod = UNKNOWN;
  return(numTuples);
}

/** 
 * Finalizes the current trial, preparing the VG function for the next
 * sample.
 */
JNIEXPORT void JNICALL Java_simsql_runtime_VGFunction_finalizeTrial
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = FINALIZETRIAL;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return;
  }


  // call the function
  inst->func->finalizeTrial();
  currentMethod = UNKNOWN;
}

/**
 * Returns the VG function's input schema.
 */
JNIEXPORT jobjectArray JNICALL Java_simsql_runtime_VGFunction_getInputSchema
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = GETINPUTSCHEMA;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return NULL;
  }

  // build the array
  jobjectArray ret = env->NewObjectArray(inst->inSchema.numAtts,
                                         env->FindClass("java/lang/String"),
                                         env->NewStringUTF(""));

  // populate it
  for (int i=0;i<inst->inSchema.numAtts;i++) {
    env->SetObjectArrayElement(ret, i, env->NewStringUTF(inst->inSchema.attTypes[i]));
  }

  currentMethod = UNKNOWN;
  return(ret);
}

/** 
 * Returns the VG function's output schema.
 */
JNIEXPORT jobjectArray JNICALL Java_simsql_runtime_VGFunction_getOutputSchema
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = GETOUTPUTSCHEMA;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return NULL;
  }

  // build the array
  jobjectArray ret = env->NewObjectArray(inst->outSchema.numAtts,
                                         env->FindClass("java/lang/String"),
                                         env->NewStringUTF(""));

  // populate it
  for (int i=0;i<inst->outSchema.numAtts;i++) {
    env->SetObjectArrayElement(ret, i, env->NewStringUTF(inst->outSchema.attTypes[i]));
  }

  currentMethod = UNKNOWN;
  return(ret);
}

/**
 * Returns the VG function's output attribute names.
 */
JNIEXPORT jobjectArray JNICALL Java_simsql_runtime_VGFunction_getOutputAttNames
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = GETOUTPUTATTNAMES;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return NULL;
  }

  // build the array
  jobjectArray ret = env->NewObjectArray(inst->outSchema.numAtts,
                                         env->FindClass("java/lang/String"),
                                         env->NewStringUTF(""));

  // populate it
  for (int i=0;i<inst->outSchema.numAtts;i++) {
    env->SetObjectArrayElement(ret, i, env->NewStringUTF(inst->outSchema.attNames[i]));
  }

  currentMethod = UNKNOWN;  
  return(ret);
}

JNIEXPORT jobjectArray JNICALL Java_simsql_runtime_VGFunction_getInputAttNames
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = GETINPUTATTNAMES;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return NULL;
  }

  // build the array
  jobjectArray ret = env->NewObjectArray(inst->inSchema.numAtts,
                                         env->FindClass("java/lang/String"),
                                         env->NewStringUTF(""));

  // populate it
  for (int i=0;i<inst->inSchema.numAtts;i++) {
    env->SetObjectArrayElement(ret, i, env->NewStringUTF(inst->inSchema.attNames[i]));
  }
  
  currentMethod = UNKNOWN;
  return(ret);
}

/**
 * Returns an array of indices indicating the positions of the VG
 * function's random output attributes.
 *
 JNIEXPORT jlongArray JNICALL Java_simsql_runtime_VGFunction_getRandomOutputAtts
 (JNIEnv *env, jobject, jlong _inst) {

 // set the method
 currentMethod = GETRANDOMOUTPUTATTS;

 // get the instance
 instance *inst = (instance *)_inst;
 if (inst == NULL) {
 throwException(env, "Invalid VG function instance pointer.");
 return NULL;
 }

 // count the number of random attributes
 int n = 0;
 for (int i=0;i<inst->outSchema.numAtts;i++) {
 if (inst->outSchema.isRandom[i] > 0) {
 n++;
 }
 }

 // create the output array
 int j = 0;
 jlong *longs = (jlong *)malloc(sizeof(jlong) * n);
 for (int i=0;i<inst->outSchema.numAtts;i++) {
 if (inst->outSchema.isRandom[i] > 0) {
 longs[j++] = i;
 }
 }

 // create and fill the output array
 jlongArray ret = env->NewLongArray(n);
 env->SetLongArrayRegion(ret, 0, n, longs);
  
 // return it
 currentMethod = UNKNOWN;
 return(ret);
 }
*/

/**
 * Returns the name of the VG function.
 */
JNIEXPORT jstring JNICALL Java_simsql_runtime_VGFunction_getName
(JNIEnv *env, jobject, jlong _inst) {

  // set the method
  currentMethod = GETNAME;

  // get the instance
  instance *inst = (instance *)_inst;
  if (inst == NULL) {
    throwException(env, "Invalid VG function instance pointer.");
    return NULL;
  }

  // get the string
  currentMethod = UNKNOWN;
  return env->NewStringUTF(inst->func->getName());
}
