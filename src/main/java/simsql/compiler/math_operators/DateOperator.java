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

import java.util.HashMap;

/**
 * Represents the date math operator
 */
public class DateOperator implements MathOperator {

	/**
	 * The string value of the date
	 */
	@JsonProperty("value")
	private String string;

	@JsonCreator
	public DateOperator(@JsonProperty("value") String string)
	{
		this.string = string;
	}

	/**
	 * Returns the date as a string
	 * @return the date string
	 */
	public String getDateString() {
		return string;
	}

	/**
	 * sets the date from a string
	 * @param string the string value
	 */
	public void setDateString(String string) {
		this.string = string;
	}

	/**
	 * Does nothing
	 * @param indices the indices to be used
	 * @param translatorHelper an instance of the translator helper class
	 */
	@Override
	public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {}

	/**
     * Such atomic operator should return "". Since it only provides
     * the name of the operator, and we do not need to provide new lines.
     * @return always empty string
	 */
	@Override
	public String visitNode() {
		return "";
	}

	/**
	 * Returns the name of this node
	 * @return always the date value under single quotes ex. 'date'
	 */
	@Override
	public String getNodeName() {
		return "'" + string + "'";
	}

	/**
	 * Copies the date operator
	 * @param copyHelper the operator to be copied
	 * @return the copy
	 */
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_string = string;
		return new DateOperator(c_string);
	}
}
