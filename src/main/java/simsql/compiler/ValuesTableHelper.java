

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


package simsql.compiler; // package mcdb.compiler.parser.grammar;

import java.util.ArrayList;
import java.util.HashMap;

//import simsql.compiler.ValuesTable;
import simsql.runtime.DataType;
import simsql.runtime.Compatibility;
import simsql.runtime.DoubleType;
import simsql.runtime.StringType;

// import mcdb.catalog.Catalog;
// import mcdb.catalog.ForeignKey;
// import mcdb.catalog.Relation;
// import mcdb.catalog.Attribute;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.astVisitor.rule.TypeCheckerHelper;
// import mcdb.compiler.parser.expression.mathExpression.DateExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;
// import mcdb.compiler.parser.expression.mathExpression.NumericExpression;
// import mcdb.compiler.parser.expression.mathExpression.StringExpression;

/**
 * 
 */

/**
 * @author Bamboo
 *
 */
public class ValuesTableHelper {
	//<TableName, ValuesTable>
	private static HashMap<String, ValuesTable> tableMap = new HashMap<String, ValuesTable>();
	//<DefinitionContent, TableName>
	private static HashMap<String, String> contentTableMap = new HashMap<String, String>();
	
	public static int tableIndex = -1;
	
	public static String getValuesTableName()
	{
		tableIndex ++;
		String result = "mcdb_value_" + tableIndex + "_table";
		return result;
	}
	
	public static String getValuesTableName(String content)
	{
		if(contentTableMap.containsKey(content))
		{
			return contentTableMap.get(content);
		}
		else
		{
			tableIndex ++;
			String result = "mcdb_value" + tableIndex + "_table";
			contentTableMap.put(content, result);
			
			return result;
		}
	}
	
	public static void addValuesTable(String tableName, ValuesTable valuesTable)
	{
		/*1. Add Relation. */
		ArrayList<DataType> schemaTypeList = valuesTable.getSchemaTypeList();
		ArrayList<Integer> schemaSizeList = valuesTable.getSchemaTypeSize();
		ArrayList<Integer> schemaUniqueSizeList = valuesTable.getSchemaUniqueValueSize();
		
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>(schemaTypeList.size());
		
		for(int i = 0; i < schemaTypeList.size(); i++)
		{
			Attribute attribute = new Attribute("mcdb_" + tableName + "_attr_" + i, 
					 schemaTypeList.get(i),
					 tableName,
					 // schemaSizeList.get(i),					// TO-DO
					 schemaUniqueSizeList.get(i));
			attributeList.add(attribute);
		}
		
		int typleNum = valuesTable.getTempTableColumnList().size();
		Relation relation = new Relation(tableName, null, attributeList, typleNum, null, null, true);
		
		try
		{
			if(SimsqlCompiler.catalog.getRelation(tableName) == null)
			{
				SimsqlCompiler.catalog.addRelation(relation);
			}
		}
		catch(Exception e)
		{
			System.err.println("Could not add the value table!");
		}
		
		/*2. Put this relation to the tableMap*/
		tableMap.put(tableName, valuesTable);
	}
	
	public static boolean visitValuesTable(ValuesTable valuesTable) {
		ArrayList<ArrayList<MathExpression>> tempTableColumnList = valuesTable.getTempTableColumnList();
		
		ArrayList<ArrayList<DataType>> fieldType = new ArrayList<ArrayList<DataType>>();
		
		DataType type;
		//1. judge all the elements in the values definition.
		for(int i = 0; i < tempTableColumnList.size(); i++)
		{
			ArrayList<MathExpression> row = tempTableColumnList.get(i);
			ArrayList<DataType> rowType = new ArrayList<DataType>();
			
			for(int j = 0; j < row.size(); j++)
			{
				MathExpression fromElement = row.get(j);
				
				if(fromElement instanceof NumericExpression)
				{
					type = new DoubleType();
					rowType.add(type);
				}
				else if(fromElement instanceof StringExpression)
				{
					type = new StringType();
					rowType.add(type);
				}
			}
			fieldType.add(rowType);
		} 
		
		/*
		 * 2. Judge the column length of the definition and its type.
		 * The schema type is determined by the first row.
		 */
		if(fieldType.size() == 0)
		{
			System.err.println("Defintion in Values wrong!");
			return false;
		}
		
		int columnNum = fieldType.get(0).size();
		ArrayList<DataType> schemaType = fieldType.get(0);
		
		for(int i = 1; i < fieldType.size(); i++)
		{
			ArrayList<DataType> row = fieldType.get(i);
			if(row.size() != columnNum)
			{
				System.err.println("The column numbers in Values are not equalS!");
				return false;
			}
			
			for(int j = 0; j < row.size(); j++)
			{
				if(schemaType.get(j).parameterize(row.get(j), new HashMap <String, Integer>()) == Compatibility.BAD)		// TO-DO
				{
					System.err.println("The column types in Values do not match!");
					return false;
				}
			}
		} 
		
		return true;
	}
	
	public static void cleanValuesTable() throws Exception
	{
		SimsqlCompiler.catalog.cleanValuesTable();
	}
	
	public static HashMap<String, ValuesTable> getValuesTableMap()
	{
		return tableMap;
	}
}
