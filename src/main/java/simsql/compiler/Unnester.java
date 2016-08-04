

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
import java.util.HashSet;
import java.util.concurrent.ConcurrentLinkedQueue;


// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.sqlExpression.*;
// import mcdb.compiler.parser.expression.sqlType.*;
// import mcdb.compiler.parser.expression.util.CheckAggregate;
// import mcdb.compiler.parser.expression.util.Simplifier;





/* 
 * @author: Bamboo
 * 
 * The Unnester aims to unnest the sub queries in the SQL. Such sub queries can happens in:
 * 1. Select Clause (math expression in the select element)
 *    It is also to deal with subquery (1.)in the Select clause. We just need to unnest the subquery
 *    in the select clause. It is independent with the outside SQL.
 * 2. Where clause (math expression and predicate expression)
 * 
 * It is non-trivial to deal wit the subquery (2.). We need to create the view first, which is a
 * join between the outer tables that are referenced by the subquery and the tables in the 
 * subquery. 
 * 
 * Example: 
 * 		select A
 *      from B
 *      where C = ( select D
 *                  from E
 *                  where B.F = E.G and not E.H = ( select I
 *                                              from J
 *                                              where B.K = J.L and E.M = J.N
 *                                            )
 *                 )
 *      
 *      1). Create the View for the most inside block (subquery)
 *          CREATE VIEW LEVEL_2 AS
 *		    SELECT J.I, B.K, E.M
 *		    FROM (B JOIN J ON B.K = J.L) JOIN E ON E.M = J.N
 *          Group by B.*, E.*
 *      
 *      2). CREATE VIEW LEVEL_1 AS
 *   		SELECT B.F, B.K
 *   		FROM (B JOIN E on B.F = E.G) ANTIJOIN LEVEL_2 ON E.H = LEVEL_2.I AND B.K = LEVEL_2.K AND E.M = LEVEL_2.M
 *      
 *          CREATE VIEW LEVEL_0 AS
 *      	SELECT B.A
 *   		FROM B SEMIJOIN LEVEL_1 ON B.F = LEVEL_1.F AND B.K = LEVEL_1.k
 *         
 * 3. From clause
 * 
 * It is easy to deal with subquery (3.) in the From clause. We just need to unnest the subquery
 * in the from clause. It is independent with the outside SQL. If the From element is a view, we 
 * need to get the SQL from the catalog, and then unnest the view directly.
 *       
 * The whole function is a recursive function that reduce any component that has Math expression
 * or Boolean Predicate.
 */


public class Unnester {

	public TypeChecker typeChecker;
	
	public ArrayList<UnnestedSelectStatement> childrenViewList;
	
	
	public Unnester(TypeChecker typechecker)
	{
		this.typeChecker = typechecker;
		childrenViewList = new ArrayList<UnnestedSelectStatement>();
	}
	
	
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
	public UnnestedSelectStatement unnestSelectStatement(SelectStatement selectStatement) throws Exception
	{
		
		//If the predicate can not be done with the program
		if(TemporaryViewFactory.errorNum > 0)
			return null;
		
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<SQLExpression> tableReferenceList = selectStatement.tableReferenceList;
		BooleanPredicate whereClause = selectStatement.whereClause;
		ArrayList<OrderByColumn> orderByClauseList = selectStatement.orderByClauseList;
		BooleanPredicate havingClause = selectStatement.havingClause;
		
		/*
		 * 1.2 sub check: each element should have a visit check: if it is in the real relation
		 */
		for(int i = 0; i < tableReferenceList.size(); i++)
		{
			SQLExpression fromElement = tableReferenceList.get(i);
			
			if(fromElement instanceof FromSubquery)
			{
				tableReferenceList.remove(i);
				fromElement = unnestFromSubquery((FromSubquery)fromElement);
				tableReferenceList.add(i, fromElement);
			}	
		}
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			selectElement = unnestSelectElement(selectElement);
			selectList.remove(i);
			selectList.add(i, selectElement);
		}
		
