

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

import java.util.ArrayList;



// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;



/**
 * @author Bamboo
 *
 */
public class GeneralTableIndexExtractor {
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
	
	public static void getGeneralTableIndexInArithmeticExpression(ArithmeticExpression arithmeticExpression,
			                                              ArrayList<MathExpression> expressionList) 
	{
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			getGeneralTableIndexInMathExpression(expression, expressionList);
		}
	}

	//check subquery expression (math) type
	public static void getGeneralTableIndexInSubqueryExpression(SubqueryExpression subqueryExpression,
            											ArrayList<MathExpression> expressionList) {
	    //nothing to do..
	}
	
	public static void getGeneralTableIndexInAggregateExpression(AggregateExpression aggregateExpression,
														 ArrayList<MathExpression> expressionList) {
		// just check the subexpression
		MathExpression expression = aggregateExpression.expression;
		getGeneralTableIndexInMathExpression(expression, expressionList);
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public static void getGeneralTableIndexInFunctionExpression(GeneralFunctionExpression generalFunctionExpression,
														ArrayList<MathExpression> expressionList) 
	{
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			getGeneralTableIndexInMathExpression(expression, expressionList);
		}
	}
	
	public static void getGeneralTableIndexInPredicateWrapperExpression(PredicateWrapper predicateWrapper,
														ArrayList<MathExpression> expressionList) 
	{
		getGeneralTableIndexInBooleanPredicate(predicateWrapper.predicate, expressionList);
	}

	public static void getGeneralTableIndexInSetExpression(SetExpression setExpression,
												   ArrayList<MathExpression> expressionList)
	{
		ArrayList<MathExpression> List = setExpression.expressionList;
		for(int i = 0; i < List.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			getGeneralTableIndexInMathExpression(expression, expressionList);
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
	
	public static void getGeneralTableIndexInAndPredicate(AndPredicate andPredicate,
												  ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getGeneralTableIndexInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public static void getGeneralTableIndexInOrPredicate(OrPredicate orPredicate,
											     ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getGeneralTableIndexInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public static void getGeneralTableIndexInNotPredicate(NotPredicate notPredicate,
	 											  ArrayList<MathExpression> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getGeneralTableIndexInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public static void getGeneralTableIndexInBetweenPredicate(BetweenPredicate betweenPredicate,
													  ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		getGeneralTableIndexInMathExpression(expression, expressionList);
		getGeneralTableIndexInMathExpression(lowerExpression, expressionList);
		getGeneralTableIndexInMathExpression(upperExpression, expressionList);
	}

	//comparison predicate
	public static void getGeneralTableIndexInComparisonPredicate(ComparisonPredicate comparisonPredicate,
														 ArrayList<MathExpression> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		getGeneralTableIndexInMathExpression(leftExpression, expressionList);
		getGeneralTableIndexInMathExpression(rightExpression, expressionList);
	}

	//like predicate
	public static void getGeneralTableIndexInLikePredicate(LikePredicate likePredicate,
												   ArrayList<MathExpression> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		getGeneralTableIndexInMathExpression(valueExpression, expressionList);
		getGeneralTableIndexInMathExpression(patternExpression, expressionList);
		
	}

	//exist predicate
	public static void getGeneralTableIndexInExistPredicate(ExistPredicate existPredicate,
   													ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		getGeneralTableIndexInMathExpression(expression, expressionList);
	}
	
	//in predicate, only support the single element
	public static void getGeneralTableIndexInInPredicate(InPredicate inPredicate,
												 ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		getGeneralTableIndexInMathExpression(expression, expressionList);
		getGeneralTableIndexInMathExpression(setExpression, expressionList);
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	//-------------------------------------interface transfer-------------------------------
	public static void getGeneralTableIndexInMathExpression(MathExpression expression,
			 									    ArrayList<MathExpression> expressionList)
	{
		if(expression instanceof AggregateExpression)
		{
			getGeneralTableIndexInAggregateExpression((AggregateExpression)expression, expressionList);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			getGeneralTableIndexInArithmeticExpression((ArithmeticExpression)expression, expressionList);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			getGeneralTableIndexInFunctionExpression((GeneralFunctionExpression)expression, expressionList);
		}
		else if(expression instanceof SetExpression)
		{
			getGeneralTableIndexInSetExpression((SetExpression)expression, expressionList);
		}
		else if(expression instanceof SubqueryExpression)
		{
			getGeneralTableIndexInSubqueryExpression((SubqueryExpression)expression, expressionList);
		}
		else if(expression instanceof PredicateWrapper)
		{
			getGeneralTableIndexInPredicateWrapperExpression((PredicateWrapper)expression, expressionList);
		}
		else if(expression instanceof GeneralTableIndex)
		{
			expressionList.add(expression);
		}
	}
	
	
	public static void getGeneralTableIndexInBooleanPredicate(BooleanPredicate predicate,
			    												  ArrayList<MathExpression> expressionList)
	{
		if(predicate instanceof AndPredicate)
		{
			getGeneralTableIndexInAndPredicate((AndPredicate)predicate, expressionList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getGeneralTableIndexInBetweenPredicate((BetweenPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getGeneralTableIndexInComparisonPredicate((ComparisonPredicate)predicate, expressionList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getGeneralTableIndexInExistPredicate((ExistPredicate)predicate, expressionList);
		}
		else if(predicate instanceof InPredicate)
		{
			getGeneralTableIndexInInPredicate((InPredicate)predicate, expressionList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getGeneralTableIndexInLikePredicate((LikePredicate)predicate, expressionList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getGeneralTableIndexInNotPredicate((NotPredicate)predicate, expressionList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getGeneralTableIndexInOrPredicate((OrPredicate)predicate, expressionList);
		}
		
	}
}	
