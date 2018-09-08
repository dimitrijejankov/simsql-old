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
    Matrix *mat;
    Matrix *delta1;
    Matrix *delta2;
};

struct RecordOut {
    Matrix *w;
};

class UpdateWeightMatrix : public VGFunction {

private:

    bool finished;
    gsl_matrix* mat;
    gsl_matrix* delta1;
    gsl_matrix* delta2;

public:

  UpdateWeightMatrix() {
    // we don't have any input so it's not active
    finished = false;
    mat = NULL;
    delta1 = NULL;
    delta2 = NULL;
  }

  ~UpdateWeightMatrix() {
    if(mat != NULL)
      gsl_matrix_free(mat);
    mat = NULL;
    if(delta1 != NULL)
      gsl_matrix_free(delta1);
    delta1 = NULL;
    if(delta2 != NULL)
      gsl_matrix_free(delta2);
    delta2 = NULL;
  }

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
    if(mat != NULL)
      gsl_matrix_free(mat);
    mat = NULL;
    if(delta1 != NULL)
      gsl_matrix_free(delta1);
    delta1 = NULL;
    if(delta2 != NULL)
      gsl_matrix_free(delta2);
    delta2 = NULL;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters and copy the input
    if(input.mat != NULL) {
      mat = getMatrix(input.mat);
    }

    if(input.delta1 != NULL) {
      delta1 = getMatrix(input.delta1);
    }

    if(input.delta2 != NULL) {
      delta2 = getMatrix(input.delta2);
    }

  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    out.w = (Matrix*)malloc(sizeof(Matrix));

    // set the dimensions
    out.w->numRow = mat->size1;
    out.w->numCol = mat->size2;
    out.w->value = (double*)malloc(sizeof(double) * mat->size1 * mat->size2);

    // fill the sucker up
    for(int i = 0; i < mat->size1; i++){
        for(int j = 0; j < mat->size2; j++) {
            out.w->value[i * mat->size2 + j] = gsl_matrix_get(mat, i, j);
            if (delta1 != NULL) out.w->value[i * mat->size2 + j] -= gsl_matrix_get(delta1, i, j);
            if (delta2 != NULL) out.w->value[i * mat->size2 + j] -= gsl_matrix_get(delta2, i, j);
        }
    }

    finished = true;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){3, {"matrix[a][b]", "matrix[a][b]", "matrix[a][b]"}, {"mat", "delta1", "delta2"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"matrix[a][b]"}, {"w"}};
  }

  const char *getName() {
    return "UpdateWeightMatrix";
  }
};


VGFunction *create() {
  return(new UpdateWeightMatrix());
}

void destroy(VGFunction *vgFunction) {
  delete (UpdateWeightMatrix *)vgFunction;
}