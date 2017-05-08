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

package simsql.compiler.math_operators; // package mcdb.compiler.logicPlan.logicOperator.mathOperator;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CopyHelper;


/**
 * The string math operator
 */
public class StringOperator implements MathOperator {

    /**
     * The value of the string operator
     */
    @JsonProperty("value")
    private String string;

    @JsonCreator
	public StringOperator(@JsonProperty("value") String string) {
		this.string = string;
	}

    /**
     * returns the value of the string operator
     * @return the value string
     */
	public String getString() {
		return string;
	}

    /**
     * sets the value of the string operator
     * @param string the new value
     */
	public void setString(String string) {
		this.string = string;
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
     * @return the name of this node (the value of the operator with single quotes 'value'
     */
	@Override
	public String getNodeName() {
		return "'" + string + "'";
	}

    /**
     * Copies the string operator
     * @param copyHelper the operator to be copied
     * @return the copy
     */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		return new StringOperator(string);
	}
	
}
