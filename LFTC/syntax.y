%{
    #include <string.h>
    #include <stdio.h>
    #include <stdlib.h>

    extern int yylex();
    extern int yyparse();
    extern FILE *yyin;
    extern int lineNo;
	extern void write_asm();
    void yyerror(const char *s);
%}

%token IDENT
%token CONSTANT
%token INTSYM
%token DOUBLE
%token INCMD 
%token OUTCMD 
%token IFSYM  
%token FORSYM 
%token ELSESYM 
%token PLUS 
%token MINUS 
%token SLASH
%token MODULO
%token TIMES 
%token LSS 
%token GTR 
%token LEQ 
%token GEQ 
%token BECOMES 
%token EQ 
%token NEQ
%token ACOP 
%token ACCL 
%token SEMICOLON 
%token PERIOD 
%token LPAREN 
%token RPAREN 
%token INSTR 
%token OUTSTR 
%token ANDSYM
%token ORSYM
%token STRUCT 
%token MAINFNC 
%token IOSTR 
%token RTRNSYM 
%token INCDIR 
%token USINGSYM 
%token NMSSYM 
%token STDSYM 

%%

program: directives global INTSYM MAINFNC LPAREN RPAREN ACOP statement_list RTRNSYM CONSTANT SEMICOLON ACCL
directives: INCDIR LSS IOSTR GTR
global: USINGSYM NMSSYM STDSYM SEMICOLON

statement_list: statement | statement statement_list
statement: declaration SEMICOLON | assignment SEMICOLON | if_stmt | output SEMICOLON | input SEMICOLON
block_code: ACOP statement_list ACCL | statement

type: simple_type | IDENT
declaration: type IDENT | type IDENT BECOMES CONSTANT | type IDENT BECOMES expression
assignment: IDENT BECOMES CONSTANT | IDENT BECOMES expression | IDENT PERIOD IDENT BECOMES expression | IDENT PERIOD IDENT BECOMES CONSTANT
expression: term | term operator expression | LPAREN expression RPAREN
term: CONSTANT | IDENT | IDENT PERIOD IDENT
operator: PLUS | MINUS | SLASH | TIMES | LSS | GTR | LEQ | GEQ | EQ | NEQ | MODULO | ANDSYM | ORSYM
simple_type: INTSYM | DOUBLE

if_stmt: IFSYM LPAREN expression RPAREN block_code | IFSYM LPAREN expression RPAREN block_code ELSESYM block_code

input: INCMD INSTR IDENT | input INSTR IDENT
output: OUTCMD OUTSTR expression | output OUTSTR expression

%%

int main(int argc, char *argv[]) {
    ++argv, --argc; /* skip over program name */ 
    
    // sets the input for flex file
    if (argc > 0) 
        yyin = fopen(argv[0], "r"); 
    else 
        yyin = stdin; 
    
    //read each line from the input file and process it
    while (!feof(yyin)) {
        yyparse();
    }
	write_asm();
    printf("The file is lexically and sintactly correct!\n");
    return 0;
}

void yyerror(const char *s) {
    printf("Error: %s at line -> %d ! \n", s, lineNo);
    exit(1);
}

