

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
package simsql.compiler; // package mcdb.compiler.parser.astVisitor.rule;

import java.util.ArrayList;
import java.util.HashMap;
import simsql.compiler.TypeChecker;

// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.Relation;
// import mcdb.catalog.View;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.boolExpression.AndPredicate;
// import mcdb.compiler.parser.expression.boolExpression.BetweenPredicate;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;
// import mcdb.compiler.parser.expression.boolExpression.ComparisonPredicate;
// import mcdb.compiler.parser.expression.boolExpression.ExistPredicate;
// import mcdb.compiler.parser.expression.boolExpression.InPredicate;
// import mcdb.compiler.parser.expression.boolExpression.LikePredicate;
// import mcdb.compiler.parser.expression.boolExpression.NotPredicate;
// import mcdb.compiler.parser.expression.boolExpression.OrPredicate;
// import mcdb.compiler.parser.expression.mathExpression.AggregateExpression;
// import mcdb.compiler.parser.expression.mathExpression.ArithmeticExpression;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.mathExpression.GeneralFunctionExpression;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;
// import mcdb.compiler.parser.expression.mathExpression.PredicateWrapper;
// import mcdb.compiler.parser.expression.mathExpression.SetExpression;
// import mcdb.compiler.parser.expression.mathExpression.SubqueryExpression;
// import mcdb.compiler.parser.expression.sqlExpression.AllFromTable;
// import mcdb.compiler.parser.expression.sqlExpression.AsteriskTable;
// import mcdb.compiler.parser.expression.sqlExpression.DerivedColumn;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlExpression.SelectStatement;
// import mcdb.compiler.parser.expression.sqlExpression.TableReference;
// import mcdb.compiler.parser.expression.util.CheckAggregate;


/**
 * @author Bamboo
 * The purpose of this class is to check some rule that between select clause and groupby 
 * clause:
 * 1. All the column in the groupby clause should be
 *    1) table.attribute or 2) alias appears in the select statement.
 *    The embedded column of groupby clause includes:
 *     1) table.attribute 2) all the columns in the alias appearing in the select statement other than
 *     Aggregate Expression.
 *     
 * 2. If the column appearing in the groupby clause is from select element, then
 *    this select element should not include aggregateExpression 
 *    ( 
 *      It is implemented in the typechecker. It is difficult to check whether the column 
 *    	corresponds to an attribute of tableReference or the alias
 *    	in the select clause
 *    ). 
 *    
 *    Other than that, the element in the select clause follows (implemented here):
 *    1. Alias name in the groupby clause
 *    2. Column in the groupby clause
 *    3. Aggregate Expression on any MathExpression
 */
public class GroupbyRule {
	private SelectStatement selectStatement;
	private TypeChecker typeChecker;
	
	public GroupbyRule(SelectStatement selectStatement,
					   TypeChecker typeChecker)
	{
		this.selectStatement = selectStatement;
		this.typeChecker = typeChecker;
	}
	
