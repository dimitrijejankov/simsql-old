package simsql.functions.ud;

import simsql.runtime.*;

/**
 * A reflected function obtained from a java method.
 *
 * @author Jacob
 */

public class matrix_multiply extends UDFunction {

    public matrix_multiply() {
        super("/simsql/runtime/MatrixMatrixMultiply.ud.so");
    }

    @Override
    public String getName() {
        return "matrix_multiply";
    }

    public static void main(String[] args) {

        matrix_multiply os = new matrix_multiply();

        double[][] mat1 = new double[][]{{1, 2, 3}, {1, 2, 3}};
        double[][] mat2 = new double[][]{{7, 8, 9, 10}, {9, 10, 11, 12}, {7, 8, 9, 10}};

        MatrixAttribute ma1 = new MatrixAttribute(mat1);
        MatrixAttribute ma2 = new MatrixAttribute(mat2);

        System.out.print(ma1.print(200));
        System.out.print(ma2.print(200));

        Attribute out = os.apply(ma1, ma2);

        System.out.println(out.print(200));

    }
}