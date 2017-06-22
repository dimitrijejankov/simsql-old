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
  Matrix *v; // the vector in matrix form
  Matrix *m; // the matrix
};

// ----------------------------------------------------------- //

/** A pseudo-VG function for inverting a matrix. */
class MatrixTransposeMatrixMultiply : public UDFunction {

public:

    /** Constructor. Use this to declare your RNG and other
    * important structures.
    */
    MatrixTransposeMatrixMultiply() {}

    /** Destructor. Deallocate everything from the constructor. */
    ~MatrixTransposeMatrixMultiply() {}

    gsl_matrix* executeMatrix(RecordIn* in) {

        // allocate a matrix to output
        gsl_matrix* out = allocateOutputMatrix(in->v->matrix->size2, in->m->matrix->size2);

        // multiply the matrix
        gsl_blas_dgemm(CblasTrans, CblasNoTrans, 1.0, in->v->matrix, in->m->matrix, 0.0, out);

        // return the result
        return out;
    }

    /**
     * Returns the name
     */
    std::string getName() {
        return std::string("matrix_transpose_matrix_multiply");
    }

    /**
     * Returns the output type
     */
    std::string getOutputType(){
        return std::string("matrix[a][c]");
    }

    /**
     * Returns the input types as strings
     */
    std::vector<std::string> getInputTypes() {

        std::vector<std::string> ret;

        ret.push_back("matrix[b][a]");
        ret.push_back("matrix[b][c]");

        return ret;
    }
};

// ----------------------------------------------------------- //

/** External creation/destruction methods -- DO NOT MODIFY */
UDFunction *create() {
  return(new MatrixTransposeMatrixMultiply());
}

void destroy(UDFunction *udFunction) {
  delete (MatrixTransposeMatrixMultiply *)udFunction;
}