package simsql.functions.ud;

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for calculating the (point-wise) relu of a matrix = max(0,x).
 *
 * @author Jacob
 */
public class relu extends ReflectedUDFunction {


    public static double[][] relu_fun(double[][] mat) {

        int row = mat.length;
        int col = mat[0].length;

        double[][] out = new double[row][col];

        for (int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                out[i][j] = Math.max(0, mat[i][j]);
            }
        }

        return out;
    }

    public relu() {
        super("simsql.functions.ud.relu", "relu_fun", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[][]")));
        setOutputType(new AttributeType(new MatrixType("matrix[][]")));
    }

    @Override
    public String getName() {
        return "relu";
    }
}