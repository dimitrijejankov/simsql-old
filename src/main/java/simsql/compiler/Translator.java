

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



// NOTE: THIS IS THE NEW TRANSLATOR, FROM THE SIMULATION
// I REPLACED IT WITH THE OLD ONE TO GET THINGS TO COMPILE

/**
 * 
 */
package simsql.compiler; // package mcdb.compiler.logicPlan.translator;



import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import simsql.runtime.DataType;

/**
 * @author Bamboo
 *
 */
public class Translator {
	public static final int JOIN = 0;
	public static final int SEMIJOIN = 1;
	public static final int ANTIJOIN = 2;
	
	public TranslatorHelper translatorHelper;
	public SQLExpressionTranslator sqlExpressionTranslator;
	public MathExpressionTranslator mathExpressionTranslator;
	public BooleanPredicateTranslator predicateTranslator;
	public ColumnExtractor columnExtractor;
	
	public Translator(TranslatorHelper translatorHelper)
	{
		this.translatorHelper = translatorHelper;
		this.sqlExpressionTranslator = new SQLExpressionTranslator(translatorHelper, this);
		this.mathExpressionTranslator = new MathExpressionTranslator(translatorHelper);
		this.predicateTranslator = new BooleanPredicateTranslator(translatorHelper);
		this.columnExtractor = new ColumnExtractor(translatorHelper);
	}
	
	public Operator translate(UnnestedSelectStatement unnestedStatement)throws Exception
	{
		String viewName = unnestedStatement.getViewName();
		SelectStatement selectStatement = unnestedStatement.getSelectStatement();
		TypeChecker typechecker = unnestedStatement.getTypechecker();
		/*
		 * 1. some data structure for the ancestor
		 */
		ArrayList<SQLExpression> addedTableReferenceList = unnestedStatement.getAddedTableReference();
		//ArrayList<SQLExpression> duplicatedRemovalList = nestedStatement.getDuplicatedRemovalList();
		ArrayList<ColumnExpression> addedFromAncesterSelectList = unnestedStatement.getFromParentSelectList();
		BooleanPredicate wherePredicate = unnestedStatement.getWherePredicate();
		ArrayList<AllFromTable> addGroupbyColumnList = unnestedStatement.getAddGroupbyColumnList();
		
		/*
		 * 2. some data structure for the ancestor children 
		 */
		ArrayList<UnnestedSelectStatementJoin> childrenView = unnestedStatement.getChildrenView();
		
		
		ArrayList<String> fromList = typechecker.getFromList();
		
		Operator translatedElement;
		/*
		 * 1. Deal with the join result in the result query
		 * 1.1 Dealing with fromList: create n isolated nodes for each From element. 
		 */
		Operator translatorFromElement;
		translatorFromElement = translateFromClause(typechecker, fromList);
		
		/*
		 * 1.2 Deal with the join with the parent
		 */
		//first we check if we need duplicate removal from ancestors.
		boolean needDuplicateRemove = aggregateInSelect(selectStatement);		
		Operator ancesterFromElement;
		ancesterFromElement = translateAncester(addedTableReferenceList, needDuplicateRemove);
		
		/*
		 * 1.3. Join the ancesterFromElement and translatorFromElement
		 */
		if(translatorFromElement != null && ancesterFromElement != null)
		{
			translatedElement = join(translatorFromElement, ancesterFromElement);
		}
		else if(translatorFromElement == null && ancesterFromElement != null)
		{
			translatedElement = ancesterFromElement;
		}
		else if(translatorFromElement != null && ancesterFromElement == null)
		{
			translatedElement = translatorFromElement;
		}
		else
		{
			translatedElement = null;
			System.err.println("The from clause and the referenced table from its ancester can not be" +
					" empty at the same time!");
			throw new Exception("The from clause and the referenced table from its ancester can not be" +
					" empty at the same time!");
		}
		
		/*
		 * 1.4. Deal with the child
		 */
		translatedElement = translateChildView(childrenView, translatedElement); 
		
		
		
		/*
		 * 2. Deal with the where clause
		 *    whereExpressionList: the math expression in the aggregate function
		 *    No subquery expression in where clause
		 */
		
		
		/*
		 * 2.2 Deal with the where clause
		 */
		translatedElement = translateBooleanPredicateClause(wherePredicate, translatedElement);
		
		/*
		 * 3. Aggregate function
		 * 	
		 */
		translatedElement = translateAggregateFunction(typechecker, unnestedStatement, addGroupbyColumnList, null, translatedElement);
		
		/*
		 * 4. scalar function
		 */
		translatedElement = translateScalarFunction(typechecker, selectStatement, translatedElement);
		
		/*
		 * 5. projection
		 */
		translatedElement = translateProjection(typechecker, 
												unnestedStatement, 
												addedFromAncesterSelectList, 
												translatedElement);		
		Projection planProjection = (Projection)translatedElement;
		/*
		 * 6. having clause
		 */
		BooleanPredicate havingClause = selectStatement.havingClause;
		if(havingClause != null)
			translatedElement = translateBooleanPredicateClause(havingClause, translatedElement);
		
		/*
		 * 7. distinct
		 */
		if(selectStatement.setQuantifier == FinalVariable.DISTINCT)
		{
			translatedElement = duplicateRemoval(translatedElement);
			translatedElement = addFinalProjection(translatedElement, planProjection);
		}
		
		/*
		 * 8. rename the result attribute
		 */
		Catalog catalog = SimsqlCompiler.catalog;
		
		View view = catalog.getView(viewName);
		if(view != null)
		{
			// to do.
		}
		
		return translatedElement;
	}
	
