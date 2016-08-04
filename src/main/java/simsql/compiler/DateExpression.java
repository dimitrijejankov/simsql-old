

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
package simsql.compiler; // package mcdb.compiler.parser.expression.mathExpression;

import java.util.ArrayList;
import simsql.runtime.DataType;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;


/**
 * @author Bamboo
 *
 */
public class DateExpression extends MathExpression{
	public String value;
	
	//get the data substring from the dataString
	public DateExpression(String dataString)
	{
		int start = dataString.indexOf("'");
		this.value = dataString.substring(start+1, dataString.length() - 1);
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitDateExpression(this);	
	}
	
	public String toString()
	{
		return value;
	}
}
