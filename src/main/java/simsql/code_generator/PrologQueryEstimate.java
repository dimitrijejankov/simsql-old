

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

/** 
 * Encapsulates the statistics for an operator that are provided by
 * the optimizer.
 *
 * @author Luis
 */

class PrologQueryEstimate {

    // input or output?
    enum Mode {
	INPUT, OUTPUT
    }

    // operator type.
    private PrologQueryOperator.Type operator;
    
    // mode
    private PrologQueryEstimate.Mode mode;

    // value
    private double value;

    // constructor from a tuple
    PrologQueryEstimate(TupleTerm tup) {

	// check the arity
	tup.checkArity(2);

	// check the atom type to get the mode
	mode = PrologQueryEstimate.Mode.INPUT;
	if (tup.getAtom().equals("operatorOutputEstimate"))
	    mode = PrologQueryEstimate.Mode.OUTPUT;

	// get the operator type
	operator = PrologQueryOperator.Type.valueOf(((AtomTerm)tup.getTerm(0)).getValue().toUpperCase());

	// get the number
	value = Double.parseDouble(((AtomTerm)tup.getTerm(1)).getValue());
    }

    public PrologQueryOperator.Type getOperator() {
	return operator;
    }

    public PrologQueryEstimate.Mode getMode() {
	return mode;
    }

    public double getValue() {
	return value;
    }
}
