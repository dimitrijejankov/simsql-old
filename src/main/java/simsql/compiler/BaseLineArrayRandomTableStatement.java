

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


public class BaseLineArrayRandomTableStatement extends RandomTableStatement
{
	public String upBound;
	public String lowerBound;
	
	/**
	 * @param tableAttributes
	 * @param outerTable
	 * @param withList
	 * @param selectStatement
	 */
	public BaseLineArrayRandomTableStatement(DefinedTableSchema schema,
											 SQLExpression outerTable, 
											 ArrayList<WithStatement> withList,
											 SelectStatement selectStatement) {
		super(schema, outerTable, withList, selectStatement);
	}
	
	public BaseLineArrayRandomTableStatement(String arrayString,
											DefinedArrayTableSchema schema,
											SQLExpression outerTable, 
											ArrayList<WithStatement> withList,
											SelectStatement selectStatement)
	{
		super(schema, outerTable, withList, selectStatement);
		int start = arrayString.indexOf("..");
		lowerBound = arrayString.substring(0, start);
		upBound = arrayString.substring(start+2, arrayString.length());
	}

	
	public boolean acceptVisitor(BaselineArrayRandomTypeChecker astVisitor)throws Exception
	{
		return astVisitor.visitBaselineArrayRandomTableStatement(this);
	}
}
