

/**
 * *
 * Copyright 2014 Rice University                                           *
 * *
 * Licensed under the Apache License, Version 2.0 (the "License");          *
 * you may not use this file except in compliance with the License.         *
 * You may obtain a copy of the License at                                  *
 * *
 * http://www.apache.org/licenses/LICENSE-2.0                           *
 * *
 * Unless required by applicable law or agreed to in writing, software      *
 * distributed under the License is distributed on an "AS IS" BASIS,        *
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. *
 * See the License for the specific language governing permissions and      *
 * limitations under the License.                                           *
 * *
 */


/**
 *
 */
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.statisticsOperator;

import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class AttributeType implements StatisticsOperator {

    private String attributeName;
    private DataType type;
    private String typeString;

	/* (non-Javadoc)
     * @see logicOperator.statisticsOperator.StatisticsOperator#visitNode()
	 */


    @Override
    public String visitNode() {
        String result = "";
        result += "attributeType(";
        result += attributeName;
        result += ", ";
        String tempType = "";

        if (type != null) {
            tempType = type.writeOut().replace("[", "_");
        } else {
            tempType = typeString.replace("[", "_");
        }

        tempType = tempType.replace("]", "");
        tempType = tempType.replace(" ", "_");
        result += tempType;
        result += ").\r\n";

        if (type != null) {
            result += "attributeSize(" + attributeName + ", " + type.getSizeInBytes() + ").\r\n";
        }

        return result;
    }


    public AttributeType(String attributeName, DataType type) {
        super();
        this.attributeName = attributeName;
        this.type = type;
        typeString = null;
    }

    public AttributeType(String attributeName, String typeString) {
        super();
        this.attributeName = attributeName;
        this.typeString = typeString;
        type = null;
    }


    public String getAttributeName() {
        return attributeName;
    }


    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }


    public DataType getType() {
        return type;
    }


    public void setType(DataType type) {
        this.type = type;
    }


}
