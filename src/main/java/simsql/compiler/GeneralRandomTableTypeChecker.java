

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
// import mcdb.catalog.DataAccess;
// import mcdb.catalog.View;
// import mcdb.compiler.parser.expression.sqlExpression.DefinedTableSchema;
// import mcdb.compiler.parser.expression.sqlType.GeneralRandomTableStatement;

/**
 * @author Bamboo
 *
 */
public class GeneralRandomTableTypeChecker extends RandomTableTypeChecker
{

	/**
	 * @param allowDuplicatedAttributeAlias
	 * @throws Exception
	 */
	public GeneralRandomTableTypeChecker(boolean allowDuplicatedAttributeAlias)
			throws Exception {
		super(allowDuplicatedAttributeAlias);
	}

	/**
	 * @param generalRandomTableStatement
	 * @return
	 */
	public boolean visitGeneralRandomTableStatement(
			GeneralRandomTableStatement generalRandomTableStatement) throws Exception
	{
		return super.visitRandomTableStatement(generalRandomTableStatement);
	}

	public void saveView(DefinedTableSchema tableAttributes, 
            ArrayList<DataType> gottenAttributeTypeList,
            String sql) throws Exception
	{

		ArrayList<Attribute> schema = saveAttributes(tableAttributes, gottenAttributeTypeList);
		String viewName = tableAttributes.getViewName();

		View view = new View(viewName, sql, schema, DataAccess.OBJ_RANDRELATION);
		catalog.addView(view);
		
		int end = viewName.lastIndexOf("_");
		String realViewName = viewName.substring(0, end);
		catalog.addIndexTable(realViewName, viewName);
		
		catalog.addMCDependecy(viewName, this.getIndexedRandomTableList());
	}
}