		if(whereClause != null)
		{
			selectStatement.whereClause = unnestBooleanPredicate(whereClause);
		}
		
		/*
		 * 5. orderby clause
		 */
		if(orderByClauseList != null)
		{
			for(int i = 0; i < orderByClauseList.size(); i++)
			{
				OrderByColumn column = orderByClauseList.get(i);
				unnestOrderByColumn(column);
				orderByClauseList.remove(i);
				orderByClauseList.add(i, column);
			}
		}
		
		/*
		 * 6. Having clause
		 */
		if(havingClause != null)
		{
			selectStatement.havingClause = unnestBooleanPredicate(havingClause);
		}
		
		
		/*
		 * 7. CNF the where clause
		 */
		selectStatement.whereClause = BooleanHelper.transfer(selectStatement.whereClause);
		selectStatement.havingClause = BooleanHelper.transfer(selectStatement.havingClause);
		
		selectStatement.whereClause = Simplifier.simplifyBooleanPredicate(selectStatement.whereClause);
		selectStatement.havingClause = Simplifier.simplifyBooleanPredicate(selectStatement.havingClause);
		
		whereClause = selectStatement.whereClause;
		havingClause = selectStatement.havingClause;
			
		if(!BooleanHelper.validPredicate(whereClause))
		{
			TemporaryViewFactory.errorNum ++;
			System.err.println("Boolean expression does not satisfy our requirement!");
			throw new Exception("Boolean expression does not satisfy our requirement!");
		}
		
		if(!BooleanHelper.validPredicate(havingClause))
		{
			TemporaryViewFactory.errorNum ++;
			System.err.println("Boolean expression does not satisfy our requirement!");
			throw new Exception("Boolean expression does not satisfy our requirement!");
		}
		/*
		 * begin unnesting
		 * 8. create the affixed data structure NestedSelectStatement
		 */
		
		UnnestedSelectStatement nestedStatement = 
			            new UnnestedSelectStatement(selectStatement, typeChecker);
		String viewName = "_temp_view_" + TemporaryViewFactory.getID();
		nestedStatement.setViewName(viewName);
		TemporaryViewFactory.tempStringViewMap.put(viewName, nestedStatement);
		TemporaryViewFactory.tempTypeCheckerViewMap.put(typeChecker, nestedStatement);
		
		/*
		 * 9. Deal with the ancestors who provide the table
		 */
		/*
		 * The order of outerTableList is from the nearest parent.
		 * That means we need join from the farthest parent
		 * All element is the outerTableList should get the duplicate removal action and the outerjoin
		 */
		HashMap<SQLExpression, TypeChecker> checkerMap = new HashMap<SQLExpression, TypeChecker>();
		ArrayList<SQLExpression> ancesterTableList = getAllTableReference(checkerMap);
		//9.1 change From element join_predicate
		for(int i = 0; i < ancesterTableList.size(); i++)
		{	
			SQLExpression element = ancesterTableList.get(i);
			nestedStatement.addTableReference(element);
			nestedStatement.addDuplicatedRemoval(element);
		}
		
		//9.2 change select element
		ArrayList<ColumnExpression> ancesterColumnExpression = getAllColumnExpression();
		for(int i = 0; i < ancesterColumnExpression.size(); i++)
		{
			ColumnExpression column = ancesterColumnExpression.get(i);
			nestedStatement.addFromParentSelectElement(column);
		}
		
		//9.3 change where clause
		BooleanPredicate updatedWherePredicate = BooleanHelper.getWherePredicate(whereClause);
		updatedWherePredicate = Simplifier.simplifyBooleanPredicate(updatedWherePredicate);
		nestedStatement.setWherePredicate(updatedWherePredicate);
		
