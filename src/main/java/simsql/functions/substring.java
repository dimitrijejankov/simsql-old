

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
 * A 'substring' function.
 *
 * @author Luis.
 */

public class substring extends ReflectedFunction {

  public static String substr(String str1, int pos1, int pos2) {
    return str1.substring(pos1, pos2);
  }

  public substring() {
    super("simsql.functions.substring", "substr", String.class, int.class, int.class);
  }

  @Override
  public String getName() {
    return "substring";
  }
}
