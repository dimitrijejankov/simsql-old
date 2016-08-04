

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
package simsql.compiler; // package mcdb.compiler.parser.expression.sqlType;



/**
 * @author Bamboo
 *
 */
public class DropElement extends SQLExpression{
	
	public static final int TABLEORCOMMON_RANDOM_TABLE = 0;
	public static final int CONSTANT_INDEX_TABLE = 1;
	public static final int GENERAL_INDEX_TABLE = 2;
	public static final int ARRAY_CONSTANT_INDEX_TABLE = 3;
	public static final int VIEW = 4;
	public static final int UNION_VIEW = 5;
	public static final int VGFUNC = 6;
	public static final int FUNC = 7;
	
	public String objectName;
	public int type;
	public boolean isRandom;
	
	public DropElement(String objectName, int objectType) {
		super();
		this.objectName = objectName.toLowerCase();
		this.type = objectType;
		this.isRandom = false;
	}
	
	
	public String getObjectName() {
		return objectName;
	}




	public void setObjectName(String objectName) {
		this.objectName = objectName;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean me) {
		isRandom = me;
	}

	public int getType() {
		return type;
	}




	public void setType(int type) {
		this.type = type;
	}


	/* (non-Javadoc)
	 * @see mcdb.compiler.parser.expression.sqlExpression.SQLExpression#acceptVisitor(mcdb.compiler.parser.astVisitor.ASTVisitor)
	 */
	@Override
	public boolean acceptVisitor(ASTVisitor astVisitor)throws Exception {
		return astVisitor.visitDropElement(this);
	}


	/**
	 * 
	 */
	public void drop() throws Exception{
		Relation relation;
		View view;
		VGFunction vg;
		
		
		switch(type)
		{
			case DropElement.TABLEORCOMMON_RANDOM_TABLE:
				relation = SimsqlCompiler.catalog.getRelation(objectName);
				if(relation != null)
				{
					SimsqlCompiler.catalog.dropRelation(objectName);
				}
				else
				{
					SimsqlCompiler.catalog.dropView(objectName);
				}
				break;
				
			case DropElement.CONSTANT_INDEX_TABLE:
			case DropElement.GENERAL_INDEX_TABLE:
				SimsqlCompiler.catalog.dropView(objectName);
				SimsqlCompiler.catalog.dropIndexTable(objectName);
				SimsqlCompiler.catalog.deleteMCDependecy(objectName);
				break;
				
			case DropElement.VIEW:
				view = SimsqlCompiler.catalog.getView(objectName);
				
				if(view != null)
				{
					SimsqlCompiler.catalog.dropView(objectName);
				}
				break;
				
			case DropElement.UNION_VIEW:
				view = SimsqlCompiler.catalog.getView(objectName);
				if(view != null)
				{
					SimsqlCompiler.catalog.dropView(objectName);
					SimsqlCompiler.catalog.dropIndexTable(objectName);
					SimsqlCompiler.catalog.deleteMCDependecy(objectName);
				}
				break;
				
				
			case DropElement.VGFUNC:
				SimsqlCompiler.catalog.dropVGFunction(objectName);
				break;
				
			case DropElement.FUNC:
				SimsqlCompiler.catalog.dropFunction(objectName);
				break;
				
			case DropElement.ARRAY_CONSTANT_INDEX_TABLE:
				int start = objectName.lastIndexOf("_");
				if(start < 0)
				{
					throw new RuntimeException("There is exception in droping the array index table");
				}
				
				String suffix = objectName.substring(start+1, objectName.length());
				String realTableName = objectName.substring(0, start);
				
				if(suffix != null)
				{
					start = suffix.indexOf("..");
					String lowerBound = suffix.substring(0, start);
					String upBound = suffix.substring(start+2, suffix.length());
					
					try
					{
						int low = Integer.parseInt(lowerBound);
						int up = Integer.parseInt(upBound);
						
						for(int i = low; i <= up; i++)
						{
							//drop the the real constant random table iteratively.
							SimsqlCompiler.catalog.dropView(realTableName + "_" + i);
							SimsqlCompiler.catalog.dropIndexTable(realTableName + "_" + i);
							SimsqlCompiler.catalog.deleteMCDependecy(realTableName + "_" + i);
						}
					}
					catch(Exception e)
					{
						throw new RuntimeException("There is exception in droping the array index table");
					}
				}
				
				break;
		}
		
	}

}
