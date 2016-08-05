

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


#include "Gaussian.h"


Gaussian::~Gaussian() {

  // free up all our structures.
  if (mean != NULL) {
    free(mean);
    mean = NULL;
  }

  if (covariance != NULL) {

    for (int i=0;i<capacity;i++) {
      if (covariance[i] != NULL) {
	free(covariance[i]);
	covariance[i] = NULL;
      }
    }

    free(covariance);
    covariance = NULL;
  }

  if (observedRV != NULL) {
    free(observedRV);
    observedRV = NULL;
  }

  if (observed != NULL) {
    free(observed);
    observed = NULL;
  }

  if (rngDefault != NULL) {
    gsl_rng_free(rngDefault);
    rngDefault = NULL;
  }

  if (cov1 != NULL) {
    free(cov1);
    cov1 = NULL;
  }

  if (cov2 != NULL) {
    free(cov2);
    cov2 = NULL;
  }

  if (mean1 != NULL) {
    free(mean1);
    mean1 = NULL;
  }

  if (samp1 != NULL) {
    free(samp1);
    samp1 = NULL;
  }

  if (pos1 != NULL) {
    free(pos1);
    pos1 = NULL;
  }

  numRVs = 0;
  capacity = 0;
  resized = false;
  changed = false;
  dO = false;
  dR = false;
}

Gaussian::Gaussian(const unsigned int _numRVs) {
  Init(_numRVs);
}

Gaussian::Gaussian(const unsigned int _numRVs, const double *_mean, const double **_covariance) {
  Init(_numRVs);

  if (_covariance == NULL) {
    return;
  }

  // set the values.
  for (int i=0;i<_numRVs;i++) {

    if (_covariance[i] == NULL) {
      return;
    }

    SetMean(i, _mean[i]);

    for (int j=0;j<=i;j++) {
      SetCovariance(i, j, _covariance[i][j]);
    }
  }
}

Gaussian::Gaussian(const unsigned int _numRVs, const double *_mean, const double *_covariance) {
  Init(_numRVs);

  // sanity check
  if (_covariance == NULL) {
    return;
  }

  // set the values.
  gsl_matrix_const_view v = gsl_matrix_const_view_array(_covariance, _numRVs, _numRVs);

  for (int i=0;i<_numRVs;i++) {
    SetMean(i, _mean[i]);

    for (int j=0;j<=i;j++) {
      SetCovariance(i, j, gsl_matrix_get(&v.matrix, i, j));
    }
  }
}

Gaussian::Gaussian(const Gaussian &copyMe) {

  // initialize
  Init(copyMe.numRVs);

  // and copy!
  for (int i=0;i<numRVs;i++) {
    mean[i] = copyMe.mean[i];
    observedRV[i] = copyMe.observedRV[i];
    observed[i] = copyMe.observed[i];

    for (int j=0;j<=i;j++) {
      covariance[i][j] = copyMe.covariance[i][j];
    }
  }
}

void Gaussian::EnsureCapacity(const unsigned int _n) {

  // do we have enough space?
  if (_n < capacity) {
    return;
  }

  // set for doubling.
  unsigned int n = MAX(_n, capacity*2);

  // if we don't, let's set everything up.
  mean = (double *)realloc(mean, sizeof(double) * n);
  observed = (double *)realloc(observed, sizeof(double) * n);
  covariance = (double **)realloc(covariance, sizeof(double *) * n);
  observedRV = (bool *)realloc(observedRV, sizeof(bool) * n);

  // standard gaussian values, nothing observed.
  for (int i=capacity;i<n;i++) {

    mean[i] = 0.0;
    observed[i] = 0.0;
    observedRV[i] = false;

    // identity triangular matrix.
    covariance[i] = (double *)malloc(sizeof(double) * (i + 1));
    for (int j=0;j<i;j++) {
      covariance[i][j] = 0.0;
    }

    covariance[i][i] = 1.0;
  }

  // done
  capacity = n;

  // set up our changes.
  resized = true;
  changed = true;
}


void Gaussian::Init(unsigned int _numRVs) {
  numRVs = _numRVs;
  capacity = 0;
  mean = NULL;
  observed = NULL;
  observedRV = NULL;
  covariance = NULL;
  rngDefault = NULL;
  cov1 = NULL;
  cov2 = NULL;
  mean1 = NULL;
  pos1 = NULL;
  samp1 = NULL;

  // ensure a minimum capacity
  EnsureCapacity(MAX(numRVs, _GAUSSIAN_MIN_CAPACITY));
}

