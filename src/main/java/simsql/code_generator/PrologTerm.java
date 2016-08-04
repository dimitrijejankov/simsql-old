

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



package simsql.code_generator;

import java.util.*;

/** Term types. */
enum TermType {
    TUPLE, ATOM, LIST
};

/** The general term interface. */
interface PrologTerm {
    TermType getType();
    String toString();
};

/** A tuple term -- composed of an atom and a set of terms. */
class TupleTerm implements PrologTerm {

    // the name
    private AtomTerm atom;

    // the set of attributes
    private ArrayList<PrologTerm> terms;

    // constructor
    TupleTerm(AtomTerm atomIn, ArrayList<PrologTerm> termsIn) {
	atom = atomIn;
	terms = termsIn;
    }

    // type
    public TermType getType() {
	return TermType.TUPLE;
    }

    // helper methods.
    public AtomTerm getAtom() {
	return atom;
    }

    public int getArity() {
	return terms.size();
    }

    public void checkArity(int arity) {
	if (terms.size() != arity) {
	    throw new RuntimeException("Tuple " + atom.getValue() + " has incorrect arity!");
	}
    }

    public PrologTerm getTerm(int i) {
	return terms.get(i);
    }

    // string representation.
    public String toString() {

	String termStr = terms.get(0).toString();
	for (int i=1;i<terms.size();i++) {
	    termStr += ", " + terms.get(i).toString();
	}

	return atom.toString() + "(" + termStr + ")";
    }
    
}

/** A numeric or string or atomic term. */
class AtomTerm implements PrologTerm {

    // the value itself, as a string.
    private String value;

    // constructor
    AtomTerm(String valueIn) {
	value = valueIn;
    }

    // type identifier.
    public TermType getType() {
	return TermType.ATOM;
    }

    // helper method
    public String getValue() {
	return value;
    }

    // make it a string
    public String toString() {
	return value;
    }
    
    // these methods are for the HashMap...
    public boolean equals(Object o) {
	if (this == o) 
	    return true;
	
	if (o == null)
	    return false;
	
	if (!(o instanceof AtomTerm))
	    return false;
	
	return ((AtomTerm)o).getValue().equals(value);
    }
    
    public int hashCode() {
	return value.hashCode();
    }
}

/** A list term, with a sequence of values that can be iterated. */
class ListTerm implements PrologTerm, Iterable<PrologTerm> {

    private ArrayList<PrologTerm> values;

    // constructor
    public ListTerm(ArrayList<PrologTerm> valuesIn) {
	values = valuesIn;
    }

    // type getter
    public TermType getType() {
	return TermType.LIST;
    }

    // iterator
    public Iterator<PrologTerm> iterator() {
	return values.iterator();
    }

    // element getter
    public PrologTerm get(int i) {
	return values.get(i);
    }

    // make a string.
    public String toString() {

	if (values.isEmpty())
	    return "[]";

	String outStr = "[" + values.get(0).toString();
	for (int i=1;i<values.size();i++) {
	    outStr += ", " + values.get(i);
	}

	return outStr + "]";
    }
}
