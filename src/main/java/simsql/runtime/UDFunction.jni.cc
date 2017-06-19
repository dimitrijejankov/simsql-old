#include "UDFunction.jni.h"
#include "UDFunction.h"

#include <dlfcn.h>
#include <string>

/**
 * An instance of the UD function
 */
struct instance {

    // module and function
    void *module;
    UDFunction *func;

    // the input buffer stuff
    void* dataBufIn;
    long* posBufIn;
    long dataBufInCapacity;
};

/**
 * Load a function from the library
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_UDFunction_load(JNIEnv *env, jobject thisObj, jstring fileName) {


    const char *libFile = env->GetStringUTFChars(fileName, NULL);

    // if we don't have a file name provided
    if (libFile == NULL) {
        throwException(env, "Invalid library filename.", __LINE__);
        return 0;
    }

    // load the dynamic lib
    void *module = dlopen(libFile, RTLD_NOW);

    // if we could not load it throw an exception
    if (module == NULL) {
        std::string outStr = "Unable to load UD function library file ";
        outStr += libFile;
        outStr += " ";
        outStr += dlerror();
        throwException(env, outStr.c_str(), __LINE__);
        return 0;
    }

    // load the create() method
    void *fhandle = dlsym(module, "create");

    // if we could not create a new instance of the function
    if (fhandle == NULL) {
        std::string outStr = "Unable to load create() function from UD function library file ";
        outStr += libFile;
        outStr += " ";
        outStr += dlerror();
        throwException(env, outStr.c_str(), __LINE__);
        return 0;
    }

    // cast
    UDFunction *(*create)() = (UDFunction *(*)())fhandle;

    // create the function
    UDFunction *func = create();
    if (func == NULL) {
        throwException(env, "Unable to instantiate UD function.", __LINE__);
        return 0;
    }

    // set the environment
    func->setEnvironment(env);

    // make the instance type
    instance *inst = new instance;

    // set the module and function
    inst->module = module;
    inst->func = func;

    // set the data buffers to NULL
    inst->dataBufIn = NULL;
    inst->posBufIn = NULL;

    return (jlong)inst;
}

/**
 * Returns the name of the function at instance pointer address
 */
JNIEXPORT jstring JNICALL Java_simsql_runtime_UDFunction_getLoadedName(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // return the name of the function
    return env->NewStringUTF(inst->func->getName().c_str());
}

/**
 * Returns the type of the output of the function at instance pointer address
 */
JNIEXPORT jstring JNICALL Java_simsql_runtime_UDFunction_getLoadedOutputType(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // return the name of the function
    return env->NewStringUTF(inst->func->getOutputType().c_str());
}

/**
 * Returns an array of input type strings
 */
JNIEXPORT jobjectArray JNICALL Java_simsql_runtime_UDFunction_getLoadedInputTypes(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // the object array we are gonna return
    jobjectArray ret;

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the input types
    std::vector<std::string> inputTypes = inst->func->getInputTypes();

    // create a new string arrray
    ret = (jobjectArray)env->NewObjectArray(inputTypes.size(), env->FindClass("java/lang/String"), env->NewStringUTF(""));

    // go through each input type
    for(size_t i = 0; i < inputTypes.size(); ++i) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(inputTypes[i].c_str()));
    }

    return ret;
}

/**
 * Sets the buffers for the input
 */
JNIEXPORT void JNICALL Java_simsql_runtime_UDFunction_setBuffers(JNIEnv *env, jobject thisObj,
                                                                 jlong instancePtr, jobject dataBufferIn,
                                                                 jobject posBufferIn) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // set the buffers
    inst->dataBufIn = env->GetDirectBufferAddress(dataBufferIn);
    inst->posBufIn = (long *)env->GetDirectBufferAddress(posBufferIn);

    // check if we can access the memory
    if (inst->dataBufIn == NULL || inst->posBufIn == NULL) {
        throwException(env, "Unable to   access direct VG buffers for data.", __LINE__);
        return;
    }

    // get the capacity of the DataBuffer
    inst->dataBufInCapacity = env->GetDirectBufferCapacity(dataBufferIn);
}

