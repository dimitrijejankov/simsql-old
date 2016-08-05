// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g 2016-08-03 05:31:18


  package simsql.runtime;

  import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.Map;



import org.antlr.runtime.*;

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
public class DataFlowParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "Identifier", "Int", "String", "Boolean", "Float", "WS", "'{'", "'}'", "'leftInput'", "'='", "'rightInput'", "'input'", "'output'", "'operation'", "'inFiles'", "'inAtts'", "'inTypes'", "'hashAtts'", "'typeCode'", "'selection'", "'outAtts'", "'outFile'", "'groupByAtts'", "'aggregates'", "'outerInput'", "'innerInputs'", "'seedAtt'", "'function'", "'functionName'", "'vgInAtts'", "'vgOutAtts'", "'removeDuplicates'", "'isFinal'", "'sortedOn'", "'joinType'", "'rows'", "','", "'('", "')'", "'=='", "'!='", "'<'", "'<='", "'>'", "'>='", "'!'", "'null'", "'&&'", "'||'", "'+'", "'-'", "'*'", "'/'"
    };
    public static final int T__29=29;
    public static final int T__28=28;
    public static final int T__27=27;
    public static final int T__26=26;
    public static final int T__25=25;
    public static final int T__24=24;
    public static final int T__23=23;
    public static final int T__22=22;
    public static final int T__21=21;
    public static final int T__20=20;
    public static final int EOF=-1;
    public static final int Int=5;
    public static final int Identifier=4;
    public static final int T__55=55;
    public static final int T__56=56;
    public static final int T__19=19;
    public static final int T__51=51;
    public static final int T__16=16;
    public static final int T__52=52;
    public static final int T__15=15;
    public static final int T__53=53;
    public static final int T__18=18;
    public static final int T__54=54;
    public static final int T__17=17;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__14=14;
    public static final int T__13=13;
    public static final int T__10=10;
    public static final int String=6;
    public static final int T__50=50;
    public static final int T__42=42;
    public static final int T__43=43;
    public static final int T__40=40;
    public static final int T__41=41;
    public static final int T__46=46;
    public static final int T__47=47;
    public static final int T__44=44;
    public static final int T__45=45;
    public static final int T__48=48;
    public static final int T__49=49;
    public static final int Float=8;
    public static final int T__30=30;
    public static final int T__31=31;
    public static final int T__32=32;
    public static final int WS=9;
    public static final int T__33=33;
    public static final int T__34=34;
    public static final int T__35=35;
    public static final int T__36=36;
    public static final int T__37=37;
    public static final int T__38=38;
    public static final int T__39=39;
    public static final int Boolean=7;

    // delegates
    // delegators


        public DataFlowParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public DataFlowParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return DataFlowParser.tokenNames; }
    public String getGrammarFileName() { return "/home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g"; }



    	// nothing right now!



    // $ANTLR start "parse"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:44:1: parse returns [ArrayList <RelOp> value] : v1= varList (v2= varList )* ;
    public final ArrayList <RelOp> parse() throws RecognitionException {
        ArrayList <RelOp> value = null;

        Map <String, ParsedRHS> v1 = null;

        Map <String, ParsedRHS> v2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:45:2: (v1= varList (v2= varList )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:45:4: v1= varList (v2= varList )*
            {
            pushFollow(FOLLOW_varList_in_parse48);
            v1=varList();

            state._fsp--;

            value = new ArrayList <RelOp> ();
            								 ParsedRHS whichOne = v1.get ("operation");
            								 if (whichOne == null) {
            									throw new RuntimeException ("Parser: No operation defined for clause!");
            								 } else if (whichOne.getIdentifier ().equals ("join")) {
            									value.add (new JoinOp (v1));
            								 } else if (whichOne.getIdentifier ().equals ("aggregate")) {
                                                value.add (new AggregateOp (v1));
                                             } else if (whichOne.getIdentifier ().equals ("inference")) {
                                                value.add (new InferOp (v1));
                                             } else if (whichOne.getIdentifier ().equals ("vgwrapper")) {
                                                 value.add (new VGWrapperOp (v1));
                                             } else if (whichOne.getIdentifier ().equals ("select")) {
                                                 value.add (new SelectionOp (v1));
                                             } else if (whichOne.getIdentifier ().equals ("tempTable")) {
                                                 value.add (new TempTableOp (v1));
                                             } else if (whichOne.getIdentifier ().equals ("frameOutput")) {
                                                 value.add (new FrameOutputOp (v1));
            								 } else {
            									throw new RuntimeException ("Parser: Got an undefined relational op!");
            								 }
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:66:3: (v2= varList )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==10) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:66:5: v2= varList
            	    {
            	    pushFollow(FOLLOW_varList_in_parse62);
            	    v2=varList();

            	    state._fsp--;

            	    whichOne = v2.get ("operation");
            	    								 if (whichOne == null) {
            	    									throw new RuntimeException ("Parser: No operation defined for clause!");
            	    								 } else if (whichOne.getIdentifier ().equals ("join")) {
            	    									value.add (new JoinOp (v2));
            	    								 } else if (whichOne.getIdentifier ().equals ("aggregate")) {
            	                                        value.add (new AggregateOp (v2));
            	                                     } else if (whichOne.getIdentifier ().equals ("inference")) {
            	                                        value.add (new InferOp (v2));
            	                                     } else if (whichOne.getIdentifier ().equals ("vgwrapper")) {
            	                                         value.add (new VGWrapperOp (v2));
            	    								 } else if (whichOne.getIdentifier ().equals ("select")) {
            	                                         value.add (new SelectionOp (v2));
            	                                     } else if (whichOne.getIdentifier ().equals ("tempTable")) {
            	                                         value.add (new TempTableOp (v2));
            	                                     } else if (whichOne.getIdentifier ().equals ("frameOutput")) {
            	                                         value.add (new FrameOutputOp (v2));
            	                                     } else {
            	    									throw new RuntimeException ("Parser: Got an undefined relational op!");
            	    								 }

            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "parse"


    // $ANTLR start "varList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:90:1: varList returns [Map <String, ParsedRHS> value] : '{' v1= varDef (v2= varDef )* '}' ;
    public final Map <String, ParsedRHS> varList() throws RecognitionException {
        Map <String, ParsedRHS> value = null;

        VarDefPair v1 = null;

        VarDefPair v2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:91:2: ( '{' v1= varDef (v2= varDef )* '}' )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:91:4: '{' v1= varDef (v2= varDef )* '}'
            {
            match(input,10,FOLLOW_10_in_varList93); 
            pushFollow(FOLLOW_varDef_in_varList97);
            v1=varDef();

            state._fsp--;

            value = new HashMap <String, ParsedRHS> ();
            								 value.put (v1.getKey (), v1.getValue ());
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:93:3: (v2= varDef )*
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( (LA2_0==12||(LA2_0>=14 && LA2_0<=39)) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:93:5: v2= varDef
            	    {
            	    pushFollow(FOLLOW_varDef_in_varList111);
            	    v2=varDef();

            	    state._fsp--;

            	    value.put (v2.getKey (), v2.getValue ());

            	    }
            	    break;

            	default :
            	    break loop2;
                }
            } while (true);

            match(input,11,FOLLOW_11_in_varList124); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "varList"


    // $ANTLR start "varDef"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:97:1: varDef returns [VarDefPair value] : ( 'leftInput' '=' rhs1= varList | 'rightInput' '=' rhs1= varList | 'input' '=' rhs1= varList | 'output' '=' rhs1= varList | 'operation' '=' '{' rhs2= Identifier '}' | 'inFiles' '=' '{' rhs3= stringList '}' | 'inAtts' '=' '{' rhs4= identifierList '}' | 'inTypes' '=' '{' rhs4= identifierList '}' | 'hashAtts' '=' '{' rhs4= identifierList '}' | 'typeCode' '=' '{' rhs5= Int '}' | 'selection' '=' '{' rhs6= exp '}' | 'outAtts' '=' '{' rhs7= assignmentList '}' | 'outFile' '=' '{' rhs8= String '}' | 'groupByAtts' '=' '{' rhs4= identifierList '}' | 'aggregates' '=' '{' rhs10= aggregateList '}' | 'outerInput' '=' rhs1= varList | 'innerInputs' '=' rhs11= innerInputList | 'seedAtt' '=' '{' rhs2= Identifier '}' | 'function' '=' rhs1= varList | 'functionName' '=' '{' rhs8= String '}' | 'vgInAtts' '=' '{' rhs4= identifierList '}' | 'vgOutAtts' '=' '{' rhs4= identifierList '}' | 'removeDuplicates' '=' '{' rhs12= Boolean '}' | 'isFinal' '=' '{' rhs12= Boolean '}' | 'sortedOn' '=' '{' rhs2= Identifier '}' | 'joinType' '=' '{' rhs2= Identifier '}' | 'rows' '=' '{' rhs13= rowList '}' );
    public final VarDefPair varDef() throws RecognitionException {
        VarDefPair value = null;

        Token rhs2=null;
        Token rhs5=null;
        Token rhs8=null;
        Token rhs12=null;
        Map <String, ParsedRHS> rhs1 = null;

        ArrayList <String> rhs3 = null;

        ArrayList <String> rhs4 = null;

        Expression rhs6 = null;

        ArrayList <Assignment> rhs7 = null;

        ArrayList<AggregateExp> rhs10 = null;

        Map<String,ParsedRHS> rhs11 = null;

        ArrayList<ArrayList<Expression>> rhs13 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:98:2: ( 'leftInput' '=' rhs1= varList | 'rightInput' '=' rhs1= varList | 'input' '=' rhs1= varList | 'output' '=' rhs1= varList | 'operation' '=' '{' rhs2= Identifier '}' | 'inFiles' '=' '{' rhs3= stringList '}' | 'inAtts' '=' '{' rhs4= identifierList '}' | 'inTypes' '=' '{' rhs4= identifierList '}' | 'hashAtts' '=' '{' rhs4= identifierList '}' | 'typeCode' '=' '{' rhs5= Int '}' | 'selection' '=' '{' rhs6= exp '}' | 'outAtts' '=' '{' rhs7= assignmentList '}' | 'outFile' '=' '{' rhs8= String '}' | 'groupByAtts' '=' '{' rhs4= identifierList '}' | 'aggregates' '=' '{' rhs10= aggregateList '}' | 'outerInput' '=' rhs1= varList | 'innerInputs' '=' rhs11= innerInputList | 'seedAtt' '=' '{' rhs2= Identifier '}' | 'function' '=' rhs1= varList | 'functionName' '=' '{' rhs8= String '}' | 'vgInAtts' '=' '{' rhs4= identifierList '}' | 'vgOutAtts' '=' '{' rhs4= identifierList '}' | 'removeDuplicates' '=' '{' rhs12= Boolean '}' | 'isFinal' '=' '{' rhs12= Boolean '}' | 'sortedOn' '=' '{' rhs2= Identifier '}' | 'joinType' '=' '{' rhs2= Identifier '}' | 'rows' '=' '{' rhs13= rowList '}' )
            int alt3=27;
            switch ( input.LA(1) ) {
            case 12:
                {
                alt3=1;
                }
                break;
            case 14:
                {
                alt3=2;
                }
                break;
            case 15:
                {
                alt3=3;
                }
                break;
            case 16:
                {
                alt3=4;
                }
                break;
            case 17:
                {
                alt3=5;
                }
                break;
            case 18:
                {
                alt3=6;
                }
                break;
            case 19:
                {
                alt3=7;
                }
                break;
            case 20:
                {
                alt3=8;
                }
                break;
            case 21:
                {
                alt3=9;
                }
                break;
            case 22:
                {
                alt3=10;
                }
                break;
            case 23:
                {
                alt3=11;
                }
                break;
            case 24:
                {
                alt3=12;
                }
                break;
            case 25:
                {
                alt3=13;
                }
                break;
            case 26:
                {
                alt3=14;
                }
                break;
            case 27:
                {
                alt3=15;
                }
                break;
            case 28:
                {
                alt3=16;
                }
                break;
            case 29:
                {
                alt3=17;
                }
                break;
            case 30:
                {
                alt3=18;
                }
                break;
            case 31:
                {
                alt3=19;
                }
                break;
            case 32:
                {
                alt3=20;
                }
                break;
            case 33:
                {
                alt3=21;
                }
                break;
            case 34:
                {
                alt3=22;
                }
                break;
            case 35:
                {
                alt3=23;
                }
                break;
            case 36:
                {
                alt3=24;
                }
                break;
            case 37:
                {
                alt3=25;
                }
                break;
            case 38:
                {
                alt3=26;
                }
                break;
            case 39:
                {
                alt3=27;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 3, 0, input);

                throw nvae;
            }

            switch (alt3) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:98:4: 'leftInput' '=' rhs1= varList
                    {
                    match(input,12,FOLLOW_12_in_varDef139); 
                    match(input,13,FOLLOW_13_in_varDef142); 
                    pushFollow(FOLLOW_varList_in_varDef146);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("leftInput", new VarListRHS (rhs1));

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:99:4: 'rightInput' '=' rhs1= varList
                    {
                    match(input,14,FOLLOW_14_in_varDef155); 
                    match(input,13,FOLLOW_13_in_varDef158); 
                    pushFollow(FOLLOW_varList_in_varDef162);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("rightInput", new VarListRHS (rhs1));

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:100:4: 'input' '=' rhs1= varList
                    {
                    match(input,15,FOLLOW_15_in_varDef170); 
                    match(input,13,FOLLOW_13_in_varDef173); 
                    pushFollow(FOLLOW_varList_in_varDef177);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("input", new VarListRHS (rhs1));

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:101:4: 'output' '=' rhs1= varList
                    {
                    match(input,16,FOLLOW_16_in_varDef186); 
                    match(input,13,FOLLOW_13_in_varDef189); 
                    pushFollow(FOLLOW_varList_in_varDef193);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("output", new VarListRHS (rhs1));

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:102:4: 'operation' '=' '{' rhs2= Identifier '}'
                    {
                    match(input,17,FOLLOW_17_in_varDef202); 
                    match(input,13,FOLLOW_13_in_varDef208); 
                    match(input,10,FOLLOW_10_in_varDef210); 
                    rhs2=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef214); 
                    match(input,11,FOLLOW_11_in_varDef216); 
                    value = new VarDefPair ("operation", new IdentifierRHS ((rhs2!=null?rhs2.getText():null)));

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:103:4: 'inFiles' '=' '{' rhs3= stringList '}'
                    {
                    match(input,18,FOLLOW_18_in_varDef223); 
                    match(input,13,FOLLOW_13_in_varDef226); 
                    match(input,10,FOLLOW_10_in_varDef228); 
                    pushFollow(FOLLOW_stringList_in_varDef232);
                    rhs3=stringList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef234); 
                    value = new VarDefPair ("inFiles", new StringListRHS (rhs3));

                    }
                    break;
                case 7 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:104:4: 'inAtts' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,19,FOLLOW_19_in_varDef241); 
                    match(input,13,FOLLOW_13_in_varDef244); 
                    match(input,10,FOLLOW_10_in_varDef246); 
                    pushFollow(FOLLOW_identifierList_in_varDef250);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef252); 
                    value = new VarDefPair ("inAtts", new IdentifierListRHS (rhs4));

                    }
                    break;
                case 8 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:105:4: 'inTypes' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,20,FOLLOW_20_in_varDef259); 
                    match(input,13,FOLLOW_13_in_varDef262); 
                    match(input,10,FOLLOW_10_in_varDef264); 
                    pushFollow(FOLLOW_identifierList_in_varDef268);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef270); 
                    value = new VarDefPair ("inTypes", new IdentifierListRHS (rhs4));

                    }
                    break;
                case 9 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:106:4: 'hashAtts' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,21,FOLLOW_21_in_varDef277); 
                    match(input,13,FOLLOW_13_in_varDef280); 
                    match(input,10,FOLLOW_10_in_varDef282); 
                    pushFollow(FOLLOW_identifierList_in_varDef286);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef288); 
                    value = new VarDefPair ("hashAtts", new IdentifierListRHS (rhs4));

                    }
                    break;
                case 10 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:107:4: 'typeCode' '=' '{' rhs5= Int '}'
                    {
                    match(input,22,FOLLOW_22_in_varDef295); 
                    match(input,13,FOLLOW_13_in_varDef298); 
                    match(input,10,FOLLOW_10_in_varDef300); 
                    rhs5=(Token)match(input,Int,FOLLOW_Int_in_varDef304); 
                    match(input,11,FOLLOW_11_in_varDef306); 
                    value = new VarDefPair ("typeCode", new IntegerRHS (Integer.parseInt ((rhs5!=null?rhs5.getText():null))));

                    }
                    break;
                case 11 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:108:4: 'selection' '=' '{' rhs6= exp '}'
                    {
                    match(input,23,FOLLOW_23_in_varDef314); 
                    match(input,13,FOLLOW_13_in_varDef316); 
                    match(input,10,FOLLOW_10_in_varDef318); 
                    pushFollow(FOLLOW_exp_in_varDef322);
                    rhs6=exp();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef324); 
                    value = new VarDefPair ("selection", new ExpressionRHS (rhs6));

                    }
                    break;
                case 12 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:109:4: 'outAtts' '=' '{' rhs7= assignmentList '}'
                    {
                    match(input,24,FOLLOW_24_in_varDef332); 
                    match(input,13,FOLLOW_13_in_varDef335); 
                    match(input,10,FOLLOW_10_in_varDef337); 
                    pushFollow(FOLLOW_assignmentList_in_varDef341);
                    rhs7=assignmentList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef343); 
                    value = new VarDefPair ("outAtts", new AssignmentListRHS (rhs7));

                    }
                    break;
                case 13 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:110:4: 'outFile' '=' '{' rhs8= String '}'
                    {
                    match(input,25,FOLLOW_25_in_varDef350); 
                    match(input,13,FOLLOW_13_in_varDef353); 
                    match(input,10,FOLLOW_10_in_varDef355); 
                    rhs8=(Token)match(input,String,FOLLOW_String_in_varDef359); 
                    match(input,11,FOLLOW_11_in_varDef361); 
                    if ((rhs8!=null?rhs8.getText():null).length () == 2)
                    								  	value = new VarDefPair ("outFile", new StringLiteralRHS (""));	
                    								 else
                    									value = new VarDefPair ("outFile", 
                    									new StringLiteralRHS ((rhs8!=null?rhs8.getText():null).substring (1, (rhs8!=null?rhs8.getText():null).length () - 1))); 

                    }
                    break;
                case 14 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:115:9: 'groupByAtts' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,26,FOLLOW_26_in_varDef374); 
                    match(input,13,FOLLOW_13_in_varDef378); 
                    match(input,10,FOLLOW_10_in_varDef380); 
                    pushFollow(FOLLOW_identifierList_in_varDef384);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef386); 
                    value = new VarDefPair("groupByAtts", new IdentifierListRHS(rhs4)); 

                    }
                    break;
                case 15 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:116:9: 'aggregates' '=' '{' rhs10= aggregateList '}'
                    {
                    match(input,27,FOLLOW_27_in_varDef399); 
                    match(input,13,FOLLOW_13_in_varDef404); 
                    match(input,10,FOLLOW_10_in_varDef406); 
                    pushFollow(FOLLOW_aggregateList_in_varDef410);
                    rhs10=aggregateList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef412); 
                    value = new VarDefPair("aggregates", new AggregateListRHS(rhs10)); 

                    }
                    break;
                case 16 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:117:9: 'outerInput' '=' rhs1= varList
                    {
                    match(input,28,FOLLOW_28_in_varDef425); 
                    match(input,13,FOLLOW_13_in_varDef430); 
                    pushFollow(FOLLOW_varList_in_varDef434);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("outerInput", new VarListRHS (rhs1));

                    }
                    break;
                case 17 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:118:9: 'innerInputs' '=' rhs11= innerInputList
                    {
                    match(input,29,FOLLOW_29_in_varDef462); 
                    match(input,13,FOLLOW_13_in_varDef466); 
                    pushFollow(FOLLOW_innerInputList_in_varDef470);
                    rhs11=innerInputList();

                    state._fsp--;

                    value = new VarDefPair ("innerInputs", new VarListRHS (rhs11));

                    }
                    break;
                case 18 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:119:9: 'seedAtt' '=' '{' rhs2= Identifier '}'
                    {
                    match(input,30,FOLLOW_30_in_varDef490); 
                    match(input,13,FOLLOW_13_in_varDef498); 
                    match(input,10,FOLLOW_10_in_varDef500); 
                    rhs2=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef504); 
                    match(input,11,FOLLOW_11_in_varDef506); 
                    value = new VarDefPair ("seedAtt", new IdentifierRHS((rhs2!=null?rhs2.getText():null)));

                    }
                    break;
                case 19 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:120:9: 'function' '=' rhs1= varList
                    {
                    match(input,31,FOLLOW_31_in_varDef524); 
                    match(input,13,FOLLOW_13_in_varDef531); 
                    pushFollow(FOLLOW_varList_in_varDef535);
                    rhs1=varList();

                    state._fsp--;

                    value = new VarDefPair ("function", new VarListRHS(rhs1));

                    }
                    break;
                case 20 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:121:9: 'functionName' '=' '{' rhs8= String '}'
                    {
                    match(input,32,FOLLOW_32_in_varDef563); 
                    match(input,13,FOLLOW_13_in_varDef566); 
                    match(input,10,FOLLOW_10_in_varDef568); 
                    rhs8=(Token)match(input,String,FOLLOW_String_in_varDef572); 
                    match(input,11,FOLLOW_11_in_varDef574); 
                    if ((rhs8!=null?rhs8.getText():null).length () == 2)
                    								  	value = new VarDefPair ("functionName", new StringLiteralRHS (""));	
                    								 else
                    									value = new VarDefPair ("functionName", 
                    									new StringLiteralRHS ((rhs8!=null?rhs8.getText():null).substring (1, (rhs8!=null?rhs8.getText():null).length () - 1)));

                    }
                    break;
                case 21 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:127:9: 'vgInAtts' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,33,FOLLOW_33_in_varDef596); 
                    match(input,13,FOLLOW_13_in_varDef606); 
                    match(input,10,FOLLOW_10_in_varDef608); 
                    pushFollow(FOLLOW_identifierList_in_varDef612);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef614); 
                    value = new VarDefPair ("vgInAtts", new IdentifierListRHS(rhs4));

                    }
                    break;
                case 22 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:129:9: 'vgOutAtts' '=' '{' rhs4= identifierList '}'
                    {
                    match(input,34,FOLLOW_34_in_varDef632); 
                    match(input,13,FOLLOW_13_in_varDef641); 
                    match(input,10,FOLLOW_10_in_varDef643); 
                    pushFollow(FOLLOW_identifierList_in_varDef647);
                    rhs4=identifierList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef649); 
                    value = new VarDefPair ("vgOutAtts", new IdentifierListRHS(rhs4));

                    }
                    break;
                case 23 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:131:9: 'removeDuplicates' '=' '{' rhs12= Boolean '}'
                    {
                    match(input,35,FOLLOW_35_in_varDef667); 
                    match(input,13,FOLLOW_13_in_varDef669); 
                    match(input,10,FOLLOW_10_in_varDef671); 
                    rhs12=(Token)match(input,Boolean,FOLLOW_Boolean_in_varDef675); 
                    match(input,11,FOLLOW_11_in_varDef677); 
                    value = new VarDefPair("removeDuplicates", new StringLiteralRHS((rhs12!=null?rhs12.getText():null))); 

                    }
                    break;
                case 24 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:133:9: 'isFinal' '=' '{' rhs12= Boolean '}'
                    {
                    match(input,36,FOLLOW_36_in_varDef690); 
                    match(input,13,FOLLOW_13_in_varDef701); 
                    match(input,10,FOLLOW_10_in_varDef703); 
                    rhs12=(Token)match(input,Boolean,FOLLOW_Boolean_in_varDef707); 
                    match(input,11,FOLLOW_11_in_varDef709); 
                    value = new VarDefPair("isFinal", new StringLiteralRHS((rhs12!=null?rhs12.getText():null))); 

                    }
                    break;
                case 25 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:135:9: 'sortedOn' '=' '{' rhs2= Identifier '}'
                    {
                    match(input,37,FOLLOW_37_in_varDef722); 
                    match(input,13,FOLLOW_13_in_varDef732); 
                    match(input,10,FOLLOW_10_in_varDef734); 
                    rhs2=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef738); 
                    match(input,11,FOLLOW_11_in_varDef740); 
                    value = new VarDefPair ("sortedOn", new IdentifierRHS((rhs2!=null?rhs2.getText():null)));

                    }
                    break;
                case 26 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:137:9: 'joinType' '=' '{' rhs2= Identifier '}'
                    {
                    match(input,38,FOLLOW_38_in_varDef759); 
                    match(input,13,FOLLOW_13_in_varDef769); 
                    match(input,10,FOLLOW_10_in_varDef771); 
                    rhs2=(Token)match(input,Identifier,FOLLOW_Identifier_in_varDef775); 
                    match(input,11,FOLLOW_11_in_varDef777); 
                    value = new VarDefPair("joinType", new IdentifierRHS((rhs2!=null?rhs2.getText():null)));

                    }
                    break;
                case 27 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:138:9: 'rows' '=' '{' rhs13= rowList '}'
                    {
                    match(input,39,FOLLOW_39_in_varDef789); 
                    match(input,13,FOLLOW_13_in_varDef803); 
                    match(input,10,FOLLOW_10_in_varDef805); 
                    pushFollow(FOLLOW_rowList_in_varDef809);
                    rhs13=rowList();

                    state._fsp--;

                    match(input,11,FOLLOW_11_in_varDef811); 
                    value = new VarDefPair("rows", new RowListRHS(rhs13));

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "varDef"


    // $ANTLR start "rowList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:142:1: rowList returns [ArrayList<ArrayList<Expression>> value] : '{' l1= colList '}' ( ',' '{' l2= colList '}' )* ;
    public final ArrayList<ArrayList<Expression>> rowList() throws RecognitionException {
        ArrayList<ArrayList<Expression>> value = null;

        ArrayList<Expression> l1 = null;

        ArrayList<Expression> l2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:143:5: ( '{' l1= colList '}' ( ',' '{' l2= colList '}' )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:143:7: '{' l1= colList '}' ( ',' '{' l2= colList '}' )*
            {
            match(input,10,FOLLOW_10_in_rowList840); 
            pushFollow(FOLLOW_colList_in_rowList844);
            l1=colList();

            state._fsp--;

            match(input,11,FOLLOW_11_in_rowList846); 

                        value = new ArrayList<ArrayList<Expression>>();
                        value.add(l1);
                    
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:148:8: ( ',' '{' l2= colList '}' )*
            loop4:
            do {
                int alt4=2;
                int LA4_0 = input.LA(1);

                if ( (LA4_0==40) ) {
                    alt4=1;
                }


                switch (alt4) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:148:10: ',' '{' l2= colList '}'
            	    {
            	    match(input,40,FOLLOW_40_in_rowList867); 
            	    match(input,10,FOLLOW_10_in_rowList869); 
            	    pushFollow(FOLLOW_colList_in_rowList873);
            	    l2=colList();

            	    state._fsp--;

            	    match(input,11,FOLLOW_11_in_rowList875); 

            	                value.add(l2);
            	            

            	    }
            	    break;

            	default :
            	    break loop4;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "rowList"


    // $ANTLR start "colList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:155:1: colList returns [ArrayList<Expression> value] : e1= atomExp ( ',' e2= atomExp )* ;
    public final ArrayList<Expression> colList() throws RecognitionException {
        ArrayList<Expression> value = null;

        Expression e1 = null;

        Expression e2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:156:5: (e1= atomExp ( ',' e2= atomExp )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:156:8: e1= atomExp ( ',' e2= atomExp )*
            {
            pushFollow(FOLLOW_atomExp_in_colList919);
            e1=atomExp();

            state._fsp--;


                        value = new ArrayList<Expression>();
                        value.add(e1);
                    
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:161:8: ( ',' e2= atomExp )*
            loop5:
            do {
                int alt5=2;
                int LA5_0 = input.LA(1);

                if ( (LA5_0==40) ) {
                    alt5=1;
                }


                switch (alt5) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:161:10: ',' e2= atomExp
            	    {
            	    match(input,40,FOLLOW_40_in_colList940); 
            	    pushFollow(FOLLOW_atomExp_in_colList944);
            	    e2=atomExp();

            	    state._fsp--;


            	                    value.add(e2);
            	                

            	    }
            	    break;

            	default :
            	    break loop5;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "colList"


    // $ANTLR start "aggregateList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:169:1: aggregateList returns [ArrayList<AggregateExp> value] : i1= Identifier '=' t1= Identifier '(' (e1= addExp )? ')' ( ',' i2= Identifier '=' t2= Identifier '(' (e2= addExp )? ')' )* ;
    public final ArrayList<AggregateExp> aggregateList() throws RecognitionException {
        ArrayList<AggregateExp> value = null;

        Token i1=null;
        Token t1=null;
        Token i2=null;
        Token t2=null;
        Expression e1 = null;

        Expression e2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:170:5: (i1= Identifier '=' t1= Identifier '(' (e1= addExp )? ')' ( ',' i2= Identifier '=' t2= Identifier '(' (e2= addExp )? ')' )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:170:7: i1= Identifier '=' t1= Identifier '(' (e1= addExp )? ')' ( ',' i2= Identifier '=' t2= Identifier '(' (e2= addExp )? ')' )*
            {
            i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_aggregateList1008); 
            match(input,13,FOLLOW_13_in_aggregateList1010); 
            t1=(Token)match(input,Identifier,FOLLOW_Identifier_in_aggregateList1014); 
            match(input,41,FOLLOW_41_in_aggregateList1016); 
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:170:45: (e1= addExp )?
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( ((LA6_0>=Identifier && LA6_0<=String)||LA6_0==Float||LA6_0==41||LA6_0==54) ) {
                alt6=1;
            }
            switch (alt6) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:170:45: e1= addExp
                    {
                    pushFollow(FOLLOW_addExp_in_aggregateList1020);
                    e1=addExp();

                    state._fsp--;


                    }
                    break;

            }

            match(input,42,FOLLOW_42_in_aggregateList1023); 

                        value = new ArrayList<AggregateExp>();
                        value.add(new AggregateExp((i1!=null?i1.getText():null), (t1!=null?t1.getText():null), e1));
                    
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:176:7: ( ',' i2= Identifier '=' t2= Identifier '(' (e2= addExp )? ')' )*
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==40) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:176:9: ',' i2= Identifier '=' t2= Identifier '(' (e2= addExp )? ')'
            	    {
            	    match(input,40,FOLLOW_40_in_aggregateList1048); 
            	    i2=(Token)match(input,Identifier,FOLLOW_Identifier_in_aggregateList1052); 
            	    match(input,13,FOLLOW_13_in_aggregateList1054); 
            	    t2=(Token)match(input,Identifier,FOLLOW_Identifier_in_aggregateList1058); 
            	    match(input,41,FOLLOW_41_in_aggregateList1060); 
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:176:51: (e2= addExp )?
            	    int alt7=2;
            	    int LA7_0 = input.LA(1);

            	    if ( ((LA7_0>=Identifier && LA7_0<=String)||LA7_0==Float||LA7_0==41||LA7_0==54) ) {
            	        alt7=1;
            	    }
            	    switch (alt7) {
            	        case 1 :
            	            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:176:51: e2= addExp
            	            {
            	            pushFollow(FOLLOW_addExp_in_aggregateList1064);
            	            e2=addExp();

            	            state._fsp--;


            	            }
            	            break;

            	    }

            	    match(input,42,FOLLOW_42_in_aggregateList1067); 

            	                value.add(new AggregateExp((i2!=null?i2.getText():null), (t2!=null?t2.getText():null), e2));
            	            

            	    }
            	    break;

            	default :
            	    break loop8;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "aggregateList"


    // $ANTLR start "innerInputList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:184:1: innerInputList returns [Map<String,ParsedRHS> value] : '{' i1= Identifier '=' v1= varList (i2= Identifier '=' v2= varList )* '}' ;
    public final Map<String,ParsedRHS> innerInputList() throws RecognitionException {
        Map<String,ParsedRHS> value = null;

        Token i1=null;
        Token i2=null;
        Map <String, ParsedRHS> v1 = null;

        Map <String, ParsedRHS> v2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:185:5: ( '{' i1= Identifier '=' v1= varList (i2= Identifier '=' v2= varList )* '}' )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:185:7: '{' i1= Identifier '=' v1= varList (i2= Identifier '=' v2= varList )* '}'
            {
            match(input,10,FOLLOW_10_in_innerInputList1117); 
            i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_innerInputList1121); 
            match(input,13,FOLLOW_13_in_innerInputList1123); 
            pushFollow(FOLLOW_varList_in_innerInputList1128);
            v1=varList();

            state._fsp--;

            value = new HashMap<String, ParsedRHS> ();
                                                               value.put((i1!=null?i1.getText():null), new VarListRHS(v1));
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:188:7: (i2= Identifier '=' v2= varList )*
            loop9:
            do {
                int alt9=2;
                int LA9_0 = input.LA(1);

                if ( (LA9_0==Identifier) ) {
                    alt9=1;
                }


                switch (alt9) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:188:8: i2= Identifier '=' v2= varList
            	    {
            	    i2=(Token)match(input,Identifier,FOLLOW_Identifier_in_innerInputList1152); 
            	    match(input,13,FOLLOW_13_in_innerInputList1154); 
            	    pushFollow(FOLLOW_varList_in_innerInputList1158);
            	    v2=varList();

            	    state._fsp--;

            	    value.put((i2!=null?i2.getText():null), new VarListRHS(v2));

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match(input,11,FOLLOW_11_in_innerInputList1185); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "innerInputList"


    // $ANTLR start "stringList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:192:1: stringList returns [ArrayList <String> value] : s1= String ( ',' s2= String )* ;
    public final ArrayList <String> stringList() throws RecognitionException {
        ArrayList <String> value = null;

        Token s1=null;
        Token s2=null;

        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:193:2: (s1= String ( ',' s2= String )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:193:4: s1= String ( ',' s2= String )*
            {
            s1=(Token)match(input,String,FOLLOW_String_in_stringList1205); 
            value = new ArrayList <String> ();
            								 if ((s1!=null?s1.getText():null).length () == 2)
            								  	value.add ("");
            								 else
            								 	value.add ((s1!=null?s1.getText():null).substring (1, (s1!=null?s1.getText():null).length () - 1));
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:198:3: ( ',' s2= String )*
            loop10:
            do {
                int alt10=2;
                int LA10_0 = input.LA(1);

                if ( (LA10_0==40) ) {
                    alt10=1;
                }


                switch (alt10) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:198:5: ',' s2= String
            	    {
            	    match(input,40,FOLLOW_40_in_stringList1217); 
            	    s2=(Token)match(input,String,FOLLOW_String_in_stringList1221); 
            	    if ((s2!=null?s2.getText():null).length () == 2)
            	    									value.add ("");
            	    								 else
            	    								 	value.add ((s2!=null?s2.getText():null).substring (1, (s2!=null?s2.getText():null).length () - 1));

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
        }
        return value;
    }
    // $ANTLR end "stringList"


    // $ANTLR start "identifierList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:205:1: identifierList returns [ArrayList <String> value] : s1= Identifier ( ',' s2= Identifier )* ;
    public final ArrayList <String> identifierList() throws RecognitionException {
        ArrayList <String> value = null;

        Token s1=null;
        Token s2=null;

        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:206:2: (s1= Identifier ( ',' s2= Identifier )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:206:4: s1= Identifier ( ',' s2= Identifier )*
            {
            s1=(Token)match(input,Identifier,FOLLOW_Identifier_in_identifierList1249); 
            value = new ArrayList <String> ();
            								 value.add ((s1!=null?s1.getText():null));
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:208:3: ( ',' s2= Identifier )*
            loop11:
            do {
                int alt11=2;
                int LA11_0 = input.LA(1);

                if ( (LA11_0==40) ) {
                    alt11=1;
                }


                switch (alt11) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:208:5: ',' s2= Identifier
            	    {
            	    match(input,40,FOLLOW_40_in_identifierList1261); 
            	    s2=(Token)match(input,Identifier,FOLLOW_Identifier_in_identifierList1265); 
            	    value.add ((s2!=null?s2.getText():null));

            	    }
            	    break;

            	default :
            	    break loop11;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "identifierList"


    // $ANTLR start "assignmentList"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:212:1: assignmentList returns [ArrayList <Assignment> value] : i1= Identifier '=' a1= addExp ( ',' i2= Identifier '=' a2= addExp )* ;
    public final ArrayList <Assignment> assignmentList() throws RecognitionException {
        ArrayList <Assignment> value = null;

        Token i1=null;
        Token i2=null;
        Expression a1 = null;

        Expression a2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:213:2: (i1= Identifier '=' a1= addExp ( ',' i2= Identifier '=' a2= addExp )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:213:4: i1= Identifier '=' a1= addExp ( ',' i2= Identifier '=' a2= addExp )*
            {
            i1=(Token)match(input,Identifier,FOLLOW_Identifier_in_assignmentList1292); 
            match(input,13,FOLLOW_13_in_assignmentList1294); 
            pushFollow(FOLLOW_addExp_in_assignmentList1298);
            a1=addExp();

            state._fsp--;

            value = new ArrayList <Assignment> ();
            								 value.add (new Assignment ((i1!=null?i1.getText():null), a1));
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:215:3: ( ',' i2= Identifier '=' a2= addExp )*
            loop12:
            do {
                int alt12=2;
                int LA12_0 = input.LA(1);

                if ( (LA12_0==40) ) {
                    alt12=1;
                }


                switch (alt12) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:215:5: ',' i2= Identifier '=' a2= addExp
            	    {
            	    match(input,40,FOLLOW_40_in_assignmentList1308); 
            	    i2=(Token)match(input,Identifier,FOLLOW_Identifier_in_assignmentList1312); 
            	    match(input,13,FOLLOW_13_in_assignmentList1314); 
            	    pushFollow(FOLLOW_addExp_in_assignmentList1318);
            	    a2=addExp();

            	    state._fsp--;

            	    value.add (new Assignment ((i2!=null?i2.getText():null), a2));

            	    }
            	    break;

            	default :
            	    break loop12;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "assignmentList"


    // $ANTLR start "comparison"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:219:1: comparison returns [Expression value] : (e1= addExp ( '==' e2= addExp | '!=' e2= addExp | '<' e2= addExp | '<=' e2= addExp | '>' e2= addExp | '>=' e2= addExp )? | '!' c= comparison | 'null' c= comparison );
    public final Expression comparison() throws RecognitionException {
        Expression value = null;

        Expression e1 = null;

        Expression e2 = null;

        Expression c = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:220:2: (e1= addExp ( '==' e2= addExp | '!=' e2= addExp | '<' e2= addExp | '<=' e2= addExp | '>' e2= addExp | '>=' e2= addExp )? | '!' c= comparison | 'null' c= comparison )
            int alt14=3;
            switch ( input.LA(1) ) {
            case Identifier:
            case Int:
            case String:
            case Float:
            case 41:
            case 54:
                {
                alt14=1;
                }
                break;
            case 49:
                {
                alt14=2;
                }
                break;
            case 50:
                {
                alt14=3;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 14, 0, input);

                throw nvae;
            }

            switch (alt14) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:220:4: e1= addExp ( '==' e2= addExp | '!=' e2= addExp | '<' e2= addExp | '<=' e2= addExp | '>' e2= addExp | '>=' e2= addExp )?
                    {
                    pushFollow(FOLLOW_addExp_in_comparison1343);
                    e1=addExp();

                    state._fsp--;

                    value = e1;
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:221:3: ( '==' e2= addExp | '!=' e2= addExp | '<' e2= addExp | '<=' e2= addExp | '>' e2= addExp | '>=' e2= addExp )?
                    int alt13=7;
                    switch ( input.LA(1) ) {
                        case 43:
                            {
                            alt13=1;
                            }
                            break;
                        case 44:
                            {
                            alt13=2;
                            }
                            break;
                        case 45:
                            {
                            alt13=3;
                            }
                            break;
                        case 46:
                            {
                            alt13=4;
                            }
                            break;
                        case 47:
                            {
                            alt13=5;
                            }
                            break;
                        case 48:
                            {
                            alt13=6;
                            }
                            break;
                    }

                    switch (alt13) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:221:5: '==' e2= addExp
                            {
                            match(input,43,FOLLOW_43_in_comparison1356); 
                            pushFollow(FOLLOW_addExp_in_comparison1360);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("equals");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;
                        case 2 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:224:5: '!=' e2= addExp
                            {
                            match(input,44,FOLLOW_44_in_comparison1372); 
                            pushFollow(FOLLOW_addExp_in_comparison1376);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("not equal");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;
                        case 3 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:227:5: '<' e2= addExp
                            {
                            match(input,45,FOLLOW_45_in_comparison1388); 
                            pushFollow(FOLLOW_addExp_in_comparison1392);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("less than");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;
                        case 4 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:230:5: '<=' e2= addExp
                            {
                            match(input,46,FOLLOW_46_in_comparison1404); 
                            pushFollow(FOLLOW_addExp_in_comparison1408);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("less than or equal");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;
                        case 5 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:233:5: '>' e2= addExp
                            {
                            match(input,47,FOLLOW_47_in_comparison1420); 
                            pushFollow(FOLLOW_addExp_in_comparison1424);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("greater than");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;
                        case 6 :
                            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:236:5: '>=' e2= addExp
                            {
                            match(input,48,FOLLOW_48_in_comparison1436); 
                            pushFollow(FOLLOW_addExp_in_comparison1440);
                            e2=addExp();

                            state._fsp--;

                            Expression temp = value;
                            				 				 value = new Expression ("greater than or equal");
                            				 				 value.setSubexpression (temp, e2);

                            }
                            break;

                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:240:5: '!' c= comparison
                    {
                    match(input,49,FOLLOW_49_in_comparison1457); 
                    pushFollow(FOLLOW_comparison_in_comparison1461);
                    c=comparison();

                    state._fsp--;

                    value = new Expression ("not");
                    				    				     value.setSubexpression (c);

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:242:9: 'null' c= comparison
                    {
                    match(input,50,FOLLOW_50_in_comparison1477); 
                    pushFollow(FOLLOW_comparison_in_comparison1481);
                    c=comparison();

                    state._fsp--;

                    value = new Expression("is null");
                                                             value.setSubexpression (c);

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "comparison"


    // $ANTLR start "exp"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:247:1: exp returns [Expression value] : m1= comparison ( '&&' m2= comparison | '||' m2= comparison )* ;
    public final Expression exp() throws RecognitionException {
        Expression value = null;

        Expression m1 = null;

        Expression m2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:248:2: (m1= comparison ( '&&' m2= comparison | '||' m2= comparison )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:248:4: m1= comparison ( '&&' m2= comparison | '||' m2= comparison )*
            {
            pushFollow(FOLLOW_comparison_in_exp1513);
            m1=comparison();

            state._fsp--;

            value = m1;
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:249:3: ( '&&' m2= comparison | '||' m2= comparison )*
            loop15:
            do {
                int alt15=3;
                int LA15_0 = input.LA(1);

                if ( (LA15_0==51) ) {
                    alt15=1;
                }
                else if ( (LA15_0==52) ) {
                    alt15=2;
                }


                switch (alt15) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:249:5: '&&' m2= comparison
            	    {
            	    match(input,51,FOLLOW_51_in_exp1532); 
            	    pushFollow(FOLLOW_comparison_in_exp1536);
            	    m2=comparison();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("and");
            	    				     				 value.setSubexpression (temp, m2);

            	    }
            	    break;
            	case 2 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:252:5: '||' m2= comparison
            	    {
            	    match(input,52,FOLLOW_52_in_exp1550); 
            	    pushFollow(FOLLOW_comparison_in_exp1554);
            	    m2=comparison();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("or");
            	    				     				 value.setSubexpression (temp, m2);

            	    }
            	    break;

            	default :
            	    break loop15;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "exp"


    // $ANTLR start "addExp"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:258:1: addExp returns [Expression value] : m1= multExp ( '+' m2= multExp | '-' m2= multExp )* ;
    public final Expression addExp() throws RecognitionException {
        Expression value = null;

        Expression m1 = null;

        Expression m2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:259:2: (m1= multExp ( '+' m2= multExp | '-' m2= multExp )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:259:4: m1= multExp ( '+' m2= multExp | '-' m2= multExp )*
            {
            pushFollow(FOLLOW_multExp_in_addExp1584);
            m1=multExp();

            state._fsp--;

            value = m1;
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:260:3: ( '+' m2= multExp | '-' m2= multExp )*
            loop16:
            do {
                int alt16=3;
                int LA16_0 = input.LA(1);

                if ( (LA16_0==53) ) {
                    alt16=1;
                }
                else if ( (LA16_0==54) ) {
                    alt16=2;
                }


                switch (alt16) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:260:5: '+' m2= multExp
            	    {
            	    match(input,53,FOLLOW_53_in_addExp1605); 
            	    pushFollow(FOLLOW_multExp_in_addExp1609);
            	    m2=multExp();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("plus");
            	    				     				 value.setSubexpression (temp, m2);

            	    }
            	    break;
            	case 2 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:263:5: '-' m2= multExp
            	    {
            	    match(input,54,FOLLOW_54_in_addExp1624); 
            	    pushFollow(FOLLOW_multExp_in_addExp1628);
            	    m2=multExp();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("minus");
            	    				     				 value.setSubexpression (temp, m2);

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
        }
        return value;
    }
    // $ANTLR end "addExp"


    // $ANTLR start "multExp"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:269:1: multExp returns [Expression value] : m1= atomExp ( '*' m2= atomExp | '/' m2= atomExp )* ;
    public final Expression multExp() throws RecognitionException {
        Expression value = null;

        Expression m1 = null;

        Expression m2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:270:2: (m1= atomExp ( '*' m2= atomExp | '/' m2= atomExp )* )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:270:4: m1= atomExp ( '*' m2= atomExp | '/' m2= atomExp )*
            {
            pushFollow(FOLLOW_atomExp_in_multExp1659);
            m1=atomExp();

            state._fsp--;

            value = m1;
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:271:3: ( '*' m2= atomExp | '/' m2= atomExp )*
            loop17:
            do {
                int alt17=3;
                int LA17_0 = input.LA(1);

                if ( (LA17_0==55) ) {
                    alt17=1;
                }
                else if ( (LA17_0==56) ) {
                    alt17=2;
                }


                switch (alt17) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:271:5: '*' m2= atomExp
            	    {
            	    match(input,55,FOLLOW_55_in_multExp1680); 
            	    pushFollow(FOLLOW_atomExp_in_multExp1684);
            	    m2=atomExp();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("times");
            	    				     				 value.setSubexpression (temp, m2);

            	    }
            	    break;
            	case 2 :
            	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:274:5: '/' m2= atomExp
            	    {
            	    match(input,56,FOLLOW_56_in_multExp1699); 
            	    pushFollow(FOLLOW_atomExp_in_multExp1703);
            	    m2=atomExp();

            	    state._fsp--;

            	    Expression temp = value;
            	    				     				 value = new Expression ("divided by");
            	    				     				 value.setSubexpression (temp, m2);

            	    }
            	    break;

            	default :
            	    break loop17;
                }
            } while (true);


            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "multExp"


    // $ANTLR start "atomExp"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:281:1: atomExp returns [Expression value] : (n= Int | i= Identifier | c= String | c= Float | '(' e1= exp ')' | '-' e1= atomExp | f= funcCall );
    public final Expression atomExp() throws RecognitionException {
        Expression value = null;

        Token n=null;
        Token i=null;
        Token c=null;
        Expression e1 = null;

        Expression f = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:282:2: (n= Int | i= Identifier | c= String | c= Float | '(' e1= exp ')' | '-' e1= atomExp | f= funcCall )
            int alt18=7;
            switch ( input.LA(1) ) {
            case Int:
                {
                alt18=1;
                }
                break;
            case Identifier:
                {
                int LA18_2 = input.LA(2);

                if ( (LA18_2==41) ) {
                    alt18=7;
                }
                else if ( (LA18_2==11||LA18_2==40||(LA18_2>=42 && LA18_2<=48)||(LA18_2>=51 && LA18_2<=56)) ) {
                    alt18=2;
                }
                else {
                    NoViableAltException nvae =
                        new NoViableAltException("", 18, 2, input);

                    throw nvae;
                }
                }
                break;
            case String:
                {
                alt18=3;
                }
                break;
            case Float:
                {
                alt18=4;
                }
                break;
            case 41:
                {
                alt18=5;
                }
                break;
            case 54:
                {
                alt18=6;
                }
                break;
            default:
                NoViableAltException nvae =
                    new NoViableAltException("", 18, 0, input);

                throw nvae;
            }

            switch (alt18) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:282:4: n= Int
                    {
                    n=(Token)match(input,Int,FOLLOW_Int_in_atomExp1735); 
                    value = new Expression ("literal int");
                    				     				 value.setValue ((n!=null?n.getText():null));

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:284:4: i= Identifier
                    {
                    i=(Token)match(input,Identifier,FOLLOW_Identifier_in_atomExp1755); 
                    value = new Expression ("identifier");
                    				     				 value.setValue ((i!=null?i.getText():null));

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:286:4: c= String
                    {
                    c=(Token)match(input,String,FOLLOW_String_in_atomExp1774); 
                    value = new Expression ("literal string");
                    				     				 value.setValue ((c!=null?c.getText():null));

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:288:4: c= Float
                    {
                    c=(Token)match(input,Float,FOLLOW_Float_in_atomExp1791); 
                    value = new Expression ("literal float");
                    				     				 value.setValue ((c!=null?c.getText():null));

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:290:4: '(' e1= exp ')'
                    {
                    match(input,41,FOLLOW_41_in_atomExp1807); 
                    pushFollow(FOLLOW_exp_in_atomExp1811);
                    e1=exp();

                    state._fsp--;

                    match(input,42,FOLLOW_42_in_atomExp1813); 
                    value = e1;

                    }
                    break;
                case 6 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:291:5: '-' e1= atomExp
                    {
                    match(input,54,FOLLOW_54_in_atomExp1827); 
                    pushFollow(FOLLOW_atomExp_in_atomExp1831);
                    e1=atomExp();

                    state._fsp--;

                    value = new Expression ("unary minus");
                    				     				 value.setSubexpression (e1);

                    }
                    break;
                case 7 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:293:4: f= funcCall
                    {
                    pushFollow(FOLLOW_funcCall_in_atomExp1848);
                    f=funcCall();

                    state._fsp--;

                    value = f;

                    }
                    break;

            }
        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "atomExp"


    // $ANTLR start "funcCall"
    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:297:1: funcCall returns [Expression value] : i= Identifier '(' (e1= exp ( ',' e2= exp )* )? ')' ;
    public final Expression funcCall() throws RecognitionException {
        Expression value = null;

        Token i=null;
        Expression e1 = null;

        Expression e2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:298:2: (i= Identifier '(' (e1= exp ( ',' e2= exp )* )? ')' )
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:298:4: i= Identifier '(' (e1= exp ( ',' e2= exp )* )? ')'
            {
            i=(Token)match(input,Identifier,FOLLOW_Identifier_in_funcCall1872); 
            match(input,41,FOLLOW_41_in_funcCall1874); 
            value = new Expression ("func"); value.setValue ((i!=null?i.getText():null)); 
            // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:299:14: (e1= exp ( ',' e2= exp )* )?
            int alt20=2;
            int LA20_0 = input.LA(1);

            if ( ((LA20_0>=Identifier && LA20_0<=String)||LA20_0==Float||LA20_0==41||(LA20_0>=49 && LA20_0<=50)||LA20_0==54) ) {
                alt20=1;
            }
            switch (alt20) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:299:15: e1= exp ( ',' e2= exp )*
                    {
                    pushFollow(FOLLOW_exp_in_funcCall1901);
                    e1=exp();

                    state._fsp--;

                    value.addSubexpression (e1);
                    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:300:3: ( ',' e2= exp )*
                    loop19:
                    do {
                        int alt19=2;
                        int LA19_0 = input.LA(1);

                        if ( (LA19_0==40) ) {
                            alt19=1;
                        }


                        switch (alt19) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/runtime/dataflow/DataFlow.g:300:5: ',' e2= exp
                    	    {
                    	    match(input,40,FOLLOW_40_in_funcCall1929); 
                    	    pushFollow(FOLLOW_exp_in_funcCall1933);
                    	    e2=exp();

                    	    state._fsp--;

                    	    value.addSubexpression (e2);

                    	    }
                    	    break;

                    	default :
                    	    break loop19;
                        }
                    } while (true);


                    }
                    break;

            }

            match(input,42,FOLLOW_42_in_funcCall1948); 

            }

        }
        catch (RecognitionException re) {
            reportError(re);
            recover(input,re);
        }
        finally {
        }
        return value;
    }
    // $ANTLR end "funcCall"

    // Delegated rules


 

    public static final BitSet FOLLOW_varList_in_parse48 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_varList_in_parse62 = new BitSet(new long[]{0x0000000000000402L});
    public static final BitSet FOLLOW_10_in_varList93 = new BitSet(new long[]{0x000000FFFFFFD000L});
    public static final BitSet FOLLOW_varDef_in_varList97 = new BitSet(new long[]{0x000000FFFFFFD800L});
    public static final BitSet FOLLOW_varDef_in_varList111 = new BitSet(new long[]{0x000000FFFFFFD800L});
    public static final BitSet FOLLOW_11_in_varList124 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_12_in_varDef139 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef142 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef146 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_14_in_varDef155 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef158 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef162 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_15_in_varDef170 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef173 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef177 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_16_in_varDef186 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef189 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef193 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_17_in_varDef202 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef208 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef210 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_varDef214 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef216 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_18_in_varDef223 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef226 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef228 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_stringList_in_varDef232 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef234 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_19_in_varDef241 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef244 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef246 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef250 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef252 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_20_in_varDef259 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef262 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef264 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef268 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef270 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_21_in_varDef277 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef280 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef282 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef286 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef288 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_22_in_varDef295 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef298 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef300 = new BitSet(new long[]{0x0000000000000020L});
    public static final BitSet FOLLOW_Int_in_varDef304 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef306 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_23_in_varDef314 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef316 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef318 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_exp_in_varDef322 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef324 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_24_in_varDef332 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef335 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef337 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_assignmentList_in_varDef341 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef343 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_25_in_varDef350 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef353 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef355 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_String_in_varDef359 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef361 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_26_in_varDef374 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef378 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef380 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef384 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef386 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_27_in_varDef399 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef404 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef406 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_aggregateList_in_varDef410 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef412 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_28_in_varDef425 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef430 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef434 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_29_in_varDef462 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef466 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_innerInputList_in_varDef470 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_30_in_varDef490 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef498 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef500 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_varDef504 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef506 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_31_in_varDef524 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef531 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_varDef535 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_32_in_varDef563 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef566 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef568 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_String_in_varDef572 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef574 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_33_in_varDef596 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef606 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef608 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef612 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef614 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_34_in_varDef632 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef641 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef643 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_identifierList_in_varDef647 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef649 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_35_in_varDef667 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef669 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef671 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_Boolean_in_varDef675 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef677 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_36_in_varDef690 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef701 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef703 = new BitSet(new long[]{0x0000000000000080L});
    public static final BitSet FOLLOW_Boolean_in_varDef707 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef709 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_37_in_varDef722 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef732 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef734 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_varDef738 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef740 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_38_in_varDef759 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef769 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef771 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_varDef775 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef777 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_39_in_varDef789 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_varDef803 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_varDef805 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_rowList_in_varDef809 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_varDef811 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_10_in_rowList840 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_colList_in_rowList844 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_rowList846 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_rowList867 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_rowList869 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_colList_in_rowList873 = new BitSet(new long[]{0x0000000000000800L});
    public static final BitSet FOLLOW_11_in_rowList875 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_atomExp_in_colList919 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_colList940 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_atomExp_in_colList944 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_Identifier_in_aggregateList1008 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_aggregateList1010 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_aggregateList1014 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_aggregateList1016 = new BitSet(new long[]{0x0040060000000170L});
    public static final BitSet FOLLOW_addExp_in_aggregateList1020 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_aggregateList1023 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_aggregateList1048 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_aggregateList1052 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_aggregateList1054 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_aggregateList1058 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_aggregateList1060 = new BitSet(new long[]{0x0040060000000170L});
    public static final BitSet FOLLOW_addExp_in_aggregateList1064 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_aggregateList1067 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_10_in_innerInputList1117 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_innerInputList1121 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_innerInputList1123 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_innerInputList1128 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_Identifier_in_innerInputList1152 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_innerInputList1154 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_varList_in_innerInputList1158 = new BitSet(new long[]{0x0000000000000810L});
    public static final BitSet FOLLOW_11_in_innerInputList1185 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_stringList1205 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_stringList1217 = new BitSet(new long[]{0x0000000000000040L});
    public static final BitSet FOLLOW_String_in_stringList1221 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_Identifier_in_identifierList1249 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_identifierList1261 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_identifierList1265 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_Identifier_in_assignmentList1292 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_assignmentList1294 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_assignmentList1298 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_40_in_assignmentList1308 = new BitSet(new long[]{0x0000000000000010L});
    public static final BitSet FOLLOW_Identifier_in_assignmentList1312 = new BitSet(new long[]{0x0000000000002000L});
    public static final BitSet FOLLOW_13_in_assignmentList1314 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_assignmentList1318 = new BitSet(new long[]{0x0000010000000002L});
    public static final BitSet FOLLOW_addExp_in_comparison1343 = new BitSet(new long[]{0x0001F80000000002L});
    public static final BitSet FOLLOW_43_in_comparison1356 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1360 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_44_in_comparison1372 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1376 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_45_in_comparison1388 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1392 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_46_in_comparison1404 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1408 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_47_in_comparison1420 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1424 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_48_in_comparison1436 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_addExp_in_comparison1440 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_49_in_comparison1457 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_comparison_in_comparison1461 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_50_in_comparison1477 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_comparison_in_comparison1481 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_comparison_in_exp1513 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_51_in_exp1532 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_comparison_in_exp1536 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_52_in_exp1550 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_comparison_in_exp1554 = new BitSet(new long[]{0x0018000000000002L});
    public static final BitSet FOLLOW_multExp_in_addExp1584 = new BitSet(new long[]{0x0060000000000002L});
    public static final BitSet FOLLOW_53_in_addExp1605 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_multExp_in_addExp1609 = new BitSet(new long[]{0x0060000000000002L});
    public static final BitSet FOLLOW_54_in_addExp1624 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_multExp_in_addExp1628 = new BitSet(new long[]{0x0060000000000002L});
    public static final BitSet FOLLOW_atomExp_in_multExp1659 = new BitSet(new long[]{0x0180000000000002L});
    public static final BitSet FOLLOW_55_in_multExp1680 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_atomExp_in_multExp1684 = new BitSet(new long[]{0x0180000000000002L});
    public static final BitSet FOLLOW_56_in_multExp1699 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_atomExp_in_multExp1703 = new BitSet(new long[]{0x0180000000000002L});
    public static final BitSet FOLLOW_Int_in_atomExp1735 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_atomExp1755 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_String_in_atomExp1774 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Float_in_atomExp1791 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_41_in_atomExp1807 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_exp_in_atomExp1811 = new BitSet(new long[]{0x0000040000000000L});
    public static final BitSet FOLLOW_42_in_atomExp1813 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_54_in_atomExp1827 = new BitSet(new long[]{0x0040020000000170L});
    public static final BitSet FOLLOW_atomExp_in_atomExp1831 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_funcCall_in_atomExp1848 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_Identifier_in_funcCall1872 = new BitSet(new long[]{0x0000020000000000L});
    public static final BitSet FOLLOW_41_in_funcCall1874 = new BitSet(new long[]{0x0046060000000170L});
    public static final BitSet FOLLOW_exp_in_funcCall1901 = new BitSet(new long[]{0x0000050000000000L});
    public static final BitSet FOLLOW_40_in_funcCall1929 = new BitSet(new long[]{0x0046020000000170L});
    public static final BitSet FOLLOW_exp_in_funcCall1933 = new BitSet(new long[]{0x0000050000000000L});
    public static final BitSet FOLLOW_42_in_funcCall1948 = new BitSet(new long[]{0x0000000000000002L});

}