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
import simsql.runtime.DataType;


/**
 * The number operator
 */
public class NumberOperator implements MathOperator {

    /**
     * The value of the number operator
     */
    @JsonProperty("value")
    public double value;

    /**
     * the data type of the operator
     */
    @JsonProperty("type")
	private DataType type;

    @JsonCreator
	public NumberOperator(@JsonProperty("value") double value, @JsonProperty("type") DataType type)
	{
		this.value = value;
		this.type = type;
	}

    /**
     * returns the value of the operator
     * @return the double value
     */
	public double getValue() {
		return value;
	}

    /**
     * Sets the new value of the operator
     * @param value the new value
     */
	public void setValue(double value) {
		this.value = value;
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
	    return type.getTypeName().equals("double") ? Double.toString(value) : Integer.toString((int)value);
	}

    /**
     * Gets the type of the operator
     * @return the type
     */
	public DataType getType() {
		return type;
	}

    /**
     * Copies the star operator
     * @param copyHelper the operator to be copied
     * @return the copy
     */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		return new NumberOperator(value, type);
	}
}
