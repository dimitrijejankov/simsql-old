

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
public class AtomOperator extends BooleanOperator{

	/**
	 * @param name
	 */
	public AtomOperator(String name) {
		super(name);
	}

	/* (non-Javadoc)
	 * @see logicOperator.booleanOperator.BooleanOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		/*
		 * Such atomic operator should return "". Since it only provides
		 * the name of the operator, and we do not need to provide new lines.
		 */
		return "";
	}

	/* (non-Javadoc)
	 * @see mcdb.compiler.logicPlan.logicOperator.booleanOperator.BooleanOperator#copy()
	 */
	@Override
	public BooleanOperator copy(CopyHelper copyHelper) {
		String c_name = new String(this.getName());
		
		return new AtomOperator(c_name);
	}
	
	
}
