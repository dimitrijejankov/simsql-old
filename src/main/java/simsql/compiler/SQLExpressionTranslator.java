

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
package simsql.compiler; // package mcdb.compiler.logicPlan.translator;


import java.util.ArrayList;
import java.util.HashMap;


import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;
import simsql.runtime.Hash;


// import mcdb.catalog.Attribute;
// import mcdb.catalog.Relation;
// import mcdb.catalog.View;
// import mcdb.compiler.CompilerProcessor;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.EFunction;
// import mcdb.compiler.logicPlan.logicOperator.mathOperator.MathOperator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Projection;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.ScalarFunction;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.TableScan;
// import mcdb.compiler.logicPlan.logicOperator.statisticsOperator.RelationStatistics;
// import mcdb.compiler.parser.astVisitor.RandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.expression.Expression;
// import mcdb.compiler.parser.expression.sqlExpression.DefinedTableSchema;
// import mcdb.compiler.parser.expression.sqlExpression.TableReference;

// import mcdb.compiler.parser.expression.sqlType.*;
// import mcdb.compiler.parser.grammar.QueryLexer;
// import mcdb.compiler.parser.grammar.QueryParser;
// import mcdb.compiler.parser.unnester.RandomTableUnnester;
// import mcdb.compiler.parser.unnester.UnnestedRandomTableStatement;
// import mcdb.compiler.parser.unnester.UnnestedSelectStatement;
// import mcdb.compiler.parser.unnester.Unnester;

/**
 * @author Bamboo
 *
 */
public class SQLExpressionTranslator {
	
	public Translator translator;
	public TranslatorHelper translatorHelper;
	
	public SQLExpressionTranslator(TranslatorHelper translatorHelper, Translator translator)
	{
		this.translator = translator;
		this.translatorHelper = translatorHelper;
	}
	
