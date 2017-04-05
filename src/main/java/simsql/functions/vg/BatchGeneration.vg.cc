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
    Matrix *data;
};

struct RecordOut {
    Matrix *x;
};

class BatchGeneration : public VGFunction {

private:

    bool finished;

    long num_col;
    long num_row;
    long data;

    vector<gsl_matrix*> inputs;

public:

  BatchGeneration() {
    // we don't have any input so it's not active
    finished = false;
  }

  ~BatchGeneration() {}

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters...
    if(input.data == NULL) {
       return;
    }

    // copy the input
    inputs.push_back(getMatrix(input.data));
  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    out.x = (Matrix*)malloc(sizeof(Matrix));

    out.x->numRow = inputs.size();
    out.x->numCol = inputs.front()->size2;
    out.x->value = (double*) calloc (inputs.size() * inputs.front()->size2, sizeof(double));

    for(int i = 0; i < inputs.size(); i++) {
        memcpy(&out.x->value[inputs.front()->size2 * i], inputs[i]->data, sizeof(double) * inputs.front()->size2);
    }

    finished = true;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){1, {"matrix[][]"}, {"data"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[][]"}, {"x"}};
  }

  const char *getName() {
    return "BatchGeneration";
  }
};


VGFunction *create() {
  return(new BatchGeneration());
}

void destroy(VGFunction *vgFunction) {
  delete (BatchGeneration *)vgFunction;
}