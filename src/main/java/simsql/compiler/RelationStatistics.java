

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
package simsql.compiler; // package mcdb.compiler.logicPlan.logicOperator.statisticsOperator;

import java.util.ArrayList;
import java.util.HashMap;
import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class RelationStatistics implements StatisticsOperator
{
	private String relation;
	private String directory;
	private ArrayList<String> attributeList;
	
	private String indexString;
	private int type;
	
	private PreviousTable tableInfo;

	public RelationStatistics(String relation, 
			String directory,
			ArrayList<String> attributeList,
			int type) {
		super();
		this.relation = relation;
		this.directory = directory;
		this.attributeList = attributeList;
		this.indexString = null;
		this.tableInfo = null;
		this.type = type;
	}
	
	public RelationStatistics(String relation, 
			String directory,
			ArrayList<String> attributeList,
			String indexString,
			int type) {
		super();
		this.relation = relation;
		this.directory = directory;
		this.attributeList = attributeList;
		this.indexString = indexString;
		this.tableInfo = null;
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see logicOperator.statisticsOperator.StatisticsOperator#visitNode()
	 */
	@Override
	public String visitNode() throws Exception{
		String result = "";
		
		if(!ValuesTableHelper.getValuesTableMap().containsKey(relation))
		{
			if(indexString == null && type == TableReference.COMMON_TABLE)//
			{
				result += "relation('";
				result += relation;
				result += "', ";
				result += directory;
				result += ", [";
				
				if(attributeList != null)
				{
					for(int i = 0; i < attributeList.size(); i++)
					{
						result += attributeList.get(i);
						
						if(i != attributeList.size()-1)
						{
							result += ", ";
						}
					}
				}
				
				result += "]).\r\n";
				
				ArrayList<Attribute> attributeList = SimsqlCompiler.catalog.getRelation(relation).getAttributes();
				for(int i = 0; i < attributeList.size(); i++)
				{
					Attribute temp = attributeList.get(i);
					String attributeName = temp.getName();
					DataType attributeType = temp.getType();
					AttributeType attributeTypeNode = null;
					attributeTypeNode = new AttributeType(attributeName, attributeType);
					
					// switch(attributeType)
					// {
					// 	case Attribute.INTEGER:
					// 		attributeTypeNode = new AttributeType(attributeName, "integer");
					// 		break;
							
					// 	case Attribute.DOUBLE:
					// 		attributeTypeNode = new AttributeType(attributeName, "double");
					// 		break;
							
					// 	case Attribute.STRING:
					// 		attributeTypeNode = new AttributeType(attributeName, "string");
					// 		break;
							
					// 	case Attribute.DATE:
					// 		attributeTypeNode = new AttributeType(attributeName, "date");
					// 		break;
							
					// 	case Attribute.STOCHINT:
					// 		attributeTypeNode = new AttributeType(attributeName, "integer");
					// 		break;
							
					// 	case Attribute.STOCHDBL:
					// 		attributeTypeNode = new AttributeType(attributeName, "double");
					// 		break;
							
					// 	case Attribute.STOCHDAT:
					// 		attributeTypeNode = new AttributeType(attributeName, "date");
					// 		break;
							
					// 	case Attribute.SEED:
					// 		attributeTypeNode = new AttributeType(attributeName, "seed");
					// 		break;	
					// }
					 
					if(attributeTypeNode != null)
					{
						result += attributeTypeNode.visitNode();
					}
				}
			}
			else
			{
				if(tableInfo == null)
				{
					result += "relation('";
					result += relation;
					result += "', ";
					result += directory;
					result += ", [";
					
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							result += attributeList.get(i);
							
							if(i != attributeList.size()-1)
							{
								result += ", ";
							}
						}
					}
					
					result += "]).\r\n";
					
					ArrayList<Attribute> attributeList = SimsqlCompiler.catalog.getView(relation).getAttributes();
					for(int i = 0; i < attributeList.size(); i++)
					{
						Attribute temp = attributeList.get(i);
						String attributeName = temp.getName();
						DataType attributeType = temp.getType();
						AttributeType attributeTypeNode = null;
						attributeTypeNode = new AttributeType(attributeName, attributeType);
						
						// switch(attributeType)
						// {
						// 	case Attribute.INTEGER:
						// 		attributeTypeNode = new AttributeType(attributeName, "integer");
						// 		break;
								
						// 	case Attribute.DOUBLE:
						// 		attributeTypeNode = new AttributeType(attributeName, "double");
						// 		break;
								
						// 	case Attribute.STRING:
						// 		attributeTypeNode = new AttributeType(attributeName, "string");
						// 		break;
								
						// 	case Attribute.DATE:
						// 		attributeTypeNode = new AttributeType(attributeName, "date");
						// 		break;
								
						// 	case Attribute.STOCHINT:
						// 		attributeTypeNode = new AttributeType(attributeName, "integer");
						// 		break;
								
						// 	case Attribute.STOCHDBL:
						// 		attributeTypeNode = new AttributeType(attributeName, "double");
						// 		break;
								
						// 	case Attribute.STOCHDAT:
						// 		attributeTypeNode = new AttributeType(attributeName, "date");
						// 		break;
								
						// 	case Attribute.SEED:
						// 		attributeTypeNode = new AttributeType(attributeName, "seed");
						// 		break;	
						// }
						 
						if(attributeTypeNode != null)
						{
							result += attributeTypeNode.visitNode();
						}
					}
				}
				else
				{
					result += "relation('";
					result += relation;
					result += "', '";
					//result += "file(\\\'" + TempScanHelper.fileSystem + tableInfo.getFileDirectory() + "\\\')"; //for file system
					result += "hdfs(\\\'" + tableInfo.getFileDirectory() + "\\\')";// for hdfs
					result += "', [";
					
					this.attributeList = tableInfo.getAttributeList();
					if(attributeList != null)
					{
						for(int i = 0; i < attributeList.size(); i++)
						{
							result += attributeList.get(i);
							
							if(i != attributeList.size()-1)
							{
								result += ", ";
							}
						}
					}
					
					result += "]).\r\n";
					
					HashMap<String, String> attributeTypeMap = tableInfo.getAttributeMap();
					
					for(int i = 0; i < attributeList.size(); i++)
					{
						String attributeName = attributeList.get(i);
						String attributeType = attributeTypeMap.get(attributeName);
						
						AttributeType attributeTypeNode = new AttributeType(attributeName, attributeType);
						 
						if(attributeTypeNode != null)
						{
							result += attributeTypeNode.visitNode();
						}
					}
				}
			}
		}
		else
		{
			ValuesTable valuesTable = ValuesTableHelper.getValuesTableMap().get(relation);
			result += "temporaryTable('";
			result += relation + "', [";
			
			if(attributeList != null)
			{
				for(int i = 0; i < attributeList.size(); i++)
				{
					result += attributeList.get(i);
					
					if(i != attributeList.size()-1)
					{
						result += ", ";
					}
				}
			}
			
			result += "], [";
			ArrayList<ArrayList<MathExpression>> tempTableRowList = valuesTable.getTempTableColumnList();
			for(int i = 0; i < tempTableRowList.size(); i++)
			{
				result += "[";
				ArrayList<MathExpression> row = tempTableRowList.get(i);
				for(int j = 0; j < row.size(); j++)
				{
					MathExpression expression = row.get(j);
					
					if(expression instanceof DateExpression)
					{
						result += "(Date)" + ((DateExpression)(expression)).toString();
					}
					else if(expression instanceof NumericExpression)
					{
						result += ((NumericExpression)(expression)).toString();
					}
					else if(expression instanceof StringExpression)
					{
						result += "'" + ((StringExpression)(expression)).toString() + "'";
					}
					
					if(j != row.size() - 1)
					{
						result += ", ";
					}
				}
				result += "]";
				if(i != tempTableRowList.size() - 1)
				{
					result += ", ";
				}
			}
			result += "]).\r\n";
			
			ArrayList<Attribute> attributeList = SimsqlCompiler.catalog.getRelation(relation).getAttributes();
			for(int i = 0; i < attributeList.size(); i++)
			{
				Attribute temp = attributeList.get(i);
				String attributeName = temp.getName();
				DataType attributeType = temp.getType();
				AttributeType attributeTypeNode = null;
				attributeTypeNode = new AttributeType(attributeName, attributeType);
				
				// switch(attributeType)
				// {
				// 	case Attribute.INTEGER:
				// 		attributeTypeNode = new AttributeType(attributeName, "integer");
				// 		break;
						
				// 	case Attribute.DOUBLE:
				// 		attributeTypeNode = new AttributeType(attributeName, "double");
				// 		break;
						
				// 	case Attribute.STRING:
				// 		attributeTypeNode = new AttributeType(attributeName, "string");
				// 		break;
						
				// 	case Attribute.DATE:
				// 		attributeTypeNode = new AttributeType(attributeName, "date");
				// 		break;
						
				// 	case Attribute.STOCHINT:
				// 		attributeTypeNode = new AttributeType(attributeName, "integer");
				// 		break;
						
				// 	case Attribute.STOCHDBL:
				// 		attributeTypeNode = new AttributeType(attributeName, "double");
				// 		break;
						
				// 	case Attribute.STOCHDAT:
				// 		attributeTypeNode = new AttributeType(attributeName, "date");
				// 		break;
						
				// 	case Attribute.SEED:
				// 		attributeTypeNode = new AttributeType(attributeName, "seed");
				// 		break;	
				// }
				 
				if(attributeTypeNode != null)
				{
					result += attributeTypeNode.visitNode();
				}
			}
		}
		
		
		return result;
	}

	public String getRelation() {
		return relation;
	}

	public void setRelation(String relation) {
		this.relation = relation;
	}

	public String getDirectory() {
		return directory;
	}

	public void setDirectory(String directory) {
		this.directory = directory;
	}

	public ArrayList<String> getAttributeList() {
		return attributeList;
	}

	public void setAttributeList(ArrayList<String> attributeList) {
		this.attributeList = attributeList;
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