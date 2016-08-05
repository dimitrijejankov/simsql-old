

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
#include <string.h>
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>
#include "VGFunction.h"

#define MAX_CHOICES 512
//#define DEBUG

/** 
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  char *choice;
  double *prob;

};

/**
 * Output record, used by the outputVals().
 * If any of the values is NULL, then the user must allocate space for
 * it (the engine will de-allocate). 
 */
struct RecordOut {
  char *value;

};

// ----------------------------------------------------------- // 

/** VG Function class */
class StringChoice : public VGFunction {

private:
  /** RNG and structures go here. */  
  gsl_rng *rng;

  int num_choices;
  bool active;

  struct po_pair {
    double prob;
    char *val;
  };

  po_pair choices[MAX_CHOICES];

  // comparison function -- for sorting
  static int compare_pairs(const po_pair *a, const po_pair *b) {

    if (a->prob < b->prob)
      return -1;
    else if (a->prob > b->prob)
      return 1;
    else 
      return 0;
  }


  // ----------------------------------------------------------- // 

public:

  /** Constructor. Use this to declare your RNG and other
   * important structures .
   */
  StringChoice() {

#ifdef DEBUG
    printf("[StringChoice]: constructor.\n");
    fflush(stdout);
#endif

    num_choices = 0;
    rng = gsl_rng_alloc(gsl_rng_mt19937);
  }

  /** Destructor. Deallocate everything from the constructor. */
  ~StringChoice() {

#ifdef DEBUG
    printf("[StringChoice]: destructor.\n");
    fflush(stdout);
#endif

    // destroy the RNG.
    gsl_rng_free(rng);	

    // and the strings
    for (int i=0;i<num_choices;i++) {
      free(choices[i].val);
    }
  }

  /** Initializes the RNG seed for every group. */
  void initializeSeed(long seedValue) {

#ifdef DEBUG
    printf("[StringChoice]: initializeSeed(%ld).\n", seedValue);
    fflush(stdout);
#endif

    // set the seed.
    gsl_rng_set(rng, seedValue);
  }

  /** Finalizes the current group and prepares the structure for
   * the next call to outputVals(). */
  void finalizeTrial() {

#ifdef DEBUG
    printf("[StringChoice]: finalizeTrial.\n");
    fflush(stdout);
#endif

    // set active
    active = num_choices > 0;
  }

  /**
   * Clears the set of parameters for the first call to takeParams.
   * If possible, uses the default parameter set.
   */
  void clearParams() {

#ifdef DEBUG
    printf("[StringChoice]: clearParams.\n");
#endif

    //  free the strings
    for (int i=0;i<num_choices;i++) {
      free(choices[i].val);
    }
      
    // and set inactive
    num_choices = 0;
    active = false;
  }

  /** 
   * Passes the parameter values. Might be called several times
   * for each group. 
   */ 
  void takeParams(RecordIn &input) {
   

#ifdef DEBUG
    printf("[StringChoice]: takeParams(");
    if (input.choice == NULL)
      printf("NULL, ");
    if (input.prob == NULL)
      printf("NULL).\n");    
#endif
    
    // copy, if possible.
    if (input.choice != NULL && input.prob != NULL && num_choices <= MAX_CHOICES) {

#ifdef DEBUG
      printf("%s, %f).\n", input.choice, *input.prob);
#endif 

      choices[num_choices] = (po_pair){*input.prob, strdup(input.choice)};
      num_choices++;
    }

    active = num_choices > 0;
  }

  /** 
   * Produces the sample values. Returns 1 if there are more
   * records to be produced for the current sample, 0 otherwise. 
   */
  int outputVals(RecordOut &output) { 

    // not active.
    if (!active)
      return 0;

    // only one choice
    if (num_choices == 1) {
      output.value = choices[0].val;
      active = false;

#ifdef DEBUG
      printf("[StringChoice] => %s [single choice].\n", output.value);
      fflush(stdout);
#endif
      return 1;
    }

    // multiple choices -- sort first.
    qsort(choices, num_choices, sizeof(po_pair), (int (*)(const void*, const void*)) StringChoice::compare_pairs);
    
    // p-value
    double p = gsl_rng_uniform(rng);
    int whichCand = -1;
    double pSoFar = 0.0;

    while (p >= pSoFar && (whichCand + 1) < num_choices) {
      whichCand++;
      pSoFar += choices[whichCand].prob;
    }

    if (whichCand < 0)
      whichCand = 0;

    output.value = choices[whichCand].val;

#ifdef DEBUG
    printf("[StringChoice] => %s [chosen from %i possible values].\n", output.value, num_choices);
    fflush(stdout);
#endif

    active = false;
    return 1;

  }

  // ----------------------------------------------------------- // 

  /** Schema information methods -- DO NOT MODIFY */
  VGSchema inputSchema() {

    return (VGSchema){2, {STRING, DOUBLE}, {"value", "prob"}, {0, 0}};
  }

  VGSchema outputSchema() {

    return (VGSchema){1, {STRING}, {"value"}, {1}};
  }

  const char *getName() {
    return "StringChoice";
  }
};

// ----------------------------------------------------------- // 

/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
  return(new StringChoice());
}

void destroy(VGFunction *vgFunction) {
  delete (StringChoice *)vgFunction;
}

