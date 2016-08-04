

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
package simsql.compiler; // package mcdb.compiler.logicPlan.translator;

import java.util.HashMap;





// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;



/**
 * @author Bamboo
 *
 */
public class TranslatorHelper {
	private int instantiateNodeIndex;
	
	private int nodeIndex; 
	private int aggregateIndex;
	private int predicateIndex;
	private int arithExpIndex;
	private int scalarAttributeNameIndex;
	private int tempAttributeIndex;
	private int vgWrapperIndex;
	public HashMap<String, Operator> viewPlanMap;
	
	/*
	 * some fromSubquery view
	 */
	public HashMap<TypeChecker, Operator> fromqueryPlanMap;
	
	
	/*
	 * save the temporary attribute name
	 */
	public HashMap<MathExpression, String> expressionNameMap;
	
	//<viewName, plan>
	/*
	 * it saves the plan of temporary view due to unnesting, view saved in database
	 */
	
	public TranslatorHelper()
	{
		instantiateNodeIndex = 1000000;
		nodeIndex = 0;
		aggregateIndex = 0;
		predicateIndex = 0;
		arithExpIndex = 0;
		scalarAttributeNameIndex = 0;
		tempAttributeIndex = 0;
		vgWrapperIndex = 0;
		
		viewPlanMap = new HashMap<String, Operator>();
		fromqueryPlanMap = new HashMap<TypeChecker, Operator>();
		expressionNameMap = new HashMap<MathExpression, String>();
		
		viewPlanMap.clear();
		fromqueryPlanMap.clear();
		expressionNameMap.clear();
	}
	
	
	public int getNodeIndex()
	{
		int result = nodeIndex;
		nodeIndex++;
		return result;
	}
	
	public int getInstantiateNodeIndex()
	{
		int result = instantiateNodeIndex;
		instantiateNodeIndex--;
		return result;
	}
	
	public int getAggregateIndex()
	{
		int result = aggregateIndex;
		aggregateIndex++;
		return result;
	}
	
	public int getPredicateIndex()
	{
		int result = predicateIndex;
		predicateIndex++;
		return result;
	}
	
	public int getTempAttributeIndex()
	{
		int result = tempAttributeIndex;
		tempAttributeIndex ++;
		return result;
	}
	
	public int getScalarAttributeNameIndex()
	{
		int result = scalarAttributeNameIndex;
		scalarAttributeNameIndex ++;
		return result;
	}
	
	public int getArithExpIndex()
	{
		int result = arithExpIndex;
		arithExpIndex ++;
		return result;
	}
	
	public int getVgWrapperIndex()
	{
		int result = vgWrapperIndex;
		vgWrapperIndex ++;
		return result;
	}
}
