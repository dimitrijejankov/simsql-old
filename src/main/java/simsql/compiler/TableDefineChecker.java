

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
import java.util.HashSet;
import simsql.runtime.DataType;


// import mcdb.catalog.Attribute;
// import mcdb.catalog.Relation;
// import mcdb.catalog.View;
// import mcdb.compiler.parser.expression.sqlExpression.AttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.ForeignKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.PrimaryKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlType.TableDefinitionStatement;

/**
 * @author Bamboo
 *
 */
public class TableDefineChecker extends TypeChecker{

	/**
	 * @param allowDuplicatedAttributeAlias
	 */
	public TableDefineChecker(boolean allowDuplicatedAttributeAlias) throws Exception{
		super(allowDuplicatedAttributeAlias);
	}
	
	public TableDefineChecker() throws Exception
	{
		super(false);
	}

	/**
	 * @param tableDefinitionStatement
	 * @return
	 */
	public boolean visitTableDefinitionStatement(
			TableDefinitionStatement tableDefinitionStatement) throws Exception
	{

		String tableName  = tableDefinitionStatement.tableName;
		ArrayList<SQLExpression> attributeStatementList = tableDefinitionStatement.attributeStatementList;
		
		int errorNum = 0;
		
		boolean subcheck;
		
		if(tableName.endsWith("_i"))
		{
			System.err.println("Schema [" + tableName + "] ends with \"_i\", which is not allowed!");
			return false;
		}
		else
		{
			int start = tableName.lastIndexOf("_");
			if(start >= 0)
			{
				String suffix = tableName.substring(start+1, tableName.length());
				if(suffix != null && suffix.length() >= 1)
				{
					boolean valid = false;
					for(int i = 0; i < suffix.length(); i++)
					{
						if(suffix.charAt(i) < '0' || suffix.charAt(i) > '9')
						{
							valid = true;
							break;
						}
					}
					
					if(!valid)
					{
						System.err.println("Schema [" + tableName + "] ends with \"_<number>\", which is not allowed!");
						return false;
					}
				}
			}
		}
		
		Relation relation = catalog.getRelation(tableName);
		View view = catalog.getView(tableName);
		
		if(relation != null)
		{
			errorNum ++;
			subcheck = false;
			System.err.println("Defined table [" + tableName + "] exists!");
		}
		
		if(view!= null)
		{
			errorNum ++;
			subcheck = false;
			System.err.println("Defined view [" + tableName + "] exists!");
		}
		
		if(attributeStatementList == null || attributeStatementList.size() <= 0)
		{
			errorNum ++;
			subcheck = false;
			System.err.println("Defined table [" + tableName + "] has no attributes!");
		}
		else
		{
			ArrayList<String> attributeNameList = new ArrayList<String>();
			boolean hasPrimaryKey = false;
			HashSet<String> foreignKeySet = new HashSet<String>();
			HashMap<String, DataType> typeMap = new HashMap<String, DataType>();
			
			for(int i = 0; i < attributeStatementList.size(); i++)
			{
				SQLExpression expression = attributeStatementList.get(i);
				
				subcheck = expression.acceptVisitor(this);
				if(!subcheck)
				{
					errorNum ++;
					throw new Exception("Table definition error!");
				}
				
				if(expression instanceof AttributeDefineElement)
				{
					String attributeName = ((AttributeDefineElement)expression).attributeName;
					DataType type = ((AttributeDefineElement)expression).getType();
					if(attributeNameList.contains(attributeName))
					{
						subcheck = false;
						errorNum ++;
						System.err.println("Defined table [" + tableName + 
								"] has repeated definition for attributes!");
					}
					else
					{
						attributeNameList.add(attributeName);
						typeMap.put(attributeName, type);
					}
				}
				else if(expression instanceof PrimaryKeyElement)
				{
					if(hasPrimaryKey)
					{
						subcheck = false;
						errorNum ++;
						System.err.println("Repeated primary key definition in " +
								"table [" + tableName + "]!");
					}
					
					ArrayList<String> keyList = ((PrimaryKeyElement)expression).keyList;
					
					if(keyList.size() == 0)
					{
						subcheck = false;
						errorNum ++;
						System.err.println("Defined table [" + tableName + 
								"] has repeated definition for attributes!");
					}
					else
					{
						String primaryKeyElement;
						HashSet<String> set = new HashSet<String>();
						for(int j = 0; j < keyList.size(); j++)
						{
							primaryKeyElement = keyList.get(j);
							if(!attributeNameList.contains(primaryKeyElement))
							{
								subcheck = false;
								errorNum ++;
								System.err.println("Primary key element [" + primaryKeyElement + "] " +
										"is not found in the definition!");
							}
							
							if(set.contains(primaryKeyElement))
							{
								subcheck = false;
								errorNum ++;
								System.err.println("Primary key element [" + primaryKeyElement + "] " +
										"appears more than 1 time");
							}
							else
							{
								set.add(primaryKeyElement);
							}
						}
					}
				}
				else if(expression instanceof ForeignKeyElement)
				{
					String attribute  = ((ForeignKeyElement)expression).attribute;
					String referencedRelation  = ((ForeignKeyElement)expression).referencedRelation;
					String referencedAttribute  = ((ForeignKeyElement)expression).referencedAttribute;
					
					if(!attributeNameList.contains(attribute))
					{
						subcheck = false;
						errorNum ++;
						System.err.println("ForeignKey key name [" + attribute + "] " +
								"is not found in the definition!");
					}
					
					DataType type = typeMap.get(attribute);
					
					Relation temprelation = catalog.getRelation(referencedRelation);
					ArrayList<Attribute> attributeList = temprelation.getAttributes();
					Attribute tempattribute = null;
						
					for(int j = 0; j < attributeList.size(); j++)
					{
						if(attributeList.get(j).getName().equals(referencedAttribute))
						{
							tempattribute = attributeList.get(j);
						}
					}
					
					if(tempattribute == null || !tempattribute.getType().getClass().equals(type.getClass()))		// TO-DO
					{
						subcheck = false;
						errorNum ++;
						System.err.println("ForeignKey key name [" + attribute + "] " +
								" has contradiction in type matching!");
					}
					
					if(foreignKeySet.contains(attribute))
					{
						subcheck = false;
						errorNum ++;
						System.err.println("ForeignKey key name [" + attribute + "] " +
								"is repeated!");
					}
					else
					{
						foreignKeySet.add(attribute);
					}
				}
			}
		}
		
		if(errorNum > 0)
		{
			System.err.println("Type check wrong in table definition [" + tableName + "]");
			throw new Exception("Type check wrong in table definition [" + tableName + "]");
			
		}
		else
		{
			return true;
		}
	}
	
	
}
