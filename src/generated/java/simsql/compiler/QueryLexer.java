// $ANTLR 3.2 Sep 23, 2009 12:02:23 /home/dimitrije/git/v0.5/src/compiler/parser/Query.g 2016-08-03 05:31:16

package simsql.compiler;


import org.antlr.runtime.*;
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;

public class QueryLexer extends Lexer {
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
    public static final int B=82;
    public static final int VIEW=13;
    public static final int RBRACE=109;
    public static final int C=83;
    public static final int ASC=34;
    public static final int L=92;
    public static final int M=93;
    public static final int N=94;
    public static final int KEY=47;
    public static final int O=95;
    public static final int H=88;
    public static final int I=89;
    public static final int J=90;
    public static final int ELSE=56;
    public static final int MATERIALIZED=14;
    public static final int K=91;
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
    public static final int LIKE=75;
    public static final int WITH=42;
    public static final int LESSTHAN=76;
    public static final int BY=32;
    public static final int VALUES=26;
    public static final int NOTEQUALS=71;
    public static final int HAVING=36;
    public static final int COLMATRIX=67;
    public static final int MIN=61;
    public static final int MINUS=52;
    public static final int TRUE=70;
    public static final int UNION=45;
    public static final int COLON=38;
    public static final int VECTOR=65;
    public static final int VGFUNCTION=15;
    public static final int DROP=5;
    public static final int WHEN=54;
    public static final int GENERALTABLEINDEX=12;
    public static final int STDEV=64;
    public static final int DESC=35;
    public static final int BETWEEN=74;
    public static final int STRING=29;

    // delegates
    // delegators

    public QueryLexer() {;} 
    public QueryLexer(CharStream input) {
        this(input, new RecognizerSharedState());
    }
    public QueryLexer(CharStream input, RecognizerSharedState state) {
        super(input,state);

    }
    public String getGrammarFileName() { return "/home/dimitrije/git/v0.5/src/compiler/parser/Query.g"; }

