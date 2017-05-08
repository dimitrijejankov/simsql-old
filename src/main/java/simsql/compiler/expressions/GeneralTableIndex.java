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


package simsql.compiler.expressions;

import java.util.ArrayList;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.ASTVisitor;
import simsql.runtime.DataType;
import simsql.runtime.IntType;


/**
 * Represents a general table index inside a math expression, it't value is the value of the index it represents.
 * The index is specified by the identifier.
 */
public class GeneralTableIndex extends MathExpression {


    /**
     * The identifier of the general index {i, j, k...}
     */
    @JsonProperty("identifier")
	public String identifier;


    @JsonCreator
	public GeneralTableIndex(@JsonProperty("identifier") String identifier)
	{
		this.identifier = identifier;
	}

	@Override
	public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor)  throws Exception{
		return astVisitor.visitGeneralTableIndexExpression(this);	
	}

    /**
     * Returns the type of this expression (always IntType)
     * @return the type of this expression
     */
    @JsonIgnore
	public DataType getType() {
		return new IntType();
	}

    /**
     * Returns the string representation of the GeneralTableIndex (the identifier name)
     * @return string representation
     */
	@Override
	public String toString()
	{
		return identifier;
	}
}
