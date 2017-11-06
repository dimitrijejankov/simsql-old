#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_errno.h>
#include <gsl/gsl_blas.h>
#include <math.h>

#include "VGFunction.h"

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  Matrix *value1;
  Vector *value2;
};

/**
 * Output record, used by the outputVals() method.
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  Vector *outValue;
};

int runningError;
void handler (const char * reason,
        const char * file,
        int line,
        int gsl_errno)
{

  runningError = 1;
  fprintf(stderr, "reason: %s\n, file: %s\n, line: %d\n, gsl_errno: %d\n\n", reason, file, line, gsl_errno);
}

// ----------------------------------------------------------- // 

/** A pseudo-VG function for inverting a matrix. */
class MatrixVectorMultiply : public VGFunction {

private:

  // input matrix and vector.
  gsl_matrix *input1 = NULL;
  gsl_vector *input2 = NULL;

  bool active;

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures.
   */
  MatrixVectorMultiply() {

    gsl_set_error_handler(&handler);

    active = true;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~MatrixVectorMultiply() {

    gsl_matrix_free(input1);
    gsl_vector_free(input2);
  }

  /** Initializes the RNG seed for a given call. */
  void initializeSeed(long seedValue) {

    // do nothing.
  }

  /** Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). */
  void finalizeTrial() {

    // do nothing.
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

    // if input
    if(input1 != NULL)
	gsl_matrix_free(input1);
    input1 = NULL;

    if(input2 != NULL)
	gsl_vector_free(input2);
    input2 = NULL;
  }

  
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

    if (input.value1 != NULL) {
      this->input1 = getMatrix(input.value1);
    }
    else
      printf("we are reading a null matrix!");

    if (input.value2 != NULL) {
      this->input2 = getVector(input.value2);
    }
    else
      printf("we are reading a null vector!");

    active = true;
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 

    if (!active)
      return 0;

    runningError = -1;

    gsl_vector* product = gsl_vector_calloc(input1->size1);

    /* if input2 is not an all-0 vector */
    
    if (input2->size != 0)
    /* Compute y = A x */	
    	gsl_blas_dgemv (CblasNoTrans, 1.0, input1, input2, 0.0, product);

    if (runningError > 0) {
      gsl_vector_set_zero(product);
    }

    setVector(product, &output.outValue);

    gsl_vector_free(product);
    active = false;

    return 1;
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){2, {"matrix[a][b]", "vector[b]"}, {"value1", "value2"}};
  }

  VGSchema outputSchema() {

    return (VGSchema){1, {"vector[a]"}, {"outValue"}};
  }

  const char *getName() {
    return "MatrixVectorMultiply";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new MatrixVectorMultiply());
}

void destroy(VGFunction *vgFunction) {
  delete (MatrixVectorMultiply *)vgFunction;
}

