

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


// for Emacs -*- mode:c++ -*-

#ifndef _INVGAUSSIAN_H
#define _INVGAUSSIAN_H

#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include <math.h>
#include <time.h>

/**
 * This is a class for sampling from an inverse-Gaussian
 * distribution. This one is necessary for the Bayesian Lasso.
 *
 * @author L.L. Perez <lp6@rice.edu>
 */
class InverseGaussian {
public:

  // destructor.
  ~InverseGaussian();

  // constructor with specified parameters.
  InverseGaussian(double mean=1.0, double shape=1.0);

  // copy constructor.
  InverseGaussian(InverseGaussian &me);

  // sets the mean parameter, must be greater than zero.
  // returns true if successful, false otherwise.
  bool SetMean(double mean);

  // sets the shape parameter, must be greater than zero.
  // returns true if successful, false otherwise.
  bool SetShape(double shape);

  // clears the distribution parameters, going back to mean=1 and shape=1.
  void ClearParameters();

  // samples from the distribution with defined mean and shape, using
  // the desired GSL random number generator (if NULL, the default
  // will be used). returns the sampled value if successful, NaN otherwise.
  double Sample(const gsl_rng *r = NULL);

  // returns the mean parameter.
  double GetMean();

  // returns the shape parameter.
  double GetShape();

  // displays the parameters of the distribution.
  void Print(FILE *fd = stdout);

private:

  // mean parameter.
  double mean;

  // shape parameter.
  double shape;

  // default GSL RNG.
  gsl_rng *rngDefault;
};

#endif // _INVGAUSSIAN_H
