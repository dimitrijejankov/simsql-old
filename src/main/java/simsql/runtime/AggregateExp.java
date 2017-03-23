

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


package simsql.runtime;

/**
 * This class is used to store aggregate operations of the form
 * <p>
 * var = aggType(expression)
 */
class AggregateExp {

    public enum Type {
        COUNT, COUNTALL, SUM, AVG, VAR, STDEV, MAX, MIN, VECTOR, ROWMATRIX, COLMATRIX
    }

    private String identifier;
    private AggregateExp.Type type;
    private Expression exp;

    public String getIdentifier() {
        return identifier;
    }

    public AggregateExp.Type getType() {
        return type;
    }

    public Expression getExpression() {
        return exp;
    }

    public String print() {
        return identifier + ": " + type.toString() + "(" + exp.print() + ")";
    }

    public AggregateExp(String id, String t, Expression e) {
        identifier = id;
        exp = e;
        type = AggregateExp.Type.valueOf(t.toUpperCase());
    }
}
