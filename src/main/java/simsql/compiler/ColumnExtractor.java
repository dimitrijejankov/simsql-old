

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

import simsql.compiler.expressions.*;

import java.util.ArrayList;



// import mcdb.compiler.parser.astVisitor.TypeChecker;
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
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.mathExpression.GeneralFunctionExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;
// import mcdb.compiler.parser.expression.mathExpression.PredicateWrapper;
// import mcdb.compiler.parser.expression.mathExpression.SetExpression;
// import mcdb.compiler.parser.expression.mathExpression.SubqueryExpression;
// import mcdb.compiler.parser.unnester.TemporaryViewFactory;
// import mcdb.compiler.parser.unnester.UnnestedSelectStatement;



/**
 * @author Bamboo
 *	This Column Extractor also extracts the temporary columns that are created during the running. 
 */
public class ColumnExtractor {
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
	public TranslatorHelper translatorHelper;
	
	
	/**
	 * @param translatorHelper
	 */
	public ColumnExtractor(TranslatorHelper translatorHelper) {
		super();
		this.translatorHelper = translatorHelper;
	}

	public void getColumnInArithmeticExpression(ArithmeticExpression arithmeticExpression,
		                                               ArrayList<String> columnList) 
	{
		if(translatorHelper.expressionNameMap.containsKey(arithmeticExpression))
		{
			String name = translatorHelper.expressionNameMap.get(arithmeticExpression);
			columnList.add(name);
		}
		else
		{
			ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
			for(int i = 0; i < operandList.size(); i++)
			{
				MathExpression expression = operandList.get(i);
				getColumnInMathExpression(expression, columnList);
			}
		}
	}

	//check subquery expression (math) type
	public void getColumnInSubqueryExpression(SubqueryExpression subqueryExpression,
            										 ArrayList<String> columnList) {
		if(translatorHelper.expressionNameMap.containsKey(subqueryExpression))
		{
			String name = translatorHelper.expressionNameMap.get(subqueryExpression);
			columnList.add(name);
		}
		else
		{
			TypeChecker typechecker = ((SubqueryExpression) subqueryExpression).getTypeChecker();
			UnnestedSelectStatement unnestedStatement = TemporaryViewFactory.tempTypeCheckerViewMap.get(typechecker);
			String tempViewName = unnestedStatement.getViewName();
			
			ArrayList<String> subqueryOutput = typechecker.getAttributeList();
			String attribute = subqueryOutput.get(0);
			translatorHelper.expressionNameMap.put(subqueryExpression, attribute);
			columnList.add(attribute);
		}
	}
	
