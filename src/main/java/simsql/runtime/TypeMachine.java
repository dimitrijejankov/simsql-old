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

import java.util.ArrayList;
import java.util.HashMap;

import simsql.compiler.FinalVariable;
import simsql.compiler.SimsqlCompiler;
import simsql.compiler.Function;
import simsql.compiler.Attribute;
import simsql.compiler.VGFunction;

public class TypeMachine {

    // this looks at the first few characters of the string, creates a DataType object of the
    // appropriate type, and then calls DataType.parse (parseMe) to get any additional info on the type
    // returns a null if there is nothing that matches
    public static DataType fromString(String parseMe) {

        DataType outType = null;

        if (parseMe.contains("integer"))
            outType = new IntType();
        else if (parseMe.contains("double"))
            outType = new DoubleType();
        else if (parseMe.contains("string"))
            outType = new StringType();
        else if (parseMe.contains("scalar"))
            outType = new ScalarType();
        else if (parseMe.contains("vector"))
            outType = new VectorType();
        else if (parseMe.contains("matrix"))
            outType = new MatrixType();
        else if (parseMe.contains("seed")){
            outType = new SeedType();
        }
        else if (parseMe.contains("unknown") || parseMe.contains("bottom") || parseMe.contains("versatile"))
            outType = new VersType();
        else
            return null;

        outType.Parse(parseMe);
        return outType;
    }

