

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

import org.antlr.runtime.*;

/** Translates a query into an object of type E. */
interface PrologQueryTranslator<E> {

    E translate(PrologQuery q);
}

/*
 * Contains a query, with its operators, graph, statistics and
 * constants.
 *
 * @author Luis
 */
public class PrologQuery {

    // all the operators.
    private HashMap<String, PrologQueryOperator> operators = new HashMap<String, PrologQueryOperator>();

    // all the statistics.
    private ArrayList<PrologQueryEstimate> estimates = new ArrayList<PrologQueryEstimate>();

    // all the constants.
    private HashMap<String, String> constants = new HashMap<String, String>();

    // the graph, each node with its list of children.
    private HashMap<String, ArrayList<String>> graph = new HashMap<String, ArrayList<String>>();

    // the reverse graph, each node with its list of parents.
    private HashMap<String, ArrayList<String>> reverseGraph = new HashMap<String, ArrayList<String>>();

    // the root of the graph
    private String graphRoot = null;

    // the leaves of the graph
    private ArrayList<String> graphLeaves = new ArrayList<String>();

    // a topological order of the graph
    private ArrayList<String> topologicalOrder = new ArrayList<String>();

    // set of output attributes for each operator
    private HashMap<String, ArrayList<String>> outputAtts = new HashMap<String, ArrayList<String>>();
    
    // outer relations of VGWrapper operators
    private HashMap<String, String> outerRelations = new HashMap<String, String>();

    // all the random attributes in the query
    private HashSet<String> randomAtts = new HashSet<String>();

    // string representation for the attribute types
    private HashMap<String, String> attTypeStr = new HashMap<String, String>();

