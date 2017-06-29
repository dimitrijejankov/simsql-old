package tests;

import simsql.runtime.Matrix;
import java.util.LinkedList;

/**
 * Has to be run with the parameter -XX:MaxDirectMemorySize=128K
 */
public class MatrixMemoryTest {

    public static void main(String[] args) {
        double data[][] = {{1, 2, 3, 4}, {2, 4, 5, 6}};


        LinkedList<Matrix> tmp = new LinkedList<>();

        // add four matrices
        tmp.addLast(new Matrix(2, 4, true, data));
        tmp.addLast(new Matrix(2, 4, true, data));
        tmp.addLast(new Matrix(2, 4, true, data));
        tmp.addLast(new Matrix(2, 4, true, data));

        // create and remove 2048 matrices in the native memory the test has a limit of 1024 matrices in the native memory
        for(int i = 0; i < 2048; ++i) {
            tmp.addLast(new Matrix(2, 4, true, data));
            tmp.removeFirst();
        }

        for(Matrix m : tmp) {
            System.out.println("Matrica");
        }
    }

;
}
