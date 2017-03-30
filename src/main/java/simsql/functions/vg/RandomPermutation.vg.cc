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
#include <set>
#include "VGFunction.h"
#include <gsl/gsl_rng.h>
#include <gsl/gsl_randist.h>

using namespace std;

struct RecordIn {
    long *sample_num;
    long *data_size;
};

struct RecordOut {
    long *data_id;
};


class RandomPermutation : public VGFunction {

private:

    bool active;

    long sample_num;
    long data_size;

    long next_data_id;

    set<long> usedIndices;

public:

  RandomPermutation() {

    // initialize the next_data_id
    next_data_id = 0;

    // we don't have any input so it's not active
    active = false;
  }

  ~RandomPermutation() {}

  void finalizeTrial() {
    active = false;
  }

  void initializeSeed(long seed) {
    // set the seed
    srand(seed);
  }

  void clearParams() {

    // initialize the next_data_id
    next_data_id = 0;

    // we don't have any input so it's not active
    active = false;
  }

  void takeParams(RecordIn &input) {

    // check if we have input parameters...
    if(input.sample_num == NULL || input.data_size == NULL) {
       return;
    }

    // set the input parameters
    sample_num = *input.sample_num;
    data_size =  *input.data_size;

    active = true;
  }

  int outputVals(RecordOut &out) {

    // do we still have to go
    if (!active) {
        return 0;
    }

    // current index
    long index;

    // find the index...
    do {
        // set the index...
        index = rand() % data_size;

    } while(usedIndices.find(index) != usedIndices.end());

    // allocating the data_id
    out.data_id = (long*)malloc(sizeof(long));

    // set the index
    (*out.data_id) = index;

    // add the index as used...
    usedIndices.insert(index);

    // increase the next_data_id...
    next_data_id++;

    // done outputting
    active = next_data_id < sample_num;

    return 1;
  }

  VGSchema inputSchema() {
    return (VGSchema){2, {"integer", "integer"}, {"sample_num", "data_size"}};
  }

  VGSchema outputSchema() {
    return (VGSchema){1, {"integer"}, {"data_id"}};
  }

  const char *getName() {
    return "RandomPermutation";
  }
};


VGFunction *create() {
  return(new RandomPermutation());
}

void destroy(VGFunction *vgFunction) {
  delete (RandomPermutation *)vgFunction;
}

