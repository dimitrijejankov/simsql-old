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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Map;

public abstract class DataType {

    /**
     * Factory method for creating a DataType from a string make by the writeOut method
     * @param value the string value
     * @return the parsed type
     * @exception RuntimeException throws a runtime exception if it can't parse the string..
     */
    @JsonCreator
    public static DataType createFromString(String value) {

        DataType ret;

        if(value.contains("double")) {
            ret = new DoubleType();
        }
        else if(value.contains("integer")) {
            ret = new IntType();
        }
        else if(value.contains("matrix")) {
            ret = new MatrixType();
        }
        else if(value.contains("null")){
            ret = new NullType();
        }
        else if(value.contains("scalar")){
            ret = new ScalarType();
        }
        else if(value.contains("seed")){
            ret = new SeedType();
        }
        else if(value.contains("string")){
            ret = new StringType();
        }
        else if(value.contains("vector")){
            ret = new StringType();
        }
        else if(value.contains("versatile")) {
            ret = new VersType();
        }
        else {
            throw new RuntimeException("Unknown type!");
        }

        ret.Parse(value);

        return ret;
    }

    private boolean isStoch = false;

    void parseStoch(String parseMe) {
        //isStoch = false;
        for (String a : parseMe.split(" ")) {
            if (a.toLowerCase().equals("random")) {
                isStoch = true;
                return;
            }
        }

    }

    public boolean ifStoch() {
        return isStoch;
    }

    public void setStoch(boolean fromMe) {
        isStoch = fromMe;
    }

    // get integer value of each dimension
    public int getFirstDim() {
        return -1;
    }
    public int getSecondDim() {
        return -1;
    }

    // get variable value of each dimension
    public String getFirstDimVar() {
        return null;
    }
    public String getSecondDimVar() {
        return null;
    }

    // this takes the DataType, written as a string, and parses it to get any additional info
    // for example, we might have "matrix[100][Length]" as input, which this would parse
    public abstract boolean Parse (String fromMe);

    // this is the reverse of the above operation... creating a string that encodes the data type
    // this can be used, for example, to serialze the data type and write it to the catalog
    @JsonValue
    public abstract String writeOut ();

    // gets the size, in bytes, of this particular data type
    public abstract int getSizeInBytes ();

    // gets the definite value for a dimension (or a -1, if it has no definite value).  For example,
    // calling (Matrix[10][]).getDefiniteValue (0) will return a 10, but calling
    // (Matrix[10][]).getDefiniteValue (1) will return a -1, and calling String.getDefiniteValue (1)
    // will give you a -1, as will (Matrix[10][N]).getDefiniteValue (1)
    public abstract int getDefiniteValue (int whichDim);

    // this takes as input a mapping from parameter names to sizes, as well as a type that is
    // being used to parameterize this type, and adds to the mappings, as is needed.  This will
    // be used as follows.  Let's say that we have a UDF MatrixMultiply (Matrix[n][m], Matrix[m,p]) ->
    // Matrix[n,p].  We want to call this with the call MatrixMultiply (Matrix[10][], Matrix[][100]).
    // First, we would have (Matrix[n][m]).parameterize (Matrix[10][], {}), which would modify
    // the map so we have {(n,10)}.  We would than have (Matrix[m,p]).parameterize (Matrix[][100], {(n,10)})
    // which would then mdify the map so we have {(n,10),(p,100)}.
    //
    // If the method finds that a particular parameter has already been parameterized differently, it
    // returns BAD, indicating that there is a type mismatch.
    //
    // This function will also check compatibility. If two types' names are the same, the return result is OK
    // One type and its corresponding random type will always return PROMOTABLE
    public abstract Compatibility parameterize (DataType withMe, Map<String, Integer> addToMe);

    // this takes a map such as {(n,10),(p,100)}, and applies it to get a new data type.  For example,
    // say that this object is Matrix[n,p].  If we call applyMapping ({(n,10),(p,100)}), we will return
    // a Matrix[10][100].
    public abstract DataType applyMapping (Map <String, Integer> useMe);

    // get the runtime representation of corresponding type
    public abstract simsql.runtime.Attribute getPhysicalRealization();

    /**
     * Returns the type name as a string
     * @return the string representation of the type
     */
    public abstract String getTypeName();
}
