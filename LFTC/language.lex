%{

#include <string.h>
#include <stdio.h>
#include <stdlib.h>
#include "syntax.tab.h"

/* ------------------------------
Symbol Table functions
------------------------------ */

int constant_serial = 0;
int identifier_serial = 0;
int pif_serial = 0;
FILE* dbg;

struct node
{
    char* key;
    int id;
    struct node *left;
    struct node *right;
};

void destroy_tree(struct node *leaf)
{
    if (leaf != 0)
    {
        destroy_tree(leaf->left);
        destroy_tree(leaf->right);
        free(leaf);
    }
}

int insert(char* key, struct node **leaf, int constant)
{
    if (*leaf == 0)
    {
        *leaf = (struct node*) malloc(sizeof(struct node));
        (*leaf)->key = key;
        /* initialize the children to null */
        (*leaf)->id = constant ? constant_serial++ : identifier_serial++;
        (*leaf)->left = 0;
        (*leaf)->right = 0;
            return (*leaf)->id;
    }
    else if (strcmp(key, (*leaf)->key) < 0)
            return insert(key, &(*leaf)->left, constant);
    else if (strcmp(key, (*leaf)->key) > 0)
            return insert(key, &(*leaf)->right, constant);
    else
            return (*leaf)->id;
}

void print_symbol_table(struct node *leaf, FILE* out)
{
    if (leaf != 0)
    {
        print_symbol_table(leaf->left, out);
		if (out == NULL)
			printf("%s: %d\n", leaf->key, leaf->id);
		else
			fprintf(out, "int %s;\n", leaf->key);
        print_symbol_table(leaf->right, out);
    }
}

void print_id(int id, FILE* out, struct node *leaf)
{
    if (leaf != 0)
    {
        print_id(id, out, leaf->left);
        if (leaf->id == id)
		{
			if (out == NULL)
				printf("%s: ", leaf->key);
			else
				fprintf(out, "%s", leaf->key);
		}
        print_id(id, out, leaf->right);
    }
};

struct pif_entry
{
    int token;
    int index;
};

struct pif_entry pif[5000];
struct node *constants_root = 0;
struct node *identifiers_root = 0;
int lineNo = 1;


void addPIF(int token, char* identifier) {
    pif[pif_serial].token = token;
    pif[pif_serial++].index = -1;
    if (identifier != NULL) {
        char *p = (char*)malloc(sizeof(char) * 9);
        p[0] = 0;
        int pos = 0;
        int now = 0;
        while (identifier[now]) {
            if (token == IDENT && pos >= 8)
            {
                printf("Identifiers can be at most 8 characters long!\n");
                exit(2);
            }
            p[pos] = identifier[now];
            ++now;
            ++pos;
        }
        p[pos] = '\0';
        if (token == IDENT)
            pif[pif_serial - 1].index = insert(p, &identifiers_root, 0);
        else
            pif[pif_serial - 1].index = insert(p, &constants_root, 1);
    }
}

void print() {
    printf("Identifiers: %d\n", pif_serial);
    for (int i = 0; i < pif_serial; ++i) {
        if (pif[i].token == IDENT || pif[i].token == CONSTANT)
            printf("%d->%d\n", pif[i].token, pif[i].index);
        else
            printf("%d\n", pif[i].token);
    }

    printf("\nConstants: %d\n", constant_serial);
    print_symbol_table(constants_root, NULL);

    printf("\nIdentifiers: %d\n", identifier_serial);
    print_symbol_table(identifiers_root, NULL);
}

void saveRegisters(FILE* out)
{
	fprintf(out, "push eax;\npush ebx;\npush ecx;\npush edx;\n");
}

void loadRegisters(FILE* out)
{
	fprintf(out, "pop edx;\npop ecx;\npop ebx;\npop eax;\n");
}

int evaluate(FILE* out, int i)
{
	int initially = i;
    if (pif[i].token == CONSTANT) {
        fprintf(out, "mov eax, ");
		print_id(pif[i].index, out, constants_root);
		fprintf(out, ";\n");
    } else {
        fprintf(out, "mov eax, [");
		print_id(pif[i].index, out, identifiers_root);
		fprintf(out, "];\n");
    }
    while (pif[i+1].token > ELSESYM && pif[i+1].token < LSS) {
        switch(pif[i+1].token) {
            case PLUS:
                fprintf(out, "adc");
                break;
            case MINUS:
                fprintf(out, "sbb");
                break;
            case TIMES:
                fprintf(out, "imul");
                break;
            case SLASH:
                fprintf(out, "idiv");
                break;
        }
        if (pif[i+2].token == CONSTANT) {
            fprintf(out, " eax, ");
			print_id(pif[i+2].index, out, constants_root);
			fprintf(out, ";\n");
        } else {
            fprintf(out, " eax, [");
			print_id(pif[i+2].index, out, identifiers_root);
			fprintf(out, "];\n");
        }
        i += 2;
    }
    return i - initially + 1;
}

int handle_assignment(FILE* out, int i){
	if (pif[i+2].token == CONSTANT) {
		fprintf(out, "mov dword ptr [");
		print_id(pif[i].index, out, identifiers_root);
		fprintf(out, "], ");
		print_id(pif[i+2].index, out, constants_root);
		fprintf(out, ";\n");
		return 3;                
	} else {
		int size = evaluate(out, i+2);
		fprintf(out, "mov [");
		print_id(pif[i].index, out, identifiers_root);
		fprintf(out, "], eax;\n");
		return 2 + size;
	}
}

int parse_statement_list(FILE* out, int i)
{
	int initially = i;
	while (pif[i].token != ACCL)
	{
		int skip = 0;
		if (pif[i].token == SEMICOLON)
		{
			i++;
			continue;
		}
		switch(pif[i].token)
		{
			case INTSYM:
				if (pif[i + 2].token != BECOMES)
					i += 3;
				else
					i += 1;
				break;
			case IDENT:
				if (pif[i + 1].token == BECOMES)
					i += handle_assignment(out, i);
				else
					i += 1;
				break;
			case IFSYM:
				evaluate(out, i+2);
				fprintf(out, "cmp eax, 0;\nje else%d;\n", i);
				skip = parse_statement_list(out, i + 4);
				fprintf(out, "else%d:\n", i);
				i += skip + 4;
				break;
			case OUTCMD:
				if (pif[i+2].token == IDENT)
				{
					saveRegisters(out);
					fprintf(out, "mov eax, dword ptr [");
					print_id(pif[i+2].index, out, identifiers_root);
					fprintf(out, "];\npush eax;\nlea eax, [outmsg];\npush eax;\ncall printf;\nadd esp, 8;\n");
					loadRegisters(out);
					i += 3;
				}
				else
					i++;
				break;
			case INCMD:
				if (pif[i+2].token == IDENT)
				{
					saveRegisters(out);
					fprintf(out, "lea eax, [");
					print_id(pif[i+2].index, out, identifiers_root);
					fprintf(out, "];\npush eax;\nlea eax, [inmsg];\npush eax;\ncall scanf;\nadd esp, 8;\n");
					loadRegisters(out);
					i += 3;
				}
				else
					i++;
				break;
			default:
				i += 1;
				break;
		}
	}
	return i - initially + 1;
}

void write_asm(){
	FILE* asmFile = fopen("file.cpp", "w");
	fprintf(asmFile, "#include <iostream>\n\nusing namespace std;\n\nint main(){\n");
	fprintf(asmFile, "char outmsg[4] = { '%%', 'd', '\\n', '\\0' };\nchar inmsg[3] = { '%%', 'd', '\\0' };\n");
	print_symbol_table(identifiers_root, asmFile);
	fprintf(asmFile, "__asm{\n");
	parse_statement_list(asmFile, 14);
	fprintf(asmFile, "\n}\nreturn 0;\n}");
}

%}

