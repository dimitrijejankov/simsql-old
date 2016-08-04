

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.mathOperator;


// import mcdb.compiler.logicPlan.logicOperator.CopyHelper;
// import mcdb.compiler.parser.expression.util.FinalVariable;

/**
 * @author Bamboo
 *
 */
public class AggregateOperator implements MathOperator{
	private String name;
	
	/*
	 * The "type" is defined in Class expression.util.FinalVariable
	 *  public static final int AVG = 0;
	 *	public static final int SUM = 1;
	 *	public static final int COUNT = 2;
	 *	public static final int COUNTALL = 3;
	 *	public static final int MIN = 4;
	 *	public static final int MAX = 5;
	 *	public static final int VARIANCE = 6;
	 */
	private int type;
	
	/*
	 *  The "operator" can be:
	 *  	Expression
	 *  	Identifier (Column)
	 */
	private MathOperator childOperator;
	private String childOperatorType;

	public AggregateOperator(String name, 
			                 int type, 
			                 MathOperator childOperator,
			                 String childOperatorType) {
		super();
		this.name = name;
		this.type = type;
		this.childOperator = childOperator;
		this.childOperatorType = childOperatorType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public MathOperator getChildOperator() {
		return childOperator;
	}

	public void setChildOperator(MathOperator childOperator) {
		this.childOperator = childOperator;
	}

	public String getChildOperatorType() {
		return childOperatorType;
	}

	public void setChildOperatorType(String childOperatorType) {
		this.childOperatorType = childOperatorType;
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#visitNode()
	 */
	@Override
	public String visitNode() {
		String result = "aggExp(";
		result += this.getName();
		result += ", ";
		
		result += translaterAggregateType(type);
		result += ", ";
		
		result += childOperator.getNodeName();
		result += ", ";
		
		result += childOperatorType;
		result += ").\r\n";
		
		result += childOperator.visitNode();
		return result;
	}

	/* (non-Javadoc)
	 * @see logicOperator.mathOperator.MathOperator#getNodeName()
	 */
	@Override
	public String getNodeName() {
		return name;
	}

	public String translaterAggregateType(int type)
	{
		String result = "";
		
		switch(type)
		{
			case FinalVariable.AVG:
				result = "avg";
				break;
				
			case FinalVariable.SUM:
				result ="sum";
				break;
				
			case FinalVariable.COUNT:
				result = "count";
				break;
				
			case FinalVariable.COUNTALL:
				result = "countall";
				break;
				
			case FinalVariable.MIN:
				result = "min";
				break;
				
			case FinalVariable.MAX:
				result = "max";
				break;
				
			case FinalVariable.VARIANCE:
				result = "var";
				break;
				
			case FinalVariable.STDEV:
				result = "stdev";
				break;

			case FinalVariable.VECTOR:
				result = "vector";
				break;
				
			case FinalVariable.ROWMATRIX:
				result = "rowmatrix";
				break;
				
			case FinalVariable.COLMATRIX:
				result = "colmatrix";
				break;
		}
		
		return result;
	}
	
	public MathOperator copy(CopyHelper copyHelper)
	{
		String c_name = new String(name);
		int c_type = this.type;
		MathOperator c_childOperator = childOperator.copy(copyHelper);
		String c_childOperatorType = new String(this.childOperatorType);
		
		AggregateOperator c_aggregate = new AggregateOperator(c_name, 
															c_type, 
															c_childOperator,
															c_childOperatorType);
		
		return c_aggregate;
	}
}
