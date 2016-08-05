

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


grammar PrologQuery;

options {
    k = 2;
}

@header {
    package simsql.code_generator;
}

@lexer::header {
    package simsql.code_generator;
}

/** The query is a sequence of tuples. */
query returns [ArrayList<TupleTerm> value]
    :
        t1=tupleTerm '.'
        {
            $value = new ArrayList<TupleTerm>();
            $value.add($t1.value);
        }

        (
            t2=tupleTerm '.'
            {
                $value.add($t2.value);
            }
        )*
    ;


/** A tuple term. */
tupleTerm returns [TupleTerm value] 
    :
        a1=ATOM '(' t1=termSet ')'
        {
            $value = new TupleTerm(new AtomTerm($a1.text), $t1.value);
        }
    ;

/** A list term. */
listTerm returns [ListTerm value]
    :
        '[' t1=termSet ']'
        {
            $value = new ListTerm($t1.value);
        }
    |
        '(' t1=termSet ')'
        {
            $value = new ListTerm($t1.value);
        }

    ;
        
/** A set of terms, separated by commas. */
termSet returns [ArrayList<PrologTerm> value] 
    :
        {
            $value = new ArrayList<PrologTerm>();
        }

        (
            t1=term
            {
                $value.add($t1.value);
            }
            
            (
                ',' t2=term
                {
                    $value.add($t2.value);
                }
            )*
        )?
    ;

term returns [PrologTerm value]
    :
        t1=tupleTerm
        {
            $value = $t1.value;
        }
    |
        l1=listTerm
        {
            $value = $l1.value;
        }
    |
        a1=ATOM
        {
            $value = new AtomTerm($a1.text);
        }
    |
        n1=NUMBER
        {
            $value = new AtomTerm($n1.text);
        }
    |
        s1=STRING
        {
            $value = new AtomTerm($s1.text);
        }
        
    ;



NUMBER 
    :
       ('-')? ('0'..'9')+ ('.' ('0'..'9')+ ('e' ('+'|'-')? ('0'..'9')+)?)?
    ;

ATOM
    :
        ('a'..'z' | 'A'..'Z' | '0'..'9' | '_' | '-' | '%' | '*' | ' ')+
    ;

STRING
    : 
        '\'' ('a'..'z'|'A'..'Z'|'0'..'9'|'/'|'('|')'|':'|'\\'|'.'|('\\' '\'')|'{'|'}'|'|'|','|' '|'_'|'-'|'!'|'@'|'#'|'$'|'%'|'^'|'&'|'*'|'+'|'='|'['|']'|'~'|'?')* '\''
    ;

WS :
        '\n' '%' (options {greedy=false;}: .*) '\n' 
        {
            $channel = HIDDEN;
        }
    |
        ( ' ' | '\t' | '\n' | '\r' | '\u000C')
        {
            $channel = HIDDEN;
        }
    ;

