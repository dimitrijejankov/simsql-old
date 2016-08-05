

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



grammar DataFlow;

@header {

  package simsql.runtime;

  import java.util.ArrayList;
  import java.util.HashMap;
  import java.util.Map;

}

@lexer::header {
  package simsql.runtime;
}

@parser::members {

	// nothing right now!
}

parse returns [ArrayList <RelOp> value]
	:	v1=varList					{$value = new ArrayList <RelOp> ();
								 ParsedRHS whichOne = $v1.value.get ("operation");
								 if (whichOne == null) {
									throw new RuntimeException ("Parser: No operation defined for clause!");
								 } else if (whichOne.getIdentifier ().equals ("join")) {
									$value.add (new JoinOp ($v1.value));
								 } else if (whichOne.getIdentifier ().equals ("aggregate")) {
                                    $value.add (new AggregateOp ($v1.value));
                                 } else if (whichOne.getIdentifier ().equals ("inference")) {
                                    $value.add (new InferOp ($v1.value));
                                 } else if (whichOne.getIdentifier ().equals ("vgwrapper")) {
                                     $value.add (new VGWrapperOp ($v1.value));
                                 } else if (whichOne.getIdentifier ().equals ("select")) {
                                     $value.add (new SelectionOp ($v1.value));
                                 } else if (whichOne.getIdentifier ().equals ("tempTable")) {
                                     $value.add (new TempTableOp ($v1.value));
                                 } else if (whichOne.getIdentifier ().equals ("frameOutput")) {
                                     $value.add (new FrameOutputOp ($v1.value));
								 } else {
									throw new RuntimeException ("Parser: Got an undefined relational op!");
								 }}
		( v2=varList 					{whichOne = $v2.value.get ("operation");
								 if (whichOne == null) {
									throw new RuntimeException ("Parser: No operation defined for clause!");
								 } else if (whichOne.getIdentifier ().equals ("join")) {
									$value.add (new JoinOp ($v2.value));
								 } else if (whichOne.getIdentifier ().equals ("aggregate")) {
                                    $value.add (new AggregateOp ($v2.value));
                                 } else if (whichOne.getIdentifier ().equals ("inference")) {
                                    $value.add (new InferOp ($v2.value));
                                 } else if (whichOne.getIdentifier ().equals ("vgwrapper")) {
                                     $value.add (new VGWrapperOp ($v2.value));
								 } else if (whichOne.getIdentifier ().equals ("select")) {
                                     $value.add (new SelectionOp ($v2.value));
                                 } else if (whichOne.getIdentifier ().equals ("tempTable")) {
                                     $value.add (new TempTableOp ($v2.value));
                                 } else if (whichOne.getIdentifier ().equals ("frameOutput")) {
                                     $value.add (new FrameOutputOp ($v2.value));
                                 } else {
									throw new RuntimeException ("Parser: Got an undefined relational op!");
								 }}
		)* 
	;


varList returns [Map <String, ParsedRHS> value]		
	:	'{' v1=varDef					{$value = new HashMap <String, ParsedRHS> ();
								 $value.put ($v1.value.getKey (), $v1.value.getValue ());}
		( v2=varDef					{$value.put ($v2.value.getKey (), $v2.value.getValue ());}
		)* '}'
	;

