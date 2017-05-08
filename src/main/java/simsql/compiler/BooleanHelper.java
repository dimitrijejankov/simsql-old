

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
import simsql.compiler.expressions.SubqueryExpression;

import java.util.ArrayList;


// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.ColumnExpression;
// import mcdb.compiler.parser.expression.mathExpression.SubqueryExpression;
// import mcdb.compiler.parser.expression.sqlExpression.TableReference;
// import mcdb.compiler.parser.expression.util.CheckSubquery;
// import mcdb.compiler.parser.expression.util.FinalVariable;




/**
 * @author Bamboo
 *
 */
public class BooleanHelper {
	/*
	 * transfer to CNF
	 */
	public static BooleanPredicate transfer(BooleanPredicate predicate)
	{
		if(predicate instanceof AndPredicate)
		{
			ArrayList<BooleanPredicate> andList = ((AndPredicate) predicate).andList;
			for(int i = 0; i < andList.size(); i++)
			{
				BooleanPredicate temp = transfer(andList.get(i));
				andList.remove(i);
				andList.add(i, temp);
			}
			
			for(int i = 0; i < andList.size(); i++)
			{
				BooleanPredicate temp = andList.get(i);
				
				if(temp instanceof AndPredicate)
				{
					andList.remove(i);
					ArrayList<BooleanPredicate> childList = ((AndPredicate) temp).andList;
					for(int j = childList.size()-1; j >= 0; j--)
					{
						BooleanPredicate child = childList.get(j);
						andList.add(i, child);
					}
				}
			}
			return predicate;
		}
		else if(predicate instanceof OrPredicate)
		{
			ArrayList<BooleanPredicate> orList = ((OrPredicate) predicate).orList;
			for(int i = 0; i < orList.size(); i++)
			{
				BooleanPredicate temp = transfer(orList.get(i));
				orList.remove(i);
				orList.add(i, temp);
			}
			
			for(int i = 0; i < orList.size(); i++)
			{
				BooleanPredicate temp = orList.get(i);
				
				if(temp instanceof OrPredicate)
				{
					orList.remove(i);
					ArrayList<BooleanPredicate> childList = ((OrPredicate) temp).orList;
					for(int j = childList.size()-1; j >= 0; j--)
					{
						BooleanPredicate child = childList.get(j);
						orList.add(i, child);
					}
					return transfer(predicate);
				}
				else if(temp instanceof AndPredicate)
				{
					ArrayList<BooleanPredicate> childList = ((AndPredicate) temp).andList;
					
					orList.remove(i);
					ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
					for(int j = 0; j < childList.size(); j++)
					{
						ArrayList<BooleanPredicate> orTempList = copyList(orList);
						orTempList.add(i, childList.get(j));
						OrPredicate tempOrPredicate = new OrPredicate(orTempList);
						resultList.add(tempOrPredicate);
					}
					AndPredicate resultPredicate = new AndPredicate(resultList);
					return transfer(resultPredicate);
				}
				else if(temp instanceof NotPredicate)
				{
					((NotPredicate) temp).predicate = transfer(((NotPredicate) temp).predicate);
				}
			}
			return predicate;
			
		}
		else if(predicate instanceof NotPredicate)
		{
			BooleanPredicate childPredicate = ((NotPredicate) predicate).predicate;
			childPredicate = transfer(childPredicate);
			((NotPredicate) predicate).predicate = childPredicate;
			
			childPredicate = ((NotPredicate) predicate).predicate;
			if(childPredicate instanceof OrPredicate)
			{
				ArrayList<BooleanPredicate> childList = ((OrPredicate) childPredicate).orList;
				ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
				
				//De'Morgan law
				for(int i = 0; i < childList.size(); i++)
				{
					BooleanPredicate tempPredicate = childList.get(i);
					BooleanPredicate tempPredicate2 = new NotPredicate(tempPredicate);
					resultList.add(tempPredicate2);
				}
				AndPredicate resultPredicate = new AndPredicate(resultList);
				return transfer(resultPredicate);
			}
			else if(childPredicate instanceof AndPredicate)
			{
				ArrayList<BooleanPredicate> childList = ((AndPredicate) childPredicate).andList;
				ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
				
				//De'Morgan law
				for(int i = 0; i < childList.size(); i++)
				{
					BooleanPredicate tempPredicate = childList.get(i);
					BooleanPredicate tempPredicate2 = new NotPredicate(tempPredicate);
					resultList.add(tempPredicate2);
				}
				OrPredicate resultPredicate = new OrPredicate(resultList);
				return transfer(resultPredicate);
			}
			else if(childPredicate instanceof NotPredicate)
			{
				return transfer(((NotPredicate) childPredicate).predicate);
			}
			else
				return predicate;
		}
		else 
		{
			return predicate;
		}
	}
	
