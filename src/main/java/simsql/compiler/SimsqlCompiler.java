

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
package simsql.compiler; // package mcdb.compiler;

import java.io.BufferedWriter;
import java.util.LinkedList;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;

import simsql.compiler.timetable.TimeTableNode;
import simsql.shell.Compiler;
import simsql.shell.query_processor.SimSQLCompiledQuery;
import simsql.shell.PhysicalDatabase;
import simsql.shell.RuntimeParameter;

/**
 * @author Bamboo
 *
 */
public class SimsqlCompiler implements Compiler<SimSQLCompiledQuery> 

{
	
	public static Catalog catalog = new Catalog();

	private PhysicalDatabase physicalDB;
	private RuntimeParameter runtimeParameters;
	private TranslatorHelper translatorHelper;
     

	public SimsqlCompiler (PhysicalDatabase physicalDBIn, 
						   RuntimeParameter runtimeParametersIn) {
	    physicalDB = physicalDBIn; 
	    runtimeParameters = runtimeParametersIn;
	}
	
	
	public Catalog getCatalog () {
	    return catalog;
	}
	
	public TranslatorHelper getTranslatorHelper()
	{
		return translatorHelper;
	}
	
	public SimSQLCompiledQuery parseFile(String fileName)
	{
		String query = FileOperation.getFile_info (fileName);
		return parseString(query);
	}

