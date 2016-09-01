

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


grammar Query;

options {
	k = 4;
	backtrack = true;
	memoize=true;
}

/*
 * written by Zhuhua Cai
 * version 1
 */

@header {

package simsql.compiler;

import org.antlr.runtime.*;

import java.util.Stack;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;
import simsql.compiler.*;
}

@lexer::header {
package simsql.compiler;
}

@members {
	  private int errorNum = 0;
	  public int getErrorNum()
	  {
	  		return errorNum;
	  }
	  
	  public void reportError(RecognitionException re) {
            super.reportError(re);
            errorNum++;
      }
}



prog returns[ArrayList<Expression> queryList]: statements
		  {
		  	   
		  	   $queryList = $statements.queryList;
		  };
		  
statements returns[ArrayList<Expression> queryList]:
	      q1=statement
	      {
	      	   $queryList = new ArrayList<Expression>();
	      	   $queryList.add($q1.statement);
	      }
	      (
	      SEMICOLON
	      	   q2=statement
	      	   {
	      	   		$queryList.add($q2.statement);
	      	   }
	      )*
	      SEMICOLON?;
	      
statement returns[Expression statement]:
		 selectStatement
         {
         	  $statement = $selectStatement.statement;
         }
         |
         createStatement
		 {
		 	  $statement = $createStatement.statement;
		 }
         |
         dropStatement
         {
         	  $statement = $dropStatement.statement;
         }
         ;
	 
createStatement returns[Expression statement]:
		createRandomTableStatement
	    {
	      	  $statement = $createRandomTableStatement.statement;
	    }
	    |
		createViewStatement
        {
        	  $statement = $createViewStatement.statement;
        }
        |
        createTableStatement
        {
        	  $statement = $createTableStatement.statement;
        }
        |
        createFunctionStatement
        {
        	  $statement = $createFunctionStatement.statement;
        }
        |
        createVGFunctionStatement
        {
        	  $statement = $createVGFunctionStatement.statement;
        }
        |
        createUnionViewStatement
        {
              $statement = $createUnionViewStatement.statement;
        };
         
dropStatement returns[Expression statement]:
		 dropTableStatement
		 {
		 	  $statement = $dropTableStatement.statement;
		 }
		 |
		 dropViewStatement
		 {
		 	  $statement = $dropViewStatement.statement;
		 }
		 |
         dropFunctionStatement
         {
             $statement = $dropFunctionStatement.statement;
         }
         |
		 dropVGFunctionStatement
		 {
		 	  $statement = $dropVGFunctionStatement.statement;
		 };
		 
dropTableStatement returns[Expression statement]:
		DROP TABLE 
		(
		    viewName=IDENTIFIER LBRACKET lb=NUMERIC TWODOT ub=NUMERIC RBRACKET
            {
                  $statement = new DropElement($viewName.text + "_" + $lb.text + ".." + $ub.text, DropElement.ARRAY_CONSTANT_INDEX_TABLE);
            }
            |
            tableName=IDENTIFIER LBRACKET indexName=NUMERIC RBRACKET
			{
			      $statement = new DropElement($tableName.text+"_" + $indexName.text, DropElement.CONSTANT_INDEX_TABLE);
			}
			|
			tableName=IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET
			{
			      $statement = new DropElement($tableName.text+"_i", DropElement.GENERAL_INDEX_TABLE); 
			}
			|
			tableName=IDENTIFIER
	        {
	              $statement = new DropElement($tableName.text, DropElement.TABLEORCOMMON_RANDOM_TABLE);
	        }
	     );
		
dropViewStatement returns[Expression statement]:
		DROP VIEW viewName=IDENTIFIER
		(
			LBRACKET indexName=NUMERIC RBRACKET
            {
                 $statement = new DropElement($viewName.text + "_" +$indexName.text, DropElement.UNION_VIEW);
            }
            |
			LBRACKET GENERALTABLEINDEX RBRACKET
            {
                 $statement = new DropElement($viewName.text + "_i", DropElement.UNION_VIEW);
            }
            |
			{
				  $statement = new DropElement($viewName.text, DropElement.VIEW);
			}
		)
		|
		DROP MATERIALIZED VIEW viewName2=IDENTIFIER
        {
             $statement = new DropElement($viewName2.text, DropElement.TABLEORCOMMON_RANDOM_TABLE);
        }
		;
		
dropVGFunctionStatement returns[Expression statement]:
		DROP VGFUNCTION vgName=IDENTIFIER
		{
			  $statement = new DropElement($vgName.text, DropElement.VGFUNC);	
		};	
		
dropFunctionStatement returns[Expression statement]:
        DROP FUNCTION fgName=IDENTIFIER
        {
              $statement = new DropElement($fgName.text, DropElement.FUNC); 
        };
/*
 * 1. ---------------------------select statement--------------------------------------
*/
			   
selectStatement returns [SelectStatement statement]: 
           SELECT setQuantifier selectList
           fromClause 
           whereClause 
           groupByClause 
           havingClause
           orderByClause
           {
                $statement = new SelectStatement($setQuantifier.setQuantifier, 
				                                     $selectList.selectList,
				                                     $fromClause.tableReferenceList,
				                                     $whereClause.predicate,
				                                     $groupByClause.columnList,
				                                     $havingClause.predicate,
				                                     $orderByClause.columnList);
           };
/*
 *	1.1 ------------------------set quantifier--------------------------------------------------
 */
setQuantifier returns[int setQuantifier]:
            ALL
            {
                 $setQuantifier = FinalVariable.ALL;
            }
            |
            DISTINCT
            {
                  $setQuantifier = FinalVariable.DISTINCT;
            }
            |
            {
                 $setQuantifier = FinalVariable.ALL;
            };

/*
 *	1.2 ------------------------select clause--------------------------------------------------
 */

selectList returns [ArrayList<SQLExpression> selectList]:
			selectVarious
 			{
 			      $selectList = $selectVarious.selectList;
 			};
 
selectVarious returns [ArrayList<SQLExpression> selectList]:
            l1=selectSubList
            {
            	 $selectList = new ArrayList<SQLExpression>();
            	 $selectList.add($l1.vcolumn);
            }
            (COMMA l2=selectSubList
            {
                 $selectList.add($l2.vcolumn);
            })*;
            
