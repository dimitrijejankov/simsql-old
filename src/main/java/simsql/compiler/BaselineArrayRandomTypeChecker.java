

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
import java.util.HashMap;
import java.util.HashSet;
import simsql.runtime.DataType;


public class BaselineArrayRandomTypeChecker extends RandomTableTypeChecker {
	private ArrayList<String> initializedQueryList = null;
	
	public BaselineArrayRandomTypeChecker()
			throws Exception {
		super(false);
		initializedQueryList = new ArrayList<String>();
	}
	
	
	public ArrayList<String> getQueryList()
	{
		return initializedQueryList;
	}
	
	public String getInitializedQueryList() {
		String result = "";
		if(initializedQueryList != null)
		{
			for(int i = 0; i < initializedQueryList.size(); i++)
			{
				result += initializedQueryList.get(i);
				result += ";\r\n";
			}
		}
		
		return result;
	}



	/**
	 * @param baseLineRandomTableStatement
	 * @return
	 */
	public boolean visitBaselineArrayRandomTableStatement(
					BaseLineArrayRandomTableStatement baselineRandomTableStatement) throws Exception
	{
		String lowerBound = baselineRandomTableStatement.lowerBound;
		String upBound = baselineRandomTableStatement.upBound;
		DefinedArrayTableSchema arraySchema = (DefinedArrayTableSchema) baselineRandomTableStatement.definedTableSchema;
		SQLExpression outerTable = baselineRandomTableStatement.outerTable;
		ArrayList<WithStatement> withList = baselineRandomTableStatement.withList;
		SelectStatement selectStatement  = baselineRandomTableStatement.selectStatement;
		String query = baselineRandomTableStatement.getSqlString();
				
		/*
		 * 1. check the schema.
		 */
		
		
		boolean subCheck;
		subCheck = arraySchema.acceptVisitor(this);
		
		if(!subCheck)
		{
			return false;
		}
		
		/*
		 * 2. check the random table in the general form. The check includes checking the schema under the different
		 * time ticks has the same form.
		 */
		DefinedTableSchema tempSchema = new DefinedTableSchema(arraySchema.viewName + "_i", 
															   arraySchema.tableAttributeList,
																true);
		GeneralRandomTableStatement tempStatement = new GeneralRandomTableStatement(tempSchema,
																outerTable, 
																withList,
																selectStatement);
		GeneralRandomTableTypeChecker tempChecker = new GeneralRandomTableTypeChecker(false);
		tempChecker.setSaved(false);
		subCheck = tempStatement.acceptVisitor(tempChecker);
		if(!subCheck)
		{
			return false;
		}
		
		/*
		 * 3. try to save the view. Basically we iterate from lowerBound to upBound, and replace each "i" with 
		 * the initialized index.
		 */
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<DataType> typeList = tempChecker.getRandomTableAttributes(selectList);
		
		int lower = Integer.parseInt(lowerBound);
		int up = Integer.parseInt(upBound);
		String initializedQuery;
		for(int i = lower; i <= up; i++)
		{
			tempSchema = new DefinedTableSchema(arraySchema.viewName + "_" + i, 
												arraySchema.tableAttributeList,
												true);
			
			//We are sure that this BaselineArrayRandom table should be saved.
			initializedQuery = replaceGeneralIndexInQuery(query, i);
			initializedQueryList.add(initializedQuery);
			saveView(tempSchema, typeList, initializedQuery, tempChecker.getIndexedRandomTableList());
		}
		
		return true;
	}
	
