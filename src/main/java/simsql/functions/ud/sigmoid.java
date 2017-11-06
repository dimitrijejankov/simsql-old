package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the sigmoid of a vector.
 *
 * @author Jacob
 */
public class sigmoid extends ReflectedUDFunction {


    public static double[][] sigmoid_fun(double[][] mat) {

        int row = mat.length;
        int col = mat[0].length;

        double[][] out = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                out[i][j] = 1.0 / (1.0 + Math.exp(-mat[i][j]));
            }
        }

        return out;
    }

    public sigmoid() {
        super("simsql.functions.ud.sigmoid", "sigmoid_fun", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[][]")));
        setOutputType(new AttributeType(new MatrixType("matrix[][]")));
    }

    @Override
    public String getName() {
        return "sigmoid";
    }
}