

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




// import mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.AggregateOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.ArithmeticOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.ColumnOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.DateOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.FunctionOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.NumberOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.PredicateToMathWrapper;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.SetOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.StarOperator;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.StringOperator;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.util.FinalVariable;
// import mcdb.compiler.parser.unnester.TemporaryViewFactory;
// import mcdb.compiler.parser.unnester.UnnestedSelectStatement;



/**
 * @author Bamboo
 *
 */
public class MathExpressionTranslator {
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
	public BooleanPredicateTranslator predicateTranslator;
	
	/**
	 * @param translatorHelper
	 */
	public MathExpressionTranslator(TranslatorHelper translatorHelper) {
		super();
		this.translatorHelper = translatorHelper;
		this.predicateTranslator = new BooleanPredicateTranslator(translatorHelper);
	}
	
	public MathExpressionTranslator(TranslatorHelper translatorHelper, 
			                        BooleanPredicateTranslator predicateTranslator) {
		super();
		this.translatorHelper = translatorHelper;
		this.predicateTranslator = predicateTranslator;
	}

	public String translateArithmeticExpression(ArithmeticExpression arithmeticExpression,
		                                               ArrayList<String> resultList,
		                                               ArrayList<MathOperator> mathOperatorList) throws Exception 
	{
		if(translatorHelper.expressionNameMap.containsKey(arithmeticExpression))
		{
			String name = translatorHelper.expressionNameMap.get(arithmeticExpression);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			
			ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
			ArrayList<Integer> operatorList = arithmeticExpression.operatorList;
			ArrayList<String> nameList = new ArrayList<String>();
			
			/*
			 * 1. Get all the sub MathOperation.
			 */
			ArrayList<MathOperator> subMathOperatorList = new ArrayList<MathOperator>();
			
			for(int i = 0; i < operandList.size(); i++)
			{
				MathExpression tempExp = operandList.get(i);
				String tempName = translateMathExpression(tempExp, resultList, subMathOperatorList);
				nameList.add(tempName);
			}
			
			MathOperator currentMathOperator = subMathOperatorList.get(0);
			String leftName = nameList.get(0);
			String resultName = "";
			String result;
			for(int i = 0; i < operatorList.size(); i++)
			{
				/*
				 * 2. Generate the tree by all the sub MathOperations.
				 */
				String mathName;
				int type;
				MathOperator left, right;
				String leftType, rightType;
				
				result = "";
				String rightName = nameList.get(i+1);
				MathExpression tempExp2 = operandList.get(i+1);
				int op = operatorList.get(i);
				resultName = getArithExpName();
				
				result += "arithExp(" + resultName;
				result += ", ";
				
				result += translaterArithmeticOperation(op);
				result += ", ";
				
				result += leftName;
				result += ", ";
				
				
				MathExpression tempExp1 = operandList.get(0);
				
				/*
				 * pay attention to here: i == 0;
				 */
				if(i == 0 && isAtomMathExpression(tempExp1))
				{
					leftType = "literal";
					result += "literal";
				}
				else if(i == 0 && (tempExp1 instanceof ColumnExpression
						|| tempExp1 instanceof SubqueryExpression) ||
						translatorHelper.expressionNameMap.containsKey(tempExp1))
				{
					leftType = "identifier";
					result += "identifier";
				}
				else
				{
					leftType = "expression";
					result += "expression";
				}
				
				result += ", ";
				
				result += rightName;
				result += ", ";
				
				if(isAtomMathExpression(tempExp2))
				{
					rightType = "literal";
					result += "literal";
				}
				else if(tempExp2 instanceof ColumnExpression ||
						translatorHelper.expressionNameMap.containsKey(tempExp2)||
						tempExp2 instanceof SubqueryExpression)
				{
					rightType = "identifier";
					result += "identifier";
				}
				else
				{
					rightType = "expression";
					result += "expression";
				}
				
				/*
				 * 2.1 Assign associated values
				 */
				mathName = resultName;
				type = op;
				right = subMathOperatorList.get(i+1);
				left = currentMathOperator;
				
				currentMathOperator = new ArithmeticOperator(mathName, type, left, right, leftType, rightType);
				result += ").\r\n";
				resultList.add(result);
				leftName = resultName;
			}
			
			mathOperatorList.add(currentMathOperator);
			return resultName;
		}
	}

