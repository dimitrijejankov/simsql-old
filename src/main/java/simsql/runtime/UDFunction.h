#ifndef _UDFUNCTIONLIB_H
#define _UDFUNCTIONLIB_H

#include <string>
#include <vector>
#include <jni.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_vector.h>

/**
 * Error printing helper method: prints an error message that includes
 * the method on top of the call stack.
 */
void printError(const char *message, int line) {
  fprintf(stderr, "[VGFunction.jni::%d]: %s\n", line, message);
}

/**
 * Exception printing method: prints an error message that includes
 * the method on top of the call stack, and produces a Java exception.
 */
void throwException(JNIEnv *env, const char *message, int line) {

    // print the error message
    printError(message, line);

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
 * Vector field in input record
 */
struct Vector {

    Vector(double length, double* data) : length(length), data(data) {}

    // the length of the vector
    double length;

    // a pointer to the data
    double *data;

    // returns a gsl vector representation
    gsl_vector to_gsl_vector() {

        gsl_vector v;

        // setup the data...
        v.data = data;
        v.size = (size_t)length;
        v.stride = 1;
        v.block = 0;
        v.owner = 0;

        // return the vector
        return v;
    }
};

/**
 * Matrix field in input record
 */
struct Matrix {

    // pointer to the gsl matrix
    gsl_matrix *matrix;

    // true if this is a row matrix, false otherwise
    char ifRow;
};

/**
 * The record in
 */
class RecordIn;

/**
 * The general abstract definition of a UD Function.
 */
class UDFunction {
private:

    // a pointer to the JNI environment
    JNIEnv *env;

    // a reference of the java object
    jobject instance;

public:

    /**
     * Execute a function with a double output
     */
    virtual double executeDouble(RecordIn* in) {
        throwException(env, "This is a double UD function but executeDouble not implemented.", __LINE__);
        return -1;
    }

    /**
     * Execute a function with a long output
     */
    virtual long executeInteger(RecordIn* in) {
        throwException(env, "This is a integer UD function but executeInteger not implemented.", __LINE__);
        return -1;
    }

    /**
     * Execute a function with a vector output
     */
    virtual gsl_vector* executeVector(RecordIn* in) {
        throwException(env, "This is a vector UD function but executeVector not implemented.", __LINE__);
        return NULL;
    }

    /**
     * Execute a function with a matrix output
     */
    virtual gsl_matrix* executeMatrix(RecordIn* in) {
        throwException(env, "This is a matrix UD function but executeMatrix not implemented.", __LINE__);
        return NULL;
    }

    /**
     * Execute a function with a string output
     */
    virtual std::string executeString(RecordIn* in) {
        throwException(env, "This is a string UD function but executeString not implemented.", __LINE__);
        return "";
    }

    /**
     * Returns the name
     */
    virtual std::string getName() = 0;

    /**
     * Returns the output type
     */
    virtual std::string getOutputType() = 0;

    /**
     * Returns the input types as strings
     */
    virtual std::vector<std::string> getInputTypes() = 0;

    /**
     * Set the environment
     */
    void setEnvironment(JNIEnv *env) {
        this->env = env;
    }

    /**
     * Set function instance
     */
     void setFunctionInstance(jobject instance) {
        this->instance = instance;
     }

     /**
      * Allocate a matrix to be used as a intermediate value
      */
     gsl_matrix* allocateMatrix(long size1, long size2) {
        return gsl_matrix_alloc(size1, size2);
     }

     /**
      * Allocate the matrix used as an output value
      */
     gsl_matrix* allocateOutputMatrix(long size1, long size2) {

        // get the class
        jclass cls = env->FindClass("simsql/runtime/UDFunction");

        // get the getOutputMatrix from
        jmethodID methodID = env->GetMethodID(cls, "getOutputMatrix", "(JJ)J");

        // if method is not found throw exception
        if (methodID == 0) {
            throwException(env, "Failed to load the getOutputMatrix method.", __LINE__);
        }

        // call the long method
        return (gsl_matrix*)env->CallLongMethod(instance, methodID, size1, size2);
     }

     /**
      * Allocate a vector to be used a a intermediate value
      */
     gsl_vector* allocateVector(long size) {
        return gsl_vector_alloc((size_t)size);
     }

     /**
      * Allocate a vector to be used as an output value
      */
     gsl_vector* allocateOutputVector(long size) {
        return allocateVector(size);
     }
};

// These methods are generated for each VGFunction type.
extern "C" {
  UDFunction *create();
  void destroy(UDFunction *ud);
};


#endif // _UDFUNCTIONLIB_H
