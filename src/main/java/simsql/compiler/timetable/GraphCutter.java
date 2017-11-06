package simsql.compiler.timetable;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import simsql.compiler.*;
import simsql.compiler.expressions.MathExpression;
import simsql.compiler.math_operators.EFunction;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.*;
import simsql.runtime.DataType;

import java.util.*;
import java.util.concurrent.LinkedBlockingDeque;

import static simsql.compiler.MultidimensionalTableSchema.isGeneralTable;

public class GraphCutter {

    /**
     * The cost model for the graph cutter
     */
    private CostModel costModel;

    /**
     * The (general index name, relational operations) pairs for general index tables.
     */
    private HashMap<String, Operator> tableOperationMap;

    /**
     * Each random table (string) corresponds to an operator.
     * This map contains the instantiated random operators for the tables they represent.
     */
    private HashMap<String, Operator> generatedPlanMap = new HashMap<>();

    /**
     * Each random table (string) has a list of tableScan which should be replaced,
     * because they should be linked with the rest of the query plan.
     */
    private HashMap<String, ArrayList<TableScan>> replacedPlanMap = new HashMap<>();

    /**
     * list of tables in order they are removed for the graph
     */
    private ArrayList<String> tpList = new ArrayList<>();

    /**
     * TODO for some reason this exists...
     */
    private ArrayList<Operator> hostOperatorList = new ArrayList<>();

    /**
     * The operators we have linked...
     */
    private ArrayList<Operator> linkedOperatorList = new ArrayList<>();

    /**
     * The final queries
     */
    private ArrayList<Operator> queries = new ArrayList<>();

    /**
     * The tables that are required by the final queries
     */
    private ArrayList<String> sinkTableList;

    /**
     * The operators associated with the required tables
     */
    private LinkedList<Operator> sinkListOperators;

    /**
     * The operators associated with the required tables
     */
    private LinkedList<Operator> sourceOperators;

    /**
     * An instance of the TranslatorHelper class - we use that to generate the operator indices
     */
    private TranslatorHelper translatorHelper;

    /**
     * The table name for the child name
     */
    private HashMap<String, String> tableNameForChild;

    /**
     * How many nodes counter
     */
    private long projectedNodesCounter;

    /**
     * The number of detached nodes
     */
    private long detachedNodesCounter;

    /**
     * The required tables the name of the table with the number of times it is required
     */
    private HashMap<String, Integer> requiredTables;

    /**
     * The desired number of operators in a cut, in certain cases can be greater
     */
    private static final int LOWER_OPERATOR_BOUND = 10;

    /**
     * Each cut should not be bigger than this value
     */
    private static final int UPPER_OPERATOR_BOUND = 200;

    /**
     * An instane of the copy helper for the operators
     */
    private CopyHelper copyHelper;


    /**
     * The rule map contains the information on what table depends on what table...
     */
    HashMap<String, HashSet<String>> ruleMap;

    public GraphCutter(LinkedList<TimeTableNode> requiredTables,
                       HashMap<String, HashSet<String>> backwardEdges,
                       ArrayList<Operator> queries,
                       HashMap<String, Operator> tableOperationMap) {

        // initialize the model
        costModel = new CostModel();

        // store these values
        this.tableOperationMap = tableOperationMap;

        // init stuff
        this.detachedNodesCounter = 0;
        this.projectedNodesCounter = 0;
        this.translatorHelper = new TranslatorHelper();
        this.sinkTableList = new ArrayList<>();
        this.sinkListOperators = new LinkedList<>();
        this.sourceOperators = new LinkedList<>();
        this.queries = queries;
        this.tableNameForChild = new HashMap<>();
        this.requiredTables = new HashMap<>();
        this.ruleMap = new HashMap<>();
        this.copyHelper = new CopyHelper();

        // create a new table graph
        TableGraph tableGraph = new TableGraph(backwardEdges, requiredTables);

        // create the rule map
        ruleMap = tableGraph.generateRuleMap();

        // generate the bipartite graph
        try {

            generateBipartiteGraph(tableGraph, requiredTables);
        } catch (Exception ignore) {
            throw new RuntimeException("Failed to generate the bipartite graph");
        }
    }

    public GraphCutter(LinkedList<TimeTableNode> requiredTables,
                       HashMap<String, HashSet<String>> backwardEdges,
                       HashMap<Operator, String> planTableMap,
                       ArrayList<Operator> queries) throws Exception {

        // initialize the model
        costModel = new CostModel();

        // store these values
        this.tableOperationMap = new HashMap<>();

        // fill in the table operation map
        for (Operator o : planTableMap.keySet()) {
            tableOperationMap.put(planTableMap.get(o), o);
        }

        // init stuff
        this.detachedNodesCounter = 0;
        this.projectedNodesCounter = 0;
        this.translatorHelper = new TranslatorHelper();
        this.sinkTableList = new ArrayList<>();
        this.sinkListOperators = new LinkedList<>();
        this.sourceOperators = new LinkedList<>();
        this.queries = queries;
        this.tableNameForChild = new HashMap<>();
        this.requiredTables = new HashMap<>();
        this.copyHelper = new CopyHelper();

        // JSON object loader
        ObjectMapper mapper = new ObjectMapper();

        try {
            String q = mapper.writerFor(new TypeReference<ArrayList<Operator>>() {
            }).writeValueAsString(queries);
            String r = mapper.writerFor(new TypeReference<LinkedList<TimeTableNode>>() {
            }).writeValueAsString(requiredTables);
            String to = mapper.writerFor(new TypeReference<HashMap<String, Operator>>() {
            }).writeValueAsString(tableOperationMap);
            String be = mapper.writerFor(new TypeReference<HashMap<String, HashSet<String>>>() {
            }).writeValueAsString(backwardEdges);

            System.out.println("Serialized the input!");
        } catch (Exception ignore) {
        }

        // create a new table graph
        TableGraph tableGraph = new TableGraph(backwardEdges, requiredTables);

        // create the rule map
        ruleMap = tableGraph.generateRuleMap();

        // generate the bipartite graph
        generateBipartiteGraph(tableGraph, requiredTables);
    }

