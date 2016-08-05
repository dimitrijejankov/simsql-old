

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


#include "InverseGaussian.h"

InverseGaussian::~InverseGaussian() {

  // destroy the default RNG, if we ever created one.
  if (rngDefault != NULL) {
    gsl_rng_free(rngDefault);
    rngDefault = NULL;
  }

}

InverseGaussian::InverseGaussian(double meanIn, double shapeIn) {
  SetMean(meanIn);
  SetShape(shapeIn);
  rngDefault = NULL;
}

InverseGaussian::InverseGaussian(InverseGaussian &me) {
  mean = me.mean;
  shape = me.shape;
  rngDefault = NULL;
}

bool InverseGaussian::SetMean(double meanIn) {

  if (meanIn < 0 || isnan(meanIn) || !isfinite(meanIn)) {
    return false;
  }

  mean = meanIn;

  return true;
}

bool InverseGaussian::SetShape(double shapeIn) {

  if (shapeIn < 0 || isnan(shapeIn) || !isfinite(shapeIn)) {
    return false;
  }

  shape = shapeIn;

  return true;
}

void InverseGaussian::ClearParameters() {
  mean = 1.0;
  shape = 1.0;
}

double InverseGaussian::Sample(const gsl_rng *_rng) {
  
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

  // this is the standard sampling method described in the 1976 paper
  // by Michael, et al.  "Generating Random Variates Using
  // Transformations with Multiple Roots"

  double y = gsl_ran_ugaussian(rng);
  y *= y;

  double x = mean + ((mean*mean*y) / (2*shape)) - ((mean / (2*shape)) * sqrt(4*mean*shape*y + (mean*mean*y*y)));
  double z = gsl_rng_uniform(rng);

  if (z <= mean / (mean + x)) {
    return x;
  } else {
    return (mean*mean)/x;
  }
}

double InverseGaussian::GetMean() {
  return mean;
}

double InverseGaussian::GetShape() {
  return shape;
}

void InverseGaussian::Print(FILE *fd) {

  fprintf(fd, "mean = %f;\nshape = %f;\n", mean, shape);
}
