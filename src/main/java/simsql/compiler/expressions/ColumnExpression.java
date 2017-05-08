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
 * Represents a column expression in the AST
 */
public class ColumnExpression extends MathExpression {

	/**
	 * The table this column belongs to
	 */
	@JsonProperty("table")
	public String table;

	/**
	 * The name of this column
	 */
	@JsonProperty("column-name")
	public String columnName;

	/**
	 * True if this is a group by column
	 */
	@JsonProperty("is-group-by-column")
	private boolean isGroupByColumn; 

	@JsonCreator
	public ColumnExpression(@JsonProperty("table") String table, @JsonProperty("column-name") String columnName) {
		super();
		this.table = table;
		this.columnName = columnName;
		isGroupByColumn = false;
	}

	/**
	 * Check if this is a group by column
	 * @return true if this is a group by column, false otherwise
	 */
	public boolean isGroupByColumn() {
		return isGroupByColumn;
	}

	/**
	 * Mark if this is a group by column
	 * @param isGroupByColumn true if it is, false otherwise
	 */
	public void setIsGroupByColumn(boolean isGroupByColumn) {
		this.isGroupByColumn = isGroupByColumn;
	}

    /**
     * Check if two column expressions match.
     * @param expression the the column expression to compare this column expression to
     * @return true if they do false otherwise
     */
    public boolean equals(ColumnExpression expression) {
		return expression != null && table.equals(expression.table) && columnName.equals(expression.columnName);
	}

    /**
     * @param astVisitor Type checker
     * @return the type of the column expression
     * @throws Exception if the expression is not valid throws an exception
     */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitColumnExpression(this);	
	}

    /**
     * gets the string representation of this column expression expression
     * @return "table_column" name string
     */
    @Override
	public String toString()
	{
		if(table == null)
			return columnName;
		else
			return table + "_" + columnName;
	}
	
}
