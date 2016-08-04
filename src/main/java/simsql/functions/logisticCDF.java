

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


package simsql.functions;

import simsql.runtime.*;
import java.text.*;
import java.util.*;

/**
 * Evaluates the logistic CDF function.
 *
 * @author Luis.
 */

public class logisticCDF extends ReflectedFunction {

    public static double logistic_cdf(double x, double intercept, double coef) {
        return 1.0 / (1 + Math.exp(-1*(intercept + (x*coef))));
    }

    public logisticCDF() {
	super("simsql.functions.logisticCDF", "logistic_cdf", double.class, double.class, double.class);
    }
    

    @Override
    public String getName() {
	return "logisticCDF";
    }
}
