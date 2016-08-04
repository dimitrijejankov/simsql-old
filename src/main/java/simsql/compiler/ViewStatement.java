

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
package simsql.compiler; // package mcdb.compiler.parser.expression.sqlType;



// import mcdb.compiler.parser.astVisitor.ViewTypeChecker;
// import mcdb.compiler.parser.expression.Expression;
// import mcdb.compiler.parser.expression.sqlExpression.DefinedTableSchema;
// import mcdb.compiler.parser.expression.sqlExpression.SelectStatement;


/**
 * @author Bamboo
 *
 */
public class ViewStatement extends Expression{

	public DefinedTableSchema definedTableSchema;
	public SelectStatement statement;
	private String sqlString;
	
	public ViewStatement(DefinedTableSchema definedTableSchema, SelectStatement statement) {
		super();
		this.definedTableSchema = definedTableSchema;
		this.statement = statement;
	}

	public String getSqlString() {
		return sqlString;
	}

	public void setSqlString(String sqlString) {
		this.sqlString = sqlString;
	}

	public boolean acceptVisitor(ViewTypeChecker astVisitor)throws Exception {
		return astVisitor.visitViewStatement(this);
	}
}