	/*
	 * Currently the union view only supports union the table in different time ticks, to make the system simpler.
	 */
	public Operator translateUnionViewStatement(UnionViewStatement expression) throws Exception
	{
		Catalog catalog = SimsqlCompiler.catalog;
		DefinedTableSchema schema = expression.getSchema();
		ArrayList<SQLExpression> tableNameList = expression.getTableNameList();
		
		/*
		 * 1. translate the elements of the union view.
		 */
		View view;
		Relation relation;
		String prefix, viewName, indexName;
		String firstViewName = null;
		int lowerIndex, upIndex;
		MathExpression indexExp, lowerExp, upExp;
		TableReference tempReference;
		
		ArrayList<Operator> tableElementList = new ArrayList<Operator>();
		
		ArrayList<DataType> inputAttributeTypeList = new ArrayList<DataType>();
		ArrayList<String> inputAttributeNameList =  new ArrayList<String>();
		
		ArrayList<String> childProjectedNameList;
		
		//THe following three list is used to record the element List.
		ArrayList<String> constantTableList = new ArrayList<String> ();
		ArrayList<MathOperator> individualGeneralIndexList = new ArrayList<MathOperator>();
		ArrayList<MathExpression> individualGeneralIndexExpressionList = new ArrayList<MathExpression>();
		ArrayList<MathOperator> generalTableBoundList = new ArrayList<MathOperator>();
		ArrayList<MathExpression>  generalTableBoundExpressionList = new ArrayList<MathExpression>();
		
		String elementName = null;
		
		Operator translatorElement;
		ArrayList<MathOperator> translatedMathOperatorList;
		
		for(int i = 0; i < tableNameList.size(); i++)
		{
			SQLExpression element = tableNameList.get(i);
			
			if(element instanceof BaselineTableName)
			{
				viewName = ((BaselineTableName) element).getVersionedName();
				
				if(firstViewName == null)
				{
					firstViewName = viewName;
					elementName =  ((BaselineTableName) element).getName();
				}
				
				view = catalog.getView(viewName);
				indexName = ((BaselineTableName) element).getIndex();
				tempReference = new TableReference(viewName,
						viewName,
						indexName,
						TableReference.CONSTANT_INDEX_TABLE);
				translatorElement = sqlExpressionTranslator.indexTableScan(view, viewName, tempReference);
				tableElementList.add(translatorElement);
				constantTableList.add(viewName);
				
				if(translatorElement instanceof Projection)
				{
					childProjectedNameList = ((Projection) translatorElement).getProjectedNameList();
					inputAttributeNameList.addAll(childProjectedNameList);
				}
				else
				{
					throw new RuntimeException("The children of UnionView operator should be Projection");
				}
			}
			else if(element instanceof BaselineTableNameArray)
			{
				prefix = ((BaselineTableNameArray) element).getName();
				lowerIndex =  ((BaselineTableNameArray) element).getLowerBoundValue();
				upIndex =  ((BaselineTableNameArray) element).getUpBoundValue();
				
				for(int j = lowerIndex; j <= upIndex; j++)
				{
					viewName = prefix + "_" + j;

					if(firstViewName == null)
					{
						firstViewName = viewName;
						elementName =  ((BaselineTableNameArray) element).getName();
					}
					
					view = catalog.getView(viewName);
					indexName = j + "";
					tempReference = new TableReference(viewName,
							viewName,
							indexName,
							TableReference.CONSTANT_INDEX_TABLE);
					translatorElement = sqlExpressionTranslator.indexTableScan(view, viewName, tempReference);
					
					tableElementList.add(translatorElement);
					constantTableList.add(viewName);
					
					if(translatorElement instanceof Projection)
					{
						childProjectedNameList = ((Projection) translatorElement).getProjectedNameList();
						inputAttributeNameList.addAll(childProjectedNameList);
					}
					else
					{
						throw new RuntimeException("The children of UnionView operator should be Projection");
					}
				}
			}
			else if(element instanceof CommonTableName)
			{
				viewName = ((CommonTableName) element).getName();

				if(firstViewName == null)
				{
					firstViewName = viewName;
					elementName = viewName;
				}
				
				tempReference = new TableReference(viewName);
				
				relation = catalog.getRelation(viewName);
				if(relation != null)
				{
					translatorElement = sqlExpressionTranslator.tableScan(relation, viewName);
					tableElementList.add(translatorElement);
					constantTableList.add(viewName);
				}
				else
				{
					view = catalog.getView(viewName);
					translatorElement = sqlExpressionTranslator.viewScan(view, viewName);
					tableElementList.add(translatorElement);
					constantTableList.add(viewName);
				}
				
				if(translatorElement instanceof Projection)
				{
					childProjectedNameList = ((Projection) translatorElement).getProjectedNameList();
					inputAttributeNameList.addAll(childProjectedNameList);
				}
				else
				{
					throw new RuntimeException("The children of UnionView operator should be Projection");
				}
			}
			else if(element instanceof GeneralTableName)
			{
				viewName = ((GeneralTableName) element).getName() + "_i";

				if(firstViewName == null)
				{
					firstViewName = viewName;
					elementName =  ((GeneralTableName) element).getName();
				}
				
				String catalogViewName = ((GeneralTableName) element).getName();
				view = catalog.getView(catalogViewName + "_i");
				indexExp = ((GeneralTableName) element).getIndexExp();
				tempReference = new TableReference(viewName,
						viewName,
						null,
						TableReference.GENERAL_INDEX_TABLE,
						indexExp);
				translatorElement = sqlExpressionTranslator.indexTableScan(view, viewName, tempReference);
				tableElementList.add(translatorElement);
				
				translatedMathOperatorList = new ArrayList<MathOperator>();
				mathExpressionTranslator.translateMathExpression(indexExp, 
																new ArrayList<String>(), 
																translatedMathOperatorList);
					
					
				individualGeneralIndexList.add(translatedMathOperatorList.get(0));
				individualGeneralIndexExpressionList.add(indexExp);
				
				if(translatorElement instanceof Projection)
				{
					childProjectedNameList = ((Projection) translatorElement).getProjectedNameList();
					inputAttributeNameList.addAll(childProjectedNameList);
				}
				else
				{
					throw new RuntimeException("The children of UnionView operator should be Projection");
				}
			}
			else if(element instanceof GeneralTableNameArray)
			{
				//Here we need more to handle the range based GeneralTableNameArray.
				prefix = ((GeneralTableNameArray) element).getName();
				lowerExp =  ((GeneralTableNameArray) element).getLowerBoundExp();
				upExp =  ((GeneralTableNameArray) element).getUpBoundExp();
				
				viewName = ((GeneralTableNameArray) element).getName() + "_i";

				if(firstViewName == null)
				{
					firstViewName = viewName;
					elementName =  ((GeneralTableNameArray) element).getName();
				}
				
				view = catalog.getView(prefix + "_i");
				
				indexExp = lowerExp;
				tempReference = new TableReference(viewName,
						viewName,
						null,
						TableReference.GENERAL_INDEX_TABLE,
						indexExp);
				translatorElement = sqlExpressionTranslator.indexTableScan(view, viewName, tempReference);
				tableElementList.add(translatorElement);
				
				// translate the lower expression and upExpression.
				translatedMathOperatorList = new ArrayList<MathOperator>();
				mathExpressionTranslator.translateMathExpression(lowerExp, 
																new ArrayList<String>(), 
																translatedMathOperatorList);
				generalTableBoundList.add(translatedMathOperatorList.get(0));
				generalTableBoundExpressionList.add(lowerExp);
				
				translatedMathOperatorList = new ArrayList<MathOperator>();
				mathExpressionTranslator.translateMathExpression(upExp, 
																new ArrayList<String>(), 
																translatedMathOperatorList);
				generalTableBoundList.add(translatedMathOperatorList.get(0));
				generalTableBoundExpressionList.add(upExp);
				
				if(translatorElement instanceof Projection)
				{
					childProjectedNameList = ((Projection) translatorElement).getProjectedNameList();
					inputAttributeNameList.addAll(childProjectedNameList);
				}
				else
				{
					throw new RuntimeException("The children of UnionView operator should be Projection");
				}
			}
			else
			{
				throw new RuntimeException("We do not suppor the other type in the Union view definition!");
			}
		}
		
		/*
		 * 2. translate the seed
		 */
		Operator seedOperator = seed(tableElementList.get(0), firstViewName);
		
		/*
		 * 3. projection the seed.
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		children.add(seedOperator);
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<String> projectedNameList = new ArrayList<String>();
		String inputSeedAttributeName = firstViewName + "_seed";
		projectedNameList.add(inputSeedAttributeName);
		
		Projection seedProjection = new Projection(nodeName, children, parents, projectedNameList);
		seedOperator.addParent(seedProjection);
		
		Projection firstElement = (Projection)(tableElementList.get((0)));
		String tempNodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> tempChildren = new ArrayList<Operator>();
		ArrayList<Operator> tempParents = new ArrayList<Operator>();
		ArrayList<String> tempProjectedNameList = new ArrayList<String>();
		tempProjectedNameList.addAll(firstElement.getProjectedNameList());
		tempChildren.add(seedOperator);
		Projection repeatedFirstElement = new Projection(tempNodeName, tempChildren, tempParents, tempProjectedNameList);
		seedOperator.addParent(repeatedFirstElement);
		/*
		 * 4. translate the VGWrapper, which simulates the UnionView.
		 * Data structure for the UnionView operator.
		 */
		nodeName = "node" + translatorHelper.getNodeIndex();
		children = new ArrayList<Operator>();
		parents = new ArrayList<Operator>();
		String vgWrapperName = UnionViewHelper.UNION_VIEW_NAME_PREFIX + translatorHelper.getVgWrapperIndex();
		String vgFunctionName = UnionViewHelper.UNION_VIEW_FUNCTION_PREFIX + translatorHelper.getVgWrapperIndex();
		inputSeedAttributeName = firstViewName + "_seed";
		String outputSeedAttributeName = schema.viewName + "_seed2";
		Operator outerRelationOperator = seedProjection;
		VGFunctionStatistics vgFunctionStatistics;
		viewName = schema.viewName;
		
		ArrayList<DataType> outputAttributeTypeList = new ArrayList<DataType>();
		
		view = catalog.getView(viewName);
		ArrayList<Attribute> realAttributes = view.getAttributes();
		for(int i = 0; i < realAttributes.size(); i++)
		{
			outputAttributeTypeList.add(realAttributes.get(i).getType());
		}
		
		boolean isAligned = schema.isAlignd;
		//String elementName;
		//ArrayList<String> constantTableList;
		//ArrayList<MathOperator> individualGeneralIndexList;
		//ArrayList<MathOperator> generalTableBoundList;
		
		children.add(seedProjection);
		children.add(repeatedFirstElement);//--modification
		inputAttributeTypeList.addAll(outputAttributeTypeList); //--modification
		
		for(int i = 1; i < tableElementList.size(); i++)
		{
			children.add(tableElementList.get(i));
			inputAttributeTypeList.addAll(outputAttributeTypeList);
		}
		
		ArrayList<String> outputAttributeNameList =  new ArrayList<String>();
		ArrayList<String> schemaAttributeList = schema.tableAttributeList;
		for(int i = 0; i < schemaAttributeList.size(); i++)
		{
			outputAttributeNameList.add(viewName + "_" + schemaAttributeList.get(i));
		}
		
		vgFunctionStatistics = new VGFunctionStatistics(vgFunctionName,
				"null",
				1,
				inputAttributeNameList, 
				outputAttributeNameList
				);
		
		UnionView uionViewOperator = new UnionView(nodeName, 
								children, 
								parents,
								vgWrapperName, 
								vgFunctionName,
								inputSeedAttributeName, 
								outputSeedAttributeName,
								outerRelationOperator,
								vgFunctionStatistics,
								viewName,
								inputAttributeTypeList,
								outputAttributeTypeList,
								isAligned,
								elementName,
								constantTableList,
								individualGeneralIndexList,
								individualGeneralIndexExpressionList,
								generalTableBoundList,
								generalTableBoundExpressionList);
		
		uionViewOperator.setInputAttributeNameList(inputAttributeNameList);
		uionViewOperator.setOutputAttributeNameList(outputAttributeNameList);
		
		
		for(int i = 0; i < children.size(); i++)
		{
			children.get(i).addParent(uionViewOperator);
		}
		
		/*
		 * 5. add a projection on the top of VGWrapper.
		 */
		nodeName = "node" + translatorHelper.getNodeIndex();
		children = new ArrayList<Operator>();
		children.add(uionViewOperator);
		parents = new ArrayList<Operator>();
		projectedNameList = new ArrayList<String>();
		for(int i = 0; i < outputAttributeNameList.size(); i++)
		{
			projectedNameList.add(outputAttributeNameList.get(i));
		}
		
		Projection unionProjection = new Projection(nodeName, children, parents, projectedNameList);
		uionViewOperator.addParent(unionProjection);
		return unionProjection;
	}
	
	
	/**
	 * @param typechecker
	 * @param wherePredicate
	 * @param translatedElement
	 * @return
	 * @throws Exception 
	 */
	private Operator translateBooleanScalarFunction(BooleanPredicate wherePredicate,
														   Operator originalOperator) throws Exception {
		ArrayList<MathExpression> mathExpressionList = new ArrayList<MathExpression>();
		MathExpressionExtractor.getMathExpressionInBooleanPredicate(wherePredicate, mathExpressionList);
		
		//data structure for the scalar function
		ArrayList<MathExpression> expList = new ArrayList<MathExpression>();
		ArrayList<ArrayList<String>> sourceList = new ArrayList<ArrayList<String>>();
		HashMap<MathExpression, String> expressionNameMap = new HashMap<MathExpression, String>();
		
		for(int i = 0; i < mathExpressionList.size(); i++)
		{
			MathExpression mathExpression = mathExpressionList.get(i);
			
			if(isAtomMathExpression(mathExpression) || mathExpression instanceof SubqueryExpression ||
					translatorHelper.expressionNameMap.containsKey(mathExpression))
			{
				continue;
			}
			
			expList.add(mathExpression);
			
			ArrayList<String> tempExpList = new ArrayList<String>();
			columnExtractor.getColumnInMathExpression(mathExpression, tempExpList);
			sourceList.add(tempExpList);
			
			String outputName = "attribute" + translatorHelper.getTempAttributeIndex();
			expressionNameMap.put(mathExpression, outputName);
		}
		
		//translate the middle structure <expList, sourceList>.
		if(expList.size() != 0)
		{
			/*
			 * 1. Associated data structure
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
			ArrayList<Operator> parents = new ArrayList<Operator>();
			ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
			HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
			HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
			
			if(originalOperator != null)
				children.add(originalOperator);
			
			/*
			 * 2. Assigned associated values to data structure of ScalarFunction.
			 */
			ArrayList<String> subTranlatedResult = new ArrayList<String>();
			
			for(int i = 0; i < expList.size(); i++)
			{
				MathExpression tempExpression = expList.get(i);
				mathExpressionTranslator.translateMathExpression(tempExpression, subTranlatedResult, scalarExpressionList);
			}
			
			for(int i = 0; i < sourceList.size(); i++)
			{
				/*
				 * 2.2 Fill in the columnListMap.
				 */
				ArrayList<String> columnList = sourceList.get(i);
				
				/*
				 * 2.2.3 Fill the columnListMap with columnList
				 * Following array List has the same length:
				 * (1). ExpList
				 * (2). SourceList
				 * (3). scalarExpressionList
				 */
				columnListMap.put(scalarExpressionList.get(i), columnList);
			}
			
			//element in expList is the expression in AggregateExpression
			for(int i =0; i < expList.size(); i++)
			{
				MathExpression tempExpression = expList.get(i);
				/*
				 * 3. Fill the outputMap.
				 */
				outputMap.put(scalarExpressionList.get(i), expressionNameMap.get(tempExpression));
				
				translatorHelper.expressionNameMap.put(tempExpression, expressionNameMap.get(tempExpression));
			}
			
			ScalarFunction element = new ScalarFunction(nodeName, children, parents, translatorHelper);
			element.setScalarExpressionList(scalarExpressionList);
			element.setColumnListMap(columnListMap);
			element.setOutputMap(outputMap);
			
			/*
			 * 4. set the parent of originalOperator
			 */
			if(originalOperator != null)
				originalOperator.addParent(element);
			return element;
		}
		else
			return originalOperator;
	}