    // reads a query from a long string.
    public PrologQuery(String toParse) {

	// all the terms as read from the parser go here...
	ArrayList<TupleTerm> terms = null;

	try {      
	    // then parse the string and get a set of tuples.
	    ANTLRStringStream parserIn = new ANTLRStringStream (toParse);
	    PrologQueryLexer lexer = new PrologQueryLexer (parserIn);
	    CommonTokenStream tokens = new CommonTokenStream (lexer);
	    PrologQueryParser parser = new PrologQueryParser (tokens);
	    terms = parser.query();
	} catch (Exception e) {
	    throw new RuntimeException("Could not parse query.", e);
	}

	// check if we got something back...
	if (terms == null || terms.size() == 0) {
	    throw new RuntimeException("Could not parse query.");
	}

	// we will put all the terms into a set of temporary hash
	// tables, organized by their types, hashed with their IDs as
	// given by the parser.
	HashMap<AtomTerm, TupleTerm> vgTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> attributeTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> relationTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> parentTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> exprTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> opTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> statTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> constTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> outputAttsTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> randomAttsTuples = new HashMap<AtomTerm, TupleTerm>();
	HashMap<AtomTerm, TupleTerm> outerRelTuples = new HashMap<AtomTerm, TupleTerm>();

	// this one here will be a map of maps, mapping the atom
	// string type to its appropriate hash map
	HashMap<AtomTerm, HashMap<AtomTerm, TupleTerm>> termTypes = new HashMap<AtomTerm, HashMap<AtomTerm, TupleTerm>>();
	termTypes.put(new AtomTerm("vgfunction"), vgTuples);
	termTypes.put(new AtomTerm("attributeType"), attributeTuples);
	termTypes.put(new AtomTerm("relation"), relationTuples);
	termTypes.put(new AtomTerm("temporaryTable"), relationTuples);
	termTypes.put(new AtomTerm("parent"), parentTuples);
	termTypes.put(new AtomTerm("compExp"), exprTuples);
	termTypes.put(new AtomTerm("boolOr"), exprTuples);
	termTypes.put(new AtomTerm("boolNot"), exprTuples);
	termTypes.put(new AtomTerm("boolAnd"), exprTuples);
	termTypes.put(new AtomTerm("set"), exprTuples);
	termTypes.put(new AtomTerm("arithExp"), exprTuples);
	termTypes.put(new AtomTerm("verbatim"), exprTuples);
	termTypes.put(new AtomTerm("function"), exprTuples);
	termTypes.put(new AtomTerm("aggExp"), exprTuples);
	termTypes.put(new AtomTerm("selection"), opTuples);
	termTypes.put(new AtomTerm("tablescan"), opTuples);
	termTypes.put(new AtomTerm("seed"), opTuples);
	termTypes.put(new AtomTerm("split"), opTuples);
	termTypes.put(new AtomTerm("join"), opTuples);
	termTypes.put(new AtomTerm("antijoin"), opTuples);
	termTypes.put(new AtomTerm("semijoin"), opTuples);
	termTypes.put(new AtomTerm("dedup"), opTuples);
	termTypes.put(new AtomTerm("projection"), opTuples);
	termTypes.put(new AtomTerm("genagg"), opTuples);
	termTypes.put(new AtomTerm("vgwrapper"), opTuples);
	termTypes.put(new AtomTerm("scalarfunc"), opTuples);
	termTypes.put(new AtomTerm("frameoutput"), opTuples);
	termTypes.put(new AtomTerm("monteCarloIterations"), constTuples);
	termTypes.put(new AtomTerm("materializeNodes"), constTuples);
	termTypes.put(new AtomTerm("operatorOutputEstimate"), statTuples);
	termTypes.put(new AtomTerm("operatorInputEstimate"), statTuples);
	termTypes.put(new AtomTerm("outputAttributes"), outputAttsTuples);
	termTypes.put(new AtomTerm("randomAttributes"), randomAttsTuples);
	termTypes.put(new AtomTerm("outerRelation"), outerRelTuples);

	// add some constant expression names.
	exprTuples.put(new AtomTerm("*"), new TupleTerm(new AtomTerm("__const"), new ArrayList<PrologTerm>(Arrays.asList(new AtomTerm("star")))));
	exprTuples.put(new AtomTerm("-"), new TupleTerm(new AtomTerm("__const"), new ArrayList<PrologTerm>(Arrays.asList(new AtomTerm("minus")))));

	// fill all those hashmaps
	for (TupleTerm t: terms) {

	    HashMap<AtomTerm, TupleTerm> tt = termTypes.get(t.getAtom());

	    // we convert things like constants, statistics and graph relations right away.
	    if (tt == constTuples) {
		constants.put(t.getAtom().toString(), t.getTerm(0).toString());
	    }
	    else if (tt == statTuples) {
		estimates.add(new PrologQueryEstimate(t));
	    }
	    else if (tt == parentTuples) {

		String from = ((AtomTerm)t.getTerm(0)).toString();
		String to = ((AtomTerm)t.getTerm(1)).toString();

		if (!(graph.containsKey(from))) {
		    graph.put(from, new ArrayList<String>());
		}

		graph.get(from).add(to);
	    }

	    // the rest are put where they belong.
	    else if (tt != null)
		tt.put(((AtomTerm)t.getTerm(0)), t);
	}

	// now that we have everything organized, we convert all the
	// operators.
	for (TupleTerm t: opTuples.values()) {

	    PrologQueryOperator op = PrologQueryOperator.fromTuple(t, vgTuples, attributeTuples, relationTuples, exprTuples);
	    operators.put(op.getName(), op);
	}

	// and the outputAttributes tuples.
	for (AtomTerm t: outputAttsTuples.keySet()) {

	    ArrayList<String> at = new ArrayList<String>();
	    for (PrologTerm tt: (ListTerm)outputAttsTuples.get(t).getTerm(1)) {
		at.add(tt.toString());
	    }

	    outputAtts.put(t.getValue(), at);
	}

	// and the randomAttributes tuples
	for (AtomTerm t: randomAttsTuples.keySet()) {
	    
	    for (PrologTerm tt: (ListTerm)randomAttsTuples.get(t).getTerm(1)) {
	        randomAtts.add(tt.toString());
	    }
	}

	// and the outerRelations tuples.
	for (AtomTerm t: outerRelTuples.keySet()) {
	    outerRelations.put(t.getValue(), ((AtomTerm)outerRelTuples.get(t).getTerm(1)).getValue());
	}

	// and those attribute strings
	for (AtomTerm t: attributeTuples.keySet()) {
	    attTypeStr.put(t.getValue(), ((AtomTerm)attributeTuples.get(t).getTerm(1)).getValue());
	}

	// it's time to check the properties of the graph.
	// first, all the operators referenced exist
	for (String s: graph.keySet()) {

	    if (!operators.keySet().contains(s)) {
		throw new RuntimeException("Operator " + s + " appears in the graph but not in the query.");
	    }

	    for (String t: graph.get(s)) {
		if (!operators.keySet().contains(t)) {
		    throw new RuntimeException("Operator " + t + " appears in the graph but not in the query.");
		}
	    }
	}

	// now, we will create a reverse of the graph and obtain the roots and leaves.
	HashSet<String> roots = new HashSet<String>();
	HashSet<String> leaves = new HashSet<String>();
	roots.addAll(graph.keySet());

	for (String s: graph.keySet()) {

	    for (String t: graph.get(s)) {

		// children now point to their parents...
		if (!reverseGraph.containsKey(t)) {
		    reverseGraph.put(t, new ArrayList<String>());
		}

		reverseGraph.get(t).add(s);
		
		// we also calculate the roots and the leaves.
		roots.remove(t);
		leaves.add(t);
	    }
	}

	leaves.removeAll(graph.keySet());

	// we check that there is only one root.
	if (roots.size() > 1) {
	    throw new RuntimeException("Query graph contains more than one root: " + roots);
	}

	// and we get it, with the leaves.
	graphRoot = (String) (roots.toArray()[0]);
	graphLeaves.addAll(leaves);

	// finally, we will obtain a topological order -- this will
	// also check if there are any cycles in the graph.

	// our working set of nodes... we start at the leaves.
	LinkedList<String> S = new LinkedList<String>(leaves);

	// a copy of the reverse graph
	HashMap<String, ArrayList<String>> G = new HashMap<String, ArrayList<String>>(reverseGraph);
	
	// loop over...
	while (!S.isEmpty()) {

	    // get a node from S.
	    String n = S.poll();

	    // put it into the order.
	    topologicalOrder.add(n);

	    // get its outgoing edges.
	    if (G.get(n) != null) {

		ArrayList<String> gn = new ArrayList<String>(G.get(n));
		G.put(n, new ArrayList<String>(gn));

		// now, check which of those nodes are free.
		for (String m: gn) {

		    // remove from the graph...
		    G.get(n).remove(m);

		    boolean isThere = false;
		    for (ArrayList<String> ss: G.values()) {
			isThere |= ss.contains(m);
		    }

		    // is it free? then get it out
		    if (!isThere) {
			S.push(m);
		    }
		}
	    }

	}

	// check if any edges remain...
	for (ArrayList<String> s: G.values()) {
	    if (s.size() > 0)
		throw new RuntimeException("Query graph has at least one cycle.");
	}
    }


