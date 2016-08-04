

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



// import mcdb.compiler.parser.astVisitor.ASTVisitor;


/*
 * @Author: Bamboo
 *  Date: 09/02/2010
 *  This class corresponds to numeric expression
 */

public class StringExpression extends MathExpression {
	public String value;
	
	//remove the quotation marks ''
	public StringExpression(String value)
	{
		this.value = value.substring(1, value.length()-1);
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitStringExpression(this);	
	}
	
	@Override
	public String toString()
	{
		return ""+value;
	}
}
