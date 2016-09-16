#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_errno.h>
#include <math.h>

#include "VGFunction.h"

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  Matrix *value;
};

/**
 * Output record, used by the outputVals() method.
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  Matrix *outValue;
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
class MatrixInverse : public VGFunction {

private:

  // input matrix.
  gsl_matrix *input;

  bool active;

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures.
   */
  MatrixInverse() {

    gsl_set_error_handler(&handler);

    active = true;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~MatrixInverse() {

    gsl_matrix_free(input);
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

    // do nothing.
  }

  
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

    if (input.value != NULL) {
      this->input = getMatrix(input.value);
    }

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

    bool isHermitian = true;

    for (int i = 0; i < input->size1; i++) {
      for (int j = 0; j < input->size1; j++) {
        if (gsl_matrix_get(input, i, j) != gsl_matrix_get(input, j, i)) {
          isHermitian = false;
          break;
        }
      }
    }

    if (isHermitian) {
    
      // cholesky-decompose it.

      // gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

      gsl_linalg_cholesky_decomp(input);
      // gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

      // invert it.
      gsl_linalg_cholesky_invert(input);
      // gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

    }
    else {

      // perform a normal LU decomposition

      int s;
      gsl_matrix * inverse = gsl_matrix_alloc(input->size1, input->size1);
      gsl_permutation * perm = gsl_permutation_alloc(input->size1);

      gsl_linalg_LU_decomp (input, perm, &s);

      gsl_linalg_LU_invert (input, perm, inverse);

      gsl_matrix_memcpy (input, inverse);

      gsl_permutation_free (perm);

      gsl_matrix_free (inverse);

    }

    if (runningError > 0) {
      gsl_matrix_set_identity(input);
    }

    setMatrix(input, &output.outValue);

    active = false;

    return 1;
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){1, {"matrix[a][a]"}, {"value"}};
  }

  VGSchema outputSchema() {

    return (VGSchema){1, {"matrix[a][a]"}, {"outValue"}};
  }

  const char *getName() {
    return "MatrixInverse";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new MatrixInverse());
}

void destroy(VGFunction *vgFunction) {
  delete (MatrixInverse *)vgFunction;
}

