
package simsql.runtime;

import java.lang.*;
import java.lang.reflect.*;

/**
 * A general function type that can be constructed from a Method type
 * that has been obtained using Java reflection.
 * 
 * @author Jacob.
 */
public class ReflectedUDFunction extends Function {

    private Method method;
    private AttributeType outType;

    // default constructor
    public ReflectedUDFunction(Method _method, AttributeType _outType) {

	// call Function's constructor
	super(IntermediateValue.whichTypes(_method.getParameterTypes()));

	// set
	outType = _outType;
	method = _method;      
    }

    // another constructor
    public ReflectedUDFunction(String className, String methodName, AttributeType outType, Class... inTypes) {
	this(getMethod(className, methodName, inTypes), outType);
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
    
    public void setInputTypes(AttributeType... types) {
    	inTypes = types;    	
    }
    
    public void setOutputType(AttributeType type) {
    	outType = type;
    }
}
