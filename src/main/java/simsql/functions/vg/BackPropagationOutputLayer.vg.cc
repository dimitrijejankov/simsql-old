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
#include <stdlib.h>
#include <string.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_errno.h>
#include <gsl/gsl_blas.h>
#include <math.h>

#include "VGFunction.h"

using namespace std;

#define NUM_OPERATORS 2

struct RecordIn {
    Vector *y;
    Matrix *o;
};

struct RecordOut {
    Matrix *e;
};

class BackPropagationOutputLayer : public VGFunction {

private:

    int active;

    // input
    gsl_vector *y;
    gsl_matrix *o;

public:

  BackPropagationOutputLayer() {

    // set everything to null
    y = NULL;
    o = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  ~BackPropagationOutputLayer() {}

  void finalizeTrial() {
    active = NUM_OPERATORS;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    // set everything to null
    y = NULL;
    o = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  void takeParams(RecordIn &input) {

    // weight matrix is provided store it
    if(input.y != NULL) {
        y = getVector(input.y);
        active--;
    }

    // bias matrix is provided store it
    if(input.o != NULL) {
        o = input.o->matrix;
        active--;
    }

  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (active != 0) {
        return 0;
    }

    // allocate the memory for the matrix
    out.e = (Matrix*)malloc(sizeof(Matrix));

    // set the dimensions
    out.e->numRow = o->size1;
    out.e->numCol = o->size2;
    out.e->ifRow = true;
    out.e->value = (double*)malloc(sizeof(double) * o->size1 * o->size2);

    // do the calculation
    for(int i = 0; i < o->size1; i++) {

        // figure out the category id
        long category = (long)gsl_vector_get(y, i);

        for(int j = 0; j < o->size2; j++) {

            if(j == category) {
                out.e->value[i * o->size2 + j] = gsl_matrix_get(o, i, j) - 1;
            }
            else {
                out.e->value[i * o->size2 + j] = gsl_matrix_get(o, i, j);
            }
        }
    }

    // free the input
    gsl_vector_free(y);
    gsl_matrix_free(o);

    // set the number of required operators to 3
    active = NUM_OPERATORS;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"vector[a]", "matrix[1][c]"}, {"y", "o"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[e][f]"}, {"e"}};
  }

  const char *getName() {
    return "BackPropagationOutputLayer";
  }
};


VGFunction *create() {
  return(new BackPropagationOutputLayer());
}

void destroy(VGFunction *vgFunction) {
  delete (BackPropagationOutputLayer *)vgFunction;
}