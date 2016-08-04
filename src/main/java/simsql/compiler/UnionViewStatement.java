

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


package simsql.compiler;

import java.util.ArrayList;


public abstract class UnionViewStatement extends Expression{
	private String sqlString;
	private DefinedTableSchema schema;
	private ArrayList<SQLExpression> tableNameList;
	
	
	
	public UnionViewStatement() {
		super();
	}


	public UnionViewStatement(DefinedTableSchema schema,
			ArrayList<SQLExpression> tableNameList) {
		super();
		this.schema = schema;
		this.tableNameList = tableNameList;
	}

	public String getSqlString() {
		return sqlString;
	}


	public void setSqlString(String sqlString) {
		this.sqlString = sqlString;
	}

	public DefinedTableSchema getSchema() {
		return schema;
	}
	public void setSchema(DefinedTableSchema schema) {
		this.schema = schema;
	}
	public ArrayList<SQLExpression> getTableNameList() {
		return tableNameList;
	}
	public void setTableNameList(ArrayList<SQLExpression> tableNameList) {
		this.tableNameList = tableNameList;
	}
	

	public abstract boolean acceptVisitor(UnionViewStatementTypeChecker astVisitor)
			throws Exception;
}
