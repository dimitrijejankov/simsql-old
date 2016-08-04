

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
import java.util.*;

/** 
 * Represents a relation, composed of attributes and keys.
 *
 * @author Luis, Bamboo
 */
public class Relation {

  /** Relation name */
  private String name;

  /** Physical location */
  private String fileName;

  /** Set of attributes. Positional */
  private ArrayList<Attribute> attributes;

  /** Set of attributes that defines the primary key. Optional */
  private ArrayList<String> primaryKey;

  /** Foreign keys. */
  private ArrayList<ForeignKey> foreignKeys;
  
/* denotes whether the relations is a temporary table */
  private boolean isTemporary;

  private ArrayList<ArrayList<String>> tableRowList;

  private long tupleNum;

  /** Public constructor */
  public Relation(String _name, String _fileName, ArrayList<Attribute> _attributes) {
    name = _name;
    fileName = _fileName;
    attributes = _attributes;
    primaryKey = new ArrayList<String>();
    foreignKeys = new ArrayList<ForeignKey>();
 	isTemporary = false;
    tableRowList = null;
  }
  
  /** Public constructor */
  public Relation(String _name, String _fileName, ArrayList<Attribute> _attributes, String domainType, String domainSize) {
    name = _name;
    fileName = _fileName;
    attributes = _attributes;
    primaryKey = new ArrayList<String>();
    foreignKeys = new ArrayList<ForeignKey>();
  }

/* constructor for temporary table */
  public Relation(String _name, 
  		  String _fileName, 
		  ArrayList<Attribute> _attributes, 
		  ArrayList<ArrayList<String>> _rows,
		  boolean isTemp) {
    name = _name;
    fileName = _fileName;
    attributes = _attributes;
    isTemporary = isTemp;
    tableRowList = _rows;
  }
  
  /** Public constructor + primary key */
  public Relation(String _name, String _fileName, ArrayList<Attribute> _attributes, ArrayList<String> _primaryKey) {
    name = _name;
    fileName = _fileName;
    attributes = _attributes;
    primaryKey = _primaryKey;
    foreignKeys = new ArrayList<ForeignKey>();
	 isTemporary = false;
    tableRowList = null;
  }

  /** Public constructor + primary key + foreign key */
    public Relation(String _name, String _fileName, ArrayList<Attribute> _attributes, ArrayList<String> _primaryKey, ArrayList<ForeignKey> _foreignKeys) {
    name = _name;
    fileName = _fileName;
    attributes = _attributes;
    primaryKey = _primaryKey;
    foreignKeys = _foreignKeys;
  }
    
    public Relation(String _name, String _fileName, ArrayList<Attribute> _attributes, ArrayList<String> _primaryKey, ArrayList<ForeignKey> _foreignKeys, long tupleNum) {
        name = _name;
        fileName = _fileName;
        attributes = _attributes;
        primaryKey = _primaryKey;
        foreignKeys = _foreignKeys;
        this.tupleNum = tupleNum;
      }
    
    /** Public constructor + primary key + foreign key */
    public Relation(String _name, 
		    		String _fileName, 
		    		ArrayList<Attribute> _attributes, 
		    		long _tupleNum,
		    		ArrayList<String> _primaryKey, 
		    		ArrayList<ForeignKey> _foreignKeys) {
	    name = _name;
	    fileName = _fileName;
	    attributes = _attributes;
	    tupleNum = _tupleNum;
	    primaryKey = _primaryKey;
	    foreignKeys = _foreignKeys;
		isTemporary = false;
    	tableRowList = null;
    }
    
    /** Public constructor + primary key + foreign key */
    public Relation(String _name, 
		    		String _fileName, 
		    		ArrayList<Attribute> _attributes, 
		    		long _tupleNum,
		    		ArrayList<String> _primaryKey, 
		    		ArrayList<ForeignKey> _foreignKeys,
		    		boolean isTemp) {
	    name = _name;
	    fileName = _fileName;
	    attributes = _attributes;
	    tupleNum = _tupleNum;
	    primaryKey = _primaryKey;
	    foreignKeys = _foreignKeys;
		isTemporary = isTemp;
    	tableRowList = null;
    	
    }
  
  /** Returns the relation name */
  public String getName() {
    return(name);
  }

  /** Returns the physical location of the relation */
  public String getFileName() {
    return(fileName);
  }

  /** Returns the set of attributes */
  public ArrayList<Attribute> getAttributes() {
    return(attributes);
  }

  /** Returns the set of attributes that define the primary key */
  public ArrayList<String> getPrimaryKey() {
    return(primaryKey);
  }

  /** Returns the set of foreign keys */
  public ArrayList<ForeignKey> getForeignKeys() {
    return(foreignKeys);
  }

  /** Sets the relation name */
  public void setName(String _name) {
    name = _name;
  }

  /** Sets the physical location of the relation */
  public void setFileName(String _fileName) {
    fileName = _fileName;
  }

  /** Returns a string representation, for print() */
  public String toString() {
	  return(name + ": (" + fileName + "), " + attributes + ", pk=" + primaryKey + ", fk=" + foreignKeys);
  }

  /** A nicer string representation. */
  public String formatString() {
    
    String attStr = String.format("%-35s | %-15s | %-15s%n",
				  "attribute", "type", "unique values");    

    attStr += new String(new char[65]).replace("\0", "-") + "\n";

    for (Attribute a: attributes) {
      attStr += String.format(
			      "%-35s | %-15s | %-15d%n",
			      a.getName(), a.getType().writeOut(), a.getUniqueValue());
    }

    return String.format(
			 "%s%n" +
			 "Number of records: %d%n", attStr, tupleNum);
  }

  public boolean isTemporaryTable() {
    return (isTemporary);
  }
/**
 * @return the tupleNum
 */
public long getTupleNum() {
	return tupleNum;
}

  public ArrayList<ArrayList<String>> getRows() {
      return (tableRowList);
  }


/**
 * @param tupleNum the tupleNum to set
 */
public void setTupleNum(long tupleNum) {
	this.tupleNum = tupleNum;
}
  
  
}
