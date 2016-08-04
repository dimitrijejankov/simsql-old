

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

import java.util.ArrayList;



// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;



/**
 * @author Bamboo
 *
 */
public class AggregateExpressionExtractor {
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
	 */
	
	public static void getAggregateInArithmeticExpression(ArithmeticExpression arithmeticExpression,
			                                              ArrayList<MathExpression> expressionList) 
	{
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			getAggregateInMathExpression(expression, expressionList);
		}
	}

	//check subquery expression (math) type
	public static void getAggregateInSubqueryExpression(SubqueryExpression subqueryExpression,
            											ArrayList<MathExpression> expressionList) {
	    //nothing to do..
	}
	
	public static void getAggregateInAggregateExpression(AggregateExpression aggregateExpression,
														 ArrayList<MathExpression> expressionList) {
		// just check the subexpression
		expressionList.add(aggregateExpression);
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public static void getAggregateGeneralFunctionExpression(GeneralFunctionExpression generalFunctionExpression,
														ArrayList<MathExpression> expressionList) 
	{
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			getAggregateInMathExpression(expression, expressionList);
		}
	}
	
	public static void getAggregateInPredicateWrapperExpression(PredicateWrapper predicateWrapper,
														ArrayList<MathExpression> expressionList) 
	{
		getAggregateInBooleanPredicate(predicateWrapper.predicate, expressionList);
	}

	public static void getAggregateInSetExpression(SetExpression setExpression,
												   ArrayList<MathExpression> expressionList)
	{
		ArrayList<MathExpression> List = setExpression.expressionList;
		for(int i = 0; i < List.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			getAggregateInMathExpression(expression, expressionList);
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
	
	public static void getAggregateInAndPredicate(AndPredicate andPredicate,
												  ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getAggregateInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public static void getAggregateInOrPredicate(OrPredicate orPredicate,
											     ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getAggregateInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public static void getAggregateInNotPredicate(NotPredicate notPredicate,
	 											  ArrayList<MathExpression> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getAggregateInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public static void getAggregateInBetweenPredicate(BetweenPredicate betweenPredicate,
													  ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		getAggregateInMathExpression(expression, expressionList);
		getAggregateInMathExpression(lowerExpression, expressionList);
		getAggregateInMathExpression(upperExpression, expressionList);
	}

	//comparison predicate
	public static void getAggregateInComparisonPredicate(ComparisonPredicate comparisonPredicate,
														 ArrayList<MathExpression> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		getAggregateInMathExpression(leftExpression, expressionList);
		getAggregateInMathExpression(rightExpression, expressionList);
	}

	//like predicate
	public static void getAggregateInLikePredicate(LikePredicate likePredicate,
												   ArrayList<MathExpression> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		getAggregateInMathExpression(valueExpression, expressionList);
		getAggregateInMathExpression(patternExpression, expressionList);
		
	}

	//exist predicate
	public static void getAggregateInExistPredicate(ExistPredicate existPredicate,
   													ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		getAggregateInMathExpression(expression, expressionList);
	}
	
	//in predicate, only support the single element
	public static void getAggregateInInPredicate(InPredicate inPredicate,
												 ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		getAggregateInMathExpression(expression, expressionList);
		getAggregateInMathExpression(setExpression, expressionList);
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	//-------------------------------------interface transfer-------------------------------
	public static void getAggregateInMathExpression(MathExpression expression,
			 									    ArrayList<MathExpression> expressionList)
	{
		if(expression instanceof AggregateExpression)
		{
			getAggregateInAggregateExpression((AggregateExpression)expression, expressionList);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			getAggregateInArithmeticExpression((ArithmeticExpression)expression, expressionList);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			getAggregateGeneralFunctionExpression((GeneralFunctionExpression)expression, expressionList);
		}
		else if(expression instanceof SetExpression)
		{
			getAggregateInSetExpression((SetExpression)expression, expressionList);
		}
		else if(expression instanceof SubqueryExpression)
		{
			getAggregateInSubqueryExpression((SubqueryExpression)expression, expressionList);
		}
		else if(expression instanceof PredicateWrapper)
		{
			getAggregateInPredicateWrapperExpression((PredicateWrapper)expression, expressionList);
		}
	}
	
	
	public static void getAggregateInBooleanPredicate(BooleanPredicate predicate,
			    												  ArrayList<MathExpression> expressionList)
	{
		if(predicate instanceof AndPredicate)
		{
			getAggregateInAndPredicate((AndPredicate)predicate, expressionList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getAggregateInBetweenPredicate((BetweenPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getAggregateInComparisonPredicate((ComparisonPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getAggregateInExistPredicate((ExistPredicate)predicate, expressionList);
		}
		else if(predicate instanceof InPredicate)
		{
			getAggregateInInPredicate((InPredicate)predicate, expressionList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getAggregateInLikePredicate((LikePredicate)predicate, expressionList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getAggregateInNotPredicate((NotPredicate)predicate, expressionList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getAggregateInOrPredicate((OrPredicate)predicate, expressionList);
		}
		
	}
}	