selectSubList returns [SQLExpression vcolumn]:
            derivedColumn
            {
                 $vcolumn = $derivedColumn.vcolumn;
            }
            |
            IDENTIFIER DOT ASTERISK
            {
                 $vcolumn = new AllFromTable($IDENTIFIER.text);
            }
            |
            ASTERISK
 			{
 				  $vcolumn = new AsteriskTable(); 		      
 			};
 
derivedColumn returns [SQLExpression vcolumn]:
            valueExpression
            (
	           	AS? IDENTIFIER
	            {
	                 $vcolumn = new DerivedColumn($valueExpression.expression, $IDENTIFIER.text);
	            }
	            |
	            {
	           	     $vcolumn = new DerivedColumn($valueExpression.expression, $valueExpression.text);
	           	}
	        )
	        |
	        IDENTIFIER EQUALS caseExpression
	        {
	        	$vcolumn = new DerivedColumn($caseExpression.expression, $IDENTIFIER.text);
	        }
	        |
	        caseExpression
	        {
	        	$vcolumn = new DerivedColumn($caseExpression.expression, $caseExpression.text);
	        } 
	        ;
            

/*
 *	1.3 ------------------------from clause--------------------------------------------------
 */            
fromClause returns [ArrayList<SQLExpression> tableReferenceList]:
			FROM tableReferenceList
			{
				$tableReferenceList =  $tableReferenceList.tableReferenceList;
			}
			|
			{
				$tableReferenceList = new ArrayList<SQLExpression>();
			};
            
tableReferenceList returns [ArrayList<SQLExpression> tableReferenceList]:
            tr1=tableReference
            {
            	 $tableReferenceList = new ArrayList<SQLExpression>();
                 $tableReferenceList.add($tr1.expression);
            }
            (COMMA tr2=tableReference
            {
                 $tableReferenceList.add($tr2.expression);
            })*;
            
tableReference returns [SQLExpression expression]:
            IDENTIFIER
            {
                 $expression = new TableReference($IDENTIFIER.text);
            }
            |
            tableName=IDENTIFIER AS? aliasName=IDENTIFIER
            {
                 $expression = new TableReference($tableName.text, $aliasName.text);
            }
            |
            /* -------------------------For Simulation------------------------
            */
            indexTableReference AS? aliasName=IDENTIFIER
            {
                 $expression = new TableReference($indexTableReference.table,
		                  $aliasName.text,
		                  $indexTableReference.index, 
		                  $indexTableReference.type, 
		                  $indexTableReference.indexMathExp);
            }
            |
            indexTableReference
            {
	            $expression = new TableReference($indexTableReference.table, 
		                 $indexTableReference.table, 
		                 $indexTableReference.index, 
		                 $indexTableReference.type,
		                 $indexTableReference.indexMathExp);
            }
            /* ---------------------------------end------------------------
            */
            |
            subquery
            (
            	AS aliasName=IDENTIFIER
            	{
            		$expression = new FromSubquery($subquery.expression, $IDENTIFIER.text);
            	}
            )
            |
            tempTable
            {
            	ValuesTable valuesTable = $tempTable.valuesTable;
            	boolean typeCheck = ValuesTableHelper.visitValuesTable(valuesTable);
            	if(!typeCheck)
            	{
            		System.err.println("ValuesTable defintion for \r\n" + valuesTable.toString() + " \r\n wrong!");
            		
            	}
            	
            	String valueTableName = ValuesTableHelper.getValuesTableName($tempTable.text);
            	$expression = new TableReference(valueTableName);
            	ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
            }
            |
            tempTable AS? aliasName = IDENTIFIER
            {
            	ValuesTable valuesTable = $tempTable.valuesTable;
            	boolean typeCheck = ValuesTableHelper.visitValuesTable(valuesTable);
            	if(!typeCheck)
            	{
            		System.err.println("ValuesTable defintion for \r\n" + valuesTable.toString() + " \r\n wrong!");
            		reportError(new RecognitionException());
            	}
            	
            	String valueTableName = ValuesTableHelper.getValuesTableName($tempTable.text);
            	$expression = new TableReference(valueTableName, $IDENTIFIER.text);
            	ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
            };
            
indexTableReference returns [String table, String index, int type, MathExpression indexMathExp]:
            tableName=IDENTIFIER LBRACKET indexString=NUMERIC RBRACKET
            {
                  $table = $tableName.text+"_"+$indexString.text;
                  $index = $indexString.text;
                  $type = TableReference.CONSTANT_INDEX_TABLE;
                  $indexMathExp = null;
            }
            |
            tableName=IDENTIFIER LBRACKET valueExpression RBRACKET
            {
                   $table = $tableName.text + "_i";
                   $index = null;
                   $type = TableReference.GENERAL_INDEX_TABLE;
                   $indexMathExp =  $valueExpression.expression;
            };            

tempTable returns [ValuesTable valuesTable]:
		    VALUES 
		    (
		    	 tempTableRow
		    	 {
			    	 ArrayList<MathExpression> tempTableRow = $tempTableRow.tempTableRow;
			    	 ArrayList<ArrayList<MathExpression>> tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
			    	 tempTableRowList.add(tempTableRow);
			     	 $valuesTable = new ValuesTable(tempTableRowList);
		    	 }
		    	 |
			     tempTableRowList
			     {
			    	 $valuesTable = new ValuesTable($tempTableRowList.tempTableRowList);
			    	
			     }
		    )
		    ;
		    
tempTableRowList returns [ArrayList<ArrayList<MathExpression>> tempTableRowList]:
			LPAREN
			tr1 = tempTableRow
            {
                 $tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
                 $tempTableRowList.add($tr1.tempTableRow);
            }
            (COMMA tr2 = tempTableRow
            {
                 $tempTableRowList.add($tr2.tempTableRow);
            })*
            RPAREN;
            
tempTableRow returns[ArrayList<MathExpression> tempTableRow]:
		    LPAREN
			tr1 = constantValue
			{
			    $tempTableRow = new ArrayList<MathExpression>();
				$tempTableRow.add($tr1.expression);
			}
			(COMMA tr2 = constantValue
            {
                 $tempTableRow.add($tr2.expression);
            })*
            RPAREN;
		    
