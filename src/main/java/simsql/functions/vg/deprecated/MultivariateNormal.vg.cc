

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
#include "VGFunction.h"
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <math.h>

// #define DEBUG
#define INITIAL_SIZE 64
#define MAX(a,b) ( (a > b) ? (a) : (b) )

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  long *rv;
  double *mean;
  long *rv_x;
  long *rv_y;
  double *covar;
};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  long *rv;
  double *value;

};

// ----------------------------------------------------------- // 

int runningError;
void handler(const char *reason, const char *file, int line, int gsl_errno) {
  runningError = 1;
}

// ----------------------------------------------------------- // 

/** VG Function class */
class MultivariateNormal : public VGFunction {

private:
  /** RNG and structures go here. */
  gsl_rng *rng;
  gsl_error_handler_t *oldHandler;

  // input data
  long num_rv;
  long currentSize;
  double *mean;
  double **covar;

  // flags
  bool active;
  bool decomposed;
  bool sampled;

  // internal structures
  double *inCov;
  double *N;
  double *samples;
  long sizeInternals;
  long currentRV;

  // output data
  long outRV;
  double outValue;

  // ----------------------------------------------------------- // 

  // this is used to resize the mean vector and covariance matrix.
  void resizeVectors(long newSize) {

    long oldSize = currentSize;
    currentSize = newSize;

    // reallocate the mean vector
    mean = (double *)realloc(mean, currentSize * sizeof(double));
	
    // reallocate the covariance matrix
    covar = (double **)realloc(covar, currentSize * sizeof(double *));
    for (int i=0;i<oldSize;i++) {
      covar[i] = (double *)realloc(covar[i], currentSize * sizeof(double));
    }

    for (int i=oldSize;i<currentSize;i++) {
      covar[i] = (double *)malloc(currentSize * sizeof(double));
    }
  }

  // ----------------------------------------------------------- // 
  
public:

