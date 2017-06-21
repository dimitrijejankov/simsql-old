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
};

// ----------------------------------------------------------- //

/** A pseudo-VG function for inverting a matrix. */
class ReduceMatrixByRow : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    ReduceMatrixByRow() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~ReduceMatrixByRow() {}

    gsl_matrix* executeMatrix(RecordIn* in) {

        // allocate a matrix to output
        gsl_matrix* output = allocateOutputMatrix(1, in->m->matrix->size2);

        for (size_t i = 0; i < in->m->matrix->size2; i++) {
            gsl_matrix_set(output, 0, i, 0);
        }

        for (size_t i = 0; i < in->m->matrix->size1; i++) {
            for (size_t j = 0; j < in->m->matrix->size2; j++) {
                gsl_matrix_set(output, 0, j, gsl_matrix_get(output, 0, j) + gsl_matrix_get(in->m->matrix, i, j));
            }
        }

        // return the result
        return output;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("reduce_matrix_by_row");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("matrix[1][b]");
    }

    /**
     * Returns the input types as strings
     */
    std::vector<std::string> getInputTypes() {

        std::vector<std::string> ret;

        ret.push_back("matrix[a][b]");

        return ret;
    }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
UDFunction *create() {
  return(new ReduceMatrixByRow());
}

void destroy(UDFunction *udFunction) {
  delete (ReduceMatrixByRow *)udFunction;
}