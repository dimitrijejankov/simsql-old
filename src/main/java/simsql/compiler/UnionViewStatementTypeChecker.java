

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
import java.util.HashSet;
import simsql.runtime.DataType;


public class UnionViewStatementTypeChecker extends TypeChecker {
	private boolean toSave = true;
	/**
	 * @param allowDuplicatedAttributeAlias
	 */
	public UnionViewStatementTypeChecker(boolean allowDuplicatedAttributeAlias) throws Exception{
		super(allowDuplicatedAttributeAlias);
	}
	
	public UnionViewStatementTypeChecker() throws Exception {
		super(false);
	}
	
	public boolean visitUnionViewStatement(UnionViewStatement unionViewStatement) throws Exception
	{
		//record this statements
		if(super.statement == null)
		{
			super.statement = unionViewStatement;
		}
		
		DefinedTableSchema schema = unionViewStatement.getSchema();
		ArrayList<SQLExpression> tableNameList = unionViewStatement.getTableNameList();
		String query = unionViewStatement.getSqlString();
		
		/*
		 * 1. check the schema.
		 */
		boolean subCheck;
		if(toSave)
		{
			if(schema == null)
			{
				System.err.println("Schema for view should be not NULL!");
				throw new Exception("Schema for view should be not NULL!");
			}
			subCheck = schema.acceptVisitor(this);
		}
		else
		{
			subCheck = true;
		}
		
		if(!subCheck)
		{
			return false;
		}
		
		/*
		 * 2. check the table name list.
		 */
		if(tableNameList == null || tableNameList.size() == 0)
		{
			System.err.println("The union view needs some tables!");
			return false;
		}
		
		ArrayList<String> viewAttributeList = schema.tableAttributeList;
		SQLExpression expression;
		String entityName = null;
		String entityPrefix = null;
		
		/*
		 * the elementNameSet is used to check if the elements repeat!
		 */
		HashSet<String> elementNameSet = new HashSet<String>();
		HashSet<String> dependedRandomTableSet = new HashSet<String>();
		
		for(int i = 0; i < tableNameList.size(); i++)
		{
			expression = tableNameList.get(i);
			subCheck = expression.acceptVisitor(this);
			if(!subCheck)
			{
				return false;
			}
			
			if(unionViewStatement instanceof BaselineUnionViewStatement ||
					unionViewStatement instanceof ConstantUnionViewStatement)
			{
				if(!(expression instanceof BaselineTableName || 
						expression instanceof BaselineTableNameArray ||
						expression instanceof CommonTableName))
				{
					System.err.println("Baseline/ConstantUnionViewStatement union view should not have general index \"i\" ");
					return false;
				}	
			}
			
			String elementName = null;
			String elementPrefix = null;
			
			if(expression instanceof CommonTableName)
			{
				elementName = ((CommonTableName) expression).getName();
				elementPrefix = ((CommonTableName) expression).getName();
				
				//check if it repeats
				if(!elementNameSet.contains(elementName))
				{
					elementNameSet.add(elementName);
				}
				else
				{
					System.err.println("Union view have repeated elements! ");
					return false;
				}
			}
			else if(expression instanceof BaselineTableName)
			{
				elementName = ((BaselineTableName) expression).getName() + "_" + 
						 ((BaselineTableName) expression).getIndex();
				elementPrefix = ((BaselineTableName) expression).getName();
				dependedRandomTableSet.add(elementName);
				//check if it repeats
				if(!elementNameSet.contains(elementName))
				{
					elementNameSet.add(elementName);
				}
				else
				{
					System.err.println("Union view have repeated elements! ");
					return false;
				}
			}
			else if(expression instanceof BaselineTableNameArray){
				elementName = ((BaselineTableNameArray) expression).getName() + "_" + 
						 ((BaselineTableNameArray) expression).getLowerBound();
				elementPrefix = ((BaselineTableNameArray) expression).getName();
				
				//check if it repeats
				for(int j = ((BaselineTableNameArray) expression).getLowerBoundValue(); 
						j <= ((BaselineTableNameArray) expression).getUpBoundValue();
						j++)
				{
					String tempName = ((BaselineTableNameArray) expression).getName() + "_" + j;
					dependedRandomTableSet.add(tempName);
					if(!elementNameSet.contains(tempName))
					{
						elementNameSet.add(tempName);
					}
					else
					{
						System.err.println("Union view have repeated elements! ");
						return false;
					}
				}
			}
			else if(expression instanceof GeneralTableName)
			{
				elementName = ((GeneralTableName) expression).getName() + "_i";
				dependedRandomTableSet.add(elementName);
				elementPrefix = ((GeneralTableName) expression).getName();
				
				//check if it repeats
				String tempName = ((GeneralTableName) expression).getVersionedName();
				if(!elementNameSet.contains(tempName))
				{
					elementNameSet.add(tempName);
				}
				else
				{
					System.err.println("Union view have repeated elements! ");
					return false;
				}
			}
			else if(expression instanceof GeneralTableNameArray)
			{
				elementName = ((GeneralTableNameArray) expression).getName() + "_i";
				dependedRandomTableSet.add(elementName);
				elementPrefix = ((GeneralTableNameArray) expression).getName();
				
				//check if it repeats
				String tempName = ((GeneralTableNameArray) expression).getVersionLowerName();
				if(!elementNameSet.contains(tempName))
				{
					elementNameSet.add(tempName);
				}
				else
				{
					System.err.println("Union view have repeated elements! ");
					return false;
				}
				
				tempName = ((GeneralTableNameArray) expression).getVersionUpName();
				if(!elementNameSet.contains(tempName))
				{
					elementNameSet.add(tempName);
				}
				else
				{
					System.err.println("Union view have repeated elements! ");
					return false;
				}
			}
			
			if(elementName == null)
			{
				System.err.println("Union view should have entities! ");
				return false;
			}
			else
			{
				if(entityName == null && entityPrefix == null)
				{
					entityName = elementName;
					entityPrefix = elementPrefix;
				}
				else if(!entityPrefix.equals(elementPrefix))
				{
					System.err.println("Union view elements should follow the same name! ");
					return false;
				}
			}
			
		}
		
		/*
		 * 3. individual check for each kind of statement.
		 */
		if(unionViewStatement instanceof BaselineUnionViewStatement)
		{
			subCheck = visitBaseLineUnionViewStatement((BaselineUnionViewStatement)unionViewStatement);
		}
		else if(unionViewStatement instanceof ConstantUnionViewStatement)
		{
			subCheck = visitConstantUnionViewStatement((ConstantUnionViewStatement)unionViewStatement);
		}
		else if(unionViewStatement instanceof GeneralUnionViewStatement)
		{
			subCheck = visitGeneralUnionViewStatement((GeneralUnionViewStatement)unionViewStatement);
		}
		
		if(!subCheck)
			return false;
		
		//4. check if the number of attributes match
		ArrayList<String> timeTickElementTableList = catalog.getIndexTableList(entityPrefix);
		if(timeTickElementTableList == null || timeTickElementTableList.size() == 0)
		{
			System.err.println("Currently we only support the union view on time tick based random table!");
			return false;
		}
		
		Relation relation = catalog.getRelation(entityName);
		View view = catalog.getView(entityName);
		
		ArrayList<Attribute> attributeList;
		if(relation != null)
		{
			System.err.println("Currently we only support the union view on relation!");
			return false;
		}
		else
		{
			attributeList = view.getAttributes();
		}
		
		ArrayList<DataType> typeList = new ArrayList<DataType>();
		ArrayList<String> tempAttributeNameList = new ArrayList<String>();
		
		for(int i = 0; i < attributeList.size(); i++)
		{
			typeList.add(attributeList.get(i).getType());
			tempAttributeNameList.add(attributeList.get(i).getName());
		}
		
		/*
		 * 5. check if the Table under the different time tick has the same schema.
		 * Note that BaselineArrayRandomTypeChecker has already been considered by the GeneralRandomTableTypeChecker,
		 * which would be called by the BaselineArrayRandomTypeChecker.
		 */
		if(unionViewStatement instanceof BaselineUnionViewStatement ||
				unionViewStatement instanceof GeneralUnionViewStatement)
		{
			String viewName = schema.getViewName();
			int end = viewName.lastIndexOf("_");
			
			String realViewName = viewName.substring(0, end);
			ArrayList<String> timeTickTableList = catalog.getIndexTableList(realViewName);
			if(timeTickTableList != null && timeTickTableList.size() > 0)
			{
				String anotherTimeTickTable = timeTickTableList.get(0);
				View anotherTimeTickView = catalog.getView(anotherTimeTickTable);
				ArrayList<Attribute> tempAttributeList = anotherTimeTickView.getAttributes();
				if(tempAttributeList.size() != typeList.size())
				{
					System.err.println("The random table under the different time tick has the different number of attributes!");
					return false;
				}
				
				for(int i = 0; i < tempAttributeList.size(); i++)
				{
					if(!typeList.get(i).getTypeName().equals(tempAttributeList.get(i).getType().getTypeName()))
					{
						System.err.println("The random table under the different time tick has the different type!");
						return false;
					}
					
					if(viewAttributeList != null)
					{
						if(!viewAttributeList.get(i).equals(tempAttributeList.get(i).getName()))
						{
							System.err.println("The random table under the different time tick has the different name!");
							return false;
						}
					}
					else
					{
						if(!tempAttributeNameList.get(i).equals(tempAttributeList.get(i).getName()))
						{
							System.err.println("The random table under the different time tick has the different name!");
							return false;
						}
					}
				}
			}
		}
		
		if(viewAttributeList != null)
		{
			if(viewAttributeList.size() != attributeList.size())
			{
				System.err.println("The number of attributes defined in the random table [" +
						schema.toString() + "] does not match its elements!");
				return false;
			}
			else
			{
				if(toSave)
				{
					saveView(schema, typeList, query, unionViewStatement, dependedRandomTableSet);
				}
			}
		}
		else
		{
			schema.tableAttributeList = tempAttributeNameList;
			if(toSave)
			{
					saveView(schema, typeList, query, unionViewStatement, dependedRandomTableSet);
			}
		}
		
		return true;
	}
	
