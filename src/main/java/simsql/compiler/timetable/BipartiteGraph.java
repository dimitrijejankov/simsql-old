package simsql.compiler.timetable;

import simsql.compiler.*;
import simsql.compiler.expressions.MathExpression;
import simsql.compiler.math_operators.EFunction;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingDeque;

import static simsql.compiler.MultidimensionalTableSchema.getPrefixFromBracketsTableName;
import static simsql.compiler.MultidimensionalTableSchema.isGeneralTable;

public class BipartiteGraph {

    /**
     * The (general index name, relational operations) pairs for general index tables.
     */
    private HashMap<String, Operator> tableOperationMap;

    /**
     * The nodes of the table graph
     */
    private HashMap<TimeTableNode, HashSet<TimeTableNode>> nodes;

    /**
     * The the (table, set of dependent tables) pairs in general index form
     */
    private HashMap<String, HashSet<String>> backwardEdges;

    /** Each random table (string) corresponds to an operator.
     *  This map contains the instantiated random operators for the tables they represent.
     */
    private HashMap<String, Operator> generatedPlanMap = new HashMap<String, Operator>();

    /** Each random table (string) has a list of tableScan which should be replaced,
     *  because they should be linked with the rest of the query plan.
     */
    private HashMap<String, ArrayList<TableScan>> replacedPlanMap = new HashMap<String, ArrayList<TableScan>>();

    /** list of tables in order they are removed for the graph
     */
    private ArrayList<String> tpList = new ArrayList<String>();

    /**
     * TODO for some reason this exists...
     */
    private ArrayList<Operator> hostOperatorList = new ArrayList<Operator>();

    /**
     * The operators we have linked...
     */
    private ArrayList<Operator> linkedOperatorList = new ArrayList<Operator>();

    /**
     * An instance of the TranslatorHelper class
     */
    private TranslatorHelper translatorHelper;

    public BipartiteGraph(LinkedList<TimeTableNode> requiredTables,
                          HashMap<String, HashSet<String>> backwardEdges,
                          HashMap<String, Operator> tableOperationMap) throws Exception {

        // store these values
        this.tableOperationMap = tableOperationMap;
        this.backwardEdges = backwardEdges;

        // init stuff
        this.translatorHelper = new TranslatorHelper();
        this.nodes = new HashMap<TimeTableNode, HashSet<TimeTableNode>>();

        // generate the table graph
        generateTableGraph(requiredTables);

        // generate the bipartite graph
        generateBipartiteGraph();
    }


    /**
     * Generates the table dependency graph for the tables that are in the final nodes list
     *
     * @param finalNodes the list of the final nodes
     */
    private void generateTableGraph(LinkedList<TimeTableNode> finalNodes) {

        LinkedList<TimeTableNode> processingList = new LinkedList<TimeTableNode>(finalNodes);
        HashSet<TimeTableNode> visitedNodes = new HashSet<TimeTableNode>();

        while (processingList.size() != 0) {

            // this is the current table we are processing
            TimeTableNode activeNode = processingList.iterator().next();

            // find the backward edges for the current table
            HashSet<String> edges = findBackwardEdge(activeNode);

            // evaluate the indices of the backward edges we just found
            HashSet<TimeTableNode> tables = evaluateDependentTables(edges, activeNode.getIndexStrings());

            // only adds not previously visited tables to the processing list
            for (TimeTableNode table : tables) {
                if (!visitedNodes.contains(table)) {
                    processingList.add(table);
                }
            }

            // adds the dependencies for the table
            nodes.put(activeNode, tables);

            // remove the current table from the list of unprocessed tables
            processingList.remove(activeNode);

            // add the current table to the list of visited nodes so we don't check it out twice...
            visitedNodes.add(activeNode);
        }
    }

    /**
     * For a given set of edges and the specified index strings calculates the index strings of the tables in the set of edges
     *
     * @param edges        the set of edges given in brackets general index form with index formulas
     * @param indexStrings the the index strings
     * @return the evaluated time table nodes
     */
    private HashSet<TimeTableNode> evaluateDependentTables(HashSet<String> edges, HashMap<String, Integer> indexStrings) {

        HashSet<TimeTableNode> retValue = new HashSet<TimeTableNode>();

        for (String edge : edges) {

            String prefix = getPrefixFromBracketsTableName(edge);
            MultidimensionalSchemaExpressions expressions = new MultidimensionalSchemaExpressions(edge.substring(edge.indexOf("[")));
            HashMap<String, Integer> edgeIndex = expressions.evaluateExpressions(indexStrings);
            TimeTableNode node = new TimeTableNode(prefix, edgeIndex);

            retValue.add(node);
        }

        return retValue;
    }

