

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



package simsql.runtime;

import java.util.Collection;
import java.util.Set;
import java.util.HashSet;

// this simple little class is used to produce the set of all subsets of a collection of String objects
class SubsetMachine {

  static Set <Set <String>> getAllSubsets (Collection <String> input) {
    return recurse (0, input.toArray (new String[0]));
  }

  static Set <Set <String>> recurse (int index, String [] myArray) {

    if (index == myArray.length - 1) {
      Set <Set <String>> returnVal = new HashSet <Set <String>> ();
      Set <String> littleOne = new HashSet <String> ();
      littleOne.add (myArray[index]);
      returnVal.add (littleOne);
      return returnVal;
    }
    Set <Set <String>> setOne = recurse (index + 1, myArray);
    Set <Set <String>> setTwo = recurse (index + 1, myArray);
    for (Set <String> s : setTwo) {
      s.add (myArray[index]);
    }
    Set <String> littleOne = new HashSet <String> ();
    littleOne.add (myArray[index]);
    setOne.add (littleOne);
    setOne.addAll (setTwo);
    return setOne;
  }
}
