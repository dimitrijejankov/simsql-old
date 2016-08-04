

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
package simsql.compiler; // package mcdb.compiler.parser.unnester;

import java.util.ArrayList;
import java.util.HashMap;



// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.Relation;
// import mcdb.catalog.VGFunction;
// import mcdb.catalog.View;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.mathExpression.GeneralFunctionExpression;
// import mcdb.compiler.parser.expression.sqlExpression.*;


/**
 * @author Bamboo
 *
 */

public class UnnestHelper {
	public static ArrayList<String> getAttributeString(TypeChecker typeChecker, SQLExpression element)throws Exception
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		if(element instanceof TableReference)
		{
			String tableName = ((TableReference) element).table;
			HashMap<String, GeneralFunctionExpression> vgFunctionMap = typeChecker.getVgFunctionMap();
			Catalog catalog = SimsqlCompiler.catalog;
			
			if(vgFunctionMap.containsKey(tableName))
			{
				GeneralFunctionExpression expression = vgFunctionMap.get(tableName);
				VGFunction vgFunction = catalog.getVGFunction(expression.functionName);
				ArrayList<Attribute> tempList = vgFunction.getOutputAtts();
				for(int k = 0; k < tempList.size(); k++)
				{
					String name = tempList.get(k).getName();
					resultList.add(name);
				}
			}
			else
			{
				Relation relation = catalog.getRelation(tableName);
				ArrayList<Attribute> tempList;
				
				View view;
				if(relation != null)
				{
					tempList = relation.getAttributes();
				}
				else
				{
					view = catalog.getView(tableName);
					tempList = view.getAttributes();
				}
				
				for(int k = 0; k < tempList.size(); k++)
				{
					String name = tempList.get(k).getName();
					resultList.add(name);
				}
			}
		}
		else if(element instanceof FromSubquery)
		{
			String fromAlias = ((FromSubquery) element).alias;
			HashMap<String, TypeChecker> typerCheckerMap = typeChecker.getTyperCheckerMap();
			
			TypeChecker tempChecker = typerCheckerMap.get(fromAlias);
			ArrayList<String> outputAttributeStringList = tempChecker.getAttributeList();
			
			for(int k = 0; k < outputAttributeStringList.size(); k++)
			{
				String name = outputAttributeStringList.get(k);
				resultList.add(name);
			}
		}
		
		return resultList;
	}
	
}
