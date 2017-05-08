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

package simsql.compiler.boolean_operator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CopyHelper;

/**
 * Represents an atomic boolean operator (constant)
 */
public class AtomOperator extends BooleanOperator{

	@JsonCreator
	public AtomOperator(@JsonProperty("name") String name) {
		super(name);
	}

	/**
	 * Such atomic operator should return "". Since it only provides
	 * the name of the operator, and we do not need to provide new lines.
	 * @see simsql.compiler.boolean_operator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {

		return "";
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#copy(CopyHelper)
	 */
	@Override
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = this.getName();
		
		return new AtomOperator(c_name);
	}
	
	
}
