#include "Matrix.h"

#include <dlfcn.h>
#include <string.h>
#include <string>
#include <stdio.h>
#include <stdlib.h>
#include <signal.h>
#include <gsl/gsl_matrix.h>

#ifdef LINUX
#include <malloc.h>
#endif // LINUX

using namespace std;

/*
 * Class:     simsql_runtime_Matrix
 * Method:    getMatrixHeaderSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_Matrix_getMatrixHeaderSize (JNIEnv *env,
                                                                        jclass thisObj) {
    return sizeof(gsl_matrix) + sizeof(gsl_block);
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    getNativeDoubleSize
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_Matrix_getNativeDoubleSize (JNIEnv *env,
                                                                        jclass thisObj) {
    return sizeof(double);
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    initMatrix
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_initMatrix (JNIEnv *env,
                                                              jclass thisObj,
                                                              jlong size1,
                                                              jlong size2,
                                                              jlong data){

    // grab a pointer to the matrix header
    gsl_matrix *matrix = (gsl_matrix*)data;

    // set the sizes
    matrix->size1 = size1;
    matrix->size2 = size2;

    // set the tda (same as size2)
    matrix->tda = matrix->size2;

    // set the pointer to data
    matrix->data = (double*)(data + sizeof(gsl_block) + sizeof(gsl_matrix));

    // block
    matrix->block = (gsl_block*)(data + sizeof(gsl_matrix));

    // set the size of the block
    matrix->block->size = sizeof(double) * matrix->size1 * matrix->size2;

    // set a pointer to the beginning of the block
    matrix->block->data = matrix->data;

    // the matrix doesn't own the memory it's right after the header
    matrix->owner = false;
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeReshape
 * Signature: (JJJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeReshape(JNIEnv *env, jclass thisObj, jlong size1, jlong size2, jlong data) {
    // grab a pointer to the matrix header
    gsl_matrix *matrix = (gsl_matrix*)data;

    // set the sizes
    matrix->size1 = size1;
    matrix->size2 = size2;

    // set the tda (same as size2)
    matrix->tda = matrix->size2;
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    allocatedMatrixSize
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_Matrix_allocatedMatrixSize(JNIEnv *env, jclass thisObj, jlong data) {
    // grab a pointer to the matrix header
    gsl_matrix *matrix = (gsl_matrix*)data;

    // return the size of the block
    return matrix->block->size;
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    getMatrixSize1
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_Matrix_getMatrixSize1 (JNIEnv *env,
                                                                   jclass thisObj,
                                                                   jlong data) {
    return ((gsl_matrix*)data)->size1;
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    getMatrixSize2
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_Matrix_getMatrixSize2 (JNIEnv *env,
                                                                   jclass thisObj,
                                                                   jlong data) {
    return ((gsl_matrix*)data)->size2;
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeAddDouble
 * Signature: (JDDJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeAddDouble (JNIEnv *env,
                                                                   jclass thisObj,
                                                                   jlong a,
                                                                   jdouble sa,
                                                                   jdouble v,
                                                                   jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = (lhs->data[i * N + j] * sa) + v;
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeAddVector
 * Signature: (JZD[DDJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeAddVector (JNIEnv *env,
                                                                   jclass thisObj,
                                                                   jlong a,
                                                                   jboolean ta,
                                                                   jdouble sa,
                                                                   jdoubleArray b,
                                                                   jdouble sb,
                                                                   jlong out){

    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    size_t M = ta ? lhs->size1 : lhs->size2;
    size_t N = ta ? lhs->size2 : lhs->size1;

    // grab the pointer to the data of the vector
    double *vector_data = env->GetDoubleArrayElements(b, 0);

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = (lhs->data[i * N + j] * sa) + vector_data[j];
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeAddMatrix
 * Signature: (JZDJZDJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeAddMatrix (JNIEnv *env,
                                                                   jclass thisObj,
                                                                   jlong a,
                                                                   jboolean ta,
                                                                   jdouble sa,
                                                                   jlong b,
                                                                   jboolean tb,
                                                                   jdouble sb,
                                                                   jlong out){

    // multiplier
    double multiplier = sa * sb;

    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // right hand size matrix
    gsl_matrix *rhs = (gsl_matrix*)b;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // figure out what is transposed
    if((ta && tb) || (!ta && !tb)) {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] * rhs->data[i * N + j] * multiplier;
          }
        }
    }
    else {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] * rhs->data[j * M + i] * multiplier;
          }
        }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeMultiplyDouble
 * Signature: (JDJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeMultiplyDouble (JNIEnv *env, jclass thisObj, jlong a, jdouble v, jlong out){

    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = lhs->data[i * N + j] * v;
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeMultiplyVector
 * Signature: ([DJZJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeMultiplyVector(JNIEnv *env,
                                                                       jclass thisObj,
                                                                       jdoubleArray b,
                                                                       jlong a,
                                                                       jboolean ta,
                                                                       jlong out) {

    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    size_t M = ta ? lhs->size1 : lhs->size2;
    size_t N = ta ? lhs->size2 : lhs->size1;

    // grab the pointer to the data of the vector
    double *vector_data = env->GetDoubleArrayElements(b, 0);

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = lhs->data[i * N + j] * vector_data[j];
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeMultiplyMatrix
 * Signature: (JZJZJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeMultiplyMatrix (JNIEnv *env,
                                                                        jclass thisObj,
                                                                        jlong a,
                                                                        jboolean ta,
                                                                        jlong b,
                                                                        jboolean tb,
                                                                        jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // right hand size matrix
    gsl_matrix *rhs = (gsl_matrix*)b;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // figure out what is transposed
    if((ta && tb) || (!ta && !tb)) {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] * rhs->data[i * N + j];
          }
        }
    }
    else {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] * rhs->data[j * M + i];
          }
        }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeDivideMatrixDouble
 * Signature: (JDJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeDivideMatrixDouble (JNIEnv *env,
                                                                            jclass thisObj,
                                                                            jlong a,
                                                                            jdouble v,
                                                                            jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = lhs->data[i * N + j] / v;
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeDivideDoubleMatrix
 * Signature: (DJJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeDivideDoubleMatrix (JNIEnv *env,
                                                                            jclass thisObj,
                                                                            jdouble v,
                                                                            jlong a,
                                                                            jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = v / lhs->data[i * N + j];
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeDivideVectorMatrix
 * Signature: (JZ[DJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeDivideVectorMatrix (JNIEnv *env,
                                                                            jclass thisObj,
                                                                            jlong a,
                                                                            jboolean ta,
                                                                            jdoubleArray b,
                                                                            jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    size_t M = ta ? lhs->size1 : lhs->size2;
    size_t N = ta ? lhs->size2 : lhs->size1;

    // grab the pointer to the data of the vector
    double *vector_data = env->GetDoubleArrayElements(b, 0);

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] =  vector_data[j] / lhs->data[i * N + j];
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeDivideMatrixVector
 * Signature: (JZ[DJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeDivideMatrixVector (JNIEnv *env,
                                                                            jclass thisObj,
                                                                            jlong a,
                                                                            jboolean ta,
                                                                            jdoubleArray b,
                                                                            jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    size_t M = ta ? lhs->size1 : lhs->size2;
    size_t N = ta ? lhs->size2 : lhs->size1;

    // grab the pointer to the data of the vector
    double *vector_data = env->GetDoubleArrayElements(b, 0);

    // go through the whole matrix
    for (size_t i = 0; i < M; i++) {
      for (size_t j = 0; j < N; j++) {
          result->data[i * N + j] = lhs->data[i * N + j] / vector_data[j];
      }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeDivideMatrixMatrix
 * Signature: (JZJZJ)V
 */