    /**
     * Checks if a table is required in the next cuts
     *
     * @param tableName the name of the table
     * @return true if it is false otherwise
     */
    public boolean isTableRequired(String tableName) {
        return requiredTables.get(tableName) != null && requiredTables.get(tableName) != 0;
    }

    /**
     * If we have finished the graph cutting
     *
     * @return true if we have false otherwise
     */
    public boolean isFinished() {
        return sourceOperators.isEmpty();
    }

    public ArrayList<Operator> getCut() throws Exception {

        // if we don't have any operators left....
        if (isFinished()) {
            return null;
        }

        // the nodes that are at the edge
        LinkedHashSet<Operator> edgeNodeList = new LinkedHashSet<>();

        // stores all the operators that are in the cut
        Set<Operator> cut = new HashSet<>();

        // figure out the best cut...
        getBestCut(edgeNodeList, cut);

        ArrayList<Operator> parents = new ArrayList<>();
        ArrayList<Operator> children = new ArrayList<>();
        ArrayList<String> tableList = new ArrayList<>();

        FrameOutput frameOutput = new FrameOutput("frameOutput", children, parents, tableList);

        // stores the name of the node and a reference to it's projection operator
        HashMap<String, Projection> projectedOperators = new HashMap<>();

        /*
         * 0. remove the just used tables from the required table we don't need them anymore
         */
        updateRequiredTables(cut);

        /*
         * 1. remove the links from the edges and add a frame output to the cut
         */
        for (Operator edge : new LinkedList<>(edgeNodeList)) {

            // go through each child
            for (Operator child : new ArrayList<>(edge.getChildren())) {

                // if the child is in the cut
                if (cut.contains(child)) {

                    // detaches this child
                    detachChild(edgeNodeList, cut, children, tableList, frameOutput, edge, child, projectedOperators);
                }
            }
        }

        /*
         * 2. if there are final sinks for the requested query in the cut add them to the frameOutput
         */
        for (int i = 0; i < sinkTableList.size(); ++i) {

            // grab the i-th sink
            Operator sink = sinkListOperators.get(i);

            // grab the name of the i-th sink
            String sinkName = sinkTableList.get(i);

            // if this cut contains one of the sink operators
            if (cut.contains(sink)) {

                // add the sink to the frame output
                children.add(sink);

                // set the frame output as te parent
                sink.getParents().add(frameOutput);

                frameOutput.getTableList().add(sinkName);
            }
        }

        // create a sink list from just the frame output
        ArrayList<Operator> sinkList = new ArrayList<>();
        sinkList.add(frameOutput);

        /*
         * 3. Renaming
		 */
        for (Operator operator : cut) {
            operator.clearRenamingInfo();
        }

        PostProcessor processor = new PostProcessor(sinkList, translatorHelper);

        processor.removeRedundant(new ArrayList<>(cut));
        processor.mergeDuplicates(frameOutput, sourceOperators);
        processor.renaming();

        children = frameOutput.getChildren();

        /*
         * 4. Projection copying
         */
        for (int i = 0; i < children.size(); i++) {
            Operator temp = children.get(i);
            //we need to create another operator to replace this operator if this operator is used by other operators
            if (temp.getParents().size() > 1 && temp instanceof Projection) {
                temp.removeParent(frameOutput);

                Projection projection = new Projection(temp.getNodeName() + "_temp",
                        copyOperatorList(temp.getChildren()), new ArrayList<>(), copyStringList(((Projection) temp).getProjectedNameList()));

                children.remove(i);
                children.add(i, projection);
                projection.addParent(frameOutput);
                temp.getChildren().get(0).addParent(projection);
            }
        }

        // remove all table scans from the source operators
        for (Operator o : cut) {
            if (o instanceof TableScan) {
                sourceOperators.remove(o);
            }
        }

        return sinkList;
    }

    private void updateRequiredTables(Set<Operator> cut) {

        for (Operator o : cut) {
            if (o instanceof TableScan) {
                TableScan ts = (TableScan) o;

                // the table is one of the required tables
                if (requiredTables.containsKey(ts.getTableName())) {

                    // reduce the reference count
                    requiredTables.replace(ts.getTableName(), requiredTables.get(ts.getTableName()) - 1);
                }
            }
        }
    }

    /**
     * Detaches the child operator from it's edge. If necessary it might wrap the child in a projection.
     * The output of the child operator is forwarded to the frameOutput
     *
     * @param edgeNodeList       the list of the edge nodes
     * @param cut                all the operators in this cut
     * @param children           the list of children in the frame output
     * @param tableList          the list of tables in the frame output
     * @param frameOutput        the frame output
     * @param edge               the edge
     * @param child              the child
     * @param projectedOperators the projections that are associated with the child operator
     */
    private void detachChild(LinkedHashSet<Operator> edgeNodeList,
                             Set<Operator> cut,
                             ArrayList<Operator> children,
                             ArrayList<String> tableList,
                             FrameOutput frameOutput,
                             Operator edge,
                             Operator child, HashMap<String, Projection> projectedOperators) {

        // if we have a projection as a node
        if (child instanceof Projection) {

            if (!projectedOperators.keySet().contains(child.getNodeName())) {
                projectedOperators.put(child.getNodeName(), (Projection) child);
            }

            decoupleGraph(child, projectedOperators, edge, tableList, children, frameOutput);
        }
        // else we got something else so we need to figure it out
        else {
            // checks if this operator is wrapped by a sequence of operators if true it will return a projection
            // we are searching for something like this (child)-(edge)-(op1)...(op_n)-(projection)
            // the parents of this projection can not be in the cut because otherwise all of it's indirect children
            // are in the cut which means that the edge is not an edge..
            LinkedList<Operator> nodes = checkForWrapper(child);

            // we found a wrapper
            if (nodes != null) {
                // remove this edge...
                edgeNodeList.remove(edge);

                // add all edges..
                if (nodes.getLast().getParents().size() != 0) {

                    // Add all the parents
                    edgeNodeList.addAll(nodes.getLast().getParents());

                    // add the operator to the projected operator
                    if (!projectedOperators.keySet().contains(child.getNodeName())) {
                        projectedOperators.put(nodes.getLast().getNodeName(), (Projection) nodes.getLast());
                    }

                    // decouple the graph for all the edges...
                    for (Operator newEdge : new ArrayList<>(nodes.getLast().getParents())) {
                        decoupleGraph(nodes.getLast(), projectedOperators, newEdge, tableList, children, frameOutput);
                    }
                }

                cut.addAll(nodes);
            } else {
                // ok we need to wrap this operator with a projection
                // if it's not already wrapped
                if (!projectedOperators.keySet().contains(child.getNodeName())) {
                    projectedOperators.put(child.getNodeName(), (Projection) wrapOperator(child, edge));
                }
                // we already have the operator just do a rewiring...
                else {
                    Operator projection = projectedOperators.get(child.getNodeName());

                    // rewire
                    child.removeParent(edge);
                    projection.addParent(edge);
                    edge.replaceChild(child, projection);
                }

                // decouple the graph
                decoupleGraph(child, projectedOperators, edge, tableList, children, frameOutput);
            }
        }
    }

