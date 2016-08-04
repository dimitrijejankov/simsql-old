

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



// import mcdb.compiler.parser.astVisitor.ASTVisitor;


public class AllFromTable extends SQLExpression{
	public String table;

	public AllFromTable(String table) {
		super();
		this.table = table;
	}
	
	public String getTableName()
	{
		return table;
	}

	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor)throws Exception {
		return astVisitor.visitAllFromTable(this);	
	}

	@Override
	public String toString()
	{
		return table+".*";
	}
}
