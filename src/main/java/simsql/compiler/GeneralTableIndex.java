

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
import simsql.runtime.DataType;
import simsql.runtime.IntType;



public class GeneralTableIndex extends MathExpression{
	public DataType type;
	public String identifier;
	
	public GeneralTableIndex(String identifier)
	{

		this.type = new IntType();
		this.identifier = identifier;
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitGeneralTableIndexExpression(this);	
	}
	
	public DataType getType()
	{
		return type;
	}
	
	@Override
	public String toString()
	{
		return identifier;
	}
}