JNIEXPORT void JNICALL Java_simsql_runtime_Matrix_nativeDivideMatrixMatrix (JNIEnv *env,
                                                                            jclass thisObj,
                                                                            jlong a,
                                                                            jboolean ta,
                                                                            jlong b,
                                                                            jboolean tb,
                                                                            jlong out) {
    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // right hand size matrix
    gsl_matrix *rhs = (gsl_matrix*)b;

    // the matrix where we want to output this thing
    gsl_matrix *result = (gsl_matrix*)out;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // figure out what is transposed
    if((ta && tb) || (!ta && !tb)) {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] / rhs->data[i * N + j];
          }
        }
    }
    else {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            result->data[i * N + j] = lhs->data[i * N + j] / rhs->data[j * M + i];
          }
        }
    }
}

/*
 * Class:     simsql_runtime_Matrix
 * Method:    nativeEqual
 * Signature: (JZJZ)Z
 */
JNIEXPORT jboolean JNICALL Java_simsql_runtime_Matrix_nativeEqual(JNIEnv *env,
                                                                  jclass thisObj,
                                                                  jlong a,
                                                                  jboolean ta,
                                                                  jlong b,
                                                                  jboolean tb) {

    // left hand size matrix
    gsl_matrix *lhs = (gsl_matrix*)a;

    // right hand size matrix
    gsl_matrix *rhs = (gsl_matrix*)b;

    // grab the sizes of the lhs matrix
    const size_t M = lhs->size1;
    const size_t N = lhs->size2;

    // figure out what is transposed
    if((ta && tb) || (!ta && !tb)) {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            if(lhs->data[i * N + j] != rhs->data[i * N + j]) {
                return false;
            }
          }
        }
    }
    else {
        // go through both matrices
        for (size_t i = 0; i < M; i++) {
          for (size_t j = 0; j < N; j++) {
            if(lhs->data[i * N + j] != rhs->data[j * M + i]){
                return false;
            }
          }
        }
    }

    return true;
}