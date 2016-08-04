

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
package simsql.compiler; // package mcdb.compiler.parser.expression.sqlType;

import java.util.ArrayList;
import java.util.HashMap;
import simsql.runtime.DataType;

// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.Relation;
// import mcdb.catalog.ForeignKey;
// import mcdb.compiler.parser.astVisitor.TableDefineChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.Expression;
// import mcdb.compiler.parser.expression.sqlExpression.AttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.ForeignKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.PrimaryKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;

/**
 * @author Bamboo
 *
 */
public class TableDefinitionStatement extends Expression
{
	public String tableName;
	/*
	 * Here attributeStatementList can be definition for table attribute,
	 * primaryKeyConstraint, or foreignKeyConstraint.
	 */
	public ArrayList<SQLExpression> attributeStatementList;
	
        public String getTableName () {
          return tableName;
        }
	
	public TableDefinitionStatement(String tableName,
			ArrayList<SQLExpression> attributeStatementList) {
		super();
		this.tableName = tableName.toLowerCase();
		this.attributeStatementList = attributeStatementList;
	}
	
	public boolean acceptVisitor(TableDefineChecker astVisitor)throws Exception {
		return astVisitor.visitTableDefinitionStatement(this);
	}

	public void save() throws Exception
	{
		ArrayList<Attribute> attributeList = new ArrayList<Attribute>();
		HashMap<String, Attribute> nameAttributeMap = new HashMap<String, Attribute>();
		ArrayList<String> primaryKeyList = null;
		ArrayList<ForeignKey> foreignKeys = new ArrayList<ForeignKey>();
		
		for(int i = 0; i < attributeStatementList.size(); i++)
		{
			SQLExpression expression = attributeStatementList.get(i);
			if(expression instanceof AttributeDefineElement)
			{
				String attributeName = ((AttributeDefineElement)expression).attributeName;
				DataType type = ((AttributeDefineElement)expression).getType();
				
				Attribute attribute = new Attribute(attributeName, 
													type,
													tableName,
													10
													);
				attributeList.add(attribute);
				nameAttributeMap.put(attributeName, attribute);
			}
			else if(expression instanceof PrimaryKeyElement)
			{
				primaryKeyList = ((PrimaryKeyElement)expression).keyList;
			}
			else if(expression instanceof ForeignKeyElement)
			{
				String attributeStr  = ((ForeignKeyElement)expression).attribute;
				String referencedRelationStr  = ((ForeignKeyElement)expression).referencedRelation;
				String referencedAttributeStr  = ((ForeignKeyElement)expression).referencedAttribute;
				
				Attribute attribute = nameAttributeMap.get(attributeStr);
				Relation targetRelation = SimsqlCompiler.catalog.getRelation(referencedRelationStr);
				ArrayList<Attribute> targetAttributeList = targetRelation.getAttributes();
				Attribute targetAttribute = null;
				
				for(int j = 0; j < targetAttributeList.size(); j++)
				{
					if(targetAttributeList.get(j).getName().equals(referencedAttributeStr))
					{
						targetAttribute = targetAttributeList.get(j);
					}
				}
				
				if(targetAttribute != null)
				{
					ForeignKey foreignKey = new ForeignKey(attribute, targetAttribute);
					foreignKeys.add(foreignKey);
				}
			}
		}
		
		Relation relation = new Relation(tableName,
									 null,
									 attributeList,
									 primaryKeyList,
									 foreignKeys,
									 10
									 );
		
		SimsqlCompiler.catalog.addRelation(relation);
	}
}