    // Returns the output type of this operation, given the one or more input types.  Returns
    // a null if the inputs are not compatible.  operation should be one of "plus", "minus",
    // "times", etc (that is, a built in operation).
    public static DataType checkArithmetic(ArrayList<Integer> operation, ArrayList<DataType> input) {

        // Make a table to store the possible combinations for each arithmetic operation

        // The table is of this form: op -> [(para1, para2), output]
        DataType[][][] rules = new DataType[5][][];

        // +
        rules[0] = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new IntType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()},
                {new IntType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new ScalarType(), new DoubleType()},
                {new VectorType(), new IntType(), new VectorType()},
                {new IntType(), new VectorType(), new VectorType()},
                {new VectorType(), new DoubleType(), new VectorType()},
                {new DoubleType(), new VectorType(), new VectorType()},
                {new VectorType(), new ScalarType(), new VectorType()},
                {new ScalarType(), new VectorType(), new VectorType()},
                {new VectorType(), new VectorType(), new VectorType()},
                {new MatrixType(), new IntType(), new MatrixType()},
                {new IntType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new DoubleType(), new MatrixType()},
                {new DoubleType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new ScalarType(), new MatrixType()},
                {new ScalarType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new VectorType(), new MatrixType()},
                {new VectorType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new MatrixType(), new MatrixType()},
                {new StringType(), new IntType(), new StringType()},
                {new StringType(), new DoubleType(), new StringType()},
                {new StringType(), new ScalarType(), new StringType()},
                {new StringType(), new StringType(), new StringType()}};

        // -
        rules[1] = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new IntType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()},
                {new IntType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new ScalarType(), new DoubleType()},
                {new VectorType(), new IntType(), new VectorType()},
                {new IntType(), new VectorType(), new VectorType()},
                {new VectorType(), new DoubleType(), new VectorType()},
                {new DoubleType(), new VectorType(), new VectorType()},
                {new VectorType(), new ScalarType(), new VectorType()},
                {new ScalarType(), new VectorType(), new VectorType()},
                {new VectorType(), new VectorType(), new VectorType()},
                {new MatrixType(), new IntType(), new MatrixType()},
                {new IntType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new DoubleType(), new MatrixType()},
                {new DoubleType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new ScalarType(), new MatrixType()},
                {new ScalarType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new VectorType(), new MatrixType()},
                {new VectorType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new MatrixType(), new MatrixType()}};

        // *
        rules[2] = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new IntType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()},
                {new IntType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new ScalarType(), new DoubleType()},
                {new VectorType(), new IntType(), new VectorType()},
                {new IntType(), new VectorType(), new VectorType()},
                {new VectorType(), new DoubleType(), new VectorType()},
                {new DoubleType(), new VectorType(), new VectorType()},
                {new VectorType(), new ScalarType(), new VectorType()},
                {new ScalarType(), new VectorType(), new VectorType()},
                {new VectorType(), new VectorType(), new VectorType()},
                {new MatrixType(), new IntType(), new MatrixType()},
                {new IntType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new DoubleType(), new MatrixType()},
                {new DoubleType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new ScalarType(), new MatrixType()},
                {new ScalarType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new VectorType(), new MatrixType()},
                {new VectorType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new MatrixType(), new MatrixType()}};

        // /
        rules[3] = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new IntType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()},
                {new IntType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new ScalarType(), new DoubleType()},
                {new VectorType(), new IntType(), new VectorType()},
                {new IntType(), new VectorType(), new VectorType()},
                {new VectorType(), new DoubleType(), new VectorType()},
                {new DoubleType(), new VectorType(), new VectorType()},
                {new VectorType(), new ScalarType(), new VectorType()},
                {new ScalarType(), new VectorType(), new VectorType()},
                {new VectorType(), new VectorType(), new VectorType()},
                {new MatrixType(), new IntType(), new MatrixType()},
                {new IntType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new DoubleType(), new MatrixType()},
                {new DoubleType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new ScalarType(), new MatrixType()},
                {new ScalarType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new VectorType(), new MatrixType()},
                {new VectorType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new MatrixType(), new MatrixType()}};
        // %
        rules[4] = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()}};

        DataType compatibleResult = input.get(0);
        for (int i = 1; i < input.size(); i++) {
            if (compatibleResult == null)
                return null;

            int op = operation.get(i - 1);

            // Read in two inputs and one operation. Based on the operation, get the scores for each (para1, para2)
            int max = -1;
            int maxj = 0;
            for (int j = 0; j < rules[op].length; j++) {
                HashMap<String, Integer> mapping = new HashMap<String, Integer>();
                int score1 = rules[op][j][0].parameterize(compatibleResult, mapping).getValue();
                int score2 = rules[op][j][1].parameterize(input.get(i), mapping).getValue();
                int score = (score1 < score2) ? score1 : score2;
                if (score > max) {
                    max = score;
                    maxj = j;
                }
            }

            // Pick the (para1, para2) pair with the highest score
            compatibleResult = (max == -1) ? null : rules[op][maxj][2];

            // compatibleResult = TypeCheckerHelper.arithmeticCompate(operation.get(i - 1), compatibleResult, input.get(i));
        }
        return compatibleResult;
    }

    public static DataType checkAggregate(int operation, ArrayList<DataType> input) {

        DataType[][] rules = new DataType[][]{{new IntType(), new IntType(), new IntType()},
                {new DoubleType(), new IntType(), new DoubleType()},
                {new IntType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new DoubleType(), new DoubleType()},
                {new ScalarType(), new IntType(), new DoubleType()},
                {new IntType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new DoubleType(), new DoubleType()},
                {new DoubleType(), new ScalarType(), new DoubleType()},
                {new ScalarType(), new ScalarType(), new DoubleType()},
                {new VectorType(), new IntType(), new VectorType()},
                {new IntType(), new VectorType(), new VectorType()},
                {new VectorType(), new DoubleType(), new VectorType()},
                {new DoubleType(), new VectorType(), new VectorType()},
                {new VectorType(), new ScalarType(), new VectorType()},
                {new ScalarType(), new VectorType(), new VectorType()},
                {new VectorType(), new VectorType(), new VectorType()},
                {new MatrixType(), new IntType(), new MatrixType()},
                {new IntType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new DoubleType(), new MatrixType()},
                {new DoubleType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new ScalarType(), new MatrixType()},
                {new ScalarType(), new MatrixType(), new MatrixType()},
                {new MatrixType(), new MatrixType(), new MatrixType()},
                {new StringType(), new IntType(), new StringType()},
                {new StringType(), new DoubleType(), new StringType()},
                {new StringType(), new StringType(), new StringType()}};

        DataType resultType = null;
        switch (operation) {
            case FinalVariable.AVG:
            case FinalVariable.VARIANCE:
            case FinalVariable.STDEV:
                int max = -1;
                int maxj = 0;
                for (int j = 0; j < rules.length; j++) {
                    HashMap<String, Integer> mapping = new HashMap<String, Integer>();
                    int score1 = rules[j][0].parameterize(new DoubleType(), mapping).getValue();
                    int score2 = rules[j][1].parameterize(input.get(0), mapping).getValue();
                    int score = (score1 < score2) ? score1 : score2;
                    if (score > max) {
                        max = score;
                        maxj = j;
                    }
                }

                resultType = (max == -1) ? null : rules[maxj][2];

                // resultType = TypeCheckerHelper.arithmeticCompate(FinalVariable.PLUS, new DoubleType(), input.get(0));
                break;

            case FinalVariable.SUM:
                resultType = input.get(0);
                break;

            case FinalVariable.COUNT:
            case FinalVariable.COUNTALL:
                resultType = new IntType();
                break;

            case FinalVariable.MIN:
            case FinalVariable.MAX:
                resultType = input.get(0);
                break;

            case FinalVariable.VECTOR:
                resultType = new VectorType();
                break;

            case FinalVariable.ROWMATRIX:
            case FinalVariable.COLMATRIX:
                resultType = new MatrixType();
                break;
        }
        return resultType;
    }

    // same as above, but this uses the catalog to figure out the return type from a UD function
    // Should hadle the case where you have a UDF such as MatrixMultiply (Matrix[n][m], Matrix[m,p]) ->
    // Matrix[n,p].  If I make the call runUDFunction ("MatrixMultiply", <Matrix[10][], Matrix[][100]>,
    // this will first call (Matrix[n][m]).parameterize (Matrix[10][], .) and then
    // (Matrix[m,p]).parameterize (Matrix[][100], .), which would give us the map {(n,10),(p,100)}.
    // We'd then call (Matrix[n,p]).applyMapping ({(n,10),(p,100)}), which would give us
    // Matrix[10][100] as the ultimate output type.
    public static DataType checkUDFunction(String funcName, ArrayList<DataType> input) throws Exception {
        Function function = SimsqlCompiler.catalog.getFunction(funcName);
        ArrayList<Attribute> inputAtts = function.getInputAtts();
        Attribute outputAtt = function.getOutputAtt();

        if (funcName.equals("case_1")) {
            Compatibility ifMatch;
            for (int i = 1; i < input.size() - 1; i = i + 2) {
                ifMatch = input.get(1).parameterize(input.get(i), new HashMap<String, Integer>());
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function ["
                            + funcName
                            + "]"
                            + " are not compatible!");
                    throw new Exception(
                            "The type of parameters of Function ["
                                    + funcName + "]"
                                    + " are not compatible!");
                }

                ifMatch = inputAtts.get(1).getType().parameterize(input.get(i), new HashMap<String, Integer>());
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function ["
                            + funcName
                            + "]"
                            + " does not match the defined type!");
                    throw new Exception(
                            "The type of parameters of Function ["
                                    + funcName
                                    + "]"
                                    + " does not match the defined type!");
                }
            }

            ifMatch = inputAtts.get(1).getType().parameterize(input.get(input.size() - 1), new HashMap<String, Integer>());
            if (ifMatch == Compatibility.BAD) {
                System.err.println("The type of parameters of Function ["
                        + funcName + "]"
                        + " does not match the defined type!");
                throw new Exception(
                        "The type of parameters of Function ["
                                + funcName + "]"
                                + " does not match the defined type!");
            }
            DataType outputType = input.get(input.size() - 1);
            return outputType;
        } else if (funcName.equals("case_2")) {
            Compatibility ifMatch;
            ifMatch = inputAtts.get(0).getType().parameterize(input.get(0), new HashMap<String, Integer>());
            if (ifMatch == Compatibility.BAD) {
                System.err.println("The type of parameters of Function ["
                        + funcName + "]"
                        + " does not match the defined type!");
                throw new Exception(
                        "The type of parameters of Function ["
                                + funcName + "]"
                                + " does not match the defined type!");

            }

            for (int i = 1; i < input.size() - 1; i++) {
                ifMatch = input.get((i + 1) % 2 + 1).parameterize(input.get(i), new HashMap<String, Integer>());
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function ["
                            + funcName
                            + "]"
                            + " are not compatible!");
                    throw new Exception(
                            "The type of parameters of Function ["
                                    + funcName + "]"
                                    + " are not compatible!");
                }

                ifMatch = inputAtts.get((i + 1) % 2 + 1).getType().parameterize(input.get(i), new HashMap<String, Integer>());
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function ["
                            + funcName
                            + "]"
                            + " does not match the defined type!");
                    throw new Exception(
                            "The type of parameters of Function ["
                                    + funcName
                                    + "]"
                                    + " does not match the defined type!");
                }
            }

            ifMatch = inputAtts.get(2).getType().parameterize(input.get(input.size() - 1), new HashMap<String, Integer>());
            if (ifMatch == Compatibility.BAD) {
                System.err.println("The type of parameters of Function ["
                        + funcName + "]"
                        + " does not match the defined type!");
                throw new Exception(
                        "The type of parameters of Function ["
                                + funcName + "]"
                                + " does not match the defined type!");
            }
            DataType outputType = input.get(input.size() - 1);
            return outputType;
        } else {
            if (input.size() != inputAtts.size()) {
                System.err.println("Found " + input.size()
                        + " atts in input; [" + funcName
                        + "] expected " + inputAtts.size());
                throw new Exception("Bad number of input atts to ["
                        + funcName + "]");
            }

            HashMap<String, Integer> mapping = new HashMap<String, Integer>();
            for (int i = 0; i < input.size(); i++) {
                Compatibility ifMatch = inputAtts.get(i).getType().parameterize(input.get(i), mapping);
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function ["
                            + funcName + "]"
                            + " does not match the defined type!");
                    throw new Exception(
                            "The type of parameters of Function ["
                                    + funcName + "]"
                                    + " does not match the defined type!");
                }
            }
            DataType outputType = outputAtt.getType();
            return outputType.applyMapping(mapping);
        }
    }

    // same as above, but this uses the catalog to figure out the return from a VG function
    public static ArrayList<DataType> checkVGFunction(String vgName, ArrayList<DataType> input) throws Exception {
        VGFunction function = SimsqlCompiler.catalog.getVGFunction(vgName);
        ArrayList<Attribute> inputAtts = function.getInputAtts();
        ArrayList<Attribute> outputAtts = function.getOutputAtts();

        if (input.size() != inputAtts.size()) {
            System.err.println("Found " + input.size() + " atts in input; [" + vgName + "] expected " +
                    inputAtts.size());
            throw new Exception("Bad number of input atts to [" + vgName + "]");
        }

        if (outputAtts.size() == 0) {
            System.err.println("No output from Function [" + vgName + "]!");
            throw new Exception("No output from Function [" + vgName + "]!");
        } else {
            HashMap<String, Integer> mapping = new HashMap<String, Integer>();
            for (int i = 0; i < input.size(); i++) {
                Compatibility ifMatch = inputAtts.get(i).getType().parameterize(input.get(i), mapping);
                if (ifMatch == Compatibility.BAD) {
                    System.err.println("The type of parameters of Function [" + vgName + "]" +
                            " does not match the defined type!");
                    System.err.println("Required input type: " + inputAtts.get(i).getType().writeOut());
                    System.err.println("User input type: " + input.get(i).writeOut());
                    throw new Exception("The type of parameters of Function [" + vgName + "]" +
                            " does not match the defined type!");
                }
            }
            ArrayList<DataType> resultList = new ArrayList<DataType>();
            for (int i = 0; i < outputAtts.size(); i++) {
                DataType outputType = outputAtts.get(i).getType();
                resultList.add(outputType.applyMapping(mapping));
            }
            return resultList;
        }
    }
}
