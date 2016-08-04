

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.statisticsOperator;

/**
 * @author Bamboo
 *
 */
public class SystemParameters implements StatisticsOperator{
	private int monteCarloIterations;
	private int vgThreads;
	private int vgTuples;
	private int vgPipeSize;
	private int isPresSize;
	private int seedSize;
	
	private double selectionCostWeight;
	private double tablescanCostWeight;
	private double seedCostWeight;
	private double splitCostWeight;
	private double joinCostWeight;
	private double antijoinCostWeight;
	private double semijoinCostWeight;
	private double dedupCostWeight;
	private double projectionCostWeight;
	private double genaggCostWeight;
	private double vgwrapperCostWeight;
	private double scalarfuncCostWeight;
	
	
	private double defaultSelectivity;
	
	public SystemParameters(int monteCarloIterations, 
							int vgThreads,
							int vgTuples, 
							int vgPipeSize,
							double defaultSelectivity,
							int isPresSize,
							int seedSize,
							double selectionCostWeight,
							double tablescanCostWeight,
							double seedCostWeight,
							double splitCostWeight,
							double joinCostWeight,
							double antijoinCostWeight,
							double semijoinCostWeight,
							double dedupCostWeight,
							double projectionCostWeight,
							double genaggCostWeight,
							double vgwrapperCostWeight,
							double scalarfuncCostWeight
							) {
		super();
		this.monteCarloIterations = monteCarloIterations;
		this.vgThreads = vgThreads;
		this.vgTuples = vgTuples;
		this.vgPipeSize = vgPipeSize;
		this.defaultSelectivity = defaultSelectivity;
		this.isPresSize = isPresSize;
		this.seedSize = seedSize;
		
		this.selectionCostWeight = selectionCostWeight;
		this.tablescanCostWeight = tablescanCostWeight;
		this.seedCostWeight = seedCostWeight;
		this.splitCostWeight = splitCostWeight;
		this.joinCostWeight = joinCostWeight;
		this.antijoinCostWeight = antijoinCostWeight;
		this.semijoinCostWeight = semijoinCostWeight;
		this.dedupCostWeight = dedupCostWeight;
		this.projectionCostWeight = projectionCostWeight;
		this.genaggCostWeight = genaggCostWeight;
		this.vgwrapperCostWeight = vgwrapperCostWeight;
		this.scalarfuncCostWeight = scalarfuncCostWeight;
	}
	
	/* (non-Javadoc)
	 * @see logicOperator.statisticsOperator.StatisticsOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "";
		result += "monteCarloIterations(";
		result += monteCarloIterations;
		result += ").\r\n";

		result += "vgThreads(";
		result += vgThreads;
		result += ").\r\n";
		
		result += "vgTuples(";
		result += vgTuples;
		result += ").\r\n";
		
		result += "vgPipeSize(";
		result += vgPipeSize;
		result += ").\r\n";
		
		result += "defaultSelectivity(";
		result += defaultSelectivity;
		result += ").\r\n";
		
		result += "attributeSize(";
		result += "isPres, ";
		result += isPresSize;
		result += ").\r\n";
		
		result += "attributeSize(";
		result += "seed, ";
		result += seedSize;
		result += ").\r\n";
		
		//--------------cost weight----------------
		result += "operatorCostWeight(";
		result += "selection, ";
		result += selectionCostWeight;
		result += ").\r\n";
		
		result += "operatorCostWeight(";
		result += "tablescan, ";
		result += tablescanCostWeight;
		result += ").\r\n";
		
		result += "operatorCostWeight(";
		result += "seed, ";
		result += seedCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "split, ";
		result += splitCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "join, ";
		result += joinCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "antijoin, ";
		result += antijoinCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "semijoin, ";
		result += semijoinCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "dedup, ";
		result += dedupCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "projection, ";
		result += projectionCostWeight;
		result += ").\r\n";
		
		
		result += "operatorCostWeight(";
		result += "genagg, ";
		result += genaggCostWeight;
		result += ").\r\n";
		
		result += "operatorCostWeight(";
		result += "vgwrapper, ";
		result += vgwrapperCostWeight;
		result += ").\r\n";
		
		result += "operatorCostWeight(";
		result += "scalarfunc, ";
		result += scalarfuncCostWeight;
		result += ").\r\n";
		
		return result;
	}
	
	
	public int getMonteCarloIterations() {
		return monteCarloIterations;
	}


	public void setMonteCarloIterations(int monteCarloIterations) {
		this.monteCarloIterations = monteCarloIterations;
	}


	public int getVgThreads() {
		return vgThreads;
	}


	public void setVgThreads(int vgThreads) {
		this.vgThreads = vgThreads;
	}


	public int getVgTuples() {
		return vgTuples;
	}


	public void setVgTuples(int vgTuples) {
		this.vgTuples = vgTuples;
	}


	public int getVgPipeSize() {
		return vgPipeSize;
	}


	public void setVgPipeSize(int vgPipeSize) {
		this.vgPipeSize = vgPipeSize;
	}

	/**
	 * @return the defaultSelectivity
	 */
	public double getDefaultSelectivity() {
		return defaultSelectivity;
	}

