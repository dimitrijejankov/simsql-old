

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


/**
 * 
 */
package simsql.compiler; // package mcdb.catalog;

import java.util.ArrayList;

/**
 * @author Bamboo
 *
 */
public class View {
	/** Relation name */
	  private String name;

	  /** Physical location */
	  private String sql;

	  /** Set of attributes. Positional */
	  private ArrayList<Attribute> attributes;
	  
	  private int type;

	  public View(String name, String sql, ArrayList<Attribute> attributes, int type)
	  {
		  this.name = name;
		  this.sql = sql;
		  this.attributes = attributes;
		  this.type = type;
	  }

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public ArrayList<Attribute> getAttributes() {
		return attributes;
	}
	
	

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public void setAttributes(ArrayList<Attribute> attributes) {
		this.attributes = attributes;
	}
	  
	public String toString()
	{
		 return(name + ": (" + sql + "), " + attributes);
	}
}
