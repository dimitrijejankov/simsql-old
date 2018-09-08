package simsql.functions.ud;

import simsql.runtime.*;
import java.util.HashSet;

/**
 * Created by jacobgao on 7/24/17.
 */
public class log_uniform_sampling extends ReflectedUDFunction {

    public static double[] sample(int vocab_num, int neg_num) {

        // double t[] = new double[neg_num + 1];
        // for (int i = 0; i < neg_num; i++)
        //     t[i] = Math.floor(Math.random() * vocab_num);
        // t[neg_num] = -1;
        // return t;

        double p[] = new double[vocab_num];
        for (int i = 0; i < vocab_num; i++) {
            p[i] = (Math.log(i + 2) - Math.log(i + 1)) / Math.log(vocab_num + 1);
            if (i > 0) {
                p[i] += p[i - 1];
            }
        }

        HashSet<Integer> values = new HashSet<Integer>();
        double t[] = new double[neg_num + 1];
        int n = 0;
        while (n < neg_num) {
            double u = Math.random();
            for (int i = 0; i < vocab_num; i++) {
                if (u <= p[i]) {
                    if (!values.contains(i)) {
                        t[n++] = i;
                        values.add(i);
                    }
                    break;
                }
            }
        }
        t[neg_num] = -1;

        return t;
    }

    public log_uniform_sampling() {
        super("simsql.functions.ud.log_uniform_sampling", "sample", new AttributeType(new VectorType()), int.class, int.class);
        setInputTypes(new AttributeType(new IntType()), new AttributeType(new IntType()));
        setOutputType(new AttributeType(new VectorType("vector[a]")));
    }

    @Override
    public String getName() {
        return "log_uniform_sampling";
    }
}
