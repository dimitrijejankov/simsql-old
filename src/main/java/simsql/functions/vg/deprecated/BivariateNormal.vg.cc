

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
#include <stdio.h>
#include <math.h>

// #define DEBUG

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  double *avg1;
  double *avg2;
  double *var1;
  double *var2;
  double *covar;
  double *default1;
  double *default2;
};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  double *value1;
  double *value2;
};

// ----------------------------------------------------------- // 

/** VG Function class */
class BivariateNormal : public VGFunction {

private:
  /** RNG and structures go here. */
  gsl_rng *rng;

  // flags
  bool active;
  bool haveD1;
  bool haveD2;
  bool haveM1;
  bool haveM2;
  bool haveV1;
  bool haveV2;
  bool haveCovar;

  // parameters
  double avg1;
  double avg2;
  double var1;
  double var2;
  double covar;
  double default1;
  double default2;

  // output values.
  double val1, val2, sampled1, sampled2;

  // ----------------------------------------------------------- // 

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures .
   */
  BivariateNormal() {

#ifdef DEBUG
    printf("[BivariateNormal]: constructor.\n");
#endif

    /** initialize a mersenne-twister RNG */
    rng = gsl_rng_alloc(gsl_rng_mt19937);
    active = false;
    haveD1 = false;
    haveD2 = false;
    haveM1 = false;
    haveM2 = false;
    haveV1 = false;
    haveV2 = false;
    haveCovar = false;
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~BivariateNormal() {

#ifdef DEBUG
    printf("[BivariateNormal]: destructor.\n");
#endif

    gsl_rng_free(rng);
  }

  /** Initializes the RNG seed for every group. */
  void initializeSeed(long seedValue) {

#ifdef DEBUG
    printf("[BivariateNormal]: initializeSeed(%ld).\n", seedValue);
#endif

    /** set the seed with our parameter */
    gsl_rng_set(rng, seedValue);
  }

  /** Finalizes the current group and prepares the structure for
   * the next call to outputVals(). */
  void finalizeTrial() {

#ifdef DEBUG
    printf("[BivariateNormal]: finalizeTrial.\n");
#endif

    active = (haveM1 && haveM2 && haveV1 && haveV2 && haveCovar);
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

#ifdef DEBUG
    printf("[BivariateNormal]: clearParams.\n");
#endif

    active = false;
    haveD1 = false;
    haveD2 = false;
    haveM1 = false;
    haveM2 = false;
    haveV1 = false;
    haveV2 = false;
    haveCovar = false;
  }
  
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

#ifdef DEBUG
    printf("[BivariateNormal]: takeParams(");
#endif

    if (input.avg1 != NULL) {
      avg1 = *input.avg1;
      haveM1 = true;

#ifdef DEBUG
      printf("avg1=%f ", avg1);
#endif
    }

    if (input.avg2 != NULL) {
      avg2 = *input.avg2;
      haveM2 = true;

#ifdef DEBUG
      printf("avg2=%f ", avg2);
#endif
    }

    if (input.var1 != NULL) {
      var1 = *input.var1;
      haveV1 = true;

#ifdef DEBUG
      printf("var1=%f ", var1);
#endif
    }

    if (input.var2 != NULL) {
      var2 = *input.var2;
      haveV2 = true;

#ifdef DEBUG
      printf("var2=%f ", var2);
#endif      
    }

    if (input.covar != NULL) {
      covar = *input.covar;
      haveCovar = true;

#ifdef DEBUG
      printf("covar=%f ", covar);
#endif
    }

    if (input.default1 != NULL) {
      default1 = *input.default1;
      haveD1 = true;

#ifdef DEBUG
      printf("default1=%f ", default1);
#endif
    }

    if (input.default2 != NULL) {
      default2 = *input.default2;
      haveD2 = true;

#ifdef DEBUG
      printf("default2=%f ", default2);
#endif
    }

#ifdef DEBUG
    printf(").\n");
#endif

    active = (haveM1 && haveM2 && haveV1 && haveV2 && haveCovar);
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 
    
    if (!active)
      return 0;

    // put both defaults
    if (haveD1 && haveD2) {

      output.value1 = &default1;
      output.value2 = &default2;

#ifdef DEBUG
      printf("[BivariateNormal] => %f, %f (default values).\n", *output.value1, *output.value2);
#endif

      active = false;
      return 1;
    }

    // sample dimension 2 given the default dimension 1 value.
    if (haveD1 && !haveD2) {

      double condMean = avg2 + ((covar / var1) * (default1 - avg1));
      double condVar = var2 - ((covar*covar)/var1);
      
#ifdef DEBUG      
      printf("[BivariateNormal] conditional mu = %f / sigma^2 = %f\n", condMean, condVar);
#endif

      sampled2 = condMean + gsl_ran_gaussian(rng, sqrt(condVar));
      output.value1 = &default1;
      output.value2 = &sampled2;
      
#ifdef DEBUG
      printf("[BivariateNormal] => %f, %f (default 1, sampled 2).\n", *output.value1, *output.value2);
#endif
      
      active = false;
      return 1;
    }
    
    // sample dimension 1 given the default dimension 2 value
    if (!haveD1 && haveD2) {

      double condMean = avg1 + ((covar / var2)*(default2 - avg2));
      double condVar = var1 - ((covar*covar) / var2);

#ifdef DEBUG      
      printf("[BivariateNormal] conditional mu = %f / sigma^2 = %f\n", condMean, condVar);
#endif

      sampled1 = condMean + gsl_ran_gaussian(rng, sqrt(condVar));
      output.value1 = &sampled1;
      output.value2 = &default2;
      
      
#ifdef DEBUG
      printf("[BivariateNormal] => %f, %f (sampled 1, default 2).\n", *output.value1, *output.value2);
#endif
      
      active = false;
      return 1;
    }
    
    gsl_ran_bivariate_gaussian(rng, sqrt(var1), sqrt(var2), covar / (sqrt(var1)*sqrt(var2)), &val1, &val2);

    val1 += avg1;
    val2 += avg2;

    output.value1 = &val1;
    output.value2 = &val2;

#ifdef DEBUG
    printf("[BivariateNormal] => %f, %f (sampled both).\n", *output.value1, *output.value2);
#endif

    active = false;
    return 1;
    
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){7, {DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE, DOUBLE}, {"avg1", "avg2", "var1", "var2", "covar", "default1", "default2"}, {0, 0, 0, 0, 0, 0, 0}};
  }

  VGSchema outputSchema() {

    return (VGSchema){2, {DOUBLE, DOUBLE}, {"value1", "value2"}, {1, 1}};
  }

  const char *getName() {
    return "BivariateNormal";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new BivariateNormal());
}

void destroy(VGFunction *vgFunction) {
  delete (BivariateNormal *)vgFunction;
}

