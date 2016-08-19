

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


public class DefinedModuloTableSchema extends DefinedTableSchema {
	String multiplier;
	String offset;

	public DefinedModuloTableSchema(String viewName,
									String multiplier,
									String offset,
									ArrayList<String> tableAttributeList)
	{
		super(viewName, tableAttributeList, true);
		this.viewName = viewName.toLowerCase() + "_mod_" + multiplier + "_" + offset + "_i";
		this.tableAttributeList = tableAttributeList;
		this.multiplier = multiplier;
		this.offset = offset;
	}

	public DefinedModuloTableSchema(String viewName,
                                    String multiplier,
                                    String offset)
	{
		this(viewName, multiplier, offset, null);
	}
	
	public String toString()
	{
		return viewName;
	}


	/* (non-Javadoc)
	 * @see component.sqlExpression.SQLExpression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitModuloTableSchemaExpression(this);
	}

	// Static methods for dealing with modulo tables.

    static boolean isModuloTableForIndex(String table, int index) {

        if(!table.matches(".*_mod_[0-9]+_[0-9]+\\Q[i]\\E$"))
            return false;

        int idx = table.lastIndexOf("_mod");

        String[] splits = table.substring(idx + 1).split("_");
        String offsetString = splits[2].substring(0, splits[2].length()-3);

        Integer multiplier = Integer.parseInt(splits[1]);
        Integer offset = Integer.parseInt(offsetString);

        return (index % multiplier) - offset == 0;
    }

    static boolean isModuloPrefixTableForIndex(String table, int index) {

        if(!table.matches(".*_mod_[0-9]+_[0-9]+\\Q_i\\E$"))
            return false;

        int idx = table.lastIndexOf("_mod");

        String[] splits = table.substring(idx + 1).split("_");
        String offsetString = splits[2].substring(0, splits[2].length());

        Integer multiplier = Integer.parseInt(splits[1]);
        Integer offset = Integer.parseInt(offsetString);

        return (index % multiplier) - offset == 0;
    }
	
}
