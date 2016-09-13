

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


package simsql.shell;

import simsql.optimizer.CompiledOptimizer;
import simsql.optimizer.SimSQLOptimizedQuery;
import simsql.compiler.Relation;
import simsql.compiler.View;
import simsql.compiler.Attribute;
import simsql.compiler.SimsqlCompiler;
import simsql.code_generator.WrappedTranslator;
import simsql.code_generator.DataFlowQuery;
import simsql.runtime.HadoopRuntime;
import simsql.runtime.HadoopResult;
import simsql.compiler.Operator;
import simsql.compiler.PlanStatistics;
import simsql.compiler.PostProcessorHelper;
import simsql.compiler.PreviousTable;
import simsql.compiler.TableScan;
import simsql.compiler.Topologic;
import simsql.compiler.ChainGeneration;
import simsql.compiler.TranslatorHelper;
import simsql.compiler.PlanInstantiation;
import simsql.compiler.FrameOutput;
import simsql.compiler.PlanHelper;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

// This is a simple query processor skeleton that supports a Parser->Optimizer->Translator->Runtime data flow
public class MCMCQueryProcessor implements QueryProcessor<SimSQLCompiledQuery, SimSQLOptimizedQuery, DataFlowQuery, HadoopResult> {

    private SimSQLCompiledQuery parsedQuery;
    private boolean isMCMC;

    //private FoulaOptimizer myOptimizer;
    private CompiledOptimizer myOptimizer;
    private WrappedTranslator myTranslator;
    private HadoopRuntime myRuntime;
    private SimsqlCompiler myParser;

    // data structures for MCMC
    private PlanInstantiation planInstantiation;
    private int maxLoop;

    private HadoopResult previousResult;
    private int timeTick;
    private boolean isFirstIteration;

    // remembers the output from the last iteration... used to save/delete this output
    private ArrayList<Relation> requiredRelations = new ArrayList<Relation>();

    public void saveRequiredRelations() {
        for (Relation relation : requiredRelations) {

            // first, we see if this is one of the MCMC tables
            String oldName = relation.getName();

            // strip off the MC iteration
            String[] parts = oldName.split("_");
            String without = "";
            for (int i = 0; i < parts.length - 1; i++) {
                without += parts[i] + "_";
            }

            if (parts[parts.length - 1].equals("0"))
                without += "0";
            else
                without += "i";

            // see if this is a view
            View res = getCatalog().getView(without);

            // if not, move on
            if (res == null)
                continue;

            // rename the attributes to match the view
            int index = 0;
            for (Attribute a : relation.getAttributes()) {
                a.setName(res.getAttributes().get(index).getName());
                index++;
            }

            // now save the relation
            String newName = relation.getName() + "_saved";
            relation.setName(newName);
            relation.setFileName(null);
            getCatalog().addRelation(relation);
            myTranslator.getPhysicalDatabase().rename(oldName, newName);
        }
        requiredRelations = new ArrayList<Relation>();
    }

    public void deleteNotRequiredRelationsFromLastIteration() {
        // if it's a recursive query, some relations are necessary and some are not.
        if(isMCMC) {
            ChainGeneration chainGeneration = this.planInstantiation.getChain();
            HashSet<String> finalTables = this.planInstantiation.getFinalTables();
            HashSet<Relation> removedTables = new HashSet<Relation>();

            for (Relation relation : requiredRelations) {
                if (!chainGeneration.isTableRequiredAfterIteration(relation.getName(), getIteration()) &&
                        !finalTables.contains(relation.getName())) {
                    getPhysicalDatabase().deleteTable(relation.getName());
                    removedTables.add(relation);
                }
            }

            requiredRelations.removeAll(removedTables);
        }
        else {
        // if it's a normal query remove them all.
            for (Relation relation : requiredRelations) {
                getPhysicalDatabase().deleteTable(relation.getName());
            }
            requiredRelations.clear();
        }
    }

    public MCMCQueryProcessor() {
        parsedQuery = null;
        isMCMC = false;
        //myOptimizer = new FoulaOptimizer();
        myOptimizer = new CompiledOptimizer();
        myTranslator = new WrappedTranslator();

        myRuntime = new HadoopRuntime(myTranslator.getPhysicalDatabase());
        myParser = new SimsqlCompiler(
                myTranslator.getPhysicalDatabase(),
                myRuntime.getRuntimeParameters()
        );
        previousResult = null;
        timeTick = -1;
        isFirstIteration = true;
    }

