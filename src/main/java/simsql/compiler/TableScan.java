

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.relationOperator;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author Bamboo
 *
 */
public class TableScan extends Operator{

	private String tableName;
	private ArrayList<String> attributeList;
	
	/*
	 * Here vgStatistics share the inputAttributeNameList and outputAttributeNameList
	 */
	private RelationStatistics relationStatistics;
	private Catalog catalog;
	
	private String indexString;
	private MathExpression indexMathExp;
	private int type;
	
	/*
	 * Add a data struture here for supporting simulation.
	 */
	private PreviousTable tableInfo;
	
	/**
	 * @param nodeName
	 * @param translatedStatement
	 * @param children
	 * @param parents
	 */
	public TableScan(String nodeName, 
			ArrayList<Operator> children, 
			ArrayList<Operator> parents)
	{
		super(nodeName, children, parents);
		this.type = TableReference.COMMON_TABLE;
		tableInfo = null;
		indexMathExp = null;
	}
	
	
	public TableScan(String nodeName, 
					 ArrayList<Operator> children, 
					 ArrayList<Operator> parents,
					 String tableName, 
					 ArrayList<String> attributeList,
					 RelationStatistics relationStatistics,
					 int type,
					 MathExpression exp)
	{
		super(nodeName, children, parents);
		this.tableName = tableName;
		this.attributeList = attributeList;
		this.relationStatistics = relationStatistics;
		this.catalog = SimsqlCompiler.catalog;
		this.indexString = null;
		tableInfo = null;
		this.type = type;
		this.indexMathExp = exp;
	}
	
	public TableScan(String nodeName, 
			 ArrayList<Operator> children, 
			 ArrayList<Operator> parents,
			 String tableName, 
			 ArrayList<String> attributeList,
			 RelationStatistics relationStatistics,
			 String indexString,
			 int type,
			 MathExpression exp)
	{
		super(nodeName, children, parents);
		this.tableName = tableName;
		this.attributeList = attributeList;
		this.relationStatistics = relationStatistics;
		this.catalog = SimsqlCompiler.catalog;
		this.indexString = indexString;
		tableInfo = null;
		this.type = type;
		this.indexMathExp = exp;
	}

	/*
	 * The tableName of this tableScan.
	 */
	public String getTableName() {
		return tableName;
	}
	public void setTableName(String tableName) {
		this.tableName = tableName;
	}
	public ArrayList<String> getAttributeList() {
		return attributeList;
	}
	public void setAttributeList(ArrayList<String> attributeList) {
		this.attributeList = attributeList;
	}


	public int getType() {
		return type;
	}


	public void setType(int type) {
		this.type = type;
	}

	public String getIndexString() {
		return indexString;
	}


	public void setIndexString(String indexString) {
		this.indexString = indexString;
	}


