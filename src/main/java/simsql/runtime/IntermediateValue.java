

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

import java.util.*;
import java.lang.*;
import java.lang.reflect.*;
import simsql.compiler.*;

/**
 * Intermediates are primitive attribute values that we exchange with
 * Functions and VGFunctions. This class encapsulates them and allows
 * for some interesting features like compressing array attributes
 * on-line and casting numerical types.
 * 
 * @author Luis
 */
public class IntermediateValue {

    // our type
    private AttributeType type;
    private int lastIteration;
    private int numMC;
    private boolean sameSoFar;

    // values
    private long[] intValue = null;
    private double[] doubleValue = null;
    private String[] stringValue = null;

    private double[] scalarValue = null;
    private double[][] vectorValue = null;
    private double[][][] matrixValue = null;

    private int[] scalarLabel = null;
    private int[] vectorLabel = null;
    private boolean[] ifRows = null;

    // constructor
    public IntermediateValue(AttributeType t, int inNumMC) {
	type = t;
	numMC = inNumMC;

	// create arrays with default values.
	switch(t.getTypeCode()) {
	case SEED:
	case INT: {
	    intValue = new long[numMC];
	    for (int i=0;i<numMC;i++) {
		intValue[i] = 1;
	    }
	}
	    break;

	case DOUBLE: {
	    doubleValue = new double[numMC];
	    for (int i=0;i<numMC;i++) {
		doubleValue[i] = 0.0;
	    }
	}
	    break;

	case STRING: {
	    stringValue = new String[numMC];
	    for (int i=0;i<numMC;i++) {
		stringValue[i] = "";
	    }
	}
	    break;

	case SCALAR: {
	    scalarValue = new double[numMC];
	    scalarLabel = new int[numMC];
	    for (int i=0;i<numMC;i++) {
		scalarValue[i] = 0.0;
		scalarLabel[i] = -1;
	    }
	}
	    break;

	case VECTOR: {
	    vectorValue = new double[numMC][];
	    vectorLabel = new int[numMC];
	    for (int i=0;i<numMC;i++) {
		vectorValue[i] = null;
		vectorLabel[i] = -1;
	    }
	}
	    break;

	case MATRIX: {
	    matrixValue = new double[numMC][][];
	    ifRows = new boolean[numMC];
	    for (int i=0;i<numMC;i++) {
		matrixValue[i] = null;
		ifRows[i] = true;
	    }
	}
	    break;

	default:
	    break;
	}

	reset();
    }

    public void reset() {
	lastIteration = 0;
	sameSoFar = true;
    }

    public void set(long val, int mc) {

	// cast, if necessary.
	if (type.getTypeCode() == TypeCode.DOUBLE) {
	    set((double)val, mc);
	    return;
	}
	
	if (type.getTypeCode() != TypeCode.INT && type.getTypeCode() != TypeCode.SEED)
	    throw new RuntimeException("Invalid intermediate type!");

	// compress
	intValue[mc] = val;
	sameSoFar &= (val == intValue[lastIteration]);
	lastIteration = mc;
    }

    public void set(double val, int mc) {

	// cast, if necessary
	if (type.getTypeCode() == TypeCode.INT || type.getTypeCode() == TypeCode.SEED) {
	    set((long)val, mc);
	    return;
	}
	
	if (type.getTypeCode() != TypeCode.DOUBLE)
	    throw new RuntimeException("Invalid intermediate type!");

	// compress
	doubleValue[mc] = val;
	sameSoFar &= (val == doubleValue[lastIteration]);
	lastIteration = mc;
    }

    public void set(String val, int mc) {

	// only strings are allowed!
	if (type.getTypeCode() != TypeCode.STRING)
	    throw new RuntimeException("Invalid intermediate type!");
	
	// compress
	stringValue[mc] = val;
	sameSoFar &= (val.equals(stringValue[lastIteration]));
	lastIteration = mc;
    }

    public void set(double val, int label, int mc) {

    // cast, if necessary
	if (type.getTypeCode() == TypeCode.DOUBLE) {
	    set(val, mc);
	    return;
	}
	
	if (type.getTypeCode() != TypeCode.SCALAR)
	    throw new RuntimeException("Invalid intermediate type!");

	// compress
	scalarValue[mc] = val;
	scalarLabel[mc] = label;
	sameSoFar &= (val == scalarValue[lastIteration]);
	lastIteration = mc;
    }

    public void set(double[] val, int label, int mc) {
	
	if (type.getTypeCode() != TypeCode.VECTOR)
	    throw new RuntimeException("Invalid intermediate type!");

	// compress
	vectorValue[mc] = val;
	vectorLabel[mc] = label;
	sameSoFar = (lastIteration == 0);
	lastIteration = mc;
    }

    public void set(double[][] val, boolean ifRow, int mc) {
	
	if (type.getTypeCode() != TypeCode.MATRIX)
	    throw new RuntimeException("Invalid intermediate type!");

	// compress
	matrixValue[mc] = val;
	ifRows[mc] = ifRow;
	sameSoFar = (lastIteration == 0);
	lastIteration = mc;
    }

