package simsql.functions.ud;

import simsql.runtime.Attribute;
import simsql.runtime.MatrixAttribute;
import simsql.runtime.UDFunction;


public class matrix_matrix_transpose_multiply extends UDFunction {

    public matrix_matrix_transpose_multiply() {
        super("/simsql/runtime/MatrixMatrixTransposeMultiply.ud.so");
    }

    public static void main(String[] args) {

        matrix_matrix_transpose_multiply os = new matrix_matrix_transpose_multiply();

        double[][] mat1 = new double[][]{{1, 2, 3}, {1, 2, 3}};
        double[][] mat2 = new double[][]{{7, 8, 9}, {9, 10, 11}, {7, 8, 9}, {10, 11, 12}};

        MatrixAttribute ma1 = new MatrixAttribute(mat1);
        MatrixAttribute ma2 = new MatrixAttribute(mat2);

        System.out.print(ma1.print(200));
        System.out.print(ma2.print(200));

        Attribute out = os.apply(ma1, ma2);

        System.out.println(out.print(200));
    }
}
