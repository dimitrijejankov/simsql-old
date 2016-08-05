/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this of except in compliance with the License.         *
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

struct RecordIn {
    Vector *x;
    long *row_id;
    Matrix *w;
    long *data_id;
};

struct RecordOut {
    Vector *chunk;
    long *data_id;
    long *column_id;
};

/** VG Function class */
class WeightedInputChunk : public VGFunction {
public:
/** Constructor. Use this to declare your RNG and other
  * important structures .
  */
    WeightedInputChunk() {}

/** Destructor. Deallocate everything from the constructor. */
    ~WeightedInputChunk() {}

/** Initializes the RNG seed for a given call. */
    void initializeSeed(long seedValue) {}

/**
  * Clears the set of parameters for the first call to takeParams.
  * If possible, uses the default parameter set.
  */

    void clearParams() {
        printf("---DATA START---\n");
        fflush(stdout);
    }

/** Finalizes the current trial and prepares the structures for
  * another fresh call to outputVals(). */
    void finalizeTrial() {
        printf("---DATA END---\n");
        fflush(stdout);
    }

    void printVector(Vector* v){

        if(v->value == NULL)
            return;

        printf("[");
        for(int i = 0; i < (size_t)v->length; i++){
            if (i != ((size_t)v->length) - 1) {
                printf("%lf ", v->value[i]);
            }
            else {
                printf("%lf", v->value[i]);
            }
        }
        printf("] ");
    }


    void printMatrix(Matrix* m){

        if(m->value == NULL)
            return;

        printf("[");
        for(int i = 0; i < (size_t)m->numRow; i++){
            printf("[");
            for(int j = 0; j < (size_t)m->numCol; j++) {
                if (j != ((size_t)m->numCol) - 1) {
                    printf("%lf ", m->value[i * (size_t)m->numCol + j]);
                }
                else {
                    printf("%lf", m->value[i * (size_t)m->numCol + j]);
                }
            }
            printf("]");
        }
        printf("] ");
    }

/**
  * Passes the parameter values. Might be called several times
  * for each group.
  */
    void takeParams(RecordIn &input) {

        if (input.x != NULL) {
            printVector(input.x);
        }
        else {
            printf("null ");
        }

        if (input.row_id != NULL) {
            printf("%ld ", *input.row_id);
        }
        else
            printf("null ");

        if (input.w != NULL) {
            printMatrix(input.w);
        }
        else
            printf("null ");

        if (input.data_id != NULL) {
            printf("%ld ", *input.data_id);
        }
        else
            printf("null ");

        printf("\n");
    }

/**
  * Produces the sample values. Returns 1 if there are more
  * records to be produced for the current sample, 0 otherwise.
  */
    int outputVals(RecordOut &output) {
        return 0;
    }

/** Schema information methods -- DO NOT MODIFY */
    VGSchema inputSchema() {
        return (VGSchema){4, {"vector[a]", "integer", "matrix[a][a]", "integer"}, {"x", "row_id", "w", "data_id"}};
    }

    VGSchema outputSchema() {
        return (VGSchema){3, {"vector[a]", "integer", "integer"}, {"chunk", "data_id", "column_id"}};
    }

    const char *getName() {
        return "WeightedInputChunk";
    }

};
/** External creation/destruction methods -- DO NOT MODIFY */
VGFunction *create() {
    return(new WeightedInputChunk());
}

void destroy(VGFunction *vgFunction) {
    delete (WeightedInputChunk *)vgFunction;
}