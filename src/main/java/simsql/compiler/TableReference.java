

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
import static simsql.compiler.MultidimensionalTableSchema.getQualifiedTableNameFromIndices;
import static simsql.compiler.MultidimensionalTableSchema.getGeneralIndexTableNameFromExpressions;


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

	private String table;
	private String alias;
	
	/*
	 * --------------------For simulation-----------------------
	 */

	public HashMap<String, Integer> indexStrings;
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
		this.setTable(table);
		this.setAlias(alias);
		this.type = type;
		this.expressions = new HashMap<String, MathExpression>();
		this.indexStrings = new HashMap<String, Integer>();
	}

    public TableReference(String table, String alias, String indexString, int type)
    {
        this(table, alias, type);
        this.expressions = new HashMap<String, MathExpression>();

        if(indexString != null) {
            this.indexStrings.put("i", Integer.parseInt(indexString));
        }
    }

    public TableReference(String table, String alias, HashMap<String, Integer> indices, int type)
    {
        this(table, alias, type);
        this.expressions = new HashMap<String, MathExpression>();
        this.indexStrings = indices;
    }

    public TableReference(String table,
                          String alias,
                          String indexString,
                          int type,
                          MathExpression expression)
    {
        this(table, alias, type);

        if(indexString != null) {
            this.indexStrings.put("i", Integer.parseInt(indexString));
        }
        this.expressions.put("i", expression);
    }

    public boolean isMultidimensionalConstantTable() {
        return (type == MULTIDIMENSIONAL_CONSTANT_INDEX_TABLE);
    }

    public boolean isMultidimensionalGeneralIndexTable() {
        return (type == MULTIDIMENSIONAL_GENERAL_INDEX_TABLE);
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

	    if(type == MULTIDIMENSIONAL_CONSTANT_INDEX_TABLE || type == CONSTANT_INDEX_TABLE) {
	        return getQualifiedTableNameFromIndices(table, indexStrings);
        }

        if(type == MULTIDIMENSIONAL_GENERAL_INDEX_TABLE || type == GENERAL_INDEX_TABLE) {
            return getGeneralIndexTableNameFromExpressions(table, expressions);
        }

		return table;
	}

	public String getAlias() {
		return alias == null ? table : alias;
	}

    public HashMap<String, Integer> getIndexStrings() {
        return indexStrings;
    }

    public HashMap<String, MathExpression> getExpressions() {
        return expressions;
    }

    public int getTableInferenceType()
    {
        return type;
    }

    public MathExpression getExpression(String index) {
        return expressions.get(index);
    }

    public Integer getIndexString(String index) {
        return indexStrings.get(index);
    }
    public void setTable(String table) {
        this.table = table;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public void setExpression(String index, MathExpression expression) {
        expressions.put(index, expression);
    }

    public void addExpression(MathExpression expression) {
        String index = labelingOrder[expressions.size()];
        setExpression(index, expression);
    }

    public void setIndexString(String index, String indexString) {
        this.indexStrings.put(index, Integer.parseInt(indexString));
    }

    public void addIndexString(String indexString) {
        String index = labelingOrder[indexStrings.size()];
        setIndexString(index, indexString);
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
		return getTable() +" as " + getAlias();
	}

}
