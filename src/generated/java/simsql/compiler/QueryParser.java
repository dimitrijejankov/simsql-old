// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/dimitrije/git/v0.5/src/compiler/parser/Query.g 2016-08-03 05:31:13


package simsql.compiler;

import org.antlr.runtime.*;

import java.util.ArrayList;
import java.util.HashMap;

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
public class QueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "SEMICOLON", "DROP", "TABLE", "IDENTIFIER", "LBRACKET", "NUMERIC", "TWODOT", "RBRACKET", "GENERALTABLEINDEX", "VIEW", "MATERIALIZED", "VGFUNCTION", "FUNCTION", "SELECT", "ALL", "DISTINCT", "COMMA", "DOT", "ASTERISK", "AS", "EQUALS", "FROM", "VALUES", "LPAREN", "RPAREN", "STRING", "WHERE", "GROUP", "BY", "ORDER", "ASC", "DESC", "HAVING", "CREATE", "COLON", "FOR", "EACH", "IN", "WITH", "RETURNS", "SOURCE", "UNION", "PRIMARY", "KEY", "FOREIGN", "REFERENCES", "RANDOM", "PLUS", "MINUS", "CASE", "WHEN", "THEN", "ELSE", "SLASH", "AVG", "SUM", "COUNT", "MIN", "MAX", "VAR", "STDEV", "VECTOR", "ROWMATRIX", "COLMATRIX", "OR", "AND", "TRUE", "NOTEQUALS", "FALSE", "NOT", "BETWEEN", "LIKE", "LESSTHAN", "GREATERTHAN", "LESSEQUAL", "GREATEREQUAL", "EXISTS", "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "OUT", "LBRACE", "RBRACE", "WS", "COMMENT"
    };
    public static final int FUNCTION=16;
    public static final int CASE=53;
    public static final int GREATEREQUAL=79;
    public static final int EQUALS=24;
    public static final int COUNT=60;
    public static final int NOT=73;
    public static final int EOF=-1;
    public static final int FOREIGN=48;
    public static final int LBRACKET=8;
    public static final int TWODOT=10;
    public static final int RPAREN=28;
    public static final int CREATE=37;
    public static final int VAR=63;
    public static final int EACH=40;
    public static final int COMMENT=111;
    public static final int SELECT=17;
    public static final int GREATERTHAN=77;
    public static final int D=84;
    public static final int E=85;
    public static final int F=86;
    public static final int G=87;
    public static final int A=81;
    public static final int RBRACE=109;
    public static final int VIEW=13;
    public static final int B=82;
    public static final int ASC=34;
    public static final int C=83;
    public static final int L=92;
    public static final int M=93;
    public static final int N=94;
    public static final int O=95;
    public static final int KEY=47;
    public static final int H=88;
    public static final int I=89;
    public static final int ELSE=56;
    public static final int J=90;
    public static final int K=91;
    public static final int MATERIALIZED=14;
    public static final int U=101;
    public static final int T=100;
    public static final int ROWMATRIX=66;
    public static final int W=103;
    public static final int V=102;
    public static final int Q=97;
    public static final int SEMICOLON=4;
    public static final int PRIMARY=46;
    public static final int P=96;
    public static final int S=99;
    public static final int R=98;
    public static final int NUMERIC=9;
    public static final int Y=105;
    public static final int X=104;
    public static final int Z=106;
    public static final int GROUP=31;
    public static final int WS=110;
    public static final int OUT=107;
    public static final int OR=68;
    public static final int FROM=25;
    public static final int FALSE=72;
    public static final int DISTINCT=19;
    public static final int LESSEQUAL=78;
    public static final int WHERE=30;
    public static final int ORDER=33;
    public static final int LBRACE=108;
    public static final int TABLE=6;
    public static final int MAX=62;
    public static final int FOR=39;
    public static final int SOURCE=44;
    public static final int RANDOM=50;
    public static final int SUM=59;
    public static final int AND=69;
    public static final int LPAREN=27;
    public static final int ASTERISK=22;
    public static final int AS=23;
    public static final int SLASH=57;
    public static final int IN=41;
    public static final int THEN=55;
    public static final int COMMA=20;
    public static final int REFERENCES=49;
    public static final int IDENTIFIER=7;
    public static final int AVG=58;
    public static final int ALL=18;
    public static final int PLUS=51;
    public static final int EXISTS=80;
    public static final int RBRACKET=11;
    public static final int DOT=21;
    public static final int RETURNS=43;
    public static final int WITH=42;
    public static final int LIKE=75;
    public static final int BY=32;
    public static final int LESSTHAN=76;
    public static final int VALUES=26;
    public static final int NOTEQUALS=71;
    public static final int HAVING=36;
    public static final int MIN=61;
    public static final int COLMATRIX=67;
    public static final int MINUS=52;
    public static final int TRUE=70;
    public static final int UNION=45;
    public static final int COLON=38;
    public static final int VECTOR=65;
    public static final int DROP=5;
    public static final int VGFUNCTION=15;
    public static final int GENERALTABLEINDEX=12;
    public static final int WHEN=54;
    public static final int STDEV=64;
    public static final int DESC=35;
    public static final int BETWEEN=74;
    public static final int STRING=29;

    // delegates
    // delegators


        public QueryParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public QueryParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
            this.state.ruleMemo = new HashMap[228+1];
             
             
        }
        

    public String[] getTokenNames() { return QueryParser.tokenNames; }
    public String getGrammarFileName() { return "/home/dimitrije/git/v0.5/src/compiler/parser/Query.g"; }


    	  private int errorNum = 0;
    	  public int getErrorNum()
    	  {
    	  		return errorNum;
    	  }
    	  
    	  public void reportError(RecognitionException re) {
                super.reportError(re);
                errorNum++;
          }



    // $ANTLR start "prog"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:68:1: prog returns [ArrayList<Expression> queryList] : statements ;
    public final ArrayList<Expression> prog() throws RecognitionException {
        ArrayList<Expression> queryList = null;
        int prog_StartIndex = input.index();
        ArrayList<Expression> statements1 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 1) ) { return queryList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:68:46: ( statements )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:68:48: statements
            {
            pushFollow(FOLLOW_statements_in_prog71);
            statements1=statements();

            state._fsp--;
            if (state.failed) return queryList;
            if ( state.backtracking==0 ) {

              		  	   
              		  	   queryList = statements1;
              		  
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 1, prog_StartIndex); }
        }
        return queryList;
    }
    // $ANTLR end "prog"


    // $ANTLR start "statements"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:74:1: statements returns [ArrayList<Expression> queryList] : q1= statement ( SEMICOLON q2= statement )* ( SEMICOLON )? ;
    public final ArrayList<Expression> statements() throws RecognitionException {
        ArrayList<Expression> queryList = null;
        int statements_StartIndex = input.index();
        Expression q1 = null;

        Expression q2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 2) ) { return queryList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:74:52: (q1= statement ( SEMICOLON q2= statement )* ( SEMICOLON )? )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:75:8: q1= statement ( SEMICOLON q2= statement )* ( SEMICOLON )?
            {
            pushFollow(FOLLOW_statement_in_statements100);
            q1=statement();

            state._fsp--;
            if (state.failed) return queryList;
            if ( state.backtracking==0 ) {

              	      	   queryList = new ArrayList<Expression>();
              	      	   queryList.add(q1);
              	      
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:80:8: ( SEMICOLON q2= statement )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==SEMICOLON) ) {
                    int LA1_1 = input.LA(2);

                    if ( (LA1_1==DROP||LA1_1==SELECT||LA1_1==CREATE) ) {
                        alt1=1;
                    }


                }


                switch (alt1) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:81:8: SEMICOLON q2= statement
            	    {
            	    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_statements127); if (state.failed) return queryList;
            	    pushFollow(FOLLOW_statement_in_statements142);
            	    q2=statement();

            	    state._fsp--;
            	    if (state.failed) return queryList;
            	    if ( state.backtracking==0 ) {

            	      	      	   		queryList.add(q2);
            	      	      	   
            	    }

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);

            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:87:8: ( SEMICOLON )?
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==SEMICOLON) ) {
                alt2=1;
            }
            switch (alt2) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: SEMICOLON
                    {
                    match(input,SEMICOLON,FOLLOW_SEMICOLON_in_statements174); if (state.failed) return queryList;

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 2, statements_StartIndex); }
        }
        return queryList;
    }
    // $ANTLR end "statements"


    // $ANTLR start "statement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:89:1: statement returns [Expression statement] : ( selectStatement | createStatement | dropStatement );
    public final Expression statement() throws RecognitionException {
        Expression statement = null;
        int statement_StartIndex = input.index();
        SelectStatement selectStatement2 = null;

        Expression createStatement3 = null;

        Expression dropStatement4 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 3) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:89:40: ( selectStatement | createStatement | dropStatement )
            int alt3=3;
            switch ( input.LA(1) ) {
            case SELECT:
                {
                alt3=1;
                }
                break;
            case CREATE:
                {
                alt3=2;
                }
                break;
            case DROP:
                {
                alt3=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:90:4: selectStatement
                    {
                    pushFollow(FOLLOW_selectStatement_in_statement195);
                    selectStatement2=selectStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                               	  statement = selectStatement2;
                               
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:95:10: createStatement
                    {
                    pushFollow(FOLLOW_createStatement_in_statement228);
                    createStatement3=createStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      		 	  statement = createStatement3;
                      		 
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:100:10: dropStatement
                    {
                    pushFollow(FOLLOW_dropStatement_in_statement255);
                    dropStatement4=dropStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                               	  statement = dropStatement4;
                               
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 3, statement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "statement"


    // $ANTLR start "createStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:106:1: createStatement returns [Expression statement] : ( createRandomTableStatement | createViewStatement | createTableStatement | createFunctionStatement | createVGFunctionStatement | createUnionViewStatement );
    public final Expression createStatement() throws RecognitionException {
        Expression statement = null;
        int createStatement_StartIndex = input.index();
        RandomTableStatement createRandomTableStatement5 = null;

        Expression createViewStatement6 = null;

        TableDefinitionStatement createTableStatement7 = null;

        FunctionDefinitionStatement createFunctionStatement8 = null;

        VGFunctionDefinitionStatement createVGFunctionStatement9 = null;

        UnionViewStatement createUnionViewStatement10 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 4) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:106:46: ( createRandomTableStatement | createViewStatement | createTableStatement | createFunctionStatement | createVGFunctionStatement | createUnionViewStatement )
            int alt4=6;
            alt4 = dfa4.predict(input);
            switch (alt4) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:107:3: createRandomTableStatement
                    {
                    pushFollow(FOLLOW_createRandomTableStatement_in_createStatement290);
                    createRandomTableStatement5=createRandomTableStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	      	  statement = createRandomTableStatement5;
                      	    
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:112:3: createViewStatement
                    {
                    pushFollow(FOLLOW_createViewStatement_in_createStatement308);
                    createViewStatement6=createViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                              	  statement = createViewStatement6;
                              
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:117:9: createTableStatement
                    {
                    pushFollow(FOLLOW_createTableStatement_in_createStatement338);
                    createTableStatement7=createTableStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                              	  statement = createTableStatement7;
                              
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:122:9: createFunctionStatement
                    {
                    pushFollow(FOLLOW_createFunctionStatement_in_createStatement368);
                    createFunctionStatement8=createFunctionStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                              	  statement = createFunctionStatement8;
                              
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:127:9: createVGFunctionStatement
                    {
                    pushFollow(FOLLOW_createVGFunctionStatement_in_createStatement398);
                    createVGFunctionStatement9=createVGFunctionStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                              	  statement = createVGFunctionStatement9;
                              
                    }

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:132:9: createUnionViewStatement
                    {
                    pushFollow(FOLLOW_createUnionViewStatement_in_createStatement428);
                    createUnionViewStatement10=createUnionViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                    statement = createUnionViewStatement10;
                              
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 4, createStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createStatement"


    // $ANTLR start "dropStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:137:1: dropStatement returns [Expression statement] : ( dropTableStatement | dropViewStatement | dropFunctionStatement | dropVGFunctionStatement );
    public final Expression dropStatement() throws RecognitionException {
        Expression statement = null;
        int dropStatement_StartIndex = input.index();
        Expression dropTableStatement11 = null;

        Expression dropViewStatement12 = null;

        Expression dropFunctionStatement13 = null;

        Expression dropVGFunctionStatement14 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 5) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:137:44: ( dropTableStatement | dropViewStatement | dropFunctionStatement | dropVGFunctionStatement )
            int alt5=4;
            int LA5_0 = input.LA(1);

            if ( (LA5_0==DROP) ) {
                switch ( input.LA(2) ) {
                case TABLE:
                    {
                    alt5=1;
                    }
                    break;
                case VIEW:
                case MATERIALIZED:
                    {
                    alt5=2;
                    }
                    break;
                case FUNCTION:
                    {
                    alt5=3;
                    }
                    break;
                case VGFUNCTION:
                    {
                    alt5=4;
                    }
                    break;
                default:
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 5, 1, input);

                    throw nvae;
                }

            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 5, 0, input);

                throw nvae;
            }
            switch (alt5) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:138:4: dropTableStatement
                    {
                    pushFollow(FOLLOW_dropTableStatement_in_dropStatement460);
                    dropTableStatement11=dropTableStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      		 	  statement = dropTableStatement11;
                      		 
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:143:4: dropViewStatement
                    {
                    pushFollow(FOLLOW_dropViewStatement_in_dropStatement475);
                    dropViewStatement12=dropViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      		 	  statement = dropViewStatement12;
                      		 
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:148:10: dropFunctionStatement
                    {
                    pushFollow(FOLLOW_dropFunctionStatement_in_dropStatement496);
                    dropFunctionStatement13=dropFunctionStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                   statement = dropFunctionStatement13;
                               
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:153:4: dropVGFunctionStatement
                    {
                    pushFollow(FOLLOW_dropVGFunctionStatement_in_dropStatement523);
                    dropVGFunctionStatement14=dropVGFunctionStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      		 	  statement = dropVGFunctionStatement14;
                      		 
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 5, dropStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "dropStatement"


    // $ANTLR start "dropTableStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:158:1: dropTableStatement returns [Expression statement] : DROP TABLE (viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET | tableName= IDENTIFIER ) ;
    public final Expression dropTableStatement() throws RecognitionException {
        Expression statement = null;
        int dropTableStatement_StartIndex = input.index();
        Token viewName=null;
        Token lb=null;
        Token ub=null;
        Token tableName=null;
        Token indexName=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 6) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:158:49: ( DROP TABLE (viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET | tableName= IDENTIFIER ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:159:3: DROP TABLE (viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET | tableName= IDENTIFIER )
            {
            match(input,DROP,FOLLOW_DROP_in_dropTableStatement543); if (state.failed) return statement;
            match(input,TABLE,FOLLOW_TABLE_in_dropTableStatement545); if (state.failed) return statement;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:160:3: (viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET | tableName= IDENTIFIER )
            int alt6=4;
            int LA6_0 = input.LA(1);

            if ( (LA6_0==IDENTIFIER) ) {
                int LA6_1 = input.LA(2);

                if ( (LA6_1==LBRACKET) ) {
                    int LA6_2 = input.LA(3);

                    if ( (LA6_2==NUMERIC) ) {
                        int LA6_5 = input.LA(4);

                        if ( (synpred13_Query()) ) {
                            alt6=1;
                        }
                        else if ( (synpred14_Query()) ) {
                            alt6=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return statement;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 6, 5, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA6_2==GENERALTABLEINDEX) ) {
                        alt6=3;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return statement;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 6, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA6_1==EOF||LA6_1==SEMICOLON) ) {
                    alt6=4;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 6, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 6, 0, input);

                throw nvae;
            }
            switch (alt6) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:161:7: viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET
                    {
                    viewName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropTableStatement560); if (state.failed) return statement;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_dropTableStatement562); if (state.failed) return statement;
                    lb=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_dropTableStatement566); if (state.failed) return statement;
                    match(input,TWODOT,FOLLOW_TWODOT_in_dropTableStatement568); if (state.failed) return statement;
                    ub=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_dropTableStatement572); if (state.failed) return statement;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_dropTableStatement574); if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                        statement = new DropElement((viewName!=null?viewName.getText():null) + "_" + (lb!=null?lb.getText():null) + "src/main" + (ub!=null?ub.getText():null), DropElement.ARRAY_CONSTANT_INDEX_TABLE);
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:166:13: tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropTableStatement618); if (state.failed) return statement;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_dropTableStatement620); if (state.failed) return statement;
                    indexName=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_dropTableStatement624); if (state.failed) return statement;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_dropTableStatement626); if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      			      statement = new DropElement((tableName!=null?tableName.getText():null)+"_" + (indexName!=null?indexName.getText():null), DropElement.CONSTANT_INDEX_TABLE);
                      			
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:171:4: tableName= IDENTIFIER LBRACKET GENERALTABLEINDEX RBRACKET
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropTableStatement643); if (state.failed) return statement;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_dropTableStatement645); if (state.failed) return statement;
                    match(input,GENERALTABLEINDEX,FOLLOW_GENERALTABLEINDEX_in_dropTableStatement647); if (state.failed) return statement;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_dropTableStatement649); if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      			      statement = new DropElement((tableName!=null?tableName.getText():null)+"_i", DropElement.GENERAL_INDEX_TABLE); 
                      			
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:176:4: tableName= IDENTIFIER
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropTableStatement666); if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	              statement = new DropElement((tableName!=null?tableName.getText():null), DropElement.TABLEORCOMMON_RANDOM_TABLE);
                      	        
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 6, dropTableStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "dropTableStatement"


    // $ANTLR start "dropViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:182:1: dropViewStatement returns [Expression statement] : ( DROP VIEW viewName= IDENTIFIER ( LBRACKET indexName= NUMERIC RBRACKET | LBRACKET GENERALTABLEINDEX RBRACKET | ) | DROP MATERIALIZED VIEW viewName2= IDENTIFIER );
    public final Expression dropViewStatement() throws RecognitionException {
        Expression statement = null;
        int dropViewStatement_StartIndex = input.index();
        Token viewName=null;
        Token indexName=null;
        Token viewName2=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 7) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:182:48: ( DROP VIEW viewName= IDENTIFIER ( LBRACKET indexName= NUMERIC RBRACKET | LBRACKET GENERALTABLEINDEX RBRACKET | ) | DROP MATERIALIZED VIEW viewName2= IDENTIFIER )
            int alt8=2;
            int LA8_0 = input.LA(1);

            if ( (LA8_0==DROP) ) {
                int LA8_1 = input.LA(2);

                if ( (LA8_1==VIEW) ) {
                    alt8=1;
                }
                else if ( (LA8_1==MATERIALIZED) ) {
                    alt8=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 8, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 8, 0, input);

                throw nvae;
            }
            switch (alt8) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:183:3: DROP VIEW viewName= IDENTIFIER ( LBRACKET indexName= NUMERIC RBRACKET | LBRACKET GENERALTABLEINDEX RBRACKET | )
                    {
                    match(input,DROP,FOLLOW_DROP_in_dropViewStatement699); if (state.failed) return statement;
                    match(input,VIEW,FOLLOW_VIEW_in_dropViewStatement701); if (state.failed) return statement;
                    viewName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropViewStatement705); if (state.failed) return statement;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:184:3: ( LBRACKET indexName= NUMERIC RBRACKET | LBRACKET GENERALTABLEINDEX RBRACKET | )
                    int alt7=3;
                    int LA7_0 = input.LA(1);

                    if ( (LA7_0==LBRACKET) ) {
                        int LA7_1 = input.LA(2);

                        if ( (LA7_1==NUMERIC) ) {
                            alt7=1;
                        }
                        else if ( (LA7_1==GENERALTABLEINDEX) ) {
                            alt7=2;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return statement;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 7, 1, input);

                            throw nvae;
                        }
                    }
                    else if ( (LA7_0==EOF||LA7_0==SEMICOLON) ) {
                        alt7=3;
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return statement;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 7, 0, input);

                        throw nvae;
                    }
                    switch (alt7) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:185:4: LBRACKET indexName= NUMERIC RBRACKET
                            {
                            match(input,LBRACKET,FOLLOW_LBRACKET_in_dropViewStatement714); if (state.failed) return statement;
                            indexName=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_dropViewStatement718); if (state.failed) return statement;
                            match(input,RBRACKET,FOLLOW_RBRACKET_in_dropViewStatement720); if (state.failed) return statement;
                            if ( state.backtracking==0 ) {

                                               statement = new DropElement((viewName!=null?viewName.getText():null) + "_" +(indexName!=null?indexName.getText():null), DropElement.UNION_VIEW);
                                          
                            }

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:190:4: LBRACKET GENERALTABLEINDEX RBRACKET
                            {
                            match(input,LBRACKET,FOLLOW_LBRACKET_in_dropViewStatement753); if (state.failed) return statement;
                            match(input,GENERALTABLEINDEX,FOLLOW_GENERALTABLEINDEX_in_dropViewStatement755); if (state.failed) return statement;
                            match(input,RBRACKET,FOLLOW_RBRACKET_in_dropViewStatement757); if (state.failed) return statement;
                            if ( state.backtracking==0 ) {

                                               statement = new DropElement((viewName!=null?viewName.getText():null) + "_i", DropElement.UNION_VIEW);
                                          
                            }

                            }
                            break;
                        case 3 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:195:4: 
                            {
                            if ( state.backtracking==0 ) {

                              				  statement = new DropElement((viewName!=null?viewName.getText():null), DropElement.VIEW);
                              			
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:200:3: DROP MATERIALIZED VIEW viewName2= IDENTIFIER
                    {
                    match(input,DROP,FOLLOW_DROP_in_dropViewStatement802); if (state.failed) return statement;
                    match(input,MATERIALIZED,FOLLOW_MATERIALIZED_in_dropViewStatement804); if (state.failed) return statement;
                    match(input,VIEW,FOLLOW_VIEW_in_dropViewStatement806); if (state.failed) return statement;
                    viewName2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropViewStatement810); if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                   statement = new DropElement((viewName2!=null?viewName2.getText():null), DropElement.TABLEORCOMMON_RANDOM_TABLE);
                              
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 7, dropViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "dropViewStatement"


    // $ANTLR start "dropVGFunctionStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:206:1: dropVGFunctionStatement returns [Expression statement] : DROP VGFUNCTION vgName= IDENTIFIER ;
    public final Expression dropVGFunctionStatement() throws RecognitionException {
        Expression statement = null;
        int dropVGFunctionStatement_StartIndex = input.index();
        Token vgName=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 8) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:206:54: ( DROP VGFUNCTION vgName= IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:207:3: DROP VGFUNCTION vgName= IDENTIFIER
            {
            match(input,DROP,FOLLOW_DROP_in_dropVGFunctionStatement837); if (state.failed) return statement;
            match(input,VGFUNCTION,FOLLOW_VGFUNCTION_in_dropVGFunctionStatement839); if (state.failed) return statement;
            vgName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropVGFunctionStatement843); if (state.failed) return statement;
            if ( state.backtracking==0 ) {

              			  statement = new DropElement((vgName!=null?vgName.getText():null), DropElement.VGFUNC);	
              		
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 8, dropVGFunctionStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "dropVGFunctionStatement"


    // $ANTLR start "dropFunctionStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:212:1: dropFunctionStatement returns [Expression statement] : DROP FUNCTION fgName= IDENTIFIER ;
    public final Expression dropFunctionStatement() throws RecognitionException {
        Expression statement = null;
        int dropFunctionStatement_StartIndex = input.index();
        Token fgName=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 9) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:212:52: ( DROP FUNCTION fgName= IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:213:9: DROP FUNCTION fgName= IDENTIFIER
            {
            match(input,DROP,FOLLOW_DROP_in_dropFunctionStatement868); if (state.failed) return statement;
            match(input,FUNCTION,FOLLOW_FUNCTION_in_dropFunctionStatement870); if (state.failed) return statement;
            fgName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_dropFunctionStatement874); if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                            statement = new DropElement((fgName!=null?fgName.getText():null), DropElement.FUNC); 
                      
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 9, dropFunctionStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "dropFunctionStatement"


    // $ANTLR start "selectStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:221:1: selectStatement returns [SelectStatement statement] : SELECT setQuantifier selectList fromClause whereClause groupByClause havingClause orderByClause ;
    public final SelectStatement selectStatement() throws RecognitionException {
        SelectStatement statement = null;
        int selectStatement_StartIndex = input.index();
        int setQuantifier15 = 0;

        ArrayList<SQLExpression> selectList16 = null;

        ArrayList<SQLExpression> fromClause17 = null;

        BooleanPredicate whereClause18 = null;

        ArrayList<ColumnExpression> groupByClause19 = null;

        BooleanPredicate havingClause20 = null;

        ArrayList<OrderByColumn> orderByClause21 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 10) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:221:52: ( SELECT setQuantifier selectList fromClause whereClause groupByClause havingClause orderByClause )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:222:12: SELECT setQuantifier selectList fromClause whereClause groupByClause havingClause orderByClause
            {
            match(input,SELECT,FOLLOW_SELECT_in_selectStatement915); if (state.failed) return statement;
            pushFollow(FOLLOW_setQuantifier_in_selectStatement917);
            setQuantifier15=setQuantifier();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_selectList_in_selectStatement919);
            selectList16=selectList();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_fromClause_in_selectStatement932);
            fromClause17=fromClause();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_whereClause_in_selectStatement946);
            whereClause18=whereClause();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_groupByClause_in_selectStatement960);
            groupByClause19=groupByClause();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_havingClause_in_selectStatement974);
            havingClause20=havingClause();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_orderByClause_in_selectStatement987);
            orderByClause21=orderByClause();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                              statement = new SelectStatement(setQuantifier15, 
              				                                     selectList16,
              				                                     fromClause17,
              				                                     whereClause18,
              				                                     groupByClause19,
              				                                     havingClause20,
              				                                     orderByClause21);
                         
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 10, selectStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "selectStatement"


    // $ANTLR start "setQuantifier"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:240:1: setQuantifier returns [int setQuantifier] : ( ALL | DISTINCT | );
    public final int setQuantifier() throws RecognitionException {
        int setQuantifier = 0;
        int setQuantifier_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 11) ) { return setQuantifier; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:240:41: ( ALL | DISTINCT | )
            int alt9=3;
            alt9 = dfa9.predict(input);
            switch (alt9) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:241:13: ALL
                    {
                    match(input,ALL,FOLLOW_ALL_in_setQuantifier1023); if (state.failed) return setQuantifier;
                    if ( state.backtracking==0 ) {

                                       setQuantifier = FinalVariable.ALL;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:246:13: DISTINCT
                    {
                    match(input,DISTINCT,FOLLOW_DISTINCT_in_setQuantifier1065); if (state.failed) return setQuantifier;
                    if ( state.backtracking==0 ) {

                                        setQuantifier = FinalVariable.DISTINCT;
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:251:13: 
                    {
                    if ( state.backtracking==0 ) {

                                       setQuantifier = FinalVariable.ALL;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 11, setQuantifier_StartIndex); }
        }
        return setQuantifier;
    }
    // $ANTLR end "setQuantifier"


    // $ANTLR start "selectList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:259:1: selectList returns [ArrayList<SQLExpression> selectList] : selectVarious ;
    public final ArrayList<SQLExpression> selectList() throws RecognitionException {
        ArrayList<SQLExpression> selectList = null;
        int selectList_StartIndex = input.index();
        ArrayList<SQLExpression> selectVarious22 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 12) ) { return selectList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:259:57: ( selectVarious )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:260:4: selectVarious
            {
            pushFollow(FOLLOW_selectVarious_in_selectList1124);
            selectVarious22=selectVarious();

            state._fsp--;
            if (state.failed) return selectList;
            if ( state.backtracking==0 ) {

               			      selectList = selectVarious22;
               			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 12, selectList_StartIndex); }
        }
        return selectList;
    }
    // $ANTLR end "selectList"


    // $ANTLR start "selectVarious"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:265:1: selectVarious returns [ArrayList<SQLExpression> selectList] : l1= selectSubList ( COMMA l2= selectSubList )* ;
    public final ArrayList<SQLExpression> selectVarious() throws RecognitionException {
        ArrayList<SQLExpression> selectList = null;
        int selectVarious_StartIndex = input.index();
        SQLExpression l1 = null;

        SQLExpression l2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 13) ) { return selectList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:265:60: (l1= selectSubList ( COMMA l2= selectSubList )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:266:13: l1= selectSubList ( COMMA l2= selectSubList )*
            {
            pushFollow(FOLLOW_selectSubList_in_selectVarious1156);
            l1=selectSubList();

            state._fsp--;
            if (state.failed) return selectList;
            if ( state.backtracking==0 ) {

                          	 selectList = new ArrayList<SQLExpression>();
                          	 selectList.add(l1);
                          
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:271:13: ( COMMA l2= selectSubList )*
            loop10:
            do {
                int alt10=2;
                alt10 = dfa10.predict(input);
                switch (alt10) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:271:14: COMMA l2= selectSubList
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_selectVarious1185); if (state.failed) return selectList;
            	    pushFollow(FOLLOW_selectSubList_in_selectVarious1189);
            	    l2=selectSubList();

            	    state._fsp--;
            	    if (state.failed) return selectList;
            	    if ( state.backtracking==0 ) {

            	                       selectList.add(l2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop10;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 13, selectVarious_StartIndex); }
        }
        return selectList;
    }
    // $ANTLR end "selectVarious"


    // $ANTLR start "selectSubList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:276:1: selectSubList returns [SQLExpression vcolumn] : ( derivedColumn | IDENTIFIER DOT ASTERISK | ASTERISK );
    public final SQLExpression selectSubList() throws RecognitionException {
        SQLExpression vcolumn = null;
        int selectSubList_StartIndex = input.index();
        Token IDENTIFIER24=null;
        SQLExpression derivedColumn23 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 14) ) { return vcolumn; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:276:46: ( derivedColumn | IDENTIFIER DOT ASTERISK | ASTERISK )
            int alt11=3;
            alt11 = dfa11.predict(input);
            switch (alt11) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:277:13: derivedColumn
                    {
                    pushFollow(FOLLOW_derivedColumn_in_selectSubList1240);
                    derivedColumn23=derivedColumn();

                    state._fsp--;
                    if (state.failed) return vcolumn;
                    if ( state.backtracking==0 ) {

                                       vcolumn = derivedColumn23;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:282:13: IDENTIFIER DOT ASTERISK
                    {
                    IDENTIFIER24=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_selectSubList1282); if (state.failed) return vcolumn;
                    match(input,DOT,FOLLOW_DOT_in_selectSubList1284); if (state.failed) return vcolumn;
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_selectSubList1286); if (state.failed) return vcolumn;
                    if ( state.backtracking==0 ) {

                                       vcolumn = new AllFromTable((IDENTIFIER24!=null?IDENTIFIER24.getText():null));
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:287:13: ASTERISK
                    {
                    match(input,ASTERISK,FOLLOW_ASTERISK_in_selectSubList1328); if (state.failed) return vcolumn;
                    if ( state.backtracking==0 ) {

                       				  vcolumn = new AsteriskTable(); 		      
                       			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 14, selectSubList_StartIndex); }
        }
        return vcolumn;
    }
    // $ANTLR end "selectSubList"


    // $ANTLR start "derivedColumn"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:292:1: derivedColumn returns [SQLExpression vcolumn] : ( valueExpression ( ( AS )? IDENTIFIER | ) | IDENTIFIER EQUALS caseExpression | caseExpression );
    public final SQLExpression derivedColumn() throws RecognitionException {
        SQLExpression vcolumn = null;
        int derivedColumn_StartIndex = input.index();
        Token IDENTIFIER26=null;
        Token IDENTIFIER28=null;
        QueryParser.valueExpression_return valueExpression25 = null;

        QueryParser.caseExpression_return caseExpression27 = null;

        QueryParser.caseExpression_return caseExpression29 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 15) ) { return vcolumn; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:292:46: ( valueExpression ( ( AS )? IDENTIFIER | ) | IDENTIFIER EQUALS caseExpression | caseExpression )
            int alt14=3;
            alt14 = dfa14.predict(input);
            switch (alt14) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:293:13: valueExpression ( ( AS )? IDENTIFIER | )
                    {
                    pushFollow(FOLLOW_valueExpression_in_derivedColumn1358);
                    valueExpression25=valueExpression();

                    state._fsp--;
                    if (state.failed) return vcolumn;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:294:13: ( ( AS )? IDENTIFIER | )
                    int alt13=2;
                    alt13 = dfa13.predict(input);
                    switch (alt13) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:295:14: ( AS )? IDENTIFIER
                            {
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:295:14: ( AS )?
                            int alt12=2;
                            int LA12_0 = input.LA(1);

                            if ( (LA12_0==AS) ) {
                                alt12=1;
                            }
                            switch (alt12) {
                                case 1 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: AS
                                    {
                                    match(input,AS,FOLLOW_AS_in_derivedColumn1387); if (state.failed) return vcolumn;

                                    }
                                    break;

                            }

                            IDENTIFIER26=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_derivedColumn1390); if (state.failed) return vcolumn;
                            if ( state.backtracking==0 ) {

                              	                 vcolumn = new DerivedColumn((valueExpression25!=null?valueExpression25.expression:null), (IDENTIFIER26!=null?IDENTIFIER26.getText():null));
                              	            
                            }

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:300:14: 
                            {
                            if ( state.backtracking==0 ) {

                              	           	     vcolumn = new DerivedColumn((valueExpression25!=null?valueExpression25.expression:null), (valueExpression25!=null?input.toString(valueExpression25.start,valueExpression25.stop):null));
                              	           	
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:305:10: IDENTIFIER EQUALS caseExpression
                    {
                    IDENTIFIER28=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_derivedColumn1468); if (state.failed) return vcolumn;
                    match(input,EQUALS,FOLLOW_EQUALS_in_derivedColumn1470); if (state.failed) return vcolumn;
                    pushFollow(FOLLOW_caseExpression_in_derivedColumn1472);
                    caseExpression27=caseExpression();

                    state._fsp--;
                    if (state.failed) return vcolumn;
                    if ( state.backtracking==0 ) {

                      	        	vcolumn = new DerivedColumn((caseExpression27!=null?caseExpression27.expression:null), (IDENTIFIER28!=null?IDENTIFIER28.getText():null));
                      	        
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:310:10: caseExpression
                    {
                    pushFollow(FOLLOW_caseExpression_in_derivedColumn1505);
                    caseExpression29=caseExpression();

                    state._fsp--;
                    if (state.failed) return vcolumn;
                    if ( state.backtracking==0 ) {

                      	        	vcolumn = new DerivedColumn((caseExpression29!=null?caseExpression29.expression:null), (caseExpression29!=null?input.toString(caseExpression29.start,caseExpression29.stop):null));
                      	        
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 15, derivedColumn_StartIndex); }
        }
        return vcolumn;
    }
    // $ANTLR end "derivedColumn"


    // $ANTLR start "fromClause"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:320:1: fromClause returns [ArrayList<SQLExpression> tableReferenceList] : ( FROM tableReferenceList | );
    public final ArrayList<SQLExpression> fromClause() throws RecognitionException {
        ArrayList<SQLExpression> tableReferenceList = null;
        int fromClause_StartIndex = input.index();
        ArrayList<SQLExpression> tableReferenceList30 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 16) ) { return tableReferenceList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:320:65: ( FROM tableReferenceList | )
            int alt15=2;
            int LA15_0 = input.LA(1);

            if ( (LA15_0==FROM) ) {
                alt15=1;
            }
            else if ( (LA15_0==EOF||LA15_0==SEMICOLON||LA15_0==RPAREN||(LA15_0>=WHERE && LA15_0<=GROUP)||LA15_0==ORDER||LA15_0==HAVING) ) {
                alt15=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return tableReferenceList;}
                NoViableAltException nvae =
                    new NoViableAltException("", 15, 0, input);

                throw nvae;
            }
            switch (alt15) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:321:4: FROM tableReferenceList
                    {
                    match(input,FROM,FOLLOW_FROM_in_fromClause1568); if (state.failed) return tableReferenceList;
                    pushFollow(FOLLOW_tableReferenceList_in_fromClause1570);
                    tableReferenceList30=tableReferenceList();

                    state._fsp--;
                    if (state.failed) return tableReferenceList;
                    if ( state.backtracking==0 ) {

                      				tableReferenceList =  tableReferenceList30;
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:326:4: 
                    {
                    if ( state.backtracking==0 ) {

                      				tableReferenceList = new ArrayList<SQLExpression>();
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 16, fromClause_StartIndex); }
        }
        return tableReferenceList;
    }
    // $ANTLR end "fromClause"


    // $ANTLR start "tableReferenceList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:330:1: tableReferenceList returns [ArrayList<SQLExpression> tableReferenceList] : tr1= tableReference ( COMMA tr2= tableReference )* ;
    public final ArrayList<SQLExpression> tableReferenceList() throws RecognitionException {
        ArrayList<SQLExpression> tableReferenceList = null;
        int tableReferenceList_StartIndex = input.index();
        SQLExpression tr1 = null;

        SQLExpression tr2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 17) ) { return tableReferenceList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:330:73: (tr1= tableReference ( COMMA tr2= tableReference )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:331:13: tr1= tableReference ( COMMA tr2= tableReference )*
            {
            pushFollow(FOLLOW_tableReference_in_tableReferenceList1622);
            tr1=tableReference();

            state._fsp--;
            if (state.failed) return tableReferenceList;
            if ( state.backtracking==0 ) {

                          	 tableReferenceList = new ArrayList<SQLExpression>();
                               tableReferenceList.add(tr1);
                          
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:336:13: ( COMMA tr2= tableReference )*
            loop16:
            do {
                int alt16=2;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==COMMA) ) {
                    alt16=1;
                }


                switch (alt16) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:336:14: COMMA tr2= tableReference
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_tableReferenceList1651); if (state.failed) return tableReferenceList;
            	    pushFollow(FOLLOW_tableReference_in_tableReferenceList1655);
            	    tr2=tableReference();

            	    state._fsp--;
            	    if (state.failed) return tableReferenceList;
            	    if ( state.backtracking==0 ) {

            	                       tableReferenceList.add(tr2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop16;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 17, tableReferenceList_StartIndex); }
        }
        return tableReferenceList;
    }
    // $ANTLR end "tableReferenceList"


    // $ANTLR start "tableReference"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:341:1: tableReference returns [SQLExpression expression] : ( IDENTIFIER | tableName= IDENTIFIER ( AS )? aliasName= IDENTIFIER | indexTableReference ( AS )? aliasName= IDENTIFIER | indexTableReference | subquery ( AS aliasName= IDENTIFIER ) | tempTable | tempTable ( AS )? aliasName= IDENTIFIER );
    public final SQLExpression tableReference() throws RecognitionException {
        SQLExpression expression = null;
        int tableReference_StartIndex = input.index();
        Token tableName=null;
        Token aliasName=null;
        Token IDENTIFIER31=null;
        QueryParser.indexTableReference_return indexTableReference32 = null;

        QueryParser.indexTableReference_return indexTableReference33 = null;

        MathExpression subquery34 = null;

        QueryParser.tempTable_return tempTable35 = null;

        QueryParser.tempTable_return tempTable36 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 18) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:341:50: ( IDENTIFIER | tableName= IDENTIFIER ( AS )? aliasName= IDENTIFIER | indexTableReference ( AS )? aliasName= IDENTIFIER | indexTableReference | subquery ( AS aliasName= IDENTIFIER ) | tempTable | tempTable ( AS )? aliasName= IDENTIFIER )
            int alt20=7;
            alt20 = dfa20.predict(input);
            switch (alt20) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:342:13: IDENTIFIER
                    {
                    IDENTIFIER31=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference1706); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new TableReference((IDENTIFIER31!=null?IDENTIFIER31.getText():null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:347:13: tableName= IDENTIFIER ( AS )? aliasName= IDENTIFIER
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference1750); if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:347:34: ( AS )?
                    int alt17=2;
                    int LA17_0 = input.LA(1);

                    if ( (LA17_0==AS) ) {
                        alt17=1;
                    }
                    switch (alt17) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: AS
                            {
                            match(input,AS,FOLLOW_AS_in_tableReference1752); if (state.failed) return expression;

                            }
                            break;

                    }

                    aliasName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference1757); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new TableReference((tableName!=null?tableName.getText():null), (aliasName!=null?aliasName.getText():null));
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:354:13: indexTableReference ( AS )? aliasName= IDENTIFIER
                    {
                    pushFollow(FOLLOW_indexTableReference_in_tableReference1813);
                    indexTableReference32=indexTableReference();

                    state._fsp--;
                    if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:354:33: ( AS )?
                    int alt18=2;
                    int LA18_0 = input.LA(1);

                    if ( (LA18_0==AS) ) {
                        alt18=1;
                    }
                    switch (alt18) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: AS
                            {
                            match(input,AS,FOLLOW_AS_in_tableReference1815); if (state.failed) return expression;

                            }
                            break;

                    }

                    aliasName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference1820); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new TableReference((indexTableReference32!=null?indexTableReference32.table:null),
                      		                  (aliasName!=null?aliasName.getText():null),
                      		                  (indexTableReference32!=null?indexTableReference32.index:null), 
                      		                  (indexTableReference32!=null?indexTableReference32.type:0), 
                      		                  (indexTableReference32!=null?indexTableReference32.indexMathExp:null));
                                  
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:363:13: indexTableReference
                    {
                    pushFollow(FOLLOW_indexTableReference_in_tableReference1862);
                    indexTableReference33=indexTableReference();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                      	            expression = new TableReference((indexTableReference33!=null?indexTableReference33.table:null), 
                      		                 (indexTableReference33!=null?indexTableReference33.table:null), 
                      		                 (indexTableReference33!=null?indexTableReference33.index:null), 
                      		                 (indexTableReference33!=null?indexTableReference33.type:0),
                      		                 (indexTableReference33!=null?indexTableReference33.indexMathExp:null));
                                  
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:374:13: subquery ( AS aliasName= IDENTIFIER )
                    {
                    pushFollow(FOLLOW_subquery_in_tableReference1918);
                    subquery34=subquery();

                    state._fsp--;
                    if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:375:13: ( AS aliasName= IDENTIFIER )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:376:14: AS aliasName= IDENTIFIER
                    {
                    match(input,AS,FOLLOW_AS_in_tableReference1947); if (state.failed) return expression;
                    aliasName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference1951); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  		expression = new FromSubquery(subquery34, (aliasName!=null?aliasName.getText():null));
                                  	
                    }

                    }


                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:382:13: tempTable
                    {
                    pushFollow(FOLLOW_tempTable_in_tableReference2008);
                    tempTable35=tempTable();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	ValuesTable valuesTable = (tempTable35!=null?tempTable35.valuesTable:null);
                                  	boolean typeCheck = ValuesTableHelper.visitValuesTable(valuesTable);
                                  	if(!typeCheck)
                                  	{
                                  		System.err.println("ValuesTable defintion for \r\n" + valuesTable.toString() + " \r\n wrong!");
                                  		
                                  	}
                                  	
                                  	String valueTableName = ValuesTableHelper.getValuesTableName((tempTable35!=null?input.toString(tempTable35.start,tempTable35.stop):null));
                                  	expression = new TableReference(valueTableName);
                                  	ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
                                  
                    }

                    }
                    break;
                case 7 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:397:13: tempTable ( AS )? aliasName= IDENTIFIER
                    {
                    pushFollow(FOLLOW_tempTable_in_tableReference2050);
                    tempTable36=tempTable();

                    state._fsp--;
                    if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:397:23: ( AS )?
                    int alt19=2;
                    int LA19_0 = input.LA(1);

                    if ( (LA19_0==AS) ) {
                        alt19=1;
                    }
                    switch (alt19) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: AS
                            {
                            match(input,AS,FOLLOW_AS_in_tableReference2052); if (state.failed) return expression;

                            }
                            break;

                    }

                    aliasName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tableReference2059); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	ValuesTable valuesTable = (tempTable36!=null?tempTable36.valuesTable:null);
                                  	boolean typeCheck = ValuesTableHelper.visitValuesTable(valuesTable);
                                  	if(!typeCheck)
                                  	{
                                  		System.err.println("ValuesTable defintion for \r\n" + valuesTable.toString() + " \r\n wrong!");
                                  		reportError(new RecognitionException());
                                  	}
                                  	
                                  	String valueTableName = ValuesTableHelper.getValuesTableName((tempTable36!=null?input.toString(tempTable36.start,tempTable36.stop):null));
                                  	expression = new TableReference(valueTableName, (aliasName!=null?aliasName.getText():null));
                                  	ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 18, tableReference_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "tableReference"

    public static class indexTableReference_return extends ParserRuleReturnScope {
        public String table;
        public String index;
        public int type;
        public MathExpression indexMathExp;
    };

    // $ANTLR start "indexTableReference"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:412:1: indexTableReference returns [String table, String index, int type, MathExpression indexMathExp] : (tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET valueExpression RBRACKET );
    public final QueryParser.indexTableReference_return indexTableReference() throws RecognitionException {
        QueryParser.indexTableReference_return retval = new QueryParser.indexTableReference_return();
        retval.start = input.LT(1);
        int indexTableReference_StartIndex = input.index();
        Token tableName=null;
        Token indexString=null;
        QueryParser.valueExpression_return valueExpression37 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 19) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:412:96: (tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET valueExpression RBRACKET )
            int alt21=2;
            alt21 = dfa21.predict(input);
            switch (alt21) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:413:13: tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_indexTableReference2110); if (state.failed) return retval;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_indexTableReference2112); if (state.failed) return retval;
                    indexString=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_indexTableReference2116); if (state.failed) return retval;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_indexTableReference2118); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                        retval.table = (tableName!=null?tableName.getText():null)+"_"+(indexString!=null?indexString.getText():null);
                                        retval.index = (indexString!=null?indexString.getText():null);
                                        retval.type = TableReference.CONSTANT_INDEX_TABLE;
                                        retval.indexMathExp = null;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:421:13: tableName= IDENTIFIER LBRACKET valueExpression RBRACKET
                    {
                    tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_indexTableReference2162); if (state.failed) return retval;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_indexTableReference2164); if (state.failed) return retval;
                    pushFollow(FOLLOW_valueExpression_in_indexTableReference2166);
                    valueExpression37=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_indexTableReference2168); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                         retval.table = (tableName!=null?tableName.getText():null) + "_i";
                                         retval.index = null;
                                         retval.type = TableReference.GENERAL_INDEX_TABLE;
                                         retval.indexMathExp =  (valueExpression37!=null?valueExpression37.expression:null);
                                  
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 19, indexTableReference_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "indexTableReference"

    public static class tempTable_return extends ParserRuleReturnScope {
        public ValuesTable valuesTable;
    };

    // $ANTLR start "tempTable"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:429:1: tempTable returns [ValuesTable valuesTable] : VALUES ( tempTableRow | tempTableRowList ) ;
    public final QueryParser.tempTable_return tempTable() throws RecognitionException {
        QueryParser.tempTable_return retval = new QueryParser.tempTable_return();
        retval.start = input.LT(1);
        int tempTable_StartIndex = input.index();
        ArrayList<MathExpression> tempTableRow38 = null;

        ArrayList<ArrayList<MathExpression>> tempTableRowList39 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 20) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:429:44: ( VALUES ( tempTableRow | tempTableRowList ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:430:7: VALUES ( tempTableRow | tempTableRowList )
            {
            match(input,VALUES,FOLLOW_VALUES_in_tempTable2211); if (state.failed) return retval;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:431:7: ( tempTableRow | tempTableRowList )
            int alt22=2;
            int LA22_0 = input.LA(1);

            if ( (LA22_0==LPAREN) ) {
                int LA22_1 = input.LA(2);

                if ( (LA22_1==LPAREN) ) {
                    alt22=2;
                }
                else if ( (LA22_1==NUMERIC||LA22_1==STRING||(LA22_1>=PLUS && LA22_1<=MINUS)) ) {
                    alt22=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return retval;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 22, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 22, 0, input);

                throw nvae;
            }
            switch (alt22) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:432:9: tempTableRow
                    {
                    pushFollow(FOLLOW_tempTableRow_in_tempTable2230);
                    tempTableRow38=tempTableRow();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      			    	 ArrayList<MathExpression> tempTableRow = tempTableRow38;
                      			    	 ArrayList<ArrayList<MathExpression>> tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
                      			    	 tempTableRowList.add(tempTableRow);
                      			     	 retval.valuesTable = new ValuesTable(tempTableRowList);
                      		    	 
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:440:9: tempTableRowList
                    {
                    pushFollow(FOLLOW_tempTableRowList_in_tempTable2260);
                    tempTableRowList39=tempTableRowList();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      			    	 retval.valuesTable = new ValuesTable(tempTableRowList39);
                      			    	
                      			     
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 20, tempTable_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "tempTable"


    // $ANTLR start "tempTableRowList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:448:1: tempTableRowList returns [ArrayList<ArrayList<MathExpression>> tempTableRowList] : LPAREN tr1= tempTableRow ( COMMA tr2= tempTableRow )* RPAREN ;
    public final ArrayList<ArrayList<MathExpression>> tempTableRowList() throws RecognitionException {
        ArrayList<ArrayList<MathExpression>> tempTableRowList = null;
        int tempTableRowList_StartIndex = input.index();
        ArrayList<MathExpression> tr1 = null;

        ArrayList<MathExpression> tr2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 21) ) { return tempTableRowList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:448:81: ( LPAREN tr1= tempTableRow ( COMMA tr2= tempTableRow )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:449:4: LPAREN tr1= tempTableRow ( COMMA tr2= tempTableRow )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_tempTableRowList2305); if (state.failed) return tempTableRowList;
            pushFollow(FOLLOW_tempTableRow_in_tempTableRowList2314);
            tr1=tempTableRow();

            state._fsp--;
            if (state.failed) return tempTableRowList;
            if ( state.backtracking==0 ) {

                               tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
                               tempTableRowList.add(tr1);
                          
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:455:13: ( COMMA tr2= tempTableRow )*
            loop23:
            do {
                int alt23=2;
                int LA23_0 = input.LA(1);

                if ( (LA23_0==COMMA) ) {
                    alt23=1;
                }


                switch (alt23) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:455:14: COMMA tr2= tempTableRow
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_tempTableRowList2343); if (state.failed) return tempTableRowList;
            	    pushFollow(FOLLOW_tempTableRow_in_tempTableRowList2349);
            	    tr2=tempTableRow();

            	    state._fsp--;
            	    if (state.failed) return tempTableRowList;
            	    if ( state.backtracking==0 ) {

            	                       tempTableRowList.add(tr2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop23;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_tempTableRowList2379); if (state.failed) return tempTableRowList;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 21, tempTableRowList_StartIndex); }
        }
        return tempTableRowList;
    }
    // $ANTLR end "tempTableRowList"


    // $ANTLR start "tempTableRow"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:461:1: tempTableRow returns [ArrayList<MathExpression> tempTableRow] : LPAREN tr1= constantValue ( COMMA tr2= constantValue )* RPAREN ;
    public final ArrayList<MathExpression> tempTableRow() throws RecognitionException {
        ArrayList<MathExpression> tempTableRow = null;
        int tempTableRow_StartIndex = input.index();
        MathExpression tr1 = null;

        MathExpression tr2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 22) ) { return tempTableRow; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:461:61: ( LPAREN tr1= constantValue ( COMMA tr2= constantValue )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:462:7: LPAREN tr1= constantValue ( COMMA tr2= constantValue )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_tempTableRow2407); if (state.failed) return tempTableRow;
            pushFollow(FOLLOW_constantValue_in_tempTableRow2416);
            tr1=constantValue();

            state._fsp--;
            if (state.failed) return tempTableRow;
            if ( state.backtracking==0 ) {

              			    tempTableRow = new ArrayList<MathExpression>();
              				tempTableRow.add(tr1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:468:4: ( COMMA tr2= constantValue )*
            loop24:
            do {
                int alt24=2;
                int LA24_0 = input.LA(1);

                if ( (LA24_0==COMMA) ) {
                    alt24=1;
                }


                switch (alt24) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:468:5: COMMA tr2= constantValue
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_tempTableRow2427); if (state.failed) return tempTableRow;
            	    pushFollow(FOLLOW_constantValue_in_tempTableRow2433);
            	    tr2=constantValue();

            	    state._fsp--;
            	    if (state.failed) return tempTableRow;
            	    if ( state.backtracking==0 ) {

            	                       tempTableRow.add(tr2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop24;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_tempTableRow2463); if (state.failed) return tempTableRow;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 22, tempTableRow_StartIndex); }
        }
        return tempTableRow;
    }
    // $ANTLR end "tempTableRow"


    // $ANTLR start "constantValue"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:474:1: constantValue returns [MathExpression expression] : ( STRING | signedNumeric );
    public final MathExpression constantValue() throws RecognitionException {
        MathExpression expression = null;
        int constantValue_StartIndex = input.index();
        Token STRING40=null;
        MathExpression signedNumeric41 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 23) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:474:50: ( STRING | signedNumeric )
            int alt25=2;
            int LA25_0 = input.LA(1);

            if ( (LA25_0==STRING) ) {
                alt25=1;
            }
            else if ( (LA25_0==NUMERIC||(LA25_0>=PLUS && LA25_0<=MINUS)) ) {
                alt25=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 25, 0, input);

                throw nvae;
            }
            switch (alt25) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:475:10: STRING
                    {
                    STRING40=(Token)match(input,STRING,FOLLOW_STRING_in_constantValue2489); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new StringExpression((STRING40!=null?STRING40.getText():null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:480:13: signedNumeric
                    {
                    pushFollow(FOLLOW_signedNumeric_in_constantValue2531);
                    signedNumeric41=signedNumeric();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {
                       
                                       expression = signedNumeric41;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 23, constantValue_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "constantValue"


    // $ANTLR start "whereClause"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:488:1: whereClause returns [BooleanPredicate predicate] : ( WHERE searchCondition | );
    public final BooleanPredicate whereClause() throws RecognitionException {
        BooleanPredicate predicate = null;
        int whereClause_StartIndex = input.index();
        BooleanPredicate searchCondition42 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 24) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:488:49: ( WHERE searchCondition | )
            int alt26=2;
            int LA26_0 = input.LA(1);

            if ( (LA26_0==WHERE) ) {
                alt26=1;
            }
            else if ( (LA26_0==EOF||LA26_0==SEMICOLON||LA26_0==RPAREN||LA26_0==GROUP||LA26_0==ORDER||LA26_0==HAVING) ) {
                alt26=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return predicate;}
                NoViableAltException nvae =
                    new NoViableAltException("", 26, 0, input);

                throw nvae;
            }
            switch (alt26) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:489:13: WHERE searchCondition
                    {
                    match(input,WHERE,FOLLOW_WHERE_in_whereClause2594); if (state.failed) return predicate;
                    pushFollow(FOLLOW_searchCondition_in_whereClause2596);
                    searchCondition42=searchCondition();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                                       predicate = searchCondition42;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:494:13: 
                    {
                    if ( state.backtracking==0 ) {

                                       predicate = null;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 24, whereClause_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "whereClause"


    // $ANTLR start "groupByClause"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:502:1: groupByClause returns [ArrayList<ColumnExpression> columnList] : ( GROUP BY groupingColumns | );
    public final ArrayList<ColumnExpression> groupByClause() throws RecognitionException {
        ArrayList<ColumnExpression> columnList = null;
        int groupByClause_StartIndex = input.index();
        ArrayList<ColumnExpression> groupingColumns43 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 25) ) { return columnList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:502:63: ( GROUP BY groupingColumns | )
            int alt27=2;
            int LA27_0 = input.LA(1);

            if ( (LA27_0==GROUP) ) {
                alt27=1;
            }
            else if ( (LA27_0==EOF||LA27_0==SEMICOLON||LA27_0==RPAREN||LA27_0==ORDER||LA27_0==HAVING) ) {
                alt27=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return columnList;}
                NoViableAltException nvae =
                    new NoViableAltException("", 27, 0, input);

                throw nvae;
            }
            switch (alt27) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:503:13: GROUP BY groupingColumns
                    {
                    match(input,GROUP,FOLLOW_GROUP_in_groupByClause2691); if (state.failed) return columnList;
                    match(input,BY,FOLLOW_BY_in_groupByClause2693); if (state.failed) return columnList;
                    pushFollow(FOLLOW_groupingColumns_in_groupByClause2695);
                    groupingColumns43=groupingColumns();

                    state._fsp--;
                    if (state.failed) return columnList;
                    if ( state.backtracking==0 ) {

                                       columnList = groupingColumns43;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:508:13: 
                    {
                    if ( state.backtracking==0 ) {

                                       columnList = null;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 25, groupByClause_StartIndex); }
        }
        return columnList;
    }
    // $ANTLR end "groupByClause"


    // $ANTLR start "groupingColumns"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:513:1: groupingColumns returns [ArrayList<ColumnExpression> columnList] : ce1= columnExpression ( COMMA ce2= columnExpression )* ;
    public final ArrayList<ColumnExpression> groupingColumns() throws RecognitionException {
        ArrayList<ColumnExpression> columnList = null;
        int groupingColumns_StartIndex = input.index();
        MathExpression ce1 = null;

        MathExpression ce2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 26) ) { return columnList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:513:65: (ce1= columnExpression ( COMMA ce2= columnExpression )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:514:13: ce1= columnExpression ( COMMA ce2= columnExpression )*
            {
            pushFollow(FOLLOW_columnExpression_in_groupingColumns2787);
            ce1=columnExpression();

            state._fsp--;
            if (state.failed) return columnList;
            if ( state.backtracking==0 ) {

                          	 columnList = new ArrayList<ColumnExpression>();
                               columnList.add((ColumnExpression)ce1);
                          
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:519:13: ( COMMA ce2= columnExpression )*
            loop28:
            do {
                int alt28=2;
                int LA28_0 = input.LA(1);

                if ( (LA28_0==COMMA) ) {
                    alt28=1;
                }


                switch (alt28) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:519:14: COMMA ce2= columnExpression
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_groupingColumns2816); if (state.failed) return columnList;
            	    pushFollow(FOLLOW_columnExpression_in_groupingColumns2820);
            	    ce2=columnExpression();

            	    state._fsp--;
            	    if (state.failed) return columnList;
            	    if ( state.backtracking==0 ) {

            	                       columnList.add((ColumnExpression)ce2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop28;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 26, groupingColumns_StartIndex); }
        }
        return columnList;
    }
    // $ANTLR end "groupingColumns"


    // $ANTLR start "orderByClause"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:528:1: orderByClause returns [ArrayList<OrderByColumn> columnList] : ( ORDER BY orderByColumns | );
    public final ArrayList<OrderByColumn> orderByClause() throws RecognitionException {
        ArrayList<OrderByColumn> columnList = null;
        int orderByClause_StartIndex = input.index();
        ArrayList<OrderByColumn> orderByColumns44 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 27) ) { return columnList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:528:59: ( ORDER BY orderByColumns | )
            int alt29=2;
            int LA29_0 = input.LA(1);

            if ( (LA29_0==ORDER) ) {
                alt29=1;
            }
            else if ( (LA29_0==EOF||LA29_0==SEMICOLON||LA29_0==RPAREN) ) {
                alt29=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return columnList;}
                NoViableAltException nvae =
                    new NoViableAltException("", 29, 0, input);

                throw nvae;
            }
            switch (alt29) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:529:4: ORDER BY orderByColumns
                    {
                    match(input,ORDER,FOLLOW_ORDER_in_orderByClause2866); if (state.failed) return columnList;
                    match(input,BY,FOLLOW_BY_in_orderByClause2868); if (state.failed) return columnList;
                    pushFollow(FOLLOW_orderByColumns_in_orderByClause2870);
                    orderByColumns44=orderByColumns();

                    state._fsp--;
                    if (state.failed) return columnList;
                    if ( state.backtracking==0 ) {

                      				columnList = orderByColumns44;
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:534:4: 
                    {
                    if ( state.backtracking==0 ) {

                      				columnList = null;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 27, orderByClause_StartIndex); }
        }
        return columnList;
    }
    // $ANTLR end "orderByClause"


    // $ANTLR start "orderByColumns"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:538:1: orderByColumns returns [ArrayList<OrderByColumn> columnList] : ve1= valueExpression ( | ASC | DESC ) ( COMMA ve2= valueExpression ( | ASC | DESC ) )* ;
    public final ArrayList<OrderByColumn> orderByColumns() throws RecognitionException {
        ArrayList<OrderByColumn> columnList = null;
        int orderByColumns_StartIndex = input.index();
        QueryParser.valueExpression_return ve1 = null;

        QueryParser.valueExpression_return ve2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 28) ) { return columnList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:538:61: (ve1= valueExpression ( | ASC | DESC ) ( COMMA ve2= valueExpression ( | ASC | DESC ) )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:539:4: ve1= valueExpression ( | ASC | DESC ) ( COMMA ve2= valueExpression ( | ASC | DESC ) )*
            {
            if ( state.backtracking==0 ) {

              				
              			
            }
            pushFollow(FOLLOW_valueExpression_in_orderByColumns2906);
            ve1=valueExpression();

            state._fsp--;
            if (state.failed) return columnList;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:542:24: ( | ASC | DESC )
            int alt30=3;
            switch ( input.LA(1) ) {
            case EOF:
            case SEMICOLON:
            case COMMA:
            case RPAREN:
                {
                alt30=1;
                }
                break;
            case ASC:
                {
                alt30=2;
                }
                break;
            case DESC:
                {
                alt30=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return columnList;}
                NoViableAltException nvae =
                    new NoViableAltException("", 30, 0, input);

                throw nvae;
            }

            switch (alt30) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:543:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					 columnList = new ArrayList<OrderByColumn>();
                      	                 columnList.add(new OrderByColumn((ve1!=null?ve1.expression:null), FinalVariable.ASC));
                      	            
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:547:14: ASC
                    {
                    match(input,ASC,FOLLOW_ASC_in_orderByColumns2930); if (state.failed) return columnList;
                    if ( state.backtracking==0 ) {

                      	            	 columnList = new ArrayList<OrderByColumn>();
                      	                 columnList.add(new OrderByColumn((ve1!=null?ve1.expression:null), FinalVariable.ASC));
                      	            
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:552:14: DESC
                    {
                    match(input,DESC,FOLLOW_DESC_in_orderByColumns2961); if (state.failed) return columnList;
                    if ( state.backtracking==0 ) {

                      	            	 columnList = new ArrayList<OrderByColumn>();
                      	                 columnList.add(new OrderByColumn((ve1!=null?ve1.expression:null), FinalVariable.DESC));
                      	            
                    }

                    }
                    break;

            }

            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:557:10: ( COMMA ve2= valueExpression ( | ASC | DESC ) )*
            loop32:
            do {
                int alt32=2;
                int LA32_0 = input.LA(1);

                if ( (LA32_0==COMMA) ) {
                    alt32=1;
                }


                switch (alt32) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:557:11: COMMA ve2= valueExpression ( | ASC | DESC )
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_orderByColumns2989); if (state.failed) return columnList;
            	    pushFollow(FOLLOW_valueExpression_in_orderByColumns2993);
            	    ve2=valueExpression();

            	    state._fsp--;
            	    if (state.failed) return columnList;
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:557:37: ( | ASC | DESC )
            	    int alt31=3;
            	    switch ( input.LA(1) ) {
            	    case EOF:
            	    case SEMICOLON:
            	    case COMMA:
            	    case RPAREN:
            	        {
            	        alt31=1;
            	        }
            	        break;
            	    case ASC:
            	        {
            	        alt31=2;
            	        }
            	        break;
            	    case DESC:
            	        {
            	        alt31=3;
            	        }
            	        break;
            	    default:
            	        if (state.backtracking>0) {state.failed=true; return columnList;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 31, 0, input);

            	        throw nvae;
            	    }

            	    switch (alt31) {
            	        case 1 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:558:11: 
            	            {
            	            if ( state.backtracking==0 ) {

            	              	                 columnList.add(new OrderByColumn((ve2!=null?ve2.expression:null), FinalVariable.ASC));
            	              	            
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:561:14: ASC
            	            {
            	            match(input,ASC,FOLLOW_ASC_in_orderByColumns3023); if (state.failed) return columnList;
            	            if ( state.backtracking==0 ) {

            	              	                 columnList.add(new OrderByColumn((ve2!=null?ve2.expression:null), FinalVariable.ASC));
            	              	            
            	            }

            	            }
            	            break;
            	        case 3 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:565:14: DESC
            	            {
            	            match(input,DESC,FOLLOW_DESC_in_orderByColumns3054); if (state.failed) return columnList;
            	            if ( state.backtracking==0 ) {

            	              	                 columnList.add(new OrderByColumn((ve2!=null?ve2.expression:null), FinalVariable.DESC));
            	              	            
            	            }

            	            }
            	            break;

            	    }


            	    }
            	    break;

            	default :
            	    break loop32;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 28, orderByColumns_StartIndex); }
        }
        return columnList;
    }
    // $ANTLR end "orderByColumns"


    // $ANTLR start "havingClause"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:574:1: havingClause returns [BooleanPredicate predicate] : ( | HAVING searchCondition );
    public final BooleanPredicate havingClause() throws RecognitionException {
        BooleanPredicate predicate = null;
        int havingClause_StartIndex = input.index();
        BooleanPredicate searchCondition45 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 29) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:574:49: ( | HAVING searchCondition )
            int alt33=2;
            int LA33_0 = input.LA(1);

            if ( (LA33_0==EOF||LA33_0==SEMICOLON||LA33_0==RPAREN||LA33_0==ORDER) ) {
                alt33=1;
            }
            else if ( (LA33_0==HAVING) ) {
                alt33=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return predicate;}
                NoViableAltException nvae =
                    new NoViableAltException("", 33, 0, input);

                throw nvae;
            }
            switch (alt33) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:575:4: 
                    {
                    if ( state.backtracking==0 ) {

                                       predicate = null;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:579:13: HAVING searchCondition
                    {
                    match(input,HAVING,FOLLOW_HAVING_in_havingClause3129); if (state.failed) return predicate;
                    pushFollow(FOLLOW_searchCondition_in_havingClause3131);
                    searchCondition45=searchCondition();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                                       predicate = searchCondition45;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 29, havingClause_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "havingClause"


    // $ANTLR start "subquery"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:587:1: subquery returns [MathExpression expression] : LPAREN selectStatement RPAREN ;
    public final MathExpression subquery() throws RecognitionException {
        MathExpression expression = null;
        int subquery_StartIndex = input.index();
        SelectStatement selectStatement46 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 30) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:587:45: ( LPAREN selectStatement RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:588:4: LPAREN selectStatement RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_subquery3180); if (state.failed) return expression;
            pushFollow(FOLLOW_selectStatement_in_subquery3185);
            selectStatement46=selectStatement();

            state._fsp--;
            if (state.failed) return expression;
            if ( state.backtracking==0 ) {

              				expression = new SubqueryExpression(selectStatement46);
              			
            }
            match(input,RPAREN,FOLLOW_RPAREN_in_subquery3195); if (state.failed) return expression;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 30, subquery_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "subquery"


    // $ANTLR start "createRandomTableStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:600:1: createRandomTableStatement returns [RandomTableStatement statement] : CREATE TABLE ( nonIndexRandomStatement | baselineArrayRandomStatement | baselineRandomStatement | generalRandomStatement ) ;
    public final RandomTableStatement createRandomTableStatement() throws RecognitionException {
        RandomTableStatement statement = null;
        int createRandomTableStatement_StartIndex = input.index();
        RandomTableStatement nonIndexRandomStatement47 = null;

        RandomTableStatement baselineArrayRandomStatement48 = null;

        RandomTableStatement baselineRandomStatement49 = null;

        RandomTableStatement generalRandomStatement50 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 31) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:600:67: ( CREATE TABLE ( nonIndexRandomStatement | baselineArrayRandomStatement | baselineRandomStatement | generalRandomStatement ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:601:11: CREATE TABLE ( nonIndexRandomStatement | baselineArrayRandomStatement | baselineRandomStatement | generalRandomStatement )
            {
            match(input,CREATE,FOLLOW_CREATE_in_createRandomTableStatement3220); if (state.failed) return statement;
            match(input,TABLE,FOLLOW_TABLE_in_createRandomTableStatement3222); if (state.failed) return statement;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:602:8: ( nonIndexRandomStatement | baselineArrayRandomStatement | baselineRandomStatement | generalRandomStatement )
            int alt34=4;
            int LA34_0 = input.LA(1);

            if ( (LA34_0==IDENTIFIER) ) {
                int LA34_1 = input.LA(2);

                if ( (LA34_1==LBRACKET) ) {
                    int LA34_2 = input.LA(3);

                    if ( (LA34_2==NUMERIC) ) {
                        alt34=3;
                    }
                    else if ( (LA34_2==GENERALTABLEINDEX) ) {
                        int LA34_6 = input.LA(4);

                        if ( (synpred55_Query()) ) {
                            alt34=2;
                        }
                        else if ( (true) ) {
                            alt34=4;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return statement;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 34, 6, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return statement;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 34, 2, input);

                        throw nvae;
                    }
                }
                else if ( (LA34_1==AS||LA34_1==LPAREN) ) {
                    alt34=1;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 34, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 34, 0, input);

                throw nvae;
            }
            switch (alt34) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:603:12: nonIndexRandomStatement
                    {
                    pushFollow(FOLLOW_nonIndexRandomStatement_in_createRandomTableStatement3245);
                    nonIndexRandomStatement47=nonIndexRandomStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	            statement = nonIndexRandomStatement47;
                      	          
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:610:12: baselineArrayRandomStatement
                    {
                    pushFollow(FOLLOW_baselineArrayRandomStatement_in_createRandomTableStatement3303);
                    baselineArrayRandomStatement48=baselineArrayRandomStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	            statement = baselineArrayRandomStatement48;
                      	          
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:615:12: baselineRandomStatement
                    {
                    pushFollow(FOLLOW_baselineRandomStatement_in_createRandomTableStatement3342);
                    baselineRandomStatement49=baselineRandomStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	            statement = baselineRandomStatement49;
                      	          
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:620:12: generalRandomStatement
                    {
                    pushFollow(FOLLOW_generalRandomStatement_in_createRandomTableStatement3381);
                    generalRandomStatement50=generalRandomStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                      	            statement = generalRandomStatement50;
                      	          
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 31, createRandomTableStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createRandomTableStatement"


    // $ANTLR start "nonIndexRandomStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:629:1: nonIndexRandomStatement returns [RandomTableStatement statement] : schema AS randomParameters ;
    public final RandomTableStatement nonIndexRandomStatement() throws RecognitionException {
        RandomTableStatement statement = null;
        int nonIndexRandomStatement_StartIndex = input.index();
        DefinedTableSchema schema51 = null;

        QueryParser.randomParameters_return randomParameters52 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 32) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:629:64: ( schema AS randomParameters )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:630:11: schema AS randomParameters
            {
            pushFollow(FOLLOW_schema_in_nonIndexRandomStatement3452);
            schema51=schema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_nonIndexRandomStatement3454); if (state.failed) return statement;
            pushFollow(FOLLOW_randomParameters_in_nonIndexRandomStatement3466);
            randomParameters52=randomParameters();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                          statement = new RandomTableStatement(schema51,
                                             (randomParameters52!=null?randomParameters52.tableReference:null),
                                             (randomParameters52!=null?randomParameters52.withStatementList:null),
                                             (randomParameters52!=null?randomParameters52.statement:null));
                        
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 32, nonIndexRandomStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "nonIndexRandomStatement"


    // $ANTLR start "baselineRandomStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:642:1: baselineRandomStatement returns [RandomTableStatement statement] : baselineSchema AS randomParameters ;
    public final RandomTableStatement baselineRandomStatement() throws RecognitionException {
        RandomTableStatement statement = null;
        int baselineRandomStatement_StartIndex = input.index();
        QueryParser.baselineSchema_return baselineSchema53 = null;

        QueryParser.randomParameters_return randomParameters54 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 33) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:642:64: ( baselineSchema AS randomParameters )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:643:11: baselineSchema AS randomParameters
            {
            pushFollow(FOLLOW_baselineSchema_in_baselineRandomStatement3526);
            baselineSchema53=baselineSchema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_baselineRandomStatement3528); if (state.failed) return statement;
            pushFollow(FOLLOW_randomParameters_in_baselineRandomStatement3540);
            randomParameters54=randomParameters();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                          statement = new BaseLineRandomTableStatement((baselineSchema53!=null?baselineSchema53.index:null),
                                             (baselineSchema53!=null?baselineSchema53.schema:null),
                                             (randomParameters54!=null?randomParameters54.tableReference:null),
                                             (randomParameters54!=null?randomParameters54.withStatementList:null),
                                             (randomParameters54!=null?randomParameters54.statement:null));
                        
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 33, baselineRandomStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "baselineRandomStatement"


    // $ANTLR start "baselineArrayRandomStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:653:1: baselineArrayRandomStatement returns [RandomTableStatement statement] : baselineArraySchema AS randomParameters ;
    public final RandomTableStatement baselineArrayRandomStatement() throws RecognitionException {
        RandomTableStatement statement = null;
        int baselineArrayRandomStatement_StartIndex = input.index();
        QueryParser.baselineArraySchema_return baselineArraySchema55 = null;

        QueryParser.randomParameters_return randomParameters56 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 34) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:653:69: ( baselineArraySchema AS randomParameters )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:654:12: baselineArraySchema AS randomParameters
            {
            pushFollow(FOLLOW_baselineArraySchema_in_baselineArrayRandomStatement3583);
            baselineArraySchema55=baselineArraySchema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_baselineArrayRandomStatement3585); if (state.failed) return statement;
            pushFollow(FOLLOW_randomParameters_in_baselineArrayRandomStatement3598);
            randomParameters56=randomParameters();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                            statement = new BaseLineArrayRandomTableStatement((baselineArraySchema55!=null?baselineArraySchema55.arrayString:null),
                                             (baselineArraySchema55!=null?baselineArraySchema55.schema:null),
                                             (randomParameters56!=null?randomParameters56.tableReference:null),
                                             (randomParameters56!=null?randomParameters56.withStatementList:null),
                                             (randomParameters56!=null?randomParameters56.statement:null));
                         
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 34, baselineArrayRandomStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "baselineArrayRandomStatement"


    // $ANTLR start "generalRandomStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:664:1: generalRandomStatement returns [RandomTableStatement statement] : generalSchema AS randomParameters ;
    public final RandomTableStatement generalRandomStatement() throws RecognitionException {
        RandomTableStatement statement = null;
        int generalRandomStatement_StartIndex = input.index();
        DefinedTableSchema generalSchema57 = null;

        QueryParser.randomParameters_return randomParameters58 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 35) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:664:63: ( generalSchema AS randomParameters )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:665:11: generalSchema AS randomParameters
            {
            pushFollow(FOLLOW_generalSchema_in_generalRandomStatement3641);
            generalSchema57=generalSchema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_generalRandomStatement3643); if (state.failed) return statement;
            pushFollow(FOLLOW_randomParameters_in_generalRandomStatement3655);
            randomParameters58=randomParameters();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                          statement = new GeneralRandomTableStatement(generalSchema57,
                                             (randomParameters58!=null?randomParameters58.tableReference:null),
                                             (randomParameters58!=null?randomParameters58.withStatementList:null),
                                             (randomParameters58!=null?randomParameters58.statement:null));
                        
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 35, generalRandomStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "generalRandomStatement"

    public static class baselineSchema_return extends ParserRuleReturnScope {
        public DefinedTableSchema schema;
        public String index;
    };

    // $ANTLR start "baselineSchema"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:674:1: baselineSchema returns [DefinedTableSchema schema, String index] : schemaName LBRACKET NUMERIC RBRACKET ( LPAREN attributeList RPAREN | ) ;
    public final QueryParser.baselineSchema_return baselineSchema() throws RecognitionException {
        QueryParser.baselineSchema_return retval = new QueryParser.baselineSchema_return();
        retval.start = input.LT(1);
        int baselineSchema_StartIndex = input.index();
        Token NUMERIC59=null;
        String schemaName60 = null;

        ArrayList<String> attributeList61 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 36) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:674:64: ( schemaName LBRACKET NUMERIC RBRACKET ( LPAREN attributeList RPAREN | ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:675:7: schemaName LBRACKET NUMERIC RBRACKET ( LPAREN attributeList RPAREN | )
            {
            pushFollow(FOLLOW_schemaName_in_baselineSchema3694);
            schemaName60=schemaName();

            state._fsp--;
            if (state.failed) return retval;
            match(input,LBRACKET,FOLLOW_LBRACKET_in_baselineSchema3696); if (state.failed) return retval;
            NUMERIC59=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_baselineSchema3698); if (state.failed) return retval;
            match(input,RBRACKET,FOLLOW_RBRACKET_in_baselineSchema3700); if (state.failed) return retval;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:676:7: ( LPAREN attributeList RPAREN | )
            int alt35=2;
            int LA35_0 = input.LA(1);

            if ( (LA35_0==LPAREN) ) {
                alt35=1;
            }
            else if ( (LA35_0==AS) ) {
                alt35=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 35, 0, input);

                throw nvae;
            }
            switch (alt35) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:677:9: LPAREN attributeList RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_baselineSchema3718); if (state.failed) return retval;
                    pushFollow(FOLLOW_attributeList_in_baselineSchema3728);
                    attributeList61=attributeList();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,RPAREN,FOLLOW_RPAREN_in_baselineSchema3738); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                retval.index = (NUMERIC59!=null?NUMERIC59.getText():null);
                                retval.schema = new DefinedTableSchema(schemaName60+"_"+retval.index, attributeList61, true);
                              
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:685:9: 
                    {
                    if ( state.backtracking==0 ) {

                                retval.index = (NUMERIC59!=null?NUMERIC59.getText():null);
                                retval.schema = new DefinedTableSchema(schemaName60+"_"+retval.index, true);
                              
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 36, baselineSchema_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "baselineSchema"

    public static class baselineArraySchema_return extends ParserRuleReturnScope {
        public DefinedArrayTableSchema schema;
        public String arrayString;
    };

    // $ANTLR start "baselineArraySchema"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:692:1: baselineArraySchema returns [DefinedArrayTableSchema schema, String arrayString] : schemaName LBRACKET GENERALTABLEINDEX COLON lb= NUMERIC TWODOT ub= NUMERIC RBRACKET ( LPAREN attributeList RPAREN | ) ;
    public final QueryParser.baselineArraySchema_return baselineArraySchema() throws RecognitionException {
        QueryParser.baselineArraySchema_return retval = new QueryParser.baselineArraySchema_return();
        retval.start = input.LT(1);
        int baselineArraySchema_StartIndex = input.index();
        Token lb=null;
        Token ub=null;
        String schemaName62 = null;

        ArrayList<String> attributeList63 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 37) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:692:80: ( schemaName LBRACKET GENERALTABLEINDEX COLON lb= NUMERIC TWODOT ub= NUMERIC RBRACKET ( LPAREN attributeList RPAREN | ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:693:7: schemaName LBRACKET GENERALTABLEINDEX COLON lb= NUMERIC TWODOT ub= NUMERIC RBRACKET ( LPAREN attributeList RPAREN | )
            {
            pushFollow(FOLLOW_schemaName_in_baselineArraySchema3806);
            schemaName62=schemaName();

            state._fsp--;
            if (state.failed) return retval;
            match(input,LBRACKET,FOLLOW_LBRACKET_in_baselineArraySchema3808); if (state.failed) return retval;
            match(input,GENERALTABLEINDEX,FOLLOW_GENERALTABLEINDEX_in_baselineArraySchema3810); if (state.failed) return retval;
            match(input,COLON,FOLLOW_COLON_in_baselineArraySchema3812); if (state.failed) return retval;
            lb=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_baselineArraySchema3816); if (state.failed) return retval;
            match(input,TWODOT,FOLLOW_TWODOT_in_baselineArraySchema3818); if (state.failed) return retval;
            ub=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_baselineArraySchema3822); if (state.failed) return retval;
            match(input,RBRACKET,FOLLOW_RBRACKET_in_baselineArraySchema3824); if (state.failed) return retval;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:694:7: ( LPAREN attributeList RPAREN | )
            int alt36=2;
            int LA36_0 = input.LA(1);

            if ( (LA36_0==LPAREN) ) {
                alt36=1;
            }
            else if ( (LA36_0==AS) ) {
                alt36=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 36, 0, input);

                throw nvae;
            }
            switch (alt36) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:695:9: LPAREN attributeList RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_baselineArraySchema3842); if (state.failed) return retval;
                    pushFollow(FOLLOW_attributeList_in_baselineArraySchema3852);
                    attributeList63=attributeList();

                    state._fsp--;
                    if (state.failed) return retval;
                    match(input,RPAREN,FOLLOW_RPAREN_in_baselineArraySchema3862); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                retval.arrayString = (lb!=null?lb.getText():null)+ "src/main" +(ub!=null?ub.getText():null);
                                retval.schema = new DefinedArrayTableSchema(schemaName62, 
                                                                      (lb!=null?lb.getText():null)+ "src/main" +(ub!=null?ub.getText():null),
                                                                      attributeList63);
                              
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:705:9: 
                    {
                    if ( state.backtracking==0 ) {

                                retval.arrayString = (lb!=null?lb.getText():null)+ "src/main" +(ub!=null?ub.getText():null);
                                retval.schema = new DefinedArrayTableSchema(schemaName62, 
                                                                      (lb!=null?lb.getText():null)+ "src/main" +(ub!=null?ub.getText():null));
                              
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 37, baselineArraySchema_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "baselineArraySchema"


    // $ANTLR start "generalSchema"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:713:1: generalSchema returns [DefinedTableSchema schema] : schemaName LBRACKET GENERALTABLEINDEX RBRACKET ( LPAREN attributeList RPAREN | ) ;
    public final DefinedTableSchema generalSchema() throws RecognitionException {
        DefinedTableSchema schema = null;
        int generalSchema_StartIndex = input.index();
        String schemaName64 = null;

        ArrayList<String> attributeList65 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 38) ) { return schema; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:713:49: ( schemaName LBRACKET GENERALTABLEINDEX RBRACKET ( LPAREN attributeList RPAREN | ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:714:7: schemaName LBRACKET GENERALTABLEINDEX RBRACKET ( LPAREN attributeList RPAREN | )
            {
            pushFollow(FOLLOW_schemaName_in_generalSchema3930);
            schemaName64=schemaName();

            state._fsp--;
            if (state.failed) return schema;
            match(input,LBRACKET,FOLLOW_LBRACKET_in_generalSchema3932); if (state.failed) return schema;
            match(input,GENERALTABLEINDEX,FOLLOW_GENERALTABLEINDEX_in_generalSchema3934); if (state.failed) return schema;
            match(input,RBRACKET,FOLLOW_RBRACKET_in_generalSchema3936); if (state.failed) return schema;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:715:7: ( LPAREN attributeList RPAREN | )
            int alt37=2;
            int LA37_0 = input.LA(1);

            if ( (LA37_0==LPAREN) ) {
                alt37=1;
            }
            else if ( (LA37_0==AS) ) {
                alt37=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return schema;}
                NoViableAltException nvae =
                    new NoViableAltException("", 37, 0, input);

                throw nvae;
            }
            switch (alt37) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:716:9: LPAREN attributeList RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_generalSchema3954); if (state.failed) return schema;
                    pushFollow(FOLLOW_attributeList_in_generalSchema3964);
                    attributeList65=attributeList();

                    state._fsp--;
                    if (state.failed) return schema;
                    match(input,RPAREN,FOLLOW_RPAREN_in_generalSchema3974); if (state.failed) return schema;
                    if ( state.backtracking==0 ) {

                                schema = new DefinedTableSchema(schemaName64+"_i", attributeList65, true);
                              
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:723:9: 
                    {
                    if ( state.backtracking==0 ) {

                                schema = new DefinedTableSchema(schemaName64+"_i", true);
                              
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 38, generalSchema_StartIndex); }
        }
        return schema;
    }
    // $ANTLR end "generalSchema"

    public static class randomParameters_return extends ParserRuleReturnScope {
        public SQLExpression tableReference;
        public ArrayList<WithStatement> withStatementList;
        public SelectStatement statement;
    };

    // $ANTLR start "randomParameters"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:735:1: randomParameters returns [SQLExpression tableReference,\n ArrayList<WithStatement> withStatementList,\n SelectStatement statement] : ( FOR forsentense withStatements selectStatement | withStatements selectStatement );
    public final QueryParser.randomParameters_return randomParameters() throws RecognitionException {
        QueryParser.randomParameters_return retval = new QueryParser.randomParameters_return();
        retval.start = input.LT(1);
        int randomParameters_StartIndex = input.index();
        SQLExpression forsentense66 = null;

        ArrayList<WithStatement> withStatements67 = null;

        SelectStatement selectStatement68 = null;

        ArrayList<WithStatement> withStatements69 = null;

        SelectStatement selectStatement70 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 39) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:737:52: ( FOR forsentense withStatements selectStatement | withStatements selectStatement )
            int alt38=2;
            int LA38_0 = input.LA(1);

            if ( (LA38_0==FOR) ) {
                alt38=1;
            }
            else if ( (LA38_0==SELECT||LA38_0==WITH) ) {
                alt38=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return retval;}
                NoViableAltException nvae =
                    new NoViableAltException("", 38, 0, input);

                throw nvae;
            }
            switch (alt38) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:738:11: FOR forsentense withStatements selectStatement
                    {
                    match(input,FOR,FOLLOW_FOR_in_randomParameters4048); if (state.failed) return retval;
                    pushFollow(FOLLOW_forsentense_in_randomParameters4050);
                    forsentense66=forsentense();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_withStatements_in_randomParameters4077);
                    withStatements67=withStatements();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_selectStatement_in_randomParameters4089);
                    selectStatement68=selectStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                      retval.tableReference = forsentense66;
                                      retval.withStatementList = withStatements67;
                                      retval.statement = selectStatement68;
                                
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:747:11: withStatements selectStatement
                    {
                    pushFollow(FOLLOW_withStatements_in_randomParameters4126);
                    withStatements69=withStatements();

                    state._fsp--;
                    if (state.failed) return retval;
                    pushFollow(FOLLOW_selectStatement_in_randomParameters4138);
                    selectStatement70=selectStatement();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                      ArrayList<MathExpression> tempTableRow = new ArrayList<MathExpression>();
                                      tempTableRow.add(new NumericExpression("0"));
                                      ArrayList<ArrayList<MathExpression>> tempTableRowList = new ArrayList<ArrayList<MathExpression>>();
                                      tempTableRowList.add(tempTableRow);
                                      ValuesTable valuesTable = new ValuesTable(tempTableRowList);
                                   
                                      String valueTableName = ValuesTableHelper.getValuesTableName();
                      	              retval.tableReference = new TableReference(valueTableName, valueTableName);
                      	              ValuesTableHelper.addValuesTable(valueTableName, valuesTable);
                      	              
                      	              retval.withStatementList = withStatements69;
                                      retval.statement = selectStatement70;
                                
                    }

                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 39, randomParameters_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "randomParameters"


    // $ANTLR start "forsentense"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:767:1: forsentense returns [SQLExpression tableReference] : EACH tupleName IN tableReference ;
    public final SQLExpression forsentense() throws RecognitionException {
        SQLExpression tableReference = null;
        int forsentense_StartIndex = input.index();
        SQLExpression tableReference71 = null;

        String tupleName72 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 40) ) { return tableReference; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:767:51: ( EACH tupleName IN tableReference )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:768:4: EACH tupleName IN tableReference
            {
            match(input,EACH,FOLLOW_EACH_in_forsentense4175); if (state.failed) return tableReference;
            pushFollow(FOLLOW_tupleName_in_forsentense4177);
            tupleName72=tupleName();

            state._fsp--;
            if (state.failed) return tableReference;
            match(input,IN,FOLLOW_IN_in_forsentense4179); if (state.failed) return tableReference;
            pushFollow(FOLLOW_tableReference_in_forsentense4181);
            tableReference71=tableReference();

            state._fsp--;
            if (state.failed) return tableReference;
            if ( state.backtracking==0 ) {

              				tableReference = tableReference71;
              				if(tableReference instanceof TableReference)
              			    {
              					 ((TableReference)tableReference).setAlias(tupleName72);
              			    }
              			    else if(tableReference instanceof FromSubquery)
              			    {
              			   		  ((FromSubquery)tableReference).setAlias(tupleName72);
              			    }
              				   
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 40, forsentense_StartIndex); }
        }
        return tableReference;
    }
    // $ANTLR end "forsentense"


    // $ANTLR start "schema"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:782:1: schema returns [DefinedTableSchema schema] : schemaName ( LPAREN attributeList RPAREN | ) ;
    public final DefinedTableSchema schema() throws RecognitionException {
        DefinedTableSchema schema = null;
        int schema_StartIndex = input.index();
        String schemaName73 = null;

        ArrayList<String> attributeList74 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 41) ) { return schema; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:782:42: ( schemaName ( LPAREN attributeList RPAREN | ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:783:4: schemaName ( LPAREN attributeList RPAREN | )
            {
            pushFollow(FOLLOW_schemaName_in_schema4210);
            schemaName73=schemaName();

            state._fsp--;
            if (state.failed) return schema;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:784:4: ( LPAREN attributeList RPAREN | )
            int alt39=2;
            int LA39_0 = input.LA(1);

            if ( (LA39_0==LPAREN) ) {
                alt39=1;
            }
            else if ( (LA39_0==AS) ) {
                alt39=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return schema;}
                NoViableAltException nvae =
                    new NoViableAltException("", 39, 0, input);

                throw nvae;
            }
            switch (alt39) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:785:5: LPAREN attributeList RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_schema4221); if (state.failed) return schema;
                    pushFollow(FOLLOW_attributeList_in_schema4227);
                    attributeList74=attributeList();

                    state._fsp--;
                    if (state.failed) return schema;
                    match(input,RPAREN,FOLLOW_RPAREN_in_schema4233); if (state.failed) return schema;
                    if ( state.backtracking==0 ) {

                      					schema = new DefinedTableSchema(schemaName73, attributeList74, false);
                      				
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:792:5: 
                    {
                    if ( state.backtracking==0 ) {

                      					schema = new DefinedTableSchema(schemaName73, false);
                      				
                    }

                    }
                    break;

            }


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 41, schema_StartIndex); }
        }
        return schema;
    }
    // $ANTLR end "schema"


    // $ANTLR start "schemaName"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:798:1: schemaName returns [String name] : IDENTIFIER ;
    public final String schemaName() throws RecognitionException {
        String name = null;
        int schemaName_StartIndex = input.index();
        Token IDENTIFIER75=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 42) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:798:32: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:799:4: IDENTIFIER
            {
            IDENTIFIER75=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_schemaName4274); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              			    name = (IDENTIFIER75!=null?IDENTIFIER75.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 42, schemaName_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "schemaName"


    // $ANTLR start "attributeList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:804:1: attributeList returns [ArrayList<String> attributeList] : at1= attribute ( COMMA at2= attribute )* ;
    public final ArrayList<String> attributeList() throws RecognitionException {
        ArrayList<String> attributeList = null;
        int attributeList_StartIndex = input.index();
        String at1 = null;

        String at2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 43) ) { return attributeList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:804:55: (at1= attribute ( COMMA at2= attribute )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:805:4: at1= attribute ( COMMA at2= attribute )*
            {
            pushFollow(FOLLOW_attribute_in_attributeList4299);
            at1=attribute();

            state._fsp--;
            if (state.failed) return attributeList;
            if ( state.backtracking==0 ) {

              				attributeList = new ArrayList<String>();
              				attributeList.add(at1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:810:4: ( COMMA at2= attribute )*
            loop40:
            do {
                int alt40=2;
                int LA40_0 = input.LA(1);

                if ( (LA40_0==COMMA) ) {
                    alt40=1;
                }


                switch (alt40) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:810:5: COMMA at2= attribute
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_attributeList4310); if (state.failed) return attributeList;
            	    pushFollow(FOLLOW_attribute_in_attributeList4316);
            	    at2=attribute();

            	    state._fsp--;
            	    if (state.failed) return attributeList;
            	    if ( state.backtracking==0 ) {

            	      					attributeList.add(at2);
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop40;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 43, attributeList_StartIndex); }
        }
        return attributeList;
    }
    // $ANTLR end "attributeList"


    // $ANTLR start "attribute"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:816:1: attribute returns [String name] : IDENTIFIER ;
    public final String attribute() throws RecognitionException {
        String name = null;
        int attribute_StartIndex = input.index();
        Token IDENTIFIER76=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 44) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:816:31: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:817:4: IDENTIFIER
            {
            IDENTIFIER76=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_attribute4341); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              				name = (IDENTIFIER76!=null?IDENTIFIER76.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 44, attribute_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "attribute"


    // $ANTLR start "tupleName"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:822:1: tupleName returns [String name] : IDENTIFIER ;
    public final String tupleName() throws RecognitionException {
        String name = null;
        int tupleName_StartIndex = input.index();
        Token IDENTIFIER77=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 45) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:822:31: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:823:4: IDENTIFIER
            {
            IDENTIFIER77=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tupleName4367); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              				name = (IDENTIFIER77!=null?IDENTIFIER77.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 45, tupleName_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "tupleName"


    // $ANTLR start "withStatements"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:828:1: withStatements returns [ArrayList<WithStatement> withStatementList] : ( withStatement )* ;
    public final ArrayList<WithStatement> withStatements() throws RecognitionException {
        ArrayList<WithStatement> withStatementList = null;
        int withStatements_StartIndex = input.index();
        WithStatement withStatement78 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 46) ) { return withStatementList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:828:67: ( ( withStatement )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:829:4: ( withStatement )*
            {
            if ( state.backtracking==0 ) {

              				withStatementList = new ArrayList<WithStatement>();
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:832:4: ( withStatement )*
            loop41:
            do {
                int alt41=2;
                int LA41_0 = input.LA(1);

                if ( (LA41_0==WITH) ) {
                    alt41=1;
                }


                switch (alt41) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:833:4: withStatement
            	    {
            	    pushFollow(FOLLOW_withStatement_in_withStatements4395);
            	    withStatement78=withStatement();

            	    state._fsp--;
            	    if (state.failed) return withStatementList;
            	    if ( state.backtracking==0 ) {

            	      				withStatementList.add(withStatement78);
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    break loop41;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 46, withStatements_StartIndex); }
        }
        return withStatementList;
    }
    // $ANTLR end "withStatements"


    // $ANTLR start "withStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:839:1: withStatement returns [WithStatement statement] : WITH tempVGTable AS vgFunctionStatement ;
    public final WithStatement withStatement() throws RecognitionException {
        WithStatement statement = null;
        int withStatement_StartIndex = input.index();
        GeneralFunctionExpression vgFunctionStatement79 = null;

        String tempVGTable80 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 47) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:839:47: ( WITH tempVGTable AS vgFunctionStatement )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:840:4: WITH tempVGTable AS vgFunctionStatement
            {
            match(input,WITH,FOLLOW_WITH_in_withStatement4419); if (state.failed) return statement;
            pushFollow(FOLLOW_tempVGTable_in_withStatement4421);
            tempVGTable80=tempVGTable();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_withStatement4423); if (state.failed) return statement;
            pushFollow(FOLLOW_vgFunctionStatement_in_withStatement4425);
            vgFunctionStatement79=vgFunctionStatement();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

              				statement = new WithStatement(vgFunctionStatement79, tempVGTable80);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 47, withStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "withStatement"


    // $ANTLR start "tempVGTable"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:845:1: tempVGTable returns [String name] : IDENTIFIER ;
    public final String tempVGTable() throws RecognitionException {
        String name = null;
        int tempVGTable_StartIndex = input.index();
        Token IDENTIFIER81=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 48) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:845:33: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:846:4: IDENTIFIER
            {
            IDENTIFIER81=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_tempVGTable4443); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              				name = (IDENTIFIER81!=null?IDENTIFIER81.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 48, tempVGTable_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "tempVGTable"


    // $ANTLR start "vgFunctionStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:852:1: vgFunctionStatement returns [GeneralFunctionExpression expression] : generalFunction ;
    public final GeneralFunctionExpression vgFunctionStatement() throws RecognitionException {
        GeneralFunctionExpression expression = null;
        int vgFunctionStatement_StartIndex = input.index();
        GeneralFunctionExpression generalFunction82 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 49) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:852:66: ( generalFunction )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:853:4: generalFunction
            {
            pushFollow(FOLLOW_generalFunction_in_vgFunctionStatement4466);
            generalFunction82=generalFunction();

            state._fsp--;
            if (state.failed) return expression;
            if ( state.backtracking==0 ) {

              				// remember that this corresponds to a VG function call
              				expression = generalFunction82;
              				expression.setVGFunctionCall (true);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 49, vgFunctionStatement_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "vgFunctionStatement"


    // $ANTLR start "createViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:864:1: createViewStatement returns [Expression statement] : ( CREATE VIEW schema AS selectStatement | CREATE MATERIALIZED VIEW schema AS selectStatement );
    public final Expression createViewStatement() throws RecognitionException {
        Expression statement = null;
        int createViewStatement_StartIndex = input.index();
        DefinedTableSchema schema83 = null;

        SelectStatement selectStatement84 = null;

        DefinedTableSchema schema85 = null;

        SelectStatement selectStatement86 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 50) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:864:50: ( CREATE VIEW schema AS selectStatement | CREATE MATERIALIZED VIEW schema AS selectStatement )
            int alt42=2;
            int LA42_0 = input.LA(1);

            if ( (LA42_0==CREATE) ) {
                int LA42_1 = input.LA(2);

                if ( (LA42_1==VIEW) ) {
                    alt42=1;
                }
                else if ( (LA42_1==MATERIALIZED) ) {
                    alt42=2;
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 42, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 42, 0, input);

                throw nvae;
            }
            switch (alt42) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:865:13: CREATE VIEW schema AS selectStatement
                    {
                    match(input,CREATE,FOLLOW_CREATE_in_createViewStatement4502); if (state.failed) return statement;
                    match(input,VIEW,FOLLOW_VIEW_in_createViewStatement4504); if (state.failed) return statement;
                    pushFollow(FOLLOW_schema_in_createViewStatement4506);
                    schema83=schema();

                    state._fsp--;
                    if (state.failed) return statement;
                    match(input,AS,FOLLOW_AS_in_createViewStatement4508); if (state.failed) return statement;
                    pushFollow(FOLLOW_selectStatement_in_createViewStatement4522);
                    selectStatement84=selectStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                  	statement = new ViewStatement(schema83, selectStatement84);
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:871:13: CREATE MATERIALIZED VIEW schema AS selectStatement
                    {
                    match(input,CREATE,FOLLOW_CREATE_in_createViewStatement4564); if (state.failed) return statement;
                    match(input,MATERIALIZED,FOLLOW_MATERIALIZED_in_createViewStatement4566); if (state.failed) return statement;
                    match(input,VIEW,FOLLOW_VIEW_in_createViewStatement4568); if (state.failed) return statement;
                    pushFollow(FOLLOW_schema_in_createViewStatement4570);
                    schema85=schema();

                    state._fsp--;
                    if (state.failed) return statement;
                    match(input,AS,FOLLOW_AS_in_createViewStatement4572); if (state.failed) return statement;
                    pushFollow(FOLLOW_selectStatement_in_createViewStatement4586);
                    selectStatement86=selectStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                      statement = new MaterializedViewStatement(schema85, selectStatement86);
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 50, createViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createViewStatement"


    // $ANTLR start "createTableStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:880:1: createTableStatement returns [TableDefinitionStatement statement] : CREATE TABLE definedTableName table_contents_source ;
    public final TableDefinitionStatement createTableStatement() throws RecognitionException {
        TableDefinitionStatement statement = null;
        int createTableStatement_StartIndex = input.index();
        String definedTableName87 = null;

        ArrayList<SQLExpression> table_contents_source88 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 51) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:880:65: ( CREATE TABLE definedTableName table_contents_source )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:881:4: CREATE TABLE definedTableName table_contents_source
            {
            match(input,CREATE,FOLLOW_CREATE_in_createTableStatement4637); if (state.failed) return statement;
            match(input,TABLE,FOLLOW_TABLE_in_createTableStatement4639); if (state.failed) return statement;
            pushFollow(FOLLOW_definedTableName_in_createTableStatement4641);
            definedTableName87=definedTableName();

            state._fsp--;
            if (state.failed) return statement;
            pushFollow(FOLLOW_table_contents_source_in_createTableStatement4643);
            table_contents_source88=table_contents_source();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

              				statement = new TableDefinitionStatement(definedTableName87, 
              				                         table_contents_source88);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 51, createTableStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createTableStatement"


    // $ANTLR start "createVGFunctionStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:887:1: createVGFunctionStatement returns [VGFunctionDefinitionStatement statement] : CREATE VGFUNCTION i1= IDENTIFIER functionInAtts RETURNS vgFunctionOutAtts SOURCE file ;
    public final VGFunctionDefinitionStatement createVGFunctionStatement() throws RecognitionException {
        VGFunctionDefinitionStatement statement = null;
        int createVGFunctionStatement_StartIndex = input.index();
        Token i1=null;
        ArrayList<SQLExpression> functionInAtts89 = null;

        ArrayList<SQLExpression> vgFunctionOutAtts90 = null;

        String file91 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 52) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:887:75: ( CREATE VGFUNCTION i1= IDENTIFIER functionInAtts RETURNS vgFunctionOutAtts SOURCE file )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:888:4: CREATE VGFUNCTION i1= IDENTIFIER functionInAtts RETURNS vgFunctionOutAtts SOURCE file
            {
            match(input,CREATE,FOLLOW_CREATE_in_createVGFunctionStatement4664); if (state.failed) return statement;
            match(input,VGFUNCTION,FOLLOW_VGFUNCTION_in_createVGFunctionStatement4666); if (state.failed) return statement;
            i1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_createVGFunctionStatement4670); if (state.failed) return statement;
            pushFollow(FOLLOW_functionInAtts_in_createVGFunctionStatement4672);
            functionInAtts89=functionInAtts();

            state._fsp--;
            if (state.failed) return statement;
            match(input,RETURNS,FOLLOW_RETURNS_in_createVGFunctionStatement4674); if (state.failed) return statement;
            pushFollow(FOLLOW_vgFunctionOutAtts_in_createVGFunctionStatement4676);
            vgFunctionOutAtts90=vgFunctionOutAtts();

            state._fsp--;
            if (state.failed) return statement;
            match(input,SOURCE,FOLLOW_SOURCE_in_createVGFunctionStatement4678); if (state.failed) return statement;
            pushFollow(FOLLOW_file_in_createVGFunctionStatement4680);
            file91=file();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

              				statement = new VGFunctionDefinitionStatement((i1!=null?i1.getText():null), functionInAtts89,
              						vgFunctionOutAtts90, file91);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 52, createVGFunctionStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createVGFunctionStatement"


    // $ANTLR start "createFunctionStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:894:1: createFunctionStatement returns [FunctionDefinitionStatement statement] : CREATE FUNCTION i1= IDENTIFIER functionInAtts RETURNS returnType SOURCE file ;
    public final FunctionDefinitionStatement createFunctionStatement() throws RecognitionException {
        FunctionDefinitionStatement statement = null;
        int createFunctionStatement_StartIndex = input.index();
        Token i1=null;
        ArrayList<SQLExpression> functionInAtts92 = null;

        TypeInfo returnType93 = null;

        String file94 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 53) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:894:71: ( CREATE FUNCTION i1= IDENTIFIER functionInAtts RETURNS returnType SOURCE file )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:895:4: CREATE FUNCTION i1= IDENTIFIER functionInAtts RETURNS returnType SOURCE file
            {
            match(input,CREATE,FOLLOW_CREATE_in_createFunctionStatement4698); if (state.failed) return statement;
            match(input,FUNCTION,FOLLOW_FUNCTION_in_createFunctionStatement4700); if (state.failed) return statement;
            i1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_createFunctionStatement4704); if (state.failed) return statement;
            pushFollow(FOLLOW_functionInAtts_in_createFunctionStatement4706);
            functionInAtts92=functionInAtts();

            state._fsp--;
            if (state.failed) return statement;
            match(input,RETURNS,FOLLOW_RETURNS_in_createFunctionStatement4708); if (state.failed) return statement;
            pushFollow(FOLLOW_returnType_in_createFunctionStatement4710);
            returnType93=returnType();

            state._fsp--;
            if (state.failed) return statement;
            match(input,SOURCE,FOLLOW_SOURCE_in_createFunctionStatement4712); if (state.failed) return statement;
            pushFollow(FOLLOW_file_in_createFunctionStatement4714);
            file94=file();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

              				statement = new FunctionDefinitionStatement((i1!=null?i1.getText():null), functionInAtts92,
              					returnType93, file94);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 53, createFunctionStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createFunctionStatement"


    // $ANTLR start "createUnionViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:901:1: createUnionViewStatement returns [UnionViewStatement statement] : ( commonUnionViewStatement | baselineUnionViewStatement | generalUnionViewStatement );
    public final UnionViewStatement createUnionViewStatement() throws RecognitionException {
        UnionViewStatement statement = null;
        int createUnionViewStatement_StartIndex = input.index();
        ConstantUnionViewStatement commonUnionViewStatement95 = null;

        BaselineUnionViewStatement baselineUnionViewStatement96 = null;

        GeneralUnionViewStatement generalUnionViewStatement97 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 54) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:901:63: ( commonUnionViewStatement | baselineUnionViewStatement | generalUnionViewStatement )
            int alt43=3;
            int LA43_0 = input.LA(1);

            if ( (LA43_0==CREATE) ) {
                int LA43_1 = input.LA(2);

                if ( (LA43_1==VIEW) ) {
                    int LA43_2 = input.LA(3);

                    if ( (LA43_2==IDENTIFIER) ) {
                        int LA43_3 = input.LA(4);

                        if ( (synpred65_Query()) ) {
                            alt43=1;
                        }
                        else if ( (synpred66_Query()) ) {
                            alt43=2;
                        }
                        else if ( (true) ) {
                            alt43=3;
                        }
                        else {
                            if (state.backtracking>0) {state.failed=true; return statement;}
                            NoViableAltException nvae =
                                new NoViableAltException("", 43, 3, input);

                            throw nvae;
                        }
                    }
                    else {
                        if (state.backtracking>0) {state.failed=true; return statement;}
                        NoViableAltException nvae =
                            new NoViableAltException("", 43, 2, input);

                        throw nvae;
                    }
                }
                else {
                    if (state.backtracking>0) {state.failed=true; return statement;}
                    NoViableAltException nvae =
                        new NoViableAltException("", 43, 1, input);

                    throw nvae;
                }
            }
            else {
                if (state.backtracking>0) {state.failed=true; return statement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 43, 0, input);

                throw nvae;
            }
            switch (alt43) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:902:13: commonUnionViewStatement
                    {
                    pushFollow(FOLLOW_commonUnionViewStatement_in_createUnionViewStatement4741);
                    commonUnionViewStatement95=commonUnionViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                      statement = commonUnionViewStatement95;
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:907:13: baselineUnionViewStatement
                    {
                    pushFollow(FOLLOW_baselineUnionViewStatement_in_createUnionViewStatement4783);
                    baselineUnionViewStatement96=baselineUnionViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                      statement = baselineUnionViewStatement96;
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:912:13: generalUnionViewStatement
                    {
                    pushFollow(FOLLOW_generalUnionViewStatement_in_createUnionViewStatement4825);
                    generalUnionViewStatement97=generalUnionViewStatement();

                    state._fsp--;
                    if (state.failed) return statement;
                    if ( state.backtracking==0 ) {

                                      statement = generalUnionViewStatement97;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 54, createUnionViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "createUnionViewStatement"


    // $ANTLR start "commonUnionViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:917:1: commonUnionViewStatement returns [ConstantUnionViewStatement statement] : CREATE VIEW schema AS UNION tableNameList ;
    public final ConstantUnionViewStatement commonUnionViewStatement() throws RecognitionException {
        ConstantUnionViewStatement statement = null;
        int commonUnionViewStatement_StartIndex = input.index();
        DefinedTableSchema schema98 = null;

        ArrayList<SQLExpression> tableNameList99 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 55) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:917:71: ( CREATE VIEW schema AS UNION tableNameList )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:918:14: CREATE VIEW schema AS UNION tableNameList
            {
            match(input,CREATE,FOLLOW_CREATE_in_commonUnionViewStatement4874); if (state.failed) return statement;
            match(input,VIEW,FOLLOW_VIEW_in_commonUnionViewStatement4876); if (state.failed) return statement;
            pushFollow(FOLLOW_schema_in_commonUnionViewStatement4878);
            schema98=schema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_commonUnionViewStatement4880); if (state.failed) return statement;
            match(input,UNION,FOLLOW_UNION_in_commonUnionViewStatement4882); if (state.failed) return statement;
            pushFollow(FOLLOW_tableNameList_in_commonUnionViewStatement4897);
            tableNameList99=tableNameList();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                               statement = new ConstantUnionViewStatement(schema98, 
                                                                         tableNameList99);
                           
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 55, commonUnionViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "commonUnionViewStatement"


    // $ANTLR start "baselineUnionViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:925:1: baselineUnionViewStatement returns [BaselineUnionViewStatement statement] : CREATE VIEW baselineSchema AS UNION tableNameList ;
    public final BaselineUnionViewStatement baselineUnionViewStatement() throws RecognitionException {
        BaselineUnionViewStatement statement = null;
        int baselineUnionViewStatement_StartIndex = input.index();
        QueryParser.baselineSchema_return baselineSchema100 = null;

        ArrayList<SQLExpression> tableNameList101 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 56) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:925:73: ( CREATE VIEW baselineSchema AS UNION tableNameList )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:926:14: CREATE VIEW baselineSchema AS UNION tableNameList
            {
            match(input,CREATE,FOLLOW_CREATE_in_baselineUnionViewStatement4948); if (state.failed) return statement;
            match(input,VIEW,FOLLOW_VIEW_in_baselineUnionViewStatement4950); if (state.failed) return statement;
            pushFollow(FOLLOW_baselineSchema_in_baselineUnionViewStatement4952);
            baselineSchema100=baselineSchema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_baselineUnionViewStatement4954); if (state.failed) return statement;
            match(input,UNION,FOLLOW_UNION_in_baselineUnionViewStatement4956); if (state.failed) return statement;
            pushFollow(FOLLOW_tableNameList_in_baselineUnionViewStatement4971);
            tableNameList101=tableNameList();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                               statement = new BaselineUnionViewStatement((baselineSchema100!=null?baselineSchema100.schema:null), 
              										                 tableNameList101, 
              										                 (baselineSchema100!=null?baselineSchema100.index:null));
                           
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 56, baselineUnionViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "baselineUnionViewStatement"


    // $ANTLR start "generalUnionViewStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:934:1: generalUnionViewStatement returns [GeneralUnionViewStatement statement] : CREATE VIEW generalSchema AS UNION tableNameList ;
    public final GeneralUnionViewStatement generalUnionViewStatement() throws RecognitionException {
        GeneralUnionViewStatement statement = null;
        int generalUnionViewStatement_StartIndex = input.index();
        DefinedTableSchema generalSchema102 = null;

        ArrayList<SQLExpression> tableNameList103 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 57) ) { return statement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:934:71: ( CREATE VIEW generalSchema AS UNION tableNameList )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:935:14: CREATE VIEW generalSchema AS UNION tableNameList
            {
            match(input,CREATE,FOLLOW_CREATE_in_generalUnionViewStatement5022); if (state.failed) return statement;
            match(input,VIEW,FOLLOW_VIEW_in_generalUnionViewStatement5024); if (state.failed) return statement;
            pushFollow(FOLLOW_generalSchema_in_generalUnionViewStatement5026);
            generalSchema102=generalSchema();

            state._fsp--;
            if (state.failed) return statement;
            match(input,AS,FOLLOW_AS_in_generalUnionViewStatement5028); if (state.failed) return statement;
            match(input,UNION,FOLLOW_UNION_in_generalUnionViewStatement5030); if (state.failed) return statement;
            pushFollow(FOLLOW_tableNameList_in_generalUnionViewStatement5045);
            tableNameList103=tableNameList();

            state._fsp--;
            if (state.failed) return statement;
            if ( state.backtracking==0 ) {

                               statement = new GeneralUnionViewStatement(generalSchema102, 
              										                 tableNameList103);
                           
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 57, generalUnionViewStatement_StartIndex); }
        }
        return statement;
    }
    // $ANTLR end "generalUnionViewStatement"


    // $ANTLR start "tableNameList"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:942:1: tableNameList returns [ArrayList<SQLExpression> tableNameList] : tr1= uniontableName ( COMMA tr2= uniontableName )* ;
    public final ArrayList<SQLExpression> tableNameList() throws RecognitionException {
        ArrayList<SQLExpression> tableNameList = null;
        int tableNameList_StartIndex = input.index();
        SQLExpression tr1 = null;

        SQLExpression tr2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 58) ) { return tableNameList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:942:62: (tr1= uniontableName ( COMMA tr2= uniontableName )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:943:13: tr1= uniontableName ( COMMA tr2= uniontableName )*
            {
            pushFollow(FOLLOW_uniontableName_in_tableNameList5097);
            tr1=uniontableName();

            state._fsp--;
            if (state.failed) return tableNameList;
            if ( state.backtracking==0 ) {

                               tableNameList = new ArrayList<SQLExpression>();
                               tableNameList.add(tr1);
                          
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:948:13: ( COMMA tr2= uniontableName )*
            loop44:
            do {
                int alt44=2;
                int LA44_0 = input.LA(1);

                if ( (LA44_0==COMMA) ) {
                    alt44=1;
                }


                switch (alt44) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:948:14: COMMA tr2= uniontableName
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_tableNameList5126); if (state.failed) return tableNameList;
            	    pushFollow(FOLLOW_uniontableName_in_tableNameList5130);
            	    tr2=uniontableName();

            	    state._fsp--;
            	    if (state.failed) return tableNameList;
            	    if ( state.backtracking==0 ) {

            	                       tableNameList.add(tr2);
            	                  
            	    }

            	    }
            	    break;

            	default :
            	    break loop44;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 58, tableNameList_StartIndex); }
        }
        return tableNameList;
    }
    // $ANTLR end "tableNameList"


    // $ANTLR start "uniontableName"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:953:1: uniontableName returns [SQLExpression table] : (commonTableName= IDENTIFIER | baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET | generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET | baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | generalTableNameArray= IDENTIFIER LBRACKET generalLowerBound= valueExpression TWODOT generalUpBound= valueExpression RBRACKET );
    public final SQLExpression uniontableName() throws RecognitionException {
        SQLExpression table = null;
        int uniontableName_StartIndex = input.index();
        Token commonTableName=null;
        Token baselineTableName=null;
        Token generalTableName=null;
        Token baselineTableNameArray=null;
        Token lb=null;
        Token ub=null;
        Token generalTableNameArray=null;
        Token NUMERIC104=null;
        QueryParser.valueExpression_return generalLowerBound = null;

        QueryParser.valueExpression_return generalUpBound = null;

        QueryParser.valueExpression_return valueExpression105 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 59) ) { return table; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:953:44: (commonTableName= IDENTIFIER | baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET | generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET | baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | generalTableNameArray= IDENTIFIER LBRACKET generalLowerBound= valueExpression TWODOT generalUpBound= valueExpression RBRACKET )
            int alt45=5;
            alt45 = dfa45.predict(input);
            switch (alt45) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:954:13: commonTableName= IDENTIFIER
                    {
                    commonTableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_uniontableName5182); if (state.failed) return table;
                    if ( state.backtracking==0 ) {

                                      table = new CommonTableName((commonTableName!=null?commonTableName.getText():null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:959:13: baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET
                    {
                    baselineTableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_uniontableName5226); if (state.failed) return table;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_uniontableName5228); if (state.failed) return table;
                    NUMERIC104=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_uniontableName5230); if (state.failed) return table;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_uniontableName5232); if (state.failed) return table;
                    if ( state.backtracking==0 ) {

                                      table = new BaselineTableName((baselineTableName!=null?baselineTableName.getText():null), (NUMERIC104!=null?NUMERIC104.getText():null));
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:964:13: generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET
                    {
                    generalTableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_uniontableName5276); if (state.failed) return table;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_uniontableName5278); if (state.failed) return table;
                    pushFollow(FOLLOW_valueExpression_in_uniontableName5280);
                    valueExpression105=valueExpression();

                    state._fsp--;
                    if (state.failed) return table;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_uniontableName5282); if (state.failed) return table;
                    if ( state.backtracking==0 ) {

                                      table = new GeneralTableName((generalTableName!=null?generalTableName.getText():null), (valueExpression105!=null?valueExpression105.expression:null));
                                  
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:969:13: baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET
                    {
                    baselineTableNameArray=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_uniontableName5326); if (state.failed) return table;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_uniontableName5328); if (state.failed) return table;
                    lb=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_uniontableName5332); if (state.failed) return table;
                    match(input,TWODOT,FOLLOW_TWODOT_in_uniontableName5334); if (state.failed) return table;
                    ub=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_uniontableName5338); if (state.failed) return table;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_uniontableName5340); if (state.failed) return table;
                    if ( state.backtracking==0 ) {

                                      table = new BaselineTableNameArray((baselineTableNameArray!=null?baselineTableNameArray.getText():null), 
                                                                          (lb!=null?lb.getText():null) + "src/main" + (ub!=null?ub.getText():null));
                                  
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:975:13: generalTableNameArray= IDENTIFIER LBRACKET generalLowerBound= valueExpression TWODOT generalUpBound= valueExpression RBRACKET
                    {
                    generalTableNameArray=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_uniontableName5384); if (state.failed) return table;
                    match(input,LBRACKET,FOLLOW_LBRACKET_in_uniontableName5386); if (state.failed) return table;
                    pushFollow(FOLLOW_valueExpression_in_uniontableName5390);
                    generalLowerBound=valueExpression();

                    state._fsp--;
                    if (state.failed) return table;
                    match(input,TWODOT,FOLLOW_TWODOT_in_uniontableName5392); if (state.failed) return table;
                    pushFollow(FOLLOW_valueExpression_in_uniontableName5396);
                    generalUpBound=valueExpression();

                    state._fsp--;
                    if (state.failed) return table;
                    match(input,RBRACKET,FOLLOW_RBRACKET_in_uniontableName5398); if (state.failed) return table;
                    if ( state.backtracking==0 ) {

                                      table = new GeneralTableNameArray((generalTableNameArray!=null?generalTableNameArray.getText():null), 
                                                                          (generalLowerBound!=null?generalLowerBound.expression:null),
                                                                          (generalUpBound!=null?generalUpBound.expression:null));
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 59, uniontableName_StartIndex); }
        }
        return table;
    }
    // $ANTLR end "uniontableName"


    // $ANTLR start "file"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:983:1: file returns [String fName] : ( IDENTIFIER | STRING );
    public final String file() throws RecognitionException {
        String fName = null;
        int file_StartIndex = input.index();
        Token IDENTIFIER106=null;
        Token STRING107=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 60) ) { return fName; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:983:27: ( IDENTIFIER | STRING )
            int alt46=2;
            int LA46_0 = input.LA(1);

            if ( (LA46_0==IDENTIFIER) ) {
                alt46=1;
            }
            else if ( (LA46_0==STRING) ) {
                alt46=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return fName;}
                NoViableAltException nvae =
                    new NoViableAltException("", 46, 0, input);

                throw nvae;
            }
            switch (alt46) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:984:4: IDENTIFIER
                    {
                    IDENTIFIER106=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_file5438); if (state.failed) return fName;
                    if ( state.backtracking==0 ) {

                      				return (IDENTIFIER106!=null?IDENTIFIER106.getText():null);
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:989:4: STRING
                    {
                    STRING107=(Token)match(input,STRING,FOLLOW_STRING_in_file5453); if (state.failed) return fName;
                    if ( state.backtracking==0 ) {

                      				return (STRING107!=null?STRING107.getText():null);
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 60, file_StartIndex); }
        }
        return fName;
    }
    // $ANTLR end "file"


    // $ANTLR start "functionInAtts"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:995:1: functionInAtts returns [ArrayList<SQLExpression> attributeList] : LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN ;
    public final ArrayList<SQLExpression> functionInAtts() throws RecognitionException {
        ArrayList<SQLExpression> attributeList = null;
        int functionInAtts_StartIndex = input.index();
        SQLExpression a1 = null;

        SQLExpression a2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 61) ) { return attributeList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:995:63: ( LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:996:4: LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_functionInAtts5476); if (state.failed) return attributeList;
            pushFollow(FOLLOW_attributeDefineElement_in_functionInAtts5483);
            a1=attributeDefineElement();

            state._fsp--;
            if (state.failed) return attributeList;
            if ( state.backtracking==0 ) {

              				attributeList = new ArrayList<SQLExpression>();
              				((AttributeDefineElement) a1).setInput (true);
              				attributeList.add(a1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1003:25: ( COMMA a2= attributeDefineElement )*
            loop47:
            do {
                int alt47=2;
                int LA47_0 = input.LA(1);

                if ( (LA47_0==COMMA) ) {
                    alt47=1;
                }


                switch (alt47) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1003:26: COMMA a2= attributeDefineElement
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_functionInAtts5515); if (state.failed) return attributeList;
            	    pushFollow(FOLLOW_attributeDefineElement_in_functionInAtts5522);
            	    a2=attributeDefineElement();

            	    state._fsp--;
            	    if (state.failed) return attributeList;
            	    if ( state.backtracking==0 ) {

            	      				((AttributeDefineElement) a2).setInput (true);
            	      				attributeList.add(a2);
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    break loop47;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_functionInAtts5534); if (state.failed) return attributeList;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 61, functionInAtts_StartIndex); }
        }
        return attributeList;
    }
    // $ANTLR end "functionInAtts"


    // $ANTLR start "vgFunctionOutAtts"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1011:1: vgFunctionOutAtts returns [ArrayList<SQLExpression> attributeList] : LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN ;
    public final ArrayList<SQLExpression> vgFunctionOutAtts() throws RecognitionException {
        ArrayList<SQLExpression> attributeList = null;
        int vgFunctionOutAtts_StartIndex = input.index();
        SQLExpression a1 = null;

        SQLExpression a2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 62) ) { return attributeList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1011:66: ( LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1012:4: LPAREN a1= attributeDefineElement ( COMMA a2= attributeDefineElement )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_vgFunctionOutAtts5551); if (state.failed) return attributeList;
            pushFollow(FOLLOW_attributeDefineElement_in_vgFunctionOutAtts5558);
            a1=attributeDefineElement();

            state._fsp--;
            if (state.failed) return attributeList;
            if ( state.backtracking==0 ) {

              				attributeList = new ArrayList<SQLExpression>();
              				((AttributeDefineElement) a1).setInput (false);
              				attributeList.add(a1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1019:25: ( COMMA a2= attributeDefineElement )*
            loop48:
            do {
                int alt48=2;
                int LA48_0 = input.LA(1);

                if ( (LA48_0==COMMA) ) {
                    alt48=1;
                }


                switch (alt48) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1019:26: COMMA a2= attributeDefineElement
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_vgFunctionOutAtts5590); if (state.failed) return attributeList;
            	    pushFollow(FOLLOW_attributeDefineElement_in_vgFunctionOutAtts5597);
            	    a2=attributeDefineElement();

            	    state._fsp--;
            	    if (state.failed) return attributeList;
            	    if ( state.backtracking==0 ) {

            	      				((AttributeDefineElement) a2).setInput (false);
            	      				attributeList.add(a2);
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    break loop48;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_vgFunctionOutAtts5613); if (state.failed) return attributeList;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 62, vgFunctionOutAtts_StartIndex); }
        }
        return attributeList;
    }
    // $ANTLR end "vgFunctionOutAtts"


    // $ANTLR start "returnType"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1029:1: returnType returns [TypeInfo typeInfo] : type ;
    public final TypeInfo returnType() throws RecognitionException {
        TypeInfo typeInfo = null;
        int returnType_StartIndex = input.index();
        QueryParser.type_return type108 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 63) ) { return typeInfo; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1029:39: ( type )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1030:4: type
            {
            pushFollow(FOLLOW_type_in_returnType5631);
            type108=type();

            state._fsp--;
            if (state.failed) return typeInfo;
            if ( state.backtracking==0 ) {

              				typeInfo = new TypeInfo ((type108!=null?type108.dataType:null));
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 63, returnType_StartIndex); }
        }
        return typeInfo;
    }
    // $ANTLR end "returnType"


    // $ANTLR start "definedTableName"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1036:1: definedTableName returns [String name] : IDENTIFIER ;
    public final String definedTableName() throws RecognitionException {
        String name = null;
        int definedTableName_StartIndex = input.index();
        Token IDENTIFIER109=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 64) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1036:38: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1037:4: IDENTIFIER
            {
            IDENTIFIER109=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_definedTableName5655); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              				return (IDENTIFIER109!=null?IDENTIFIER109.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 64, definedTableName_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "definedTableName"


    // $ANTLR start "table_contents_source"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1042:1: table_contents_source returns [ArrayList<SQLExpression> attributeList] : LPAREN a1= attributeElement ( COMMA a2= attributeElement )* RPAREN ;
    public final ArrayList<SQLExpression> table_contents_source() throws RecognitionException {
        ArrayList<SQLExpression> attributeList = null;
        int table_contents_source_StartIndex = input.index();
        SQLExpression a1 = null;

        SQLExpression a2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 65) ) { return attributeList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1042:70: ( LPAREN a1= attributeElement ( COMMA a2= attributeElement )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1043:4: LPAREN a1= attributeElement ( COMMA a2= attributeElement )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_table_contents_source5676); if (state.failed) return attributeList;
            pushFollow(FOLLOW_attributeElement_in_table_contents_source5683);
            a1=attributeElement();

            state._fsp--;
            if (state.failed) return attributeList;
            if ( state.backtracking==0 ) {

              				attributeList = new ArrayList<SQLExpression>();
              	      	    attributeList.add(a1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1049:4: ( COMMA a2= attributeElement )*
            loop49:
            do {
                int alt49=2;
                int LA49_0 = input.LA(1);

                if ( (LA49_0==COMMA) ) {
                    alt49=1;
                }


                switch (alt49) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1049:5: COMMA a2= attributeElement
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_table_contents_source5694); if (state.failed) return attributeList;
            	    pushFollow(FOLLOW_attributeElement_in_table_contents_source5701);
            	    a2=attributeElement();

            	    state._fsp--;
            	    if (state.failed) return attributeList;
            	    if ( state.backtracking==0 ) {

            	      				attributeList.add(a2);
            	      			
            	    }

            	    }
            	    break;

            	default :
            	    break loop49;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_table_contents_source5713); if (state.failed) return attributeList;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 65, table_contents_source_StartIndex); }
        }
        return attributeList;
    }
    // $ANTLR end "table_contents_source"


    // $ANTLR start "attributeElement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1056:1: attributeElement returns [SQLExpression attributeElement] : ( attributeDefineElement | attributeConstraintElement );
    public final SQLExpression attributeElement() throws RecognitionException {
        SQLExpression attributeElement = null;
        int attributeElement_StartIndex = input.index();
        SQLExpression attributeDefineElement110 = null;

        SQLExpression attributeConstraintElement111 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 66) ) { return attributeElement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1056:57: ( attributeDefineElement | attributeConstraintElement )
            int alt50=2;
            int LA50_0 = input.LA(1);

            if ( (LA50_0==IDENTIFIER) ) {
                alt50=1;
            }
            else if ( (LA50_0==PRIMARY||LA50_0==FOREIGN) ) {
                alt50=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return attributeElement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 50, 0, input);

                throw nvae;
            }
            switch (alt50) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1057:4: attributeDefineElement
                    {
                    pushFollow(FOLLOW_attributeDefineElement_in_attributeElement5730);
                    attributeDefineElement110=attributeDefineElement();

                    state._fsp--;
                    if (state.failed) return attributeElement;
                    if ( state.backtracking==0 ) {

                      				attributeElement = attributeDefineElement110;
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1062:4: attributeConstraintElement
                    {
                    pushFollow(FOLLOW_attributeConstraintElement_in_attributeElement5745);
                    attributeConstraintElement111=attributeConstraintElement();

                    state._fsp--;
                    if (state.failed) return attributeElement;
                    if ( state.backtracking==0 ) {

                      				attributeElement = attributeConstraintElement111;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 66, attributeElement_StartIndex); }
        }
        return attributeElement;
    }
    // $ANTLR end "attributeElement"


    // $ANTLR start "attributeDefineElement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1068:1: attributeDefineElement returns [SQLExpression attributeElement] : name= IDENTIFIER dataType= type ;
    public final SQLExpression attributeDefineElement() throws RecognitionException {
        SQLExpression attributeElement = null;
        int attributeDefineElement_StartIndex = input.index();
        Token name=null;
        QueryParser.type_return dataType = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 67) ) { return attributeElement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1068:63: (name= IDENTIFIER dataType= type )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1069:4: name= IDENTIFIER dataType= type
            {
            name=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_attributeDefineElement5774); if (state.failed) return attributeElement;
            pushFollow(FOLLOW_type_in_attributeDefineElement5783);
            dataType=type();

            state._fsp--;
            if (state.failed) return attributeElement;
            if ( state.backtracking==0 ) {

              				attributeElement = new AttributeDefineElement((name!=null?name.getText():null), (dataType!=null?input.toString(dataType.start,dataType.stop):null));
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 67, attributeDefineElement_StartIndex); }
        }
        return attributeElement;
    }
    // $ANTLR end "attributeDefineElement"


    // $ANTLR start "attributeConstraintElement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1075:1: attributeConstraintElement returns [SQLExpression attributeElement] : ( primaryKeyStatement | foreignKeyStatement );
    public final SQLExpression attributeConstraintElement() throws RecognitionException {
        SQLExpression attributeElement = null;
        int attributeConstraintElement_StartIndex = input.index();
        PrimaryKeyElement primaryKeyStatement112 = null;

        ForeignKeyElement foreignKeyStatement113 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 68) ) { return attributeElement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1075:67: ( primaryKeyStatement | foreignKeyStatement )
            int alt51=2;
            int LA51_0 = input.LA(1);

            if ( (LA51_0==PRIMARY) ) {
                alt51=1;
            }
            else if ( (LA51_0==FOREIGN) ) {
                alt51=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return attributeElement;}
                NoViableAltException nvae =
                    new NoViableAltException("", 51, 0, input);

                throw nvae;
            }
            switch (alt51) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1076:4: primaryKeyStatement
                    {
                    pushFollow(FOLLOW_primaryKeyStatement_in_attributeConstraintElement5804);
                    primaryKeyStatement112=primaryKeyStatement();

                    state._fsp--;
                    if (state.failed) return attributeElement;
                    if ( state.backtracking==0 ) {

                      				attributeElement = primaryKeyStatement112;
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1081:4: foreignKeyStatement
                    {
                    pushFollow(FOLLOW_foreignKeyStatement_in_attributeConstraintElement5819);
                    foreignKeyStatement113=foreignKeyStatement();

                    state._fsp--;
                    if (state.failed) return attributeElement;
                    if ( state.backtracking==0 ) {

                      				attributeElement = foreignKeyStatement113;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 68, attributeConstraintElement_StartIndex); }
        }
        return attributeElement;
    }
    // $ANTLR end "attributeConstraintElement"


    // $ANTLR start "primaryKeyStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1086:1: primaryKeyStatement returns [PrimaryKeyElement primaryKeyElement] : PRIMARY KEY LPAREN i1= IDENTIFIER ( COMMA i2= IDENTIFIER )* RPAREN ;
    public final PrimaryKeyElement primaryKeyStatement() throws RecognitionException {
        PrimaryKeyElement primaryKeyElement = null;
        int primaryKeyStatement_StartIndex = input.index();
        Token i1=null;
        Token i2=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 69) ) { return primaryKeyElement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1086:65: ( PRIMARY KEY LPAREN i1= IDENTIFIER ( COMMA i2= IDENTIFIER )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1087:4: PRIMARY KEY LPAREN i1= IDENTIFIER ( COMMA i2= IDENTIFIER )* RPAREN
            {
            match(input,PRIMARY,FOLLOW_PRIMARY_in_primaryKeyStatement5840); if (state.failed) return primaryKeyElement;
            match(input,KEY,FOLLOW_KEY_in_primaryKeyStatement5842); if (state.failed) return primaryKeyElement;
            match(input,LPAREN,FOLLOW_LPAREN_in_primaryKeyStatement5848); if (state.failed) return primaryKeyElement;
            i1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primaryKeyStatement5855); if (state.failed) return primaryKeyElement;
            if ( state.backtracking==0 ) {

              				primaryKeyElement = new PrimaryKeyElement();
              				primaryKeyElement.addKey((i1!=null?i1.getText():null));
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1094:4: ( COMMA i2= IDENTIFIER )*
            loop52:
            do {
                int alt52=2;
                int LA52_0 = input.LA(1);

                if ( (LA52_0==COMMA) ) {
                    alt52=1;
                }


                switch (alt52) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1095:5: COMMA i2= IDENTIFIER
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_primaryKeyStatement5871); if (state.failed) return primaryKeyElement;
            	    i2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_primaryKeyStatement5880); if (state.failed) return primaryKeyElement;
            	    if ( state.backtracking==0 ) {

            	      			 		primaryKeyElement.addKey((i2!=null?i2.getText():null));
            	      			 	
            	    }

            	    }
            	    break;

            	default :
            	    break loop52;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_primaryKeyStatement5898); if (state.failed) return primaryKeyElement;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 69, primaryKeyStatement_StartIndex); }
        }
        return primaryKeyElement;
    }
    // $ANTLR end "primaryKeyStatement"


    // $ANTLR start "foreignKeyStatement"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1103:1: foreignKeyStatement returns [ForeignKeyElement foreignKeyElement] : FOREIGN KEY LPAREN i1= IDENTIFIER RPAREN REFERENCES i2= IDENTIFIER LPAREN i3= IDENTIFIER RPAREN ;
    public final ForeignKeyElement foreignKeyStatement() throws RecognitionException {
        ForeignKeyElement foreignKeyElement = null;
        int foreignKeyStatement_StartIndex = input.index();
        Token i1=null;
        Token i2=null;
        Token i3=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 70) ) { return foreignKeyElement; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1103:65: ( FOREIGN KEY LPAREN i1= IDENTIFIER RPAREN REFERENCES i2= IDENTIFIER LPAREN i3= IDENTIFIER RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1104:4: FOREIGN KEY LPAREN i1= IDENTIFIER RPAREN REFERENCES i2= IDENTIFIER LPAREN i3= IDENTIFIER RPAREN
            {
            match(input,FOREIGN,FOLLOW_FOREIGN_in_foreignKeyStatement5914); if (state.failed) return foreignKeyElement;
            match(input,KEY,FOLLOW_KEY_in_foreignKeyStatement5916); if (state.failed) return foreignKeyElement;
            match(input,LPAREN,FOLLOW_LPAREN_in_foreignKeyStatement5921); if (state.failed) return foreignKeyElement;
            i1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_foreignKeyStatement5928); if (state.failed) return foreignKeyElement;
            match(input,RPAREN,FOLLOW_RPAREN_in_foreignKeyStatement5933); if (state.failed) return foreignKeyElement;
            match(input,REFERENCES,FOLLOW_REFERENCES_in_foreignKeyStatement5938); if (state.failed) return foreignKeyElement;
            i2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_foreignKeyStatement5945); if (state.failed) return foreignKeyElement;
            match(input,LPAREN,FOLLOW_LPAREN_in_foreignKeyStatement5950); if (state.failed) return foreignKeyElement;
            i3=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_foreignKeyStatement5957); if (state.failed) return foreignKeyElement;
            match(input,RPAREN,FOLLOW_RPAREN_in_foreignKeyStatement5962); if (state.failed) return foreignKeyElement;
            if ( state.backtracking==0 ) {

              				foreignKeyElement = new ForeignKeyElement((i1!=null?i1.getText():null),
              												   (i2!=null?i2.getText():null),
              												   (i3!=null?i3.getText():null));
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 70, foreignKeyStatement_StartIndex); }
        }
        return foreignKeyElement;
    }
    // $ANTLR end "foreignKeyStatement"

    public static class type_return extends ParserRuleReturnScope {
        public String dataType;
    };

    // $ANTLR start "type"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1119:1: type returns [String dataType] : IDENTIFIER ( LBRACKET ( length )? RBRACKET )* ( RANDOM )? ;
    public final QueryParser.type_return type() throws RecognitionException {
        QueryParser.type_return retval = new QueryParser.type_return();
        retval.start = input.LT(1);
        int type_StartIndex = input.index();
        Token IDENTIFIER114=null;
        Token LBRACKET115=null;
        Token RBRACKET117=null;
        Token RANDOM118=null;
        String length116 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 71) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1119:31: ( IDENTIFIER ( LBRACKET ( length )? RBRACKET )* ( RANDOM )? )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1120:7: IDENTIFIER ( LBRACKET ( length )? RBRACKET )* ( RANDOM )?
            {
            IDENTIFIER114=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_type5987); if (state.failed) return retval;
            if ( state.backtracking==0 ) {

                      retval.dataType = (IDENTIFIER114!=null?IDENTIFIER114.getText():null);
                    
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1124:7: ( LBRACKET ( length )? RBRACKET )*
            loop54:
            do {
                int alt54=2;
                int LA54_0 = input.LA(1);

                if ( (LA54_0==LBRACKET) ) {
                    alt54=1;
                }


                switch (alt54) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1125:9: LBRACKET ( length )? RBRACKET
            	    {
            	    LBRACKET115=(Token)match(input,LBRACKET,FOLLOW_LBRACKET_in_type6015); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {

            	                retval.dataType = retval.dataType + (LBRACKET115!=null?LBRACKET115.getText():null);
            	              
            	    }
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1129:9: ( length )?
            	    int alt53=2;
            	    int LA53_0 = input.LA(1);

            	    if ( (LA53_0==IDENTIFIER||LA53_0==NUMERIC) ) {
            	        alt53=1;
            	    }
            	    switch (alt53) {
            	        case 1 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1130:9: length
            	            {
            	            pushFollow(FOLLOW_length_in_type6045);
            	            length116=length();

            	            state._fsp--;
            	            if (state.failed) return retval;
            	            if ( state.backtracking==0 ) {

            	                        retval.dataType = retval.dataType + length116;
            	                      
            	            }

            	            }
            	            break;

            	    }

            	    RBRACKET117=(Token)match(input,RBRACKET,FOLLOW_RBRACKET_in_type6076); if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {

            	                retval.dataType = retval.dataType + (RBRACKET117!=null?RBRACKET117.getText():null);
            	              
            	    }

            	    }
            	    break;

            	default :
            	    break loop54;
                }
            } while (true);

            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1140:7: ( RANDOM )?
            int alt55=2;
            int LA55_0 = input.LA(1);

            if ( (LA55_0==RANDOM) ) {
                alt55=1;
            }
            switch (alt55) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1141:9: RANDOM
                    {
                    RANDOM118=(Token)match(input,RANDOM,FOLLOW_RANDOM_in_type6113); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                                retval.dataType = retval.dataType + " " + (RANDOM118!=null?RANDOM118.getText():null);
                              
                    }

                    }
                    break;

            }


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 71, type_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "type"


    // $ANTLR start "length"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1148:1: length returns [String length] : ( IDENTIFIER | NUMERIC );
    public final String length() throws RecognitionException {
        String length = null;
        int length_StartIndex = input.index();
        Token IDENTIFIER119=null;
        Token NUMERIC120=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 72) ) { return length; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1148:31: ( IDENTIFIER | NUMERIC )
            int alt56=2;
            int LA56_0 = input.LA(1);

            if ( (LA56_0==IDENTIFIER) ) {
                alt56=1;
            }
            else if ( (LA56_0==NUMERIC) ) {
                alt56=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return length;}
                NoViableAltException nvae =
                    new NoViableAltException("", 56, 0, input);

                throw nvae;
            }
            switch (alt56) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1149:4: IDENTIFIER
                    {
                    IDENTIFIER119=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_length6158); if (state.failed) return length;
                    if ( state.backtracking==0 ) {

                      			  length = (IDENTIFIER119!=null?IDENTIFIER119.getText():null);
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1154:4: NUMERIC
                    {
                    NUMERIC120=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_length6174); if (state.failed) return length;
                    if ( state.backtracking==0 ) {

                      			  if((NUMERIC120!=null?NUMERIC120.getText():null).indexOf(".") >= 0)
                      			  {
                                  		    System.err.println("The length of a string, vector or matrix is a double!");
                                  		    reportError(new RecognitionException());
                                  		  }
                      			  length = (NUMERIC120!=null?NUMERIC120.getText():null);
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 72, length_StartIndex); }
        }
        return length;
    }
    // $ANTLR end "length"

    public static class valueExpression_return extends ParserRuleReturnScope {
        public MathExpression expression;
    };

    // $ANTLR start "valueExpression"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1171:1: valueExpression returns [MathExpression expression] : me1= mul_expr ( ( PLUS | MINUS ) me2= mul_expr )* ;
    public final QueryParser.valueExpression_return valueExpression() throws RecognitionException {
        QueryParser.valueExpression_return retval = new QueryParser.valueExpression_return();
        retval.start = input.LT(1);
        int valueExpression_StartIndex = input.index();
        MathExpression me1 = null;

        MathExpression me2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 73) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1171:52: (me1= mul_expr ( ( PLUS | MINUS ) me2= mul_expr )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1172:4: me1= mul_expr ( ( PLUS | MINUS ) me2= mul_expr )*
            {
            pushFollow(FOLLOW_mul_expr_in_valueExpression6228);
            me1=mul_expr();

            state._fsp--;
            if (state.failed) return retval;
            if ( state.backtracking==0 ) {

              				ArrayList<MathExpression> operandList = new ArrayList<MathExpression>();
              				ArrayList<Integer> operatorList = new ArrayList<Integer>();
              				retval.expression = new ArithmeticExpression(operandList, operatorList);
              				operandList.add(me1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1179:4: ( ( PLUS | MINUS ) me2= mul_expr )*
            loop58:
            do {
                int alt58=2;
                alt58 = dfa58.predict(input);
                switch (alt58) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1180:5: ( PLUS | MINUS ) me2= mul_expr
            	    {
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1180:5: ( PLUS | MINUS )
            	    int alt57=2;
            	    int LA57_0 = input.LA(1);

            	    if ( (LA57_0==PLUS) ) {
            	        alt57=1;
            	    }
            	    else if ( (LA57_0==MINUS) ) {
            	        alt57=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return retval;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 57, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt57) {
            	        case 1 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1180:6: PLUS
            	            {
            	            match(input,PLUS,FOLLOW_PLUS_in_valueExpression6245); if (state.failed) return retval;
            	            if ( state.backtracking==0 ) {

            	              					((ArithmeticExpression)retval.expression).addOperator(FinalVariable.PLUS);
            	              				
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1185:5: MINUS
            	            {
            	            match(input,MINUS,FOLLOW_MINUS_in_valueExpression6264); if (state.failed) return retval;
            	            if ( state.backtracking==0 ) {

            	              					((ArithmeticExpression)retval.expression).addOperator(FinalVariable.MINUS);
            	              				
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_mul_expr_in_valueExpression6281);
            	    me2=mul_expr();

            	    state._fsp--;
            	    if (state.failed) return retval;
            	    if ( state.backtracking==0 ) {

            	      					((ArithmeticExpression)retval.expression).addMathExpression(me2);
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop58;
                }
            } while (true);


            }

            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 73, valueExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "valueExpression"

    public static class caseExpression_return extends ParserRuleReturnScope {
        public MathExpression expression;
    };

    // $ANTLR start "caseExpression"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1195:1: caseExpression returns [MathExpression expression] : ( ( CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression ) | ( CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression ) );
    public final QueryParser.caseExpression_return caseExpression() throws RecognitionException {
        QueryParser.caseExpression_return retval = new QueryParser.caseExpression_return();
        retval.start = input.LT(1);
        int caseExpression_StartIndex = input.index();
        QueryParser.valueExpression_return v1 = null;

        QueryParser.valueExpression_return v2 = null;

        QueryParser.valueExpression_return v3 = null;

        QueryParser.valueExpression_return v4 = null;

        BooleanPredicate searchCondition121 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 74) ) { return retval; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1195:51: ( ( CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression ) | ( CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression ) )
            int alt61=2;
            alt61 = dfa61.predict(input);
            switch (alt61) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1196:4: ( CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1196:4: ( CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1197:5: CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression
                    {
                    match(input,CASE,FOLLOW_CASE_in_caseExpression6316); if (state.failed) return retval;
                    pushFollow(FOLLOW_valueExpression_in_caseExpression6322);
                    v1=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      					ArrayList<MathExpression> parasList = new ArrayList<MathExpression>();
                      					parasList.add((v1!=null?v1.expression:null));
                      					retval.expression = new GeneralFunctionExpression("case", parasList);
                      				
                    }
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1203:5: ( WHEN v2= valueExpression THEN v3= valueExpression )+
                    int cnt59=0;
                    loop59:
                    do {
                        int alt59=2;
                        int LA59_0 = input.LA(1);

                        if ( (LA59_0==WHEN) ) {
                            alt59=1;
                        }


                        switch (alt59) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1204:6: WHEN v2= valueExpression THEN v3= valueExpression
                    	    {
                    	    match(input,WHEN,FOLLOW_WHEN_in_caseExpression6342); if (state.failed) return retval;
                    	    pushFollow(FOLLOW_valueExpression_in_caseExpression6348);
                    	    v2=valueExpression();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    match(input,THEN,FOLLOW_THEN_in_caseExpression6350); if (state.failed) return retval;
                    	    pushFollow(FOLLOW_valueExpression_in_caseExpression6356);
                    	    v3=valueExpression();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {

                    	      				 		((GeneralFunctionExpression)retval.expression).addParameter((v2!=null?v2.expression:null));
                    	      				 		((GeneralFunctionExpression)retval.expression).addParameter((v3!=null?v3.expression:null));
                    	      				 	
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt59 >= 1 ) break loop59;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(59, input);
                                throw eee;
                        }
                        cnt59++;
                    } while (true);

                    match(input,ELSE,FOLLOW_ELSE_in_caseExpression6378); if (state.failed) return retval;
                    pushFollow(FOLLOW_valueExpression_in_caseExpression6384);
                    v4=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      					((GeneralFunctionExpression)retval.expression).addParameter((v4!=null?v4.expression:null));
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1216:4: ( CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1216:4: ( CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1217:5: CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression
                    {
                    match(input,CASE,FOLLOW_CASE_in_caseExpression6411); if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      					 ArrayList<MathExpression> parasList = new ArrayList<MathExpression>();
                      					 retval.expression = new GeneralFunctionExpression("case", parasList);
                      				
                    }
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1222:5: ( WHEN searchCondition THEN v3= valueExpression )+
                    int cnt60=0;
                    loop60:
                    do {
                        int alt60=2;
                        int LA60_0 = input.LA(1);

                        if ( (LA60_0==WHEN) ) {
                            alt60=1;
                        }


                        switch (alt60) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1223:7: WHEN searchCondition THEN v3= valueExpression
                    	    {
                    	    match(input,WHEN,FOLLOW_WHEN_in_caseExpression6431); if (state.failed) return retval;
                    	    pushFollow(FOLLOW_searchCondition_in_caseExpression6433);
                    	    searchCondition121=searchCondition();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    match(input,THEN,FOLLOW_THEN_in_caseExpression6435); if (state.failed) return retval;
                    	    pushFollow(FOLLOW_valueExpression_in_caseExpression6441);
                    	    v3=valueExpression();

                    	    state._fsp--;
                    	    if (state.failed) return retval;
                    	    if ( state.backtracking==0 ) {

                    	      				 		 ((GeneralFunctionExpression)retval.expression).addParameter(new PredicateWrapper(searchCondition121));
                    	      				 		 ((GeneralFunctionExpression)retval.expression).addParameter((v3!=null?v3.expression:null));
                    	      				 	 
                    	    }

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt60 >= 1 ) break loop60;
                    	    if (state.backtracking>0) {state.failed=true; return retval;}
                                EarlyExitException eee =
                                    new EarlyExitException(60, input);
                                throw eee;
                        }
                        cnt60++;
                    } while (true);

                    match(input,ELSE,FOLLOW_ELSE_in_caseExpression6465); if (state.failed) return retval;
                    pushFollow(FOLLOW_valueExpression_in_caseExpression6471);
                    v4=valueExpression();

                    state._fsp--;
                    if (state.failed) return retval;
                    if ( state.backtracking==0 ) {

                      					 ((GeneralFunctionExpression)retval.expression).addParameter((v4!=null?v4.expression:null));
                      				 
                    }

                    }


                    }
                    break;

            }
            retval.stop = input.LT(-1);

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 74, caseExpression_StartIndex); }
        }
        return retval;
    }
    // $ANTLR end "caseExpression"


    // $ANTLR start "mul_expr"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1238:1: mul_expr returns [MathExpression expression] : ve1= valueExpressionPrimary ( ( ASTERISK | SLASH ) ve2= valueExpressionPrimary )* ;
    public final MathExpression mul_expr() throws RecognitionException {
        MathExpression expression = null;
        int mul_expr_StartIndex = input.index();
        MathExpression ve1 = null;

        MathExpression ve2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 75) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1238:45: (ve1= valueExpressionPrimary ( ( ASTERISK | SLASH ) ve2= valueExpressionPrimary )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1239:13: ve1= valueExpressionPrimary ( ( ASTERISK | SLASH ) ve2= valueExpressionPrimary )*
            {
            pushFollow(FOLLOW_valueExpressionPrimary_in_mul_expr6534);
            ve1=valueExpressionPrimary();

            state._fsp--;
            if (state.failed) return expression;
            if ( state.backtracking==0 ) {

              				ArrayList<MathExpression> operandList = new ArrayList<MathExpression>();
              				ArrayList<Integer> operatorList = new ArrayList<Integer>();
              				expression = new ArithmeticExpression(operandList, operatorList);
              				operandList.add(ve1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1246:4: ( ( ASTERISK | SLASH ) ve2= valueExpressionPrimary )*
            loop63:
            do {
                int alt63=2;
                alt63 = dfa63.predict(input);
                switch (alt63) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1247:5: ( ASTERISK | SLASH ) ve2= valueExpressionPrimary
            	    {
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1247:5: ( ASTERISK | SLASH )
            	    int alt62=2;
            	    int LA62_0 = input.LA(1);

            	    if ( (LA62_0==ASTERISK) ) {
            	        alt62=1;
            	    }
            	    else if ( (LA62_0==SLASH) ) {
            	        alt62=2;
            	    }
            	    else {
            	        if (state.backtracking>0) {state.failed=true; return expression;}
            	        NoViableAltException nvae =
            	            new NoViableAltException("", 62, 0, input);

            	        throw nvae;
            	    }
            	    switch (alt62) {
            	        case 1 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1247:6: ASTERISK
            	            {
            	            match(input,ASTERISK,FOLLOW_ASTERISK_in_mul_expr6551); if (state.failed) return expression;
            	            if ( state.backtracking==0 ) {

            	              					((ArithmeticExpression)expression).addOperator(FinalVariable.TIMES);
            	              				
            	            }

            	            }
            	            break;
            	        case 2 :
            	            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1252:5: SLASH
            	            {
            	            match(input,SLASH,FOLLOW_SLASH_in_mul_expr6570); if (state.failed) return expression;
            	            if ( state.backtracking==0 ) {

            	              					((ArithmeticExpression)expression).addOperator(FinalVariable.DIVIDE);
            	              				
            	            }

            	            }
            	            break;

            	    }

            	    pushFollow(FOLLOW_valueExpressionPrimary_in_mul_expr6587);
            	    ve2=valueExpressionPrimary();

            	    state._fsp--;
            	    if (state.failed) return expression;
            	    if ( state.backtracking==0 ) {

            	      					((ArithmeticExpression)expression).addMathExpression(ve2);
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop63;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 75, mul_expr_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "mul_expr"


    // $ANTLR start "valueExpressionPrimary"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1262:1: valueExpressionPrimary returns [MathExpression expression] : ( GENERALTABLEINDEX | signedNumeric | STRING | setFunction | setExpression | generalFunction | LPAREN valueExpression RPAREN | columnExpression );
    public final MathExpression valueExpressionPrimary() throws RecognitionException {
        MathExpression expression = null;
        int valueExpressionPrimary_StartIndex = input.index();
        Token STRING123=null;
        MathExpression signedNumeric122 = null;

        MathExpression setFunction124 = null;

        MathExpression setExpression125 = null;

        GeneralFunctionExpression generalFunction126 = null;

        QueryParser.valueExpression_return valueExpression127 = null;

        MathExpression columnExpression128 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 76) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1262:59: ( GENERALTABLEINDEX | signedNumeric | STRING | setFunction | setExpression | generalFunction | LPAREN valueExpression RPAREN | columnExpression )
            int alt64=8;
            alt64 = dfa64.predict(input);
            switch (alt64) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1263:13: GENERALTABLEINDEX
                    {
                    match(input,GENERALTABLEINDEX,FOLLOW_GENERALTABLEINDEX_in_valueExpressionPrimary6623); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new GeneralTableIndex();
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1268:13: signedNumeric
                    {
                    pushFollow(FOLLOW_signedNumeric_in_valueExpressionPrimary6665);
                    signedNumeric122=signedNumeric();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {
                       
                                      expression = signedNumeric122;
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1273:13: STRING
                    {
                    STRING123=(Token)match(input,STRING,FOLLOW_STRING_in_valueExpressionPrimary6708); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new StringExpression((STRING123!=null?STRING123.getText():null));
                                  
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1278:13: setFunction
                    {
                    pushFollow(FOLLOW_setFunction_in_valueExpressionPrimary6750);
                    setFunction124=setFunction();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = setFunction124;
                                  
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1283:13: setExpression
                    {
                    pushFollow(FOLLOW_setExpression_in_valueExpressionPrimary6792);
                    setExpression125=setExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	expression = setExpression125;
                                 	
                    }

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1288:13: generalFunction
                    {
                    pushFollow(FOLLOW_generalFunction_in_valueExpressionPrimary6834);
                    generalFunction126=generalFunction();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	expression = generalFunction126;
                                  
                    }

                    }
                    break;
                case 7 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1293:13: LPAREN valueExpression RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_valueExpressionPrimary6877); if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_valueExpressionPrimary6879);
                    valueExpression127=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_valueExpressionPrimary6881); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = (valueExpression127!=null?valueExpression127.expression:null);
                                  
                    }

                    }
                    break;
                case 8 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1298:13: columnExpression
                    {
                    pushFollow(FOLLOW_columnExpression_in_valueExpressionPrimary6924);
                    columnExpression128=columnExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = columnExpression128;
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 76, valueExpressionPrimary_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "valueExpressionPrimary"


    // $ANTLR start "signedNumeric"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1303:1: signedNumeric returns [MathExpression expression] : ( NUMERIC | PLUS NUMERIC | MINUS NUMERIC );
    public final MathExpression signedNumeric() throws RecognitionException {
        MathExpression expression = null;
        int signedNumeric_StartIndex = input.index();
        Token NUMERIC129=null;
        Token NUMERIC130=null;
        Token NUMERIC131=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 77) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1303:50: ( NUMERIC | PLUS NUMERIC | MINUS NUMERIC )
            int alt65=3;
            switch ( input.LA(1) ) {
            case NUMERIC:
                {
                alt65=1;
                }
                break;
            case PLUS:
                {
                alt65=2;
                }
                break;
            case MINUS:
                {
                alt65=3;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 65, 0, input);

                throw nvae;
            }

            switch (alt65) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1304:13: NUMERIC
                    {
                    NUMERIC129=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_signedNumeric6961); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	expression = new NumericExpression((NUMERIC129!=null?NUMERIC129.getText():null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1309:13: PLUS NUMERIC
                    {
                    match(input,PLUS,FOLLOW_PLUS_in_signedNumeric7003); if (state.failed) return expression;
                    NUMERIC130=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_signedNumeric7005); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	expression = new NumericExpression((NUMERIC130!=null?NUMERIC130.getText():null));
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1314:13: MINUS NUMERIC
                    {
                    match(input,MINUS,FOLLOW_MINUS_in_signedNumeric7047); if (state.failed) return expression;
                    NUMERIC131=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_signedNumeric7049); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  	expression = new NumericExpression("-"+(NUMERIC131!=null?NUMERIC131.getText():null));
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 77, signedNumeric_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "signedNumeric"


    // $ANTLR start "setFunction"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1319:1: setFunction returns [MathExpression expression] : ( AVG LPAREN setQuantifier valueExpression RPAREN | SUM LPAREN setQuantifier valueExpression RPAREN | COUNT LPAREN ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN ) | MIN LPAREN setQuantifier valueExpression RPAREN | MAX LPAREN setQuantifier valueExpression RPAREN | VAR LPAREN setQuantifier valueExpression RPAREN | STDEV LPAREN setQuantifier valueExpression RPAREN | VECTOR LPAREN setQuantifier valueExpression RPAREN | ROWMATRIX LPAREN setQuantifier valueExpression RPAREN | COLMATRIX LPAREN setQuantifier valueExpression RPAREN );
    public final MathExpression setFunction() throws RecognitionException {
        MathExpression expression = null;
        int setFunction_StartIndex = input.index();
        int setQuantifier132 = 0;

        QueryParser.valueExpression_return valueExpression133 = null;

        int setQuantifier134 = 0;

        QueryParser.valueExpression_return valueExpression135 = null;

        int setQuantifier136 = 0;

        QueryParser.valueExpression_return valueExpression137 = null;

        int setQuantifier138 = 0;

        QueryParser.valueExpression_return valueExpression139 = null;

        int setQuantifier140 = 0;

        QueryParser.valueExpression_return valueExpression141 = null;

        int setQuantifier142 = 0;

        QueryParser.valueExpression_return valueExpression143 = null;

        int setQuantifier144 = 0;

        QueryParser.valueExpression_return valueExpression145 = null;

        int setQuantifier146 = 0;

        QueryParser.valueExpression_return valueExpression147 = null;

        int setQuantifier148 = 0;

        QueryParser.valueExpression_return valueExpression149 = null;

        int setQuantifier150 = 0;

        QueryParser.valueExpression_return valueExpression151 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 78) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1319:48: ( AVG LPAREN setQuantifier valueExpression RPAREN | SUM LPAREN setQuantifier valueExpression RPAREN | COUNT LPAREN ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN ) | MIN LPAREN setQuantifier valueExpression RPAREN | MAX LPAREN setQuantifier valueExpression RPAREN | VAR LPAREN setQuantifier valueExpression RPAREN | STDEV LPAREN setQuantifier valueExpression RPAREN | VECTOR LPAREN setQuantifier valueExpression RPAREN | ROWMATRIX LPAREN setQuantifier valueExpression RPAREN | COLMATRIX LPAREN setQuantifier valueExpression RPAREN )
            int alt68=10;
            alt68 = dfa68.predict(input);
            switch (alt68) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1320:13: AVG LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,AVG,FOLLOW_AVG_in_setFunction7099); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7101); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7103);
                    setQuantifier132=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7105);
                    valueExpression133=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7107); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new AggregateExpression(FinalVariable.AVG,
                                                                            setQuantifier132,
                                                                            (valueExpression133!=null?valueExpression133.expression:null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1327:13: SUM LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,SUM,FOLLOW_SUM_in_setFunction7149); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7151); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7153);
                    setQuantifier134=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7155);
                    valueExpression135=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7157); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.SUM,
                                                                           setQuantifier134,
                                                                           (valueExpression135!=null?valueExpression135.expression:null));  
                                  
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1334:13: COUNT LPAREN ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN )
                    {
                    match(input,COUNT,FOLLOW_COUNT_in_setFunction7199); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7201); if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1335:13: ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN )
                    int alt67=2;
                    alt67 = dfa67.predict(input);
                    switch (alt67) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1336:14: setQuantifier valueExpression RPAREN
                            {
                            pushFollow(FOLLOW_setQuantifier_in_setFunction7231);
                            setQuantifier136=setQuantifier();

                            state._fsp--;
                            if (state.failed) return expression;
                            pushFollow(FOLLOW_valueExpression_in_setFunction7233);
                            valueExpression137=valueExpression();

                            state._fsp--;
                            if (state.failed) return expression;
                            match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7235); if (state.failed) return expression;
                            if ( state.backtracking==0 ) {

                                              	 expression = new AggregateExpression(FinalVariable.COUNT,
                                                                                   setQuantifier136,
                                                                                   (valueExpression137!=null?valueExpression137.expression:null)); 
                                          	
                            }

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1343:14: ( ALL )? ASTERISK RPAREN
                            {
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1343:14: ( ALL )?
                            int alt66=2;
                            int LA66_0 = input.LA(1);

                            if ( (LA66_0==ALL) ) {
                                alt66=1;
                            }
                            switch (alt66) {
                                case 1 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: ALL
                                    {
                                    match(input,ALL,FOLLOW_ALL_in_setFunction7280); if (state.failed) return expression;

                                    }
                                    break;

                            }

                            match(input,ASTERISK,FOLLOW_ASTERISK_in_setFunction7283); if (state.failed) return expression;
                            match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7285); if (state.failed) return expression;
                            if ( state.backtracking==0 ) {

                                          		 AsteriskExpression asteriskExpression = new AsteriskExpression();
                                              	 expression = new AggregateExpression(FinalVariable.COUNT,
                                              	                                      FinalVariable.ALL,
                                              	                                      asteriskExpression); 
                                          	
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1352:13: MIN LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,MIN,FOLLOW_MIN_in_setFunction7342); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7344); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7346);
                    setQuantifier138=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7348);
                    valueExpression139=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7350); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.MIN,
                                                                           setQuantifier138,
                                                                           (valueExpression139!=null?valueExpression139.expression:null));
                                  
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1359:13: MAX LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,MAX,FOLLOW_MAX_in_setFunction7392); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7394); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7396);
                    setQuantifier140=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7398);
                    valueExpression141=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7400); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.MAX,
                                                                           setQuantifier140,
                                                                           (valueExpression141!=null?valueExpression141.expression:null));
                                  
                    }

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1366:13: VAR LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,VAR,FOLLOW_VAR_in_setFunction7442); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7444); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7446);
                    setQuantifier142=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7448);
                    valueExpression143=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7450); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.VARIANCE,
                                                                           setQuantifier142,
                                                                           (valueExpression143!=null?valueExpression143.expression:null));
                                  
                    }

                    }
                    break;
                case 7 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1373:13: STDEV LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,STDEV,FOLLOW_STDEV_in_setFunction7492); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7494); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7496);
                    setQuantifier144=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7498);
                    valueExpression145=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7500); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.STDEV,
                                                                           setQuantifier144,
                                                                           (valueExpression145!=null?valueExpression145.expression:null));
                                  
                    }

                    }
                    break;
                case 8 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1380:13: VECTOR LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,VECTOR,FOLLOW_VECTOR_in_setFunction7542); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7544); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7546);
                    setQuantifier146=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7548);
                    valueExpression147=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7550); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.VECTOR,
                                                                           setQuantifier146,
                                                                           (valueExpression147!=null?valueExpression147.expression:null));
                                  
                    }

                    }
                    break;
                case 9 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1387:13: ROWMATRIX LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,ROWMATRIX,FOLLOW_ROWMATRIX_in_setFunction7592); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7594); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7596);
                    setQuantifier148=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7598);
                    valueExpression149=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7600); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.ROWMATRIX,
                                                                           setQuantifier148,
                                                                           (valueExpression149!=null?valueExpression149.expression:null));
                                  
                    }

                    }
                    break;
                case 10 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1394:13: COLMATRIX LPAREN setQuantifier valueExpression RPAREN
                    {
                    match(input,COLMATRIX,FOLLOW_COLMATRIX_in_setFunction7642); if (state.failed) return expression;
                    match(input,LPAREN,FOLLOW_LPAREN_in_setFunction7644); if (state.failed) return expression;
                    pushFollow(FOLLOW_setQuantifier_in_setFunction7646);
                    setQuantifier150=setQuantifier();

                    state._fsp--;
                    if (state.failed) return expression;
                    pushFollow(FOLLOW_valueExpression_in_setFunction7648);
                    valueExpression151=valueExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    match(input,RPAREN,FOLLOW_RPAREN_in_setFunction7650); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                      expression = new AggregateExpression(FinalVariable.COLMATRIX,
                                                                           setQuantifier150,
                                                                           (valueExpression151!=null?valueExpression151.expression:null));
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 78, setFunction_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "setFunction"


    // $ANTLR start "columnExpression"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1404:1: columnExpression returns [MathExpression expression] : (id2= IDENTIFIER | table= IDENTIFIER DOT id1= IDENTIFIER );
    public final MathExpression columnExpression() throws RecognitionException {
        MathExpression expression = null;
        int columnExpression_StartIndex = input.index();
        Token id2=null;
        Token table=null;
        Token id1=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 79) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1404:53: (id2= IDENTIFIER | table= IDENTIFIER DOT id1= IDENTIFIER )
            int alt69=2;
            alt69 = dfa69.predict(input);
            switch (alt69) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1405:13: id2= IDENTIFIER
                    {
                    id2=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_columnExpression7717); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new ColumnExpression(null, (id2!=null?id2.getText():null));
                                  
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1410:13: table= IDENTIFIER DOT id1= IDENTIFIER
                    {
                    table=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_columnExpression7761); if (state.failed) return expression;
                    match(input,DOT,FOLLOW_DOT_in_columnExpression7763); if (state.failed) return expression;
                    id1=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_columnExpression7767); if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                       expression = new ColumnExpression((table!=null?table.getText():null),
                                                                         (id1!=null?id1.getText():null));
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 79, columnExpression_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "columnExpression"


    // $ANTLR start "setExpression"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1417:1: setExpression returns [MathExpression expression] : ( VALUES ( enumerationExpression ) | subquery );
    public final MathExpression setExpression() throws RecognitionException {
        MathExpression expression = null;
        int setExpression_StartIndex = input.index();
        MathExpression enumerationExpression152 = null;

        MathExpression subquery153 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 80) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1417:50: ( VALUES ( enumerationExpression ) | subquery )
            int alt70=2;
            int LA70_0 = input.LA(1);

            if ( (LA70_0==VALUES) ) {
                alt70=1;
            }
            else if ( (LA70_0==LPAREN) ) {
                alt70=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return expression;}
                NoViableAltException nvae =
                    new NoViableAltException("", 70, 0, input);

                throw nvae;
            }
            switch (alt70) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1418:4: VALUES ( enumerationExpression )
                    {
                    match(input,VALUES,FOLLOW_VALUES_in_setExpression7808); if (state.failed) return expression;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1419:4: ( enumerationExpression )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1420:5: enumerationExpression
                    {
                    pushFollow(FOLLOW_enumerationExpression_in_setExpression7819);
                    enumerationExpression152=enumerationExpression();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                      					expression = enumerationExpression152;
                      				
                    }

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1426:4: subquery
                    {
                    pushFollow(FOLLOW_subquery_in_setExpression7840);
                    subquery153=subquery();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                      				expression = subquery153;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 80, setExpression_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "setExpression"


    // $ANTLR start "enumerationExpression"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1431:1: enumerationExpression returns [MathExpression expression] : LPAREN ve1= valueExpression ( COMMA ve2= valueExpression )* RPAREN ;
    public final MathExpression enumerationExpression() throws RecognitionException {
        MathExpression expression = null;
        int enumerationExpression_StartIndex = input.index();
        QueryParser.valueExpression_return ve1 = null;

        QueryParser.valueExpression_return ve2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 81) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1431:57: ( LPAREN ve1= valueExpression ( COMMA ve2= valueExpression )* RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1432:4: LPAREN ve1= valueExpression ( COMMA ve2= valueExpression )* RPAREN
            {
            match(input,LPAREN,FOLLOW_LPAREN_in_enumerationExpression7864); if (state.failed) return expression;
            pushFollow(FOLLOW_valueExpression_in_enumerationExpression7868);
            ve1=valueExpression();

            state._fsp--;
            if (state.failed) return expression;
            if ( state.backtracking==0 ) {

              				ArrayList<MathExpression> expressionList = new ArrayList<MathExpression>();
              				expression = new SetExpression(expressionList);
              				expressionList.add((ve1!=null?ve1.expression:null));
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1438:4: ( COMMA ve2= valueExpression )*
            loop71:
            do {
                int alt71=2;
                int LA71_0 = input.LA(1);

                if ( (LA71_0==COMMA) ) {
                    alt71=1;
                }


                switch (alt71) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1439:5: COMMA ve2= valueExpression
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_enumerationExpression7884); if (state.failed) return expression;
            	    pushFollow(FOLLOW_valueExpression_in_enumerationExpression7888);
            	    ve2=valueExpression();

            	    state._fsp--;
            	    if (state.failed) return expression;
            	    if ( state.backtracking==0 ) {

            	      					((SetExpression)expression).addMathExpression((ve2!=null?ve2.expression:null));
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop71;
                }
            } while (true);

            match(input,RPAREN,FOLLOW_RPAREN_in_enumerationExpression7902); if (state.failed) return expression;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 81, enumerationExpression_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "enumerationExpression"


    // $ANTLR start "generalFunction"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1446:1: generalFunction returns [GeneralFunctionExpression expression] : functionName LPAREN ( functionParas | selectStatement ) RPAREN ;
    public final GeneralFunctionExpression generalFunction() throws RecognitionException {
        GeneralFunctionExpression expression = null;
        int generalFunction_StartIndex = input.index();
        String functionName154 = null;

        ArrayList<MathExpression> functionParas155 = null;

        SelectStatement selectStatement156 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 82) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1446:62: ( functionName LPAREN ( functionParas | selectStatement ) RPAREN )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1447:13: functionName LPAREN ( functionParas | selectStatement ) RPAREN
            {
            pushFollow(FOLLOW_functionName_in_generalFunction7925);
            functionName154=functionName();

            state._fsp--;
            if (state.failed) return expression;
            match(input,LPAREN,FOLLOW_LPAREN_in_generalFunction7939); if (state.failed) return expression;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1449:13: ( functionParas | selectStatement )
            int alt72=2;
            alt72 = dfa72.predict(input);
            switch (alt72) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1450:14: functionParas
                    {
                    pushFollow(FOLLOW_functionParas_in_generalFunction7968);
                    functionParas155=functionParas();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                                  		expression = new GeneralFunctionExpression(functionName154,
                                  										       functionParas155);
                                  	
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1456:14: selectStatement
                    {
                    pushFollow(FOLLOW_selectStatement_in_generalFunction8013);
                    selectStatement156=selectStatement();

                    state._fsp--;
                    if (state.failed) return expression;
                    if ( state.backtracking==0 ) {

                      					ArrayList<MathExpression> paraList = new ArrayList<MathExpression>();
                      		    		paraList.add(new SubqueryExpression(selectStatement156));
                      		    		expression = new GeneralFunctionExpression(functionName154, paraList);
                      				
                    }

                    }
                    break;

            }

            match(input,RPAREN,FOLLOW_RPAREN_in_generalFunction8047); if (state.failed) return expression;

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 82, generalFunction_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "generalFunction"


    // $ANTLR start "functionName"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1466:1: functionName returns [String name] : IDENTIFIER ;
    public final String functionName() throws RecognitionException {
        String name = null;
        int functionName_StartIndex = input.index();
        Token IDENTIFIER157=null;

        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 83) ) { return name; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1466:34: ( IDENTIFIER )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1467:4: IDENTIFIER
            {
            IDENTIFIER157=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_functionName8085); if (state.failed) return name;
            if ( state.backtracking==0 ) {

              				name = (IDENTIFIER157!=null?IDENTIFIER157.getText():null);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 83, functionName_StartIndex); }
        }
        return name;
    }
    // $ANTLR end "functionName"


    // $ANTLR start "functionParas"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1473:1: functionParas returns [ArrayList<MathExpression> paraList] : fp1= functionPara ( COMMA fp2= functionPara )* ;
    public final ArrayList<MathExpression> functionParas() throws RecognitionException {
        ArrayList<MathExpression> paraList = null;
        int functionParas_StartIndex = input.index();
        MathExpression fp1 = null;

        MathExpression fp2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 84) ) { return paraList; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1473:58: (fp1= functionPara ( COMMA fp2= functionPara )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1474:5: fp1= functionPara ( COMMA fp2= functionPara )*
            {
            pushFollow(FOLLOW_functionPara_in_functionParas8109);
            fp1=functionPara();

            state._fsp--;
            if (state.failed) return paraList;
            if ( state.backtracking==0 ) {

              			    	paraList = new ArrayList<MathExpression>();
              			    	paraList.add(fp1);
              			    
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1479:8: ( COMMA fp2= functionPara )*
            loop73:
            do {
                int alt73=2;
                int LA73_0 = input.LA(1);

                if ( (LA73_0==COMMA) ) {
                    alt73=1;
                }


                switch (alt73) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1480:9: COMMA fp2= functionPara
            	    {
            	    match(input,COMMA,FOLLOW_COMMA_in_functionParas8137); if (state.failed) return paraList;
            	    pushFollow(FOLLOW_functionPara_in_functionParas8143);
            	    fp2=functionPara();

            	    state._fsp--;
            	    if (state.failed) return paraList;
            	    if ( state.backtracking==0 ) {

            	      			    		paraList.add(fp2);
            	      			    	
            	    }

            	    }
            	    break;

            	default :
            	    break loop73;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 84, functionParas_StartIndex); }
        }
        return paraList;
    }
    // $ANTLR end "functionParas"


    // $ANTLR start "functionPara"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1487:1: functionPara returns [MathExpression expression] : valueExpression ;
    public final MathExpression functionPara() throws RecognitionException {
        MathExpression expression = null;
        int functionPara_StartIndex = input.index();
        QueryParser.valueExpression_return valueExpression158 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 85) ) { return expression; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1487:48: ( valueExpression )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1488:5: valueExpression
            {
            pushFollow(FOLLOW_valueExpression_in_functionPara8193);
            valueExpression158=valueExpression();

            state._fsp--;
            if (state.failed) return expression;
            if ( state.backtracking==0 ) {

              					expression = (valueExpression158!=null?valueExpression158.expression:null);
              				
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 85, functionPara_StartIndex); }
        }
        return expression;
    }
    // $ANTLR end "functionPara"


    // $ANTLR start "searchCondition"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1496:1: searchCondition returns [BooleanPredicate predicate] : bt1= booleanTerm ( OR bt2= booleanTerm )* ;
    public final BooleanPredicate searchCondition() throws RecognitionException {
        BooleanPredicate predicate = null;
        int searchCondition_StartIndex = input.index();
        BooleanPredicate bt1 = null;

        BooleanPredicate bt2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 86) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1496:53: (bt1= booleanTerm ( OR bt2= booleanTerm )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1497:4: bt1= booleanTerm ( OR bt2= booleanTerm )*
            {
            pushFollow(FOLLOW_booleanTerm_in_searchCondition8232);
            bt1=booleanTerm();

            state._fsp--;
            if (state.failed) return predicate;
            if ( state.backtracking==0 ) {

              				ArrayList<BooleanPredicate> orList = new ArrayList<BooleanPredicate>();
              				predicate = new OrPredicate(orList); 
              				orList.add(bt1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1503:4: ( OR bt2= booleanTerm )*
            loop74:
            do {
                int alt74=2;
                int LA74_0 = input.LA(1);

                if ( (LA74_0==OR) ) {
                    alt74=1;
                }


                switch (alt74) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1504:5: OR bt2= booleanTerm
            	    {
            	    match(input,OR,FOLLOW_OR_in_searchCondition8248); if (state.failed) return predicate;
            	    pushFollow(FOLLOW_booleanTerm_in_searchCondition8254);
            	    bt2=booleanTerm();

            	    state._fsp--;
            	    if (state.failed) return predicate;
            	    if ( state.backtracking==0 ) {

            	      					((OrPredicate)predicate).addPredicate(bt2);
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop74;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 86, searchCondition_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "searchCondition"


    // $ANTLR start "booleanTerm"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1510:1: booleanTerm returns [BooleanPredicate predicate] : bf1= booleanFactor ( AND bf2= booleanFactor )* ;
    public final BooleanPredicate booleanTerm() throws RecognitionException {
        BooleanPredicate predicate = null;
        int booleanTerm_StartIndex = input.index();
        BooleanPredicate bf1 = null;

        BooleanPredicate bf2 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 87) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1510:49: (bf1= booleanFactor ( AND bf2= booleanFactor )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1511:13: bf1= booleanFactor ( AND bf2= booleanFactor )*
            {
            pushFollow(FOLLOW_booleanFactor_in_booleanTerm8296);
            bf1=booleanFactor();

            state._fsp--;
            if (state.failed) return predicate;
            if ( state.backtracking==0 ) {

              				ArrayList<BooleanPredicate> andList = new ArrayList<BooleanPredicate>();
                          	predicate = new AndPredicate(andList);
                          	andList.add(bf1);
              			
            }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1517:4: ( AND bf2= booleanFactor )*
            loop75:
            do {
                int alt75=2;
                alt75 = dfa75.predict(input);
                switch (alt75) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1518:5: AND bf2= booleanFactor
            	    {
            	    match(input,AND,FOLLOW_AND_in_booleanTerm8312); if (state.failed) return predicate;
            	    pushFollow(FOLLOW_booleanFactor_in_booleanTerm8318);
            	    bf2=booleanFactor();

            	    state._fsp--;
            	    if (state.failed) return predicate;
            	    if ( state.backtracking==0 ) {

            	      					((AndPredicate)predicate).addPredicate(bf2);
            	      				
            	    }

            	    }
            	    break;

            	default :
            	    break loop75;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 87, booleanTerm_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "booleanTerm"


    // $ANTLR start "booleanFactor"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1524:1: booleanFactor returns [BooleanPredicate predicate] : ( booleanPredicate ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | ) | NOT booleanPredicate );
    public final BooleanPredicate booleanFactor() throws RecognitionException {
        BooleanPredicate predicate = null;
        int booleanFactor_StartIndex = input.index();
        BooleanPredicate booleanPredicate159 = null;

        BooleanPredicate booleanPredicate160 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 88) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1524:51: ( booleanPredicate ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | ) | NOT booleanPredicate )
            int alt79=2;
            alt79 = dfa79.predict(input);
            switch (alt79) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1525:13: booleanPredicate ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | )
                    {
                    pushFollow(FOLLOW_booleanPredicate_in_booleanFactor8365);
                    booleanPredicate159=booleanPredicate();

                    state._fsp--;
                    if (state.failed) return predicate;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1526:13: ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | )
                    int alt78=3;
                    alt78 = dfa78.predict(input);
                    switch (alt78) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1527:14: ( EQUALS TRUE | NOTEQUALS FALSE )
                            {
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1527:14: ( EQUALS TRUE | NOTEQUALS FALSE )
                            int alt76=2;
                            int LA76_0 = input.LA(1);

                            if ( (LA76_0==EQUALS) ) {
                                alt76=1;
                            }
                            else if ( (LA76_0==NOTEQUALS) ) {
                                alt76=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return predicate;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 76, 0, input);

                                throw nvae;
                            }
                            switch (alt76) {
                                case 1 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1527:15: EQUALS TRUE
                                    {
                                    match(input,EQUALS,FOLLOW_EQUALS_in_booleanFactor8396); if (state.failed) return predicate;
                                    match(input,TRUE,FOLLOW_TRUE_in_booleanFactor8398); if (state.failed) return predicate;

                                    }
                                    break;
                                case 2 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1527:29: NOTEQUALS FALSE
                                    {
                                    match(input,NOTEQUALS,FOLLOW_NOTEQUALS_in_booleanFactor8402); if (state.failed) return predicate;
                                    match(input,FALSE,FOLLOW_FALSE_in_booleanFactor8404); if (state.failed) return predicate;

                                    }
                                    break;

                            }

                            if ( state.backtracking==0 ) {

                                         	 		predicate = booleanPredicate159;
                                         	 	
                            }

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1532:15: ( EQUALS FALSE | NOTEQUALS TRUE )
                            {
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1532:15: ( EQUALS FALSE | NOTEQUALS TRUE )
                            int alt77=2;
                            int LA77_0 = input.LA(1);

                            if ( (LA77_0==EQUALS) ) {
                                alt77=1;
                            }
                            else if ( (LA77_0==NOTEQUALS) ) {
                                alt77=2;
                            }
                            else {
                                if (state.backtracking>0) {state.failed=true; return predicate;}
                                NoViableAltException nvae =
                                    new NoViableAltException("", 77, 0, input);

                                throw nvae;
                            }
                            switch (alt77) {
                                case 1 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1532:16: EQUALS FALSE
                                    {
                                    match(input,EQUALS,FOLLOW_EQUALS_in_booleanFactor8454); if (state.failed) return predicate;
                                    match(input,FALSE,FOLLOW_FALSE_in_booleanFactor8456); if (state.failed) return predicate;

                                    }
                                    break;
                                case 2 :
                                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1532:31: NOTEQUALS TRUE
                                    {
                                    match(input,NOTEQUALS,FOLLOW_NOTEQUALS_in_booleanFactor8460); if (state.failed) return predicate;
                                    match(input,TRUE,FOLLOW_TRUE_in_booleanFactor8462); if (state.failed) return predicate;

                                    }
                                    break;

                            }

                            if ( state.backtracking==0 ) {

                                         	 		predicate = new NotPredicate(booleanPredicate159);
                                         	 	
                            }

                            }
                            break;
                        case 3 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1537:15: 
                            {
                            if ( state.backtracking==0 ) {

                                               	predicate = booleanPredicate159;
                                         	 	
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1542:13: NOT booleanPredicate
                    {
                    match(input,NOT,FOLLOW_NOT_in_booleanFactor8553); if (state.failed) return predicate;
                    pushFollow(FOLLOW_booleanPredicate_in_booleanFactor8555);
                    booleanPredicate160=booleanPredicate();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                                       predicate = new NotPredicate(booleanPredicate160);
                                  
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 88, booleanFactor_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "booleanFactor"


    // $ANTLR start "booleanPredicate"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1548:1: booleanPredicate returns [BooleanPredicate predicate] : ( booleanAtom | LPAREN searchCondition RPAREN | exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression ) | conditionExists );
    public final BooleanPredicate booleanPredicate() throws RecognitionException {
        BooleanPredicate predicate = null;
        int booleanPredicate_StartIndex = input.index();
        QueryParser.valueExpression_return exp1 = null;

        QueryParser.valueExpression_return right = null;

        QueryParser.valueExpression_return low = null;

        QueryParser.valueExpression_return up = null;

        QueryParser.valueExpression_return pattern = null;

        MathExpression set1Expression = null;

        MathExpression set2Expression = null;

        boolean booleanAtom161 = false;

        BooleanPredicate searchCondition162 = null;

        int comparisonOperator163 = 0;

        BooleanPredicate conditionExists164 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 89) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1548:54: ( booleanAtom | LPAREN searchCondition RPAREN | exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression ) | conditionExists )
            int alt81=4;
            alt81 = dfa81.predict(input);
            switch (alt81) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1549:4: booleanAtom
                    {
                    pushFollow(FOLLOW_booleanAtom_in_booleanPredicate8608);
                    booleanAtom161=booleanAtom();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                      				predicate = new AtomPredicate(booleanAtom161);
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1554:13: LPAREN searchCondition RPAREN
                    {
                    match(input,LPAREN,FOLLOW_LPAREN_in_booleanPredicate8633); if (state.failed) return predicate;
                    pushFollow(FOLLOW_searchCondition_in_booleanPredicate8647);
                    searchCondition162=searchCondition();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                                  	predicate = searchCondition162;
                                  
                    }
                    match(input,RPAREN,FOLLOW_RPAREN_in_booleanPredicate8675); if (state.failed) return predicate;

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1561:4: exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )
                    {
                    pushFollow(FOLLOW_valueExpression_in_booleanPredicate8698);
                    exp1=valueExpression();

                    state._fsp--;
                    if (state.failed) return predicate;
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1562:4: ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )
                    int alt80=7;
                    alt80 = dfa80.predict(input);
                    switch (alt80) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1563:5: comparisonOperator right= valueExpression
                            {
                            pushFollow(FOLLOW_comparisonOperator_in_booleanPredicate8709);
                            comparisonOperator163=comparisonOperator();

                            state._fsp--;
                            if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8713);
                            right=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                              					 predicate = new ComparisonPredicate((exp1!=null?exp1.expression:null),
                                                                                       (right!=null?right.expression:null),
                                                                                       comparisonOperator163);
                              				
                            }

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1570:5: BETWEEN low= valueExpression AND up= valueExpression
                            {
                            match(input,BETWEEN,FOLLOW_BETWEEN_in_booleanPredicate8731); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8735);
                            low=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            match(input,AND,FOLLOW_AND_in_booleanPredicate8737); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8741);
                            up=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                               	 predicate = new BetweenPredicate((exp1!=null?exp1.expression:null),
                                                                                    (low!=null?low.expression:null),
                                                                                    (up!=null?up.expression:null));
                                          	
                            }

                            }
                            break;
                        case 3 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1577:14: NOT BETWEEN low= valueExpression AND up= valueExpression
                            {
                            match(input,NOT,FOLLOW_NOT_in_booleanPredicate8777); if (state.failed) return predicate;
                            match(input,BETWEEN,FOLLOW_BETWEEN_in_booleanPredicate8779); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8783);
                            low=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            match(input,AND,FOLLOW_AND_in_booleanPredicate8785); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8789);
                            up=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                          		 predicate = new NotPredicate(
                                                                  new BetweenPredicate((exp1!=null?exp1.expression:null),
                              	                                                     (low!=null?low.expression:null),
                              	                                                     (up!=null?up.expression:null)));
                                          	
                            }

                            }
                            break;
                        case 4 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1585:14: LIKE pattern= valueExpression
                            {
                            match(input,LIKE,FOLLOW_LIKE_in_booleanPredicate8834); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8838);
                            pattern=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                          		 predicate = new LikePredicate((exp1!=null?exp1.expression:null),
                                                                                 (pattern!=null?pattern.expression:null));
                                          	
                            }

                            }
                            break;
                        case 5 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1591:14: NOT LIKE pattern= valueExpression
                            {
                            match(input,NOT,FOLLOW_NOT_in_booleanPredicate8883); if (state.failed) return predicate;
                            match(input,LIKE,FOLLOW_LIKE_in_booleanPredicate8885); if (state.failed) return predicate;
                            pushFollow(FOLLOW_valueExpression_in_booleanPredicate8889);
                            pattern=valueExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                               	 predicate = new NotPredicate(new LikePredicate((exp1!=null?exp1.expression:null),
                                                                                                  (pattern!=null?pattern.expression:null)));
                                          	
                            }

                            }
                            break;
                        case 6 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1597:14: IN set1Expression= setExpression
                            {
                            match(input,IN,FOLLOW_IN_in_booleanPredicate8934); if (state.failed) return predicate;
                            pushFollow(FOLLOW_setExpression_in_booleanPredicate8938);
                            set1Expression=setExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                          		predicate = new InPredicate((exp1!=null?exp1.expression:null),
                                          		                            set1Expression);
                                          	
                            }

                            }
                            break;
                        case 7 :
                            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1603:14: NOT IN set2Expression= setExpression
                            {
                            match(input,NOT,FOLLOW_NOT_in_booleanPredicate8983); if (state.failed) return predicate;
                            match(input,IN,FOLLOW_IN_in_booleanPredicate8985); if (state.failed) return predicate;
                            pushFollow(FOLLOW_setExpression_in_booleanPredicate8989);
                            set2Expression=setExpression();

                            state._fsp--;
                            if (state.failed) return predicate;
                            if ( state.backtracking==0 ) {

                                          	    predicate =  new NotPredicate(new InPredicate((exp1!=null?exp1.expression:null),
                                                                              set2Expression));
                                          	
                            }

                            }
                            break;

                    }


                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1610:4: conditionExists
                    {
                    pushFollow(FOLLOW_conditionExists_in_booleanPredicate9020);
                    conditionExists164=conditionExists();

                    state._fsp--;
                    if (state.failed) return predicate;
                    if ( state.backtracking==0 ) {

                      				predicate = conditionExists164;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 89, booleanPredicate_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "booleanPredicate"


    // $ANTLR start "booleanAtom"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1615:1: booleanAtom returns [boolean value] : ( TRUE | FALSE );
    public final boolean booleanAtom() throws RecognitionException {
        boolean value = false;
        int booleanAtom_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 90) ) { return value; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1615:36: ( TRUE | FALSE )
            int alt82=2;
            int LA82_0 = input.LA(1);

            if ( (LA82_0==TRUE) ) {
                alt82=1;
            }
            else if ( (LA82_0==FALSE) ) {
                alt82=2;
            }
            else {
                if (state.backtracking>0) {state.failed=true; return value;}
                NoViableAltException nvae =
                    new NoViableAltException("", 82, 0, input);

                throw nvae;
            }
            switch (alt82) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1616:4: TRUE
                    {
                    match(input,TRUE,FOLLOW_TRUE_in_booleanAtom9042); if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                      				value = true;
                      			
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1621:4: FALSE
                    {
                    match(input,FALSE,FOLLOW_FALSE_in_booleanAtom9057); if (state.failed) return value;
                    if ( state.backtracking==0 ) {

                      				value = false;
                      			
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 90, booleanAtom_StartIndex); }
        }
        return value;
    }
    // $ANTLR end "booleanAtom"


    // $ANTLR start "comparisonOperator"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1627:1: comparisonOperator returns [int op] : ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSEQUAL | GREATEREQUAL );
    public final int comparisonOperator() throws RecognitionException {
        int op = 0;
        int comparisonOperator_StartIndex = input.index();
        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 91) ) { return op; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1627:36: ( EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSEQUAL | GREATEREQUAL )
            int alt83=6;
            switch ( input.LA(1) ) {
            case EQUALS:
                {
                alt83=1;
                }
                break;
            case NOTEQUALS:
                {
                alt83=2;
                }
                break;
            case LESSTHAN:
                {
                alt83=3;
                }
                break;
            case GREATERTHAN:
                {
                alt83=4;
                }
                break;
            case LESSEQUAL:
                {
                alt83=5;
                }
                break;
            case GREATEREQUAL:
                {
                alt83=6;
                }
                break;
            default:
                if (state.backtracking>0) {state.failed=true; return op;}
                NoViableAltException nvae =
                    new NoViableAltException("", 83, 0, input);

                throw nvae;
            }

            switch (alt83) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1628:8: EQUALS
                    {
                    match(input,EQUALS,FOLLOW_EQUALS_in_comparisonOperator9087); if (state.failed) return op;
                    if ( state.backtracking==0 ) {

                      		          op = FinalVariable.EQUALS;
                      		     
                    }

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1633:8: NOTEQUALS
                    {
                    match(input,NOTEQUALS,FOLLOW_NOTEQUALS_in_comparisonOperator9114); if (state.failed) return op;
                    if ( state.backtracking==0 ) {
                       
                      		          op = FinalVariable.NOTEQUALS;
                      		     
                    }

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1638:8: LESSTHAN
                    {
                    match(input,LESSTHAN,FOLLOW_LESSTHAN_in_comparisonOperator9141); if (state.failed) return op;
                    if ( state.backtracking==0 ) {

                      		          op = FinalVariable.LESSTHAN;
                      		     
                    }

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1643:8: GREATERTHAN
                    {
                    match(input,GREATERTHAN,FOLLOW_GREATERTHAN_in_comparisonOperator9168); if (state.failed) return op;
                    if ( state.backtracking==0 ) {

                      		          op = FinalVariable.GREATERTHAN;
                      		     
                    }

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1648:8: LESSEQUAL
                    {
                    match(input,LESSEQUAL,FOLLOW_LESSEQUAL_in_comparisonOperator9195); if (state.failed) return op;
                    if ( state.backtracking==0 ) {

                      		          op = FinalVariable.LESSEQUAL;
                      		     
                    }

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1653:8: GREATEREQUAL
                    {
                    match(input,GREATEREQUAL,FOLLOW_GREATEREQUAL_in_comparisonOperator9222); if (state.failed) return op;
                    if ( state.backtracking==0 ) {

                      		          op = FinalVariable.GREATEREQUAL;
                      		     
                    }

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 91, comparisonOperator_StartIndex); }
        }
        return op;
    }
    // $ANTLR end "comparisonOperator"


    // $ANTLR start "conditionExists"
    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1659:1: conditionExists returns [BooleanPredicate predicate] : EXISTS subquery ;
    public final BooleanPredicate conditionExists() throws RecognitionException {
        BooleanPredicate predicate = null;
        int conditionExists_StartIndex = input.index();
        MathExpression subquery165 = null;


        try {
            if ( state.backtracking>0 && alreadyParsedRule(input, 92) ) { return predicate; }
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1659:53: ( EXISTS subquery )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1660:4: EXISTS subquery
            {
            match(input,EXISTS,FOLLOW_EXISTS_in_conditionExists9246); if (state.failed) return predicate;
            pushFollow(FOLLOW_subquery_in_conditionExists9248);
            subquery165=subquery();

            state._fsp--;
            if (state.failed) return predicate;
            if ( state.backtracking==0 ) {

              				predicate = new ExistPredicate(subquery165);
              			
            }

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
            if ( state.backtracking>0 ) { memoize(input, 92, conditionExists_StartIndex); }
        }
        return predicate;
    }
    // $ANTLR end "conditionExists"

    // $ANTLR start synpred5_Query
    public final void synpred5_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:107:3: ( createRandomTableStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:107:3: createRandomTableStatement
        {
        pushFollow(FOLLOW_createRandomTableStatement_in_synpred5_Query290);
        createRandomTableStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred5_Query

    // $ANTLR start synpred6_Query
    public final void synpred6_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:112:3: ( createViewStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:112:3: createViewStatement
        {
        pushFollow(FOLLOW_createViewStatement_in_synpred6_Query308);
        createViewStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred6_Query

    // $ANTLR start synpred7_Query
    public final void synpred7_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:117:9: ( createTableStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:117:9: createTableStatement
        {
        pushFollow(FOLLOW_createTableStatement_in_synpred7_Query338);
        createTableStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred7_Query

    // $ANTLR start synpred13_Query
    public final void synpred13_Query_fragment() throws RecognitionException {   
        Token viewName=null;
        Token lb=null;
        Token ub=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:161:7: (viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:161:7: viewName= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET
        {
        viewName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred13_Query560); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred13_Query562); if (state.failed) return ;
        lb=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred13_Query566); if (state.failed) return ;
        match(input,TWODOT,FOLLOW_TWODOT_in_synpred13_Query568); if (state.failed) return ;
        ub=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred13_Query572); if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred13_Query574); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred13_Query

    // $ANTLR start synpred14_Query
    public final void synpred14_Query_fragment() throws RecognitionException {   
        Token tableName=null;
        Token indexName=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:166:13: (tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:166:13: tableName= IDENTIFIER LBRACKET indexName= NUMERIC RBRACKET
        {
        tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred14_Query618); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred14_Query620); if (state.failed) return ;
        indexName=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred14_Query624); if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred14_Query626); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred14_Query

    // $ANTLR start synpred34_Query
    public final void synpred34_Query_fragment() throws RecognitionException {   
        Token aliasName=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:354:13: ( indexTableReference ( AS )? aliasName= IDENTIFIER )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:354:13: indexTableReference ( AS )? aliasName= IDENTIFIER
        {
        pushFollow(FOLLOW_indexTableReference_in_synpred34_Query1813);
        indexTableReference();

        state._fsp--;
        if (state.failed) return ;
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:354:33: ( AS )?
        int alt89=2;
        int LA89_0 = input.LA(1);

        if ( (LA89_0==AS) ) {
            alt89=1;
        }
        switch (alt89) {
            case 1 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:0:0: AS
                {
                match(input,AS,FOLLOW_AS_in_synpred34_Query1815); if (state.failed) return ;

                }
                break;

        }

        aliasName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred34_Query1820); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred34_Query

    // $ANTLR start synpred35_Query
    public final void synpred35_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:363:13: ( indexTableReference )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:363:13: indexTableReference
        {
        pushFollow(FOLLOW_indexTableReference_in_synpred35_Query1862);
        indexTableReference();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred35_Query

    // $ANTLR start synpred37_Query
    public final void synpred37_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:382:13: ( tempTable )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:382:13: tempTable
        {
        pushFollow(FOLLOW_tempTable_in_synpred37_Query2008);
        tempTable();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred37_Query

    // $ANTLR start synpred39_Query
    public final void synpred39_Query_fragment() throws RecognitionException {   
        Token tableName=null;
        Token indexString=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:413:13: (tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:413:13: tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET
        {
        tableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred39_Query2110); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred39_Query2112); if (state.failed) return ;
        indexString=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred39_Query2116); if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred39_Query2118); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred39_Query

    // $ANTLR start synpred55_Query
    public final void synpred55_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:610:12: ( baselineArrayRandomStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:610:12: baselineArrayRandomStatement
        {
        pushFollow(FOLLOW_baselineArrayRandomStatement_in_synpred55_Query3303);
        baselineArrayRandomStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred55_Query

    // $ANTLR start synpred65_Query
    public final void synpred65_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:902:13: ( commonUnionViewStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:902:13: commonUnionViewStatement
        {
        pushFollow(FOLLOW_commonUnionViewStatement_in_synpred65_Query4741);
        commonUnionViewStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred65_Query

    // $ANTLR start synpred66_Query
    public final void synpred66_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:907:13: ( baselineUnionViewStatement )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:907:13: baselineUnionViewStatement
        {
        pushFollow(FOLLOW_baselineUnionViewStatement_in_synpred66_Query4783);
        baselineUnionViewStatement();

        state._fsp--;
        if (state.failed) return ;

        }
    }
    // $ANTLR end synpred66_Query

    // $ANTLR start synpred69_Query
    public final void synpred69_Query_fragment() throws RecognitionException {   
        Token baselineTableName=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:959:13: (baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:959:13: baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET
        {
        baselineTableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred69_Query5226); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred69_Query5228); if (state.failed) return ;
        match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred69_Query5230); if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred69_Query5232); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred69_Query

    // $ANTLR start synpred70_Query
    public final void synpred70_Query_fragment() throws RecognitionException {   
        Token generalTableName=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:964:13: (generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:964:13: generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET
        {
        generalTableName=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred70_Query5276); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred70_Query5278); if (state.failed) return ;
        pushFollow(FOLLOW_valueExpression_in_synpred70_Query5280);
        valueExpression();

        state._fsp--;
        if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred70_Query5282); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred70_Query

    // $ANTLR start synpred71_Query
    public final void synpred71_Query_fragment() throws RecognitionException {   
        Token baselineTableNameArray=null;
        Token lb=null;
        Token ub=null;

        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:969:13: (baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:969:13: baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET
        {
        baselineTableNameArray=(Token)match(input,IDENTIFIER,FOLLOW_IDENTIFIER_in_synpred71_Query5326); if (state.failed) return ;
        match(input,LBRACKET,FOLLOW_LBRACKET_in_synpred71_Query5328); if (state.failed) return ;
        lb=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred71_Query5332); if (state.failed) return ;
        match(input,TWODOT,FOLLOW_TWODOT_in_synpred71_Query5334); if (state.failed) return ;
        ub=(Token)match(input,NUMERIC,FOLLOW_NUMERIC_in_synpred71_Query5338); if (state.failed) return ;
        match(input,RBRACKET,FOLLOW_RBRACKET_in_synpred71_Query5340); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred71_Query

    // $ANTLR start synpred123_Query
    public final void synpred123_Query_fragment() throws RecognitionException {   
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1554:13: ( LPAREN searchCondition RPAREN )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1554:13: LPAREN searchCondition RPAREN
        {
        match(input,LPAREN,FOLLOW_LPAREN_in_synpred123_Query8633); if (state.failed) return ;
        pushFollow(FOLLOW_searchCondition_in_synpred123_Query8647);
        searchCondition();

        state._fsp--;
        if (state.failed) return ;
        match(input,RPAREN,FOLLOW_RPAREN_in_synpred123_Query8675); if (state.failed) return ;

        }
    }
    // $ANTLR end synpred123_Query

    // $ANTLR start synpred130_Query
    public final void synpred130_Query_fragment() throws RecognitionException {   
        QueryParser.valueExpression_return exp1 = null;

        QueryParser.valueExpression_return right = null;

        QueryParser.valueExpression_return low = null;

        QueryParser.valueExpression_return up = null;

        QueryParser.valueExpression_return pattern = null;

        MathExpression set1Expression = null;

        MathExpression set2Expression = null;


        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1561:4: (exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression ) )
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1561:4: exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )
        {
        pushFollow(FOLLOW_valueExpression_in_synpred130_Query8698);
        exp1=valueExpression();

        state._fsp--;
        if (state.failed) return ;
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1562:4: ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )
        int alt100=7;
        alt100 = dfa100.predict(input);
        switch (alt100) {
            case 1 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1563:5: comparisonOperator right= valueExpression
                {
                pushFollow(FOLLOW_comparisonOperator_in_synpred130_Query8709);
                comparisonOperator();

                state._fsp--;
                if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8713);
                right=valueExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 2 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1570:5: BETWEEN low= valueExpression AND up= valueExpression
                {
                match(input,BETWEEN,FOLLOW_BETWEEN_in_synpred130_Query8731); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8735);
                low=valueExpression();

                state._fsp--;
                if (state.failed) return ;
                match(input,AND,FOLLOW_AND_in_synpred130_Query8737); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8741);
                up=valueExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 3 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1577:14: NOT BETWEEN low= valueExpression AND up= valueExpression
                {
                match(input,NOT,FOLLOW_NOT_in_synpred130_Query8777); if (state.failed) return ;
                match(input,BETWEEN,FOLLOW_BETWEEN_in_synpred130_Query8779); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8783);
                low=valueExpression();

                state._fsp--;
                if (state.failed) return ;
                match(input,AND,FOLLOW_AND_in_synpred130_Query8785); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8789);
                up=valueExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 4 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1585:14: LIKE pattern= valueExpression
                {
                match(input,LIKE,FOLLOW_LIKE_in_synpred130_Query8834); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8838);
                pattern=valueExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 5 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1591:14: NOT LIKE pattern= valueExpression
                {
                match(input,NOT,FOLLOW_NOT_in_synpred130_Query8883); if (state.failed) return ;
                match(input,LIKE,FOLLOW_LIKE_in_synpred130_Query8885); if (state.failed) return ;
                pushFollow(FOLLOW_valueExpression_in_synpred130_Query8889);
                pattern=valueExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 6 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1597:14: IN set1Expression= setExpression
                {
                match(input,IN,FOLLOW_IN_in_synpred130_Query8934); if (state.failed) return ;
                pushFollow(FOLLOW_setExpression_in_synpred130_Query8938);
                set1Expression=setExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;
            case 7 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1603:14: NOT IN set2Expression= setExpression
                {
                match(input,NOT,FOLLOW_NOT_in_synpred130_Query8983); if (state.failed) return ;
                match(input,IN,FOLLOW_IN_in_synpred130_Query8985); if (state.failed) return ;
                pushFollow(FOLLOW_setExpression_in_synpred130_Query8989);
                set2Expression=setExpression();

                state._fsp--;
                if (state.failed) return ;

                }
                break;

        }


        }
    }
    // $ANTLR end synpred130_Query

    // Delegated rules

    public final boolean synpred70_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred70_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred5_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred5_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred123_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred123_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred39_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred39_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred66_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred66_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred34_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred34_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred65_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred65_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred7_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred7_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred130_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred130_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred37_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred37_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred69_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred69_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred14_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred14_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred6_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred6_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred71_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred71_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred55_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred55_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred13_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred13_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }
    public final boolean synpred35_Query() {
        state.backtracking++;
        int start = input.mark();
        try {
            synpred35_Query_fragment(); // can never throw exception
        } catch (RecognitionException re) {
            System.err.println("impossible: "+re);
        }
        boolean success = !state.failed;
        input.rewind(start);
        state.backtracking--;
        state.failed=false;
        return success;
    }


    protected DFA4 dfa4 = new DFA4(this);
    protected DFA9 dfa9 = new DFA9(this);
    protected DFA10 dfa10 = new DFA10(this);
    protected DFA11 dfa11 = new DFA11(this);
    protected DFA14 dfa14 = new DFA14(this);
    protected DFA13 dfa13 = new DFA13(this);
    protected DFA20 dfa20 = new DFA20(this);
    protected DFA21 dfa21 = new DFA21(this);
    protected DFA45 dfa45 = new DFA45(this);
    protected DFA58 dfa58 = new DFA58(this);
    protected DFA61 dfa61 = new DFA61(this);
    protected DFA63 dfa63 = new DFA63(this);
    protected DFA64 dfa64 = new DFA64(this);
    protected DFA68 dfa68 = new DFA68(this);
    protected DFA67 dfa67 = new DFA67(this);
    protected DFA69 dfa69 = new DFA69(this);
    protected DFA72 dfa72 = new DFA72(this);
    protected DFA75 dfa75 = new DFA75(this);
    protected DFA79 dfa79 = new DFA79(this);
    protected DFA78 dfa78 = new DFA78(this);
    protected DFA81 dfa81 = new DFA81(this);
    protected DFA80 dfa80 = new DFA80(this);
    protected DFA100 dfa100 = new DFA100(this);
    static final String DFA4_eotS =
        "\14\uffff";
    static final String DFA4_eofS =
        "\14\uffff";
    static final String DFA4_minS =
        "\1\45\1\6\2\7\3\uffff\2\0\3\uffff";
    static final String DFA4_maxS =
        "\1\45\1\20\2\7\3\uffff\2\0\3\uffff";
    static final String DFA4_acceptS =
        "\4\uffff\1\2\1\4\1\5\2\uffff\1\1\1\3\1\6";
    static final String DFA4_specialS =
        "\7\uffff\1\0\1\1\3\uffff}>";
    static final String[] DFA4_transitionS = {
            "\1\1",
            "\1\2\6\uffff\1\3\1\4\1\6\1\5",
            "\1\7",
            "\1\10",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            ""
    };

    static final short[] DFA4_eot = DFA.unpackEncodedString(DFA4_eotS);
    static final short[] DFA4_eof = DFA.unpackEncodedString(DFA4_eofS);
    static final char[] DFA4_min = DFA.unpackEncodedStringToUnsignedChars(DFA4_minS);
    static final char[] DFA4_max = DFA.unpackEncodedStringToUnsignedChars(DFA4_maxS);
    static final short[] DFA4_accept = DFA.unpackEncodedString(DFA4_acceptS);
    static final short[] DFA4_special = DFA.unpackEncodedString(DFA4_specialS);
    static final short[][] DFA4_transition;

    static {
        int numStates = DFA4_transitionS.length;
        DFA4_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA4_transition[i] = DFA.unpackEncodedString(DFA4_transitionS[i]);
        }
    }

    class DFA4 extends DFA {

        public DFA4(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 4;
            this.eot = DFA4_eot;
            this.eof = DFA4_eof;
            this.min = DFA4_min;
            this.max = DFA4_max;
            this.accept = DFA4_accept;
            this.special = DFA4_special;
            this.transition = DFA4_transition;
        }
        public String getDescription() {
            return "106:1: createStatement returns [Expression statement] : ( createRandomTableStatement | createViewStatement | createTableStatement | createFunctionStatement | createVGFunctionStatement | createUnionViewStatement );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA4_7 = input.LA(1);

                         
                        int index4_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred5_Query()) ) {s = 9;}

                        else if ( (synpred7_Query()) ) {s = 10;}

                         
                        input.seek(index4_7);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA4_8 = input.LA(1);

                         
                        int index4_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred6_Query()) ) {s = 4;}

                        else if ( (true) ) {s = 11;}

                         
                        input.seek(index4_8);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 4, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA9_eotS =
        "\27\uffff";
    static final String DFA9_eofS =
        "\27\uffff";
    static final String DFA9_minS =
        "\1\7\26\uffff";
    static final String DFA9_maxS =
        "\1\103\26\uffff";
    static final String DFA9_acceptS =
        "\1\uffff\1\1\1\2\1\3\23\uffff";
    static final String DFA9_specialS =
        "\27\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\3\1\uffff\1\3\2\uffff\1\3\5\uffff\1\1\1\2\2\uffff\1\3\3\uffff"+
            "\2\3\1\uffff\1\3\25\uffff\3\3\4\uffff\12\3",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA9_eot = DFA.unpackEncodedString(DFA9_eotS);
    static final short[] DFA9_eof = DFA.unpackEncodedString(DFA9_eofS);
    static final char[] DFA9_min = DFA.unpackEncodedStringToUnsignedChars(DFA9_minS);
    static final char[] DFA9_max = DFA.unpackEncodedStringToUnsignedChars(DFA9_maxS);
    static final short[] DFA9_accept = DFA.unpackEncodedString(DFA9_acceptS);
    static final short[] DFA9_special = DFA.unpackEncodedString(DFA9_specialS);
    static final short[][] DFA9_transition;

    static {
        int numStates = DFA9_transitionS.length;
        DFA9_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA9_transition[i] = DFA.unpackEncodedString(DFA9_transitionS[i]);
        }
    }

    class DFA9 extends DFA {

        public DFA9(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 9;
            this.eot = DFA9_eot;
            this.eof = DFA9_eof;
            this.min = DFA9_min;
            this.max = DFA9_max;
            this.accept = DFA9_accept;
            this.special = DFA9_special;
            this.transition = DFA9_transition;
        }
        public String getDescription() {
            return "240:1: setQuantifier returns [int setQuantifier] : ( ALL | DISTINCT | );";
        }
    }
    static final String DFA10_eotS =
        "\12\uffff";
    static final String DFA10_eofS =
        "\1\1\11\uffff";
    static final String DFA10_minS =
        "\1\4\11\uffff";
    static final String DFA10_maxS =
        "\1\44\11\uffff";
    static final String DFA10_acceptS =
        "\1\uffff\1\2\7\uffff\1\1";
    static final String DFA10_specialS =
        "\12\uffff}>";
    static final String[] DFA10_transitionS = {
            "\1\1\17\uffff\1\11\4\uffff\1\1\2\uffff\1\1\1\uffff\2\1\1\uffff"+
            "\1\1\2\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA10_eot = DFA.unpackEncodedString(DFA10_eotS);
    static final short[] DFA10_eof = DFA.unpackEncodedString(DFA10_eofS);
    static final char[] DFA10_min = DFA.unpackEncodedStringToUnsignedChars(DFA10_minS);
    static final char[] DFA10_max = DFA.unpackEncodedStringToUnsignedChars(DFA10_maxS);
    static final short[] DFA10_accept = DFA.unpackEncodedString(DFA10_acceptS);
    static final short[] DFA10_special = DFA.unpackEncodedString(DFA10_specialS);
    static final short[][] DFA10_transition;

    static {
        int numStates = DFA10_transitionS.length;
        DFA10_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA10_transition[i] = DFA.unpackEncodedString(DFA10_transitionS[i]);
        }
    }

    class DFA10 extends DFA {

        public DFA10(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 10;
            this.eot = DFA10_eot;
            this.eof = DFA10_eof;
            this.min = DFA10_min;
            this.max = DFA10_max;
            this.accept = DFA10_accept;
            this.special = DFA10_special;
            this.transition = DFA10_transition;
        }
        public String getDescription() {
            return "()* loopback of 271:13: ( COMMA l2= selectSubList )*";
        }
    }
    static final String DFA11_eotS =
        "\51\uffff";
    static final String DFA11_eofS =
        "\22\uffff\1\1\26\uffff";
    static final String DFA11_minS =
        "\1\7\21\uffff\1\4\2\uffff\1\7\23\uffff";
    static final String DFA11_maxS =
        "\1\103\21\uffff\1\71\2\uffff\1\26\23\uffff";
    static final String DFA11_acceptS =
        "\1\uffff\1\1\22\uffff\1\3\23\uffff\1\2";
    static final String DFA11_specialS =
        "\51\uffff}>";
    static final String[] DFA11_transitionS = {
            "\1\22\1\uffff\1\1\2\uffff\1\1\11\uffff\1\24\3\uffff\2\1\1\uffff"+
            "\1\1\25\uffff\3\1\4\uffff\12\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\1\2\uffff\1\1\14\uffff\1\1\1\25\4\1\1\uffff\2\1\1\uffff"+
            "\2\1\1\uffff\1\1\2\uffff\1\1\16\uffff\2\1\4\uffff\1\1",
            "",
            "",
            "\1\1\16\uffff\1\50",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA11_eot = DFA.unpackEncodedString(DFA11_eotS);
    static final short[] DFA11_eof = DFA.unpackEncodedString(DFA11_eofS);
    static final char[] DFA11_min = DFA.unpackEncodedStringToUnsignedChars(DFA11_minS);
    static final char[] DFA11_max = DFA.unpackEncodedStringToUnsignedChars(DFA11_maxS);
    static final short[] DFA11_accept = DFA.unpackEncodedString(DFA11_acceptS);
    static final short[] DFA11_special = DFA.unpackEncodedString(DFA11_specialS);
    static final short[][] DFA11_transition;

    static {
        int numStates = DFA11_transitionS.length;
        DFA11_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA11_transition[i] = DFA.unpackEncodedString(DFA11_transitionS[i]);
        }
    }

    class DFA11 extends DFA {

        public DFA11(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 11;
            this.eot = DFA11_eot;
            this.eof = DFA11_eof;
            this.min = DFA11_min;
            this.max = DFA11_max;
            this.accept = DFA11_accept;
            this.special = DFA11_special;
            this.transition = DFA11_transition;
        }
        public String getDescription() {
            return "276:1: selectSubList returns [SQLExpression vcolumn] : ( derivedColumn | IDENTIFIER DOT ASTERISK | ASTERISK );";
        }
    }
    static final String DFA14_eotS =
        "\46\uffff";
    static final String DFA14_eofS =
        "\22\uffff\1\1\23\uffff";
    static final String DFA14_minS =
        "\1\7\21\uffff\1\4\23\uffff";
    static final String DFA14_maxS =
        "\1\103\21\uffff\1\71\23\uffff";
    static final String DFA14_acceptS =
        "\1\uffff\1\1\21\uffff\1\3\1\uffff\1\2\20\uffff";
    static final String DFA14_specialS =
        "\46\uffff}>";
    static final String[] DFA14_transitionS = {
            "\1\22\1\uffff\1\1\2\uffff\1\1\15\uffff\2\1\1\uffff\1\1\25\uffff"+
            "\2\1\1\23\4\uffff\12\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\1\2\uffff\1\1\14\uffff\4\1\1\25\1\1\1\uffff\2\1\1\uffff"+
            "\2\1\1\uffff\1\1\2\uffff\1\1\16\uffff\2\1\4\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA14_eot = DFA.unpackEncodedString(DFA14_eotS);
    static final short[] DFA14_eof = DFA.unpackEncodedString(DFA14_eofS);
    static final char[] DFA14_min = DFA.unpackEncodedStringToUnsignedChars(DFA14_minS);
    static final char[] DFA14_max = DFA.unpackEncodedStringToUnsignedChars(DFA14_maxS);
    static final short[] DFA14_accept = DFA.unpackEncodedString(DFA14_acceptS);
    static final short[] DFA14_special = DFA.unpackEncodedString(DFA14_specialS);
    static final short[][] DFA14_transition;

    static {
        int numStates = DFA14_transitionS.length;
        DFA14_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA14_transition[i] = DFA.unpackEncodedString(DFA14_transitionS[i]);
        }
    }

    class DFA14 extends DFA {

        public DFA14(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 14;
            this.eot = DFA14_eot;
            this.eof = DFA14_eof;
            this.min = DFA14_min;
            this.max = DFA14_max;
            this.accept = DFA14_accept;
            this.special = DFA14_special;
            this.transition = DFA14_transition;
        }
        public String getDescription() {
            return "292:1: derivedColumn returns [SQLExpression vcolumn] : ( valueExpression ( ( AS )? IDENTIFIER | ) | IDENTIFIER EQUALS caseExpression | caseExpression );";
        }
    }
    static final String DFA13_eotS =
        "\14\uffff";
    static final String DFA13_eofS =
        "\1\3\13\uffff";
    static final String DFA13_minS =
        "\1\4\13\uffff";
    static final String DFA13_maxS =
        "\1\44\13\uffff";
    static final String DFA13_acceptS =
        "\1\uffff\1\1\1\uffff\1\2\10\uffff";
    static final String DFA13_specialS =
        "\14\uffff}>";
    static final String[] DFA13_transitionS = {
            "\1\3\2\uffff\1\1\14\uffff\1\3\2\uffff\1\1\1\uffff\1\3\2\uffff"+
            "\1\3\1\uffff\2\3\1\uffff\1\3\2\uffff\1\3",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA13_eot = DFA.unpackEncodedString(DFA13_eotS);
    static final short[] DFA13_eof = DFA.unpackEncodedString(DFA13_eofS);
    static final char[] DFA13_min = DFA.unpackEncodedStringToUnsignedChars(DFA13_minS);
    static final char[] DFA13_max = DFA.unpackEncodedStringToUnsignedChars(DFA13_maxS);
    static final short[] DFA13_accept = DFA.unpackEncodedString(DFA13_acceptS);
    static final short[] DFA13_special = DFA.unpackEncodedString(DFA13_specialS);
    static final short[][] DFA13_transition;

    static {
        int numStates = DFA13_transitionS.length;
        DFA13_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA13_transition[i] = DFA.unpackEncodedString(DFA13_transitionS[i]);
        }
    }

    class DFA13 extends DFA {

        public DFA13(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 13;
            this.eot = DFA13_eot;
            this.eof = DFA13_eof;
            this.min = DFA13_min;
            this.max = DFA13_max;
            this.accept = DFA13_accept;
            this.special = DFA13_special;
            this.transition = DFA13_transition;
        }
        public String getDescription() {
            return "294:13: ( ( AS )? IDENTIFIER | )";
        }
    }
    static final String DFA20_eotS =
        "\55\uffff";
    static final String DFA20_eofS =
        "\1\uffff\1\5\53\uffff";
    static final String DFA20_minS =
        "\1\7\1\4\1\uffff\1\33\1\7\14\uffff\1\11\27\0\4\uffff";
    static final String DFA20_maxS =
        "\1\33\1\52\1\uffff\1\33\1\103\14\uffff\1\64\27\0\4\uffff";
    static final String DFA20_acceptS =
        "\2\uffff\1\5\2\uffff\1\1\11\uffff\1\2\31\uffff\1\3\1\4\1\6\1\7";
    static final String DFA20_specialS =
        "\22\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1"+
        "\14\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\4\uffff}>";
    static final String[] DFA20_transitionS = {
            "\1\1\22\uffff\1\3\1\2",
            "\1\5\2\uffff\1\17\1\4\10\uffff\1\5\2\uffff\1\5\2\uffff\1\17"+
            "\4\uffff\1\5\1\uffff\2\5\1\uffff\1\5\2\uffff\1\5\5\uffff\1\5",
            "",
            "\1\21",
            "\1\43\1\uffff\1\22\2\uffff\1\23\15\uffff\1\41\1\42\1\uffff"+
            "\1\26\25\uffff\1\24\1\25\5\uffff\1\27\1\30\1\31\1\32\1\33\1"+
            "\34\1\35\1\36\1\37\1\40",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\46\21\uffff\1\44\1\uffff\1\45\25\uffff\1\47\1\50",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA20_eot = DFA.unpackEncodedString(DFA20_eotS);
    static final short[] DFA20_eof = DFA.unpackEncodedString(DFA20_eofS);
    static final char[] DFA20_min = DFA.unpackEncodedStringToUnsignedChars(DFA20_minS);
    static final char[] DFA20_max = DFA.unpackEncodedStringToUnsignedChars(DFA20_maxS);
    static final short[] DFA20_accept = DFA.unpackEncodedString(DFA20_acceptS);
    static final short[] DFA20_special = DFA.unpackEncodedString(DFA20_specialS);
    static final short[][] DFA20_transition;

    static {
        int numStates = DFA20_transitionS.length;
        DFA20_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA20_transition[i] = DFA.unpackEncodedString(DFA20_transitionS[i]);
        }
    }

    class DFA20 extends DFA {

        public DFA20(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 20;
            this.eot = DFA20_eot;
            this.eof = DFA20_eof;
            this.min = DFA20_min;
            this.max = DFA20_max;
            this.accept = DFA20_accept;
            this.special = DFA20_special;
            this.transition = DFA20_transition;
        }
        public String getDescription() {
            return "341:1: tableReference returns [SQLExpression expression] : ( IDENTIFIER | tableName= IDENTIFIER ( AS )? aliasName= IDENTIFIER | indexTableReference ( AS )? aliasName= IDENTIFIER | indexTableReference | subquery ( AS aliasName= IDENTIFIER ) | tempTable | tempTable ( AS )? aliasName= IDENTIFIER );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA20_18 = input.LA(1);

                         
                        int index20_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_18);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA20_19 = input.LA(1);

                         
                        int index20_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_19);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA20_20 = input.LA(1);

                         
                        int index20_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_20);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA20_21 = input.LA(1);

                         
                        int index20_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_21);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA20_22 = input.LA(1);

                         
                        int index20_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_22);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA20_23 = input.LA(1);

                         
                        int index20_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_23);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA20_24 = input.LA(1);

                         
                        int index20_24 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_24);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA20_25 = input.LA(1);

                         
                        int index20_25 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_25);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA20_26 = input.LA(1);

                         
                        int index20_26 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_26);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA20_27 = input.LA(1);

                         
                        int index20_27 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_27);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA20_28 = input.LA(1);

                         
                        int index20_28 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_28);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA20_29 = input.LA(1);

                         
                        int index20_29 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_29);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA20_30 = input.LA(1);

                         
                        int index20_30 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_30);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA20_31 = input.LA(1);

                         
                        int index20_31 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_31);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA20_32 = input.LA(1);

                         
                        int index20_32 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_32);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA20_33 = input.LA(1);

                         
                        int index20_33 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_33);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA20_34 = input.LA(1);

                         
                        int index20_34 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_34);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA20_35 = input.LA(1);

                         
                        int index20_35 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred34_Query()) ) {s = 41;}

                        else if ( (synpred35_Query()) ) {s = 42;}

                         
                        input.seek(index20_35);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA20_36 = input.LA(1);

                         
                        int index20_36 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Query()) ) {s = 43;}

                        else if ( (true) ) {s = 44;}

                         
                        input.seek(index20_36);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA20_37 = input.LA(1);

                         
                        int index20_37 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Query()) ) {s = 43;}

                        else if ( (true) ) {s = 44;}

                         
                        input.seek(index20_37);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA20_38 = input.LA(1);

                         
                        int index20_38 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Query()) ) {s = 43;}

                        else if ( (true) ) {s = 44;}

                         
                        input.seek(index20_38);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA20_39 = input.LA(1);

                         
                        int index20_39 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Query()) ) {s = 43;}

                        else if ( (true) ) {s = 44;}

                         
                        input.seek(index20_39);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA20_40 = input.LA(1);

                         
                        int index20_40 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred37_Query()) ) {s = 43;}

                        else if ( (true) ) {s = 44;}

                         
                        input.seek(index20_40);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 20, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA21_eotS =
        "\26\uffff";
    static final String DFA21_eofS =
        "\26\uffff";
    static final String DFA21_minS =
        "\1\7\1\10\1\7\1\0\22\uffff";
    static final String DFA21_maxS =
        "\1\7\1\10\1\103\1\0\22\uffff";
    static final String DFA21_acceptS =
        "\4\uffff\1\2\20\uffff\1\1";
    static final String DFA21_specialS =
        "\3\uffff\1\0\22\uffff}>";
    static final String[] DFA21_transitionS = {
            "\1\1",
            "\1\2",
            "\1\4\1\uffff\1\3\2\uffff\1\4\15\uffff\2\4\1\uffff\1\4\25\uffff"+
            "\2\4\5\uffff\12\4",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA21_eot = DFA.unpackEncodedString(DFA21_eotS);
    static final short[] DFA21_eof = DFA.unpackEncodedString(DFA21_eofS);
    static final char[] DFA21_min = DFA.unpackEncodedStringToUnsignedChars(DFA21_minS);
    static final char[] DFA21_max = DFA.unpackEncodedStringToUnsignedChars(DFA21_maxS);
    static final short[] DFA21_accept = DFA.unpackEncodedString(DFA21_acceptS);
    static final short[] DFA21_special = DFA.unpackEncodedString(DFA21_specialS);
    static final short[][] DFA21_transition;

    static {
        int numStates = DFA21_transitionS.length;
        DFA21_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA21_transition[i] = DFA.unpackEncodedString(DFA21_transitionS[i]);
        }
    }

    class DFA21 extends DFA {

        public DFA21(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 21;
            this.eot = DFA21_eot;
            this.eof = DFA21_eof;
            this.min = DFA21_min;
            this.max = DFA21_max;
            this.accept = DFA21_accept;
            this.special = DFA21_special;
            this.transition = DFA21_transition;
        }
        public String getDescription() {
            return "412:1: indexTableReference returns [String table, String index, int type, MathExpression indexMathExp] : (tableName= IDENTIFIER LBRACKET indexString= NUMERIC RBRACKET | tableName= IDENTIFIER LBRACKET valueExpression RBRACKET );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA21_3 = input.LA(1);

                         
                        int index21_3 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred39_Query()) ) {s = 21;}

                        else if ( (true) ) {s = 4;}

                         
                        input.seek(index21_3);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 21, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA45_eotS =
        "\34\uffff";
    static final String DFA45_eofS =
        "\1\uffff\1\3\32\uffff";
    static final String DFA45_minS =
        "\1\7\1\4\1\7\3\uffff\22\0\4\uffff";
    static final String DFA45_maxS =
        "\1\7\1\24\1\103\3\uffff\22\0\4\uffff";
    static final String DFA45_acceptS =
        "\3\uffff\1\1\24\uffff\1\2\1\3\1\4\1\5";
    static final String DFA45_specialS =
        "\6\uffff\1\0\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\4\uffff}>";
    static final String[] DFA45_transitionS = {
            "\1\1",
            "\1\3\3\uffff\1\2\13\uffff\1\3",
            "\1\27\1\uffff\1\6\2\uffff\1\7\15\uffff\1\25\1\26\1\uffff\1"+
            "\12\25\uffff\1\10\1\11\5\uffff\1\13\1\14\1\15\1\16\1\17\1\20"+
            "\1\21\1\22\1\23\1\24",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA45_eot = DFA.unpackEncodedString(DFA45_eotS);
    static final short[] DFA45_eof = DFA.unpackEncodedString(DFA45_eofS);
    static final char[] DFA45_min = DFA.unpackEncodedStringToUnsignedChars(DFA45_minS);
    static final char[] DFA45_max = DFA.unpackEncodedStringToUnsignedChars(DFA45_maxS);
    static final short[] DFA45_accept = DFA.unpackEncodedString(DFA45_acceptS);
    static final short[] DFA45_special = DFA.unpackEncodedString(DFA45_specialS);
    static final short[][] DFA45_transition;

    static {
        int numStates = DFA45_transitionS.length;
        DFA45_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA45_transition[i] = DFA.unpackEncodedString(DFA45_transitionS[i]);
        }
    }

    class DFA45 extends DFA {

        public DFA45(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 45;
            this.eot = DFA45_eot;
            this.eof = DFA45_eof;
            this.min = DFA45_min;
            this.max = DFA45_max;
            this.accept = DFA45_accept;
            this.special = DFA45_special;
            this.transition = DFA45_transition;
        }
        public String getDescription() {
            return "953:1: uniontableName returns [SQLExpression table] : (commonTableName= IDENTIFIER | baselineTableName= IDENTIFIER LBRACKET NUMERIC RBRACKET | generalTableName= IDENTIFIER LBRACKET valueExpression RBRACKET | baselineTableNameArray= IDENTIFIER LBRACKET lb= NUMERIC TWODOT ub= NUMERIC RBRACKET | generalTableNameArray= IDENTIFIER LBRACKET generalLowerBound= valueExpression TWODOT generalUpBound= valueExpression RBRACKET );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA45_6 = input.LA(1);

                         
                        int index45_6 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred69_Query()) ) {s = 24;}

                        else if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (synpred71_Query()) ) {s = 26;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_6);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA45_7 = input.LA(1);

                         
                        int index45_7 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_7);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA45_8 = input.LA(1);

                         
                        int index45_8 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_8);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA45_9 = input.LA(1);

                         
                        int index45_9 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_9);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA45_10 = input.LA(1);

                         
                        int index45_10 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_10);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA45_11 = input.LA(1);

                         
                        int index45_11 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_11);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA45_12 = input.LA(1);

                         
                        int index45_12 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_12);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA45_13 = input.LA(1);

                         
                        int index45_13 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_13);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA45_14 = input.LA(1);

                         
                        int index45_14 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_14);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA45_15 = input.LA(1);

                         
                        int index45_15 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_15);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA45_16 = input.LA(1);

                         
                        int index45_16 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_16);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA45_17 = input.LA(1);

                         
                        int index45_17 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_17);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA45_18 = input.LA(1);

                         
                        int index45_18 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_18);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA45_19 = input.LA(1);

                         
                        int index45_19 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_19);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA45_20 = input.LA(1);

                         
                        int index45_20 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_20);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA45_21 = input.LA(1);

                         
                        int index45_21 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_21);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA45_22 = input.LA(1);

                         
                        int index45_22 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_22);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA45_23 = input.LA(1);

                         
                        int index45_23 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred70_Query()) ) {s = 25;}

                        else if ( (true) ) {s = 27;}

                         
                        input.seek(index45_23);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 45, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA58_eotS =
        "\41\uffff";
    static final String DFA58_eofS =
        "\1\1\40\uffff";
    static final String DFA58_minS =
        "\1\4\40\uffff";
    static final String DFA58_maxS =
        "\1\117\40\uffff";
    static final String DFA58_acceptS =
        "\1\uffff\1\2\35\uffff\1\1\1\uffff";
    static final String DFA58_specialS =
        "\41\uffff}>";
    static final String[] DFA58_transitionS = {
            "\1\1\2\uffff\1\1\2\uffff\2\1\10\uffff\1\1\2\uffff\3\1\2\uffff"+
            "\1\1\1\uffff\2\1\1\uffff\4\1\4\uffff\1\1\11\uffff\2\37\1\uffff"+
            "\3\1\13\uffff\2\1\1\uffff\1\1\1\uffff\7\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA58_eot = DFA.unpackEncodedString(DFA58_eotS);
    static final short[] DFA58_eof = DFA.unpackEncodedString(DFA58_eofS);
    static final char[] DFA58_min = DFA.unpackEncodedStringToUnsignedChars(DFA58_minS);
    static final char[] DFA58_max = DFA.unpackEncodedStringToUnsignedChars(DFA58_maxS);
    static final short[] DFA58_accept = DFA.unpackEncodedString(DFA58_acceptS);
    static final short[] DFA58_special = DFA.unpackEncodedString(DFA58_specialS);
    static final short[][] DFA58_transition;

    static {
        int numStates = DFA58_transitionS.length;
        DFA58_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA58_transition[i] = DFA.unpackEncodedString(DFA58_transitionS[i]);
        }
    }

    class DFA58 extends DFA {

        public DFA58(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 58;
            this.eot = DFA58_eot;
            this.eof = DFA58_eof;
            this.min = DFA58_min;
            this.max = DFA58_max;
            this.accept = DFA58_accept;
            this.special = DFA58_special;
            this.transition = DFA58_transition;
        }
        public String getDescription() {
            return "()* loopback of 1179:4: ( ( PLUS | MINUS ) me2= mul_expr )*";
        }
    }
    static final String DFA61_eotS =
        "\25\uffff";
    static final String DFA61_eofS =
        "\25\uffff";
    static final String DFA61_minS =
        "\1\65\1\7\23\uffff";
    static final String DFA61_maxS =
        "\1\65\1\103\23\uffff";
    static final String DFA61_acceptS =
        "\2\uffff\1\1\21\uffff\1\2";
    static final String DFA61_specialS =
        "\25\uffff}>";
    static final String[] DFA61_transitionS = {
            "\1\1",
            "\1\2\1\uffff\1\2\2\uffff\1\2\15\uffff\2\2\1\uffff\1\2\25\uffff"+
            "\2\2\1\uffff\1\24\3\uffff\12\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA61_eot = DFA.unpackEncodedString(DFA61_eotS);
    static final short[] DFA61_eof = DFA.unpackEncodedString(DFA61_eofS);
    static final char[] DFA61_min = DFA.unpackEncodedStringToUnsignedChars(DFA61_minS);
    static final char[] DFA61_max = DFA.unpackEncodedStringToUnsignedChars(DFA61_maxS);
    static final short[] DFA61_accept = DFA.unpackEncodedString(DFA61_acceptS);
    static final short[] DFA61_special = DFA.unpackEncodedString(DFA61_specialS);
    static final short[][] DFA61_transition;

    static {
        int numStates = DFA61_transitionS.length;
        DFA61_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA61_transition[i] = DFA.unpackEncodedString(DFA61_transitionS[i]);
        }
    }

    class DFA61 extends DFA {

        public DFA61(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 61;
            this.eot = DFA61_eot;
            this.eof = DFA61_eof;
            this.min = DFA61_min;
            this.max = DFA61_max;
            this.accept = DFA61_accept;
            this.special = DFA61_special;
            this.transition = DFA61_transition;
        }
        public String getDescription() {
            return "1195:1: caseExpression returns [MathExpression expression] : ( ( CASE v1= valueExpression ( WHEN v2= valueExpression THEN v3= valueExpression )+ ELSE v4= valueExpression ) | ( CASE ( WHEN searchCondition THEN v3= valueExpression )+ ELSE v4= valueExpression ) );";
        }
    }
    static final String DFA63_eotS =
        "\43\uffff";
    static final String DFA63_eofS =
        "\1\1\42\uffff";
    static final String DFA63_minS =
        "\1\4\42\uffff";
    static final String DFA63_maxS =
        "\1\117\42\uffff";
    static final String DFA63_acceptS =
        "\1\uffff\1\2\37\uffff\1\1\1\uffff";
    static final String DFA63_specialS =
        "\43\uffff}>";
    static final String[] DFA63_transitionS = {
            "\1\1\2\uffff\1\1\2\uffff\2\1\10\uffff\1\1\1\uffff\1\41\3\1\2"+
            "\uffff\1\1\1\uffff\2\1\1\uffff\4\1\4\uffff\1\1\11\uffff\2\1"+
            "\1\uffff\3\1\1\41\12\uffff\2\1\1\uffff\1\1\1\uffff\7\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA63_eot = DFA.unpackEncodedString(DFA63_eotS);
    static final short[] DFA63_eof = DFA.unpackEncodedString(DFA63_eofS);
    static final char[] DFA63_min = DFA.unpackEncodedStringToUnsignedChars(DFA63_minS);
    static final char[] DFA63_max = DFA.unpackEncodedStringToUnsignedChars(DFA63_maxS);
    static final short[] DFA63_accept = DFA.unpackEncodedString(DFA63_acceptS);
    static final short[] DFA63_special = DFA.unpackEncodedString(DFA63_specialS);
    static final short[][] DFA63_transition;

    static {
        int numStates = DFA63_transitionS.length;
        DFA63_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA63_transition[i] = DFA.unpackEncodedString(DFA63_transitionS[i]);
        }
    }

    class DFA63 extends DFA {

        public DFA63(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 63;
            this.eot = DFA63_eot;
            this.eof = DFA63_eof;
            this.min = DFA63_min;
            this.max = DFA63_max;
            this.accept = DFA63_accept;
            this.special = DFA63_special;
            this.transition = DFA63_transition;
        }
        public String getDescription() {
            return "()* loopback of 1246:4: ( ( ASTERISK | SLASH ) ve2= valueExpressionPrimary )*";
        }
    }
    static final String DFA64_eotS =
        "\112\uffff";
    static final String DFA64_eofS =
        "\22\uffff\1\46\67\uffff";
    static final String DFA64_minS =
        "\1\7\20\uffff\1\7\1\4\67\uffff";
    static final String DFA64_maxS =
        "\1\103\20\uffff\1\103\1\117\67\uffff";
    static final String DFA64_acceptS =
        "\1\uffff\1\1\1\2\2\uffff\1\3\1\4\11\uffff\1\5\2\uffff\1\7\22\uffff"+
        "\1\10\42\uffff\1\6";
    static final String DFA64_specialS =
        "\112\uffff}>";
    static final String[] DFA64_transitionS = {
            "\1\22\1\uffff\1\2\2\uffff\1\1\15\uffff\1\20\1\21\1\uffff\1\5"+
            "\25\uffff\2\2\5\uffff\12\6",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\23\1\uffff\1\23\2\uffff\1\23\4\uffff\1\20\10\uffff\2\23"+
            "\1\uffff\1\23\25\uffff\2\23\5\uffff\12\23",
            "\1\46\2\uffff\1\46\2\uffff\2\46\10\uffff\6\46\1\uffff\1\111"+
            "\1\46\1\uffff\2\46\1\uffff\4\46\4\uffff\1\46\11\uffff\2\46\1"+
            "\uffff\4\46\12\uffff\2\46\1\uffff\1\46\1\uffff\7\46",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA64_eot = DFA.unpackEncodedString(DFA64_eotS);
    static final short[] DFA64_eof = DFA.unpackEncodedString(DFA64_eofS);
    static final char[] DFA64_min = DFA.unpackEncodedStringToUnsignedChars(DFA64_minS);
    static final char[] DFA64_max = DFA.unpackEncodedStringToUnsignedChars(DFA64_maxS);
    static final short[] DFA64_accept = DFA.unpackEncodedString(DFA64_acceptS);
    static final short[] DFA64_special = DFA.unpackEncodedString(DFA64_specialS);
    static final short[][] DFA64_transition;

    static {
        int numStates = DFA64_transitionS.length;
        DFA64_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA64_transition[i] = DFA.unpackEncodedString(DFA64_transitionS[i]);
        }
    }

    class DFA64 extends DFA {

        public DFA64(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 64;
            this.eot = DFA64_eot;
            this.eof = DFA64_eof;
            this.min = DFA64_min;
            this.max = DFA64_max;
            this.accept = DFA64_accept;
            this.special = DFA64_special;
            this.transition = DFA64_transition;
        }
        public String getDescription() {
            return "1262:1: valueExpressionPrimary returns [MathExpression expression] : ( GENERALTABLEINDEX | signedNumeric | STRING | setFunction | setExpression | generalFunction | LPAREN valueExpression RPAREN | columnExpression );";
        }
    }
    static final String DFA68_eotS =
        "\13\uffff";
    static final String DFA68_eofS =
        "\13\uffff";
    static final String DFA68_minS =
        "\1\72\12\uffff";
    static final String DFA68_maxS =
        "\1\103\12\uffff";
    static final String DFA68_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12";
    static final String DFA68_specialS =
        "\13\uffff}>";
    static final String[] DFA68_transitionS = {
            "\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\10\1\11\1\12",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA68_eot = DFA.unpackEncodedString(DFA68_eotS);
    static final short[] DFA68_eof = DFA.unpackEncodedString(DFA68_eofS);
    static final char[] DFA68_min = DFA.unpackEncodedStringToUnsignedChars(DFA68_minS);
    static final char[] DFA68_max = DFA.unpackEncodedStringToUnsignedChars(DFA68_maxS);
    static final short[] DFA68_accept = DFA.unpackEncodedString(DFA68_acceptS);
    static final short[] DFA68_special = DFA.unpackEncodedString(DFA68_specialS);
    static final short[][] DFA68_transition;

    static {
        int numStates = DFA68_transitionS.length;
        DFA68_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA68_transition[i] = DFA.unpackEncodedString(DFA68_transitionS[i]);
        }
    }

    class DFA68 extends DFA {

        public DFA68(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 68;
            this.eot = DFA68_eot;
            this.eof = DFA68_eof;
            this.min = DFA68_min;
            this.max = DFA68_max;
            this.accept = DFA68_accept;
            this.special = DFA68_special;
            this.transition = DFA68_transition;
        }
        public String getDescription() {
            return "1319:1: setFunction returns [MathExpression expression] : ( AVG LPAREN setQuantifier valueExpression RPAREN | SUM LPAREN setQuantifier valueExpression RPAREN | COUNT LPAREN ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN ) | MIN LPAREN setQuantifier valueExpression RPAREN | MAX LPAREN setQuantifier valueExpression RPAREN | VAR LPAREN setQuantifier valueExpression RPAREN | STDEV LPAREN setQuantifier valueExpression RPAREN | VECTOR LPAREN setQuantifier valueExpression RPAREN | ROWMATRIX LPAREN setQuantifier valueExpression RPAREN | COLMATRIX LPAREN setQuantifier valueExpression RPAREN );";
        }
    }
    static final String DFA67_eotS =
        "\51\uffff";
    static final String DFA67_eofS =
        "\51\uffff";
    static final String DFA67_minS =
        "\2\7\47\uffff";
    static final String DFA67_maxS =
        "\2\103\47\uffff";
    static final String DFA67_acceptS =
        "\2\uffff\1\1\22\uffff\1\2\23\uffff";
    static final String DFA67_specialS =
        "\51\uffff}>";
    static final String[] DFA67_transitionS = {
            "\1\2\1\uffff\1\2\2\uffff\1\2\5\uffff\1\1\1\2\2\uffff\1\25\3"+
            "\uffff\2\2\1\uffff\1\2\25\uffff\2\2\5\uffff\12\2",
            "\1\2\1\uffff\1\2\2\uffff\1\2\11\uffff\1\25\3\uffff\2\2\1\uffff"+
            "\1\2\25\uffff\2\2\5\uffff\12\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA67_eot = DFA.unpackEncodedString(DFA67_eotS);
    static final short[] DFA67_eof = DFA.unpackEncodedString(DFA67_eofS);
    static final char[] DFA67_min = DFA.unpackEncodedStringToUnsignedChars(DFA67_minS);
    static final char[] DFA67_max = DFA.unpackEncodedStringToUnsignedChars(DFA67_maxS);
    static final short[] DFA67_accept = DFA.unpackEncodedString(DFA67_acceptS);
    static final short[] DFA67_special = DFA.unpackEncodedString(DFA67_specialS);
    static final short[][] DFA67_transition;

    static {
        int numStates = DFA67_transitionS.length;
        DFA67_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA67_transition[i] = DFA.unpackEncodedString(DFA67_transitionS[i]);
        }
    }

    class DFA67 extends DFA {

        public DFA67(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 67;
            this.eot = DFA67_eot;
            this.eof = DFA67_eof;
            this.min = DFA67_min;
            this.max = DFA67_max;
            this.accept = DFA67_accept;
            this.special = DFA67_special;
            this.transition = DFA67_transition;
        }
        public String getDescription() {
            return "1335:13: ( setQuantifier valueExpression RPAREN | ( ALL )? ASTERISK RPAREN )";
        }
    }
    static final String DFA69_eotS =
        "\45\uffff";
    static final String DFA69_eofS =
        "\1\uffff\1\3\43\uffff";
    static final String DFA69_minS =
        "\1\7\1\4\43\uffff";
    static final String DFA69_maxS =
        "\1\7\1\117\43\uffff";
    static final String DFA69_acceptS =
        "\2\uffff\1\2\1\1\41\uffff";
    static final String DFA69_specialS =
        "\45\uffff}>";
    static final String[] DFA69_transitionS = {
            "\1\1",
            "\1\3\2\uffff\1\3\2\uffff\2\3\10\uffff\1\3\1\2\4\3\2\uffff\1"+
            "\3\1\uffff\2\3\1\uffff\4\3\4\uffff\1\3\11\uffff\2\3\1\uffff"+
            "\4\3\12\uffff\2\3\1\uffff\1\3\1\uffff\7\3",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA69_eot = DFA.unpackEncodedString(DFA69_eotS);
    static final short[] DFA69_eof = DFA.unpackEncodedString(DFA69_eofS);
    static final char[] DFA69_min = DFA.unpackEncodedStringToUnsignedChars(DFA69_minS);
    static final char[] DFA69_max = DFA.unpackEncodedStringToUnsignedChars(DFA69_maxS);
    static final short[] DFA69_accept = DFA.unpackEncodedString(DFA69_acceptS);
    static final short[] DFA69_special = DFA.unpackEncodedString(DFA69_specialS);
    static final short[][] DFA69_transition;

    static {
        int numStates = DFA69_transitionS.length;
        DFA69_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA69_transition[i] = DFA.unpackEncodedString(DFA69_transitionS[i]);
        }
    }

    class DFA69 extends DFA {

        public DFA69(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 69;
            this.eot = DFA69_eot;
            this.eof = DFA69_eof;
            this.min = DFA69_min;
            this.max = DFA69_max;
            this.accept = DFA69_accept;
            this.special = DFA69_special;
            this.transition = DFA69_transition;
        }
        public String getDescription() {
            return "1404:1: columnExpression returns [MathExpression expression] : (id2= IDENTIFIER | table= IDENTIFIER DOT id1= IDENTIFIER );";
        }
    }
    static final String DFA72_eotS =
        "\24\uffff";
    static final String DFA72_eofS =
        "\24\uffff";
    static final String DFA72_minS =
        "\1\7\23\uffff";
    static final String DFA72_maxS =
        "\1\103\23\uffff";
    static final String DFA72_acceptS =
        "\1\uffff\1\1\21\uffff\1\2";
    static final String DFA72_specialS =
        "\24\uffff}>";
    static final String[] DFA72_transitionS = {
            "\1\1\1\uffff\1\1\2\uffff\1\1\4\uffff\1\23\10\uffff\2\1\1\uffff"+
            "\1\1\25\uffff\2\1\5\uffff\12\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA72_eot = DFA.unpackEncodedString(DFA72_eotS);
    static final short[] DFA72_eof = DFA.unpackEncodedString(DFA72_eofS);
    static final char[] DFA72_min = DFA.unpackEncodedStringToUnsignedChars(DFA72_minS);
    static final char[] DFA72_max = DFA.unpackEncodedStringToUnsignedChars(DFA72_maxS);
    static final short[] DFA72_accept = DFA.unpackEncodedString(DFA72_acceptS);
    static final short[] DFA72_special = DFA.unpackEncodedString(DFA72_specialS);
    static final short[][] DFA72_transition;

    static {
        int numStates = DFA72_transitionS.length;
        DFA72_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA72_transition[i] = DFA.unpackEncodedString(DFA72_transitionS[i]);
        }
    }

    class DFA72 extends DFA {

        public DFA72(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 72;
            this.eot = DFA72_eot;
            this.eof = DFA72_eof;
            this.min = DFA72_min;
            this.max = DFA72_max;
            this.accept = DFA72_accept;
            this.special = DFA72_special;
            this.transition = DFA72_transition;
        }
        public String getDescription() {
            return "1449:13: ( functionParas | selectStatement )";
        }
    }
    static final String DFA75_eotS =
        "\12\uffff";
    static final String DFA75_eofS =
        "\1\1\11\uffff";
    static final String DFA75_minS =
        "\1\4\11\uffff";
    static final String DFA75_maxS =
        "\1\105\11\uffff";
    static final String DFA75_acceptS =
        "\1\uffff\1\2\7\uffff\1\1";
    static final String DFA75_specialS =
        "\12\uffff}>";
    static final String[] DFA75_transitionS = {
            "\1\1\27\uffff\1\1\2\uffff\1\1\1\uffff\1\1\2\uffff\1\1\22\uffff"+
            "\1\1\14\uffff\1\1\1\11",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA75_eot = DFA.unpackEncodedString(DFA75_eotS);
    static final short[] DFA75_eof = DFA.unpackEncodedString(DFA75_eofS);
    static final char[] DFA75_min = DFA.unpackEncodedStringToUnsignedChars(DFA75_minS);
    static final char[] DFA75_max = DFA.unpackEncodedStringToUnsignedChars(DFA75_maxS);
    static final short[] DFA75_accept = DFA.unpackEncodedString(DFA75_acceptS);
    static final short[] DFA75_special = DFA.unpackEncodedString(DFA75_specialS);
    static final short[][] DFA75_transition;

    static {
        int numStates = DFA75_transitionS.length;
        DFA75_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA75_transition[i] = DFA.unpackEncodedString(DFA75_transitionS[i]);
        }
    }

    class DFA75 extends DFA {

        public DFA75(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 75;
            this.eot = DFA75_eot;
            this.eof = DFA75_eof;
            this.min = DFA75_min;
            this.max = DFA75_max;
            this.accept = DFA75_accept;
            this.special = DFA75_special;
            this.transition = DFA75_transition;
        }
        public String getDescription() {
            return "()* loopback of 1517:4: ( AND bf2= booleanFactor )*";
        }
    }
    static final String DFA79_eotS =
        "\27\uffff";
    static final String DFA79_eofS =
        "\27\uffff";
    static final String DFA79_minS =
        "\1\7\26\uffff";
    static final String DFA79_maxS =
        "\1\120\26\uffff";
    static final String DFA79_acceptS =
        "\1\uffff\1\1\24\uffff\1\2";
    static final String DFA79_specialS =
        "\27\uffff}>";
    static final String[] DFA79_transitionS = {
            "\1\1\1\uffff\1\1\2\uffff\1\1\15\uffff\2\1\1\uffff\1\1\25\uffff"+
            "\2\1\5\uffff\12\1\2\uffff\1\1\1\uffff\1\1\1\26\6\uffff\1\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA79_eot = DFA.unpackEncodedString(DFA79_eotS);
    static final short[] DFA79_eof = DFA.unpackEncodedString(DFA79_eofS);
    static final char[] DFA79_min = DFA.unpackEncodedStringToUnsignedChars(DFA79_minS);
    static final char[] DFA79_max = DFA.unpackEncodedStringToUnsignedChars(DFA79_maxS);
    static final short[] DFA79_accept = DFA.unpackEncodedString(DFA79_acceptS);
    static final short[] DFA79_special = DFA.unpackEncodedString(DFA79_specialS);
    static final short[][] DFA79_transition;

    static {
        int numStates = DFA79_transitionS.length;
        DFA79_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA79_transition[i] = DFA.unpackEncodedString(DFA79_transitionS[i]);
        }
    }

    class DFA79 extends DFA {

        public DFA79(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 79;
            this.eot = DFA79_eot;
            this.eof = DFA79_eof;
            this.min = DFA79_min;
            this.max = DFA79_max;
            this.accept = DFA79_accept;
            this.special = DFA79_special;
            this.transition = DFA79_transition;
        }
        public String getDescription() {
            return "1524:1: booleanFactor returns [BooleanPredicate predicate] : ( booleanPredicate ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | ) | NOT booleanPredicate );";
        }
    }
    static final String DFA78_eotS =
        "\20\uffff";
    static final String DFA78_eofS =
        "\1\3\17\uffff";
    static final String DFA78_minS =
        "\1\4\2\106\15\uffff";
    static final String DFA78_maxS =
        "\1\107\2\110\15\uffff";
    static final String DFA78_acceptS =
        "\3\uffff\1\3\10\uffff\1\1\1\2\2\uffff";
    static final String DFA78_specialS =
        "\20\uffff}>";
    static final String[] DFA78_transitionS = {
            "\1\3\23\uffff\1\1\3\uffff\1\3\2\uffff\1\3\1\uffff\1\3\2\uffff"+
            "\1\3\22\uffff\1\3\14\uffff\2\3\1\uffff\1\2",
            "\1\14\1\uffff\1\15",
            "\1\15\1\uffff\1\14",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA78_eot = DFA.unpackEncodedString(DFA78_eotS);
    static final short[] DFA78_eof = DFA.unpackEncodedString(DFA78_eofS);
    static final char[] DFA78_min = DFA.unpackEncodedStringToUnsignedChars(DFA78_minS);
    static final char[] DFA78_max = DFA.unpackEncodedStringToUnsignedChars(DFA78_maxS);
    static final short[] DFA78_accept = DFA.unpackEncodedString(DFA78_acceptS);
    static final short[] DFA78_special = DFA.unpackEncodedString(DFA78_specialS);
    static final short[][] DFA78_transition;

    static {
        int numStates = DFA78_transitionS.length;
        DFA78_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA78_transition[i] = DFA.unpackEncodedString(DFA78_transitionS[i]);
        }
    }

    class DFA78 extends DFA {

        public DFA78(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 78;
            this.eot = DFA78_eot;
            this.eof = DFA78_eof;
            this.min = DFA78_min;
            this.max = DFA78_max;
            this.accept = DFA78_accept;
            this.special = DFA78_special;
            this.transition = DFA78_transition;
        }
        public String getDescription() {
            return "1526:13: ( ( EQUALS TRUE | NOTEQUALS FALSE ) | ( EQUALS FALSE | NOTEQUALS TRUE ) | )";
        }
    }
    static final String DFA81_eotS =
        "\u008f\uffff";
    static final String DFA81_eofS =
        "\u008f\uffff";
    static final String DFA81_minS =
        "\1\7\2\uffff\1\7\22\uffff\2\26\2\11\1\26\13\33\1\7\1\25\5\uffff"+
        "\4\0\13\uffff\4\0\13\uffff\6\0\13\uffff\36\0\4\uffff\6\0\13\uffff";
    static final String DFA81_maxS =
        "\1\120\2\uffff\1\120\22\uffff\2\117\2\11\1\117\13\33\1\120\1\117"+
        "\5\uffff\4\0\13\uffff\4\0\13\uffff\6\0\13\uffff\36\0\4\uffff\6\0"+
        "\13\uffff";
    static final String DFA81_acceptS =
        "\1\uffff\1\1\2\uffff\1\3\20\uffff\1\4\22\uffff\1\2\146\uffff";
    static final String DFA81_specialS =
        "\55\uffff\1\0\1\1\1\2\1\3\13\uffff\1\4\1\5\1\6\1\7\13\uffff\1\10"+
        "\1\11\1\12\1\13\1\14\1\15\13\uffff\1\16\1\17\1\20\1\21\1\22\1\23"+
        "\1\24\1\25\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35\1\36\1\37\1\40"+
        "\1\41\1\42\1\43\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53\4\uffff"+
        "\1\54\1\55\1\56\1\57\1\60\1\61\13\uffff}>";
    static final String[] DFA81_transitionS = {
            "\1\4\1\uffff\1\4\2\uffff\1\4\15\uffff\1\4\1\3\1\uffff\1\4\25"+
            "\uffff\2\4\5\uffff\12\4\2\uffff\1\1\1\uffff\1\1\7\uffff\1\25",
            "",
            "",
            "\1\47\1\uffff\1\27\2\uffff\1\26\4\uffff\1\4\10\uffff\1\45\1"+
            "\46\1\uffff\1\32\25\uffff\1\30\1\31\5\uffff\1\33\1\34\1\35\1"+
            "\36\1\37\1\40\1\41\1\42\1\43\1\44\2\uffff\1\50\1\uffff\2\50"+
            "\6\uffff\1\50",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\55\1\uffff\1\50\3\uffff\1\4\14\uffff\1\50\11\uffff\1\57"+
            "\1\60\4\uffff\1\56\15\uffff\1\50\1\uffff\7\50",
            "\1\74\1\uffff\1\50\3\uffff\1\4\14\uffff\1\50\11\uffff\1\76"+
            "\1\77\4\uffff\1\75\15\uffff\1\50\1\uffff\7\50",
            "\1\113",
            "\1\114",
            "\1\115\1\uffff\1\50\3\uffff\1\4\14\uffff\1\50\11\uffff\1\117"+
            "\1\120\4\uffff\1\116\15\uffff\1\50\1\uffff\7\50",
            "\1\134",
            "\1\135",
            "\1\136",
            "\1\137",
            "\1\140",
            "\1\141",
            "\1\142",
            "\1\143",
            "\1\144",
            "\1\145",
            "\1\146",
            "\1\171\1\uffff\1\151\2\uffff\1\150\4\uffff\1\147\10\uffff\1"+
            "\167\1\170\1\uffff\1\154\25\uffff\1\152\1\153\5\uffff\1\155"+
            "\1\156\1\157\1\160\1\161\1\162\1\163\1\164\1\165\1\166\2\uffff"+
            "\1\50\1\uffff\2\50\6\uffff\1\50",
            "\1\176\1\u0080\1\uffff\1\50\2\uffff\1\177\1\4\14\uffff\1\50"+
            "\11\uffff\1\u0082\1\u0083\4\uffff\1\u0081\15\uffff\1\50\1\uffff"+
            "\7\50",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "\1\uffff",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA81_eot = DFA.unpackEncodedString(DFA81_eotS);
    static final short[] DFA81_eof = DFA.unpackEncodedString(DFA81_eofS);
    static final char[] DFA81_min = DFA.unpackEncodedStringToUnsignedChars(DFA81_minS);
    static final char[] DFA81_max = DFA.unpackEncodedStringToUnsignedChars(DFA81_maxS);
    static final short[] DFA81_accept = DFA.unpackEncodedString(DFA81_acceptS);
    static final short[] DFA81_special = DFA.unpackEncodedString(DFA81_specialS);
    static final short[][] DFA81_transition;

    static {
        int numStates = DFA81_transitionS.length;
        DFA81_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA81_transition[i] = DFA.unpackEncodedString(DFA81_transitionS[i]);
        }
    }

    class DFA81 extends DFA {

        public DFA81(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 81;
            this.eot = DFA81_eot;
            this.eof = DFA81_eof;
            this.min = DFA81_min;
            this.max = DFA81_max;
            this.accept = DFA81_accept;
            this.special = DFA81_special;
            this.transition = DFA81_transition;
        }
        public String getDescription() {
            return "1548:1: booleanPredicate returns [BooleanPredicate predicate] : ( booleanAtom | LPAREN searchCondition RPAREN | exp1= valueExpression ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression ) | conditionExists );";
        }
        public int specialStateTransition(int s, IntStream _input) throws NoViableAltException {
            TokenStream input = (TokenStream)_input;
        	int _s = s;
            switch ( s ) {
                    case 0 : 
                        int LA81_45 = input.LA(1);

                         
                        int index81_45 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_45);
                        if ( s>=0 ) return s;
                        break;
                    case 1 : 
                        int LA81_46 = input.LA(1);

                         
                        int index81_46 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_46);
                        if ( s>=0 ) return s;
                        break;
                    case 2 : 
                        int LA81_47 = input.LA(1);

                         
                        int index81_47 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_47);
                        if ( s>=0 ) return s;
                        break;
                    case 3 : 
                        int LA81_48 = input.LA(1);

                         
                        int index81_48 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_48);
                        if ( s>=0 ) return s;
                        break;
                    case 4 : 
                        int LA81_60 = input.LA(1);

                         
                        int index81_60 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_60);
                        if ( s>=0 ) return s;
                        break;
                    case 5 : 
                        int LA81_61 = input.LA(1);

                         
                        int index81_61 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_61);
                        if ( s>=0 ) return s;
                        break;
                    case 6 : 
                        int LA81_62 = input.LA(1);

                         
                        int index81_62 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_62);
                        if ( s>=0 ) return s;
                        break;
                    case 7 : 
                        int LA81_63 = input.LA(1);

                         
                        int index81_63 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_63);
                        if ( s>=0 ) return s;
                        break;
                    case 8 : 
                        int LA81_75 = input.LA(1);

                         
                        int index81_75 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_75);
                        if ( s>=0 ) return s;
                        break;
                    case 9 : 
                        int LA81_76 = input.LA(1);

                         
                        int index81_76 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_76);
                        if ( s>=0 ) return s;
                        break;
                    case 10 : 
                        int LA81_77 = input.LA(1);

                         
                        int index81_77 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_77);
                        if ( s>=0 ) return s;
                        break;
                    case 11 : 
                        int LA81_78 = input.LA(1);

                         
                        int index81_78 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_78);
                        if ( s>=0 ) return s;
                        break;
                    case 12 : 
                        int LA81_79 = input.LA(1);

                         
                        int index81_79 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_79);
                        if ( s>=0 ) return s;
                        break;
                    case 13 : 
                        int LA81_80 = input.LA(1);

                         
                        int index81_80 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_80);
                        if ( s>=0 ) return s;
                        break;
                    case 14 : 
                        int LA81_92 = input.LA(1);

                         
                        int index81_92 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_92);
                        if ( s>=0 ) return s;
                        break;
                    case 15 : 
                        int LA81_93 = input.LA(1);

                         
                        int index81_93 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_93);
                        if ( s>=0 ) return s;
                        break;
                    case 16 : 
                        int LA81_94 = input.LA(1);

                         
                        int index81_94 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_94);
                        if ( s>=0 ) return s;
                        break;
                    case 17 : 
                        int LA81_95 = input.LA(1);

                         
                        int index81_95 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_95);
                        if ( s>=0 ) return s;
                        break;
                    case 18 : 
                        int LA81_96 = input.LA(1);

                         
                        int index81_96 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_96);
                        if ( s>=0 ) return s;
                        break;
                    case 19 : 
                        int LA81_97 = input.LA(1);

                         
                        int index81_97 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_97);
                        if ( s>=0 ) return s;
                        break;
                    case 20 : 
                        int LA81_98 = input.LA(1);

                         
                        int index81_98 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_98);
                        if ( s>=0 ) return s;
                        break;
                    case 21 : 
                        int LA81_99 = input.LA(1);

                         
                        int index81_99 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_99);
                        if ( s>=0 ) return s;
                        break;
                    case 22 : 
                        int LA81_100 = input.LA(1);

                         
                        int index81_100 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_100);
                        if ( s>=0 ) return s;
                        break;
                    case 23 : 
                        int LA81_101 = input.LA(1);

                         
                        int index81_101 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_101);
                        if ( s>=0 ) return s;
                        break;
                    case 24 : 
                        int LA81_102 = input.LA(1);

                         
                        int index81_102 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_102);
                        if ( s>=0 ) return s;
                        break;
                    case 25 : 
                        int LA81_103 = input.LA(1);

                         
                        int index81_103 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_103);
                        if ( s>=0 ) return s;
                        break;
                    case 26 : 
                        int LA81_104 = input.LA(1);

                         
                        int index81_104 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_104);
                        if ( s>=0 ) return s;
                        break;
                    case 27 : 
                        int LA81_105 = input.LA(1);

                         
                        int index81_105 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_105);
                        if ( s>=0 ) return s;
                        break;
                    case 28 : 
                        int LA81_106 = input.LA(1);

                         
                        int index81_106 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_106);
                        if ( s>=0 ) return s;
                        break;
                    case 29 : 
                        int LA81_107 = input.LA(1);

                         
                        int index81_107 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_107);
                        if ( s>=0 ) return s;
                        break;
                    case 30 : 
                        int LA81_108 = input.LA(1);

                         
                        int index81_108 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_108);
                        if ( s>=0 ) return s;
                        break;
                    case 31 : 
                        int LA81_109 = input.LA(1);

                         
                        int index81_109 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_109);
                        if ( s>=0 ) return s;
                        break;
                    case 32 : 
                        int LA81_110 = input.LA(1);

                         
                        int index81_110 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_110);
                        if ( s>=0 ) return s;
                        break;
                    case 33 : 
                        int LA81_111 = input.LA(1);

                         
                        int index81_111 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_111);
                        if ( s>=0 ) return s;
                        break;
                    case 34 : 
                        int LA81_112 = input.LA(1);

                         
                        int index81_112 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_112);
                        if ( s>=0 ) return s;
                        break;
                    case 35 : 
                        int LA81_113 = input.LA(1);

                         
                        int index81_113 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_113);
                        if ( s>=0 ) return s;
                        break;
                    case 36 : 
                        int LA81_114 = input.LA(1);

                         
                        int index81_114 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_114);
                        if ( s>=0 ) return s;
                        break;
                    case 37 : 
                        int LA81_115 = input.LA(1);

                         
                        int index81_115 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_115);
                        if ( s>=0 ) return s;
                        break;
                    case 38 : 
                        int LA81_116 = input.LA(1);

                         
                        int index81_116 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_116);
                        if ( s>=0 ) return s;
                        break;
                    case 39 : 
                        int LA81_117 = input.LA(1);

                         
                        int index81_117 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_117);
                        if ( s>=0 ) return s;
                        break;
                    case 40 : 
                        int LA81_118 = input.LA(1);

                         
                        int index81_118 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_118);
                        if ( s>=0 ) return s;
                        break;
                    case 41 : 
                        int LA81_119 = input.LA(1);

                         
                        int index81_119 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_119);
                        if ( s>=0 ) return s;
                        break;
                    case 42 : 
                        int LA81_120 = input.LA(1);

                         
                        int index81_120 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_120);
                        if ( s>=0 ) return s;
                        break;
                    case 43 : 
                        int LA81_121 = input.LA(1);

                         
                        int index81_121 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_121);
                        if ( s>=0 ) return s;
                        break;
                    case 44 : 
                        int LA81_126 = input.LA(1);

                         
                        int index81_126 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_126);
                        if ( s>=0 ) return s;
                        break;
                    case 45 : 
                        int LA81_127 = input.LA(1);

                         
                        int index81_127 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_127);
                        if ( s>=0 ) return s;
                        break;
                    case 46 : 
                        int LA81_128 = input.LA(1);

                         
                        int index81_128 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_128);
                        if ( s>=0 ) return s;
                        break;
                    case 47 : 
                        int LA81_129 = input.LA(1);

                         
                        int index81_129 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_129);
                        if ( s>=0 ) return s;
                        break;
                    case 48 : 
                        int LA81_130 = input.LA(1);

                         
                        int index81_130 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_130);
                        if ( s>=0 ) return s;
                        break;
                    case 49 : 
                        int LA81_131 = input.LA(1);

                         
                        int index81_131 = input.index();
                        input.rewind();
                        s = -1;
                        if ( (synpred123_Query()) ) {s = 40;}

                        else if ( (synpred130_Query()) ) {s = 4;}

                         
                        input.seek(index81_131);
                        if ( s>=0 ) return s;
                        break;
            }
            if (state.backtracking>0) {state.failed=true; return -1;}
            NoViableAltException nvae =
                new NoViableAltException(getDescription(), 81, _s, input);
            error(nvae);
            throw nvae;
        }
    }
    static final String DFA80_eotS =
        "\16\uffff";
    static final String DFA80_eofS =
        "\16\uffff";
    static final String DFA80_minS =
        "\1\30\7\uffff\1\51\5\uffff";
    static final String DFA80_maxS =
        "\1\117\7\uffff\1\113\5\uffff";
    static final String DFA80_acceptS =
        "\1\uffff\1\1\5\uffff\1\2\1\uffff\1\4\1\6\1\3\1\5\1\7";
    static final String DFA80_specialS =
        "\16\uffff}>";
    static final String[] DFA80_transitionS = {
            "\1\1\20\uffff\1\12\35\uffff\1\1\1\uffff\1\10\1\7\1\11\4\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\15\40\uffff\1\13\1\14",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA80_eot = DFA.unpackEncodedString(DFA80_eotS);
    static final short[] DFA80_eof = DFA.unpackEncodedString(DFA80_eofS);
    static final char[] DFA80_min = DFA.unpackEncodedStringToUnsignedChars(DFA80_minS);
    static final char[] DFA80_max = DFA.unpackEncodedStringToUnsignedChars(DFA80_maxS);
    static final short[] DFA80_accept = DFA.unpackEncodedString(DFA80_acceptS);
    static final short[] DFA80_special = DFA.unpackEncodedString(DFA80_specialS);
    static final short[][] DFA80_transition;

    static {
        int numStates = DFA80_transitionS.length;
        DFA80_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA80_transition[i] = DFA.unpackEncodedString(DFA80_transitionS[i]);
        }
    }

    class DFA80 extends DFA {

        public DFA80(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 80;
            this.eot = DFA80_eot;
            this.eof = DFA80_eof;
            this.min = DFA80_min;
            this.max = DFA80_max;
            this.accept = DFA80_accept;
            this.special = DFA80_special;
            this.transition = DFA80_transition;
        }
        public String getDescription() {
            return "1562:4: ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )";
        }
    }
    static final String DFA100_eotS =
        "\16\uffff";
    static final String DFA100_eofS =
        "\16\uffff";
    static final String DFA100_minS =
        "\1\30\7\uffff\1\51\5\uffff";
    static final String DFA100_maxS =
        "\1\117\7\uffff\1\113\5\uffff";
    static final String DFA100_acceptS =
        "\1\uffff\1\1\5\uffff\1\2\1\uffff\1\4\1\6\1\3\1\5\1\7";
    static final String DFA100_specialS =
        "\16\uffff}>";
    static final String[] DFA100_transitionS = {
            "\1\1\20\uffff\1\12\35\uffff\1\1\1\uffff\1\10\1\7\1\11\4\1",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\15\40\uffff\1\13\1\14",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA100_eot = DFA.unpackEncodedString(DFA100_eotS);
    static final short[] DFA100_eof = DFA.unpackEncodedString(DFA100_eofS);
    static final char[] DFA100_min = DFA.unpackEncodedStringToUnsignedChars(DFA100_minS);
    static final char[] DFA100_max = DFA.unpackEncodedStringToUnsignedChars(DFA100_maxS);
    static final short[] DFA100_accept = DFA.unpackEncodedString(DFA100_acceptS);
    static final short[] DFA100_special = DFA.unpackEncodedString(DFA100_specialS);
    static final short[][] DFA100_transition;

    static {
        int numStates = DFA100_transitionS.length;
        DFA100_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA100_transition[i] = DFA.unpackEncodedString(DFA100_transitionS[i]);
        }
    }

    class DFA100 extends DFA {

        public DFA100(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 100;
            this.eot = DFA100_eot;
            this.eof = DFA100_eof;
            this.min = DFA100_min;
            this.max = DFA100_max;
            this.accept = DFA100_accept;
            this.special = DFA100_special;
            this.transition = DFA100_transition;
        }
        public String getDescription() {
            return "1562:4: ( comparisonOperator right= valueExpression | BETWEEN low= valueExpression AND up= valueExpression | NOT BETWEEN low= valueExpression AND up= valueExpression | LIKE pattern= valueExpression | NOT LIKE pattern= valueExpression | IN set1Expression= setExpression | NOT IN set2Expression= setExpression )";
        }
    }
 

    public static final BitSet FOLLOW_statements_in_prog71 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_statement_in_statements100 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_SEMICOLON_in_statements127 = new BitSet(new long[]{0x0000002000020020L});
    public static final BitSet FOLLOW_statement_in_statements142 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_SEMICOLON_in_statements174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectStatement_in_statement195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createStatement_in_statement228 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropStatement_in_statement255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createRandomTableStatement_in_createStatement290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createViewStatement_in_createStatement308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createTableStatement_in_createStatement338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createFunctionStatement_in_createStatement368 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createVGFunctionStatement_in_createStatement398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createUnionViewStatement_in_createStatement428 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropTableStatement_in_dropStatement460 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropViewStatement_in_dropStatement475 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropFunctionStatement_in_dropStatement496 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_dropVGFunctionStatement_in_dropStatement523 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropTableStatement543 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_TABLE_in_dropTableStatement545 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropTableStatement560 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_dropTableStatement562 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_dropTableStatement566 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_dropTableStatement568 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_dropTableStatement572 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_dropTableStatement574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropTableStatement618 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_dropTableStatement620 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_dropTableStatement624 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_dropTableStatement626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropTableStatement643 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_dropTableStatement645 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_GENERALTABLEINDEX_in_dropTableStatement647 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_dropTableStatement649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropTableStatement666 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropViewStatement699 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_dropViewStatement701 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropViewStatement705 = new BitSet(new long[]{0x0000000000000102L});
    public static final BitSet FOLLOW_LBRACKET_in_dropViewStatement714 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_dropViewStatement718 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_dropViewStatement720 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LBRACKET_in_dropViewStatement753 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_GENERALTABLEINDEX_in_dropViewStatement755 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_dropViewStatement757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropViewStatement802 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_MATERIALIZED_in_dropViewStatement804 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_dropViewStatement806 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropViewStatement810 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropVGFunctionStatement837 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_VGFUNCTION_in_dropVGFunctionStatement839 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropVGFunctionStatement843 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DROP_in_dropFunctionStatement868 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_FUNCTION_in_dropFunctionStatement870 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_dropFunctionStatement874 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SELECT_in_selectStatement915 = new BitSet(new long[]{0xFC3800002C4C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_selectStatement917 = new BitSet(new long[]{0xFC3800002C4C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_selectList_in_selectStatement919 = new BitSet(new long[]{0x00000012C2000000L});
    public static final BitSet FOLLOW_fromClause_in_selectStatement932 = new BitSet(new long[]{0x00000012C0000000L});
    public static final BitSet FOLLOW_whereClause_in_selectStatement946 = new BitSet(new long[]{0x0000001280000000L});
    public static final BitSet FOLLOW_groupByClause_in_selectStatement960 = new BitSet(new long[]{0x0000001200000000L});
    public static final BitSet FOLLOW_havingClause_in_selectStatement974 = new BitSet(new long[]{0x0000000200000000L});
    public static final BitSet FOLLOW_orderByClause_in_selectStatement987 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_setQuantifier1023 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_DISTINCT_in_setQuantifier1065 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectVarious_in_selectList1124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_selectSubList_in_selectVarious1156 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_selectVarious1185 = new BitSet(new long[]{0xFC3800002C4C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_selectSubList_in_selectVarious1189 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_derivedColumn_in_selectSubList1240 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_selectSubList1282 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_DOT_in_selectSubList1284 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_ASTERISK_in_selectSubList1286 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ASTERISK_in_selectSubList1328 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_derivedColumn1358 = new BitSet(new long[]{0x0000000000800082L});
    public static final BitSet FOLLOW_AS_in_derivedColumn1387 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_derivedColumn1390 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_derivedColumn1468 = new BitSet(new long[]{0x0000000001000000L});
    public static final BitSet FOLLOW_EQUALS_in_derivedColumn1470 = new BitSet(new long[]{0xFC3800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_caseExpression_in_derivedColumn1472 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_caseExpression_in_derivedColumn1505 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FROM_in_fromClause1568 = new BitSet(new long[]{0x000000000C000080L});
    public static final BitSet FOLLOW_tableReferenceList_in_fromClause1570 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tableReference_in_tableReferenceList1622 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_tableReferenceList1651 = new BitSet(new long[]{0x000000000C000080L});
    public static final BitSet FOLLOW_tableReference_in_tableReferenceList1655 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference1706 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference1750 = new BitSet(new long[]{0x0000000000800080L});
    public static final BitSet FOLLOW_AS_in_tableReference1752 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference1757 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTableReference_in_tableReference1813 = new BitSet(new long[]{0x0000000000800080L});
    public static final BitSet FOLLOW_AS_in_tableReference1815 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTableReference_in_tableReference1862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_tableReference1918 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_tableReference1947 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference1951 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tempTable_in_tableReference2008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tempTable_in_tableReference2050 = new BitSet(new long[]{0x0000000000800080L});
    public static final BitSet FOLLOW_AS_in_tableReference2052 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tableReference2059 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_indexTableReference2110 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_indexTableReference2112 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_indexTableReference2116 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_indexTableReference2118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_indexTableReference2162 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_indexTableReference2164 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_indexTableReference2166 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_indexTableReference2168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_tempTable2211 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_tempTableRow_in_tempTable2230 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tempTableRowList_in_tempTable2260 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tempTableRowList2305 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_tempTableRow_in_tempTableRowList2314 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_tempTableRowList2343 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_tempTableRow_in_tempTableRowList2349 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_tempTableRowList2379 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_tempTableRow2407 = new BitSet(new long[]{0x0018000020000200L});
    public static final BitSet FOLLOW_constantValue_in_tempTableRow2416 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_tempTableRow2427 = new BitSet(new long[]{0x0018000020000200L});
    public static final BitSet FOLLOW_constantValue_in_tempTableRow2433 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_tempTableRow2463 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_constantValue2489 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumeric_in_constantValue2531 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_WHERE_in_whereClause2594 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_searchCondition_in_whereClause2596 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GROUP_in_groupByClause2691 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_BY_in_groupByClause2693 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_groupingColumns_in_groupByClause2695 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnExpression_in_groupingColumns2787 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_groupingColumns2816 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_columnExpression_in_groupingColumns2820 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_ORDER_in_orderByClause2866 = new BitSet(new long[]{0x0000000100000000L});
    public static final BitSet FOLLOW_BY_in_orderByClause2868 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_orderByColumns_in_orderByClause2870 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_orderByColumns2906 = new BitSet(new long[]{0x0000000C00100002L});
    public static final BitSet FOLLOW_ASC_in_orderByColumns2930 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_DESC_in_orderByColumns2961 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_orderByColumns2989 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_orderByColumns2993 = new BitSet(new long[]{0x0000000C00100002L});
    public static final BitSet FOLLOW_ASC_in_orderByColumns3023 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_DESC_in_orderByColumns3054 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_HAVING_in_havingClause3129 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_searchCondition_in_havingClause3131 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_subquery3180 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_selectStatement_in_subquery3185 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_subquery3195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createRandomTableStatement3220 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_TABLE_in_createRandomTableStatement3222 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_nonIndexRandomStatement_in_createRandomTableStatement3245 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineArrayRandomStatement_in_createRandomTableStatement3303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineRandomStatement_in_createRandomTableStatement3342 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalRandomStatement_in_createRandomTableStatement3381 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schema_in_nonIndexRandomStatement3452 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_nonIndexRandomStatement3454 = new BitSet(new long[]{0x0000048000020000L});
    public static final BitSet FOLLOW_randomParameters_in_nonIndexRandomStatement3466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineSchema_in_baselineRandomStatement3526 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_baselineRandomStatement3528 = new BitSet(new long[]{0x0000048000020000L});
    public static final BitSet FOLLOW_randomParameters_in_baselineRandomStatement3540 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineArraySchema_in_baselineArrayRandomStatement3583 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_baselineArrayRandomStatement3585 = new BitSet(new long[]{0x0000048000020000L});
    public static final BitSet FOLLOW_randomParameters_in_baselineArrayRandomStatement3598 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalSchema_in_generalRandomStatement3641 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_generalRandomStatement3643 = new BitSet(new long[]{0x0000048000020000L});
    public static final BitSet FOLLOW_randomParameters_in_generalRandomStatement3655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schemaName_in_baselineSchema3694 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_baselineSchema3696 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_baselineSchema3698 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_baselineSchema3700 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_LPAREN_in_baselineSchema3718 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeList_in_baselineSchema3728 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_baselineSchema3738 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schemaName_in_baselineArraySchema3806 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_baselineArraySchema3808 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_GENERALTABLEINDEX_in_baselineArraySchema3810 = new BitSet(new long[]{0x0000004000000000L});
    public static final BitSet FOLLOW_COLON_in_baselineArraySchema3812 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_baselineArraySchema3816 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_baselineArraySchema3818 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_baselineArraySchema3822 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_baselineArraySchema3824 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_LPAREN_in_baselineArraySchema3842 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeList_in_baselineArraySchema3852 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_baselineArraySchema3862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schemaName_in_generalSchema3930 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_generalSchema3932 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_GENERALTABLEINDEX_in_generalSchema3934 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_generalSchema3936 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_LPAREN_in_generalSchema3954 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeList_in_generalSchema3964 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_generalSchema3974 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOR_in_randomParameters4048 = new BitSet(new long[]{0x0000010000000000L});
    public static final BitSet FOLLOW_forsentense_in_randomParameters4050 = new BitSet(new long[]{0x0000040000020000L});
    public static final BitSet FOLLOW_withStatements_in_randomParameters4077 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_selectStatement_in_randomParameters4089 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_withStatements_in_randomParameters4126 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_selectStatement_in_randomParameters4138 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EACH_in_forsentense4175 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_tupleName_in_forsentense4177 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_IN_in_forsentense4179 = new BitSet(new long[]{0x000000000C000080L});
    public static final BitSet FOLLOW_tableReference_in_forsentense4181 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_schemaName_in_schema4210 = new BitSet(new long[]{0x0000000008000002L});
    public static final BitSet FOLLOW_LPAREN_in_schema4221 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeList_in_schema4227 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_schema4233 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_schemaName4274 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attribute_in_attributeList4299 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_attributeList4310 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attribute_in_attributeList4316 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_attribute4341 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tupleName4367 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_withStatement_in_withStatements4395 = new BitSet(new long[]{0x0000040000000002L});
    public static final BitSet FOLLOW_WITH_in_withStatement4419 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_tempVGTable_in_withStatement4421 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_withStatement4423 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_vgFunctionStatement_in_withStatement4425 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_tempVGTable4443 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalFunction_in_vgFunctionStatement4466 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createViewStatement4502 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_createViewStatement4504 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_schema_in_createViewStatement4506 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_createViewStatement4508 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_selectStatement_in_createViewStatement4522 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createViewStatement4564 = new BitSet(new long[]{0x0000000000004000L});
    public static final BitSet FOLLOW_MATERIALIZED_in_createViewStatement4566 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_createViewStatement4568 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_schema_in_createViewStatement4570 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_createViewStatement4572 = new BitSet(new long[]{0x0000000000020000L});
    public static final BitSet FOLLOW_selectStatement_in_createViewStatement4586 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createTableStatement4637 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_TABLE_in_createTableStatement4639 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_definedTableName_in_createTableStatement4641 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_table_contents_source_in_createTableStatement4643 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createVGFunctionStatement4664 = new BitSet(new long[]{0x0000000000008000L});
    public static final BitSet FOLLOW_VGFUNCTION_in_createVGFunctionStatement4666 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_createVGFunctionStatement4670 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_functionInAtts_in_createVGFunctionStatement4672 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_RETURNS_in_createVGFunctionStatement4674 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_vgFunctionOutAtts_in_createVGFunctionStatement4676 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_SOURCE_in_createVGFunctionStatement4678 = new BitSet(new long[]{0x0000000020000080L});
    public static final BitSet FOLLOW_file_in_createVGFunctionStatement4680 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_createFunctionStatement4698 = new BitSet(new long[]{0x0000000000010000L});
    public static final BitSet FOLLOW_FUNCTION_in_createFunctionStatement4700 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_createFunctionStatement4704 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_functionInAtts_in_createFunctionStatement4706 = new BitSet(new long[]{0x0000080000000000L});
    public static final BitSet FOLLOW_RETURNS_in_createFunctionStatement4708 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_returnType_in_createFunctionStatement4710 = new BitSet(new long[]{0x0000100000000000L});
    public static final BitSet FOLLOW_SOURCE_in_createFunctionStatement4712 = new BitSet(new long[]{0x0000000020000080L});
    public static final BitSet FOLLOW_file_in_createFunctionStatement4714 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_commonUnionViewStatement_in_createUnionViewStatement4741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineUnionViewStatement_in_createUnionViewStatement4783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalUnionViewStatement_in_createUnionViewStatement4825 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_commonUnionViewStatement4874 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_commonUnionViewStatement4876 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_schema_in_commonUnionViewStatement4878 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_commonUnionViewStatement4880 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_UNION_in_commonUnionViewStatement4882 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_tableNameList_in_commonUnionViewStatement4897 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_baselineUnionViewStatement4948 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_baselineUnionViewStatement4950 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_baselineSchema_in_baselineUnionViewStatement4952 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_baselineUnionViewStatement4954 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_UNION_in_baselineUnionViewStatement4956 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_tableNameList_in_baselineUnionViewStatement4971 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CREATE_in_generalUnionViewStatement5022 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_VIEW_in_generalUnionViewStatement5024 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_generalSchema_in_generalUnionViewStatement5026 = new BitSet(new long[]{0x0000000000800000L});
    public static final BitSet FOLLOW_AS_in_generalUnionViewStatement5028 = new BitSet(new long[]{0x0000200000000000L});
    public static final BitSet FOLLOW_UNION_in_generalUnionViewStatement5030 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_tableNameList_in_generalUnionViewStatement5045 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_uniontableName_in_tableNameList5097 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_tableNameList5126 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_uniontableName_in_tableNameList5130 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_uniontableName5182 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_uniontableName5226 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_uniontableName5228 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_uniontableName5230 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_uniontableName5232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_uniontableName5276 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_uniontableName5278 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_uniontableName5280 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_uniontableName5282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_uniontableName5326 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_uniontableName5328 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_uniontableName5332 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_uniontableName5334 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_uniontableName5338 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_uniontableName5340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_uniontableName5384 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_uniontableName5386 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_uniontableName5390 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_uniontableName5392 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_uniontableName5396 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_uniontableName5398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_file5438 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_file5453 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_functionInAtts5476 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeDefineElement_in_functionInAtts5483 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_functionInAtts5515 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeDefineElement_in_functionInAtts5522 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_functionInAtts5534 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_vgFunctionOutAtts5551 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeDefineElement_in_vgFunctionOutAtts5558 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_vgFunctionOutAtts5590 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_attributeDefineElement_in_vgFunctionOutAtts5597 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_vgFunctionOutAtts5613 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_type_in_returnType5631 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_definedTableName5655 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_table_contents_source5676 = new BitSet(new long[]{0x0001400000000080L});
    public static final BitSet FOLLOW_attributeElement_in_table_contents_source5683 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_table_contents_source5694 = new BitSet(new long[]{0x0001400000000080L});
    public static final BitSet FOLLOW_attributeElement_in_table_contents_source5701 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_table_contents_source5713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attributeDefineElement_in_attributeElement5730 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_attributeConstraintElement_in_attributeElement5745 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_attributeDefineElement5774 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_type_in_attributeDefineElement5783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_primaryKeyStatement_in_attributeConstraintElement5804 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_foreignKeyStatement_in_attributeConstraintElement5819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PRIMARY_in_primaryKeyStatement5840 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_KEY_in_primaryKeyStatement5842 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_primaryKeyStatement5848 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_primaryKeyStatement5855 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_primaryKeyStatement5871 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_primaryKeyStatement5880 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_primaryKeyStatement5898 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FOREIGN_in_foreignKeyStatement5914 = new BitSet(new long[]{0x0000800000000000L});
    public static final BitSet FOLLOW_KEY_in_foreignKeyStatement5916 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_foreignKeyStatement5921 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_foreignKeyStatement5928 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_foreignKeyStatement5933 = new BitSet(new long[]{0x0002000000000000L});
    public static final BitSet FOLLOW_REFERENCES_in_foreignKeyStatement5938 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_foreignKeyStatement5945 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_foreignKeyStatement5950 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_foreignKeyStatement5957 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_foreignKeyStatement5962 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_type5987 = new BitSet(new long[]{0x0004000000000102L});
    public static final BitSet FOLLOW_LBRACKET_in_type6015 = new BitSet(new long[]{0x0000000000000A80L});
    public static final BitSet FOLLOW_length_in_type6045 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_type6076 = new BitSet(new long[]{0x0004000000000102L});
    public static final BitSet FOLLOW_RANDOM_in_type6113 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_length6158 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMERIC_in_length6174 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_mul_expr_in_valueExpression6228 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_PLUS_in_valueExpression6245 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_MINUS_in_valueExpression6264 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_mul_expr_in_valueExpression6281 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_CASE_in_caseExpression6316 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6322 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_WHEN_in_caseExpression6342 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6348 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_THEN_in_caseExpression6350 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6356 = new BitSet(new long[]{0x0140000000000000L});
    public static final BitSet FOLLOW_ELSE_in_caseExpression6378 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6384 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_CASE_in_caseExpression6411 = new BitSet(new long[]{0x0040000000000000L});
    public static final BitSet FOLLOW_WHEN_in_caseExpression6431 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_searchCondition_in_caseExpression6433 = new BitSet(new long[]{0x0080000000000000L});
    public static final BitSet FOLLOW_THEN_in_caseExpression6435 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6441 = new BitSet(new long[]{0x0140000000000000L});
    public static final BitSet FOLLOW_ELSE_in_caseExpression6465 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_caseExpression6471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpressionPrimary_in_mul_expr6534 = new BitSet(new long[]{0x0200000000400002L});
    public static final BitSet FOLLOW_ASTERISK_in_mul_expr6551 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_SLASH_in_mul_expr6570 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpressionPrimary_in_mul_expr6587 = new BitSet(new long[]{0x0200000000400002L});
    public static final BitSet FOLLOW_GENERALTABLEINDEX_in_valueExpressionPrimary6623 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_signedNumeric_in_valueExpressionPrimary6665 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_valueExpressionPrimary6708 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setFunction_in_valueExpressionPrimary6750 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_setExpression_in_valueExpressionPrimary6792 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_generalFunction_in_valueExpressionPrimary6834 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_valueExpressionPrimary6877 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_valueExpressionPrimary6879 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_valueExpressionPrimary6881 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_columnExpression_in_valueExpressionPrimary6924 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMERIC_in_signedNumeric6961 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_PLUS_in_signedNumeric7003 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_signedNumeric7005 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MINUS_in_signedNumeric7047 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_signedNumeric7049 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_AVG_in_setFunction7099 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7101 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7103 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7105 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7107 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_SUM_in_setFunction7149 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7151 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7153 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7155 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7157 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COUNT_in_setFunction7199 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7201 = new BitSet(new long[]{0xFC1800002C4C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7231 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7233 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7235 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ALL_in_setFunction7280 = new BitSet(new long[]{0x0000000000400000L});
    public static final BitSet FOLLOW_ASTERISK_in_setFunction7283 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7285 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MIN_in_setFunction7342 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7344 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7346 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7348 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7350 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_MAX_in_setFunction7392 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7394 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7396 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7398 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7400 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VAR_in_setFunction7442 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7444 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7446 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7448 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7450 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STDEV_in_setFunction7492 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7494 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7496 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7498 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7500 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VECTOR_in_setFunction7542 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7544 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7546 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7548 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7550 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ROWMATRIX_in_setFunction7592 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7594 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7596 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7598 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7600 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_COLMATRIX_in_setFunction7642 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_setFunction7644 = new BitSet(new long[]{0xFC1800002C0C1280L,0x000000000000000FL});
    public static final BitSet FOLLOW_setQuantifier_in_setFunction7646 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_setFunction7648 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_setFunction7650 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_columnExpression7717 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_columnExpression7761 = new BitSet(new long[]{0x0000000000200000L});
    public static final BitSet FOLLOW_DOT_in_columnExpression7763 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_columnExpression7767 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_VALUES_in_setExpression7808 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_enumerationExpression_in_setExpression7819 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_subquery_in_setExpression7840 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_enumerationExpression7864 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_enumerationExpression7868 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_COMMA_in_enumerationExpression7884 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_enumerationExpression7888 = new BitSet(new long[]{0x0000000010100000L});
    public static final BitSet FOLLOW_RPAREN_in_enumerationExpression7902 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionName_in_generalFunction7925 = new BitSet(new long[]{0x0000000008000000L});
    public static final BitSet FOLLOW_LPAREN_in_generalFunction7939 = new BitSet(new long[]{0xFC1800002C021280L,0x000000000000000FL});
    public static final BitSet FOLLOW_functionParas_in_generalFunction7968 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_selectStatement_in_generalFunction8013 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_generalFunction8047 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_functionName8085 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_functionPara_in_functionParas8109 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_COMMA_in_functionParas8137 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_functionPara_in_functionParas8143 = new BitSet(new long[]{0x0000000000100002L});
    public static final BitSet FOLLOW_valueExpression_in_functionPara8193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanTerm_in_searchCondition8232 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_OR_in_searchCondition8248 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_booleanTerm_in_searchCondition8254 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000010L});
    public static final BitSet FOLLOW_booleanFactor_in_booleanTerm8296 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_AND_in_booleanTerm8312 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_booleanFactor_in_booleanTerm8318 = new BitSet(new long[]{0x0000000000000002L,0x0000000000000020L});
    public static final BitSet FOLLOW_booleanPredicate_in_booleanFactor8365 = new BitSet(new long[]{0x0000000001000002L,0x0000000000000080L});
    public static final BitSet FOLLOW_EQUALS_in_booleanFactor8396 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_TRUE_in_booleanFactor8398 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUALS_in_booleanFactor8402 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_FALSE_in_booleanFactor8404 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_booleanFactor8454 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000100L});
    public static final BitSet FOLLOW_FALSE_in_booleanFactor8456 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUALS_in_booleanFactor8460 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000040L});
    public static final BitSet FOLLOW_TRUE_in_booleanFactor8462 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanFactor8553 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001014FL});
    public static final BitSet FOLLOW_booleanPredicate_in_booleanFactor8555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_booleanAtom_in_booleanPredicate8608 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_booleanPredicate8633 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_searchCondition_in_booleanPredicate8647 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_booleanPredicate8675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8698 = new BitSet(new long[]{0x0000020001000000L,0x000000000000FE80L});
    public static final BitSet FOLLOW_comparisonOperator_in_booleanPredicate8709 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BETWEEN_in_booleanPredicate8731 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8735 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AND_in_booleanPredicate8737 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanPredicate8777 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_BETWEEN_in_booleanPredicate8779 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8783 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AND_in_booleanPredicate8785 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIKE_in_booleanPredicate8834 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanPredicate8883 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_LIKE_in_booleanPredicate8885 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_booleanPredicate8889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_booleanPredicate8934 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_setExpression_in_booleanPredicate8938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_booleanPredicate8983 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_IN_in_booleanPredicate8985 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_setExpression_in_booleanPredicate8989 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_conditionExists_in_booleanPredicate9020 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_TRUE_in_booleanAtom9042 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_FALSE_in_booleanAtom9057 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EQUALS_in_comparisonOperator9087 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOTEQUALS_in_comparisonOperator9114 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESSTHAN_in_comparisonOperator9141 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATERTHAN_in_comparisonOperator9168 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LESSEQUAL_in_comparisonOperator9195 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_GREATEREQUAL_in_comparisonOperator9222 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_EXISTS_in_conditionExists9246 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_subquery_in_conditionExists9248 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createRandomTableStatement_in_synpred5_Query290 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createViewStatement_in_synpred6_Query308 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_createTableStatement_in_synpred7_Query338 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred13_Query560 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred13_Query562 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred13_Query566 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_synpred13_Query568 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred13_Query572 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred13_Query574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred14_Query618 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred14_Query620 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred14_Query624 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred14_Query626 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTableReference_in_synpred34_Query1813 = new BitSet(new long[]{0x0000000000800080L});
    public static final BitSet FOLLOW_AS_in_synpred34_Query1815 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred34_Query1820 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_indexTableReference_in_synpred35_Query1862 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_tempTable_in_synpred37_Query2008 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred39_Query2110 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred39_Query2112 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred39_Query2116 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred39_Query2118 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineArrayRandomStatement_in_synpred55_Query3303 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_commonUnionViewStatement_in_synpred65_Query4741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_baselineUnionViewStatement_in_synpred66_Query4783 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred69_Query5226 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred69_Query5228 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred69_Query5230 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred69_Query5232 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred70_Query5276 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred70_Query5278 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred70_Query5280 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred70_Query5282 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IDENTIFIER_in_synpred71_Query5326 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_LBRACKET_in_synpred71_Query5328 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred71_Query5332 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_TWODOT_in_synpred71_Query5334 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_NUMERIC_in_synpred71_Query5338 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_RBRACKET_in_synpred71_Query5340 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LPAREN_in_synpred123_Query8633 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000001034FL});
    public static final BitSet FOLLOW_searchCondition_in_synpred123_Query8647 = new BitSet(new long[]{0x0000000010000000L});
    public static final BitSet FOLLOW_RPAREN_in_synpred123_Query8675 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8698 = new BitSet(new long[]{0x0000020001000000L,0x000000000000FE80L});
    public static final BitSet FOLLOW_comparisonOperator_in_synpred130_Query8709 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8713 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_BETWEEN_in_synpred130_Query8731 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8735 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AND_in_synpred130_Query8737 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8741 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_synpred130_Query8777 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000400L});
    public static final BitSet FOLLOW_BETWEEN_in_synpred130_Query8779 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8783 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000020L});
    public static final BitSet FOLLOW_AND_in_synpred130_Query8785 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8789 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_LIKE_in_synpred130_Query8834 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8838 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_synpred130_Query8883 = new BitSet(new long[]{0x0000000000000000L,0x0000000000000800L});
    public static final BitSet FOLLOW_LIKE_in_synpred130_Query8885 = new BitSet(new long[]{0xFC1800002C001280L,0x000000000000000FL});
    public static final BitSet FOLLOW_valueExpression_in_synpred130_Query8889 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_IN_in_synpred130_Query8934 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_setExpression_in_synpred130_Query8938 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NOT_in_synpred130_Query8983 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_IN_in_synpred130_Query8985 = new BitSet(new long[]{0x000000000C000000L});
    public static final BitSet FOLLOW_setExpression_in_synpred130_Query8989 = new BitSet(new long[]{0x0000000000000002L});

}