    // $ANTLR start "A"
    public final void mA() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1670:13: ( ( 'a' | 'A' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1670:15: ( 'a' | 'A' )
            {
            if ( input.LA(1)=='A'||input.LA(1)=='a' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "A"

    // $ANTLR start "B"
    public final void mB() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1671:13: ( ( 'b' | 'B' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1671:15: ( 'b' | 'B' )
            {
            if ( input.LA(1)=='B'||input.LA(1)=='b' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "B"

    // $ANTLR start "C"
    public final void mC() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1672:13: ( ( 'c' | 'C' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1672:15: ( 'c' | 'C' )
            {
            if ( input.LA(1)=='C'||input.LA(1)=='c' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "C"

    // $ANTLR start "D"
    public final void mD() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1673:13: ( ( 'd' | 'D' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1673:15: ( 'd' | 'D' )
            {
            if ( input.LA(1)=='D'||input.LA(1)=='d' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "D"

    // $ANTLR start "E"
    public final void mE() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1674:13: ( ( 'e' | 'E' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1674:15: ( 'e' | 'E' )
            {
            if ( input.LA(1)=='E'||input.LA(1)=='e' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "E"

    // $ANTLR start "F"
    public final void mF() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1675:13: ( ( 'f' | 'F' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1675:15: ( 'f' | 'F' )
            {
            if ( input.LA(1)=='F'||input.LA(1)=='f' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "F"

    // $ANTLR start "G"
    public final void mG() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1676:13: ( ( 'g' | 'G' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1676:15: ( 'g' | 'G' )
            {
            if ( input.LA(1)=='G'||input.LA(1)=='g' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "G"

    // $ANTLR start "H"
    public final void mH() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1677:13: ( ( 'h' | 'H' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1677:15: ( 'h' | 'H' )
            {
            if ( input.LA(1)=='H'||input.LA(1)=='h' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "H"

    // $ANTLR start "I"
    public final void mI() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1678:13: ( ( 'i' | 'I' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1678:15: ( 'i' | 'I' )
            {
            if ( input.LA(1)=='I'||input.LA(1)=='i' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "I"

    // $ANTLR start "J"
    public final void mJ() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1679:13: ( ( 'j' | 'J' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1679:15: ( 'j' | 'J' )
            {
            if ( input.LA(1)=='J'||input.LA(1)=='j' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "J"

    // $ANTLR start "K"
    public final void mK() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1680:13: ( ( 'k' | 'K' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1680:15: ( 'k' | 'K' )
            {
            if ( input.LA(1)=='K'||input.LA(1)=='k' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "K"

    // $ANTLR start "L"
    public final void mL() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1681:13: ( ( 'l' | 'L' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1681:15: ( 'l' | 'L' )
            {
            if ( input.LA(1)=='L'||input.LA(1)=='l' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "L"

    // $ANTLR start "M"
    public final void mM() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1682:13: ( ( 'm' | 'M' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1682:15: ( 'm' | 'M' )
            {
            if ( input.LA(1)=='M'||input.LA(1)=='m' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "M"

    // $ANTLR start "N"
    public final void mN() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1683:13: ( ( 'n' | 'N' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1683:15: ( 'n' | 'N' )
            {
            if ( input.LA(1)=='N'||input.LA(1)=='n' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "N"

    // $ANTLR start "O"
    public final void mO() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1684:13: ( ( 'o' | 'O' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1684:15: ( 'o' | 'O' )
            {
            if ( input.LA(1)=='O'||input.LA(1)=='o' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "O"

    // $ANTLR start "P"
    public final void mP() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1685:13: ( ( 'p' | 'P' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1685:15: ( 'p' | 'P' )
            {
            if ( input.LA(1)=='P'||input.LA(1)=='p' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "P"

    // $ANTLR start "Q"
    public final void mQ() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1686:13: ( ( 'q' | 'Q' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1686:15: ( 'q' | 'Q' )
            {
            if ( input.LA(1)=='Q'||input.LA(1)=='q' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Q"

    // $ANTLR start "R"
    public final void mR() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1687:13: ( ( 'r' | 'R' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1687:15: ( 'r' | 'R' )
            {
            if ( input.LA(1)=='R'||input.LA(1)=='r' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "R"

    // $ANTLR start "S"
    public final void mS() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1688:13: ( ( 's' | 'S' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1688:15: ( 's' | 'S' )
            {
            if ( input.LA(1)=='S'||input.LA(1)=='s' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "S"

    // $ANTLR start "T"
    public final void mT() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1689:13: ( ( 't' | 'T' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1689:15: ( 't' | 'T' )
            {
            if ( input.LA(1)=='T'||input.LA(1)=='t' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "T"

    // $ANTLR start "U"
    public final void mU() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1690:13: ( ( 'u' | 'U' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1690:15: ( 'u' | 'U' )
            {
            if ( input.LA(1)=='U'||input.LA(1)=='u' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "U"

    // $ANTLR start "V"
    public final void mV() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1691:13: ( ( 'v' | 'V' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1691:15: ( 'v' | 'V' )
            {
            if ( input.LA(1)=='V'||input.LA(1)=='v' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "V"

    // $ANTLR start "W"
    public final void mW() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1692:13: ( ( 'w' | 'W' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1692:15: ( 'w' | 'W' )
            {
            if ( input.LA(1)=='W'||input.LA(1)=='w' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "W"

    // $ANTLR start "X"
    public final void mX() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1693:13: ( ( 'x' | 'X' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1693:15: ( 'x' | 'X' )
            {
            if ( input.LA(1)=='X'||input.LA(1)=='x' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "X"

    // $ANTLR start "Y"
    public final void mY() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1694:13: ( ( 'y' | 'Y' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1694:15: ( 'y' | 'Y' )
            {
            if ( input.LA(1)=='Y'||input.LA(1)=='y' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Y"

    // $ANTLR start "Z"
    public final void mZ() throws RecognitionException {
        try {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1695:13: ( ( 'z' | 'Z' ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1695:15: ( 'z' | 'Z' )
            {
            if ( input.LA(1)=='Z'||input.LA(1)=='z' ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}


            }

        }
        finally {
        }
    }
    // $ANTLR end "Z"

    // $ANTLR start "SELECT"
    public final void mSELECT() throws RecognitionException {
        try {
            int _type = SELECT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1698:8: ( S E L E C T )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1698:10: S E L E C T
            {
            mS(); 
            mE(); 
            mL(); 
            mE(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SELECT"

    // $ANTLR start "CREATE"
    public final void mCREATE() throws RecognitionException {
        try {
            int _type = CREATE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1699:7: ( C R E A T E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1699:9: C R E A T E
            {
            mC(); 
            mR(); 
            mE(); 
            mA(); 
            mT(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CREATE"

    // $ANTLR start "TABLE"
    public final void mTABLE() throws RecognitionException {
        try {
            int _type = TABLE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1700:6: ( T A B L E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1700:8: T A B L E
            {
            mT(); 
            mA(); 
            mB(); 
            mL(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TABLE"

    // $ANTLR start "VIEW"
    public final void mVIEW() throws RecognitionException {
        try {
            int _type = VIEW;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1701:5: ( V I E W )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1701:7: V I E W
            {
            mV(); 
            mI(); 
            mE(); 
            mW(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VIEW"

    // $ANTLR start "MATERIALIZED"
    public final void mMATERIALIZED() throws RecognitionException {
        try {
            int _type = MATERIALIZED;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1702:13: ( M A T E R I A L I Z E D )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1702:15: M A T E R I A L I Z E D
            {
            mM(); 
            mA(); 
            mT(); 
            mE(); 
            mR(); 
            mI(); 
            mA(); 
            mL(); 
            mI(); 
            mZ(); 
            mE(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MATERIALIZED"

    // $ANTLR start "FROM"
    public final void mFROM() throws RecognitionException {
        try {
            int _type = FROM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1703:6: ( F R O M )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1703:8: F R O M
            {
            mF(); 
            mR(); 
            mO(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FROM"

    // $ANTLR start "WHERE"
    public final void mWHERE() throws RecognitionException {
        try {
            int _type = WHERE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1704:6: ( W H E R E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1704:8: W H E R E
            {
            mW(); 
            mH(); 
            mE(); 
            mR(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHERE"

    // $ANTLR start "GROUP"
    public final void mGROUP() throws RecognitionException {
        try {
            int _type = GROUP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1705:6: ( G R O U P )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1705:8: G R O U P
            {
            mG(); 
            mR(); 
            mO(); 
            mU(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GROUP"

    // $ANTLR start "ORDER"
    public final void mORDER() throws RecognitionException {
        try {
            int _type = ORDER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1706:6: ( O R D E R )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1706:8: O R D E R
            {
            mO(); 
            mR(); 
            mD(); 
            mE(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ORDER"

    // $ANTLR start "BY"
    public final void mBY() throws RecognitionException {
        try {
            int _type = BY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1707:3: ( B Y )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1707:5: B Y
            {
            mB(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BY"

    // $ANTLR start "HAVING"
    public final void mHAVING() throws RecognitionException {
        try {
            int _type = HAVING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1708:7: ( H A V I N G )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1708:9: H A V I N G
            {
            mH(); 
            mA(); 
            mV(); 
            mI(); 
            mN(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "HAVING"

    // $ANTLR start "RANDOM"
    public final void mRANDOM() throws RecognitionException {
        try {
            int _type = RANDOM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1709:7: ( R A N D O M )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1709:9: R A N D O M
            {
            mR(); 
            mA(); 
            mN(); 
            mD(); 
            mO(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RANDOM"

    // $ANTLR start "SOURCE"
    public final void mSOURCE() throws RecognitionException {
        try {
            int _type = SOURCE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1710:7: ( S O U R C E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1710:9: S O U R C E
            {
            mS(); 
            mO(); 
            mU(); 
            mR(); 
            mC(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SOURCE"

    // $ANTLR start "RETURNS"
    public final void mRETURNS() throws RecognitionException {
        try {
            int _type = RETURNS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1711:8: ( R E T U R N S )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1711:10: R E T U R N S
            {
            mR(); 
            mE(); 
            mT(); 
            mU(); 
            mR(); 
            mN(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RETURNS"

    // $ANTLR start "FOR"
    public final void mFOR() throws RecognitionException {
        try {
            int _type = FOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1713:4: ( F O R )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1713:6: F O R
            {
            mF(); 
            mO(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FOR"

    // $ANTLR start "EACH"
    public final void mEACH() throws RecognitionException {
        try {
            int _type = EACH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1714:5: ( E A C H )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1714:7: E A C H
            {
            mE(); 
            mA(); 
            mC(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EACH"

    // $ANTLR start "WITH"
    public final void mWITH() throws RecognitionException {
        try {
            int _type = WITH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1715:5: ( W I T H )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1715:7: W I T H
            {
            mW(); 
            mI(); 
            mT(); 
            mH(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WITH"

    // $ANTLR start "ALL"
    public final void mALL() throws RecognitionException {
        try {
            int _type = ALL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1717:4: ( A L L )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1717:6: A L L
            {
            mA(); 
            mL(); 
            mL(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ALL"

    // $ANTLR start "DISTINCT"
    public final void mDISTINCT() throws RecognitionException {
        try {
            int _type = DISTINCT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1718:9: ( D I S T I N C T )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1718:11: D I S T I N C T
            {
            mD(); 
            mI(); 
            mS(); 
            mT(); 
            mI(); 
            mN(); 
            mC(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DISTINCT"

    // $ANTLR start "AS"
    public final void mAS() throws RecognitionException {
        try {
            int _type = AS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1719:3: ( A S )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1719:5: A S
            {
            mA(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AS"

    // $ANTLR start "IN"
    public final void mIN() throws RecognitionException {
        try {
            int _type = IN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1720:3: ( I N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1720:5: I N
            {
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IN"

    // $ANTLR start "VALUES"
    public final void mVALUES() throws RecognitionException {
        try {
            int _type = VALUES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1721:7: ( V A L U E S )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1721:9: V A L U E S
            {
            mV(); 
            mA(); 
            mL(); 
            mU(); 
            mE(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VALUES"

    // $ANTLR start "AND"
    public final void mAND() throws RecognitionException {
        try {
            int _type = AND;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1723:4: ( A N D )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1723:6: A N D
            {
            mA(); 
            mN(); 
            mD(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AND"

    // $ANTLR start "OR"
    public final void mOR() throws RecognitionException {
        try {
            int _type = OR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1724:3: ( O R )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1724:5: O R
            {
            mO(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OR"

    // $ANTLR start "NOT"
    public final void mNOT() throws RecognitionException {
        try {
            int _type = NOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1725:4: ( N O T )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1725:6: N O T
            {
            mN(); 
            mO(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOT"

    // $ANTLR start "LIKE"
    public final void mLIKE() throws RecognitionException {
        try {
            int _type = LIKE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1726:5: ( L I K E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1726:7: L I K E
            {
            mL(); 
            mI(); 
            mK(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LIKE"

    // $ANTLR start "BETWEEN"
    public final void mBETWEEN() throws RecognitionException {
        try {
            int _type = BETWEEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1727:8: ( B E T W E E N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1727:10: B E T W E E N
            {
            mB(); 
            mE(); 
            mT(); 
            mW(); 
            mE(); 
            mE(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "BETWEEN"

    // $ANTLR start "EXISTS"
    public final void mEXISTS() throws RecognitionException {
        try {
            int _type = EXISTS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1728:7: ( E X I S T S )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1728:9: E X I S T S
            {
            mE(); 
            mX(); 
            mI(); 
            mS(); 
            mT(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EXISTS"

    // $ANTLR start "ASC"
    public final void mASC() throws RecognitionException {
        try {
            int _type = ASC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1730:4: ( A S C )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1730:6: A S C
            {
            mA(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASC"

    // $ANTLR start "DESC"
    public final void mDESC() throws RecognitionException {
        try {
            int _type = DESC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1731:5: ( D E S C )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1731:7: D E S C
            {
            mD(); 
            mE(); 
            mS(); 
            mC(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DESC"

    // $ANTLR start "AVG"
    public final void mAVG() throws RecognitionException {
        try {
            int _type = AVG;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1734:4: ( A V G )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1734:6: A V G
            {
            mA(); 
            mV(); 
            mG(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "AVG"

    // $ANTLR start "SUM"
    public final void mSUM() throws RecognitionException {
        try {
            int _type = SUM;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1735:4: ( S U M )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1735:6: S U M
            {
            mS(); 
            mU(); 
            mM(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SUM"

    // $ANTLR start "COUNT"
    public final void mCOUNT() throws RecognitionException {
        try {
            int _type = COUNT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1736:6: ( C O U N T )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1736:8: C O U N T
            {
            mC(); 
            mO(); 
            mU(); 
            mN(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COUNT"

    // $ANTLR start "MAX"
    public final void mMAX() throws RecognitionException {
        try {
            int _type = MAX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1737:4: ( M A X )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1737:6: M A X
            {
            mM(); 
            mA(); 
            mX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MAX"

    // $ANTLR start "MIN"
    public final void mMIN() throws RecognitionException {
        try {
            int _type = MIN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1738:4: ( M I N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1738:6: M I N
            {
            mM(); 
            mI(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MIN"

    // $ANTLR start "VAR"
    public final void mVAR() throws RecognitionException {
        try {
            int _type = VAR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1739:4: ( V A R )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1739:6: V A R
            {
            mV(); 
            mA(); 
            mR(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VAR"

    // $ANTLR start "STDEV"
    public final void mSTDEV() throws RecognitionException {
        try {
            int _type = STDEV;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1740:6: ( S T D E V )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1740:8: S T D E V
            {
            mS(); 
            mT(); 
            mD(); 
            mE(); 
            mV(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STDEV"

    // $ANTLR start "VECTOR"
    public final void mVECTOR() throws RecognitionException {
        try {
            int _type = VECTOR;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1741:7: ( V E C T O R I Z E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1741:9: V E C T O R I Z E
            {
            mV(); 
            mE(); 
            mC(); 
            mT(); 
            mO(); 
            mR(); 
            mI(); 
            mZ(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VECTOR"

    // $ANTLR start "ROWMATRIX"
    public final void mROWMATRIX() throws RecognitionException {
        try {
            int _type = ROWMATRIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1742:10: ( R O W M A T R I X )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1742:12: R O W M A T R I X
            {
            mR(); 
            mO(); 
            mW(); 
            mM(); 
            mA(); 
            mT(); 
            mR(); 
            mI(); 
            mX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ROWMATRIX"

    // $ANTLR start "COLMATRIX"
    public final void mCOLMATRIX() throws RecognitionException {
        try {
            int _type = COLMATRIX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1743:10: ( C O L M A T R I X )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1743:12: C O L M A T R I X
            {
            mC(); 
            mO(); 
            mL(); 
            mM(); 
            mA(); 
            mT(); 
            mR(); 
            mI(); 
            mX(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLMATRIX"

    // $ANTLR start "TRUE"
    public final void mTRUE() throws RecognitionException {
        try {
            int _type = TRUE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1745:5: ( T R U E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1745:7: T R U E
            {
            mT(); 
            mR(); 
            mU(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TRUE"

    // $ANTLR start "FALSE"
    public final void mFALSE() throws RecognitionException {
        try {
            int _type = FALSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1746:6: ( F A L S E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1746:8: F A L S E
            {
            mF(); 
            mA(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FALSE"

    // $ANTLR start "CASE"
    public final void mCASE() throws RecognitionException {
        try {
            int _type = CASE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1748:5: ( C A S E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1748:7: C A S E
            {
            mC(); 
            mA(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "CASE"

    // $ANTLR start "WHEN"
    public final void mWHEN() throws RecognitionException {
        try {
            int _type = WHEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1749:5: ( W H E N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1749:7: W H E N
            {
            mW(); 
            mH(); 
            mE(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WHEN"

    // $ANTLR start "ELSE"
    public final void mELSE() throws RecognitionException {
        try {
            int _type = ELSE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1750:5: ( E L S E )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1750:7: E L S E
            {
            mE(); 
            mL(); 
            mS(); 
            mE(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ELSE"

    // $ANTLR start "THEN"
    public final void mTHEN() throws RecognitionException {
        try {
            int _type = THEN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1751:5: ( T H E N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1751:7: T H E N
            {
            mT(); 
            mH(); 
            mE(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "THEN"

    // $ANTLR start "PRIMARY"
    public final void mPRIMARY() throws RecognitionException {
        try {
            int _type = PRIMARY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1753:8: ( P R I M A R Y )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1753:10: P R I M A R Y
            {
            mP(); 
            mR(); 
            mI(); 
            mM(); 
            mA(); 
            mR(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PRIMARY"

    // $ANTLR start "FOREIGN"
    public final void mFOREIGN() throws RecognitionException {
        try {
            int _type = FOREIGN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1754:8: ( F O R E I G N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1754:10: F O R E I G N
            {
            mF(); 
            mO(); 
            mR(); 
            mE(); 
            mI(); 
            mG(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FOREIGN"

    // $ANTLR start "KEY"
    public final void mKEY() throws RecognitionException {
        try {
            int _type = KEY;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1755:4: ( K E Y )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1755:6: K E Y
            {
            mK(); 
            mE(); 
            mY(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "KEY"

    // $ANTLR start "REFERENCES"
    public final void mREFERENCES() throws RecognitionException {
        try {
            int _type = REFERENCES;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1756:11: ( R E F E R E N C E S )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1756:13: R E F E R E N C E S
            {
            mR(); 
            mE(); 
            mF(); 
            mE(); 
            mR(); 
            mE(); 
            mN(); 
            mC(); 
            mE(); 
            mS(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "REFERENCES"

    // $ANTLR start "OUT"
    public final void mOUT() throws RecognitionException {
        try {
            int _type = OUT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1757:4: ( O U T )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1757:6: O U T
            {
            mO(); 
            mU(); 
            mT(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "OUT"

    // $ANTLR start "DROP"
    public final void mDROP() throws RecognitionException {
        try {
            int _type = DROP;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1759:5: ( D R O P )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1759:7: D R O P
            {
            mD(); 
            mR(); 
            mO(); 
            mP(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DROP"

    // $ANTLR start "VGFUNCTION"
    public final void mVGFUNCTION() throws RecognitionException {
        try {
            int _type = VGFUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1760:11: ( V G F U N C T I O N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1760:13: V G F U N C T I O N
            {
            mV(); 
            mG(); 
            mF(); 
            mU(); 
            mN(); 
            mC(); 
            mT(); 
            mI(); 
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "VGFUNCTION"

    // $ANTLR start "FUNCTION"
    public final void mFUNCTION() throws RecognitionException {
        try {
            int _type = FUNCTION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1761:9: ( F U N C T I O N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1761:11: F U N C T I O N
            {
            mF(); 
            mU(); 
            mN(); 
            mC(); 
            mT(); 
            mI(); 
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "FUNCTION"

    // $ANTLR start "GENERALTABLEINDEX"
    public final void mGENERALTABLEINDEX() throws RecognitionException {
        try {
            int _type = GENERALTABLEINDEX;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1763:18: ( I )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1763:20: I
            {
            mI(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GENERALTABLEINDEX"

    // $ANTLR start "UNION"
    public final void mUNION() throws RecognitionException {
        try {
            int _type = UNION;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1764:6: ( U N I O N )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1764:8: U N I O N
            {
            mU(); 
            mN(); 
            mI(); 
            mO(); 
            mN(); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "UNION"

    // $ANTLR start "ASTERISK"
    public final void mASTERISK() throws RecognitionException {
        try {
            int _type = ASTERISK;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1767:10: ( '*' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1767:12: '*'
            {
            match('*'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "ASTERISK"

    // $ANTLR start "SEMICOLON"
    public final void mSEMICOLON() throws RecognitionException {
        try {
            int _type = SEMICOLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1768:10: ( ';' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1768:12: ';'
            {
            match(';'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SEMICOLON"

    // $ANTLR start "COMMA"
    public final void mCOMMA() throws RecognitionException {
        try {
            int _type = COMMA;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1769:6: ( ',' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1769:8: ','
            {
            match(','); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COMMA"

    // $ANTLR start "TWODOT"
    public final void mTWODOT() throws RecognitionException {
        try {
            int _type = TWODOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1770:7: ( ',..,' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1770:8: ',..,'
            {
            match(",..,"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "TWODOT"

    // $ANTLR start "DOT"
    public final void mDOT() throws RecognitionException {
        try {
            int _type = DOT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1771:4: ( '.' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1771:6: '.'
            {
            match('.'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "DOT"

    // $ANTLR start "COLON"
    public final void mCOLON() throws RecognitionException {
        try {
            int _type = COLON;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1772:6: ( ':' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1772:8: ':'
            {
            match(':'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "COLON"

    // $ANTLR start "PLUS"
    public final void mPLUS() throws RecognitionException {
        try {
            int _type = PLUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1773:5: ( '+' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1773:7: '+'
            {
            match('+'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "PLUS"

    // $ANTLR start "MINUS"
    public final void mMINUS() throws RecognitionException {
        try {
            int _type = MINUS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1774:6: ( '-' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1774:8: '-'
            {
            match('-'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "MINUS"

    // $ANTLR start "SLASH"
    public final void mSLASH() throws RecognitionException {
        try {
            int _type = SLASH;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1775:6: ( '/' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1775:8: '/'
            {
            match('/'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "SLASH"

    // $ANTLR start "LPAREN"
    public final void mLPAREN() throws RecognitionException {
        try {
            int _type = LPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1776:7: ( '(' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1776:9: '('
            {
            match('('); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LPAREN"

    // $ANTLR start "RPAREN"
    public final void mRPAREN() throws RecognitionException {
        try {
            int _type = RPAREN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1777:7: ( ')' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1777:9: ')'
            {
            match(')'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RPAREN"

    // $ANTLR start "LBRACE"
    public final void mLBRACE() throws RecognitionException {
        try {
            int _type = LBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1778:7: ( '{' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1778:9: '{'
            {
            match('{'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACE"

    // $ANTLR start "RBRACE"
    public final void mRBRACE() throws RecognitionException {
        try {
            int _type = RBRACE;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1779:7: ( '}' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1779:9: '}'
            {
            match('}'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACE"

    // $ANTLR start "LBRACKET"
    public final void mLBRACKET() throws RecognitionException {
        try {
            int _type = LBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1780:9: ( '[' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1780:11: '['
            {
            match('['); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LBRACKET"

    // $ANTLR start "RBRACKET"
    public final void mRBRACKET() throws RecognitionException {
        try {
            int _type = RBRACKET;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1781:9: ( ']' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1781:11: ']'
            {
            match(']'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "RBRACKET"

    // $ANTLR start "EQUALS"
    public final void mEQUALS() throws RecognitionException {
        try {
            int _type = EQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1782:7: ( '=' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1782:9: '='
            {
            match('='); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "EQUALS"

    // $ANTLR start "NOTEQUALS"
    public final void mNOTEQUALS() throws RecognitionException {
        try {
            int _type = NOTEQUALS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1783:10: ( '<>' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1783:12: '<>'
            {
            match("<>"); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "NOTEQUALS"

    // $ANTLR start "LESSTHAN"
    public final void mLESSTHAN() throws RecognitionException {
        try {
            int _type = LESSTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1784:9: ( '<' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1784:11: '<'
            {
            match('<'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSTHAN"

    // $ANTLR start "GREATERTHAN"
    public final void mGREATERTHAN() throws RecognitionException {
        try {
            int _type = GREATERTHAN;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1785:12: ( '>' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1785:14: '>'
            {
            match('>'); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATERTHAN"

    // $ANTLR start "LESSEQUAL"
    public final void mLESSEQUAL() throws RecognitionException {
        try {
            int _type = LESSEQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1786:10: ( '<=' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1786:12: '<='
            {
            match("<="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "LESSEQUAL"

    // $ANTLR start "GREATEREQUAL"
    public final void mGREATEREQUAL() throws RecognitionException {
        try {
            int _type = GREATEREQUAL;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1787:13: ( '>=' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1787:15: '>='
            {
            match(">="); 


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "GREATEREQUAL"

    // $ANTLR start "IDENTIFIER"
    public final void mIDENTIFIER() throws RecognitionException {
        try {
            int _type = IDENTIFIER;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1788:13: ( ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )* )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1788:15: ( 'a' .. 'z' | 'A' .. 'Z' | '_' ) ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            {
            if ( (input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
                input.consume();

            }
            else {
                MismatchedSetException mse = new MismatchedSetException(null,input);
                recover(mse);
                throw mse;}

            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1788:39: ( 'a' .. 'z' | 'A' .. 'Z' | '_' | '0' .. '9' )*
            loop1:
            do {
                int alt1=2;
                int LA1_0 = input.LA(1);

                if ( ((LA1_0>='0' && LA1_0<='9')||(LA1_0>='A' && LA1_0<='Z')||LA1_0=='_'||(LA1_0>='a' && LA1_0<='z')) ) {
                    alt1=1;
                }


                switch (alt1) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:
            	    {
            	    if ( (input.LA(1)>='0' && input.LA(1)<='9')||(input.LA(1)>='A' && input.LA(1)<='Z')||input.LA(1)=='_'||(input.LA(1)>='a' && input.LA(1)<='z') ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    break loop1;
                }
            } while (true);


            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "IDENTIFIER"

    // $ANTLR start "STRING"
    public final void mSTRING() throws RecognitionException {
        try {
            int _type = STRING;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:7: ( '\\'' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | ( '\\\\\\'' ) | '.' | '-' | '_' | ' ' | '%' | '#' )+ '\\'' )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:9: '\\'' ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | ( '\\\\\\'' ) | '.' | '-' | '_' | ' ' | '%' | '#' )+ '\\''
            {
            match('\''); 
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:14: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | ( '\\\\\\'' ) | '.' | '-' | '_' | ' ' | '%' | '#' )+
            int cnt2=0;
            loop2:
            do {
                int alt2=16;
                alt2 = dfa2.predict(input);
                switch (alt2) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:15: 'a' .. 'z'
            	    {
            	    matchRange('a','z'); 

            	    }
            	    break;
            	case 2 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:24: 'A' .. 'Z'
            	    {
            	    matchRange('A','Z'); 

            	    }
            	    break;
            	case 3 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:33: '0' .. '9'
            	    {
            	    matchRange('0','9'); 

            	    }
            	    break;
            	case 4 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:42: '/'
            	    {
            	    match('/'); 

            	    }
            	    break;
            	case 5 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:46: '('
            	    {
            	    match('('); 

            	    }
            	    break;
            	case 6 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:50: ')'
            	    {
            	    match(')'); 

            	    }
            	    break;
            	case 7 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:54: ':'
            	    {
            	    match(':'); 

            	    }
            	    break;
            	case 8 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:58: '\\\\'
            	    {
            	    match('\\'); 

            	    }
            	    break;
            	case 9 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:63: ( '\\\\\\'' )
            	    {
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:63: ( '\\\\\\'' )
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:64: '\\\\\\''
            	    {
            	    match("\\'"); 


            	    }


            	    }
            	    break;
            	case 10 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:72: '.'
            	    {
            	    match('.'); 

            	    }
            	    break;
            	case 11 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:76: '-'
            	    {
            	    match('-'); 

            	    }
            	    break;
            	case 12 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:80: '_'
            	    {
            	    match('_'); 

            	    }
            	    break;
            	case 13 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:84: ' '
            	    {
            	    match(' '); 

            	    }
            	    break;
            	case 14 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:88: '%'
            	    {
            	    match('%'); 

            	    }
            	    break;
            	case 15 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1789:92: '#'
            	    {
            	    match('#'); 

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

            match('\''); 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "STRING"

    // $ANTLR start "NUMERIC"
    public final void mNUMERIC() throws RecognitionException {
        try {
            int _type = NUMERIC;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:8: ( ( ( ( '1' .. '9' ) ( '0' .. '9' )* ) | '0' ) ( | ( '.' ( ( '0' .. '9' )+ ) ) ) )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:10: ( ( ( '1' .. '9' ) ( '0' .. '9' )* ) | '0' ) ( | ( '.' ( ( '0' .. '9' )+ ) ) )
            {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:10: ( ( ( '1' .. '9' ) ( '0' .. '9' )* ) | '0' )
            int alt4=2;
            int LA4_0 = input.LA(1);

            if ( ((LA4_0>='1' && LA4_0<='9')) ) {
                alt4=1;
            }
            else if ( (LA4_0=='0') ) {
                alt4=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 4, 0, input);

                throw nvae;
            }
            switch (alt4) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:11: ( ( '1' .. '9' ) ( '0' .. '9' )* )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:11: ( ( '1' .. '9' ) ( '0' .. '9' )* )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:12: ( '1' .. '9' ) ( '0' .. '9' )*
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:12: ( '1' .. '9' )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:13: '1' .. '9'
                    {
                    matchRange('1','9'); 

                    }

                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:22: ( '0' .. '9' )*
                    loop3:
                    do {
                        int alt3=2;
                        int LA3_0 = input.LA(1);

                        if ( ((LA3_0>='0' && LA3_0<='9')) ) {
                            alt3=1;
                        }


                        switch (alt3) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:23: '0' .. '9'
                    	    {
                    	    matchRange('0','9'); 

                    	    }
                    	    break;

                    	default :
                    	    break loop3;
                        }
                    } while (true);


                    }


                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:35: '0'
                    {
                    match('0'); 

                    }
                    break;

            }

            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:39: ( | ( '.' ( ( '0' .. '9' )+ ) ) )
            int alt6=2;
            int LA6_0 = input.LA(1);

            if ( (LA6_0=='.') ) {
                alt6=2;
            }
            else {
                alt6=1;}
            switch (alt6) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:40: 
                    {
                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:41: ( '.' ( ( '0' .. '9' )+ ) )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:41: ( '.' ( ( '0' .. '9' )+ ) )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:42: '.' ( ( '0' .. '9' )+ )
                    {
                    match('.'); 
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:45: ( ( '0' .. '9' )+ )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:46: ( '0' .. '9' )+
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:46: ( '0' .. '9' )+
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
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1791:47: '0' .. '9'
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
    // $ANTLR end "NUMERIC"

    // $ANTLR start "WS"
    public final void mWS() throws RecognitionException {
        try {
            int _type = WS;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1795:4: ( ( ' ' | '\\t' | '\\n' | '\\r' | '\\u000C' )+ )
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1795:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\u000C' )+
            {
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1795:6: ( ' ' | '\\t' | '\\n' | '\\r' | '\\u000C' )+
            int cnt7=0;
            loop7:
            do {
                int alt7=2;
                int LA7_0 = input.LA(1);

                if ( ((LA7_0>='\t' && LA7_0<='\n')||(LA7_0>='\f' && LA7_0<='\r')||LA7_0==' ') ) {
                    alt7=1;
                }


                switch (alt7) {
            	case 1 :
            	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:
            	    {
            	    if ( (input.LA(1)>='\t' && input.LA(1)<='\n')||(input.LA(1)>='\f' && input.LA(1)<='\r')||input.LA(1)==' ' ) {
            	        input.consume();

            	    }
            	    else {
            	        MismatchedSetException mse = new MismatchedSetException(null,input);
            	        recover(mse);
            	        throw mse;}


            	    }
            	    break;

            	default :
            	    if ( cnt7 >= 1 ) break loop7;
                        EarlyExitException eee =
                            new EarlyExitException(7, input);
                        throw eee;
                }
                cnt7++;
            } while (true);

             _channel = HIDDEN; 

            }

            state.type = _type;
            state.channel = _channel;
        }
        finally {
        }
    }
    // $ANTLR end "WS"

    // $ANTLR start "COMMENT"
    public final void mCOMMENT() throws RecognitionException {
        try {
            int _type = COMMENT;
            int _channel = DEFAULT_TOKEN_CHANNEL;
            // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:9: ( ( '--' ( . )* '\\n' ) | ( '/*' ) ( . )* ( '*/' ) )
            int alt10=2;
            int LA10_0 = input.LA(1);

            if ( (LA10_0=='-') ) {
                alt10=1;
            }
            else if ( (LA10_0=='/') ) {
                alt10=2;
            }
            else {
                NoViableAltException nvae =
                    new NoViableAltException("", 10, 0, input);

                throw nvae;
            }
            switch (alt10) {
                case 1 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:11: ( '--' ( . )* '\\n' )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:11: ( '--' ( . )* '\\n' )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:12: '--' ( . )* '\\n'
                    {
                    match("--"); 

                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:17: ( . )*
                    loop8:
                    do {
                        int alt8=2;
                        int LA8_0 = input.LA(1);

                        if ( (LA8_0=='\n') ) {
                            alt8=2;
                        }
                        else if ( ((LA8_0>='\u0000' && LA8_0<='\t')||(LA8_0>='\u000B' && LA8_0<='\uFFFF')) ) {
                            alt8=1;
                        }


                        switch (alt8) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1800:17: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop8;
                        }
                    } while (true);

                    match('\n'); 

                    }

                     _channel = HIDDEN; 

                    }
                    break;
                case 2 :
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:11: ( '/*' ) ( . )* ( '*/' )
                    {
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:11: ( '/*' )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:12: '/*'
                    {
                    match("/*"); 


                    }

                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:17: ( . )*
                    loop9:
                    do {
                        int alt9=2;
                        int LA9_0 = input.LA(1);

                        if ( (LA9_0=='*') ) {
                            int LA9_1 = input.LA(2);

                            if ( (LA9_1=='/') ) {
                                alt9=2;
                            }
                            else if ( ((LA9_1>='\u0000' && LA9_1<='.')||(LA9_1>='0' && LA9_1<='\uFFFF')) ) {
                                alt9=1;
                            }


                        }
                        else if ( ((LA9_0>='\u0000' && LA9_0<=')')||(LA9_0>='+' && LA9_0<='\uFFFF')) ) {
                            alt9=1;
                        }


                        switch (alt9) {
                    	case 1 :
                    	    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:17: .
                    	    {
                    	    matchAny(); 

                    	    }
                    	    break;

                    	default :
                    	    break loop9;
                        }
                    } while (true);

                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:20: ( '*/' )
                    // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1802:21: '*/'
                    {
                    match("*/"); 


                    }

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
    // $ANTLR end "COMMENT"

    public void mTokens() throws RecognitionException {
        // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:8: ( SELECT | CREATE | TABLE | VIEW | MATERIALIZED | FROM | WHERE | GROUP | ORDER | BY | HAVING | RANDOM | SOURCE | RETURNS | FOR | EACH | WITH | ALL | DISTINCT | AS | IN | VALUES | AND | OR | NOT | LIKE | BETWEEN | EXISTS | ASC | DESC | AVG | SUM | COUNT | MAX | MIN | VAR | STDEV | VECTOR | ROWMATRIX | COLMATRIX | TRUE | FALSE | CASE | WHEN | ELSE | THEN | PRIMARY | FOREIGN | KEY | REFERENCES | OUT | DROP | VGFUNCTION | FUNCTION | GENERALTABLEINDEX | UNION | ASTERISK | SEMICOLON | COMMA | TWODOT | DOT | COLON | PLUS | MINUS | SLASH | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSEQUAL | GREATEREQUAL | IDENTIFIER | STRING | NUMERIC | WS | COMMENT )
        int alt11=82;
        alt11 = dfa11.predict(input);
        switch (alt11) {
            case 1 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:10: SELECT
                {
                mSELECT(); 

                }
                break;
            case 2 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:17: CREATE
                {
                mCREATE(); 

                }
                break;
            case 3 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:24: TABLE
                {
                mTABLE(); 

                }
                break;
            case 4 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:30: VIEW
                {
                mVIEW(); 

                }
                break;
            case 5 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:35: MATERIALIZED
                {
                mMATERIALIZED(); 

                }
                break;
            case 6 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:48: FROM
                {
                mFROM(); 

                }
                break;
            case 7 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:53: WHERE
                {
                mWHERE(); 

                }
                break;
            case 8 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:59: GROUP
                {
                mGROUP(); 

                }
                break;
            case 9 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:65: ORDER
                {
                mORDER(); 

                }
                break;
            case 10 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:71: BY
                {
                mBY(); 

                }
                break;
            case 11 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:74: HAVING
                {
                mHAVING(); 

                }
                break;
            case 12 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:81: RANDOM
                {
                mRANDOM(); 

                }
                break;
            case 13 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:88: SOURCE
                {
                mSOURCE(); 

                }
                break;
            case 14 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:95: RETURNS
                {
                mRETURNS(); 

                }
                break;
            case 15 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:103: FOR
                {
                mFOR(); 

                }
                break;
            case 16 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:107: EACH
                {
                mEACH(); 

                }
                break;
            case 17 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:112: WITH
                {
                mWITH(); 

                }
                break;
            case 18 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:117: ALL
                {
                mALL(); 

                }
                break;
            case 19 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:121: DISTINCT
                {
                mDISTINCT(); 

                }
                break;
            case 20 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:130: AS
                {
                mAS(); 

                }
                break;
            case 21 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:133: IN
                {
                mIN(); 

                }
                break;
            case 22 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:136: VALUES
                {
                mVALUES(); 

                }
                break;
            case 23 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:143: AND
                {
                mAND(); 

                }
                break;
            case 24 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:147: OR
                {
                mOR(); 

                }
                break;
            case 25 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:150: NOT
                {
                mNOT(); 

                }
                break;
            case 26 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:154: LIKE
                {
                mLIKE(); 

                }
                break;
            case 27 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:159: BETWEEN
                {
                mBETWEEN(); 

                }
                break;
            case 28 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:167: EXISTS
                {
                mEXISTS(); 

                }
                break;
            case 29 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:174: ASC
                {
                mASC(); 

                }
                break;
            case 30 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:178: DESC
                {
                mDESC(); 

                }
                break;
            case 31 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:183: AVG
                {
                mAVG(); 

                }
                break;
            case 32 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:187: SUM
                {
                mSUM(); 

                }
                break;
            case 33 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:191: COUNT
                {
                mCOUNT(); 

                }
                break;
            case 34 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:197: MAX
                {
                mMAX(); 

                }
                break;
            case 35 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:201: MIN
                {
                mMIN(); 

                }
                break;
            case 36 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:205: VAR
                {
                mVAR(); 

                }
                break;
            case 37 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:209: STDEV
                {
                mSTDEV(); 

                }
                break;
            case 38 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:215: VECTOR
                {
                mVECTOR(); 

                }
                break;
            case 39 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:222: ROWMATRIX
                {
                mROWMATRIX(); 

                }
                break;
            case 40 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:232: COLMATRIX
                {
                mCOLMATRIX(); 

                }
                break;
            case 41 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:242: TRUE
                {
                mTRUE(); 

                }
                break;
            case 42 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:247: FALSE
                {
                mFALSE(); 

                }
                break;
            case 43 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:253: CASE
                {
                mCASE(); 

                }
                break;
            case 44 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:258: WHEN
                {
                mWHEN(); 

                }
                break;
            case 45 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:263: ELSE
                {
                mELSE(); 

                }
                break;
            case 46 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:268: THEN
                {
                mTHEN(); 

                }
                break;
            case 47 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:273: PRIMARY
                {
                mPRIMARY(); 

                }
                break;
            case 48 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:281: FOREIGN
                {
                mFOREIGN(); 

                }
                break;
            case 49 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:289: KEY
                {
                mKEY(); 

                }
                break;
            case 50 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:293: REFERENCES
                {
                mREFERENCES(); 

                }
                break;
            case 51 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:304: OUT
                {
                mOUT(); 

                }
                break;
            case 52 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:308: DROP
                {
                mDROP(); 

                }
                break;
            case 53 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:313: VGFUNCTION
                {
                mVGFUNCTION(); 

                }
                break;
            case 54 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:324: FUNCTION
                {
                mFUNCTION(); 

                }
                break;
            case 55 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:333: GENERALTABLEINDEX
                {
                mGENERALTABLEINDEX(); 

                }
                break;
            case 56 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:351: UNION
                {
                mUNION(); 

                }
                break;
            case 57 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:357: ASTERISK
                {
                mASTERISK(); 

                }
                break;
            case 58 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:366: SEMICOLON
                {
                mSEMICOLON(); 

                }
                break;
            case 59 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:376: COMMA
                {
                mCOMMA(); 

                }
                break;
            case 60 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:382: TWODOT
                {
                mTWODOT(); 

                }
                break;
            case 61 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:389: DOT
                {
                mDOT(); 

                }
                break;
            case 62 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:393: COLON
                {
                mCOLON(); 

                }
                break;
            case 63 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:399: PLUS
                {
                mPLUS(); 

                }
                break;
            case 64 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:404: MINUS
                {
                mMINUS(); 

                }
                break;
            case 65 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:410: SLASH
                {
                mSLASH(); 

                }
                break;
            case 66 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:416: LPAREN
                {
                mLPAREN(); 

                }
                break;
            case 67 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:423: RPAREN
                {
                mRPAREN(); 

                }
                break;
            case 68 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:430: LBRACE
                {
                mLBRACE(); 

                }
                break;
            case 69 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:437: RBRACE
                {
                mRBRACE(); 

                }
                break;
            case 70 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:444: LBRACKET
                {
                mLBRACKET(); 

                }
                break;
            case 71 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:453: RBRACKET
                {
                mRBRACKET(); 

                }
                break;
            case 72 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:462: EQUALS
                {
                mEQUALS(); 

                }
                break;
            case 73 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:469: NOTEQUALS
                {
                mNOTEQUALS(); 

                }
                break;
            case 74 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:479: LESSTHAN
                {
                mLESSTHAN(); 

                }
                break;
            case 75 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:488: GREATERTHAN
                {
                mGREATERTHAN(); 

                }
                break;
            case 76 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:500: LESSEQUAL
                {
                mLESSEQUAL(); 

                }
                break;
            case 77 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:510: GREATEREQUAL
                {
                mGREATEREQUAL(); 

                }
                break;
            case 78 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:523: IDENTIFIER
                {
                mIDENTIFIER(); 

                }
                break;
            case 79 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:534: STRING
                {
                mSTRING(); 

                }
                break;
            case 80 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:541: NUMERIC
                {
                mNUMERIC(); 

                }
                break;
            case 81 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:549: WS
                {
                mWS(); 

                }
                break;
            case 82 :
                // /home/dimitrije/git/v0.5/src/compiler/parser/Query.g:1:552: COMMENT
                {
                mCOMMENT(); 

                }
                break;

        }

    }


    protected DFA2 dfa2 = new DFA2(this);
    protected DFA11 dfa11 = new DFA11(this);
    static final String DFA2_eotS =
        "\20\uffff\1\21\2\uffff";
    static final String DFA2_eofS =
        "\23\uffff";
    static final String DFA2_minS =
        "\1\40\10\uffff\1\40\6\uffff\1\40\2\uffff";
    static final String DFA2_maxS =
        "\1\172\10\uffff\1\172\6\uffff\1\172\2\uffff";
    static final String DFA2_acceptS =
        "\1\uffff\1\20\1\1\1\2\1\3\1\4\1\5\1\6\1\7\1\uffff\1\12\1\13\1\14"+
        "\1\15\1\16\1\17\1\uffff\1\10\1\11";
    static final String DFA2_specialS =
        "\23\uffff}>";
    static final String[] DFA2_transitionS = {
            "\1\15\2\uffff\1\17\1\uffff\1\16\1\uffff\1\1\1\6\1\7\3\uffff"+
            "\1\13\1\12\1\5\12\4\1\10\6\uffff\32\3\1\uffff\1\11\2\uffff\1"+
            "\14\1\uffff\32\2",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\21\2\uffff\1\21\1\uffff\1\21\1\uffff\1\20\2\21\3\uffff\16"+
            "\21\6\uffff\32\21\1\uffff\1\21\2\uffff\1\21\1\uffff\32\21",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\22\2\uffff\1\22\1\uffff\1\22\1\uffff\3\22\3\uffff\16\22"+
            "\6\uffff\32\22\1\uffff\1\22\2\uffff\1\22\1\uffff\32\22",
            "",
            ""
    };

    static final short[] DFA2_eot = DFA.unpackEncodedString(DFA2_eotS);
    static final short[] DFA2_eof = DFA.unpackEncodedString(DFA2_eofS);
    static final char[] DFA2_min = DFA.unpackEncodedStringToUnsignedChars(DFA2_minS);
    static final char[] DFA2_max = DFA.unpackEncodedStringToUnsignedChars(DFA2_maxS);
    static final short[] DFA2_accept = DFA.unpackEncodedString(DFA2_acceptS);
    static final short[] DFA2_special = DFA.unpackEncodedString(DFA2_specialS);
    static final short[][] DFA2_transition;

    static {
        int numStates = DFA2_transitionS.length;
        DFA2_transition = new short[numStates][];
        for (int i=0; i<numStates; i++) {
            DFA2_transition[i] = DFA.unpackEncodedString(DFA2_transitionS[i]);
        }
    }

    class DFA2 extends DFA {

        public DFA2(BaseRecognizer recognizer) {
            this.recognizer = recognizer;
            this.decisionNumber = 2;
            this.eot = DFA2_eot;
            this.eof = DFA2_eof;
            this.min = DFA2_min;
            this.max = DFA2_max;
            this.accept = DFA2_accept;
            this.special = DFA2_special;
            this.transition = DFA2_transition;
        }
        public String getDescription() {
            return "()+ loopback of 1789:14: ( 'a' .. 'z' | 'A' .. 'Z' | '0' .. '9' | '/' | '(' | ')' | ':' | '\\\\' | ( '\\\\\\'' ) | '.' | '-' | '_' | ' ' | '%' | '#' )+";
        }
    }
    static final String DFA11_eotS =
        "\1\uffff\17\47\1\125\5\47\2\uffff\1\134\3\uffff\1\136\1\137\7\uffff"+
        "\1\142\1\144\4\uffff\27\47\1\177\1\47\1\u0082\12\47\1\u008e\4\47"+
        "\1\u0094\1\uffff\5\47\12\uffff\1\u009a\13\47\1\u00a6\3\47\1\u00aa"+
        "\1\47\1\u00ac\1\47\1\u00ae\5\47\1\uffff\1\47\1\u00b7\1\uffff\11"+
        "\47\1\u00c1\1\u00c2\1\uffff\1\u00c3\1\u00c4\3\47\1\uffff\1\u00c8"+
        "\2\47\1\u00cb\1\47\1\uffff\5\47\1\u00d2\1\47\1\u00d4\1\47\1\u00d6"+
        "\1\47\1\uffff\2\47\1\u00da\1\uffff\1\47\1\uffff\1\u00dc\1\uffff"+
        "\4\47\1\u00e1\1\u00e2\2\47\1\uffff\6\47\1\u00eb\1\47\1\u00ed\4\uffff"+
        "\1\u00ee\1\u00ef\1\47\1\uffff\1\u00f1\1\47\1\uffff\3\47\1\u00f6"+
        "\1\u00f7\1\47\1\uffff\1\47\1\uffff\1\u00fa\1\uffff\3\47\1\uffff"+
        "\1\47\1\uffff\1\47\1\u0100\1\47\1\u0102\2\uffff\1\u0103\1\u0104"+
        "\6\47\1\uffff\1\47\3\uffff\1\47\1\uffff\1\47\1\u010e\1\u010f\1\u0110"+
        "\2\uffff\1\47\1\u0112\1\uffff\1\u0113\4\47\1\uffff\1\47\3\uffff"+
        "\1\47\1\u011a\3\47\1\u011e\1\u011f\2\47\3\uffff\1\47\2\uffff\3\47"+
        "\1\u0126\1\47\1\u0128\1\uffff\1\47\1\u012a\1\47\2\uffff\1\47\1\u012d"+
        "\4\47\1\uffff\1\u0132\1\uffff\1\47\1\uffff\1\47\1\u0135\1\uffff"+
        "\1\u0136\1\47\1\u0138\1\47\1\uffff\1\u013a\1\47\2\uffff\1\u013c"+
        "\1\uffff\1\47\1\uffff\1\u013e\1\uffff\1\47\1\uffff\1\u0140\1\uffff";
    static final String DFA11_eofS =
        "\u0141\uffff";
    static final String DFA11_minS =
        "\1\11\1\105\5\101\1\110\2\122\1\105\3\101\1\114\1\105\1\60\1\117"+
        "\1\111\1\122\1\105\1\116\2\uffff\1\56\3\uffff\1\55\1\52\7\uffff"+
        "\2\75\4\uffff\1\115\1\114\1\125\1\104\1\114\1\123\1\105\1\125\1"+
        "\102\1\105\1\114\1\106\1\103\1\105\1\116\1\124\1\117\1\122\1\114"+
        "\1\116\1\105\1\124\1\117\1\60\1\124\1\60\1\124\1\126\1\127\1\106"+
        "\1\116\1\103\1\111\1\123\1\107\1\104\1\60\1\114\1\117\2\123\1\60"+
        "\1\uffff\1\124\1\113\1\111\1\131\1\111\12\uffff\1\60\1\105\1\122"+
        "\1\105\1\116\1\115\1\105\1\101\1\105\1\114\1\116\1\125\1\60\1\125"+
        "\1\124\1\127\1\60\1\105\1\60\1\115\1\60\1\123\1\103\1\116\1\110"+
        "\1\125\1\uffff\1\105\1\60\1\uffff\1\127\1\111\1\115\1\125\1\105"+
        "\1\104\1\110\1\123\1\105\2\60\1\uffff\2\60\1\120\1\103\1\124\1\uffff"+
        "\1\60\1\105\1\115\1\60\1\117\1\uffff\2\103\1\126\1\124\1\101\1\60"+
        "\1\124\1\60\1\105\1\60\1\105\1\uffff\1\116\1\117\1\60\1\uffff\1"+
        "\122\1\uffff\1\60\1\uffff\1\111\1\105\1\124\1\105\2\60\1\120\1\122"+
        "\1\uffff\1\105\1\116\1\101\2\122\1\117\1\60\1\124\1\60\4\uffff\2"+
        "\60\1\111\1\uffff\1\60\1\101\1\uffff\1\116\1\124\1\105\2\60\1\124"+
        "\1\uffff\1\105\1\uffff\1\60\1\uffff\1\123\1\103\1\122\1\uffff\1"+
        "\111\1\uffff\1\107\1\60\1\111\1\60\2\uffff\2\60\1\105\1\107\1\124"+
        "\1\116\1\105\1\115\1\uffff\1\123\3\uffff\1\116\1\uffff\1\122\3\60"+
        "\2\uffff\1\122\1\60\1\uffff\1\60\1\124\1\111\1\101\1\116\1\uffff"+
        "\1\117\3\uffff\1\116\1\60\1\122\1\123\1\116\2\60\1\103\1\131\3\uffff"+
        "\1\111\2\uffff\1\111\1\132\1\114\1\60\1\116\1\60\1\uffff\1\111\1"+
        "\60\1\103\2\uffff\1\124\1\60\1\130\1\117\1\105\1\111\1\uffff\1\60"+
        "\1\uffff\1\130\1\uffff\1\105\1\60\1\uffff\1\60\1\116\1\60\1\132"+
        "\1\uffff\1\60\1\123\2\uffff\1\60\1\uffff\1\105\1\uffff\1\60\1\uffff"+
        "\1\104\1\uffff\1\60\1\uffff";
    static final String DFA11_maxS =
        "\1\175\1\165\2\162\2\151\1\165\1\151\1\162\1\165\1\171\1\141\1\157"+
        "\1\170\1\166\1\162\1\172\1\157\1\151\1\162\1\145\1\156\2\uffff\1"+
        "\56\3\uffff\1\55\1\52\7\uffff\1\76\1\75\4\uffff\1\155\1\154\1\165"+
        "\1\144\1\165\1\163\1\145\1\165\1\142\1\145\1\162\1\146\1\143\1\145"+
        "\1\156\1\170\1\157\1\162\1\154\1\156\1\145\1\164\1\157\1\172\1\164"+
        "\1\172\1\164\1\166\1\167\1\164\1\156\1\143\1\151\1\163\1\147\1\144"+
        "\1\172\1\154\1\157\2\163\1\172\1\uffff\1\164\1\153\1\151\1\171\1"+
        "\151\12\uffff\1\172\1\145\1\162\1\145\1\156\1\155\1\145\1\141\1"+
        "\145\1\154\1\156\1\165\1\172\1\165\1\164\1\167\1\172\1\145\1\172"+
        "\1\155\1\172\1\163\1\143\1\162\1\150\1\165\1\uffff\1\145\1\172\1"+
        "\uffff\1\167\1\151\1\155\1\165\1\145\1\144\1\150\1\163\1\145\2\172"+
        "\1\uffff\2\172\1\160\1\143\1\164\1\uffff\1\172\1\145\1\155\1\172"+
        "\1\157\1\uffff\2\143\1\166\1\164\1\141\1\172\1\164\1\172\1\145\1"+
        "\172\1\145\1\uffff\1\156\1\157\1\172\1\uffff\1\162\1\uffff\1\172"+
        "\1\uffff\1\151\1\145\1\164\1\145\2\172\1\160\1\162\1\uffff\1\145"+
        "\1\156\1\141\2\162\1\157\1\172\1\164\1\172\4\uffff\2\172\1\151\1"+
        "\uffff\1\172\1\141\1\uffff\1\156\1\164\1\145\2\172\1\164\1\uffff"+
        "\1\145\1\uffff\1\172\1\uffff\1\163\1\143\1\162\1\uffff\1\151\1\uffff"+
        "\1\147\1\172\1\151\1\172\2\uffff\2\172\1\145\1\147\1\164\1\156\1"+
        "\145\1\155\1\uffff\1\163\3\uffff\1\156\1\uffff\1\162\3\172\2\uffff"+
        "\1\162\1\172\1\uffff\1\172\1\164\1\151\1\141\1\156\1\uffff\1\157"+
        "\3\uffff\1\156\1\172\1\162\1\163\1\156\2\172\1\143\1\171\3\uffff"+
        "\1\151\2\uffff\1\151\1\172\1\154\1\172\1\156\1\172\1\uffff\1\151"+
        "\1\172\1\143\2\uffff\1\164\1\172\1\170\1\157\1\145\1\151\1\uffff"+
        "\1\172\1\uffff\1\170\1\uffff\1\145\1\172\1\uffff\1\172\1\156\2\172"+
        "\1\uffff\1\172\1\163\2\uffff\1\172\1\uffff\1\145\1\uffff\1\172\1"+
        "\uffff\1\144\1\uffff\1\172\1\uffff";
    static final String DFA11_acceptS =
        "\26\uffff\1\71\1\72\1\uffff\1\75\1\76\1\77\2\uffff\1\102\1\103\1"+
        "\104\1\105\1\106\1\107\1\110\2\uffff\1\116\1\117\1\120\1\121\52"+
        "\uffff\1\67\5\uffff\1\74\1\73\1\122\1\100\1\101\1\111\1\114\1\112"+
        "\1\115\1\113\32\uffff\1\30\2\uffff\1\12\13\uffff\1\24\5\uffff\1"+
        "\25\5\uffff\1\40\13\uffff\1\44\3\uffff\1\43\1\uffff\1\42\1\uffff"+
        "\1\17\10\uffff\1\63\11\uffff\1\37\1\27\1\35\1\22\3\uffff\1\31\2"+
        "\uffff\1\61\6\uffff\1\53\1\uffff\1\51\1\uffff\1\56\3\uffff\1\4\1"+
        "\uffff\1\6\4\uffff\1\54\1\21\10\uffff\1\20\1\uffff\1\55\1\64\1\36"+
        "\1\uffff\1\32\4\uffff\1\45\1\41\2\uffff\1\3\5\uffff\1\52\1\uffff"+
        "\1\7\1\10\1\11\11\uffff\1\70\1\1\1\15\1\uffff\1\2\1\26\6\uffff\1"+
        "\13\3\uffff\1\14\1\34\6\uffff\1\60\1\uffff\1\33\1\uffff\1\16\2\uffff"+
        "\1\57\4\uffff\1\66\2\uffff\1\23\1\50\1\uffff\1\46\1\uffff\1\47\1"+
        "\uffff\1\65\1\uffff\1\62\1\uffff\1\5";
    static final String DFA11_specialS =
        "\u0141\uffff}>";
    static final String[] DFA11_transitionS = {
            "\2\52\1\uffff\2\52\22\uffff\1\52\6\uffff\1\50\1\36\1\37\1\26"+
            "\1\33\1\30\1\34\1\31\1\35\12\51\1\32\1\27\1\45\1\44\1\46\2\uffff"+
            "\1\16\1\12\1\2\1\17\1\15\1\6\1\10\1\13\1\20\1\47\1\24\1\22\1"+
            "\5\1\21\1\11\1\23\1\47\1\14\1\1\1\3\1\25\1\4\1\7\3\47\1\42\1"+
            "\uffff\1\43\1\uffff\1\47\1\uffff\1\16\1\12\1\2\1\17\1\15\1\6"+
            "\1\10\1\13\1\20\1\47\1\24\1\22\1\5\1\21\1\11\1\23\1\47\1\14"+
            "\1\1\1\3\1\25\1\4\1\7\3\47\1\40\1\uffff\1\41",
            "\1\54\11\uffff\1\55\4\uffff\1\56\1\53\17\uffff\1\54\11\uffff"+
            "\1\55\4\uffff\1\56\1\53",
            "\1\60\15\uffff\1\57\2\uffff\1\61\16\uffff\1\60\15\uffff\1\57"+
            "\2\uffff\1\61",
            "\1\63\6\uffff\1\64\11\uffff\1\62\16\uffff\1\63\6\uffff\1\64"+
            "\11\uffff\1\62",
            "\1\65\3\uffff\1\67\1\uffff\1\66\1\uffff\1\70\27\uffff\1\65"+
            "\3\uffff\1\67\1\uffff\1\66\1\uffff\1\70",
            "\1\72\7\uffff\1\71\27\uffff\1\72\7\uffff\1\71",
            "\1\75\15\uffff\1\74\2\uffff\1\73\2\uffff\1\76\13\uffff\1\75"+
            "\15\uffff\1\74\2\uffff\1\73\2\uffff\1\76",
            "\1\77\1\100\36\uffff\1\77\1\100",
            "\1\101\37\uffff\1\101",
            "\1\102\2\uffff\1\103\34\uffff\1\102\2\uffff\1\103",
            "\1\105\23\uffff\1\104\13\uffff\1\105\23\uffff\1\104",
            "\1\106\37\uffff\1\106",
            "\1\111\3\uffff\1\110\11\uffff\1\107\21\uffff\1\111\3\uffff"+
            "\1\110\11\uffff\1\107",
            "\1\112\12\uffff\1\114\13\uffff\1\113\10\uffff\1\112\12\uffff"+
            "\1\114\13\uffff\1\113",
            "\1\120\1\uffff\1\116\4\uffff\1\117\2\uffff\1\115\25\uffff\1"+
            "\120\1\uffff\1\116\4\uffff\1\117\2\uffff\1\115",
            "\1\122\3\uffff\1\123\10\uffff\1\121\22\uffff\1\122\3\uffff"+
            "\1\123\10\uffff\1\121",
            "\12\47\7\uffff\15\47\1\124\14\47\4\uffff\1\47\1\uffff\15\47"+
            "\1\124\14\47",
            "\1\126\37\uffff\1\126",
            "\1\127\37\uffff\1\127",
            "\1\130\37\uffff\1\130",
            "\1\131\37\uffff\1\131",
            "\1\132\37\uffff\1\132",
            "",
            "",
            "\1\133",
            "",
            "",
            "",
            "\1\135",
            "\1\135",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\1\141\1\140",
            "\1\143",
            "",
            "",
            "",
            "",
            "\1\145\37\uffff\1\145",
            "\1\146\37\uffff\1\146",
            "\1\147\37\uffff\1\147",
            "\1\150\37\uffff\1\150",
            "\1\152\10\uffff\1\151\26\uffff\1\152\10\uffff\1\151",
            "\1\153\37\uffff\1\153",
            "\1\154\37\uffff\1\154",
            "\1\155\37\uffff\1\155",
            "\1\156\37\uffff\1\156",
            "\1\157\37\uffff\1\157",
            "\1\160\5\uffff\1\161\31\uffff\1\160\5\uffff\1\161",
            "\1\162\37\uffff\1\162",
            "\1\163\37\uffff\1\163",
            "\1\164\37\uffff\1\164",
            "\1\165\37\uffff\1\165",
            "\1\166\3\uffff\1\167\33\uffff\1\166\3\uffff\1\167",
            "\1\170\37\uffff\1\170",
            "\1\171\37\uffff\1\171",
            "\1\172\37\uffff\1\172",
            "\1\173\37\uffff\1\173",
            "\1\174\37\uffff\1\174",
            "\1\175\37\uffff\1\175",
            "\1\176\37\uffff\1\176",
            "\12\47\7\uffff\3\47\1\u0080\26\47\4\uffff\1\47\1\uffff\3\47"+
            "\1\u0080\26\47",
            "\1\u0081\37\uffff\1\u0081",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0083\37\uffff\1\u0083",
            "\1\u0084\37\uffff\1\u0084",
            "\1\u0085\37\uffff\1\u0085",
            "\1\u0087\15\uffff\1\u0086\21\uffff\1\u0087\15\uffff\1\u0086",
            "\1\u0088\37\uffff\1\u0088",
            "\1\u0089\37\uffff\1\u0089",
            "\1\u008a\37\uffff\1\u008a",
            "\1\u008b\37\uffff\1\u008b",
            "\1\u008c\37\uffff\1\u008c",
            "\1\u008d\37\uffff\1\u008d",
            "\12\47\7\uffff\2\47\1\u008f\27\47\4\uffff\1\47\1\uffff\2\47"+
            "\1\u008f\27\47",
            "\1\u0090\37\uffff\1\u0090",
            "\1\u0091\37\uffff\1\u0091",
            "\1\u0092\37\uffff\1\u0092",
            "\1\u0093\37\uffff\1\u0093",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u0095\37\uffff\1\u0095",
            "\1\u0096\37\uffff\1\u0096",
            "\1\u0097\37\uffff\1\u0097",
            "\1\u0098\37\uffff\1\u0098",
            "\1\u0099\37\uffff\1\u0099",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u009b\37\uffff\1\u009b",
            "\1\u009c\37\uffff\1\u009c",
            "\1\u009d\37\uffff\1\u009d",
            "\1\u009e\37\uffff\1\u009e",
            "\1\u009f\37\uffff\1\u009f",
            "\1\u00a0\37\uffff\1\u00a0",
            "\1\u00a1\37\uffff\1\u00a1",
            "\1\u00a2\37\uffff\1\u00a2",
            "\1\u00a3\37\uffff\1\u00a3",
            "\1\u00a4\37\uffff\1\u00a4",
            "\1\u00a5\37\uffff\1\u00a5",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00a7\37\uffff\1\u00a7",
            "\1\u00a8\37\uffff\1\u00a8",
            "\1\u00a9\37\uffff\1\u00a9",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00ab\37\uffff\1\u00ab",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00ad\37\uffff\1\u00ad",
            "\12\47\7\uffff\4\47\1\u00af\25\47\4\uffff\1\47\1\uffff\4\47"+
            "\1\u00af\25\47",
            "\1\u00b0\37\uffff\1\u00b0",
            "\1\u00b1\37\uffff\1\u00b1",
            "\1\u00b3\3\uffff\1\u00b2\33\uffff\1\u00b3\3\uffff\1\u00b2",
            "\1\u00b4\37\uffff\1\u00b4",
            "\1\u00b5\37\uffff\1\u00b5",
            "",
            "\1\u00b6\37\uffff\1\u00b6",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u00b8\37\uffff\1\u00b8",
            "\1\u00b9\37\uffff\1\u00b9",
            "\1\u00ba\37\uffff\1\u00ba",
            "\1\u00bb\37\uffff\1\u00bb",
            "\1\u00bc\37\uffff\1\u00bc",
            "\1\u00bd\37\uffff\1\u00bd",
            "\1\u00be\37\uffff\1\u00be",
            "\1\u00bf\37\uffff\1\u00bf",
            "\1\u00c0\37\uffff\1\u00c0",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00c5\37\uffff\1\u00c5",
            "\1\u00c6\37\uffff\1\u00c6",
            "\1\u00c7\37\uffff\1\u00c7",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00c9\37\uffff\1\u00c9",
            "\1\u00ca\37\uffff\1\u00ca",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00cc\37\uffff\1\u00cc",
            "",
            "\1\u00cd\37\uffff\1\u00cd",
            "\1\u00ce\37\uffff\1\u00ce",
            "\1\u00cf\37\uffff\1\u00cf",
            "\1\u00d0\37\uffff\1\u00d0",
            "\1\u00d1\37\uffff\1\u00d1",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00d3\37\uffff\1\u00d3",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00d5\37\uffff\1\u00d5",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00d7\37\uffff\1\u00d7",
            "",
            "\1\u00d8\37\uffff\1\u00d8",
            "\1\u00d9\37\uffff\1\u00d9",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u00db\37\uffff\1\u00db",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u00dd\37\uffff\1\u00dd",
            "\1\u00de\37\uffff\1\u00de",
            "\1\u00df\37\uffff\1\u00df",
            "\1\u00e0\37\uffff\1\u00e0",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00e3\37\uffff\1\u00e3",
            "\1\u00e4\37\uffff\1\u00e4",
            "",
            "\1\u00e5\37\uffff\1\u00e5",
            "\1\u00e6\37\uffff\1\u00e6",
            "\1\u00e7\37\uffff\1\u00e7",
            "\1\u00e8\37\uffff\1\u00e8",
            "\1\u00e9\37\uffff\1\u00e9",
            "\1\u00ea\37\uffff\1\u00ea",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00ec\37\uffff\1\u00ec",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "",
            "",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00f0\37\uffff\1\u00f0",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00f2\37\uffff\1\u00f2",
            "",
            "\1\u00f3\37\uffff\1\u00f3",
            "\1\u00f4\37\uffff\1\u00f4",
            "\1\u00f5\37\uffff\1\u00f5",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u00f8\37\uffff\1\u00f8",
            "",
            "\1\u00f9\37\uffff\1\u00f9",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u00fb\37\uffff\1\u00fb",
            "\1\u00fc\37\uffff\1\u00fc",
            "\1\u00fd\37\uffff\1\u00fd",
            "",
            "\1\u00fe\37\uffff\1\u00fe",
            "",
            "\1\u00ff\37\uffff\1\u00ff",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0101\37\uffff\1\u0101",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0105\37\uffff\1\u0105",
            "\1\u0106\37\uffff\1\u0106",
            "\1\u0107\37\uffff\1\u0107",
            "\1\u0108\37\uffff\1\u0108",
            "\1\u0109\37\uffff\1\u0109",
            "\1\u010a\37\uffff\1\u010a",
            "",
            "\1\u010b\37\uffff\1\u010b",
            "",
            "",
            "",
            "\1\u010c\37\uffff\1\u010c",
            "",
            "\1\u010d\37\uffff\1\u010d",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "",
            "\1\u0111\37\uffff\1\u0111",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0114\37\uffff\1\u0114",
            "\1\u0115\37\uffff\1\u0115",
            "\1\u0116\37\uffff\1\u0116",
            "\1\u0117\37\uffff\1\u0117",
            "",
            "\1\u0118\37\uffff\1\u0118",
            "",
            "",
            "",
            "\1\u0119\37\uffff\1\u0119",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u011b\37\uffff\1\u011b",
            "\1\u011c\37\uffff\1\u011c",
            "\1\u011d\37\uffff\1\u011d",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0120\37\uffff\1\u0120",
            "\1\u0121\37\uffff\1\u0121",
            "",
            "",
            "",
            "\1\u0122\37\uffff\1\u0122",
            "",
            "",
            "\1\u0123\37\uffff\1\u0123",
            "\1\u0124\37\uffff\1\u0124",
            "\1\u0125\37\uffff\1\u0125",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0127\37\uffff\1\u0127",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u0129\37\uffff\1\u0129",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u012b\37\uffff\1\u012b",
            "",
            "",
            "\1\u012c\37\uffff\1\u012c",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u012e\37\uffff\1\u012e",
            "\1\u012f\37\uffff\1\u012f",
            "\1\u0130\37\uffff\1\u0130",
            "\1\u0131\37\uffff\1\u0131",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u0133\37\uffff\1\u0133",
            "",
            "\1\u0134\37\uffff\1\u0134",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0137\37\uffff\1\u0137",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u0139\37\uffff\1\u0139",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "\1\u013b\37\uffff\1\u013b",
            "",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u013d\37\uffff\1\u013d",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
            "",
            "\1\u013f\37\uffff\1\u013f",
            "",
            "\12\47\7\uffff\32\47\4\uffff\1\47\1\uffff\32\47",
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
            return "1:1: Tokens : ( SELECT | CREATE | TABLE | VIEW | MATERIALIZED | FROM | WHERE | GROUP | ORDER | BY | HAVING | RANDOM | SOURCE | RETURNS | FOR | EACH | WITH | ALL | DISTINCT | AS | IN | VALUES | AND | OR | NOT | LIKE | BETWEEN | EXISTS | ASC | DESC | AVG | SUM | COUNT | MAX | MIN | VAR | STDEV | VECTOR | ROWMATRIX | COLMATRIX | TRUE | FALSE | CASE | WHEN | ELSE | THEN | PRIMARY | FOREIGN | KEY | REFERENCES | OUT | DROP | VGFUNCTION | FUNCTION | GENERALTABLEINDEX | UNION | ASTERISK | SEMICOLON | COMMA | TWODOT | DOT | COLON | PLUS | MINUS | SLASH | LPAREN | RPAREN | LBRACE | RBRACE | LBRACKET | RBRACKET | EQUALS | NOTEQUALS | LESSTHAN | GREATERTHAN | LESSEQUAL | GREATEREQUAL | IDENTIFIER | STRING | NUMERIC | WS | COMMENT );";
        }
    }
 

}