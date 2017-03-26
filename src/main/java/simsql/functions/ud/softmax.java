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
public class softmax extends ReflectedUDFunction {


    public static double[][] softmax_fun(double[][] mat) {

        int row = mat.length;
        int col = mat[0].length;

        double[][] out = new double[row][col];

        for (int i = 0; i < row; i++) {
            double max_value = Float.MIN_VALUE;
            for (int j = 0; j < col; j++) {
                max_value = max_value < mat[i][j] ? mat[i][j] : max_value;
            }
            double sum = 0.0;
            for (int j = 0; j < col; j++) {
                sum += Math.exp(mat[i][j] - max_value);
            }
            sum = Math.log(sum) + max_value;
            for (int j = 0; j < col; j++) {
                out[i][j] = Math.exp(mat[i][j] - sum);
            }
        }

        return out;
    }

    public softmax() {
        super("simsql.functions.ud.softmax", "softmax_fun", new AttributeType(new MatrixType("matrix[][]")), double[][].class);
        setInputTypes(new AttributeType(new MatrixType("matrix[][]")));
        setOutputType(new AttributeType(new MatrixType("matrix[][]")));
    }

    @Override
    public String getName() {
        return "softmax";
    }
}