	public SimSQLCompiledQuery parseString(String parseMe)
	{
		ArrayList<Operator> sinkList = new ArrayList<Operator>();
		ArrayList<String> sqlList = new ArrayList<String>();
		SimSQLCompiledQuery tempQueryFile = null;
        
		try {
			ValuesTableHelper.cleanValuesTable();
			
			String query = parseMe;
	    	        query = SQLTextHelper.toLowerCase(query);

	    	/*
	    	 * 1. Parse the input text
	    	 */
	    	ArrayList<Expression> expressionList = CompilerProcessor.parse(query);
	    	CompilerProcessor.simplify(expressionList);
	    	
	    	boolean empty = true;
	    	String errMessage = null;
	    	
	    	/*
	         * 3. Do unnesting
	         */
	        Unnester unnester;
	        
	        query = SQLTextHelper.removeComments(query);
	        
	        StringTokenizer token = new StringTokenizer(query, ";");

		    this.translatorHelper = new TranslatorHelper();
	        Translator translator = new Translator (translatorHelper);
	        
	        for(int i = 0; i < expressionList.size(); i++)
	        {
	        	String sql = (String)token.nextElement();
	        	//System.out.println(sql);
	        	Expression expression = expressionList.get(i);
	        	
	        	TypeChecker tempChecker = CompilerProcessor.typeCheck(expression, sql);
	        	
	        	if(tempChecker == null)
	        	{
	        		errMessage = "Nothing came back from the type checker!";
	        		break;
	        	}
	        	else if(tempChecker instanceof RandomTableTypeChecker)
	        	{
	            	unnester = new RandomTableUnnester((RandomTableTypeChecker)tempChecker);
	        	}
	        	else
	        	{
	        		unnester = new Unnester(tempChecker);
	        	}
	        	
	        	/*
	        	 * 4. Translate the unnestedElement
	        	 */
		        if(expression instanceof SelectStatement)
		        {
		        	UnnestedSelectStatement result = unnester.unnestSelectStatement((SelectStatement)expression);
		        	//System.out.println(result);
		        	Operator element = translator.translate(result);
		        	
		        	/*
		        	 * 5. PostProcessing
		        	 */
		        	sinkList.add(element);
		        	sqlList.add(sql);
		        	
		        	empty = false;
		        }
		        else if(expression instanceof BaseLineArrayRandomTableStatement)
		        {
		        	String baseLineArrayQueryElmentHyphenate = ((BaselineArrayRandomTypeChecker)tempChecker).getInitializedQueryList();
		        	ArrayList<Expression> baseLineArrayQueryElmentHypList = CompilerProcessor.parse(baseLineArrayQueryElmentHyphenate);
		        	Expression baseLineArrayElement;
		        	
		        	if(baseLineArrayQueryElmentHypList != null)
		        	{
			        	CompilerProcessor.simplify(baseLineArrayQueryElmentHypList);
			        	
			        	for(int j = 0; j < baseLineArrayQueryElmentHypList.size(); j++)
			        	{
			        		baseLineArrayElement = baseLineArrayQueryElmentHypList.get(j);
			        		((BaseLineRandomTableStatement) baseLineArrayElement).setSqlString(sql);
			        		BaseLineRandomTableTypeChecker baselineElmentChecker = new BaseLineRandomTableTypeChecker(false);
			        		baselineElmentChecker.setSaved(false);
			        		boolean subcheck = baselineElmentChecker.visitBaseLineRandomTableStatement((BaseLineRandomTableStatement)baseLineArrayElement);
							if(!subcheck)
							{
								throw new RuntimeException("BaseLineArrayRandomTableStatement typecher wrong");
							}
			        	}
		        	}
		        }
		        else if(expression instanceof BaseLineRandomTableStatement)
		        {
		        	//nothing to do
		        }
		        else if(expression instanceof GeneralRandomTableStatement)
		        {
		        	//nothing to do
		        }
		        else if(expression instanceof RandomTableStatement)
		        {
		        	//nothing to do
		        }
		        else if(expression instanceof ViewStatement)
		        {
		        	//nothing to do
		        }
		        else if(expression instanceof MaterializedViewStatement)
		        {
		        	UnnestedSelectStatement result = unnester.unnestSelectStatement(((MaterializedViewStatement) expression).statement);
		        	//System.out.println(result);
		        	Operator element = translator.translate(result);
		        	MaterializedViewTypeChecker checker = (MaterializedViewTypeChecker)tempChecker;
		        	
		        	/*
		    		 * 5. Combine the sinkOperatorlist together by frameOutput.
		    		 *
		    		 */
		    		String nodeName = "frameOutput";
		    		ArrayList<Operator> parents = new ArrayList<Operator>();
		    		ArrayList<Operator> children = new ArrayList<Operator>();
		    		children.add(element);
		    		ArrayList<String> tableNameList = new ArrayList<String>();
		    		tableNameList.add(checker.getDefinedSchema().getViewName());
		    		Operator frameOutput = new FrameOutput(nodeName, children, parents, tableNameList);
		    		
		    		element.addParent(frameOutput);
		    		sinkList.add(frameOutput);
		    		sqlList.add(sql);
		        	
		        	empty = false;
		        }
		        else if(expression instanceof UnionViewStatement)
		        {
		        	//nothing to do
		        }
		        else if(expression instanceof TableDefinitionStatement)
		        {
		        	// save the table to the catalog
		            TableDefinitionStatement myStatement = (TableDefinitionStatement) expression;
		            myStatement.save ();
		             
		            // and also save a physical version of the table
		            physicalDB.registerTable (myStatement.getTableName ());
		            
		        }
		        else if(expression instanceof VGFunctionDefinitionStatement)
		        {
		        	// and load up the corresponding VG function into the runtime parameters
		            VGFunctionDefinitionStatement myStatement = (VGFunctionDefinitionStatement) expression;
		            // save it to the catalog
		           
		            String myPath = myStatement.getPath ();
		            if (myPath.charAt (0) == '\'') {
		              myPath = myPath.substring (1, myPath.length () - 1);
		            }
		  			
		            File myFile = new File (myPath);
		            if (!myFile.isFile()) {
		              System.err.println ("Error!  I could not find the file " + myStatement.getPath ());
		              errMessage = "Error!  I could not find the file " + myStatement.getPath ();
		        		
		              continue;
		            }
		            
		            myStatement.save ();
		            
		            runtimeParameters.readVGFunctionFromFile (myStatement.getName (), myFile);

		        }
		        else if(expression instanceof FunctionDefinitionStatement)
		        {
		        	// and load up the corresponding function into the runtime parameters 
		            FunctionDefinitionStatement myStatement = (FunctionDefinitionStatement) expression;
		            // save it to the catalog
		           
		            String myPath = myStatement.getPath ();
		            if (myPath.charAt (0) == '\'') {
		              myPath = myPath.substring (1, myPath.length () - 1);
		            }
		  					File myFile = new File (myPath);
		            if (!myFile.isFile ()) {
		              System.err.println ("Error!  I could not find the file " + myStatement.getPath ());
		              errMessage = "Error!  I could not find the file " + myStatement.getPath ();
		        		
		              continue;
		            } 
		            
		            myStatement.save ();
		        	
		            runtimeParameters.readFunctionFromFile (myStatement.getName (), myFile);		            
		        }
		        else if(expression instanceof DropElement)
		        {
		        	DropElement drop = (DropElement)expression;
	
					// do the dropping on the catalog.
					drop.drop();
					// is it a table?
					if (drop.getType() == DropElement.TABLEORCOMMON_RANDOM_TABLE && !drop.isRandom()) {
						
						// if so, kill it on the phys database
						physicalDB.deleteTable(drop.getObjectName());
					}
		        }
		        else
		        {
		        	
		        	errMessage = "I saw an expression type I don't know how to handle";
	        		break;
		        }
	        }
	        
	        // handle different kinds of errors.
	        if(errMessage != null)
	        {
	        	tempQueryFile = new SimSQLCompiledQuery (".err");
	        	tempQueryFile.setError (errMessage);
	        	return tempQueryFile;
	        }
	        else if(empty)
	        {
	        	tempQueryFile = new SimSQLCompiledQuery (".pl");
	        	tempQueryFile.setEmpty();
	        	return tempQueryFile;
	        }
	        
	        ArrayList<TableScan> referencedIndexTableList = PlanHelper.findReferencedRandomTable(sinkList);
	        if(referencedIndexTableList.size() == 0) //mcdb query
	        {
		        tempQueryFile = new SimSQLCompiledQuery (".pl");
		        BufferedWriter wri = new BufferedWriter (new FileWriter (tempQueryFile.getFName ()));
		    	wri.write(":- dynamic compExp/5.");
		        wri.newLine();
		        for(int i = 0; i < sinkList.size(); i++)
		    	{
		    		Operator element = sinkList.get(i);
		    		wri.write("parent(planRoot, " + element.getNodeName() + ").\r\n");
		    		wri.newLine();
		    	}
		        
		        PostProcessor processor = new PostProcessor(sinkList, translatorHelper);
		    	processor.renaming();
		        
		        
				wri.write(PlanHelper.BFS(sinkList));
				wri.newLine();
	
				/*
				 * 6. Add the statistics
				 */
				PlanStatistics statistics = new PlanStatistics(sinkList);
				wri.write(statistics.getStatistics());
	
				wri.write(statistics.getAttributeTypeStatistics());
				wri.write("attributeType(isPres, bottom).\r\n");
				wri.close();
	
				for (int i = 0; i < sinkList.size(); i++){
					Operator element = sinkList.get(i);
					if (element instanceof FrameOutput) {
						tempQueryFile.addMaterilizedView((FrameOutput) element);
					}
				}
	        }
	        else //mcmc query.
	        {
	        	tempQueryFile = executeBayesianModel(referencedIndexTableList, sinkList);
			for (int i = 0; i < sinkList.size(); i++){
				Operator element = sinkList.get(i);
				if (element instanceof FrameOutput) {
					tempQueryFile.addMaterilizedView((FrameOutput) element);
				}
			}
	        }
			
			return tempQueryFile;
		}
		catch(Exception e)
		{
			e.printStackTrace ();
			tempQueryFile = new SimSQLCompiledQuery (".pl");
        	tempQueryFile.setError (e + "; while parsing");
        	return tempQueryFile;
		}
	}
	
	
	
	
	/*
	 * querySinkList is the query on the top of the MCMC DAG. 
	 */
	public SimSQLCompiledQuery executeBayesianModel(ArrayList<TableScan> indexedTableList,
													ArrayList<Operator> querySinkList)
	{
		ArrayList<String> tableReferenceList = PlanHelper.findModelTableList(indexedTableList);
        LinkedList<TimeTableNode> requiredTables = PlanHelper.getRequiredTables(indexedTableList);
		
		try {
			String query = "";
			View view;
			String tableName, sql;
			
			for(int i = 0; i < tableReferenceList.size(); i++)
			{
				tableName = tableReferenceList.get(i);
				view = catalog.getView(tableName);
				sql = view.getSql();
				query += sql + ";\r\n";
			}
			
			ArrayList<Operator> sinkList = new ArrayList<Operator>();
			ArrayList<String> sqlList = new ArrayList<String>();
			HashMap<Operator, String> definitionMap = new HashMap<Operator, String>();
			
			ValuesTableHelper.cleanValuesTable();
			
			query = SQLTextHelper.toLowerCase(query);
	    	/*
	    	 * 1. Parse the input text
	    	 */
	    	ArrayList<Expression> expressionList = CompilerProcessor.parse(query);
	    	CompilerProcessor.simplify(expressionList);
	    	
	    	boolean empty = true;
	    	String errMessage = null;
	    	
	    	/*
	         * 3. Do unnesting
	         */
	        Unnester unnester;
	        
	        query = SQLTextHelper.removeComments(query);
	        
	        StringTokenizer token = new StringTokenizer(query, ";");

	        TranslatorHelper tHelper = new TranslatorHelper();
	        Translator translator = new Translator (tHelper);

			for (Expression anExpressionList : expressionList) {
				sql = (String) token.nextElement();
				Expression expression = anExpressionList;

				TypeChecker tempChecker = CompilerProcessor.typeCheck(expression, sql, false);

				if (tempChecker == null) {
					errMessage = "Nothing came back from the type checker!";
					break;
				} else if (tempChecker instanceof RandomTableTypeChecker) {
					unnester = new RandomTableUnnester((RandomTableTypeChecker) tempChecker);
				} else {
					unnester = new Unnester(tempChecker);
				}

	        	/*
	        	 * 4. Translate the unnestedElement
	        	 */
				if (expression instanceof BaseLineArrayRandomTableStatement) {
					String baseLineArrayQueryElmentHyphenate = ((BaselineArrayRandomTypeChecker) tempChecker).getInitializedQueryList();
					ArrayList<String> baseLineQueryList = ((BaselineArrayRandomTypeChecker) tempChecker).getQueryList();
					ArrayList<Expression> baseLineArrayQueryElmentHypList = CompilerProcessor.parse(baseLineArrayQueryElmentHyphenate);
					Expression baseLineArrayElement;

					if (baseLineArrayQueryElmentHypList != null) {
						CompilerProcessor.simplify(baseLineArrayQueryElmentHypList);

						for (int j = 0; j < baseLineArrayQueryElmentHypList.size(); j++) {
							baseLineArrayElement = baseLineArrayQueryElmentHypList.get(j);
							((BaseLineRandomTableStatement) baseLineArrayElement).setSqlString(sql);
							BaseLineRandomTableTypeChecker baselineElmentChecker = new BaseLineRandomTableTypeChecker(false);
							baselineElmentChecker.setSaved(false);
							boolean subcheck = baselineElmentChecker.visitBaseLineRandomTableStatement((BaseLineRandomTableStatement) baseLineArrayElement);
							if (!subcheck) {
								throw new RuntimeException("BaseLineArrayRandomTableStatement typecher wrong");
							}
							unnester = new RandomTableUnnester((RandomTableTypeChecker) baselineElmentChecker);
							UnnestedRandomTableStatement result = ((RandomTableUnnester) unnester).unnestRandomTableStatement((RandomTableStatement) baseLineArrayElement);
							//System.out.println(result);
							Operator element = translator.translate(result);
							//System.out.println(Process.toString(element));
				        	
				        	/*
				        	 * 5. PostProcessing
				        	 */
							sinkList.add(element);
							sqlList.add(baseLineQueryList.get(j));

							DefinedTableSchema definedTableSchema = ((BaseLineRandomTableStatement) baseLineArrayElement).definedTableSchema;
							String viewName = definedTableSchema.getViewName();

							definitionMap.put(element, viewName);
						}
					}

					empty = false;
				} else if (expression instanceof BaseLineRandomTableStatement) {
					UnnestedRandomTableStatement result = ((RandomTableUnnester) unnester).unnestRandomTableStatement((RandomTableStatement) expression);
					//System.out.println(result);
					Operator element = translator.translate(result);
					//System.out.println(Process.toString(element));
		        	
		        	/*
		        	 * 5. PostProcessing
		        	 */
					sinkList.add(element);
					sqlList.add(sql);

					DefinedTableSchema definedTableSchema = ((BaseLineRandomTableStatement) expression).definedTableSchema;
					String viewName = definedTableSchema.getViewName();

					definitionMap.put(element, viewName);
					empty = false;
				} else if (expression instanceof MultidimensionalTableStatement) {
					UnnestedRandomTableStatement result = ((RandomTableUnnester) unnester).unnestRandomTableStatement((RandomTableStatement) expression);
					//System.out.println(result);
					Operator element = translator.translate(result);
					//System.out.println(Process.toString(element));

		        	/*
		        	 * 5. PostProcessing
		        	 */
					sinkList.add(element);
					sqlList.add(sql);
					DefinedTableSchema definedTableSchema = ((MultidimensionalTableStatement) expression).definedTableSchema;
					String viewName = definedTableSchema.getViewName();

					definitionMap.put(element, viewName);
				} else if (expression instanceof GeneralRandomTableStatement) {
					UnnestedRandomTableStatement result = ((RandomTableUnnester) unnester).unnestRandomTableStatement((RandomTableStatement) expression);
					//System.out.println(result);
					Operator element = translator.translate(result);
					//System.out.println(Process.toString(element));
		        	
		        	/*
		        	 * 5. PostProcessing
		        	 */
					sinkList.add(element);
					sqlList.add(sql);
					DefinedTableSchema definedTableSchema = ((GeneralRandomTableStatement) expression).definedTableSchema;
					String viewName = definedTableSchema.getViewName();

					definitionMap.put(element, viewName);
					empty = false;
				} else if (expression instanceof UnionViewStatement) {
					if (expression instanceof BaselineUnionViewStatement ||
							expression instanceof GeneralUnionViewStatement) {
						Operator element = translator.translateUnionViewStatement((UnionViewStatement) expression);
			        	
			        	/*
			        	 * 5. PostProcessing
			        	 */
						sinkList.add(element);
						sqlList.add(sql);

						DefinedTableSchema definedTableSchema = ((UnionViewStatement) expression).getSchema();
						String viewName = definedTableSchema.getViewName();

						definitionMap.put(element, viewName);
						empty = false;
					}
				}
			}
	        
	        // handle different kinds of errors.
	        if(errMessage != null)
	        {
	        	SimSQLCompiledQuery tempQueryFile = new SimSQLCompiledQuery (".err");
	        	tempQueryFile.setError (errMessage);
	        	return tempQueryFile;
	        }
	        
	        SimSQLCompiledQuery mcmcCompiledOutput = new SimSQLCompiledQuery(sinkList, sqlList, querySinkList, definitionMap, requiredTables);
	        return mcmcCompiledOutput;
		}
		catch(Exception e)
		{
			e.printStackTrace ();
			SimSQLCompiledQuery tempQueryFile = new SimSQLCompiledQuery (".pl");
        	tempQueryFile.setError (e + "; while parsing");
        	return tempQueryFile;
		}
	}
}
