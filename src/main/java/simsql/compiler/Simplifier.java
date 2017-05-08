

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
package simsql.compiler; // package mcdb.compiler.parser.expression.util;

import simsql.compiler.expressions.*;

import java.util.ArrayList;




// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.sqlExpression.*;
// import mcdb.compiler.parser.expression.sqlType.RandomTableStatement;
// import mcdb.compiler.parser.expression.sqlType.ViewStatement;



/**
 * @author Bamboo
 *
 */
public class Simplifier {
	
	/*
	 * The simplifier aims to simplify the data structure of the AST. In the AST, there are two
	 * kinds of expressions that have redundant structure, i.e., Math Expression, and Boolean
	 * Predicate. Both of them have lots of layers to implement the priority; however, this makes
	 * them to have lots of layers that have only one element.
	 * 
	 * The whole function is a recursive function that reduce any component that has Math expression
	 * or Boolean Predicate.
	 */
	
	/*
	 * ------------------------------------SQLExpression-------------------------------------
	 * The SQL Expressions that can contain the MathExpression or BooleanPredicate includes
	 * 1. SelectStatement
	 * 2. FromSubquery
	 * 3. DerivedColumn
	 * 4. OrderByColumn
	 * 5. WithStatement
	 * 6. ViewStatement
	 * 7. RandomTableStatement
	 * AllFromTable and AsteriskTable do not have MathExpression or BooleanPredicate
	 */
	public static void simplifySelectStatement(SelectStatement selectStatement) throws Exception {
		
		if(selectStatement == null)
		{
			System.err.println("selectStatement should be not NULL!");
			throw new Exception("selectStatement should be not NULL!");
		}
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<SQLExpression> tableReferenceList = selectStatement.tableReferenceList;
		BooleanPredicate whereClause = selectStatement.whereClause;
		ArrayList<OrderByColumn> orderByClauseList = selectStatement.orderByClauseList;
		BooleanPredicate havingClause = selectStatement.havingClause;
		
		/*
		 * 1.2 sub check: each element should have a visit check: if it is in the real relation
		 */
		if(tableReferenceList != null)
		{
			for(int i = 0; i < tableReferenceList.size(); i++)
			{
				SQLExpression fromElement = tableReferenceList.get(i);
				
				if(fromElement instanceof FromSubquery)
				{
					simplifySQLExpression(fromElement);
				}
				else if(fromElement instanceof TableReference)
				{
					TableReference tmp = ((TableReference)fromElement);

					if(tmp.getExpression("i") != null)
					{
						tmp.setExpression("i", simplifyMathExpression(tmp.getExpression("i")));
					}
				}
			}
		}
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			simplifySQLExpression(selectElement);
		}
		
		if(whereClause != null)
		{
			selectStatement.whereClause = simplifyBooleanPredicate(whereClause);
		}
		
		/*
		 * 5. orderby clause
		 */
		if(orderByClauseList != null)
		{
			for(int i = 0; i < orderByClauseList.size(); i++)
			{
				OrderByColumn column = orderByClauseList.get(i);
				simplifySQLExpression(column);
			}
		}
		