constantValue returns [MathExpression expression]:
	        STRING
            {
                 $expression = new StringExpression($STRING.text);
            }
            |
            signedNumeric
            { 
                 $expression = $signedNumeric.expression;
            };          
/*
 *	1.4 ------------------------where clause--------------------------------------------------
 */  
            
whereClause returns [BooleanPredicate predicate]:
            WHERE searchCondition
            {
                 $predicate = $searchCondition.predicate;
            }
            |
            {
                 $predicate = null;
            };
            
/*
 *	1.5 ------------------------groupby clause--------------------------------------------------
 */  
             
groupByClause returns [ArrayList<ColumnExpression> columnList]:
            GROUP BY groupingColumns
            {
                 $columnList = $groupingColumns.columnList;
            }
            |
            {
                 $columnList = null;
            }
            ;
            
groupingColumns returns [ArrayList<ColumnExpression> columnList]:
            ce1=columnExpression
            {
            	 $columnList = new ArrayList<ColumnExpression>();
                 $columnList.add((ColumnExpression)$ce1.expression);
            }
            (COMMA ce2=columnExpression
            {
                 $columnList.add((ColumnExpression)$ce2.expression);
            })*;

/*
 *	1.6 ------------------------groupby clause--------------------------------------------------
 */ 
             
orderByClause returns[ArrayList<OrderByColumn> columnList]:
			ORDER BY orderByColumns
			{
				$columnList = $orderByColumns.columnList;
			}
			|
			{
				$columnList = null;
			};

orderByColumns returns [ArrayList<OrderByColumn> columnList]:
			{
				
			}
			ve1=valueExpression (
				{
					 $columnList = new ArrayList<OrderByColumn>();
	                 $columnList.add(new OrderByColumn($ve1.expression, FinalVariable.ASC));
	            }|
	            ASC
	            {
	            	 $columnList = new ArrayList<OrderByColumn>();
	                 $columnList.add(new OrderByColumn($ve1.expression, FinalVariable.ASC));
	            }|
	            DESC
	            {
	            	 $columnList = new ArrayList<OrderByColumn>();
	                 $columnList.add(new OrderByColumn($ve1.expression, FinalVariable.DESC));
	            })
	        (COMMA ve2=valueExpression (
	        	{
	                 $columnList.add(new OrderByColumn($ve2.expression, FinalVariable.ASC));
	            }|
	            ASC
	            {
	                 $columnList.add(new OrderByColumn($ve2.expression, FinalVariable.ASC));
	            }|
	            DESC
	            {
	                 $columnList.add(new OrderByColumn($ve2.expression, FinalVariable.DESC));
	            }))*;           

/*
 *	1.7 ------------------------orderby clause--------------------------------------------------
 */ 
 
havingClause returns[BooleanPredicate predicate]:
			{
                 $predicate = null;
            }
            |
            HAVING searchCondition
            {
                 $predicate = $searchCondition.predicate;
            }; 
			
            
		

subquery returns [MathExpression expression]:
			LPAREN
			selectStatement
			{
				$expression = new SubqueryExpression($selectStatement.statement);
			}
			RPAREN;

/*
 *  2 ----------------------------This section describes the extended MDCB query-------------------------------
 */

//random statement for MCDB2
createRandomTableStatement returns[RandomTableStatement statement]: 
          CREATE TABLE 
	      (
	          nonIndexRandomStatement
	          {
	            $statement = $nonIndexRandomStatement.statement;
	          }
	          /* -------------------------For Simulation------------------------
	            */      
	          |
	          baselineArrayRandomStatement
	          {
	            $statement = $baselineArrayRandomStatement.statement;
	          }
	          |
	          baselineRandomStatement
	          {
	            $statement = $baselineRandomStatement.statement;
	          }
	          |
	          generalRandomStatement
	          {
	            $statement = $generalRandomStatement.statement;
	          }
	          |
              multidimensionalStatement
              {
                $statement = $multidimensionalStatement.statement;
              }
	      )
          /*------------------------------end-------------------------------
          */
          ;
					
nonIndexRandomStatement returns[RandomTableStatement statement]: 
          schema AS
          randomParameters
          {
            $statement = new RandomTableStatement($schema.schema,
                               $randomParameters.tableReference,
                               $randomParameters.withStatementList,
                               $randomParameters.statement);
          };

/* -------------------------For Simulation-------------------------------------
*/      
                   
baselineRandomStatement returns[RandomTableStatement statement]:
          baselineSchema AS
          randomParameters
          {
            $statement = new BaseLineRandomTableStatement($baselineSchema.index,
                               $baselineSchema.schema,
                               $randomParameters.tableReference,
                               $randomParameters.withStatementList,
                               $randomParameters.statement);
          };
          
baselineArrayRandomStatement returns[RandomTableStatement statement]:
           baselineArraySchema AS
           randomParameters
           {
              $statement = new BaseLineArrayRandomTableStatement($baselineArraySchema.arrayString,
                               $baselineArraySchema.schema,
                               $randomParameters.tableReference,
                               $randomParameters.withStatementList,
                               $randomParameters.statement);
           };
          
generalRandomStatement returns[RandomTableStatement statement]:
          generalSchema AS
          randomParameters
          {
            $statement = new GeneralRandomTableStatement($generalSchema.schema,
                               $randomParameters.tableReference,
                               $randomParameters.withStatementList,
                               $randomParameters.statement);
          };

multidimensionalStatement returns[MultidimensionalTableStatement statement]:
          multidimensionalSchema AS
          randomParameters
          {
            $statement = new MultidimensionalTableStatement($multidimensionalSchema.schema,
                               $randomParameters.tableReference,
                               $randomParameters.withStatementList,
                               $randomParameters.statement);
          };
          
baselineSchema returns[DefinedTableSchema schema, String index]: 
      schemaName LBRACKET NUMERIC RBRACKET
      (
        LPAREN
        attributeList
        RPAREN
        {
          $index = $NUMERIC.text;
          $schema = new DefinedTableSchema($schemaName.name+"_"+$index, $attributeList.attributeList, true);
        }
        |
        {
          $index = $NUMERIC.text;
          $schema = new DefinedTableSchema($schemaName.name+"_"+$index, true);
        }
      )
      ;
      
