

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
package simsql.compiler; // package mcdb.compiler.parser.grammar;

import java.util.ArrayList;
import java.util.HashSet;
import simsql.runtime.DataType;
import simsql.runtime.StringType;

/**
 * @author Bamboo
 *
 */
public class ValuesTable
{
	private ArrayList<ArrayList<MathExpression>> tempTableRowList;
	private ArrayList<DataType> schemaTypeList;
	
	public ValuesTable(ArrayList<ArrayList<MathExpression>> _tempTableRowList)
	{
		tempTableRowList = _tempTableRowList;
		schemaTypeList = getType();
	}
	
	@Override
	public String toString()
	{
		String result = "[";
		if(tempTableRowList != null)
		{
			for(int i = 0; i < tempTableRowList.size(); i++)
			{
				ArrayList<MathExpression> row = tempTableRowList.get(i);
				result += "\t";
				
				for(int j = 0; j < row.size(); j++)
				{
					result += row.get(j);
					result += " ";
				}
				result += "\r\n";
			}
		}
		
		result += "]";
		return result;
	}

	public ArrayList<ArrayList<MathExpression>> getTempTableColumnList() {
		return tempTableRowList;
	}
	
	private ArrayList<DataType> getType()
	{
		DataType type;
		ArrayList<DataType> attributeTypeList = new ArrayList<DataType>();
		//1. judge all the elements in the values definition.
		ArrayList<MathExpression> row = tempTableRowList.get(0);
		
		for(int j = 0; j < row.size(); j++)
		{
			MathExpression fromElement = row.get(j);
			
			if(fromElement instanceof NumericExpression)
			{
				type = ((NumericExpression) fromElement).getType();
				attributeTypeList.add(type);
			}
			else if(fromElement instanceof StringExpression)
			{
				type = new StringType();
				attributeTypeList.add(type);
			}
		}
		return attributeTypeList;
	}
	
	public ArrayList<DataType> getSchemaTypeList() {
		return schemaTypeList;
	}

	public ArrayList<Integer> getSchemaTypeSize()
	{
		ArrayList<Integer> schemaTypeSizeList = new ArrayList<Integer>();
		//1. judge all the elements in the values definition.
		ArrayList<MathExpression> row = tempTableRowList.get(0);
		int size[] = new int[row.size()];
		             		
		for(int j = 0; j < row.size(); j++)
		{
			size[j] = 0;
		}
		
		for(int i = 0; i < tempTableRowList.size(); i++)
		{
			row = tempTableRowList.get(i);
			for(int j = 0; j < row.size(); j++)
			{
				MathExpression fromElement = row.get(j);
				
				if(fromElement instanceof DateExpression)
				{
					size[j] += ((DateExpression)(fromElement)).value.length()*32;
				}
				else if(fromElement instanceof NumericExpression)
				{
					size[j] += 64;
				}
				else if(fromElement instanceof StringExpression)
				{
					size[j] += ((StringExpression)(fromElement)).value.length()*32;
				}
			}
		}
		
		for(int i = 0; i < size.length; i++)
		{
			schemaTypeSizeList.add((int)(size[i]/tempTableRowList.size()));
		}
		
		return schemaTypeSizeList;
	}
	
	public ArrayList<Integer> getSchemaUniqueValueSize()
	{
		ArrayList<Integer> schemaUniqueValueSizeList = new ArrayList<Integer>();
		
		ArrayList<HashSet<String>> uniqueValueSet = new ArrayList<HashSet<String>>();
		ArrayList<MathExpression> row = tempTableRowList.get(0);
		for(int i = 0; i < row.size(); i++)
		{
			uniqueValueSet.add(new HashSet<String>());
		}
		
		for(int i = 0; i < tempTableRowList.size(); i++)
		{
			row = tempTableRowList.get(i);
			for(int j = 0; j < row.size(); j++)
			{
				MathExpression fromElement = row.get(j);
				
				if(fromElement instanceof DateExpression)
				{
					uniqueValueSet.get(j).add(((DateExpression)(fromElement)).value);
				}
				else if(fromElement instanceof NumericExpression)
				{
					uniqueValueSet.get(j).add(((NumericExpression)(fromElement)).value + "");
				}
				else if(fromElement instanceof StringExpression)
				{
					uniqueValueSet.get(j).add(((StringExpression)(fromElement)).value);
				}
			}
		}
		
		for(int i = 0; i < uniqueValueSet.size(); i++)
		{
			schemaUniqueValueSizeList.add(uniqueValueSet.get(i).size());
		}
		
		return schemaUniqueValueSizeList;
	}
}