		/*
		 * 6. Having clause
		 */
		if(havingClause != null)
		{
			selectStatement.havingClause = simplifyBooleanPredicate(havingClause);
		}

	}
	
	public static void simplifyFromSubquery(FromSubquery fromSubquery) throws Exception {
		MathExpression sql = fromSubquery.expression;
		fromSubquery.expression = simplifyMathExpression(sql);
	}
		
	public static void simplifyDerivedColumn(DerivedColumn derivedColumn) throws Exception {
		MathExpression expression = derivedColumn.expression;
		derivedColumn.expression = simplifyMathExpression(expression);
	}
	
	
	public static void simplifyOrderByColumn(OrderByColumn orderByColumn) throws Exception {
		MathExpression expression = orderByColumn.expression;
		orderByColumn.expression = simplifyMathExpression(expression);
	}

	public static void simplifyWithStatement(WithStatement withStatement) throws Exception {
		GeneralFunctionExpression expression = withStatement.expression;
		if(expression == null)
		{
			System.err.println("Function expression should be not null!");
			throw new Exception("Function expression should be not null!");
		}
		withStatement.expression = ((GeneralFunctionExpression)
		                   simlifyGeneralFunctionExpression(expression));
	}

	
	public static void simplifyViewStatement(ViewStatement viewStatement) throws Exception
	{
		SelectStatement statement = viewStatement.statement;
		simplifySQLExpression(statement);
	}
	
	public static void simplifyMaterializedViewStatement(MaterializedViewStatement viewStatement) throws Exception
	{
		SelectStatement statement = viewStatement.statement;
		simplifySQLExpression(statement);
	}
	public static void simplifyRandomTableStatement(
			RandomTableStatement randomTableStatement) throws Exception {
		
		SQLExpression outerTable = randomTableStatement.outerTable;
		
		ArrayList<WithStatement> withList = randomTableStatement.withList;
		SelectStatement selectStatement = randomTableStatement.selectStatement;
		
		for(int i = 0; i < withList.size(); i ++)
		{
			WithStatement statement = withList.get(i);
			simplifySQLExpression(statement);
		}
		
		if(outerTable instanceof FromSubquery)
		{
			simplifySQLExpression(outerTable);
		}
		
		simplifySQLExpression(selectStatement);
	}
	
	public static void simplifyUnionViewStatement(UnionViewStatement expression) throws Exception
	{
		ArrayList<SQLExpression> tableNameList = expression.getTableNameList();
		for(int i = 0; i < tableNameList.size(); i++)
		{
			SQLExpression  temp = tableNameList.get(i);
			simplifySQLExpression(temp);
		}
	}
	
	/*
	 * --------------------------------------------MathExpression--------------------------
	 * The MathExpression that can contain the MathExpression or BooleanPredicate includes
	 * 1. ArithmeticExpression
	 * 2. SubqueryExpression
	 * 3. AggregateExpression
	 * 4. GeneralFunctionExpression
	 * 5. SetExpression
	 * 
	 * Basic MathExpressions do not have some component that needs simplification:
	 * 1. AsteriskExpression
	 * 2. ColumnExpression
	 * 3. DateExpression
	 * 4. NumericExpression
	 * 5. StringExpression
	 * 6. GeneralTableIndex
	 */
	
	public static MathExpression simplifyArithmeticExpression(ArithmeticExpression arithmeticExpression) throws Exception
	{
		
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			expression = simplifyMathExpression(expression);
			operandList.remove(i);
			operandList.add(i,expression);
		}
		
		if(operandList.size() == 1)
		{
			return operandList.get(0);
		}
		else 
		{
			return arithmeticExpression;
		}
	}

	//check subquery expression (math) type
	public static MathExpression simplifySubqueryExpression(SubqueryExpression subqueryExpression) throws Exception {
		SelectStatement statement = subqueryExpression.value;
		simplifySelectStatement(statement);
		return subqueryExpression;
	}
	
	public static MathExpression simplifyAggregateExpression(AggregateExpression aggregateExpression) throws Exception {
		// just check the subexpression
		MathExpression expression = aggregateExpression.expression;
		aggregateExpression.expression = simplifyMathExpression(expression);
		return aggregateExpression;
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public static MathExpression simlifyGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression) throws Exception {
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			expression = simplifyMathExpression(expression);
			paraList.remove(i);
			paraList.add(i,expression);
		}
		return generalFunctionExpression;
	}
	
	public static MathExpression simplifyPredicateWrapperExpression(PredicateWrapper wrapper) throws Exception
	{
		wrapper.predicate = simplifyBooleanPredicate(wrapper.predicate);
		return wrapper;
	}

	public static MathExpression simplifySetExpression(SetExpression setExpression) throws Exception {
		ArrayList<MathExpression> expressionList = setExpression.expressionList;
		for(int i = 0; i < expressionList.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			expression = simplifyMathExpression(expression);
			expressionList.remove(i);
			expressionList.add(i,expression);
		}
		return setExpression;
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
	
	public static BooleanPredicate simplifyAndPredicate(AndPredicate andPredicate) throws Exception
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			BooleanPredicate element = simplifyBooleanPredicate(andList.get(i));
			andList.remove(i);
			andList.add(i, element);
		}
		
		if(andList.size() == 1)
		{
			return andList.get(0);
		}
		else 
		{
			return andPredicate;
		}
		
	}
	
	//or predicate
	public static BooleanPredicate simplifyOrPredicate(OrPredicate orPredicate) throws Exception {
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			BooleanPredicate element = simplifyBooleanPredicate(orList.get(i));
			orList.remove(i);
			orList.add(i, element);
		}
		
		if(orList.size() == 1)
		{
			return orList.get(0);
		}
		else 
		{
			return orPredicate;
		}
	}
	
	
	//Not predicate
	public static BooleanPredicate simplifyNotPredicate(NotPredicate notPredicate) throws Exception 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		
		BooleanPredicate temp = simplifyBooleanPredicate(predicate);
		
		if(temp instanceof NotPredicate)
		{
			return ((NotPredicate) temp).predicate;
		}
		else
		{
			notPredicate.predicate = temp;
			return notPredicate;
		}
	}
	
	//Between Predicate
	public static BooleanPredicate simplifyBetweenPredicate(BetweenPredicate betweenPredicate) throws Exception {
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		betweenPredicate.expression = simplifyMathExpression(expression);
		betweenPredicate.lowExpression = simplifyMathExpression(lowerExpression);
		betweenPredicate.upExpression = simplifyMathExpression(upperExpression);
		
		return betweenPredicate;
	}

	//comparison predicate
	public static BooleanPredicate simplifyComparisonPredicate(ComparisonPredicate comparisonPredicate) throws Exception {
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		comparisonPredicate.leftExpression = simplifyMathExpression(leftExpression);
		comparisonPredicate.rightExpression = simplifyMathExpression(rightExpression);
		
		return comparisonPredicate;
	}

	//like predicate
	public static BooleanPredicate simplifyLikePredicate(LikePredicate likePredicate) throws Exception {
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		likePredicate.value = simplifyMathExpression(valueExpression);
		likePredicate.pattern = simplifyMathExpression(patternExpression);
		
		return likePredicate;
	}

	//exist predicate
	public static BooleanPredicate simplifyExistPredicate(ExistPredicate existPredicate) throws Exception {
		MathExpression expression = existPredicate.existExpression;
		existPredicate.existExpression = simplifyMathExpression(expression);
		
		return existPredicate;
	}
	
	//in predicate, only support the single element
	public static BooleanPredicate simplifyInPredicate(InPredicate inPredicate) throws Exception {
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		inPredicate.expression = simplifyMathExpression(expression);
		inPredicate.setExpression = simplifyMathExpression(setExpression);
		
		return inPredicate;
	}

	/*
	 * Since that are kinds of SQLExpressions, MathExpressions, PredicateExpressions, we need
	 * to differ between such expression. Especially all kinds of component, their subcomponent 
	 * is expression by such interface instead of specific expressions. One way to do them is to 
	 * provide another common interface for all component like ASTVisitor. But this makes the things
	 * much more complex, so I just use this program to do that. But its extensibility is worse.
	 */
	
	//-------------------------------------interface transfer-------------------------------
	
	public static void simplifySQLExpression(SQLExpression expression) throws Exception
	{
		MathExpression temp_expression;
		
		if(expression instanceof DerivedColumn)
		{
			simplifyDerivedColumn((DerivedColumn)expression);
		}
		else if(expression instanceof FromSubquery)
		{
			simplifyFromSubquery((FromSubquery)expression);
		}
		else if(expression instanceof OrderByColumn)
		{
			simplifyOrderByColumn((OrderByColumn)expression);
		}
		else if(expression instanceof SelectStatement)
		{
			simplifySelectStatement((SelectStatement)expression);
		}
		else if(expression instanceof WithStatement)
		{
			simplifyWithStatement((WithStatement)expression);
		}
		else if(expression instanceof TableReference)
		{
			temp_expression = ((TableReference)expression).getExpression("i");
			temp_expression = simplifyMathExpression(temp_expression);
			((TableReference)expression).setExpression("i", temp_expression);
		}
		else if(expression instanceof GeneralTableName)
		{
			temp_expression = ((GeneralTableName)expression).getIndexExp();
			temp_expression = simplifyMathExpression(temp_expression);
			((GeneralTableName)expression).setIndexExp(temp_expression);
		}
		else if(expression instanceof GeneralTableNameArray)
		{
			//lower bound
			temp_expression = ((GeneralTableNameArray)expression).getLowerBoundExp();
			temp_expression = simplifyMathExpression(temp_expression);
			((GeneralTableNameArray)expression).setLowerBoundExp(temp_expression);
			
			//up bound
			temp_expression = ((GeneralTableNameArray)expression).getUpBoundExp();
			temp_expression = simplifyMathExpression(temp_expression);
			((GeneralTableNameArray)expression).setUpBoundExp(temp_expression);
		}
	}
	
	
	public static MathExpression simplifyMathExpression(MathExpression expression) throws Exception
	{
		if(expression instanceof AggregateExpression)
		{
			expression = simplifyAggregateExpression((AggregateExpression)expression);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			expression = simplifyArithmeticExpression((ArithmeticExpression)expression);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			expression = simlifyGeneralFunctionExpression((GeneralFunctionExpression)expression);
		}
		else if(expression instanceof SetExpression)
		{
			expression = simplifySetExpression((SetExpression)expression);
		}
		else if(expression instanceof SubqueryExpression)
		{
			expression = simplifySubqueryExpression((SubqueryExpression)expression);
		}
		else if(expression instanceof PredicateWrapper)
		{
			expression = simplifyPredicateWrapperExpression((PredicateWrapper)expression);
		}
		
		return expression;
	}
	
	
	public static BooleanPredicate simplifyBooleanPredicate(BooleanPredicate predicate) throws Exception
	{
		if(predicate instanceof AndPredicate)
		{
			predicate = simplifyAndPredicate((AndPredicate)predicate);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			predicate = simplifyBetweenPredicate((BetweenPredicate)predicate);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			predicate = simplifyComparisonPredicate((ComparisonPredicate)predicate);
		}
		else if(predicate instanceof ExistPredicate)
		{
			predicate = simplifyExistPredicate((ExistPredicate)predicate);
		}
		else if(predicate instanceof InPredicate)
		{
			predicate = simplifyInPredicate((InPredicate)predicate);
		}
		else if(predicate instanceof LikePredicate)
		{
			predicate = simplifyLikePredicate((LikePredicate)predicate);
		}
		else if(predicate instanceof NotPredicate)
		{
			predicate = simplifyNotPredicate((NotPredicate)predicate);
		}
		else if(predicate instanceof OrPredicate)
		{
			predicate = simplifyOrPredicate((OrPredicate)predicate);
		}
		
		return predicate;
	}
}
