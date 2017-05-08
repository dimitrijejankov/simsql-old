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

package simsql.compiler.expressions; // package mcdb.compiler.parser.expression.mathExpression;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.compiler.BooleanPredicate;
import simsql.runtime.DataType;


/**
 * Represents a boolean predicate in the AST
 */
public class PredicateWrapper extends MathExpression
{

	/**
	 * boolean predicate
	 */
    @JsonProperty("predicate")
    public BooleanPredicate predicate;

    @JsonCreator
	public PredicateWrapper(@JsonProperty("predicate") BooleanPredicate predicate) {
		super();
		this.predicate = predicate;
	}

	/**
	 * @param astVisitor Type checker
	 * @return the type of the predicate
	 * @throws Exception if the expression is not valid throws an exception
	 */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitPredicateWrapper(this);	
	}

}