	public boolean checkGroupbyRule()throws Exception
	{		
		ArrayList<ColumnExpression> groupByClauseList = selectStatement.groupByClauseList;
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		
		ArrayList<ColumnExpression> columnInGroupList;
		ArrayList<String> aliasInGroupbyList;
		
		if(groupByClauseList != null)
		{
			columnInGroupList = columnInGroupby(groupByClauseList);
			aliasInGroupbyList = aliasInGroupby(groupByClauseList);
		}
		else
		{
			columnInGroupList = new ArrayList<ColumnExpression>();
			aliasInGroupbyList = new ArrayList<String>();
		}
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression expression = selectList.get(i);
			ArrayList<ColumnExpression> tempList = null;
			
			if(expression instanceof AllFromTable)
			{
				tempList = getColumnFromAllFromTable((AllFromTable)expression);
			}
			else if(expression instanceof AsteriskTable)
			{
				tempList = getColumnFromAsteriskTable((AsteriskTable)expression);
			}
			else if(expression instanceof DerivedColumn)
			{
				String name = ((DerivedColumn) expression).alias;
				MathExpression valueExpression = ((DerivedColumn) expression).expression;
				if(aliasInGroupbyList.contains(name))
					continue;
				else
				{
					tempList = new ArrayList<ColumnExpression>();
					getColumnFromMathExpression(tempList, valueExpression);
				}
			}
			
			HashMap<String, TypeChecker> edgeMap = typeChecker.getEdgeTypeChecker();
			tempList = subtractSet(tempList, edgeMap);
			
			if(!subSet(tempList, columnInGroupList))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * @param tempList
	 * @param keys
	 * @return
	 */
	private ArrayList<ColumnExpression> subtractSet(
			ArrayList<ColumnExpression> tempList, HashMap<String, TypeChecker> edgeMap) {
		for(int i = 0; i < tempList.size(); i++)
		{
			ColumnExpression expression = tempList.get(i);
			if(edgeMap.containsKey(expression.toString()))
			{
				tempList.remove(i);
				i--;
			}
		}
		return tempList;
	}

	//rule when groupby list is empty
	public boolean checkEmptyGroupbyRule()throws Exception
	{		
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		boolean hasAggregate = false;
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression expression = selectList.get(i);
			
			if(expression instanceof DerivedColumn)
			{
				String name = ((DerivedColumn) expression).alias;
				MathExpression valueExpression = ((DerivedColumn) expression).expression;
				
				if(CheckAggregate.directAggregateInMathExpression(valueExpression))
				{
					hasAggregate = true;
					break;
				}
			}
		}
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression expression = selectList.get(i);
			ArrayList<ColumnExpression> tempList = null;
			
			if(expression instanceof AllFromTable)
			{
				tempList = getColumnFromAllFromTable((AllFromTable)expression);
			}
			else if(expression instanceof AsteriskTable)
			{
				tempList = getColumnFromAsteriskTable((AsteriskTable)expression);
			}
			else if(expression instanceof DerivedColumn)
			{
				String name = ((DerivedColumn) expression).alias;
				MathExpression valueExpression = ((DerivedColumn) expression).expression;
				tempList = new ArrayList<ColumnExpression>();
				getColumnFromMathExpression(tempList, valueExpression);
			}
			
			if(tempList != null && tempList.size() > 0 && hasAggregate)
			{
				System.err.println("Aggregate expression in selectList requires group by lists for some attributes");
				return false;
			}
		}
		
