

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

import java.util.ArrayList;




// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AndOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.AtomOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.CompOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.NotOperator;
// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.OrOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.util.FinalVariable;


/**
 * @author Bamboo
 *
 */
public class BooleanPredicateTranslator {
	
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
	public MathExpressionTranslator mathExpressionTranslator;
	public TranslatorHelper translatorHelper;
	
	
	/**
	 * @param mathExpressionTranslator
	 * @param translatorHelper
	 */
	public BooleanPredicateTranslator(TranslatorHelper translatorHelper) {
		super();
		this.mathExpressionTranslator = new MathExpressionTranslator(translatorHelper, this);
		this.translatorHelper = translatorHelper;
	}

	//AndPredicate
	public String translateAndPredicate(AndPredicate andPredicate,
											   ArrayList<String> resultList,
											   ArrayList<BooleanOperator> operatorList) throws Exception
	{
		String predicateName = getPredicateName();
		
		ArrayList<BooleanOperator> subOperatorList = new ArrayList<BooleanOperator>();
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		String result = "boolAnd(" + predicateName + ", [";
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			if(i != andList.size() - 1)
			{
				result += translateBooleanPredicate(andList.get(i), resultList, subOperatorList);
				result += ", ";
			}
			else
			{
				result += translateBooleanPredicate(andList.get(i), resultList, subOperatorList);
			}
		}
		operatorList.add(new AndOperator(predicateName, subOperatorList));
		
		result += "]).\r\n";
		resultList.add(result);
		
