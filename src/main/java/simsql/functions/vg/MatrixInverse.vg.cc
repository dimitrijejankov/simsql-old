

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


#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_errno.h>
#include <math.h>

#include "VGFunction.h"

#define INITIAL_CAPACITY 10

#define MAX(a,b) ((a) > (b) ? (a) : (b))

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  long *rv_i;
  long *rv_j;
  double *value;
};

/**
 * Output record, used by the outputVals() method.
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  long *rv_out_i;
  long *rv_out_j;
  double *outValue;
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
  double *input;

  // "exchange" matrix.
  double *exchange;

  // matrix capacity
  int capacity;

  // actual matrix size.
  int numRVs;

  // RV index.
  int whichRV_i;
  int whichRV_j;

  // output stuff.
  long outI;
  long outJ;
  double outV;

  // ----------------------------------------------------------- // 

  void ensureCapacity(int _n) {

    if (_n < capacity) {
      return;
    }

    // set for doubling.
    int n = MAX(_n, capacity * 2);

    // copy the contents into the exchange matrix.
    memcpy(exchange, input, sizeof(double) * capacity * capacity);

    // resize the input matrix.
    input = (double *)realloc(input, sizeof(double) * n * n);
    memset(input, 0, sizeof(double) * n * n);

    // copy.
    for (int i=0;i<numRVs;i++) {
      for (int j=0;j<numRVs;j++) {
	input[i*n + j] = exchange[i*capacity + j];
      }
    }

    // resize the exchange matrix.
    exchange = (double *)realloc(exchange, sizeof(double) * n  * n);
    memset(exchange, 0, sizeof(double) * n * n);

    // set the new capacity.
    capacity = n;
  }

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures.
   */
  MatrixInverse() {

    capacity = INITIAL_CAPACITY;
    input = (double *)malloc(sizeof(double) * capacity * capacity);
    exchange = (double *)malloc(sizeof(double) * capacity * capacity);
    numRVs = 0;

    memset(input, 0, sizeof(double) * capacity * capacity);
    memset(exchange, 0, sizeof(double) * capacity * capacity);

    gsl_set_error_handler(&handler);
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~MatrixInverse() {

    free(input);
    free(exchange);
    capacity = 0;
    numRVs = 0;
  }

  /** Initializes the RNG seed for a given call. */
  void initializeSeed(long seedValue) {

    // do nothing.
  }

  /** Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). */
  void finalizeTrial() {

    // reset.
    whichRV_i = 0;
    whichRV_j = 0;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

    whichRV_i = 0;
    whichRV_j = 0;
    numRVs = 0;
    memset(input, 0, sizeof(double) * capacity * capacity);
    memset(exchange, 0, sizeof(double) * capacity * capacity);
  }

	
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

    if (input.rv_i != NULL && input.rv_j != NULL && input.value != NULL) {
      
      int i = *input.rv_i;
      int j = *input.rv_j;
      double val = *input.value;

      // check values.
      if (isnan(val) || !isfinite(val)) {
	return;
      }

      // make sure we have enough space.
      ensureCapacity(MAX(i,j) + 1);

      numRVs = MAX(numRVs, MAX(i,j) + 1);

      (this->input)[j*capacity + i] = val;
    }
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 

    // are we without params?
    if (numRVs <= 0) {
      return 0;
    }

    // have we already produced the whole sample?
    if (whichRV_i >= numRVs) {
      return 0;
    }

    // do we have to sample?
    if (whichRV_i == 0 && whichRV_j == 0) {

      // now, do a cholesky decomposition.
      runningError = -1;
      gsl_matrix_view mat = gsl_matrix_view_array_with_tda(input, numRVs, numRVs, capacity);
      
      // cholesky-decompose it.

      //      gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

      gsl_linalg_cholesky_decomp(&mat.matrix);
      //      gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

      // invert it.
      gsl_linalg_cholesky_invert(&mat.matrix);
      //      gsl_matrix_fprintf(stdout, &mat.matrix, "%f");

      if (runningError > 0) {
	gsl_matrix_set_identity(&mat.matrix);
      }
    }

    // set the sample output.
    outI = whichRV_i;
    outJ = whichRV_j;
    outV = input[outI*capacity + outJ];

    // set the record.
    output.rv_out_i = &outI;
    output.rv_out_j = &outJ;
    output.outValue = &outV;

    // increase
    whichRV_j++;
    
    // if we exceeded the current column, go back and move to the next
    // row.
    if (whichRV_j >= numRVs) {
      whichRV_i++;
      whichRV_j = 0;
    }

    return 1;
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){3, {"integer", "integer", "double"}, {"rv_i", "rv_j", "value"}};
  }

  VGSchema outputSchema() {

    return (VGSchema){3, {"integer", "integer", "double"}, {"rv_out_i", "rv_out_j", "outValue"}};
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