	/*
	 * Here, for the setQuantifier, we need extension.
	 */
	public String translateAggregateExpression(AggregateExpression aggregateExpression,
			 										  ArrayList<String> resultList,
			 										  ArrayList<MathOperator> mathOperatorList) throws Exception 
	{
		if(translatorHelper.expressionNameMap.containsKey(aggregateExpression))
		{
			String name = translatorHelper.expressionNameMap.get(aggregateExpression);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			/*
			 * 1. Associated data structure.
			 */
			String name;
			int type;
			MathOperator childOperator;
			String childOperatorType;
			ArrayList<MathOperator> childMathOperator = new ArrayList<MathOperator>();
			
			MathExpression expression = aggregateExpression.expression;
			int setQuantifier = aggregateExpression.setQuantifier;
			int aggType = aggregateExpression.aggType;
			
			String resultName = getArithExpName();
			String result = "aggExp(";
			result += resultName;
			result += ", ";
			
			result += translaterAggregateType(aggType);
			result += ", ";
			
			result += translateMathExpression(expression, resultList, childMathOperator);
			result += ", ";
			
			if(isAtomMathExpression(expression))
			{
				childOperatorType = "literal";
				result += "literal";
			}
			else if(expression instanceof ColumnExpression ||
					translatorHelper.expressionNameMap.containsKey(aggregateExpression))
			{
				childOperatorType = "identifier";
				result += "identifier";
			}
			else
			{
				childOperatorType = "expression";
				result += "expression";
			}
			
			/*
			 * 2. Fill in the data structure.
			 */
			name = resultName;
			type = aggType;
			childOperator = childMathOperator.get(0);
			
			AggregateOperator aggregateOperator = new AggregateOperator(name, type, childOperator, childOperatorType);
			mathOperatorList.add(aggregateOperator);
			result += ").\r\n";
			
			resultList.add(result);
			return resultName;
		}
	}

	public String translateColumnExpression(ColumnExpression columnExpression,
		    					  ArrayList<String> resultList,
		    					  ArrayList<MathOperator> mathOperatorList)
	{
		mathOperatorList.add(new ColumnOperator(columnExpression.toString()));
		return columnExpression.toString();
	}
	
	
	public String translateDateExpression(DateExpression dateExpression,
			  ArrayList<String> resultList,
			  ArrayList<MathOperator> mathOperatorList)
	{
		mathOperatorList.add(new DateOperator(dateExpression.toString()));
		return dateExpression.toString();
	}
	
	public String translateNumericExpression(NumericExpression numericExpression,
			  ArrayList<String> resultList,
			  ArrayList<MathOperator> mathOperatorList)
	{
		double value = numericExpression.value;
		mathOperatorList.add(new NumberOperator(value, numericExpression.type));
		return numericExpression.toString();
	}
	
	public String translateGeneralTableIndexExpression(
			GeneralTableIndex generalIndexExpression,
			ArrayList<String> resultList,
			ArrayList<MathOperator> mathOperatorList)
	{
		mathOperatorList.add(new GeneralTableIndexOperator(generalIndexExpression.type, generalIndexExpression.identifier));
		return "GeneralIndex";
	}
	