/**
 * Create input record
 */
 void** getRecord(instance* inst) {

    // grab the number of parameters from the function
    std::vector<std::string> inputTypes = inst->func->getInputTypes();

    // allocate the record
    void** inRecord = (void**)malloc(sizeof(size_t) * inputTypes.size());

    // setup the parameters
    for(size_t i = 0; i < inputTypes.size(); ++i) {

        // if this value is not null
        if(inst->posBufIn[i] != -1){

            // if it's a vector we need to create a proxy object
            if(inputTypes[i].find("vector") == std::string::npos) {
                inRecord[i] = (void*) (inst->posBufIn[i] + (long)inst->dataBufIn);
            }
            else {
                inRecord[i] = new Vector(*((double*)(inst->posBufIn[i] + (long)inst->dataBufIn)),
                                          (double*)(inst->posBufIn[i] + (long)inst->dataBufIn + sizeof(double)));
            }
        }
        else {
            inRecord[i] = NULL;
        }
    }

    return inRecord;
}

/**
 * Free input record
 */
void freeRecord(instance* inst, void **inRecord) {

    // grab the number of parameters from the function
    std::vector<std::string> inputTypes = inst->func->getInputTypes();

    // free the vectors
    for(size_t i = 0; i < inputTypes.size(); ++i) {
        if(inputTypes[i].find("vector") != std::string::npos) {
            delete (Vector*)inRecord[i];
        }
    }

    free(inRecord);
}

/**
 * Execute the function return the resulting the double result
 */
JNIEXPORT jdouble JNICALL Java_simsql_runtime_UDFunction_getDoubleResult(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the record
    void** inRecord = getRecord(inst);

    // return double
    jdouble ret = inst->func->executeDouble((RecordIn*)inRecord);

    // free the memory of the record
    freeRecord(inst, inRecord);

    return ret;
}

/**
 * Execute the function and return the resulting the resulting integer
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_UDFunction_getIntResult(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the record
    void** inRecord = getRecord(inst);

    // return long
    jlong ret = inst->func->executeInteger((RecordIn*)inRecord);

    // free the memory of the record
    freeRecord(inst, inRecord);

    return ret;
}

/**
 * Execute the function and return the resulting the resulting vector
 */
JNIEXPORT jdoubleArray JNICALL Java_simsql_runtime_UDFunction_getVectorResult(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the record
    void** inRecord = getRecord(inst);

    // grab the vector after executing
    gsl_vector* v = inst->func->executeVector((RecordIn*)inRecord);

    // create the double array
    jdoubleArray result = env->NewDoubleArray(v->size);

    // throw out of memory error
    if (result == NULL) {
        throwException(env, "Out of memory can't create double array!.", __LINE__);
    }

    // set the data
    env->SetDoubleArrayRegion(result, 0, v->size, v->data);

    // free the vector memory
    gsl_vector_free(v);

    // free the memory of the record
    freeRecord(inst, inRecord);

    // return array
    return result;
}

/**
 * Execute the function and return the resulting the resulting matrix
 */
JNIEXPORT jlong JNICALL Java_simsql_runtime_UDFunction_getMatrixResult(JNIEnv *env, jobject thisObj, jlong instancePtr) {

    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the record
    void** inRecord = getRecord(inst);

    // set the instance of the object
    inst->func->setFunctionInstance(thisObj);

    // grab the matrix after executing
    gsl_matrix* ret = inst->func->executeMatrix((RecordIn*)inRecord);

    // free the memory of the record
    freeRecord(inst, inRecord);

    // return double
    return (long)ret;
}

/**
 * Execute the function and return the resulting string
 */
JNIEXPORT jstring JNICALL Java_simsql_runtime_UDFunction_getStringResult(JNIEnv *env, jobject thisObj, jlong instancePtr) {


    // grab the passed instance
    instance* inst = (instance*)instancePtr;

    // grab the record
    void** inRecord = getRecord(inst);

    // grab the matrix after executing
    jstring ret = env->NewStringUTF(inst->func->executeString((RecordIn*)inRecord).c_str());

    // free the memory of the record
    freeRecord(inst, inRecord);

    // return double
    return ret;
}