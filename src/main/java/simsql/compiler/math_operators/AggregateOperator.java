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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import simsql.compiler.CopyHelper;
import simsql.compiler.FinalVariable;

/**
 * represents the aggregate math operator
 */
public class AggregateOperator implements MathOperator {

    /**
     * the name of the aggregate operator
     */
    @JsonProperty("name")
    private String name;

    /**
     * The "type" is defined in Class simsql.compiler.FinalVariable
     * public static final int AVG = 0;
     * public static final int SUM = 1;
     * public static final int COUNT = 2;
     * public static final int COUNTALL = 3;
     * public static final int MIN = 4;
     * public static final int MAX = 5;
     * public static final int VARIANCE = 6;
     */
    @JsonProperty("type")
    private int type;

    /**
     * The "operator" can be:
     * Expression
     * Identifier (Column)
     */
    @JsonProperty("child-operator")
    private MathOperator childOperator;

    /**
     * The type of the child operator in the string format
     */
    @JsonProperty("child-operator-type")
    private String childOperatorType;

    @JsonCreator
    public AggregateOperator(@JsonProperty("name") String name,
                             @JsonProperty("type") int type,
                             @JsonProperty("child-operator") MathOperator childOperator,
                             @JsonProperty("child-operator-type") String childOperatorType) {
        super();
        this.name = name;
        this.type = type;
        this.childOperator = childOperator;
        this.childOperatorType = childOperatorType;
    }

    /**
     * returns the name of this operator
     *
     * @return the name as set by the constructor or the setName method
     */
    public String getName() {
        return name;
    }

    /**
     * sets the name of the operator
     *
     * @param name the new name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * returns the type of the operator can be one of :
     * <p>
     * public static final int AVG = 0;
     * public static final int SUM = 1;
     * public static final int COUNT = 2;
     * public static final int COUNTALL = 3;
     * public static final int MIN = 4;
     * public static final int MAX = 5;
     * public static final int VARIANCE = 6;
     *
     * @return the int value of the type
     */
    public int getType() {
        return type;
    }

    /**
     * set the type in the int form
     *
     * @param type the type as specified in the type attribute
     */
    public void setType(int type) {
        this.type = type;
    }

    /**
     * returns the child operator of the aggregate
     *
     * @return the child operator
     */
    public MathOperator getChildOperator() {
        return childOperator;
    }

    /**
     * sets a new child operator
     *
     * @param childOperator the new operator
     */
    public void setChildOperator(MathOperator childOperator) {
        this.childOperator = childOperator;
    }

    /**
     * returns the type of the child operator
     *
     * @return the type of the child operator as string
     */
    public String getChildOperatorType() {
        return childOperatorType;
    }

    /**
     * sets the type of the child operator
     *
     * @param childOperatorType the type of the child operator as string
     */
    public void setChildOperatorType(String childOperatorType) {
        this.childOperatorType = childOperatorType;
    }

    /**
     * returns the string representation of the aggregate operator for the relation statistics
     *
     * @return the string representation
     */
    @Override
    public String visitNode() {
        String result = "aggExp(";
        result += this.getName();
        result += ", ";

        result += translateAggregateType(type);
        result += ", ";

        result += childOperator.getNodeName();
        result += ", ";

        result += childOperatorType;
        result += ").\r\n";

        result += childOperator.visitNode();
        return result;
    }

    /**
     * returns the name of the operator
     *
     * @return the name string
     */
    @Override
    public String getNodeName() {
        return name;
    }

    /**
     * translates the aggregate type into the string representation
     * <p>
     * FinalVariable.AVG => "avg"
     * FinalVariable.SUM => "sum"
     * FinalVariable.COUNT => "count"
     * FinalVariable.COUNTALL => "countall"
     * FinalVariable.MIN => "min"
     * FinalVariable.MAX => "max"
     * FinalVariable.VARIANCE => "variance"
     * FinalVariable.STDEV => "stdev"
     * FinalVariable.VECTOR => "vector"
     * FinalVariable.ROWMATRIX => "rowmatrix"
     * FinalVariable.COLMATRIX => "colmatrix"
     *
     * @param type the type in it's integer form
     * @return the type in it's string form
     */
    private String translateAggregateType(int type) {
        String result = "";

        switch (type) {
            case FinalVariable.AVG:
                result = "avg";
                break;

            case FinalVariable.SUM:
                result = "sum";
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

    /**
     * Copies the aggregate operator
     *
     * @param copyHelper the operator to be copied
     * @return the copy
     */
    public MathOperator copy(CopyHelper copyHelper) {
        String c_name = name;
        int c_type = this.type;
        MathOperator c_childOperator = childOperator.copy(copyHelper);
        String c_childOperatorType = this.childOperatorType;

        return new AggregateOperator(c_name,
                c_type,
                c_childOperator,
                c_childOperatorType);
    }
}
