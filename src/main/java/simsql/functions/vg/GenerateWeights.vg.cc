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

struct RecordIn {
    long *input_dim;
    long *output_dim;
    long *input_split;
    long *output_split;
};

struct RecordOut {
    long *row_id;
    long *col_id;
    Matrix *w;
};


class GenerateWeights : public VGFunction {

private:

    bool active;

    long input_dim;
    long output_dim;
    long input_split;
    long output_split;

    long currentRow;
    long currentColumn;

    gsl_rng *rng;

public:

  GenerateWeights() {

    // initialize the random number generator..
    rng = gsl_rng_alloc(gsl_rng_mt19937);

    // initialize the current row and column
    currentRow = 0;
    currentColumn = 0;

    // we don't have any input so it's not active
    active = false;
  }

  ~GenerateWeights() {
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
    // initialize the current row and column
    currentRow = 0;
    currentColumn = 0;

    fprintf(stderr, "cl 1 \n");

    // we don't have any input so it's not active
    active = false;
  }

  void takeParams(RecordIn &input) {
    fprintf(stderr, "in 1 \n");

    // check if we have input parameters...
    if(input.input_dim == NULL || input.output_dim == NULL || input.input_split == NULL || input.output_split == NULL) {
       return;
    }

    fprintf(stderr, "in 2 \n");

    // set the input parameters
    input_dim = *input.input_dim;
    output_dim =  *input.output_dim;
    input_split = *input.input_split;
    output_split = *input.output_split;

    fprintf(stderr, "in 3 \n");

    active = true;
  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (!active) {
        return 0;
    }

    // allocating the matrix
    out.row_id = (long*)malloc(sizeof(long));
    out.col_id = (long*)malloc(sizeof(long));

    (*out.row_id) = currentRow;
    (*out.col_id) = currentColumn;

    out.w = (Matrix*)malloc(sizeof(Matrix));

    // set the dimensions
    out.w->numRow = input_dim/input_split;
    out.w->numCol = output_dim/output_split;
    out.w->value = (double*)malloc(sizeof(double) * (long)out.w->numRow * (long)out.w->numCol);


    // fill the sucker up
    for(int i = 0; i < out.w->numRow ; i++){
        for(int j = 0; j < out.w->numCol; j++) {
            out.w->value[i * (int)out.w->numCol + j] = gsl_rng_uniform(rng);
        }
    }

    // update the rows and the columns..
    currentRow = currentColumn + 1 < input_split ? currentRow : currentRow + 1;
    currentColumn = currentColumn + 1 < input_split ? currentColumn + 1 : 0;

    // done outputting
    active = currentRow < output_split ? true : false;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){4, {"integer", "integer", "integer", "integer"}, {"input_dim", "output_dim", "input_split", "output_split"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){3, {"integer","integer", "matrix[a][b]"}, {"row_id" ,"col_id", "w"}};
  }

  const char *getName() {
    return "GenerateWeights";
  }
};


VGFunction *create() {
  return(new GenerateWeights());
}

void destroy(VGFunction *vgFunction) {
  delete (GenerateWeights *)vgFunction;
}

