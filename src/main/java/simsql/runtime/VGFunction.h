

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


#ifndef _VGFUNCTIONLIB_H
#define _VGFUNCTIONLIB_H

#include <gsl/gsl_vector.h>
#include <gsl/gsl_matrix.h>
#include <math.h>

// This must mirror the AttributeType enum from the Java classes.
enum VGType {
  NULLS,
  SEED,
  INT,
  DOUBLE,
  STRING,
  SCALAR,
  VECTOR,
  MATRIX
};

// Encapsulates the schema
struct VGSchema {
  int numAtts;
  const char *attTypes[16384];
  const char *attNames[16384];
};

// The definition of a Vector type.
struct Vector {
  double length;
  double *value;
};

// The definition of a Matrix type.
struct Matrix {
  double numRow;
  double numCol;
  double *value;
};

class RecordIn;
class RecordOut;

// The general abstract definition of a VG Function.
class VGFunction {
 public:

  // public functions
  virtual void initializeSeed(long seed) = 0;
  virtual void takeParams(RecordIn &in_rec) = 0;
  virtual int outputVals(RecordOut &out_rec) = 0;
  virtual void finalizeTrial() = 0;
  virtual void clearParams() = 0;
  virtual VGSchema inputSchema() = 0;
  virtual VGSchema outputSchema() = 0;
  virtual const char *getName() = 0;
};

gsl_vector* getVector(Vector* input) {
  gsl_vector* output = gsl_vector_calloc((size_t)input->length);
  for (size_t i = 0; i < output->size; i++) {
    if (!isnan(input->value[i]) && isfinite(input->value[i])) {
      gsl_vector_set(output, i, input->value[i]);
    }
  }
  return output;
}

gsl_matrix* getMatrix(Matrix* input) {
  gsl_matrix* output = gsl_matrix_calloc((size_t)input->numRow, (size_t)input->numCol);
  for (size_t i = 0; i < output->size1; i++) {
    for (size_t j = 0; j < output->size2; j++) {
      if (!isnan(input->value[i * output->size2 + j]) && isfinite(input->value[i * output->size2 + j])) {
        gsl_matrix_set(output, i, j, input->value[i * output->size2 + j]);
      }
    }
  }
  return output;
}

void setVector(gsl_vector* input, Vector** output) {
  *output = (Vector *)malloc(sizeof(Vector));
  (*output)->length = (double)input->size;
  (*output)->value = (double *)malloc(sizeof(double) * input->size);
  for (size_t i = 0; i < input->size; i++) {
    (*output)->value[i] = gsl_vector_get(input, i);
  }
}

void setMatrix(gsl_matrix* input, Matrix** output) {
  *output = (Matrix *)malloc(sizeof(Matrix));
  (*output)->numRow = (double)input->size1;
  (*output)->numCol = (double)input->size2;
  (*output)->value = (double *)malloc(sizeof(double) * input->size1 * input->size2);
  for (size_t i = 0; i < input->size1; i++) {
    for (size_t j = 0; j < input->size2; j++) {
      (*output)->value[i * input->size2 + j] = gsl_matrix_get(input, i, j);
    }
  }
}

// These methods are generated for each VGFunction type. 
extern "C" {
  VGFunction *create();
  void destroy(VGFunction *vg);
};

#endif // _VGFUNCTIONLIB_H