    public LinkedList<Operator> getSinkListOperators() {
        return sinkListOperators;
    }

    private Operator wrapOperator(Operator child, Operator edge) {

        Projection projection = new Projection("wrapped" + detachedNodesCounter, new ArrayList<>(), new ArrayList<>());

        // link the plan
        projection.getChildren().add(child);

        // rewire the edge
        child.replaceParent(edge, projection);
        projection.addParent(edge);
        edge.replaceChild(child, projection);

        // set projected names
        projection.setProjectedNameList(child.getOutputAttributeNames());

        return projection;
    }

    private LinkedList<Operator> checkForWrapper(Operator child) {

        LinkedList<Operator> operators = new LinkedList<>();

        // add the child to the operator list
        operators.add(child);

        // set it as the current operator
        Operator current = child;

        while (true) {

            // if this is a projection we are done
            if (current instanceof Projection) {
                return operators;
            }

            // if we don't have any operator wrapping this one return null
            if (current.getParents().size() != 1) {
                return null;
            }

            // if the parent has other children there is no wrapper
            if (current.getParents().get(0).getChildren().size() != 1) {
                return null;
            }

            // go to the next parent
            current = current.getParents().get(0);

            // add the current to the operators
            operators.add(current);
        }
    }

    private void getBestCut(LinkedHashSet<Operator> edgeNodeList, Set<Operator> cut) {
        // smallest cut
        int smallestCut = Integer.MAX_VALUE;

        LinkedList<Operator> bestEdgeNodeList = new LinkedList<>();
        Set<Operator> bestCut = new HashSet<>();

        // do this for each source operator until we find one that is good or just grab the best one...
        for (int i = 0; i < sourceOperators.size(); ++i) {

            // clear the cut set
            cut.clear();

            // clear the edge node list
            edgeNodeList.clear();

            // the operator we start with
            Operator source = sourceOperators.getFirst();

            // remove the first
            sourceOperators.removeFirst();

            // add the source operator
            cut.add(source);

            // all the parents are edges
            edgeNodeList.addAll(source.getParents());

            // select a cut for this source node if we haven't made a cut...
            selectCut(edgeNodeList, cut);

            // if we have a smaller one store it
            if (smallestCut > cut.size()) {

                // remove the cut
                bestEdgeNodeList.clear();
                bestCut.clear();

                // set this as the best candidate
                bestEdgeNodeList.addAll(edgeNodeList);
                bestCut.addAll(cut);

                // set this as the smallest cut
                smallestCut = cut.size();
            }

            // this has to be either the last cut or in the bounds
            if (cut.size() <= UPPER_OPERATOR_BOUND && (cut.size() >= LOWER_OPERATOR_BOUND || edgeNodeList.isEmpty())) {
                break;
            }

            // return it
            sourceOperators.addLast(source);
        }

        // clear the cut set and edge node list
        cut.clear();
        edgeNodeList.clear();

        // set the best candidates
        cut.addAll(bestCut);
        edgeNodeList.addAll(bestEdgeNodeList);
    }

