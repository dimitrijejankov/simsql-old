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
  Matrix *m; // the vector in matrix form
  Vector *v; // the matrix
};

// ----------------------------------------------------------- //

/** A pseudo-VG function for inverting a matrix. */
class MatrixVectorMultiply : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    MatrixVectorMultiply() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~MatrixVectorMultiply() {}

    gsl_vector* executeVector(RecordIn* in) {

        // allocate a matrix to output
        gsl_vector *out = allocateOutputVector(in->m->matrix->size1);

        // convert the vector to a gsl_vector
        gsl_vector vec = in->v->to_gsl_vector();

        // multiply
        gsl_blas_dgemv(CblasNoTrans, 1.0, in->m->matrix, &vec, 1.0, out);

        // return the result
        return out;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("matrix_vector_multiply");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("vector[a]");
    }

    /**
     * Returns the input types as strings
     */
    std::vector<std::string> getInputTypes() {

        std::vector<std::string> ret;

        ret.push_back("matrix[a][b]");
        ret.push_back("vector[b]");

        return ret;
    }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
UDFunction *create() {
  return(new MatrixVectorMultiply());
}

void destroy(UDFunction *udFunction) {
  delete (MatrixVectorMultiply *)udFunction;
}