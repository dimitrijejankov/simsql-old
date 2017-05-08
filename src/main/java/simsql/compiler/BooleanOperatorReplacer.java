

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
package simsql.compiler; // package mcdb.compiler.logicPlan.postProcessor;

import simsql.compiler.boolean_operator.*;
import simsql.compiler.math_operators.MathOperator;
import simsql.compiler.operators.Operator;

import java.util.ArrayList;



// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AndOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AtomOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.CompOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.NotOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.OrOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;

/**
 * @author Bamboo
 * 
 */
public class BooleanOperatorReplacer {
	private Operator operator;

	public BooleanOperatorReplacer(Operator operator) {
		this.operator = operator;
	}
	
	public void replaceAttributeInBooleanOperator(BooleanOperator element) 
	{
		if (element instanceof AndOperator)
		{
			replaceAttributeInAndOperator((AndOperator) element);
		} 
		else if (element instanceof AtomOperator)
		{
			replaceAttributeInAtomOperator((AtomOperator) element);
		} 
		else if (element instanceof CompOperator)
		{
			replaceAttributeInCompOperator((CompOperator) element);
		} 
		else if (element instanceof NotOperator)
		{
			replaceAttributeInNotOperator((NotOperator) element);
		} 
		else if (element instanceof OrOperator)
		{
			replaceAttributeInOrOperator((OrOperator) element);
		} 
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInAndOperator(AndOperator element) {
		ArrayList<BooleanOperator> operatorList = element.getOperatorList();
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				replaceAttributeInBooleanOperator(operatorList.get(i));
			}
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInAtomOperator(AtomOperator element) {
		return;
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInCompOperator(CompOperator element) 
	{
		MathOperator left = element.getLeft();
		MathOperator right = element.getRight();
		
		MathOperatorReplacer mathReplacer = new MathOperatorReplacer(operator);
		
		if(left != null)
			mathReplacer.replaceAttributeInMathOperator(left);
		
		if(right != null)
			mathReplacer.replaceAttributeInMathOperator(right);
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInNotOperator(NotOperator element) {
		BooleanOperator operator = element.getBooleanOperator();
		if(operator != null)
		{
			replaceAttributeInBooleanOperator(operator);
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInOrOperator(OrOperator element) {
		ArrayList<BooleanOperator> operatorList = element.getOperatorList();
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				replaceAttributeInBooleanOperator(operatorList.get(i));
			}
		}
	}

	public Operator getOperator() {
		return operator;
	}

	public void setOperator(Operator operator) {
		this.operator = operator;
	}
	
	

}
