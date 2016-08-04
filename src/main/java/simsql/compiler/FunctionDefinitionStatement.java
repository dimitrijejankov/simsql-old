

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

// import mcdb.catalog.Attribute;
// import mcdb.catalog.Catalog;
// import mcdb.catalog.ForeignKey;
// import mcdb.catalog.Relation;
// import mcdb.catalog.VGFunction;
// import mcdb.compiler.parser.astVisitor.TableDefineChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.astVisitor.VGFunctionDefineChecker;
// import mcdb.compiler.parser.expression.Expression;
// import mcdb.compiler.parser.expression.sqlExpression.AttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.ForeignKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.PrimaryKeyElement;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlExpression.VGAttributeDefineElement;
// import mcdb.compiler.parser.expression.sqlExpression.VGPathElement;

/**
 * @author Bamboo
 *
 */
public class FunctionDefinitionStatement extends Expression {
	
	// the name of the function
	private String funcName;

	// the name of the file where the function is physically loacated
	private String funcPath;

	// the list of input attributes
	private ArrayList <SQLExpression> inputAttributeList;	

	// the list of output attributes
	private TypeInfo outputAttribute;	

	public String getName () {
		return funcName;
	}

        public String getPath () {
                return funcPath;
        }

	public ArrayList <SQLExpression> getInputAttributeList () {
		return inputAttributeList;
	}

	public TypeInfo getReturnType () {
		return outputAttribute;
	}

	public FunctionDefinitionStatement (String funcNameIn, ArrayList <SQLExpression> inputAttributeListIn,
		TypeInfo outputAttributeIn, String funcPathIn) {
		funcName = funcNameIn;
		funcPath = funcPathIn;
		inputAttributeList = inputAttributeListIn;
		outputAttribute = outputAttributeIn;
	}
	
	public boolean acceptVisitor (FunctionDefineChecker astVisitor) throws Exception {
		return astVisitor.visitFunctionDefinitionStatement (this);
	}

	public void save() throws Exception {

		ArrayList <Attribute> inputAttList = new ArrayList<Attribute>();
		
		for (SQLExpression expression : inputAttributeList) {
			if (expression instanceof AttributeDefineElement) {
				String attributeName = ((AttributeDefineElement) expression).attributeName;
				DataType type = ((AttributeDefineElement) expression).getType ();
				Attribute attribute = new Attribute (attributeName, type, funcName, "nothing", "infinite");
				inputAttList.add(attribute);
			} else {
				throw new RuntimeException ("Got some weird attribute info in VG function definition");
			}
		}

		Function rf = new Function (funcName, inputAttList, new Attribute (funcName + "_output", outputAttribute.getType (), funcName));
		SimsqlCompiler.catalog.addFunction (rf);
	}
}
