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
#include <limits>

#include "VGFunction.h"

using namespace std;

#define NUM_OPERATORS 3

struct RecordIn {
    Matrix *w;
    Matrix *e;
    Matrix *o;
};

struct RecordOut {
    Matrix *u;
};

class BackPropagationHiddenLayer : public VGFunction {

private:

    int active;

    // input
    gsl_matrix *w;
    gsl_matrix *e;
    gsl_matrix *o;

public:

  BackPropagationHiddenLayer() {

    // set everything to null
    w = NULL;
    e = NULL;
    o = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  ~BackPropagationHiddenLayer() {}

  void finalizeTrial() {
    active = NUM_OPERATORS;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    // set everything to null
    w = NULL;
    e = NULL;
    o = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  void takeParams(RecordIn &input) {

    // weight matrix is provided store it
    if(input.w != NULL) {
        w = input.w->matrix;
        active--;
    }

    // bias matrix is provided store it
    if(input.e != NULL) {
        e = input.e->matrix;
        active--;
    }

    // store activation from previous layer
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

    // allocate the matrix
    gsl_matrix* product = gsl_matrix_calloc(o->size1, o->size2);

    // multiply x * w
    gsl_blas_dgemm(CblasNoTrans, CblasTrans, 1.0, e, w, 0.0, product);

    // free the matrices
    gsl_matrix_free(e);
    gsl_matrix_free(w);

    // do RELU
    for(int i = 0; i < o->size1; ++i) {
        for(int j = 0; j < o->size2; ++j) {
            product->data[i * o->size2 + j] = o->data[i * o->size2 + j] < 0 ? 0 : product->data[i * o->size2 + j];
        }
    }

    // allocate the output matrix
    out.u = (Matrix*)malloc(sizeof(Matrix));

    // give the data to the output matrix
    out.u->numRow = product->size1;
    out.u->numCol = product->size2;
    out.u->ifRow = true;

    // the data in the product is not his anymore
    product->owner = false;

    // give the data to the matrix
    out.u->value = product->data;

    // free the product
    gsl_matrix_free(product);
    gsl_matrix_free(o);

    // set the number of required operators to 3
    active = NUM_OPERATORS;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){3, {"matrix[c][b]", "matrix[a][b]", "matrix[a][c]"}, {"w", "e", "o"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[a][c]"}, {"u"}};
  }

  const char *getName() {
    return "BackPropagationHiddenLayer";
  }
};


VGFunction *create() {
  return(new BackPropagationHiddenLayer());
}

void destroy(VGFunction *vgFunction) {
  delete (BackPropagationHiddenLayer *)vgFunction;
}