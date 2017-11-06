

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


/**
 * 
 */
package simsql.compiler;

import simsql.compiler.operators.Operator;
import simsql.compiler.timetable.GraphCutter;
import simsql.compiler.timetable.TableDependencyGraph;
import simsql.compiler.timetable.TimeTableNode;

import java.util.*;


/**
 * @author Bamboo
 *
 */
public class ChainGeneration
{
	private Topologic topologic;
	/*
	 * The ruleMap records for each random table, which random tables are needed.
	 */
	private HashMap<String, HashSet<String>> ruleMap;
	/*
	 * The simulateTableMap saves for each time tick, the list of "TABLE" at this time, with the corresponding 
	 * tables generated by "TABLE".
	 */
	private HashMap<Integer, TableByTime> simulateTableMap;
	private ArrayList<String> startPointList;
	
	private int startTimeTick;
    private LinkedList<TimeTableNode> requiredTables;

	private GraphCutter bp;
	
	public ChainGeneration(Topologic topologic, LinkedList<TimeTableNode> requiredTables, HashMap<Operator, String> planTableMap, ArrayList<Operator> queryList)
	{
        this.topologic = topologic;
        this.requiredTables = requiredTables;

		this.ruleMap = new HashMap<>();
        this.simulateTableMap = new HashMap<>();
        this.startPointList = new ArrayList<>();



		instantiateChain();
	}

	public GraphCutter getBp() {
		return bp;
	}

	public ArrayList<String> getTopologicalList(int start, int end)
	{
		return new ArrayList<>(this.simulateTableMap.get(start).getTableSet());
	}

	private void instantiateChain()
	{

        TableDependencyGraph dependencyGraph = new TableDependencyGraph(requiredTables, topologic.getBackwardEdges());

        /*
		 * 1. ruleMap
		 */
        ruleMap = dependencyGraph.generateRuleMap();

        /*
		 * 2. simulateTableMap
		 */
        simulateTableMap = dependencyGraph.extractSimulateTableMap();

		/*
		 * 3. starting point
		 */
		startPointList = simulateTableMap.get(0).getTableSet();

        /*
		 * 4. startTimeTick
		 */
        startTimeTick = 0;
	}

	public HashMap<Integer, TableByTime> getSimulateTableMap() {
		return simulateTableMap;
	}
	
	public int getMinimumTimeTick()
	{
		return startTimeTick;
	}

	public int getTickForTable(String tableName) {

		for(int tick : simulateTableMap.keySet()){
			if(simulateTableMap.get(tick).getTableSet().contains(tableName)){
			    return tick;
            }
		}

		throw new RuntimeException("Tick not found for table " + tableName + "!");
	}

	public boolean isTableRequiredAfterIteration(String tableName, int iteration) {

        HashSet<String> dependentTables = getDependentTables(tableName);

        for(String dependentTable : dependentTables) {
            if(getTickForTable(dependentTable) >= iteration) {
                return true;
            }
        }

        return false;
    }

    private HashSet<String> getDependentTables(String tableName) {
        tableName = MultidimensionalTableSchema.getBracketsTableNameFromQualifiedTableName(tableName);

        for(int i : simulateTableMap.keySet()) {
            TableByTime tt = simulateTableMap.get(i);
            if(tt.getTableSet().contains(tableName)){
                return tt.getTimeMap().get(tableName) != null ? tt.getTimeMap().get(tableName) : new HashSet<String>();
            }
        }

        return new HashSet<>();
    }

	/**
	 * @return the ruleMap
	 */
	public HashMap<String, HashSet<String>> getRuleMap() {
		return ruleMap;
	}

	public int getMaxLoop() {
	    return simulateTableMap.size() - 1;
    }
}
