

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


package simsql.compiler;

import java.util.ArrayList;

public class AggregationRule {
	private SelectStatement selectStatement;
	private TypeChecker typeChecker;
	
	public AggregationRule(SelectStatement selectStatement,
					   TypeChecker typeChecker)
	{
		this.selectStatement = selectStatement;
		this.typeChecker = typeChecker;
	}

	public SelectStatement getSelectStatement() {
		return selectStatement;
	}

	public void setSelectStatement(SelectStatement selectStatement) {
		this.selectStatement = selectStatement;
	}

	public TypeChecker getTypeChecker() {
		return typeChecker;
	}

	public void setTypeChecker(TypeChecker typeChecker) {
		this.typeChecker = typeChecker;
	}

	public boolean checkRule() {
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<AggregateExpression> aggExpressionList = new ArrayList<AggregateExpression>();
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			if(selectElement instanceof DerivedColumn)
			{
				MathExpression expression = ((DerivedColumn) selectElement).expression;
				
				/*
				 * 1. Get the aggregate list in the DerivedColumn
				 */
				ArrayList<MathExpression> aggregateListInOneDerivedColumn = new ArrayList<MathExpression>();
				AggregateExpressionExtractor.getAggregateInMathExpression(expression, aggregateListInOneDerivedColumn);
				
				for(int j = 0; j < aggregateListInOneDerivedColumn.size(); j++)
				{
					AggregateExpression aggExpression = (AggregateExpression)(aggregateListInOneDerivedColumn.get(j));
					aggExpressionList.add(aggExpression);
				}
			}
		}
		
		if(aggExpressionList.size() == 0)
		{
			return true;
		}
		else
		{
			AggregateExpression firstElement = aggExpressionList.get(0);
			int aggType = firstElement.setQuantifier;
			for(int i = 1; i < aggExpressionList.size(); i++)
			{
				if(aggType != aggExpressionList.get(i).setQuantifier)
				{
					return false;
				}
			}
			
			return true;
		}
	}
	
	
}
