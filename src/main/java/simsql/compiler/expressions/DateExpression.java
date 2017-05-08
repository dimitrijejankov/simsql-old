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


package simsql.compiler.expressions;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;


/**
 * Represents the date expression in the AST
 */
public class DateExpression extends MathExpression {

	/**
	 * The date value in string format.
	 */
	@JsonProperty("value")
	public String value;

	@JsonCreator
	public DateExpression(@JsonProperty("value") String dataString)
	{
		int start = dataString.indexOf("'");
		this.value = dataString.substring(start+1, dataString.length() - 1);
	}

    /**
     * @param astVisitor Type checker
     * @return the type of the date expression (string type)
     * @throws Exception if the expression is not valid throws an exception
     */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitDateExpression(this);	
	}

    /**
     * Returns the string representation of the date
     * @return date as string...
     */
	public String toString()
	{
		return value;
	}
}
