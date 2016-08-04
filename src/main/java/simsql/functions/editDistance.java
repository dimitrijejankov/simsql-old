

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
 * An 'edit distance' function, reflected from its own method.
 *
 * @author Luis.
 */

public class editDistance extends ReflectedFunction {

    // minimum of 3 values.                                                                                                                                                                                
    public static int min3(int a, int b, int c) {
        return Math.min(Math.min(a, b), c);
    }

    // edit distance between two strings,
    // got it from http://en.wikibooks.org/wiki/Algorithm_Implementation/Strings/Levenshtein_distance#Java
    public static int ed(String str1, String str2) {
        int[][] distance = new int[str1.length() + 1][str2.length() + 1];

        for (int i = 0; i <= str1.length(); i++)
            distance[i][0] = i;
        for (int j = 0; j <= str2.length(); j++)
            distance[0][j] = j;

        for (int i = 1; i <= str1.length(); i++)
            for (int j = 1; j <= str2.length(); j++)
                distance[i][j] = min3(
                                      distance[i - 1][j] + 1,
                                      distance[i][j - 1] + 1,
                                      distance[i - 1][j - 1]
                                      + ((str1.charAt(i - 1) == str2.charAt(j - 1)) ? 0
                                         : 1));

        return distance[str1.length()][str2.length()];
    }


    public editDistance() {
	super("simsql.functions.editDistance", "ed", String.class, String.class);
    }

    @Override
    public String getName() {
	return "editDistance";
    }
}