unsigned int Gaussian::SetMean(const unsigned int i, const double value) {

  // make sure the number is meaningful
  if (isnan(value) || !isfinite(value)) {
    return 0;
  }

  // make sure we have space
  EnsureCapacity(i+1);

  // now, set it.
  mean[i] = value;
  
  // set the number of RVs, if necessary.
  numRVs = MAX(i+1, numRVs);

  // we have changed (not necessarily resized).
  changed = true;

  return numRVs;
}

unsigned int Gaussian::SetCovariance(const unsigned int i, const unsigned int j, const double value) { 

  // make sure the number is meaningful
  if (isnan(value) || !isfinite(value)) {
    return 0;
  }

  // make sure we have space
  EnsureCapacity(MAX(i,j)+1);

  // now, set it
  if (j > i) {

    // if upper triangular, flip.
    covariance[j][i] = value;
  } else {
    covariance[i][j] = value;
  }
  
  // set the number of RVs.
  numRVs = MAX(MAX(i, j) + 1, numRVs);

  // set changed.
  changed = true;
  return numRVs;
}

unsigned int Gaussian::Condition(const unsigned int i, const double value) {

  // check that the value is good.
  if (isnan(value) || !isfinite(value)) {
    return 0;
  }

  // make sure we have space.
  EnsureCapacity(i+1);

  // then, set it.
  observed[i] = value;
  observedRV[i] = true;

  // set as changed
  changed = true;

  // update the number of RVs.
  numRVs = MAX(i+1, numRVs);

  return numRVs;
}


unsigned int Gaussian::ClearParameters(const bool keepRVs) {

  for (int i=0;i<capacity;i++) {
    mean[i] = 0.0;
    observed[i] = 0.0;
    observedRV[i] = false;

    for (int j=0;j<=i;j++) {
      covariance[i][j] = 0.0;
    }

    covariance[i][i] = 1.0;
  }

  if (!keepRVs) {
    numRVs = 1;
  }

  return numRVs;
}

unsigned int Gaussian::Sample(double *output, const gsl_rng *_rng) {

  // sanity checks.
  if (output == NULL) {
    return 0;
  }

  // check the RNG.
  gsl_rng *rng = (gsl_rng *)_rng;
  if (rng == NULL) {

    if (rngDefault == NULL) {

      // allocate default GSL RNG.
      rngDefault = gsl_rng_alloc(gsl_rng_default);
      if (rngDefault == NULL) {
	return 0;
      }
      gsl_rng_set(rngDefault, time(NULL));
    }

    rng = rngDefault;
  }

  // see if we have resized our structures.  if so, we'll have to
  // re-do the cholesky decomp and conditioning, but we'd like to
  // recycle that space as much as possible.
  ResizeCovariance();

  // if the parameters and/or observed values have changed, we'll have
  // to re-calculate the whole conditioning and cholesky decomposition.
  if (UpdateCovariance()) {

    // now, let's do the sampling.

    // we start by obtaining a vector of independent gaussian samples
    // and the observed values.
    for (int i=0;i<numRVs;i++) {
      if (observedRV[i]) {
	samp1[i] = 0.0;
	continue;
      }

      samp1[i] = gsl_ran_ugaussian(rng);
    }

    // then, we do our straightforward matrix-vector multiplication.
    gsl_matrix_view sigma = gsl_matrix_view_array(cov1, numRVs, numRVs);

    for (int i=0;i<numRVs;i++) {

      if (observedRV[i]) {
	output[i] = observed[i];
	continue;
      }

      output[i] = 0;

      for (int j=0;j<=i;j++) {
	output[i] += gsl_matrix_get(&sigma.matrix, i, j) * samp1[j];
      }

      output[i] += mean1[i];
    }

    // and we're done.
    return numRVs;
  }

  return 0;
}

