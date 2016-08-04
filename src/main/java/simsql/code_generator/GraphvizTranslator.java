

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
 * Translates a query into a GraphViz program, for visualization.
 * Also serves as an example of a query translator...
 *
 * @author Luis.
 */
public class GraphvizTranslator implements PrologQueryTranslator<String> {

    // main translation method
    public String translate(PrologQuery q) {

	OpVisitor vis = new OpVisitor();

	String out = "strict digraph {\nnode [fontname=\"Helvetica\"];\n";

	// get the topological order and write them all out
	for (PrologQueryOperator op: q.getTopologicalOrder()) {
	    

	    out += op.acceptVisitor(vis) + "\n";
	}

	// make the materialized nodes red.
	if (q.isConstant("materializeNodes")) {

	    String s = q.getConstant("materializeNodes");
	    while (true) {

		int m = s.indexOf("node");
		int k = s.indexOf("]");
		if (m < 0 || k < 0)
		    break;

		out += s.substring(m,k) + " [style=filled,fillcolor=red];\n";
		s = s.substring(k+1);
	    }
	}
	 
	out += "\n";

	// now, we'll write the graph
	for (PrologQueryOperator op: q.getTopologicalOrder()) {
	    
	    for (String s: q.getChildren(op.getName())) {

		out += op.getName() + " -> " + s + ";\n";
	    }
	}
	
	return out + "}\n";
    }

    // visitor for expressions
    private class ExpVisitor implements PrologQueryExpressionVisitor<String> {

	public String visit(PrologAttributeExp exp) {

	    String s = exp.getName();
	    if (exp.getAttType().equals("unknown"))
		s = "*";

	    return "<font face=\"Courier\">" + s + "</font>";
	}

	public String visit(PrologLiteralExp exp) {

	    return "<font face=\"Courier\">" + exp.getValue() + "</font>";
	}

	public String visit(PrologCompExp exp) {

	    switch(exp.getOp()) {
	    case EQUALS:
		return "(" + exp.getLeft().acceptVisitor(this) + " = " + exp.getRight().acceptVisitor(this) + ")";
	    case NOTEQUALS:
		return "(" + exp.getLeft().acceptVisitor(this) + " &ne; " + exp.getRight().acceptVisitor(this) + ")";
	    case LESSTHAN: 
		return "(" + exp.getLeft().acceptVisitor(this) + " &amp;lt; " + exp.getRight().acceptVisitor(this) + ")";
	    case GREATERTHAN:
		return "(" + exp.getLeft().acceptVisitor(this) + " &amp;gt; " + exp.getRight().acceptVisitor(this) + ")";
	    case LESSEQUALS:
		return "(" + exp.getLeft().acceptVisitor(this) + " &le; " + exp.getRight().acceptVisitor(this) + ")";
	    case GREATEREQUALS:
		return "(" + exp.getLeft().acceptVisitor(this) + " &ge; " + exp.getRight().acceptVisitor(this) + ")";
	    }

	    return "";
	}

	public String visit(PrologBoolExp exp) {
	    
	    String op = "";
	    String out = "";
	    String end = "";

	    switch(exp.getBoolType()) {
	    case NOT:
		out = "&not;";
		break;
	    case SET:
		op = ", ";
		out = "&lang; ";
		end = " &rang; ";

	    case AND:
		op = " &and; ";
		out = "(";
		end = ")";
		break;
		
	    case OR:		
		op = " &or; ";
		out = "(";
		end = ")";
		break;
	    }

	    out += exp.getChildren().get(0).acceptVisitor(this);
	    for (int i=1;i<exp.getChildren().size();i++) {
		out += op + exp.getChildren().get(i).acceptVisitor(this);
	    }

	    return out + end;
	}

	public String visit(PrologAggExp exp) {

	    return exp.getAggType().toString() + "(" + exp.getExp().acceptVisitor(this)  + ")";
	}

	public String visit(PrologArithExp exp) {

	    String op = "";
	    switch(exp.getOp()) {
	    case PLUS:
		op = " + ";
		break;
	    case MINUS:
		op = " - ";
		break;

	    case TIMES:
		op = " * ";
		break;
	    case DIVIDE:
		op = " / ";
		break;
	    }

	    return "(" + exp.getLeft().acceptVisitor(this) + op + exp.getRight().acceptVisitor(this) + ")";
	}

	public String visit(PrologFunctionExp exp) {

	    if (exp.getParams().size() == 0) {
		return exp.getFunction() + "()";
	    }

	    String out = exp.getParams().get(0).acceptVisitor(this);
	    for (int i=1;i<exp.getParams().size();i++) {
		out += ", " + exp.getParams().get(i);
	    }

	    return exp.getFunction() + "(" + out + ")";
	}
    }

    // visitor for operators.
    private class OpVisitor implements PrologQueryOperatorVisitor<String> {

	private ExpVisitor vis = new ExpVisitor();

	public String visit(PrologSelectionOp op) {
	    
	    String out = op.getName() + " [shape=box,label=<&sigma;<sub>" + op.getExpressions().get(0).acceptVisitor(vis);

	    for (int i=1;i<op.getExpressions().size();i++) {
		out += " &and; " + op.getExpressions().get(i).acceptVisitor(vis);
	    }

	    return out + "</sub>>];";
	}

