

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
import java.util.HashMap;

import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;


//Middle expression
public class MPNGenerator {
	private MathExpression mathExpression;
	String tokenForLeftBraket, tokenForRightBraket;
	String tokenForComma;
	
	public MPNGenerator(MathExpression exp)
	{
		this.mathExpression = exp;
		this.tokenForLeftBraket = "(";
		this.tokenForRightBraket = ")";
		this.tokenForComma = ", ";
	}
	
	public MPNGenerator()
	{
		
	}
	
	public String convertToMPN()
	{
		if(mathExpression == null)
		{
			return null;
		}
		else
		{
			String result = convert(mathExpression);
			
			if(result.startsWith(tokenForLeftBraket))
			{
				return result.substring(tokenForLeftBraket.length(), result.length() - tokenForRightBraket.length());
			}
			else
			{
				return result;
			}
		}	
	}
	
	/* SpecialToken to connect tokens: 
	 * "(", ")" => <token1> = _mcdb1_
	 * "function()" => <token2> = _mcdb2_
	 * "," => <token3> = _mcdb3
	 * notation: (a+b)*c-(a+f(b+c,d,g))/e =>
	 * 
	 */
	
	private String convert(MathExpression exp)
	{
		if(exp instanceof ArithmeticExpression)
		{
			return tokenForLeftBraket + convertArithmeticExpression((ArithmeticExpression)exp)
					+ tokenForRightBraket;
		}
		else if(exp instanceof GeneralFunctionExpression)
		{
			return convertFunction((GeneralFunctionExpression)exp);
		}
		else if(exp instanceof NumericExpression)
		{
			return ((NumericExpression)exp).toString();
		}
		else if(exp instanceof GeneralTableIndex)
		{
			return exp.toString();
		}
		else
		{
			throw new RuntimeException("could not convert the general table index expression");
		}
	}
	
	private String convertArithmeticExpression(ArithmeticExpression exp)
	{
		String result = "";
		ArrayList<Integer> operatorList = exp.operatorList;
		ArrayList<MathExpression> operandList = exp.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			result += convert(expression);
			
			if(i != operandList.size()-1)
			{
				int operator = operatorList.get(i);
				switch(operator)
				{
					case FinalVariable.PLUS:
						result += "+";
						break;
						
					case FinalVariable.MINUS:
						result += "-";
						break;
						
					case FinalVariable.TIMES:
						result += "*";
						break;
						
					case FinalVariable.DIVIDE:
						result += "/";
						break;

					case FinalVariable.MOD:
						result += "%";
						break;
						
					default:
						throw new RuntimeException("wrong operator type in MPNGenerator");
				}
			}
		}
		return result;
	}
	
	private String convertFunction(GeneralFunctionExpression exp)
	{
		String result = exp.functionName;
		ArrayList<MathExpression> paraList = exp.parasList;
		result += tokenForLeftBraket;
		
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			result += convert(expression);
			
			if(i != paraList.size()-1)
			{
				result += tokenForComma;
			}
		}
		
		result += tokenForRightBraket;
		return result;
	}

	public int initializeTime(HashMap<String, Integer> indices) {
		return (int)initializeTime(mathExpression, indices);
	}
	
	public double initializeTime(MathExpression exp, HashMap<String, Integer> indices) {
		if(exp instanceof ArithmeticExpression)
		{
			return initializeArithmeticExpressionTime((ArithmeticExpression)exp, indices);
		}
		else if(exp instanceof GeneralFunctionExpression)
		{
			return initializeFunctionTime((GeneralFunctionExpression)exp, indices);
		}
		else if(exp instanceof NumericExpression)
		{
			return ((NumericExpression)exp).value;
		}
		else if(exp instanceof GeneralTableIndex)
		{
			return indices.get(exp.toString());
		}
		else
		{
			throw new RuntimeException("could not convert the general table index expression");
		}
	}
	
	public double initializeArithmeticExpressionTime(ArithmeticExpression exp, HashMap<String, Integer> indices)
	{
		ArrayList<Integer> operatorList = exp.operatorList;
		ArrayList<MathExpression> operandList = exp.operandList;
		double value = initializeTime(operandList.get(0), indices);
				
		for(int i = 0; i < operatorList.size(); i++)
		{
			MathExpression expression = operandList.get(i+1);
			
			if(i != operandList.size()-1)
			{
				int operator = operatorList.get(i);
				switch(operator)
				{
					case FinalVariable.PLUS:
						value += initializeTime(expression, indices);
						break;
						
					case FinalVariable.MINUS:
						value -= initializeTime(expression, indices);
						break;
						
					case FinalVariable.TIMES:
						value *= initializeTime(expression, indices);
						break;
						
					case FinalVariable.DIVIDE:
						value /= initializeTime(expression, indices);
						break;

					case FinalVariable.MOD:
						value %= initializeTime(expression, indices);
						break;
						
					default:
						throw new RuntimeException("wrong operator type in MPNGenerator");
				}
			}
		}
		return value;
	}
	
	public double initializeFunctionTime(GeneralFunctionExpression exp, HashMap<String, Integer> indices)
	{
		String name = exp.functionName.toLowerCase();
		ArrayList<MathExpression> paraList = exp.parasList;
		ArrayList<Double> paraValueList = new ArrayList<Double>();
		
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			paraValueList.add(initializeTime(expression, indices));
		}
		
		/*
		 * currently I use the naive expression to implement that.
		 */
		if(name.equals("mod") && paraList.size() == 2)
		{
			return paraValueList.get(0) % paraValueList.get(1);
		}
		else
		{
			throw new RuntimeException("The current function does not support his one!");
		}
	}
	
	public double compute(String indexString, HashMap<String, Integer> indices)
	{
		
		//replace the tokenForComma
		ANTLRStringStream input = new ANTLRStringStream(indexString);
        QueryLexer lexer = new QueryLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryParser parser = new QueryParser(tokens);
        try
        {
        	MathExpression mathExpression = parser.valueExpression().expression;
        	return initializeTime(mathExpression, indices);
        }
        catch(Exception e)
        {
        	throw new RuntimeException("expcetion in compute MPN expression");
        }
	}
}