	/* (non-Javadoc)
	 * @see logicOperator.relationOperator.Operator#visitNode()
	 */
	@Override
	public String visitNode()throws Exception{
		String result = "";
		if(indexString == null && type == TableReference.COMMON_TABLE)
		{
			
			result += this.getNodeStructureString();
			
			result += "tablescan(" + this.getNodeName() + ", '" + tableName;
			result += "', [";
			
			if(attributeList != null)
			{
				for(int i = 0; i < attributeList.size(); i++)
				{
					result += attributeList.get(i);
					
					if(i != attributeList.size() - 1)
					{
						result += ", ";
					}
				}
			}
			
			result += "]).\r\n";
			
			/* add the statistics of this table and its attributes */
			result += "stats(" + this.getNodeName() + ", [";
			
			Relation relation = catalog.getRelation(tableName);
			ArrayList<Attribute> realAttributeList = relation.getAttributes();
			String attributeAlias;
			Attribute attribute;
			int attributeSize;
			long uniqueValue;
			
			if(attributeList != null)
			{
				for(int i = 0; i < attributeList.size(); i++)
				{
					attributeAlias = attributeList.get(i);
					attribute = realAttributeList.get(i);
					
					uniqueValue = attribute.getUniqueValue();
					result += "uniqueValues(" + attributeAlias + ", " + uniqueValue +")";
					
					if(i != attributeList.size() - 1)
					{
						result += ", ";
					}
				}
			}
			
			result += "], [], \r\n\t\t[";
			
			if(attributeList != null)
			{
				for(int i = 0; i < attributeList.size(); i++)
				{
					attributeAlias = attributeList.get(i);
					attribute = realAttributeList.get(i);
					
					attributeSize = attribute.getAttributeSize();
					result += "attributeSize(" + attributeAlias + ", " + attributeSize +")";
					
					if(i != attributeList.size() - 1)
					{
						result += ", ";
					}
				}
			}
			
			result += "], " + relation.getTupleNum() + ", _).\r\n";
			
			//primary key
			ArrayList<String> primaryKey = relation.getPrimaryKey();
			
			if(primaryKey != null && primaryKey.size() >= 1)
			{
				int keyIndex;
				
				HashMap<String, Integer> attributeIndexMap = new HashMap<String, Integer>();
				if(realAttributeList != null)
				{
					for(int i = 0; i < realAttributeList.size(); i++)
					{
						attribute = realAttributeList.get(i);
						attributeIndexMap.put(attribute.getName(), i);
					}
				}
				
				result += "candidateKey(" + this.getNodeName();
				result += ", [";
				
				for(int i = 0; i < primaryKey.size(); i++)
				{
					keyIndex = attributeIndexMap.get(primaryKey.get(i));
					
					result += attributeList.get(keyIndex);
					
					if(i != primaryKey.size()-1)
					{
						result += ", ";
					}
				}
				
				result += "]).\r\n";
			}
		}
		else
		{
			if(tableName.endsWith("_i"))
			{
				result += this.getNodeStructureString();
				
				String indexString = new MPNGenerator(this.indexMathExp).convertToMPN();
				result += "tablescan(" + this.getNodeName() + ", '" +
						tableName.substring(0, tableName.length()-2) + 
						"_" + indexString;
				
				
				result += "', [";
				
				if(attributeList != null)
				{
					for(int i = 0; i < attributeList.size(); i++)
					{
						result += attributeList.get(i);
						
						if(i != attributeList.size() - 1)
						{
							result += ", ";
						}
					}
				}
				
				result += "]).\r\n";
				
				/* add the statistics of this table and its attributes */
				result += "stats(" + this.getNodeName() + ", [";
				
				View view = catalog.getView(tableName);
				ArrayList<Attribute> realAttributeList = view.getAttributes();
				String attributeAlias;
				Attribute attribute;
				int attributeSize;
				long uniqueValue;
				
				if(attributeList != null)
				{
					for(int i = 0; i < attributeList.size(); i++)
					{
						attributeAlias = attributeList.get(i);
						attribute = realAttributeList.get(i);
						
						uniqueValue = attribute.getUniqueValue();
						result += "uniqueValues(" + attributeAlias + ", " + uniqueValue +")";
						
						if(i != attributeList.size() - 1)
						{
							result += ", ";
						}
					}
				}
				
				result += "], [], \r\n\t\t[";
				
				if(attributeList != null)
				{
					for(int i = 0; i < attributeList.size(); i++)
					{
						attributeAlias = attributeList.get(i);
						attribute = realAttributeList.get(i);
						
						attributeSize = attribute.getAttributeSize();
						result += "attributeSize(" + attributeAlias + ", " + attributeSize +")";
						
						if(i != attributeList.size() - 1)
						{
							result += ", ";
						}
					}
				}
				
				result += "], " + "_" + ", _).\r\n";
			}
			else
			{
				result += this.getNodeStructureString();
				
				result += "tablescan(" + this.getNodeName() + ", '" + tableName;
				result += "', [";
				
				if(attributeList != null)
				{
					for(int i = 0; i < attributeList.size(); i++)
					{
						result += attributeList.get(i);
						
						if(i != attributeList.size() - 1)
						{
							result += ", ";
						}
					}
				}
				
				result += "]).\r\n";
				
				/* add the statistics of this table and its attributes */
				result += "stats(" + this.getNodeName() + ", [";
				
				if(tableInfo == null)
				{
					View view = catalog.getView(tableName);
					ArrayList<Attribute> realAttributeList = view.getAttributes();
					String attributeAlias;
					Attribute attribute;
					int attributeSize;
					long uniqueValue;
					
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							attributeAlias = attributeList.get(i);
							attribute = realAttributeList.get(i);
							
							uniqueValue = attribute.getUniqueValue();
							result += "uniqueValues(" + attributeAlias + ", " + uniqueValue +")";
							
							if(i != attributeList.size() - 1)
							{
								result += ", ";
							}
						}
					}
					
					result += "], [], \r\n\t\t[";
					
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							attributeAlias = attributeList.get(i);
							attribute = realAttributeList.get(i);
							
							attributeSize = attribute.getAttributeSize();
							result += "attributeSize(" + attributeAlias + ", " + attributeSize +")";
							
							if(i != attributeList.size() - 1)
							{
								result += ", ";
							}
						}
					}
					