	/**
	 * @param defaultSelectivity the defaultSelectivity to set
	 */
	public void setDefaultSelectivity(double defaultSelectivity) {
		this.defaultSelectivity = defaultSelectivity;
	}

	/**
	 * @return the isPresSize
	 */
	public int getIsPresSize() {
		return isPresSize;
	}

	/**
	 * @param isPresSize the isPresSize to set
	 */
	public void setIsPresSize(int isPresSize) {
		this.isPresSize = isPresSize;
	}

	/**
	 * @return the seedSize
	 */
	public int getSeedSize() {
		return seedSize;
	}

	/**
	 * @param seedSize the seedSize to set
	 */
	public void setSeedSize(int seedSize) {
		this.seedSize = seedSize;
	}

	/**
	 * @return the selectionCostWeight
	 */
	public double getSelectionCostWeight() {
		return selectionCostWeight;
	}

	/**
	 * @param selectionCostWeight the selectionCostWeight to set
	 */
	public void setSelectionCostWeight(double selectionCostWeight) {
		this.selectionCostWeight = selectionCostWeight;
	}

	/**
	 * @return the tablescanCostWeight
	 */
	public double getTablescanCostWeight() {
		return tablescanCostWeight;
	}

	/**
	 * @param tablescanCostWeight the tablescanCostWeight to set
	 */
	public void setTablescanCostWeight(double tablescanCostWeight) {
		this.tablescanCostWeight = tablescanCostWeight;
	}

	/**
	 * @return the seedCostWeight
	 */
	public double getSeedCostWeight() {
		return seedCostWeight;
	}

	/**
	 * @param seedCostWeight the seedCostWeight to set
	 */
	public void setSeedCostWeight(double seedCostWeight) {
		this.seedCostWeight = seedCostWeight;
	}

	/**
	 * @return the splitCostWeight
	 */
	public double getSplitCostWeight() {
		return splitCostWeight;
	}

	/**
	 * @param splitCostWeight the splitCostWeight to set
	 */
	public void setSplitCostWeight(double splitCostWeight) {
		this.splitCostWeight = splitCostWeight;
	}

	/**
	 * @return the joinCostWeight
	 */
	public double getJoinCostWeight() {
		return joinCostWeight;
	}

	/**
	 * @param joinCostWeight the joinCostWeight to set
	 */
	public void setJoinCostWeight(double joinCostWeight) {
		this.joinCostWeight = joinCostWeight;
	}

	/**
	 * @return the antijoinCostWeight
	 */
	public double getAntijoinCostWeight() {
		return antijoinCostWeight;
	}

	/**
	 * @param antijoinCostWeight the antijoinCostWeight to set
	 */
	public void setAntijoinCostWeight(double antijoinCostWeight) {
		this.antijoinCostWeight = antijoinCostWeight;
	}

	/**
	 * @return the semijoinCostWeight
	 */
	public double getSemijoinCostWeight() {
		return semijoinCostWeight;
	}

	/**
	 * @param semijoinCostWeight the semijoinCostWeight to set
	 */
	public void setSemijoinCostWeight(double semijoinCostWeight) {
		this.semijoinCostWeight = semijoinCostWeight;
	}

	/**
	 * @return the dedupCostWeight
	 */
	public double getDedupCostWeight() {
		return dedupCostWeight;
	}

	/**
	 * @param dedupCostWeight the dedupCostWeight to set
	 */
	public void setDedupCostWeight(double dedupCostWeight) {
		this.dedupCostWeight = dedupCostWeight;
	}

	/**
	 * @return the projectionCostWeight
	 */
	public double getProjectionCostWeight() {
		return projectionCostWeight;
	}

	/**
	 * @param projectionCostWeight the projectionCostWeight to set
	 */
	public void setProjectionCostWeight(double projectionCostWeight) {
		this.projectionCostWeight = projectionCostWeight;
	}

	/**
	 * @return the genaggCostWeight
	 */
	public double getGenaggCostWeight() {
		return genaggCostWeight;
	}

	/**
	 * @param genaggCostWeight the genaggCostWeight to set
	 */
	public void setGenaggCostWeight(double genaggCostWeight) {
		this.genaggCostWeight = genaggCostWeight;
	}

	/**
	 * @return the vgwrapperCostWeight
	 */
	public double getVgwrapperCostWeight() {
		return vgwrapperCostWeight;
	}

	/**
	 * @param vgwrapperCostWeight the vgwrapperCostWeight to set
	 */
	public void setVgwrapperCostWeight(double vgwrapperCostWeight) {
		this.vgwrapperCostWeight = vgwrapperCostWeight;
	}

	/**
	 * @return the scalarfuncCostWeight
	 */
	public double getScalarfuncCostWeight() {
		return scalarfuncCostWeight;
	}

	/**
	 * @param scalarfuncCostWeight the scalarfuncCostWeight to set
	 */
	public void setScalarfuncCostWeight(double scalarfuncCostWeight) {
		this.scalarfuncCostWeight = scalarfuncCostWeight;
	}


	
	
}
