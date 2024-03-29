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
import simsql.compiler.CopyHelper;
import simsql.compiler.TranslatorHelper;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * the or boolean operator
 */
public class OrOperator extends BooleanOperator {

    /**
     * list of operators that the or is gonna be preformed on
     */
    @JsonProperty("operator-list")
	private ArrayList<BooleanOperator> operatorList;

	public OrOperator(String name) {
		super(name);
	}
	
	public OrOperator(@JsonProperty("name") String name,
                      @JsonProperty("operator-list") ArrayList<BooleanOperator> operatorList)
	{
		this(name);
		this.operatorList = operatorList;
	}

    /**
     * Returns the list of operators the OR is gonna be preformed on.
     * @return the list of operators
     */
	public ArrayList<BooleanOperator> getOperatorList() {
		return operatorList;
	}

    /**
     * Sets the list of operators the OR is gonna be preformed on.
     * @param operatorList the new list of operators
     */
	public void setOperatorList(ArrayList<BooleanOperator> operatorList) {
		this.operatorList = operatorList;
	}

    /**
     * @see simsql.compiler.boolean_operator.BooleanOperator#visitNode()
     */
	@Override
	public String visitNode() {
		String result = "boolOr(" + this.getName() + ", [";
		
		if(operatorList != null)
		{
			for(int i = 0 ; i < operatorList.size(); i++)
			{
				result += operatorList.get(i).getName();
				
				if(i != operatorList.size() - 1)
				{
					result += ", ";
				}
			}
		}
		
		result += "]).\r\n";
		
		if(operatorList != null)
		{
			for (BooleanOperator anOperatorList : operatorList) {
				result += anOperatorList.visitNode();
			}
		}
		return result;
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#changeProperty(HashMap, TranslatorHelper)
	 */
	@Override
	public void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper) {
		super.changeProperty(indices, translatorHelper);

		// change the properties of the operator list
		for (BooleanOperator subPredicate : operatorList) {
			subPredicate.changeProperty(indices, translatorHelper);
		}
	}

	/**
	 * @see simsql.compiler.boolean_operator.BooleanOperator#copy(CopyHelper)
	 */
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = this.getName();
		ArrayList<BooleanOperator> c_operatorList = copyHelper.copyBooleanOperatorList(operatorList);

		return new OrOperator(c_name, c_operatorList);
	}
}
