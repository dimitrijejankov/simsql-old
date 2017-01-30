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

#define LEARNING_RATE 1

struct RecordIn {
    long *id;
    double *a;
    double *b;
    double *r;

    double *w_a;
    double *w_b;
    double *theta;
};

struct RecordOut {
    long *id;

    double *w_a;
    double *w_b;
    double *theta;
};

float sigmoid(float x) {
     float exp_value;
     float return_value;
     exp_value = exp((double) -x);

     return 1 / (1 + exp_value);
}

class TrainPerceptron : public VGFunction {

private:

    bool active;
    bool initialized;

    long id;
    double w_a;
    double w_b;
    double theta;

public:

  TrainPerceptron() {

    id = 0;
    w_a = NAN;
    w_b = NAN;
    theta = NAN;

    active = false;
    initialized = false;
  }

  ~TrainPerceptron() {
  }

  void finalizeTrial() {
    initialized = false;
    active = false;
  }

  void initializeSeed(long seed) {
  }

  void clearParams() {

    w_a = NAN;
    w_b = NAN;
    theta = NAN;
    active = false;
  }

  void takeParams(RecordIn &input) {

    if(input.a == NULL || input.b == NULL || input.r == NULL || input.id == NULL || input.w_a == NULL || input.w_b == NULL || input.theta == NULL) {
       return;
    }

    if (!initialized) {
        id = *input.id;
        w_a = *input.w_a;
        w_b = *input.w_b;
        theta = *input.theta;
        initialized = true;

        printf("Init %ld \n", id);
        fflush(stdout);
    }

    double a = *input.a;
    double b = *input.b;
    double r = *input.r;

    double rr = sigmoid(w_a * a + w_b * b + theta);

    w_a = w_a + LEARNING_RATE * a * (r - rr);
    w_b = w_b + LEARNING_RATE * b * (r - rr);
    theta = theta + LEARNING_RATE * (r - rr);

    active = true;
  }

  int outputVals(RecordOut &output) {

    if (!active) {
        return 0;
    }

    printf("Init id = %ld, w_a = %f \n", id, w_a);
    fflush(stdout);

    output.id = &id;
    output.w_a = &w_a;
    output.w_b = &w_b;
    output.theta = &theta;

    printf("Init id = %ld, w_a = %f \n", *output.id, *output.w_a);
    fflush(stdout);

    active = false;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){7, {"integer", "double", "double", "double", "double", "double", "double"},
                          {"id", "a", "b", "r", "w_a", "w_b", "theta"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){4, {"integer","double", "double", "double"}, {"id" ,"w_a", "w_b", "theta"}};
  }

  const char *getName() {
    return "TrainPerceptron";
  }
};


VGFunction *create() {
  return(new TrainPerceptron());
}

void destroy(VGFunction *vgFunction) {
  delete (TrainPerceptron *)vgFunction;
}

