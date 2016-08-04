

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
package simsql.compiler; // package mcdb.compiler.parser.unnester;

import java.util.ArrayList;
import java.util.HashMap;



// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;



/**
 * @author Bamboo
 *
 */
public class CheckerEdge {
	private ArrayList<SQLExpression> tableReferenceList;
	private HashMap<SQLExpression, ArrayList<ColumnExpression>> columnReferenceMap;
	
	public CheckerEdge()
	{
		tableReferenceList = new ArrayList<SQLExpression>();
		columnReferenceMap = new HashMap<SQLExpression, ArrayList<ColumnExpression>>();
	}
	
	public CheckerEdge(ArrayList<SQLExpression> tableReferenceList,
					   HashMap<SQLExpression, ArrayList<ColumnExpression>> columnReferenceMap)
	{
		this.tableReferenceList = tableReferenceList;
		this.columnReferenceMap = columnReferenceMap;
	}
	
	public boolean containTableReference(SQLExpression expression)
	{
		return tableReferenceList.contains(expression);
	}
	
	public void addTableReference(SQLExpression expression)
	{
		/*
		 * for testing
		 */
		if(expression == null)
		{
			//System.out.println("testing");
		}
		if(!tableReferenceList.contains(expression))
		{
			tableReferenceList.add(expression);
		}
	}
	
	public void addColumnExpression(SQLExpression expression, ColumnExpression column)
	{
		if(columnReferenceMap.containsKey(expression))
		{
			ArrayList<ColumnExpression> tempList = columnReferenceMap.get(expression);
			if(!tempList.contains(column))
			{
				tempList.add(column);
			}
		}
		else
		{
			ArrayList<ColumnExpression> tempList = new ArrayList<ColumnExpression>();
			tempList.add(column);
			columnReferenceMap.put(expression, tempList);
		}
	}

	public ArrayList<SQLExpression> getTableReferenceList() {
		return tableReferenceList;
	}

	public void setTableReferenceList(ArrayList<SQLExpression> tableReferenceList) {
		this.tableReferenceList = tableReferenceList;
	}

	public HashMap<SQLExpression, ArrayList<ColumnExpression>> getColumnReferenceMap() {
		return columnReferenceMap;
	}

	public void setColumnReferenceMap(
			HashMap<SQLExpression, ArrayList<ColumnExpression>> columnReferenceMap) {
		this.columnReferenceMap = columnReferenceMap;
	}
	
	
}
