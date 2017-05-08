

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



/**
 * @author Bamboo
 *
 */
public class GeneralTableIndexChecker {
	
	/*
	 * The goal is to make sure that An MathExpression are consists of basic
	 * calculations of Numerical values, GeneralTableIndex, functions, and the arithmetic calculations.
	 */
	
	/*
	 * --------------------------------------------MathExpression--------------------------
	 * 1. ArithmeticExpression
	 * 2. SubqueryExpression
	 * 3. AggregateExpression
	 * 4. GeneralFunctionExpression
	 * 5. SetExpression
	 * 6. AsteriskExpression
	 * 7. ColumnExpression
	 * 8. DateExpression
	 * 9. NumericExpression
	 * 10. StringExpression
	 * 11. GeneralIndexTable
	 */
	
	public static boolean checkInArithmeticExpression(ArithmeticExpression arithmeticExpression)
	{
		
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			if(!checkInMathExpression(expression))
				return false;
		}
		return true;
	}
	
	public static boolean checkInGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression) {
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for (MathExpression expression : paraList) {
			if (!checkInMathExpression(expression))
				return false;
		}
		return true;
	}
	
	public static boolean checkInMathExpression(MathExpression expression)
	{
		if(expression instanceof ArithmeticExpression)
		{
			return checkInArithmeticExpression((ArithmeticExpression)expression);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			return checkInGeneralFunctionExpression((GeneralFunctionExpression)expression);
		}
		else if(expression instanceof NumericExpression)
		{
			return true;
		}
		else if(expression instanceof GeneralTableIndex)
		{
			return true;
		}

		return false;
	}
}
