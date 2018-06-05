grammar Narwhal;

prog: ( (stat | function)? (NEWLINE) )*
    ;

block: ( stat? NEWLINE )*
    ;

stat: PRINT value                               #print
       | ID '=' value                           #assign
       | ID '=' expression                      #exprAssign
       | READINT ID                             #readInt
       | READREAL ID                            #readReal
       | ID '()'                                #call
       | IF equal '{' blockif '}' 		        #if
       | REPEAT repetitions '{' block '}'		#repeat
    ;

value: ID
       | STRING
       | INT
       | REAL
    ;

expression: term ( ( add | sub )  term )*
    ;

term: factor ( ( mul | div ) factor )*
    ;

factor: lp expression rp    #ex
       | INT                #int
       | REAL               #real
       | ID                 #id
    ;

lp: '('
    ;

rp: ')'
    ;

add: '+'
    ;

sub: '-'
    ;

mul: '*'
    ;

div: '/'
    ;

function: FUNCTION ID '()' '{' block '}'
    ;

equal: ID '==' INT
    ;

blockif: block
    ;

repetitions: ID
    ;


REPEAT: 'repeat'
	;

IF: 'if'
    ;

FUNCTION: 'func'
    ;

READREAL: 'readReal'
    ;

READINT: 'readInt'
    ;

PRINT: 'print'
    ;

STRING: '"' ( ~('\\'|'"') )* '"'
    ;

ID: ('a'..'z'|'A'..'Z')+
    ;

INT: '0'..'9'+
    ;

REAL: '0'..'9'+'.''0'..'9'+
    ;

NEWLINE: '\r'? '\n'
    ;

WS: (' '|'\t')+ { skip(); }
    ;
