

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
package simsql.compiler; 

import java.util.ArrayList;



public class FunctionDefineChecker extends TypeChecker {

	public FunctionDefineChecker(boolean allowDuplicatedAttributeAlias) throws Exception{
		super(allowDuplicatedAttributeAlias);
	}

	public FunctionDefineChecker() throws Exception
	{
		super(false);
	}

	public boolean visitFunctionDefinitionStatement(
			FunctionDefinitionStatement definitionStatement)throws Exception 
	{
		String funcName  = definitionStatement.getName ();
		ArrayList<SQLExpression> attributeStatementList = definitionStatement.getInputAttributeList ();
		String typeString = definitionStatement.getReturnType ().getTypeString ();
	
		int errorNum = 0;
		
		boolean subCheck;
		
		Function func = SimsqlCompiler.catalog.getFunction (funcName);
		
		if(func != null)
		{
			errorNum ++;
			subCheck = false;
			System.err.println("Defined Function [" + funcName + "] exists!");
		}
		
		if (definitionStatement.getReturnType().getType() == null){
			subCheck = false;
			System.err.println("I did not recognize attribute type " + typeString);
			
		}
  //               if(!typeString.equals("integer") &&											// TO-DO
  //                  !typeString.equals("double") &&
  //                  !typeString.equals("char"))
  //               {
  //                       subCheck = false;
  //                       System.err.println("I did not recognize attribute type " + typeString);
  //               }

		// try
  //               {
  //                       if(!typeString.equals("integer") && !typeString.equals("double"))
  //                       {
  //                               int size = Integer.parseInt(typeSize);
  //                               if(size <= 0)
  //                               {
  //                                       subCheck = false;
  //                                       System.err.println("The attribute size [" + typeSize + "] should be positive!");
  //                               }
  //                       }
  //                       else
  //                       {
  //                               if(typeSize != null)
  //                               {
  //                                       subCheck = false;
  //                                       System.err.println("The attribute size [" + typeSize + "] should not be set!");
  //                               }
  //                       }
  //               }
  //               catch(Exception e)
  //               {
  //                       System.err.println ("Processing return val found a type size of " + typeSize + "; expected an int.");
  //                       subCheck = false;
  //               }

		if(attributeStatementList == null)
		{
			errorNum ++;
			subCheck = false;
			System.err.println("Defined Function [" + funcName + "] has no attributes!");
		}
		else
		{
			ArrayList<String> attributeList = new ArrayList<String>();
			
			for(int i = 0; i < attributeStatementList.size(); i++)
			{
				SQLExpression expression = attributeStatementList.get(i);
				subCheck = expression.acceptVisitor(this);
				if(!subCheck)
				{
					errorNum ++;
					throw new Exception("Function definition error!");
				}
				
				if(expression instanceof AttributeDefineElement)
				{
					String attributeName = ((AttributeDefineElement)expression).attributeName;
					
					if(attributeList.contains(attributeName))
					{
						subCheck = false;
						errorNum ++;
						System.err.println("Defined Function [" + funcName + 
								"] has repeated definition for input attributes!");
					}
					else
					{
						attributeList.add(attributeName);
					}
				} else {
					throw new RuntimeException ("Why did I get a non-attribute in here??");
				}
			}
		}
		
		if(errorNum > 0)
		{
			System.err.println("Type check wrong in Function definition [" + funcName + "]");
			throw new Exception("Type check wrong in Function definition [" + funcName + "]");
		}
		else
		{
			return true;
		}
	}
}
