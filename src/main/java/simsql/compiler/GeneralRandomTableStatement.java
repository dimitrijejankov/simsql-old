

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

import java.util.ArrayList;



// import mcdb.compiler.parser.astVisitor.GeneralRandomTableTypeChecker;
// import mcdb.compiler.parser.expression.sqlExpression.DefinedTableSchema;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlExpression.SelectStatement;
// import mcdb.compiler.parser.expression.sqlExpression.WithStatement;
	
/**
 * @author Bamboo
 *
 */
public class GeneralRandomTableStatement extends RandomTableStatement {

	/**
	 * @param tableAttributes
	 * @param outerTable
	 * @param withList
	 * @param selectStatement
	 */
	public GeneralRandomTableStatement(DefinedTableSchema tableAttributes,
			SQLExpression outerTable, ArrayList<WithStatement> withList,
			SelectStatement selectStatement) {
		super(tableAttributes, outerTable, withList, selectStatement);
	}
	
	public boolean acceptVisitor(GeneralRandomTableTypeChecker astVisitor)throws Exception
	{
		return astVisitor.visitGeneralRandomTableStatement(this);
	}
}
