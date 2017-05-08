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

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import simsql.compiler.ASTVisitor;
import simsql.compiler.Expression;
import simsql.runtime.DataType;


@JsonTypeInfo(
		use = JsonTypeInfo.Id.NAME,
		include = JsonTypeInfo.As.PROPERTY,
		property = "class-name")
@JsonSubTypes({
		@JsonSubTypes.Type(value = AggregateExpression.class, name = "aggregate-expression"),
		@JsonSubTypes.Type(value = ArithmeticExpression.class, name = "arithmetic-expression"),
		@JsonSubTypes.Type(value = AsteriskExpression.class, name = "asterisk-expression"),
		@JsonSubTypes.Type(value = ColumnExpression.class, name = "column-expression"),
		@JsonSubTypes.Type(value = DateExpression.class, name = "date-expression"),
		@JsonSubTypes.Type(value = GeneralFunctionExpression.class, name = "general-function-expression"),
		@JsonSubTypes.Type(value = GeneralTableIndex.class, name = "general-table-index"),
		@JsonSubTypes.Type(value = NumericExpression.class, name = "numeric-expression"),
		@JsonSubTypes.Type(value = PredicateWrapper.class, name = "predicate-wrapper"),
		@JsonSubTypes.Type(value = SetExpression.class, name = "set-expression"),
		@JsonSubTypes.Type(value = StringExpression.class, name = "string-expression"),
		@JsonSubTypes.Type(value = SubqueryExpression.class, name = "subquery-expression")
})
public abstract class MathExpression extends Expression {
	/**
	 * return the type of this math expression
	 * @param astVisitor an instance of an ASTVisitor subclass
	 * @return the type of this math expression
	 * @throws Exception if something goes wrong in checking
	 */
	public abstract ArrayList<DataType> acceptVisitor(ASTVisitor astVisitor) throws Exception;
}
