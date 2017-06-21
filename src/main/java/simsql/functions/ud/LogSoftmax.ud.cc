#include <stdio.h>
#include <stdlib.h>
#include <string.h>
#include <float.h>
#include <math.h>
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
class LogSoftmax : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    LogSoftmax() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~LogSoftmax() {}

    gsl_matrix* executeMatrix(RecordIn* in) {

        // allocate a matrix to output
        gsl_matrix* output = allocateOutputMatrix(in->m->matrix->size1, in->m->matrix->size2);

        for (size_t i = 0; i < output->size1; i++) {
            double max_value = -DBL_MAX;
            for (size_t j = 0; j < output->size2; j++) {
                double tmp = gsl_matrix_get(in->m->matrix, i, j);
                max_value = max_value < tmp ? tmp : max_value;
            }
            double sum = 0.0;
            for (size_t j = 0; j < output->size2; j++) {
                sum += exp(gsl_matrix_get(in->m->matrix, i, j) - max_value);
            }
            sum = log(sum) + max_value;
            for (size_t j = 0; j < output->size2; j++) {
                gsl_matrix_set(output, i, j, gsl_matrix_get(in->m->matrix, i, j) - sum);
            }
        }

        // return the result
        return output;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("log_softmax");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("matrix[a][b]");
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
  return(new LogSoftmax());
}

void destroy(UDFunction *udFunction) {
  delete (LogSoftmax *)udFunction;
}