	/*
	 * check subquery expression (math) type
	 * I am not sure if I should add tempViewName to the return result.
	 */
	public String translateSubqueryExpression(SubqueryExpression subqueryExpression,
            										 ArrayList<String> resultList,
            										 ArrayList<MathOperator> mathOperatorList) {
		if(translatorHelper.expressionNameMap.containsKey(subqueryExpression))
		{
			String name = translatorHelper.expressionNameMap.get(subqueryExpression);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			TypeChecker typechecker = ((SubqueryExpression) subqueryExpression).getTypeChecker();
			UnnestedSelectStatement unnestedStatement = TemporaryViewFactory.tempTypeCheckerViewMap.get(typechecker);
			String tempViewName = unnestedStatement.getViewName();
			
			ArrayList<String> subqueryOutput = typechecker.getAttributeList();
			String name = subqueryOutput.get(0);
			mathOperatorList.add(new ColumnOperator(name));
			translatorHelper.expressionNameMap.put(subqueryExpression, name);
			return name;
		}
	}
	
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public String translateGeneralFunctionExpression(GeneralFunctionExpression generalFunctionExpression,
															ArrayList<String> resultList,
															ArrayList<MathOperator> mathOperatorList) throws Exception 
	{
		if(translatorHelper.expressionNameMap.containsKey(generalFunctionExpression))
		{
			String name = translatorHelper.expressionNameMap.get(generalFunctionExpression);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			/*
			 * 1. Data structure for FunctionOperator.
			 */
			String name;
			String functionName;
			ArrayList<MathOperator>  parameterList = new ArrayList<MathOperator>();
			
			ArrayList<MathExpression> operandList = generalFunctionExpression.parasList;
			String funcName = generalFunctionExpression.functionName;
			ArrayList<String> nameList = new ArrayList<String>();
			ArrayList<String> typeList = new ArrayList<String>();
			
			for(int i = 0; i < operandList.size(); i++)
			{
				MathExpression tempExp = operandList.get(i);
				String tempName = translateMathExpression(tempExp, resultList, parameterList);
				nameList.add(tempName);
				
				if(isAtomMathExpression(tempExp))
				{
					typeList.add("literal");
				}
				else if(tempExp instanceof ColumnExpression ||
						translatorHelper.expressionNameMap.containsKey(tempExp))
				{
					typeList.add("identifier");
				}
				else if(tempExp instanceof PredicateWrapper)
				{
					typeList.add("booleanexp");
				}
				else
				{
					typeList.add("expression");
				}
			}
			
			String result = "";
			String resultName = getArithExpName();
			
			result += "function(";
			result += resultName;
			result += ", ";
			result += funcName;
			result += ", [";
			
			for(int i = 0; i < nameList.size(); i++)
			{
				result += "[" + nameList.get(i) + ", " + typeList.get(i) + "]";
				if(i != nameList.size() - 1)
				{
					result += ", ";
				}
			}
			result += "]).\r\n";
			
			/*
			 * 2. Assign the associated values.
			 */
			name = resultName;
			functionName = funcName;
			FunctionOperator funcOperator = new FunctionOperator(name, functionName, parameterList, typeList);
			mathOperatorList.add(funcOperator);
			resultList.add(result);
			
			return resultName;
		}
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public String translatePredicateWrapperExpression(PredicateWrapper predicateWrapper,
															 ArrayList<String> resultList,
															 ArrayList<MathOperator> mathOperatorList) throws Exception 
	{
		if(translatorHelper.expressionNameMap.containsKey(predicateWrapper))
		{
			String name = translatorHelper.expressionNameMap.get(predicateWrapper);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			BooleanPredicate predicate = predicateWrapper.predicate;
			ArrayList<BooleanOperator> booleanOperatorList = new ArrayList<BooleanOperator>(2);
			String name = predicateTranslator.translateBooleanPredicate(predicate, resultList, booleanOperatorList);
			
			for(int i = 0; i < booleanOperatorList.size(); i++)
			{
				mathOperatorList.add(new PredicateToMathWrapper(booleanOperatorList.get(i)));
			}
			return name;
		}
	}

	public String translateStringExpression(StringExpression stringExpression,
			  									   ArrayList<String> resultList,
			  									   ArrayList<MathOperator> mathOperatorList)
	{
		mathOperatorList.add(new StringOperator(stringExpression.toString()));
		return stringExpression.toString();
	}
	
	public String translateAsteriskExpression(AsteriskExpression asteriskExpression,
			  										 ArrayList<String> resultList,
			  										 ArrayList<MathOperator> mathOperatorList)
	{
		mathOperatorList.add(new StarOperator());
		return "*";
	}
	
	
	public String translateSetExpression(SetExpression setExpression,
											    ArrayList<String> resultList,
											    ArrayList<MathOperator> mathOperatorList) throws Exception
	{
		if(translatorHelper.expressionNameMap.containsKey(setExpression))
		{
			String name = translatorHelper.expressionNameMap.get(setExpression);
			mathOperatorList.add(new ColumnOperator(name));
			return name;
		}
		else
		{
			/*
			 * 1. Associated data structure
			 */
			String name;
			ArrayList<MathOperator> elementList = new ArrayList<MathOperator>();
			
			ArrayList<MathExpression> operandList = setExpression.expressionList;
			ArrayList<String> nameList = new ArrayList<String>();
			
			for(int i = 0; i < operandList.size(); i++)
			{
				MathExpression tempExp = operandList.get(i);
				String tempName = translateMathExpression(tempExp, resultList, elementList);
				nameList.add(tempName);
			}
			
			String result = "";
			String resultName = getArithExpName();
			
			result += "set(";
			result += resultName;
			result += ", ";
			result += "[";
			
			for(int i = 0; i < nameList.size(); i++)
			{
				result += nameList.get(i);
				if(i != nameList.size() - 1)
				{
					result += ", ";
				}
			}
			result += "]).\r\n";
			
			/*
			 * 2. Assign the associated values
			 */
			name = resultName;
			SetOperator setOperator = new SetOperator(name, elementList);
			mathOperatorList.add(setOperator);
			
			resultList.add(result);
			return resultName;
		}
	}
	
	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	//-------------------------------------interface transfer-------------------------------
	public String translateMathExpression(MathExpression expression,
		 									     ArrayList<String> resultList,
			 									 ArrayList<MathOperator> mathOperatorList) throws Exception
	{
		if(expression instanceof AggregateExpression)
		{
			return translateAggregateExpression((AggregateExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			return translateArithmeticExpression((ArithmeticExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			return translateGeneralFunctionExpression((GeneralFunctionExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof PredicateWrapper)
		{
			return translatePredicateWrapperExpression((PredicateWrapper)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof SetExpression)
		{
			return translateSetExpression((SetExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof SubqueryExpression)
		{
			return translateSubqueryExpression((SubqueryExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof ColumnExpression)
		{
			return translateColumnExpression((ColumnExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof DateExpression)
		{
			return translateDateExpression((DateExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof StringExpression)
		{
			return translateStringExpression((StringExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof NumericExpression)
		{
			return translateNumericExpression((NumericExpression)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof GeneralTableIndex)
		{
			return translateGeneralTableIndexExpression((GeneralTableIndex)expression, resultList, mathOperatorList);
		}
		else if(expression instanceof AsteriskExpression)
		{
			return translateAsteriskExpression((AsteriskExpression)expression, resultList, mathOperatorList);
		}
		else
			return "";
	}
	
	
	private String getArithExpName()
	{
		int index = translatorHelper.getArithExpIndex();
		return "arithExp" + index;
	}
	
	//translate the arithmetic expression type
	public String translaterArithmeticOperation(int type)
	{
		String result = "";
		
		switch(type)
		{
			case FinalVariable.PLUS:
				result = "plus";
				break;
				
			case FinalVariable.MINUS:
				result ="minus";
				break;
				
			case FinalVariable.TIMES:
				result = "times";
				break;
				
			case FinalVariable.DIVIDE:
				result = "divide";
				break;
		}
		
		return result;
	}
	
	
	public String translaterAggregateType(int type)
	{
		String result = "";
		
		switch(type)
		{
			case FinalVariable.AVG:
				result = "avg";
				break;
				
			case FinalVariable.SUM:
				result ="sum";
				break;
				
			case FinalVariable.COUNT:
				result = "count";
				break;
				
			case FinalVariable.COUNTALL:
				result = "countall";
				break;
				
			case FinalVariable.MIN:
				result = "min";
				break;
				
			case FinalVariable.MAX:
				result = "max";
				break;
				
			case FinalVariable.VARIANCE:
				result = "variance";
				break;
				
			case FinalVariable.STDEV:
				result = "stddev";
				break;

			case FinalVariable.VECTOR:
				result = "vector";
				break;
				
			case FinalVariable.ROWMATRIX:
				result = "rowmatrix";
				break;
				
			case FinalVariable.COLMATRIX:
				result = "colmatrix";
				break;
		}
		
		return result;
	}
	
	
	public boolean isAtomMathExpression(MathExpression expression)
	{
		if(expression instanceof DateExpression ||
				expression instanceof NumericExpression ||
				expression instanceof StringExpression||
				expression instanceof GeneralTableIndex)
		{
			return true;
		}
		else
			return false;		
				
	}
	
}