	public void getColumnInAggregateExpression(AggregateExpression aggregateExpression,
														 ArrayList<String> columnList) {
		if(translatorHelper.expressionNameMap.containsKey(aggregateExpression))
		{
			String name = translatorHelper.expressionNameMap.get(aggregateExpression);
			columnList.add(name);
		}
		else
		{
			// just check the subexpression
			MathExpression expression = aggregateExpression.expression;
			getColumnInMathExpression(expression, columnList);
		}
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public void getColumnInGeneralFunctionExpression(GeneralFunctionExpression generalFunctionExpression,
														    ArrayList<String> columnList) 
	{
		if(translatorHelper.expressionNameMap.containsKey(generalFunctionExpression))
		{
			String name = translatorHelper.expressionNameMap.get(generalFunctionExpression);
			columnList.add(name);
		}
		else
		{
			ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
			for(int i = 0; i < paraList.size(); i++)
			{
				MathExpression expression = paraList.get(i);
				getColumnInMathExpression(expression, columnList);
			}
		}
	}
	
	public void getColumnInWrapperPredicateExpression(PredicateWrapper wrapperPredicate,
			ArrayList<String> columnList) 
	{
		if(translatorHelper.expressionNameMap.containsKey(wrapperPredicate))
		{
			String name = translatorHelper.expressionNameMap.get(wrapperPredicate);
			columnList.add(name);
		}
		else
		{
			BooleanPredicate predicate = wrapperPredicate.predicate;
			getColumnInBooleanPredicate(predicate, columnList);
		}
	}
	public void getColumnInSetExpression(SetExpression setExpression,
											    ArrayList<String> columnList)
	{
		if(translatorHelper.expressionNameMap.containsKey(setExpression))
		{
			String name = translatorHelper.expressionNameMap.get(setExpression);
			columnList.add(name);
		}
		else
		{
			ArrayList<MathExpression> expressionList = setExpression.expressionList;
			for(int i = 0; i < expressionList.size(); i++)
			{
				MathExpression expression = expressionList.get(i);
				getColumnInMathExpression(expression, columnList);
			}
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
	
	public void getColumnInAndPredicate(AndPredicate andPredicate,
												  ArrayList<String> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getColumnInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public void getColumnInOrPredicate(OrPredicate orPredicate,
											     ArrayList<String> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getColumnInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public void getColumnInNotPredicate(NotPredicate notPredicate,
	 											  ArrayList<String> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getColumnInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public void getColumnInBetweenPredicate(BetweenPredicate betweenPredicate,
													  ArrayList<String> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		getColumnInMathExpression(expression, expressionList);
		getColumnInMathExpression(lowerExpression, expressionList);
		getColumnInMathExpression(upperExpression, expressionList);
	}

	//comparison predicate
	public void getColumnInComparisonPredicate(ComparisonPredicate comparisonPredicate,
														 ArrayList<String> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		getColumnInMathExpression(leftExpression, expressionList);
		getColumnInMathExpression(rightExpression, expressionList);
	}

	//like predicate
	public void getColumnInLikePredicate(LikePredicate likePredicate,
												   ArrayList<String> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		getColumnInMathExpression(valueExpression, expressionList);
		getColumnInMathExpression(patternExpression, expressionList);
		
	}

	//exist predicate
	public void getColumnInExistPredicate(ExistPredicate existPredicate,
   													ArrayList<String> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		getColumnInMathExpression(expression, expressionList);
	}
	
	//in predicate, only support the single element
	public void getColumnInInPredicate(InPredicate inPredicate,
												 ArrayList<String> expressionList)
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
	public void getColumnInMathExpression(MathExpression expression,
			 									    ArrayList<String> columnList)
	{
		if(expression instanceof AggregateExpression)
		{
			getColumnInAggregateExpression((AggregateExpression)expression, columnList);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			getColumnInArithmeticExpression((ArithmeticExpression)expression, columnList);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			getColumnInGeneralFunctionExpression((GeneralFunctionExpression)expression, columnList);
		}
		else if(expression instanceof PredicateWrapper)
		{
			getColumnInWrapperPredicateExpression((PredicateWrapper)expression, columnList);
		}
		else if(expression instanceof SetExpression)
		{
			getColumnInSetExpression((SetExpression)expression, columnList);
		}
		else if(expression instanceof SubqueryExpression)
		{
			getColumnInSubqueryExpression((SubqueryExpression)expression, columnList);
		}
		else if(expression instanceof ColumnExpression)
		{
			columnList.add(expression.toString());
		}
	}
	
	
	public void getColumnInBooleanPredicate(BooleanPredicate predicate,
												   ArrayList<String> columnList)
	{
		if(predicate instanceof AndPredicate)
		{
			getColumnInAndPredicate((AndPredicate)predicate, columnList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getColumnInBetweenPredicate((BetweenPredicate)predicate, columnList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getColumnInComparisonPredicate((ComparisonPredicate)predicate, columnList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getColumnInExistPredicate((ExistPredicate)predicate, columnList);
		}
		else if(predicate instanceof InPredicate)
		{
			getColumnInInPredicate((InPredicate)predicate, columnList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getColumnInLikePredicate((LikePredicate)predicate, columnList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getColumnInNotPredicate((NotPredicate)predicate, columnList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getColumnInOrPredicate((OrPredicate)predicate, columnList);
		}
		
	}
}