	public Operator translate(UnnestedRandomTableStatement randomStatement)throws Exception
	{
		/*
		 * All the information embedded in the UnnestedRandomTableStatement
		 */
		RandomTableStatement randomTableStatement = randomStatement.getRandomTableStatement();
		RandomTableTypeChecker randomTableChecker = randomStatement.getRandomTableChecker();
		
		//UnnestedSelectStatement outTableUnnestStatement = randomStatement.getOutTableUnnestStatement();
		ArrayList<String> vgTableNameList = randomStatement.getVgTableNameList();
		HashMap<String, ArrayList<UnnestedSelectStatement>> vgWrapperParaStatementMap
											      = randomStatement.getVgWrapperParaStatementMap();
		UnnestedSelectStatement unnestedSelectStatement = randomStatement.getUnnestedSelectStatement();
		
		/*
		 * Begin translation
		 * 1. Translate the outer table.
		 */
		Operator translatedOutTableElment;
		String outerTableAlias = randomTableChecker.getOutTableName();
		translatedOutTableElment = translateOuterTableReference(randomTableChecker, outerTableAlias);
		
		/*
		 * 2. seed operation
		 */
		translatedOutTableElment = seed(translatedOutTableElment, outerTableAlias);
		
		/*
		 * 3. Translate the VGFunction. All the vgfunction in the withlist.
		 * Some data structure used here:
		 * 	vgTableNameList: ArrayList<String>
		 *  vgWrapperParaStatementMap: HashMap<String, ArrayList<UnnestedSelectStatement>>
		 *  translatedOutTableElment
		 */
		
		/*
		 * 3.1 Translate all the parameter sql in the vgfunction.
		 * There can be multiple vgFunctions in the statement,
		 * and each vgFucntion can have multiple parameter sql.
		 */
		HashMap<String, ArrayList<Operator>> tranlatedElementMap = new HashMap<String, ArrayList<Operator>>();
		for(int i = 0; i < vgTableNameList.size(); i++)
		{
			String vgTableName = vgTableNameList.get(i);
			ArrayList<UnnestedSelectStatement> vgParaList = vgWrapperParaStatementMap.get(vgTableName);
			
			ArrayList<Operator> translatedParaElementList = new ArrayList<Operator>();
			for(int j = 0; j < vgParaList.size(); j++)
			{
				UnnestedSelectStatement paraStatement = vgParaList.get(j);
				Operator translatedParaElement = translateVGFunctionParameter(paraStatement,
																					   translatedOutTableElment);
				translatedParaElementList.add(translatedParaElement);
			}
			tranlatedElementMap.put(vgTableName, translatedParaElementList);
		}
		/*
		 * 3.2 Translate each VGWrapper
		 */
		HashMap<String, Operator> tranlatedVGWrapperMap = new HashMap<String, Operator>();
		ArrayList<WithStatement> withList = randomTableStatement.withList;
		
		for(int i = 0; i < vgTableNameList.size(); i++)
		{
			String vgTableName = vgTableNameList.get(i);
			String vgFunctionName = withList.get(i).expression.functionName;
			ArrayList<Operator> translatedParaElementList = tranlatedElementMap.get(vgTableName);
			
			Operator element = translateVGWrapper(translatedOutTableElment, 
					                                       translatedParaElementList, 
					                                       outerTableAlias, 
					                                       vgFunctionName);
			tranlatedVGWrapperMap.put(vgTableName, element);
		}
		
		/*
		 * 4. Join all the result of VGWrappers if it is needed.
		 * It depends on the select sql in the RandomTable.
		 * Maybe some of them are not needed. But if is needed, we should guarantee
		 * that all the VGWrapper are joined fist. And then, the the result joins with
		 * the outTable with the seed.
		 */
		Operator resultElement = translateInnerSelectStatement(translatedOutTableElment,
				vgTableNameList,
				tranlatedVGWrapperMap,
				randomTableChecker,
				unnestedSelectStatement);
				
		
		/*
		 * 5. Join with the seed
		 */
		
		
		/*
		 * 6. Selection 
		 */
		
		/*
		 * 7. Scalar function
		 */
		
		/*
		 * 8. Aggregate function
		 */
		
		/*
		 * 9. Projection.
		 */
		
		/*
		 * 10. Having clause
		 */
		return resultElement;
	}
	
	
	
	public Operator translateOuterTableReference(TypeChecker typeChecker, String alias)throws Exception
	{
		Operator translatedOutTableElment = null;
		
		Catalog catalog = SimsqlCompiler.catalog;
		
		String tableName;
		Relation relation;
		
		HashMap<String, TableReference> tableReferenceMap = typeChecker.getTableReferenceMap();
		//<table_alias, subquery>
		HashMap<String, FromSubquery> fromSubqueryMap = typeChecker.getFromSubqueryMap();
		//<table_alias, typechecker>
		HashMap<String, TypeChecker> typerCheckerMap = typeChecker.getTyperCheckerMap();
		
		if(tableReferenceMap.containsKey(alias))
		{
			TableReference tempTableReference = tableReferenceMap.get(alias);
			
			tableName = tempTableReference.table;
			
			relation = catalog.getRelation(tableName);
			View view;
			if(relation != null)
			{
				translatedOutTableElment = sqlExpressionTranslator.tableScan(relation, alias);
			}
			else if(((TableReference)tempTableReference).getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE ||
					((TableReference)tempTableReference).getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
			{
				/* throw new RuntimeException("Out table does not allow random table");
				/*---------------hack for hmm-----------------------*/
				view = catalog.getView(tableName);
				translatedOutTableElment = sqlExpressionTranslator.indexTableScan(view, alias, (TableReference)tempTableReference);
				/*---------------------------------------------------*/
			}
			else
			{
				view = catalog.getView(tableName);
				translatedOutTableElment = sqlExpressionTranslator.viewScan(view, alias);
			}
		}
		//for subquery
		else if(fromSubqueryMap.containsKey(alias))
		{
			TypeChecker tempFromTypeChecker = typerCheckerMap.get(alias);
			
			HashMap<TypeChecker, Operator> viewPlanMap = translatorHelper.fromqueryPlanMap;
			
			//if this subquery has been processed
			if(viewPlanMap.containsKey(tempFromTypeChecker))
			{
				translatedOutTableElment = viewPlanMap.get(tempFromTypeChecker);
			}
			else
			{
				UnnestedSelectStatement nestedFromSubquery 
								= TemporaryViewFactory.tempTypeCheckerViewMap.get(tempFromTypeChecker);
				translatedOutTableElment = translate(nestedFromSubquery);
				translatedOutTableElment = sqlExpressionTranslator.addHolder(tempFromTypeChecker.getAttributeList(), translatedOutTableElment, alias);
				viewPlanMap.put(tempFromTypeChecker, translatedOutTableElment);
			}
		}
		
		return translatedOutTableElment;
	}
	
	public Operator translateBooleanPredicateClause(BooleanPredicate booleanredicate,
														   Operator originalElement) throws Exception
	{
		if(booleanredicate == null)
			return originalElement;
		
		/*
		 * 0.1 Do the scalarFunction for where clause
		 */
		
		originalElement = translateBooleanScalarFunction(booleanredicate, originalElement);
		
		/*
		 * 1. The data structure.
		 * 
		 */
		String nodeName;
		ArrayList<Operator> children = new ArrayList<Operator>(1);
		ArrayList<Operator> parents = new ArrayList<Operator>();
		BooleanOperator booleanOperator;
		
		/*
		 * 2. Fill the nodeName
		 */
		ArrayList<String> subResultList = new ArrayList<String>();
		int nodeIndex = translatorHelper.getNodeIndex();
		nodeName = "node"+nodeIndex;
		
		/*
		 * 3. Fill the translatedStatement
		 */
		
		/*
		 * 4. FIll the BooleanOperator
		 */
		ArrayList<BooleanOperator> operatorList = new ArrayList<BooleanOperator>();
		
		if(booleanredicate instanceof AndPredicate)
		{
			/*
			 * case 4.1: AndOperator
			 */
			ArrayList<BooleanPredicate> andList = ((AndPredicate) booleanredicate).andList;
			
			for(int i = 0 ; i < andList.size(); i++)
			{
				if(i != andList.size() - 1)
				{
					predicateTranslator.translateBooleanPredicate(andList.get(i), subResultList, operatorList);
					
				}
				else
				{
					predicateTranslator.translateBooleanPredicate(andList.get(i), subResultList, operatorList);
				}	
			}
			booleanOperator = new AndOperator(nodeName, operatorList);
		}
		else
		{
			predicateTranslator.translateBooleanPredicate(booleanredicate, subResultList, operatorList);
			booleanOperator = operatorList.get(0);
		}
		
		/*
		 * 4. Fill the children
		 */
		children.add(originalElement);
		
		/*
		 * 5. Create the Selection Node
		 */
		Selection selection = new Selection(nodeName, children, parents, booleanOperator);
		
		/*
		 * 6. Add parent for its children
		 */
		originalElement.addParent(selection);
		return selection;
	}
	
	public Operator translateChildView(ArrayList<UnnestedSelectStatementJoin> childrenView, 
			                         				   Operator leftJoinResult)throws Exception
	{
		Operator resultElement = leftJoinResult;
		
		/*
		 * There can be n subqueries in the where clause, but after we transform the where clause to 
		 * CNF, such subqueries can overlap in the same predicate.
		 */
		
		for(int i = 0; i < childrenView.size(); i++)
		{
			UnnestedSelectStatementJoin tempElement = childrenView.get(i);
			ArrayList<UnnestedSelectStatement> viewList = tempElement.viewList;
			boolean semi_join = tempElement.semi_join;
			BooleanPredicate predicate = tempElement.predicate;
			
			HashMap<String, Operator> viewPlanMap = translatorHelper.viewPlanMap;
			
			/*
			 * (constant or not Q1 or not Q2) => not(not constant and (Q1 and Q2)
			 * => anti_join(left, join(Q1, Q2)) on not constant.
			 * Here inner join is a data structure to save the join between Q1 and Q2.
			 * 1.1 Translate each temporary view
			 */
			ArrayList<Operator> innerJoin = new ArrayList<Operator>();
			for(int j = 0; j < viewList.size(); j++)
			{
				UnnestedSelectStatement joinElement = viewList.get(j);
				String temporyViewName = joinElement.getViewName();
				Operator tempInnerElement;
				if(viewPlanMap.containsKey(temporyViewName))
				{
					tempInnerElement = viewPlanMap.get(temporyViewName);
				}
				else
				{
					tempInnerElement = translate(joinElement);
				}
				innerJoin.add(tempInnerElement);
			}
			
			/*
			 * 1.2 Join such temporary view
			 */
			Operator innerJoinElement = null;
			if(innerJoin.size() != 0)
			{
				innerJoinElement = innerJoin.get(0);
				for(int j = 1; j < innerJoin.size(); j++)
				{
					innerJoinElement = join(innerJoinElement, innerJoin.get(j));
				}
			}
			
			/*
			 * 1.3 Anti/Semi join with such view
			 */
			if(semi_join)
			{
				resultElement = join(resultElement, innerJoinElement, SEMIJOIN, predicate);
			}
			else
			{
				resultElement = join(resultElement, innerJoinElement, ANTIJOIN, predicate);
			}
		}
		return resultElement;
	}
	
	public boolean aggregateInSelect(SelectStatement selectStatement)
	{
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression expression = selectList.get(i);
			if(expression instanceof DerivedColumn)
			{
				MathExpression mExpression = ((DerivedColumn)expression).expression;
				if(CheckAggregate.directAggregateInMathExpression(mExpression))
				{
					return true;
				}
			}
		}
		
		return false;
	}

