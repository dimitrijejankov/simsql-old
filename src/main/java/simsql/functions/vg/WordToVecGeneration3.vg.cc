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
#include <vector>
#include <cstring>

using namespace std;

struct RecordIn {
    Vector *doc;
};

struct RecordOut {
    long *id;
    long *input;
    long *output;
};

class WordToVecGeneration3 : public VGFunction {

private:

    bool finished;
    vector<gsl_vector*> inputs;
    long docID;
    long wordID;
    long tempID;
    long tempInput;
    long tempOutput;

public:

  WordToVecGeneration3() {
    // we don't have any input so it's not active
    finished = false;
    docID = 0;
    wordID = 0;
    tempID = 0;
  }

  ~WordToVecGeneration3() {
    for(int i = 0; i < inputs.size(); i++) {
        if(inputs[i] != NULL)
            gsl_vector_free(inputs[i]);
        inputs[i] = NULL;
    }
  }

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
    for(int i = 0; i < inputs.size(); i++) {
        if(inputs[i] != NULL)
            gsl_vector_free(inputs[i]);
        inputs[i] = NULL;
    }
    inputs.clear();
    docID = 0;
    wordID = 0;
    tempID = 0;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters...
    if(input.doc == NULL) {
       return;
    }

    // copy the input
    inputs.push_back(getVector(input.doc));

  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    int batchSize = 2 * (inputs[docID]->size - 2);

    tempInput = gsl_vector_get(inputs[docID], wordID / 2 + 1);
    tempOutput = (wordID % 2 == 0) ? gsl_vector_get(inputs[docID], wordID / 2) : gsl_vector_get(inputs[docID], wordID / 2 + 2);

    out.id = &tempID;
    out.input = &tempInput;
    out.output = &tempOutput;

    tempID++;
    wordID++;
    if (wordID >= batchSize) {
      docID++;
      wordID = 0;
    }
    finished = (docID >= inputs.size());

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){1, {"vector[b]"}, {"doc"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){3, {"integer", "integer", "integer"}, {"id", "input", "output"}};
  }

  const char *getName() {
    return "WordToVecGeneration3";
  }
};


VGFunction *create() {
  return(new WordToVecGeneration3());
}

void destroy(VGFunction *vgFunction) {
  delete (WordToVecGeneration3 *)vgFunction;
}