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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import simsql.compiler.CopyHelper;

/**
 * Represents the boolean operator
 */
@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "class-name")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AndOperator.class, name = "and-operator"),
		@JsonSubTypes.Type(value = AtomOperator.class, name = "atom-operator"),
		@JsonSubTypes.Type(value = CompOperator.class, name = "comp-operator"),
		@JsonSubTypes.Type(value = NotOperator.class, name = "not-operator"),
		@JsonSubTypes.Type(value = OrOperator.class, name = "or-operator")
})
public abstract class BooleanOperator {

	@JsonProperty("name")
	private String name;
	
	public BooleanOperator(String name)
	{
		this.name = name;
	}

	/**
	 * Returns the name of this operator
	 * @return the name
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of this operator
	 * @param name the new name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * returns the string representation of this operator for relation statistics
	 * @return the string
	 */
	public abstract String visitNode();

    /**
     * Copies this operator
     * @param copyHelper an instance of the copy helper class
     * @return the copy of this operator
     */
	public abstract BooleanOperator copy(CopyHelper copyHelper);
}