	/*
	 * This algorithm tries to replace the "i" in the query with the value string.
	 */
	public String replaceGeneralIndexInQuery(String query, int value)
	{
		//1. replace the "i" in the query with value.
		ArrayList<String> tokenList = new ArrayList<String>();
		ArrayList<Integer> positionList = new ArrayList<Integer>();
		
		int numberOfToken = 0;
		if(query.length() == 0 || query.charAt(0) == '\'')
		{
			throw new RuntimeException("error in the query, the query could not start with \"'\"!");
		}
		
		for(int i = 1; i < query.length(); i++)
		{
			if(query.charAt(i) == '\'' && query.charAt(i-1) != '\\')
			{
				numberOfToken ++;
				
				if(numberOfToken % 2 == 1)
				{
					positionList.add(i);
				}
				else
				{
					positionList.add(i+1);
				}
			}
		}
		
		if(positionList.size() == 0)
		{
			tokenList.add(query);
		}
		else
		{
			tokenList.add(query.substring(0, positionList.get(0)));
		}
		
		for(int i = 1; i < positionList.size(); i++)
		{
			tokenList.add(query.substring(positionList.get(i-1), positionList.get(i)));
		}
		
		if(positionList.size() >= 1 && positionList.get(positionList.size()-1) < query.length())
		{
			tokenList.add(query.substring(positionList.get(positionList.size()-1), query.length()));
		}
		
		
		String token;
		char before, after;
		
		//
		String resultQuery = "";
		for(int i = 0; i < tokenList.size(); i++)
		{
			positionList.clear();
			
			token = tokenList.get(i);
			if(token.length() == 0)
			{
				//nothing to do..
			}
			else if(token.charAt(0) == '\'')
			{
				resultQuery += token;
			}
			else //replace the "i" in the token with the specific value in the input.
			{
				for(int j = 0; j < token.length(); j++)
				{
					if(j==0)
					{
						before = ' ';
					}
					else
					{
						before = token.charAt(j-1);
					}
					
					if(j == token.length()-1)
					{
						after = ' ';
					}
					else
					{
						after = token.charAt(j+1);
					}
					
					if(!(before >= 'a' && before <= 'z' || 
							before >= '0' && before <= '9') &&
						!(after >= 'a' && after <= 'z' || 
								after >= '0' && after <= '9') && token.charAt(j) == 'i')
					{
						positionList.add(j);
					}
				}
				
				StringBuffer sb = new StringBuffer(token);
				
				for(int j = positionList.size()-1; j >= 0 ; j--)
				{
					sb.replace(positionList.get(j), positionList.get(j)+1, value + "");
				}
				
				resultQuery += sb.toString();
			}
		}
		
		//2. replace the baseline index.
		int start = resultQuery.indexOf("[");
		int end = resultQuery.indexOf("]");
		
		if(end >= 0 && start >= 0)
		{
			resultQuery = resultQuery.substring(0, start+1) + value + resultQuery.substring(end, resultQuery.length());
		}
		else
		{
			throw new RuntimeException("This is not an array random table statement!");
		}
		
		return resultQuery;
	}
	
	public void saveView(DefinedTableSchema tableAttributes, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql,
            ArrayList<String> dependedRandomTableList) throws Exception
	{
		String viewName;
		
		viewName = tableAttributes.viewName;
		
		ArrayList<String> attributeNameList = tableAttributes.tableAttributeList;
		ArrayList<DataType> attributeTypeList = gottenAttributeTypeList;

		ArrayList<Attribute> schema = new ArrayList<Attribute>();
		// The view explicitly define the schema
		if (attributeNameList != null) {
			for (int i = 0; i < attributeNameList.size(); i++) {
				String name = attributeNameList.get(i);
				DataType type = attributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schema.add(attribute);
			}
		}
		else {
			for (int i = 0; i < attributeTypeList.size(); i++) {
				String name = attributeList.get(i);
				DataType type = attributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schema.add(attribute);
			}
		}

		View view = new View(viewName, sql, schema, DataAccess.OBJ_RANDRELATION);
		catalog.addView(view);
		
		int end = viewName.lastIndexOf("_");
		String realViewName = viewName.substring(0, end);
		catalog.addIndexTable(realViewName, viewName);

		/*
		 * add support the query interface for the SimSQL
		 */
		catalog.addMCDependecy(viewName, dependedRandomTableList);
	}
	
}
