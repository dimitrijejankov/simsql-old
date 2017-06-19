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

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.*;
import java.lang.*;
import java.lang.reflect.*;

/**
 * This class encapsulates a function that can be applied on
 * attributes. The eval() method must be overloaded and use the get()
 * methods to obtain the parameter values.
 *
 * @author Luis
 */
public abstract class Function {

    // array of parameters
    protected IntermediateValue[] inParams;

    // number of MCs, for sanity checks
    protected int numMC;

    // current parameter being injected
    protected int curParam;

    // current isNulls value.
    protected Bitstring isNull;

    // input types.
    protected AttributeType[] inTypes;

    public static String extractFile(String fileName) {

        String longFileName = null;
        try {
            File fx = File.createTempFile("simsql_", ".so");

            // NOTE: commented this because Linux tends to crash if one deletes a loaded .so file!
            // fx.deleteOnExit();
            longFileName = fx.getAbsolutePath();
        } catch (Exception e) {
            e.printStackTrace();
            longFileName = new File(fileName.replaceAll("/", "_")).getAbsolutePath() + new Random().nextLong();
        }

        // extract the file to the work directory
        try {
            FileOutputStream out = new FileOutputStream(longFileName);
            InputStream in = VGFunction.class.getResourceAsStream(fileName);
            while (in.available() > 0) {
                out.write(in.read());
            }
            in.close();
            out.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to extract file.", e);
        }

        return longFileName;
    }

    /**
     * Simple constructor, the input represents the output types.
     */
    public Function(AttributeType... types) {

        // create the parameters array
        inParams = IntermediateValue.getIntermediates(1, types);
        inTypes = types;

        // set the start.
        numMC = 1;
        curParam = 0;
        isNull = BitstringWithSingleValue.FALSE;
    }

    /**
     * Used to construct an empty instance of function
     */
    protected Function() {
    }

    /**
     * Applies the function to a sequence of attributes, forcing them to inject their contents.
     */
    public Attribute apply(Attribute... atts) {

        // inject each attribute
        for (Attribute a : atts) {
            a.injectSelf(this);
        }

        // evaluate
        Attribute out = eval();

        // reset
        curParam = 0;
        numMC = 1;
        isNull = BitstringWithSingleValue.FALSE;

        // return
        return out;
    }

    // Injectors: Attribute classes must use these in their
    // injectSelf() methods to place their values into the function as
    // parameters.
    public void inject(long val) {
        inParams[curParam].set(val, 0);
        curParam++;
    }

    public void inject(long[] val) {
        inParams[curParam].set(val);
        curParam++;

        if (numMC == 1)
            numMC = val.length;
        else if (numMC != val.length)
            throw new RuntimeException("Inconsistent number of MC iterations in parameters.");
    }

    public void inject(double val) {
        inParams[curParam].set(val, 0);
        curParam++;
    }

    public void inject(double[] val) {
        inParams[curParam].set(val);
        curParam++;

        if (numMC == 1)
            numMC = val.length;
        else if (numMC != val.length)
            throw new RuntimeException("Inconsistent number of MC iterations in parameters.");
    }

    public void inject(String val) {
        inParams[curParam].set(val, 0);
        curParam++;
    }

    public void inject(String[] val) {
        inParams[curParam].set(val);
        curParam++;

        if (numMC == 1)
            numMC = val.length;
        else if (numMC != val.length)
            throw new RuntimeException("Inconsistent number of MC iterations in parameters.");
    }

    public void inject(double val, int label) {
        inParams[curParam].set(val, label, 0);
        curParam++;
    }

    public void inject(double[] val, int[] label) {
        inParams[curParam].set(val, label);
        curParam++;

        if (numMC == 1)
            numMC = val.length;
        else if (numMC != val.length)
            throw new RuntimeException("Inconsistent number of MC iterations in parameters.");
    }

    public void inject(double[] val, int label) {
        inParams[curParam].set(val, label, 0);
        curParam++;
    }

    public void inject(Matrix val) {
        inParams[curParam].set(val, 0);
        curParam++;
    }

    /**
     * This one is used by those attributes that have a bitstring
     * associated with them.
     */
    public void injectBitstring(Bitstring isNullIn) {
        isNull = isNull.or(isNullIn);
    }


    /**
     * Returns the number of MC iterations.
     */
    protected int getNumMC() {
        return numMC;
    }

    /**
     * Individual function evaluation method.
     */
    protected abstract Attribute eval();

    /**
     * Returns the name of this function.
     */
    public abstract String getName();

    /**
     * Returns the output type of the function.
     */
    public abstract AttributeType getOutputType();

    /**
     * Returns the input parameter types of the function.
     */
    public AttributeType[] getInputTypes() {
        return inTypes;
    }

    /**
     * Returns the SQL creation statement for this function.
     */
    public String getCreateSQL(String sourceFile) {
        String outStr = "create function " + getName().toLowerCase() + "(";

        if (getInputTypes().length > 0) {

            outStr += "inAtt0 " + getInputTypes()[0].getType().writeOut();
            for (int i = 1; i < getInputTypes().length; i++) {
                outStr += ", inAtt" + i + " " + getInputTypes()[i].getType().writeOut();
            }
        }

        outStr += ") returns " + getOutputType().getType().writeOut() + " source '" + sourceFile + "';";

        return outStr;
    }
}