void Gaussian::Print(const bool printObserved, FILE *fd) {

  fprintf(fd, "mu = [");
  for (int i=0;i<numRVs;i++) {
    fprintf(fd, "%f ", mean[i]);
  }
  fprintf(fd, "];\n\n");

  fprintf(fd, "cov = [\n");
  for (int i=0;i<numRVs;i++) {

    for (int j=0;j<=i;j++) {
      fprintf(fd, "%f ", covariance[i][j]);
    }

    for (int j=i+1;j<numRVs;j++) {
      fprintf(fd, "%f ", covariance[j][i]);
    }
    fprintf(fd, "\n");
  }
  fprintf(fd, "];\n\n");

  fprintf(fd, "conditioned = [\n");
  for (int i=0;i<numRVs;i++) {
    if (observedRV[i]) {
      fprintf(fd, "%i %f\n", i, observed[i]);
    }
  }

  fprintf(fd, "];\n\n");

  if (printObserved) {
    ResizeCovariance();
    if (UpdateCovariance()) {
      gsl_vector_view v = gsl_vector_view_array(mean1, numRVs);
      fprintf(fd, "condMean = [\n");
      gsl_vector_fprintf(fd, &v.vector, "%f");
      fprintf(fd, "];\n\n");

      gsl_matrix_view m = gsl_matrix_view_array(cov1, numRVs, numRVs);
      fprintf(fd, "condVar = [\n");
      for (int i=0;i<numRVs;i++) {
	for (int j=0;j<numRVs;j++) {
	  fprintf(fd, "%f ", gsl_matrix_get(&m.matrix, i, j));
	}
	fprintf(fd, "\n");
      }
      fprintf(fd, "];\n\n");
    }
  }
}

void Gaussian::ResizeCovariance() {

  // update to our current capacity.
  if (resized) {
    cov1 = (double *)realloc(cov1, sizeof(double) * (capacity * capacity));
    
    // never used, unless someone is calling GetCovariance(double **)
    if (cov2 != NULL) {
      cov2 = (double *)realloc(cov2, sizeof(double) * (capacity * capacity));
    }

    pos1 = (unsigned int *)realloc(pos1, sizeof(unsigned int) * capacity);
    mean1 = (double *)realloc(mean1, sizeof(double) * capacity);
    samp1 = (double *)realloc(samp1, sizeof(double) * capacity);
    resized = false;
    changed = true;
  }
}

bool Gaussian::UpdateCovariance() {

  // see if we actually need to update.
  if (!changed) {
    return true;
  }
  
  // check the observed variables.
  dO = 0;
  for (int i=0;i<numRVs;i++) {
    dO += (observedRV[i]);
  }

  // get the number of random variables.
  dR = numRVs - dO;

  // get views for our data structures.
  gsl_matrix_view sigma = gsl_matrix_view_array(cov1, numRVs, numRVs);
  gsl_vector_view mu = gsl_vector_view_array(mean1, numRVs);

  // zero everything out.
  gsl_matrix_set_identity(&sigma.matrix);
  gsl_vector_set_zero(&mu.vector);

  // if all variables are observed, we don't have to do anything!
  if (dR == 0) {
    changed = false;
    return true;
  }

  // obtain array positions so that the first [0,...,dR-1] are random,
  // and the remaining [dR,...,numRVs-1] are observed.
  for (int i=0, l=0, k=dR;i<numRVs;i++) {

    if (observedRV[i]) {
      pos1[i] = k;
      k++;
    } else {
      pos1[i] = l;
      l++;
    }
  }


  // populate the structures
  for (int i=0;i<numRVs;i++) {
    gsl_vector_set(&mu.vector, pos1[i], mean[i]);

    for (int j=0;j<=i;j++) {
      gsl_matrix_set(&sigma.matrix, pos1[i], pos1[j], covariance[i][j]);
      gsl_matrix_set(&sigma.matrix, pos1[j], pos1[i], covariance[i][j]);
    }
  }

  // if all variables are random, it's just a matter of getting the
  // cholesky decomposition of the entire covariance matrix.
  if (dO == 0) {

    // obtain its cholesky decomposition.
    int ret = gsl_linalg_cholesky_decomp(&sigma.matrix);

    // return true if the decomposition was succesful.
    changed = false;
    return (ret == GSL_SUCCESS);
  }


  // random mean vector
  gsl_vector_view mu1 = gsl_vector_subvector(&mu.vector, 0, dR);

  // observed mean vector
  gsl_vector_view mu2 = gsl_vector_subvector(&mu.vector, dR, dO);

  // random covariance matrix
  gsl_matrix_view sig11 = gsl_matrix_submatrix(&sigma.matrix, 0, 0, dR, dR);

  // observed covariance matrix
  gsl_matrix_view sig22 = gsl_matrix_submatrix(&sigma.matrix, dR, dR, dO, dO);

  // random-observed covarinace matrices
  gsl_matrix_view sig12 = gsl_matrix_submatrix(&sigma.matrix, 0, dR, dR, dO);
  gsl_matrix_view sig21 = gsl_matrix_submatrix(&sigma.matrix, dR, 0, dO, dR);

  // now that we've done this copy, we'll proceed with the conditioning.

  // first, invert sig22 so that we can multiply it.
  if (gsl_linalg_cholesky_decomp(&sig22.matrix) != GSL_SUCCESS) {
    return false;
  }

  // first, do sig12 <- sig12 * inv(sig22). needs to be done twice, as dtrsm uses only one of the
  // triangles of the matrix
  if (gsl_blas_dtrsm(CblasRight, CblasLower, CblasTrans, CblasNonUnit, 1.0, &sig22.matrix, &sig12.matrix) != GSL_SUCCESS) {
    return false;
  }

  if (gsl_blas_dtrsm(CblasRight, CblasLower, CblasNoTrans, CblasNonUnit, 1.0, &sig22.matrix, &sig12.matrix) != GSL_SUCCESS) {
    return false;
  }

  // then, mu1 <- mu1 + sig12*mu2
  if (gsl_blas_dgemv(CblasNoTrans, 1.0, &sig12.matrix, &mu2.vector, 1.0, &mu1.vector) != GSL_SUCCESS) {
    return false;
  }

  // then, sig11 <- -sig12*sig21 + sig11
  if (gsl_blas_dgemm(CblasNoTrans, CblasNoTrans, -1.0, &sig12.matrix, &sig21.matrix, 1.0, &sig11.matrix) != GSL_SUCCESS) {
    return false;
  }

  // at this point, sig11/cov1 contains the conditioned matrix and mu1/mean1 the mean vector.

  // we'll zero out everything in the remaining matrices.
  gsl_matrix_set_zero(&sig12.matrix);
  gsl_matrix_set_zero(&sig21.matrix);
  gsl_matrix_set_zero(&sig22.matrix);
  gsl_vector_set_zero(&mu2.vector);

  // before moving on, we will we do cholesky decomposition on the remaining random variables.
  int r = gsl_linalg_cholesky_decomp(&sig11.matrix);

  // and swap the columns and rows back to what they were originally
  for (int i=0;i<numRVs;i++) {

    if (observedRV[i]) {
      gsl_vector_swap_elements(&mu.vector, i, pos1[i]);
      gsl_matrix_swap_rows(&sigma.matrix, i, pos1[i]);
      gsl_matrix_swap_columns(&sigma.matrix, i, pos1[i]);
      gsl_matrix_set(&sigma.matrix, i, i, 1.0);
    }    
  }

  changed = false;
  
  return (r == GSL_SUCCESS);
}