	public String visit(PrologProjectionOp op) {

	    String out = op.getName() + " [shape=box,label=<&pi;<sub>" + op.getAttributes().get(0).acceptVisitor(vis);
	    
	    for (int i=1;i<op.getAttributes().size();i++) {
		out += ", " + op.getAttributes().get(i).acceptVisitor(vis);
	    }

	    return out + "</sub>>];";
	}

	public String visit(PrologJoinOp op) {

	    if (op.getExpressions().size() == 0) {
		return op.getName() + " [shape=box,label=<&times;>];";
	    }
	       
	    String out = op.getName() + " [shape=box,label=<&#8904;<sub>"; 
	    
	    out += op.getExpressions().get(0).acceptVisitor(vis);
	    
	    for (int i=1;i<op.getExpressions().size();i++) {
		out += " &and; " + op.getExpressions().get(i).acceptVisitor(vis);
	    }
	    
	    return out + "</sub>>];";
	}

	public String visit(PrologTablescanOp op) {
	    return op.getName() + " [shape=box,label=<<font face=\"Courier\">" + op.getRelation().getName() + "</font>>];";
	}

	public String visit(PrologSeedOp op) {
	    return op.getName() + " [shape=box,label=<<sub>" + op.getAttribute().acceptVisitor(vis) + " &larr; seed</sub>>];";
	}
	
	public String visit(PrologVGWrapperOp op) {
	    String out = op.getName() + " [shape=box,label=<<b>VG</b><br/><sub>" + "&lang;" + op.getOutputAtts().get(0).acceptVisitor(vis);
	    
	    for (int i=1;i<op.getOutputAtts().size();i++) {
		out += "," + op.getOutputAtts().get(i).acceptVisitor(vis);
	    }

	    out += "&rang; &larr; " + op.getFunction().getName() + "(" + op.getInputAtts().get(0).acceptVisitor(vis);
	    
	    for (int i=1;i<op.getInputAtts().size();i++) {
		out += "," + op.getInputAtts().get(i).acceptVisitor(vis);
	    }

	    return out + ")</sub>>];";
	}

	public String visit(PrologScalarFunctionOp op) {
	    String out = op.getName() + " [shape=box,label=<<sub>";

	    out += op.getOutputAtts().get(0).acceptVisitor(vis) + " &larr; " + op.getExpressions().get(0).acceptVisitor(vis);
	    for (int i=1;i<op.getOutputAtts().size();i++) {
		out += "<br/>" + op.getOutputAtts().get(i).acceptVisitor(vis) + " &larr; " + op.getExpressions().get(0).acceptVisitor(vis);
	    }

	    return out + "</sub>>];";
	}

	public String visit(PrologGeneralAggregateOp op) {

	    String out = op.getName() + " [shape=box,label=<&Sigma;";
	    
	    if (op.getGroupByAtts().size() > 0) {
		out += "<sub>&lang;" + op.getGroupByAtts().get(0).acceptVisitor(vis);

		for (int i=1;i<op.getGroupByAtts().size();i++) {
		    out += ", " + op.getGroupByAtts().get(i).acceptVisitor(vis);
		}
		out += "&rang;</sub><br/>";
	    }

	    for (int i=0;i<op.getOutputAtts().size();i++) {
		out += "<br/>" + "<sub>" + op.getOutputAtts().get(i).acceptVisitor(vis) + " &larr; " + op.getExpressions().get(i).acceptVisitor(vis) + "</sub>";
	    }

	    return out + ">];";
	}

	public String visit(PrologSplitOp op) {
	    String out = op.getName() + " [shape=box,label=<<sub>split(" + op.getSplitAtts().get(0).acceptVisitor(vis);

	    for (int i=1;i<op.getSplitAtts().size();i++) {
		out += ", " + op.getSplitAtts().get(i).acceptVisitor(vis);
	    }

	    return out + ")</sub>>];";
	}

	public String visit(PrologAntiJoinOp op) {
	    String out = op.getName() + " [shape=box,label=<&#8883;<sub>" + op.getExpressions().get(0).acceptVisitor(vis);

	    for (int i=1;i<op.getExpressions().size();i++) {
		out += " &and; " + op.getExpressions().get(i);
	    }

	    return out + "</sub>>];\n " + op.getName() + " -> " + op.getSource() + "[color=red]";
	}

	public String visit(PrologSemiJoinOp op) {
	    String out = op.getName() + " [shape=box,label=<&#8905;<sub>" + op.getExpressions().get(0).acceptVisitor(vis);

	    for (int i=1;i<op.getExpressions().size();i++) {
		out += " &and; " + op.getExpressions().get(i);
	    }

	    return out + "</sub>>];\n " + op.getName() + " -> " + op.getSource() + "[color=red]";
	}

	public String visit(PrologDedupOp op) {
	    return op.getName() + " [shape=box,label=<dedup>];";
	}

	public String visit(PrologFrameOutputOp op) {
	    String out = op.getName() + " [label=<out>];\n";

	    // make all the final guys reddish
	    for (String s: op.getSources()) {
		out += s + " [style=filled,fillcolor=red];\n";
	    }

	    return out;
	}
	
    }
}
