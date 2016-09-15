

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

import java.util.*;
import simsql.runtime.*;
import java.net.*;
import java.lang.*;
import java.lang.management.*;

/**
 * A function for simulating the CASE expression.
 *
 * @author Luis
 */
public class caseexpr extends ReflectedFunction {

  public static double casefn(int booleanVal, double return1, double return2) {

    return (booleanVal == 1) ? return1 : return2;
  }

  public caseexpr() {
    super("simsql.functions.scalar.caseexpr", "casefn", int.class, double.class, double.class);
  }

  @Override
  public String getName() {
    return "caseexpr";
  }
}
