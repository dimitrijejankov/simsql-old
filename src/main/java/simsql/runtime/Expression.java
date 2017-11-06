

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

import java.util.Map;
import java.lang.String;
import java.util.ArrayList;

public class Expression {
  
    // this is an exhaustive list of expression types
    static public final String [] validTypes = {"plus", "minus", "times", 
						"divided by",  "or", "and", "not", "literal string", "literal float",
						"literal int", "identifier", "unary minus", "func", "less than or equal",
						"equals", "greater than", "less than", "greater than or equal", "is null", "not equal"};
  
    // this is an exhaustive list of the unary expression types
    static public final String [] unaryTypes = {"not", "unary minus", "is null"};
  
    // this is an exhaustive list of the binary expression types
    static public final String [] binaryTypes = {"plus", "minus", "times",
						 "divided by", "or", "and", "equals", "greater than", "less than", 
						 "less than or equal", "greater than or equal", "not equal"};
  
    // this is an exhaustive list of th n-ary types
    static public final String [] naryTypes = {"func"};
  
    // this is an exhaustive list of the value types
    public final String [] valueTypes = {"literal string", "literal float",
					 "literal int", "identifier", "func"};
  
    // this is the type of the expression
    private String myType;
  
    // this is the literal value contained in the expression; only non-null
    // if myType is "literal" or "identifier" or "func"
    private String myValue;
  
    // these are the two subexpressions
    private Expression leftSubexpression;
    private Expression rightSubexpression;
  
    private ArrayList <Expression> subexpressionList = new ArrayList <Expression> ();
 
    // prints the expression as Java code, using the map that is passed in to replace identifiers as indicated
    public String print (Map <String, String> replacementMap) {
    
	// this nasty code simply considers each of the various expression types and prints them out
	String toMe;
		switch (myType) {
			case "less than or equal":
				toMe = leftSubexpression.print(replacementMap) + ".lessThanOrEqual (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "equals":
				toMe = leftSubexpression.print(replacementMap) + ".equals (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "not equal":
				toMe = leftSubexpression.print(replacementMap) + ".notEqual (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "greater than":
				toMe = leftSubexpression.print(replacementMap) + ".greaterThan (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "less than":
				toMe = leftSubexpression.print(replacementMap) + ".lessThan (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "greater than or equal":
				toMe = leftSubexpression.print(replacementMap) + ".greaterThanOrEqual (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "plus":
				toMe = leftSubexpression.print(replacementMap) + ".add (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "minus":
				toMe = leftSubexpression.print(replacementMap) + ".subtract (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "times":
				toMe = leftSubexpression.print(replacementMap) + ".multiply (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "divided by":
				toMe = leftSubexpression.print(replacementMap) + ".divide (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "or":
				toMe = leftSubexpression.print(replacementMap) + ".or (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "and":
				toMe = leftSubexpression.print(replacementMap) + ".and (" + rightSubexpression.print(replacementMap) + ")";
				break;
			case "not":
				toMe = leftSubexpression.print(replacementMap) + ".not ()";
				break;
			case "is null":
				toMe = leftSubexpression.print(replacementMap) + ".isNull()";
				break;
			case "literal string":
				toMe = "(new StringAttribute (" + myValue + "))";
				break;
			case "literal float":
				toMe = "(new DoubleAttribute (" + myValue + "))";
				break;
			case "literal int":
				toMe = "(new IntAttribute (" + myValue + "))";
				break;
			case "identifier":
				toMe = replacementMap.get(myValue);
				if (toMe == null) {
					System.err.println(replacementMap);
					throw new RuntimeException("When outputting Java code, I could not find any reference to attribute " + myValue +
							" from the dataflow program");
				}
				break;
			case "unary minus":
				toMe = "(new IntAttribute (0)).subtract (" + leftSubexpression.print(replacementMap) + ")";
				break;
			case "func":
				toMe = "func_" + myValue + ".apply (";
				boolean first = true;
				for (Expression e : subexpressionList) {
					if (first) {
						toMe += e.print(replacementMap);
					} else {
						toMe += ", " + e.print(replacementMap);
					}
					first = false;
				}
				toMe += ")";
				break;
			default:
				throw new RuntimeException("found an operation that I did not recognize!");
		}
     
	return toMe;
    }
  
  
    // prints the expression
    public String print () {
    
	String toMe = null;
    
	// see if it is an nary type
	for (int i = 0; i < naryTypes.length; i++) {
	    if (myType.equals (naryTypes[i])) {
		for (Expression e: subexpressionList) {
		    if (toMe == null) {
			toMe = myValue + "(" + e.print ();
		    } else {
			toMe += ", " + e.print (); 
		    }
		}
		return toMe + ")";
	    }
	}
    
	// see if it is a literal type
	for (int i = 0; i < valueTypes.length; i++) {
	    if (myType.equals (valueTypes[i])) {
		toMe = myValue;
		return toMe;
	    } 
	}
    
	// see if it is a unary type
	for (int i = 0; i < unaryTypes.length; i++) {
	    if (myType.equals (unaryTypes[i])) {
		toMe = "(" + myType + " " + leftSubexpression.print () + ")";
		return toMe;
	    }
	}
    
	// see it it is a binary type
	for (int i = 0; i < binaryTypes.length; i++) {
	    if (myType.equals (binaryTypes[i])) {
		toMe = "(" + leftSubexpression.print () + " " + myType + " " + rightSubexpression.print () + ")";
		return toMe;
	    }
	}
    
	throw new RuntimeException ("got a bad type in the expression when printing");
    }
  
