

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


package simsql.compiler; // package mcdb.compiler;
/**
 * 
 */



import java.util.ArrayList;
import java.util.StringTokenizer;


import org.antlr.runtime.ANTLRStringStream;
import org.antlr.runtime.CommonTokenStream;






// import mcdb.catalog.Catalog;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.Operator;
// import mcdb.compiler.logicPlan.logicOperator.relationOperator.TableScan;
// import mcdb.compiler.logicPlan.logicOperator.statisticsOperator.SystemParameters;
// import mcdb.compiler.parser.astVisitor.BaseLineRandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.GeneralRandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.RandomTableTypeChecker;
// import mcdb.compiler.parser.astVisitor.TableDefineChecker;
// import mcdb.compiler.parser.astVisitor.TypeChecker;
// import mcdb.compiler.parser.astVisitor.VGFunctionDefineChecker;
// import mcdb.compiler.parser.astVisitor.ViewTypeChecker;
// import mcdb.compiler.parser.expression.Expression;
// import mcdb.compiler.parser.expression.sqlExpression.SQLExpression;
// import mcdb.compiler.parser.expression.sqlExpression.SelectStatement;
// import mcdb.compiler.parser.expression.sqlType.BaseLineRandomTableStatement;
// import mcdb.compiler.parser.expression.sqlType.DropElement;
// import mcdb.compiler.parser.expression.sqlType.GeneralRandomTableStatement;
// import mcdb.compiler.parser.expression.sqlType.RandomTableStatement;
// import mcdb.compiler.parser.expression.sqlType.TableDefinitionStatement;
// import mcdb.compiler.parser.expression.sqlType.VGFunctionDefinitionStatement;
// import mcdb.compiler.parser.expression.sqlType.ViewStatement;
// import mcdb.compiler.parser.expression.util.Simplifier;
// import mcdb.compiler.parser.grammar.QueryLexer;
// import mcdb.compiler.parser.grammar.QueryParser;

/**
 * @author Bamboo
 *
 */
public class CompilerProcessor {
	
