

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
package simsql.compiler; // package mcdb.compiler.logicPlan.postProcessor;

import java.util.HashMap;

/**
 * @author Bamboo
 *
 */
public class AttributeNameSpace {
	private static HashMap<String, Integer> nameSpaceMap = new HashMap<String, Integer>();
	
	public static String getString(String name)
	{
		name = getIdentifier(name);
		int index;
		if(!nameSpaceMap.containsKey(name))
		{
			index = 0;
		}
		else
		{
			index = nameSpaceMap.get(name);
			index++;			
		}
		
		nameSpaceMap.put(name, index);
		
		if(name.length() > 100)
		{
			name = name.substring(0, 100);
		}
		
		return "mcdb_"+ name +"_"+index;
	}
	
	public static String getIdentifier(String name)
	{
		if(isIdentifier(name))
		{
			return name;
		}
		else
		{
			char letters[] = name.toCharArray();
			for(int i = 0; i < letters.length; i++)
			{
				if( (letters[i] >= '0' && letters[i] <= '9') ||
					(letters[i] >= 'a' && letters[i] <= 'z') ||
					(letters[i] >= 'A' && letters[i] <= 'Z') ||
					letters[i] == '_'
				  )
				{
					continue;
				}
				else
				{
					letters[i] = '_';
				}
			}
			
			return new String(letters);
		}
	}
	
	public static boolean isIdentifier(String name)
	{
		char letters[] = name.toCharArray();
		for(int i = 0; i < letters.length; i++)
		{
			if( (letters[i] >= '0' && letters[i] <= '9') ||
				(letters[i] >= 'a' && letters[i] <= 'z') ||
				(letters[i] >= 'A' && letters[i] <= 'Z') ||
				letters[i] == '_'
			  )
			{
				continue;
			}
			else
			{
				return false;
			}
		}
		
		return true;
	}
}