    // checks if an operator is present.
    public boolean isOperator(String name) {
	return operators.containsKey(name);
    }

    // gets an operator.
    public PrologQueryOperator getOperator(String name) {
	
	PrologQueryOperator op = operators.get(name);
	if (op == null)
	    throw new RuntimeException("Unknown query operator " + name);

	return op;
    }

    // gets the entire set of statistics
    public ArrayList<PrologQueryEstimate> getEstimates() {
	return new ArrayList<PrologQueryEstimate>(estimates);
    }

    // returns true if the constant is there.
    public boolean isConstant(String name) {
	return constants.containsKey(name);
    }

    // returns a given constant.
    public String getConstant(String name) {

	String con = constants.get(name);
	if (con == null)
	    throw new RuntimeException("Unknown query constant " + name);

	return con;
    }

    // returns the children of a given operator
    public ArrayList<String> getChildren(String op) {

	ArrayList<String> out = new ArrayList<String>();
	if (graph.containsKey(op))
	    out.addAll(graph.get(op));

	return out;
    }

    // returns the children of a given operator (op version)
    public ArrayList<PrologQueryOperator> getChildren(PrologQueryOperator op) {
	
	ArrayList<PrologQueryOperator> out = new ArrayList<PrologQueryOperator>();
	for (String s: getChildren(op.getName())) {
	    out.add(getOperator(s));
	}

	return out;
    }

    // returns the parents of a given operator 
    public ArrayList<String> getParents(String op) {

	ArrayList<String> out = new ArrayList<String>();
	if (reverseGraph.containsKey(op)) {
	    out.addAll(reverseGraph.get(op));
	}

	return out;
    }

    // returns the parents of a given operator (op version)
    public ArrayList<PrologQueryOperator> getParents(PrologQueryOperator op) {

	ArrayList<PrologQueryOperator> out = new ArrayList<PrologQueryOperator>();
	for (String s: getParents(op.getName())) {
	    out.add(getOperator(s));
	}

	return out;
    }

    // returns the root op of the graph
    public PrologQueryOperator getRoot() {
	return getOperator(graphRoot);
    }

    // returns the leaves of the graph
    public ArrayList<PrologQueryOperator> getLeaves() {

	ArrayList<PrologQueryOperator> out = new ArrayList<PrologQueryOperator>();
	for (String s: graphLeaves) {
	    out.add(getOperator(s));
	}

	return out;
    }

    // returns the topological order of the graph
    public ArrayList<PrologQueryOperator> getTopologicalOrder() {


	ArrayList<PrologQueryOperator> out = new ArrayList<PrologQueryOperator>();
	for (String s: topologicalOrder) {
	    out.add(getOperator(s));
	}

	return out;
    }

    // returns a set of output attributes
    public ArrayList<String> getOutputAtts(String op) {
	ArrayList<String> out = new ArrayList<String>();
	
	if (outputAtts.containsKey(op)) {
	    out.addAll(outputAtts.get(op));
	}

	return out;
    }

    // returns the outer relation of a given node
    public String getOuterRelation(String op) {
	String out = "";

	if (outerRelations.containsKey(op))
	    out = outerRelations.get(op);

	return out;
    }
  
    // returns true if a given attribute is random
    public boolean isAttributeRandom(String attName) {
        return randomAtts.contains(attName);
    }

    // returns a string with the type of an attribute
    public String getAttributeTypeString(String attName) {
        if (!attTypeStr.containsKey(attName))
	  return "unknown";

        return attTypeStr.get(attName);
    }
}

