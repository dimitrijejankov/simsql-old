/**
 * *
 * Copyright 2014 Rice University                                           *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                           *
 * *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 * *
 */

package simsql.compiler;

import simsql.compiler.boolean_operator.*;
import simsql.compiler.expressions.MathExpression;
import simsql.compiler.math_operators.*;
import simsql.compiler.operators.*;
import simsql.runtime.DataType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.LinkedBlockingDeque;

public class PlanInstantiation {
    /* randomTablePlanMap records all the plans for the Random tables. */
    private HashMap<String, Operator> randomTablePlanMap;
    private ChainGeneration chain;
    private TranslatorHelper translatorHelper;
    /*
     * mcmcDagBundledPlan is the mcmc tree bundled plan. This plan means that we run the mcmc templates, that I could run such
     * query plans simutenously. The other plans that may span multiple time ticks is left to future extension.
     */
    private HashMap<Integer, ArrayList<Operator>> mcmcDagBundledPlan;
    private HashSet<String> finalTables;

    /**
     * @param randomPlanTableMap
     * @param chain
     */
    public PlanInstantiation(HashMap<Operator, String> randomPlanTableMap,
                             ChainGeneration chain,
                             TranslatorHelper translatorHelper,
                             ArrayList<Operator> queryList) {
        super();

        randomTablePlanMap = new HashMap<String, Operator>();
        for (Operator o : randomPlanTableMap.keySet()) {
            randomTablePlanMap.put(MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(randomPlanTableMap.get(o)), o);
        }
        this.chain = chain;
        this.translatorHelper = translatorHelper;
        this.finalTables = gerReferencedTableInQueryList(queryList);
        this.mcmcDagBundledPlan = processQueryList(queryList);
    }

    public ChainGeneration getChain() {
        return chain;
    }

    public HashSet<String> getFinalTables() {
        return finalTables;
    }

    /*
             * Here, dataFromCatalog denotes whether we should get the statistics from the local file. So if
             * dataFromCatalog = true, then we should use the temporary file from the hdfs (previous result).
             * In addition, we should explicitly output the random attributes, statistics. The following function
             * generates the plan from start_index (inclusive) to end_index (exclusive).
             */
    public ArrayList<Operator> generatePlan(int start_index, int end_index) throws Exception {
        ArrayList<String> tpList = chain.getTopologicalList(start_index, end_index);

        if (tpList.size() == 0) {
            throw new RuntimeException("The generated plan should not be null");
        }

		/* Each random table (string) corresponds to an operator. */
        HashMap<String, Operator> generatedPlanMap = new HashMap<>();
        /* Each random table (string) has a list of tableScan which should be replaced. */
        HashMap<String, ArrayList<TableScan>> replacedPlanMap = new HashMap<>();

        ArrayList<Operator> hostOperatorList = new ArrayList<>();
        ArrayList<Operator> linkedOperatorList = new ArrayList<>();

        //1. create the partial plan.
        for (int index = tpList.size() - 1; index >= 0; index--) {
            String table = tpList.get(index);
            Operator operator;
            if (randomTablePlanMap.containsKey(table)) {
                operator = randomTablePlanMap.get(table);
                generatedPlanMap.put(table, operator);
            } else {
                String randomTable = findMatchingGeneralIndexTable(table);
                operator = randomTablePlanMap.get(randomTable);
            }

            CopyHelper copyHelper = new CopyHelper();
            operator = operator.copy(copyHelper);

            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);
            instantiateOperator(table, operator, indices, generatedPlanMap, replacedPlanMap);
        }

