

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



// import mcdb.compiler.parser.astVisitor.ASTVisitor;

/**
 * @author Bamboo
 *
 */
public class ForeignKeyElement extends SQLExpression 
{

	public String attribute;
	public String referencedRelation;
	public String referencedAttribute;
	
	
	public ForeignKeyElement(String attribute, 
					String referencedRelation,
					String referencedAttribute)
	{
		super();
		this.attribute = attribute.toLowerCase();
		this.referencedRelation = referencedRelation.toLowerCase();
		this.referencedAttribute = referencedAttribute.toLowerCase();
	}


	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.expression.sqlExpression.SQLExpression#acceptVisitor(mcdb.compiler.parser.astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception
	{
		return astVisitor.visitForeignKey(this);
	}

}
