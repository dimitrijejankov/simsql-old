

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

import static simsql.compiler.MultidimensionalSchemaIndices.labelingOrder;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;

/*
 * @Author: Bamboo
 *  Date: 09/03/2010
 *  
 */

import java.util.HashMap;

public class TableReference extends SQLExpression{
	public static final int COMMON_TABLE = 0;
	public static final int CONSTANT_INDEX_TABLE = 1;
	public static final int GENERAL_INDEX_TABLE = 2;
	public static final int MULTIDIMENSIONAL_CONSTANT_INDEX_TABLE = 3;
	public static final int MULTIDIMENSIONAL_GENERAL_INDEX_TABLE = 4;

	public String table;
	public String alias;
	
	/*
	 * --------------------For simulation-----------------------
	 */

	public HashMap<String, String> indexStrings;
	private int type;
	public HashMap<String, MathExpression> expressions;
	/*
	 * ----------------------------end--------------------------
	 */
	
	public TableReference(String table)
	{
		this(table, table);
		type = COMMON_TABLE;
	}
	
	public TableReference(String table, String alias)
	{
		this(table, alias, COMMON_TABLE);
	}
	
	public TableReference(String table, String alias, int type)
	{
		this.table = table;
		this.alias = alias;
		this.type = type;
		this.expressions = new HashMap<String, MathExpression>();
		this.indexStrings = new HashMap<String, String>();
	}

    public TableReference(String table, String alias, String indexString, int type)
    {
        this.table = table;
        this.alias = alias;
        this.type = type;
        this.expressions = new HashMap<String, MathExpression>();
        this.indexStrings.put("i", indexString);
    }

    public TableReference(String table,
                          String alias,
                          String indexString,
                          int type,
                          MathExpression expression)
    {
        this.table = table;
        this.alias = alias;
        this.type = type;
        this.indexStrings.put("i", indexString);
        this.expressions.put("i", expression);
    }
	
	public boolean isConstantRandomTable()
	{
		return (type == CONSTANT_INDEX_TABLE);
	}
	
	public boolean isGeneralIndexTable()
	{
		return  (type == GENERAL_INDEX_TABLE);
	}

	public String getTable() {
		return table;
	}

	public void setTable(String table) {
		this.table = table;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public int getTableInferenceType()
	{
		return type;
	}

	public MathExpression getExpression() {
		return expressions.get("i");
	}

	public void setExpression(MathExpression expression) {
		expressions.put("i", expression);
	}

	/* (non-Javadoc)
	 * @see component.expression.Expression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitTableReferenceExpression(this);
	}
	
	@Override
	public String toString()
	{
		return table +" as " + alias;
	}

    public String getIndexString() {
        return indexStrings.get("i");
    }

    public void setIndexString(String indexString) {
        this.indexStrings.put("i", indexString);
    }
}