baselineArraySchema returns[DefinedArrayTableSchema schema, String arrayString]: 
      schemaName LBRACKET GENERALTABLEINDEX COLON lb=NUMERIC TWODOT ub=NUMERIC RBRACKET
      (
        LPAREN
        attributeList
        RPAREN
        {
          $arrayString = $lb.text+".."+$ub.text;
          $schema = new DefinedArrayTableSchema($schemaName.name, 
                                                $lb.text+".."+$ub.text, 
                                                $attributeList.attributeList);
        }
        |
        {
          $arrayString = $lb.text+".."+$ub.text;
          $schema = new DefinedArrayTableSchema($schemaName.name, 
                                                $lb.text+".."+$ub.text);
        }
      )
      ;
      
generalSchema returns[DefinedTableSchema schema]: 
      schemaName LBRACKET GENERALTABLEINDEX RBRACKET
      (
        LPAREN
        attributeList
        RPAREN
        {
          $schema = new DefinedTableSchema($schemaName.name+"_i", $attributeList.attributeList, true);
        }
        |
        {
          $schema = new DefinedTableSchema($schemaName.name+"_i", true);
        }
      )
      ;

multidimensionalSchema returns[MultidimensionalTableSchema schema]:
      schemaName ids=multidimensionalSchemaIndices
      (
        LPAREN
        attributeList
        RPAREN
        {
          $schema = new MultidimensionalTableSchema($schemaName.name, $ids.indices, $attributeList.attributeList, true);
        }
        |
        {
          $schema = new MultidimensionalTableSchema($schemaName.name, $ids.indices, true);
        }
      )
      ;

multidimensionalSchemaIndices returns[MultidimensionalSchemaIndices indices]:
    LBRACKET idx=GENERALTABLEINDEX COLON spec=multidimensionalSchemaIndexSpecification RBRACKET
    {
        $indices = new MultidimensionalSchemaIndices();
        $indices.add($idx.text, $spec.index);
    }
    (
        LBRACKET idx=GENERALTABLEINDEX COLON spec=multidimensionalSchemaIndexSpecification RBRACKET
        {
            $indices.add($idx.text, $spec.index);
        }
    )+
    ;

multidimensionalSchemaIndexSpecification returns[MultidimensionalSchemaIndexSpecification index]:
    rb=NUMERIC TREEDOT lb=NUMERIC
    {
        $index = new MultidimensionalSchemaIndexSpecification($rb.text, $lb.text);
    }
    |
    rb=NUMERIC TREEDOT
    {
        $index = new MultidimensionalSchemaIndexSpecification($rb.text, true);
    }
    |
    rb=NUMERIC
    {
        $index = new MultidimensionalSchemaIndexSpecification($rb.text, false);
    }
    ;

/* ---------------------------------------end----------------------------------------
*/
					
/*
 * WHen the Forsentense is empty, we assume that there is a value table in the sentense
 * that help creating the seed.
 */
randomParameters returns[SQLExpression tableReference,
                         ArrayList<WithStatement> withStatementList,
                         SelectStatement statement]:
          FOR forsentense               
          withStatements
          selectStatement 
          {
                $tableReference = $forsentense.tableReference;
                $withStatementList = $withStatements.withStatementList;
                $statement = $selectStatement.statement;
          }
          |
          withStatements
          selectStatement
          {
                ArrayList<MathExpression> tempTableRow = new ArrayList<MathExpression>();
                tempTableRow.add(new NumericExpression("0"));
                ArrayList<ArrayList<MathExpression>> tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
                tempTableRowList.add(tempTableRow);
                ValuesTable valuesTable = new ValuesTable(tempTableRowList);
             
                String valueTableName = ValuesTableHelper.getValuesTableName();
	              $tableReference = new TableReference(valueTableName, valueTableName);
	              ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
	              
	              $withStatementList = $withStatements.withStatementList;
                $statement = $selectStatement.statement;
          };		
/*
 * We add two different 
 */	
					
forsentense returns [SQLExpression tableReference]: 
			EACH tupleName IN tableReference
			{
				tableReference = $tableReference.expression;
				if(tableReference instanceof TableReference)
			    {
					 ((TableReference)tableReference).setAlias($tupleName.name);
			    }
			    else if(tableReference instanceof FromSubquery)
			    {
			   		  ((FromSubquery)tableReference).setAlias($tupleName.name);
			    }
				   
			};
	        	
schema returns[DefinedTableSchema schema]: 
			schemaName
			(
				LPAREN
				attributeList
				RPAREN
				{
					$schema = new DefinedTableSchema($schemaName.name, $attributeList.attributeList, false);
				}
				|
				{
					$schema = new DefinedTableSchema($schemaName.name, false);
				}
			)
			;

schemaName returns[String name]: 
			IDENTIFIER
			{
			    $name = $IDENTIFIER.text;
			};
			
attributeList returns[ArrayList<String> attributeList]:
			at1 = attribute
			{
				$attributeList = new ArrayList<String>();
				$attributeList.add($at1.name);
			}
			(COMMA at2 = attribute
				{
					$attributeList.add($at2.name);
				}
			)*;

attribute returns[String name]:
			IDENTIFIER
			{
				$name = $IDENTIFIER.text;
			};
       
tupleName returns[String name]: 
			IDENTIFIER
			{
				$name = $IDENTIFIER.text;
			};

withStatements returns[ArrayList<WithStatement> withStatementList]:
			{
				$withStatementList = new ArrayList<WithStatement>();
			}
			(
			withStatement
			{
				$withStatementList.add($withStatement.statement);
			}
			)*;

withStatement returns[WithStatement statement]:
			WITH tempVGTable AS vgFunctionStatement
			{
				$statement = new WithStatement($vgFunctionStatement.expression, $tempVGTable.name);
			};

tempVGTable returns[String name]:
			IDENTIFIER
			{
				$name = $IDENTIFIER.text;
			};
			

vgFunctionStatement returns[GeneralFunctionExpression expression]: 
			generalFunction
			{
				// remember that this corresponds to a VG function call
				$expression = $generalFunction.expression;
				$expression.setVGFunctionCall (true);
			};			
			
/*
 * 8 --------------This section describes the table definition for catalog---------------------------
 */

