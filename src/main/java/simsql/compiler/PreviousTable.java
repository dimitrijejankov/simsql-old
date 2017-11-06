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
package simsql.compiler; // package mcdb.runtime;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Bamboo
 *
 */
public class PreviousTable
{
	private String fileDirectory;
	private String tableName;
	private ArrayList<String> attributeList;
	private HashMap<String, String> attributeMap;
	private ArrayList<String> randamAttributeList;
	private ArrayList<Attribute> realAttributeList;
	private ArrayList<String> primaryKey;
	
	private HashMap<String, Long> uniqueValueNumMap;
	private long tupleNum;
	/**
	 * @param fileDirectory
	 * @param tableName
	 * @param attributeList
	 * @param attributeMap
	 * @param randamAttributeList
	 */
	public PreviousTable(String fileDirectory, String tableName,
						 ArrayList<String> attributeList,
						 HashMap<String, String> attributeMap,
						 ArrayList<String> randamAttributeList) {
		super();
		this.fileDirectory = fileDirectory;
		this.tableName = tableName;
		this.attributeList = attributeList;
		this.attributeMap = attributeMap;
		this.randamAttributeList = randamAttributeList;
		
		uniqueValueNumMap = new HashMap<String, Long>();
		tupleNum = -1;
	}

	public PreviousTable(String fileDirectory, String tableName,
						 ArrayList<String> attributeList,
						 HashMap<String, String> attributeMap,
						 ArrayList<String> randamAttributeList,
						 ArrayList<Attribute> realAttributeList) {
		super();
		this.fileDirectory = fileDirectory;
		this.tableName = tableName;
		this.attributeList = attributeList;
		this.attributeMap = attributeMap;
		this.randamAttributeList = randamAttributeList;
		this.realAttributeList = realAttributeList;

		uniqueValueNumMap = new HashMap<String, Long>();
		tupleNum = -1;
	}

	/**
	 * TODO this is temporary and needs to be refactored
	 * @return
	 */
	public ArrayList<Attribute> getRealAttributeList() {
		return realAttributeList;
	}

	/**
	 * @return the fileDirectory
	 */
	public String getFileDirectory() {
		return fileDirectory;
	}
	/**
	 * @param fileDirectory the fileDirectory to set
	 */
	public void setFileDirectory(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}
	/**
	 * @return the tableName
	 */
	public String getTableName() {
		return tableName;
	}
	/**
	 * @param tableName the tableName to set
	 */
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	/**
	 * @return the attributeList
	 */
	public ArrayList<String> getAttributeList() {
		return attributeList;
	}
	/**
	 * @param attributeList the attributeList to set
	 */
	public void setAttributeList(ArrayList<String> attributeList) {
		this.attributeList = attributeList;
	}
	/**
	 * @return the attributeMap
	 */
	public HashMap<String, String> getAttributeMap() {
		return attributeMap;
	}
	/**
	 * @param attributeMap the attributeMap to set
	 */
	public void setAttributeMap(HashMap<String, String> attributeMap) {
		this.attributeMap = attributeMap;
	}
	/**
	 * @return the randamAttributeList
	 */
	public ArrayList<String> getRandamAttributeList() {
		return randamAttributeList;
	}
	/**
	 * @param randamAttributeList the randamAttributeList to set
	 */
	public void setRandamAttributeList(ArrayList<String> randamAttributeList) {
		this.randamAttributeList = randamAttributeList;
	}
	/**
	 * @return the uniqueValueNumMap
	 */
	public HashMap<String, Long> getUniqueValueNumMap() {
		return uniqueValueNumMap;
	}
	/**
	 * @param uniqueValueNumMap the uniqueValueNumMap to set
	 */
	public void setUniqueValueNumMap(HashMap<String, Long> uniqueValueNumMap) {
		this.uniqueValueNumMap = uniqueValueNumMap;
	}
	/**
	 * @return the tupleNum
	 */
	public long getTupleNum() {
		return tupleNum;
	}
	/**
	 * @param tupleNum the tupleNum to set
	 */
	public void setTupleNum(long tupleNum) {
		this.tupleNum = tupleNum;
	}

	public ArrayList<String> getPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(ArrayList<String> primaryKey) {
		this.primaryKey = primaryKey;
	}

	public void addStat(String attribute, long statValue)
	{
		if(!uniqueValueNumMap.containsKey(attribute))
		{
			uniqueValueNumMap.put(attribute, statValue);
		}
	}
	
	public ArrayList<Integer> getRandomAttributeIndex()
	{
		ArrayList<Integer> resultList = new ArrayList<Integer>();
		
		if(randamAttributeList != null)
		{
			for(int i = 0; i < randamAttributeList.size(); i++)
			{
				resultList.add(attributeList.indexOf(randamAttributeList.get(i)));
			}
		}
		
		return resultList;
	}
}	
