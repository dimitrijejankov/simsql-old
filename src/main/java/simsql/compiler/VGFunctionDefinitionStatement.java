

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
package simsql.compiler; // package mcdb.compiler.parser.expression.sqlType;

import java.util.ArrayList;
import java.util.HashMap;
import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class VGFunctionDefinitionStatement extends Expression {
	
	// the name of the VG function
	private String vgName;

	// the name of the file where the VG function is physically loacated
	private String vgPath;

	// the list of input attributes
	private ArrayList <SQLExpression> inputAttributeList;	

	// the list of output attributes
	private ArrayList <SQLExpression> outputAttributeList;	

	public String getName () {
		return vgName;
	}
	
        public String getPath () {
		return vgPath;
	}

	public ArrayList <SQLExpression> getInputList () {
		return inputAttributeList;
	}

	public ArrayList <SQLExpression> getOutputList () {
		return outputAttributeList;
	}

	public VGFunctionDefinitionStatement (String vgNameIn, ArrayList <SQLExpression> inputAttributeListIn,
		ArrayList <SQLExpression> outputAttributeListIn, String vgPathIn) {
		vgName = vgNameIn;
		vgPath = vgPathIn;
		inputAttributeList = inputAttributeListIn;
		outputAttributeList = outputAttributeListIn;
	}
	
	public boolean acceptVisitor (VGFunctionDefineChecker astVisitor) throws Exception {
		return astVisitor.visitVGFunctionDefinitionStatement (this);
	}

	public void save() throws Exception {

		ArrayList <Attribute> inputAttList = new ArrayList<Attribute>();
		ArrayList <Attribute> outputAttList = new ArrayList<Attribute>();
		
		for (SQLExpression expression : inputAttributeList) {
			if (expression instanceof AttributeDefineElement) {
				String attributeName = ((AttributeDefineElement) expression).attributeName;
				DataType type = ((AttributeDefineElement) expression).getType ();
				Attribute attribute = new Attribute (attributeName, type, vgName, "nothing", "infinite");
				inputAttList.add(attribute);
			} else {
				throw new RuntimeException ("Got some weird attribute info in VG function definition");
			}
		}

		for (SQLExpression expression : outputAttributeList) {
			if (expression instanceof AttributeDefineElement) {
				String attributeName = ((AttributeDefineElement) expression).attributeName;
				DataType type = ((AttributeDefineElement) expression).getType ();
				Attribute attribute = new Attribute (attributeName, type, vgName, "nothing", "infinite");
				outputAttList.add(attribute);
			} else {
				throw new RuntimeException ("Got some weird attribute info in VG function definition");
			}
		}

		VGFunction vgf = new VGFunction(vgName, 1, inputAttList, outputAttList);
		SimsqlCompiler.catalog.addVGFunction(vgf);
	}
}