    public int getIteration() {
        return timeTick;
    }

    public Catalog getCatalog() {
        return myParser.getCatalog();
    }

    public PhysicalDatabase<DataFlowQuery> getPhysicalDatabase() {
        return myTranslator.getPhysicalDatabase();
    }

    public RuntimeParameter getRuntimeParameter() {
        return myRuntime.getRuntimeParameters();
    }

    public Compiler<SimSQLCompiledQuery> getCompiler() {
        return myParser;
    }

    public Optimizer<SimSQLCompiledQuery, SimSQLOptimizedQuery> getOptimizer() {
        return myOptimizer;
    }

    public CodeGenerator<SimSQLOptimizedQuery, DataFlowQuery> getTranslator() {
        return myTranslator;
    }

    public Runtime<DataFlowQuery, HadoopResult> getRuntime() {
        return myRuntime;
    }

    public void killRuntime() {
        myRuntime.killRuntime();
    }

    public void reset() {
        parsedQuery = null;
        planInstantiation = null;
        timeTick = -1;
        isFirstIteration = true;
        previousResult = null;
        isMCMC = false;
    }

    public void doneParsing(SimSQLCompiledQuery parseResult) {
        /*
		 * Here, for MCDB2 queries, we have already done postProcessing in
		 * SimsqlCompiler, so here we just transfer the object.
		 * 
		 * For MCMC queries, we should do postProcessing here, including the
		 */
        if (parseResult.getFName() != null) {
            parsedQuery = parseResult;
        } else {
            isMCMC = true;
            try {
                ArrayList<Operator> sinkList = parseResult.sinkList;
                ArrayList<Operator> queryList = parseResult.queryList;
                ArrayList<String> sqlList = parseResult.sqlList;
                HashMap<Operator, String> planTableMap = parseResult.definitionMap;

                Topologic topologic = new Topologic(sinkList, planTableMap);

                ChainGeneration chain = new ChainGeneration(topologic, parseResult.requiredTables);
                maxLoop = chain.getMaxLoop();

                TranslatorHelper translatorHelper = myParser.getTranslatorHelper();
                planInstantiation = new PlanInstantiation(planTableMap, chain,
                        translatorHelper, queryList);
                timeTick = chain.getMinimumTimeTick();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public SimSQLCompiledQuery nextIteration() {
        if (!isMCMC) {
            SimSQLCompiledQuery oneToReturn = parsedQuery;
            parsedQuery = null;
            return oneToReturn;
        }

        if (timeTick <= maxLoop) {
            SimSQLCompiledQuery compiledQuery = generatePlan(planInstantiation,
                                                             timeTick,
                                                             previousResult,
                                                             isFirstIteration);

            if (isFirstIteration)// from now on, it is not the first iteration
            {
                isFirstIteration = false;
            }
            System.out.println("Monte Carlo iteration " + timeTick + "/" + maxLoop);
            timeTick++;

            return compiledQuery;
        } else {
            timeTick = -1;
            return null;
        }

    }

    public void doneExecuting(HadoopResult queryResult, String outputFile) {

        // unlink all the unnecessary files
        for (String f : queryResult.getFilesToUnlink()) {
            getPhysicalDatabase().deleteTable(getPhysicalDatabase().getTableName(f));
        }

        // there might be some empty files that got left around... they were allocated before
        // the query was executed, but they were never actually used (so they don't appear in
        // the return set of "getFilesToUnlink"
        getPhysicalDatabase().removeZeroSizeRelations("query_block");

        // build a relation for the iterator creation
        if (queryResult.getOutputRelation(getPhysicalDatabase()) != null) {

            Relation rel = queryResult.getOutputRelation(getPhysicalDatabase());

            // where are we printing?
            if (outputFile == null) {
                // only print out if we are at the end
                if (timeTick == -1 || timeTick == maxLoop + 1)
                    getPhysicalDatabase().printRelation(rel);
            } else {
                // a file
                getPhysicalDatabase().saveRelation(rel, outputFile);
            }

            // delete that old file
            getPhysicalDatabase().unregisterTable(rel.getName());

            previousResult = queryResult;

            // Relations that are the final result of the current iteration.
            ArrayList<Relation> finalRelations = previousResult.getFinalRelations();

            // Delete the relation that are not required anymore...
            deleteNotRequiredRelationsFromLastIteration();

            // Add all the relations from the current iteration to the required relations
            requiredRelations.addAll(previousResult.getFinalRelations());

            // If there are relations with the same name already in the catalog update their statistics.
            for (Relation relation : finalRelations) {
                Relation catalogRelation = getCatalog().getRelation(relation.getName());

                if (catalogRelation != null) {
                    getCatalog().updateTableStatistics(catalogRelation, relation);
                }
            }
        }
    }

    public SimSQLCompiledQuery generatePlan(PlanInstantiation planInstantiation,
                                            int timeTick,
                                            HadoopResult previousResult,
                                            boolean dataInCatalog) {
        SimSQLCompiledQuery compiledQuery = new SimSQLCompiledQuery("_" + timeTick + ".sql.pl");

        try {
            String tempQueryFile = compiledQuery.getFName();
            BufferedWriter wri = new BufferedWriter(new FileWriter(tempQueryFile, false));
	
			/*
			 * 1. Instantiate the plan.
			 */
            ArrayList<Operator> generatedSinkList = planInstantiation.generatePlan(timeTick, timeTick + 1);
	
			/*
			 * 1. If dataInCatalog, we should change the tableScan from the local
			 * file; furthermore, we should change the statistics accordingly.
			 */
            if (!dataInCatalog) {
                updatePlanByPreviousResult(generatedSinkList, previousResult);
            }

            wri.write(":- dynamic compExp/5.\r\n");
            wri.write(":- dynamic stats/6.\r\n");
            wri.write(":- dynamic relation/3.\r\n");

            for (int i = 0; i < generatedSinkList.size(); i++) {
                Operator element = generatedSinkList.get(i);
                wri.write("parent(planRoot, " + element.getNodeName() + ").\r\n");
            }

            wri.write(PlanHelper.BFS(generatedSinkList));

            PlanStatistics statistics = new PlanStatistics(generatedSinkList);
            wri.write(statistics.getStatistics());

            wri.write(statistics.getAttributeTypeStatistics());
            wri.write("attributeType(isPres, bottom).\r\n");

            wri.close();

            for (int i = 0; i < generatedSinkList.size(); i++) {
                Operator element = generatedSinkList.get(i);
                if (element instanceof FrameOutput) {
                    compiledQuery.addMaterilizedView((FrameOutput) element);
                }
            }

            return compiledQuery;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("exception happens in generatePlan");
        }
    }

    public void updatePlanByPreviousResult(ArrayList<Operator> generatedSinkList, HadoopResult previousResult)
            throws Exception {
        HashMap<String, PreviousTable> tableMap = new HashMap<String, PreviousTable>();

		/*
		 * 1. Get the directory and attributeList.
		 */
        for (Relation outRelation : requiredRelations) {
            String file = outRelation.getFileName();
            String relName = outRelation.getName();

            ArrayList<String> attributeNameList = new ArrayList<String>();
            HashMap<String, String> attributeTypeMap = new HashMap<String, String>();
            ArrayList<String> randamAttributeList = new ArrayList<String>();

            ArrayList<Attribute> attributeList = outRelation.getAttributes();
            Attribute attribute;
            String attributeType;
            for (Attribute anAttributeList : attributeList) {
                attribute = anAttributeList;
                attributeNameList.add(attribute.getName());
                attributeType = attribute.getType().writeOut();                // TO-DO
                attributeTypeMap.put(attribute.getName(), attributeType);
                if (attribute.getIsRandom()) {
                    randamAttributeList.add(attribute.getName());
                }
            }

            PreviousTable resultTable = new PreviousTable(file,
                    relName, attributeNameList, attributeTypeMap, randamAttributeList);

            resultTable.setTupleNum(outRelation.getTupleNum());

            for (Attribute anAttributeList : attributeList) {
                attribute = anAttributeList;
                resultTable.addStat(attribute.getName(), attribute.getUniqueValue());

            }
            tableMap.put(relName, resultTable);
        }
		
		/*
		 * 3. Update the plan.
		 */
        ArrayList<Operator> allNodeList = PostProcessorHelper.findAllNode(generatedSinkList);
        for (Operator operator : allNodeList) {
            if (operator instanceof TableScan) {
                String tableName = ((TableScan) operator).getTableName();
                if (tableMap.containsKey(tableName)) {
                    PreviousTable hdfsTable = tableMap.get(tableName);
                    ((TableScan) operator).setTableInfo(hdfsTable);
                }
            }
        }
    }

}
