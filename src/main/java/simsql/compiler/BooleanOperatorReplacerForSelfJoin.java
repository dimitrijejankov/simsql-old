

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


package simsql.compiler;

import java.util.ArrayList;

public class BooleanOperatorReplacerForSelfJoin {
	private String originalName;
	private String mappedName;
	
	public BooleanOperatorReplacerForSelfJoin(String originalName,
			String mappedName) {
		super();
		this.originalName = originalName;
		this.mappedName = mappedName;
	}

	public String getOriginalName() {
		return originalName;
	}

	public void setOriginalName(String originalName) {
		this.originalName = originalName;
	}

	public String getMappedName() {
		return mappedName;
	}

	public void setMappedName(String mappedName) {
		this.mappedName = mappedName;
	}
	
	public void replaceAttributeInBooleanOperator(BooleanOperator element) 
	{
		if (element instanceof AndOperator) 
		{
			replaceAttributeInAndOperator((AndOperator) element);
		} 
		else if (element instanceof AtomOperator) 
		{
			replaceAttributeInAtomOperator((AtomOperator) element);
		} 
		else if (element instanceof CompOperator )
		{
			replaceAttributeInCompOperator((CompOperator) element);
		} 
		else if (element instanceof NotOperator ) 
		{
			replaceAttributeInNotOperator((NotOperator) element);
		} 
		else if (element instanceof OrOperator) 
		{
			replaceAttributeInOrOperator((OrOperator) element);
		} 
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInAndOperator(AndOperator element) {
		ArrayList<BooleanOperator> operatorList = element.getOperatorList();
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				replaceAttributeInBooleanOperator(operatorList.get(i));
			}
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInAtomOperator(AtomOperator element) {
		return;
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInCompOperator(CompOperator element) 
	{
		MathOperator left = element.getLeft();
		MathOperator right = element.getRight();
		
		MathOperatorReplacerForSelfJoin mathReplacer = new MathOperatorReplacerForSelfJoin(originalName, mappedName);
		
		if(left != null)
			mathReplacer.replaceAttributeInMathOperator(left);
		
		if(right != null)
			mathReplacer.replaceAttributeInMathOperator(right);
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInNotOperator(NotOperator element) {
		BooleanOperator operator = element.getBooleanOperator();
		if(operator != null)
		{
			replaceAttributeInBooleanOperator(operator);
		}
	}

	/**
	 * @param element
	 * @return
	 */
	private void replaceAttributeInOrOperator(OrOperator element) {
		ArrayList<BooleanOperator> operatorList = element.getOperatorList();
		if(operatorList != null)
		{
			for(int i = 0; i < operatorList.size(); i++)
			{
				replaceAttributeInBooleanOperator(operatorList.get(i));
			}
		}
	}
}
