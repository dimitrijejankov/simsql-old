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
 * The not boolean operator
 */
public class NotOperator extends BooleanOperator {

	/**
	 * the boolean operator to be negated
	 */
	@JsonProperty("boolean-operator")
	private BooleanOperator booleanOperator;

	public NotOperator(String name) {
		super(name);
	}

	@JsonCreator
	public NotOperator(@JsonProperty("name") String name,
					   @JsonProperty("boolean-operator") BooleanOperator booleanOperator) {
		super(name);
		this.booleanOperator = booleanOperator;
	}

	/**
	 * returns the boolean operator to be negated
	 * @return the boolean operator
	 */
	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}

	/**
	 * sets the boolean operator to be negated
	 * @param booleanOperator the new operator to be negated
	 */
	public void setBooleanOperator(BooleanOperator booleanOperator) {
		this.booleanOperator = booleanOperator;
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "boolNot(" + this.getName() + ", [";
		result += booleanOperator.getName();
		result += "]).\r\n";
		
		result += booleanOperator.visitNode();
		
		return result;
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#copy(CopyHelper)
	 */
	@Override
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = this.getName();
		return new NotOperator(c_name, this.booleanOperator.copy(copyHelper));
	}
	
}
