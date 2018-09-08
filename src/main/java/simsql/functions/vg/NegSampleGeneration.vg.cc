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
#include <set>
#include <cstring>
#include <math.h>
#include <stdlib.h>
#include <time.h>

using namespace std;

struct RecordIn {
    long *vocab_num;
    long *neg_num;
};

struct RecordOut {
    long *nid;
};

class NegSampleGeneration : public VGFunction {

private:

    bool finished;
    long vocab_num;
    long neg_num;
    double *prob;
    long tempID;
    long count;
    set<long> samples;

public:

  NegSampleGeneration() {
    // we don't have any input so it's not active
    finished = false;
    count = 0;
    prob = NULL;
  }

  ~NegSampleGeneration() {
    if (prob != NULL)
      free(prob);
    prob = NULL;
  }

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
    if (prob != NULL)
      free(prob);
    prob = NULL;
    count = 0;
    samples.clear();
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters and copy the input
    if(input.vocab_num != NULL) {
      vocab_num = *input.vocab_num;
      prob = (double*) malloc(vocab_num * sizeof(double));
      for(int i = 0; i < vocab_num; i++) {
        prob[i] = (log(i + 2) - log(i + 1)) / log(vocab_num + 1);
        if (i > 0) {
            prob[i] += prob[i - 1];
        }
      }
    }

    if(input.neg_num != NULL) {
      neg_num = *input.neg_num;
    }

  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    do {
      srand (time(NULL));
      double u = ((double)rand() / RAND_MAX);
      for (int i = 0; i < vocab_num; i++) {
        if (u <= prob[i]) {
          tempID = i;
          break;
        }
      }
    } while(samples.find(tempID) != samples.end()); 

    out.nid = &tempID;
    samples.insert(tempID);
    count++;
    finished = (count >= neg_num);

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"integer", "integer"}, {"vocab_num", "neg_num"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"integer"}, {"nid"}};
  }

  const char *getName() {
    return "NegSampleGeneration";
  }
};


VGFunction *create() {
  return(new NegSampleGeneration());
}

void destroy(VGFunction *vgFunction) {
  delete (NegSampleGeneration *)vgFunction;
}