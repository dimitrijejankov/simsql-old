

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

import java.lang.*;
import java.lang.reflect.*;

/**
 * A general function type that can be constructed from a Method type
 * that has been obtained using Java reflection.
 * 
 * @author Luis.
 */
public class ReflectedFunction extends Function {

    private Method method;
    private AttributeType outType;

    // default constructor
    public ReflectedFunction(Method _method) {

	// call Function's constructor
	super(IntermediateValue.whichTypes(_method.getParameterTypes()));

	// set
	outType = IntermediateValue.whichType(_method.getReturnType());
	method = _method;      
    }

    // another constructor
    public ReflectedFunction(String className, String methodName, Class... inTypes) {
	this(getMethod(className, methodName, inTypes));
    }

    // returns the corresponding method...
    private static Method getMethod(String className, String methodName, Class... inTypes) {
	try {
	    Class<?> inClass = Class.forName(className);
	    return inClass.getMethod(methodName, inTypes);
	} catch (Exception e) {
	    throw new RuntimeException("Unable to obtain function from class method.", e);
	}
    }

    // name.
    public String getName() {
	return method.getName();
    }

    // output type.
    public AttributeType getOutputType() {
	return outType;
    }

    // universal evaluation method.
    protected Attribute eval() {

	// deal with NULL singletons
	if (getNumMC() == 1 && !isNull.allAreFalseOrUnknown())
	    return new NullAttribute();

	// get an output intermediate
	IntermediateValue outVal = new IntermediateValue(outType, getNumMC());

	// apply the function to all MCs
	for (int i=0;i<getNumMC();i++) {

	    // skip the NULL ones.
	    if (!isNull.getValue(i))
		try {
		    outVal.set(method.invoke(null, IntermediateValue.getValuesCast(i, inParams, method.getParameterTypes())), i);
		} catch (Exception e) {
		    throw new RuntimeException("Unable to invoke reflected function.", e);
		}
	}

	// deal with NULL arrays.
	if (!isNull.allAreFalseOrUnknown())
	    return new ArrayAttributeWithNulls(isNull, outVal.getAttribute());
	else
	    return outVal.getAttribute();
    }

	public static boolean isScalarFunction(String function) {
		try {
			Class scalar = Class.forName("simsql.functions.scalar." + function);

			return scalar != null;
		} catch (Exception e) {
			return false;
		}
	}
}
