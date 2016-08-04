

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
 * Encapsulates all the information for a regular function such as "sqrt", which is: name, input attributes, and output attribute
 */
public class Function {
  
  /** Function name */
  private String name;

  /** Set of input attributes. Positional. */
  private ArrayList<Attribute> inputAtts;

  /** output attribute. Positional. */
  private Attribute outputAtt;

  /** Public constructor */
  public Function(String _name, ArrayList<Attribute> _inputAtts, Attribute _outputAtt) {
    name = _name;
    inputAtts = _inputAtts;
    outputAtt = _outputAtt;
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
  public Attribute getOutputAtt() {
    return(outputAtt);
  }

  /** Returns a string representation, for printf() */
  public String toString() {
    return(name + ": in=" + inputAtts + ", out=" + outputAtt);
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
	
	public String getOutputAttribute()
	{
		return outputAtt.getName ();
	}
	
}
