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
  long *columnID;
  Vector *category;
};

// ----------------------------------------------------------- //

/** A pseudo-VG function for inverting a matrix. */
class CrossEntropy : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    CrossEntropy() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~CrossEntropy() {}

    double executeDouble(RecordIn* in) {

        // allocate the output
        double sum = 0.0;

        for (size_t i = 0; i < in->m->matrix->size1; i++) {
            for (size_t j = 0; j < in->m->matrix->size2; j++) {
                if ((size_t) *(in->columnID) == (((size_t)in->category->data[i]) / in->m->matrix->size2)
                    && j == ((size_t)in->category->data[i]) % in->m->matrix->size2) {
                    sum += gsl_matrix_get(in->m->matrix, i, j);
                }
            }
        }

        // return the result
        return -sum / in->m->matrix->size1;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("cross_entropy");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("double");
    }

    /**
     * Returns the input types as strings
     */
    std::vector<std::string> getInputTypes() {

        std::vector<std::string> ret;

        ret.push_back("matrix[a][b]");
        ret.push_back("integer");
        ret.push_back("vector[a]");

        return ret;
    }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
UDFunction *create() {
  return(new CrossEntropy());
}

void destroy(UDFunction *udFunction) {
  delete (CrossEntropy *)udFunction;
}