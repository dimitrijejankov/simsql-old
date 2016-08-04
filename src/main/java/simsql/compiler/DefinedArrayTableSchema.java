

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


public class DefinedArrayTableSchema extends DefinedTableSchema {
	public String lowerBound;
	public String upBound;
	
	public DefinedArrayTableSchema(String viewName, 
								   String arrayString,  
								   ArrayList<String> tableAttributeList)
	{
		super(viewName, tableAttributeList, true);
		this.viewName = viewName.toLowerCase();
		this.tableAttributeList = tableAttributeList;
		int start = arrayString.indexOf("..");
		lowerBound = arrayString.substring(0, start);
		upBound = arrayString.substring(start+2, arrayString.length());
	}
	
	public DefinedArrayTableSchema(String viewName, 
			  String arrayString)
	{
		this(viewName, arrayString, null);
	}
	
	public String toString()
	{
		return viewName + "[" + lowerBound + "..." + upBound + "]";
	}


	/* (non-Javadoc)
	 * @see component.sqlExpression.SQLExpression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitDefinedArrayTableSchemaExpression(this);
	}
	
}
