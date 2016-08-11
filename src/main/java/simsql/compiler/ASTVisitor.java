

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


package simsql.compiler; // package mcdb.compiler.parser.astVisitor;

/*
 * @Author: Bamboo
 *  Date: 09/02/2010
 *  This class corresponds to the super class of typer checker
 */

import java.util.ArrayList;
import simsql.runtime.DataType;



// import mcdb.compiler.parser.expression.boolExpression.*;
// import mcdb.compiler.parser.expression.mathExpression.*;
// import mcdb.compiler.parser.expression.sqlExpression.*;
// import mcdb.compiler.parser.expression.sqlType.DropElement;


public abstract class ASTVisitor {
	
	 // visiting math expression
	 public abstract ArrayList<DataType> visitColumnExpression(ColumnExpression columnExpression) throws Exception;
	 public abstract ArrayList<DataType> visitNumericExpression(NumericExpression numericExpression) throws Exception;
	 public abstract ArrayList<DataType> visitStringExpression(StringExpression stringExpression) throws Exception;
	 public abstract ArrayList<DataType> visitAggregateExpression(AggregateExpression aggregateExpression) throws Exception;
	 public abstract ArrayList<DataType> visitArithmeticExpression(ArithmeticExpression arithmeticExpression) throws Exception;
	 public abstract ArrayList<DataType> visitGeneralFunctionExpression(GeneralFunctionExpression generalFunctionExpression) throws Exception;
	 public abstract ArrayList<DataType> visitSetExpression(SetExpression setExpression) throws Exception;
	 public abstract ArrayList<DataType> visitSubqueryExpression(SubqueryExpression subqueryExpression) throws Exception;
	 public abstract ArrayList<DataType> visitDateExpression(DateExpression dateExpression) throws Exception;
	 public abstract ArrayList<DataType> visitAsteriskExpression(AsteriskExpression asteriskExpression) throws Exception;
	 public abstract ArrayList<DataType> visitPredicateWrapper(PredicateWrapper predicateWrapper) throws Exception;	
	 public abstract ArrayList<DataType> visitGeneralTableIndexExpression(GeneralTableIndex generalTableIndex);
		
     // visiting boolean predicates
	 public abstract boolean visitComparisonPredicate(ComparisonPredicate comparisonPredicate)throws Exception;
	 public abstract boolean visitBetweenPredicate(BetweenPredicate betweenPredicate)throws Exception;
	 public abstract boolean visitLikePredicate(LikePredicate likePredicate)throws Exception;
	 public abstract boolean visitNotPredicate(NotPredicate notPredicate)throws Exception;
	 public abstract boolean visitAndPredicate(AndPredicate andPredicate)throws Exception;
	 public abstract boolean visitOrPredicate(OrPredicate orPredicate)throws Exception;
	 public abstract boolean visitAtomPredicate(AtomPredicate atomPredicate)throws Exception;
	 public abstract boolean visitInExpression(InPredicate inPredicate)throws Exception;
	 public abstract boolean visitExistPredicate(ExistPredicate existPredicate)throws Exception;
	 	

     // visiting sql expressions
	 public abstract boolean visitAllFromTable(AllFromTable allFromTable)throws Exception;
	 public abstract boolean visitDerivedColumn(DerivedColumn derivedColumn) throws Exception;
	 public abstract boolean visitAsteriskTable(AsteriskTable asteriskTable)throws Exception;
	 public abstract boolean visitSelectStatement(SelectStatement selectStatement) throws Exception;
	 public abstract boolean visitDefinedTableSchemaExpression(DefinedTableSchema table)throws Exception;
	 public abstract boolean visitDefinedArrayTableSchemaExpression(DefinedArrayTableSchema definedArrayTableSchema)throws Exception;
	 public abstract boolean visitModuloTableSchemaExpression(DefinedModuloTableSchema definedModuloTableSchema)throws Exception;
	 public abstract boolean visitWithStatementExpression(WithStatement withStatement)throws Exception;
	 public abstract boolean visitTableReferenceExpression(TableReference tableReference)throws Exception;
	 public abstract boolean visitOrderByColumn(OrderByColumn orderByColumn)throws Exception;
	 public abstract boolean visitFromSubqueryExpression(FromSubquery viewTable)throws Exception;
	 public abstract boolean visitAttributeElement(AttributeDefineElement attributeElement)throws Exception;
	 public abstract boolean visitForeignKey(ForeignKeyElement foreignKey)throws Exception;
	 public abstract boolean visitPrimaryKey(PrimaryKeyElement primaryKey)throws Exception;
	 public abstract boolean visitDropElement(DropElement dropElement)throws Exception;
	 public abstract boolean visitCommonTableNameTable(CommonTableName commonTableName) throws Exception;
	 public abstract boolean visitBaselineTableNameTable(BaselineTableName baselineTableName) throws Exception;
	 public abstract boolean visitGeneralTableNameTable(GeneralTableName generalTableTableName) throws Exception;
	 public abstract boolean visitBaselineTableNameArray(BaselineTableNameArray generalTableTableName) throws Exception;
	 public abstract boolean visitGeneralTableNameArray(GeneralTableNameArray generalTableTableName) throws Exception;
	
	 
}