		//9.4 change groupby element
		boolean aggregateInSelect = aggregateInSelect(selectList);
		if(aggregateInSelect)
		{
			for(int i = 0; i < ancesterTableList.size(); i++)
			{	
				SQLExpression element = ancesterTableList.get(i);
				String alias = "";
				if(element instanceof TableReference)
				{
					alias = ((TableReference) element).alias;
				}
				else if(element instanceof FromSubquery)
				{
					alias = ((FromSubquery) element).alias;
				}
				
				if(!alias.equals(""))
				{	
					AllFromTable tempAllTable = new AllFromTable(alias);
					nestedStatement.addGroupbyElmement(tempAllTable);
					TypeChecker tempChecker = checkerMap.get(element);
					nestedStatement.getTableCheckerMap().put(tempAllTable, tempChecker);
				}
			}
		}
		
		/*
		 * 10. Deal with the where, group by, order by, having clause where this layer have some 
		 * subqueries 
		 */
		
		/*
		 * 10.1 get the semi-join or anti-join predicates together from where clause.
		 */
		BooleanPredicate semi_antiPredicates = BooleanHelper.getSemiOrAntiJoinPreicate(whereClause);
		semi_antiPredicates = Simplifier.simplifyBooleanPredicate(semi_antiPredicates);
		
		/*
		 * 10.2 for each Disjunctive predicate, create the anti-join or semi-join.
		 */
		if(semi_antiPredicates instanceof AndPredicate)
		{
			ArrayList<BooleanPredicate> childList = ((AndPredicate) semi_antiPredicates).andList;
			for(int i = 0; i < childList.size(); i++)
			{
				BooleanPredicate child = childList.get(i);
				UnnestedSelectStatementJoin joinElement = BooleanHelper.getSingleSemi_AntiPreicate(child, typeChecker);
				
				nestedStatement.addChildrenView(joinElement);
			}
		}
		else if(semi_antiPredicates != null)
		{
			UnnestedSelectStatementJoin joinElement = BooleanHelper.getSingleSemi_AntiPreicate(semi_antiPredicates, typeChecker);
			nestedStatement.addChildrenView(joinElement);
		}
		
