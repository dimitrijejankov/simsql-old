

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.statisticsOperator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

import simsql.compiler.math_operators.*;
import simsql.runtime.DataType;
import simsql.runtime.VersType;
import simsql.runtime.IntType;
import simsql.runtime.StringType;
import simsql.runtime.DoubleType;
import simsql.runtime.SeedType;
import simsql.runtime.MatrixType;
import simsql.runtime.VectorType;
import simsql.runtime.ScalarType;



// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.VGFunction;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.*;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.util.FinalVariable;


/**
 * @author Bamboo
 *
 */
public class TypeInference {
	private HashMap<String, DataType> attributeTypeMap;
	private MathOperator tempOperator;
	private Catalog catalog;
	
	
	public TypeInference(HashMap<String, DataType> attributeTypeMap,
			MathOperator tempOperator) {
		super();
		this.attributeTypeMap = attributeTypeMap;
		this.tempOperator = tempOperator;
		catalog = SimsqlCompiler.catalog;
	}
	
	public DataType getType() throws Exception
	{
		return getType(tempOperator);
	}
	
	public DataType getType(MathOperator mathOperator) throws Exception
	{
		if(mathOperator instanceof AggregateOperator)
		{
			return getTypeInAggregateOperator((AggregateOperator)mathOperator);
		}
		else if(mathOperator instanceof ArithmeticOperator)
		{
			return getTypeInArithmeticOperator((ArithmeticOperator)mathOperator);
		}
		else if(mathOperator instanceof ColumnOperator)
		{
			return getTypeInColumnOperator((ColumnOperator)mathOperator);
		}
		else if(mathOperator instanceof DateOperator)
		{
			return getTypeInDateOperator((DateOperator)mathOperator);
		}
		else if(mathOperator instanceof EFunction)
		{
			return getTypeInEFunction((EFunction)mathOperator);
		}
		else if(mathOperator instanceof FunctionOperator)
		{
			return getTypeInFunctionOperator((FunctionOperator)mathOperator);
		}
		else if(mathOperator instanceof PredicateToMathWrapper)
		{
			return new VersType();				// TO-DO
		}
		else if(mathOperator instanceof NumberOperator)
		{
			return getTypeInNumberOperator((NumberOperator)mathOperator);
		}
		else if(mathOperator instanceof SetOperator)
		{
			return getTypeInSetOperator((SetOperator)mathOperator);
		}
		else if(mathOperator instanceof StarOperator)
		{
			return getTypeInStarOperator((StarOperator)mathOperator);
		}
		else if(mathOperator instanceof StringOperator)
		{
			return getTypeInStringOperator((StringOperator)mathOperator);
		}
		else
		{
			return new IntType();
		}
	}
	
	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInStringOperator(StringOperator mathOperator) {
		return new StringType();
	}

