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
    Matrix *b;
    Matrix *x;
};

struct RecordOut {
    Matrix *o;
};

class ForwardPropagationOutputLayer : public VGFunction {

private:

    int active;

    // input
    gsl_matrix *w;
    gsl_matrix *b;
    gsl_matrix *x;

public:

  ForwardPropagationOutputLayer() {

    // set everything to null
    w = NULL;
    b = NULL;
    x = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  ~ForwardPropagationOutputLayer() {}

  void finalizeTrial() {
    active = NUM_OPERATORS;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    // set everything to null
    w = NULL;
    b = NULL;
    x = NULL;

    // we don't have any input so it's not active
    active = NUM_OPERATORS;
  }

  void takeParams(RecordIn &input) {

    // weight matrix is provided store it
    if(input.w != NULL) {
        w = getMatrix(input.w);
        printf("a\n");
        active--;
    }

    // bias matrix is provided store it
    if(input.b != NULL) {
        b = getMatrix(input.b);
        printf("b\n");
        active--;
    }

    //
    if(input.x != NULL) {
        x = getMatrix(input.x);
        printf("c\n");
        active--;
    }

  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (active != 0) {
        return 0;
    }

    // allocate the matrix
    gsl_matrix* product = gsl_matrix_calloc(x->size1, w->size2);

    // multiply x * w
    gsl_blas_dgemm(CblasNoTrans, CblasNoTrans, 1.0, x, w, 0.0, product);

    // free stuff so we have memory
    gsl_matrix_free(w);
    gsl_matrix_free(x);

    // prepare the output
    out.o = (Matrix*)malloc(sizeof(Matrix));

    // set the dimensions
    out.o->numRow = product->size1;
    out.o->numCol = product->size2;
    out.o->value = (double*)malloc(sizeof(double) * product->size1 * product->size2);

    for (int i = 0; i < out.o->numRow; i++) {

        double max_value = std::numeric_limits<int>::min();

        for (int j = 0; j < out.o->numCol; j++) {

            // value at [i][j] of the output matrix
            double tmp = gsl_matrix_get(product, i, j) + gsl_matrix_get(b, 0, j);

            // set the value to the output
            out.o->value[i * product->size2 + j] = tmp;

            // if this value at [i][j] is the new max set it...
            max_value = max_value < tmp ? tmp : max_value;
        }

        double sum = 0.0;

        // exponents of the thing
        for (int j = 0; j < out.o->numCol; j++) {
            sum += exp(out.o->value[i * product->size2 + j] - max_value);
        }

        sum = log(sum) + max_value;

        for (int j = 0; j < out.o->numCol; j++) {
            out.o->value[i * product->size2 + j] = exp(out.o->value[i * product->size2 + j] - sum);
        }
    }


    // free the product
    gsl_matrix_free(product);
    gsl_matrix_free(b);

    // set the number of required operators to 3
    active = NUM_OPERATORS;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){3, {"matrix[b][c]", "matrix[1][c]", "matrix[a][b]"}, {"w", "b", "x"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[a][c]"}, {"o"}};
  }

  const char *getName() {
    return "ForwardPropagationOutputLayer";
  }
};


VGFunction *create() {
  return(new ForwardPropagationOutputLayer());
}

void destroy(VGFunction *vgFunction) {
  delete (ForwardPropagationOutputLayer *)vgFunction;
}