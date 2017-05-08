

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
package simsql.compiler; // package mcdb.compiler.parser.expression.util;

import simsql.compiler.expressions.*;

import java.util.ArrayList;



// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;



/**
 * @author Bamboo
 *
 */
public class ColumnExpressionExtractor {
	/*
	 * --------------------------------------------MathExpression--------------------------
	 * The MathExpression that can contain the MathExpression or BooleanPredicate includes
	 * 1. ArithmeticExpression
	 * 2. SubqueryExpression
	 * 3. AggregateExpression
	 * 4. GeneralFunctionExpression
	 * 5. SetExpression
	 * 
	 * Basic MathExpressions do not have some component that needs simplification:
	 * 1. AsteriskExpression
	 * 2. ColumnExpression
	 * 3. DateExpression
	 * 4. NumericExpression
	 * 5. StringExpression
	 * 6. GeneralTableIndex
	 */
	
	public static void getColumnInArithmeticExpression(ArithmeticExpression arithmeticExpression,
			                                              ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			getColumnInMathExpression(expression, expressionList);
		}
	}

	//check subquery expression (math) type
	public static void getColumnInSubqueryExpression(SubqueryExpression subqueryExpression,
            											ArrayList<ColumnExpression> expressionList) {
	    //nothing to do..
	}
	
	public static void getColumnInAggregateExpression(AggregateExpression aggregateExpression,
														 ArrayList<ColumnExpression> expressionList) {
		// just check the subexpression
		MathExpression expression = aggregateExpression.expression;
		getColumnInMathExpression(expression, expressionList);
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public static void getColumnInGeneralFunctionExpression(GeneralFunctionExpression generalFunctionExpression,
														ArrayList<ColumnExpression> expressionList) 
	{
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			getColumnInMathExpression(expression, expressionList);
		}
	}
	
	public static void getColumnInPredicateWrapperExpression(
			PredicateWrapper predicateWrapperExpression,
			ArrayList<ColumnExpression> expressionList) {
		BooleanPredicate predicate = predicateWrapperExpression.predicate;
		getColumnInBooleanPredicate(predicate, expressionList);
	}

	public static void getColumnInSetExpression(SetExpression setExpression,
												   ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<MathExpression> List = setExpression.expressionList;
		for(int i = 0; i < List.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			getColumnInMathExpression(expression, expressionList);
		}
	}
	
	
	
	
	/*
	 * --------------------------------------------BooleanPredicate--------------------------
	 * The BooleanPredicate that can contain the MathExpression or BooleanPredicate includes
	 * 1. AndPredicate
	 * 2. OrPredicate
	 * 3. NotPredicate
	 * 4. BetweenPredicate
	 * 5. ComparisonPredicate
	 * 6. LikePredicate
	 * 7. ExistPredicate
	 * 8. InPredicate
	 * 
	 * Basic BooleanPredicate do not have some components that need simplification:
	 * 1. AtomPredicate
	 */
	
	public static void getColumnInAndPredicate(AndPredicate andPredicate,
												  ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getColumnInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public static void getColumnInOrPredicate(OrPredicate orPredicate,
											     ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getColumnInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public static void getColumnInNotPredicate(NotPredicate notPredicate,
	 											  ArrayList<ColumnExpression> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getColumnInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public static void getColumnInBetweenPredicate(BetweenPredicate betweenPredicate,
													  ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		getColumnInMathExpression(expression, expressionList);
		getColumnInMathExpression(lowerExpression, expressionList);
		getColumnInMathExpression(upperExpression, expressionList);
	}

	//comparison predicate
	public static void getColumnInComparisonPredicate(ComparisonPredicate comparisonPredicate,
														 ArrayList<ColumnExpression> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		getColumnInMathExpression(leftExpression, expressionList);
		getColumnInMathExpression(rightExpression, expressionList);
	}

	//like predicate
	public static void getColumnInLikePredicate(LikePredicate likePredicate,
												   ArrayList<ColumnExpression> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		getColumnInMathExpression(valueExpression, expressionList);
		getColumnInMathExpression(patternExpression, expressionList);
		
	}

	//exist predicate
	public static void getColumnInExistPredicate(ExistPredicate existPredicate,
   													ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		getColumnInMathExpression(expression, expressionList);
	}
	
	//in predicate, only support the single element
	public static void getColumnInInPredicate(InPredicate inPredicate,
												 ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		getColumnInMathExpression(expression, expressionList);
		getColumnInMathExpression(setExpression, expressionList);
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	//-------------------------------------interface transfer-------------------------------
	public static void getColumnInMathExpression(MathExpression expression,
			 									    ArrayList<ColumnExpression> expressionList)
	{
		if(expression instanceof AggregateExpression)
		{
			getColumnInAggregateExpression((AggregateExpression)expression, expressionList);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			getColumnInArithmeticExpression((ArithmeticExpression)expression, expressionList);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			getColumnInGeneralFunctionExpression((GeneralFunctionExpression)expression, expressionList);
		}
		else if(expression instanceof SetExpression)
		{
			getColumnInSetExpression((SetExpression)expression, expressionList);
		}
		else if(expression instanceof SubqueryExpression)
		{
			getColumnInSubqueryExpression((SubqueryExpression)expression, expressionList);
		}
		else if(expression instanceof ColumnExpression)
		{
			expressionList.add((ColumnExpression)expression);
		}
		else if(expression instanceof PredicateWrapper)
		{
			getColumnInPredicateWrapperExpression((PredicateWrapper) expression, expressionList);
		}
	}
	
	
	public static void getColumnInBooleanPredicate(BooleanPredicate predicate,
												   ArrayList<ColumnExpression> expressionList)
	{
		if(predicate instanceof AndPredicate)
		{
			getColumnInAndPredicate((AndPredicate)predicate, expressionList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getColumnInBetweenPredicate((BetweenPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getColumnInComparisonPredicate((ComparisonPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getColumnInExistPredicate((ExistPredicate)predicate, expressionList);
		}
		else if(predicate instanceof InPredicate)
		{
			getColumnInInPredicate((InPredicate)predicate, expressionList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getColumnInLikePredicate((LikePredicate)predicate, expressionList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getColumnInNotPredicate((NotPredicate)predicate, expressionList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getColumnInOrPredicate((OrPredicate)predicate, expressionList);
		}
		
	}
}
