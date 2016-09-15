

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

import simsql.code_generator.DataFlowQuery;
import simsql.code_generator.MyPhysicalDatabase;
import simsql.code_generator.translator.expressions.*;
import simsql.code_generator.translator.operators.*;

import java.util.*;
import java.io.*;

/**
 * Translates a query into a SimSQL DataFlow program.
 *
 * @author Luis.
 */

public class DataFlowTranslator implements PrologQueryTranslator<DataFlowQuery> {

    private MyPhysicalDatabase db = null;

    public DataFlowTranslator(MyPhysicalDatabase dbIN) {
        db = dbIN;
    }

    public DataFlowQuery translate(PrologQuery q) {

        // get a visitor for operators.
        OpToBlockVis vis = new OpToBlockVis(q, db);

        // make a block from each operator.
        LinkedHashMap<String, BlockNode> blocks = new LinkedHashMap<String, BlockNode>();
        LinkedHashSet<String> blockNames = new LinkedHashSet<String>();

        for (PrologQueryOperator op : q.getTopologicalOrder()) {
            BlockNode bx = op.acceptVisitor(vis);
            blocks.put(op.getName(), bx);
            blockNames.add(bx.getName());
        }

        // before connecting the blocks, we will use the final node information
        // to set the output file names.
        for (String s : vis.getFinalNodes().keySet()) {
            blocks.get(s).setFinal(db.getTypeCode(vis.getFinalNodes().get(s)), db.getFileName(vis.getFinalNodes().get(s)));
        }

        // connect the blocks  with their descendants
        for (String s : blocks.keySet()) {
            BlockNode bx = blocks.get(s);
            for (String t : q.getChildren(s)) {
                bx.connectChild(blocks.get(t));
            }
        }

        // iteratively merge all possible blocks.
        int numMergers = 0;
        do {

            numMergers = 0;

            // proceed in topological order.
            for (PrologQueryOperator op : q.getTopologicalOrder()) {

                // get the current block.
                BlockNode bx = blocks.get(op.getName());

                // get its parent node names
                ArrayList<String> parents = q.getParents(op.getName());

                // make sure that it has only one parent.
                if (parents.size() == 1) {

                    // get the parent block.
                    BlockNode px = blocks.get(parents.get(0));

                    // attempt to merge them.
                    BlockNode nx = bx.mergeParent(px);

                    // were we succesful?
                    if (nx != null) {

                        // if so, associate the new block with its old names.
                        // this will replace them in the map.
                        for (String name : nx.getPlanNodes()) {
                            blocks.put(name, nx);
                        }

                        // update the number of mergers...
                        numMergers++;
                    }
                }
            }

            // repeat until we have no more mergers
        } while (numMergers > 0);


        // print the unique output blocks, in their topological order
        String outStr = "";
        DataFlowQuery dfq = new DataFlowQuery();
        LinkedHashSet<String> seen = new LinkedHashSet<String>();
        for (PrologQueryOperator op : q.getTopologicalOrder()) {

            BlockNode bx = blocks.get(op.getName());
            outStr += "# " + op.getName() + " --> " + bx.getName() + "\n";
            if (!seen.containsAll(bx.getPlanNodes())) {
                seen.addAll(bx.getPlanNodes());

                outStr += "##### " + bx.getName() + " " + bx.getPlanNodes() + "\n";
                outStr += bx + "\n";
                blockNames.remove(bx.getName());

                boolean isFinalBX = false;
                for (String s : bx.getPlanNodes()) {
                    if (vis.getFinalNodes().containsKey(s)) {
                        isFinalBX = true;
                    }

                    for (String u : q.getChildren(s)) {
                        dfq.addGraphEdge(s, u);
                    }
                }

                dfq.addGraphNode(bx.getOutFile(), bx.getType().toString(), new ArrayList<String>(bx.getPlanNodes()), isFinalBX);
            }
        }

        // print the temporary tables
        outStr += vis.getTempTables() + "\n";

        // add the final node information.
        for (String sk : vis.getFinalNodes().keySet()) {

            // add the path
            dfq.setFinalPath(vis.getFinalNodes().get(sk), db.getFileName(vis.getFinalNodes().get(sk)));

            // add the final attributes
            dfq.setFinalAttributes(vis.getFinalNodes().get(sk), blocks.get(sk).getOutAtts());

            // and their type/randomness
            for (String tx : blocks.get(sk).getOutAtts()) {
                dfq.setFinalType(tx, q.getAttributeTypeString(tx));
                dfq.setFinalRandomAttribute(tx, q.isAttributeRandom(tx));
            }
        }

        // de-register the block names that were merged
        for (String t : blockNames) {
            db.unregisterTable(t);
        }

        // set the query string.
        dfq.setQueryStr(outStr);

        // and go back.
        return dfq;
    }

    // same for the blocks.
    public static short block = 1;

    // names for aggregate expressions
    private static int aggI = 1;

    // names for vgwrapper inner inputs
    private static int innerI = 1;

    // --- CLASSES FOR BLOCK NODES, I.E. DATAFLOW OPERATIONS ---
    private static abstract class BlockNode {

        // types of block nodes.
        static enum Type {
            SELECT, VGWRAPPER, VGINPUT, AGGREGATE, JOIN, INFERENCE, FRAMEOUTPUT
        }

        // set of nodes from the original plan that are spanned by this block.
        protected LinkedHashSet<String> planNodes = new LinkedHashSet<String>();

        // block node name
        protected String name;

        // output files -- should be created automatically using the node name, except for finals.
        protected boolean hasOutFile = false;
        protected String outFN = "";
        protected boolean isFinal = false;

        // type codes
        protected short typeCodeIn = 0;
        protected short typeCodeOut = 0;

        // outer relation flag
        protected boolean outerRelation = false;

        // set of output attributes and their expressions.
        protected LinkedHashMap<String, String> outAtts = new LinkedHashMap<String, String>();

        // set of output attributes and their required input attributes.
        protected LinkedHashMap<String, LinkedHashSet<String>> outAttsReq = new LinkedHashMap<String, LinkedHashSet<String>>();

        // set of projections -- this is carried temporarily and used to build outAtts
        protected LinkedHashSet<String> projections = new LinkedHashSet<String>();

        // simple constructor using a general OP
        public BlockNode(PrologQueryOperator op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {

            // give it a name and register it (if possibiru).
            isFinal = false;
            name = "simsql_query_block" + DataFlowTranslator.block++;

            if (db != null) {
                db.registerTable(name);

                // give it an output typeCode
                typeCodeOut = db.getTypeCode(name);
                hasOutFile = true;
                outFN = db.getFileName(name);
            } else {
                typeCodeOut = -1;
            }

            // add the involved node to the list of plan nodes associated
            if (op != null) {
                planNodes.add(op.getName());
            }

            // add the projections
            if (outputAtts != null) {
                projections.addAll(outputAtts);
            }
        }

        // return its type
        public abstract BlockNode.Type getType();

        // returns the set of attributes that must be in the input section
        // of a given operator.
        public abstract LinkedHashSet<String> getNecessaryAtts();

        // returns a new block node by merging.
        // returns null if they cannot be merged.
        public abstract BlockNode mergeParent(BlockNode me);

        // sets the input of this current node as the output of the parameter.
        public abstract void connectChild(BlockNode me);

        // returns a DataFlow representation of this block node.
        public abstract String toString();

        // returns the list of nodes from the original plan that are "contained" in this block.
        public LinkedHashSet<String> getPlanNodes() {
            return planNodes;
        }

        // returns the name of the operator
        public String getName() {
            return name;
        }

        // set stuff for the finals...
        public void setFinal(short tc, String of) {
            typeCodeOut = tc;
            hasOutFile = true;
            isFinal = true;
            outFN = of;
        }

        // returns an array with the output attributes...
        public ArrayList<String> getOutAtts() {
            return new ArrayList<String>(outAtts.keySet());
        }

        // returns the output file name of the operator
        public String getOutFile() {
            return hasOutFile ? outFN : "output_" + name + ".tbl";
        }

        // utility -- used to replace mappings in a template file
        public static String replaceTemplateMappings(String inputFile, HashMap<String, String> replacements) {

            // get the parent Jar or directory
            // read the file into a string.
            String toParse = "";
            try {
                InputStream reader = BlockNode.class.getResourceAsStream(inputFile);
                StringBuilder builder = new StringBuilder();
                while (reader.available() > 0) {
                    builder.append((char) reader.read());
                }

                toParse = builder.toString();
            } catch (Exception e) {
                throw new RuntimeException("Could not read template file " + inputFile, e);
            }

            for (String s : replacements.keySet()) {
                toParse = toParse.replaceAll("<<<<" + s + ">>>>", replacements.get(s));
            }

            return toParse;
        }

        // utility -- create a string from a set of separated values form a set.
        public static String fromSet(Set<String> set, String separator, String opener, String closer, String ifEmpty) {

            String[] onx = set.toArray(new String[0]);

            // quick check.
            if (onx.length == 0)
                return ifEmpty;

            String ons = opener + onx[0] + closer;
            for (int i = 1; i < onx.length; i++) {
                ons += separator + opener + onx[i] + closer;
            }

            return ons;
        }

        // utility -- create a string for the outAtts
        public static String fromAssignmentSet(LinkedHashMap<String, String> set) {
            String[] onx = set.keySet().toArray(new String[0]);

            if (onx.length == 0)
                return "";

            String ons = onx[0] + " = " + set.get(onx[0]);
            for (int i = 1; i < onx.length; i++) {
                ons += ", " + onx[i] + " = " + set.get(onx[i]);
            }

            return ons;
        }

        // substitutes the necessary attributes of a given block by
        // using the output attributes of its child. for example, if
        // node A returns {att1 = att2 + att3}, its necessary
        // attributes would be {att2,att3}; now, with its child node B
        // returning {att2 = attX, att3 = attY}, then this replacement
        // will return {attX, attY} for nodeA.
        public static LinkedHashSet<String> substituteNecessaryAtts(BlockNode parent, BlockNode child) {

            LinkedHashSet<String> needed = parent.getNecessaryAtts();

            // go through all the output attributes
            for (String s : child.outAttsReq.keySet()) {

                // is this attribute in the set of necessaries?
                if (needed.contains(s)) {
                    needed.remove(s);
                    needed.addAll(child.outAttsReq.get(s));
                }
            }

            // return the updated set
            return needed;
        }

        // does the above substitution but with sets of output attributes.
        public void substituteOutAtts(LinkedHashMap<String, String> inOutAtts, LinkedHashMap<String, LinkedHashSet<String>> inOutAttsReq) {

            LinkedHashMap<String, String> newOutAtts = new LinkedHashMap<String, String>();
            LinkedHashMap<String, LinkedHashSet<String>> newOutAttsReq = new LinkedHashMap<String, LinkedHashSet<String>>();

            for (String s : outAtts.keySet()) {
                String newS = outAtts.get(s);

                LinkedHashSet<String> newReq = new LinkedHashSet<String>();
                for (String t : inOutAtts.keySet()) {
                    if (newS.indexOf(t) >= 0) {
                        newReq.addAll(inOutAttsReq.get(t));
                        newS = newS.replaceAll(t, inOutAtts.get(t));
                    }
                }

                newOutAtts.put(s, newS);
                newOutAttsReq.put(s, newReq);
            }

            outAtts = newOutAtts;
            outAttsReq = newOutAttsReq;
        }

        public void substituteOutAtts(LinkedHashMap<String, String> origOutAtts, LinkedHashMap<String, LinkedHashSet<String>> origOutAttsReq, LinkedHashMap<String, String> inOutAtts, LinkedHashMap<String, LinkedHashSet<String>> inOutAttsReq) {

            LinkedHashMap<String, String> newOutAtts = new LinkedHashMap<String, String>();
            LinkedHashMap<String, LinkedHashSet<String>> newOutAttsReq = new LinkedHashMap<String, LinkedHashSet<String>>();


            for (String s : origOutAtts.keySet()) {
                String newS = origOutAtts.get(s);

                LinkedHashSet<String> newReq = new LinkedHashSet<String>();
                for (String t : inOutAtts.keySet()) {
                    if (newS.indexOf(t) >= 0) {
                        newReq.addAll(inOutAttsReq.get(t));
                        newS = newS.replaceAll(t, inOutAtts.get(t));
                    }
                }

                newOutAtts.put(s, newS);
                newOutAttsReq.put(s, newReq);
            }

            origOutAtts.clear();
            origOutAttsReq.clear();
            origOutAtts.putAll(newOutAtts);
            origOutAttsReq.putAll(newOutAttsReq);
        }

        // substitutes a set of selection expressions so that they
        // reflect the usage given by its child node. for example, if
        // node A selects (att1 > att2) and node B has the output
        // {att1 = year(attX), att2 = year(attY)}, then this
        // replacement will return (year(attX) > year(attY)). it also
        // computes the requisite attributes.
        public static LinkedHashMap<String, LinkedHashSet<String>> substituteSelections(LinkedHashMap<String, LinkedHashSet<String>> selections,
                                                                                        LinkedHashMap<String, String> outAtts,
                                                                                        LinkedHashMap<String, LinkedHashSet<String>> outAttsReq) {

            LinkedHashMap<String, LinkedHashSet<String>> newSelections = new LinkedHashMap<String, LinkedHashSet<String>>();
            for (String s : selections.keySet()) {

                LinkedHashSet<String> newReq = new LinkedHashSet<String>();
                String newS = s;
                for (String t : outAtts.keySet()) {
                    if (newS.indexOf(t) >= 0) {
                        newReq.addAll(outAttsReq.get(t));
                        newS = newS.replaceAll(t, outAtts.get(t));
                    }
                }

                newSelections.put(newS, newReq);
            }

            return newSelections;
        }
    }