createViewStatement returns[Expression statement]:
            CREATE VIEW schema AS
            selectStatement
            {
            	$statement = new ViewStatement($schema.schema, $selectStatement.statement);
            }
            |
            CREATE MATERIALIZED VIEW schema AS
            selectStatement
            {
                $statement = new MaterializedViewStatement($schema.schema, $selectStatement.statement);
            }
            ;


         
createTableStatement returns[TableDefinitionStatement statement]:
			CREATE TABLE definedTableName table_contents_source
			{
				$statement = new TableDefinitionStatement($definedTableName.name, 
				                         $table_contents_source.attributeList);
			};
			
createVGFunctionStatement returns[VGFunctionDefinitionStatement statement]:
			CREATE VGFUNCTION i1=IDENTIFIER functionInAtts RETURNS vgFunctionOutAtts SOURCE file
			{
				$statement = new VGFunctionDefinitionStatement($i1.text, $functionInAtts.attributeList,
						$vgFunctionOutAtts.attributeList, $file.fName);
			};

createFunctionStatement returns[FunctionDefinitionStatement statement]:
			CREATE FUNCTION i1=IDENTIFIER functionInAtts RETURNS returnType SOURCE file
			{
				$statement = new FunctionDefinitionStatement($i1.text, $functionInAtts.attributeList,
					$returnType.typeInfo, $file.fName);
			};

createUnionViewStatement returns[UnionViewStatement statement]:
            commonUnionViewStatement
            {
                $statement = $commonUnionViewStatement.statement;
            }
            |
            baselineUnionViewStatement
            {
                $statement = $baselineUnionViewStatement.statement;
            }
            |
            generalUnionViewStatement
            {
                $statement = $generalUnionViewStatement.statement;
            };
            
commonUnionViewStatement returns[ConstantUnionViewStatement statement]:
             CREATE VIEW schema AS UNION
             tableNameList
             {
                 $statement = new ConstantUnionViewStatement($schema.schema, 
                                                           $tableNameList.tableNameList);
             };
             
baselineUnionViewStatement returns[BaselineUnionViewStatement statement]:
             CREATE VIEW baselineSchema AS UNION
             tableNameList
             {
                 $statement = new BaselineUnionViewStatement($baselineSchema.schema, 
										                 $tableNameList.tableNameList, 
										                 $baselineSchema.index);
             };
             
generalUnionViewStatement returns[GeneralUnionViewStatement statement]:
             CREATE VIEW generalSchema AS UNION
             tableNameList
             {
                 $statement = new GeneralUnionViewStatement($generalSchema.schema, 
										                 $tableNameList.tableNameList);
             };
             
tableNameList returns[ArrayList<SQLExpression> tableNameList]:
            tr1=uniontableName
            {
                 $tableNameList = new ArrayList<SQLExpression>();
                 $tableNameList.add($tr1.table);
            }
            (COMMA tr2=uniontableName
            {
                 $tableNameList.add($tr2.table);
            })*;
            
uniontableName returns[SQLExpression table]:
            commonTableName=IDENTIFIER
            {
                $table = new CommonTableName($commonTableName.text);
            }
            |
            baselineTableName=IDENTIFIER LBRACKET NUMERIC RBRACKET
            {
                $table = new BaselineTableName($baselineTableName.text, $NUMERIC.text);
            }
            |
            generalTableName=IDENTIFIER LBRACKET valueExpression RBRACKET
            {
                $table = new GeneralTableName($generalTableName.text, $valueExpression.expression);
            }
            |
            baselineTableNameArray=IDENTIFIER LBRACKET lb=NUMERIC TWODOT ub=NUMERIC RBRACKET
            {
                $table = new BaselineTableNameArray($baselineTableNameArray.text, 
                                                    $lb.text + ".." + $ub.text);
            }
            |
            generalTableNameArray=IDENTIFIER LBRACKET generalLowerBound=valueExpression TWODOT generalUpBound=valueExpression RBRACKET
            {
                $table = new GeneralTableNameArray($generalTableNameArray.text, 
                                                    $generalLowerBound.expression,
                                                    $generalUpBound.expression);
            };
            

file returns[String fName]:
			IDENTIFIER
			{
				return $IDENTIFIER.text;
			}
			|
			STRING
			{
				return $STRING.text;
			}	
			;

functionInAtts returns[ArrayList<SQLExpression> attributeList]:
			LPAREN
			a1=attributeDefineElement
			{
				$attributeList = new ArrayList<SQLExpression>();
				((AttributeDefineElement) $a1.attributeElement).setInput (true);
				$attributeList.add($a1.attributeElement);
			}
                        (COMMA
			a2=attributeDefineElement
			{
				((AttributeDefineElement) $a2.attributeElement).setInput (true);
				$attributeList.add($a2.attributeElement);
			})*
			RPAREN;
				
vgFunctionOutAtts returns[ArrayList<SQLExpression> attributeList]:
			LPAREN
			a1=attributeDefineElement
			{
				$attributeList = new ArrayList<SQLExpression>();
				((AttributeDefineElement) $a1.attributeElement).setInput (false);
				$attributeList.add($a1.attributeElement);
			}
                        (COMMA
			a2=attributeDefineElement
			{
				((AttributeDefineElement) $a2.attributeElement).setInput (false);
				$attributeList.add($a2.attributeElement);
			}
			)*
			RPAREN;
			

returnType returns [TypeInfo typeInfo]:
			type 
			{
				typeInfo = new TypeInfo ($type.dataType);
			}
			;	

definedTableName returns[String name]:
			IDENTIFIER
			{
				return $IDENTIFIER.text;
			};
			
table_contents_source returns[ArrayList<SQLExpression> attributeList]:
			LPAREN
			a1=attributeElement
			{
				$attributeList = new ArrayList<SQLExpression>();
	      	    $attributeList.add($a1.attributeElement);
			}
			(COMMA
			a2=attributeElement
			{
				$attributeList.add($a2.attributeElement);
			})*
			RPAREN;	
			
attributeElement returns[SQLExpression attributeElement]:
			attributeDefineElement
			{
				$attributeElement = $attributeDefineElement.attributeElement;
			}
			|
			attributeConstraintElement
			{
				$attributeElement = $attributeConstraintElement.attributeElement;
			}
			;
			
