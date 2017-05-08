

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
// import mcdb.compiler.parser.expression.mathExpression.SubqueryExpression;



/**
 * @author Bamboo
 *
 */
public class CheckSubquery {
	public BooleanPredicate predicate;
	public ArrayList<SubqueryExpression> queryList;
	
	public CheckSubquery(BooleanPredicate predicate)
	{
		this.predicate = predicate;
		queryList = new ArrayList<SubqueryExpression>();
		checkSubqueryInBooleanPredicate(predicate);
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
	
	private void checkSubqueryInAndPredicate(AndPredicate andPredicate)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		for(int i = 0; i < andList.size(); i++)
		{
			BooleanPredicate child = andList.get(i);
			checkSubqueryInBooleanPredicate(child);
		}
	}
	
	//or predicate
	private void checkSubqueryInOrPredicate(OrPredicate orPredicate) {
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		for(int i = 0; i < orList.size(); i++)
		{
			BooleanPredicate child = orList.get(i);
			checkSubqueryInBooleanPredicate(child);
		}
	}
	
	//not predicate
	private void checkSubqueryInNotPredicate(NotPredicate predicate)
	{
		BooleanPredicate child = predicate.predicate;
		checkSubqueryInBooleanPredicate(child);
	}
	
	//Between Predicate
	private void checkSubqueryInBetweenPredicate(BetweenPredicate betweenPredicate) {
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		checkSubqueryInMathExpression(expression);
		checkSubqueryInMathExpression(lowerExpression);
		checkSubqueryInMathExpression(upperExpression);
	}

	//comparison predicate
	private void checkSubqueryInComparisonPredicate(ComparisonPredicate comparisonPredicate) {
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		checkSubqueryInMathExpression(leftExpression);
		checkSubqueryInMathExpression(rightExpression);
	}

	//like predicate
	private void checkSubqueryInLikePredicate(LikePredicate likePredicate) {
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		checkSubqueryInMathExpression(valueExpression);
		checkSubqueryInMathExpression(patternExpression);
	}

	//exist predicate
	private void checkSubqueryInExistPredicate(ExistPredicate existPredicate) {
		MathExpression expression = existPredicate.existExpression;
		checkSubqueryInMathExpression(expression);
	}
	
	//in predicate, only support the single element
	private void checkSubqueryInPredicate(InPredicate inPredicate) {
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		checkSubqueryInMathExpression(expression);
		checkSubqueryInMathExpression(setExpression);
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
	
	private void checkSubqueryInArithmeticExpression(ArithmeticExpression arithmeticExpression)
	{
		
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			checkSubqueryInMathExpression(expression);
		}
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	private void checkSubqueryInGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression) {
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			checkSubqueryInMathExpression(expression);
		}
	}
	
	private void checkSubqueryInPredicateWrapperExpression(PredicateWrapper expression)
	{
		BooleanPredicate predicate = expression.predicate;
		checkSubqueryInBooleanPredicate(predicate);
	}
	
	/*
	 * check the aggregate function(non-Javadoc)
	 */
	private void checkSubqueryInAggregateExpression(AggregateExpression aggregateExpression)
	{
		MathExpression expression = aggregateExpression.expression;
		checkSubqueryInMathExpression(expression);
	}

	/*
	 * check the Set function
	 */
	private void checkSubqueryInSetExpression(SetExpression setExpression) {
		ArrayList<MathExpression> expressionList = setExpression.expressionList;
		for(int i = 0; i < expressionList.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			checkSubqueryInMathExpression(expression);
		}
	}
	
	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	public void checkSubqueryInBooleanPredicate(BooleanPredicate predicate)
	{
		if(predicate instanceof AndPredicate)
		{
			checkSubqueryInAndPredicate((AndPredicate)predicate);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			checkSubqueryInBetweenPredicate((BetweenPredicate)predicate);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			checkSubqueryInComparisonPredicate((ComparisonPredicate)predicate);
		}
		else if(predicate instanceof ExistPredicate)
		{
			checkSubqueryInExistPredicate((ExistPredicate)predicate);
		}
		else if(predicate instanceof InPredicate)
		{
			checkSubqueryInPredicate((InPredicate)predicate);
		}
		else if(predicate instanceof LikePredicate)
		{
			checkSubqueryInLikePredicate((LikePredicate)predicate);
		}
		else if(predicate instanceof NotPredicate)
		{
			checkSubqueryInNotPredicate((NotPredicate)predicate);
		}
		else if(predicate instanceof OrPredicate)
		{
			checkSubqueryInOrPredicate((OrPredicate)predicate);
		}
	}
	
	public void checkSubqueryInMathExpression(MathExpression expression)
	{
		if(expression instanceof AggregateExpression)
		{
			checkSubqueryInAggregateExpression((AggregateExpression)expression);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			checkSubqueryInArithmeticExpression((ArithmeticExpression)expression);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			checkSubqueryInGeneralFunctionExpression((GeneralFunctionExpression)expression);
		}
		else if(expression instanceof SetExpression)
		{
			checkSubqueryInSetExpression((SetExpression)expression);
		}
		else if(expression instanceof SubqueryExpression)
		{
			if(!queryList.contains(expression))
				queryList.add((SubqueryExpression)expression);
		}
		else if (expression instanceof PredicateWrapper)
		{
			checkSubqueryInPredicateWrapperExpression((PredicateWrapper)expression);
		}
	}
	
	public boolean hasSubqueryInBooleanPredicate()
	{
		return queryList.size() >= 1;
	}
	
	public ArrayList<SubqueryExpression> getSubqueryList()
	{
		return queryList;
	}
}