	public static ArrayList<BooleanPredicate> copyList(ArrayList<BooleanPredicate> targetList)
	{
		ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
		
		for(int i = 0; i < targetList.size(); i++)
		{
			resultList.add(targetList.get(i));
		}
		
		return resultList;
	}
	
	/*
	 * The predicate should be a CNF
	 */
	public static boolean validPredicate(BooleanPredicate predicate)
	{
		/*
		 * predicate has the form V1 ^ V2 ^ V3.. and V_n (n >=1)
		 * each V_i should has the form:
		 * 		1. constant
		 *      2. (constant or not subquery)
		 *      3. subquery
		 */
		if(predicate instanceof OrPredicate)
		{
			//condition 1
			if(!new CheckSubquery(predicate).hasSubqueryInBooleanPredicate())
			{
				return true;
			}
			else
			{
				//condition 3
				ArrayList<BooleanPredicate> childList = ((OrPredicate) predicate).orList;
				if(childList.size() == 1)
				{
					return true;
				}
				//condition 2
				else
				{
					for(int i = 0; i < childList.size(); i++)
					{
						BooleanPredicate child = childList.get(i);
						if(new CheckSubquery(child).hasSubqueryInBooleanPredicate())
						{
							if(!(child instanceof NotPredicate))
							{
								return false;
							}
						}
					}
					return true;
				}
			}
			
		}
		else if(predicate instanceof NotPredicate)
		{
			return true;
		}
		else if(predicate instanceof AndPredicate)
		{
			ArrayList<BooleanPredicate> childList = ((AndPredicate) predicate).andList;
			for(int i = 0; i < childList.size(); i++)
			{
				if(!validPredicate(childList.get(i)))
				{
					return false;
				}
			}
			return true;
		}
		else
		{
			return true;
		}
	}

	
	/*
	 * divide the predicate to two parts, some in join/semi-join/anti-join, while others in
	 * where clause. This method returns the updated whereClause.
	 */
	public static BooleanPredicate getWherePredicate(BooleanPredicate predicate)
	{
		if(predicate instanceof AndPredicate)
		{
			ArrayList<BooleanPredicate> childList = ((AndPredicate) predicate).andList;
			ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
			
			for(int i = 0; i < childList.size(); i++)
			{
				BooleanPredicate child = childList.get(i);
				
				if(!new CheckSubquery(child).hasSubqueryInBooleanPredicate())
				{
					resultList.add(child);
				}
			}
			if(resultList.size() == 0)
			{
				return null;
			}
			else
			{
				AndPredicate result = new AndPredicate(resultList);
				return result;
			}
		}
		else
		{
			if(!new CheckSubquery(predicate).hasSubqueryInBooleanPredicate())
			{
				return predicate;
			}
			else
			{
				return null;
			}
		}
	}
	
	/*
	 * divide the predicate to two parts, some in join/semi-join/anti-join, while others in
	 * where clause. This method returns the clause stayed in From clause
	 */
	public static BooleanPredicate getSemiOrAntiJoinPreicate(BooleanPredicate predicate)
	{
		if(predicate instanceof AndPredicate)
		{
			ArrayList<BooleanPredicate> childList = ((AndPredicate) predicate).andList;
			ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
			
			for(int i = 0; i < childList.size(); i++)
			{
				BooleanPredicate child = childList.get(i);
				
				if(new CheckSubquery(child).hasSubqueryInBooleanPredicate())
				{
					resultList.add(child);
				}
			}
			if(resultList.size() == 0)
			{
				return null;
			}
			else
			{
				AndPredicate result = new AndPredicate(resultList);
				return result;
			}
		}
		else
		{
			if(new CheckSubquery(predicate).hasSubqueryInBooleanPredicate())
			{
				return predicate;
			}
			else
			{
				return null;
			}
		}
	}
	