attributeDefineElement returns[SQLExpression attributeElement]:		
			name=IDENTIFIER  
			dataType=type
			{
				$attributeElement = new AttributeDefineElement($name.text, $dataType.text);
			};
			
attributeConstraintElement returns[SQLExpression attributeElement]:
			primaryKeyStatement
			{
				$attributeElement = $primaryKeyStatement.primaryKeyElement;
			}
			|
			foreignKeyStatement
			{
				$attributeElement = $foreignKeyStatement.foreignKeyElement;
			};
			
primaryKeyStatement returns[PrimaryKeyElement primaryKeyElement]:
			PRIMARY KEY 
			LPAREN
			i1=IDENTIFIER
			{
				$primaryKeyElement = new PrimaryKeyElement();
				$primaryKeyElement.addKey($i1.text);
			}
			(
				COMMA
			 	i2=IDENTIFIER
			 	{
			 		$primaryKeyElement.addKey($i2.text);
			 	}
			)*
			RPAREN;
			
foreignKeyStatement returns[ForeignKeyElement foreignKeyElement]:
			FOREIGN KEY
			LPAREN
			i1=IDENTIFIER
			RPAREN
			REFERENCES
			i2=IDENTIFIER
			LPAREN
			i3=IDENTIFIER
			RPAREN
			{
				$foreignKeyElement = new ForeignKeyElement($i1.text,
												   $i2.text,
												   $i3.text);
			};
			
type returns [String dataType]:
      IDENTIFIER  
      {
        $dataType = $IDENTIFIER.text;
      }
      (
        LBRACKET
        {
          $dataType = $dataType + $LBRACKET.text;
        }
        (
        length
        {
          $dataType = $dataType + $length.length;
        }
        )?
        RBRACKET
        {
          $dataType = $dataType + $RBRACKET.text;
        }
      )*
      (
        RANDOM
        {
          $dataType = $dataType + " " + $RANDOM.text;
        }
      )?
      ;
     
length returns [String length]:
			IDENTIFIER
			{
			  $length = $IDENTIFIER.text;
			}
			| 
			NUMERIC
			{
			  if($NUMERIC.text.indexOf(".") >= 0)
			  {
            		    System.err.println("The length of a string, vector or matrix is a double!");
            		    reportError(new RecognitionException());
            		  }
			  $length = $NUMERIC.text;
			}
			;
			
			
			
/*
 * 4 --------------This section describes the mathimatical expression---------------------------
 */
	            
valueExpression returns [MathExpression expression]:
			me1 = mul_expr
			{
				ArrayList<MathExpression> operandList = new ArrayList<MathExpression>();
				ArrayList<Integer> operatorList = new ArrayList<Integer>();
				$expression = new ArithmeticExpression(operandList, operatorList);
				operandList.add($me1.expression);
			}
			(
				(PLUS
				{
					((ArithmeticExpression)$expression).addOperator(FinalVariable.PLUS);
				}
				| 
				MINUS
				{
					((ArithmeticExpression)$expression).addOperator(FinalVariable.MINUS);
				})
				me2 = mul_expr
				{
					((ArithmeticExpression)$expression).addMathExpression($me2.expression);
				}
			)*;
			
caseExpression returns [MathExpression expression]:
			(
				CASE v1 = valueExpression 
				{
					ArrayList<MathExpression> parasList = new ArrayList<MathExpression>();
					parasList.add($v1.expression);
					$expression = new GeneralFunctionExpression("case", parasList);
				}
				(
					WHEN v2 = valueExpression THEN v3 = valueExpression
				 	{
				 		((GeneralFunctionExpression)$expression).addParameter($v2.expression);
				 		((GeneralFunctionExpression)$expression).addParameter($v3.expression);
				 	}
				 )+
				ELSE v4 = valueExpression
				{
					((GeneralFunctionExpression)$expression).addParameter($v4.expression);
				}
			)
			|
			(
				CASE
				{
					 ArrayList<MathExpression> parasList = new ArrayList<MathExpression>();
					 $expression = new GeneralFunctionExpression("case", parasList);
				}
				(
					 WHEN searchCondition THEN v3 = valueExpression
				 	 {
				 		 ((GeneralFunctionExpression)$expression).addParameter(new PredicateWrapper($searchCondition.predicate));
				 		 ((GeneralFunctionExpression)$expression).addParameter($v3.expression);
				 	 }
				 )+
				 ELSE v4 = valueExpression
				 {
					 ((GeneralFunctionExpression)$expression).addParameter($v4.expression);
				 }
			)
			;
			
			
            
mul_expr returns [MathExpression expression]:
           	ve1 = valueExpressionPrimary
			{
				ArrayList<MathExpression> operandList = new ArrayList<MathExpression>();
				ArrayList<Integer> operatorList = new ArrayList<Integer>();
				$expression = new ArithmeticExpression(operandList, operatorList);
				operandList.add($ve1.expression);
			}
			(
				(ASTERISK
				{
					((ArithmeticExpression)$expression).addOperator(FinalVariable.TIMES);
				}
				| 
				SLASH
				{
					((ArithmeticExpression)$expression).addOperator(FinalVariable.DIVIDE);
				})
				ve2 = valueExpressionPrimary
				{
					((ArithmeticExpression)$expression).addMathExpression($ve2.expression);
				}
			)*;
 
valueExpressionPrimary returns [MathExpression expression]:
            GENERALTABLEINDEX
            {
                $expression = new GeneralTableIndex();
            }
            |
            signedNumeric
            { 
                $expression = $signedNumeric.expression;
            }
            | 
            STRING
            {
                 $expression = new StringExpression($STRING.text);
            }
            |
           	setFunction
            {
                 $expression = $setFunction.expression;
            }
            |
            setExpression
            {
            	$expression = $setExpression.expression;
           	}
            |
            generalFunction
            {
            	$expression = $generalFunction.expression;
            }
            | 
            LPAREN valueExpression RPAREN
            {
                 $expression = $valueExpression.expression;
            }
            | 
            columnExpression
            {
                 $expression = $columnExpression.expression;
            };

