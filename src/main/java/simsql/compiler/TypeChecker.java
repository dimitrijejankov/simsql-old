

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
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;

import simsql.runtime.TypeMachine;
import simsql.runtime.DataType;
import simsql.runtime.IntType;
import simsql.runtime.StringType;
import simsql.runtime.Compatibility;





/**
 * @author Bamboo
 *
 */
public class TypeChecker extends ASTVisitor{
	public static final int FROMSTAGE = 0;
	public static final int SELECTSTAGE = 1;
	public static final int WHERESTAGE = 2;
	public static final int GROUPBYSTAGE = 3;
	public static final int ORDERBYSTAGE = 4;
	public static final int HAVINGSTAGE = 5;
	
	public static int id = 0;
	
    protected Catalog catalog = SimsqlCompiler.catalog;
	private String identifierName;
	
	/*
	 * 1. the link structure (tree) of typechecker
	 */
	protected TypeChecker parent;
	
	//----------additional data structure from "from clause"----------------------
	
	//all the name in the from clause.
	protected ArrayList<String> fromList;
	
	//<table_alias, typerchecker> children of this typechecker
	public HashMap<String, TypeChecker> typerCheckerMap;
	
	//<table_alias, table>
	public HashMap<String, TableReference> tableReferenceMap;
	//<table_alias, subquery>
	protected HashMap<String, FromSubquery> fromSubqueryMap;
	//<table_alias, VGFunction>
	protected HashMap<String, GeneralFunctionExpression> vgFunctionMap;
	
	//----------additional data structure from "select clause"----------------------
	
	/*
	 * <attribute_alias, table_alias>: if the attribute_alias is an expression, then table_alias = "expression".
	 * Both data structure have meaning only when layer >= 1
	 * Here, the attribute means the output attribute.
	 */
	protected ArrayList<String> attributeList;
	protected HashMap<String, String> attributeTableAliasMap;
	protected HashMap<String, DataType> attributeTypeMap;
	protected HashMap<String, DataType> functionAttTypeMap;
	
	/*
	 * 3. save the output attributes
	 */
	protected HashMap<String, ArrayList<MathExpression>> outputAliasTempMap;
	/*
	 * for the column name after "where" clause, we first use outputAliasTempMap to save such output
	 * attributes with aliases, and then after the "where" clause, we use the outputAliasMap to check 
	 * their emergence as "alias".
	 */
	public HashMap<String, ArrayList<MathExpression>> outputAliasMap;
	
	//record the selectStatement.
	protected Expression  statement;
	
	
	/*
	 * 4. current check in which stage: 
	 * 	   1. from clause -> 2. select clause -> 3. where ->4. group by ->5. having
	 */
	protected int checkState;
	
	/*
	 * 5. Additional information for 
	 */
	
	//Which typechecker provides what tableReference
	protected HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap;
	protected HashMap<String, TypeChecker> edgeTypeChecker;
	
	//this typechecker provides what tableReference
	protected HashSet<String> providedReferenceSet;
	//children typechecker
	protected HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap;
	
	private boolean allowDuplicatedAttributeAlias;
	
	public TypeChecker(boolean allowDuplicatedAttributeAlias) throws Exception{
		this(null, allowDuplicatedAttributeAlias);
		
	}
	
	public TypeChecker(TypeChecker parent, boolean allowDuplicatedAttributeAlias) throws Exception
	{
		super();	
		
		identifierName = "block"+ id;
		id++;
		
		//data structure
		this.parent = parent;
		this.allowDuplicatedAttributeAlias = allowDuplicatedAttributeAlias;
		
		fromList = new ArrayList<String>();
		tableReferenceMap = new HashMap<String, TableReference>();
		fromSubqueryMap = new HashMap<String, FromSubquery>();
		vgFunctionMap = new HashMap<String, GeneralFunctionExpression>();
		
		typerCheckerMap = new HashMap<String, TypeChecker>();
		
		attributeList = new ArrayList<String>();
		attributeTableAliasMap = new HashMap<String, String>();
		attributeTypeMap = new HashMap<String, DataType>();
		functionAttTypeMap = new HashMap<String, DataType>();
		
		outputAliasTempMap = new HashMap<String, ArrayList<MathExpression>>();
		outputAliasMap = null;
		
		statement = null;
		
		checkState = -1;
		
		//edges for Typecheckers
		providedTableReferenceMap = new HashMap<TypeChecker, CheckerEdge>();
		edgeTypeChecker = new HashMap<String, TypeChecker>();
		providedReferenceSet = new HashSet<String>();
		childTypeCheckerMap = new HashMap<SubqueryExpression, TypeChecker>();
	}