    // create a new expression of type specified type
    public Expression (String expressionType) {
    
	// verfiy it is a valid expression type
	for (int i = 0; i < validTypes.length; i++) {
	    if (expressionType.equals (validTypes[i])) {
		myType = expressionType;
		return;
	    }
	}
    
	// it is not valid, so throw an exception
	throw new RuntimeException ("you tried to create an invalid expr type '" + expressionType + "'");
    }
  
    public String getType () {
	return myType;
    }
  
    // this returns the value of the expression, if it is a literal (in which
    // case the literal values encoded as a string is returned), or it is an
    // identifier (in which case the name if the identifier is returned)
    public String getValue () {
	for (int i = 0; i < valueTypes.length; i++) {
	    if (myType.equals (valueTypes[i])) {
		return myValue;
	    }
	} 
	throw new RuntimeException ("you can't get a value for that expr type!");
    }
  
    // this sets the value of the expression, if it is a literal or an 
    // identifier
    public void setValue (String toMe) {
	for (int i = 0; i < valueTypes.length; i++) {
	    if (myType.equals (valueTypes[i])) {
		myValue = toMe;
		return;
	    }
	} 
	throw new RuntimeException ("you can't set a value for that expr type!");
    }
  
    // this gets the subexpression, which is only possible if this is a 
    // unary operation (such as "unary minus" or "not")
    public Expression getSubexpression () {
    
	// verfiy it is a valid expression type
	for (int i = 0; i < unaryTypes.length; i++) {
	    if (myType.equals (unaryTypes[i])) {
		return leftSubexpression;
	    }
	}
    
	// it is not valid, so throw an exception
	throw new RuntimeException ("you can't get the subexpression of an " +
				    "expression that is not unary!");
    }
  
    // this sets the subexpression, which is only possible if this is a 
    // unary operation (such as "unary minus" or "not")
    public void setSubexpression (Expression newChild) {
    
	// verfiy it is a valid expression type
	for (int i = 0; i < unaryTypes.length; i++) {
	    if (myType.equals (unaryTypes[i])) {
		leftSubexpression = newChild;
		return;
	    }
	}
    
	// it is not valid, so throw an exception
	throw new RuntimeException ("you can't set the subexpression of an " +
				    "expression that is not unary!");
    }
  
    // this gets either the left or the right subexpression, which is only 
    // possible if this is a binary operation... whichOne should either be
    // the string "left" or the string "right"
    public Expression getSubexpression (String whichOne) {
    
	// verfiy it is a valid expression type
	for (int i = 0; i < binaryTypes.length; i++) {
	    if (myType.equals (binaryTypes[i])) {
		if (whichOne.equals ("left"))
		    return leftSubexpression;
		else if (whichOne.equals ("right"))
		    return rightSubexpression;
		else
		    throw new RuntimeException ("whichOne must be left or right");
	    }
	}
    
	// it is not valid, so throw an exception
	throw new RuntimeException ("you can't set the l/r subexpression of " +
				    "an expression that is not binry!");
    }
  
    // this gets the list of subexpressions, which is only possible if this
    // is an nary operation
    public ArrayList <Expression> getSubexpressions () {
	for (int i = 0; i < naryTypes.length; i++) {
	    if (myType.equals (naryTypes[i])) {
		return subexpressionList;
	    }
	}
    
	throw new RuntimeException ("you can't get the subexpression list of " +
				    "an expression that is not n-ary!");
    
    }
  
    // this appends a new subexpression, which is only possible if this
    // is an nary operation
    public void addSubexpression (Expression addMe) {
	for (int i = 0; i < naryTypes.length; i++) {
	    if (myType.equals (naryTypes[i])) {
		subexpressionList.add (addMe);
		return;
	    }
	}
    
	throw new RuntimeException ("you can't get the subexpression list of " +
				    "an expression that is not n-ary!");
    }
  
    // this sets the left and the right subexpression
    public void setSubexpression (Expression left, Expression right) {
    
	// verfiy it is a valid expression type
	for (int i = 0; i < binaryTypes.length; i++) {
	    if (myType.equals (binaryTypes[i])) {
		leftSubexpression = left;
		rightSubexpression = right;
		return;
	    }
	}
    
	// it is not valid, so throw an exception
	throw new RuntimeException ("you can't get the l/r subexpression of " +
				    "an expression that is not binry!");
    }


    // enumerates the set of functions being applied in this expression
    public ArrayList<String> getAllFunctions() {


	// case 1: nary type
	for (String tx: naryTypes) {
	    if (myType.equals(tx)) {

		ArrayList<String> ret = new ArrayList<String>();

		// add the function name, if it's a function type
		if (myType.equals("func")) {
		    ret.add(myValue);
		}

		// and recursively go through its parameters
		for (Expression e: subexpressionList) {
		    ret.addAll(e.getAllFunctions());
		}

		return ret;
	    }
	}

	// case 2: unary
	for (String tx: unaryTypes) {
	    if (myType.equals(tx)) {
		return leftSubexpression.getAllFunctions();
	    }
	}

	// case 3: binary
	for (String tx: binaryTypes) {
	    if (myType.equals(tx)) {
		ArrayList<String> wx = leftSubexpression.getAllFunctions();
		wx.addAll(rightSubexpression.getAllFunctions());
		return wx;
	    }
	}

	// otherwise, just return an empty.
	return new ArrayList<String>();
    }
}