varDef returns [VarDefPair value]
	:	'leftInput' 	'=' rhs1=varList 		{$value = new VarDefPair ("leftInput", new VarListRHS ($rhs1.value));}
	|	'rightInput' 	'=' rhs1=varList		{$value = new VarDefPair ("rightInput", new VarListRHS ($rhs1.value));}
	|	'input' 	'=' rhs1=varList 		{$value = new VarDefPair ("input", new VarListRHS ($rhs1.value));}
	|	'output' 	'=' rhs1=varList 		{$value = new VarDefPair ("output", new VarListRHS ($rhs1.value));}
	|	'operation'     '=' '{' rhs2=Identifier '}'	{$value = new VarDefPair ("operation", new IdentifierRHS ($rhs2.text));}
	|	'inFiles' 	'=' '{' rhs3=stringList '}'	{$value = new VarDefPair ("inFiles", new StringListRHS ($rhs3.value));}
	|	'inAtts' 	'=' '{' rhs4=identifierList '}' {$value = new VarDefPair ("inAtts", new IdentifierListRHS ($rhs4.value));}
	|	'inTypes' 	'=' '{' rhs4=identifierList '}' {$value = new VarDefPair ("inTypes", new IdentifierListRHS ($rhs4.value));}
	|	'hashAtts' 	'=' '{' rhs4=identifierList '}' {$value = new VarDefPair ("hashAtts", new IdentifierListRHS ($rhs4.value));}
	|	'typeCode' 	'=' '{' rhs5=Int '}'		{$value = new VarDefPair ("typeCode", new IntegerRHS (Integer.parseInt ($rhs5.text)));}
	|	'selection' '=' '{' rhs6=exp '}'		{$value = new VarDefPair ("selection", new ExpressionRHS ($rhs6.value));}
	|	'outAtts' 	'=' '{' rhs7=assignmentList '}' {$value = new VarDefPair ("outAtts", new AssignmentListRHS ($rhs7.value));}
	|	'outFile' 	'=' '{' rhs8=String '}'		{if ($rhs8.text.length () == 2)
								  	$value = new VarDefPair ("outFile", new StringLiteralRHS (""));	
								 else
									$value = new VarDefPair ("outFile", 
									new StringLiteralRHS ($rhs8.text.substring (1, $rhs8.text.length () - 1))); }
    |   'groupByAtts'   '=' '{' rhs4=identifierList '}'  {$value = new VarDefPair("groupByAtts", new IdentifierListRHS($rhs4.value)); }
    |   'aggregates'    '=' '{' rhs10=aggregateList '}'  {$value = new VarDefPair("aggregates", new AggregateListRHS($rhs10.value)); }
    |   'outerInput'    '=' rhs1=varList                 {$value = new VarDefPair ("outerInput", new VarListRHS ($rhs1.value));}
    |   'innerInputs'   '=' rhs11=innerInputList         {$value = new VarDefPair ("innerInputs", new VarListRHS ($rhs11.value));}
    |   'seedAtt'       '=' '{' rhs2=Identifier '}'      {$value = new VarDefPair ("seedAtt", new IdentifierRHS($rhs2.text));} 
    |   'function'      '=' rhs1=varList                 {$value = new VarDefPair ("function", new VarListRHS($rhs1.value));}
    |   'functionName'  '=' '{' rhs8=String '}'          {if ($rhs8.text.length () == 2)
								  	$value = new VarDefPair ("functionName", new StringLiteralRHS (""));	
								 else
									$value = new VarDefPair ("functionName", 
									new StringLiteralRHS ($rhs8.text.substring (1, $rhs8.text.length () - 1)));}

    |   'vgInAtts'         '=' '{' rhs4=identifierList '}'      {$value = new VarDefPair ("vgInAtts", new IdentifierListRHS($rhs4.value));}

    |   'vgOutAtts'        '=' '{' rhs4=identifierList '}'      {$value = new VarDefPair ("vgOutAtts", new IdentifierListRHS($rhs4.value));}

    |   'removeDuplicates' '=' '{' rhs12=Boolean '}' {$value = new VarDefPair("removeDuplicates", new StringLiteralRHS($rhs12.text)); }

    |   'isFinal'          '=' '{' rhs12=Boolean '}' {$value = new VarDefPair("isFinal", new StringLiteralRHS($rhs12.text)); }

    |   'sortedOn'         '=' '{' rhs2=Identifier '}'      {$value = new VarDefPair ("sortedOn", new IdentifierRHS($rhs2.text));} 

    |   'joinType'         '=' '{' rhs2=Identifier '}' {$value = new VarDefPair("joinType", new IdentifierRHS($rhs2.text));}
    |   'rows'             '=' '{' rhs13=rowList '}' {$value = new VarDefPair("rows", new RowListRHS($rhs13.value));}
	;
    
    
rowList returns [ArrayList<ArrayList<Expression>> value]
    : '{' l1=colList '}'
        {
            $value = new ArrayList<ArrayList<Expression>>();
            $value.add($l1.value);
        }
       ( ',' '{' l2=colList '}'
        {
            $value.add($l2.value);
        }
       )*
    ;

colList returns [ArrayList<Expression> value]
    :  e1=atomExp
        {
            $value = new ArrayList<Expression>();
            $value.add($e1.value);
        }
       ( ',' e2=atomExp
            {
                $value.add($e2.value);
            }        
       )*
    ;
        

aggregateList returns [ArrayList<AggregateExp> value]
    : i1=Identifier '=' t1=Identifier '(' e1=addExp? ')' 
        {
            $value = new ArrayList<AggregateExp>();
            $value.add(new AggregateExp($i1.text, $t1.text, $e1.value));
        }   

      ( ',' i2=Identifier '=' t2=Identifier '(' e2=addExp? ')'
        {
            $value.add(new AggregateExp($i2.text, $t2.text, $e2.value));
        }
      )*
      
    ;   

innerInputList returns [Map<String,ParsedRHS> value]
    : '{' i1=Identifier '='  v1=varList           {$value = new HashMap<String, ParsedRHS> ();
                                                   $value.put($i1.text, new VarListRHS($v1.value));}

      (i2=Identifier '=' v2=varList               {$value.put($i2.text, new VarListRHS($v2.value));}
      )* '}'
    ;

stringList returns [ArrayList <String> value]
	:	s1=String					{$value = new ArrayList <String> ();
								 if ($s1.text.length () == 2)
								  	$value.add ("");
								 else
								 	$value.add ($s1.text.substring (1, $s1.text.length () - 1));}
		( ',' s2=String					{if ($s2.text.length () == 2)
									$value.add ("");
								 else
								 	$value.add ($s2.text.substring (1, $s2.text.length () - 1));}
		)*
	;

