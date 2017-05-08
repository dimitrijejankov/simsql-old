

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

import simsql.compiler.expressions.ColumnExpression;

import java.util.ArrayList;
import java.util.HashMap;



// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.mathExpression.SubqueryExpression;
// import mcdb.compiler.parser.expression.sqlExpression.*;
// import mcdb.compiler.parser.expression.util.Simplifier;




/**
 * @author Bamboo
 *
 */
public class UnnestedSelectStatement {
	
	private String viewName;
	
	private SelectStatement selectStatement;
	private TypeChecker typechecker;
	
	/*
	 * 1. some data structure for the ancestor
	 */
	
	/* change from clause */
	private ArrayList<SQLExpression> addedTableReference;
	private ArrayList<SQLExpression> duplicatedRemovalList;
	
	/* change select clause */
	private ArrayList<ColumnExpression> addedFromParentSelectList;
	
	/*join condition with parent*/
	private BooleanPredicate wherePredicate;
	
	/*change groupby clause */
	private ArrayList<AllFromTable> addGroupbyColumnList;
	private HashMap<AllFromTable, TypeChecker> tableCheckerMap;
	
	
	/*
	 * 2. some data structure for the ancestor children 
	 */
	private ArrayList<UnnestedSelectStatementJoin> childrenView;

	public UnnestedSelectStatement(SelectStatement statement, TypeChecker typeChecker)
	{
		this.selectStatement = statement;
		this.typechecker = typeChecker;
		initializeData();
		
	}

	public void initializeData()
	{
		addedTableReference = new ArrayList<SQLExpression>();
		duplicatedRemovalList = new ArrayList<SQLExpression>();
		addedFromParentSelectList = new ArrayList<ColumnExpression>();
		addGroupbyColumnList = new ArrayList<AllFromTable>();
		childrenView = new ArrayList<UnnestedSelectStatementJoin>();
		tableCheckerMap = new HashMap<AllFromTable, TypeChecker>();
		
		wherePredicate = null;
		viewName = null;
	}
	
	
	public void addTableReference(SQLExpression tableRefernceElement)
	{
		if(!addedTableReference.contains(tableRefernceElement))
		{
			addedTableReference.add(tableRefernceElement);
		}	
	}
	
	public void addDuplicatedRemoval(SQLExpression tableRefernceElement)
	{
		if(!duplicatedRemovalList.contains(tableRefernceElement))
		{
			duplicatedRemovalList.add(tableRefernceElement);
		}	
	}
	
	public void addFromParentSelectElement(ColumnExpression element)
	{
		if(!addedFromParentSelectList.contains(element))
			addedFromParentSelectList.add(element);
	}
	
	public SelectStatement getSelectStatement() {
		return selectStatement;
	}

	public void addGroupbyElmement(AllFromTable element)
	{
		if(!addGroupbyColumnList.contains(element))
			addGroupbyColumnList.add(element);
	}


	public void setSelectStatement(SelectStatement selectStatement) {
		this.selectStatement = selectStatement;
	}


	public ArrayList<SQLExpression> getAddedTableReference() {
		return addedTableReference;
	}

	public void setAddedTableReference(ArrayList<SQLExpression> addedTableReference) {
		this.addedTableReference = addedTableReference;
	}

	public ArrayList<SQLExpression> getDuplicatedRemovalList() {
		return duplicatedRemovalList;
	}

	public void setDuplicatedRemovalList(
			ArrayList<SQLExpression> duplicatedRemovalList) {
		this.duplicatedRemovalList = duplicatedRemovalList;
	}



	public ArrayList<ColumnExpression> getFromParentSelectList() {
		return addedFromParentSelectList;
	}



	public void setFromParentSelectList(ArrayList<ColumnExpression> addedFromParentSelectList) {
		this.addedFromParentSelectList = addedFromParentSelectList;
	}
	
	
	public ArrayList<AllFromTable> getAddGroupbyColumnList() {
		return addGroupbyColumnList;
	}



	public void setAddGroupbyColumnList(ArrayList<AllFromTable> addGroupbyColumnList) {
		this.addGroupbyColumnList = addGroupbyColumnList;
	}

	public TypeChecker getTypechecker() {
		return typechecker;
	}

	public void setTypechecker(TypeChecker typechecker) {
		this.typechecker = typechecker;
	}

	public String getViewName() {
		return viewName;
	}

	public void setViewName(String viewName) {
		this.viewName = viewName;
	}

	public ArrayList<UnnestedSelectStatementJoin> getChildrenView() {
		return childrenView;
	}

	public void setChildrenView(ArrayList<UnnestedSelectStatementJoin> childrenView) {
		this.childrenView = childrenView;
	}
	
	public void addChildrenView(UnnestedSelectStatementJoin element)
	{
		if(!childrenView.contains(element))
		{
			childrenView.add(element);
		}
	}

	public BooleanPredicate getWherePredicate() {
		return wherePredicate;
	}

	public void setWherePredicate(BooleanPredicate wherePredicate) throws Exception {
		wherePredicate = Simplifier.simplifyBooleanPredicate(wherePredicate);
		this.wherePredicate = wherePredicate;
	}

	public HashMap<AllFromTable, TypeChecker> getTableCheckerMap() {
		return tableCheckerMap;
	}

	public void setTableCheckerMap(
			HashMap<AllFromTable, TypeChecker> tableCheckerMap) {
		this.tableCheckerMap = tableCheckerMap;
	}

	public ArrayList<ColumnExpression> getTotalAddedSelectList() {
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		resultList.addAll(addedFromParentSelectList);
		return resultList;
	}
}
