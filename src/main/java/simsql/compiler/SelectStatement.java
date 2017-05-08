

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


package simsql.compiler; // package mcdb.compiler.parser.expression.sqlExpression;
/*
 * @Author: Bamboo
 *  Date: 09/02/2010
 *  This class corresponds to the select..from..where query
 */

import simsql.compiler.expressions.ColumnExpression;

import java.util.ArrayList;



// import mcdb.compiler.parser.astVisitor.ASTVisitor;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;





public class SelectStatement extends SQLExpression{
	
	//SetQuantifier: all or distinct
	public int setQuantifier; 	
	
	public ArrayList<SQLExpression> selectList;
	public ArrayList<SQLExpression> tableReferenceList;
	public BooleanPredicate whereClause;
	public ArrayList<ColumnExpression> groupByClauseList;
	public ArrayList<OrderByColumn> orderByClauseList;
	public BooleanPredicate havingClause;
	
	public SelectStatement(int quantifier, 
			ArrayList<SQLExpression> selectList,
			ArrayList<SQLExpression> tableReferenceList,
			BooleanPredicate whereClause,
			ArrayList<ColumnExpression> groupByClause,
			BooleanPredicate havingClause,
			ArrayList<OrderByColumn> orderByClauseList
		    )
	{
		this.setQuantifier = quantifier;
		this.selectList = selectList;
		this.tableReferenceList = tableReferenceList;
		this.whereClause = whereClause;
		this.groupByClauseList = groupByClause;
		this.havingClause = havingClause;
		this.orderByClauseList = orderByClauseList;
	}

	/* (non-Javadoc)
	 * @see component.expression.Expression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitSelectStatement(this);
	}
}