	/**
	 * @param mathOperator--------------------------*********************------------------------------
	 * @return
	 */
	private DataType getTypeInStarOperator(StarOperator mathOperator) {
		return new DoubleType();
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInSetOperator(SetOperator mathOperator)  throws Exception{
		ArrayList<MathOperator> elementList = mathOperator.getElementList();
		DataType[] elementTypes = new DataType[elementList.size()];
		
		for(int i = 0; i < elementList.size(); i++)
		{
			elementTypes[i] = getType(elementList.get(i));
		}
		
		return getType(elementTypes);
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInNumberOperator(NumberOperator mathOperator) {
		return mathOperator.getType();
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInFunctionOperator(FunctionOperator mathOperator) throws Exception {
		String functionName = mathOperator.getFunctionName();
		Function func = catalog.getFunction(functionName);
		ArrayList<MathOperator>  parameterList = mathOperator.getParameterList();
		Attribute attribute = func.getOutputAtt();
		
		DataType[] elementTypes = new DataType[parameterList.size()];
		
		for(int i = 0; i < parameterList.size(); i++)
		{
			elementTypes[i] = getType(parameterList.get(i));
		}
		
		//DataType parameterType = getType(elementTypes);
		
		DataType outType;
		
		if(functionName.equals("case"))
		{
			outType = elementTypes[elementTypes.length-1];
		}
		else
		{
			outType = attribute.getType();
		}
		
		
		if(randomType(elementTypes))
		{
			outType.setStoch(true);				// TO-DO
			return outType;
			// if(outType == Attribute.SEED)
			// {
			// 	return Attribute.SEED;
			// }
			// else if(outType == Attribute.STOCHDAT)
			// {
			// 	return Attribute.STOCHDAT;
			// }
			// else if(outType == Attribute.STOCHDBL)
			// {
			// 	return Attribute.STOCHDBL;
			// }
			// else if(outType == Attribute.STOCHINT)
			// {
			// 	return Attribute.STOCHINT;
			// }
			// else if(outType == Attribute.DATE)
			// {
			// 	return Attribute.STOCHDAT;
			// }
			// else if(outType == Attribute.STRING)
			// {
			// 	return Attribute.STRING;
			// }
			// else if(outType == Attribute.DOUBLE)
			// {
			// 	return Attribute.STOCHDBL;
			// }
			// else if(outType == Attribute.INTEGER)
			// {
			// 	return Attribute.STOCHINT;
			// }
			// else
			// 	return Attribute.HYBRID;
		}
		else
		{
			return outType;
		}
	}

	/**
	 * @param mathOperator--------------------------*********************------------------------------
	 * @return
	 */
	private DataType getTypeInEFunction(EFunction mathOperator) {
		return new VersType();
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInDateOperator(DateOperator mathOperator) {
		return new StringType();
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInColumnOperator(ColumnOperator mathOperator) {
		return attributeTypeMap.get(mathOperator.getColumnName());
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInArithmeticOperator(ArithmeticOperator mathOperator)  throws Exception{
		MathOperator left = mathOperator.getLeft();
		MathOperator right = mathOperator.getRight();
		
		DataType leftType = getType(left);
		DataType rightType = getType(right);
		
		DataType[] typeArray = new DataType[2];
		typeArray[0] = leftType;
		typeArray[1] = rightType;
		
		if(typeArray[0].getTypeName().equals("integer") && typeArray[1].getTypeName().equals("integer"))
		{
			return new DoubleType();
		}
		else
		{
			return getType(typeArray);
		}
	}

	/**
	 * @param mathOperator
	 * @return
	 */
	private DataType getTypeInAggregateOperator(AggregateOperator mathOperator)  throws Exception{
		/*
		 * The "type" is defined in Class expression.util.FinalVariable
		 *  public static final int AVG = 0;
		 *	public static final int SUM = 1;
		 *	public static final int COUNT = 2;
		 *	public static final int COUNTALL = 3;
		 *	public static final int MIN = 4;
		 *	public static final int MAX = 5;
		 *	public static final int VARIANCE = 6;
		 */
		int type = mathOperator.getType();
		
		/*
		 *  The "operator" can be:
		 *  	Expression
		 *  	Identifier (Column)
		 */
		MathOperator childOperator = mathOperator.getChildOperator();
		DataType childType = getType(childOperator);
		switch(type)
		{
			case FinalVariable.AVG:
			case FinalVariable.SUM:
			case FinalVariable.MIN:
			case FinalVariable.MAX:
			case FinalVariable.VARIANCE:
			case FinalVariable.STDEV:
				return childType;
				
			case FinalVariable.COUNT:
			case FinalVariable.COUNTALL:
				if(childType.ifStoch())
				{
					DataType outType = new IntType();				// TO-DO
					outType.setStoch(true);
					return outType;
				}
				else
				{
					return new IntType();	
				}

			case FinalVariable.VECTOR:
				if(childType.ifStoch())
				{
					DataType outType = new VectorType();				// TO-DO
					outType.setStoch(true);
					return outType;
				}
				else
				{
					return new VectorType();	
				}

			case FinalVariable.ROWMATRIX:
			case FinalVariable.COLMATRIX:
				if(childType.ifStoch())
				{
					DataType outType = new MatrixType();				// TO-DO
					outType.setStoch(true);
					return outType;
				}
				else
				{
					return new MatrixType();	
				}
		}
		
		return new DoubleType();
	}

	public HashMap<String, DataType> getAttributeTypeMap() {
		return attributeTypeMap;
	}
	
	
	public void setAttributeTypeMap(HashMap<String, DataType> attributeTypeMap) {
		this.attributeTypeMap = attributeTypeMap;
	}
	
	
	public MathOperator getTempOperator() {
		return tempOperator;
	}
	
	
	public void setTempOperator(MathOperator tempOperator) {
		this.tempOperator = tempOperator;
	}
	
	// Judge if we should return a random type, based on input types
	public boolean randomType(DataType[] types) {
		for(DataType t: types) {
			if (t.ifStoch())
				return true;
		}
		return false;
	}
	
	public DataType getType(DataType[] types)
	{
		HashSet<String> typeNum = new HashSet<String>();
		
		for(int i = 0; i < types.length; i++)
		{
			typeNum.add(types[i].getTypeName());
		}
		
		if(typeNum.contains("seed"))
		{
			return new SeedType();
		}
		else if(typeNum.contains("matrix random"))				// TO-DO
		{
			DataType outType = new MatrixType();
			outType.setStoch(true);
			return outType;
		}
		else if(typeNum.contains("vector random"))
		{
			DataType outType = new VectorType();
			outType.setStoch(true);
			return outType;
		}
		else if(typeNum.contains("string random"))
		{
			DataType outType = new StringType();
			outType.setStoch(true);
			return outType;
		}
		else if(typeNum.contains("scalar random"))
		{
			DataType outType = new ScalarType();
			outType.setStoch(true);
			return outType;
		}
		else if(typeNum.contains("double random"))
		{
			DataType outType = new DoubleType();
			outType.setStoch(true);
			return outType;
		}
		else if(typeNum.contains("integer random"))
		{
			if(typeNum.contains("scalar"))
			{
				DataType outType = new ScalarType();
				outType.setStoch(true);
				return outType;
			}
			else if(typeNum.contains("double"))
			{
				DataType outType = new DoubleType();
				outType.setStoch(true);
				return outType;
			}
			else 
			{
				DataType outType = new IntType();
				outType.setStoch(true);
				return outType;
			}
		}
		else if(typeNum.contains("matrix"))
		{
			return new MatrixType();
		}
		else if(typeNum.contains("vector"))
		{
			return new VectorType();
		}
		else if(typeNum.contains("string"))
		{
			return new StringType();
		}
		else if(typeNum.contains("scalar"))
		{
			return new ScalarType();
		}
		else if(typeNum.contains("double"))
		{
			return new DoubleType();
		}
		else if(typeNum.contains("integer"))
		{
			return new IntType();
		}
		else
			return new VersType();

		// int typeNum[] = new int[Attribute.TOTALTYPE];
		// for(int i = 0; i < typeNum.length; i++)
		// {
		// 	typeNum[i] = 0;
		// }
		
		// for(int i = 0; i < types.length; i++)
		// {
		// 	typeNum[types[i]] ++;
		// }

		// if(typeNum[Attribute.SEED] != 0)
		// {
		// 	return Attribute.SEED;
		// }
		// else if(typeNum[Attribute.STOCHDAT] != 0)
		// {
		// 	return Attribute.STOCHDAT;
		// }
		// else if(typeNum[Attribute.STOCHDBL] != 0)
		// {
		// 	return Attribute.STOCHDBL;
		// }
		// else if(typeNum[Attribute.STOCHINT] != 0)
		// {
		// 	if(typeNum[Attribute.DOUBLE] != 0)
		// 	{
		// 		return Attribute.STOCHDBL;
		// 	}
		// 	else
		// 	{
		// 		return Attribute.STOCHINT;
		// 	}
		// }
		// else if(typeNum[Attribute.DATE] != 0)
		// {
		// 	return Attribute.DATE;
		// }
		// else if(typeNum[Attribute.STRING] != 0)
		// {
		// 	return Attribute.STRING;
		// }
		// else if(typeNum[Attribute.DOUBLE] != 0)
		// {
		// 	return Attribute.DOUBLE;
		// }
		// else if(typeNum[Attribute.INTEGER] != 0)
		// {
		// 	return Attribute.INTEGER;
		// }
		// else
		// 	return Attribute.HYBRID;
	}
	
	/****
	public boolean isRandomType(String type)
	{
		if(type.equals(Attribute.STOCHDAT) ||
				type.equals(Attribute.STOCHDBL) ||
				type.equals(Attribute.STOCHINT) ||
				type.equals(Attribute.STOCHSTR))
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	****/
	
}
