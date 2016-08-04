

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
 * This file contains all the classes used to represent relational operations
 * in the query.
 *
 * These are constructed from the prolog tuples and provide a visitor interface
 * for traversal and translation.
 *
 * @author Luis.
 */

/** Visitor interface for operators. */
interface PrologQueryOperatorVisitor<E> {

    E visit(PrologSelectionOp op);
    E visit(PrologProjectionOp op);
    E visit(PrologJoinOp op);
    E visit(PrologTablescanOp op);
    E visit(PrologSeedOp op);
    E visit(PrologVGWrapperOp op);
    E visit(PrologScalarFunctionOp op);
    E visit(PrologGeneralAggregateOp op);
    E visit(PrologSplitOp op);
    E visit(PrologAntiJoinOp op);
    E visit(PrologSemiJoinOp op);
    E visit(PrologDedupOp op);
    E visit(PrologFrameOutputOp op);
}


/** A general query operator type. */
abstract class PrologQueryOperator {
    enum Type {
	SELECTION, PROJECTION, JOIN, TABLESCAN, SEED, VGWRAPPER, SCALARFUNC, GENAGG, SPLIT, ANTIJOIN, SEMIJOIN, DEDUP, FRAMEOUTPUT
    }
    
    
    // the name, always the first element in the tuple.
    protected String name;
    public String getName() {
	return name;
    }

    // all operators must return their type.
    public abstract PrologQueryOperator.Type getType();

    // and accept a visitor
    public abstract <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor);

    // creates an operator from a prolog tuple.
    public static PrologQueryOperator fromTuple(TupleTerm t, 
					  HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
					  HashMap<AtomTerm, TupleTerm> exprTuples) {

	// use the operator type to decide.
	if (t.getAtom().equals(new AtomTerm("selection"))) {
	    return new PrologSelectionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("projection"))) {
	    return new PrologProjectionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("join"))) {
	    return new PrologJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("tablescan"))) {
	    return new PrologTablescanOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("seed"))) {
	    return new PrologSeedOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("vgwrapper"))) {
	    return new PrologVGWrapperOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("scalarfunc"))) {
	    return new PrologScalarFunctionOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("genagg"))) {
	    return new PrologGeneralAggregateOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("split"))) {
	    return new PrologSplitOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("antijoin"))) {
	    return new PrologAntiJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("semijoin"))) {
	    return new PrologSemiJoinOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("dedup"))) {
	    return new PrologDedupOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	if (t.getAtom().equals(new AtomTerm("frameoutput"))) {
	    return new PrologFrameOutputOp(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	}

	throw new RuntimeException("Unrecognized operator type " + t);
    }
}

/**** ---- ALL THE SPECIFIC TYPES AHEAD ---- ****/

/** Selection operator. */
class PrologSelectionOp extends PrologQueryOperator {

