

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


// import mcdb.compiler.parser.expression.boolExpression.AndPredicate;
// import mcdb.compiler.parser.expression.boolExpression.BetweenPredicate;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;
// import mcdb.compiler.parser.expression.boolExpression.ComparisonPredicate;
// import mcdb.compiler.parser.expression.boolExpression.ExistPredicate;
// import mcdb.compiler.parser.expression.boolExpression.InPredicate;
// import mcdb.compiler.parser.expression.boolExpression.LikePredicate;
// import mcdb.compiler.parser.expression.boolExpression.NotPredicate;
// import mcdb.compiler.parser.expression.boolExpression.OrPredicate;
// import mcdb.compiler.parser.expression.mathExpression.AggregateExpression;
// import mcdb.compiler.parser.expression.mathExpression.ArithmeticExpression;
// import mcdb.compiler.parser.expression.mathExpression.GeneralFunctionExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;
// import mcdb.compiler.parser.expression.mathExpression.PredicateWrapper;
// import mcdb.compiler.parser.expression.mathExpression.SetExpression;



/**
 * @author Bamboo
 *
 */
public class CheckAggregate {
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
	
	public static boolean directAggregateInAndPredicate(AndPredicate andPredicate)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			if(directAggregateInBooleanPredicate(andList.get(i)))
			{
				return true;
			}
		}
		return false;
	}
	
	//or predicate
	public static boolean directAggregateInOrPredicate(OrPredicate orPredicate) {
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			if(directAggregateInBooleanPredicate(orList.get(i)))
			{
				return true;
			}
		}	
		return false;
	}
	
	
	//Not predicate
	public static boolean directAggregateInNotPredicate(NotPredicate notPredicate) 
	{
		return directAggregateInBooleanPredicate(notPredicate.predicate);
	}
	
	//Between Predicate
	public static boolean directAggregateInBetweenPredicate(BetweenPredicate betweenPredicate) {
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		if(directAggregateInMathExpression(expression) ||
			directAggregateInMathExpression(lowerExpression) ||
			directAggregateInMathExpression(upperExpression))
		{
			return true;
		}
		
		return false;
	}

	//comparison predicate
	public static boolean directAggregateInComparisonPredicate(ComparisonPredicate comparisonPredicate) {
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		if(directAggregateInMathExpression(leftExpression) ||
			directAggregateInMathExpression(rightExpression))
		{
			return true;
		}
				
	    return false;
	}

	//like predicate
	public static boolean directAggregateInLikePredicate(LikePredicate likePredicate) {
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		if(directAggregateInMathExpression(valueExpression) ||
				directAggregateInMathExpression(patternExpression))
		{
			return true;
		}
				
	    return false;
	}

	//exist predicate
	public static boolean directAggregateInExistPredicate(ExistPredicate existPredicate) {
		MathExpression expression = existPredicate.existExpression;
		return directAggregateInMathExpression(expression);
	}
	
	//in predicate, only support the single element
	public static boolean directAggregateInInPredicate(InPredicate inPredicate) {
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		if(directAggregateInMathExpression(expression) ||
				directAggregateInMathExpression(setExpression))
		{
			return true;
		}
						
	    return false;
	}
	
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
	
	public static boolean directAggregateInArithmeticExpression(ArithmeticExpression arithmeticExpression) 
	{
		
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			if(directAggregateInMathExpression(expression))
				return true;
		}
		
		return false;
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public static boolean directAggregateInGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression) {
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			if(directAggregateInMathExpression(expression))
				return true;
		}
		return false;
	}
	
	public static boolean directAggregateInPredicateWrapperExpression(
			PredicateWrapper predicateWrapper) {
		return directAggregateInBooleanPredicate(predicateWrapper.predicate);
	}

	public static boolean directAggregateInSetExpression(SetExpression setExpression) {
		ArrayList<MathExpression> expressionList = setExpression.expressionList;
		for(int i = 0; i < expressionList.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			if(directAggregateInMathExpression(expression))
				return true;
		}
		return false;
	}
	
	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	public static boolean directAggregateInBooleanPredicate(BooleanPredicate predicate)
	{
		boolean result = false;
		if(predicate instanceof AndPredicate)
		{
			result = directAggregateInAndPredicate((AndPredicate)predicate);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			result = directAggregateInBetweenPredicate((BetweenPredicate)predicate);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			result = directAggregateInComparisonPredicate((ComparisonPredicate)predicate);
		}
		else if(predicate instanceof ExistPredicate)
		{
			result = directAggregateInExistPredicate((ExistPredicate)predicate);
		}
		else if(predicate instanceof InPredicate)
		{
			result = directAggregateInInPredicate((InPredicate)predicate);
		}
		else if(predicate instanceof LikePredicate)
		{
			result = directAggregateInLikePredicate((LikePredicate)predicate);
		}
		else if(predicate instanceof NotPredicate)
		{
			result = directAggregateInNotPredicate((NotPredicate)predicate);
		}
		else if(predicate instanceof OrPredicate)
		{
			result = directAggregateInOrPredicate((OrPredicate)predicate);
		}
		
		return result;
	}
	
	public static boolean directAggregateInMathExpression(MathExpression expression)
	{
		if(expression instanceof AggregateExpression)
		{
			return true;
		}
		else if(expression instanceof ArithmeticExpression)
		{
			return directAggregateInArithmeticExpression((ArithmeticExpression)expression);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			return directAggregateInGeneralFunctionExpression((GeneralFunctionExpression)expression);
		}
		else if(expression instanceof SetExpression)
		{
			return directAggregateInSetExpression((SetExpression)expression);
		}
		else if(expression instanceof PredicateWrapper)
		{
			return directAggregateInPredicateWrapperExpression((PredicateWrapper)expression);
		}
		else
		{
			return false;
		}
	}
}
