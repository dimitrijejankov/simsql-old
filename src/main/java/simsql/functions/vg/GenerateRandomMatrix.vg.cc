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
#include <list>

using namespace std;

struct RecordIn {
    long *row_num;
    long *col_num;
};

struct RecordOut {
    Matrix *w;
};

struct input_node {
    long row_num;
    long col_num;
};

class GenerateRandomMatrix : public VGFunction {

private:

    bool active;

    long row_num;
    long col_num;

    gsl_rng *rng;

    list<input_node> inputs;

public:

  GenerateRandomMatrix() {

    // initialize the random number generator..
    rng = gsl_rng_alloc(gsl_rng_mt19937);

    // we don't have any input so it's not active
    active = false;
  }

  ~GenerateRandomMatrix() {
    // deallocate the rng
    gsl_rng_free(rng);
  }

  void finalizeTrial() {
    active = false;
  }

  void initializeSeed(long seed) {
    // set the seed
    gsl_rng_set(rng, seed);
  }

  void clearParams() {

    // we don't have any input so it's not active
    active = false;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters...
    if(input.row_num == NULL || input.col_num == NULL) {
       return;
    }

    input_node tmp;

    tmp.row_num = *input.row_num;
    tmp.col_num = *input.col_num;

    inputs.push_back(tmp);
  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (inputs.empty()) {
        return 0;
    }

    out.w = (Matrix*)malloc(sizeof(Matrix));

    // set the dimensions
    out.w->numRow = inputs.back().row_num;
    out.w->numCol = inputs.back().col_num;
    out.w->value = (double*)malloc(sizeof(double) * inputs.back().row_num * inputs.back().col_num);

    // fill the sucker up
    for(int i = 0; i < inputs.back().row_num; i++){
        for(int j = 0; j < inputs.back().col_num; j++) {
            out.w->value[i * inputs.back().col_num + j] = gsl_rng_uniform(rng);
        }
    }

    inputs.pop_back();

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"integer", "integer"}, {"row_num", "col_num"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[a][b]"}, {"w"}};
  }

  const char *getName() {
    return "GenerateRandomMatrix";
  }
};


VGFunction *create() {
  return(new GenerateRandomMatrix());
}

void destroy(VGFunction *vgFunction) {
  delete (GenerateRandomMatrix *)vgFunction;
}