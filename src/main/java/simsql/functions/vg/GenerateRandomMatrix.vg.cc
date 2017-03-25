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
    long *col_num;
    long *row_num;
    long *col_id;
    long *row_id;
};

struct RecordOut {
    long *row_id;
    long *col_id;
    Matrix *w;
};

struct input_node {
    long col_num;
    long row_num;
    long col_id;
    long row_id;
};

class GenerateRandomMatrix : public VGFunction {

private:

    bool active;

    long col_num;
    long row_num;
    long col_id;
    long row_id;

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
    if(input.col_num == NULL || input.row_num == NULL || input.col_id == NULL || input.row_id == NULL) {
       return;
    }

    input_node tmp;

    tmp.col_num = *input.col_num;
    tmp.row_num =  *input.row_num;
    tmp.col_id = *input.col_id;
    tmp.row_id = *input.row_id;

    inputs.push_back(tmp);
  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (inputs.empty()) {
        return 0;
    }

    // allocating the output
    out.row_id = (long*)malloc(sizeof(long));
    out.col_id = (long*)malloc(sizeof(long));

    (*out.row_id) = inputs.back().row_id;
    (*out.col_id) = inputs.back().col_id;

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
    return (VGSchema){4, {"integer", "integer", "integer", "integer"}, {"col_num", "row_num", "col_id", "row_id"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){3, {"integer","integer", "matrix[a][b]"}, {"row_id" ,"col_id", "w"}};
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

