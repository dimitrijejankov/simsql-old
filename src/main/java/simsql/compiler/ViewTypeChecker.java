

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
import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class ViewTypeChecker extends TypeChecker{

	/*
	 * When we get the view definition from the catalog, we are not allowed to save it again!
	 * Otherwise, it would cause the error!
	 */
	private boolean toSave = true;
	/**
	 * @param allowDuplicatedAttributeAlias
	 */
	public ViewTypeChecker(boolean allowDuplicatedAttributeAlias)  throws Exception{
		super(allowDuplicatedAttributeAlias);
	}
	
	public boolean visitViewStatement(ViewStatement viewStatement)throws Exception {
		
		//record this statements
		if(statement == null)
		{
			this.statement = viewStatement;
		}
		
		DefinedTableSchema definedSchema = viewStatement.definedTableSchema;
		SelectStatement statement = viewStatement.statement;
		
		boolean subcheck;
		
		if(toSave)
		{
			if(definedSchema == null)
			{
				System.err.println("Schema for view should be not NULL!");
				throw new Exception("Schema for view should be not NULL!");
			}
			subcheck = definedSchema.acceptVisitor(this);
		}
		else
			subcheck = true;
		
		if(!subcheck)
		{
			return false;
		}
		
		//In the view statement, it is not allowed to have duplicated attribute alias.
		this.setAllowDuplicatedAttributeAlias(false);
		
		if(statement == null)
		{
			System.err.println("Select Statement in View clause should be not NULL!");
			throw new Exception("Select Statement in View clause should be not NULL!");
		}
		subcheck = statement.acceptVisitor(this);
		
		if(!subcheck)
		{
			return false;
		}
		
		String viewName = definedSchema.getViewName();
		ArrayList<String> viewAttributeList = definedSchema.tableAttributeList;
		ArrayList<DataType> gottenAttributeTypeList = getTableAttributes(statement.selectList);
		
		String query = viewStatement.getSqlString();
		
		if(viewAttributeList!= null)
		{
			if(attributeList.size() != viewAttributeList.size())
			{
				System.err.println("The number of attributes defined in the random table [" +
						viewName + "] does not match its defintion in its subquery!");
			}
			else
			{
				if(toSave)
					saveView(definedSchema, gottenAttributeTypeList, query);
			}
		}
		else
		{
			//save the information to the catalog..in the end of this function
			if(toSave)
				saveView(definedSchema, gottenAttributeTypeList, query);
		}
		
		
		return subcheck;
	}
	
	
	
	public ArrayList<DataType> getTableAttributes(ArrayList<SQLExpression> selectList)throws Exception
	{
		ArrayList<DataType> attributeTypeList = new ArrayList<DataType>();
		
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			//select * from ..
			if(selectElement instanceof AsteriskTable)
			{
				for(int j = 0; j < fromList.size(); j ++)
				{
					String fromAlias = fromList.get(j);
					
					TableReference tempTableReference;
					String tableName;
					Relation relation;
					View view;
					ArrayList<Attribute> tempList;
					
					if(tableReferenceMap.containsKey(fromAlias))
					{
						tempTableReference = tableReferenceMap.get(fromAlias);
						tableName = tempTableReference.table;
						
						
						relation = SimsqlCompiler.catalog.getRelation(tableName);
						if(relation != null)
						{
							tempList = relation.getAttributes();
						}
						else
						{
							view = SimsqlCompiler.catalog.getView(tableName);
							tempList = view.getAttributes();
						}
						
						for(int k = 0; k < tempList.size(); k++)
						{
							attributeTypeList.add(tempList.get(k).getType());
						}
					}
					//subquery
					else if(typerCheckerMap.containsKey(fromAlias))
					{
						TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
						ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
						HashMap<String, DataType> attributeTypeInSubQuery = tempChecker.getAttributeTypeMap();
						
						for(int k = 0; k < outputAttributeStringList.size(); k++)
						{
							attributeTypeList.add(attributeTypeInSubQuery.get(outputAttributeStringList.get(k)));
						}
					}
				}
			}
			//select A.* from
			else if(selectElement instanceof AllFromTable)
			{
				String fromAlias = ((AllFromTable) selectElement).table;
				
				//if the tableAlias is from a table reference.
				if(tableReferenceMap.containsKey(fromAlias))
				{
					TableReference tempTableReference = tableReferenceMap.get(fromAlias);
					String tableName = tempTableReference.table;
					
					Relation relation = SimsqlCompiler.catalog.getRelation(tableName);
					ArrayList<Attribute> tempList;
					View view;
					if(relation != null)
					{
						tempList = relation.getAttributes();
					}
					else
					{
						view = SimsqlCompiler.catalog.getView(tableName);
						tempList = view.getAttributes();
					}
					
					for(int k = 0; k < tempList.size(); k++)
					{
						attributeTypeList.add(tempList.get(k).getType());
					}
				}
				//if the table alias is from a subquery in from clause.
				else if(typerCheckerMap.containsKey(fromAlias))
				{
					TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
					ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
					HashMap<String, DataType> attributeTypeInSubQuery = tempChecker.getAttributeTypeMap();
					
					for(int k = 0; k < outputAttributeStringList.size(); k++)
					{
						attributeTypeList.add(attributeTypeInSubQuery.get(outputAttributeStringList.get(k)));
					}
				}
			}
			//select A.B, B, expression
			else
			{
				String alias = ((DerivedColumn)selectElement).alias;
				attributeTypeList.add(attributeTypeMap.get(alias));
			}
		}
		
		return attributeTypeList;
	}

	public void saveView(DefinedTableSchema tableAttributes, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql) throws Exception
	{
		String viewName = tableAttributes.getViewName();
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

		View view = new View(viewName, sql, schema, DataAccess.OBJ_VIEW);
		SimsqlCompiler.catalog.addView(view);
	}

	public boolean isSaved() {
		return toSave;
	}

	public void setSaved(boolean toSave) {
		this.toSave = toSave;
	}


}
