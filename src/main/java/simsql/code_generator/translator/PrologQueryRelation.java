

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
import simsql.code_generator.translator.expressions.PrologLiteralExp;
import simsql.code_generator.translator.terms.AtomTerm;
import simsql.code_generator.translator.terms.ListTerm;
import simsql.code_generator.translator.terms.TupleTerm;

import java.util.*;

/**
 * A relation, with its name, location, and set of attributes.
 */
public class PrologQueryRelation {

    enum Type {
        RELATION, TEMPORARYTABLE
    }

    private String name;
    private String file;
    private Type type;
    private ArrayList<PrologAttributeExp> attributes = new ArrayList<PrologAttributeExp>();
    private ArrayList<ArrayList<PrologLiteralExp>> rows = new ArrayList<ArrayList<PrologLiteralExp>>();

    public PrologQueryRelation(TupleTerm tup, HashMap<AtomTerm, TupleTerm> allAttributes) {

        // check the arity
        tup.checkArity(3);

        // check the type
        if (tup.getAtom().equals(new AtomTerm("relation"))) {

            // get the name
            name = ((AtomTerm) tup.getTerm(0)).getValue();

            // get the file
            file = ((AtomTerm) tup.getTerm(1)).getValue();

            // get the set of attributes.
            for (PrologTerm t : ((ListTerm) tup.getTerm(2))) {
                attributes.add(new PrologAttributeExp(allAttributes.get(t)));
            }

            // set the type
            type = Type.RELATION;
        } else if (tup.getAtom().equals(new AtomTerm("temporaryTable"))) {

            // get the name and file
            name = ((AtomTerm) tup.getTerm(0)).getValue();
            file = name;

            // get the set of attributes.
            for (PrologTerm t : ((ListTerm) tup.getTerm(1))) {
                attributes.add(new PrologAttributeExp(allAttributes.get(t)));
            }

            // get the set of literals.
            for (PrologTerm t : ((ListTerm) tup.getTerm(2))) {

                ArrayList<PrologLiteralExp> tx = new ArrayList<PrologLiteralExp>();
                for (PrologTerm tt : (ListTerm) t) {
                    tx.add(new PrologLiteralExp((AtomTerm) tt));
                }

                rows.add(tx);
            }

            // set the type.
            type = Type.TEMPORARYTABLE;
        }
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public String getFile() {
        return file;
    }

    public ArrayList<PrologAttributeExp> getAttributes() {
        return attributes;
    }

    public String toString() {
        return name;
    }

    public ArrayList<ArrayList<PrologLiteralExp>> getRows() {
        return rows;
    }
}
