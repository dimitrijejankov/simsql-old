// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g 2016-08-03 05:31:11

    package simsql.code_generator;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

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
public class PrologQueryParser extends Parser {
    public static final String[] tokenNames = new String[] {
        "<invalid>", "<EOR>", "<DOWN>", "<UP>", "ATOM", "NUMBER", "STRING", "WS", "'.'", "'('", "')'", "'['", "']'", "','"
    };
    public static final int WS=7;
    public static final int T__12=12;
    public static final int T__11=11;
    public static final int T__13=13;
    public static final int T__10=10;
    public static final int NUMBER=5;
    public static final int ATOM=4;
    public static final int EOF=-1;
    public static final int T__9=9;
    public static final int T__8=8;
    public static final int STRING=6;

    // delegates
    // delegators


        public PrologQueryParser(TokenStream input) {
            this(input, new RecognizerSharedState());
        }
        public PrologQueryParser(TokenStream input, RecognizerSharedState state) {
            super(input, state);
             
        }
        

    public String[] getTokenNames() { return PrologQueryParser.tokenNames; }
    public String getGrammarFileName() { return "/home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g"; }



    // $ANTLR start "query"
    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:36:1: query returns [ArrayList<TupleTerm> value] : t1= tupleTerm '.' (t2= tupleTerm '.' )* ;
    public final ArrayList<TupleTerm> query() throws RecognitionException {
        ArrayList<TupleTerm> value = null;

        TupleTerm t1 = null;

        TupleTerm t2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:38:5: (t1= tupleTerm '.' (t2= tupleTerm '.' )* )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:39:9: t1= tupleTerm '.' (t2= tupleTerm '.' )*
            {
            pushFollow(FOLLOW_tupleTerm_in_query67);
            t1=tupleTerm();

            state._fsp--;

            match(input,8,FOLLOW_8_in_query69); 

                        value = new ArrayList<TupleTerm>();
                        value.add(t1);
                    
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:45:9: (t2= tupleTerm '.' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( (LA1_0==ATOM) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:46:13: t2= tupleTerm '.'
            	    {
            	    pushFollow(FOLLOW_tupleTerm_in_query106);
            	    t2=tupleTerm();

            	    state._fsp--;

            	    match(input,8,FOLLOW_8_in_query108); 

            	                    value.add(t2);
            	                

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
    // $ANTLR end "query"


    // $ANTLR start "tupleTerm"
    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:54:1: tupleTerm returns [TupleTerm value] : a1= ATOM '(' t1= termSet ')' ;
    public final TupleTerm tupleTerm() throws RecognitionException {
        TupleTerm value = null;

        Token a1=null;
        ArrayList<PrologTerm> t1 = null;


        try {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:56:5: (a1= ATOM '(' t1= termSet ')' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:57:9: a1= ATOM '(' t1= termSet ')'
            {
            a1=(Token)match(input,ATOM,FOLLOW_ATOM_in_tupleTerm168); 
            match(input,9,FOLLOW_9_in_tupleTerm170); 
            pushFollow(FOLLOW_termSet_in_tupleTerm174);
            t1=termSet();

            state._fsp--;

            match(input,10,FOLLOW_10_in_tupleTerm176); 

                        value = new TupleTerm(new AtomTerm((a1!=null?a1.getText():null)), t1);
                    

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
    // $ANTLR end "tupleTerm"


    // $ANTLR start "listTerm"
    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:63:1: listTerm returns [ListTerm value] : ( '[' t1= termSet ']' | '(' t1= termSet ')' );
    public final ListTerm listTerm() throws RecognitionException {
        ListTerm value = null;

        ArrayList<PrologTerm> t1 = null;


        try {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:65:5: ( '[' t1= termSet ']' | '(' t1= termSet ')' )
            int alt2=2;
            int LA2_0 = input.LA(1);

            if ( (LA2_0==11) ) {
                alt2=1;
            }
            else if ( (LA2_0==9) ) {
                alt2=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 2, 0, input);

                throw nvae;
            }
            switch (alt2) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:66:9: '[' t1= termSet ']'
                    {
                    match(input,11,FOLLOW_11_in_listTerm217); 
                    pushFollow(FOLLOW_termSet_in_listTerm221);
                    t1=termSet();

                    state._fsp--;

                    match(input,12,FOLLOW_12_in_listTerm223); 

                                value = new ListTerm(t1);
                            

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:71:9: '(' t1= termSet ')'
                    {
                    match(input,9,FOLLOW_9_in_listTerm249); 
                    pushFollow(FOLLOW_termSet_in_listTerm253);
                    t1=termSet();

                    state._fsp--;

                    match(input,10,FOLLOW_10_in_listTerm255); 

                                value = new ListTerm(t1);
                            

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
    // $ANTLR end "listTerm"


    // $ANTLR start "termSet"
    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:78:1: termSet returns [ArrayList<PrologTerm> value] : (t1= term ( ',' t2= term )* )? ;
    public final ArrayList<PrologTerm> termSet() throws RecognitionException {
        ArrayList<PrologTerm> value = null;

        PrologTerm t1 = null;

        PrologTerm t2 = null;


        try {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:80:5: ( (t1= term ( ',' t2= term )* )? )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:81:9: (t1= term ( ',' t2= term )* )?
            {

                        value = new ArrayList<PrologTerm>();
                    
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:85:9: (t1= term ( ',' t2= term )* )?
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>=ATOM && LA4_0<=STRING)||LA4_0==9||LA4_0==11) ) {
                alt4=1;
            }
            switch (alt4) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:86:13: t1= term ( ',' t2= term )*
                    {
                    pushFollow(FOLLOW_term_in_termSet333);
                    t1=term();

                    state._fsp--;


                                    value.add(t1);
                                
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:91:13: ( ',' t2= term )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( (LA3_0==13) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:92:17: ',' t2= term
                    	    {
                    	    match(input,13,FOLLOW_13_in_termSet392); 
                    	    pushFollow(FOLLOW_term_in_termSet396);
                    	    t2=term();

                    	    state._fsp--;


                    	                        value.add(t2);
                    	                    

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);


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
        }
        return value;
    }
    // $ANTLR end "termSet"


    // $ANTLR start "term"
    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:100:1: term returns [PrologTerm value] : (t1= tupleTerm | l1= listTerm | a1= ATOM | n1= NUMBER | s1= STRING );
    public final PrologTerm term() throws RecognitionException {
        PrologTerm value = null;

        Token a1=null;
        Token n1=null;
        Token s1=null;
        TupleTerm t1 = null;

        ListTerm l1 = null;


        try {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:101:5: (t1= tupleTerm | l1= listTerm | a1= ATOM | n1= NUMBER | s1= STRING )
            int alt5=5;
            alt5 = dfa5.predict(input);
            switch (alt5) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:102:9: t1= tupleTerm
                    {
                    pushFollow(FOLLOW_tupleTerm_in_term471);
                    t1=tupleTerm();

                    state._fsp--;


                                value = t1;
                            

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:107:9: l1= listTerm
                    {
                    pushFollow(FOLLOW_listTerm_in_term499);
                    l1=listTerm();

                    state._fsp--;


                                value = l1;
                            

                    }
                    break;
                case 3 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:112:9: a1= ATOM
                    {
                    a1=(Token)match(input,ATOM,FOLLOW_ATOM_in_term527); 

                                value = new AtomTerm((a1!=null?a1.getText():null));
                            

                    }
                    break;
                case 4 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:117:9: n1= NUMBER
                    {
                    n1=(Token)match(input,NUMBER,FOLLOW_NUMBER_in_term555); 

                                value = new AtomTerm((n1!=null?n1.getText():null));
                            

                    }
                    break;
                case 5 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:122:9: s1= STRING
                    {
                    s1=(Token)match(input,STRING,FOLLOW_STRING_in_term583); 

                                value = new AtomTerm((s1!=null?s1.getText():null));
                            

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
    // $ANTLR end "term"

    // Delegated rules


    protected DFA5 dfa5 = new DFA5(this);
    static final String DFA5_eotS =
        "\12\uffff";
    static final String DFA5_eofS =
        "\12\uffff";
    static final String DFA5_minS =
        "\1\4\1\11\10\uffff";
    static final String DFA5_maxS =
        "\1\13\1\15\10\uffff";
    static final String DFA5_acceptS =
        "\2\uffff\1\2\1\uffff\1\4\1\5\1\1\1\3\2\uffff";
    static final String DFA5_specialS =
        "\12\uffff}>";
    static final String[] DFA5_transitionS = {
            "\1\1\1\4\1\5\2\uffff\1\2\1\uffff\1\2",
            "\1\6\1\7\1\uffff\2\7",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA5_eot = DFA.unpackEncodedString(DFA5_eotS);
    static final short[] DFA5_eof = DFA.unpackEncodedString(DFA5_eofS);
    static final char[] DFA5_min = DFA.unpackEncodedStringToUnsignedChars(DFA5_minS);
    static final char[] DFA5_max = DFA.unpackEncodedStringToUnsignedChars(DFA5_maxS);
    static final short[] DFA5_accept = DFA.unpackEncodedString(DFA5_acceptS);
    static final short[] DFA5_special = DFA.unpackEncodedString(DFA5_specialS);
    static final short[][] DFA5_transition;

    static {
        int numStates = DFA5_transitionS.length;
        DFA5_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA5_transition[i] = DFA.unpackEncodedString(DFA5_transitionS[i]);
        }
    }

    class DFA5 extends DFA {

        public DFA5(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 5;
            this.eot = DFA5_eot;
            this.eof = DFA5_eof;
            this.min = DFA5_min;
            this.max = DFA5_max;
            this.accept = DFA5_accept;
            this.special = DFA5_special;
            this.transition = DFA5_transition;
        }
        public String getDescription() {
            return "100:1: term returns [PrologTerm value] : (t1= tupleTerm | l1= listTerm | a1= ATOM | n1= NUMBER | s1= STRING );";
        }
    }
 

    public static final BitSet FOLLOW_tupleTerm_in_query67 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_8_in_query69 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_tupleTerm_in_query106 = new BitSet(new long[]{0x0000000000000100L});
    public static final BitSet FOLLOW_8_in_query108 = new BitSet(new long[]{0x0000000000000012L});
    public static final BitSet FOLLOW_ATOM_in_tupleTerm168 = new BitSet(new long[]{0x0000000000000200L});
    public static final BitSet FOLLOW_9_in_tupleTerm170 = new BitSet(new long[]{0x0000000000000E70L});
    public static final BitSet FOLLOW_termSet_in_tupleTerm174 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_tupleTerm176 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_11_in_listTerm217 = new BitSet(new long[]{0x0000000000001A70L});
    public static final BitSet FOLLOW_termSet_in_listTerm221 = new BitSet(new long[]{0x0000000000001000L});
    public static final BitSet FOLLOW_12_in_listTerm223 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_9_in_listTerm249 = new BitSet(new long[]{0x0000000000000E70L});
    public static final BitSet FOLLOW_termSet_in_listTerm253 = new BitSet(new long[]{0x0000000000000400L});
    public static final BitSet FOLLOW_10_in_listTerm255 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_term_in_termSet333 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_13_in_termSet392 = new BitSet(new long[]{0x0000000000000A70L});
    public static final BitSet FOLLOW_term_in_termSet396 = new BitSet(new long[]{0x0000000000002002L});
    public static final BitSet FOLLOW_tupleTerm_in_term471 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_listTerm_in_term499 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_ATOM_in_term527 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_NUMBER_in_term555 = new BitSet(new long[]{0x0000000000000002L});
    public static final BitSet FOLLOW_STRING_in_term583 = new BitSet(new long[]{0x0000000000000002L});

}