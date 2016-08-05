

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


// for Emacs -*- mode: c++ -*-

#ifndef _GAUSSIAN_H
#define _GAUSSIAN_H

#include <gsl/gsl_rng.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_blas.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_randist.h>
#include <stdlib.h>
#include <math.h>
#include <time.h>
#include <string.h>

#define MAX(a,b) ((a) > (b) ? (a) : (b))

#define _GAUSSIAN_MIN_CAPACITY 10

/**
 * This is a class for easy sampling from a Multivariate Gaussian
 * distribution, with the option of conditioning some of its random
 * variables upon specific observed values. The interface follows the
 * contract of the MCDB/SimSQL database system, which allows for
 * parameterizing the distribution in arbitrary order via multiple
 * calls, without knowledge of the number of random variables
 * involved.
 *
 * By default, instances of this class are created with standard
 * univariate Gaussian parameters. If a larger number of random
 * variables is specified, the default distribution has a mean vector
 * of zero and an identity covariance matrix.
 *
 * Although the parameters of the distribution can be given upon
 * object construction, they don't necessarily have to be, and can be
 * changed and extended via calls to the SetMean() and SetCovariance()
 * methods. For example, an object containing a univariate Gaussian
 * distribution handles a request to set the mean of random variable 3
 * to 7.5 by expanding the mean vector and covariance matrix so that
 * they can hold 4 random variables. There is no limit on the amount
 * of random variables, so be cautious!
 *
 * All the matrix operations and sampling are done with standard GSL
 * functions, written in a way that minimizes the amount of requests
 * for memory allocation. The user is responsible for
 * setting/unsetting any GSL error handling.
 *
 * @author L.L. Perez
 */

class Gaussian {

public:

  // basic destructor
  ~Gaussian();

  // creates an instance with a mean vector of zeros and an identity
  // covariance matrix. by default, a univariate gaussian is used.
  Gaussian(const unsigned int numRVs = 1);

  // constructor with mean and covariance parameters.
  Gaussian(const unsigned int numRVs, const double *mean, const double **covariance);

  // similar to the above constructor, but the covariance matrix is
  // presented as a row-major array where the (i,j)th element is in
  // position numRVs*j + i.
  Gaussian(const unsigned int numRVs, const double *mean, const double *covariance);

  // copy constructor -- does a deep copy of the distribution parameters.
  Gaussian(const Gaussian &copyMe);

  // sets the mean of a given variable. if rv >= numRVs, the number of
  // variables is increased.  returns the number of variables if
  // successful, zero otherwise.
  unsigned int SetMean(const unsigned int rv, const double value);

  // sets the covariance between a pair of random variables, adding
  // more if necessary. returns the number of variables if successful,
  // zero otherwise.
  unsigned int SetCovariance(const unsigned int rv1, const unsigned int rv2, const double value);

  // clears the distribution parameters. if keepRVs = true, returns to
  // a standard gaussian with the current number of random variables;
  // if false, returns to a univariate gaussian.
  unsigned int ClearParameters(const bool keepRVs = false);

  // samples from the distribution with defined mean and covariance,
  // conditioned on the observations, using the desired GSL random
  // number generator (if NULL, the default will be used). returns the
  // number of RVs if successful, zero otherwise. the output values
  // are written into the user-declared array output.
  unsigned int Sample(double *output, const gsl_rng *r = NULL);

  // conditions the distribution on an observed value,
  // obtaining a new mean and covariance. returns the number of random
  // variables if succesful, zero otherwise.
  unsigned int Condition(const unsigned int rv, const double value);

  // returns the number of random variables
  unsigned int GetNumRVs();

  // returns the contitioned mean vector of the distribution. observed
  // values are zero'd out. returns the number of random variables if
  // successful, zero otherwise. the parameter 'vector' must be
  // allocated/destroyed by the user.
  unsigned int GetMean(double *vector);

  // returns the conditioned covariance matrix of the
  // distribution. observed variables are zero'd out. returns the
  // number of random variables if successful, zero otherwise. the
  // parameter 'matrix' must be allocated by the user as a n*n array,
  // where n is the number of random variables, so that the (i,j)^th
  // element of the matrix can be found in position j*n + i. the
  // matrix is symmetric and both the upper and lower triangles are
  // filled.
  unsigned int GetCovariance(double *matrix);

  // similar to the above, but the parameter is a vector of n
  // pointers, each pointing to an array of n double elements. the
  // user is responsible for allocating and destroying it.
  unsigned int GetCovariance(double **matrix);

  // displays the parameters of the distribution.
  // if printObserved is true, it will also print the conditioned
  // mean vector and covariance matrix.
  void Print(const bool printObserved = false, FILE *fd = stdout);

private:

  // number of random variables
  unsigned int numRVs;

  // capacity of our structures
  unsigned int capacity;

  // mean vector.
  double *mean;

  // covariance matrix.
  double **covariance;

  // observations for conditioning.
  bool *observedRV;
  double *observed;

  // tracks the state of parameter/matrix changes.
  bool resized;
  bool changed;

  // default GSL RNG.
  gsl_rng *rngDefault;

  // stores the cholesky decomposition of the covariance matrix (is
  // updated upon sampling if the parameters changed).
  double *cov1;

  // used by calls to GetCovariance(double **) to store temporary
  // results.
  double *cov2;

  // stores the updated mean vector after observation (similarly
  // updated if the parameters change).
  double *mean1;

  // for sampled values
  double *samp1;

  // stores a mapping of array positions for observed/random
  // variables.
  unsigned int *pos1;

  // tracks the numer of observed vs. random variables.
  unsigned int dO;
  unsigned int dR;

  // initializes -- common constructor code.
  void Init(const unsigned int numRVs);

  // makes sure we have enough capacity for the parameters.
  void EnsureCapacity(const unsigned int n);

  // resize the covariance matrix and other structures used for
  // sampling and conditioning.
  void ResizeCovariance();

  // updates the covariance matrix for sampling. returns false if
  // unsuccesful.
  bool UpdateCovariance();
};

#endif // _GAUSSIAN_H
