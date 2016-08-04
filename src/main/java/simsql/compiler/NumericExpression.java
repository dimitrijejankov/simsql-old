

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


package simsql.compiler; // package mcdb.compiler.parser.expression.mathExpression;


import java.util.ArrayList;
import simsql.runtime.DataType;
import simsql.runtime.DoubleType;
import simsql.runtime.IntType;



// import mcdb.catalog.Attribute;
// import mcdb.compiler.parser.astVisitor.ASTVisitor;


/*
 * @Author: Bamboo
 *  Date: 09/02/2010
 *  This class corresponds to numeric expression
 */

public class NumericExpression extends MathExpression{
	public double value;
	public DataType type;
	
	public NumericExpression(String valueString)
	{
		value = new Double(valueString).doubleValue();
		if(valueString.indexOf(".") >= 0)
		{
			type = new DoubleType();
		}
		else
		{
			type = new IntType();
		}
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitNumericExpression(this);	
	}
	
	public DataType getType()
	{
		return type;
	}
	
	@Override
	public String toString()
	{
		if(type instanceof DoubleType)
			return ""+value;
		else
			return "" + (int)value;
	}
}
