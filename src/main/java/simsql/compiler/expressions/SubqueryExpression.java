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
import simsql.compiler.SelectStatement;
import simsql.compiler.TypeChecker;
import simsql.runtime.DataType;


public class SubqueryExpression extends MathExpression {

    /**
     * The select statement of the sub-query
     */
    @JsonProperty("value")
    public SelectStatement value;

    /**
     * A reference to the type checker
     */
    @JsonIgnore
    private TypeChecker typeChecker;

    /**
     * true if it needs a parent
     */
    @JsonProperty("need-parent")
    private boolean needParent;

    @JsonCreator
    public SubqueryExpression(@JsonProperty("value") SelectStatement value) {
        this.value = value;
        needParent = true;
    }

    /**
     * Returns the type checker of this sub-query
     * @return the type checker
     */
    public TypeChecker getTypeChecker() {
        return typeChecker;
    }

    /**
     * Sets the type checker of this sub-query
     * @param typeChecker the new type checker
     */
    public void setTypeChecker(TypeChecker typeChecker) {
        this.typeChecker = typeChecker;
    }

    /**
     * Marks this sub-query
     * True if this sub-query needs a parent
     * @param value the new value
     */
    public void setNeedParent(boolean value) {
        this.needParent = value;
    }

    /**
     * @return true if this sub-query needs a parent
     */
    public boolean getNeedParent() {
        return needParent;
    }

    /**
     * @param astVisitor Type checker
     * @return list of the sub-query record attribute types
     * @throws Exception if the expression is not valid throws an exception
     */
    @Override
    public ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception {
        return astVisitor.visitSubqueryExpression(this);
    }
}