    // the selection node block -- the most basic type of block.
    private class SelectBlockNode extends BlockNode {

        // set of input attributes
        private LinkedHashSet<String> inAtts = new LinkedHashSet<String>();
        private String sortedOn = null;

        // set of filtering expressions, mapped to the set of attributes they need.
        private LinkedHashMap<String, LinkedHashSet<String>> selections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // set of input files
        private LinkedHashSet<String> inFiles = new LinkedHashSet<String>();

        // duplicate removal flag
        private boolean removeDuplicates = false;
        private boolean alreadyConnected = false;

        // a visitor for the attribute sets...
        private ExpToAttsVisitor visAtt = new ExpToAttsVisitor();
        private ExpToStrVisitor visStr = new ExpToStrVisitor();

        // constructor -- builds from a tablescan operator
        public SelectBlockNode(PrologTablescanOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // first, get the relation information
            inFiles.add(db.getFileName(op.getRelation().getName()));
            typeCodeIn = db.getTypeCode(op.getRelation().getName());

            // sorted?
            int sortedIdx = -1;
            if (db.isTableSorted(op.getRelation().getName())) {
                sortedIdx = db.getTableSortingAttribute(op.getRelation().getName());
            }

            // get the input and output attributes.
            int jj = 0;
            for (PrologAttributeExp e : op.getAttributes()) {

                // is it the sorting guy
                if (jj == sortedIdx) {
                    sortedOn = e.getName();
                }

                jj++;

                inAtts.add(e.getName());

                // outAtts[i] = inAtts[i], but keep the proper names.
                outAtts.put(e.getName(), e.getName());
                outAttsReq.put(e.getName(), e.acceptVisitor(visAtt));
            }

            // remove the projected attributes.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        // constructor -- builds from a selection operator
        public SelectBlockNode(PrologSelectionOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // get the expressions and their requisite attributes
            for (PrologQueryExpression e : op.getExpressions()) {
                selections.put(e.acceptVisitor(visStr), e.acceptVisitor(visAtt));
            }
        }


        // constructor -- builds from a projection operator
        public SelectBlockNode(PrologProjectionOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // get the output attributes, they have the same input/output names
            for (PrologAttributeExp e : op.getAttributes()) {
                outAtts.put(e.getName(), e.getName());
                outAttsReq.put(e.getName(), e.acceptVisitor(visAtt));
            }

            // remove the projected stuff.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        // constructor -- builds from a seed operator
        public SelectBlockNode(PrologSeedOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // set the output attribute with a call to the seed function.
            outAtts.put(op.getAttribute().getName(), "seed()");

            // no attribute requirements for seeding.
            outAttsReq.put(op.getAttribute().getName(), new LinkedHashSet<String>());

            // and claim this as an outer relation.
            outerRelation = true;

            // remove the projected stuff.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        // constructor -- builds from a scalar function operator
        public SelectBlockNode(PrologScalarFunctionOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // set the output attributes according to their associated expressions
            int ix = 0;
            for (PrologAttributeExp e : op.getOutputAtts()) {
                outAtts.put(e.getName(), op.getExpressions().get(ix).acceptVisitor(visStr));
                outAttsReq.put(e.getName(), op.getExpressions().get(ix).acceptVisitor(visAtt));
                ix++;
            }

            // remove the projected stuff.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        // constructor -- builds from a split operator
        public SelectBlockNode(PrologSplitOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // does nothing!
        }

        // constructor -- builds from a duplicate removal operator
        public SelectBlockNode(PrologDedupOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // this sets the duplicate removal flag
            removeDuplicates = true;
        }

        // get the type.
        public BlockNode.Type getType() {
            return BlockNode.Type.SELECT;
        }

        // returns the set of attributes that *must* be in the input
        // section of the operator.
        public LinkedHashSet<String> getNecessaryAtts() {

            // for a selection:
            // selection atts + output atts RHS.
            LinkedHashSet<String> neededAtts = new LinkedHashSet<String>();

            // we get the ones from selections
            for (LinkedHashSet<String> selAtts : selections.values()) {
                neededAtts.addAll(selAtts);
            }

            // and the ones from the output attributes
            for (LinkedHashSet<String> reqAtts : outAttsReq.values()) {
                neededAtts.addAll(reqAtts);
            }

            return neededAtts;
        }

        // merges two blocks.
        public BlockNode mergeParent(BlockNode me) {

            // first thing to check: is this thing final?
            if (isFinal) {
                return null;
            }

            // second thing: no self-merge.
            if (me == this || removeDuplicates) {
                return null;
            }

            // is the parent block a selection?
            if (me.getType() == BlockNode.Type.SELECT) {

                // if we have all their necessary attributes in our
                // input, then we can merge these two.
                SelectBlockNode sel = (SelectBlockNode) me;

                // check if we have the substituted necessary atts.
                if (inAtts.containsAll(BlockNode.substituteNecessaryAtts(sel, this)) && !sel.removeDuplicates) {

                    // copy its constants
                    sel.removeDuplicates |= removeDuplicates;
                    sel.outerRelation |= outerRelation;

                    // create a new set of selections.
                    sel.selections = BlockNode.substituteSelections(sel.selections, outAtts, outAttsReq);
                    sel.selections.putAll(selections);

                    // and a new set of output attributes
                    sel.substituteOutAtts(outAtts, outAttsReq);

                    // and input information
                    sel.typeCodeIn = typeCodeIn;
                    sel.inFiles = inFiles;
                    sel.inAtts = inAtts;
                    sel.planNodes.addAll(planNodes);
                    sel.isFinal |= isFinal;
                    sel.sortedOn = sortedOn;

                    // and we are done!
                    return sel;
                }
            }


            // is the parent node an aggregate?
            if (me.getType() == BlockNode.Type.AGGREGATE) {

                // the rule is simple: if we can cover all of the
                // aggregate's necessary attributes with the
                // selection's input, then a merger is possible.
                AggregateBlockNode agg = (AggregateBlockNode) me;
                if (inAtts.containsAll(agg.getNecessaryAtts())) {

                    // give it our selections
                    agg.inputSelections.putAll(substituteSelections(selections, outAtts, outAttsReq));

                    // and our constants
                    agg.outerRelation |= outerRelation;

                    // and our input information
                    agg.inAtts = inAtts;
                    agg.typeCodeIn = typeCodeIn;
                    agg.inFiles = inFiles;

                    agg.planNodes.addAll(planNodes);
                    agg.isFinal |= isFinal;
                    agg.sortedOn = sortedOn;

                    // and we're done
                    return agg;
                }
            }

            // is the parent node a join?
            if (me.getType() == BlockNode.Type.JOIN) {

                // find the side of the join we will be working on
                JoinBlockNode join = (JoinBlockNode) me;

                LinkedHashSet<String> wInAtts = null;
                LinkedHashSet<String> wInFiles = null;
                LinkedHashMap<String, LinkedHashSet<String>> wSelections = null;
                LinkedHashMap<String, String> wOutAtts = null;
                LinkedHashMap<String, LinkedHashSet<String>> wOutAttsReq = null;
                boolean left = false;

                if (join.leftTypeCode == typeCodeOut) {

                    // we've got the left side.
                    wInAtts = join.leftInAtts;
                    wInFiles = join.leftInFiles;
                    wSelections = join.leftSelections;
                    wOutAtts = join.leftOutAtts;
                    wOutAttsReq = join.leftOutAttsReq;
                    left = true;
                } else if (join.rightTypeCode == typeCodeOut) {

                    // we've got the right side.
                    wInAtts = join.rightInAtts;
                    wInFiles = join.rightInFiles;
                    wSelections = join.rightSelections;
                    wOutAtts = join.rightOutAtts;
                    wOutAttsReq = join.rightOutAttsReq;
                } else {

                    // otherwise?
                    return null;
                }

                // get the necessary attributes for that side
                LinkedHashSet<String> needed = new LinkedHashSet<String>();
                for (LinkedHashSet<String> selAtts : wSelections.values()) {
                    needed.addAll(selAtts);
                }

                for (LinkedHashSet<String> reqAtts : wOutAttsReq.values()) {
                    needed.addAll(reqAtts);
                }

                // do we cover all of them?
                if (inAtts.containsAll(needed)) {

                    // then, we push the input information
                    wInAtts.clear();
                    wInAtts.addAll(inAtts);

                    wInFiles.clear();
                    wInFiles.addAll(inFiles);

                    if (left) {
                        join.leftTypeCode = typeCodeIn;
                        join.leftSortedOn = sortedOn;
                    } else {
                        join.rightTypeCode = typeCodeIn;
                        join.rightSortedOn = sortedOn;
                    }

                    // and the selections
                    wSelections.putAll(substituteSelections(selections, outAtts, outAttsReq));
                    substituteOutAtts(wOutAtts, wOutAttsReq, outAtts, outAttsReq);

                    // and the constants
                    join.outerRelation |= outerRelation;
                    join.planNodes.addAll(planNodes);
                    join.isFinal |= isFinal;

                    // good, we are done
                    return join;
                }
            }

            // is it a VGWrapper?
            if (me.getType() == BlockNode.Type.VGWRAPPER) {

                // find the revelant input
                VGWrapperBlockNode vgw = (VGWrapperBlockNode) me;
                VGInputBlockNode vin = null;

                // is it the outer input?
                if (vgw.outerInput != null && vgw.outerInput.typeCodeIn == typeCodeOut) {
                    vin = vgw.outerInput;
                } else {

                    // if not, check the inner inputs.
                    for (VGInputBlockNode vx : vgw.innerInputs) {
                        if (vx.typeCodeIn == typeCodeOut) {
                            vin = vx;
                            break;
                        }
                    }
                }

                // just in case we didn't find it.
                if (vin == null) {
                    return null;
                }

                // do we cover the needed atts?
                if (inAtts.containsAll(vin.getNecessaryAtts())) {

                    // if so, then we will merge.
                    // move the input information
                    vin.inAtts = inAtts;
                    vin.inFiles = inFiles;
                    vin.typeCodeIn = typeCodeIn;
                    vin.selections.putAll(substituteSelections(selections, outAtts, outAttsReq));
                    vin.substituteOutAtts(outAtts, outAttsReq);
                    vin.sortedOn = sortedOn;

                    // and the constants
                    vgw.planNodes.addAll(planNodes);
                    vgw.isFinal |= isFinal;

                    // call the optimization that removes unnecessary inputs.
                    vgw.removeUnnecessaryInnerInputs();

                    // we are done
                    return vgw;
                }
            }

            // inference node?
            if (me.getType() == BlockNode.Type.INFERENCE) {

                InferenceBlockNode inf = (InferenceBlockNode) me;
                if (inAtts.containsAll(inf.getNecessaryAtts())) {

                    // give it our selections
                    inf.inputSelections.putAll(substituteSelections(selections, outAtts, outAttsReq));

                    // and our constants
                    inf.outerRelation |= outerRelation;

                    // and our input information
                    inf.inAtts = inAtts;
                    inf.inFiles = inFiles;
                    inf.typeCodeIn = typeCodeIn;
                    inf.planNodes.addAll(planNodes);
                    inf.isFinal |= isFinal;
                    inf.sortedOn = sortedOn;

                    // and we're done
                    return inf;
                }
            }

            return null;
        }

        // connects the input of this block to a child block.
        public void connectChild(BlockNode me) {

            if (me == this || alreadyConnected) {
                return;
            }

            // just connect once -- this is to avoid a large projection from the frame output.
            alreadyConnected = true;

            // the output of the child block becomes our input.
            inAtts = new LinkedHashSet<String>(me.outAtts.keySet());
            inFiles = new LinkedHashSet<String>(Arrays.asList(me.getOutFile()));
            typeCodeIn = me.typeCodeOut;

            // we update our output based on the projections
            for (String s : projections) {

                if (inAtts.contains(s)) {
                    outAtts.put(s, s);
                    outAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                }
            }

            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }


        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();

            mappings.put("typeCodeIn", "" + typeCodeIn);
            mappings.put("typeCodeOut", "" + typeCodeOut);
            mappings.put("removeDuplicates", "" + removeDuplicates);
            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("inAtts", fromSet(inAtts, ", ", "", "", ""));
            mappings.put("inFiles", fromSet(inFiles, ", ", "\"", "\"", ""));
            mappings.put("isFinal", "" + isFinal);

            if (selections.isEmpty()) {
                mappings.put("selection", "");
            } else {
                mappings.put("selection", "selection = {" + fromSet(selections.keySet(), " && ", "(", ")", "") + "}");
            }

            if (sortedOn == null) {
                mappings.put("sortedOn", "");
            } else {
                mappings.put("sortedOn", "sortedOn = {" + sortedOn + "}");
            }

            mappings.put("outAtts", fromAssignmentSet(outAtts));

            return replaceTemplateMappings("select.dft", mappings);
        }
    }

    // the VGWrapper node block.
    private class VGWrapperBlockNode extends BlockNode {

        // library file for the VG function
        private String funcName = "";

        // set of VG function input attribute names
        private LinkedHashSet<String> vgInAtts = new LinkedHashSet<String>();

        // set of VG function output attribute names
        private LinkedHashSet<String> vgOutAtts = new LinkedHashSet<String>();

        // input/output seed attribute name.
        private String inputSeedAtt = "";
        private String outputSeedAtt = "";

        // input attribute names from relations
        private LinkedHashSet<String> relInAtts = new LinkedHashSet<String>();

        // output attribute names for relations
        private LinkedHashSet<String> relOutAtts = new LinkedHashSet<String>();

        // name of the outer node.
        private String outerNode = "";

        // the outer input
        private VGInputBlockNode outerInput = null;

        // the inner inputs
        private LinkedHashSet<VGInputBlockNode> innerInputs = new LinkedHashSet<VGInputBlockNode>();

        // possible output selection, with the involved attribute sets
        private LinkedHashMap<String, LinkedHashSet<String>> outputSelections = new LinkedHashMap<String, LinkedHashSet<String>>();

        public VGWrapperBlockNode(PrologVGWrapperOp op, ArrayList<String> outputAtts, String outerNodeIn, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            outerNode = outerNodeIn;
            inputSeedAtt = op.getInputSeed().getName();
            outputSeedAtt = op.getOutputSeed().getName();
            funcName = op.getFunction().getName();

            // get the input atts for VG
            for (PrologAttributeExp e : op.getInputAtts()) {
                vgInAtts.add(e.getName());
            }

            // get the output atts for VG
            for (PrologAttributeExp e : op.getOutputAtts()) {
                vgOutAtts.add("vg_out_" + e.getName());
            }

            // get the input atts from relations
            for (PrologAttributeExp e : op.getInputAtts()) {
                relInAtts.add(e.getName());
            }

            // get the input atts to relations
            for (PrologAttributeExp e : op.getOutputAtts()) {
                relOutAtts.add(e.getName());
            }
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.VGWRAPPER;
        }

        public LinkedHashSet<String> getNecessaryAtts() {

            // the necessary atts for the VGW are:
            // relation input atts + input seed att.
            LinkedHashSet<String> neededAtts = new LinkedHashSet<String>(relInAtts);
            neededAtts.add(inputSeedAtt);
            return neededAtts;
        }

        // removes unnecessary vgwrapper inputs
        public void removeUnnecessaryInnerInputs() {

            // don't do anything if we don't have an outer
            if (outerInput == null)
                return;

            LinkedHashSet<VGInputBlockNode> toEliminate = new LinkedHashSet<VGInputBlockNode>();

            for (VGInputBlockNode v : innerInputs) {

                // check if they are the same inputs, that they are
                // compatible and that the output contains a seed.
                if (v.typeCodeIn == outerInput.typeCodeIn &&
                        outerInput.inAtts.containsAll(v.getNecessaryAtts()) &&
                        v.outAtts.keySet().contains(inputSeedAtt)) {

                    // if so, add them for elimination
                    toEliminate.add(v);
                }
            }

            for (VGInputBlockNode v : toEliminate) {

                // move the selections
                outerInput.selections.putAll(v.selections);

                // and the output attributes and their requirements.
                outerInput.outAtts.putAll(v.outAtts);
                outerInput.outAttsReq.putAll(v.outAttsReq);

                // and eliminate it.
                innerInputs.remove(v);
            }
        }


        public BlockNode mergeParent(BlockNode me) {

            // first off, do some checks
            if (isFinal || me == this || me.getType() != BlockNode.Type.JOIN || outerInput == null) {
                return null;
            }

            // get the join operator.
            JoinBlockNode join = (JoinBlockNode) me;

            // first, check that the other side of the join is the same as the
            // outer input and identify which side is which.
            LinkedHashMap<String, LinkedHashSet<String>> joinOuterAttsReq;
            LinkedHashMap<String, String> joinOuterOutputAtts;

            LinkedHashMap<String, LinkedHashSet<String>> wSelections;
            LinkedHashMap<String, LinkedHashSet<String>> wOutAttsReq;
            LinkedHashMap<String, String> wOutAtts;


            // is it left=VGW, right=Outer ?
            if (join.leftTypeCode == typeCodeOut && join.rightTypeCode == outerInput.typeCodeIn) {
                joinOuterAttsReq = join.rightOutAttsReq;
                joinOuterOutputAtts = join.rightOutAtts;
                wOutAtts = join.leftOutAtts;
                wSelections = join.leftSelections;
                wOutAttsReq = join.leftOutAttsReq;
            }

            // is it left=Outer, right=VGW ?
            else if (join.rightTypeCode == typeCodeOut && join.leftTypeCode == outerInput.typeCodeIn) {
                joinOuterAttsReq = join.leftOutAttsReq;
                joinOuterOutputAtts = join.leftOutAtts;
                wOutAtts = join.rightOutAtts;
                wSelections = join.rightSelections;
                wOutAttsReq = join.rightOutAttsReq;
            }

            // otherwise, no merger is possible
            else {
                return null;
            }

            // now, check the hash attributes and selections to confirm that
            // there are no other join predicates mixed into it.
            if (!(join.hashAtts.size() == 2 &&
                    join.hashAtts.contains(inputSeedAtt) &&
                    join.hashAtts.contains(outputSeedAtt) &&
                    join.outputSelections.size() == 1)) {

                // if not, then no merger is possible.
                return null;
            }

            // do the merger -- move the constants.
            outerRelation |= join.outerRelation;
            isFinal |= join.isFinal;
            name = join.name;
            outFN = join.outFN;
            hasOutFile = join.hasOutFile;
            typeCodeOut = join.typeCodeOut;
            planNodes.addAll(join.planNodes);

            // we need to update:
            // 1. the global input attributes
            // 2. the outer output attributes
            // 3. the global output attributes

            // update (1)
            for (LinkedHashSet<String> reqAtts : joinOuterAttsReq.values()) {
                relInAtts.addAll(reqAtts);
                relOutAtts.addAll(reqAtts);
            }

            // update (2)
            outerInput.outAtts.putAll(joinOuterOutputAtts);
            outerInput.outAttsReq.putAll(joinOuterAttsReq);

            // update (3)

            // we have to substitute this VGW's output attributes to reflect
            // those of the side of the join that it is an input of.
            substituteOutAtts(wOutAtts, wOutAttsReq);

            // then, we add the ones that are needed from the outer side.
            outAtts.putAll(joinOuterOutputAtts);
            outAttsReq.putAll(joinOuterAttsReq);

            // do a full substitution and consume it.
            join.substituteOutAtts(outAtts, outAttsReq);
            outAtts = join.outAtts;
            outAttsReq = join.outAttsReq;

            // update (4)
            outputSelections.putAll(BlockNode.substituteSelections(wSelections, outAtts, outAttsReq));

            // provisional
            return this;
        }

        public void connectChild(BlockNode me) {

            if (me == this)
                return;

            // we need to identify the outer input
            if (me.planNodes.contains(outerNode)) {

                if (outerInput != null) {
                    throw new RuntimeException("A VGWrapper cannot have more than one outer node!");
                }

                // create the outer input
                outerInput = new VGInputBlockNode(new LinkedHashSet<String>(Arrays.asList(me.getOutFile())), me.typeCodeOut, new LinkedHashSet<String>(me.outAtts.keySet()),
                        inputSeedAtt, relInAtts, vgInAtts, "outerInput");
            } else {

                // add an inner input
                innerInputs.add(new VGInputBlockNode(new LinkedHashSet<String>(Arrays.asList(me.getOutFile())), me.typeCodeOut, new LinkedHashSet<String>(me.outAtts.keySet()),
                        inputSeedAtt, relInAtts, vgInAtts, "innerInput" + innerI++));
            }

            // now, update the outer attributes by looking at the projections
            ArrayList<String> roa = new ArrayList<String>(relOutAtts);
            ArrayList<String> voa = new ArrayList<String>(vgOutAtts);

            for (String s : projections) {
                if (s.equals(outputSeedAtt)) {
                    outAtts.put(s, inputSeedAtt);
                    outAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(inputSeedAtt)));
                }

                if (relOutAtts.contains(s)) {
                    outAtts.put(s, voa.get(roa.indexOf(s)));
                    outAttsReq.put(s, relInAtts);
                }
            }

            // retain.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();

            LinkedHashSet<String> vgInAttsSeed = new LinkedHashSet<String>();
            vgInAttsSeed.add(inputSeedAtt);
      /* vgInAttsSeed.addAll(vgInAtts); */
            vgInAttsSeed.addAll(relInAtts);

            mappings.put("vgInAttsSeed", fromSet(vgInAttsSeed, ", ", "", "", ""));
            mappings.put("vgSeedAttIn", inputSeedAtt);
            mappings.put("functionName", "\"" + funcName + "\"");
            mappings.put("vgInAtts", fromSet(vgInAtts, ", ", "", "", ""));
            mappings.put("vgOutAtts", fromSet(vgOutAtts, ", ", "", "", ""));
            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("outTypeCode", "" + typeCodeOut);
            mappings.put("outputAtts", fromAssignmentSet(outAtts));
            mappings.put("isFinal", "" + isFinal);


            if (outputSelections.isEmpty()) {
                mappings.put("outputSelection", "");
            } else {
                mappings.put("outputSelection", "selection = {" + fromSet(outputSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            if (outerInput == null) {
                mappings.put("outerInput", "");
            } else {
                mappings.put("outerInput", outerInput.toString().replaceAll("\t\t", "\t"));
            }

            if (innerInputs.isEmpty()) {
                mappings.put("innerInputs", "");
            } else {

                String outInners = "innerInputs = {\n";
                for (VGInputBlockNode ix : innerInputs) {
                    outInners += ix.toString() + "\n";
                }

                outInners += "\t}";
                mappings.put("innerInputs", outInners);
            }


            return replaceTemplateMappings("vgwrapper.dft", mappings);
        }
    }

    // a block node to be exclusively used as a VGWrapper input.
    private class VGInputBlockNode extends BlockNode {

        // set of input files
        private LinkedHashSet<String> inFiles = new LinkedHashSet<String>();

        // set of input attributes
        private LinkedHashSet<String> inAtts = new LinkedHashSet<String>();
        private String sortedOn = null;

        // set of filtering expressions, mapped to the set of attributes they need.
        private LinkedHashMap<String, LinkedHashSet<String>> selections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // name of the input
        private String inputName = "";

        // constructor for the VGWrapper
        public VGInputBlockNode(LinkedHashSet<String> filesIn, short typeCode, LinkedHashSet<String> attsIn, String inputSeedAtt,
                                LinkedHashSet<String> relInputAtts, LinkedHashSet<String> vgInputAtts, String name) {
            super(null, null, null);

            inFiles = filesIn;
            typeCodeIn = typeCode;
            inAtts = attsIn;
            inputName = name;

            // now, we figure out the output attributes.
            // do we have a seed-merge or a cross product?
            if (inAtts.contains(inputSeedAtt)) {
                outAtts.put(inputSeedAtt, inputSeedAtt);
                outAttsReq.put(inputSeedAtt, new LinkedHashSet<String>(Arrays.asList(inputSeedAtt)));
            }

            // and do the same for the rest of the attributes
            String[] ria = relInputAtts.toArray(new String[0]);
            String[] via = vgInputAtts.toArray(new String[0]);

            for (int i = 0; i < ria.length; i++) {
                if (inAtts.contains(ria[i])) {
                    outAtts.put(via[i], ria[i]);
                    outAttsReq.put(via[i], new LinkedHashSet<String>(Arrays.asList(ria[i])));
                }
            }
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.VGINPUT;
        }

        public LinkedHashSet<String> getNecessaryAtts() {

            // necessary atts: selection RHS + outAtts RHS
            LinkedHashSet<String> neededAtts = new LinkedHashSet<String>();

            // we get the ones from selections
            for (LinkedHashSet<String> selAtts : selections.values()) {
                neededAtts.addAll(selAtts);
            }

            // and the ones from the output attributes
            for (LinkedHashSet<String> reqAtts : outAttsReq.values()) {
                neededAtts.addAll(reqAtts);
            }

            return neededAtts;
        }


        public BlockNode mergeParent(BlockNode me) {

            // these don't merge.
            return null;
        }

        public void connectChild(BlockNode me) {

            // they don't conntect either. the constructor does that!
        }

        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();
            mappings.put("inputName", inputName);
            mappings.put("inTypeCode", "" + typeCodeIn);
            mappings.put("inFiles", fromSet(inFiles, ", ", "\"", "\"", ""));
            mappings.put("inAtts", fromSet(inAtts, ", ", "", "", ""));
            mappings.put("outAtts", fromAssignmentSet(outAtts));
            mappings.put("isFinal", "" + isFinal);

            if (sortedOn == null) {
                mappings.put("sortedOn", "");
            } else {
                mappings.put("sortedOn", "sortedOn = {" + sortedOn + "}");
            }

            if (selections.isEmpty()) {
                mappings.put("selection", "");
            } else {
                mappings.put("selection", "selection = {" + fromSet(selections.keySet(), " && ", "(", ")", "") + "}");
            }

            return replaceTemplateMappings("vgwrapper_input.dft", mappings);
        }

    }

    // the aggregation node block.
    private class AggregateBlockNode extends BlockNode {

        // set of input files
        private LinkedHashSet<String> inFiles = new LinkedHashSet<String>();

        // set of input attributes
        private LinkedHashSet<String> inAtts = new LinkedHashSet<String>();
        private String sortedOn = null;

        // set of grouping attributes
        private LinkedHashSet<String> groupByAtts = new LinkedHashSet<String>();

        // possible input selection, with the involved attribute sets
        private LinkedHashMap<String, LinkedHashSet<String>> inputSelections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // possible output selection, with the involved attribute sets
        private LinkedHashMap<String, LinkedHashSet<String>> outputSelections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // set of aggregate names, with their respective expressions
        private LinkedHashMap<String, String> aggregates = new LinkedHashMap<String, String>();

        // set of aggregate names, with the required attribute sets.
        private LinkedHashMap<String, LinkedHashSet<String>> aggsReq = new LinkedHashMap<String, LinkedHashSet<String>>();

        // visitors
        private ExpToStrVisitor visStr = new ExpToStrVisitor();
        private ExpToAttsVisitor visAtt = new ExpToAttsVisitor();

        public AggregateBlockNode(PrologGeneralAggregateOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // get the grouping attributes and put them in the output set.
            for (PrologAttributeExp e : op.getGroupByAtts()) {
                groupByAtts.add(e.getName());
                outAtts.put(e.getName(), "input." + e.getName());
                outAttsReq.put(e.getName(), e.acceptVisitor(visAtt));
            }

            // get the aggregates, give them names, and put them in the output set.
            int ix = 0;
            for (PrologQueryExpression e : op.getExpressions()) {
                aggregates.put("agg" + aggI, e.acceptVisitor(visStr));
                aggsReq.put("agg" + aggI, e.acceptVisitor(visAtt));
                outAtts.put(op.getOutputAtts().get(ix).getName(), "aggregates.agg" + aggI);
                outAttsReq.put(op.getOutputAtts().get(ix).getName(), e.acceptVisitor(visAtt));
                ix++;
                aggI++;
            }

            // remove the projected stuff.
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.AGGREGATE;
        }

        public LinkedHashSet<String> getNecessaryAtts() {

            // needed attributes = group by + input selection + aggregate expression
            LinkedHashSet<String> neededAtts = new LinkedHashSet<String>(groupByAtts);

            // add all the atts from the input selections.
            for (LinkedHashSet<String> selAtts : inputSelections.values()) {
                neededAtts.addAll(selAtts);
            }

            // add all the atts from the aggregates.
            for (LinkedHashSet<String> aggAtts : aggsReq.values()) {
                neededAtts.addAll(aggAtts);
            }

            return neededAtts;
        }

        // merge with parent node.
        public BlockNode mergeParent(BlockNode me) {

            // first thing to check: if the current block contains any
            // plan nodes that have been declared final, we cannot do
            // any merging
            if (isFinal)
                return null;

            // no self-merge
            if (me == this) {
                return null;
            }

            // we can merge with selections.
            if (me.getType() == BlockNode.Type.SELECT) {

                // confirm that the selection is not a duplicate
                // removal.
                SelectBlockNode sel = (SelectBlockNode) me;
                if (sel.removeDuplicates) {
                    return null;
                }

                // check if we have the substituted necessary atts in our input
                if (inAtts.containsAll(substituteNecessaryAtts(sel, this))) {

                    // move the selections
                    outputSelections.putAll(BlockNode.substituteSelections(sel.selections, outAtts, outAttsReq));

                    // and constants
                    outerRelation |= sel.outerRelation;

                    // and output attributes.
                    sel.substituteOutAtts(outAtts, outAttsReq);
                    outAtts = sel.outAtts;
                    outAttsReq = sel.outAttsReq;

                    // get the output table information
                    name = me.name;
                    outFN = me.outFN;
                    hasOutFile = me.hasOutFile;
                    typeCodeOut = me.typeCodeOut;
                    planNodes.addAll(me.planNodes);
                    isFinal |= me.isFinal;

                    // and we are done
                    return this;
                }
            }

            return null;
        }

        public void connectChild(BlockNode me) {

            if (me == this)
                return;

            // the output of the child block becomes our input.
            inAtts = new LinkedHashSet<String>(me.outAtts.keySet());
            inFiles = new LinkedHashSet<String>(Arrays.asList(me.getOutFile()));
            typeCodeIn = me.typeCodeOut;

            // and remove all projections
            outAtts.keySet().retainAll(projections);
            outAtts.keySet().retainAll(projections);
        }


        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();

            mappings.put("inFiles", fromSet(inFiles, ", ", "\"", "\"", ""));
            mappings.put("inAtts", fromSet(inAtts, ", ", "", "", ""));
            mappings.put("typeCodeIn", "" + typeCodeIn);

            if (groupByAtts.isEmpty()) {
                mappings.put("groupByAtts", "");
            } else {
                mappings.put("groupByAtts", "groupByAtts = {" + fromSet(groupByAtts, ", ", "", "", "") + "}");
            }

            if (inputSelections.isEmpty()) {
                mappings.put("inputSelection", "");
            } else {
                mappings.put("inputSelection", "selection = {" + fromSet(inputSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            mappings.put("aggregates", fromAssignmentSet(aggregates));
            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("typeCodeOut", "" + typeCodeOut);
            mappings.put("outAtts", fromAssignmentSet(outAtts).replaceAll("input\\.input\\.", "input."));
            mappings.put("isFinal", "" + isFinal);

            if (sortedOn == null) {
                mappings.put("sortedOn", "");
            } else {
                mappings.put("sortedOn", "sortedOn = {" + sortedOn + "}");
            }

            if (outputSelections.isEmpty()) {
                mappings.put("outputSelection", "");
            } else {
                mappings.put("outputSelection", "selection = {" + fromSet(outputSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            return replaceTemplateMappings("aggregate.dft", mappings);
        }
    }

    // the join node block.
    private class JoinBlockNode extends BlockNode {

        // join type string
        private String joinType = "";

        // source node name (this defines the LHS of semijoins and antijoins)
        private String sourceNode = "";

        // input typeCodes
        private short leftTypeCode = 0;
        private short rightTypeCode = 0;

        // input files
        private LinkedHashSet<String> leftInFiles = new LinkedHashSet<String>();
        private LinkedHashSet<String> rightInFiles = new LinkedHashSet<String>();

        // input attributes
        private LinkedHashSet<String> leftInAtts = new LinkedHashSet<String>();
        private String leftSortedOn = null;

        private LinkedHashSet<String> rightInAtts = new LinkedHashSet<String>();
        private String rightSortedOn = null;

        // selections on the inputs, with their sets of attributes.
        private LinkedHashMap<String, LinkedHashSet<String>> leftSelections = new LinkedHashMap<String, LinkedHashSet<String>>();
        private LinkedHashMap<String, LinkedHashSet<String>> rightSelections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // projections on the inputs, with their expressions and sets of attributes
        private LinkedHashMap<String, String> leftOutAtts = new LinkedHashMap<String, String>();
        private LinkedHashMap<String, String> rightOutAtts = new LinkedHashMap<String, String>();
        private LinkedHashMap<String, LinkedHashSet<String>> leftOutAttsReq = new LinkedHashMap<String, LinkedHashSet<String>>();
        private LinkedHashMap<String, LinkedHashSet<String>> rightOutAttsReq = new LinkedHashMap<String, LinkedHashSet<String>>();

        // hashing attributes: these we join with.
        private LinkedHashSet<String> hashAtts = new LinkedHashSet<String>();

        // selections on the output attribute.
        private LinkedHashMap<String, LinkedHashSet<String>> outputSelections = new LinkedHashMap<String, LinkedHashSet<String>>();

        // true means the left input has already been plugged
        private boolean pluggedLeft = false;
        private boolean pluggedRight = false;

        // some visitors
        private ExpToStrVisitor visStr = new ExpToStrVisitor();
        private ExpToAttsVisitor attStr = new ExpToAttsVisitor();
        private ExpToHashAttsVisitor hashAttStr = new ExpToHashAttsVisitor();


        // builds the set of hash attributes
        private void buildOutAndHashAtts(ArrayList<PrologQueryExpression> exp) {

            for (PrologQueryExpression e : exp) {
                outputSelections.put(e.acceptVisitor(visStr), e.acceptVisitor(attStr));

                // add only the hashAtts that have not been considered
                // this is for situations when you have predicates of the form R.a = S.b && R.a = S.c
                // so that you only have either 'b' or 'c' as hashAtts for S.
                LinkedHashSet<String> someAtts = e.acceptVisitor(hashAttStr);
                boolean alreadySeen = false;
                for (String s : someAtts) {
                    if (hashAtts.contains(s)) {
                        alreadySeen = true;
                        break;
                    }
                }

                if (!alreadySeen) {
                    hashAtts.addAll(someAtts);
                }
            }
        }

        // builds from join op
        public JoinBlockNode(PrologJoinOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // natural join
            joinType = "natural";

            // parse the expressions
            buildOutAndHashAtts(op.getExpressions());
        }

        // builds from semi-join op
        public JoinBlockNode(PrologSemiJoinOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // semijoin
            joinType = "semijoin";
            sourceNode = op.getSource();

            // parse the expressions
            buildOutAndHashAtts(op.getExpressions());
        }

        // builds from anti-join op
        public JoinBlockNode(PrologAntiJoinOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // antijoin
            joinType = "antijoin";
            sourceNode = op.getSource();

            // parse the expressions
            buildOutAndHashAtts(op.getExpressions());
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.JOIN;
        }

        public LinkedHashSet<String> getNecessaryAtts() {

            // necessary attributes for join:
            // hashAtts + leftSelection RHS + rightSelection RHS + outSelection RHS + outAtts RHS
            LinkedHashSet<String> neededAtts = new LinkedHashSet<String>(hashAtts);

            // add everything from the left selection
            for (LinkedHashSet<String> leftAtts : leftSelections.values()) {
                neededAtts.addAll(leftAtts);
            }

            // add everything from the right selection
            for (LinkedHashSet<String> rightAtts : rightSelections.values()) {
                neededAtts.addAll(rightAtts);
            }

            // add everything from the final selection
            for (LinkedHashSet<String> finalAtts : outputSelections.values()) {
                neededAtts.addAll(finalAtts);
            }

            // add everything "required"
            for (LinkedHashSet<String> reqAtts : outAttsReq.values()) {
                neededAtts.addAll(reqAtts);
            }

            for (LinkedHashSet<String> reqAtts : leftOutAttsReq.values()) {
                neededAtts.addAll(reqAtts);
            }

            for (LinkedHashSet<String> reqAtts : rightOutAttsReq.values()) {
                neededAtts.addAll(reqAtts);
            }

            return neededAtts;
        }

        public BlockNode mergeParent(BlockNode me) {

            // first thing to check: if the current block contains any
            // plan nodes that have been declared final, we cannot do
            // any merging
            if (isFinal)
                return null;

            // no self-merge
            if (me == this) {
                return null;
            }

            // joins can merge with selections
            if (me.getType() == BlockNode.Type.SELECT) {

                // don't merge if there is a duplicate removal
                SelectBlockNode sel = (SelectBlockNode) me;
                if (sel.removeDuplicates) {
                    return null;
                }

                // first, intersect the left/right attributes.
                LinkedHashSet<String> allInAtts = new LinkedHashSet<String>();
                allInAtts.addAll(leftOutAtts.keySet());
                allInAtts.addAll(rightOutAtts.keySet());

                // check if we have the replacements.
                if (allInAtts.containsAll(substituteNecessaryAtts(sel, this))) {

                    // get the selections
                    outputSelections.putAll(substituteSelections(sel.selections, outAtts, outAttsReq));

                    // its constants
                    outerRelation |= sel.outerRelation;

                    // and output attributes
                    sel.substituteOutAtts(outAtts, outAttsReq);
                    outAtts = sel.outAtts;
                    outAttsReq = sel.outAttsReq;

                    // and output information
                    typeCodeOut = sel.typeCodeOut;
                    name = sel.name;
                    outFN = sel.outFN;
                    hasOutFile = sel.hasOutFile;
                    planNodes.addAll(sel.planNodes);
                    isFinal |= sel.isFinal;

                    // and we're done
                    return this;
                }

            }

            // this is important: joins can be merged into a VGWrapper
            // if they are cross products, simply by making them inner
            // inputs.
            if (me.getType() == BlockNode.Type.VGWRAPPER) {

                VGWrapperBlockNode vgw = (VGWrapperBlockNode) me;

                // confirm that this join is a cross product
                if (!hashAtts.isEmpty() || !joinType.equals("natural")) {
                    return null;
                }

                // find the relevant inner input
                VGInputBlockNode vin = null;
                for (VGInputBlockNode v : vgw.innerInputs) {
                    if (v.typeCodeIn == typeCodeOut) {
                        vin = v;
                        break;
                    }
                }

                // in case we couldn't find it.
                if (vin == null) {
                    return null;
                }

                // check the outer input.
                if (vgw.outerInput == null) {
                    return null;
                }

                // see which side of the join it corresponds to
                boolean rightSide = false;
                LinkedHashSet<String> wInAtts = null;
                String wSortedOn = null;
                LinkedHashSet<String> wInFiles = null;
                LinkedHashMap<String, LinkedHashSet<String>> wSelections = null;
                LinkedHashMap<String, String> wOutAtts = null;
                LinkedHashMap<String, LinkedHashSet<String>> wOutAttsReq = null;
                short wTypeCode = 0;

                if (leftTypeCode == vgw.outerInput.typeCodeIn) {
                    rightSide = true;

                    wInAtts = rightInAtts;
                    wSortedOn = rightSortedOn;
                    wInFiles = rightInFiles;
                    wSelections = rightSelections;
                    wOutAtts = rightOutAtts;
                    wOutAttsReq = rightOutAttsReq;
                    wTypeCode = rightTypeCode;

                } else if (rightTypeCode == vgw.outerInput.typeCodeIn) {
                    rightSide = false;

                    wInAtts = leftInAtts;
                    wSortedOn = leftSortedOn;
                    wInFiles = leftInFiles;
                    wSelections = leftSelections;
                    wOutAtts = leftOutAtts;
                    wOutAttsReq = leftOutAttsReq;
                    wTypeCode = leftTypeCode;

                } else {

                    // in case we couldn't find it.
                    return null;
                }


                // get the output selection attributes
                LinkedHashSet<String> selOutAtts = new LinkedHashSet<String>();
                for (String s : outputSelections.keySet()) {
                    selOutAtts.addAll(outputSelections.get(s));
                }

                // make sure we have all of them, minus the input seed value.
                LinkedHashSet<String> subNeeded = substituteNecessaryAtts(vin, this);
                subNeeded.remove(vgw.inputSeedAtt);
                if (wInAtts.containsAll(selOutAtts) && wInAtts.containsAll(subNeeded)) {

                    // update with requirements and other things.
                    wOutAtts.putAll(outAtts);
                    wOutAttsReq.putAll(outAttsReq);

                    // move the input information
                    vin.inAtts = wInAtts;
                    vin.typeCodeIn = wTypeCode;
                    vin.inFiles = wInFiles;
                    vin.sortedOn = wSortedOn;

                    // remove the input seed attributes.
                    vin.outAtts.remove(vgw.inputSeedAtt);
                    vin.outAttsReq.remove(vgw.inputSeedAtt);

                    // add the selections and projections
                    vin.selections = BlockNode.substituteSelections(vin.selections, wOutAtts, wOutAttsReq);
                    vin.selections.putAll(wSelections);
                    vin.selections.putAll(outputSelections);
                    vin.substituteOutAtts(wOutAtts, wOutAttsReq);

                    // add the nodes
                    vgw.planNodes.addAll(planNodes);
                    vgw.isFinal |= isFinal;

                    // done
                    return vgw;
                }


            }

            return null;
        }

        public void connectChild(BlockNode me) {

            if (me == this)
                return;

            // if we have a semijoin or an antijoin, the left side
            // input must be identified. for natural joins, it does
            // not matter.
            if (!pluggedLeft && (joinType.equals("natural") || me.planNodes.contains(sourceNode))) {

                // we will plug the left side
                pluggedLeft = true;
                leftInAtts = new LinkedHashSet<String>(me.outAtts.keySet());
                leftInFiles = new LinkedHashSet<String>(Arrays.asList(me.getOutFile()));
                leftTypeCode = me.typeCodeOut;
            } else if (!pluggedRight) {

                // we will plug the right side.
                pluggedRight = true;
                rightInAtts = new LinkedHashSet<String>(me.outAtts.keySet());
                rightInFiles = new LinkedHashSet<String>(Arrays.asList(me.getOutFile()));
                rightTypeCode = me.typeCodeOut;
            } else {
                throw new RuntimeException("Could not identify left/right hand sides of join.");
            }

            // don't do the following until both are plugged.
            if (!pluggedRight || !pluggedLeft)
                return;

            // update the output attributes everywhere.
            // first, the selection atts have to be moved around.
            for (LinkedHashSet<String> grp : outputSelections.values()) {

                // check those self-joins.
                if (grp.size() == 2) {

                    String[] dat = grp.toArray(new String[0]);

                    String toLeft = null;
                    String toRight = null;
                    if (leftInAtts.contains(dat[0]) && rightInAtts.contains(dat[1])) {
                        toLeft = dat[0];
                        toRight = dat[1];
                    } else if (leftInAtts.contains(dat[1]) && rightInAtts.contains(dat[0])) {
                        toLeft = dat[1];
                        toRight = dat[0];
                    }

                    if (toLeft != null && toRight != null) {
                        leftOutAtts.put(toLeft, toLeft);
                        leftOutAttsReq.put(toLeft, new LinkedHashSet<String>(Arrays.asList(toLeft)));
                        rightOutAtts.put(toRight, toRight);
                        rightOutAttsReq.put(toRight, new LinkedHashSet<String>(Arrays.asList(toRight)));
                        continue;
                    }
                }

                for (String s : grp) {
                    if (leftInAtts.contains(s)) {
                        leftOutAtts.put(s, s);
                        leftOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                    }

                    if (rightInAtts.contains(s)) {
                        rightOutAtts.put(s, s);
                        rightOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                    }
                }
            }

            /***
             LinkedHashSet<String> h1 = new LinkedHashSet<String>(leftInAtts);
             h1.retainAll(hashAtts);
             for (String s: h1) {
             leftOutAtts.put(s, s);
             leftOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
             }
             ***/

            // same thing with the right side...

            /***
             LinkedHashSet<String> h2 = new LinkedHashSet<String>(rightInAtts);
             h2.retainAll(hashAtts);
             h2.retainAll();
             for (String s: h2) {
             rightOutAtts.put(s, s);
             rightOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
             }
             ***/

            // now, we go to the final output, using the projection.
            for (String s : projections) {

                if (leftInAtts.contains(s) && rightInAtts.contains(s)) {

                    if (leftOutAtts.containsKey(s) || rightOutAtts.containsKey(s)) {
                        outAtts.put(s, s);
                        outAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                        continue;
                    }
                }

                if (leftInAtts.contains(s)) {
                    leftOutAtts.put(s, s);
                    leftOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                    outAtts.put(s, s);
                    outAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                }

                if (rightInAtts.contains(s)) {
                    rightOutAtts.put(s, s);
                    rightOutAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                    outAtts.put(s, s);
                    outAttsReq.put(s, new LinkedHashSet<String>(Arrays.asList(s)));
                }
            }

            // and retain
            outAtts.keySet().retainAll(projections);
            outAttsReq.keySet().retainAll(projections);

            /*****
             // in some cases (e.g. self-join), it is possible to have an empty left/right outAtts.
             // so we just force one attribute from the input to work as a dummy.
             if (leftOutAtts.isEmpty() && pluggedLeft) {
             String attToAdd = leftInAtts.toArray(new String[0])[0];
             leftOutAtts.put(attToAdd, attToAdd);
             leftOutAttsReq.put(attToAdd, new LinkedHashSet<String>(Arrays.asList(attToAdd)));
             }

             if (rightOutAtts.isEmpty() && pluggedRight) {
             String attToAdd = rightInAtts.toArray(new String[0])[0];
             rightOutAtts.put(attToAdd, attToAdd);
             rightOutAttsReq.put(attToAdd, new LinkedHashSet<String>(Arrays.asList(attToAdd)));
             }
             ****/

            // in the case of antijoins, we will merge all the predicates
            // now since the following ones will be OR'd
            if (joinType.equals("antijoin")) {

                if (!outputSelections.isEmpty()) {
                    LinkedHashSet<String> subSelAtts = new LinkedHashSet<String>();
                    String newSel = "!(";
                    boolean notFirst = false;
                    for (String s : outputSelections.keySet()) {
                        if (notFirst) {
                            newSel += " && ";
                        }
                        notFirst = true;
                        newSel += s;
                        subSelAtts.addAll(outputSelections.get(s));
                    }

                    newSel += ")";
                    outputSelections.clear();
                    outputSelections.put(newSel, subSelAtts);
                }
            }
        }


        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();
            mappings.put("joinType", joinType);
            mappings.put("leftInAtts", fromSet(leftInAtts, ", ", "", "", ""));
            mappings.put("leftInFiles", fromSet(leftInFiles, ", ", "\"", "\"", ""));

            mappings.put("leftTypeCode", "" + leftTypeCode);

            if (leftSelections.isEmpty()) {
                mappings.put("leftInputSelection", "");
            } else {
                mappings.put("leftInputSelection", "selection = {" + fromSet(leftSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            if (leftSortedOn == null) {
                mappings.put("leftSortedOn", "");
            } else {
                mappings.put("leftSortedOn", "sortedOn = {" + leftSortedOn + "}");
            }

            // update the hashAtts by going through the selections.
            for (String sel : outputSelections.keySet()) {

                // is this an equality predicate?
                LinkedHashSet<String> grp = outputSelections.get(sel);
                if (sel.matches("\\(+[A-Za-z0-9_\\.]+ == [A-Za-z0-9_\\.]+\\)+") && grp.size() == 2) {
                    String[] dat = grp.toArray(new String[0]);

                    if ((leftOutAtts.containsKey(dat[0]) && rightOutAtts.containsKey(dat[1])) ^
                            (rightOutAtts.containsKey(dat[0]) && leftOutAtts.containsKey(dat[1]))) {

                        if (!hashAtts.contains(dat[0]) && !hashAtts.contains(dat[1])) {
                            hashAtts.add(dat[0]);
                            hashAtts.add(dat[1]);
                        }
                    }
                }
            }

            mappings.put("leftOutAtts", fromAssignmentSet(leftOutAtts));
            LinkedHashSet<String> h1 = new LinkedHashSet<String>(leftOutAtts.keySet());
            h1.retainAll(hashAtts);

            // if there are no hashAtts, then we have to do a cross product.
            if (h1.isEmpty()) {

                mappings.put("hashAttsLeft", "" /*fromSet(leftOutAtts.keySet(), ", ", "", "", "")*/);
            } else {
                mappings.put("hashAttsLeft", "hashAtts = {" + fromSet(h1, ", ", "", "", "") + "}");
            }

            // do the same on the right hand side.
            mappings.put("rightInAtts", fromSet(rightInAtts, ", ", "", "", ""));
            mappings.put("rightInFiles", fromSet(rightInFiles, ", ", "\"", "\"", ""));
            mappings.put("rightTypeCode", "" + rightTypeCode);

            if (rightSelections.isEmpty()) {
                mappings.put("rightInputSelection", "");
            } else {
                mappings.put("rightInputSelection", "selection = {" + fromSet(rightSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            if (rightSortedOn == null) {
                mappings.put("rightSortedOn", "");
            } else {
                mappings.put("rightSortedOn", "sortedOn = {" + rightSortedOn + "}");
            }


            mappings.put("rightOutAtts", fromAssignmentSet(rightOutAtts));
            LinkedHashSet<String> h2 = new LinkedHashSet<String>(rightOutAtts.keySet());
            h2.retainAll(hashAtts);

            // if there are no hashAtts, then we have to do a cross product.
            if (h2.isEmpty()) {
                mappings.put("hashAttsRight", "" /*fromSet(rightOutAtts.keySet(), ", ", "", "", "")*/);
            } else {
                mappings.put("hashAttsRight", "hashAtts = {" + fromSet(h2, ", ", "", "", "") + "}");
            }

            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("outTypeCode", "" + typeCodeOut);
            mappings.put("isFinal", "" + isFinal);

            // there could be an empty selection on the output (i.e. a cross product)
            if (outputSelections.isEmpty()) {
                mappings.put("outputSelection", "");
            } else {

                String selStr = fromSet(outputSelections.keySet(), " && ", "(", ")", "");
                if (joinType.equals("antijoin")) {
                    selStr = "!(" + selStr + ")";
                }

                for (String s : leftOutAtts.keySet()) {
                    selStr = selStr.replaceAll("((left\\.)?|(right\\.))?" + s, "left." + s);
                }

                for (String s : rightOutAtts.keySet()) {
                    selStr = selStr.replaceAll("((left\\.)?|(right\\.))?" + s, "right." + s);
                }

                for (String s : leftOutAtts.keySet()) {
                    selStr = selStr.replaceAll("((left\\.)?|(right\\.))?" + s, "left." + s);
                }

                for (String s : rightOutAtts.keySet()) {
                    selStr = selStr.replaceAll("((left\\.)?|(right\\.))?" + s, "right." + s);
                }

                mappings.put("outputSelection", "selection = {" + selStr + "}");
            }

            // replace left/right on the output attributes.
            LinkedHashMap<String, String> dotOutAtts = new LinkedHashMap<String, String>();
            for (String ax : outAtts.keySet()) {

                String attStr = outAtts.get(ax);

                for (String s : leftOutAtts.keySet()) {
                    attStr = attStr.replaceAll("((left\\.)?|(right\\.))?" + s, "left." + s);
                }

                for (String s : rightOutAtts.keySet()) {
                    attStr = attStr.replaceAll("((left\\.)?|(right\\.))?" + s, "right." + s);
                }


                for (String s : leftOutAtts.keySet()) {
                    attStr = attStr.replaceAll("((left\\.)?|(right\\.))?" + s, "left." + s);
                }

                for (String s : rightOutAtts.keySet()) {
                    attStr = attStr.replaceAll("((left\\.)?|(right\\.))?" + s, "right." + s);
                }


                dotOutAtts.put(ax, attStr);
            }


            mappings.put("outAtts", fromAssignmentSet(dotOutAtts));

            // and make the string.
            return replaceTemplateMappings("join.dft", mappings);

        }
    }

    // the frameoutput node block.
    private class FrameOutputBlockNode extends BlockNode {

        private LinkedHashSet<String> inFiles = new LinkedHashSet<String>();

        // constructor -- builds from a frame output operator
        public FrameOutputBlockNode(PrologFrameOutputOp op, ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(op, outputAtts, db);

            // set the input files.
            for (String s : op.getOutputFiles()) {
                inFiles.add(db.getFileName(s));
            }

            for (String s : outputAtts) {
                outAtts.put(s, s);
            }
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.FRAMEOUTPUT;
        }

        public LinkedHashSet<String> getNecessaryAtts() {
            return new LinkedHashSet<String>();
        }

        public BlockNode mergeParent(BlockNode me) {

            // inference nodes never have parent nodes.
            return null;
        }

        public void connectChild(BlockNode me) {
            // nada.
        }

        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();

            mappings.put("inFiles", fromSet(inFiles, ", ", "\"", "\"", ""));
            mappings.put("typeCodeOut", "" + typeCodeOut);
            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("outAtts", fromAssignmentSet(outAtts));
            mappings.put("isFinal", "" + isFinal);

            return replaceTemplateMappings("frameOutput.dft", mappings);
        }
    }

    // the inference node block.
    private class InferenceBlockNode extends BlockNode {

        // set of input files
        private LinkedHashSet<String> inFiles = new LinkedHashSet<String>();

        // set of input attributes
        private LinkedHashSet<String> inAtts = new LinkedHashSet<String>();
        private String sortedOn;

        // set of filtering expressions, mapped to the set of attributes they need.
        private LinkedHashMap<String, LinkedHashSet<String>> inputSelections = new LinkedHashMap<String, LinkedHashSet<String>>();


        public InferenceBlockNode(ArrayList<String> outputAtts, MyPhysicalDatabase db) {
            super(null, outputAtts, db);
        }

        public BlockNode.Type getType() {
            return BlockNode.Type.INFERENCE;
        }

        public LinkedHashSet<String> getNecessaryAtts() {
            return new LinkedHashSet<String>();
        }

        public BlockNode mergeParent(BlockNode me) {

            // inference nodes never have parent nodes.
            return null;
        }

        public void connectChild(BlockNode me) {

        }

        public String toString() {

            // make all the mappings
            HashMap<String, String> mappings = new HashMap<String, String>();

            mappings.put("inFiles", fromSet(inFiles, ", ", "\"", "\"", ""));
            mappings.put("inAtts", fromSet(inAtts, ", ", "", "", ""));
            mappings.put("typeCodeIn", "" + typeCodeIn);
            mappings.put("typeCodeOut", "" + typeCodeOut);
            mappings.put("outFile", "\"" + getOutFile() + "\"");
            mappings.put("outAtts", fromAssignmentSet(outAtts));
            mappings.put("isFinal", "" + isFinal);

            if (sortedOn == null) {
                mappings.put("sortedOn", "");
            } else {
                mappings.put("sortedOn", "sortedOn = {" + sortedOn + "}");
            }

            if (inputSelections.isEmpty()) {
                mappings.put("inputSelection", "");
            } else {
                mappings.put("inputSelection", "selection = {" + fromSet(inputSelections.keySet(), " && ", "(", ")", "") + "}");
            }

            return replaceTemplateMappings("inference.dft", mappings);
        }
    }


    // --- VISITOR CLASS FOR TURNING EXPRESSIONS INTO DATAFLOW STRINGS ---
    private class ExpToStrVisitor implements PrologQueryExpressionVisitor<String> {

        public String visit(PrologAttributeExp exp) {

            // attribute names stay the same.
            return exp.getName();
        }

        public String visit(PrologLiteralExp exp) {

            // replace the quote and special characters, for "lost" strings.

            String s = exp.getValue();
            if (!s.matches("\\-?[0-9]+(\\.[0-9]+(e\\-?[0-9]+)?)?")) {
                s = "\"" + s.replaceAll("\"", "").replaceAll("''", "'").replaceAll("^'(.*)'$", "$1") + "\"";
            }

            return s;
        }

        public String visit(PrologCompExp exp) {

            // get the right operator.
            String op = "";
            switch (exp.getOp()) {
                case EQUALS:
                    op = " == ";
                    break;

                case NOTEQUALS:
                    op = " != ";
                    break;

                case LESSTHAN:
                    op = " < ";
                    break;

                case GREATERTHAN:
                    op = " > ";
                    break;

                case LESSEQUALS:
                    op = " <= ";
                    break;

                case GREATEREQUALS:
                    op = " >= ";
                    break;
            }

            // make it
            return "(" + exp.getLeft().acceptVisitor(this) + op + exp.getRight().acceptVisitor(this) + ")";
        }

        public String visit(PrologBoolExp exp) {

            // deal with SET first (this isn't even considered by the optimizer!)
            if (exp.getBoolType() == PrologBoolExp.Type.SET) {
                throw new RuntimeException("Set expressions are not supported by SimSQL.");
            }

            // deal with NOT. there should be only one element.
            if (exp.getBoolType() == PrologBoolExp.Type.NOT) {
                return "(!" + exp.getChildren().get(0).acceptVisitor(this) + ")";
            }

            // now, AND and OR
            String op = " || ";
            if (exp.getBoolType() == PrologBoolExp.Type.AND) {
                op = " && ";
            }

            String out = "(" + exp.getChildren().get(0).acceptVisitor(this);
            for (int i = 1; i < exp.getChildren().size(); i++) {
                out += op + exp.getChildren().get(i).acceptVisitor(this);
            }

            return out + ")";
        }

        public String visit(PrologAggExp exp) {

            // get the correct function names.
            String func = "";
            switch (exp.getAggType()) {
                case AVG:
                    func = "avg";
                    break;

                case SUM:
                    func = "sum";
                    break;

                case COUNT:
                    func = "count";
                    break;

                case COUNTALL:
                    func = "countAll";
                    break;

                case MIN:
                    func = "min";
                    break;

                case MAX:
                    func = "max";
                    break;

                case VAR:
                    func = "var";
                    break;

                case STDEV:
                    func = "stdev";
                    break;

                case VECTOR:
                    func = "vector";
                    break;

                case ROWMATRIX:
                    func = "rowmatrix";
                    break;

                case COLMATRIX:
                    func = "colmatrix";
                    break;
            }

            return func + "(" + exp.getExp().acceptVisitor(this) + ")";
        }


        public String visit(PrologArithExp exp) {

            // arithmetic expressions are always the same.
            String op = "";
            switch (exp.getOp()) {
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

            // function expressions can have zero parameters.
            if (exp.getParams().size() == 0) {
                return exp.getFunction() + "()";
            }

            String out = exp.getParams().get(0).acceptVisitor(this);
            for (int i = 1; i < exp.getParams().size(); i++) {
                out += ", " + exp.getParams().get(i).acceptVisitor(this);
            }

            return exp.getFunction() + "(" + out + ")";
        }
    }

    // --- VISITOR CLASS FOR EXTRACTING THE ATTRIBUTE NAMES FROM A GIVEN EXPRESSION ---
    private class ExpToAttsVisitor implements PrologQueryExpressionVisitor<LinkedHashSet<String>> {

        public LinkedHashSet<String> visit(PrologAttributeExp exp) {

            if (exp.getAttType().equals("unknown"))
                return new LinkedHashSet<String>();

            return new LinkedHashSet<String>(Arrays.asList(exp.getName()));
        }

        public LinkedHashSet<String> visit(PrologLiteralExp exp) {

            return new LinkedHashSet<String>();
        }

        public LinkedHashSet<String> visit(PrologCompExp exp) {

            LinkedHashSet<String> attsL = exp.getLeft().acceptVisitor(this);
            attsL.addAll(exp.getRight().acceptVisitor(this));
            return attsL;
        }

        public LinkedHashSet<String> visit(PrologBoolExp exp) {

            LinkedHashSet<String> atts = new LinkedHashSet<String>();
            for (PrologQueryExpression e : exp.getChildren()) {
                atts.addAll(e.acceptVisitor(this));
            }

            return atts;
        }

        public LinkedHashSet<String> visit(PrologAggExp exp) {

            return exp.getExp().acceptVisitor(this);
        }

        public LinkedHashSet<String> visit(PrologArithExp exp) {

            LinkedHashSet<String> attsL = exp.getLeft().acceptVisitor(this);
            attsL.addAll(exp.getRight().acceptVisitor(this));
            return attsL;
        }

        public LinkedHashSet<String> visit(PrologFunctionExp exp) {

            LinkedHashSet<String> atts = new LinkedHashSet<String>();
            for (PrologQueryExpression e : exp.getParams()) {
                atts.addAll(e.acceptVisitor(this));
            }

            return atts;
        }
    }

    // --- VISITOR CLASS FOR EXTRACTING THE HASH ATTRIBUTE NAMES FROM A JOIN PREDICATE ---
    private class ExpToHashAttsVisitor implements PrologQueryExpressionVisitor<LinkedHashSet<String>> {

        public LinkedHashSet<String> visit(PrologAttributeExp exp) {

            if (exp.getAttType().equals("unknown"))
                return new LinkedHashSet<String>();

            return new LinkedHashSet<String>(Arrays.asList(exp.getName()));
        }

        public LinkedHashSet<String> visit(PrologLiteralExp exp) {

            return new LinkedHashSet<String>();
        }

        public LinkedHashSet<String> visit(PrologCompExp exp) {

            // only accept equi-joins and attributes on both sides.
            if (exp.getOp() != PrologCompExp.Op.EQUALS ||
                    exp.getLeft().getType() != PrologQueryExpression.Type.ATTRIBUTE ||
                    exp.getRight().getType() != PrologQueryExpression.Type.ATTRIBUTE) {

                return new LinkedHashSet<String>();
            }

            LinkedHashSet<String> attsL = exp.getLeft().acceptVisitor(this);
            attsL.addAll(exp.getRight().acceptVisitor(this));
            return attsL;
        }

        public LinkedHashSet<String> visit(PrologBoolExp exp) {

            return new LinkedHashSet<String>();
        }

        public LinkedHashSet<String> visit(PrologAggExp exp) {

            return new LinkedHashSet<String>();
        }

        public LinkedHashSet<String> visit(PrologArithExp exp) {

            return new LinkedHashSet<String>();
        }

        public LinkedHashSet<String> visit(PrologFunctionExp exp) {

            return new LinkedHashSet<String>();
        }
    }


    // ---- VISITOR CLASS FOR TURNING OPERATORS INTO BLOCK NODES, COLLECTING TEMPORARY TABLES AND FINAL NODES ----
    private class OpToBlockVis implements PrologQueryOperatorVisitor<BlockNode> {

        private PrologQuery q;
        private MyPhysicalDatabase inDB;

        // string with temporary table definitions.
        private String tempTables = "";

        // set of final node names, mapped to their output file names.
        private LinkedHashMap<String, String> finalNodes = new LinkedHashMap<String, String>();

        public OpToBlockVis(PrologQuery inQ, MyPhysicalDatabase inDB) {
            q = inQ;
            db = inDB;

            // set the materialized nodes as final
            if (q.isConstant("materializeNodes")) {

                // parse the list of nodes.
                String qx = q.getConstant("materializeNodes").replaceAll("\\[|\\]|\\s", "").replaceAll("\\(([0-9]+),(node[0-9]+)\\)", "$1|$2");
                String[] ss = qx.split(",");

                for (String s : ss) {
                    String[] qs = s.split("\\|");

                    if (qs.length == 2) {
                        db.registerTable(qs[0]);
                        finalNodes.put(qs[1], qs[0]);
                    }
                }
            }
        }

        public String getTempTables() {
            return tempTables;
        }

        public LinkedHashMap<String, String> getFinalNodes() {
            return finalNodes;
        }

        public BlockNode visit(PrologSelectionOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologProjectionOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologJoinOp op) {
            return new JoinBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologTablescanOp op) {

            // before we get the block node, we'll check for temporary tables and make mappings with them.
            if (op.getRelation().getType() == PrologQueryRelation.Type.TEMPORARYTABLE && op.getRelation().getRows().size() > 0) {
                HashMap<String, String> mappings = new HashMap<String, String>();

                // register the temporary table
                db.registerTable(op.getRelation().getName());

                // set the mappings
                mappings.put("tempTableFileName", "\"" + db.getFileName(op.getRelation().getName()) + "\"");
                mappings.put("tempTableTypeCode", "" + db.getTypeCode(op.getRelation().getName()));

                boolean brow = false;
                boolean bcol = false;
                String os = "";
                for (ArrayList<PrologLiteralExp> row : op.getRelation().getRows()) {

                    if (brow)
                        os += ", ";

                    os += "{";

                    brow = true;
                    bcol = false;

                    for (PrologLiteralExp col : row) {

                        if (bcol)
                            os += ", ";
                        os += col.acceptVisitor(new ExpToStrVisitor());
                        bcol = true;
                    }
                    os += "}";
                }

                mappings.put("tempTableRows", os);

                tempTables += BlockNode.replaceTemplateMappings("tempTable.dft", mappings);
            }

            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologSeedOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologVGWrapperOp op) {
            return new VGWrapperBlockNode(op, q.getOutputAtts(op.getName()), q.getOuterRelation(op.getName()), db);
        }

        public BlockNode visit(PrologScalarFunctionOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologGeneralAggregateOp op) {
            return new AggregateBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologSplitOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologAntiJoinOp op) {
            return new JoinBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologSemiJoinOp op) {
            return new JoinBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologDedupOp op) {
            return new SelectBlockNode(op, q.getOutputAtts(op.getName()), db);
        }

        public BlockNode visit(PrologFrameOutputOp op) {

            // add all the source nodes and their output files.
            for (int i = 0; i < op.getSources().size(); i++) {

                // register all the output files
                db.registerTable(op.getOutputFiles().get(i));
                finalNodes.put(op.getSources().get(i), op.getOutputFiles().get(i));
            }

            return new FrameOutputBlockNode(op, q.getOutputAtts(op.getName()), db);
        }
    }

}
