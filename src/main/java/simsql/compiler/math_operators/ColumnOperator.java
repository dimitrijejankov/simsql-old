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
 * Represents the column math operator
 */
public class ColumnOperator implements MathOperator
{
	/**
	 * The name of the column this operator represents
	 */
	@JsonProperty("column-name")
	private String columnName;

	@JsonCreator
	public ColumnOperator(@JsonProperty("column-name") String columnName) {
		super();
		this.columnName = columnName;
	}

	/**
	 * returns the name of the column this operator represents
	 * @return the name of the column
	 */
	public String getColumnName() {
		return columnName;
	}

	/**
	 * r
	 * @param columnName
	 */
	public void setColumnName(String columnName) {
		this.columnName = columnName;
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
	 * @return always an empty string
     */
	@Override
	public String visitNode() {
		return "";
	}

	/**
	 * returns the name of this node
	 * @return always the name of the column this operator represents
	 */
	@Override
	public String getNodeName() {
		return columnName;
	}

	/**
	 * Copies the column operator
	 * @param copyHelper the operator to be copied
	 * @return the copy
	 */
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_columnName = new String(columnName);
		ColumnOperator c_operator = new ColumnOperator(c_columnName);
		
		return c_operator;
	}
	
}