    public void set(long[] val) {

	// cast, if necessary
	if (type.getTypeCode() == TypeCode.DOUBLE) {
	    doubleValue = new double[val.length];
	    numMC = doubleValue.length;

	    for (int i=0;i<val.length;i++) {
		set((double)val[i], i);
	    }

	    return;
	}

	if (type.getTypeCode() != TypeCode.INT && type.getTypeCode() != TypeCode.DOUBLE)
	    throw new RuntimeException("Invalid intermediate type!");

	intValue = val;
	sameSoFar = false;
	numMC = intValue.length;
    }

    public void set(double[] val) {

	if (type.getTypeCode() == TypeCode.INT || type.getTypeCode() == TypeCode.SEED) {
	    
	    intValue = new long[val.length];
	    numMC = intValue.length;
	    for (int i=0;i<val.length;i++) {
		set((long)val[i], i);
	    }

	    return;
	}

	if (type.getTypeCode() != TypeCode.DOUBLE)
	    throw new RuntimeException("Invalid intermediate type!");

	doubleValue = val;
	numMC = doubleValue.length;
	sameSoFar = false;
    }

    public void set(String[] val) {

	if (type.getTypeCode() != TypeCode.STRING)
	    throw new RuntimeException("Invalid intermediate type!");

	stringValue = val;
	numMC = stringValue.length;
	sameSoFar = false;
    }

    public void set(double[] val, int[] label) {

    // cast, if necessary
	if (type.getTypeCode() == TypeCode.DOUBLE) {
	    set(val);
	    return;
	}

	if (type.getTypeCode() != TypeCode.SCALAR)
	    throw new RuntimeException("Invalid intermediate type!");

	scalarValue = val;
	scalarLabel = label;
	numMC = scalarValue.length;
	sameSoFar = false;
    }

    public void set(double[][] val, int[] label) {

	if (type.getTypeCode() != TypeCode.VECTOR)
	    throw new RuntimeException("Invalid intermediate type!");

	vectorValue = val;
	vectorLabel = label;
	numMC = vectorValue.length;
	sameSoFar = false;
    }

    public void set(double[][][] val, boolean[] ifRow) {

	if (type.getTypeCode() != TypeCode.MATRIX)
	    throw new RuntimeException("Invalid intermediate type!");

	matrixValue = val;
	ifRows = ifRow;
	numMC = matrixValue.length;
	sameSoFar = false;
    }

    public void set(Object val, int mc) {

	switch(type.getTypeCode()) {
	case SEED:
	case INT:
	    set(((Number)val).longValue(), mc);
	    break;

	case DOUBLE:
	    set(((Number)val).doubleValue(), mc);
	    break;

	case STRING:
	    set(((String)val), mc);
	    break;

	case SCALAR:
	    set(((double[])val)[0], (int) ((double[])val)[1], mc);
	    break;

	case VECTOR:
		int len = ((double[])val).length;
		double [] temp = new double[len - 1];
		for (int i = 0; i < len - 1; i++)
			temp[i] = ((double[])val)[i];
		set(temp, (int) ((double[])val)[len - 1], mc);
	    break;

	case MATRIX:
		set(((double[][])val), true, mc);
	    break;

	default:
	    break;
	}
    }

    // gets the corresponding Java object-type.
    public Object getValue(int mc) {

	// for singletons...
	if (numMC == 1)
	    mc = 0;

	switch(type.getTypeCode()) {
	case SEED:
	case INT: {
	    return intValue[mc];
	}

	case DOUBLE: {
	    return doubleValue[mc];
	}

	case STRING: {
	    return stringValue[mc];
	}

	case SCALAR: {
	    return scalarValue[mc];
	}

	case VECTOR: {
	    return vectorValue[mc];
	}

	case MATRIX: {
		if (ifRows[mc])
	    	return matrixValue[mc];
	    else
	    	return transpose(matrixValue[mc]);
	}

	default: {
	    return null;
	}

	}
    }

    public static double[][] transpose(double[][] a) {
		int r = a.length;
		int c = a[0].length;
		double[][] t = new double[c][r];
		for(int i = 0; i < r; i++) {
	    	for(int j = 0; j < c; j++) {
				t[j][i] = a[i][j];
			}
		}
		return t;
	}

