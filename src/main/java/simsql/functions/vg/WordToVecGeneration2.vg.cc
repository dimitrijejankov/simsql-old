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
    long *id;
    long *input;
    long *output;
};

class WordToVecGeneration2 : public VGFunction {

private:

    bool finished;
    gsl_vector* data;
    long window;
    long wordID;
    long tempID;
    long tempInput;
    long tempOutput;

public:

  WordToVecGeneration2() {
    // we don't have any input so it's not active
    finished = false;
    data = NULL;
    tempID = 0;
  }

  ~WordToVecGeneration2() {
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
    wordID = 0;
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

    tempInput = gsl_vector_get(data, wordID / 2 + 1);
    tempOutput = (wordID % 2 == 0) ? gsl_vector_get(data, wordID / 2) : gsl_vector_get(data, wordID / 2 + 2);

    out.id = &tempID;
    out.input = &tempInput;
    out.output = &tempOutput;

    tempID++;
    wordID++;
    finished = (wordID >= batchSize);

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"vector[b]", "integer"}, {"doc", "window"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){3, {"integer", "integer", "integer"}, {"id", "input", "output"}};
  }

  const char *getName() {
    return "WordToVecGeneration2";
  }
};


VGFunction *create() {
  return(new WordToVecGeneration2());
}

void destroy(VGFunction *vgFunction) {
  delete (WordToVecGeneration2 *)vgFunction;
}