		return predicateName;
	}
	
	//OrPredicate
	public String translateOrPredicate(OrPredicate orPredicate,
											  ArrayList<String> resultList,
											  ArrayList<BooleanOperator> operatorList) throws Exception
	{
		String predicateName = getPredicateName();
		
		ArrayList<BooleanOperator> subOperatorList = new ArrayList<BooleanOperator>();
		ArrayList<BooleanPredicate> andList = orPredicate.orList;
		
		String result = "boolOr(" + predicateName + ", [";
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			if(i != andList.size() - 1)
			{
				result += translateBooleanPredicate(andList.get(i), resultList, subOperatorList);
				result += ", ";
			}
			else
			{
				result += translateBooleanPredicate(andList.get(i), resultList, subOperatorList);
			}	
		}
		
		operatorList.add(new OrOperator(predicateName, subOperatorList));
		result += "]).\r\n";
		resultList.add(result);
		
		return predicateName;
	}
	
	//Not predicate
	public String translateNotPredicate(NotPredicate notPredicate,
	 										   ArrayList<String> resultList,
	 										   ArrayList<BooleanOperator> operatorList) throws Exception 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		String predicateName = getPredicateName();
		String result = "boolNot(" + predicateName + ", [";
		
		ArrayList<BooleanOperator> subOperatorList = new ArrayList<BooleanOperator>();
		
		result += translateBooleanPredicate(predicate, resultList, subOperatorList);
		
		result += "]).\r\n";
		resultList.add(result);
		operatorList.add(new NotOperator(predicateName, subOperatorList.get(0)));
		return predicateName;
	}
	
	//AtomPredicate
	public String translateAtomPredicate(AtomPredicate atomPredicate,
	 										   	ArrayList<String> resultList,
	 										   	ArrayList<BooleanOperator> operatorList) 
	{
		boolean predicate = atomPredicate.predicate;
		operatorList.add(new AtomOperator(predicate + ""));
		
		return predicate + "";
	}
	
	//Between Predicate
	public String translateBetweenPredicate(BetweenPredicate betweenPredicate,
												   ArrayList<String> resultList,
												   ArrayList<BooleanOperator> operatorList) throws Exception
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate .upExpression;
		
		BooleanPredicate leftPredicate = new ComparisonPredicate(expression,
																 lowerExpression,
				                                                 FinalVariable.GREATEREQUAL);
		BooleanPredicate rightPredicate = new ComparisonPredicate(expression,
				upperExpression,
                FinalVariable.LESSEQUAL);

		ArrayList<BooleanPredicate> predicateList = new ArrayList<BooleanPredicate>();
		predicateList.add(leftPredicate);
		predicateList.add(rightPredicate);
		
		BooleanPredicate andPredicate = new AndPredicate(predicateList);
		return translateBooleanPredicate(andPredicate, resultList, operatorList);
	}

	//comparison predicate
	public String translateComparisonPredicate(ComparisonPredicate comparisonPredicate,
													  ArrayList<String> resultList, 
													  ArrayList<BooleanOperator> operatorList) throws Exception
	{
		/*
		 * 1. The data structure of CompOperator
		 */
		String predicateName = getPredicateName();
		int op = comparisonPredicate.op;
		MathOperator left, right;
		String leftType, rightType;
		
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		String result = "compExp(" + predicateName;
		
		result += ", ";
		
		//2. Deal with the operation type
		result += translaterComparisonOperation(op);
		result += ", ";
		
		ArrayList<MathOperator> mathOperatorList = new ArrayList<MathOperator>(2);
		result += mathExpressionTranslator.translateMathExpression(leftExpression, resultList, mathOperatorList);
		result += ", ";
		result += mathExpressionTranslator.translateMathExpression(rightExpression, resultList, mathOperatorList);
		result += ", ";
		
		if(mathExpressionTranslator.isAtomMathExpression(leftExpression))
		{
			leftType = "literal";
		}
		else if(leftExpression instanceof ColumnExpression ||
				translatorHelper.expressionNameMap.containsKey(leftExpression) ||
				leftExpression instanceof SubqueryExpression)
		{
			leftType = "identifier";
		}
		else
		{
			leftType = "expression";
		}
		
		if(mathExpressionTranslator.isAtomMathExpression(rightExpression))
		{
			rightType = "literal";
			result += "literal";
		}
		else if(rightExpression instanceof ColumnExpression ||
				translatorHelper.expressionNameMap.containsKey(rightExpression)||
				rightExpression instanceof SubqueryExpression)
		{
			rightType = "identifier";
			result += "identifier";
		}
		else
		{
			rightType = "expression";
			result += "expression";
		}
		
		
		result += ").\r\n";
		resultList.add(result);
		/*
		 * 3. Create the CompOperator.
		 */
		left = mathOperatorList.get(0);
		right = mathOperatorList.get(1);
		CompOperator compOperator = new CompOperator(predicateName, op, left, right, leftType, rightType);
		operatorList.add(compOperator);
		
		return predicateName;
	}

	//like predicate
	public String translateLikePredicate(LikePredicate likePredicate,
												ArrayList<String> resultList,
												ArrayList<BooleanOperator> operatorList) throws Exception
	{
		System.err.println("Like predicate is not supported!");
		throw new Exception("Like predicate is not supported!");
	}

	//exist predicate
	public String translateExistPredicate(ExistPredicate existPredicate,
												 ArrayList<String> resultList,
												 ArrayList<BooleanOperator> operatorList)
	{
		//nothing to do..
		return true+"";
	}
	
	/*
	 * in predicate, only support the single element
	 * Pay attention here: maybe it does not support the semantics..
	 */
	
	public String translateInPredicate(InPredicate inPredicate,
											  ArrayList<String> resultList,
											  ArrayList<BooleanOperator> operatorList) throws Exception
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		BooleanPredicate predicate = new ComparisonPredicate(expression, setExpression, FinalVariable.EQUALS);
		return translateBooleanPredicate(predicate, resultList, operatorList);
	}

	public String translateBooleanPredicate(BooleanPredicate predicate,
												   ArrayList<String> resultList,
												   ArrayList<BooleanOperator> operatorList) throws Exception
	{
		if(predicate instanceof AndPredicate)
		{
			return translateAndPredicate((AndPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof AtomPredicate)
		{
			return translateAtomPredicate((AtomPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			return translateBetweenPredicate((BetweenPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			return translateComparisonPredicate((ComparisonPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			return translateExistPredicate((ExistPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof InPredicate)
		{
			return translateInPredicate((InPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof LikePredicate)
		{
			return translateLikePredicate((LikePredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof NotPredicate)
		{
			return translateNotPredicate((NotPredicate)predicate, resultList, operatorList);
		}
		else if(predicate instanceof OrPredicate)
		{
			return translateOrPredicate((OrPredicate)predicate, resultList, operatorList);
		}
		else
		{
			return "";
		}
	}
	
	private String getPredicateName()
	{
		String name = "predicate" + translatorHelper.getPredicateIndex();
		return name;
	}
	
	public String translaterComparisonOperation(int type)
	{
		String result = "";
		switch(type)
		{
			case FinalVariable.EQUALS:
				result = "equals";
				break;
				
			case FinalVariable.NOTEQUALS:
				result ="notEquals";
				break;
				
			case FinalVariable.LESSTHAN:
				result = "lessThan";
				break;
				
			case FinalVariable.GREATERTHAN:
				result = "greaterThan";
				break;
				
			case FinalVariable.LESSEQUAL:
				result = "lessEquals";
				break;
				
			case FinalVariable.GREATEREQUAL:
				result = "greaterEquals";
				break;
		}
		
		return result;
	}
	
}
