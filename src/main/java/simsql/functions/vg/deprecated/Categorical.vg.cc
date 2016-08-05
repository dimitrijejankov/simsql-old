

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
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include "VGFunction.h"

// #define DEBUG
#define INITIAL_SIZE 128

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  long *num_cat;
  long *category;
  double *freq;

};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  long *value;
  
};

// ----------------------------------------------------------- // 

/** VG Function class */
class Categorical : public VGFunction {

private:

  /** RNG and structures go here. */
  gsl_rng *rng;
  bool active;
  bool normalized;

  long num_cat;
  long numCategories;
  long currentSize;

  struct category {
    long id;
    double freq;
  };


  category *options;

  // ----------------------------------------------------------- // 

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures .
   */
  Categorical() {

#ifdef DEBUG
    printf("[Categorical]: constructor.\n");
    fflush(stdout);
#endif 

    // create the RNG
    rng = gsl_rng_alloc(gsl_rng_mt19937);

    // create the array
    currentSize = INITIAL_SIZE;
    options = (category *)malloc(currentSize * sizeof(category));
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~Categorical() {

#ifdef DEBUG
    printf("[Categorical]: destructor.\n");
    fflush(stdout);
#endif 	

    // destroy the RNG
    gsl_rng_free(rng);

    // destroy the array.
    free(options);
  }

  /** Initializes the RNG seed for a given call. */
  void initializeSeed(long seedValue) {

#ifdef DEBUG
    printf("[Categorical]: initializeSeed(%ld).\n", seedValue);
    fflush(stdout);
#endif 

    // set the seed.
    gsl_rng_set(rng, seedValue);
  }

  /** Finalizes the current trial and prepares the structures for
   * another fresh call to outputVals(). */
  void finalizeTrial() {

#ifdef DEBUG
    printf("[Categorical]: finalizeTrial.\n");
    fflush(stdout);
#endif 

    // set active
    active = (num_cat == numCategories) && (numCategories > 0);
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

#ifdef DEBUG
    printf("[Categorical]: clearParams.\n");
    fflush(stdout);
#endif 

    // reset the categories
    numCategories = 0;
    num_cat = 0;
    active = false;
    normalized = false;
  }

	
  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {

#ifdef DEBUG
    printf("[Categorical]: takeParams(");

    if (input.num_cat == NULL)
      printf("NULL, ");
    else
      printf("%ld, ", *input.num_cat);

    if (input.category == NULL)
      printf("NULL, ");
    else
      printf("%ld, ", *input.category);

    if (input.freq == NULL)
      printf("NULL).\n");
    else
      printf("%f).\n", *input.freq);

    fflush(stdout);
#endif 

    // number of categories?
    if (input.num_cat != NULL) {
      num_cat = *input.num_cat;

      // do we have enough space?
      if (num_cat > currentSize) {
	currentSize = num_cat;
	options = (category *)realloc(options, sizeof(category) * currentSize);
      }
    }

    // actual *valid* categories?
    if (input.category != NULL && input.freq != NULL) {

      // did we exceed?
      if (numCategories + 1 >= currentSize) {
	currentSize = (currentSize + (currentSize >> 1));
	options = (category *)realloc(options, sizeof(category) * currentSize);
      }

      // place it.
      options[numCategories++] = (category){*input.category, *input.freq};
    }

    // set active
    active = (num_cat == numCategories) && (numCategories > 0);
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 

#ifdef DEBUG
    printf("[Categorical]: outputVals(%ld) => ", numCategories);
#endif

    // active?
    if (!active) {

#ifdef DEBUG
      printf("NULL.\n");
#endif
      return 0;
    }

    // is it a correct CDF?
    if (!normalized) {

      // first, add up
      double sum = 0;
      for (int i=0;i<numCategories;i++) {
	double val = options[i].freq;
	options[i].freq += sum;
	sum += val;
      }

      // then, normalize to 1.
      for (int i=0;i<numCategories;i++) {
	options[i].freq /= sum;
      }
      
      // and set the flag for subsequent iterations.
      normalized = true;
    }

    // get a probability
    double p = gsl_rng_uniform(rng);

    // loop over...
    long choice = 0;
    while (choice < numCategories && options[choice].freq < p)
      choice++;

    // and set the output.
    output.value = &(options[choice].id);

#ifdef DEBUG
    printf("%ld\n", *output.value);
    fflush(stdout);
#endif

    // and the flags.
    active = false;
    return 1;
  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){3, {INT, INT, DOUBLE}, {"num_cat", "freq", "p"}, {0,0,0}};
  }

  VGSchema outputSchema() {

    return (VGSchema){1, {INT}, {"value"}, {1}};
  }

  const char *getName() {
    return "Categorical";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new Categorical());
}

void destroy(VGFunction *vgFunction) {
  delete (Categorical *)vgFunction;
}

