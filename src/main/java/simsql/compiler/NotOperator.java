

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


/**
 * 
 */
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.booleanOperator;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;

/**
 * @author Bamboo
 *
 */
public class NotOperator extends BooleanOperator {

	private BooleanOperator booleanOperator;
	/**
	 * @param name
	 */
	public NotOperator(String name) {
		super(name);
	}
	
	public NotOperator(String name, BooleanOperator booleanOperator) {
		super(name);
		this.booleanOperator = booleanOperator;
	}
	
	public BooleanOperator getBooleanOperator() {
		return booleanOperator;
	}
	
	public void setBooleanOperator(BooleanOperator booleanOperator) {
		this.booleanOperator = booleanOperator;
	}

	/* (non-Javadoc)
	 * @see logicOperator.booleanOperator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "boolNot(" + this.getName() + ", [";
		result += booleanOperator.getName();
		result += "]).\r\n";
		
		result += booleanOperator.visitNode();
		
		return result;
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator#copy()
	 */
	@Override
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.getName());
		return new NotOperator(c_name, this.booleanOperator.copy(copyHelper));
	}
	
}
