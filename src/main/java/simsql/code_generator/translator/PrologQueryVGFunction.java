

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


package simsql.code_generator.translator;

import simsql.code_generator.translator.expressions.PrologAttributeExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.*;

/**
 * a VG function with its name, location and sets of input and output attributes.
 */
public class PrologQueryVGFunction {

    private String name;
    private String file;
    private ArrayList<PrologAttributeExp> inputAttributes = new ArrayList<PrologAttributeExp>();
    private ArrayList<PrologAttributeExp> outputAttributes = new ArrayList<PrologAttributeExp>();

    public PrologQueryVGFunction(TupleTerm tup, HashMap<AtomTerm, TupleTerm> allAttributes) {

        // check the arity
        tup.checkArity(4);

        // get the name
        name = ((AtomTerm) tup.getTerm(0)).getValue();

        // get the file
        file = ((AtomTerm) tup.getTerm(1)).getValue();

        // get the set of input attributes.
        for (PrologTerm t : ((ListTerm) tup.getTerm(2))) {
            inputAttributes.add(new PrologAttributeExp(allAttributes.get(t)));
        }

        // get the set of output attributes.
        for (PrologTerm t : ((ListTerm) tup.getTerm(3))) {
            outputAttributes.add(new PrologAttributeExp(allAttributes.get(t)));
        }
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
        return inputAttributes;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
        return outputAttributes;
    }

    public String toString() {
        return name;
    }
}
