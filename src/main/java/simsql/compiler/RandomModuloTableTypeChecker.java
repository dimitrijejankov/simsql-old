

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

import simsql.runtime.DataType;

import java.util.ArrayList;

/**
 * @author Dimitrije
 *
 */
public class RandomModuloTableTypeChecker extends GeneralRandomTableTypeChecker {

	/**
	 * @param allowDuplicatedAttributeAlias
	 * @throws Exception
	 */
	public RandomModuloTableTypeChecker(boolean allowDuplicatedAttributeAlias) throws Exception {
		super(allowDuplicatedAttributeAlias);
	}

	public boolean visitModuloRandomTableStatement(ModuloRandomTableStatement statement) throws Exception {
        return super.visitRandomTableStatement(statement);
	}

    @Override
    public void saveView(DefinedTableSchema tableAttributes, ArrayList<DataType> gottenAttributeTypeList, String sql) throws Exception {
        super.saveToCatalog(tableAttributes, gottenAttributeTypeList, sql);

        String viewName = tableAttributes.viewName;

        int end = viewName.lastIndexOf("_mod");
        String realViewName = viewName.substring(0, end);

        catalog.addIndexTable(realViewName, viewName);
        catalog.addMCDependecy(viewName, this.getIndexedRandomTableList());
    }
}