	/*
	 * Here, predicate is an element in CNF, and predicate can not be an andPredicate 
	 * in CNF. A1 or A2 or not A3.. or not Q1 or not Q2
	 */
	public static UnnestedSelectStatementJoin getSingleSemi_AntiPreicate(BooleanPredicate predicate,
																		TypeChecker currentTypeChecker)
	{
		if(!new CheckSubquery(predicate).hasSubqueryInBooleanPredicate())
		{
			return null;
		}
		/*
		 * predicate can not be andPredicate -> anti-join
		 */
		if(predicate instanceof OrPredicate)
		{
			ArrayList<BooleanPredicate> orList = ((OrPredicate) predicate).orList;
			ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
			
			for(int i = 0; i < orList.size(); i++)
			{
				BooleanPredicate child = orList.get(i);
				if(child instanceof NotPredicate)
				{
					resultList.add(((NotPredicate) child).predicate);
				}
				else
				{
					resultList.add(new NotPredicate(child));
				}
			}
			AndPredicate resultPredicate = new AndPredicate(resultList);
			
			return getNestedSelectStatementJoin(currentTypeChecker, predicate, resultPredicate, false);
		}
		//basic predicate
		else if(predicate instanceof NotPredicate) //-> anti-join
		{
			BooleanPredicate resultPredicate = ((NotPredicate) predicate).predicate;
			return getNestedSelectStatementJoin(currentTypeChecker, predicate, resultPredicate, false);
		}
		else //->semi-join
		{
			return getNestedSelectStatementJoin(currentTypeChecker, predicate, predicate, true);
		}
	}
	/*
	 * Apply the absorb law (A is a subquery)
	 * 1. A or A = A
	 * 2. A and A = A
	 * 3. A or not A = 1
	 * 4. A and not A = 0
	 * 5. A and (not A or B) = A and B
	 * 6. not A and (not A or B) = not A
	 * 7. (not A or B) and (not A or C) we do not need to consider
	 * 8. 
	 */
	public static BooleanPredicate applyAbsorbLaw(BooleanPredicate predicate)
	{
		/*
		 * 1. A or A = A
		 * 2. A and A = A
		 */
		return null;
	}
	
	public static UnnestedSelectStatementJoin getNestedSelectStatementJoin(TypeChecker currentTypeChecker,
																			BooleanPredicate originalPredicate, 
																			BooleanPredicate updatedPredicate,
																			boolean equi_join)
	{
		/*
		 * 1. get the nestedSelectStatements corresponding to the subqueries in this predicate.
		 * 
		 */
		ArrayList<SubqueryExpression> subqueryList =  new CheckSubquery(originalPredicate).getSubqueryList();
		ArrayList<UnnestedSelectStatement> nestedSelectStatementList = new ArrayList<UnnestedSelectStatement>();
		
		for(int i = 0; i < subqueryList.size(); i++)
		{
			SubqueryExpression temp = subqueryList.get(i);
			TypeChecker tempChecker = temp.getTypeChecker();
			UnnestedSelectStatement tempnestedStatement = 
				          TemporaryViewFactory.tempTypeCheckerViewMap.get(tempChecker);
			nestedSelectStatementList.add(tempnestedStatement);
			
			/*
			 * 2. add some link predicate between this subquery (offerspring) and
			 *  (this level and parents)
			 */
			
			ArrayList<ColumnExpression> addedSelectList = tempnestedStatement.getTotalAddedSelectList();
			
			String viewName = tempnestedStatement.getViewName();
			for(int j = 0; j < addedSelectList.size(); j++)
			{
				ColumnExpression tempColumn = addedSelectList.get(j);
				String columnName = tempColumn.toString();
				
				ColumnExpression viewColumn = new ColumnExpression(viewName, columnName);
				ComparisonPredicate predicate = 
								new ComparisonPredicate(viewColumn, tempColumn, FinalVariable.EQUALS);
				updatedPredicate = addPredicate(updatedPredicate, predicate);
			}
		}
		
		UnnestedSelectStatementJoin result = 
			     new UnnestedSelectStatementJoin(nestedSelectStatementList,updatedPredicate, equi_join);
		
		return result;
	}
	
	public static BooleanPredicate addPredicate(BooleanPredicate predicate, BooleanPredicate addingPredicate)
	{
		if(predicate instanceof AndPredicate)
		{
			((AndPredicate) predicate).andList.add(addingPredicate);
			return predicate;
		}
		else 
		{
			ArrayList<BooleanPredicate> resultList = new ArrayList<BooleanPredicate>();
			resultList.add(predicate);
			resultList.add(addingPredicate);
			return new AndPredicate(resultList);
		}
	}
}