unsigned int Gaussian::GetNumRVs() {
  return numRVs;
}

unsigned int Gaussian::GetMean(double *vector) {

  // check
  if (vector == NULL) {
    return 0;
  }

  ResizeCovariance();
  if (!UpdateCovariance()) {
    return 0;
  }

  memcpy(vector, mean1, sizeof(double) * numRVs);
  return numRVs;
}

unsigned int Gaussian::GetCovariance(double *matrix) {

  // check
  if (matrix == NULL) {
    return 0;
  }

  ResizeCovariance();
  if (!UpdateCovariance()) {
    return 0;
  }
  // un-do the cholesky.
  gsl_matrix_view c = gsl_matrix_view_array(matrix, numRVs, numRVs);
  gsl_matrix_view v = gsl_matrix_view_array(cov1, numRVs, numRVs);
  
  if (gsl_blas_dsyrk(CblasLower, CblasTrans, 1.0, &v.matrix, 0.0, &c.matrix) != GSL_SUCCESS) {
    return 0;
  }

  return numRVs;
}

unsigned int Gaussian::GetCovariance(double **matrix) {

  if (matrix == NULL) {
    return 0;
  }

  if (cov2 == NULL) {
    cov2 = (double *)malloc(sizeof(double) * (capacity * capacity));
  }

  if (GetCovariance(cov2) != numRVs) {
    return 0;
  }

  // copy
  gsl_matrix_view c = gsl_matrix_view_array(cov1, numRVs, numRVs);
  for (int i=0;i<numRVs;i++) {

    if (matrix[i] == NULL) {
      return 0;
    }

    for (int j=0;j<=i;j++) {
      matrix[i][j] = gsl_matrix_get(&c.matrix, i, j);
      matrix[j][i] = gsl_matrix_get(&c.matrix, i, j);
    }
  }

  return numRVs;
}