    /**
     * Finds the backward edges for a given table
     * @param node the table to find the backward edges for
     * @return the set of backward edges
     */
    private HashSet<String> findBackwardEdge(TimeTableNode node) {
        String tableName = node.getBracketsTableName();
        String tablePrefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(tableName);

        if (backwardEdges.containsKey(tableName)) {
            return backwardEdges.get(tableName);
        }

        if (backwardEdges.containsKey(tablePrefix + "[i]")) {
            return backwardEdges.get(tablePrefix + "[i]");
        }

        for (String edge : backwardEdges.keySet()) {

            if (edge.matches("^[^_]+((\\[[0-9]+to[0-9]])+|(\\[[0-9]+to])|(\\[[0-9]+]))+$")) {
                MultidimensionalSchemaIndices indices = new MultidimensionalSchemaIndices(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(edge));
                String prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(edge);

                if (indices.areIndicesForThisTable(node.getIndexStrings()) &&
                        prefix.equals(node.getTableName())) {
                    return backwardEdges.get(edge);
                }
            }
        }

        throw new RuntimeException("Failed to find an appropriate edge.");
    }

    /**
     * Generates the bipartite graph used for graph cutting optimization
     * @throws Exception if the root of any random table query is not a projection!
     */
    private void generateBipartiteGraph() throws Exception {

        // makes an operator instance for every table in the table graph (generate partial plans...)
        instantiateAllOperators();

        // link the unconnected graphs
        linkPartialPlans();
    }

    private void instantiateAllOperators() throws Exception {
        // while we haven't used up all the tables to instantiate their operators
        while(!this.nodes.isEmpty()) {

            // find a leaf node
            TimeTableNode node = getLeafNode();

            // grab the qualified table name
            String table = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(node.getBracketsTableName());

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
            instantiateOperator(table, operator, indices, generatedPlanMap, replacedPlanMap);

            // remove node from graph
            updateTableGraph(node);
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
            ArrayList<Operator> children = new ArrayList<Operator>();
            ArrayList<Operator> parents = new ArrayList<Operator>();
            ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
            HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
            HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();

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
                tempList = new ArrayList<String>();
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
            children = new ArrayList<Operator>();
            parents = new ArrayList<Operator>();
            ArrayList<String> projectedNameList = new ArrayList<String>();

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
			 * 2.6 "sclarFunction" fills it parents with the projection.
			 */
            scalarFunction.addParent(projection);
            return projection;
        } else {
            return originalElement;
        }
    }

    /**
     * Removes the node from the graph and updates the dependencies
     * @param node the node to be removed
     */
    private void updateTableGraph(TimeTableNode node) {

        // remove the node from the graph
        nodes.remove(node);

        // remove the dependency to this node from every node
        for(HashSet<TimeTableNode> n : nodes.values()) {
            n.remove(node);
        }
    }

    /**
     * Extracts a leaf node from the table graph
     * @return a leaf node name
     */
    private TimeTableNode getLeafNode() {

        // go through the graph and find a node
        for(TimeTableNode table : nodes.keySet()) {
            if(nodes.get(table).isEmpty()) {
                return table;
            }
        }

        // something went wrong this should not happen!
        throw new RuntimeException("Can't find a leaf!");
    }

    /**
     * Instantiates an operator to he specified indices
     * TODO handle union view
     *
     * @param rootTable
     * @param operator the operator we ant to instantiate
     * @param indices
     * @param generatedPlanMap
     * @param replacedPlanMap
     * @return
     */
    private Operator instantiateOperator(String rootTable,
                                         Operator operator,
                                         HashMap<String, Integer> indices,
                                         HashMap<String, Operator> generatedPlanMap,
                                         HashMap<String, ArrayList<TableScan>> replacedPlanMap) {

		// the set of all the operators we already have visited
        HashSet<Operator> finishedQueue = new HashSet<Operator>();

        // the list of all the operators that are still unprocessed
        LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();

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

        if(table.matches("^[^_]+(_[0-9]+){2,}$")) {
            String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(table);
            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);

            for(String randomTable : tableOperationMap.keySet()) {
                if(randomTable.startsWith(prefix)) {
                    MultidimensionalSchemaIndices indexSpecification = new MultidimensionalSchemaIndices(randomTable);

                    if (indexSpecification.areIndicesForThisTable(indices)){
                        return randomTable;
                    }
                }
            }

            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        String randomTableName = getTablePrefix(table) + "_i";

        if(!tableOperationMap.containsKey(randomTableName)) {
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
            ArrayList<TableScan> tempList = new ArrayList<TableScan>();
            tempList.add(value);
            replacedPlanMap.put(key, tempList);
        }
    }

    /**
     * Gets the prefix of the table
     * TODO move this somewhere
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

}
