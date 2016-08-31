

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
package simsql.compiler; // package mcdb.compiler.parser.astVisitor;

import java.util.ArrayList;
import java.util.HashMap;
import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class RandomTableTypeChecker extends TypeChecker {

	public static final int SCHEMASTAGE = 6;
	public static final int OUTTABLESTAGE = 7;
	public static final int WITHSTAGE = 8;
	
	
	/*
	 * When we get the view definition from the catalog, we are not allowed to save it again!
	 * Otherwise, it would cause the error!
	 */
	public boolean toSave = true;
	
	/*
	 * outTableName corresponds to the alias of out_table
	 * vgCheckerMap means the vgFuncion and its corresponding subquery typechecker
	 */
	public String outTableName;
	
	public RandomTableTypeChecker(boolean allowDuplicatedAttributeAlias) throws Exception
	{
		super(allowDuplicatedAttributeAlias);
	}

	public boolean visitRandomTableStatement(
			RandomTableStatement randomTableStatement)throws Exception
	{
		
		//record this statements
		if(super.statement == null)
		{
			super.statement = randomTableStatement;
		}
		
		int errorNum = 0;
		boolean subcheck;
		
		DefinedTableSchema definedSchema = randomTableStatement.definedTableSchema;
		SQLExpression outerTable = randomTableStatement.outerTable;
		
		ArrayList<WithStatement> withList = randomTableStatement.withList;
		SelectStatement selectStatement = randomTableStatement.selectStatement;
		
		
		/*
		 * 1.1 check if the view name is overlapped.
		 */
		checkState = SCHEMASTAGE;
		
		boolean attributeSubcheck;
		
		if(toSave)
		{
			attributeSubcheck = definedSchema.acceptVisitor(this);
		}
		else
		{
			attributeSubcheck = true;
		}
		if(!attributeSubcheck)
			return false;
		
		/*
		 * 2.1 check if the table references overlapped. It includes the outer table which can be a 
		 *     subquery or a real table, and the some inner new created table by the VGFunction.
		 */
		
		checkState = OUTTABLESTAGE;
		if(outerTable instanceof TableReference)
		{
			outTableName = ((TableReference) outerTable).alias;
			/*
			 * Here if sentence can not be executed. I write here just for keeping the format
			 * of the method for typecheck the visitSelectStatement
			 */
			if(tableReferenceMap.containsKey(((TableReference) outerTable).alias))
			{
				System.err.println("Relation [" + ((TableReference) outerTable).alias +
						"] is ambigurous!");
				errorNum ++;
			}
			else
			{
				tableReferenceMap.put(((TableReference) outerTable).alias, ((TableReference) outerTable));
				fromList.add(((TableReference) outerTable).alias);
			}
		}
		else if(outerTable instanceof FromSubquery)
		{
			outTableName = ((FromSubquery) outerTable).alias;
			/*
			 * Here if sentence can not be executed. I write here just for keeping the format
			 * of the method for typecheck the visitSelectStatement
			 */
			if(fromSubqueryMap.containsKey(((FromSubquery) outerTable).alias))
			{
				System.err.println("Subquery [" + ((FromSubquery) outerTable).alias +
						"] is ambigurous!");
				errorNum ++;
			}
			else
			{
				fromSubqueryMap.put(((FromSubquery) outerTable).alias, ((FromSubquery) outerTable));
				fromList.add(((FromSubquery) outerTable).alias);
			}
		}	
		else
		{
			errorNum++;
		}

		//deal with the table from VGFunciton
		checkState = WITHSTAGE;
		for(int i = 0; i < withList.size(); i ++)
		{
			WithStatement statement = withList.get(i);
			String tempTableName  = statement.tempTableName;
			GeneralFunctionExpression vgFunction = statement.expression;
			if(vgFunctionMap.containsKey(tempTableName))
			{
				System.err.println("Ramdom table [" + tempTableName + "] is ambigurous!");
				errorNum ++;
			}
			else
			{
				vgFunctionMap.put(tempTableName, vgFunction);
			}
		}
		
		/*
		 * 2.2 sub check: each element should have a visit check: if it is in the real relation
		 */

		/*
		 * 2.2.1 sub check the outertable
		 */
		boolean outerTableSubcheck = outerTable.acceptVisitor(this);
		if(!outerTableSubcheck)
		{
			errorNum ++;
			return false;
		}
		
		
		if(outerTable instanceof FromSubquery)
		{
			MathExpression expression = ((FromSubquery) outerTable).expression;
			TypeChecker typechecker = ((SubqueryExpression)expression).getTypeChecker();
			typerCheckerMap.put(((FromSubquery) outerTable).alias, typechecker);
		}
		
		/*
		 * 2.2.2 sub check the table defined by the VGFunction
		 */
		
		boolean withStatementSubcheck[] = new boolean[withList.size()];
		
		
		for(int i = 0; i < withList.size(); i++)
		{
			withStatementSubcheck[i] = withList.get(i).acceptVisitor(this);
			if(!withStatementSubcheck[i])
				return false;
		}
		
		/*
		 * 3 check the select statement in the random table definition
		 */
		
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<SQLExpression> tableReferenceList = selectStatement.tableReferenceList;
		BooleanPredicate whereClause = selectStatement.whereClause;
		ArrayList<ColumnExpression> groupByClauseList = selectStatement.groupByClauseList;
		ArrayList<OrderByColumn> orderByClauseList = selectStatement.orderByClauseList;
		BooleanPredicate havingClause = selectStatement.havingClause;
		
		
		/*
		 * Check the FROM clause
		 * 3.1.1 Check if the table's alias appear twice in the FROM clause.
 		 */
		checkState = FROMSTAGE;
		if(tableReferenceList == null || tableReferenceList.size() == 0)
		{
			System.err.println("From clause can not be empty!");
			errorNum ++;
		}
		
		for(int i = 0; i < tableReferenceList.size(); i++)
		{
			SQLExpression fromElement = tableReferenceList.get(i);
			
			if(fromElement instanceof TableReference)
			{
				if(tableReferenceMap.containsKey(((TableReference) fromElement).alias))
				{
					System.err.println("Relation [" + ((TableReference) fromElement).alias +
							"] is ambigurous!");
					errorNum ++;
				}
				else
				{
					tableReferenceMap.put(((TableReference) fromElement).alias, ((TableReference) fromElement));
					fromList.add(((TableReference) fromElement).alias);
				}
			}
			else if(fromElement instanceof FromSubquery)
			{
				if(fromSubqueryMap.containsKey(((FromSubquery) fromElement).alias))
				{
					System.err.println("Subquery [" + ((FromSubquery) fromElement).alias +
							"] is ambigurous!");
					errorNum ++;
				}
				else
				{
					fromSubqueryMap.put(((FromSubquery) fromElement).alias, ((FromSubquery) fromElement));
					fromList.add(((FromSubquery) fromElement).alias);
				}
			}
			else
			{
				errorNum ++;
			}
		}
		
		/*
		 * 3.1.2 sub check: each element should have a visit check: if it is in the real relation
		 */
		for(int i = 0; i < tableReferenceList.size(); i++)
		{
			SQLExpression fromElement = tableReferenceList.get(i);
			//from table
			
			if(fromElement instanceof TableReference)
			{
				String table = ((TableReference) fromElement).table;
				//String alias = ((TableReference) fromElement).alias;
				
				//if the table is referenced to a VGFunction
				if(vgFunctionMap.containsKey(table))
				{
					continue;
				}
				else
				{
					subcheck = ((TableReference)fromElement).acceptVisitor(this);
					if(subcheck == false)
					{
						errorNum ++;
						return false;
					}
				}
			}
			// from subquery
			else if(fromElement instanceof FromSubquery)
			{
				subcheck = ((FromSubquery)fromElement).acceptVisitor(this);
				MathExpression expression = ((FromSubquery) fromElement).expression;
				TypeChecker typechecker = ((SubqueryExpression)expression).getTypeChecker();
				typerCheckerMap.put(((FromSubquery) fromElement).alias, typechecker);
				if(subcheck == false)
				{
					errorNum ++;
					return false;
				}
			}	
		}
		
		/*
		 * 3.2.1 Check select clause, if the layer >= 1, then such selected attributes should have no overlapped name.
		 */
		checkState = SELECTSTAGE;
		if(selectList == null || selectList.size() == 0)
		{
			System.err.println("Select clause can not be empty!");
			errorNum ++;
		}
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			subcheck = selectElement.acceptVisitor(this);
			if(subcheck == false)
			{
				errorNum ++;
			}
		}
		/*
		 * 3.2.2 if the layer >= 1, then such output attributes should have no overlapped name.
		 */
		this.setAllowDuplicatedAttributeAlias(false);
		if(!this.isAllowDuplicatedAttributeAlias())
		{
			checkDuplicateOutputForRandomTable(selectList);
		}
		
		fillInOutputAttributeForRandomTable(selectList, fromList);
		
		/*
		 * 3.3 Check where clause
		 */
		checkState = WHERESTAGE;
		if(whereClause != null)
		{
			subcheck = whereClause.acceptVisitor(this);
			if(subcheck == false)
			{
				errorNum ++;
			}
		}
		
		/*
		 * There should not be direct aggregationExpression in where clause.
		 */
		subcheck = new WhereRule(whereClause).checkWhereClause();
		if(subcheck == false)
		{
			System.err.println("Invalide use of group function in where clause!");
			errorNum ++;
		}
		
		outputAliasMap = outputAliasTempMap;
		/*
		 * 4. Check groupby clause, we only support the column name of the group by.
		 */
		checkState = GROUPBYSTAGE;
		if(groupByClauseList != null)
		{
			ArrayList<DataType> subTypeList;
			for(int i = 0; i < groupByClauseList.size(); i++)
			{
				ColumnExpression column = groupByClauseList.get(i);
				subTypeList = column.acceptVisitor(this);
				
				if(subTypeList == null)
				{
					errorNum ++;
				}
			}
			
			GroupbyRule rule = new GroupbyRule(selectStatement, this);
			if(!rule.checkGroupbyRule())
			{
				System.err.println("The column in select clause and groupby clause should follow the " +
						"Groupby rule!");
				errorNum ++;
			}
		}
		
		/*
		 * 5. orderby clause
		 */
		checkState = ORDERBYSTAGE;
		if(orderByClauseList != null)
		{
			for(int i = 0; i < orderByClauseList.size(); i++)
			{
				OrderByColumn column = orderByClauseList.get(i);
				subcheck = column.acceptVisitor(this);
				if(subcheck == false)
				{
					errorNum ++;
				}
			}
		}
		
		/*
		 * 6. Having clause
		 */
		checkState = HAVINGSTAGE;
		if(havingClause != null)
		{
			subcheck = havingClause.acceptVisitor(this);
			if(subcheck == false)
			{
				errorNum ++;
			}
		}
		
/*
		 * 7. Aggregation rule: all the aggregation should all have distinct, or all not.
		 */
		AggregationRule aggregationRule = new AggregationRule(selectStatement, this);
		if(!aggregationRule.checkRule())
		{
			System.err.println("The aggregations should have all distinct or all not-distinct!");
			errorNum ++;
		}
		
		
		/*
		 * 8. check if the output attributes match the random table definition, and save view if possible.
		 */
		
		ArrayList<DataType> gottenAttributeTypeList =  getRandomTableAttributes(selectList);
		
		ArrayList<String> definedAttribteList = definedSchema.tableAttributeList;
		
		String query = randomTableStatement.getSqlString();
		
		if(errorNum >= 1)
		{
			System.err.println("TypeChecker wrong!");
			throw new RuntimeException("TypeChecker wrong!");
		}
		
		
		/*
		 * 8. try to see if the number of attributes matches.
		 */
		if(definedAttribteList!= null && gottenAttributeTypeList.size() != definedAttribteList.size())
		{
			errorNum ++;
			System.err.println("The number of attributes defined in the random table [" +
					definedSchema.getViewName() + "] does not match its defintion in its subquery!");
			return false;
		}
		
		/*
		 * 9. check if the Table under the different time tick has the same schema.
		 * Note that BaselineArrayRandomTypeChecker has already been considered by the GeneralRandomTableTypeChecker,
		 * which would be called by the BaselineArrayRandomTypeChecker.
		 */
		if(this instanceof BaseLineRandomTableTypeChecker ||
				this instanceof GeneralRandomTableTypeChecker)
		{
			String viewName = definedSchema.getViewName();
			int end = viewName.lastIndexOf("_");
			
			String realViewName = viewName.substring(0, end);
			ArrayList<String> timeTickTableList = catalog.getIndexTableList(realViewName);
			if(timeTickTableList != null && timeTickTableList.size() > 0)
			{
				String anotherTimeTickTable = timeTickTableList.get(0);
				View anotherTimeTickView = catalog.getView(anotherTimeTickTable);
				ArrayList<Attribute> tempAttributeList = anotherTimeTickView.getAttributes();
				if(tempAttributeList.size() != gottenAttributeTypeList.size())
				{
					System.err.println("The random table under the different time tick has the different number of attributes!");
					return false;
				}
				
				for(int i = 0; i < tempAttributeList.size(); i++)
				{
					if(!gottenAttributeTypeList.get(i).getTypeName().equals(tempAttributeList.get(i).getType().getTypeName()))
					{
				//		System.out.println("first attribute: " + gottenAttributeTypeList.get(i).getTypeName());
				//		System.out.println("second attribute: " + tempAttributeList.get(i).getType().getTypeName());
				//		System.err.println("The random table under the different time tick has the different type!");
				//		return false;
					}
					
					if(definedAttribteList != null)
					{
						if(!definedAttribteList.get(i).equals(tempAttributeList.get(i).getName()))
						{
							System.err.println("The random table under the different time tick has the different name!");
							return false;
						}
					}
					else
					{
						if(!attributeList.get(i).equals(tempAttributeList.get(i).getName()))
						{
							System.err.println("The random table under the different time tick has the different name!");
							return false;
						}
					}
				}
			}
		}
		
		/*
		 * 10. to save.
		 */
		if(toSave)
		{
			saveView(definedSchema, gottenAttributeTypeList, query);
		}
		
		
		return errorNum == 0;
	}
	
	public int checkDuplicateOutputForRandomTable(ArrayList<SQLExpression> selectList)throws Exception
	{
		int errorNum = 0;
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			//select * from ..
			if(selectElement instanceof AsteriskTable)
			{
				//consider the tableReferences, see
				String tableAlias[] = new String[tableReferenceMap.size()];
				tableAlias = tableReferenceMap.keySet().toArray(tableAlias);
				
				TableReference tempTableReference;
				String tableName;
				Relation relation;
				ArrayList<Attribute> tempList;
				for(int j = 0; j < tableAlias.length; j++)
				{
					tempTableReference = tableReferenceMap.get(tableAlias[j]);
					tableName = tempTableReference.table;
					
					//for the VGFunction
					if(vgFunctionMap.containsKey(tableName))
					{
						GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
						tempList = vgFunction.getOutputAtts();
						for(int k = 0; k < tempList.size(); k++)
						{
							if(attributeTableAliasMap.containsKey(tempList.get(k).getName()))
							{
								System.err.println("Attributes in \" select * \" have the same attribute name!");
								errorNum ++;
							}
							else
							{
								attributeTableAliasMap.put(tempList.get(k).getName(), tableAlias[j]);
								attributeTypeMap.put(tempList.get(k).getName(), tempList.get(k).getType());
							}
						}
					}
					//for the relation
					else
					{
						relation = catalog.getRelation(tableName);
						
						tempList = relation.getAttributes();
						for(int k = 0; k < tempList.size(); k++)
						{
							if(attributeTableAliasMap.containsKey(tempList.get(k).getName()))
							{
								System.err.println("Attributes in \" select * \" have the same attribute name!");
								errorNum ++;
							}
							else
							{
								attributeTableAliasMap.put(tempList.get(k).getName(), tableAlias[j]);
								attributeTypeMap.put(tempList.get(k).getName(), tempList.get(k).getType());
							}
						}
					}
					
				}
				
				//consider the subquery
				String subqueryAlias[] = new String[typerCheckerMap.size()];
				subqueryAlias = typerCheckerMap.keySet().toArray(subqueryAlias);
				TypeChecker tempChecker;
				String subQueryAttributes[];
				for(int j = 0; j < subqueryAlias.length; j++)
				{
					tempChecker = typerCheckerMap.get(subqueryAlias[j]);
					HashMap<String, String> tempAttributeMap = tempChecker.getAttributeMap();
					subQueryAttributes = new String[tempAttributeMap.size()];
					subQueryAttributes = tempAttributeMap.keySet().toArray(subQueryAttributes);
					for(int k = 0; k < subQueryAttributes.length; k++)
					{
						if(attributeTableAliasMap.containsKey(subQueryAttributes[k]))
						{
							System.err.println("Attributes in \" select * \" have the same attribute name!");
							errorNum ++;
						}
						else
						{
							attributeTableAliasMap.put(subQueryAttributes[k], subqueryAlias[j]);
							attributeTypeMap.put(subQueryAttributes[k], tempChecker.getAttributeTypeMap().get(subQueryAttributes[k]));
						}
					}
				}
			}
			//select A.* from
			else if(selectElement instanceof AllFromTable)
			{
				String tableAlias = ((AllFromTable) selectElement).table;
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				//if the tableAlias is from a table reference.
				if(tempTableReference != null)
				{
					String tableName = tempTableReference.table;
					
					if(vgFunctionMap.containsKey(tableName))
					{
						GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
						ArrayList<Attribute> tempList = vgFunction.getOutputAtts();
						for(int k = 0; k < tempList.size(); k++)
						{
							if(attributeTableAliasMap.containsKey(tempList.get(k).getName()))
							{
								System.err.println("Attributes in \" select * \" have the same attribute name!");
								errorNum ++;
							}
							else
							{
								attributeTableAliasMap.put(tempList.get(k).getName(), tableAlias);
								attributeTypeMap.put(tempList.get(k).getName(), tempList.get(k).getType());
							}
						}
					}
					else
					{
						Relation relation = catalog.getRelation(tableName);
						ArrayList<Attribute> tempList = relation.getAttributes();
						for(int k = 0; k < tempList.size(); k++)
						{
							if(attributeTableAliasMap.containsKey(tempList.get(k).getName()))
							{
								System.err.println("Attributes in \" select * \" have the same attribute name!");
								errorNum ++;
							}
							else
							{
								attributeTableAliasMap.put(tempList.get(k).getName(), tableAlias);
								attributeTypeMap.put(tempList.get(k).getName(), tempList.get(k).getType());
							}
						}
					}
					
				}
				//if the table alias is from a subquery in from clause.
				else
				{
					TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
					HashMap<String, String> tempAttributeMap = tempChecker.getAttributeMap();
					String subQueryAttributes[] = new String[tempAttributeMap.size()];
					subQueryAttributes = tempAttributeMap.keySet().toArray(subQueryAttributes);
					for(int k = 0; k < subQueryAttributes.length; k++)
					{
						if(attributeTableAliasMap.containsKey(subQueryAttributes[k]))
						{
							System.err.println("Attributes in \" select * \" have the same attribute name!");
							errorNum ++;
						}
						else
						{
							attributeTableAliasMap.put(subQueryAttributes[k], tableAlias);
							attributeTypeMap.put(subQueryAttributes[k], tempChecker.getAttributeTypeMap().get(subQueryAttributes[k]));
						}
					}
				}
			}
			//select A.B, B, expression
			else
			{
				String alias = ((DerivedColumn)selectElement).alias;
				if(attributeTableAliasMap.containsKey(alias))
				{
					System.err.println("Attributes in \" select * \" have the same attribute name!");
					errorNum ++;
				}
				else
				{
					attributeTableAliasMap.put(alias, "expression");
				}
			}
		}
		if(errorNum >= 1)
		{
			throw new Exception("Errors in grammar!");
		}
		
		return errorNum;
	}

	/*
	 * return the superset of ColumnExpressions AllFromTable
	 */
	public ArrayList<ColumnExpression> getColumnFromAllFromTable(AllFromTable table)throws Exception
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		String fromAlias = table.table;
		
		//if the tableAlias is from a table reference.
		if(tableReferenceMap.containsKey(fromAlias))
		{
			TableReference tempTableReference = tableReferenceMap.get(fromAlias);
			String tableName = tempTableReference.table;
			
			if(vgFunctionMap.containsKey(tableName))
			{
				GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
				VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
				ArrayList<Attribute> tempList = vgFunction.getOutputAtts();
				for(int k = 0; k < tempList.size(); k++)
				{
					String name = tempList.get(k).getName();
					ColumnExpression column = new ColumnExpression(fromAlias, name);
					resultList.add(column);
				}
			}
			else
			{
				Relation relation = catalog.getRelation(tableName);
				ArrayList<Attribute> tempList;
				
				View view;
				if(relation != null)
				{
					tempList = relation.getAttributes();
				}
				else
				{
					view = catalog.getView(tableName);
					tempList = view.getAttributes();
				}
				
				for(int k = 0; k < tempList.size(); k++)
				{
					String name = tempList.get(k).getName();
					ColumnExpression column = new ColumnExpression(fromAlias, name);
					resultList.add(column);
				}
			}
		}
		//if the table alias is from a subquery in from clause.
		else if(typerCheckerMap.containsKey(fromAlias))
		{
			TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
			ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
			
			for(int k = 0; k < outputAttributeStringList.size(); k++)
			{
				String name = outputAttributeStringList.get(k);
				ColumnExpression column = new ColumnExpression(fromAlias, name);
				resultList.add(column);
			}
		}
		
		return resultList;
	}
	
	/*
	 * return the superset of all the ColumnExpression in the from clause
	 */
	public ArrayList<ColumnExpression> getAsteriskTable()throws Exception
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		for(int j = 0; j < fromList.size(); j ++)
		{
			String fromAlias = fromList.get(j);
			
			TableReference tempTableReference;
			String tableName;
			Relation relation;
			ArrayList<Attribute> tempList;
			
			if(tableReferenceMap.containsKey(fromAlias))
			{
				tempTableReference = tableReferenceMap.get(fromAlias);
				tableName = tempTableReference.table;
				
				if(vgFunctionMap.containsKey(tableName))
				{
					GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
					VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
					tempList = vgFunction.getOutputAtts();
					for(int k = 0; k < tempList.size(); k++)
					{
						String name = tempList.get(k).getName();
						ColumnExpression column = new ColumnExpression(fromAlias, name);
						resultList.add(column);
					}
				}
				//for the relation
				else
				{
					relation = catalog.getRelation(tableName);
					
					View view;
					if(relation != null)
					{
						tempList = relation.getAttributes();
					}
					else
					{
						view = catalog.getView(tableName);
						tempList = view.getAttributes();
					}
					
					for(int k = 0; k < tempList.size(); k++)
					{
						String name = tempList.get(k).getName();
						ColumnExpression column = new ColumnExpression(fromAlias, name);
						resultList.add(column);
					}
				}
			}
			//subquery
			else if(typerCheckerMap.containsKey(fromAlias))
			{
				TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
				ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
				
				for(int k = 0; k < outputAttributeStringList.size(); k++)
				{
					String name = outputAttributeStringList.get(k);
					ColumnExpression column = new ColumnExpression(fromAlias, name);
					resultList.add(column);
				}
			}
		}
		
		return resultList;
	}
	
	public void fillInOutputAttributeForRandomTable(ArrayList<SQLExpression> selectList, ArrayList<String> fromList)throws Exception
	{
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			//select * from ..
			if(selectElement instanceof AsteriskTable)
			{
				for(int j = 0; j < fromList.size(); j ++)
				{
					String fromAlias = fromList.get(j);
					
					TableReference tempTableReference;
					String tableName;
					Relation relation;
					ArrayList<Attribute> tempList;
					
					if(tableReferenceMap.containsKey(fromAlias))
					{
						tempTableReference = tableReferenceMap.get(fromAlias);
						tableName = tempTableReference.table;
						
						if(vgFunctionMap.containsKey(tableName))
						{
							GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
							VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
							tempList = vgFunction.getOutputAtts();
							for(int k = 0; k < tempList.size(); k++)
							{
								String name = tempList.get(k).getName();
								attributeList.add(name);
								attributeTypeMap.put(name, tempList.get(k).getType());
							}
						}
						//for the relation
						else
						{
							relation = catalog.getRelation(tableName);
							
							View view;
							if(relation != null)
							{
								tempList = relation.getAttributes();
							}
							else
							{
								view = catalog.getView(tableName);
								tempList = view.getAttributes();
							}
							
							for(int k = 0; k < tempList.size(); k++)
							{
								String name = tempList.get(k).getName();
								attributeList.add(name);
								attributeTypeMap.put(name, tempList.get(k).getType());
							}
						}
					}
					//subquery
					else if(typerCheckerMap.containsKey(fromAlias))
					{
						TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
						ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
						HashMap<String, DataType> outputAttributeTypeMap = tempChecker.getAttributeTypeMap();
						
						for(int k = 0; k < outputAttributeStringList.size(); k++)
						{
							String name = outputAttributeStringList.get(k);
							attributeList.add(name);
							attributeTypeMap.put(name, outputAttributeTypeMap.get(name));
						}
					}
				}
			}
			//select A.* from
			else if(selectElement instanceof AllFromTable)
			{
				String fromAlias = ((AllFromTable) selectElement).table;
				
				//if the tableAlias is from a table reference.
				if(tableReferenceMap.containsKey(fromAlias))
				{
					TableReference tempTableReference = tableReferenceMap.get(fromAlias);
					String tableName = tempTableReference.table;
					
					if(vgFunctionMap.containsKey(tableName))
					{
						GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
						ArrayList<Attribute> tempList = vgFunction.getOutputAtts();
						for(int k = 0; k < tempList.size(); k++)
						{
							String name = tempList.get(k).getName();
							attributeList.add(name);
							attributeTypeMap.put(name, tempList.get(k).getType());
						}
					}
					else
					{
						Relation relation = catalog.getRelation(tableName);
						ArrayList<Attribute> tempList;
						
						View view;
						if(relation != null)
						{
							tempList = relation.getAttributes();
						}
						else
						{
							view = catalog.getView(tableName);
							tempList = view.getAttributes();
						}
						
						for(int k = 0; k < tempList.size(); k++)
						{
							String name = tempList.get(k).getName();
							attributeList.add(name);
							attributeTypeMap.put(name, tempList.get(k).getType());
						}
					}
				}
				//if the table alias is from a subquery in from clause.
				else if(typerCheckerMap.containsKey(fromAlias))
				{
					TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
					ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
					HashMap<String, DataType> outputAttributeTypeMap = tempChecker.getAttributeTypeMap();
					
					for(int k = 0; k < outputAttributeStringList.size(); k++)
					{
						String name = outputAttributeStringList.get(k);
						attributeList.add(name);
						attributeTypeMap.put(name, outputAttributeTypeMap.get(name));
					}
				}
			}
			//select A.B, B, expression
			else
			{
				String alias = ((DerivedColumn)selectElement).alias;
				attributeList.add(alias);
			}
		}
	}
	
	public ArrayList<DataType> getRandomTableAttributes(ArrayList<SQLExpression> selectList)throws Exception
	{
		ArrayList<DataType> attributeTypeList = new ArrayList<DataType>();
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			//select * from ..
			if(selectElement instanceof AsteriskTable)
			{
				for(int j = 0; j < fromList.size(); j ++)
				{
					String fromAlias = fromList.get(j);
					
					TableReference tempTableReference;
					String tableName;
					Relation relation;
					ArrayList<Attribute> tempList;
					
					if(tableReferenceMap.containsKey(fromAlias))
					{
						tempTableReference = tableReferenceMap.get(fromAlias);
						tableName = tempTableReference.table;
						
						if(vgFunctionMap.containsKey(tableName))
						{
							GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
							VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
							tempList = vgFunction.getOutputAtts();
							for(int k = 0; k < tempList.size(); k++)
							{
								attributeTypeList.add(tempList.get(k).getType());
							}
						}
						//for the relation
						else
						{
							relation = catalog.getRelation(tableName);
							View view;
							if(relation != null)
							{
								tempList = relation.getAttributes();
							}
							else
							{
								view = catalog.getView(tableName);
								tempList = view.getAttributes();
							}
							
							for(int k = 0; k < tempList.size(); k++)
							{
								attributeTypeList.add(tempList.get(k).getType());
							}
						}
					}
					//subquery
					else if(typerCheckerMap.containsKey(fromAlias))
					{
						TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
						ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
						HashMap<String, DataType> attributeTypeInSubQuery = tempChecker.getAttributeTypeMap();
						
						for(int k = 0; k < outputAttributeStringList.size(); k++)
						{
							attributeTypeList.add(attributeTypeInSubQuery.get(outputAttributeStringList.get(k)));
						}
					}
				}
			}
			//select A.* from
			else if(selectElement instanceof AllFromTable)
			{
				String fromAlias = ((AllFromTable) selectElement).table;
				
				//if the tableAlias is from a table reference.
				if(tableReferenceMap.containsKey(fromAlias))
				{
					TableReference tempTableReference = tableReferenceMap.get(fromAlias);
					String tableName = tempTableReference.table;
					
					if(vgFunctionMap.containsKey(tableName))
					{
						GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
						ArrayList<Attribute> tempList = vgFunction.getOutputAtts();
						for(int k = 0; k < tempList.size(); k++)
						{
							attributeTypeList.add(tempList.get(k).getType());
						}
					}
					else
					{
						Relation relation = catalog.getRelation(tableName);
						ArrayList<Attribute> tempList;
						View view;
						if(relation != null)
						{
							tempList = relation.getAttributes();
						}
						else
						{
							view = catalog.getView(tableName);
							tempList = view.getAttributes();
						}
						
						for(int k = 0; k < tempList.size(); k++)
						{
							attributeTypeList.add(tempList.get(k).getType());
						}
					}
				}
				//if the table alias is from a subquery in from clause.
				else if(typerCheckerMap.containsKey(fromAlias))
				{
					TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
					ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
					HashMap<String, DataType> attributeTypeInSubQuery = tempChecker.getAttributeTypeMap();
					
					for(int k = 0; k < outputAttributeStringList.size(); k++)
					{
						attributeTypeList.add(attributeTypeInSubQuery.get(outputAttributeStringList.get(k)));
					}
				}
			}
			//select A.B, B, expression
			else
			{
				String alias = ((DerivedColumn)selectElement).alias;
				attributeTypeList.add(attributeTypeMap.get(alias));
			}
		}
		
		return attributeTypeList;
	}
	
	public void saveView(DefinedTableSchema tableAttributes, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql) throws Exception
	{
		ArrayList<Attribute> schema = saveAttributes(tableAttributes, gottenAttributeTypeList);

		String viewName = tableAttributes.getViewName();
		View view = new View(viewName, sql, schema, DataAccess.OBJ_RANDRELATION);
		catalog.addView(view);
	}

	protected ArrayList<Attribute> saveAttributes(DefinedTableSchema tableAttributes, ArrayList<DataType> gottenAttributeTypeList) {

		ArrayList<String> attributeNameList = tableAttributes.tableAttributeList;
		String viewName = tableAttributes.getViewName();

		ArrayList<Attribute> schema = new ArrayList<Attribute>();
		// The view explicitly define the schema
		if (attributeNameList != null) {
			for (int i = 0; i < attributeNameList.size(); i++) {
				String name = attributeNameList.get(i);
				DataType type = gottenAttributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schema.add(attribute);
			}
		}
		else {
			for (int i = 0; i < gottenAttributeTypeList.size(); i++) {
				String name = attributeList.get(i);
				DataType type = gottenAttributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schema.add(attribute);
			}
		}
		return schema;
	}

	public String getOutTableName() {
		return outTableName;
	}

	public void setOutTableName(String outTableName) {
		this.outTableName = outTableName;
	}

	
	public boolean isSaved() {
		return toSave;
	}

	public void setSaved(boolean saved) {
		this.toSave = saved;
	}

	
}