    // selection expressions
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologSelectionOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(2);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.SELECTION;
    }


    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }    

    public String toString() {
	return getType() + " " + exps;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** Projection operator. */
class PrologProjectionOp extends PrologQueryOperator {

    // the set of output attributes
    private ArrayList<PrologAttributeExp> attributes = new ArrayList<PrologAttributeExp>();

    public PrologProjectionOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(2);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the attributes
	attributes = PrologAttributeExp.fromList((ListTerm)t.getTerm(1), attributeTuples, true);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.PROJECTION;
    }

    public ArrayList<PrologAttributeExp> getAttributes() {
	return attributes;
    }

    public void updateAttributes(ArrayList<String> attsIn) {

      // rearrange
      ArrayList<PrologAttributeExp> newAtts = new ArrayList<PrologAttributeExp>();
      for (String att : attsIn) {
	boolean found = false;

	for (PrologAttributeExp attExp : getAttributes()) {
	  if (attExp.getName().equals(att)) {
	    newAtts.add(attExp);
	    found = true;
	    break;
	  }
	}

	if (!found) {
	  throw new RuntimeException("Could not find attribute " + att + " while updating materialized views!");
	}
      }

      // set anew.
      attributes = newAtts;
    }

    public String toString() {
	return getType() + " " + attributes;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** Join operator. */
class PrologJoinOp extends PrologQueryOperator {

    // cartinality types.
    enum Type {
	ONETOONE, MANYTOMANY, MANYTOONE, ONETOMANY, UNKNOWN
    }

    
    // the type
    private PrologJoinOp.Type type;

    // the join expressions.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologJoinOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(3);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);


	// get the type.
	try {
	    type = PrologJoinOp.Type.valueOf(((AtomTerm)t.getTerm(2)).getValue().toUpperCase());
	} catch (Exception e) {

	    // just in case we have one of those nasty underscores...
	    type = PrologJoinOp.Type.UNKNOWN;
	}
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.JOIN;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }    

    public PrologJoinOp.Type getJoinType() {
	return type;
    }

    public String toString() {
	return getType() + " " + exps + " <" + type + ">";
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** Tablescan operator. */
class PrologTablescanOp extends PrologQueryOperator {

    // the set of output attributes
    private PrologQueryRelation relation;
    private ArrayList<PrologAttributeExp> attributes = new ArrayList<PrologAttributeExp>();


    public PrologTablescanOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(3);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the relation
	relation = new PrologQueryRelation(relationTuples.get(t.getTerm(1)), attributeTuples);

	// get the attributes
	attributes = PrologAttributeExp.fromList((ListTerm)t.getTerm(2), attributeTuples, false);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.TABLESCAN;
    }

    public PrologQueryRelation getRelation() {
	return relation;
    }

    public ArrayList<PrologAttributeExp> getAttributes() {
	return attributes;
    }

    public String toString() {
	return getType() + " " + relation + " " + attributes;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** Seed operator. */
class PrologSeedOp extends PrologQueryOperator {

    // the one output attribute it appends.
    private PrologAttributeExp attribute;

    public PrologSeedOp(TupleTerm t, 
			HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(2);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the attribute
	attribute = new PrologAttributeExp(attributeTuples.get(t.getTerm(1)));
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.SEED;
    }
    
    public PrologAttributeExp getAttribute() {
	return attribute;
    }

    public String toString() {
	return getType() + " " + attribute;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** VGWrapper operator. */
class PrologVGWrapperOp extends PrologQueryOperator {

    // name of the VG wrapper (not the same as the node name!)
    private String id;

    // the VG function it calls.
    private PrologQueryVGFunction function;

    // the input seed attribute.
    private PrologAttributeExp inputSeedAtt;

    // the output seed attribute.
    private PrologAttributeExp outputSeedAtt;

    // list of input attributes.
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();

    // list of output attributes.
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();

    public PrologVGWrapperOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// check the arity.
	t.checkArity(9);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the ID
	id = ((AtomTerm)t.getTerm(1)).getValue();

	// get the vg function
	function = new PrologQueryVGFunction(vgTuples.get(t.getTerm(2)), attributeTuples);

	// get the input seed attribute
	inputSeedAtt = new PrologAttributeExp(attributeTuples.get(t.getTerm(3)));
	
	// get the input attributes
	inputAtts = PrologAttributeExp.fromList((ListTerm)t.getTerm(4), attributeTuples, false);
	outputAtts = PrologAttributeExp.fromList((ListTerm)t.getTerm(7), attributeTuples, true);

	// get the output seed attribute
	outputSeedAtt = new PrologAttributeExp(attributeTuples.get(t.getTerm(8)));
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.VGWRAPPER;
    }

    public String getID() {
	return id;
    }

    public PrologQueryVGFunction getFunction() {
	return function;
    }

    public PrologAttributeExp getInputSeed() {
	return inputSeedAtt;
    }

    public PrologAttributeExp getOutputSeed() {
	return outputSeedAtt;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
	return inputAtts;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
	return outputAtts;
    }

    public String toString() {
	return getType() + " " + function + "(" + inputAtts + ") -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** Scalar function operator. */
class PrologScalarFunctionOp extends PrologQueryOperator {

    // expressions, input and output attributes.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();

    public PrologScalarFunctionOp(TupleTerm t, 
				  HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
				  HashMap<AtomTerm, TupleTerm> exprTuples) {


	// check the arity.
	t.checkArity(4);
	
	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the set of expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(1), vgTuples, attributeTuples, relationTuples, exprTuples);

	// get assignment expressions
	while (true) {

	    // identify them by their UNKNOWN atribute.
	    int ix = exps.indexOf(new PrologAttributeExp());
	    if (ix < 0)
		break;

	    // set it.
	    exps.set(ix, PrologAttributeExp.fromList((ListTerm)((ListTerm)t.getTerm(2)).get(ix), attributeTuples, true).get(0));
	}

	// get the set of input attributes
	inputAtts = PrologAttributeExp.fromListOfLists((ListTerm)t.getTerm(2), attributeTuples, true);
	outputAtts = PrologAttributeExp.fromListOfLists((ListTerm)t.getTerm(3), attributeTuples, true);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.SCALARFUNC;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
	return inputAtts;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
	return outputAtts;
    }

    public String toString() {
	return getType() + " " + exps + " " + inputAtts + " -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
    
}

/** A generalized aggregate operator */
class PrologGeneralAggregateOp extends PrologQueryOperator {

    // the aggregate id -- not the same as the node name!
    private String id;

    // grouping attributes.
    private ArrayList<PrologAttributeExp> groupByAtts = new ArrayList<PrologAttributeExp>();

    // aggregate expressions.
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    // all the input attributes.
    private ArrayList<PrologAttributeExp> inputAtts = new ArrayList<PrologAttributeExp>();

    // all the output attributes.
    private ArrayList<PrologAttributeExp> outputAtts = new ArrayList<PrologAttributeExp>();
    
    public PrologGeneralAggregateOp(TupleTerm t, 
				  HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
				  HashMap<AtomTerm, TupleTerm> exprTuples) {

	// check the arity.
	t.checkArity(6);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the ID
	id = ((AtomTerm)t.getTerm(1)).getValue();

	// get the group-by attributes
	groupByAtts = PrologAttributeExp.fromList((ListTerm)t.getTerm(2), attributeTuples, true);

	// get the expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(3), vgTuples, attributeTuples, relationTuples, exprTuples);

	// get the input attributes
	inputAtts = PrologAttributeExp.fromListOfLists((ListTerm)t.getTerm(4), attributeTuples, true);
	outputAtts = PrologAttributeExp.fromListOfLists((ListTerm)t.getTerm(5), attributeTuples, true);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.GENAGG;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }

    public ArrayList<PrologAttributeExp> getInputAtts() {
	return inputAtts;
    }

    public ArrayList<PrologAttributeExp> getOutputAtts() {
	return outputAtts;
    }

    public ArrayList<PrologAttributeExp> getGroupByAtts() {
	return groupByAtts;
    }

    public String getID() {
	return id;
    }

    public String toString() {
	return getType() + " " + exps + " BY " + groupByAtts + "; " + inputAtts + " -> " + outputAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** The split operator. */
class PrologSplitOp extends PrologQueryOperator {

    // the list of attributes to split with
    private ArrayList<PrologAttributeExp> splitAtts = new ArrayList<PrologAttributeExp>();

    // simple constructor.
    public PrologSplitOp(TupleTerm t, 
			 HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			 HashMap<AtomTerm, TupleTerm> exprTuples) {

	// check the arity.
	t.checkArity(2);

	// get the name
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the attributes
	splitAtts = PrologAttributeExp.fromList((ListTerm)t.getTerm(1), attributeTuples, true);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.SPLIT;
    }

    public ArrayList<PrologAttributeExp> getSplitAtts() {
	return splitAtts;
    }

    public String toString() {
	return getType() + " " + splitAtts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** The anti-join operator. */
class PrologAntiJoinOp extends PrologQueryOperator {
   
    // the source operator, only the node name -- users must look it up.
    private String sourceName;

    // join predicates
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologAntiJoinOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(3);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the source
	sourceName = ((AtomTerm)t.getTerm(1)).getValue();
	
	// get the expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(2), vgTuples, attributeTuples, relationTuples, exprTuples);
    }
    
    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.ANTIJOIN;
    }

    public String getSource() {
	return sourceName;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }

    public String toString() {
	return getType() + " " + sourceName + " " + exps;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }

}

/** The semi-join operator. */
class PrologSemiJoinOp extends PrologQueryOperator {

    // the source operator, only the node name -- users must look it up.
    private String sourceName;

    // join predicates
    private ArrayList<PrologQueryExpression> exps = new ArrayList<PrologQueryExpression>();

    public PrologSemiJoinOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(3);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();

	// get the source
	sourceName = ((AtomTerm)t.getTerm(1)).getValue();
	
	// get the expressions
	exps = PrologQueryExpression.fromListOfTuples((ListTerm)t.getTerm(2), vgTuples, attributeTuples, relationTuples, exprTuples);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.SEMIJOIN;
    }

    public String getSource() {
	return sourceName;
    }

    public ArrayList<PrologQueryExpression> getExpressions() {
	return exps;
    }

    public String toString() {
	return getType() + " " + sourceName + " " + exps;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** The duplicate removal operator. */
class PrologDedupOp extends PrologQueryOperator {

    // set of attributes
    private ArrayList<PrologAttributeExp> atts = new ArrayList<PrologAttributeExp>();

    public PrologDedupOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(2);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the attributes
	atts = PrologAttributeExp.fromList((ListTerm)t.getTerm(1), attributeTuples, true);
    }

    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.DEDUP;
    }

    public ArrayList<PrologAttributeExp> getAtts() {
	return atts;
    }

    public String toString() {
	return getType() + " " + atts;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}

/** The SimSQL frame output operator. */
class PrologFrameOutputOp extends PrologQueryOperator {

    // set of operators to keep, only the node names.
    private ArrayList<String> sourceNames = new ArrayList<String>();

    // set of output files for those operators.
    private ArrayList<String> outputFiles = new ArrayList<String>();

    public PrologFrameOutputOp(TupleTerm t, 
			     HashMap<AtomTerm, TupleTerm> vgTuples, HashMap<AtomTerm, TupleTerm> attributeTuples, HashMap<AtomTerm, TupleTerm> relationTuples,
			     HashMap<AtomTerm, TupleTerm> exprTuples) {

	// verify the arity
	t.checkArity(3);

	// get the name.
	name = ((AtomTerm)t.getTerm(0)).getValue();
	
	// get the set of sources
	for (PrologTerm tt: (ListTerm)t.getTerm(1)) {
	    sourceNames.add(((AtomTerm)tt).getValue());
	}

	// get the set of output files
	for (PrologTerm tt: (ListTerm)t.getTerm(2)) {
	    outputFiles.add(((AtomTerm)tt).getValue());
	}

    }
    
    public PrologQueryOperator.Type getType() {
	return PrologQueryOperator.Type.FRAMEOUTPUT;
    }

    public ArrayList<String> getSources() {
	return sourceNames;
    }

    public ArrayList<String> getOutputFiles() {
	return outputFiles;
    }

    public String toString() {
	return getType() + " " + sourceNames + " -> " + outputFiles;
    }

    public <E> E acceptVisitor(PrologQueryOperatorVisitor<E> visitor) {
	return visitor.visit(this);
    }
}
