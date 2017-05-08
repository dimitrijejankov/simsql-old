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
import java.util.ArrayList;


/**
 * the set math operator
 */
public class SetOperator implements MathOperator {

	/**
	 * the name of the operator
	 */
	@JsonProperty("name")
	public String name;

	/**
	 * the list of math operators.. TODO give a good description of what this is
	 */
	@JsonProperty("element-list")
	private ArrayList<MathOperator> elementList;

	@JsonCreator
	public SetOperator(@JsonProperty("name") String name, @JsonProperty("element-list") ArrayList<MathOperator> elementList) {
		super();
		this.name = name;
		this.elementList = elementList;
	}

	/**
	 * Returns the name of the operator
	 * @return the name of the operator
	 */
	public String getName() {
		return name;
	}

	/**
	 * Sets the name of the operator
	 * @param name the name of the operator
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Returns the list of elements
	 * @return a reference to the list of elements
	 */
	public ArrayList<MathOperator> getElementList() {
		return elementList;
	}

	/**
	 * Sets a new list of elements
	 * @param elementList the new list of elements
	 */
	public void setElementList(ArrayList<MathOperator> elementList) {
		this.elementList = elementList;
	}

	/**
	 * Visits the current node and makes it's string representation for the relationship statistics
	 * @return the string for relationship statistics
	 */
	@Override
	public String visitNode() {
		String result = "";
		
		result += "set(";
		result += name;
		result += ", ";
		result += "[";
		
		for(int i = 0; i < elementList.size(); i++)
		{
			result += elementList.get(i).getNodeName();
			if(i != elementList.size() - 1)
			{
				result += ", ";
			}
		}
		result += "]).\r\n";

        for (MathOperator anElementList : elementList) {
            result += anElementList.visitNode();
        }
		
		return result;
	}

	/**
	 * Returns the name of this set operator
	 * @return the name string
	 */
	@Override
	public String getNodeName() {
		return name;
	}

    /**
     * Copies the set operator
     * @param copyHelper the operator to be copied
     * @return the copy
     */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.name);
		ArrayList<MathOperator> c_elementList = copyHelper.copyMathOperatorList(elementList);
		
		SetOperator setOperator = new SetOperator(c_name,c_elementList);
		return setOperator;
	}
}