signedNumeric returns [MathExpression expression]:
            NUMERIC
            {
            	$expression = new NumericExpression($NUMERIC.text);
            }
            |
            PLUS NUMERIC
            {
            	$expression = new NumericExpression($NUMERIC.text);
            }
            |
            MINUS NUMERIC
            {
            	$expression = new NumericExpression("-"+$NUMERIC.text);
            } ;
            
setFunction returns [MathExpression expression]:
            AVG LPAREN setQuantifier valueExpression RPAREN
            {
                 $expression = new AggregateExpression(FinalVariable.AVG,
                                                      $setQuantifier.setQuantifier,
                                                      $valueExpression.expression);
            }
            |
            SUM LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.SUM,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);  
            }
            |
            COUNT LPAREN 
            (
            	setQuantifier valueExpression RPAREN
            	{
                	 $expression = new AggregateExpression(FinalVariable.COUNT,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression); 
            	}
            	|
            	ALL? ASTERISK RPAREN
            	{
            		 AsteriskExpression asteriskExpression = new AsteriskExpression();
                	 $expression = new AggregateExpression(FinalVariable.COUNT,
                	                                      FinalVariable.ALL,
                	                                      asteriskExpression); 
            	}
            )
            |
            MIN LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.MIN,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            MAX LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.MAX,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            VAR LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.VARIANCE,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            STDEV LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.STDEV,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            VECTOR LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.VECTOR,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            ROWMATRIX LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.ROWMATRIX,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            }
            |
            COLMATRIX LPAREN setQuantifier valueExpression RPAREN
            {
                $expression = new AggregateExpression(FinalVariable.COLMATRIX,
                                                     $setQuantifier.setQuantifier,
                                                     $valueExpression.expression);
            };            
            



columnExpression returns [MathExpression expression]: 
            id2=IDENTIFIER
            {
                 $expression = new ColumnExpression(null, $id2.text);
            }
            |
            table=IDENTIFIER DOT id1=IDENTIFIER
            {
                 $expression = new ColumnExpression($table.text,
                                                   $id1.text);
            };
            

setExpression returns [MathExpression expression]:
			VALUES
			(
				enumerationExpression
				{
					$expression = $enumerationExpression.expression;
				}
			)
			|
			subquery
			{
				$expression = $subquery.expression;
			};
						
enumerationExpression returns[MathExpression expression]:
			LPAREN ve1=valueExpression
			{
				ArrayList<MathExpression> expressionList = new ArrayList<MathExpression>();
				$expression = new SetExpression(expressionList);
				expressionList.add($ve1.expression);
			}
			(
				COMMA ve2=valueExpression
				{
					((SetExpression)$expression).addMathExpression($ve2.expression);
				}
			)* RPAREN;


generalFunction returns[GeneralFunctionExpression expression]:
            functionName
            LPAREN
            (
            	functionParas
            	{
            		$expression = new GeneralFunctionExpression($functionName.name,
            										       $functionParas.paraList);
            	}
            	|
            	selectStatement
				{
					ArrayList<MathExpression> paraList = new ArrayList<MathExpression>();
		    		paraList.add(new SubqueryExpression($selectStatement.statement));
		    		$expression = new GeneralFunctionExpression($functionName.name, paraList);
				}
            )
            RPAREN
            ;
            
functionName returns[String name]:
			IDENTIFIER
			{
				$name = $IDENTIFIER.text;
			};


functionParas returns[ArrayList<MathExpression> paraList]:
				fp1 = functionPara
			    {
			    	$paraList = new ArrayList<MathExpression>();
			    	$paraList.add($fp1.expression);
			    }
			    (
			    	COMMA fp2 = functionPara
			    	{
			    		$paraList.add($fp2.expression);
			    	}
			    )*
			    ;
			    
functionPara returns[MathExpression expression]: 
				valueExpression
				{
					$expression = $valueExpression.expression;
				};           
/*
 * 5 -------------------------------------boolean expresssion ---------------------------
 */ 
 
searchCondition returns [BooleanPredicate predicate]:
			bt1 = booleanTerm
			{
				ArrayList<BooleanPredicate> orList = new ArrayList<BooleanPredicate>();
				$predicate = new OrPredicate(orList); 
				orList.add($bt1.predicate);
			}
			(
				OR bt2 = booleanTerm
				{
					((OrPredicate)$predicate).addPredicate($bt2.predicate);
				}
			)*;
			
booleanTerm returns [BooleanPredicate predicate]:
            bf1 = booleanFactor
			{
				ArrayList<BooleanPredicate> andList = new ArrayList<BooleanPredicate>();
            	$predicate = new AndPredicate(andList);
            	andList.add($bf1.predicate);
			}
			(
				AND bf2 = booleanFactor
				{
					((AndPredicate)$predicate).addPredicate($bf2.predicate);
				}
			)*;
            
booleanFactor returns [BooleanPredicate predicate]:
            booleanPredicate 
            (
            	(EQUALS TRUE | NOTEQUALS FALSE)
           	 	{
           	 		$predicate = $booleanPredicate.predicate;
           	 	}
           	 	|
           	 	(EQUALS FALSE | NOTEQUALS TRUE)
           	 	{
           	 		$predicate = new NotPredicate($booleanPredicate.predicate);
           	 	}
           	 	|
           	 	{
                 	$predicate = $booleanPredicate.predicate;
           	 	}
            )
            |
            NOT booleanPredicate
            {
                 $predicate = new NotPredicate($booleanPredicate.predicate);
            };
            
            
