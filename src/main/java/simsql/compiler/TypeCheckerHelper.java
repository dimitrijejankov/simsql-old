

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
package simsql.compiler; // package mcdb.compiler.parser.astVisitor.rule;

import simsql.compiler.expressions.AggregateExpression;
import simsql.compiler.expressions.ArithmeticExpression;
import simsql.compiler.expressions.MathExpression;

import java.util.ArrayList;


// import mcdb.catalog.Attribute;
// import mcdb.compiler.parser.astVisitor.*;
// import mcdb.compiler.parser.expression.mathExpression.AggregateExpression;
// import mcdb.compiler.parser.expression.mathExpression.ArithmeticExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;


/**
 * @author Bamboo
 *
 */
public class TypeCheckerHelper {
	public static final int BASELINE_RANDOM_TABLE_TYPECHECKER = 0;
	public static final int GENERAL_RANDOM_TABLE_TYPECHECKER = 1;
	public static final int RANDOM_TABLE_TYPECHECKER = 2;
	public static final int VIEW_TYPECHECKER = 3;
	public static final int TABLE_DEFINE_CHECKER = 4;
	public static final int VGFUNC_DEFINE_CHECKER = 5;
	public static final int TYPECHECKER = 6;
	public static final int BASELINE_ARRAY_RANDOM_TABLE_TYPECHECKER = 7;
	public static final int UNION_VIEW_TYPECHECKER = 8;
	public static final int MULTIDIMENSIONAL_VIEW_TYPECHECKER = 9;

	public static boolean hasAggregate(MathExpression expression)
	{
		if(expression instanceof AggregateExpression)
		{
			return true;
		}
		else if(expression instanceof ArithmeticExpression)
		{
			ArrayList<MathExpression> operandList = ((ArithmeticExpression) expression).operandList;
			for(int i = 0; i < operandList.size(); i++)
			{
				MathExpression temp = operandList.get(i);
				if(hasAggregate(temp))
				{
					return true;
				}
			}
		}
		return false;
	}
	
	// public static int arithmeticCompatibleType(int type[])
	// {
	// 	int compatibleResult = type[0];
		
	// 	for(int i = 1; i < type.length; i++)
	// 	{
	// 		if(compatibleResult == -1)
	// 			return -1;
			
	// 		compatibleResult = arithmeticCompate(compatibleResult, type[i]);
	// 	}
	// 	return compatibleResult;
	// }
	
	// /*
	//  * 1. Allowed expression:
	//  * 	   number + number, number + date, date+date
	//  */
	// public static DataType arithmeticCompate(int ariType, DataType type_1, DataType type_2)
	// {
	// 	/*
	// 	int INTEGER = 1;
	//     int DOUBLE = 2;
	//     int STRING = 3;
	//     int DATE = 4;
	//     int STOCHINT = 5;
	//     int STOCHDBL = 6;
	//     int STOCHDAT = 7;
	//     int SEED = 8;
	//     */

	// 	if (type_1 == null || type_2 == null) 
	// 		return null;
		
	// 	String type1String ="", type2String ="";

	// 	String str1 = type_1.getTypeName();
	// 	if (str1.equals(Attribute.INTEGER) || str1.equals(Attribute.DOUBLE) || str1.equals(Attribute.STOCHINT) 
	// 		|| str1.equals(Attribute.STOCHDBL) || str1.equals(Attribute.SEED)) {
	// 		type1String = "NUMERIC";
	// 	} else if (str1.equals(Attribute.STRING) || str1.equals(Attribute.STOCHSTR)) {
	// 		type1String = "STRING";
	// 	} else if (str1.equals(Attribute.DATE) || str1.equals(Attribute.STOCHDAT)) {
	// 		type1String = "DATE";
	// 	}
		
	// 	String str2 = type_2.getTypeName();
	// 	if (str2.equals(Attribute.INTEGER) || str2.equals(Attribute.DOUBLE) || str2.equals(Attribute.STOCHINT) 
	// 		|| str2.equals(Attribute.STOCHDBL) || str2.equals(Attribute.SEED)) {
	// 		type2String = "NUMERIC";
	// 	} else if (str2.equals(Attribute.STRING) || str2.equals(Attribute.STOCHSTR)) {
	// 		type2String = "STRING";
	// 	} else if (str2.equals(Attribute.DATE) || str2.equals(Attribute.STOCHDAT)) {
	// 		type2String = "DATE";
	// 	}

	// 	if(type1String.equals("STRING") && ariType == FinalVariable.PLUS)				// TO-DO
	// 		return type_1;
		
	// 	if(type1String.equals(type2String) && !type1String.equals("STRING"))
	// 		return type_1;
		
	// 	if((type1String.equals("NUMERIC") && type2String.equals("DATE")) ||
	// 	   (type1String.equals("DATE")    && type2String.equals("NUMERIC")))
	// 	{
	// 		return new DoubleType();
	// 	}
		
	// 	return null;
	// }
	
	// public static int directTransfer(DataType sourceType, DataType targetType)
	// {
	// 	if (sourceType == null || targetType == null) 
	// 		return -1;

