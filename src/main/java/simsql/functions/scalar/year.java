

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
 * A date function reflected from its own method.
 *
 * @author Luis.
 */

public class year extends ReflectedFunction {

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    private static Calendar cal = Calendar.getInstance();

    public static int yearfn(String date) {
        try {
            cal.setTime(sdf.parse(date));
            return cal.get(Calendar.YEAR);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse date string " + date);
        }
    }

    public year() {
	super("simsql.functions.scalar.year", "yearfn", String.class);
    }

    @Override
    public String getName() {
	return "year";
    }
}