	private Operator translateAncester(ArrayList<SQLExpression> addedTableReferenceList, boolean needDuplicateRemove)throws Exception
	{
		/*
		 * 1. scan each table reference (can be a table/subquery/view), and create nodes.
		 */
		if(addedTableReferenceList == null || addedTableReferenceList.size() == 0)
		{
			return null;
		}
		Catalog catalog = SimsqlCompiler.catalog;
		ArrayList<Operator> translatedElementList = new ArrayList<Operator>();
		
		Operator translatorElement;
		for(int i = 0; i < addedTableReferenceList.size(); i++)
		{
			SQLExpression reference = addedTableReferenceList.get(i);
			if(reference instanceof TableReference)
			{
				String tableName = ((TableReference)reference).table;
				String tableAlias = ((TableReference)reference).alias;
				Relation relation = catalog.getRelation(tableName);
				View view;
				if(relation != null)
				{
					translatorElement = sqlExpressionTranslator.tableScan(relation, tableAlias);
					translatedElementList.add(translatorElement);
				}
				else
				{
					if(((TableReference)reference).getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE ||
							((TableReference)reference).getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
					{
						view = catalog.getView(tableName);
						translatorElement = sqlExpressionTranslator.indexTableScan(view, tableAlias, (TableReference)reference);
						translatedElementList.add(translatorElement);
					}
					else
					{
						view = catalog.getView(tableName);
						translatorElement = sqlExpressionTranslator.viewScan(view, tableAlias);
						translatedElementList.add(translatorElement);
					}
				}
			}
			else if(reference instanceof FromSubquery)
			{
				MathExpression expression = ((FromSubquery) reference).expression;
				String alias = ((FromSubquery) reference).alias;
				SubqueryExpression subQExpression = (SubqueryExpression)expression;
				TypeChecker subQChecker = subQExpression.getTypeChecker();
				HashMap<TypeChecker, Operator> fromSubqueryMap = translatorHelper.fromqueryPlanMap;
				
				//if this subquery has been processed
				if(fromSubqueryMap.containsKey(subQChecker))
				{
					translatorElement = fromSubqueryMap.get(subQChecker);
				}
				else
				{
					UnnestedSelectStatement nestedFromSubquery 
									= TemporaryViewFactory.tempTypeCheckerViewMap.get(subQChecker);
					translatorElement = translate(nestedFromSubquery);
					fromSubqueryMap.put(subQChecker, translatorElement);
				}	
				
				/*
				 * --------------------------------------modified by bamboo-----------------------
				 */
				ArrayList<String> outputList = subQChecker.getAttributeList();
				translatorElement = sqlExpressionTranslator.addHolder(outputList, translatorElement, alias);
				/*
				 * -------------------------------------------------------------------------------
				 */
				translatedElementList.add(translatorElement);
			}
		}
		
		/*
		 * 2. duplicate removal.
		 */
		ArrayList<Operator> duplicatedRemovalElement = new ArrayList<Operator>();
		
		if(needDuplicateRemove)
		{
			for(int i = 0; i < translatedElementList.size(); i++)
			{
				Operator element = translatedElementList.get(i);
				element = duplicateRemoval(element);
				duplicatedRemovalElement.add(element);
			}
		}
		else
		{
			duplicatedRemovalElement = translatedElementList;
		}

		
		//finish the n isolated node, then join them
		Operator joinResult = duplicatedRemovalElement.get(0);
		
		for(int i = 1; i < duplicatedRemovalElement.size(); i++)
		{
			Operator element = duplicatedRemovalElement.get(i);
			joinResult = join(joinResult, element);
		}
		
		return joinResult;
	}
	
	/*
	 * translate the where clause
	 */
	public Operator translateFromClause(TypeChecker typechecker, ArrayList<String> fromList)throws Exception
	{
		Catalog catalog = SimsqlCompiler.catalog;
		HashMap<String, TableReference> tableReferenceMap = typechecker.getTableReferenceMap();
		//<table_alias, subquery>
		HashMap<String, FromSubquery> fromSubqueryMap = typechecker.getFromSubqueryMap();
		//<table_alias, typechecker>
		HashMap<String, TypeChecker> typerCheckerMap = typechecker.getTyperCheckerMap();
		
		ArrayList<Operator> translatedElementList = new ArrayList<Operator>();
		
		Operator translatorElement;
		
		for(int i = 0; i < fromList.size(); i++)
		{
			String tableAlias = fromList.get(i);
			
			String tableName;
			Relation relation;
			
			if(tableReferenceMap.containsKey(tableAlias))
			{
				TableReference tempTableReference = tableReferenceMap.get(tableAlias);
				
				tableName = tempTableReference.table;
				
				relation = catalog.getRelation(tableName);
				View view;
				if(relation != null)
				{
					translatorElement = sqlExpressionTranslator.tableScan(relation, tableAlias);
					translatedElementList.add(translatorElement);
				}
				else if(((TableReference)tempTableReference).getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE ||
						((TableReference)tempTableReference).getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
				{
					view = catalog.getView(tableName);
					translatorElement = sqlExpressionTranslator.indexTableScan(view, tableAlias, (TableReference)tempTableReference);
					translatedElementList.add(translatorElement);
				}
				else
				{
					view = catalog.getView(tableName);
					translatorElement = sqlExpressionTranslator.viewScan(view, tableAlias);
					translatedElementList.add(translatorElement);
				}
			}
			//for subquery
			else if(fromSubqueryMap.containsKey(tableAlias))
			{
				TypeChecker tempFromTypeChecker = typerCheckerMap.get(tableAlias);
				HashMap<TypeChecker, Operator> viewPlanMap = translatorHelper.fromqueryPlanMap;
				
				//if this subquery has been processed
				if(viewPlanMap.containsKey(tempFromTypeChecker))
				{
					translatorElement = viewPlanMap.get(tempFromTypeChecker);
				}
				else
				{
					UnnestedSelectStatement nestedFromSubquery 
									= TemporaryViewFactory.tempTypeCheckerViewMap.get(tempFromTypeChecker);
					translatorElement = translate(nestedFromSubquery);
					viewPlanMap.put(tempFromTypeChecker, translatorElement);
				}
					
				/*
				 * add the holder by tableAlias
				 */
				ArrayList<String> outputAttributeList = tempFromTypeChecker.getAttributeList();
				
				if(outputAttributeList != null)
					translatorElement = sqlExpressionTranslator.addHolder(outputAttributeList,
																			translatorElement,
																			tableAlias);
				
				translatedElementList.add(translatorElement);
			}
		}
		
		if(translatedElementList.size() > 0)
		{
			Operator joinResult = translatedElementList.get(0);
			
			for(int i = 1; i < translatedElementList.size(); i++)
			{
				Operator element = translatedElementList.get(i);
				joinResult = join(joinResult, element);
			}
			
			return joinResult;
		}
		else
		{
			return null;
		}
	}
	
	public Operator join(Operator e1, Operator e2) throws Exception
	{
		if(e1 == null && e2 == null)
		{
			return null;
		}
		else if(e1 != null && e2 == null)
		{
			return e1;
		}
		else if(e1 == null & e2 != null)
		{
			return e2;
		}
		return join(e1, e2, JOIN, null);
	}
	
	public Operator join(Operator e1, 
								Operator e2, 
								int joinType, 
								BooleanPredicate predicate) throws Exception
	{
		/*
		 * 1. We should add left two scalarFunctions on two sides, if possible.
		 */
		
		/*
		 * 1.1 We should check if A antiJion with B on (C+D > E+F)
		 * here if C, D belong to the same side, and E+F belong to the same side.
		 * So the columns in each top mathExpression should belong to the same side.
		 */
		
		ArrayList<String> projectionList = null;
		if(e2 instanceof Projection)
		{
			projectionList = ((Projection) e2).getProjectedNameList();
			
			if(checkBooleanPredicateForScalarFunction(predicate, projectionList) == false)
			{
				System.err.println("the subquery expression is not supported");
				throw new Exception("the subquery expression is not supported");
			}
		}
		
		/*
		 * 1.2 We use the getScalarFunctionForBooleanPredicate function to get the two scalarFunctions.
		 */
		if(predicate != null)
		{
			ArrayList<ScalarFunction> scalarFunctionList = new ArrayList<ScalarFunction>();
			getScalarFunctionForBooleanPredicate(predicate, 
					 							 projectionList,
					 							 scalarFunctionList);
			
			/*
			 * 1.3 Add the links for connecting them with the current two sides, i.e., e1 and e2.
			 */
			ScalarFunction leftScalarFunction = null;
			ScalarFunction rightScalarFunction = null;
			
			ArrayList<MathOperator> leftMathOperatorList = null;
			ArrayList<MathOperator> rightMathOperatorList = null;
			
			if(scalarFunctionList.size() != 0)
			{
				leftScalarFunction = scalarFunctionList.get(0);
				rightScalarFunction = scalarFunctionList.get(1);
				
				leftMathOperatorList = leftScalarFunction.getScalarExpressionList();
				rightMathOperatorList = rightScalarFunction.getScalarExpressionList();
			}
			
			
			
			
			/*
			 * add the left scalarFunction
			 */
			if(leftMathOperatorList != null && leftMathOperatorList.size() != 0)
			{
				leftScalarFunction.addChild(e1);
				e1.addParent(leftScalarFunction);
				e1 = leftScalarFunction;
			}
			
			/*
			 * add the left scalarFunction
			 */
			if(rightMathOperatorList != null && rightMathOperatorList.size() != 0)
			{
				rightScalarFunction.addChild(e2);
				e2.addParent(rightScalarFunction);
				e2 = rightScalarFunction;
			}
		}
		/*
		 * 2 Data structure of Join
		 */
		int nodeIndex = translatorHelper.getNodeIndex();
		String nodeName = "node"+nodeIndex;
		ArrayList<Operator> children = new ArrayList<Operator>(2);
		ArrayList<Operator> parents = new ArrayList<Operator>(2);
		int type;
		String leftTable;
		BooleanOperator booleanOperator;
		
		String nodeName_1 = e1.getNodeName();
		String nodeName_2 = e2.getNodeName();
		
		/*
		 * 2.1. Fill translatedStatement
		 */
		
		ArrayList<String> subResultList = new ArrayList<String>();
		
		/*
		 * 2.2. Fill type
		 */
		type = joinType;
		ArrayList<BooleanOperator> booleanOperatorList = new ArrayList<BooleanOperator>();
		
		/*
		 * 2.3. Fill leftTable
		 */
		leftTable = nodeName_1;
		
		/*
		 * 2.4. Fill booleanOperator and translatedStatement
		 */
		switch(joinType)
		{
			case SEMIJOIN:
				
				
				if(predicate instanceof AndPredicate)
				{
					ArrayList<BooleanPredicate> andList = ((AndPredicate) predicate).andList;
					
					for(int i = 0 ; i < andList.size(); i++)
					{
						String booleanPredicateName = predicateTranslator.translateBooleanPredicate(andList.get(i), 
														subResultList,
														booleanOperatorList);
						
					}
					booleanOperator = new AndOperator("", booleanOperatorList);
				}
				else
				{
					predicateTranslator.translateBooleanPredicate(predicate, 
																								subResultList,
																								booleanOperatorList);
					if(booleanOperatorList.size()> 0)
					{
						booleanOperator = booleanOperatorList.get(0);
					}
					else
					{
						booleanOperator = null;
					}
				}
				
				break;
				
			case ANTIJOIN:
				
				if(predicate instanceof AndPredicate)
				{
					ArrayList<BooleanPredicate> andList = ((AndPredicate) predicate).andList;
					
					for(int i = 0 ; i < andList.size(); i++)
					{
						String booleanPredicateName = predicateTranslator.translateBooleanPredicate(andList.get(i), 
								subResultList,
								booleanOperatorList);

					}
					booleanOperator = new AndOperator("", booleanOperatorList);
				}
				else
				{
					predicateTranslator.translateBooleanPredicate(predicate, 
							subResultList,
							booleanOperatorList);
					if(booleanOperatorList.size()> 0)
					{
						booleanOperator = booleanOperatorList.get(0);
					}
					else
					{
						booleanOperator = null;
					}
				}
				break;
				
			default:
				if(predicate instanceof AndPredicate)
				{
					ArrayList<BooleanPredicate> andList = ((AndPredicate) predicate).andList;
					
					for(int i = 0 ; i < andList.size(); i++)
					{
						String booleanPredicateName = predicateTranslator.translateBooleanPredicate(andList.get(i), 
								subResultList,
								booleanOperatorList);
					}
					booleanOperator = new AndOperator("", booleanOperatorList);
				}
				else
				{
					predicateTranslator.translateBooleanPredicate(predicate, 
							subResultList,
							booleanOperatorList);
					if(!booleanOperatorList.isEmpty())
					{
						booleanOperator = booleanOperatorList.get(0);
					}
					else
					{
						booleanOperator = null;
					}
				}
				break;
		}
		/*
		 * 2.5. Fill children
		 */
		children.add(e1);
		children.add(e2);
		
		/*
		 * 2.6. Create the Join
		 */
		Join join = new Join(nodeName, 
				children,
				parents,
				type,
				leftTable,
				booleanOperator);
		
		/*
		 * 2.7. Add the parent of its children
		 */
		
		e1.addParent(join);
		e2.addParent(join);
		
		return join;
	}
	
	
	public Operator duplicateRemoval(Operator originalOperator)
	{
		/*
		 * 1. Data structure
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();;
		ArrayList<Operator> children = new ArrayList<Operator>(1);
		ArrayList<Operator> parents = new ArrayList<Operator>(1);
		
		children.add(originalOperator);

		DuplicateRemove duplicateRemove = new DuplicateRemove(nodeName, 
				                                            children,
				                                            parents);
		originalOperator.addParent(duplicateRemove);
		return duplicateRemove;
	}
	
	/*
	 * translate the scalar function
	 */
	public Operator translateScalarFunction(TypeChecker typeChecker,
										  SelectStatement selectStatement,
										  Operator originalOperator)throws Exception
    {
		
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		
		/*
		 * Consider the case sentenses.
		 */
		ArrayList<BooleanPredicate> casePredicateList = new ArrayList<BooleanPredicate>();
		for(int i = 0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			if(selectElement instanceof DerivedColumn)
			{
				MathExpression expression = ((DerivedColumn) selectElement).expression;
				String alias = ((DerivedColumn) selectElement).alias;
				
				if(expression instanceof GeneralFunctionExpression)
				{
					String name = ((GeneralFunctionExpression)expression).functionName;
					
					if(name.equals("case"))
					{
						ArrayList<MathExpression> subMathExpressionList = ((GeneralFunctionExpression)expression).parasList;
						if(subMathExpressionList.get(0) instanceof PredicateWrapper)
						{
							for(int j = 0; j < subMathExpressionList.size()-1; j += 2)
							{
								PredicateWrapper wrapper = (PredicateWrapper)(subMathExpressionList.get(j));
								casePredicateList.add(wrapper.predicate);
							}
						}
					}
				}
			}
		}
		
		for(int i = 0; i < casePredicateList.size(); i++)
		{
			originalOperator = translateBooleanScalarFunction(casePredicateList.get(i), originalOperator);
		}
		
		ArrayList<MathExpression> expList = new ArrayList<MathExpression>();
		ArrayList<ArrayList<String>> sourceList = new ArrayList<ArrayList<String>>();
		HashMap<MathExpression, String> expressionNameMap = new HashMap<MathExpression, String>();
		
		for(int i =0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			if(selectElement instanceof DerivedColumn)
			{
				MathExpression expression = ((DerivedColumn) selectElement).expression;
				String alias = ((DerivedColumn) selectElement).alias;
				expressionNameMap.put(expression, alias);
				
				/*
				 * The way to do renaming
				 */
				//if(expression instanceof ArithmeticExpression)
				{
					ArrayList<String> tempExpList = new ArrayList<String>();
					columnExtractor.getColumnInMathExpression(expression, tempExpList);
					
					expList.add(expression);
					sourceList.add(tempExpList);
				}
			}
			else if(selectElement instanceof AsteriskTable)
			{
				ArrayList<ColumnExpression> list = typeChecker.getColumnFromAsteriskTable();
				
				for(int j = 0; j < list.size(); j++)
				{
					ColumnExpression columnExpression = list.get(j);
					String alias;
					
					if(typeChecker.isAllowDuplicatedAttributeAlias())
						alias = columnExpression.toString();
					else
						alias = columnExpression.columnName;
					
					expressionNameMap.put(columnExpression, alias);
					
					/*
					 * The way to do renaming
					 */
					//if(expression instanceof ArithmeticExpression)
					{
						ArrayList<String> tempExpList = new ArrayList<String>();
						columnExtractor.getColumnInMathExpression(columnExpression, tempExpList);
						
						expList.add(columnExpression);
						sourceList.add(tempExpList);
					}
				}
			}
			else if(selectElement instanceof AllFromTable)
			{
				ArrayList<ColumnExpression> list = 
					typeChecker.getColumnFromAllFromTable((AllFromTable)selectElement);
				
				for(int j = 0; j < list.size(); j++)
				{
					ColumnExpression columnExpression = list.get(j);
					String alias;
					
					if(typeChecker.isAllowDuplicatedAttributeAlias())
						alias = columnExpression.toString();
					else
						alias = columnExpression.columnName;
					
					expressionNameMap.put(columnExpression, alias);
					
					/*
					 * The way to do renaming
					 */
					//if(expression instanceof ArithmeticExpression)
					{
						ArrayList<String> tempExpList = new ArrayList<String>();
						columnExtractor.getColumnInMathExpression(columnExpression, tempExpList);
						
						expList.add(columnExpression);
						sourceList.add(tempExpList);
					}
				}
			}
		}
		
		//translate the middle structure <expList, sourceList>.
		if(expList.size() != 0)
		{
			/*
			 * 1. Associated data structure
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
			ArrayList<Operator> parents = new ArrayList<Operator>();
			ArrayList<MathOperator> scalarExpressionList = new ArrayList<MathOperator>();
			HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
			HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
			
			children.add(originalOperator);
			
			/*
			 * 2. Assigned associated values to data structure of ScalarFunction.
			 */
			ArrayList<String> subTranlatedResult = new ArrayList<String>();
			
			for(int i = 0; i < expList.size(); i++)
			{
				MathExpression tempExpression = expList.get(i);
				/*------------------------------------------------------------------------------
				 * 2.1 need extension here.
				 * Fill in the scalarExpressionList.
				 * -----------------------------------------------------------------------------
				 */
				if(tempExpression instanceof ColumnExpression ||
						tempExpression instanceof AggregateExpression)
				{
					scalarExpressionList.add(new EFunction());
				}
				else
				{
					mathExpressionTranslator.translateMathExpression(tempExpression, subTranlatedResult, scalarExpressionList);
				}
				
			}
			for(int i = 0; i < sourceList.size(); i++)
			{
				/*
				 * 2.2 Fill in the columnListMap.
				 */
				ArrayList<String> columnList = new ArrayList<String>();
				if(i != sourceList.size() - 1)
				{
					ArrayList<String> sourceAttributeList = sourceList.get(i);
					for(int j = 0; j < sourceAttributeList.size(); j ++)
					{
						/*
						 * 2.2.1 Branch 1:
						 * FIll the columnList
						 */
						columnList.add(sourceAttributeList.get(j));
					}
				}
				else
				{
					ArrayList<String> sourceAttributeList = sourceList.get(i);
					for(int j = 0; j < sourceAttributeList.size(); j ++)
					{
						/*
						 * 2.2.2 Branch 1:
						 * FIll the columnList
						 */
						columnList.add(sourceAttributeList.get(j));
					}
				}
				
				/*
				 * 2.2.3 Fill the columnListMap with columnList
				 * Following array List has the same length:
				 * (1). ExpList
				 * (2). SourceList
				 * (3). scalarExpressionList
				 */
				columnListMap.put(scalarExpressionList.get(i), columnList);
			}
			
			//element in expList is the expression in AggregateExpression
			for(int i =0; i < expList.size(); i++)
			{
				MathExpression temp = expList.get(i);
				String scalarName;
				
				if(!expressionNameMap.containsKey(temp))
					scalarName = "scalarName" + translatorHelper.getScalarAttributeNameIndex();
				else
				{
					scalarName = expressionNameMap.get(temp);
				}
				
				translatorHelper.expressionNameMap.put(temp, scalarName);
				
				/*
				 * 3. Fill the outputMap.
				 */
				outputMap.put(scalarExpressionList.get(i), scalarName);
			}
			
			ScalarFunction element = new ScalarFunction(nodeName, children, parents, translatorHelper);
			element.setScalarExpressionList(scalarExpressionList);
			element.setColumnListMap(columnListMap);
			element.setOutputMap(outputMap);
			
			/*
			 * 4. set the parent of originalOperator
			 */
			originalOperator.addParent(element);
			return element;
		}
		else
			return originalOperator;
    }
	
	/*
	 * Find the AggregateExpression in selectList, and extract their MathExpression, and do the 
	 * scalar function, and then do aggregate function
	 * So here: scalar -> aggregate (-> scalar(not implemented in this function))
	 */
	public Operator translateAggregateFunction(TypeChecker typeChecker,
															UnnestedSelectStatement nestedStatement,
															ArrayList<AllFromTable> addGroupbyColumnList,
															Seed tempSeed,
															Operator originalElement)throws Exception
	{
		SelectStatement selectStatement = nestedStatement.getSelectStatement();
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		ArrayList<ColumnExpression> groupByClauseList = selectStatement.groupByClauseList;
		
		ArrayList<String> subResultList = new ArrayList<String>();
		
		/*
		 * 1. Translate the aggregate expression
		 * 
		 */
		int setQuantifier = FinalVariable.ALL;  
		
		ArrayList<MathExpression> expList = new ArrayList<MathExpression>();
		ArrayList<ArrayList<String>> sourceList = new ArrayList<ArrayList<String>>();
		ArrayList<String> targetNameList = new ArrayList<String>();
		HashMap<MathExpression, String> aggregateNameMap = new HashMap<MathExpression, String>();
		
		for(int i =0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			if(selectElement instanceof DerivedColumn)
			{
				MathExpression expression = ((DerivedColumn) selectElement).expression;
				String alias = ((DerivedColumn) selectElement).alias;
				
				/*
				 * 1. Get the aggregate list in the DerivedColumn
				 */
				ArrayList<MathExpression> aggregateListInOneDerivedColumn = new ArrayList<MathExpression>();
				
				AggregateExpressionExtractor.getAggregateInMathExpression(expression, aggregateListInOneDerivedColumn);
				
				for(int j = 0; j < aggregateListInOneDerivedColumn.size(); j++)
				{
					AggregateExpression aggExpression = (AggregateExpression)(aggregateListInOneDerivedColumn.get(j));
					setQuantifier = aggExpression.setQuantifier;
					expList.add(aggExpression);
					
					String aggregateName;
					if(expression == aggExpression)
					{
						aggregateName = alias;
					}
					else
					{
						aggregateName = "attribute" + translatorHelper.getTempAttributeIndex();
					}
					
					targetNameList.add(aggregateName);
					aggregateNameMap.put(aggExpression, aggregateName);
					
					ArrayList<ColumnExpression> tempExpList = new ArrayList<ColumnExpression>();
					
					MathExpression innerExpression = aggExpression.expression;
					
					if(innerExpression instanceof AsteriskExpression)
					{
						ArrayList<ColumnExpression> tempList = typeChecker.getColumnFromAsteriskTable();
						
						for(int k = 0; k < tempList.size(); k++)
						{
							tempExpList.add(tempList.get(k));
						}
					}
					else
						ColumnExpressionExtractor.getColumnInMathExpression(aggExpression, tempExpList);
					
					
					ArrayList<String> tempList = new ArrayList<String>();
					for(int k = 0; k < tempExpList.size(); k++)
					{
						tempList.add(tempExpList.get(k).toString());
					}
					sourceList.add(tempList);
				}
			}
		}
		
		if(expList.size() > 0)
		{
			/*
			 * 1.5 check if we need to add dedup due to aggregate(dinstinct xx).
			 * table R(A, B, C), select count(distinct A) from R group by B; in this case, this distinct may still have a problem.
			 */
			if(setQuantifier == FinalVariable.DISTINCT)
			{
				originalElement = duplicateRemoval(originalElement);
			}
			/*
			 * 2. Try to create the Aggregate operator node
			 */
			String nodeName = "node" + translatorHelper.getNodeIndex();
			ArrayList<Operator> children = new ArrayList<Operator>();
			ArrayList<Operator> parents = new ArrayList<Operator>();
			String aggregateName;
			ArrayList<String> groupbyList = new ArrayList<String>();
			ArrayList<MathOperator> aggregateExpressionList = new ArrayList<MathOperator>();
			HashMap<MathOperator, ArrayList<String>> columnListMap = new HashMap<MathOperator, ArrayList<String>>();
			HashMap<MathOperator, String> outputMap = new HashMap<MathOperator, String>();
			
			/*
			 * 2.2 Fill children
			 */
			children.add(originalElement);
			
			String aggName = "agg" + translatorHelper.getAggregateIndex();
			
			/*
			 * 2.3 Fill aggregateName
			 */
			aggregateName = aggName;
			/*
			 * add group by
			 */
			if(groupByClauseList != null)
			{
				for(int i = 0; i < groupByClauseList.size(); i++)
				{
					ColumnExpression temp = groupByClauseList.get(i);
					/*
					 * 2.4.1 Fill groupby list
					 */
					groupbyList.add(temp.toString());
				}
			}
			
			/*
			 * added group by from parent
			 */
			for(int i = 0; i < addGroupbyColumnList.size(); i++)
			{
				AllFromTable tempAllFromTable = addGroupbyColumnList.get(i);
				TypeChecker tempChecker = nestedStatement.getTableCheckerMap().get(tempAllFromTable);
				ArrayList<ColumnExpression> tempList = getColumnFromAllFromTable(tempChecker,
														tempAllFromTable);
				
				if(tempSeed != null && tempAllFromTable.getTableName().equals(tempSeed.getTableName()))
				{
					tempList.add(new ColumnExpression(null, tempSeed.getSeedAttributeName()));
				}
				
				for(int j = 0; j < tempList.size(); j++)
				{
					ColumnExpression temp = tempList.get(j);
					/*
					 * 2.4.2 Fill groupby list
					 */
					groupbyList.add(temp.toString());
				}
			}
			
			for(int i = 0; i < expList.size(); i++)
			{
				/*
				 * 2.5 Fill aggregateExpressionList
				 */
				mathExpressionTranslator.translateMathExpression(expList.get(i), 
																subResultList, 
																aggregateExpressionList);
			}
			
			for(int i = 0; i < sourceList.size(); i++)
			{
				ArrayList<String> attributeNameList = sourceList.get(i);
				/*
				 * 2.6 Fill columnListMap
				 */
				columnListMap.put(aggregateExpressionList.get(i), copyList(attributeNameList));
			}
			
			for(int i = 0; i < expList.size(); i++)
			{
				MathExpression temp = expList.get(i);
				String name = aggregateNameMap.get(temp);
				/*
				 * translate the temp (sub_MathExpression)
				 */
				
				/*
				 * 2.7 Fill outputMap
				 */
				outputMap.put(aggregateExpressionList.get(i), name);
				/*
				 * After translating this MathExpression, we should put it in the 
				 */
				
				translatorHelper.expressionNameMap.put(temp, name);
			}
			
			/*
			 * 2.8 Create the node
			 */
			Aggregate aggregate = new Aggregate(nodeName,
												children,
												parents,
												aggregateName);
			aggregate.setGroupbyList(groupbyList);
			aggregate.setAggregateExpressionList(aggregateExpressionList);
			aggregate.setColumnListMap(columnListMap);
			aggregate.setOutputMap(outputMap);
			
			/*
			 * 2.9 Add the parent of its children
			 */
			originalElement.addParent(aggregate);
			return aggregate;
		}
		else
		{
			return originalElement;
		}
	}
	
	private ArrayList<String> copyList(ArrayList<String> list)
	{
		ArrayList<String> resultList = new ArrayList<String>();
		for(int i = 0; i < list.size(); i++)
		{
			resultList.add(list.get(i));
		}
		return resultList;
	}
	
	public Operator translateProjection(TypeChecker typechecker, 
												UnnestedSelectStatement nestedStatement, 
												ArrayList<ColumnExpression> addedFromAncesterSelectList, 
												Operator originalElement)throws Exception
	{
		/*
		 * 1. Associated data structure
		 * Find the output attributes
		 */
		SelectStatement selectStatement= nestedStatement.getSelectStatement();
		ArrayList<SQLExpression> selectList = selectStatement.selectList;
		
		ArrayList<String> attributeList = new ArrayList<String>();
		for(int i =0; i < selectList.size(); i++)
		{
			SQLExpression selectElement = selectList.get(i);
			if(selectElement instanceof DerivedColumn)
			{
				String alias = ((DerivedColumn) selectElement).alias;
				attributeList.add(alias);
			}
			else if(selectElement instanceof AsteriskTable)
			{
				ArrayList<ColumnExpression> list = typechecker.getColumnFromAsteriskTable();
				
				for(int j = 0; j < list.size(); j++)
				{
					ColumnExpression columnExpression = list.get(j);
					String alias;
					
					if(typechecker.isAllowDuplicatedAttributeAlias())
						alias = columnExpression.toString();
					else
						alias = columnExpression.columnName;
					
					attributeList.add(alias);
				}
			}
			else if(selectElement instanceof AllFromTable)
			{
				ArrayList<ColumnExpression> list = 
					typechecker.getColumnFromAllFromTable((AllFromTable)selectElement);
				
				for(int j = 0; j < list.size(); j++)
				{
					ColumnExpression columnExpression = list.get(j);
					String alias;
					
					if(typechecker.isAllowDuplicatedAttributeAlias())
						alias = columnExpression.toString();
					else
						alias = columnExpression.columnName;
					
					attributeList.add(alias);
				}
			}
		}
		
		/*
		 * 2. Do the projection
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<String> projectedNameList = new ArrayList<String>();
		
		children.add(originalElement);
		/*
		 * 2. Fill in translatedStatement.
		 */
		for(int i = 0; i < attributeList.size(); i++)
		{
			/*
			 * 3.1 Fill in projectedNameList
			 */
			projectedNameList.add(attributeList.get(i));
		}
		
		ArrayList<String> ansestersStringList = new ArrayList<String>();
		ArrayList<String> selectNameList = copyList(projectedNameList);
		
		if(addedFromAncesterSelectList != null)
		{
			for(int i = 0; i < addedFromAncesterSelectList.size(); i++) //if it is from ancesters that appearns in the current layer
			{
				/*
				 * 3.2 Fill in projectedNameList
				 */
				projectedNameList.add(addedFromAncesterSelectList.get(i).toString());
				ansestersStringList.add(addedFromAncesterSelectList.get(i).toString());
			}
		}
		
		/*
		 * 4. Create the projection
		 */
		Projection projection = new Projection(nodeName, children, parents, projectedNameList);
		
		/*
		 * 5. Add the parent of originalElement
		 */
		originalElement.addParent(projection);
		
		
		/*
		 * 6. if addedSelectList is not empty, then we should add some renaming function here
		 * for supporting temporary view.
		 */
		if(ansestersStringList != null && ansestersStringList.size() != 0)
		{
			/*
			 * We first devide the ansestersStringList to the direct parent list and the others. For the direct parent,
			 * we should rename the attribute list; otherwise, we only keep its name.
			 */
			Operator scalarFunction = sqlExpressionTranslator.addHolderWithoutProjection(ansestersStringList, 
																						 projection, 
																						 nestedStatement.getViewName());
			String viewName = nestedStatement.getViewName();
			for(int i = 0; i < ansestersStringList.size(); i++)
			{
				selectNameList.add(viewName+"_"+ansestersStringList.get(i));
			}
			
			
			nodeName = "node" + translatorHelper.getNodeIndex();
			children = new ArrayList<Operator>();
			parents = new ArrayList<Operator>();
			
			children.add(scalarFunction);
			
			
			projection = new Projection(nodeName, children, parents, selectNameList);
			scalarFunction.addParent(projection);
			
			return projection;
		}
		else
		{
			return projection;
		}
	}
	
	public ArrayList<ColumnExpression> getColumnFromAllFromTable(TypeChecker typeChecker,
			                                                     AllFromTable allFromTable)throws Exception
	{
		/*
		 * Pay attention here: allFromTable may be not exists in the original statement
		 * corresponding to the typechecker...Since there allFromtable is newly added in the 
		 * children view.
		 */
		ArrayList<ColumnExpression> resultList = new ArrayList<ColumnExpression>();
		
		String tableAlias = allFromTable.table;
		HashMap<String, TableReference> tableReferenceMap = typeChecker.getTableReferenceMap();
		
		TableReference tempTableReference = tableReferenceMap.get(tableAlias);
		//if the tableAlias is from a table reference.
		if(tempTableReference != null)
		{
			String tableName = tempTableReference.table;
			
			Catalog catalog = SimsqlCompiler.catalog;
			
			Relation relation = catalog.getRelation(tableName);
			ArrayList<Attribute> tempList;
			View view;
			if(relation != null)
			{
				tempList = relation.getAttributes();
			}
			else
			{
				view = catalog.getView(tableName);
				tempList = view.getAttributes();
			}
			
			for(int k = 0; k < tempList.size(); k++)
			{
				Attribute tempAttribute = tempList.get(k);
				String relationName = tempAttribute.getRelName();
				String attributeName = tempAttribute.getName();
				ColumnExpression column = new ColumnExpression(tableAlias, attributeName);
				resultList.add(column);
			}
		}
		//if the table alias is from a subquery in from clause.
		else
		{
			HashMap<String, TypeChecker> typerCheckerMap = typeChecker.getTyperCheckerMap();
			TypeChecker tempChecker = typerCheckerMap.get(tableAlias);
			ArrayList<String> outputAttributeNameList = tempChecker.getAttributeList();
			
			for(int j = 0; j < outputAttributeNameList.size(); j++)
			{
				String name = outputAttributeNameList.get(j);
				ColumnExpression column = new ColumnExpression(tableAlias, name);
				resultList.add(column);
			}	
		}
		
		return resultList;
	}
	
	public Operator seed(Operator translatedOutTableElment,
			                    String tableAlias)
	{
		/*
		 * 1. The data structure.
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		String seedAttributeName;
		
		/*
		 * 2. Fill in the translatedStatement
		 */
		/*
		 * 2. Fill children
		 */
		children.add(translatedOutTableElment);
		
		/*
		 * 3. Fill seedAttributeName
		 */
		seedAttributeName = tableAlias + "_seed";
		
		/*
		 * 4. Create the Seed
		 */
		Seed seed = new Seed(nodeName, tableAlias, children, parents, seedAttributeName);
		
		/*
		 * 5. Add the parent of OutTable.
		 */
		translatedOutTableElment.addParent(seed);
		
		return seed;
	}
	
	private Operator translateVGFunctionParameter(UnnestedSelectStatement unnestedStatement,
														 Operator outSeedElement)throws Exception
	{
		String viewName = unnestedStatement.getViewName();
		SelectStatement selectStatement = unnestedStatement.getSelectStatement();
		TypeChecker typechecker = unnestedStatement.getTypechecker();
		/*
		 * 1. some data structure for the ancestor
		 */
		//ArrayList<SQLExpression> duplicatedRemovalList = nestedStatement.getDuplicatedRemovalList();
		BooleanPredicate wherePredicate = unnestedStatement.getWherePredicate();
		ArrayList<AllFromTable> addGroupbyColumnList = unnestedStatement.getAddGroupbyColumnList();
		
		/*
		 * 2. some data structure for the ancestor children 
		 */
		ArrayList<UnnestedSelectStatementJoin> childrenView = unnestedStatement.getChildrenView();
		
		
		ArrayList<String> fromList = typechecker.getFromList();
		
		Operator translatedElement;
		/*
		 * 1. Deal with the join result in the result query
		 * 1.1 Dealing with fromList: create n isolated nodes for each From element. 
		 */
		Operator translatorFromElement;
		translatorFromElement = translateFromClause(typechecker, fromList);
		
		/*
		 * 1.2 Deal with the join with the parent, nothing to do here.
		 * We have the outSeedElement
		 */
		ArrayList<SQLExpression> addedTableReference = unnestedStatement.getAddedTableReference();
		
		/*
		 * 1.3. Join the ancesterFromElement and translatorFromElement
		 * Updated in 8/28/2011
		 */
		boolean cartisianProduct;
		if(outSeedElement != null && (addedTableReference != null && addedTableReference.size() > 0))
		{
			translatedElement = join(outSeedElement, translatorFromElement);
			cartisianProduct = false;
		}
		else
		{
			translatedElement = translatorFromElement;
			cartisianProduct = true;
		}
		
		
		/*
		 * 1.4. Deal with the child
		 */
		translatedElement = translateChildView(childrenView, translatedElement); 
		
		/*
		 * 2. Deal with the where clause
		 *    whereExpressionList: the math expression in the aggregate function
		 *    No subquery expression in where clause
		 */
		
		translatedElement = translateBooleanPredicateClause(wherePredicate, translatedElement);
		
		/*
		 * 3. Aggregate function
		 * 	
		 */
		/******************************hacked here for seed ***********************************************/
		Seed tempSeed = (Seed)outSeedElement;
		
		translatedElement = translateAggregateFunction(typechecker, unnestedStatement, addGroupbyColumnList, tempSeed, translatedElement);
		/****************************** end of hack ***********************************************/
		
		
		/*
		 * 4. scalar function
		 */
		translatedElement = translateScalarFunction(typechecker, selectStatement, translatedElement);
		
		/*
		 * 5. projection
		 * Here, we set the addedSelectedElement as null to avoid their input to the VGWrapper.
		 */
		
		/*
		 * =================================================
		 * Do we need to project the seed?
		 * =================================================
		 */
		translatedElement = translateProjection(typechecker, unnestedStatement, null, translatedElement);
		Projection planProjection = (Projection)translatedElement;
		
		/*
		 * 6. having clause
		 */
		BooleanPredicate havingClause = selectStatement.havingClause;
		if(havingClause != null)
			translatedElement = translateBooleanPredicateClause(havingClause, translatedElement);
		
		/*
		 * 7. We can have distinct in this subquery
		 */
		if(selectStatement.setQuantifier == FinalVariable.DISTINCT)
		{
			translatedElement = duplicateRemoval(translatedElement);
			translatedElement = addFinalProjection(translatedElement, planProjection);
		}
		
		/*
		 * 8. rename the result attribute
		 */
		Catalog catalog = SimsqlCompiler.catalog;
		
		View view = catalog.getView(viewName);
		if(view != null)
		{
			// to do.
		}
		
		/*
		 * 9. Cartisisan product with the outerSeed
		 */
		if(cartisianProduct)
		{
			translatedElement = join(outSeedElement, translatedElement);
			translatedElement = translateProjection(typechecker, unnestedStatement, null, translatedElement);
		}
		
		
		return translatedElement;
	}
	
	public Operator translateVGWrapper(Operator originalOperator, 
											   ArrayList<Operator> translatedParaElementList,
											   String outTableAlias,
											   String vgFunctionName)  throws Exception
	{
		/*
		 * 1. Projection the seed from the outTable
		 * Create the node Projection
		 */
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<String> projectedNameList = new ArrayList<String>();
		/*
		 * 1.1 fill tranlateResult
		 */
		/*
		 * 1.2 Fill projectedNameList
		 */
		projectedNameList.add(outTableAlias + "_seed");
		/*
		 * 1.3 Fill children
		 */
		children.add(originalOperator);
		
		Projection projection = new Projection(nodeName, children, parents, projectedNameList);
		
		/*
		 * 1.4 add the parent for the originalElement
		 */
		originalOperator.addParent(projection);
		
		//----------------------------------------------------------
		/*
		 * 2. Translate the VGWrapper
		 */
		/*
		 * 2.1 parent
		 * We need to create the VGWraper node.
		 */
		nodeName = "node" + translatorHelper.getNodeIndex();
		children = new ArrayList<Operator>();
		parents = new ArrayList<Operator>();
		String vgWrapperName;
		//String vgFunctionName (we have in the input parameter)
		String inputSeedAttributeName;
		ArrayList<String> inputAttributeNameList = new ArrayList<String>();
		ArrayList<String> outputAttributeNameList = new ArrayList<String>();
		String outputSeedAttributeName;
		Operator outerRelationOperator;
		
		/*
		 * 2.2 Fill the translatedStatement 
		 */
		children.add(projection);
		
		for(int i = 0; i < translatedParaElementList.size(); i++)
		{
			Operator temp = translatedParaElementList.get(i);
			
			if(temp instanceof Projection)
			{
				ArrayList<String> projectionList = ((Projection) temp).getProjectedNameList();
				for(int j = 0; j < projectionList.size(); j++)
				{
					inputAttributeNameList.add(projectionList.get(j));
				}
			}
			
			children.add(temp);
		}
		
		/*
		 * 2.3 vgWrapper content
		 */
		vgWrapperName = "vgwrapper" + translatorHelper.getVgWrapperIndex();
		
		
		/*
		 * 2.4 Fill in seedName.
		 */
		inputSeedAttributeName = outTableAlias + "_seed";
		
		/*********** Removed by chris: no reason to have _number at the end of the VG function name
		int start = vgFunctionName.lastIndexOf("_");
		String realVGName = vgFunctionName.substring(0, start);
		*********************************************************************************************/

		VGFunction vgFunction = SimsqlCompiler.catalog.getVGFunction(vgFunctionName);
		ArrayList<String> vgOutStringList = getOutAttribteNameList(vgFunction);
		
		
		for(int i = 0; i < vgOutStringList.size(); i++)
		{
			/*
			 * 2.6 Fill outputAttributeNameList
			 */
			outputAttributeNameList.add(vgOutStringList.get(i));
		}
		
		/*
		 * 2.7 Fill outputSeedName
		 */
		outputSeedAttributeName = "seed";
		
		/*
		 * 2.8 outerRelationOperator
		 */
		outerRelationOperator = projection;
		
		VGFunctionStatistics vgFunctionStatistics = new VGFunctionStatistics(vgFunctionName,
																			"null",
																			vgFunction.getBundlesPerTuple(),
																			inputAttributeNameList, 
																			outputAttributeNameList
																			);
		/*
		 * 2.12 add the vgfunction statistics
		 */
		
		
		/*
		 * 2.9 Create the VGWrapper node.
		 */
		
		VGWrapper vgWrapper = new VGWrapper(nodeName,
											children,
											parents,
											vgWrapperName,
											vgFunctionName,
											inputSeedAttributeName,
											outputSeedAttributeName,
											outerRelationOperator,
											vgFunctionStatistics);
		vgWrapper.setInputAttributeNameList(inputAttributeNameList);
		vgWrapper.setOutputAttributeNameList(outputAttributeNameList);
		
		/*
		 * 2.10 Add the parent of its children node
		 */
		projection.addParent(vgWrapper);
		for(int i = 0; i < translatedParaElementList.size(); i++)
		{
			Operator temp = translatedParaElementList.get(i);
			temp.addParent(vgWrapper);
		}
		
		/*
		 * 2.11 return the operator
		 */
		return vgWrapper;
	}
	
	public Operator translateInnerSelectStatement(Operator translatedOutTableElment,
			ArrayList<String> vgTableNameList,
			HashMap<String, Operator> tranlatedVGWrapperMap,
			RandomTableTypeChecker randomTableChecker,
			UnnestedSelectStatement unnestedInnerStatement)throws Exception
	{
		String viewName = unnestedInnerStatement.getViewName();
		SelectStatement selectStatement = unnestedInnerStatement.getSelectStatement();
		TypeChecker typechecker = unnestedInnerStatement.getTypechecker();
		/*
		 * 1. some data structure for the ancestor
		 */
		ArrayList<SQLExpression> addedTableReferenceList = unnestedInnerStatement.getAddedTableReference();
		//ArrayList<SQLExpression> duplicatedRemovalList = nestedStatement.getDuplicatedRemovalList();
		ArrayList<ColumnExpression> addedFromAncesterSelectList = unnestedInnerStatement.getFromParentSelectList();
		
		BooleanPredicate wherePredicate = unnestedInnerStatement.getWherePredicate();
		ArrayList<AllFromTable> addGroupbyColumnList = unnestedInnerStatement.getAddGroupbyColumnList();
		
		/*
		 * 2. some data structure for the ancestor children 
		 */
		ArrayList<UnnestedSelectStatementJoin> childrenView = unnestedInnerStatement.getChildrenView();
		
		
		ArrayList<String> fromList = typechecker.getFromList();
		
		Operator translatedElement;
		/*
		 * 1. Deal with the join result in the result query
		 * 1.1 Dealing with fromList: create n isolated nodes for each From element. 
		 */
		Operator translatorFromElement;
		String outTableName = randomTableChecker.getOutTableName();
		translatorFromElement = translateInnerFromClause(typechecker, translatedOutTableElment, fromList, tranlatedVGWrapperMap, outTableName);
		
		/*
		 * 1.2 Deal with the join with the parent
		 */
		Operator ancesterFromElement;
		ancesterFromElement = translateAncester(addedTableReferenceList, true);
		
		/*
		 * 1.3. Join the ancesterFromElement and translatorFromElement
		 */
		if(ancesterFromElement != null)
		{
			translatedElement = join(translatorFromElement, ancesterFromElement);
		}
		else
		{
			translatedElement = translatorFromElement;
		}
		
		/*======================================================================
		 * 1.4. Deal with the child
		 * If there is child subquery, and the subquery refers to the outer table,
		 * we MAYBE need to provide another seed comparison predicate.??????????
		 * =====================================================================
		 */
		translatedElement = translateChildView(childrenView, translatedElement); 
		
		/*
		 * 2. Deal with the where clause
		 *    whereExpressionList: the math expression in the aggregate function
		 *    No subquery expression in where clause
		 */
		
		/*
		 * 2.1 Do the scalarFunction for where clause
		 */
		
		translatedElement = translateBooleanPredicateClause(wherePredicate, translatedElement);
		
		/*
		 * 3. Aggregate function
		 * 	
		 */
		translatedElement = translateAggregateFunction(typechecker, unnestedInnerStatement, addGroupbyColumnList, (Seed)translatedOutTableElment, translatedElement);
		
		/*
		 * 4. scalar function
		 */
		translatedElement = translateScalarFunction(typechecker, selectStatement, translatedElement);
		
		/*
		 * 5. projection
		 */
		translatedElement = translateProjection(typechecker, 
												unnestedInnerStatement, 
												addedFromAncesterSelectList, 
												translatedElement);
												
		/*
		 * 6. having clause
		 */
		BooleanPredicate havingClause = selectStatement.havingClause;
		if(havingClause != null)
			translatedElement = translateBooleanPredicateClause(havingClause, translatedElement);
		
		/*
		 * 7. split operation
		 */
		
		/*
		 * 8. rename the result attribute
		 */
		Catalog catalog = SimsqlCompiler.catalog;
		
		View view = catalog.getView(viewName);
		if(view != null)
		{
			// to do.
		}
		
		return translatedElement;
	}
	
	
	/*
	 * translate the innerFrom clause
	 */
	public Operator translateInnerFromClause(TypeChecker typechecker, 
													 Operator translatedOutTableElment,
													 ArrayList<String> fromList,
													 HashMap<String, Operator> tranlatedVGWrapperMap,
													 String outTableName)throws Exception
	{
		Catalog catalog = SimsqlCompiler.catalog;
		HashMap<String, TableReference> tableReferenceMap = typechecker.getTableReferenceMap();
		//<table_alias, subquery>
		HashMap<String, FromSubquery> fromSubqueryMap = typechecker.getFromSubqueryMap();
		//<table_alias, typechecker>
		HashMap<String, TypeChecker> typerCheckerMap = typechecker.getTyperCheckerMap();
		HashMap<String, GeneralFunctionExpression> vgFunctionMap = typechecker.getVgFunctionMap();
		ArrayList<Operator> translatedElementList = new ArrayList<Operator>();
		
		Operator translatorElement;
		
		//This set records which TranslatorElement has the seed column
		HashMap<Operator, String> elementSeedMap = new HashMap<Operator, String>();
		
		/*
		 * 1. First join all the VGTable.
		 */
		for(int i = 1; i < fromList.size(); i++)
		{
			String tableAlias = fromList.get(i);
			TableReference tempTableReference = tableReferenceMap.get(tableAlias);
			
			String tableName = tempTableReference.table;
			Relation relation;
			
			if(vgFunctionMap.containsKey(tableName)) //consider the VGFunction
			{
				Operator element = tranlatedVGWrapperMap.get(tableName);
				
				/*
				 * Add the seed holder. Since the seed attribute from the VGWrapper is 
				 * always named "seed". So we should add the alias to be "alias.seed".
				 */
				ArrayList<String> nameList = new ArrayList<String>();
				nameList.add("seed");
				
				if(element instanceof VGWrapper)
				{
					ArrayList<String> outputNameList = ((VGWrapper) element).getOutputAttributeNameList();
					for(int j = 0; j < outputNameList.size(); j++)
					{
						nameList.add(outputNameList.get(j));
					}
				}
				
				Operator tempElement = sqlExpressionTranslator.addHolder(nameList, element, tableAlias);
				
				translatedElementList.add(tempElement);
				elementSeedMap.put(tempElement, tableAlias);
			}
			else
			{
				if(tableReferenceMap.containsKey(tableAlias))
				{
					
					tableName = tempTableReference.table;
					
					relation = catalog.getRelation(tableName);
					View view;
					if(relation != null)
					{
						translatorElement = sqlExpressionTranslator.tableScan(relation, tableAlias);
						translatedElementList.add(translatorElement);
					}
					else if(((TableReference)tempTableReference).getTableInferenceType() == TableReference.CONSTANT_INDEX_TABLE ||
							((TableReference)tempTableReference).getTableInferenceType() == TableReference.GENERAL_INDEX_TABLE)
					{
						view = catalog.getView(tableName);
						translatorElement = sqlExpressionTranslator.indexTableScan(view, tableAlias, (TableReference)tempTableReference);
						translatedElementList.add(translatorElement);
					}
					else
					{
						view = catalog.getView(tableName);
						translatorElement = sqlExpressionTranslator.viewScan(view, tableAlias);
						translatedElementList.add(translatorElement);
					}
				}
				//for subquery
				else if(fromSubqueryMap.containsKey(tableAlias))
				{
					TypeChecker tempFromTypeChecker = typerCheckerMap.get(tableAlias);
					HashMap<TypeChecker, Operator> viewPlanMap = translatorHelper.fromqueryPlanMap;
					
					//if this subquery has been processed
					if(viewPlanMap.containsKey(tempFromTypeChecker))
					{
						translatorElement = viewPlanMap.get(tempFromTypeChecker);
					}
					else
					{
						UnnestedSelectStatement nestedFromSubquery 
										= TemporaryViewFactory.tempTypeCheckerViewMap.get(tempFromTypeChecker);
						translatorElement = translate(nestedFromSubquery);
						viewPlanMap.put(tempFromTypeChecker, translatorElement);
					}
						
					translatedElementList.add(translatorElement);
				}
			}
		}
		
		//2. Join all the tables in the inner statement
		Operator joinResult = translatedElementList.get(0);
		boolean flag = false;
		ColumnExpression left = null, right = null;
		String tableName;
		
		if(elementSeedMap.containsKey(joinResult) && !flag)
		{
			tableName = elementSeedMap.get(joinResult);
			left = new ColumnExpression(tableName, "seed");
			flag = true;
		}
		
		for(int i = 1; i < translatedElementList.size(); i++)
		{
			Operator element = translatedElementList.get(i);
			
			if(elementSeedMap.containsKey(element) && !flag)
			{
				tableName = elementSeedMap.get(element);
				left = new ColumnExpression(tableName, "seed");
				joinResult = join(joinResult, element);
				flag = true;
			}
			else if(elementSeedMap.containsKey(element) && flag)
			{
				tableName = elementSeedMap.get(element);
				right = new ColumnExpression(tableName, "seed");
				
				ComparisonPredicate predicate = 
					new ComparisonPredicate(left, right, FinalVariable.EQUALS);
				joinResult = join(joinResult, element, JOIN, predicate);
			}
			else
			{
				joinResult = join(joinResult, element);
			}
		}
		
		if(!flag) // in this case, the random generated data is not used; in another word, the vgfunciton is not called.
		{
			return joinResult;
		}
		else
		{
			ColumnExpression outerSeedColumn = new ColumnExpression(outTableName, "seed");
			ComparisonPredicate predicate = 
				new ComparisonPredicate(outerSeedColumn, left, FinalVariable.EQUALS);
			//3. Join that with the outer table
			joinResult = join(translatedOutTableElment, joinResult, JOIN, predicate);
			
			return joinResult;
		}
	}
	
	public static ArrayList<String> getOutAttribteNameList(VGFunction function)
	{
		ArrayList<Attribute> outputAtts = function.getOutputAtts();
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < outputAtts.size(); i++)
		{
			Attribute temp = outputAtts.get(i);
			resultList.add(temp.getName());
		}
		
		return resultList;
	}
	
	public static ArrayList<String> getInputAttribteNameList(VGFunction function)
	{
		ArrayList<Attribute> inputAtts = function.getInputAtts();
		ArrayList<String> resultList = new ArrayList<String>();
		
		for(int i = 0; i < inputAtts.size(); i++)
		{
			Attribute temp = inputAtts.get(i);
			resultList.add(temp.getName());
		}
		
		return resultList;
	}
	
	private static boolean isAtomMathExpression(MathExpression expression)
	{
		if(expression instanceof ColumnExpression ||
				expression instanceof DateExpression ||
				expression instanceof NumericExpression ||
				expression instanceof StringExpression||
				expression instanceof GeneralTableIndex)
			return true;
		else
			return false;
	}
	
	private boolean checkBooleanPredicateForScalarFunction(BooleanPredicate booleanPredicate,
																  ArrayList<String> rightList) {

		ArrayList<MathExpression> mathExpressionList = new ArrayList<MathExpression>();
		MathExpressionExtractor.getMathExpressionInBooleanPredicate(
				booleanPredicate, mathExpressionList);

		
		for (int i = 0; i < mathExpressionList.size(); i++) {
			MathExpression mathExpression = mathExpressionList.get(i);

			ArrayList<String> tempExpList = new ArrayList<String>();
			columnExtractor.getColumnInMathExpression(mathExpression,
					tempExpList);
			
			int containedNum = 0;
			for(int j = 0; j < tempExpList.size(); j++)
			{
				if(rightList.contains(tempExpList.get(j)))
				{
					containedNum ++;
				}
			}
			
			if(!(containedNum == 0 ||containedNum ==  tempExpList.size()))
			{
				return false;
			}
		}
		
		return true;
	}
	
	/*
	 * This function puts two scalar function in the both side of join.
	 * Give a Join/SemiJoin/AntiJoin, we should give a scalar function for its join condition.
	 */
	private void getScalarFunctionForBooleanPredicate(BooleanPredicate booleanPredicate, 
															 ArrayList<String> rightList,
															 ArrayList<ScalarFunction> operatorList) throws Exception {
		/*
		 * 1. Create an isolated ScalarFunction node
		 */
		Operator operator = translateBooleanScalarFunction(booleanPredicate, null);
		ScalarFunction scalarFunction = (ScalarFunction) operator;
		
		if(scalarFunction == null)
		{
			return;
		}
		
		ArrayList<MathOperator> scalarExpressionList = scalarFunction.getScalarExpressionList();
		HashMap<MathOperator, ArrayList<String>> columnListMap = scalarFunction.getColumnListMap();
		HashMap<MathOperator, String> outputMap = scalarFunction.getOutputMap();
		
		ArrayList<MathOperator> scalarExpressionList1 = new ArrayList<MathOperator>();
		HashMap<MathOperator, ArrayList<String>> columnListMap1 = new HashMap<MathOperator, ArrayList<String>>();
		HashMap<MathOperator, String> outputMap1 = new HashMap<MathOperator, String>();
		
		ArrayList<MathOperator> scalarExpressionList2 = new ArrayList<MathOperator>();
		HashMap<MathOperator, ArrayList<String>> columnListMap2 = new HashMap<MathOperator, ArrayList<String>>();
		HashMap<MathOperator, String> outputMap2 = new HashMap<MathOperator, String>();
		
		for(int i = 0; i < scalarExpressionList.size(); i++)
		{
			MathOperator tempExpression = scalarExpressionList.get(i);
			ArrayList<String> tempList = columnListMap.get(tempExpression);
			String output = outputMap.get(tempExpression);
			
			int containedNum = 0;
			for(int j = 0; j < tempList.size(); j++)
			{
				if(rightList.contains(tempList.get(j)))
				{
					containedNum ++;
				}
			}
			
			if(containedNum == 0)//None of the attributes belong to right
			{
				scalarExpressionList1.add(tempExpression);
				columnListMap1.put(tempExpression, tempList);
				outputMap1.put(tempExpression, output);
			}
			else //belong to right
			{
				scalarExpressionList2.add(tempExpression);
				columnListMap2.put(tempExpression, tempList);
				outputMap2.put(tempExpression, output);
			}
		}
		
		int nodeIndex = translatorHelper.getNodeIndex();
		String nodeName = "node"+nodeIndex;
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		
		
		ScalarFunction leftScalarFunction = new ScalarFunction(nodeName, children, parents, translatorHelper);
		leftScalarFunction.setScalarExpressionList(scalarExpressionList1);
		leftScalarFunction.setColumnListMap(columnListMap1);
		leftScalarFunction.setOutputMap(outputMap1);
		
		nodeIndex = translatorHelper.getNodeIndex();
		nodeName = "node"+nodeIndex;
		children = new ArrayList<Operator>();
		parents = new ArrayList<Operator>();
		ScalarFunction rightScalarFunction = new ScalarFunction(nodeName, children, parents, translatorHelper);
		rightScalarFunction.setScalarExpressionList(scalarExpressionList2);
		rightScalarFunction.setColumnListMap(columnListMap2);
		rightScalarFunction.setOutputMap(outputMap2);
		
		operatorList.add(leftScalarFunction);
		operatorList.add(rightScalarFunction);
	}

	public Operator addFinalProjection(Operator originalElement, Projection projection)
	{
		String nodeName = "node" + translatorHelper.getNodeIndex();
		ArrayList<Operator> children = new ArrayList<Operator>();
		ArrayList<Operator> parents = new ArrayList<Operator>();
		ArrayList<String> projectedNameList = new ArrayList<String>();
		projectedNameList.addAll(projection.getProjectedNameList());
		children.add(originalElement);
		Projection projection2 = new Projection(nodeName, children, parents, projectedNameList);
		originalElement.addParent(projection2);
		return projection2;
	}
}