	public TableScan tableScan(Relation relation, String tableAlias)
	{
		TableScan tableScan;
		
		/*
		 * 1. The data structure of tableScan.
		 */
		int nodeIndex = translatorHelper.getNodeIndex();
		String nodeName = "node"+nodeIndex;
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		String tableName = relation.getName();
		ArrayList<String> attributeList = new ArrayList<String>();
		
		/*
		 * 2. Fill the translatedStatement
		 */
		ArrayList<Attribute> tempList = relation.getAttributes();
		String attributeString;
		for(int i = 0; i < tempList.size(); i++)
		{
			Attribute attribute = tempList.get(i);
			String attributeName = attribute.getName();
			attributeString = tableAlias + "_" + attributeName;
			/*
			 * 3. Fill the attributes of the TableScan 
			 */
			attributeList.add(attributeString);
		}
		
		/*
		 * 
		 */
		ArrayList<String> attributeList2 = new ArrayList<String>();
		ArrayList<Attribute> attributeList3 = relation.getAttributes();
		for(int i = 0; i < attributeList3.size(); i++)
		{
			attributeList2.add(attributeList3.get(i).getName());
		}
		
		
		String directory = relation.getFileName();
		RelationStatistics relationStatistics = new RelationStatistics(tableName, 
																	   directory, 
																	   attributeList2,
																	   TableReference.COMMON_TABLE);
		/*
		 * 4. Create the TableScan node.
		 */
		
		tableScan = new TableScan(nodeName,
									children,
									parents,
									tableName,
									attributeList,
									relationStatistics,
									TableReference.COMMON_TABLE,
									null);
		
		/*
		 * 5. Since the TableScan is the leaf node, so there is not children.
		 *    And there are no children who need to be assigned a parent.
		 */
		return tableScan;										 
	}
	
	
	public Operator viewScan(View view, String alias)throws Exception
	{
		HashMap<String, Operator> viewPlanMap = translatorHelper.viewPlanMap;
		String viewName = view.getName();
		
		ArrayList<String> outputAttributeList = null;
		
		String sql = view.getSql();
		Expression expression = null;
		TypeChecker tempChecker = null;
		DefinedTableSchema definedTableSchema = null;
        
		try
		{
			ANTLRStringStream input = new ANTLRStringStream(sql);
			QueryLexer lexer = new QueryLexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        QueryParser parser = new QueryParser(tokens);
	        ArrayList<Expression> expressionList = parser.prog();
	        CompilerProcessor.simplify(expressionList);
	        
	        expression = expressionList.get(0);
	        
	        if(expression instanceof ViewStatement)
	        {
	        	 tempChecker = CompilerProcessor.typeCheck(expression, sql, false);  
	        }
	        /*
	         * ----------------------for simulation------------------------
	         */
	        else if(expression instanceof BaseLineRandomTableStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
            else if(expression instanceof MultidimensionalTableStatement)
            {
                tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
            }
	        else if(expression instanceof GeneralRandomTableStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
	        else if(expression instanceof RandomTableStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
	       
	        if(expression instanceof ViewStatement && tempChecker != null)
	        {
	        	definedTableSchema = ((ViewStatement)expression).definedTableSchema;	
	        }
	        else if(expression instanceof BaseLineRandomTableStatement && tempChecker != null)
	        {
	        	definedTableSchema = ((BaseLineRandomTableStatement)expression).definedTableSchema;
	        }
            else if(expression instanceof MultidimensionalTableStatement && tempChecker != null)
            {
                definedTableSchema = ((MultidimensionalTableStatement)expression).definedTableSchema;
            }
	        else if(expression instanceof GeneralRandomTableStatement && tempChecker != null)
		    {
	        	definedTableSchema = ((GeneralRandomTableStatement)expression).definedTableSchema;
	        }
		    else if(expression instanceof RandomTableStatement && tempChecker != null)
	        {
	        	definedTableSchema = ((RandomTableStatement)expression).definedTableSchema;
	        }
	        
	        /*
        	 * Record the output attribute Name list.
        	 * And then we need to add the holder.
        	 */
	        if(definedTableSchema != null)
	        {
	        	ArrayList<String> tableAttributeList = definedTableSchema.tableAttributeList;
	    		ArrayList<String> attributeList = tempChecker.getAttributeList();
	    		
	    		if(tableAttributeList != null && tableAttributeList.size() != 0)
	    		{
	    			outputAttributeList = tableAttributeList;
	    		}
	    		else
	    		{
	    			outputAttributeList = attributeList;
	    		}
	        }
		}
		catch(Exception e)
		{
			System.err.println("CompilerProcessoring view/randomTable error!");
			return null;
		}
		
		//if the view plan is in the memory
		Operator resultElement = null;
		
		if(viewPlanMap.containsKey(viewName))
		{
			resultElement =  viewPlanMap.get(viewName);
		}
		//the view plan is not in the memory, then we should get that from database.
		else
		{
			Operator translatedElement = null;
			
			if(expression instanceof ViewStatement && tempChecker != null)
	        {
	        	Unnester tempUnester = new Unnester(tempChecker);
		        
	        	UnnestedSelectStatement nestedStatement = tempUnester.unnestViewStatement((ViewStatement)expression);
	        	translatedElement = translator.translate(nestedStatement);
	        }
	        else if(expression instanceof RandomTableStatement && tempChecker != null)
	        {
	        	RandomTableUnnester tempUnester = new RandomTableUnnester((RandomTableTypeChecker)tempChecker);
	        	UnnestedRandomTableStatement nestedStatement = 
	        		tempUnester.unnestRandomTableStatement((RandomTableStatement)expression);
	        	translatedElement = translator.translate(nestedStatement);
	        }
			
			/*
        	 * apply scalar function if possible
        	 */
			if(definedTableSchema != null && 
					translatedElement != null)
			{
				translatedElement = addSchema(definedTableSchema, 
			               tempChecker,
			               translatedElement);
	
				viewPlanMap.put(viewName, translatedElement);
				resultElement =  translatedElement;
			}
		}
		
		if(resultElement == null)
		{
			System.err.println("CompilerProcessoring view/randomTable error!");
			return null;
		}
		
		/*
		 * apply scalar function again according to the alias, add the holder (alias).
		 * Here holder means the table: A.B (A is the holder)
		 * 
		 */
		if(outputAttributeList != null)
			resultElement = addHolder(outputAttributeList,
									  resultElement,
									  alias);
		else
			resultElement = null;
		
		
		return resultElement;
	}
	
	public Operator addHolder(ArrayList<String> attributeNameList,
									 Operator originalElement,
									 String alias)
	{
		/*
		 * Scalar function followed by projection
		 */
		if(attributeNameList != null &&
				attributeNameList.size() != 0)
    	{
			/*
			 * 1. Scalar function on the new defined attribute due to the definition of the schema.
			 */
			
			/*
			 * The data structure in the ScalarFunction node.
			 */
    		String nodeName = "node" + translatorHelper.getNodeIndex();
    		ArrayList<Operator> children = new ArrayList<Operator>();
    		ArrayList<Operator> parents = new ArrayList<Operator>();
    		ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
    		HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
    		HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
    		
    		/*
    		 * 1.1. Fill the translatedStatement in the ScalarFunction.
    		 */
    		for(int i = 0; i < attributeNameList.size(); i++)
    		{
    			/*
    			 * 1.2. Fill in the scalarFunction with the concrete MathFunction
    			 *    Here it should be EFunction.
    			 */
    			scalarExpressionList.add(new EFunction());
    		}
    		
    		/*
    		 * It comes to the attribute set of each function.
    		 * Since one scalar function can have multiple functions, with each function can have
    		 * multiple involved attributes. However, since the scalar function only plays the role
    		 * of renaming, each scalar function has only one attribute.
    		 */
    		
    		ArrayList<String> tempList;
    		for(int i = 0; i < attributeNameList.size(); i++)
    		{
    			/*
    			 * 1.3. Fill each functions in the ScalarFunction with involved attributes.
    			 */
    			tempList = new ArrayList<String>();
    			tempList.add(attributeNameList.get(i));
    			columnListMap.put(scalarExpressionList.get(i), tempList);
    		}
    		
    		for(int i = 0; i < attributeNameList.size(); i++)
    		{
    			/*
    			 * 1.4. Fill each functions in the ScalarFunction with an output
    			 */
    			outputMap.put(scalarExpressionList.get(i), alias + "_" + attributeNameList.get(i));
    		}
    		
    		/*
    		 * 1.5. Fill in the children
    		 */
    		children.add(originalElement);
    		
    		/*
    		 * 1.6 Create the current scalar function node.
    		 */
    		ScalarFunction scalarFunction = new ScalarFunction(nodeName, children, parents, translatorHelper);
    		scalarFunction.setScalarExpressionList(scalarExpressionList);
    		scalarFunction.setColumnListMap(columnListMap);
    		scalarFunction.setOutputMap(outputMap);
    		
    		/*
    		 * 1.7 This translatedElement add current Node as parent
    		 */
    		originalElement.addParent(scalarFunction);
    		
    		
    		/*
    		 * 2. Projection on the result attribute
    		 */
    		/*
    		 * 2.1 Create the data structure of the Projection
    		 */
    		Projection projection;
    		nodeName = "node" + translatorHelper.getNodeIndex();
    		children = new ArrayList<Operator>();
    		parents = new ArrayList<Operator>();
    		ArrayList<String> projectedNameList = new ArrayList<String>();
    		
    		/*
    		 * 2.2 Fill the tranlsatedResult.
    		 */
    		for(int i = 0; i < attributeNameList.size(); i++)
    		{
    			/*
    			 * 2.3 Fill the projectedNameList
    			 */
    			projectedNameList.add(alias + "_" + attributeNameList.get(i));
    		}
    		
    		/*
    		 * 2.4 Fill the children
    		 */
    		children.add(scalarFunction);
    		
    		/*
    		 * 2.5 Create the current projection node.
    		 */
    		projection = new Projection(nodeName, children, parents, projectedNameList);
    		/*
    		 * 2.6 "sclarFunction" fills it parents with the projection.
    		 */
    		scalarFunction.addParent(projection);
    		return projection;
    	}
		else
		{
			return originalElement;
		}
	}
	
	public Operator addHolderWithoutProjection(ArrayList<String> attributeNameList,
													  Operator originalElement,
													  String alias) {
		/*
		 * Scalar function followed by projection
		 */
		if (attributeNameList != null && attributeNameList.size() != 0) {
			/*
			 * 1. Scalar function on the new defined attribute due to the
			 * definition of the schema.
			 */

			/*
			 * The data structure in the ScalarFunction node.
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
			ArrayList<Operator> parents = new ArrayList<Operator>();
			ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
			HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
			HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();

			/*
			 * 1.1. Fill the translatedStatement in the ScalarFunction.
			 */
			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 1.2. Fill in the scalarFunction with the concrete
				 * MathFunction Here it should be EFunction.
				 */
				scalarExpressionList.add(new EFunction());
			}

			/*
			 * It comes to the attribute set of each function. Since one scalar
			 * function can have multiple functions, with each function can have
			 * multiple involved attributes. However, since the scalar function
			 * only plays the role of renaming, each scalar function has only
			 * one attribute.
			 */

			ArrayList<String> tempList;
			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 1.3. Fill each functions in the ScalarFunction with involved
				 * attributes.
				 */
				tempList = new ArrayList<String>();
				tempList.add(attributeNameList.get(i));
				columnListMap.put(scalarExpressionList.get(i), tempList);
			}

			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 1.4. Fill each functions in the ScalarFunction with an output
				 */
				outputMap.put(scalarExpressionList.get(i), alias + "_"
						+ attributeNameList.get(i));
			}

			/*
			 * 1.5. Fill in the children
			 */
			children.add(originalElement);

			/*
			 * 1.6 Create the current scalar function node.
			 */
			ScalarFunction scalarFunction = new ScalarFunction(nodeName,
					children, parents, translatorHelper);
			scalarFunction.setScalarExpressionList(scalarExpressionList);
			scalarFunction.setColumnListMap(columnListMap);
			scalarFunction.setOutputMap(outputMap);

			/*
			 * 1.7 This translatedElement add current Node as parent
			 */
			originalElement.addParent(scalarFunction);

			
			return scalarFunction;
		} 
		else {
			return originalElement;
		}
	}
	
	
	public Operator addSeedHolder(ArrayList<String> attributeNameList,
										 Operator originalElement, 
										 String alias)
	{
		/*
		 * Scalar function on seed, to change that to "holder.seed".
		 */
		if (attributeNameList != null && attributeNameList.size() != 0) {
			
			ScalarFunction scalarFunction;
			
			/*
			 * 1. Create the data structure of the ScalarFunction.
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
			ArrayList<Operator> parents = new ArrayList<Operator>();
			ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
			HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
			HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
			
			/*
			 * 2. Fill the translatedStatement.
			 */
			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 3. Fill the scalarExpressionList
				 */
				scalarExpressionList.add(new EFunction());
			}
			
			ArrayList<String> columnList;
			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 4. Fill the columnListMap.
				 */
				columnList = new ArrayList<String>();
				columnList.add(attributeNameList.get(i));
				columnListMap.put(scalarExpressionList.get(i), columnList);
			}
			for (int i = 0; i < attributeNameList.size(); i++) {
				/*
				 * 5. Fill the outputMap.
				 */
				outputMap.put(scalarExpressionList.get(i), alias + "_" + attributeNameList.get(i));
			}
			
