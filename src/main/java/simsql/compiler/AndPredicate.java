

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
package simsql.compiler; // package mcdb.compiler.parser.expression.boolExpression;

import java.util.ArrayList;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;



/**
 * @author Bamboo
 *
 */
public class AndPredicate extends BooleanPredicate {
	
	public ArrayList<BooleanPredicate> andList;
	
	
	public AndPredicate(ArrayList<BooleanPredicate> andList) {
		super();
		this.andList = andList;
	}
	
	public void addPredicate(BooleanPredicate predicate)
	{
		andList.add(predicate);
	}

	/* (non-Javadoc)
	 * @see boolPredicate.BooleanPredicate#AcceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitAndPredicate(this);
	}
	
	@Override
	public String toString()
	{
		String result = "";
		for(int i = 0; i < andList.size()-1; i++)
		{
			result += andList.get(i).toString() + " ^ ";
		}
		result = andList.get(andList.size()-1).toString();
		
		return result;
	}

}