	// 	String sourceStr = sourceType.getTypeName();
	// 	String targetStr = targetType.getTypeName();

	// 	if(targetStr.equals(Attribute.MULTITYPE))
	// 		return 1;
		
	// 	if(sourceStr.equals(targetStr))
	// 		return 1;
		
	// 	if(sourceStr.equals(Attribute.INTEGER) && targetStr.equals(Attribute.DOUBLE))
	// 		return 1;
	// 	else if(sourceStr.equals(Attribute.STOCHINT) && targetStr.equals(Attribute.STOCHDBL))
	// 		return 1;
	// 	else if(sourceStr.equals(Attribute.DATE) && targetStr.equals(Attribute.STRING))
	// 		return 1;
		
	// 	return -1;
	// }
	
	// /*
	//  * 1. Allowed expression:
	//  * 	   number + number, number + date, date+date
	//  */
	// public static int comparisonCompate(DataType type_1, DataType type_2)
	// {
	// 	if (type_1 == null || type_2 == null) 
	// 		return -1;

	// 	String str1 = type_1.getTypeName();
	// 	String str2 = type_2.getTypeName();

	// 	if(str1.equals(str2))
	// 		return 1;
		
	// 	String type1String ="", type2String ="";

	// 	if (str1.equals(Attribute.INTEGER) || str1.equals(Attribute.DOUBLE) || str1.equals(Attribute.STOCHINT) 
	// 		|| str1.equals(Attribute.STOCHDBL) || str1.equals(Attribute.SEED)) {
	// 		type1String = "NUMERIC";
	// 	} else if (str1.equals(Attribute.STRING) || str1.equals(Attribute.STOCHSTR)) {
	// 		type1String = "STRING";
	// 	} else if (str1.equals(Attribute.DATE) || str1.equals(Attribute.STOCHDAT)) {
	// 		type1String = "DATE";
	// 	}
		
	// 	String str2 = type_2.getTypeName();
	// 	if (str2.equals(Attribute.INTEGER) || str2.equals(Attribute.DOUBLE) || str2.equals(Attribute.STOCHINT) 
	// 		|| str2.equals(Attribute.STOCHDBL) || str2.equals(Attribute.SEED)) {
	// 		type2String = "NUMERIC";
	// 	} else if (str2.equals(Attribute.STRING) || str2.equals(Attribute.STOCHSTR)) {
	// 		type2String = "STRING";
	// 	} else if (str2.equals(Attribute.DATE) || str2.equals(Attribute.STOCHDAT)) {
	// 		type2String = "DATE";
	// 	}
		
	// 	if(type1String.equals(type2String))
	// 	{
	// 		return 1;
	// 	}
		
	// 	if((type1String.equals("NUMERIC") && type2String.equals("DATE")) ||
	// 		(type1String.equals("DATE")    && type2String.equals("NUMERIC")))
	// 	{
	// 		return 1;
	// 	}
		
	// 	if((type1String.equals("STRING") && type2String.equals("DATE")) ||
	// 		(type1String.equals("DATE")    && type2String.equals("STRING")))
	// 	{
	// 		return 1;
	// 	}
		
	// 	return -1;
	// }

	/*
	 *  public static final int BASELINE_RANDOM_TABLE_TYPECHECKER = 0;
	 *	public static final int GENERAL_RANDOM_TABLE_TYPECHECKER = 1;
	 *	public static final int RANDOM_TABLE_TYPECHECKER = 2;
	 *	public static final int VIEW_TYPECHECKER = 3;
	 *	public static final int TABLE_DEFINE_CHECKER = 4;
	 *	public static final int VGFUNC_DEFINE_CHECKER = 5;
	 *	public static final int TYPECHECKER = 6;
	 */
	public static int getCheckerType(TypeChecker typeChecker)
	{
		if(typeChecker instanceof BaseLineRandomTableTypeChecker)
		{
			return BASELINE_RANDOM_TABLE_TYPECHECKER;
		}
		else if(typeChecker instanceof MultidimensionalTableTypeChecker)
		{
			return MULTIDIMENSIONAL_VIEW_TYPECHECKER;
		}
		else if(typeChecker instanceof GeneralRandomTableTypeChecker)
		{
			return GENERAL_RANDOM_TABLE_TYPECHECKER;
		}
		else if(typeChecker instanceof RandomTableTypeChecker)
		{
			return RANDOM_TABLE_TYPECHECKER;
		}
		else if(typeChecker instanceof ViewTypeChecker)
		{
			return VIEW_TYPECHECKER;
		}
		else if(typeChecker instanceof TableDefineChecker)
		{
			return TABLE_DEFINE_CHECKER;
		}
		else if(typeChecker instanceof VGFunctionDefineChecker)
		{
			return VGFUNC_DEFINE_CHECKER;
		}
		else if(typeChecker instanceof BaselineArrayRandomTypeChecker)
		{
			return BASELINE_ARRAY_RANDOM_TABLE_TYPECHECKER;
		}
		else if(typeChecker instanceof UnionViewStatementTypeChecker)
		{
			return UNION_VIEW_TYPECHECKER;
		}
		else 
		{
			return TYPECHECKER;
		}
	}
}
