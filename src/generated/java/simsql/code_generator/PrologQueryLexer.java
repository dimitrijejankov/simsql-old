// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g 2016-08-03 05:31:11

    package simsql.code_generator;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class PrologQueryLexer extends Lexer {
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

    public PrologQueryLexer() {;} 
    public PrologQueryLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public PrologQueryLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g"; }

    // $ANTLR start "T__8"
    public final void mT__8() throws RecognitionException {
        try {
            int _type = T__8;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:7:6: ( '.' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:7:8: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__8"

    // $ANTLR start "T__9"
    public final void mT__9() throws RecognitionException {
        try {
            int _type = T__9;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:8:6: ( '(' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:8:8: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__9"

    // $ANTLR start "T__10"
    public final void mT__10() throws RecognitionException {
        try {
            int _type = T__10;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:9:7: ( ')' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:9:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__10"

    // $ANTLR start "T__11"
    public final void mT__11() throws RecognitionException {
        try {
            int _type = T__11;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:10:7: ( '[' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:10:9: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__11"

    // $ANTLR start "T__12"
    public final void mT__12() throws RecognitionException {
        try {
            int _type = T__12;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:11:7: ( ']' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:11:9: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__12"

    // $ANTLR start "T__13"
    public final void mT__13() throws RecognitionException {
        try {
            int _type = T__13;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:12:7: ( ',' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:12:9: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "T__13"

    // $ANTLR start "NUMBER"
    public final void mNUMBER() throws RecognitionException {
        try {
            int _type = NUMBER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:132:5: ( ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ ( 'e' ( '+' | '-' )? ( '0' .. '9' )+ )? )? )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:8: ( '-' )? ( '0' .. '9' )+ ( '.' ( '0' .. '9' )+ ( 'e' ( '+' | '-' )? ( '0' .. '9' )+ )? )?
            {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:8: ( '-' )?
            int alt1=2;
            int LA1_0 = input.LA(1);

            if ( (LA1_0=='-') ) {
                alt1=1;
            }
            switch (alt1) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:9: '-'
                    {
                    match('-'); 

                    }
                    break;

            }

            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:15: ( '0' .. '9' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=2;
                int LA2_0 = input.LA(1);

                if ( ((LA2_0>='0' && LA2_0<='9')) ) {
                    alt2=1;
                }


                switch (alt2) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:16: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;

            	default :
            	    if ( cnt2 >= 1 ) break loop2;
                        EarlyExitException eee =
                            new EarlyExitException(2, input);
                        throw eee;
                }
                cnt2++;
            } while (true);

            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:27: ( '.' ( '0' .. '9' )+ ( 'e' ( '+' | '-' )? ( '0' .. '9' )+ )? )?
            int alt7=2;
            int LA7_0 = input.LA(1);

            if ( (LA7_0=='.') ) {
                alt7=1;
            }
            switch (alt7) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:28: '.' ( '0' .. '9' )+ ( 'e' ( '+' | '-' )? ( '0' .. '9' )+ )?
                    {
                    match('.'); 
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:32: ( '0' .. '9' )+
                    int cnt3=0;
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:33: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    if ( cnt3 >= 1 ) break loop3;
                                EarlyExitException eee =
                                    new EarlyExitException(3, input);
                                throw eee;
                        }
                        cnt3++;
                    } while (true);

                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:44: ( 'e' ( '+' | '-' )? ( '0' .. '9' )+ )?
                    int alt6=2;
                    int LA6_0 = input.LA(1);

                    if ( (LA6_0=='e') ) {
                        alt6=1;
                    }
                    switch (alt6) {
                        case 1 :
                            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:45: 'e' ( '+' | '-' )? ( '0' .. '9' )+
                            {
                            match('e'); 
                            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:49: ( '+' | '-' )?
                            int alt4=2;
                            int LA4_0 = input.LA(1);

                            if ( (LA4_0=='+'||LA4_0=='-') ) {
                                alt4=1;
                            }
                            switch (alt4) {
                                case 1 :
                                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:
                                    {
                                    if ( input.LA(1)=='+'||input.LA(1)=='-' ) {
                                        input.consume();

                                    }
                                    else {
                                        MismatchedSetException mse = new MismatchedSetException(null,input);
                                        recover(mse);
                                        throw mse;}


                                    }
                                    break;

                            }

                            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:60: ( '0' .. '9' )+
                            int cnt5=0;
                            loop5:
                            do {
                                int alt5=2;
                                int LA5_0 = input.LA(1);

                                if ( ((LA5_0>='0' && LA5_0<='9')) ) {
                                    alt5=1;
                                }


                                switch (alt5) {
                            	case 1 :
                            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:133:61: '0' .. '9'
                            	    {
                            	    matchRange('0','9'); 

                            	    }
                            	    break;

                            	default :
                            	    if ( cnt5 >= 1 ) break loop5;
                                        EarlyExitException eee =
                                            new EarlyExitException(5, input);
                                        throw eee;
                                }
                                cnt5++;
                            } while (true);


                            }
                            break;

                    }


                    }
                    break;

            }


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NUMBER"

    // $ANTLR start "ATOM"
    public final void mATOM() throws RecognitionException {
        try {
            int _type = ATOM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:137:5: ( ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '%' | '*' | ' ' )+ )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:138:9: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '%' | '*' | ' ' )+
            {
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:138:9: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '_' | '-' | '%' | '*' | ' ' )+
            int cnt8=0;
            loop8:
            do {
                int alt8=2;
                int LA8_0 = input.LA(1);

                if ( (LA8_0==' '||LA8_0=='%'||LA8_0=='*'||LA8_0=='-'||(LA8_0>='0' && LA8_0<='9')||(LA8_0>='A' && LA8_0<='Z')||LA8_0=='_'||(LA8_0>='a' && LA8_0<='z')) ) {
                    alt8=1;
                }


                switch (alt8) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:
            	    {
            	    if ( input.LA(1)==' '||input.LA(1)=='%'||input.LA(1)=='*'||input.LA(1)=='-'||(input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt8 >= 1 ) break loop8;
                        EarlyExitException eee =
                            new EarlyExitException(8, input);
                        throw eee;
                }
                cnt8++;
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ATOM"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:142:5: ( '\\'' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | '.' | ( '\\\\' '\\'' ) | '{' | '}' | '|' | ',' | ' ' | '_' | '-' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '*' | '+' | '=' | '[' | ']' | '~' | '?' )* '\\'' )
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:9: '\\'' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | '.' | ( '\\\\' '\\'' ) | '{' | '}' | '|' | ',' | ' ' | '_' | '-' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '*' | '+' | '=' | '[' | ']' | '~' | '?' )* '\\''
            {
            match('\''); 
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:14: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | '.' | ( '\\\\' '\\'' ) | '{' | '}' | '|' | ',' | ' ' | '_' | '-' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '*' | '+' | '=' | '[' | ']' | '~' | '?' )*
            loop9:
            do {
                int alt9=32;
                alt9 = dfa9.predict(input);
                switch (alt9) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:15: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 2 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:24: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 3 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:33: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;
            	case 4 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:42: '/'
            	    {
            	    match('/'); 

            	    }
            	    break;
            	case 5 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:46: '('
            	    {
            	    match('('); 

            	    }
            	    break;
            	case 6 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:50: ')'
            	    {
            	    match(')'); 

            	    }
            	    break;
            	case 7 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:54: ':'
            	    {
            	    match(':'); 

            	    }
            	    break;
            	case 8 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:58: '\\\\'
            	    {
            	    match('\\'); 

            	    }
            	    break;
            	case 9 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:63: '.'
            	    {
            	    match('.'); 

            	    }
            	    break;
            	case 10 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:67: ( '\\\\' '\\'' )
            	    {
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:67: ( '\\\\' '\\'' )
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:68: '\\\\' '\\''
            	    {
            	    match('\\'); 
            	    match('\''); 

            	    }


            	    }
            	    break;
            	case 11 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:79: '{'
            	    {
            	    match('{'); 

            	    }
            	    break;
            	case 12 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:83: '}'
            	    {
            	    match('}'); 

            	    }
            	    break;
            	case 13 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:87: '|'
            	    {
            	    match('|'); 

            	    }
            	    break;
            	case 14 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:91: ','
            	    {
            	    match(','); 

            	    }
            	    break;
            	case 15 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:95: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 16 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:99: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 17 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:103: '-'
            	    {
            	    match('-'); 

            	    }
            	    break;
            	case 18 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:107: '!'
            	    {
            	    match('!'); 

            	    }
            	    break;
            	case 19 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:111: '@'
            	    {
            	    match('@'); 

            	    }
            	    break;
            	case 20 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:115: '#'
            	    {
            	    match('#'); 

            	    }
            	    break;
            	case 21 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:119: '$'
            	    {
            	    match('$'); 

            	    }
            	    break;
            	case 22 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:123: '%'
            	    {
            	    match('%'); 

            	    }
            	    break;
            	case 23 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:127: '^'
            	    {
            	    match('^'); 

            	    }
            	    break;
            	case 24 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:131: '&'
            	    {
            	    match('&'); 

            	    }
            	    break;
            	case 25 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:135: '*'
            	    {
            	    match('*'); 

            	    }
            	    break;
            	case 26 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:139: '+'
            	    {
            	    match('+'); 

            	    }
            	    break;
            	case 27 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:143: '='
            	    {
            	    match('='); 

            	    }
            	    break;
            	case 28 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:147: '['
            	    {
            	    match('['); 

            	    }
            	    break;
            	case 29 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:151: ']'
            	    {
            	    match(']'); 

            	    }
            	    break;
            	case 30 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:155: '~'
            	    {
            	    match('~'); 

            	    }
            	    break;
            	case 31 :
            	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:143:159: '?'
            	    {
            	    match('?'); 

            	    }
            	    break;

            	default :
            	    break loop9;
                }
            } while (true);

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:146:4: ( '\\n' '%' ( options {greedy=false; } : ( . )* ) '\\n' | ( ' ' | '\\t' | '\\n' | '\\r' | '\\u000C' ) )
            int alt11=2;
            int LA11_0 = input.LA(1);

            if ( (LA11_0=='\n') ) {
                int LA11_1 = input.LA(2);

                if ( (LA11_1=='%') ) {
                    alt11=1;
                }
                else {
                    alt11=2;}
            }
            else if ( (LA11_0=='\t'||(LA11_0>='\f' && LA11_0<='\r')||LA11_0==' ') ) {
                alt11=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 11, 0, input);

                throw nvae;
            }
            switch (alt11) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:147:9: '\\n' '%' ( options {greedy=false; } : ( . )* ) '\\n'
                    {
                    match('\n'); 
                    match('%'); 
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:147:18: ( options {greedy=false; } : ( . )* )
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:147:44: ( . )*
                    {
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:147:44: ( . )*
                    loop10:
                    do {
                        int alt10=2;
                        int LA10_0 = input.LA(1);

                        if ( (LA10_0=='\n') ) {
                            alt10=2;
                        }
                        else if ( ((LA10_0>='\u0000' && LA10_0<='\t')||(LA10_0>='\u000B' && LA10_0<='\uFFFF')) ) {
                            alt10=1;
                        }


                        switch (alt10) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:147:44: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop10;
                        }
                    } while (true);


                    }

                    match('\n'); 

                                _channel = HIDDEN;
                            

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:152:9: ( ' ' | '\\t' | '\\n' | '\\r' | '\\u000C' )
                    {
                    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
                        input.consume();

                    }
                    else {
                        MismatchedSetException mse = new MismatchedSetException(null,input);
                        recover(mse);
                        throw mse;}


                                _channel = HIDDEN;
                            

                    }
                    break;

            }
            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    public void mTokens() throws RecognitionException {
        // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:8: ( T__8 | T__9 | T__10 | T__11 | T__12 | T__13 | NUMBER | ATOM | STRING | WS )
        int alt12=10;
        alt12 = dfa12.predict(input);
        switch (alt12) {
            case 1 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:10: T__8
                {
                mT__8(); 

                }
                break;
            case 2 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:15: T__9
                {
                mT__9(); 

                }
                break;
            case 3 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:20: T__10
                {
                mT__10(); 

                }
                break;
            case 4 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:26: T__11
                {
                mT__11(); 

                }
                break;
            case 5 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:32: T__12
                {
                mT__12(); 

                }
                break;
            case 6 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:38: T__13
                {
                mT__13(); 

                }
                break;
            case 7 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:44: NUMBER
                {
                mNUMBER(); 

                }
                break;
            case 8 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:51: ATOM
                {
                mATOM(); 

                }
                break;
            case 9 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:56: STRING
                {
                mSTRING(); 

                }
                break;
            case 10 :
                // /home/dimitrije/git/v0.5/src/code_generator/grammar/PrologQuery.g:1:63: WS
                {
                mWS(); 

                }
                break;

        }

    }


    protected DFA9 dfa9 = new DFA9(this);
    protected DFA12 dfa12 = new DFA12(this);
    static final String DFA9_eotS =
        "\40\uffff\1\41\2\uffff";
    static final String DFA9_eofS =
        "\43\uffff";
    static final String DFA9_minS =
        "\1\40\10\uffff\1\40\26\uffff\1\40\2\uffff";
    static final String DFA9_maxS =
        "\1\176\10\uffff\1\176\26\uffff\1\176\2\uffff";
    static final String DFA9_acceptS =
        "\1\uffff\1\40\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\11\1\13\1\14"+
        "\1\15\1\16\1\17\1\20\1\21\1\22\1\23\1\24\1\25\1\26\1\27\1\30\1\31"+
        "\1\32\1\33\1\34\1\35\1\36\1\37\1\uffff\1\10\1\12";
    static final String DFA9_specialS =
        "\43\uffff}>";
    static final String[] DFA9_transitionS = {
            "\1\17\1\22\1\uffff\1\24\1\25\1\26\1\30\1\1\1\6\1\7\1\31\1\32"+
            "\1\16\1\21\1\12\1\5\12\4\1\10\2\uffff\1\33\1\uffff\1\37\1\23"+
            "\32\3\1\34\1\11\1\35\1\27\1\20\1\uffff\32\2\1\13\1\15\1\14\1"+
            "\36",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\41\1\uffff\4\41\1\40\23\41\2\uffff\1\41\1\uffff\41\41\1"+
            "\uffff\36\41",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\2\42\1\uffff\30\42\2\uffff\1\42\1\uffff\41\42\1\uffff\36\42",
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
            return "()* loopback of 143:14: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | '.' | ( '\\\\' '\\'' ) | '{' | '}' | '|' | ',' | ' ' | '_' | '-' | '!' | '@' | '#' | '$' | '%' | '^' | '&' | '*' | '+' | '=' | '[' | ']' | '~' | '?' )*";
        }
    }
    static final String DFA12_eotS =
        "\7\uffff\1\14\1\15\5\uffff";
    static final String DFA12_eofS =
        "\16\uffff";
    static final String DFA12_minS =
        "\1\11\6\uffff\1\60\1\40\5\uffff";
    static final String DFA12_maxS =
        "\1\172\6\uffff\1\71\1\172\5\uffff";
    static final String DFA12_acceptS =
        "\1\uffff\1\1\1\2\1\3\1\4\1\5\1\6\2\uffff\1\10\1\11\1\12\1\10\1\7";
    static final String DFA12_specialS =
        "\16\uffff}>";
    static final String[] DFA12_transitionS = {
            "\2\13\1\uffff\2\13\22\uffff\1\11\4\uffff\1\14\1\uffff\1\12\1"+
            "\2\1\3\1\14\1\uffff\1\6\1\7\1\1\1\uffff\12\10\7\uffff\32\14"+
            "\1\4\1\uffff\1\5\1\uffff\1\14\1\uffff\32\14",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\10",
            "\1\14\4\uffff\1\14\4\uffff\1\14\2\uffff\1\14\2\uffff\12\10"+
            "\7\uffff\32\14\4\uffff\1\14\1\uffff\32\14",
            "",
            "",
            "",
            "",
            ""
    };

    static final short[] DFA12_eot = DFA.unpackEncodedString(DFA12_eotS);
    static final short[] DFA12_eof = DFA.unpackEncodedString(DFA12_eofS);
    static final char[] DFA12_min = DFA.unpackEncodedStringToUnsignedChars(DFA12_minS);
    static final char[] DFA12_max = DFA.unpackEncodedStringToUnsignedChars(DFA12_maxS);
    static final short[] DFA12_accept = DFA.unpackEncodedString(DFA12_acceptS);
    static final short[] DFA12_special = DFA.unpackEncodedString(DFA12_specialS);
    static final short[][] DFA12_transition;

    static {
        int numStates = DFA12_transitionS.length;
        DFA12_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA12_transition[i] = DFA.unpackEncodedString(DFA12_transitionS[i]);
        }
    }

    class DFA12 extends DFA {

        public DFA12(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 12;
            this.eot = DFA12_eot;
            this.eof = DFA12_eof;
            this.min = DFA12_min;
            this.max = DFA12_max;
            this.accept = DFA12_accept;
            this.special = DFA12_special;
            this.transition = DFA12_transition;
        }
        public String getDescription() {
            return "1:1: Tokens : ( T__8 | T__9 | T__10 | T__11 | T__12 | T__13 | NUMBER | ATOM | STRING | WS );";
        }
    }
 

}