					result += "], " + "_" + ", _).\r\n";
				}
				else //tableInfo != null
				{
					String attributeAlias;
					String attribute;
					int attributeSize;
					long uniqueValue;
					
					View view = catalog.getView(tableName);
					ArrayList<Attribute> realAttributeList = view.getAttributes();
					
					String fileDirectory = tableInfo.getFileDirectory();
					ArrayList<String> hdfsAttributeList = tableInfo.getAttributeList();
					HashMap<String, String> hdfsAttributeTypeMap = tableInfo.getAttributeMap();
					ArrayList<String> hdfsRandamAttributeList = tableInfo.getRandamAttributeList();
					HashMap<String, Long> hdfsUniqueValueNumMap = tableInfo.getUniqueValueNumMap();
					long tupleNum = tableInfo.getTupleNum();
					
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							attributeAlias = attributeList.get(i);
							attribute = hdfsAttributeList.get(i);
							
							uniqueValue = hdfsUniqueValueNumMap.get(attribute);
							result += "uniqueValues(" + attributeAlias + ", " + uniqueValue +")";
							
							if(i != attributeList.size() - 1)
							{
								result += ", ";
							}
						}
					}
					
					result += "], [";
					
					if(hdfsRandamAttributeList != null && hdfsRandamAttributeList.size() > 0)
					{
						for(int i = 0; i < hdfsRandamAttributeList.size(); i++)
						{
							String randomAttributeName = hdfsRandamAttributeList.get(i);
							int indexForAttribute = hdfsAttributeList.indexOf(randomAttributeName);
							result += "uniqueValuesPerTupleBundle(";
							result += attributeList.get(indexForAttribute);
							result += ", ";
							long monteCarloIteration = Long.parseLong(catalog.getMonteCarloIterations());
							result += min(hdfsUniqueValueNumMap.get(hdfsAttributeList.get(indexForAttribute)), monteCarloIteration);
							result += ")";
							
							if(i != hdfsRandamAttributeList.size() - 1)
							{
								result += ",";
							}
						}
					}
					
					result += "], \r\n\t\t[";
					
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							attributeAlias = attributeList.get(i);
							attribute = hdfsAttributeList.get(i);
							Attribute viewAttribute = realAttributeList.get(i);
							
							attributeSize = viewAttribute.getAttributeSize();
							result += "attributeSize(" + attributeAlias + ", " + attributeSize +")";
							
							if(i != attributeList.size() - 1)
							{
								result += ", ";
							}
						}
					}
					
					result += "], " + tupleNum + ", _).\r\n";
					
					/*
					 * random Attribute;
					 */
					result += "randomAttrsRelation(";
					result += this.getNodeName() + ", [";
					for(int i = 0; i < hdfsRandamAttributeList.size(); i++)
					{
						String randomAttributeName = hdfsRandamAttributeList.get(i);
						int indexForAttribute = hdfsAttributeList.indexOf(randomAttributeName);
						result += attributeList.get(indexForAttribute);
						
						if(i != hdfsRandamAttributeList.size() - 1)
						{
							result += ",";
						}
					}
					
					result += "]).\r\n";
					
					relationStatistics.setTableInfo(tableInfo);
				}
			}
		}
		
		return result;
	}
	
	public long min(long a, long b)
	{
		if(a < b)
			return a;
		else
			return b;
	}
	
	public ArrayList<String> getGeneratedNameList()
	{
		ArrayList<String> resultList = new ArrayList<String>();
		
		if(attributeList != null)
		{
			for(int i = 0; i < attributeList.size(); i++)
			{
				String attributeName = attributeList.get(i);
				resultList.add(attributeName);
			}
		}
		return resultList;
	}


	public RelationStatistics getRelationStatistics() {
		return relationStatistics;
	}


	public void setRelationStatistics(RelationStatistics relationStatistics) {
		this.relationStatistics = relationStatistics;
	}


	public Operator copy(CopyHelper copyHelper) throws Exception
	{
		if(copyHelper.getCopiedMap().containsKey(getNodeName()))
		{
			return copyHelper.getCopiedMap().get(getNodeName());
		}
		
		CommonContent commonContent = copyHelper.copyBasicOperator(this);
		
		String c_tableName = new String(this.tableName);
		ArrayList<String> c_attributeList = copyHelper.copyStringList(attributeList);
		
		RelationStatistics c_relationStatistics = this.relationStatistics;
		
		String c_indexString;
		if(this.indexString == null)
		{
			c_indexString = null;
		}
		else
		{
			c_indexString = new String(indexString);
		}
		
		TableScan tablescan = new TableScan(commonContent.nodeName,
				commonContent.children, 
				commonContent.parents,
				c_tableName, 
				c_attributeList,
				c_relationStatistics,
				c_indexString,
				this.type,
				this.indexMathExp);
		
		tablescan.setNameMap(commonContent.nameMap);
		tablescan.setMapSpaceNameSet(commonContent.mapSpaceNameSet);
		
		copyHelper.getCopiedMap().put(getNodeName(), tablescan);
		
		ArrayList<Operator> children = tablescan.getChildren();
		if(children != null)
		{
			for(int i = 0; i < children.size(); i++)
			{
				children.get(i).addParent(tablescan);
			}
		}
		return tablescan;
	}
	
	public String getGeneralTableIndexExp()
	{
		if(indexMathExp != null)
		{
			return "generalTableindex";
		}
		else
		{
			return null;
		}
	}
	
	


	public MathExpression getIndexMathExp() {
		return indexMathExp;
	}


	public void setIndexMathExp(MathExpression indexMathExp) {
		this.indexMathExp = indexMathExp;
	}


	/**
	 * @return the tableInfo
	 */
	public PreviousTable getTableInfo() {
		return tableInfo;
	}


	/**
	 * @param tableInfo the tableInfo to set
	 */
	public void setTableInfo(PreviousTable tableInfo) {
		this.tableInfo = tableInfo;
	}
	
}