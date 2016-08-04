

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.mathOperator;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;

/**
 * @author Bamboo
 *
 */
public class PredicateToMathWrapper implements MathOperator
{
	public BooleanOperator operator;

	
	public PredicateToMathWrapper(BooleanOperator operator) {
		super();
		this.operator = operator;
	}

	/* (non-Javadoc)
	 * @see logicPlan.logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		return operator.visitNode();
	}

	/* (non-Javadoc)
	 * @see logicPlan.logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return operator.getName();
	}

	
	/**
	 * @return the operator
	 */
	public BooleanOperator getBooleanOperator() {
		return operator;
	}

	/**
	 * @param operator the operator to set
	 */
	public void setBooleanOperator(BooleanOperator operator) {
		this.operator = operator;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator#copy()
	 */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		return new PredicateToMathWrapper(operator.copy(copyHelper));
	}
	
}
