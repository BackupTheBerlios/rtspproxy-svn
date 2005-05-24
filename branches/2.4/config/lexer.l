
%{
#define YYSTYPE char*

#include "syntax.h"

#include <string.h>
#include <stdio.h>

extern int lineno;

%}

%%

debug			return TOK_DEBUG; 
proxy_port		return TOK_PORT; 
log_to_file		return TOK_LOG_TO_FILE;
log_file		return TOK_LOG_FILE; 
deamon_mode		return TOK_DEAMON_MODE;

cache_enable		return TOK_CACHE_ENABLE;
cache_max_size		return TOK_CACHE_MAX_SIZE;
cache_dir		return TOK_CACHE_DIR;

(yes|YES|Yes)		return YES;
(no|NO|No)		return NO;

[0123456789]+           { (char*)yylval = strdup( yytext ); printf("<%s>\n",yytext); return NUMBER; }

\"[^\"\n]*\"		{
			  /* We want to remove " " from the string.. */
			  yytext += 1;
			  yytext[ strlen(yytext) -1 ] = '\0';
			  (char*)yylval = strdup( yytext );
                          printf("%s\n", (char*)yytext );
			  return STRING;
			}

"="                     return EQUAL;

"#"[^\n]*\n		{ ++lineno;/* eat up one-line comments */ }
\n			{ ++lineno; }

[ \t]+			{ /* eat up whitespace */ }

.			{
                         printf(">>>> \"%s\" ", (char*)yytext );
                         return (char*)yytext;
                        }

%%
