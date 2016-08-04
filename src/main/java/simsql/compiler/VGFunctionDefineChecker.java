

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
import java.util.HashSet;



// import mcdb.catalog.Attribute;
// import mcdb.catalog.VGFunction;
// import mcdb.compiler.parser.expression.sqlExpression.AttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.ForeignKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.PrimaryKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlExpression.VGAttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.VGPathElement;
// import mcdb.compiler.parser.expression.sqlType.VGFunctionDefinitionStatement;

/**
 * @author Bamboo
 *
 */
public class VGFunctionDefineChecker extends TypeChecker{

	/**
	 * @param allowDuplicatedAttributeAlias
	 */
	public VGFunctionDefineChecker(boolean allowDuplicatedAttributeAlias) throws Exception{
		super(allowDuplicatedAttributeAlias);
	}

	public VGFunctionDefineChecker() throws Exception
	{
		super(false);
	}

	
	public boolean visitVGFunctionDefinitionStatement(
			VGFunctionDefinitionStatement vgDefinitionStatement)throws Exception 
	{
		String vgName  = vgDefinitionStatement.getName ();
		ArrayList<SQLExpression> attributeStatementList = new ArrayList <SQLExpression> ();
		attributeStatementList.addAll (vgDefinitionStatement.getOutputList ());
		attributeStatementList.addAll (vgDefinitionStatement.getInputList ());
		
		int errorNum = 0;
		
		boolean subcheck;
		
		VGFunction vg = SimsqlCompiler.catalog.getVGFunction(vgName);
		
		if(vg != null)
		{
			errorNum ++;
			subcheck = false;
			System.err.println("Defined VGFunction [" + vgName + "] exists!");
		}
		
		if(attributeStatementList == null || attributeStatementList.size() <= 1)
		{
			errorNum ++;
			subcheck = false;
			System.err.println("Defined VGFunction [" + vgName + "] has no attributes!");
		}
		else
		{
			ArrayList<String> inputAttributeList = new ArrayList<String>();
			ArrayList<String> outputAttributeList = new ArrayList<String>();
			String path = null;
			
			for(int i = 0; i < attributeStatementList.size(); i++)
			{
				SQLExpression expression = attributeStatementList.get(i);
				subcheck = expression.acceptVisitor(this);
				if(!subcheck)
				{
					errorNum ++;
					throw new Exception("VGFunction definition error!");
				}
				
				if(expression instanceof AttributeDefineElement)
				{
					String attributeName = ((AttributeDefineElement)expression).attributeName;
					boolean input = ((AttributeDefineElement)expression).isInput();
					
					if(input)
					{
						if(inputAttributeList.contains(attributeName))
						{
							subcheck = false;
							errorNum ++;
							System.err.println("Defined VGFunction [" + vgName + 
									"] has repeated definition for input attributes!");
						}
						else
						{
							inputAttributeList.add(attributeName);
						}
					}
					else
					{
						if(outputAttributeList.contains(attributeName))
						{
							subcheck = false;
							errorNum ++;
							System.err.println("Defined VGFunction [" + vgName + 
									"] has repeated definition for output attributes!");
						}
						else
						{
							outputAttributeList.add(attributeName);
						}
					}
				}
			}
		}
		
		if(errorNum > 0)
		{
			System.err.println("Type check wrong in table definition [" + vgName + "]");
			throw new Exception("Type check wrong in table definition [" + vgName + "]");
		}
		else
		{
			return true;
		}
	}
}
