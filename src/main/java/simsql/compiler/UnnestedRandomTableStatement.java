

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
package simsql.compiler; // package mcdb.compiler.parser.unnester;

import java.util.ArrayList;
import java.util.HashMap;



// import mcdb.compiler.parser.astVisitor.RandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.sqlType.RandomTableStatement;






/**
 * @author Bamboo
 *
 */
public class UnnestedRandomTableStatement{

	private RandomTableStatement randomTableStatement;
	private RandomTableTypeChecker randomTableChecker;
	
	private UnnestedSelectStatement outTableUnnestStatement;
	private ArrayList<String> vgTableNameList;
	private HashMap<String, ArrayList<UnnestedSelectStatement>> vgWrapperParaStatementMap;
	private UnnestedSelectStatement unnestedSelectStatement;
	
	
	public UnnestedRandomTableStatement(RandomTableStatement statement,
			RandomTableTypeChecker typeChecker)
	{
		this.randomTableStatement = statement;
		this.randomTableChecker = typeChecker;
		initialize_data();
	}
	
	private void initialize_data()
	{
		outTableUnnestStatement = null;
		vgTableNameList = new ArrayList<String>();
		vgWrapperParaStatementMap = new HashMap<String, ArrayList<UnnestedSelectStatement>>();
		unnestedSelectStatement = null;
	}
	

	public RandomTableStatement getRandomTableStatement() {
		return randomTableStatement;
	}

	public void setRandomTableStatement(RandomTableStatement randomTableStatement) {
		this.randomTableStatement = randomTableStatement;
	}

	public RandomTableTypeChecker getRandomTableChecker() {
		return randomTableChecker;
	}

	public void setRandomTableChecker(RandomTableTypeChecker randomTableChecker) {
		this.randomTableChecker = randomTableChecker;
	}

	public UnnestedSelectStatement getOutTableUnnestStatement() {
		return outTableUnnestStatement;
	}

	public void setOutTableUnnestStatement(
			UnnestedSelectStatement outTableUnnestStatement) {
		this.outTableUnnestStatement = outTableUnnestStatement;
	}

	public HashMap<String, ArrayList<UnnestedSelectStatement>> getVgWrapperParaStatementMap() {
		return vgWrapperParaStatementMap;
	}

	public void setVgWrapperParaStatementMap(
			HashMap<String, ArrayList<UnnestedSelectStatement>> vgWrapperParaStatementMap) {
		this.vgWrapperParaStatementMap = vgWrapperParaStatementMap;
	}

	public UnnestedSelectStatement getUnnestedSelectStatement() {
		return unnestedSelectStatement;
	}

	public void setUnnestedSelectStatement(
			UnnestedSelectStatement unnestedSelectStatement) {
		this.unnestedSelectStatement = unnestedSelectStatement;
	}

	public ArrayList<String> getVgTableNameList() {
		return vgTableNameList;
	}

	public void setVgTableNameList(ArrayList<String> vgTableNameList) {
		this.vgTableNameList = vgTableNameList;
	}

}
