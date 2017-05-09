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

package simsql.compiler.math_operators;

import com.fasterxml.jackson.annotation.*;
import simsql.compiler.CopyHelper;
import simsql.compiler.TranslatorHelper;

import java.util.HashMap;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "class-name")
@JsonSubTypes({
        @JsonSubTypes.Type(value = AggregateOperator.class, name = "aggregate-operator"),
        @JsonSubTypes.Type(value = ArithmeticOperator.class, name = "arithmetic-operator"),
        @JsonSubTypes.Type(value = ColumnOperator.class, name = "column-operator"),
        @JsonSubTypes.Type(value = DateOperator.class, name = "date-operator"),
        @JsonSubTypes.Type(value = EFunction.class, name = "e-function"),
        @JsonSubTypes.Type(value = FunctionOperator.class, name = "function-operator"),
        @JsonSubTypes.Type(value = GeneralTableIndexOperator.class, name = "general-table-index-operator"),
        @JsonSubTypes.Type(value = NumberOperator.class, name = "number-operator"),
        @JsonSubTypes.Type(value = PredicateToMathWrapper.class, name = "predicate-to-math-wrapper"),
        @JsonSubTypes.Type(value = SetOperator.class, name = "set-operator"),
        @JsonSubTypes.Type(value = StarOperator.class, name = "star-operator"),
        @JsonSubTypes.Type(value = StringOperator.class, name = "string-operator")
})
@JsonIdentityInfo(generator = ObjectIdGenerators.IntSequenceGenerator.class, property = "math-op-id")
public interface MathOperator {

    String visitNode();

    @JsonIgnore
    String getNodeName();

    /**
     * Assign names to math operators and update their sub operators and predicates
     * @param indices the indices to be used
     * @param translatorHelper an instance of the translator helper class
     */
    void changeProperty(HashMap<String, Integer> indices, TranslatorHelper translatorHelper);

    MathOperator copy(CopyHelper copyHelper);
}
