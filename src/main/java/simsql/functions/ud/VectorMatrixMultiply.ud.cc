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
  Matrix *v; // the vector in matrix form
  Matrix *m; // the matrix
};

/**
 * Output record, used by the outputVals() method.
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate).
 */
struct RecordOut {
  Matrix *out;
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
class VectorMatrixMultiply : public VGFunction {

private:

  // input matrix and vector.
  gsl_matrix *v = NULL;
  gsl_matrix *m = NULL;

  bool hasVector;
  bool hasMatrix;

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures.
   */
  VectorMatrixMultiply() {
    gsl_set_error_handler(&handler);

    // resets the inputs...
    hasVector = false;
    hasMatrix = false;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~VectorMatrixMultiply() {

    // free the memory we are done with it...
    gsl_matrix_free(v);
    gsl_matrix_free(m);
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

      // resets the inputs...
      hasVector = false;
      hasMatrix = false;
  }


  /**
   * Passes the parameter values. Might be called several times
   * for each group.
   */
  void takeParams(RecordIn &input) {

    if (!hasVector) {

      // we don't have a vector allocated so we go and allocate it...
      if(this->v == NULL) {
        this->v = getMatrix(input.v);
      }
      // we have a vector allocated
      else {

        // can we reuse the memory we have...
        if(this->v->size1 == input.v->numRow && this->v->size2 == input.v->numCol){
            // if we can use it...
            updateMatrix(input.v, this->v);
        }
        // otherwise
        else {
            // free the vector...
            gsl_matrix_free(this->v);

            // allocate it again...
            this->v = getMatrix(input.v);
        }
      }

      // we stored the vector
      hasVector = true;
    }

    if (!hasMatrix) {

      // we don't have a vector allocated so we go and allocate it...
      if(this->m == NULL) {
        this->m = getMatrix(input.m);
      }
      // we have a vector allocated
      else {

        // can we reuse the memory we have...
        if(this->m->size1 == input.m->numRow && this->m->size2 == input.m->numCol){
            // if we can use it...
            updateMatrix(input.m, this->m);
        }
        // otherwise
        else {
            // free the vector...
            gsl_matrix_free(this->m);

            // allocate it again...
            this->m = getMatrix(input.m);
        }
      }

      // we stored the vector
      hasMatrix = true;
    }

  }

  /**
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise.
   */
  int outputVals(RecordOut &output) {

    // do we have the inputs...
    if (!hasVector || !hasMatrix)
      return 0;

    // we don't have a error
    runningError = -1;

    // the product vector
    gsl_matrix* product = gsl_matrix_calloc(1, this->m->size2);

    // if the vector is not an all 0 vector
    if (this->v->size1 != 0)
    	/* Compute y = x A */
	gsl_blas_dgemm(CblasNoTrans, CblasNoTrans, 1.0, v, m, 0.0, product);

    // check if we have gotten an error...
    if (runningError > 0) {
      gsl_matrix_set_zero(product);
    }

    // set the matrix
    setMatrix(product, &output.out);

    // free the vector
    gsl_matrix_free(product);

    // resets the inputs...
    hasVector = false;
    hasMatrix = false;

    return 1;
  }

  // ----------------------------------------------------------- //

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){2, {"matrix[a][b]", "matrix[a][b]"}, {"v", "m"}};
  }

  VGSchema outputSchema() {

    return (VGSchema){1, {"matrix[a][b]"}, {"out"}};
  }

  const char *getName() {
    return "VectorMatrixMultiply";
  }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new VectorMatrixMultiply());
}

void destroy(VGFunction *vgFunction) {
  delete (VectorMatrixMultiply *)vgFunction;
}

