

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


package simsql.compiler; // package mcdb.compiler.parser.expression.util;

/*
 * @Author: Bamboo
 *  Date: 09/03/2010
 *  This class corresponds to the parent class of Expression
 */

public class FinalVariable {
	
	//SetQuantifier
	public static final int ALL = 0;
	public static final int DISTINCT = 1;
	
	//type of aggregation type
	public static final int AVG = 0;
	public static final int SUM = 1;
	public static final int COUNT = 2;
	public static final int COUNTALL = 3;
	public static final int MIN = 4;
	public static final int MAX = 5;
	public static final int VARIANCE = 6;
	public static final int STDEV = 7;
	public static final int VECTOR = 8;
	public static final int ROWMATRIX = 9;
	public static final int COLMATRIX = 10;


	//arithic type
	public static final int PLUS = 0;
	public static final int MINUS = 1;
	public static final int TIMES = 2;
	public static final int DIVIDE = 3;
	public static final int MOD = 4;
	
	
	//comparison type
	public static final int EQUALS = 0;
	public static final int NOTEQUALS = 1;
	public static final int LESSTHAN = 2;
	public static final int GREATERTHAN = 3;
	public static final int LESSEQUAL = 4;
	public static final int GREATEREQUAL = 5;
	
	//the order of the output
	public static final int ASC = 0;
	public static final int DESC = 1;
	
}
