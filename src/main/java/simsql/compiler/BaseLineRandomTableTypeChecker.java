

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

/**
 * @author Bamboo
 *
 */
public class BaseLineRandomTableTypeChecker extends RandomTableTypeChecker 
{
	public String indexString = "";
	/**
	 * @param allowDuplicatedAttributeAlias
	 * @throws Exception
	 */
	public BaseLineRandomTableTypeChecker(boolean allowDuplicatedAttributeAlias)
			throws Exception {
		super(allowDuplicatedAttributeAlias);
	}

	/**
	 * @param baseLineRandomTableStatement
	 * @return
	 */
	public boolean visitBaseLineRandomTableStatement(
			BaseLineRandomTableStatement baseLineRandomTableStatement) throws Exception
	{
		indexString = baseLineRandomTableStatement.indexString;
		
		int errorNum = 0;
		/*
		 * 2. indexString
		 */
		String indexString = baseLineRandomTableStatement.indexString;
		try
		{
			int index = Integer.parseInt(indexString);
			if(index < 0)
			{
				errorNum ++;
				System.err.println("The index [" + indexString +"] is not valid!" );
			}
		}
		catch(Exception e)
		{
			errorNum ++;
			System.err.println("The index [" + indexString +"] is not valid!" );
		}
		
		if(errorNum > 0)
		{
			return false;
		}
		
		return super.visitRandomTableStatement(baseLineRandomTableStatement);
	}
	
	public void saveView(DefinedTableSchema tableAttributes, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql) throws Exception
	{
		String viewName;
		
		viewName = tableAttributes.getViewName();

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
		catalog.addMCDependecy(viewName, this.getIndexedRandomTableList());
	}

}
