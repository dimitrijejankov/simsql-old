

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


package simsql.compiler; // package mcdb.catalog;

import simsql.runtime.DataType;

/**
 * Encapsulates a single attribute, as a (name,type) pair. Also contains the
 * definition of all types.
 * 
 * @author Luis, Bamboo
 */
public class Attribute {

	/* Attribute type definitions/constants */
	// public final static String INTEGER = "integer";
	// public final static String DOUBLE = "double";
	// public final static String STRING = "string";
	// public final static String DATE = "date";
	// public final static String STOCHINT = "stochint";
	// public final static String STOCHDBL = "stochdbl";
	// public final static String STOCHSTR = "stochstr";
	// public final static String STOCHDAT = "stochdat";
	// public final static String SEED = "seed";
	// public final static String BOTTOM = "bottom";
	// public final static String MULTITYPE = "multitype";
	// public final static String TOTALTYPE = "totaltype";
	// public final static String UNKNOWN = "unknown";
	// public final static String HYBRID = "hybrid";

	/** Relation name. Optional. */
	private String relName;

	/** Attribute name */
	private String name;

	/** Attribute type */
	private DataType type;

	/*------------------------modified by Bamboo-----------------------------*/
	private String domainType;
	private String domainSize;
	/*------------------------ end for VGAttribute------------------------------------------*/

	/*------------------------modified by Bamboo-----------------------------*/
	private long uniqueValue;
	/*------------------------ end for VGAttribute------------------------------------------*/

	/** Public constructor for "regular" attributes */

	public Attribute(String _name, DataType _type, String _relName) {
		name = _name;
		type = _type;
		relName = _relName;
		domainType = null;
		domainSize = null;
	}

	/* Support VGAttribute */
	public Attribute(String _name, DataType _type, String _relName,
			String domainType, String domainSize) {
		name = _name;
		type = _type;
		relName = _relName;
		this.domainType = domainType;
		this.domainSize = domainSize;
	}

	public Attribute(String _name, DataType _type, String _relName,
			long _uniqueValue) {
		name = _name;
		type = _type;
		relName = _relName;
		uniqueValue = _uniqueValue;
	}

	/** Public constructor, without relation name */
	public Attribute(String _name, DataType _type) {
		name = _name;
		type = _type;
		relName = "";
	}

	/** Returns the attribute name */
	public String getName() {
		return (name);
	}

	/** Tells us if this guy is random */
	public boolean getIsRandom() {
		return type.ifStoch();
	}

	/** Returns the attribute type */
	public DataType getType() {
		return (type);
	}

	/** Returns the relation name */
	public String getRelName() {
		return (relName);
	}

	/** Sets the attribute name */
	public void setName(String _name) {
		name = _name;
	}

	/** Sets the attribute type */
	public void setType(DataType _type) {
		type = _type;
	}

	/** Sets the relation name */
	public void setRelName(String _relName) {
		relName = _relName;
	}

	/**
	 * @return the domainType
	 */
	public String getDomainType() {
		return domainType;
	}

	/**
	 * @param domainType
	 *            the domainType to set
	 */
	public void setDomainType(String domainType) {
		this.domainType = domainType;
	}

	/**
	 * @return the domainSize
	 */
	public String getDomainSize() {
		return domainSize;
	}

	/**
	 * @param domainSize
	 *            the domainSize to set
	 */
	public void setDomainSize(String domainSize) {
		this.domainSize = domainSize;
	}

	/** Returns a string representation, for printf() */
	public String toString() {
		return (relName + "." + name + ":" + type.writeOut() + ":" + uniqueValue);
	}

	/** Returns the entire name of the attribute */
	public String getFullName() {
		return (relName + "." + name);
	}

	/**
	 * @return the attributeSize
	 */
	public int getAttributeSize() {
		return type.getSizeInBytes();
	}

	/**
	 * @return the uniqueValue
	 */
	public long getUniqueValue() {
		return uniqueValue;
	}

	/**
	 * @param uniqueValue
	 *            the uniqueValue to set
	 */
	public void setUniqueValue(long uniqueValue) {
		this.uniqueValue = uniqueValue;
	}

	/****
	public String getTypeStr() {
		return type.getTypeName();				// TO-DO
		// switch (type) {
		// case INTEGER:
		// 	return ("integer");
		// case DOUBLE:
		// 	return ("double");
		// case STRING:
		// 	return ("string");
		// case DATE:
		// 	return ("date");
		// case STOCHINT:
		// 	return ("stochint");
		// case STOCHDBL:
		// 	return ("stochdbl");
		// case STOCHSTR:
		// 	return ("stochstr");
		// case STOCHDAT:
		// 	return ("stochdat");
		// case SEED:
		// 	return ("seed");
		// case BOTTOM:
		// 	return ("bottom");
		// default:
		// 	return ("unknown");
		// }
	}
	****/
	
	public simsql.runtime.Attribute getPhysicalRealization() {
		return type.getPhysicalRealization();		// TO-DO

		// switch (type) {
		// case INTEGER:
		// 	return new simsql.runtime.IntAttribute();
		// case DOUBLE:
		// 	return new simsql.runtime.DoubleAttribute();
		// case STRING:
		// 	return new simsql.runtime.StringAttribute();
		// case DATE:
		// 	return new simsql.runtime.StringAttribute();
		// case STOCHINT:
		// 	return new simsql.runtime.IntArrayAttribute();
		// case STOCHDBL:
		// 	return new simsql.runtime.DoubleArrayAttribute();
		// case STOCHSTR:
		// 	return new simsql.runtime.StringArrayAttribute();
		// case STOCHDAT:
		// 	return new simsql.runtime.StringArrayAttribute();
		// case SEED:
		// 	return new simsql.runtime.SeedAttribute();
		// }

		// throw new RuntimeException(
		// 		"tried to convert an attribute type I didn't recognize!");
	}
	
	/****
	public String getTypeStr(boolean isRandom) {
		if (!isRandom)
			return (getTypeStr());

		return type.getTypeNameStoch();				// TO-DO

		// switch (type) {
		// case INTEGER:
		// 	return ("stochint");
		// case DOUBLE:
		// 	return ("stochdbl");
		// case STRING:
		// 	return ("stochstr");
		// case DATE:
		// 	return ("stochdat");
		// case STOCHINT:
		// 	return ("stochint");
		// case STOCHDBL:
		// 	return ("stochdbl");
		// case STOCHSTR:
		// 	return ("stochstr");
		// case STOCHDAT:
		// 	return ("stochdat");
		// case SEED:
		// 	return ("seed");
		// case BOTTOM:
		// 	return ("bottom");
		// default:
		// 	return ("unknown");
		// }
	}
	****/

	/** Returns a copy of this object */
	public Attribute clone() {
		return new Attribute(name, type, relName);
	}
}
