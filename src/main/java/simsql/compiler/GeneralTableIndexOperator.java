

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


package simsql.compiler;
import simsql.runtime.DataType;


public class GeneralTableIndexOperator implements MathOperator{
	private DataType type;
	private boolean initialized;
	private int value;
	
	public GeneralTableIndexOperator(DataType type)
	{
		this.type = type;
		this.initialized = false;
	}

	public DataType getType() {
		return type;
	}

	public void setType(DataType type) {
		this.type = type;
	}

	public void setValue(int value) {
		this.value = value;
		initialized = true;
	}


	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		return "";
	}

	
	
	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		if(!initialized)
			return "i";
		else
			return value+"";
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator#copy()
	 */
	@Override
	public MathOperator copy(CopyHelper copyHelper) {
		return new GeneralTableIndexOperator(type);
	}
}