		return true;
	}
	
	public ArrayList<ColumnExpression> columnInGroupby(
			ArrayList<ColumnExpression> groupByClauseList)
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		HashMap<String, ArrayList<MathExpression>> 
		                  outputAliasMap = typeChecker.outputAliasMap;
		
		for(int i = 0; i < groupByClauseList.size(); i++)
		{
			ColumnExpression temp = groupByClauseList.get(i);
			String table = temp.table;
			String attributename = temp.columnName;
			
			if(table == null || table.equals(""))
			{
				ArrayList<MathExpression> list = outputAliasMap.get(attributename);
				ArrayList<ColumnExpression> tempList = new ArrayList<ColumnExpression>();
				getColumnFromMathExpression(tempList, list.get(0));
				
				for(int j = 0; j < tempList.size(); j++)
				{
					resultList.add(tempList.get(j));
				}
			}
			else
			{
				resultList.add(temp);
			}
		}
		
		return resultList;
	}
	
	public ArrayList<String> aliasInGroupby(
			ArrayList<ColumnExpression> groupByClauseList)
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < groupByClauseList.size(); i++)
		{
			ColumnExpression temp = groupByClauseList.get(i);
			String table = temp.table;
			String attributename = temp.columnName;
			
			if(table == null || table.equals(""))
			{
				resultList.add(attributename);
			}
		}
		return resultList;
	}
	
	public void getColumnFromMathExpression(ArrayList<ColumnExpression> resultList,
										    MathExpression expression)
	{
		/*
		 * Column in the MathExpression includes:
		 * 1. ArithmeticExpression
		 * 2. ColumnExpression
		 * 3. GeneralFunctionExpression (some errors here if it is also aggregation function)
		 */
		//1. ArithmeticExpression
		if(expression instanceof ArithmeticExpression)
		{
			ArrayList<MathExpression> operandList = ((ArithmeticExpression)expression).operandList;
			for(int i = 0; i < operandList.size(); i++)
			{
				getColumnFromMathExpression(resultList, operandList.get(i));
			}
		}
		//2. ColumnExpression
		else if(expression instanceof ColumnExpression)
		{
			resultList.add((ColumnExpression)expression);
		}
		//3. GeneralFunctionExpression
		else if(expression instanceof GeneralFunctionExpression)
		{
			ArrayList<MathExpression> operandList = ((GeneralFunctionExpression)expression).parasList;
			for(int i = 0; i < operandList.size(); i++)
			{
				getColumnFromMathExpression(resultList, operandList.get(i));
			}
		}
		else if(expression instanceof PredicateWrapper)
		{
			BooleanPredicate predicate = ((PredicateWrapper)expression).predicate;
			getColumnInBooleanPredicate(predicate, resultList);
		}
	}
	
	public ArrayList<ColumnExpression> getColumnFromAsteriskTable(AsteriskTable asteriskTable)throws Exception
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		ArrayList<String> fromList = typeChecker.getFromList();
		HashMap<String, TableReference> tableReferenceMap = typeChecker.getTableReferenceMap();
		HashMap<String, TypeChecker> typerCheckerMap = typeChecker.typerCheckerMap;
		Catalog catalog = SimsqlCompiler.catalog;
		
		for(int j = 0; j < fromList.size(); j++)
		{
			String tableAlias = fromList.get(j);
			
			String tableName;
			Relation relation;
			ArrayList<Attribute> tempList;
			
			if(tableReferenceMap.containsKey(tableAlias))
			{
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				
				tableName = tempTableReference.getTable();
				
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
					Attribute tempAttribute = tempList.get(k);
					String relationName = tempAttribute.getRelName();
					String attributeName = tempAttribute.getName();
					ColumnExpression column = new ColumnExpression(relationName, attributeName);
					resultList.add(column);
				}
			}
			//for subquery
			else if(typerCheckerMap.containsKey(tableAlias))
			{
				TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
				ArrayList<String> outputAttributeNameList = tempChecker.getAttributeList();
				
				for(int k = 0; k < outputAttributeNameList.size(); k++)
				{
					String name = outputAttributeNameList.get(k);
					ColumnExpression column = new ColumnExpression(tableAlias, name);
					resultList.add(column);
				}
			}
		}
		
		return resultList;
	}
	
	public ArrayList<ColumnExpression> getColumnFromAllFromTable(AllFromTable allFromTable)throws Exception
	{
		
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		String tableAlias = allFromTable.table;
		HashMap<String, TableReference> tableReferenceMap = typeChecker.tableReferenceMap;
		
		TableReference tempTableReference = tableReferenceMap.get(tableAlias);
		//if the tableAlias is from a table reference.
		if(tempTableReference != null)
		{
			String tableName = tempTableReference.getTable();
			
			Catalog catalog = SimsqlCompiler.catalog;
			
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
				Attribute tempAttribute = tempList.get(k);
				String relationName = tempAttribute.getRelName();
				String attributeName = tempAttribute.getName();
				ColumnExpression column = new ColumnExpression(relationName, attributeName);
				resultList.add(column);
			}
		}
		//if the table alias is from a subquery in from clause.
		else
		{
			HashMap<String, TypeChecker> typerCheckerMap = typeChecker.typerCheckerMap;
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
	
	//check if the tempList is a subset of columnInGroupbyList
	public boolean subSet(ArrayList<ColumnExpression> tempList, 
			              ArrayList<ColumnExpression> columnInGroupbyList)
	{
		if(tempList == null)
			return true;
		
		boolean found;
		for(int i = 0; i < tempList.size(); i++)
		{
			ColumnExpression tempElement = tempList.get(i);
			found = false;
			
			for(int j = 0; j < columnInGroupbyList.size(); j++)
			{
				ColumnExpression groupbyColumn = columnInGroupbyList.get(j);
				if(groupbyColumn.equals(tempElement))
				{
					found = true;
					break;
				}
			}
			
			if(found)
				continue;
			else
				return false;
		}
		return true;
	}
	
	/*
	 * --------------------------------------------BooleanPredicate--------------------------
	 * The BooleanPredicate that can contain the MathExpression or BooleanPredicate includes
	 * 1. AndPredicate
	 * 2. OrPredicate
	 * 3. NotPredicate
	 * 4. BetweenPredicate
	 * 5. ComparisonPredicate
	 * 6. LikePredicate
	 * 7. ExistPredicate
	 * 8. InPredicate
	 * 
	 * Basic BooleanPredicate do not have some components that need simplification:
	 * 1. AtomPredicate
	 */
	
	public void getColumnInAndPredicate(AndPredicate andPredicate,
									    ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			getColumnInBooleanPredicate(andList.get(i), expressionList);
		}
	}
	
	//or predicate
	public void getColumnInOrPredicate(OrPredicate orPredicate,
								       ArrayList<ColumnExpression> expressionList)
	{
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			getColumnInBooleanPredicate(orList.get(i), expressionList);
		}
	}
	
	
	//Not predicate
	public void getColumnInNotPredicate(NotPredicate notPredicate,
 										ArrayList<ColumnExpression> expressionList) 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		getColumnInBooleanPredicate(predicate, expressionList);
	}
	
	//Between Predicate
	public void getColumnInBetweenPredicate(BetweenPredicate betweenPredicate,
										    ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		getColumnFromMathExpression(expressionList, expression);
		getColumnFromMathExpression(expressionList, lowerExpression);
		getColumnFromMathExpression(expressionList, upperExpression);
	}

	//comparison predicate
	public void getColumnInComparisonPredicate(ComparisonPredicate comparisonPredicate,
														 ArrayList<ColumnExpression> expressionList)
	{
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		getColumnFromMathExpression(expressionList, leftExpression);
		getColumnFromMathExpression(expressionList, rightExpression);
	}

	//like predicate
	public void getColumnInLikePredicate(LikePredicate likePredicate,
												   ArrayList<ColumnExpression> expressionList)
	{
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		getColumnFromMathExpression(expressionList, valueExpression);
		getColumnFromMathExpression(expressionList, patternExpression);
		
	}

	//exist predicate
	public void getColumnInExistPredicate(ExistPredicate existPredicate,
   													ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = existPredicate.existExpression;
		getColumnFromMathExpression(expressionList, expression);
	}
	
	//in predicate, only support the single element
	public void getColumnInInPredicate(InPredicate inPredicate,
												 ArrayList<ColumnExpression> expressionList)
	{
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		getColumnFromMathExpression(expressionList, expression);
		getColumnFromMathExpression(expressionList, setExpression);
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	
	public void getColumnInBooleanPredicate(BooleanPredicate predicate,
												   ArrayList<ColumnExpression> columnList)
	{
		if(predicate instanceof AndPredicate)
		{
			getColumnInAndPredicate((AndPredicate)predicate, columnList);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			getColumnInBetweenPredicate((BetweenPredicate)predicate, columnList);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			getColumnInComparisonPredicate((ComparisonPredicate)predicate, columnList);
		}
		else if(predicate instanceof ExistPredicate)
		{
			getColumnInExistPredicate((ExistPredicate)predicate, columnList);
		}
		else if(predicate instanceof InPredicate)
		{
			getColumnInInPredicate((InPredicate)predicate, columnList);
		}
		else if(predicate instanceof LikePredicate)
		{
			getColumnInLikePredicate((LikePredicate)predicate, columnList);
		}
		else if(predicate instanceof NotPredicate)
		{
			getColumnInNotPredicate((NotPredicate)predicate, columnList);
		}
		else if(predicate instanceof OrPredicate)
		{
			getColumnInOrPredicate((OrPredicate)predicate, columnList);
		}
		
	}
}
