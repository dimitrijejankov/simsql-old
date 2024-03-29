

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


// import mcdb.compiler.parser.astVisitor.ASTVisitor;
// import mcdb.compiler.parser.expression.mathExpression.MathExpression;

import simsql.compiler.expressions.MathExpression;

/**
 * @author Bamboo
 *
 */
public class LikePredicate extends BooleanPredicate{

	public MathExpression value;
	public MathExpression pattern;
	
	
	public LikePredicate(MathExpression value, MathExpression pattern) {
		super();
		this.value = value;
		this.pattern = pattern;
	}


	/* (non-Javadoc)
	 * @see boolPredicate.BooleanPredicate#AcceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor) throws Exception{
		return astVisitor.visitLikePredicate(this);
	}
	
}
