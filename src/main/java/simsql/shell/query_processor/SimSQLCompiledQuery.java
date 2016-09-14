

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


package simsql.shell.query_processor;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;

import simsql.compiler.Projection;
import simsql.compiler.FrameOutput;
import simsql.compiler.Operator;
import simsql.compiler.timetable.TimeTableNode;
import simsql.shell.CompiledQuery;

public class SimSQLCompiledQuery implements CompiledQuery {
	// for MCMC query
    public ArrayList<Operator> sinkList;
	public ArrayList<Operator> queryList;
    public ArrayList<String> sqlList;
    public HashMap<Operator, String> definitionMap;
    public LinkedList<TimeTableNode> requiredTables;

    // for MCDB2 query
    private String fName = null;
    private ArrayList<String> tableNameList;
    private HashMap<String, ArrayList<String>> tableAttributeNameListMap;
    // this is the error message (if there)
    private String errMsg = null;

    // tells if there is nothing to process
    private boolean isEmp = false;

    /**
     * @param sinkList
     * @param sqlList
     * @param definitionMap
     */
    public SimSQLCompiledQuery(ArrayList<Operator> sinkList,
					           ArrayList<String> sqlList, 
					           ArrayList<Operator> queryList,
					           HashMap<Operator, String> definitionMap,
                               LinkedList<TimeTableNode> requiredTables) {
        super();
        
        this.sinkList = sinkList;
        this.sqlList = sqlList;
        this.queryList = queryList;
        this.definitionMap = definitionMap;
        this.requiredTables = requiredTables;
	//for mcmc queries on the tree
        this.tableNameList = new ArrayList<String>();
        this.tableAttributeNameListMap = new HashMap<String, ArrayList<String>>();
    }

    // this constructor creates a new (empty) file with the given extension
    public SimSQLCompiledQuery(String extension) {
    	tableNameList = new ArrayList<String>();
    	tableAttributeNameListMap = new HashMap<String, ArrayList<String>>();
    	
		if(!extension.endsWith("err"))
        {
        	// loop through all of the file names until we sucessfully create one
	        try {
	            for (int i = 0; true; i++) {
	                fName = "Temp_file_" + i  + "_" + extension;
	                if (new File(fName).createNewFile()) {
	                    break;
	                }
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	            throw new RuntimeException(
	                    "Prolem occurred when I was trying to create a temp file");
	        }
        }
        else
        {
        	if (!new File("err.txt").exists())
    		{
        		try {
        			new File("err.txt").createNewFile();
        		}catch (Exception e) {
    	            e.printStackTrace();
    	            throw new RuntimeException(
    	                    "Prolem occurred when I was trying to create a temp file");
    	        }
    		}
        }
    }
    
 // this gets the name of the file
    public String getFName() {
        return fName;
    }


    // record the fact that there is nothig to process in the file
    public void setEmpty() {
        isEmp = true;
    }

    // see if the thing is empty and there is nothing to process
    public boolean isEmpty() {
        return isEmp;
    }

    // this should be called to record an error
    public void setError(String setToMe) {
        errMsg = setToMe;
    }

    // this is called to get the error if it is there
    public String isError() {
        return errMsg;
    }

    /**
     * @return the sinkList
     */
    public ArrayList<Operator> getSinkList() {
        return sinkList;
    }

    /**
     * @param sinkList
     *            the sinkList to set
     */
    public void setSinkList(ArrayList<Operator> sinkList) {
        this.sinkList = sinkList;
    }
    
    /**
     * @return the sqlList
     */
    public ArrayList<String> getSqlList() {
        return sqlList;
    }

    /**
     * @param sqlList
     *            the sqlList to set
     */
    public void setSqlList(ArrayList<String> sqlList) {
        this.sqlList = sqlList;
    }

    /**
     * @return the definitionMap
     */
    public HashMap<Operator, String> getDefinitionMap() {
        return definitionMap;
    }

    /**
     * @param definitionMap
     *            the definitionMap to set
     */
    public void setDefinitionMap(HashMap<Operator, String> definitionMap) {
        this.definitionMap = definitionMap;
    }

    public void cleanUp() {
        if (sinkList != null) {
            sinkList.clear();
        }

        if (sqlList != null) {
            sqlList.clear();
        }

        if (definitionMap != null) {
            definitionMap.clear();
        }

        if(fName != null)
        {
            new File (fName).delete ();
        }
    }
    
	public void addMaterilizedView(FrameOutput frameOuput) {
		ArrayList<String> tableNameList = frameOuput.getTableList();
		ArrayList<Operator> children = frameOuput.getChildren();

		for (int i = 0; i < tableNameList.size(); i++) {
			Operator operator = children.get(i);

			if (operator instanceof Projection) {
				addMaterializedTable(tableNameList.get(i),
						((Projection) operator).getProjectedNameList());
			} else {
				throw new RuntimeException(
						"We have a non-projection operator under FrameOutput");
			}
		}
	}

	private void addMaterializedTable(String tableName,
			ArrayList<String> attributeNameList) {
		if (tableNameList == null || tableAttributeNameListMap == null) {
			throw new RuntimeException("Could not add materilized table "
					+ tableName);
		}

		tableNameList.add(tableName);
		tableAttributeNameListMap.put(tableName, attributeNameList);
	}

  public HashMap<String, ArrayList<String>> getMaterializedViews() {
    return tableAttributeNameListMap;
  }

}
