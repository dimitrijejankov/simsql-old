

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
package simsql.compiler; // package mcdb.compiler.parser.expression.mathExpression;

import java.util.ArrayList;
import simsql.runtime.DataType;


// import mcdb.compiler.parser.astVisitor.ASTVisitor;



/**
 * @author Bamboo
 *
 */
public class GeneralFunctionExpression extends MathExpression{

	public String functionName;
	public ArrayList<MathExpression> parasList;
	public ArrayList<DataType> outTypeList;
	
	// since this can either correspond to a VG function call or to a "regular" function
	// call, we remember which one
	private boolean isVGFunctionVal = false;
	
	
        public boolean isVGFunction () {
		return isVGFunctionVal;
	}

	public void setVGFunctionCall (boolean toMe) {
		isVGFunctionVal = toMe;
	}	

	public GeneralFunctionExpression(String functionName,
			ArrayList<MathExpression> parasList) {
		super();
		this.functionName = functionName.toLowerCase();
		this.parasList = parasList;
	}

	public void addParameter(MathExpression expression) 
	{
		parasList.add(expression);
	}

	/* (non-Javadoc)
	 * @see component.mathExpression.MathExpression#acceptVisitor(astVisitor.ASTVisitor)
	 */
	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
		return astVisitor.visitGeneralFunctionExpression(this);
	}


	/**
	 * @param typeList the typeList to set
	 */
	public void setOutTypeList(ArrayList<DataType> typeList) {
		this.outTypeList = typeList;
	}

	
}
