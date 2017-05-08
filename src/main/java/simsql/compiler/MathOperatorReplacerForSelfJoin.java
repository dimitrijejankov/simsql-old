

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


package simsql.compiler;

import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.compiler.math_operators.*;

import java.util.ArrayList;


public class MathOperatorReplacerForSelfJoin {
	private String originalName;
	private String mappedName;
	
	public MathOperatorReplacerForSelfJoin(String originalName,
			String mappedName) {
		super();
		this.originalName = originalName;
		this.mappedName = mappedName;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getMappedName() {
		return mappedName;
	}

	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
	}

	public void replaceAttributeInMathOperator(MathOperator element)
	{
		if(element instanceof AggregateOperator)
		{
			replaceAttributeInAggregateOperator((AggregateOperator) element);
		}
		else if(element instanceof ArithmeticOperator)
		{
			replaceAttributeInArithmeticOperator((ArithmeticOperator) element);
		}
		else if(element instanceof ColumnOperator)
		{
			replaceAttributeInColumnOperator((ColumnOperator) element);
		}
		else if(element instanceof DateOperator)
		{
			replaceAttributeInDateOperator((DateOperator) element);
		}
		else if(element instanceof EFunction)
		{
			replaceAttributeInEFunction((EFunction) element);
		}
		else if(element instanceof FunctionOperator)
		{
			replaceAttributeInFunctionOperator((FunctionOperator) element);
		}
		else if(element instanceof PredicateToMathWrapper)
		{
			replaceAttributeInPredicateToMathWrapper((PredicateToMathWrapper)element);
		}
		else if(element instanceof NumberOperator)
		{
			replaceAttributeInNumberOperator((NumberOperator) element);
		}
		else if(element instanceof SetOperator)
		{
			replaceAttributeInSetOperator((SetOperator) element);
		}
		else if(element instanceof StarOperator)
		{
			replaceAttributeInStarOperator((StarOperator) element);
		}
		else if(element instanceof StringOperator)
		{
			replaceAttributeInStringOperator((StringOperator) element);
		}
		
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInStringOperator(StringOperator element)
	{
		return;
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInStarOperator(StarOperator element)
	{
		return;
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInSetOperator(SetOperator element) 
	{
		ArrayList<MathOperator> elementList = element.getElementList();
		for(int i = 0; i < elementList.size(); i++)
		{
			replaceAttributeInMathOperator(elementList.get(i));
		}
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInNumberOperator(NumberOperator element) {
		return;
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInFunctionOperator(FunctionOperator element) {
		ArrayList<MathOperator>  parameterList = element.getParameterList();
		if(parameterList != null)
		{
			for(int i = 0; i < parameterList.size(); i++)
			{
				replaceAttributeInMathOperator(parameterList.get(i));
			}
		}
	}
	
	public void replaceAttributeInPredicateToMathWrapper(PredicateToMathWrapper element)
	{
		BooleanOperator booleanOperator = element.operator;
		BooleanOperatorReplacerForSelfJoin booleanOperatorReplacer = new BooleanOperatorReplacerForSelfJoin(originalName, mappedName);
		booleanOperatorReplacer.replaceAttributeInBooleanOperator(booleanOperator);
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInEFunction(EFunction element) {
		return;
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInDateOperator(DateOperator element) {
		return;
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInColumnOperator(ColumnOperator element) {
		String name = element.getColumnName();
		if(name.equals(originalName))
		{
			element.setColumnName(mappedName);
		}
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInArithmeticOperator(ArithmeticOperator element) {
		replaceAttributeInMathOperator(element.getLeft());
		replaceAttributeInMathOperator(element.getRight());
	}

	/**
	 * @param element
	 */
	private void replaceAttributeInAggregateOperator(AggregateOperator element) {
		/*
		 * Since it is an aggregateOperator, so its name can be the form like 
		 * "aggExp(...)", and we do not need to change the name field of the 
		 * AggregateOperator.
		 */
		replaceAttributeInMathOperator(element.getChildOperator());
	}
}