        //2.link the partial plan.
        for (int index = tpList.size() - 1; index >= 0; index--) {
            String table = tpList.get(index);
            /* here operator is the host operator */

            Operator hostOperator = generatedPlanMap.get(table);
            if (!hostOperatorList.contains(hostOperator)) {
                hostOperatorList.add(hostOperator);
            }

            ArrayList<TableScan> replacedTableList = replacedPlanMap.get(table);

            if (replacedTableList != null) {
                for (TableScan tableScan : replacedTableList) {
                    String tableName = tableScan.getTableName();
                    String bracketsName = MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName);

                    // TODO not sure why this is done makes no sense
                    if (chain.getTickForTable(bracketsName) >= start_index) {
                        Operator linkedOperator = generatedPlanMap.get(MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName));
                        integratePlan(tableScan, linkedOperator);

                        if (!linkedOperatorList.contains(linkedOperator)) {
                            linkedOperatorList.add(linkedOperator);
                        }
                    }
                }
            }
        }
		
		/*
		 * 3. find the sink operator list.
		 */
        HashSet<String> tpMap = new HashSet<>();
        tpMap.addAll(tpList);

        ArrayList<String> sinkTableList = getFutureUsedTable(tpMap, end_index);
        if (sinkTableList == null || sinkTableList.size() == 0) {
            sinkTableList = new ArrayList<>();
            sinkTableList.addAll(tpMap);
        }

        ArrayList<Operator> sinkOperatorList = getSinkOperatorList(generatedPlanMap, sinkTableList);
        sinkTableList = changeFormatToUnderScore(sinkTableList);
		
		/*
		 * -----------------4. find the query plan, and links them.------------------------------
		 */
        for (int i = start_index; i < end_index; i++) {
            if (mcmcDagBundledPlan.containsKey(i)) //have the plan in timetick i.
            {
                ArrayList<Operator> tempSinkList = mcmcDagBundledPlan.get(i);
                for (int j = 0; j < tempSinkList.size(); j++) {
                    // linke the plan.
                    Operator tempSink = tempSinkList.get(j);
                    CopyHelper copyHelper = new CopyHelper();
                    tempSink = tempSink.copy(copyHelper);
                    ArrayList<TableScan> replacedTableList = PlanHelper.findReferencedRandomTable(tempSink);

                    for (TableScan tableScan : replacedTableList) {
                        String tableName = tableScan.getTableName();
                        String bracketsName = MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName);

                        if (chain.getTickForTable(bracketsName) >= start_index) {
                            Operator linkedOperator = generatedPlanMap.get(MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName));
                            if (linkedOperator == null) {
                                throw new RuntimeException("The SQL contains " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName) + " is not supported");
                            } else {
                                integratePlan(tableScan, linkedOperator);
                                if (!linkedOperatorList.contains(linkedOperator)) {
                                    linkedOperatorList.add(linkedOperator);
                                }
                            }
                        }
                    }

                    //check if the query is select_from_where query or the materilized view.
                    if (tempSink instanceof FrameOutput) {
                        ArrayList<Operator> childrenList = tempSink.getChildren();
                        sinkOperatorList.addAll(childrenList);
                        sinkTableList.addAll(((FrameOutput) tempSink).getTableList());

                        for (Operator aChildrenList : childrenList) {
                            aChildrenList.removeParent(tempSink);
                        }

                        tempSink.clearLinks();
                    } else {
                        sinkOperatorList.add(tempSink);
                        sinkTableList.add("file_to_print_" + i + "_" + j);
                    }
                }
            }
        }
		
		/*
		 * 5. Combine the sinkOperatorlist together by frameOutput.
		 *
		 */
        String nodeName = "frameOutput";
        ArrayList<Operator> children = sinkOperatorList;
        ArrayList<Operator> parents = new ArrayList<Operator>();
        Operator frameOutput = new FrameOutput(nodeName, children, parents, sinkTableList);
        for (Operator aChildren : children) {
            aChildren.addParent(frameOutput);
        }

        ArrayList<Operator> sinkList = new ArrayList<Operator>();
        sinkList.add(frameOutput);
		
		
		/*
		 * 6. Renaming
		 */
        ArrayList<Operator> allNodeList = PostProcessorHelper.findAllNode(sinkList);
        for (Operator operator : allNodeList) {
            operator.clearRenamingInfo();
        }

        //System.out.println(Process.BFS(sinkList));

        PostProcessor processor = new PostProcessor(sinkList, translatorHelper);

        processor.removeRedundant(allNodeList);
        processor.renaming();

        //batch here
        children = frameOutput.getChildren();

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


        return sinkList;
    }


    private String findMatchingGeneralIndexTable(String table) {

        if (table.matches("^[^_]+((\\[[0-9]+])+){2,}$")) {
            String prefix = MultidimensionalTableSchema.getPrefixFromBracketsTableName(table);
            HashMap<String, Integer> indices = MultidimensionalTableSchema.getIndicesFromBracketsName(table);

            for (String randomTable : randomTablePlanMap.keySet()) {
                if (randomTable.startsWith(prefix)) {
                    MultidimensionalSchemaIndices indexSpecification = new MultidimensionalSchemaIndices(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(randomTable));

                    if (indexSpecification.areIndicesForThisTable(indices)) {
                        return randomTable;
                    }
                }
            }

            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        String randomTableName = getTablePrefix(table) + "[i]";

        if (!randomTablePlanMap.containsKey(randomTableName)) {
            throw new RuntimeException("Could not match the table " + MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(table) + "to a table schema");
        }

        return randomTableName;
    }

    private HashMap<Integer, ArrayList<Operator>> processQueryList(ArrayList<Operator> queryList) {
        HashMap<Integer, ArrayList<Operator>> resultMap = new HashMap<Integer, ArrayList<Operator>>();

        for (Operator tempSinkOperator : queryList) {
            ArrayList<Integer> timeTick = PlanHelper.findReferencedRandomTableTimeTicks(tempSinkOperator, chain);
            putToMap(resultMap, Collections.max(timeTick), tempSinkOperator);
        }

        return resultMap;
    }

    private HashSet<String> gerReferencedTableInQueryList(ArrayList<Operator> queryList) {
        HashSet<String> resultSet = new HashSet<String>();

        for (Operator tempSinkOperator : queryList) {
            resultSet.addAll(PlanHelper.findReferencedRandomTables(tempSinkOperator, chain));
        }

        return resultSet;
    }

    private void putToMap(HashMap<Integer, ArrayList<Operator>> resultMap, int timeTick, Operator sink) {
        if (resultMap.containsKey(timeTick)) {
            resultMap.get(timeTick).add(sink);
        } else {
            ArrayList<Operator> temp = new ArrayList<Operator>();
            temp.add(sink);
            resultMap.put(timeTick, temp);
        }
    }


    private void integratePlan(TableScan tableScan, Operator linkedOperator) throws Exception {
        ArrayList<String> new_attributeList = tableScan.getAttributeList();
        Operator newOperator;
        if (linkedOperator instanceof Projection) {
            ArrayList<String> old_attributeList = ((Projection) linkedOperator).getProjectedNameList();
            newOperator = changeAttributeName(linkedOperator, old_attributeList, new_attributeList);

            ArrayList<Operator> parentList = tableScan.getParents();
            for (Operator aParentList : parentList) {
                newOperator.addParent(aParentList);

                aParentList.replaceChild(tableScan, newOperator);
            }

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

    /*
     * Here, rootTable means the table, to which this result should be written.
     */
    private Operator instantiateOperator(String rootTable,
                                         Operator operator,
                                         HashMap<String, Integer> indices,
                                         HashMap<String, Operator> generatedPlanMap,
                                         HashMap<String, ArrayList<TableScan>> replacedPlanMap) {
        ArrayList<Operator> sinkList = new ArrayList<Operator>();
        sinkList.add(operator);
        ArrayList<Operator> allOperators = Topologic.findAllNode(sinkList);
        UnionView unionView = Topologic.findUnionVIew(allOperators);

        if (unionView != null) //if it is a union view.
        {
            HashMap<String, HashSet<String>> ruleMap = chain.getRuleMap();
            HashSet<String> referencedTables = ruleMap.get(rootTable);

            //get the tables that have been already in the plan.
            HashSet<String> alreadyInPlanTables = new HashSet<String>();
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
                for (String tableName : referencedTables) {
                    if (!alreadyInPlanTables.contains(tableName)) {
                        qualifiedTableName = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(tableName);
                        String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(qualifiedTableName);
                        HashMap<String, Integer> referencedIndices = MultidimensionalTableSchema.getIndicesFromQualifiedName(qualifiedTableName);

                        View view;
                        try {
                            view = SimsqlCompiler.catalog.getView(qualifiedTableName);
                        } catch (Exception e) {
                            throw new RuntimeException("exception in generaing the plans for a union view");
                        }

                        TableReference tempReference = new TableReference(prefix, qualifiedTableName, referencedIndices, TableReference.CONSTANT_INDEX_TABLE);
                        Operator translatorElement = translator.sqlExpressionTranslator.indexTableScan(view, qualifiedTableName, tempReference);

                        if (translatorElement instanceof Projection) {
                            unionView.getInputAttributeNameList().addAll(((Projection) translatorElement).getProjectedNameList());
                        } else {
                            throw new RuntimeException("The children of UnionView operator should be Projection");
                        }

                        ArrayList<DataType> outputAttributeTypeList = new ArrayList<DataType>();
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
		
		/*
		 * Here, I use a BFS algorithms to traverse all the nodes in the graph, and output all 
		 * of such sentences.
		 */
        HashSet<Operator> finishedQueue = new HashSet<Operator>();
        LinkedBlockingDeque<Operator> availableQueue = new LinkedBlockingDeque<Operator>();

        availableQueue.add(operator);

        while (!availableQueue.isEmpty()) {
            Operator currentElement = availableQueue.poll();

            if (!finishedQueue.contains(currentElement)) {
                changeNodeProperty(currentElement, indices);
                finishedQueue.add(currentElement);
                /*
                 * deal with the current element
                 */
                if (currentElement instanceof TableScan) {
                    String tableName = ((TableScan) currentElement).getTableName();
                    if (((TableScan) currentElement).getType() != TableReference.COMMON_TABLE) {
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

                ArrayList<Operator> children = currentElement.getChildren();
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

    private void changeNodeProperty(Operator operator, HashMap<String, Integer> indices) {
        operator.setNodeName("node_" + translatorHelper.getInstantiateNodeIndex());
		
		/* 2. Change the aggregateIndex*/
        if (operator instanceof Aggregate) {
            String aggregateName = "agg" + translatorHelper.getAggregateIndex();
            ((Aggregate) operator).setAggregateName(aggregateName);
            ArrayList<MathOperator> aggregateExpressionList = ((Aggregate) operator).getAggregateExpressionList();
            for (MathOperator tempOperator : aggregateExpressionList) {
                changeMathOperatorProperty(tempOperator, indices);
            }
        } else if (operator instanceof Join) {
            BooleanOperator booleanOperator = ((Join) operator).getBooleanOperator();
            changePredicateProperty(booleanOperator, indices);
        } else if (operator instanceof ScalarFunction) {
            ArrayList<MathOperator> scalarExpressionList = ((ScalarFunction) operator).getScalarExpressionList();
            for (MathOperator tempOperator : scalarExpressionList) {
                changeMathOperatorProperty(tempOperator, indices);
            }
        } else if (operator instanceof Selection) {
            BooleanOperator booleanOperator = ((Selection) operator).getBooleanOperator();
            changePredicateProperty(booleanOperator, indices);
        } else if (operator instanceof VGWrapper) {
            String vgWrapperName = "vgwrapper" + translatorHelper.getVgWrapperIndex();
            ((VGWrapper) operator).setVgWrapperName(vgWrapperName);
        } else if (operator instanceof TableScan) {
            RelationStatistics relationStatistics = ((TableScan) operator).getRelationStatistics();
            String tableName = ((TableScan) operator).getTableName();
            if (isGeneralTable(tableName)) {
                HashMap<String, MathExpression> expressions = ((TableScan) operator).getIndexMathExpressions();
                String prefix = MultidimensionalTableSchema.getTablePrefixFromQualifiedName(tableName);
                relationStatistics.setRelation(MultidimensionalTableSchema.getQualifiedTableNameFromEvaluatedExpressions(prefix, expressions, indices));
            }
        }
    }

    private void changeMathOperatorProperty(MathOperator operator, HashMap<String, Integer> indices) {
        if (operator instanceof AggregateOperator) {
            String name = "arithExp" + translatorHelper.getArithExpIndex();
            ((AggregateOperator) operator).setName(name);
            MathOperator childOperator = ((AggregateOperator) operator).getChildOperator();
            changeMathOperatorProperty(childOperator, indices);
        } else if (operator instanceof ArithmeticOperator) {
            String name = "arithExp" + translatorHelper.getArithExpIndex();
            ((ArithmeticOperator) operator).setName(name);

            MathOperator left = ((ArithmeticOperator) operator).getLeft();
            MathOperator right = ((ArithmeticOperator) operator).getRight();
            changeMathOperatorProperty(left, indices);
            changeMathOperatorProperty(right, indices);
        } else if (operator instanceof FunctionOperator) {
            String name = "arithExp" + translatorHelper.getArithExpIndex();
            ((FunctionOperator) operator).setName(name);

            ArrayList<MathOperator> parameterList = ((FunctionOperator) operator).getParameterList();
            for (MathOperator tempOperator : parameterList) {
                changeMathOperatorProperty(tempOperator, indices);
            }
        } else if (operator instanceof PredicateToMathWrapper) {
            BooleanOperator predicate = ((PredicateToMathWrapper) operator).getBooleanOperator();
            changePredicateProperty(predicate, indices);
        } else if (operator instanceof SetOperator) {
            String name = "arithExp" + translatorHelper.getArithExpIndex();
            ((SetOperator) operator).setName(name);

            ArrayList<MathOperator> elementList = ((SetOperator) operator).getElementList();
            for (MathOperator tempOperator : elementList) {
                changeMathOperatorProperty(tempOperator, indices);
            }
        } else if (operator instanceof GeneralTableIndexOperator) {
            GeneralTableIndexOperator tmp = (GeneralTableIndexOperator) operator;
            tmp.setValue(indices.get(tmp.getIdentifier()));
        }
    }

    private void changePredicateProperty(BooleanOperator predicate, HashMap<String, Integer> indices) {
        if (predicate == null)
            return;

        String name = "predicate" + translatorHelper.getPredicateIndex();
        predicate.setName(name);

        if (predicate instanceof AndOperator) {
            ArrayList<BooleanOperator> operatorList = ((AndOperator) predicate).getOperatorList();
            for (BooleanOperator subPredicate : operatorList) {
                changePredicateProperty(subPredicate, indices);
            }
        } else if (predicate instanceof CompOperator) {
            MathOperator left = ((CompOperator) predicate).getLeft();
            MathOperator right = ((CompOperator) predicate).getRight();
            changeMathOperatorProperty(left, indices);
            changeMathOperatorProperty(right, indices);
        } else if (predicate instanceof NotOperator) {
            BooleanOperator booleanOperator = ((NotOperator) predicate).getBooleanOperator();
            changePredicateProperty(booleanOperator, indices);
        } else if (predicate instanceof OrOperator) {
            ArrayList<BooleanOperator> operatorList = ((OrOperator) predicate).getOperatorList();
            for (BooleanOperator subPredicate : operatorList) {
                changePredicateProperty(subPredicate, indices);
            }
        }
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

    private String getTablePrefix(String table) {
        int start = table.indexOf("[");

        if (start < 0)
            return table;
        else
            return table.substring(0, start);
    }

    private boolean isGeneralTable(String table) {
        return table.matches("^[^_]+(_[a-z])+$") || table.matches("^[^_]+(\\[[a-z]])+$");
    }

    private ArrayList<String> getFutureUsedTable(HashSet<String> tpMap, int iteration) {
        ArrayList<String> resultList = new ArrayList<String>();

        for (String table : tpMap) {
            String qualifiedTableName = MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(table);
            if (this.chain.isTableRequiredAfterIteration(qualifiedTableName, iteration) || finalTables.contains(qualifiedTableName)) {
                if (!resultList.contains(table))
                    resultList.add(table);
            }
        }

        return resultList;
    }

    private ArrayList<Operator> getSinkOperatorList(HashMap<String, Operator> generatedPlanMap,
                                                    ArrayList<String> sinkTableList) {
        ArrayList<Operator> resultList = new ArrayList<Operator>();
        for (String table : sinkTableList) {
            Operator operator = generatedPlanMap.get(table);
            if (operator != null) {
                resultList.add(operator);
            }
        }

        return resultList;
    }

    private ArrayList<String> changeFormatToUnderScore(ArrayList<String> sinkOperatorSet) {
        ArrayList<String> resultList = new ArrayList<>();
        for (String table : sinkOperatorSet) {
            resultList.add(MultidimensionalTableSchema.getQualifiedTableNameFromBracketsTableName(table));
        }

        return resultList;
    }

    private static ArrayList<Operator> copyOperatorList(ArrayList<Operator> list) {
        ArrayList<Operator> c_list = new ArrayList< >(list.size());
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
