

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
package simsql.compiler; // package mcdb.compiler.parser.unnester;

import java.util.ArrayList;



// import mcdb.compiler.parser.expression.boolExpression.BooleanPredicate;



/**
 * @author Bamboo
 *
 */
public class UnnestedSelectStatementJoin {
	public ArrayList<UnnestedSelectStatement> viewList;
	public boolean semi_join;
	public BooleanPredicate predicate;
	
	public UnnestedSelectStatementJoin(ArrayList<UnnestedSelectStatement> viewList,
			                         BooleanPredicate predicate,
			                         boolean semi_join)
	{
		this.viewList = viewList;
		this.predicate = predicate;
		this.semi_join = semi_join;
	}

	public BooleanPredicate getPredicate() {
		return predicate;
	}

	public void setPredicate(BooleanPredicate predicate) {
		this.predicate = predicate;
	}
}
