

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

/** 
 * This file contains all the classes used to represent expressions
 * in the query, such as boolean predicates, arithmetics, function calls, etc.
 *
 * These are constructed from the prolog tuples and provide a visitor interface
 * for traversal and translation.
 *
 * @author Luis.
 */
 
/** Visitor interface for expressions. */
interface PrologQueryExpressionVisitor<E> {

    E visit(PrologAttributeExp exp);
    E visit(PrologLiteralExp exp);
    E visit(PrologCompExp exp);
    E visit(PrologBoolExp exp);
    E visit(PrologAggExp exp);
    E visit(PrologArithExp exp);
    E visit(PrologFunctionExp exp);
}

/** The general expression type -- all expressions must extend this class. */
abstract class PrologQueryExpression {

    // acceptable types. 
    public enum Type {
	ATTRIBUTE, LITERAL, COMPEXP, BOOLEXP, AGGEXP, ARITHEXP, FUNCTION
    }

    protected String name = "<null>";
    public String getName() {
	return name;
    }

    // all of them must return their type
    public abstract PrologQueryExpression.Type getType();

    // and accept a visitor
    public abstract <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor);

    // the constant terms...
    private static HashMap<AtomTerm, PrologQueryExpression> constTerms = new HashMap<AtomTerm, PrologQueryExpression>();
    static {

	// count-all
	constTerms.put(new AtomTerm("star"), new PrologAttributeExp());

	// assignment.
	constTerms.put(new AtomTerm("minus"), new PrologAttributeExp());
    }

    // used to create from a tuple.
    public static PrologQueryExpression fromTuple(TupleTerm t, 
				      HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
				      HashMap<AtomTerm, TupleTerm> exprTuples) {

	if (t.getAtom().equals(new AtomTerm("__const"))) {
	    return constTerms.get(t.getTerm(0));
	}

	if (t.getAtom().equals(new AtomTerm("compExp"))) {
	    return new PrologCompExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("boolAnd"))) {
	    return new PrologBoolExp(t, PrologBoolExp.Type.AND, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("boolOr"))) {
	    return new PrologBoolExp(t, PrologBoolExp.Type.OR, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("boolNot"))) {
	    return new PrologBoolExp(t, PrologBoolExp.Type.NOT, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("set"))) {
	    return new PrologBoolExp(t, PrologBoolExp.Type.SET, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("aggExp"))) {
	    return new PrologAggExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("arithExp"))) {
	    return new PrologArithExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("verbatim"))) {
	  return new PrologLiteralExp((AtomTerm)t.getTerm(1));
	}

	if (t.getAtom().equals(new AtomTerm("function"))) {
	    return new PrologFunctionExp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	throw new RuntimeException("Unrecognized expression tuple " + t);
    }

    // used to create from a list of expressions.
    public static ArrayList<PrologQueryExpression> fromListOfTuples(ListTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, 
								    HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
								    HashMap<AtomTerm, TupleTerm> exprTuples) {

	ArrayList<PrologQueryExpression> outExp = new ArrayList<PrologQueryExpression>();
	for (PrologTerm tt: t) {
	    outExp.add(PrologQueryExpression.fromTuple(exprTuples.get(tt), vgTuples, attributeTuples, relationTuples, exprTuples));
	}

	return outExp;
    }

    // used to create from a child tuple of the form (exp,type) like (o_orderkey,identifier).
    public static PrologQueryExpression fromChildTuple(AtomTerm t, AtomTerm cType, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, 
					   HashMap<AtomTerm, TupleTerm> relationTuples, HashMap<AtomTerm, TupleTerm> exprTuples) {

	// check the cType
	if (cType.equals(new AtomTerm("literal"))) {
	    return new PrologLiteralExp(t);
	}

	if (cType.equals(new AtomTerm("identifier"))) {
	    return new PrologAttributeExp(attributeTuples.get(t));
	}

	if (cType.equals(new AtomTerm("expression"))) {
	    return PrologQueryExpression.fromTuple(exprTuples.get(t), vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	throw new RuntimeException("Unrecognized child expression tuple " + t + " " + cType);
    }
}

/**** ---- ALL THE SPECIFIC TYPES AHEAD ---- ****/

/** A single attribute, with its data type. */
class PrologAttributeExp extends PrologQueryExpression {

 //    // possible types.
 //    public enum Type {
	// INTEGER, DOUBLE, STRING, BOTTOM, SEED, UNKNOWN
 //    }
	
	private String type;

    //private simsql.runtime.DataType type;				// TO-DO. What about BOTTOM and UNKNOWN?

    // unknown att constructor
    public PrologAttributeExp() {
	name = "";
	type = "unknown";
    }

    public PrologAttributeExp(TupleTerm tup) {
	tup.checkArity(2);
	AtomTerm t0 = (AtomTerm)tup.getTerm(0);
	AtomTerm t1 = (AtomTerm)tup.getTerm(1);

	name = t0.getValue();
	type = t1.getValue();
    }

    public String getName() {
	return name;
    }

    public String getAttType() {
	return type;
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.ATTRIBUTE;
    }

    public String toString() {
	return name;
    }

    // we use this to remove duplicates from sets.
    public boolean equals(Object o) {
	
	if (this == o)
	    return true;

	if (o == null)
	    return false;

	if (!(o instanceof PrologAttributeExp))
	    return false;

	return ((PrologAttributeExp)o).getName().equals(name);
    }

    public int hashCode() {
	return name.hashCode();
    }

    // method for working with lists of attribute names
    public static ArrayList<PrologAttributeExp> fromList(ListTerm t, HashMap<AtomTerm, TupleTerm> attributeTuples, boolean removeDuplicates) {
	
	ArrayList<PrologAttributeExp> outAtts = new ArrayList<PrologAttributeExp>();
	for (PrologTerm tx: t) {
	    
	    PrologAttributeExp tt = new PrologAttributeExp(attributeTuples.get(tx));
	    if (!(removeDuplicates && outAtts.contains(tt))) {
		outAtts.add(tt);
	    }
	}

	return outAtts;
    }

    // method for working with lists of lists of attribute names
    public static ArrayList<PrologAttributeExp> fromListOfLists(ListTerm t, HashMap<AtomTerm, TupleTerm> attributeTuples, boolean removeDuplicates) {
	ArrayList<PrologAttributeExp> outAtts = new ArrayList<PrologAttributeExp>();

	for (PrologTerm tx: t) {

	    for (PrologTerm ty: (ListTerm)tx) {
		PrologAttributeExp tt = new PrologAttributeExp(attributeTuples.get(ty));
		if (!(removeDuplicates && outAtts.contains(tt))) {
		    outAtts.add(tt);
		}		
	    }
	}

	return outAtts;
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** A simple literal value. No type information. */
class PrologLiteralExp extends PrologQueryExpression {

    private String value;
    
    public PrologLiteralExp(AtomTerm t) {
	name = "<literal>";
	value = t.getValue();
    }

    public String getValue() {
	return value;
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.LITERAL;
    }

    public String toString() {
	return value;
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }

    public boolean equals(Object o) {
	if (o == this)
	    return true;

	if (!(o instanceof PrologLiteralExp))
	    return false;

	return ((PrologLiteralExp)o).getValue().equals(value);
    }

    public int hashCode() {
	return value.hashCode();
    }
}

/** A comparison expression. */
class PrologCompExp extends PrologQueryExpression {

    // comparison operator types.
    enum Op {
	EQUALS, NOTEQUALS, LESSTHAN, GREATERTHAN, LESSEQUALS, GREATEREQUALS
    }

    // the operator
    private PrologCompExp.Op op;

    // expression on the left -- always an attribute
    private PrologAttributeExp left;

    // expression on the right.
    private PrologQueryExpression right;

    public PrologCompExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			 HashMap<AtomTerm, TupleTerm> exprTuples) {

	// ensure that it has the right arity.
	t.checkArity(5);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the operator
	op = PrologCompExp.Op.valueOf(((AtomTerm)t.getTerm(1)).getValue().toUpperCase());

	// get the attribute on the left.
	left = new PrologAttributeExp(attributeTuples.get(t.getTerm(2)));

	// get the expression on the right.
	right = PrologQueryExpression.fromChildTuple((AtomTerm)t.getTerm(3), (AtomTerm)t.getTerm(4), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.COMPEXP;
    }

    public PrologCompExp.Op getOp() {
	return op;
    }

    public PrologQueryExpression getLeft() {
	return left;
    }

    public PrologQueryExpression getRight() {
	return right;
    }

    public String toString() {
	switch(op) {
	case EQUALS:
	    return "(" + left + " = " + right + ")";
	case NOTEQUALS:
	    return "(" + left + " != " + right + ")";
	case LESSTHAN: 
	    return "(" + left + " < " + right + ")";
	case GREATERTHAN:
	    return "(" + left + " > " + right + ")";
	case LESSEQUALS:
	    return "(" + left + " <= " + right + ")";
	case GREATEREQUALS:
	    return "(" + left + " >= " + right + ")";
	}

	return "";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** A boolean junction expression. */
class PrologBoolExp extends PrologQueryExpression {

    // junction type.
    public enum Type {
	AND, OR, NOT, SET
    }

    // the type
    private PrologBoolExp.Type type;

    // the list of children expressions
    private ArrayList<PrologQueryExpression> children = new ArrayList<PrologQueryExpression>();

    public PrologBoolExp(TupleTerm t, PrologBoolExp.Type inType, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			 HashMap<AtomTerm, TupleTerm> exprTuples) {
	
	// arity
	t.checkArity(2);

	// set the type
	type = inType;

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the children expressions
	children = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.BOOLEXP;
    }

    public PrologBoolExp.Type getBoolType() {
	return type;
    }

    public ArrayList<PrologQueryExpression> getChildren() {
	return children;
    }

    public String toString() {
	return type + "(" + children + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** An aggregate expression. */
class PrologAggExp extends PrologQueryExpression {

    // aggregate type.
    enum Type {
	AVG, SUM, COUNT, COUNTALL, MIN, MAX, VAR, STDEV, VECTOR, ROWMATRIX, COLMATRIX
    }

    // the type.
    private PrologAggExp.Type type;

    // the expression being aggregated.
    private PrologQueryExpression exp;

    public PrologAggExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			 HashMap<AtomTerm, TupleTerm> exprTuples) {

	t.checkArity(4);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the type
	type = PrologAggExp.Type.valueOf(((AtomTerm)t.getTerm(1)).getValue().toUpperCase());

	// get the child expression.
	exp = PrologQueryExpression.fromChildTuple((AtomTerm)t.getTerm(2), (AtomTerm)t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

	// if it's an unknown attribute, then we have a COUNTALL
	if (exp.getType() == PrologQueryExpression.Type.ATTRIBUTE && ((PrologAttributeExp)exp).getAttType().equals("unknown")) {
	    type = Type.COUNTALL;
	}
    }

    public PrologAggExp.Type getAggType() {
	return type;
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.AGGEXP;
    }

    public PrologQueryExpression getExp() {
	return exp;
    }

    public String toString() {
	return type + "(" + exp + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** An arithmetic expression, with its operator. */
class PrologArithExp extends PrologQueryExpression {

    // arithmetic operator type
    enum Op {
	PLUS, MINUS, TIMES, DIVIDE
    }

    // operator
    private PrologArithExp.Op op;

    // expression on the LHS
    private PrologQueryExpression left;

    // expression on the RHS
    private PrologQueryExpression right;

    public PrologArithExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			  HashMap<AtomTerm, TupleTerm> exprTuples) {


	// ensure that it has the right arity.
	t.checkArity(6);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the operator
	op = PrologArithExp.Op.valueOf(((AtomTerm)t.getTerm(1)).getValue().toUpperCase());
	
	// get the attribute on the left.
	left = PrologQueryExpression.fromChildTuple((AtomTerm)t.getTerm(2), (AtomTerm)t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

	// get the expression on the right.
	right = PrologQueryExpression.fromChildTuple((AtomTerm)t.getTerm(4), (AtomTerm)t.getTerm(5), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.ARITHEXP;
    }

    public PrologArithExp.Op getOp() {
	return op;
    }

    public PrologQueryExpression getLeft() {
	return left;
    }

    public PrologQueryExpression getRight() {
	return right;
    }

    public String toString() {
	switch(op) {
	case PLUS:
	    return "(" + left + " + " + right + ")";
	case MINUS:
	    return "(" + left + " - " + right + ")";
	case TIMES:
	    return "(" + left + " - " + right + ")";	    
	case DIVIDE:
	    return "(" + left + " / " + right + ")";
	}

	return "";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** A function expression, with name and parameters.*/
class PrologFunctionExp extends PrologQueryExpression {

    // function name (not the same as the expression name!)
    private String function;

    // all the parameters
    private ArrayList<PrologQueryExpression> params = new ArrayList<PrologQueryExpression>();

    public PrologFunctionExp(TupleTerm t, HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			  HashMap<AtomTerm, TupleTerm> exprTuples) {

	// check the arity...
	t.checkArity(3);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the function
	function = ((AtomTerm)t.getTerm(1)).getValue();

	// now, go through the list of parameters...
	for (PrologTerm tt: (ListTerm)t.getTerm(2)) {

	    // each one is, by itself, a list.
	    ListTerm tl = (ListTerm)tt;

	    // i.e. a pair
	    params.add(PrologQueryExpression.fromChildTuple((AtomTerm)tl.get(0), (AtomTerm)tl.get(1), vgTuples, attributeTuples, relationTuples, exprTuples));
	}
    }

    public PrologQueryExpression.Type getType() {
	return PrologQueryExpression.Type.FUNCTION;
    }

    public String getFunction() {
	return function;
    }

    public ArrayList<PrologQueryExpression> getParams() { 
	return params;
    }

    public String toString() {
	return function + "(" + params + ")";
    }

    public <E> E acceptVisitor(PrologQueryExpressionVisitor<E> visitor) {
	return visitor.visit(this);
    }
}