    private void selectCut(LinkedHashSet<Operator> edgeNodeList, Set<Operator> cut) {

        // the plan we want
        double lowestCost = Double.MAX_VALUE;
        HashSet<Operator> bestCut = new HashSet<>(cut);
        LinkedHashSet<Operator> bestEdges = new LinkedHashSet<>(edgeNodeList);

        // if we haven't exceeded the upper bound of the operators
        while (cut.size() < UPPER_OPERATOR_BOUND) {

            // this operator is the best parent to add
            Operator bestEdge = null;

            // the direct and indirect children of the best edge
            Set<Operator> consequentialChildrenOfBestEdge = null;

            // to lazy to do a proper description
            LinkedList<Operator> bestNewEdges = null;

            // the cost of the best parent
            double bestCost = Double.MAX_VALUE;

            // figure out best operator
            for (Operator edge : edgeNodeList) {

                // the seed operators that were added
                LinkedList<Operator> addedSeedOperators = new LinkedList<>();

                // the children that are the consequence of adding this operator
                Set<Operator> consequentialChildren = getConsequentialChildrenWithoutSeedDependencies(cut, edge, addedSeedOperators);

                // make a cut from consequential children and cut
                HashSet<Operator> mergedCut = new HashSet<>(cut);
                mergedCut.addAll(consequentialChildren);

                // the seed operators we already visited
                HashSet<Operator> visitedSeeds = new HashSet<>();

                while (!addedSeedOperators.isEmpty() && mergedCut.size() < UPPER_OPERATOR_BOUND) {

                    LinkedList<Operator> newSeedOperators = new LinkedList<>();

                    // process each seed operator we just added
                    for(Operator seed : addedSeedOperators) {

                        // if we have processed this operator we skip it
                        if(visitedSeeds.contains(seed)) {
                            continue;
                        }

                        // the operators that must be added as a result of this seed
                        HashSet<Operator> tmp = seedDependents(seed, mergedCut, newSeedOperators);

                        // add
                        mergedCut.addAll(tmp);
                        consequentialChildren.addAll(tmp);
                    }

                    // add all the seeds we just visited to the visited operators
                    visitedSeeds.addAll(addedSeedOperators);

                    // the seed operators we just added
                    addedSeedOperators = newSeedOperators;
                }

                // if the upper bound has been reached makes no sense to use this
                if(mergedCut.size() >= UPPER_OPERATOR_BOUND) {
                    continue;
                }

                // get all edges induced by the parent
                LinkedList<Operator> newEdges = getEdges(cut, consequentialChildren, edge);

                // the cost of the cut
                double cost = calculateCost(cut, consequentialChildren, edge, newEdges);

                // we have a better edge
                if (cost < bestCost) {
                    bestCost = cost;
                    consequentialChildrenOfBestEdge = consequentialChildren;
                    bestEdge = edge;
                    bestNewEdges = newEdges;
                }
            }

            // if we don't have any edge to add
            if (bestEdge == null) {
                break;
            }

            // add the best edge to the cut
            cut.add(bestEdge);
            cut.addAll(consequentialChildrenOfBestEdge);

            // remove all edges that were recently added
            edgeNodeList.removeAll(consequentialChildrenOfBestEdge);

            // remove it from the edge list
            edgeNodeList.remove(bestEdge);
            edgeNodeList.addAll(bestNewEdges);

            System.out.println("Edge added : " + bestEdge.getNodeName());
            System.out.println("Cost : " + bestCost);

            // if we have found a bigger and better plan we want to use it!
            if (cut.size() >= LOWER_OPERATOR_BOUND && lowestCost >= bestCost) {
                lowestCost = bestCost;
                bestCut = new HashSet<>(cut);
                bestEdges = new LinkedHashSet<>(edgeNodeList);
            }
        }

        // set the cut
        cut.clear();
        cut.addAll(bestCut);

        // set the edges
        edgeNodeList.clear();
        edgeNodeList.addAll(bestEdges);


        System.out.println("Cut cost : " + lowestCost + " \nNext Cut!");
    }

    private HashSet<Operator> getOperatorsWithSeed(Operator seed,
                                                   Set<Operator> cut,
                                                   LinkedList<Operator> addedSeedOperators) {

        HashSet<Operator> retVal = new HashSet<>();
        LinkedList<Operator> operatorsToVisit = new LinkedList<>();
        HashSet<Operator> visitedNodes = new HashSet<>();

        // add the vg as one of the operators to visit
        operatorsToVisit.add(seed);

        // while we have operators to transverse
        while (!operatorsToVisit.isEmpty()) {

            // the operator we want to check parents for...
            Operator c = operatorsToVisit.getFirst();

            for (Operator p : c.getParents()) {

                // if one of the parents has a seed attribute we want to use this node
                if (hasSeedAttribute(p)) {
                    retVal.add(p);

                    // if the parent is not in the operators in to visit
                    if (!operatorsToVisit.contains(p) && !visitedNodes.contains(p)) {
                        operatorsToVisit.addLast(p);
                    }
                }
            }

            visitedNodes.add(c);
            operatorsToVisit.removeFirst();
        }

        // some operators might be without their dependencies so we have to fix this...
        for (Operator r : new ArrayList<>(retVal)) {
            if(r != seed) {
                Set<Operator> toAdd =  getConsequentialChildrenWithoutSeedDependencies(cut, r, addedSeedOperators);
                cut.addAll(toAdd);
                retVal.addAll(toAdd);
            }
        }

        return retVal;
    }

