

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
package simsql.compiler; // package mcdb.compiler.parser.expression.sqlExpression;

import simsql.runtime.TypeMachine;
import simsql.runtime.DataType;


/**
 * @author Bamboo
 *
 */
public class AttributeDefineElement extends SQLExpression
{
	public String attributeName;
	public String typeString;
	public boolean input;

	public AttributeDefineElement(String attributeName, String typeString) {
		super();
		this.attributeName = attributeName.toLowerCase();
		this.typeString = typeString.toLowerCase();
		this.input = true;
	}

	public String getAttributeName() {
		return attributeName;
	}

	public DataType getType() throws Exception
	{
		typeString = typeString.toLowerCase();
		return TypeMachine.fromString(typeString);				// TO-DO
		// if(typeString.equals("integer"))
		// {
		// 	return Attribute.INTEGER;
		// }
		// else if(typeString.equals("double"))
		// {
		// 	return Attribute.DOUBLE;
		// }
		// else if(typeString.equals("char"))
		// {
		// 	return Attribute.STRING;
		// }
		// else
		// {
		// 	System.err.println("Attribute[" + attributeName +"] has wrong type size!");
		// 	throw new Exception("Attribute[" + attributeName +"] has wrong type size!");
		// }
	}
	
	public boolean isInput() {
		return input;
	}

	public void setInput(boolean input) {
		this.input = input;
	}



	public String getTypeString() {
		return typeString;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.expression.sqlExpression.SQLExpression#acceptVisitor(mcdb.compiler.parser.astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception
	{
		return astVisitor.visitAttributeElement(this);	
	}

}