%option noyywrap
digit [0-9]
letter [a-zA-Z]

%%
"int"                                       { addPIF( INTSYM, 0);     return INTSYM;}
"double"                                    { addPIF( DOUBLE, 0);     return DOUBLE;}
"cin"                                       { addPIF( INCMD, 0);      return INCMD;}
"cout"                                      { addPIF( OUTCMD, 0);     return OUTCMD;}
"if"                                        { addPIF( IFSYM, 0);      return IFSYM;}
"for"                                     	{ addPIF( FORSYM, 0);     return FORSYM;}
"else"                                      { addPIF( ELSESYM, 0);    return ELSESYM;}
"+"                                         { addPIF( PLUS, 0);       return PLUS;}
"-"                                         { addPIF( MINUS, 0);      return MINUS;}
"/"                                         { addPIF( SLASH, 0);      return SLASH;}
"%"											{ addPIF( MODULO, 0);	  return MODULO;}
"*"                                         { addPIF( TIMES, 0);      return TIMES;}
"<"                                         { addPIF( LSS, 0);        return LSS;}
">"                                         { addPIF( GTR, 0);        return GTR;}
"<="                                        { addPIF( LEQ, 0);        return LEQ;}
">="                                        { addPIF( GEQ, 0);        return GEQ;}
"="                                         { addPIF( BECOMES, 0);    return BECOMES;}
"=="                                        { addPIF( EQ, 0);         return EQ;}
"!="                                        { addPIF( NEQ, 0);        return NEQ;}
"{"                                         { addPIF( ACOP, 0);       return ACOP;}
"}"                                         { addPIF( ACCL, 0);       return ACCL;}
";"                                         { addPIF( SEMICOLON, 0);  return SEMICOLON;}
"."                                         { addPIF( PERIOD, 0);     return PERIOD;}
"("                                         { addPIF( LPAREN, 0);     return LPAREN;}
")"                                         { addPIF( RPAREN, 0);     return RPAREN;}
">>"                                        { addPIF( INSTR, 0);      return INSTR;}
"&&"										{ addPIF( ANDSYM, 0);	  return ANDSYM;}
"||"										{ addPIF( ORSYM, 0);	  return ORSYM;}
"<<"                                        { addPIF( OUTSTR, 0);     return OUTSTR;}
"struct"                                    { addPIF( STRUCT, 0);     return STRUCT;}
"main"                                      { addPIF( MAINFNC, 0);    return MAINFNC;}
"iostream.h"                                { addPIF( IOSTR, 0);      return IOSTR;}
"return"                                    { addPIF( RTRNSYM, 0);    return RTRNSYM;}
"#include"                                  { addPIF( INCDIR, 0);     return INCDIR;}
"using"                                     { addPIF( USINGSYM, 0);   return USINGSYM;}
"namespace"                                 { addPIF( NMSSYM, 0);     return NMSSYM;}
"std"                                       { addPIF( STDSYM, 0);     return STDSYM;}
{letter}({letter}|{digit})*                 { addPIF( IDENT, yytext); return IDENT;}
"0"|((\+)?|(\-)?)[1-9]{digit}*              { addPIF( CONSTANT, yytext); return CONSTANT;}
(\+|\-)?([0]|([1-9]{digit}*))"."{digit}*[1-9]       { addPIF( CONSTANT, yytext); return CONSTANT;}
[ \t\r]            /* skip whitespace */
{digit}*{letter}     { printf("Invalid constant, cannot have letter after constant: %10s\n", yytext); exit(2);}
"0"{digit}           { printf("Cannot have 0 and a digit after it: %10s\n", yytext); exit(2);}
.                    { printf("Unknown character [%c]\n",yytext[0]); exit(2);}
[\n]                lineNo++;
%%