			/*
			 * 6. Fill the children with the original element.
			 */
			children.add(originalElement);

			scalarFunction = new ScalarFunction(nodeName,
												children,
												parents, 
												translatorHelper);
			scalarFunction.setScalarExpressionList(scalarExpressionList);
			scalarFunction.setColumnListMap(columnListMap);
			scalarFunction.setOutputMap(outputMap);
			
			/*
			 * 7. Fill the parent for the originalElement.
			 */
			originalElement.addParent(scalarFunction);

			return scalarFunction;
		} 
		else {
			System.err.println("CompilerProcessoring view/randomTable error!");
			return null;
		}
	}
	
	public Operator addSchema(DefinedTableSchema definedTableSchema, 
								               TypeChecker tempChecker,
								               Operator originalElement)
	{
		/*
		 * Scalar function followed by projection
		 */
		if(definedTableSchema.tableAttributeList != null &&
    			definedTableSchema.tableAttributeList.size() != 0)
    	{
			/*
			 * 1. Scalar function on the new defined attribute due to the definition of the schema
			 */
			
			/*
			 * 1.1 The data structure in the ScalarFunction node.
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
    		ArrayList<Operator> parents = new ArrayList<Operator>();
    		ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
    		HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
    		HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
    		
    		/*
    		 * 1.2. Fill the translatedStatement in the ScalarFunction.
    		 */
    		
    		ArrayList<String> tableAttributeList = definedTableSchema.tableAttributeList;
    		ArrayList<String> attributeList = tempChecker.getAttributeList();
    		
    		for(int i = 0; i < attributeList.size(); i++)
    		{
    			/*
    			 * 1.2. Fill in the scalarFunction with the concrete MathFunction
    			 *    Here it should be EFunction.
    			 */
    			scalarExpressionList.add(new EFunction());
    		}
    		
    		ArrayList<String> tempList;
    		for(int i = 0; i < attributeList.size(); i++)
    		{
    			/*
    			 * 1.3. Fill each functions in the ScalarFunction with involved attributes.
    			 */
    			tempList = new ArrayList<String>();
    			tempList.add(attributeList.get(i));
    			columnListMap.put(scalarExpressionList.get(i), tempList);
    		}
    		for(int i = 0; i < tableAttributeList.size(); i++)
    		{
    			/*
    			 * 1.4. Fill each functions in the ScalarFunction with an output
    			 */
    			outputMap.put(scalarExpressionList.get(i), tableAttributeList.get(i));
    		}
    		
    		/*
			 * 1.5. Fill the children with the original element.
			 */
			children.add(originalElement);
    		
			/*
    		 * 1.6 Create the current scalar function node.
    		 */
    		ScalarFunction scalarFunction = new ScalarFunction(nodeName, children, parents, translatorHelper);
    		scalarFunction.setScalarExpressionList(scalarExpressionList);
    		scalarFunction.setColumnListMap(columnListMap);
    		scalarFunction.setOutputMap(outputMap);
    		
    		/*
    		 * 1.7 This translatedElement add current Node as parent
    		 */
    		originalElement.addParent(scalarFunction);
    		
    		/*
    		 * 2. Projection on the result attribute
    		 */
    		/*
    		 * 2.1 Create the data structure of the Projection
    		 */
    		Projection projection;
    		nodeName = "node" + translatorHelper.getNodeIndex();
    		children = new ArrayList<Operator>();
    		parents = new ArrayList<Operator>();
    		ArrayList<String> projectedNameList = new ArrayList<String>();
    		
    		/*
    		 * 2.2 Fill the tranlsatedResult.
    		 */
    		
    		for(int i = 0; i < tableAttributeList.size(); i++)
    		{
    			/*
    			 * 2.3 Fill the projectedNameList
    			 */
    			projectedNameList.add(tableAttributeList.get(i));
    		}
    		
    		/*
    		 * 2.4 Fill the children
    		 */
    		children.add(scalarFunction);
    		
    		/*
    		 * 2.5 Create the current projection node.
    		 */
    		projection = new Projection(nodeName, children, parents, projectedNameList);
    		/*
    		 * 2.6 "sclarFunction" fills it parents with the projection.
    		 */
    		scalarFunction.addParent(projection);
    		return projection;
    	}
		else
		{
			return originalElement;
		}
    	
	}


	/**
	 * @param view
	 * @param tableAlias
	 * @return
	 */
	public Operator indexTableScan(View view, String tableAlias, TableReference tableReference)
	{
		HashMap<String, Operator> viewPlanMap = translatorHelper.viewPlanMap;
		String viewName = view.getName();
		HashMap<String, Integer> indexStrings = tableReference.getIndexStrings();
		int type = tableReference.getTableInferenceType();
		
		ArrayList<String> outputAttributeList = null;
		
		String sql = view.getSql();
		Expression expression = null;
		TypeChecker tempChecker = null;
		DefinedTableSchema definedTableSchema = null;
        
		try
		{
			ANTLRStringStream input = new ANTLRStringStream(sql);
			QueryLexer lexer = new QueryLexer(input);
	        CommonTokenStream tokens = new CommonTokenStream(lexer);
	        QueryParser parser = new QueryParser(tokens);
	        ArrayList<Expression> expressionList = parser.prog();
	        CompilerProcessor.simplify(expressionList);
	        
	        expression = expressionList.get(0);
	        
	        if(expression instanceof BaseLineRandomTableStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
			else if(expression instanceof MultidimensionalTableStatement)
			{
				tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
			}
	        else if(expression instanceof GeneralRandomTableStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
	        else if(expression instanceof BaselineUnionViewStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
	        else if(expression instanceof GeneralUnionViewStatement)
	        {
	        	tempChecker = CompilerProcessor.typeCheck(expression, sql, false);
	        }
	        
	        if(expression instanceof BaseLineRandomTableStatement && tempChecker != null)
	        {
	        	definedTableSchema = ((BaseLineRandomTableStatement)expression).definedTableSchema;
	        }
			else if(expression instanceof MultidimensionalTableStatement && tempChecker != null)
			{
				definedTableSchema = ((MultidimensionalTableStatement)expression).definedTableSchema;
			}
	        else if(expression instanceof GeneralRandomTableStatement && tempChecker != null)
		    {
	        	definedTableSchema = ((GeneralRandomTableStatement)expression).definedTableSchema;
	        }
	        else if(expression instanceof BaselineUnionViewStatement && tempChecker != null){
	        	definedTableSchema = ((BaselineUnionViewStatement)expression).getSchema();
	        }
	        else if(expression instanceof GeneralUnionViewStatement && tempChecker != null){
	        	definedTableSchema = ((GeneralUnionViewStatement)expression).getSchema();
	        }
	        
	        /*
        	 * Record the output attribute Name list.
        	 * And then we need to add the holder.
        	 */
	        if(definedTableSchema != null)
	        {
	        	ArrayList<String> tableAttributeList = definedTableSchema.tableAttributeList;
	    		ArrayList<String> attributeList = tempChecker.getAttributeList();
	    		
	    		if(tableAttributeList != null && tableAttributeList.size() != 0)
	    		{
	    			outputAttributeList = tableAttributeList;
	    		}
	    		else
	    		{
	    			outputAttributeList = attributeList;
	    		}
	        }
		}
		catch(Exception e)
		{
			System.err.println("CompilerProcessoring IndexTable error!" + sql);
			return null;
		}
		
		TableScan tableScan;
		
		/*
		 * 1. The data structure of tableScan.
		 */
		int nodeIndex = translatorHelper.getNodeIndex();
		String nodeName = "node"+nodeIndex;
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<String> attributeList = new ArrayList<String>();
		
		/*
		 * 2. Fill the translatedStatement
		 */
		String attributeString;
		for (String attributeName : outputAttributeList) {
			attributeString = attributeName;
			/*
			 * 3. Fill the attributes of the TableScan 
			 */
			attributeList.add(attributeString);
		}
		
		/*
		 * 
		 */
		ArrayList<String> attributeList2 = new ArrayList<String>();
		for (String anOutputAttributeList : outputAttributeList) {
			attributeList2.add(anOutputAttributeList);
		}
		
		
		String directory = "_";
		RelationStatistics relationStatistics;
		/*
		 * 4. Create the TableScan node.
		 */
		if(!viewName.equals(tableReference.getTable()))
		{
			relationStatistics = new RelationStatistics(tableReference.getTable(),
					   directory, 
					   attributeList2,
					   indexStrings,
					   type
					   );
			tableScan = new TableScan(nodeName,
					children,
					parents,
					tableReference.getTable(),
					attributeList,
					relationStatistics,
					indexStrings,
					tableReference.getTableInferenceType(),
					tableReference.getExpressions());
		}
		else
		{
			relationStatistics = new RelationStatistics(viewName,
					   directory, 
					   attributeList2,
					   indexStrings,
					   type
					   );
			tableScan = new TableScan(nodeName,
									children,
									parents,
									viewName,
									attributeList,
									relationStatistics,
									indexStrings,
									tableReference.getTableInferenceType(),
									tableReference.getExpressions());
		}
		
		/*
		 * 5. Since the TableScan is the leaf node, so there is not children.
		 *    And there are no children who need to be assigned a parent.
		 */
		Operator resultOperator = tableScan;
		
		if(outputAttributeList != null)
			resultOperator = addHolder(outputAttributeList,
									   resultOperator,
									   tableAlias);
		
		return resultOperator;	
	}
}
