grammar Narwhal;

prog: ( stat? NEWLINE )*
    ;

stat: PRINT value       #print
       | ID '=' value       #assign
    ;

value: ID
       | STRING
       | INT
       | REAL
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
