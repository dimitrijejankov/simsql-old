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
#include <map>
#include <cstring>

using namespace std;

struct RecordIn {
    Vector *doc;
    long *window;
};

struct RecordOut {
    Vector *input;
    Vector *output;
};

class WordToVecGeneration : public VGFunction {

private:

    bool finished;
    gsl_vector* data;
    long window;

public:

  WordToVecGeneration() {
    // we don't have any input so it's not active
    finished = false;
    data = NULL;
  }

  ~WordToVecGeneration() {
    if(data != NULL)
      gsl_vector_free(data);
    data = NULL;
  }

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
    if(data != NULL)
      gsl_vector_free(data);
    data = NULL;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters and copy the input
    if(input.doc != NULL) {
      data = getVector(input.doc);
    }

    if(input.window != NULL) {
      window = *input.window;
    }

  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    int batchSize = (2 * (data->size - 2) > window) ? window : 2 * (data->size - 2);

    out.input = (Vector*)malloc(sizeof(Vector));
    out.input->length = batchSize;
    out.input->value = (double*) calloc (batchSize, sizeof(double));
    out.output = (Vector*)malloc(sizeof(Vector));
    out.output->length = batchSize;
    out.output->value = (double*) calloc (batchSize, sizeof(double));

    for (int i = 0; i < batchSize / 2; i++) {
        out.input->value[2 * i] = gsl_vector_get(data, i + 1);
        out.input->value[2 * i + 1] = gsl_vector_get(data, i + 1);
        out.output->value[2 * i] = gsl_vector_get(data, i);
        out.output->value[2 * i + 1] = gsl_vector_get(data, i + 2);
    }

    finished = true;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"vector[b]", "integer"}, {"doc", "window"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){2, {"vector[a]", "vector[a]"}, {"input", "output"}};
  }

  const char *getName() {
    return "WordToVecGeneration";
  }
};


VGFunction *create() {
  return(new WordToVecGeneration());
}

void destroy(VGFunction *vgFunction) {
  delete (WordToVecGeneration *)vgFunction;
}