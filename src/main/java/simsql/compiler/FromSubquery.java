

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
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;


/**
 * @author Bamboo
 *
 */
public class FromSubquery extends SQLExpression{
	public MathExpression expression;
	public String alias;
	
	public FromSubquery(MathExpression table, String alias)
	{
		this.expression = table;
		this.alias = alias;
	}

	public FromSubquery(MathExpression table)
	{
		this.expression = table;
		this.alias = null;
	}
	
	
	public MathExpression getSql() {
		return expression;
	}

	public void setSql(MathExpression sql) {
		this.expression = sql;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	/* (non-Javadoc)
	 * @see component.expression.Expression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitFromSubqueryExpression(this);
	}
}
