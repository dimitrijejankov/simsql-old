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

package simsql.compiler.math_operators;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CopyHelper;
import simsql.compiler.TranslatorHelper;
import simsql.compiler.boolean_operator.BooleanOperator;
import simsql.runtime.DataType;

import java.util.HashMap;


/**
 * Represents the general table index operator [i]
 */
public class GeneralTableIndexOperator implements MathOperator {

	/**
	 * the data type of the general index operator (should always be integer)
	 */
	@JsonProperty("type")
	private DataType type;

	/**
	 * true if the general index operator has a value that is assigned to him
	 */
	@JsonProperty("initialized")
	private boolean initialized;

	/**
	 * the value that is assigned to the general table index operator
	 */
	@JsonProperty("value")
	private Integer value;

	/**
	 * the identifier of the general table index operator {i, j, k...}
	 */
	@JsonProperty("identifier")
	private String identifier;

	@JsonCreator
	public GeneralTableIndexOperator(@JsonProperty("type") DataType type, @JsonProperty("initialized") String identifier) {
        this.type = type;
        this.identifier = identifier;
        this.initialized = false;
	}

	/**
	 * Returns the identifier of the general table index operator
	 * @return the identifier string {i, j, k...}
	 */
	public String getIdentifier() {
        return identifier;
    }

	/**
	 * Returns the type of the general table index operator
	 * @return the type, should integer
	 */
	public DataType getType() {
		return type;
	}

	/**
	 * Assigns a new type to the general index operator
	 * @param type the new type
	 */
	public void setType(DataType type) {
		this.type = type;
	}

	/**
	 * Assigns a value to the general index operator
	 * @param value the new value
	 */
	public void setValue(int value) {
		this.value = value;
		initialized = true;
	}

	/**
	 * Such atomic operator should return "". Since it only provides the name of the operator, and we do not need to provide new lines.
	 * @return an empty string
	 */
	@Override
	public String visitNode() {
		return "";
	}

	/**
	 * returns the name of this node
	 * @return the name of this node (the name of the operator is the value in it's string form)
	 */
	@Override
	public String getNodeName() {
		if(!initialized)
			return identifier;
		else
			return value.toString();
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#changeProperty(HashMap, TranslatorHelper)
	 */
	@Override
	public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
		// assign the value for its identifier (i, j, k)....
		setValue(indices.get(getIdentifier()));
	}

	/**
	 * Copies the star operator
	 * @param copyHelper the operator to be copied
	 * @return the copy
	 */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		return new GeneralTableIndexOperator(type, identifier);
	}
}
