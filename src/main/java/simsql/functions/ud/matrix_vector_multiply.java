package simsql.functions.ud;

import simsql.runtime.*;

/**
 * A reflected function obtained from a java method.
 *
 * @author Jacob
 */

public class matrix_vector_multiply extends UDFunction {

    public matrix_vector_multiply() {
        super("/simsql/runtime/MatrixVectorMultiply.ud.so");
    }

    public static void main(String[] args) {

        matrix_vector_multiply os = new matrix_vector_multiply();

        double[][] mat = new double[][]{{1, 2, 3}, {4, 5, 6}};
        double[] vec = new double[]{7, 8, 9};

        Attribute out = os.apply(new MatrixAttribute(mat), new VectorAttribute(vec));

        System.out.println(out.print(200));
    }
}