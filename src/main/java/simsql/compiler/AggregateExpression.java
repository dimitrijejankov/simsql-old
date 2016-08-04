

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


package simsql.compiler; // package mcdb.compiler.parser.expression.mathExpression;


import java.util.ArrayList;
import simsql.runtime.DataType;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;
// import mcdb.compiler.parser.expression.util.FinalVariable;



public class AggregateExpression extends MathExpression {
	
	public int aggType;
	public int setQuantifier;
	public MathExpression expression;
	
	
	
	public AggregateExpression(int aggType) {
		this(aggType, FinalVariable.ALL, null);	
	}

	public AggregateExpression(int aggType, 
			                   int setQuantifier,
			                   MathExpression expression) 
	{
		super();
		this.aggType = aggType;
		this.setQuantifier = setQuantifier;
		this.expression = expression;
	}
	
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitAggregateExpression(this);	
	}

}