		return nestedStatement;
	}
	
	public boolean thisOrParentHasThisEdge(TypeChecker root, String columName)
	{
		TypeChecker currentTypeChecker = root;
		while(true)
		{
			HashSet<String> providedReferenceSet = currentTypeChecker.getProvidedReferenceSet();
			if(providedReferenceSet.contains(columName))
			{
				return true;
			}
			
			currentTypeChecker = currentTypeChecker.getParent();
			if(currentTypeChecker == null)
			{
				break;
			}
		}
		
		return false;
	}
	
	public FromSubquery unnestFromSubquery(FromSubquery fromSubquery) throws Exception {
		MathExpression sql = fromSubquery.expression;
		fromSubquery.expression = unnestMathExpression(sql);
		return fromSubquery;
	}
		
	public SQLExpression unnestSelectElement(SQLExpression selectElement) throws Exception
	{
		if(selectElement instanceof DerivedColumn)
		{
			unnestDerivedColumn((DerivedColumn)selectElement);
		}
		return selectElement;
	}
	
	public void unnestDerivedColumn(DerivedColumn derivedColumn) throws Exception {
		MathExpression expression = derivedColumn.expression;
		derivedColumn.expression = unnestMathExpression(expression);
	}
	
	
	public void unnestOrderByColumn(OrderByColumn orderByColumn) throws Exception {
		MathExpression expression = orderByColumn.expression;
		orderByColumn.expression = unnestMathExpression(expression);
	}

		
	public UnnestedSelectStatement unnestViewStatement(ViewStatement viewStatement) throws Exception
	{
		SelectStatement statement = viewStatement.statement;
		return unnestSelectStatement(statement);
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
	 */
	
	public MathExpression unnestArithmeticExpression(ArithmeticExpression arithmeticExpression) throws Exception 
	{
		
		ArrayList<MathExpression> operandList = arithmeticExpression.operandList;
		for(int i = 0; i < operandList.size(); i++)
		{
			MathExpression expression = operandList.get(i);
			expression = unnestMathExpression(expression);
			operandList.remove(i);
			operandList.add(i,expression);
		}
		
		return arithmeticExpression;
	}

	/*
	 * check subquery expression (math) type
	 * One of the place that need more modification
	 */
	public MathExpression unnestSubqueryExpression(SubqueryExpression subqueryExpression) throws Exception {
		SelectStatement statement = subqueryExpression.value;
		TypeChecker subTypeChecker = subqueryExpression.getTypeChecker();
		Unnester subNester = new Unnester(subTypeChecker);
		UnnestedSelectStatement nestedStatement = subNester.unnestSelectStatement(statement);
		
		if(nestedStatement == null)
		{
			return null;
		}
		
		if(subTypeChecker.getParent() != null)
		{
			String tempViewName = nestedStatement.getViewName();
			UnnestedSelectStatement childView = TemporaryViewFactory.tempStringViewMap.get(tempViewName); 
			childrenViewList.add(childView);
		}
		
		return subqueryExpression;
	}
	
	public MathExpression unnestAggregateExpression(AggregateExpression aggregateExpression) throws Exception {
		// just check the subexpression
		MathExpression expression = aggregateExpression.expression;
		aggregateExpression.expression = unnestMathExpression(expression);
		return aggregateExpression;
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public MathExpression unnestPredicateWrapperExpression(
			PredicateWrapper predicateWrapper) throws Exception {
		BooleanPredicate predicate = predicateWrapper.predicate;
		predicateWrapper.predicate = unnestBooleanPredicate(predicate);
		return predicateWrapper;
	}
	
	/*
	 * check the general function(non-Javadoc)
	 * Here, we consider the general functions are all VGFunction
	 */
	public MathExpression unnestGeneralFunctionExpression(
			GeneralFunctionExpression generalFunctionExpression) throws Exception {
		ArrayList<MathExpression> paraList = generalFunctionExpression.parasList;
		for(int i = 0; i < paraList.size(); i++)
		{
			MathExpression expression = paraList.get(i);
			expression = unnestMathExpression(expression);
			paraList.remove(i);
			paraList.add(i,expression);
		}
		return generalFunctionExpression;
	}

	public MathExpression unnestSetExpression(SetExpression setExpression) throws Exception {
		ArrayList<MathExpression> expressionList = setExpression.expressionList;
		for(int i = 0; i < expressionList.size(); i++)
		{
			MathExpression expression = expressionList.get(i);
			expression = unnestMathExpression(expression);
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
	
	public BooleanPredicate unnestAndPredicate(AndPredicate andPredicate) throws Exception
	{
		ArrayList<BooleanPredicate> andList = andPredicate.andList;
		
		for(int i = 0 ; i < andList.size(); i++)
		{
			BooleanPredicate element = unnestBooleanPredicate(andList.get(i));
			andList.remove(i);
			andList.add(i, element);
		}
		return andPredicate;
	}
	
	//or predicate
	public BooleanPredicate unnestOrPredicate(OrPredicate orPredicate) throws Exception {
		ArrayList<BooleanPredicate> orList = orPredicate.orList;
		
		for(int i = 0 ; i < orList.size(); i++)
		{
			BooleanPredicate element = unnestBooleanPredicate(orList.get(i));
			orList.remove(i);
			orList.add(i, element);
		}
		
		return orPredicate;
	}
	
	
	//Not predicate
	public BooleanPredicate unnestNotPredicate(NotPredicate notPredicate) throws Exception 
	{
		BooleanPredicate predicate = notPredicate.predicate;
		notPredicate.predicate = unnestBooleanPredicate(predicate);
		return notPredicate;
	}
	
	//Between Predicate
	public BooleanPredicate unnestBetweenPredicate(BetweenPredicate betweenPredicate) throws Exception {
		MathExpression expression = betweenPredicate.expression;
		MathExpression lowerExpression = betweenPredicate.lowExpression;
		MathExpression upperExpression = betweenPredicate.upExpression;
		
		betweenPredicate.expression = unnestMathExpression(expression);
		betweenPredicate.lowExpression = unnestMathExpression(lowerExpression);
		betweenPredicate.upExpression = unnestMathExpression(upperExpression);
		
		return betweenPredicate;
	}

	//comparison predicate
	public BooleanPredicate unnestComparisonPredicate(ComparisonPredicate comparisonPredicate) throws Exception {
		//check the right side, pop type
		MathExpression leftExpression, rightExpression;
		leftExpression = comparisonPredicate.leftExpression;
		rightExpression = comparisonPredicate.rightExpression;
		
		comparisonPredicate.leftExpression = unnestMathExpression(leftExpression);
		comparisonPredicate.rightExpression = unnestMathExpression(rightExpression);
		
		return comparisonPredicate;
	}

	//like predicate
	public BooleanPredicate unnestLikePredicate(LikePredicate likePredicate) throws Exception {
		MathExpression valueExpression, patternExpression;
		valueExpression = likePredicate.value;
		patternExpression = likePredicate.pattern;
		
		likePredicate.value = unnestMathExpression(valueExpression);
		likePredicate.pattern = unnestMathExpression(patternExpression);
		
		return likePredicate;
	}

	/*
	 * exist predicate
	 * Need more modification
	 * select A from B where not exists (
	 *                                   select D from E where B.F = E.G);
	 *                                   
	 * => X = (select D, B.* from E, B where B.F = E.G group by B.*)
	 * select A from B, X where not true
	 */
	public BooleanPredicate unnestExistPredicate(ExistPredicate existPredicate) throws Exception {
		MathExpression expression = existPredicate.existExpression;
		existPredicate.existExpression = unnestMathExpression(expression);
		
		return existPredicate;
	}
	
	/*
	 * In predicate, only support the single element
	 * More modification
	 */
	public BooleanPredicate unnestInPredicate(InPredicate inPredicate) throws Exception {
		MathExpression expression = inPredicate.expression;
		MathExpression setExpression = inPredicate.setExpression;
		
		inPredicate.expression = unnestMathExpression(expression);
		inPredicate.setExpression = unnestMathExpression(setExpression);
		
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
	
	public void unnestSQLExpression(SQLExpression expression) throws Exception
	{
		if(expression instanceof DerivedColumn)
		{
			unnestDerivedColumn((DerivedColumn)expression);
		}
		else if(expression instanceof FromSubquery)
		{
			unnestFromSubquery((FromSubquery)expression);
		}
		else if(expression instanceof OrderByColumn)
		{
			unnestOrderByColumn((OrderByColumn)expression);
		}
		else if(expression instanceof SelectStatement)
		{
			unnestSelectStatement((SelectStatement)expression);
		}
	}
	
	
	public MathExpression unnestMathExpression(MathExpression expression) throws Exception
	{
		if(expression instanceof AggregateExpression)
		{
			expression = unnestAggregateExpression((AggregateExpression)expression);
		}
		else if(expression instanceof ArithmeticExpression)
		{
			expression = unnestArithmeticExpression((ArithmeticExpression)expression);
		}
		else if(expression instanceof GeneralFunctionExpression)
		{
			expression = unnestGeneralFunctionExpression((GeneralFunctionExpression)expression);
		}
		else if(expression instanceof SetExpression)
		{
			expression = unnestSetExpression((SetExpression)expression);
		}
		else if(expression instanceof SubqueryExpression)
		{
			expression = unnestSubqueryExpression((SubqueryExpression)expression);
		}
		else if(expression instanceof PredicateWrapper)
		{
			expression = unnestPredicateWrapperExpression((PredicateWrapper)expression);
		}
		
		return expression;
	}
	
	
	public BooleanPredicate unnestBooleanPredicate(BooleanPredicate predicate) throws Exception
	{
		if(predicate instanceof AndPredicate)
		{
			predicate = unnestAndPredicate((AndPredicate)predicate);
		}
		else if(predicate instanceof BetweenPredicate)
		{
			predicate = unnestBetweenPredicate((BetweenPredicate)predicate);
		}
		else if(predicate instanceof ComparisonPredicate)
		{
			predicate = unnestComparisonPredicate((ComparisonPredicate)predicate);
		}
		else if(predicate instanceof ExistPredicate)
		{
			predicate = unnestExistPredicate((ExistPredicate)predicate);
		}
		else if(predicate instanceof InPredicate)
		{
			predicate = unnestInPredicate((InPredicate)predicate);
		}
		else if(predicate instanceof LikePredicate)
		{
			predicate = unnestLikePredicate((LikePredicate)predicate);
		}
		else if(predicate instanceof NotPredicate)
		{
			predicate = unnestNotPredicate((NotPredicate)predicate);
		}
		else if(predicate instanceof OrPredicate)
		{
			predicate = unnestOrPredicate((OrPredicate)predicate);
		}
		
		return predicate;
	} 
	
	
	public ArrayList<SQLExpression> getTableReference(
				   HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap,
				   HashMap<SQLExpression, TypeChecker> tempMap)
	{
		ArrayList<SQLExpression> resultList = new ArrayList<SQLExpression>();
		
		TypeChecker tempChecker = typeChecker.getParent();
		while(tempChecker != null)
		{
			if(providedTableReferenceMap.containsKey(tempChecker))
			{
				CheckerEdge checkerEdge = providedTableReferenceMap.get(tempChecker);
				ArrayList<SQLExpression> tempList = checkerEdge.getTableReferenceList();
				
				for(int i = 0; i < tempList.size(); i++)
				{
					SQLExpression temp = tempList.get(i);
					
					/*
					 * for testing
					 */
					
					if(!resultList.contains(temp))
					{
						resultList.add(temp);
						tempMap.put(temp, tempChecker);
					}
				}
			}
			
			tempChecker = tempChecker.getParent();
		}
		return resultList;
	}
	
	/*
	 * This gets the reference tables from its ancestors, where such referenced tables exist in this block
	 * as well as its offspring.
	 */
	public ArrayList<SQLExpression> getAllTableReference(HashMap<SQLExpression, TypeChecker> tempMap)
	{
		ArrayList<SQLExpression> resultList = new ArrayList<SQLExpression>();
		
		/*
		 * 1. get all of its ancestors.
		 */
		HashSet<TypeChecker> ancestorTypeCheckerSet = new HashSet<TypeChecker>();
		TypeChecker tempChecker = typeChecker.getParent();
		while(tempChecker != null)
		{
			ancestorTypeCheckerSet.add(tempChecker);
			tempChecker = tempChecker.getParent();
		}
		
		/*
		 * 2. bread-first-search the current typeChecker and its offspring, and find all the check-edges.
		 */
		ConcurrentLinkedQueue<TypeChecker> queue = new ConcurrentLinkedQueue<TypeChecker>();
		HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap;
		HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap;
		
		queue.add(typeChecker);
		while(!queue.isEmpty())
		{
			// process the current typeChecker
			tempChecker = queue.poll();
			providedTableReferenceMap = tempChecker.getProvidedTableReferenceMap();
			
			for(TypeChecker ancestorCheckerElement: providedTableReferenceMap.keySet())
			{
				if(ancestorTypeCheckerSet.contains(ancestorCheckerElement))
				{
					CheckerEdge tempEdge = providedTableReferenceMap.get(ancestorCheckerElement);
					ArrayList<SQLExpression> tempList = tempEdge.getTableReferenceList();
					
					for(int i = 0; i < tempList.size(); i++)
					{
						SQLExpression temp = tempList.get(i);
						if(!resultList.contains(temp))
						{
							resultList.add(temp);
							tempMap.put(temp, ancestorCheckerElement);
						}
					}
				}
			}
			
			//put the children of the tempChecker to the queue
			childTypeCheckerMap = tempChecker.getChildTypeCheckerMap();
			for(SubqueryExpression childSubQuery: childTypeCheckerMap.keySet())
			{
				queue.add(childTypeCheckerMap.get(childSubQuery));
			}
		}
		
		return resultList;
	}
	
	public ArrayList<ColumnExpression> getColumnExpression(
			HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap)
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		TypeChecker tempChecker = typeChecker.getParent();
		while(tempChecker != null)
		{
			if(providedTableReferenceMap.containsKey(tempChecker))
			{
				CheckerEdge checkerEdge = providedTableReferenceMap.get(tempChecker);
				ArrayList<SQLExpression> tempList = checkerEdge.getTableReferenceList();
				HashMap<SQLExpression, ArrayList<ColumnExpression>>
				                        tempMap = checkerEdge.getColumnReferenceMap();
				
				for(int i = 0; i < tempList.size(); i++)
				{
					SQLExpression temp = tempList.get(i);
					
					ArrayList<ColumnExpression> tempList2 = tempMap.get(temp);
					if(tempList2 != null)
					{
						for(int j = 0; j < tempList2.size(); j++)
						{
							ColumnExpression tempColumn = tempList2.get(j);
							
							if(!resultList.contains(tempColumn))
							{
								resultList.add(tempColumn);
							}
						}
					}
				}
			}
			
			tempChecker = tempChecker.getParent();
		}
		return resultList;
	}
	
	public ArrayList<ColumnExpression> getAllColumnExpression()
	{
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		/*
		 * 1. get all of its ancestors.
		 */
		HashSet<TypeChecker> ancestorTypeCheckerSet = new HashSet<TypeChecker>();
		TypeChecker tempChecker = typeChecker.getParent();
		while(tempChecker != null)
		{
			ancestorTypeCheckerSet.add(tempChecker);
			tempChecker = tempChecker.getParent();
		}
		
		/*
		 * 2. bread-first-search the current typeChecker and its offspring, and find all the check-edges.
		 */
		ConcurrentLinkedQueue<TypeChecker> queue = new ConcurrentLinkedQueue<TypeChecker>();
		HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap;
		HashMap<TypeChecker, CheckerEdge> providedTableReferenceMap;
		
		queue.add(typeChecker);
		while(!queue.isEmpty())
		{
			// process the current typeChecker
			tempChecker = queue.poll();
			providedTableReferenceMap = tempChecker.getProvidedTableReferenceMap();
			
			for(TypeChecker ancestorCheckerElement: providedTableReferenceMap.keySet())
			{
				if(ancestorTypeCheckerSet.contains(ancestorCheckerElement))
				{
					CheckerEdge tempEdge = providedTableReferenceMap.get(ancestorCheckerElement);
					ArrayList<SQLExpression> tempList = tempEdge.getTableReferenceList();
					HashMap<SQLExpression, ArrayList<ColumnExpression>>
					                        tempMap = tempEdge.getColumnReferenceMap();
					
					for(int i = 0; i < tempList.size(); i++)
					{
						SQLExpression temp = tempList.get(i);
						
						ArrayList<ColumnExpression> tempList2 = tempMap.get(temp);
						if(tempList2 != null)
						{
							for(int j = 0; j < tempList2.size(); j++)
							{
								ColumnExpression tempColumn = tempList2.get(j);
								
								if(!resultList.contains(tempColumn))
								{
									resultList.add(tempColumn);
								}
							}
						}
					}
				}
			}
			
			//put the children of the tempChecker to the queue
			childTypeCheckerMap = tempChecker.getChildTypeCheckerMap();
			for(SubqueryExpression childSubQuery: childTypeCheckerMap.keySet())
			{
				queue.add(childTypeCheckerMap.get(childSubQuery));
			}
		}
		
		return resultList;
	}

	public boolean aggregateInSelect(ArrayList<SQLExpression> selectList)
	{
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression expression = selectList.get(i);
			if(expression instanceof DerivedColumn)
			{
				MathExpression mExpression = ((DerivedColumn)expression).expression;
				if(CheckAggregate.directAggregateInMathExpression(mExpression))
				{
					return true;
				}
			}
		}
		
		return false;
	}
}