	/*
	 * check the sql statement
	 */
	public boolean visitSelectStatement(SelectStatement selectStatement) throws Exception {
		//record this statements
		if(statement == null)
		{
			this.statement = selectStatement;
		}
		
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<SQLExpression> tableReferenceList = selectStatement.tableReferenceList;
		BooleanPredicate whereClause = selectStatement.whereClause;
		ArrayList<ColumnExpression> groupByClauseList = selectStatement.groupByClauseList;
		ArrayList<OrderByColumn> orderByClauseList = selectStatement.orderByClauseList;
		BooleanPredicate havingClause = selectStatement.havingClause;
		
		int errorNum = 0;
		boolean subcheck;
		
		checkState = FROMSTAGE;
		/*
		 * Check the FROM clause
		 * 1.1 Check if the table's alias appear twice in the FROM clause.
 		 */
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
				errorNum++;
			}
		}
		
		/*
		 * 1.2 sub check: each element should have a visit check: if it is in the real relation
		 */
		for(int i = 0; i < tableReferenceList.size(); i++)
		{
			SQLExpression fromElement = tableReferenceList.get(i);
			//from table
			if(fromElement instanceof TableReference)
			{
				subcheck = ((TableReference)fromElement).acceptVisitor(this);
				if(subcheck == false)
				{
					errorNum ++;
					return false;
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
		 * 2. Check select clause, if the layer >= 1, then such selected attributes should have no overlapped name.
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
		//if the layer >= 1, then such selected attributes should have no overlapped name.
		if(!allowDuplicatedAttributeAlias)
		{
			errorNum += checkDuplicateOutputForSelect(selectList);
		}
		
		if(errorNum == 0)
		{
			fillInOutputAttributeForSelect(selectList, fromList);
		}
		
		/*
		 * 3. Check where clause
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
				column.setIsGroupByColumn(true);
				
				subTypeList = column.acceptVisitor(this);
				
				if(subTypeList == null)
				{
					errorNum ++;
				}
			}
			
			/*
			 * check the groupby rule: see the comments in GroupbyRule.java
			 */
			GroupbyRule rule = new GroupbyRule(selectStatement, this);
			if(!rule.checkGroupbyRule())
			{
				System.err.println("The column in select clause and groupby clause should follow the " +
						"Groupby rule!");
				errorNum ++;
			}
		}
		else
		{
			/*
			 * check the groupby rule: see the comments in GroupbyRule.java
			 */
			GroupbyRule rule = new GroupbyRule(selectStatement, this);
			if(!rule.checkEmptyGroupbyRule())
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
		/*-----------------------------------modified by 1/2/2010-----------------------------
		 * I need to add something here...
		 * ----------------------------------------end ---------------------------------------
		 */
		if(havingClause != null)
		{
			subcheck = havingClause.acceptVisitor(this);
			if(subcheck == false)
			{
				errorNum ++;
			}
 			
			CheckSubquery checkSubquery = new CheckSubquery(havingClause);
			ArrayList<SubqueryExpression> queryListInHavingClause = checkSubquery.getSubqueryList();
			if(queryListInHavingClause.size() > 0)
			{
				errorNum ++;
				System.err.println("Having clause should not have subqueries!");
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
		
		//final answer
		if(errorNum >= 1)
		{
			System.err.println("TypeChecker wrong!");
			throw new Exception("TypeChecker wrong!");
		}
		
		return errorNum == 0;
	}
	
	/*
	 * 1. sqlExpression
	 * 1.1 check the "from" clause.
	 */
	//from A, B..
	/* (non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitTableReferenceExpression(component.sqlExpression.TableReference)
	 */
	public boolean visitTableReferenceExpression(TableReference tableReference) throws Exception{
		
		String table = tableReference.table;
		//String alias = tableReference.alias;
		String indexString = tableReference.indexString;
		MathExpression indexMathExp = tableReference.expression;
		
		Relation relation = catalog.getRelation(table);
		View view = catalog.getView(table);
		
		if(!this.checkValidTableIdentifier(tableReference))
		{
			return false;
		}
		
		if(tableReference.getTableInferenceType() == TableReference.COMMON_TABLE ||
				tableReference.getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE)
		{
			if(relation == null && view == null)
			{
				System.err.println("Relation/View [" + table + "] does not exist!");
				return false;
			}
			else if(relation != null && view != null)
			{
				System.err.println("Relation/View [" + table + "] both exist!");
				return false;
			}
			
			if(tableReference.getTableInferenceType() == TableReference.COMMON_TABLE)
			{
				if(indexMathExp != null)
				{
					System.err.println("Relation/View [" + table + "] is common table" +
							", that the index should be null!");
					return false;
				}
			}
			else if(tableReference.getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE)
			{
				if(indexMathExp != null)
				{
					System.err.println("Relation/View [" + table + "] is constant index table" +
							", that the index should be null!");
					return false;
				}
			}
		}
		else
		{
			/*
			 * -----------------------------------needed to be changed for simulation------------------------------
			 */
			if(tableReference.getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
			{
				if(view == null)
				{
					System.err.println("View [" + table + "] does not exist!");
					return false;
				}
				
				if(indexMathExp == null || indexString != null)
				{
					System.err.println("Relation/View [" + table + "] is general index table" +
							", that the indexMathExp should be not null, and indexString should" +
							"be null!");
					return false;
				}
				
				ArrayList<DataType> typeList = indexMathExp.acceptVisitor(this);
				boolean subcheck = (typeList != null);
				if(!subcheck)
				{
					System.err.println("Relation/View [" + table + "] is general index table" +
							", that the indexMathExp has check error!");
					return false;
				}
			}
			
			/*
			 * ---------------------------------------------end----------------------------------------------------
			 */
		}
		
		if(tableReference.getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE)
		{
			/*
			 * indexString should be a natural integer
			 */
			try
			{
				int index = Integer.parseInt(indexString);
				if(index < 0)
				{
					System.err.println("The constant index is not valid!");
					return false;
				}
			}
			catch(Exception e)
			{
				System.err.println("The constant index is not valid!");
				return false;
			}
		}
		else if(tableReference.getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
		{
			/*
			 * This can only happen in the generalRandomTable definition.
			 * 1. Its root typechecker should be GENERAL_RANDOM_TABLE_TYPECHECKER.
			 * 2. Its indexMathExp should consist "i", GeneralTableIndex, and consists of only numerical expression
			 * with functions and the arithimatic calculations. "+", "-", "X", "/".
			 */
			TypeChecker root = this;
			while(root.parent != null)
			{
				root = root.parent;
			}
			if(TypeCheckerHelper.getCheckerType(root) == TypeCheckerHelper.GENERAL_RANDOM_TABLE_TYPECHECKER ||
				(TypeCheckerHelper.getCheckerType(root) == TypeCheckerHelper.UNION_VIEW_TYPECHECKER &&
				  ((UnionViewStatementTypeChecker)root).isGeneralUnionViewTypeChecker())
			){
				// nothing to do.
			}
			else
			{
				System.err.println("The general index or math expression based index can only be in the general table definition!");
				return false;
			}
			
			ArrayList<MathExpression> expressionList = new ArrayList<MathExpression>();
			GeneralTableIndexExtractor.getGeneralTableIndexInMathExpression(indexMathExp, expressionList);
			if(expressionList.size() == 0)
			{
				System.err.println("The general index does not have index \"i\" !");
				return false;
			}
			
			if(!GeneralTableIndexChecker.checkInMathExpression(indexMathExp))
			{
				System.err.println("The general index does not follow the rule!");
				return false;
			}
		}
		
		return true;
	}
	
	@Override
	public boolean visitCommonTableNameTable(CommonTableName commonTableName)
			throws Exception {
		//We use the existing TableReference name rule to check this tableName;
		TableReference tableReference = new TableReference(commonTableName.getName());
		return new TypeChecker(null, false).visitTableReferenceExpression(tableReference);
	}

	@Override
	public boolean visitBaselineTableNameTable(
			BaselineTableName baselineTableName) throws Exception {
		String name = baselineTableName.getName();
		String indexString = baselineTableName.getIndex();
		
		/*//Redundant
		TableReference tableReference1 = new TableReference(name);
		if(!this.checkValidTableIdentifier(tableReference1))
		{
			return false;
		}
		
		try
		{
			int index = Integer.parseInt(indexString);
			if(index < 0)
			{
				System.err.println("The constant index is not valid!");
				return false;
			}
		}
		catch(Exception e)
		{
			System.err.println("The constant index is not valid!");
			return false;
		}
		*/
		
		/*
		 * check the whether such a BaselineTableName exists
		 */
		TableReference tableReference2 = new TableReference(name + "_" + indexString, 
												name + "_" + indexString, 
												indexString,
												TableReference.CONSTANT_INDEX_TABLE);
		return new TypeChecker(null, false).visitTableReferenceExpression(tableReference2);
	}

	@Override
	public boolean visitGeneralTableNameTable(
			GeneralTableName generalTableTableName) throws Exception {
		String name = generalTableTableName.getName();
		MathExpression indexExp = generalTableTableName.getIndexExp();
		
		/*1. It seems that this is redundant, since I will do it in the step 2.
		TableReference tableReference1 = new TableReference(name);
		if(!this.checkValidTableIdentifier(tableReference1))
		{
			return false;
		}
		*/
		/*
		 * 2. check the whether such a BaselineTableName exists
		 */
		TableReference tableReference2 = new TableReference(name + "_i", 
												name + "_i", 
												null,
												TableReference.GENERAL_INDEX_TABLE,
												indexExp);
		return new TypeChecker(this, false).visitTableReferenceExpression(tableReference2);
	}

	@Override
	public boolean visitBaselineTableNameArray(
			BaselineTableNameArray generalTableTableName) throws Exception {
		String name = generalTableTableName.getName();
		String lowerBound = generalTableTableName.getLowerBound();
		String upBound = generalTableTableName.getUpBound();
		
		/*
		 * 1. check if both names follow the rule.
		 */
		TableReference tableReference1 = new TableReference(name + "_" + lowerBound, 
				name + "_" + lowerBound, 
				lowerBound,
				TableReference.CONSTANT_INDEX_TABLE);
		TypeChecker tempChecker = new TypeChecker(null, false);
		if(!tempChecker.visitTableReferenceExpression(tableReference1))
		{
			return false;
		}
		
		TableReference tableReference2 = new TableReference(name + "_" + upBound, 
				name + "_" + upBound, 
				upBound,
				TableReference.CONSTANT_INDEX_TABLE);
		if(!tempChecker.visitTableReferenceExpression(tableReference2))
		{
			return false;
		}
		
		/*
		 * 2. check if the lowerBound is smaller or equal with  upBound
		 */
		try
		{
			int up = Integer.parseInt(upBound);
			int lower = Integer.parseInt(lowerBound);
			
			if(up < lower)
			{
				System.err.println(generalTableTableName.toString() + "does not follow the rule!");
				return false;
			}
		}
		catch(Exception e)
		{
			System.err.println(generalTableTableName.toString() + "does not follow the rule!");
			return false;
		}
		
		return true;
	}

	@Override
	public boolean visitGeneralTableNameArray(
			GeneralTableNameArray generalTableTableName) throws Exception {
		String name = generalTableTableName.getName();
		MathExpression lowerBoundExp = generalTableTableName.getLowerBoundExp();
		MathExpression upBoundExp = generalTableTableName.getUpBoundExp();
		
		TableReference tableReference1 = new TableReference(name + "_i", 
				name + "_i", 
				null,
				TableReference.GENERAL_INDEX_TABLE,
				lowerBoundExp);
		
		TypeChecker tempChecker = new TypeChecker(this, false);
		if(!tempChecker.visitTableReferenceExpression(tableReference1))
		{
			return false;
		}
		
		TableReference tableReference2 = new TableReference(name + "_i", 
				name + "_i", 
				null,
				TableReference.GENERAL_INDEX_TABLE,
				upBoundExp);
		
		if(!tempChecker.visitTableReferenceExpression(tableReference2))
		{
			return false;
		}
		
		return true;
	}

	
	//from (select ..)
	public boolean visitFromSubqueryExpression(FromSubquery fromSubquery) throws Exception{
		MathExpression sql = fromSubquery.expression;
		//In the from clause, a subquery does not need parent typechecker
		if(this.parent == null)
			((SubqueryExpression)sql).setNeedparent(false);
		else
			((SubqueryExpression)sql).setNeedparent(true);
		ArrayList<DataType> typeList = sql.acceptVisitor(this);
		boolean subcheck = (typeList != null);
		return subcheck;
	}
		
	/*
	 * 1.2 check the "select" clause: select * from ..
	 */
	
	/*
	 * (non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitAsteriskTable(component.sqlExpression.AsteriskTable)
	 */
	public boolean visitAsteriskTable(AsteriskTable asteriskTable) {
		return true;
	}
	
	/*
	 * select A.* from .. Here A can be a parent table
	 * (non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitAllFromTable(component.sqlExpression.AllFromTable)
	 */
	public boolean visitAllFromTable(AllFromTable allFromTable)throws Exception {
		String table = allFromTable.table;
		HashMap<String, TableReference> tempTableReferenceMap;
		HashMap<String, FromSubquery> tempFromSubqueryMap;
		
		TableReference tableReference;
		FromSubquery fromSubquery;
		
		//from the current layer to find the table
		TypeChecker typeChecker = this;
		while(typeChecker != null)
		{
			tempTableReferenceMap = typeChecker.tableReferenceMap;
			tempFromSubqueryMap = typeChecker.fromSubqueryMap;
			
			tableReference = tempTableReferenceMap.get(table);
			fromSubquery = tempFromSubqueryMap.get(table);
			
			if(tableReference != null && fromSubquery!= null)
			{
				System.err.println("The selected attribute [" + table + ".*] is ambigious!");
				return false;
			}
			else if(tableReference == null && fromSubquery != null ||
					tableReference != null && fromSubquery == null)
			{
				if(typeChecker != this)
				{
					SQLExpression element = (tableReference != null? tableReference:fromSubquery);
					ArrayList<String> columnNameList = UnnestHelper.getAttributeString(typeChecker, element);
					
					if(!providedTableReferenceMap.containsKey(typeChecker))
					{
						CheckerEdge edge = new CheckerEdge();
						providedTableReferenceMap.put(typeChecker, edge);
						
					}
					
					CheckerEdge edge = providedTableReferenceMap.get(typeChecker);
					
					if(!edge.containTableReference(element))
					{
						edge.addTableReference(element);
						
					}
					
					for(int i = 0; i < columnNameList.size(); i++)
					{
						ColumnExpression column = new ColumnExpression(table, columnNameList.get(i));
						edge.addColumnExpression(element, column);
						edgeTypeChecker.put(column.toString(), typeChecker);
						typeChecker.addReferenceString(column.toString());
					}
				}
				return true;
			}
			
			//parent layer
			typeChecker = typeChecker.parent;
		}
		
		System.err.println("The selected attribute [" + table + ".*] is not found!");
		return false;
	}
	
	/*
	 * select A.B or select B or select expression or select expression as ..
	 */
	public boolean visitDerivedColumn(DerivedColumn derivedColumn) throws Exception{
		MathExpression expression = derivedColumn.expression;
		String alias = derivedColumn.alias;
		ArrayList<DataType> typeList = expression.acceptVisitor(this);
		
		
		/*
		 * derivedColumn is not allowed to have subqueries
		 */
		ComparisonPredicate predicate = new ComparisonPredicate(expression, 
												                new NumericExpression("0"),
												                FinalVariable.EQUALS);
		boolean subcheck;
		subcheck = new CheckSubquery(predicate).hasSubqueryInBooleanPredicate();
		
		if(subcheck)
		{
			System.err.println("select item is not allowed to have subqueries!");
			typeList = null;
		}
		
		if(typeList != null)
		{
			if(!outputAliasTempMap.containsKey(alias))
			{
				ArrayList<MathExpression> list = new ArrayList<MathExpression>();
				list.add(expression);
				outputAliasTempMap.put(alias, list);
			}
			else
			{
				ArrayList<MathExpression> list = outputAliasTempMap.get(alias);
				list.add(expression);
			}
		}
		attributeTypeMap.put(alias, typeList.get(0));
		subcheck = (typeList != null);
		return subcheck;
	}
	
	
	//type check orderby column
	public boolean visitOrderByColumn(OrderByColumn orderByColumn)throws Exception {
		MathExpression expression = orderByColumn.expression;
		ArrayList<DataType> subTypeList = expression.acceptVisitor(this);

		return subTypeList != null;
	}

	/* (non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitTableExpression(component.sqlExpression.TableAttributes)
	 */
	@Override
	public boolean visitDefinedTableSchemaExpression(DefinedTableSchema table) throws Exception{

		String tableName = table.getViewName();
		ArrayList<String> tableAttributeList = table.tableAttributeList;
		HashSet<String> stringSet = new HashSet<String>();

		Relation relation = catalog.getRelation(tableName);
		View view = catalog.getSchemaView(tableName);

		if(relation != null || view!= null)
		{
			System.err.println("Entity [" + tableName + "] does exist!");
			return false;
		}

		if(tableAttributeList != null)
		{
			for (String aTableAttributeList : tableAttributeList) {
				if (stringSet.contains(aTableAttributeList)) {
					System.err.println("The attributes in Relation [" + tableName + "] overlapped!");
					return false;
				} else {
					stringSet.add(aTableAttributeList);
				}
			}
		}
		return true;
	}

	/* (non-Javadoc)
 * @see astVisitor.ASTVisitor#visitTableExpression(component.sqlExpression.TableAttributes)
 */
	@Override
	public boolean visitMultidimensionalTableSchemaExpression(MultidimensionalTableSchema table) throws Exception {

		String tableName = table.getViewName();
		ArrayList<String> tableAttributeList = table.tableAttributeList;
		HashSet<String> stringSet = new HashSet<String>();

		Relation relation = catalog.getRelation(tableName);
		View view = catalog.getSchemaView(tableName);

		if(relation != null || view!= null)
		{
			System.err.println("Entity [" + tableName + "] does exist!");
			return false;
		}

		if(tableAttributeList != null)
		{
			for (String aTableAttributeList : tableAttributeList) {
				if (stringSet.contains(aTableAttributeList)) {
					System.err.println("The attributes in Relation [" + tableName + "] overlapped!");
					return false;
				} else {
					stringSet.add(aTableAttributeList);
				}
			}
		}
		return true;
	}
	
	@Override
	public boolean visitDefinedArrayTableSchemaExpression(DefinedArrayTableSchema definedArrayTableSchema) throws Exception {
		String lowerBound = definedArrayTableSchema.lowerBound;
		String upBound = definedArrayTableSchema.upBound;
		String viewName = definedArrayTableSchema.getViewName();
		ArrayList<String> tableAttributeList = definedArrayTableSchema.tableAttributeList;
		TypeChecker typeChecker = new TypeChecker(null, false);
		
		//check the both bounds
		int lowerIndex, upIndex;
		try
		{
			lowerIndex = Integer.parseInt(lowerBound);
			upIndex = Integer.parseInt(upBound);
			if(lowerIndex > upIndex)
			{
				System.err.println("Error indec in the defintion of " + definedArrayTableSchema.toString());
				
			}
			
			for(int i = lowerIndex; i <= upIndex; i++)
			{
				DefinedTableSchema tempSchema = new DefinedTableSchema(viewName+"_" + i, tableAttributeList, true);
				if(!typeChecker.visitDefinedTableSchemaExpression(tempSchema))
				{
					return false;
				}
			}
		}
		catch(Exception e)
		{
			System.err.println("Error index in the defintion of " + definedArrayTableSchema.toString());
			return false;
		}
		
		return true;
	}

	

	/* (non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitWithStatementExpression(component.sqlExpression.WithStatement)
	 */
	@Override
	public boolean visitWithStatementExpression(WithStatement withStatement) throws Exception{
		GeneralFunctionExpression expression = withStatement.expression;
		String tempTable = withStatement.tempTableName;
		
		ArrayList<DataType> subTypeList = expression.acceptVisitor(this);
		if(subTypeList != null)
		{
			// vg function
			if(expression.isVGFunction()){
				String functionName = expression.functionName;
				VGFunction function= catalog.getVGFunction(functionName);
				ArrayList<Attribute> outputAtts = function.getOutputAtts();
				for(int i = 0; i < outputAtts.size(); i++){
					functionAttTypeMap.put(functionName+"_"+tempTable+"_"+outputAtts.get(i).getName(), subTypeList.get(i));
				}
			}

			// ud function or regular function
			else{
				String functionName = expression.functionName;
				Function function= catalog.getFunction(functionName);
				Attribute outputAtt = function.getOutputAtt();
				functionAttTypeMap.put(functionName+"_"+tempTable+"_"+outputAtt.getName(), subTypeList.get(0));
			}
			return true;
		}
		else
		{
			return false;
		}
		
	}

	
	/*
	 * 2. mathExpression
	 */
	//typechecker for A+B-C+D or A*B/C*D
	public ArrayList<DataType> visitArithmeticExpression(ArithmeticExpression arithmeticExpression) throws Exception
	{
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		ArrayList<DataType> type = new ArrayList<DataType>(operandList.size());
		ArrayList<Integer> operatorList = arithmeticExpression.operatorList;
		
		for(int i = 0; i < operandList.size(); i++)
		{
			ArrayList<DataType> typeList = operandList.get(i).acceptVisitor(this);
			if(typeList == null)
			{
				type.add(null);
				throw new Exception("visitArithmeticExpression wrong!");
			}
			else
			{
				type.add(typeList.get(0));
			}
		}
		
		DataType compatibleType = TypeMachine.checkArithmetic(operatorList, type);				// TO-DO
		
		if(compatibleType == null)
		{
			System.err.println("Arithimetic expressions are not compatible!");
			throw new Exception("Arithimetic expressions are not compatible!");
		}
		
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(compatibleType);
		return resultList;
	}
	
	/*
	 * check column expression(non-Javadoc)
	 * There are two cases: select B from; select A.B from. A can be an table or a subquery
	 */
	public ArrayList<DataType> visitColumnExpression(ColumnExpression columnExpression) throws Exception{
		String table = columnExpression.table;
		String columnName = columnExpression.columnName;
		
		TypeChecker typechecker;
		HashMap<String, TableReference> tempTableReferenceMap;
		HashMap<String, TypeChecker> tempTyperCheckerMap;
		HashMap<String, GeneralFunctionExpression> tempVgFunctionMap;
		
		ArrayList<Attribute> tempList;
		Attribute tempAttribute;
		
		//saving the links between typechecker
		TableReference linkedTableReference = null;
		FromSubquery linkedSubquery = null;
		
		
		GeneralFunctionExpression functionExpression;
		Relation relation;
		View view;
		String tableName;
		
		typechecker = this;
		DataType type = null;
		int k = 0;
		
		if(table == null || table.equals(""))
		{
			
			while(typechecker != null)
			{
				tempTableReferenceMap = typechecker.tableReferenceMap;
				tempTyperCheckerMap = typechecker.typerCheckerMap;
				tempVgFunctionMap = typechecker.vgFunctionMap;
				
				String tableAlias[] = new String[tempTableReferenceMap.size()];
				tableAlias = tempTableReferenceMap.keySet().toArray(tableAlias);
				
				for(int j = 0; j < tableAlias.length; j++)
				{
					TableReference tempTableReference;
					tempTableReference = tempTableReferenceMap.get(tableAlias[j]);
					tableName = tempTableReference.table;
					
					/*
					 * TableReference can reference to table or a vgfunction
					 */
					if(tempVgFunctionMap.containsKey(tableName))
					{
						functionExpression = tempVgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(functionExpression.functionName);
						tempList = vgFunction.getOutputAtts();
						
						for(int i = 0; i < tempList.size(); i++)
						{
							tempAttribute = tempList.get(i);
							if(tempAttribute.getName().equals(columnName))
							{
								linkedTableReference = tempTableReference;
								
								table = tableAlias[j];
								columnExpression.table = table;
							//	type = tempAttribute.getType();
								type = functionAttTypeMap.get(functionExpression.functionName+"_"+tableName+"_"+tempAttribute.getName());
								k++;
								if(k >= 2)
								{
									System.err.println("selected attribute [" + columnName + "] is ambigurous ");
									throw new Exception("selected attribute [" + columnName + "] is ambigurous ");
								}
							}
						}
					}
					else
					{
						relation = catalog.getRelation(tableName);
						if(relation != null)
						{
							tempList = relation.getAttributes();
						}
						else
						{
							view = catalog.getView(tableName);
							tempList = view.getAttributes();
						}
						
						for(int i = 0; i < tempList.size(); i++)
						{
							tempAttribute = tempList.get(i);
							if(tempAttribute.getName().equals(columnName))
							{
								linkedTableReference = tempTableReference;
								
								table = tableAlias[j];
								columnExpression.table = table;
								type = tempAttribute.getType();
								k++;
								if(k >= 2)
								{
									System.err.println("selected attribute [" + columnName + "] is ambigurous ");
									throw new Exception("selected attribute [" + columnName + "] is ambigurous ");
								}
							}
						}
					}
				}
				
				//consider the fromSubquery
				String subqueryAlias[] = new String[tempTyperCheckerMap.size()];
				subqueryAlias = tempTyperCheckerMap.keySet().toArray(subqueryAlias);
				TypeChecker tempChecker;
				
				for(int j = 0; j < subqueryAlias.length; j++)
				{
					tempChecker = tempTyperCheckerMap.get(subqueryAlias[j]);
					HashMap<String, String> tempAttributeMap = tempChecker.getAttributeMap();
					HashMap<String, DataType> tempAttributeTypeMap = tempChecker.getAttributeTypeMap();
					/*
					 * ----------------------------modified by Bamboo-------------------------------
					 */
					HashMap<String, FromSubquery> tempFromSubqueryMap = typechecker.getFromSubqueryMap();
					
					if(tempAttributeMap.containsKey(columnName))
					{
						
						table = tempAttributeMap.get(columnName);
						linkedSubquery = tempFromSubqueryMap.get(subqueryAlias[j]);
						
						columnExpression.table = subqueryAlias[j];
						type = tempAttributeTypeMap.get(columnName);
						
						
						k++;
						if(k >= 2)
						{
							System.err.println("selected attribute [columnName" + "] is ambigurous ");
							throw new Exception("selected attribute [columnName" + "] is ambigurous ");
						}
					}
					/*
					 * -----------------------------------------3.18------------------------------------
					 */
				}
				
				if(k == 1)
				{
					break;
				}
				else if(k == 0)
				{
					typechecker = typechecker.parent;
				}
			}
		}
		else
		{
			//for the table
			while(typechecker != null)
			{
				
				tempTableReferenceMap = typechecker.tableReferenceMap;
				tempTyperCheckerMap = typechecker.typerCheckerMap;
				tempVgFunctionMap = typechecker.vgFunctionMap;
				
				TableReference tempTableReference = tempTableReferenceMap.get(table);
				if(tempTableReference != null)
				{
					tableName = tempTableReference.table;
					if(tempVgFunctionMap.containsKey(tableName))
					{
						functionExpression = tempVgFunctionMap.get(tableName);
						VGFunction vgFunction = catalog.getVGFunction(functionExpression.functionName);
						tempList = vgFunction.getOutputAtts();
						
						for(int i = 0; i < tempList.size(); i++)
						{
							tempAttribute = tempList.get(i);
							if(tempAttribute.getName().equals(columnName))
							{
								linkedTableReference = tempTableReference;
								
								table = tempTableReference.alias;
								columnExpression.table = table;
							//	type = tempAttribute.getType();
								type = functionAttTypeMap.get(functionExpression.functionName+"_"+tableName+"_"+tempAttribute.getName());
								k++;
								if(k >= 2)
								{
									System.err.println("selected attribute [" + columnName + "] is ambigurous ");
									throw new Exception("selected attribute [" + columnName + "] is ambigurous ");
								}
							}
						}
					}
					else
					{
						relation = catalog.getRelation(tableName);
						if(relation != null)
						{
							tempList = relation.getAttributes();
						}
						else
						{
							view = catalog.getView(tableName);
							tempList = view.getAttributes();
						}
						
						for(int i = 0; i < tempList.size(); i++)
						{
							tempAttribute = tempList.get(i);
							if(tempAttribute.getName().equals(columnName))
							{
								linkedTableReference = tempTableReference;
								
								type = tempAttribute.getType();
								k++;
								if(k >= 2)
								{
									System.err.println("selected attribute [" + columnName +  "] is ambigurous ");
									throw new Exception("selected attribute [" + columnName +  "] is ambigurous ");
								}
							}
						}
					}
				}
				
				//for subquery
				TypeChecker tempChecker = tempTyperCheckerMap.get(table);
				if(tempChecker != null)
				{
					HashMap<String, String> tempAttributeMap = tempChecker.getAttributeMap();
					HashMap<String, DataType> tempAttributeTypeMap = tempChecker.getAttributeTypeMap();
					HashMap<String, FromSubquery> tempFromSubqueryMap = tempChecker.getFromSubqueryMap();
					
					if(tempAttributeMap.containsKey(columnName))
					{
						//linkedSubquery = tempFromSubqueryMap.get(columnName);
						linkedSubquery = typechecker.getFromSubqueryMap().get(table);
												
						table = tempAttributeMap.get(columnName);
						type = tempAttributeTypeMap.get(columnName);
						
						k++;
						if(k >= 2)
						{
							System.err.println("selected attribute [" + columnName + "] is ambigurous ");
							throw new Exception("selected attribute [" + columnName + "] is ambigurous ");
						}
					}
				}
				
				if(k == 1)
				{
					break;
				}
				else if(k == 0)
				{
					typechecker = typechecker.parent;
				}
			}
		}
		
		//save the edges between typechecker (blocks)
		if(k == 1)
		{
			if(typechecker != this)
			{
				SQLExpression element;
				
				element = (linkedTableReference != null? linkedTableReference: linkedSubquery);
				
				if(!providedTableReferenceMap.containsKey(typechecker))
				{
					CheckerEdge edge = new CheckerEdge();
					providedTableReferenceMap.put(typechecker, edge);
				}
				
				CheckerEdge edge = providedTableReferenceMap.get(typechecker);
				
				if(!edge.containTableReference(element))
				{
					edge.addTableReference(element);
				}
				
				edge.addColumnExpression(element, columnExpression);
				edgeTypeChecker.put(columnExpression.toString(), typechecker);
				typechecker.addReferenceString(columnExpression.toString());
			}
		}
		//try some alias name in the select clause
		if(k == 0 && outputAliasMap!= null && outputAliasMap.containsKey(columnName))
		{
			ArrayList<MathExpression> tempExpressionList = outputAliasMap.get(columnName);
			MathExpression tempExpression = tempExpressionList.get(0);
			
			if(TypeCheckerHelper.hasAggregate(tempExpression) && columnExpression.isGroupByColumn())
			{
				k = 0;
				System.err.println("Aggregation Alias can not exist in group_by columns!");
				throw new Exception("Aggregation Alias can not exist in group_by columns!");
			}
			else
			{
				k = outputAliasMap.get(columnName).size();
				type = attributeTypeMap.get(columnName);
			}
		}
		
		if(k == 0)
		{
			if(table != null)
				System.err.println("selected attribute [" + table + "." + columnName + "] is not found in any relation!");
			else
				System.err.println("selected attribute [" + columnName + "] is not found in any relation!");
			throw new Exception("selected attribute [" + columnName + "] is not found in any relation!");
		}
		else if(k == 2)
		{
			System.err.println("selected attribute [" + columnName + "] after where clause is ambigurous ");
			throw new Exception("selected attribute [" + columnName + "] after where clause is ambigurous ");	
		}
		
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(type);
		return resultList;
	}

	//check numeric expression
	public ArrayList<DataType> visitNumericExpression(NumericExpression numericExpression) {
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(numericExpression.getType());
		return resultList;
	}
	
	@Override
	public ArrayList<DataType> visitGeneralTableIndexExpression(
			GeneralTableIndex generalTableIndex) {
		
		TypeChecker root = this;
		while(root.parent != null)
		{
			root = root.parent;
		}
		
		if(root instanceof GeneralRandomTableTypeChecker ||
				(root instanceof UnionViewStatementTypeChecker &&
						((UnionViewStatementTypeChecker)root).isGeneralUnionViewTypeChecker())
		  )
		{
			ArrayList<DataType> resultList = new ArrayList<DataType>(2);
			resultList.add(generalTableIndex.getType());
			return resultList;
		}
		else
		{
			System.err.println("General index \"i\" can only exist in the general random table definition");
			throw new RuntimeException("General index \"i\" can only exist in the general random table definition");
		}
	}

	
	//check date expression
	public ArrayList<DataType> visitDateExpression(DateExpression dateExpression) {
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(new StringType());
		return resultList;
	}
	
	//check string expression
	public ArrayList<DataType> visitStringExpression(StringExpression stringExpression) {
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(new StringType());
		return resultList;
	}
	
	/* check asteriskExpression (sum(*))
	 * @see astVisitor.ASTVisitor#visitAsteriskExpression(component.mathExpression.AsteriskExpression)
	 */
	@Override
	public ArrayList<DataType> visitAsteriskExpression(AsteriskExpression asteriskExpression) throws Exception{
		ArrayList<DataType> resultList = new ArrayList<DataType>();
		
		for(int j = 0; j < fromList.size(); j++)
		{
			String tableAlias = fromList.get(j);
			
			String tableName;
			Relation relation;
			ArrayList<Attribute> tempList;
			
			if(tableReferenceMap.containsKey(tableAlias))
			{
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				
				tableName = tempTableReference.table;
				
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
					resultList.add(tempList.get(k).getType());
				}
			}
			//for subquery
			else if(typerCheckerMap.containsKey(tableAlias))
			{
				TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
				ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
				HashMap<String, DataType> outputAttributeTypeMap = tempChecker.getAttributeTypeMap();
				
				for(int k = 0; k < outputAttributeStringList.size(); k++)
				{
					String name = outputAttributeStringList.get(k);
					resultList.add(outputAttributeTypeMap.get(name));
				}
			}
		}
		return resultList;
	}
	
	//check subquery expression (math) type
	public ArrayList<DataType> visitSubqueryExpression(SubqueryExpression subqueryExpression) throws Exception{
		
		/*
		 * Here checkeState guarantees that a subquery has parent checker if it appears in the 
		 * Where clause, Having Clause, With Clause .
		 */
		if(checkState < WHERESTAGE && this.parent == null)
		{
			subqueryExpression.setNeedparent(false);
		}
		else
		{
			subqueryExpression.setNeedparent(true);
		}
		
		SelectStatement statement = subqueryExpression.value;
		
		TypeChecker typeChecker;
		
		if(subqueryExpression.getNeedParent())
		{
			if(checkState < WHERESTAGE)
			{
				typeChecker = new TypeChecker(this.parent, false);
			}
			else
			{
				typeChecker = new TypeChecker(this, false);
			}
		}
		else
		{
			typeChecker = new TypeChecker(null, false);
		}
		
		childTypeCheckerMap.put(subqueryExpression, typeChecker);
		
		boolean subcheck = statement.acceptVisitor(typeChecker);
		subqueryExpression.setTypeChecker(typeChecker);
		HashMap<String, DataType> attributeTypeMap = typeChecker.getAttributeTypeMap();
		ArrayList<String> outputList = typeChecker.getAttributeList();
		if(!subcheck)
		{
			throw new Exception("subquery check wrong!");
		}
		
		if(attributeTypeMap.size() >= 1)
		{
			ArrayList<DataType> result = new ArrayList<DataType>();
			for(int i = 0; i < outputList.size(); i++)
			{
				result.add(attributeTypeMap.get(outputList.get(i)));
			}
			return result;
		}
		else
		{
			return null;
		}
	}
	
	/*
	 * check the type of aggregateExpression(non-Javadoc)
	 * @see astVisitor.ASTVisitor#visitAggregateExpression(component.mathExpression.AggregateExpression)
	 */
	public ArrayList<DataType> visitAggregateExpression(AggregateExpression aggregateExpression)throws Exception
	{
		// just check the subexpression
		MathExpression expression = aggregateExpression.expression;
		int aggType = aggregateExpression.aggType;
		ArrayList<DataType> typeList = null;
		if(expression != null)
		{
			typeList = expression.acceptVisitor(this);
		}
		
		
		if(typeList == null)
		{	
			throw new Exception("aggregate check wrong!");
		}
			
		
		/*
		 * aggregate function should not include aggregate function again, or subquery
		 * Here I create a boolean expression (expression == 1) to use the class CheckSubquery
		 */
		ComparisonPredicate predicate = new ComparisonPredicate(expression, 
				                                                new NumericExpression("0"),
				                                                FinalVariable.EQUALS);
		boolean subcheck = new CheckSubquery(predicate).hasSubqueryInBooleanPredicate();
		if(subcheck)
		{
			throw new Exception("Aggregate has subquery!");
		}
		
		subcheck = CheckAggregate.directAggregateInMathExpression(expression);
		if(subcheck)
		{
			throw new Exception("directAggregateInMathExpression!");
		}

		DataType resultType = TypeMachine.checkAggregate(aggType, typeList);			// TO-DO
		
		// DataType resultType = null;
		// switch(aggType)
		// {
		// 	case FinalVariable.AVG:
		// 	case FinalVariable.VARIANCE:
		// 	case FinalVariable.STDEV:
		// 		 resultType = TypeCheckerHelper.arithmeticCompate(new DoubleType(), typeList.get(0));
		// 		 break;
				
		// 	case FinalVariable.SUM:
		// 		resultType = typeList.get(0);
		// 		break;
				
		// 	case FinalVariable.COUNT:
		// 	case FinalVariable.COUNTALL:
		// 		 resultType = new IntType();
		// 		 break;
				 
		// 	case FinalVariable.MIN:
		// 	case FinalVariable.MAX:
		// 		 resultType = typeList.get(0);
		// 		 break;
		// }
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(resultType);
		return resultList;
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public ArrayList<DataType> visitGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression)
			throws Exception {

		// if this is a VG function, then handle things in a slightly different
		// way
		if (generalFunctionExpression.isVGFunction()) {
			return visitVGFunctionExpression(generalFunctionExpression);
		}

		// if we got here then it means that we are dealing with a "regular" or
		// non-VG function
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		String functionName = generalFunctionExpression.functionName;

		Function function = catalog.getFunction(functionName);
		// if it can not be found in the catalog, then it is wrong!
		if (function == null) {
			System.err.println("General function [" + functionName
					+ "] is not found in the catalog!");
			throw new Exception("General function [" + functionName
					+ "] is not found in the catalog!");
		} else {
			if (functionName.equals("case")) {
				// ArrayList<Attribute> inputAtts = function.getInputAtts();
				//Attribute outputAtt = function.getOutputAtt();

				ArrayList<DataType> typeList = new ArrayList<DataType>();

				for (int i = 0; i < paraList.size(); i++) {
					ArrayList<DataType> subTypeList = paraList.get(i)
							.acceptVisitor(this);

					if (subTypeList == null) {
						System.err
								.println("Typecheck the parameters of Function ["
										+ functionName + "] is wrong!");
						throw new Exception(
								"Typecheck the parameters of Function ["
										+ functionName + "] is wrong!");
					} else {
						for (int j = 0; j < subTypeList.size(); j++) {
							typeList.add(subTypeList.get(j));
						}
					}
				}

				MathExpression firstPara = paraList.get(0);
				if (firstPara instanceof PredicateWrapper) {
					if (paraList.size() % 2 != 1) {
						System.err.println("CASE FORMAT WRONG!");
						throw new Exception("CASE FORMAT WRONG!");
					}

					for (int i = 0; i < paraList.size() - 1; i = i + 2) {
						PredicateWrapper predicateWrapper = (PredicateWrapper) (paraList
								.get(i));
						CheckSubquery checkSubquery = new CheckSubquery(
								predicateWrapper.predicate);
						if (checkSubquery.hasSubqueryInBooleanPredicate()) {
							System.err
									.println("CASE FORMAT does not allow the subquery!");
							throw new Exception(
									"CASE FORMAT does not allow the subquery!");
						}
					}

					/****
					for (int i = 1; i < typeList.size() - 1; i = i + 2) {
						if (TypeCheckerHelper.directTransfer(typeList.get(i),
								typeList.get(1)) <= 0) {
							System.err
									.println("The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " are not compatible!");
							throw new Exception(
									"The type of parameters of Function ["
											+ functionName + "]"
											+ " are not compatible!");
						}

						if (TypeCheckerHelper.directTransfer(typeList.get(i),
								inputAtts.get(1).getType()) <= 0) {
							System.err
									.println("The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " does not match the defined type!");
							throw new Exception(
									"The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " does not match the defined type!");
						}
					}

					if (TypeCheckerHelper.directTransfer(typeList.get(typeList
							.size() - 1), inputAtts.get(1).getType()) <= 0) {
						System.err
								.println("The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
						throw new Exception(
								"The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
					}
					****/

					DataType outputType = TypeMachine.checkUDFunction("case_1", typeList);			// TO-DO

					ArrayList<DataType> resultList = new ArrayList<DataType>();
					// resultList.add(typeList.get(typeList.size() - 1));
					resultList.add(outputType);
					return resultList;
				} else {
					if (paraList.size() % 2 != 0) {
						System.err.println("CASE FORMAT WRONG!");
						throw new Exception("CASE FORMAT WRONG!");
					}

					/****
					if (TypeCheckerHelper.directTransfer(typeList.get(0),
							inputAtts.get(0).getType()) <= 0) {
						System.err
								.println("The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
						throw new Exception(
								"The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");

					}

					for (int i = 1; i < typeList.size() - 1; i++) {
						if (TypeCheckerHelper.directTransfer(typeList.get(i),
								typeList.get((i + 1) % 2 + 1)) <= 0) {
							System.err
									.println("The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " are not compatible!");
							throw new Exception(
									"The type of parameters of Function ["
											+ functionName + "]"
											+ " are not compatible!");
						}

						if (TypeCheckerHelper.directTransfer(typeList.get(i),
								inputAtts.get((i + 1) % 2 + 1).getType()) <= 0) {
							System.err
									.println("The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " does not match the defined type!");
							throw new Exception(
									"The type of parameters of Function ["
											+ functionName
											+ "]"
											+ " does not match the defined type!");
						}
					}

					if (TypeCheckerHelper.directTransfer(typeList.get(typeList
							.size() - 1), inputAtts.get(2).getType()) <= 0) {
						System.err
								.println("The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
						throw new Exception(
								"The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
					}
					****/

					DataType outputType = TypeMachine.checkUDFunction("case_2", typeList);			// TO-DO

					ArrayList<DataType> resultList = new ArrayList<DataType>();
					// resultList.add(typeList.get(typeList.size() - 1));
					resultList.add(outputType);
					return resultList;
				}

			} else {

				/*
				 * Check the attributes if they matches
				 */
				// ArrayList<Attribute> inputAtts = function.getInputAtts();
				// Attribute outputAtt = function.getOutputAtt();

				ArrayList<DataType> typeList = new ArrayList<DataType>();

				for (int i = 0; i < paraList.size(); i++) {
					ArrayList<DataType> subTypeList = paraList.get(i)
							.acceptVisitor(this);

					if (subTypeList == null) {
						System.err
								.println("Typecheck the parameters of Function ["
										+ functionName + "] is wrong!");
						throw new Exception(
								"Typecheck the parameters of Function ["
										+ functionName + "] is wrong!");
					} else {
						for (int j = 0; j < subTypeList.size(); j++) {
							typeList.add(subTypeList.get(j));
						}
					}
				}

				//check it there is repeated name from the parameters.
				/****
				HashSet<String> parameterNameSet = new HashSet<String>();
				for (SubqueryExpression s : childTypeCheckerMap.keySet()) {
					TypeChecker tempChecker = childTypeCheckerMap.get(s);
					ArrayList<String> outputNameList = tempChecker.getAttributeList();
					if (outputNameList != null) {
						for (int i = 0; i < outputNameList.size(); i++) {
							parameterNameSet.add(outputNameList.get(i));
						}
					}
				}

				if (parameterNameSet.size() != typeList.size()) {
					System.err.println("The type of parameters of Function ["
							+ functionName + "]" + " has name contradictions!");
					throw new Exception("The type of parameters of Function ["
							+ functionName + "]" + " has name contradictions!");
				}
				****/

				// first, make sure that the number of parameters match up
				/****
				if (typeList.size() != inputAtts.size()) {
					System.err.println("Found " + typeList.size()
							+ " atts in input; [" + functionName
							+ "] expected " + inputAtts.size());
					throw new Exception("Bad number of input atts to ["
							+ functionName + "]");
				}

				for (int i = 0; i < typeList.size(); i++) {
					if (TypeCheckerHelper.directTransfer(typeList.get(i),
							inputAtts.get(i).getType()) <= 0) {
						System.err
								.println("The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");
						throw new Exception(
								"The type of parameters of Function ["
										+ functionName + "]"
										+ " does not match the defined type!");

					}
				}
				****/

				DataType outputType = TypeMachine.checkUDFunction(functionName, typeList);			// TO-DO

				ArrayList<DataType> resultList = new ArrayList<DataType>();
				// resultList.add(outputAtt.getType());
				resultList.add(outputType);
				return resultList;
			}
		}
	}

	private ArrayList <DataType> visitVGFunctionExpression ( 
                        GeneralFunctionExpression generalFunctionExpression) throws Exception {

                ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
                String functionName = generalFunctionExpression.functionName;

                VGFunction function= catalog.getVGFunction(functionName);
                //if it can not be found in the catalog, then it is wrong!
                if(function == null)
                {
                        System.err.println("VG Function [" + functionName + "] is not found in the catalog!");
                        throw new Exception("VG Function [" + functionName + "] is not found in the catalog!");
                }
                else
                {

                          /*
                           *  Check the attributes if they matches
                           */
                          // ArrayList<Attribute> inputAtts = function.getInputAtts();
                          // ArrayList<Attribute> outputAtts = function.getOutputAtts();

                          ArrayList<DataType> typeList = new ArrayList<DataType>();

                          for(int i = 0; i < paraList.size(); i++)
                          {
                                  ArrayList<DataType> subTypeList = paraList.get(i).acceptVisitor(this);

                                  if(subTypeList == null)
                                  {
                                          System.err.println("Typecheck the parameters of Function [" + functionName + "] is wrong!");
                                          throw new Exception("Typecheck the parameters of Function [" + functionName + "] is wrong!");
                                  }
                                  else
                                  {
                                          for(int j = 0; j < subTypeList.size(); j++)
                                          {
                                                  typeList.add(subTypeList.get(j));
                                          }
                                  }
                          }
                          
                          
                        //check it there is repeated name from the parameters.
          				HashSet<String> parameterNameSet = new HashSet<String>();
          				for (SubqueryExpression s : childTypeCheckerMap.keySet()) {
          					TypeChecker tempChecker = childTypeCheckerMap.get(s);
          					ArrayList<String> outputNameList = tempChecker.getAttributeList();
          					if (outputNameList != null) {
          						for (int i = 0; i < outputNameList.size(); i++) {
          							if(!parameterNameSet.contains(outputNameList.get(i)))
          							{
          								parameterNameSet.add(outputNameList.get(i));
          							}
          							else
          							{
          								System.err.println("The type of parameters of Function ["
          	          							+ functionName + "]" + " has name contradictions!");
          	          					throw new Exception("The type of parameters of Function ["
          	          							+ functionName + "]" + " has name contradictions!");
          							}
          						}
          					}
          				}

						  // first, make sure that the number of parameters match up
          				  /****
						  if (typeList.size () != inputAtts.size ()) {
							System.err.println ("Found " + typeList.size () + " atts in input; [" + functionName + "] expected " +
								inputAtts.size ());
							throw new Exception ("Bad number of input atts to [" + functionName + "]");
						  } 
			  
                          for(int i = 0; i < typeList.size(); i++)
                          {
                                  if(TypeCheckerHelper.directTransfer(typeList.get(i), inputAtts.get(i).getType()) <= 0)
                                  {
                                          System.err.println("The type of parameters of Function [" + functionName + "]" +
                                                        " does not match the defined type!");
                                          throw new Exception("The type of parameters of Function [" + functionName + "]" +
                                                " does not match the defined type!");

                                  }
                          }

                          if(outputAtts.size() == 0)
                          {
                                  System.err.println("No output from Function [" + functionName + "]!");
                                  throw new Exception("No output from Function [" + functionName + "]!");
                          }
                          else
                          {
                                  ArrayList<DataType> resultList = new ArrayList<DataType>();
                                  for(int i = 0; i < outputAtts.size(); i++)
                                  {
                                          resultList.add(outputAtts.get(i).getType());
                                  }
                                  return resultList;
                          }
                          ****/

                          ArrayList <DataType> outputTypes = TypeMachine.checkVGFunction(functionName, typeList);
                          return outputTypes;
		}

	}

	
	/*
	 * check the set expression(non-Javadoc)
	 * All the elements in the set should have the same type.
	 */
	public ArrayList<DataType> visitSetExpression(SetExpression setExpression) throws Exception
	{
		ArrayList<MathExpression> expressionList = setExpression.expressionList;
		if(expressionList == null || expressionList.size() == 0)
			return null;
		
		ArrayList<DataType> subType = new ArrayList<DataType>(expressionList.size());
		for(int i = 0; i < expressionList.size(); i++)
		{
			ArrayList<DataType> subTypeList = expressionList.get(i).acceptVisitor(this);
			
			if(subTypeList == null)
			{
				System.err.println("The set expression is wrong in type checker!");
				throw new Exception("The set expression is wrong in type checker!");
			}
			else
			{
				subType.add(subTypeList.get(0));
			}
		}
		
		DataType resultType = subType.get(0);
		for(int i = 1; i < expressionList.size(); i++)
		{
			if(!resultType.getClass().equals(subType.get(i).getClass()))				// TO-DO
			{
				System.err.println("The sub types in the set are not the same!");
				throw new Exception("The sub types in the set are not the same!");
			}	
		}
		
		ArrayList<DataType> resultList = new ArrayList<DataType>(2);
		resultList.add(resultType);
		return resultList;
	}
	
	/* (non-Javadoc)
	 * @see parser.astVisitor.ASTVisitor#visitPredicateWrapper(parser.expression.mathExpression.PredicateWrapper)
	 */
	@Override
	public ArrayList<DataType> visitPredicateWrapper(
			PredicateWrapper predicateWrapper) throws Exception
	{
		BooleanPredicate predicate = predicateWrapper.predicate;
		boolean subCheck = predicate.acceptVisitor(this);
		if(!subCheck)
		{
			System.err.println("predicate check is wrong in \"Case\"!");
			throw new Exception("predicate check is wrong in \"Case\"!");
		}
		else
		{
			ArrayList<DataType> resultList = new ArrayList<DataType>(2);
			resultList.add(new IntType());
			return resultList;
		}
	}

	
	/*
	 * predicate expression
	 */
	
	//and predicate
	public boolean visitAndPredicate(AndPredicate andPredicate)throws Exception
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		boolean subCheck;
		for(int i = 0 ; i < andList.size(); i++)
		{
			if(andList.get(i) == null)
			{
				System.err.println("Type check wrong in the andPredicate");
				return false;
			}
			subCheck = andList.get(i).acceptVisitor(this);
			if(!subCheck)
			{
				System.err.println("Type check wrong in the andPredicate");
				return false;
			}
		}
		return true;
		
	}
	
	//or predicate
	public boolean visitOrPredicate(OrPredicate orPredicate) throws Exception
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		boolean subCheck;
		for(int i = 0; i < orList.size(); i++)
		{
			subCheck = orList.get(i).acceptVisitor(this);
			if(!subCheck)
			{
				System.err.println("Type check wrong in the orPredicate");
				return false;
			}
		}
		return true;
	}
	
	//atom predicate {true, false}
	public boolean visitAtomPredicate(AtomPredicate atomPredicate) {
		return true;
	}
	
	//Not predicate
	public boolean visitNotPredicate(NotPredicate notPredicate) throws Exception
	{
		BooleanPredicate predicate = notPredicate.predicate;
		return predicate.acceptVisitor(this);
	}
	
	//Between Predicate
	public boolean visitBetweenPredicate(BetweenPredicate betweenPredicate) throws Exception
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		DataType middleType = null, lowerType = null, upperType = null;
		ArrayList<DataType> expressionTypeList = expression.acceptVisitor(this);
		ArrayList<DataType> lowerExpressionTypeList = lowerExpression.acceptVisitor(this);
		ArrayList<DataType> upperExpressionTypeList = upperExpression.acceptVisitor(this);
		
		if(expressionTypeList != null)
			middleType = expressionTypeList.get(0);
		
		if(expressionTypeList != null)
			lowerType = lowerExpressionTypeList.get(0);
		
		if(expressionTypeList != null)
			upperType = upperExpressionTypeList.get(0);
		
		if(middleType == null || lowerType == null || upperType == null)
			return false;
		
		if(middleType.parameterize(lowerType, new HashMap <String, Integer>()) == Compatibility.BAD || 
				middleType.parameterize(upperType, new HashMap <String, Integer>()) == Compatibility.BAD)
			return false;
		
		return true;
	}

	//comparison predicate
	public boolean visitComparisonPredicate(ComparisonPredicate comparisonPredicate) throws Exception{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		ArrayList<DataType> leftExpressionTypeList = leftExpression.acceptVisitor(this);
		ArrayList<DataType> rightExpressionTypeList = rightExpression.acceptVisitor(this);
		
		DataType leftType = null, rightType = null;
		if(leftExpressionTypeList != null)
			leftType = leftExpressionTypeList.get(0);
		
		if(rightExpressionTypeList != null)
			rightType = rightExpressionTypeList.get(0);
		
		if(leftType == null || rightType == null)
			return false;
		
		if(leftType.parameterize(rightType, new HashMap <String, Integer>()) == Compatibility.BAD)
			return false;
		else
			return true;
	}

	//like predicate
	public boolean visitLikePredicate(LikePredicate likePredicate) throws Exception{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		ArrayList<DataType> valueExpressionTypeList = valueExpression.acceptVisitor(this);
		ArrayList<DataType> patternExpressionTypeList = patternExpression.acceptVisitor(this);
		
		DataType valueType = null, patternType = null;
		if(valueExpressionTypeList != null)
			valueType = valueExpressionTypeList.get(0);
		
		if(patternExpressionTypeList != null)
			patternType = patternExpressionTypeList.get(0);
		
		if(valueType == null || patternType == null)
			return false;
		
		if(valueType.parameterize(patternType, new HashMap <String, Integer>()) == Compatibility.BAD)
			return false;
		else
			return true;
	}

	//exist predicate
	public boolean visitExistPredicate(ExistPredicate existPredicate) throws Exception
	{
		MathExpression expression = existPredicate.existExpression;
		ArrayList<DataType> expressionTypeList = expression.acceptVisitor(this);
		
		DataType type = null;
		
		if(expressionTypeList != null)
			type = expressionTypeList.get(0);
		
		if(type != null)
			return true;
		else
			return false;
	}
	
	//in predicate, only support the single element
	public boolean visitInExpression(InPredicate inPredicate) throws Exception{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		ArrayList<DataType> expressionTypeList = expression.acceptVisitor(this);
		ArrayList<DataType> setExpressionTypeList = setExpression.acceptVisitor(this);
		
		DataType elementType = null, setType = null;
		
		if(expressionTypeList != null)
			elementType = expressionTypeList.get(0);
		
		if(setExpressionTypeList != null)
			setType = setExpressionTypeList.get(0);
		
		if(elementType == null || setType == null)
		{
			return false;
		}
		
		if(elementType.parameterize(setType, new HashMap <String, Integer>()) != Compatibility.BAD)
			return true;
		else
			return false;
	}

	

	
	
	
	/*
	 * For getting the attributes of subquery
	 */
	public HashMap<String, TableReference> getTableReferenceMap() {
		return tableReferenceMap;
	}

	public void setTableReferenceMap(
			HashMap<String, TableReference> tableReferenceMap) {
		this.tableReferenceMap = tableReferenceMap;
	}

	public HashMap<String, FromSubquery> getFromSubqueryMap() {
		return fromSubqueryMap;
	}

	public void setFromSubqueryMap(HashMap<String, FromSubquery> fromSubqueryMap) {
		this.fromSubqueryMap = fromSubqueryMap;
	}

	public HashMap<String, String> getAttributeMap() {
		return attributeTableAliasMap;
	}

	public void setAttributeMap(HashMap<String, String> attributeMap) {
		this.attributeTableAliasMap = attributeMap;
	}	

	public HashMap<String, DataType> getAttributeTypeMap() {
		return attributeTypeMap;
	}

	public void setAttributeTypeMap(HashMap<String, DataType> attributeTypeMap) {
		this.attributeTypeMap = attributeTypeMap;
	}
	
	
	
	
	//for the general select statement
	private int checkDuplicateOutputForSelect(ArrayList<SQLExpression> selectList) throws Exception
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
		
		return errorNum;
	}
	
	/*
	 * return the superset of ColumnExpressions AllFromTable
	 */
	public ArrayList<ColumnExpression> getColumnFromAllFromTable(AllFromTable table) throws Exception
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		String tableAlias = table.table;
		TableReference tempTableReference = tableReferenceMap.get(tableAlias);
		//if the tableAlias is from a table reference.
		if(tempTableReference != null)
		{
			String tableName = tempTableReference.table;
			
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
				ColumnExpression column = new ColumnExpression(tableAlias, name);
				resultList.add(column);
			}
		}
		//if the table alias is from a subquery in from clause.
		else
		{
			TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
			ArrayList<String> outputAttributeNameList = tempChecker.getAttributeList();
			
			for(int j = 0; j < outputAttributeNameList.size(); j++)
			{
				String name = outputAttributeNameList.get(j);
				ColumnExpression column = new ColumnExpression(tableAlias, name);
				resultList.add(column);
			}	
		}
		return resultList;
	}
	
	
	/*
	 * return the superset of all the ColumnExpression in the from clause
	 */
	public ArrayList<ColumnExpression> getColumnFromAsteriskTable()throws Exception
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		for(int j = 0; j < fromList.size(); j++)
		{
			String tableAlias = fromList.get(j);
			
			String tableName;
			Relation relation;
			ArrayList<Attribute> tempList;
			
			if(tableReferenceMap.containsKey(tableAlias))
			{
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				
				tableName = tempTableReference.table;
				
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
					ColumnExpression column = new ColumnExpression(tableAlias, name);
					resultList.add(column);
				}
			}
			//for subquery
			else if(typerCheckerMap.containsKey(tableAlias))
			{
				TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
				ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
				
				for(int k = 0; k < outputAttributeStringList.size(); k++)
				{
					String name = outputAttributeStringList.get(k);
					ColumnExpression column = new ColumnExpression(tableAlias, name);
					resultList.add(column);
				}
			}
		}
		
		return resultList;
	}
	
	//for the general select statement
	public void fillInOutputAttributeForSelect(ArrayList<SQLExpression> selectList, ArrayList<String> fromList) throws Exception
	{
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			//select * from ..
			if(selectElement instanceof AsteriskTable)
			{
				for(int j = 0; j < fromList.size(); j++)
				{
					String tableAlias = fromList.get(j);
					
					String tableName;
					Relation relation;
					ArrayList<Attribute> tempList;
					
					if(tableReferenceMap.containsKey(tableAlias))
					{
						TableReference tempTableReference = tableReferenceMap.get(tableAlias);
						
						tableName = tempTableReference.table;
						
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
					//for subquery
					else if(typerCheckerMap.containsKey(tableAlias))
					{
						TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
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
				String tableAlias = ((AllFromTable) selectElement).table;
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				//if the tableAlias is from a table reference.
				if(tempTableReference != null)
				{
					String tableName = tempTableReference.table;
					
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
				//if the table alias is from a subquery in from clause.
				else
				{
					TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
					ArrayList<String> outputAttributeNameList = tempChecker.getAttributeList();
					HashMap<String, DataType> outputAttributeTypeMap = tempChecker.getAttributeTypeMap();
					
					for(int j = 0; j < outputAttributeNameList.size(); j++)
					{
						String name = outputAttributeNameList.get(j);
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

	
	public ArrayList<String> getFromList() {
		return fromList;
	}

	public void setFromList(ArrayList<String> fromList) {
		this.fromList = fromList;
	}

	public ArrayList<String> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(ArrayList<String> attributeList) {
		this.attributeList = attributeList;
	}

	public Expression getStatement() {
		return statement;
	}

	public void setStatement(Expression statement) {
		this.statement = statement;
	}
	
	
	public String getIdentifierName() {
		return identifierName;
	}

	public void setIdentifierName(String identifierName) {
		this.identifierName = identifierName;
	}

	
	
	public HashMap<TypeChecker, CheckerEdge> getProvidedTableReferenceMap() {
		return providedTableReferenceMap;
	}

	public void setProvidedTableReferenceMap(
			HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap) {
		this.providedTableReferenceMap = providedTableReferenceMap;
	}

	public HashMap<SubqueryExpression, TypeChecker> getChildTypeCheckerMap() {
		return childTypeCheckerMap;
	}
	
	public TypeChecker getParent() {
		return parent;
	}

	public void setParent(TypeChecker parent) {
		this.parent = parent;
	}

	public void setChildTypeCheckerMap(
			HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap) {
		this.childTypeCheckerMap = childTypeCheckerMap;
	}

	public HashMap<String, GeneralFunctionExpression> getVgFunctionMap() {
		return vgFunctionMap;
	}

	public void setVgFunctionMap(
			HashMap<String, GeneralFunctionExpression> vgFunctionMap) {
		this.vgFunctionMap = vgFunctionMap;
	}

	public HashMap<String, TypeChecker> getTyperCheckerMap() {
		return typerCheckerMap;
	}

	public void setTyperCheckerMap(HashMap<String, TypeChecker> typerCheckerMap) {
		this.typerCheckerMap = typerCheckerMap;
	}

	public boolean isAllowDuplicatedAttributeAlias() {
		return allowDuplicatedAttributeAlias;
	}

	public void setAllowDuplicatedAttributeAlias(
			boolean allowDuplicatedAttributeAlias) {
		this.allowDuplicatedAttributeAlias = allowDuplicatedAttributeAlias;
	}
	
	public void addReferenceString(String providingReference)
	{
		providedReferenceSet.add(providingReference);
	}
	
	public HashSet<String> getProvidedReferenceSet() {
		return providedReferenceSet;
	}

	public void setProvidedReferenceSet(HashSet<String> providedReferenceSet) {
		this.providedReferenceSet = providedReferenceSet;
	}
	/**
	 * @return the edgeTypeChecker
	 */
	public HashMap<String, TypeChecker> getEdgeTypeChecker() {
		return edgeTypeChecker;
	}

	/**
	 * @param edgeTypeChecker the edgeTypeChecker to set
	 */
	public void setEdgeTypeChecker(HashMap<String, TypeChecker> edgeTypeChecker) {
		this.edgeTypeChecker = edgeTypeChecker;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.astVisitor.ASTVisitor#visitAttributeElement(mcdb.compiler.parser.expression.sqlExpression.AttributeElement)
	 */
	@Override
	public boolean visitAttributeElement(AttributeDefineElement attributeElement){
		String attributeName = attributeElement.attributeName;
		String typeString  = attributeElement.typeString;
		
		
		boolean subCheck =  true;

		try {
			if (attributeElement.getType() == null)
				subCheck = false;
		}
		catch(Exception e)
		{
			System.err.println ("Attribute[" + attributeName +"] has wrong type size!");
		}

		// /*
		//  * 1. Check for typeString: if the typeString is valid;
		//  */
		// if(!typeString.equals("integer") &&											// TO-DO
		// 		!typeString.equals("double") &&
		// 		!typeString.equals("char"))
		// {
		// 	subCheck = false;
		// 	System.err.println("I did not recognize attribute type " + typeString);
		// }
		
		// /*
		//  * 2. Check for typeSize
		//  */
		// try
		// {
		// 	if(!typeString.equals("integer") && !typeString.equals("double"))
		// 	{
		// 		int size = Integer.parseInt(typeSize);
		// 		if(size <= 0)
		// 		{
		// 			subCheck = false;
		// 			System.err.println("The attribute size for [" + attributeName + " should be positive!");
		// 		}
		// 	}
		// 	else
		// 	{
		// 		if(typeSize != null)
		// 		{
		// 			subCheck = false;
		// 			System.err.println("The attribute size for [" + attributeName + " should not be set!");
		// 		}
		// 	}
		// }
		// catch(Exception e)
		// {
		// 	System.err.println ("Processing att " + attributeName + " found a type size of " + typeSize + "; expected an int.");
		// 	subCheck = false;
		// }
		
		return subCheck;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.astVisitor.ASTVisitor#visitForeignKey(mcdb.compiler.parser.expression.sqlExpression.ForeignKey)
	 */
	@Override
	public boolean visitForeignKey(ForeignKeyElement foreignKey)throws Exception
	{
		/*
		 * Check if the referenced relation and attribute exist.
		 */
		String referencedRelation  = foreignKey.referencedRelation;
		String referencedAttribute  = foreignKey.referencedAttribute;
		boolean subCheck = true;
		Relation relation = catalog.getRelation(referencedRelation);
		
		if(relation == null)
		{
			System.err.println("The referencedRelation [" +referencedRelation+"] does not exists!" );
			subCheck = false;
		}
		else
		{
			ArrayList<Attribute> attributeList = relation.getAttributes();
			Attribute attribute;
			boolean hasAttribute = false;
			
			for(int i = 0; i < attributeList.size(); i++)
			{
				attribute = attributeList.get(i);
				if(referencedAttribute.equals(attribute.getName()))
				{
					hasAttribute = true;
					break;
				}
			}
			
			if(!hasAttribute)
			{
				System.err.println("The foreign key [" + referencedAttribute+"] does not exist!");
				subCheck = false;
			}
		}
		
		return subCheck;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.astVisitor.ASTVisitor#visitPrimaryKey(mcdb.compiler.parser.expression.sqlExpression.PrimaryKey)
	 */
	@Override
	public boolean visitPrimaryKey(PrimaryKeyElement primaryKey) 
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.astVisitor.ASTVisitor#visitDropElement(mcdb.compiler.parser.expression.sqlExpression.DropElement)
	 */
	@Override
	public boolean visitDropElement(DropElement dropElement)throws Exception {
		String objectName = dropElement.objectName;
		int type = dropElement.type;
		boolean subCheck = true;
		Relation relation;
		View view;
		VGFunction vg;
		Function fc;
		
		
		TableReference tableReference;
		
		switch(type)
		{
			case DropElement.TABLEORCOMMON_RANDOM_TABLE:
			case DropElement.CONSTANT_INDEX_TABLE:
			case DropElement.GENERAL_INDEX_TABLE:
				
				//check the name rule. We use the checkValidTableIdentifier function to check that. 
				if(type == DropElement.TABLEORCOMMON_RANDOM_TABLE)
				{
					tableReference = new TableReference(objectName, 
														objectName, 
														null,
														TableReference.COMMON_TABLE);
				}
				else if(type == DropElement.CONSTANT_INDEX_TABLE)
				{
					tableReference = new TableReference(objectName, 
														objectName, 
														null,
														TableReference.CONSTANT_INDEX_TABLE);
				}
				else
				{
					tableReference = new TableReference(objectName, 
														objectName, 
														null,
														TableReference.GENERAL_INDEX_TABLE);
				}
				
				if(!this.checkValidTableIdentifier(tableReference))
				{
					return false;
				}
				
				//check the catlog
				relation = catalog.getRelation(objectName);
				view = catalog.getView(objectName);
				
				if(relation == null && view ==null)
				{
					subCheck = false;
					if(type == DropElement.TABLEORCOMMON_RANDOM_TABLE)
					{
						System.err.println("Entity [" + objectName +"] does not exist!");
					}
					else
					{
						int start = objectName.lastIndexOf("_");
						String outputObjectName = objectName.substring(0, start) + "[" +
								objectName.substring(start + 1, objectName.length()) + "]";
						System.err.println("Entity [" + outputObjectName +"] does not exist!");
					}
				}
				else if(relation != null && view != null)
				{
					subCheck = false;
					
					if(type == DropElement.TABLEORCOMMON_RANDOM_TABLE)
					{
						System.err.println("Wrong type of table [" + objectName +"]: both table and" +
								"random table exist!");
					}
					else
					{
						int start = objectName.lastIndexOf("_");
						String outputObjectName = objectName.substring(0, start) + "[" +
								objectName.substring(start + 1, objectName.length()) + "]";
						
						System.err.println("Wrong type of table [" + outputObjectName +"]: both table and" +
								"random table exist!");
					}
					
					
				}
				else if(relation != null && view == null)
				{
					if(type ==  DropElement.CONSTANT_INDEX_TABLE
			          || type == DropElement.GENERAL_INDEX_TABLE)
					{
						subCheck = false;
						int start = objectName.lastIndexOf("_");
						String outputObjectName = objectName.substring(0, start) + "[" +
								objectName.substring(start + 1, objectName.length()) + "]";
						
						System.err.println("Table [" + outputObjectName +"] is a Simulation random table, but we find that it is a relation!");
					}
					
					boolean hasReferencingTable = catalog.hasReferencingTable(objectName);
					if(hasReferencingTable)
					{
						subCheck = false;
						System.err.println("Table [" + objectName +"] has referencing tables!");
					}
				}
				else
				{
					int objectType = catalog.getOjbectType(objectName);					
					if(objectType == DataAccess.OBJ_VIEW)
					{
						subCheck = false;
						System.err.println("Wrong type of table [" + objectName +"]: it is a view!");
					}
					else if(objectType == DataAccess.OBJ_UNION_VIEW)
					{
						subCheck = false;
						System.err.println("Wrong type of table [" + objectName +"]: it is a union view!");
					}
					else if (objectType == DataAccess.OBJ_RANDRELATION) {
						dropElement.setRandom(true);
					}
				}
				break;
				
			case DropElement.VIEW:
				view = catalog.getView(objectName);
				if(view == null)
				{
					subCheck = false;
					System.err.println("Wrong type of view [" + objectName +"]: no view exists!");
				}
				else
				{
					int objectType = catalog.getOjbectType(objectName);
					if(objectType == DataAccess.OBJ_RANDRELATION)
					{
						subCheck = false;
						System.err.println("Wrong type of view [" + objectName +"]: it is a random table!");
					}
				}
				break;
				
			case DropElement.UNION_VIEW:
				view = catalog.getView(objectName);
				if(view == null)
				{
					subCheck = false;
					System.err.println("Wrong type of union view [" + objectName +"]: no union view exists!");
				}
				else
				{
					int objectType = catalog.getOjbectType(objectName);
					if(objectType != DataAccess.OBJ_UNION_VIEW)
					{
						subCheck = false;
						System.err.println("Wrong type of view [" + objectName +"]: it is not a union view!");
					}
				}
				break;
				
				
			case DropElement.VGFUNC:
				vg = catalog.getVGFunction(objectName);
				if(vg == null)
				{
					subCheck = false;
					System.err.println("Wrong type of VGFunction [" + objectName +"]: no VGFunction exists!");
				}
				break;
			
			case DropElement.FUNC:
				fc = catalog.getFunction(objectName);
				if(fc == null)
				{
					subCheck = false;
					System.err.println("Wrong type of function [" + objectName +"]: no function exists!");
				}
				break;
				
			case DropElement.ARRAY_CONSTANT_INDEX_TABLE:
				int start = objectName.lastIndexOf("_");
				if(start < 0)
				{
					throw new RuntimeException("There is exception in droping the array index table");
				}
				
				String suffix = objectName.substring(start+1, objectName.length());
				String realTableName = objectName.substring(0, start);
				
				if(suffix != null)
				{
					start = suffix.indexOf("..");
					String lowerBound = suffix.substring(0, start);
					String upBound = suffix.substring(start+2, suffix.length());
					
					try
					{
						int low = Integer.parseInt(lowerBound);
						int up = Integer.parseInt(upBound);
						
						for(int i = low; i <= up; i++)
						{
							//check the real constant random table iteratively.
							String tempOjbectName = realTableName + "_" + i;
							
							tableReference = new TableReference(tempOjbectName, 
									tempOjbectName, 
									null,
									TableReference.CONSTANT_INDEX_TABLE);
							
							if(!this.checkValidTableIdentifier(tableReference))
							{
								return false;
							}
							
							
							//check the catlog
							relation = catalog.getRelation(tempOjbectName);
							view = catalog.getView(tempOjbectName);
							start = tempOjbectName.lastIndexOf("_");
							String outputObjectName = tempOjbectName.substring(0, start) + "[" +
									tempOjbectName.substring(start + 1, tempOjbectName.length()) + "]";
							
							if(relation == null && view ==null)
							{
								subCheck = false;
								System.err.println("Entity [" + outputObjectName +"] does not exist!");
							}
							else if(relation != null && view != null)
							{
								subCheck = false;
								System.err.println("Wrong type of table [" + outputObjectName +"]: both table and" +
											"random table exist!");
								
							}
							else if(relation != null && view == null)
							{
								subCheck = false;
								System.err.println("Table [" + outputObjectName +"] is a Simulation random table, but we find that it is a relation!");
							}
							else
							{
								int objectType = catalog.getOjbectType(tempOjbectName);					
								if(objectType == DataAccess.OBJ_VIEW)
								{
									subCheck = false;
									System.err.println("Wrong type of table [" + outputObjectName +"]: it is a view!");
								}
								else if (objectType == DataAccess.OBJ_RANDRELATION) {
									dropElement.setRandom(true);
								}
							}
						}
					}
					catch(Exception e)
					{
						throw new RuntimeException("There is exception in droping the array index table");
					}
				}
					
				break;
				
			default:
				subCheck = false;
				System.err.println("Wrong type of object [" + objectName +"]");
				break;
		}
		
		return subCheck;
	}
	
	public boolean checkValidTableIdentifier(TableReference tableReference)
	{
		/*
		// commented out by Chris... using MC tables does not work if you have this check!
                String table = tableReference.table;
		String realTableName;
		if(tableReference.getTableInferenceType() == TableReference.COMMON_TABLE)
		{
			realTableName = table;
		}
		else
		{
			int start = table.lastIndexOf("_");
			String outputObjectName = table.substring(0, start);
			realTableName = outputObjectName;
		}
		
		if(realTableName.endsWith("_i"))
		{
			System.err.println("Schema [" + realTableName + "] ends with \"_i\", which is not allowed!");
			return false;
		}
		else
		{
			int start = realTableName.lastIndexOf("_");
			if(start >= 0)
			{
				String suffix = realTableName.substring(start+1, realTableName.length());
				if(suffix != null)
				{
					boolean valid = false;
					for(int i = 0; i < suffix.length(); i++)
					{
						if(suffix.charAt(i) < '0' || suffix.charAt(i) > '9')
						{
							valid = true;
							break;
						}
					}
					
					if(!valid)
					{
						System.err.println("Schema [" + realTableName + "] ends with \"_<number>\", which is not allowed!");
						return false;
					}
				}
			}
		}*/
		
		return true;
	}
	
	//--------------------------- suppor the SimSQl query -----------------------------------
	public ArrayList<String> getIndexedRandomTableList()
	{
		/*
		 * The following code is used to support the simSQL query.
		 */
		HashSet<String> dependedSet = new HashSet<String>();
		TypeChecker currentChecker = this;
		ConcurrentLinkedQueue<TypeChecker> queue = new ConcurrentLinkedQueue<TypeChecker>();
		HashMap<String, TypeChecker> typerCheckerMap;
		HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap;
		HashMap<String, TableReference> tableReferenceMap;
		TableReference tempReference;
		
		queue.add(currentChecker);
		
		while(!queue.isEmpty())
		{
			currentChecker = queue.poll();
			tableReferenceMap = currentChecker.getTableReferenceMap();
			for(String s: tableReferenceMap.keySet())
			{
				tempReference = tableReferenceMap.get(s);
				if(tempReference.isConstantRandomTable() || tempReference.isGeneralIndexTable())
				{
					dependedSet.add(tempReference.getTable());
				}
			}
			
			typerCheckerMap = currentChecker.getTyperCheckerMap();
			childTypeCheckerMap = currentChecker.getChildTypeCheckerMap();
			
			for(String s: typerCheckerMap.keySet())
			{
				queue.add(typerCheckerMap.get(s));
			}
			
			for(SubqueryExpression s: childTypeCheckerMap.keySet())
			{
				queue.add(childTypeCheckerMap.get(s));
			}
		}
		
		//now dependedSet contains all the tables on which this table depends.
		ArrayList<String> dependedRandomTableList = new ArrayList<String>();
		dependedRandomTableList.addAll(dependedSet);
		
		return dependedRandomTableList;
	}

	//-----------------------------end -------------------------------------------------------
}
