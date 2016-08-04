

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

public class SQLTextHelper {
	public static String removeComments(String query)
	{
		int start = query.indexOf("--");
		int end;
		String pre, suf;
		
		while(start >= 0)
		{
			end = query.indexOf("\n", start);
			pre = query.substring(0, start);
			suf = query.substring(end + 1, query.length());
			query = pre + suf;
			
			start = query.indexOf("--");
		}
		
		return query;
	}
	
	public static String toLowerCase(String s) throws Exception
	{
		ArrayList<Integer> arrayList = new ArrayList<Integer>();
		for(int i = 0; i < s.length(); i++)
		{
			if(i == 0 && s.charAt(i) == '\'')
			{
				arrayList.add(i);
			}
			else if(s.charAt(i) == '\'' && s.charAt(i-1) != '\\')
			{
				arrayList.add(i);
			}
		}
		
		String result = "";
		if(arrayList.size() == 0)
		{
			return s.toLowerCase();
		}
		else
		{
			result = new String(s).toLowerCase();
		}
		
		StringBuffer resultBuffer = new StringBuffer(result);
		
		for(int  i = 0; i < arrayList.size(); i+= 2)
		{
			int start = arrayList.get(i);
			if(i+1 >= arrayList.size())
			{
				throw new Exception("string in the sql is wrong!");
			}
			else
			{
				int end = arrayList.get(i+1);
				resultBuffer.replace(start, end, s.substring(start, end));
			}
		}
		
		return resultBuffer.toString();
	}
}
