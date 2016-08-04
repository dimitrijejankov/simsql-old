

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
 *  This class corresponds to columns used by group by sub clause
 */


public class ColumnExpression extends MathExpression {
	//table name
	public String table;
	public String columnName;
	
	//whether this column expression is in gropuby subclause
	private boolean isGroupByColumn; 
	
	public ColumnExpression(String table, String columnName) {
		super();
		this.table = table;
		this.columnName = columnName;
		isGroupByColumn = false;
	}
	
	public boolean isGroupByColumn() {
		return isGroupByColumn;
	}

	public void setIsGroupByColumn(boolean isGroupByColumn) {
		this.isGroupByColumn = isGroupByColumn;
	}
	
	public boolean equals(ColumnExpression expression)
	{
		if(expression == null)
			return false;
		
		return table.equals(expression.table) && columnName.equals(expression.columnName);
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitColumnExpression(this);	
	}

	@Override
	public String toString()
	{
		if(table == null)
			return columnName;
		else
			return table + "_" + columnName;
	}
	
}
