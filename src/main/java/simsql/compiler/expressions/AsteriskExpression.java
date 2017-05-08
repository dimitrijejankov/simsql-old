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

package simsql.compiler.expressions;

import java.util.ArrayList;

import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;


/**
 * Represents an asterisk expression inside the AST
 */
public class AsteriskExpression extends MathExpression {

	/**
	 * @param astVisitor Type checker
	 * @return a list of data types that correspond to the types of the columns
	 * @throws Exception if the expression is not valid throws an exception
	 */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitAsteriskExpression(this);
	}

}