  /** Constructor. Use this to declare your RNG and other
   * important structures .
   */
  MultivariateNormal() {

#ifdef DEBUG
    printf("[MultivariateNormal]: constructor.\n");
    fflush(stdout);
#endif

    // set our GSL error handler
    oldHandler = gsl_set_error_handler (&handler);

    // get a RNG.
    rng = gsl_rng_alloc(gsl_rng_mt19937);
    gsl_set_error_handler_off ();

    // create the mean/variance structures.
    currentSize = INITIAL_SIZE;
    mean = (double *)malloc(currentSize * sizeof(double));
    covar = (double **)malloc(currentSize * sizeof(double *));
    for (int i=0;i<currentSize;i++) {
      covar[i] = (double *)malloc(currentSize * sizeof(double));
    }

    sizeInternals = 0;
    runningError = 0;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~MultivariateNormal() {
	
#ifdef DEBUG
    printf("[MultivariateNormal]: destructor.\n");
    fflush(stdout);
#endif

    // destroy the RNG.
    gsl_rng_free(rng);	

    // reset the error handler
    gsl_set_error_handler(oldHandler);

    // destroy the mean/variance structures
    free(mean);
    for (int i=0;i<currentSize;i++) {
      free(covar[i]);
    }

    free(covar);

    // destroy the internal structures, if we ever declared them
    if (sizeInternals > 0) {
      free(inCov);
      free(N);
      free(samples);
    }
  }

  /** Initializes the RNG seed for a given call. */
  void initializeSeed(long seedValue) {

#ifdef DEBUG
    printf("[MultivariateNormal]: initializeSeed(%ld).\n", seedValue);
    fflush(stdout);
#endif

    // set the seed.
    gsl_rng_set(rng, seedValue);
  }

  /** Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). */
  void finalizeTrial() {

#ifdef DEBUG
    printf("[MultivariateNormal]: finalizeTrial.\n");
    fflush(stdout);
#endif

    // set active
    active = (num_rv > 0);

    // we have to sample again, but the Cholesky decomp. stays the same.
    sampled = false;
    currentRV = 0;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

#ifdef DEBUG
    printf("[MultivariateNormal]: clearParams.\n");
#endif

    // set the flags back
    active = false;
    decomposed = false;
    sampled = false;
    currentRV = 0;
    num_rv = 0;

    // fill up
    for (int i=0;i<currentSize;i++) {
      mean[i] = 0.0;
      for (int j=0;j<currentSize;j++) {
	covar[i][j] = 0.0;
      }
    }
  }

	
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

#ifdef DEBUG
    printf("[MultivariateNormal]: takeParams(");

    if (input.rv == NULL || input.mean == NULL)
      printf("NULL, NULL, ");
    else
      printf("%ld, %f, ", *input.rv, *input.mean);

    if (input.rv_x == NULL || input.rv_y == NULL || input.covar == NULL)
      printf("NULL, NULL, NULL");
    else
      printf("%ld, %ld, %f", *input.rv_x, *input.rv_y, *input.covar);

    printf(").\n");
    fflush(stdout);
#endif

    // got a mean value?
    if (input.rv != NULL && input.mean != NULL) {
      
      // do we need to resize?
      if (*input.rv >= currentSize) {
	
	// max(number of RVs in question, 150% of current size).
	resizeVectors(MAX(*input.rv + 1, currentSize + (currentSize >> 1)));
      }

      // set the value
      mean[*input.rv] = *input.mean;

      // increase the number of RVs
      num_rv++;
    }

    // got a covariance value?
    if (input.rv_x != NULL && input.rv_y != NULL && input.covar != NULL) {

      // do we need to resize?
      if (*input.rv_x >= currentSize || *input.rv_y >= currentSize) {

	// max(number of RV in question, 50% of current size).
	long maxA = MAX(*input.rv_x + 1, *input.rv_y + 1);
	resizeVectors(MAX(maxA, currentSize + (currentSize >> 1)));
      }
            
      // set the values
      covar[*input.rv_x][*input.rv_y] = *input.covar;
    }
    
    // set active
    active = (num_rv > 0);
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 
    
#ifdef DEBUG
    printf("[MultivariateNormal]: outputVals(%ld, %ld) => ", num_rv, currentRV);
    fflush(stdout);
#endif

    // active?
    if (!active) {

#ifdef DEBUG
      printf("<NULL, NULL>\n");
#endif

      return 0;
    }

    // have we already decomposed the covariance matrix?
    gsl_matrix_view sigma;
    if (!decomposed) {
      
      // if not, declare the internal structures
      // do we have them already?
      if (sizeInternals <= 0) {

	// if not, create them
	inCov = (double *)malloc((num_rv * num_rv) * sizeof(double));
	N = (double *)malloc(num_rv * sizeof(double));
	samples = (double *)malloc(num_rv * sizeof(double));
	sizeInternals = num_rv;
      }

      // are they large enough?
      if (sizeInternals < num_rv) {

	// if not, reallocate them
	inCov = (double *)realloc(inCov, (num_rv * num_rv) * sizeof(double));
	N = (double *)realloc(N, num_rv * sizeof(double));
	samples = (double *)realloc(samples, num_rv * sizeof(double));
	sizeInternals = num_rv;
      }

      // populate the covariance matrix
      for (long i=0;i<num_rv;i++) {
	for (long j=0;j<num_rv;j++) {
	  inCov[i*num_rv + j] = covar[i][j];
	}
      }

      // get it in GSL format
      sigma = gsl_matrix_view_array(inCov, num_rv, num_rv);
      
      // do a Cholesky decomposition
      gsl_linalg_cholesky_decomp(&sigma.matrix);

      // was it correct?
      if (runningError == 1) {
	printf("[MultivariateNormal]: Failed to do the Cholesky decomposition (maybe the covariance matrix was not positive-definite?).");
	fflush(stdout);
	runningError = 0;

	// get an identity matrix.
	for (long i=0;i<num_rv;i++) {
	  for (long j=0;j<num_rv;j++) {
	    if (i == j) {
	      inCov[i*num_rv + j] = 1.0;
	    }
	    else {
	      inCov[i*num_rv + j] = 0.0;
	    }
	  }
	}
      }

      // set the flag.
      decomposed = true;
    }
    else {

      // just get it in GSL format if it's already decomposed.
      sigma = gsl_matrix_view_array(inCov, num_rv, num_rv);
    }

    // have we obtained the samples already?
    if (!sampled) {

      // obtain an array of standard gaussian samples
      for (long i=0;i<num_rv;i++) {
	N[i] = gsl_ran_ugaussian(rng);
      }

      // and multiply it by the covariance matrix and add the
      // mean vector.
      for (long i=0;i<num_rv;i++) {

	// set the zero.
	samples[i] = 0;

	// add the multiplication.
	for (long j=i;j<num_rv;j++) {
	  samples[i] += gsl_matrix_get(&sigma.matrix, i, j) * N[j];
	}

	// add the mean vector.
	samples[i] += mean[i];
      }

      sampled = true;
    }
       
    // prepare the output.
    outRV = currentRV;
    outValue = samples[outRV];
    output.rv = &outRV;
    output.value = &outValue;

#ifdef DEBUG
    printf("<%ld, %f>\n", outRV, outValue);
    fflush(stdout);
#endif

    // set inactive and return
    currentRV++;
    active = (currentRV < num_rv);

    return 1;
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){5, {INT, DOUBLE, INT, INT, DOUBLE}, {"rv", "mean", "rv_x", "rv_y", "covar"}, {0, 1, 0, 0, 1}};
  }

  VGSchema outputSchema() {

    return (VGSchema){2, {INT, DOUBLE}, {"rv", "value"}, {0, 1}};
  }

  const char *getName() {
    return "MultivariateNormal";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new MultivariateNormal());
}

void destroy(VGFunction *vgFunction) {
  delete (MultivariateNormal *)vgFunction;
}