booleanPredicate returns [BooleanPredicate predicate]:
			booleanAtom 
			{
				$predicate = new AtomPredicate($booleanAtom.value);
			}
			|
            LPAREN
            searchCondition
            {
            	$predicate = $searchCondition.predicate;
            }
            RPAREN
            |
			exp1 = valueExpression
			(
				comparisonOperator right=valueExpression
				{
					 $predicate = new ComparisonPredicate($exp1.expression,
                                                         $right.expression,
                                                         $comparisonOperator.op);
				}
				|
				BETWEEN low=valueExpression AND up=valueExpression
				{
                 	 $predicate = new BetweenPredicate($exp1.expression,
                                                      $low.expression,
                                                      $up.expression);
            	}
            	|
            	NOT BETWEEN low=valueExpression AND up=valueExpression
            	{
            		 $predicate = new NotPredicate(
                                    new BetweenPredicate($exp1.expression,
	                                                     $low.expression,
	                                                     $up.expression));
            	}
            	|
            	LIKE pattern=valueExpression
            	{
            		 $predicate = new LikePredicate($exp1.expression,
                                                   $pattern.expression);
            	}
            	|
            	NOT LIKE pattern=valueExpression
            	{
                 	 $predicate = new NotPredicate(new LikePredicate($exp1.expression,
                                                                    $pattern.expression));
            	}
            	|
            	IN set1Expression=setExpression
            	{
            		$predicate = new InPredicate($exp1.expression,
            		                            $set1Expression.expression);
            	}
            	|
            	NOT IN set2Expression=setExpression
            	{
            	    $predicate =  new NotPredicate(new InPredicate($exp1.expression,
                                                $set2Expression.expression));
            	}	
			)
			|
			conditionExists
			{
				$predicate = $conditionExists.predicate;
			};
			
booleanAtom returns [boolean value]:
			TRUE
			{
				value = true;
			}
			|
			FALSE
			{
				value = false;
			};
			
			
comparisonOperator returns [int op]:
		     EQUALS
		     {
		          op = FinalVariable.EQUALS;
		     }
		     |
		     NOTEQUALS
		     { 
		          op = FinalVariable.NOTEQUALS;
		     }
		     |
		     LESSTHAN
		     {
		          op = FinalVariable.LESSTHAN;
		     }
		     |
		     GREATERTHAN
		     {
		          op = FinalVariable.GREATERTHAN;
		     }
		     |
		     LESSEQUAL
		     {
		          op = FinalVariable.LESSEQUAL;
		     }
		     |
		     GREATEREQUAL
		     {
		          op = FinalVariable.GREATEREQUAL;
		     };


conditionExists returns [BooleanPredicate predicate]:
			EXISTS subquery
			{
				predicate = new ExistPredicate($subquery.expression);
			};

/*
 * 7 -------------------------------------basic elements ---------------------------
 */ 

/** We have this to enable case insensitivity in our keywords */
fragment A  : ('a'|'A') ;
fragment B  : ('b'|'B') ;
fragment C  : ('c'|'C') ;
fragment D  : ('d'|'D') ;
fragment E  : ('e'|'E') ;
fragment F  : ('f'|'F') ;
fragment G  : ('g'|'G') ;
fragment H  : ('h'|'H') ;
fragment I  : ('i'|'I') ;
fragment J  : ('j'|'J') ;
fragment K  : ('k'|'K') ;
fragment L  : ('l'|'L') ;
fragment M  : ('m'|'M') ;
fragment N  : ('n'|'N') ;
fragment O  : ('o'|'O') ;
fragment P  : ('p'|'P') ;
fragment Q  : ('q'|'Q') ;
fragment R  : ('r'|'R') ;
fragment S  : ('s'|'S') ;
fragment T  : ('t'|'T') ;
fragment U  : ('u'|'U') ;
fragment V  : ('v'|'V') ;
fragment W  : ('w'|'W') ;
fragment X  : ('x'|'X') ;
fragment Y  : ('y'|'Y') ;
fragment Z  : ('z'|'Z') ;

/*key words**/
SELECT : S E L E C T;
CREATE: C R E A T E;
TABLE: T A B L E;
VIEW: V I E W;
MATERIALIZED: M A T E R I A L I Z E D;
FROM : F R O M;
WHERE: W H E R E;
GROUP: G R O U P;
ORDER: O R D E R;
BY: B Y;
HAVING: H A V I N G;
RANDOM: R A N D O M;
SOURCE: S O U R C E;
RETURNS: R E T U R N S;

FOR: F O R;
EACH: E A C H;
WITH: W I T H;

ALL: A L L;
DISTINCT: D I S T I N C T;
AS: A S;
IN: I N;
VALUES: V A L U E S;

AND: A N D;
OR: O R;
NOT: N O T;
LIKE: L I K E;
BETWEEN: B E T W E E N;
EXISTS: E X I S T S;

ASC: A S C;
DESC: D E S C;
 

AVG: A V G;
SUM: S U M;
COUNT: C O U N T;
MAX: M A X;
MIN: M I N;
VAR: V A R;
STDEV: S T D E V;
VECTOR: V E C T O R I Z E;
ROWMATRIX: R O W M A T R I X;
COLMATRIX: C O L M A T R I X;

TRUE: T R U E;
FALSE: F A L S E;

CASE: C A S E;
WHEN: W H E N;
ELSE: E L S E;
THEN: T H E N;

PRIMARY: P R I M A R Y;
FOREIGN: F O R E I G N;
KEY: K E Y;
REFERENCES: R E F E R E N C E S;
OUT: O U T;

DROP: D R O P;
VGFUNCTION: V G F U N C T I O N;
FUNCTION: F U N C T I O N;
GENERALTABLEINDEX: I|J|K|L;

UNION: U N I O N;


ASTERISK : '*';
SEMICOLON: ';';
COMMA: ',';
TREEDOT:'...';
TWODOT:',..,';
DOT: '.';
COLON: ':';
PLUS: '+';
MINUS: '-';
SLASH: '/';
LPAREN: '(';
RPAREN: ')';
LBRACE: '{';
RBRACE: '}';
LBRACKET: '[';
RBRACKET: ']';
EQUALS: '=';
NOTEQUALS: '<>';
LESSTHAN: '<';
GREATERTHAN: '>';
LESSEQUAL: '<=';
GREATEREQUAL: '>=';
IDENTIFIER  : ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'_'|'0'..'9')* ;
STRING: '\'' ('a'..'z'|'A'..'Z'|'0'..'9'|'/'|'('|')'|':'|'\\'|('\\\'')|'.'|'-'|'_'|' '|'%'|'#')+ '\'';
NUMERIC: (('1'..'9')('0'..'9')*)|'0'|('.'(('0'..'9')+));


/** Skipped tokens (whitespace) */
WS : ( ' ' | '\t' | '\n' | '\r' | '\u000C')+
      { $channel = HIDDEN; }
  ; 

/** Comments, too, get ignored */
COMMENT : ('--' .* '\n')
        { $channel = HIDDEN; }
        | ('/*').* ('*/')
        { $channel = HIDDEN; }
    ;
 
