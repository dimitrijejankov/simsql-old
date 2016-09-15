

/*****************************************************************************
 *                                                                           *
 *  Copyright 2014 Rice University                                           *
 *                                                                           *
 *  Licensed under the Apache License, Version 2.0 (the "License");          *
 *  you may not use this file except in compliance with the License.         *
 *  You may obtain a copy of the License at                                  *
 *                                                                           *
 *      http://www.apache.org/licenses/LICENSE-2.0                           *
 *                                                                           *
 *  Unless required by applicable law or agreed to in writing, software      *
 *  distributed under the License is distributed on an "AS IS" BASIS,        *
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 *  See the License for the specific language governing permissions and      *
 *  limitations under the License.                                           *
 *                                                                           *
 *****************************************************************************/


package simsql.functions.scalar;

import simsql.runtime.*;
import java.text.*;
import java.util.*;

/**
 * Evaluates the normal CDF function.
 *
 * @author Luis.
 */

public class normalCDF extends ReflectedFunction {


    public static double erf(double z) {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(z));

        double ans = 1 - t * Math.exp( -z*z   -   1.26551223 +
                                       t * ( 1.00002368 +
                                       t * ( 0.37409196 +
                                       t * ( 0.09678418 +
                                       t * (-0.18628806 +
                                       t * ( 0.27886807 +
                                       t * (-1.13520398 +
                                       t * ( 1.48851587 +
                                       t * (-0.82215223 +
                                       t * ( 0.17087277))))))))));

        if (z >= 0) return  ans;
        else        return -ans;
    }


    public static double normal_cdf(double x, double mu, double stdev) {

	// error function parameter.
        double erfP = (x - mu) / (stdev * 1.4142135623);

	// return CDF value by calling the error function.
        return 0.5 * (1 + erf(erfP));
    }

    public normalCDF() {
	super("simsql.functions.scalar.normalCDF", "normal_cdf", double.class, double.class, double.class);
    }
    

    @Override
    public String getName() {
	return "normalCDF";
    }

}
