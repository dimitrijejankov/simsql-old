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
    long *dataID;
    long *data;
};

struct RecordOut {
    Vector *x;
};

class LabelGeneration : public VGFunction {

private:

    bool finished;
    long data;

    map<long, long> inputs;

public:

  LabelGeneration() {
    // we don't have any input so it's not active
    finished = false;
  }

  ~LabelGeneration() {}

  void finalizeTrial() {
    finished = false;
  }

  void initializeSeed(long seed) {}

  void clearParams() {
    finished = false;
    inputs.clear();
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters...
    if(input.data == NULL || input.dataID == NULL) {
       return;
    }

    // copy the input
    inputs.insert(std::make_pair(*input.dataID, *input.data));
  }

  int outputVals(RecordOut &out) {

    if(finished) {
        return 0;
    }

    out.x = (Vector*)malloc(sizeof(Vector));

    out.x->length = inputs.size();
    out.x->value = (double*) calloc (inputs.size(), sizeof(double));

    int i = 0;

    for (auto it = inputs.begin(); it != inputs.end(); ++it) {
        out.x->value[i] = it->second;
        i++;
    }

    finished = true;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"integer", "integer"}, {"dataID", "data"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"vector[a]"}, {"x"}};
  }

  const char *getName() {
    return "LabelGeneration";
  }
};


VGFunction *create() {
  return(new LabelGeneration());
}

void destroy(VGFunction *vgFunction) {
  delete (LabelGeneration *)vgFunction;
}