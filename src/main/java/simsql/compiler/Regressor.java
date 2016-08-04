

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


/**
 * This class encapsulates regressor information -- it keeps data
 * related to query operator input/output cardinalities.
 * 
 * @author Luis
 */

public class Regressor
{ 

    /** query ID */
    private int queryID;

    /** regressor name -- i.e. which operator */
    private String regressorName;

    /** input cardinality */
    private double input;

    /** output cardinality */
    private double output;

    /** Default constructor */
    public Regressor(int _queryID, String _regressorName, double _input, double _output) {
	queryID = _queryID;
	regressorName = _regressorName;
	input = _input;
	output = _output;
    }


    /** Public getters */
    public int getQueryID() {
	return(queryID);
    }

    public String getRegressorName() {
	return(regressorName);
    }

    public double getInput() {
	return(input);
    }

    public double getOutput() {
	return(output);
    }

    /** Public setters */
    public void setQueryID(int _queryID) {
	queryID = _queryID;
    }

    public void setRegressorName(String _regressorName) {
	regressorName = _regressorName;
    }

    public void setInput(double _input) {
	input = _input;
    }

    public void setOutput(double _output) {
	output = _output;
    }
    
}
