grammar Narwhal;

prog: ( (stat | function)? (NEWLINE) )*
    ;

block: ( stat? NEWLINE )*
    ;

stat: PRINT value           #print
       | ID '=' value       #assign
       | READINT ID         #readInt
       | READREAL ID        #readReal
    ;

value: ID
       | STRING
       | INT
       | REAL
    ;

function: FUNCTION ID '()' '{' block '}'
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