	public boolean isGeneralUnionViewTypeChecker()
	{
		if(this.statement != null)
		{
			return this.statement instanceof GeneralUnionViewStatement;
		}
		
		return false;
	}

	private boolean visitBaseLineUnionViewStatement(BaselineUnionViewStatement baseLineUnionViewStatement) throws Exception
	{
		String indexString = baseLineUnionViewStatement.getIndex();
		
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
		
		return true;
	}

	private boolean visitConstantUnionViewStatement(ConstantUnionViewStatement constantUnionViewStatement) throws Exception
	{
		return true;
	}

	private boolean visitGeneralUnionViewStatement(GeneralUnionViewStatement generalUnionViewStatement) throws Exception
	{
		return true;
	}
	
	public void saveView(DefinedTableSchema schema, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql,
            UnionViewStatement generalUnionViewStatement,
            HashSet<String> dependedRandomTableSet) throws Exception
	{
		String viewName = schema.getViewName();
		ArrayList<String> attributeNameList = schema.tableAttributeList;
		ArrayList<DataType> attributeTypeList = gottenAttributeTypeList;

		ArrayList<Attribute> schemaAttributeList = new ArrayList<Attribute>();
		// The view explicitly define the schema
		if (attributeNameList != null) {
			for (int i = 0; i < attributeNameList.size(); i++) {
				String name = attributeNameList.get(i);
				DataType type = attributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schemaAttributeList.add(attribute);
			}
		}
		else {
			for (int i = 0; i < attributeTypeList.size(); i++) {
				String name = attributeList.get(i);
				DataType type = attributeTypeList.get(i);
				Attribute attribute = new Attribute(name, type, viewName);
				schemaAttributeList.add(attribute);
			}
		}

		View view;
		
		if(generalUnionViewStatement instanceof ConstantUnionViewStatement)
		{
			view = new View(viewName, sql, schemaAttributeList, DataAccess.OBJ_VIEW);
			SimsqlCompiler.catalog.addView(view);
		}
		else if(generalUnionViewStatement instanceof BaselineUnionViewStatement || 
				generalUnionViewStatement instanceof GeneralUnionViewStatement)
		{
			view = new View(viewName, sql, schemaAttributeList, DataAccess.OBJ_UNION_VIEW);
			SimsqlCompiler.catalog.addView(view);
			
			int end = viewName.lastIndexOf("_");
			String realViewName = viewName.substring(0, end);
			catalog.addIndexTable(realViewName, viewName);
			
			/*
			 * add the query inteface. 12/24/2013
			 */
			ArrayList<String> dependedRandomTableList = new ArrayList<String>();
			dependedRandomTableList.addAll(dependedRandomTableSet);
			catalog.addMCDependecy(viewName, dependedRandomTableList);
		}
	}

	public boolean isToSave() {
		return toSave;
	}

	public void setToSave(boolean toSave) {
		this.toSave = toSave;
	}
	
	
}