    private boolean hasSeedAttribute(Operator p) {

        if(p instanceof UnionView) {
            return true;
        }

        try {
            for (String s : p.getOutputAttributeNames()) {
                if (s.contains("seed")) {
                    return true;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private void decoupleGraph(Operator child,
                               HashMap<String, Projection> projectedOperators,
                               Operator edge,
                               ArrayList<String> tableList,
                               ArrayList<Operator> children,
                               FrameOutput frameOutput) {

        Operator projection = projectedOperators.get(child.getNodeName());

        if (!children.contains(projection)) {
            // add it to the table scan
            String tableName = "projected" + projectedNodesCounter++;
            tableList.add(tableName);
            tableNameForChild.put(child.getNodeName(), tableName);
            children.add(projection);

            // replace the edge with the frame output
            projection.replaceParent(edge, frameOutput);

            // we have a table
            requiredTables.put(tableName, 0);
        } else {
            projection.removeParent(edge);
        }

        // crate a new table scan
        TableScan scan = new TableScan("detached" + detachedNodesCounter++, new ArrayList<>(), new ArrayList<>());

        // link it to the operator
        edge.replaceChild(projection, scan);
        scan.getParents().add(edge);

        // set the name of the table in the table scan
        String tableName = tableNameForChild.get(child.getNodeName());
        scan.setTableName(tableName);

        ArrayList<String> attributeList = new ArrayList<>();

        // fill-up the attribute list
        attributeList.addAll(((Projection) projection).getProjectedNameList());

        // set it...
        scan.setAttributeList(attributeList);

        // we have one more reference to the table
        requiredTables.replace(tableName, requiredTables.get(tableName) + 1);

        // put it to the source operators
        sourceOperators.add(scan);
    }


    /**
     * Calculates the cost of the selected cut.
     * The algorithm is the following :
     * Go through all the edges (the already existing edges and the parent induced edges)
     * If a parent induced edge is a "straight edge" (the edge has one parent that has only one child)
     * and it's parent is also a "straight" edge we calculate the cost as if we were to cut on this edge
     * if that parent also has a parent that is a straight edge we calculate the cost again, we do that until the
     * next parent is not a straight edge. We select the smallest cost and instead of the first edge we cut at the that
     * operator. If there are multiple operators with the same cost we use the first one we found!
     *
     * @param cut                   The current cut
     * @param consequentialChildren the children that are a consequence of adding the edge
     * @param edge                  the edge we are adding
     * @param parentInducedEdges    the edges that are induced by adding the observed edge  @return the cost of the cut
     */
    private double calculateCost(Set<Operator> cut,
                                 Set<Operator> consequentialChildren,
                                 Operator edge,
                                 LinkedList<Operator> parentInducedEdges) {

        // the operators that are going to the edges
        HashSet<Operator> danglingOperators = new HashSet<>();

        // grab the dangling operators from the parentInducedEdges
        for (Operator e : parentInducedEdges) {
            for (Operator o : e.getChildren()) {
                if (cut.contains(o) || consequentialChildren.contains(o) || o == edge) {
                    danglingOperators.add(o);
                }
            }
        }

        // the total cost of the cut
        double cost = 0;

        // sum up the cost
        for (Operator d : danglingOperators) {
            cost += getCost(cut, consequentialChildren, edge, d);
        }

        return cost;
    }

    /**
     * Gets the cost of the dangling operator.
     *
     * @param cut                   the current cut
     * @param consequentialChildren the consequential children of adding the edge
     * @param edge                  the edge we are adding
     * @param d                     the dangling operator
     * @return the cost
     */
    private double getCost(Set<Operator> cut, Set<Operator> consequentialChildren, Operator edge, Operator d) {

        // the cost materializing this operator
        double cost = 0;

        // number of operators outside the cut
        double n = 0;

        for (Operator p : d.getParents()) {

            // figure out cost for this
            double value = costModel.getCostFor(d, p);

            if (!cut.contains(p) || !consequentialChildren.contains(p) || edge == p) {
                cost += value;
                n++;
            }
        }

        // increase the cost
        return cost / Math.exp(n - 1);
    }

    /**
     * Finds all the edges in this cut
     *
     * @param cut                   the cut
     * @param consequentialChildren the operators we want to add to the cut
     * @param parent                the parent
     * @return the edge operators
     */
    private LinkedList<Operator> getEdges(Set<Operator> cut, Set<Operator> consequentialChildren, Operator parent) {

        // edges
        HashSet<Operator> edges = new HashSet<>();

        // proposed cut
        HashSet<Operator> proposedCut = new HashSet<>();
        proposedCut.addAll(cut);
        proposedCut.addAll(consequentialChildren);
        proposedCut.add(parent);

        // for each of the consequential children
        for (Operator o : proposedCut) {

            // grab a parent
            for (Operator p : o.getParents()) {

                // if the parent is not in the cut or is not the parent or is one of consequentialChildren we are considering adding add it to the edges.
                if (!proposedCut.contains(p)) {
                    edges.add(p);
                }
            }
        }

        return new LinkedList<>(edges);
    }

    private Operator rewireTableScan(Operator o, Operator p) throws Exception {

        Operator cp = o.copy(copyHelper);

        // rewire the parent
        p.replaceChild(o, cp);
        o.removeParent(p);

        // rewire the table scan
        cp.getParents().clear();
        cp.addParent(p);

        return cp;
    }

    private Set<Operator> getConsequentialChildrenWithoutSeedDependencies(Set<Operator> cut, Operator parent, LinkedList<Operator> addedSeedOperators) {

        HashSet<Operator> consequentialChildren = new HashSet<>();
        LinkedList<Operator> toVisit = new LinkedList<>();
        toVisit.add(parent);

        // if we are adding an edge that is a seed we need to add all the dependent tables that have a seed into the cut..
        if(parent instanceof Seed) {
            addedSeedOperators.add(parent);
        }

        while (!toVisit.isEmpty()) {

            // operator we are visiting
            Operator o = toVisit.getFirst();

            // add the child
            toVisit.addAll(o.getChildren());

            // go through children
            for (Operator c : o.getChildren()) {

                if (!cut.contains(c) && !consequentialChildren.contains(c)) {
                    consequentialChildren.add(c);

                    // if we are dealing with a seed operator we grab all the operators that have
                    if (c instanceof Seed) {
                        addedSeedOperators.add(c);
                    }
                }
            }

            // remove the visited operator..
            toVisit.removeFirst();
        }

        return consequentialChildren;
    }


    /**
     * Returns all the operators that have a seed attribute but are not in the cut
     *
     * @param c                     the seed operator
     * @param cut                   the operators in the current ut
     * @param addedSeedOperators    the seed operators added by dependents
     * @return the operators
     */
    private HashSet<Operator> seedDependents(Operator c, HashSet<Operator> cut, LinkedList<Operator> addedSeedOperators) {

        HashSet<Operator> operators = getOperatorsWithSeed(c, cut, addedSeedOperators);
        HashSet<Operator> ret = new HashSet<>();

        // add this operator to the consequential children...
        for (Operator o : operators) {
            if (cut.contains(o)) {
                ret.add(o);
            }
        }

        return ret;
    }


    /**
     * Generates the bipartite graph used for graph cutting optimization
     *
     * @param tableGraph     an instance of the table graph so we can tack table dependencies
     * @param requiredTables tables that are required by the requested queries
     * @throws Exception if the root of any random table query is not a projection!
     */
    private void generateBipartiteGraph(TableGraph tableGraph, LinkedList<TimeTableNode> requiredTables) throws Exception {

        // makes an operator instance for every table in the table graph (generate partial plans...)
        instantiateAllOperators(tableGraph);

        // link the unconnected graphs (link all the partial plans generated in the previous step)
        linkPartialPlans();

        // figure out the sinks (the tables we need to execute the query)
        findSinks(requiredTables);

        // link the final queries (the one the user requested)
        linkFinalQueries();

        // fix-up the source operators
        fixSourceOperators();
    }

    private void fixSourceOperators() throws Exception {

        LinkedList<Operator> newSources = new LinkedList<>();

        // go through all the source operators
        for (Operator o : sourceOperators) {

            // if this table scan has more than one
            if (o.getParents().size() > 1) {
                for (Operator p : o.getParents()) {
                    // add the source
                    newSources.add(rewireTableScan(o, p));
                }
            }
        }

        // add all sources
        sourceOperators.addAll(newSources);
    }

    /**
     * Finds all the sinks
     *
     * @param requiredTables tables that are required by the requested queries
     */
    private void findSinks(LinkedList<TimeTableNode> requiredTables) {

        // go through each table in the sink list
        for (TimeTableNode t : requiredTables) {

            // get the it's qualified table name
            String tableName = t.getQualifiedTableName();

            // add it to the sink list operators
            sinkListOperators.add(generatedPlanMap.get(tableName));
            sinkTableList.add(tableName);
        }
    }

    /**
     * Link the final queries
     */
    private void linkFinalQueries() throws Exception {

        for (int j = 0; j < queries.size(); j++) {

            // grab the first operator
            Operator tempSink = queries.get(j);

            // copy the operator
            tempSink = tempSink.copy(copyHelper);

            // find all referenced tables in this operator
            ArrayList<TableScan> replacedTableList = PlanHelper.findReferencedRandomTable(tempSink);

            // for each table referenced in the operator
            for (TableScan tableScan : replacedTableList) {

                // get the name of the referenced table
                String tableName = tableScan.getTableName();

                // grab the linked operator
                Operator linkedOperator = generatedPlanMap.get(tableName);

                // if we by any chance can't find the operator we need to link to this query, we have a problem
                if (linkedOperator == null) {
                    throw new RuntimeException("The SQL contains " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName) + " is not supported");
                } else {
                    // link the operator with the query
                    integratePlan(tableScan, linkedOperator);

                    // if the operator is not in the linked operator list add him
                    if (!linkedOperatorList.contains(linkedOperator)) {
                        linkedOperatorList.add(linkedOperator);
                    }
                }
            }

            // check if this is a materialized view
            if (tempSink instanceof FrameOutput) {
                ArrayList<Operator> childrenList = tempSink.getChildren();
                sinkListOperators.addAll(childrenList);
                sinkTableList.addAll(((FrameOutput) tempSink).getTableList());

                for (Operator aChildrenList : childrenList) {
                    aChildrenList.removeParent(tempSink);
                }

                tempSink.clearLinks();
            }
            // otherwise it's a select_from_where query
            else {
                sinkListOperators.add(tempSink);
                sinkTableList.add("file_to_print_0_" + j);
            }
        }
    }

    private void instantiateAllOperators(TableGraph tableGraph) throws Exception {
        // while we haven't used up all the tables to instantiate their operators
        while (tableGraph.hasNextTable()) {

            // find a leaf node
            TimeTableNode node = tableGraph.getNextTable();

            // grab the qualified table name
            String table = node.getQualifiedTableName();

            // add the table to the list
            tpList.add(table);

            Operator operator;

            // figure out the operator
            if (tableOperationMap.containsKey(table)) {
                operator = tableOperationMap.get(table);
                generatedPlanMap.put(table, operator);
            } else {
                String randomTable = findMatchingGeneralIndexTable(table);
                operator = tableOperationMap.get(randomTable);
            }

            CopyHelper copyHelper = new CopyHelper();

            // copy the operator
            operator = operator.copy(copyHelper);

            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);

            // make the random table operator a concrete operator for the given table with the given indices...
            instantiateOperator(table, operator, indices, generatedPlanMap, replacedPlanMap, sourceOperators);
        }
    }

    private void linkPartialPlans() throws Exception {

        // go through each table from back to front (the most dependent one to the least dependent one)
        for (int index = tpList.size() - 1; index >= 0; index--) {

            // grab the table name
            String table = tpList.get(index);

            // the operator for this table
            Operator hostOperator = generatedPlanMap.get(table);

            // store the host operator if we haven't done so already
            if (!hostOperatorList.contains(hostOperator)) {
                hostOperatorList.add(hostOperator);
            }

            // get all the tables that were replaced for this operator
            ArrayList<TableScan> replacedTableList = replacedPlanMap.get(table);

            // if there are tables that were replaced
            if (replacedTableList != null) {

                // go through each of them
                for (TableScan tableScan : replacedTableList) {

                    // extract their table name
                    String tableName = tableScan.getTableName();

                    // grab it from the instantiated operators
                    Operator linkedOperator = generatedPlanMap.get(tableName);

                    // integrate them together
                    integratePlan(tableScan, linkedOperator);

                    // add the linked operator to the linked operators if its not already there
                    if (!linkedOperatorList.contains(linkedOperator)) {
                        linkedOperatorList.add(linkedOperator);
                    }
                }
            }
        }
    }

    private void integratePlan(TableScan tableScan, Operator linkedOperator) throws Exception {

        // grab the attributes from the table scan...
        ArrayList<String> new_attributeList = tableScan.getAttributeList();
        Operator newOperator;

        // if the instance is a projection (it must be)
        if (linkedOperator instanceof Projection) {

            // list of old attributes of the projection (they are currently set)
            ArrayList<String> old_attributeList = ((Projection) linkedOperator).getProjectedNameList();

            // we change their name to match the attributes from the table scan
            newOperator = changeAttributeName(linkedOperator, old_attributeList, new_attributeList);

            // go through all the parents that depend on this table scan
            ArrayList<Operator> parentList = tableScan.getParents();
            for (Operator aParentList : parentList) {

                // add it as a parent of the new operator
                newOperator.addParent(aParentList);

                // replace the table scan in the list of children with the new operator
                aParentList.replaceChild(tableScan, newOperator);
            }

            // clear the links of the table scan so we don't bother with it anymore
            tableScan.clearLinks();
        } else {
            throw new Exception("The root of an random table plan is not projection!");
        }
    }


    private Operator changeAttributeName(Operator originalElement,
                                         ArrayList<String> old_attributeNameList,
                                         ArrayList<String> new_attributeNameList) throws Exception {
        if (old_attributeNameList == null ||
                new_attributeNameList == null ||
                old_attributeNameList.size() != new_attributeNameList.size()) {
            throw new Exception("change attribute error!");
        }
        /*
         * Scalar function followed by projection
		 */
        if (old_attributeNameList.size() != 0) {
			/*
			 * 1. Scalar function on the new defined attribute due to the
			 * definition of the schema.
			 */

			/*
			 * The data structure in the ScalarFunction node.
			 */
            String nodeName = "node" + translatorHelper.getInstantiateNodeIndex();
            ArrayList<Operator> children = new ArrayList<>();
            ArrayList<Operator> parents = new ArrayList<>();
            ArrayList<MathOperator> scalarExpressionList = new ArrayList<>();
            HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<>();
            HashMap<MathOperator, String> outputMap = new HashMap<>();

			/*
			 * 1.1. Fill the translatedStatement in the ScalarFunction.
			 */
            for (int i = 0; i < old_attributeNameList.size(); i++) {
				/*
				 * 1.2. Fill in the scalarFunction with the concrete
				 * MathFunction Here it should be EFunction.
				 */
                scalarExpressionList.add(new EFunction());
            }

			/*
			 * It comes to the attribute set of each function. Since one scalar
			 * function can have multiple functions, with each function can have
			 * multiple involved attributes. However, since the scalar function
			 * only plays the role of renaming, each scalar function has only
			 * one attribute.
			 */

            ArrayList<String> tempList;
            for (int i = 0; i < old_attributeNameList.size(); i++) {
				/*
				 * 1.3. Fill each functions in the ScalarFunction with involved
				 * attributes.
				 */
                tempList = new ArrayList<>();
                tempList.add(old_attributeNameList.get(i));
                columnListMap.put(scalarExpressionList.get(i), tempList);
            }

            for (int i = 0; i < new_attributeNameList.size(); i++) {
				/*
				 * 1.4. Fill each functions in the ScalarFunction with an output
				 */
                outputMap.put(scalarExpressionList.get(i), new_attributeNameList.get(i));
            }

			/*
			 * 1.5. Fill in the children
			 */
            children.add(originalElement);

			/*
			 * 1.6 Create the current scalar function node.
			 */
            ScalarFunction scalarFunction = new ScalarFunction(nodeName, children, parents, translatorHelper);
            scalarFunction.setScalarExpressionList(scalarExpressionList);
            scalarFunction.setColumnListMap(columnListMap);
            scalarFunction.setOutputMap(outputMap);

			/*
			 * 1.7 This translatedElement add current Node as parent
			 */
            originalElement.addParent(scalarFunction);

			/*
			 * 2. Projection on the result attribute
			 */
			/*
			 * 2.1 Create the data structure of the Projection
			 */
            Projection projection;
            nodeName = "node" + translatorHelper.getInstantiateNodeIndex();
            children = new ArrayList<>();
            parents = new ArrayList<>();
            ArrayList<String> projectedNameList = new ArrayList<>();

			/*
			 * 2.2 Fill the tranlsatedResult.
			 */
            for (String aNew_attributeNameList : new_attributeNameList) {
                /*
				 * 2.3 Fill the projectedNameList
				 */
                projectedNameList.add(aNew_attributeNameList);
            }

			/*
			 * 2.4 Fill the children
			 */
            children.add(scalarFunction);

			/*
			 * 2.5 Create the current projection node.
			 */
            projection = new Projection(nodeName, children, parents,
                    projectedNameList);
			/*
			 * 2.6 "scalarFunction" fills it parents with the projection.
			 */
            scalarFunction.addParent(projection);
            return projection;
        } else {
            return originalElement;
        }
    }


    /**
     * Instantiates an operator to he specified indices
     * TODO handle union view
     *
     * @param rootTable
     * @param operator         the operator we ant to instantiate
     * @param indices
     * @param generatedPlanMap
     * @param replacedPlanMap
     * @param sourceOperators
     * @return
     */
    private Operator instantiateOperator(String rootTable,
                                         Operator operator,
                                         HashMap<String, Integer> indices,
                                         HashMap<String, Operator> generatedPlanMap,
                                         HashMap<String, ArrayList<TableScan>> replacedPlanMap, LinkedList<Operator> sourceOperators) {

        ArrayList<Operator> sinkList = new ArrayList<Operator>();
        sinkList.add(operator);
        ArrayList<Operator> allOperators = Topologic.findAllNode(sinkList);
        UnionView unionView = Topologic.findUnionVIew(allOperators);

        if (unionView != null) {
            HashSet<String> referencedTables = ruleMap.get(MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(rootTable));

            //get the tables that have been already in the plan.
            HashSet<String> alreadyInPlanTables = new HashSet<>();
            for (Operator currentElement : allOperators) {
                if (currentElement instanceof TableScan) {
                    String tableName = ((TableScan) currentElement).getTableName();
                    if (((TableScan) currentElement).getType() != TableReference.COMMON_TABLE) {
                        if (isGeneralTable(tableName)) {
                            HashMap<String, MathExpression> expressions = ((TableScan) currentElement).getIndexMathExpressions();
                            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(tableName);
                            alreadyInPlanTables.add(MultidimensionalTableSchema.getBracketsTableNameFromEvaluatedExpressions(prefix, expressions, indices));
                        } else {
                            alreadyInPlanTables.add(MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName));
                        }
                    }
                }
            }

            //create the plan for the remaining tables.
            if (referencedTables != null) {
                String qualifiedTableName;
                Translator translator = new Translator(translatorHelper);

                ArrayList<Operator> tmp = new ArrayList<>();

                for (String tableName : referencedTables) {
                    if (!alreadyInPlanTables.contains(tableName)) {
                        qualifiedTableName = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(tableName);
                        String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(qualifiedTableName);
                        HashMap<String, Integer> referencedIndices = MultidimensionalTableSchema.getIndicesFromQualifiedName(qualifiedTableName);

                        View view;
                        try {
                            view = SimsqlCompiler.catalog.getView(qualifiedTableName);
                        } catch (Exception e) {
                            throw new RuntimeException("exception in generating the plans for a union view");
                        }

                        TableReference tempReference = new TableReference(prefix, qualifiedTableName, referencedIndices, TableReference.CONSTANT_INDEX_TABLE);
                        Operator translatorElement = translator.sqlExpressionTranslator.indexTableScan(view, qualifiedTableName, tempReference);

                        if (translatorElement instanceof Projection) {
                            unionView.getInputAttributeNameList().addAll(((Projection) translatorElement).getProjectedNameList());
                        } else {
                            throw new RuntimeException("The children of UnionView operator should be Projection");
                        }

                        tmp.add(translatorElement);

                        ArrayList<DataType> outputAttributeTypeList = new ArrayList<>();
                        ArrayList<Attribute> realAttributes = view.getAttributes();
                        for (Attribute realAttribute : realAttributes) {
                            outputAttributeTypeList.add(realAttribute.getType());
                        }

                        unionView.addChild(translatorElement);
                        unionView.getInputAttributeTypeList().addAll(outputAttributeTypeList);
                        translatorElement.addParent(unionView);
                    }
                }
            }
        }

        // the set of all the operators we already have visited
        HashSet<Operator> finishedQueue = new HashSet<>();

        // the list of all the operators that are still unprocessed
        LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<>();

        // add the operator we want to instantiate
        availableQueue.add(operator);

        // while there are no more operators that are unprocessed
        while (!availableQueue.isEmpty()) {

            // grab the first available operator
            Operator currentElement = availableQueue.poll();

            // check if we have processed it already, if we have just skip it
            if (!finishedQueue.contains(currentElement)) {

                // change the indices of the current operator
                currentElement.changeNodeProperty(indices, translatorHelper);

                // add the current operator to the set of processed operators so we don't visit him again
                finishedQueue.add(currentElement);

                // if the current operator is a table scan operator
                if (currentElement instanceof TableScan) {

                    // if this is not a normal table
                    if (((TableScan) currentElement).getType() != TableReference.COMMON_TABLE) {

                        // grab it's table name
                        String tableName = ((TableScan) currentElement).getTableName();

                        // check if it's a general index table
                        if (isGeneralTable(tableName)) {
                            HashMap<String, MathExpression> expressions = ((TableScan) currentElement).getIndexMathExpressions();
                            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(tableName);

                            HashMap<String, Integer> newIndices = MultidimensionalTableSchema.evaluateExpressions(expressions, indices);

                            ((TableScan) currentElement).setTableName(MultidimensionalTableSchema.getQualifiedTableNameFromIndices(prefix, newIndices));
                            ((TableScan) currentElement).setIndexStrings(newIndices);

                            /*
                             * Keep the state of current table scan, which should be replaced in the integrated plan.
                             */
                            putIntoMap(replacedPlanMap, rootTable, (TableScan) currentElement);
                        } else {
                            putIntoMap(replacedPlanMap, rootTable, (TableScan) currentElement);
                        }
                    }
                    // this is a common table
                    else {

                        // add the operator
                        sourceOperators.add(currentElement);
                    }
                }

                // get the children of the current operator
                ArrayList<Operator> children = currentElement.getChildren();

                // if it has some children add them to the available queue
                if (children != null) {
                    for (Operator temp : children) {
                        if (!finishedQueue.contains(temp)) {
                            availableQueue.add(temp);
                        }
                    }
                }
            }
        }

        generatedPlanMap.put(rootTable, operator);
        return operator;
    }

    private String findMatchingGeneralIndexTable(String table) {

        if (table.matches("^[^_]+(_[0-9]+){2,}$")) {
            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(table);
            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);

            for (String randomTable : tableOperationMap.keySet()) {
                if (randomTable.startsWith(prefix)) {
                    MultidimensionalSchemaIndices indexSpecification = new MultidimensionalSchemaIndices(randomTable);

                    if (indexSpecification.areIndicesForThisTable(indices)) {
                        return randomTable;
                    }
                }
            }

            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        String randomTableName = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(table) + "_i";

        if (!tableOperationMap.containsKey(randomTableName)) {
            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        return randomTableName;
    }

    private void putIntoMap(HashMap<String, ArrayList<TableScan>> replacedPlanMap, String key, TableScan value) {
        if (replacedPlanMap.containsKey(key)) {
            ArrayList<TableScan> tempList = replacedPlanMap.get(key);
            if (!tempList.contains(value)) {
                tempList.add(value);
            }
        } else {
            ArrayList<TableScan> tempList = new ArrayList<>();
            tempList.add(value);
            replacedPlanMap.put(key, tempList);
        }
    }

    /**
     * Gets the prefix of the table
     * TODO move this somewhere
     *
     * @param table the full name of the table in brackets form
     * @return the prefix
     */
    private String getTablePrefix(String table) {
        // find hte '[' character
        int start = table.indexOf("[");

        // if there brackets the prefix is the same as the name
        if (start < 0) {
            return table;
        }

        // return the prefix
        return table.substring(0, start);
    }

    private static ArrayList<Operator> copyOperatorList(ArrayList<Operator> list) {
        ArrayList<Operator> c_list = new ArrayList<Operator>(list.size());
        for (Operator aList : list) {
            c_list.add(aList);
        }
        return c_list;
    }

    private static ArrayList<String> copyStringList(ArrayList<String> list) {
        ArrayList<String> c_list = new ArrayList<String>(list.size());
        for (String aList : list) {
            c_list.add(aList);
        }
        return c_list;
    }
}