identifierList returns [ArrayList <String> value]
	:	s1=Identifier					{$value = new ArrayList <String> ();
								 $value.add ($s1.text);}
		( ',' s2=Identifier				{$value.add ($s2.text);}
		)*
	;

assignmentList returns [ArrayList <Assignment> value]
	:	i1=Identifier '=' a1=addExp			{$value = new ArrayList <Assignment> ();
								 $value.add (new Assignment ($i1.text, $a1.value));}
		( ',' i2=Identifier '=' a2=addExp		{$value.add (new Assignment ($i2.text, $a2.value));}
		)*
	;

comparison returns [Expression value]
	:	e1=addExp 					{$value = $e1.value;}
		( '==' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("equals");
				 				 $value.setSubexpression (temp, $e2.value);}
		| '!=' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("not equal");
				 				 $value.setSubexpression (temp, $e2.value);}
		| '<' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("less than");
				 				 $value.setSubexpression (temp, $e2.value);}
		| '<=' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("less than or equal");
				 				 $value.setSubexpression (temp, $e2.value);}
		| '>' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("greater than");
				 				 $value.setSubexpression (temp, $e2.value);}
		| '>=' e2=addExp 				{Expression temp = $value;
				 				 $value = new Expression ("greater than or equal");
				 				 $value.setSubexpression (temp, $e2.value);}
		)?
	| 	'!' c=comparison 				{$value = new Expression ("not");
				    				     $value.setSubexpression ($c.value);}
    |   'null' c=comparison             {$value = new Expression("is null");
                                         $value.setSubexpression ($c.value);}
	;


exp returns [Expression value]
	:	m1=comparison        				{$value = $m1.value;}
		( '&&' m2=comparison    			{Expression temp = $value;
				     				 $value = new Expression ("and");
				     				 $value.setSubexpression (temp, $m2.value);}
		| '||' m2=comparison    			{Expression temp = $value;
				     				 $value = new Expression ("or");
				     				 $value.setSubexpression (temp, $m2.value);}
		)*
	;

addExp returns [Expression value]
	:	m1=multExp          				{$value = $m1.value;}
		( '+' m2=multExp    				{Expression temp = $value;
				     				 $value = new Expression ("plus");
				     				 $value.setSubexpression (temp, $m2.value);}
		| '-' m2=multExp    				{Expression temp = $value;
				     				 $value = new Expression ("minus");
				     				 $value.setSubexpression (temp, $m2.value);}
		)*
	;

multExp returns [Expression value]
	:	m1=atomExp          				{$value = $m1.value;}
		( '*' m2=atomExp    				{Expression temp = $value;
				     				 $value = new Expression ("times");
				     				 $value.setSubexpression (temp, $m2.value);}
		| '/' m2=atomExp    				{Expression temp = $value;
				     				 $value = new Expression ("divided by");
				     				 $value.setSubexpression (temp, $m2.value);}
		)*
	;


atomExp returns [Expression value]
	:	n=Int   	    				{$value = new Expression ("literal int");
				     				 $value.setValue ($n.text);}
	|	i=Identifier  	    				{$value = new Expression ("identifier");
				     				 $value.setValue ($i.text);}
	|	c=String	    				{$value = new Expression ("literal string");
				     				 $value.setValue ($c.text);}
	|	c=Float		    				{$value = new Expression ("literal float");
				     				 $value.setValue ($c.text);}
	|	'(' e1=exp ')'   				{$value = $e1.value;}
	| 	'-' e1=atomExp	    				{$value = new Expression ("unary minus");
				     				 $value.setSubexpression ($e1.value);}
	|	f=funcCall					{$value = $f.value;}
	;


funcCall returns [Expression value]
	:	i=Identifier '(' 			    {$value = new Expression ("func"); $value.setValue ($i.text); }
             (e1=exp                     {$value.addSubexpression ($e1.value);}
		( ',' e2=exp					{$value.addSubexpression ($e2.value);}
		)*)? ')'
	;


Float
	:	('0'..'9')+ '.' ('0'..'9')+ ('e' '-'? ('0'..'9')+)?
	;

Boolean
    :  ('true' | 'false')
	;

Identifier
	:	(('a'..'z') | ('A'..'Z') | '_') (('a'..'z') | ('A'..'Z') | '.' | '_' | ('0'..'9'))*
	;
 
Int
	:	('0'..'9')+
	;

String
    : '"' ('a'..'z'|'A'..'Z'|'0'..'9'|'/'|'('|')'|':'|'\\'|'.'|('\\' '"')|'{'|'}'|'|'|','|' '|'_'|'-'|'!'|'@'|'#'|'$'|'%'|'^'|'&'|'*'|'+'|'='|'['|']'|'~'|'?')* '"'
	;

    
// whitespace and comments.
WS
	:	(' ' | '\t' | '\r'| '\n' | '\u000C')        {$channel=HIDDEN;}
    |   '#' (options {greedy=false;}: .*) '\n'      {$channel=HIDDEN;}
	;