    // gets the corresponding Java object-type, but casted.
    public Object getValueCast(int mc, Class<?> castTo) {
		
	Object v = getValue(mc);

	// is it a type I can just assign directly?
	if (castTo.isAssignableFrom(v.getClass())) {
	    return castTo.cast(v);
	}

	// long or double to...
	if (type.getTypeCode() == TypeCode.SEED || type.getTypeCode() == TypeCode.INT || type.getTypeCode() == TypeCode.DOUBLE || type.getTypeCode() == TypeCode.SCALAR) {

	    Number vv = (Number)v;

	    // byte?
	    if (castTo.equals(byte.class) ||
		castTo.equals(Byte.class)) {

		return vv.byteValue();
	    }

	    // short?
	    if (castTo.equals(short.class) || 
		castTo.equals(Short.class)) {

		return vv.shortValue();
	    }

	    // int?
	    if (castTo.equals(int.class) ||
		castTo.equals(Integer.class)) {

		return vv.intValue();
	    }

	    // float?
	    if (castTo.equals(float.class) ||
		castTo.equals(Float.class)) {

		return vv.floatValue();
	    }

	    // double?
	    if (castTo.equals(double.class) ||
		castTo.equals(Double.class)) {

		return vv.doubleValue();
	    }

	    // long?
	    if (castTo.equals(long.class) ||
		castTo.equals(Long.class)) {

		return vv.longValue();
	    }
	}

	// no more casts allowed here!
	throw new RuntimeException("Invalid cast!");
    }

    
    //    private static IntAttribute[] intAttCache = new IntAttribute[1000000];
    private Attribute cachedIntAttribute(long value) {

	/*
	if (value >= 0 && value < (long)intAttCache.length) {
	    
	    int pos = (int)value;
	    if (intAttCache[pos] == null) {
		intAttCache[pos] = new IntAttribute(value);
	    } 
	    return intAttCache[pos];
	}
	*/

	return new IntAttribute(value);
    }

    // gets the corresponding Attribute type.
    public Attribute getAttribute() {

	switch(type.getTypeCode()) {
	case INT:
	case SEED:
	    return sameSoFar ? cachedIntAttribute(intValue[0]) : new IntArrayAttribute(intValue);

	case DOUBLE:
	    return sameSoFar ? new DoubleAttribute(doubleValue[0]) : new DoubleArrayAttribute(doubleValue);

	case SCALAR:
	    // return sameSoFar ? new ScalarAttribute(scalarValue[0]) : new ScalarArrayAttribute(scalarValue);
		return sameSoFar ? new ScalarAttribute(scalarLabel[0], scalarValue[0]) : new NullAttribute();

	case VECTOR:
	    // return sameSoFar ? new VectorAttribute(vectorValue[0]) : new VectorArrayAttribute(vectorValue);
		return sameSoFar ? new VectorAttribute(vectorLabel[0], vectorValue[0]) : new NullAttribute();

	case MATRIX:
	    // return sameSoFar ? new MatrixAttribute(matrixValue[0], ifRows[0]) : new MatrixArrayAttribute(matrixValue, ifRows);
		return sameSoFar ? new MatrixAttribute(ifRows[0], matrixValue[0]) : new NullAttribute();
	    
	case STRING:
	    return sameSoFar ? new StringAttribute(stringValue[0]) : new StringArrayAttribute(stringValue);

	default:
	    return new NullAttribute();
	}
    }

    // builds an array of intermediates from an array of attribute types
    public static IntermediateValue[] getIntermediates(int numMC, AttributeType[] t) {

	IntermediateValue[] ret = new IntermediateValue[t.length];
	for (int i=0;i<t.length;i++) {
	    ret[i] = new IntermediateValue(t[i], numMC);
	}

	return ret;
    }

    // builds an array of objects from an array of intermediates
    public static Object[] getValues(int mc, IntermediateValue[] in) {

	Object[] ret = new Object[in.length];
	for (int i=0;i<in.length;i++) {
	    ret[i] = in[i].getValue(mc);
	}

	return ret;	
    }

    // builds an array of objects from an array of intermediates, casted
    public static Object[] getValuesCast(int mc, IntermediateValue[] in, Class[] toCast) {

	Object[] ret = new Object[in.length];
	for (int i=0;i<in.length;i++) {
	    ret[i] = in[i].getValueCast(mc, toCast[i]);
	}

	return ret;
    }

    // builds an array of Attribute types from an array of intermediates
    public static Attribute[] getAttributes(IntermediateValue[] in) {

	Attribute[] ret = new Attribute[in.length];
	for (int i=0;i<in.length;i++) {
	    ret[i] = in[i].getAttribute();
	    in[i].reset();
	}

	return ret;
    }

    // returns the equivalent AttributeType for a given class value.
    public static AttributeType whichType(Class<?> inType) {
	if (inType.equals(int.class) || inType.equals(long.class) || inType.equals(byte.class) || inType.equals(short.class) ||
	    inType.equals(Integer.class) || inType.equals(Long.class) || inType.equals(Byte.class) || inType.equals(Short.class)) {
	    return new AttributeType(new IntType());
	}

	if (inType.equals(double.class) || inType.equals(float.class) || inType.equals(Double.class) || inType.equals(Float.class)) {
		return new AttributeType(new DoubleType());
	}

	if (inType.equals(double[].class)) {
	    return new AttributeType(new VectorType("vector[vb]"));
	}

	if (inType.equals(double[][].class)) {
	    return new AttributeType(new MatrixType("matrix[var1][var2]"));
	}

	if (inType.equals(String.class)) {
	    return new AttributeType(new StringType("string[vb]"));
	}

	return new AttributeType(new NullType());
    }

    // returns the equivalent AttributeType for a given array of class values.
    public static AttributeType[] whichTypes(Class[] inTypes) {

	AttributeType[] outTypes = new AttributeType[inTypes.length];
	for (int i=0;i<inTypes.length;i++) {
	    outTypes[i] = whichType(inTypes[i]);
	}

	return outTypes;
    }
}
