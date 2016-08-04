

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



// import mcdb.compiler.parser.astVisitor.RandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.sqlExpression.*;
// import mcdb.compiler.parser.expression.sqlType.RandomTableStatement;




/**
 * @author Bamboo
 *
 */
public class RandomTableUnnester extends Unnester{

	/**
	 * @param typechecker
	 * In the Unnester, there are two variables:
	 * 1. TypeChecker typechecker;
	 * 2. ArrayList<UnnestedSelectStatement> childrenViewList;
	 */
	private RandomTableTypeChecker typeChecker;
	public RandomTableUnnester(RandomTableTypeChecker typechecker) {
		super(typechecker);
		this.typeChecker = typechecker;
	}
	
	/*
	 * Unnester for the Random_Table
	 */
	public UnnestedRandomTableStatement unnestRandomTableStatement(
			RandomTableStatement randomTableStatement) throws Exception {
		/*
		 * 1. First do the unnesting in all the subqueries of this random_table_statement
		 * As to the unnesting of RandomTable itself, it is left to the translator.
		 */
		
		SQLExpression outerTable = randomTableStatement.outerTable;
		
		ArrayList<WithStatement> withList = randomTableStatement.withList;
		SelectStatement selectStatement = randomTableStatement.selectStatement;
		
		for(int i = 0; i < withList.size(); i ++)
		{
			WithStatement statement = withList.get(i);
			unnestSQLExpression(statement);
		}
		
		unnestSQLExpression(outerTable);
		
		unnestSQLExpression(selectStatement);
		
		/*
		 * 2. collect associated information, and save it to the UnnestedRandomTableStatement
		 */
		
		UnnestedRandomTableStatement statement = 
			 new UnnestedRandomTableStatement(randomTableStatement, typeChecker);
		
		/*
		 * 2.1 Collect the original RandomTableStatement
		 */
		statement.setRandomTableStatement(randomTableStatement);
		
		/*
		 * 2.2 Collect the original TypeChecker
		 */
		statement.setRandomTableChecker(typeChecker);
		//Note that all the information is saved in the TemporaryViewFactory
		/*
		 * 2.3 Collect the UnnestedSelectStatement of outerTable
		 */
		String outTableAlias =  typeChecker.getOutTableName();
		UnnestedSelectStatement outerTableNestedStatement = TemporaryViewFactory.tempStringViewMap.get(outTableAlias);
		statement.setOutTableUnnestStatement(outerTableNestedStatement);
		
		/*
		 * 2.4 Collect the UnnestedSelectStatement of the selectStatement in the RandomTableStatement
		 */
		UnnestedSelectStatement unnestedSelectStatement = TemporaryViewFactory.tempTypeCheckerViewMap.get(typeChecker);
		statement.setUnnestedSelectStatement(unnestedSelectStatement);
		
		/*
		 * 2.5 Collect the Sub UnnestedSelectStatements for the SQLs in the with list.
		 * I suppose that each VGFunction can has several parameters, even constant parameters.
		 */
		ArrayList<String> vgTableNameList = statement.getVgTableNameList();
		HashMap<String, ArrayList<UnnestedSelectStatement>> vgWrapperParaStatementMap = statement.getVgWrapperParaStatementMap();
		
		HashMap<SubqueryExpression, TypeChecker> childTypeCheckerMap = typeChecker.getChildTypeCheckerMap();
		//ArrayList<WithStatement> withList
		for(int i = 0; i < withList.size(); i++)
		{
			WithStatement temp = withList.get(i);
			
			GeneralFunctionExpression expression = temp.expression;
			String tempTableName = temp.tempTableName;
			
			//String functionName = expression.functionName;
			ArrayList<MathExpression> parasList = expression.parasList;
			
			ArrayList<UnnestedSelectStatement> paraUnnesterList = new ArrayList<UnnestedSelectStatement>();
			
			for(int j = 0; j < parasList.size(); j++)
			{
				MathExpression subExp = parasList.get(j);
				
				/*================================================================================
				 * Here we can give some extensions, that some fixed parameters can be shown here.
				 * ===============================================================================
				 */
				if(subExp instanceof SubqueryExpression)
				{
					TypeChecker subTypeChecker = childTypeCheckerMap.get(subExp);
					UnnestedSelectStatement subUnnestedSelectStatement = TemporaryViewFactory.tempTypeCheckerViewMap.get(subTypeChecker);
					/*
					 * Pay attention here, we do not need the added selectElement, which is created during 
					 * during the unnest for the general selectStatement.
					 * subUnnestedSelectStatement.getAddedSelectList().clear(); Now I
					 * do not want to do that.
					 */
					paraUnnesterList.add(subUnnestedSelectStatement);
				}
			}
			vgTableNameList.add(tempTableName);
			vgWrapperParaStatementMap.put(tempTableName, paraUnnesterList);
		}
		
		return statement;
	}
	
	public void unnestWithStatement(WithStatement withStatement) throws Exception {
		GeneralFunctionExpression expression = withStatement.expression;
		withStatement.expression = ((GeneralFunctionExpression)
				unnestGeneralFunctionExpression(expression));
	}
	
	
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
		else if(expression instanceof WithStatement)
		{
			unnestWithStatement((WithStatement)expression);
		}
	}


}
