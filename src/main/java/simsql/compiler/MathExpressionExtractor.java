

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

import simsql.compiler.expressions.MathExpression;

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
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;

;

/**
 * @author Bamboo
 * This class tries to get the top level math expression in the boolean predicate
 *
 */
public class MathExpressionExtractor {
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
	
	public static void getMathExpressionInAndPredicate(AndPredicate andPredicate,
											   ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getMathExpressionInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public static void getMathExpressionInOrPredicate(OrPredicate orPredicate,
											  ArrayList<MathExpression> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getMathExpressionInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public static void getMathExpressionInNotPredicate(NotPredicate notPredicate,
											   ArrayList<MathExpression> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getMathExpressionInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public static void getMathExpressionInBetweenPredicate(BetweenPredicate betweenPredicate,
												   ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		expressionList.add(expression);
		expressionList.add(lowerExpression);
		expressionList.add(upperExpression);
	}

	//comparison predicate
	public static void getMathExpressionInComparisonPredicate(ComparisonPredicate comparisonPredicate,
													  ArrayList<MathExpression> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		expressionList.add(leftExpression);
		expressionList.add(rightExpression);
	}

	//like predicate
	public static void getMathExpressionInLikePredicate(LikePredicate likePredicate,
												ArrayList<MathExpression> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		expressionList.add(valueExpression);
		expressionList.add(patternExpression);
		
	}

	//exist predicate
	public static void getMathExpressionInExistPredicate(ExistPredicate existPredicate,
   												 ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		expressionList.add(expression);
	}
	
	//in predicate, only support the single element
	public static void getMathExpressionInInPredicate(InPredicate inPredicate,
											  ArrayList<MathExpression> expressionList)
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		expressionList.add(expression);
		expressionList.add(setExpression);
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	public static void getMathExpressionInBooleanPredicate(BooleanPredicate predicate,
												   ArrayList<MathExpression> mathExpressionList)
	{
		if(predicate instanceof AndPredicate)
		{
			getMathExpressionInAndPredicate((AndPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getMathExpressionInBetweenPredicate((BetweenPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getMathExpressionInComparisonPredicate((ComparisonPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getMathExpressionInExistPredicate((ExistPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof InPredicate)
		{
			getMathExpressionInInPredicate((InPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getMathExpressionInLikePredicate((LikePredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getMathExpressionInNotPredicate((NotPredicate)predicate, mathExpressionList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getMathExpressionInOrPredicate((OrPredicate)predicate, mathExpressionList);
		}
		
	}
}