	//call the Lexer and Parser to parse the query, return some expressions
	public static ArrayList<Expression> parse(String query) throws Exception
	{
		ANTLRStringStream input = new ANTLRStringStream(query);
        QueryLexer lexer = new QueryLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        QueryParser parser = new QueryParser(tokens);
        ArrayList<Expression> expressionList = parser.prog();
        if(parser.getErrorNum() > 0)
        {
        	throw new Exception("Grammar error!");
        }
        
        return expressionList;
	}


	
	//typecheck the expressions from the Parser
	public static TypeChecker typeCheck(Expression expression,
                                        String sql) throws Exception
	{
		boolean subcheck;
		TypeChecker typeChecker = null;
		
		if(expression instanceof SelectStatement)
		{
			typeChecker = new TypeChecker(true);
			subcheck = typeChecker.visitSelectStatement((SelectStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof ViewStatement)
		{
			((ViewStatement) expression).setSqlString(sql);
			typeChecker = new ViewTypeChecker(false);
			subcheck = ((ViewTypeChecker)typeChecker).visitViewStatement((ViewStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof MaterializedViewStatement)
		{
			((MaterializedViewStatement) expression).setSqlString(sql);
			typeChecker = new MaterializedViewTypeChecker(false);
			subcheck = ((MaterializedViewTypeChecker)typeChecker).visitMaterializedViewStatement((MaterializedViewStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof UnionViewStatement)
		{
			((UnionViewStatement)expression).setSqlString(sql);
			typeChecker = new UnionViewStatementTypeChecker();
			subcheck = ((UnionViewStatementTypeChecker)typeChecker).visitUnionViewStatement((UnionViewStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		/*
		 * ----------------------for simulation---------------------
		 */
		else if(expression instanceof BaseLineRandomTableStatement)
		{
			((BaseLineRandomTableStatement) expression).setSqlString(sql);
			typeChecker = new BaseLineRandomTableTypeChecker(false);
			subcheck = ((BaseLineRandomTableTypeChecker)typeChecker).visitBaseLineRandomTableStatement((BaseLineRandomTableStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof BaseLineArrayRandomTableStatement)
		{
			((BaseLineArrayRandomTableStatement) expression).setSqlString(sql);
			typeChecker = new BaselineArrayRandomTypeChecker();
			subcheck = ((BaselineArrayRandomTypeChecker)typeChecker).visitBaselineArrayRandomTableStatement(
					(BaseLineArrayRandomTableStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof MultidimensionalTableStatement)
		{
			((MultidimensionalTableStatement) expression).setSqlString(sql);
			typeChecker = new MultidimensionalTableTypeChecker(false);
			subcheck = ((MultidimensionalTableTypeChecker)typeChecker).visitMultidimensionalTableStatement(
					(MultidimensionalTableStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof GeneralRandomTableStatement)
		{
			((GeneralRandomTableStatement) expression).setSqlString(sql);
			typeChecker = new GeneralRandomTableTypeChecker(false);
			subcheck = ((GeneralRandomTableTypeChecker)typeChecker).visitGeneralRandomTableStatement((GeneralRandomTableStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		/*
		 * --------------------------end------------------------------
		 */
		else if(expression instanceof RandomTableStatement)
		{
			((RandomTableStatement) expression).setSqlString(sql);
			typeChecker = new RandomTableTypeChecker(false);
			subcheck = ((RandomTableTypeChecker)typeChecker).visitRandomTableStatement((RandomTableStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof TableDefinitionStatement)
		{
			typeChecker = new TableDefineChecker();
			subcheck = ((TableDefineChecker)typeChecker).visitTableDefinitionStatement((TableDefinitionStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof VGFunctionDefinitionStatement)
		{
			typeChecker = new VGFunctionDefineChecker();
			subcheck = ((VGFunctionDefineChecker)typeChecker).visitVGFunctionDefinitionStatement((VGFunctionDefinitionStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof FunctionDefinitionStatement)
		{
			typeChecker = new FunctionDefineChecker();
			
			subcheck = ((FunctionDefineChecker)typeChecker).visitFunctionDefinitionStatement((FunctionDefinitionStatement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
		else if(expression instanceof DropElement)
		{
			typeChecker = new TypeChecker(false);
			subcheck = ((TypeChecker)typeChecker).visitDropElement((DropElement)expression);
			if(!subcheck)
			{
				return null;
			}
		}
       
		return typeChecker;
	}

	//typecheck the expressions from the Parser
	public static TypeChecker typeCheck(Expression expression,
			                            String sql,
			                            boolean save) throws Exception
	{
		boolean subcheck;
		TypeChecker typechecker = null;
		
		if(expression != null)
        {
			if(expression instanceof SelectStatement)
    		{
    			typechecker = new TypeChecker(true);
    			subcheck = typechecker.visitSelectStatement((SelectStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
    		else if(expression instanceof ViewStatement)
    		{
    			((ViewStatement) expression).setSqlString(sql);
    			typechecker = new ViewTypeChecker(false);
    			
    			((ViewTypeChecker)typechecker).setSaved(save);
    			subcheck = ((ViewTypeChecker)typechecker).visitViewStatement((ViewStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
			else if(expression instanceof MaterializedViewStatement)
    		{
    			((MaterializedViewStatement) expression).setSqlString(sql);
    			typechecker = new MaterializedViewTypeChecker(false);
    			subcheck = ((MaterializedViewTypeChecker)typechecker).visitMaterializedViewStatement((MaterializedViewStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
    		else if(expression instanceof UnionViewStatement)
    		{
    			((UnionViewStatement)expression).setSqlString(sql);
    			typechecker = new UnionViewStatementTypeChecker();
    			((UnionViewStatementTypeChecker)typechecker).setToSave(save);
    			subcheck = ((UnionViewStatementTypeChecker)typechecker).visitUnionViewStatement((UnionViewStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
    		/*
    		 * ----------------------for simulation---------------------
    		 */
    		else if(expression instanceof BaseLineRandomTableStatement)
    		{
    			((BaseLineRandomTableStatement) expression).setSqlString(sql);
    			typechecker = new BaseLineRandomTableTypeChecker(false);
    			((BaseLineRandomTableTypeChecker)typechecker).setSaved(save);
    			subcheck = ((BaseLineRandomTableTypeChecker)typechecker).visitRandomTableStatement((RandomTableStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
    		else if(expression instanceof BaseLineArrayRandomTableStatement)
    		{
    			((BaseLineArrayRandomTableStatement) expression).setSqlString(sql);
    			typechecker = new BaselineArrayRandomTypeChecker();
    			subcheck = ((BaselineArrayRandomTypeChecker)typechecker).visitBaselineArrayRandomTableStatement(
    					(BaseLineArrayRandomTableStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
			else if(expression instanceof MultidimensionalTableStatement)
			{
				((MultidimensionalTableStatement) expression).setSqlString(sql);
				typechecker = new MultidimensionalTableTypeChecker(false);
				((MultidimensionalTableTypeChecker)typechecker).setSaved(save);
				subcheck = ((MultidimensionalTableTypeChecker)typechecker).visitMultidimensionalTableStatement((MultidimensionalTableStatement)expression);
				if(!subcheck)
				{
					return null;
				}
			}
    		else if(expression instanceof GeneralRandomTableStatement)
    		{
    			((GeneralRandomTableStatement) expression).setSqlString(sql);
    			typechecker = new GeneralRandomTableTypeChecker(false);
    			((GeneralRandomTableTypeChecker)typechecker).setSaved(save);
    			subcheck = ((GeneralRandomTableTypeChecker)typechecker).visitRandomTableStatement((RandomTableStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
    		else if(expression instanceof RandomTableStatement)
    		{
    			((RandomTableStatement) expression).setSqlString(sql);
    			typechecker = new RandomTableTypeChecker(false);
    			
    			((RandomTableTypeChecker)typechecker).setSaved(save);
    			subcheck = ((RandomTableTypeChecker)typechecker).visitRandomTableStatement((RandomTableStatement)expression);
    			if(!subcheck)
    			{
    				return null;
    			}
    		}
        }
		
		return typechecker;
	}

	public static void simplify(ArrayList<Expression> expressionList) throws Exception
	{
		for(int i = 0; i < expressionList.size(); i++)
		{
			Expression expression = expressionList.get(i);
			if(expression instanceof SQLExpression)
				Simplifier.simplifySQLExpression((SQLExpression)expression);
			else if(expression instanceof ViewStatement)
			{
				Simplifier.simplifyViewStatement((ViewStatement)expression);
			}
			else if(expression instanceof MaterializedViewStatement)
			{
				Simplifier.simplifyMaterializedViewStatement((MaterializedViewStatement)expression);
			}
			else if(expression instanceof RandomTableStatement)
			{
				Simplifier.simplifyRandomTableStatement((RandomTableStatement)expression);
			}
			else if(expression instanceof UnionViewStatement)
			{
				Simplifier.simplifyUnionViewStatement((UnionViewStatement) expression);
			}
		}
	}
	
}
