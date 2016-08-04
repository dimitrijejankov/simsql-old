

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
 * Encapsulates all the information for a VG Function, which is: name,
 * library file location, input attributes and output attributes.
 *
 * @author Luis, Bamboo
 */
public class VGFunction {
  
  /** Function name */
  private String name;

  /*-----------------changed by Bamboo--------------------------*/
  private int bundlesPerTuple;
  /*--------------------end-------------------------------------*/
  
  /** Set of input attributes. Positional. */
  private ArrayList<Attribute> inputAtts;

  /** Set of output attributes. Positional. */
  private ArrayList<Attribute> outputAtts;

  /** Public constructor */
  public VGFunction(String _name, int _bundlesPerTuple, ArrayList<Attribute> _inputAtts, ArrayList<Attribute> _outputAtts) {
    name = _name;
    bundlesPerTuple = _bundlesPerTuple;
    inputAtts = _inputAtts;
    outputAtts = _outputAtts;
  }

  /** Returns the function name */
  public String getName() {
    return(name);
  }

  /** Sets the function name */
  public void setName(String _name) {
    name = _name;
  }

  /** Returns the input attributes */
  public ArrayList<Attribute> getInputAtts() {
    return(inputAtts);
  }

  /** Returns the output attributes */
  public ArrayList<Attribute> getOutputAtts() {
    return(outputAtts);
  }

  /** Returns a string representation, for printf() */
  public String toString() {
    return(name + ": in=" + inputAtts + ", out=" + outputAtts);
  }
	
	/**
	 * @return the bundlesPerTuple
	 */
	public int getBundlesPerTuple() {
		return bundlesPerTuple;
	}
	
	/**
	 * @param bundlesPerTuple the bundlesPerTuple to set
	 */
	public void setBundlesPerTuple(int bundlesPerTuple) {
		this.bundlesPerTuple = bundlesPerTuple;
	}
	  
	public ArrayList<String> getInputAttributes()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < inputAtts.size(); i++)
		{
			resultList.add(inputAtts.get(i).getName());
		}
		return resultList;
	}
	
	public ArrayList<String> getOutputAttributes()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < outputAtts.size(); i++)
		{
			resultList.add(outputAtts.get(i).getName());
		}
		return resultList;
	}
	
}
