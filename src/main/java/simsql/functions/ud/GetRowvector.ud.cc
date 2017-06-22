#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <gsl/gsl_matrix.h>
#include <gsl/gsl_linalg.h>
#include <gsl/gsl_errno.h>
#include <gsl/gsl_blas.h>

#include "UDFunction.h"

/**
 * Input record, used by the takeParams() method.
 * Values could be NULL, meaning they were not present in the tuple.
 */
struct RecordIn {
  Matrix *m; // the input matrix
  long *rowID;
};

// ----------------------------------------------------------- //

/** A pseudo-VG function for inverting a matrix. */
class GetRowvector : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    GetRowvector() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~GetRowvector() {}

    gsl_vector* executeVector(RecordIn* in) {

        // allocate a vector to output
        gsl_vector *output = allocateOutputVector(in->m->matrix->size2);

        for (size_t i = 0; i < in->m->matrix->size2; i++) {
            gsl_vector_set(output, i, gsl_matrix_get(in->m->matrix, (size_t) *(in->rowID), i));
        }

        // return the result
        return output;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("get_rowvector");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("vector[b]");
    }

    /**
     * Returns the input types as strings
     */
    std::vector<std::string> getInputTypes() {

        std::vector<std::string> ret;

        ret.push_back("matrix[a][b]");
        ret.push_back("integer");

        return ret;
    }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
UDFunction *create() {
  return(new GetRowvector());
}

void destroy(UDFunction *udFunction) {
  delete (GetRowvector *)udFunction;
}