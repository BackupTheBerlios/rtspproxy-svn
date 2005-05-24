
%{
/**************************************************************************
 *   Copyright (C) 2003 Matteo Merli <matteo.merli@studenti.unipr.it>
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *
 *   $Id$
 *****************************************************************************/
 
#include <stdio.h>
#define YYSTYPE char*

#include "config_parser.h"

extern int yylex();

int lineno = 1;
u_int8_t bool_val;

void yyerror(const char *str)
{
	fprintf(stderr, "Error - Line %u - %s\n", lineno, str);
	exit(1);
}

int yywrap()
{
	return 1;
}

%}

%token EQUAL NUMBER STRING YES NO 
%token TOK_DEBUG TOK_PORT TOK_CACHE_ENABLE TOK_CACHE_MAX_SIZE
%token TOK_LOG_TO_FILE TOK_LOG_FILE TOK_DEAMON_MODE UNKNOWN
%token TOK_CACHE_DIR

%%

configuration: /* empty */
              | configuration command
;
	
command:
        TOK_DEBUG bool {
		printf("DEBUG = %s\n", bool_val ? "YES" : "NO");
		global_config.debug = bool_val;
	}
	|
	TOK_LOG_TO_FILE EQUAL bool {
		printf("LOG_TO_FILE = %s\n", bool_val ? "YES" : "NO");
		global_config.log_to_file = bool_val;
	}
	|
	TOK_DEAMON_MODE EQUAL bool {
		printf("DEAMON_MODE = %s\n", bool_val ? "YES" : "NO");
		global_config.deamon_mode = bool_val;
	}
	|
	TOK_LOG_FILE EQUAL STRING {
		printf("LOG_FILE = '%s'\n", $3);
		global_config.log_file = $3;
	}
	|
	TOK_PORT NUMBER {
		printf("PORT = %u\n", atoi($2));
		global_config.proxy_port = atol( $2 );
	}
	|
	TOK_CACHE_ENABLE EQUAL bool {
		printf("CACHE_ENABLE = %s\n", bool_val ? "YES" : "NO");
		global_config.cache_enable = bool_val;
	}
	|
	TOK_CACHE_MAX_SIZE EQUAL NUMBER {
		printf("CACHE_MAX_SIZE = %u\n", atoi($3));
		global_config.cache_max_size = atol( $3 );
	}
	|
	TOK_CACHE_DIR EQUAL STRING {
		printf("CACHE_DIR = '%s'\n", $3);
		global_config.cache_dir = $3;
	}
;

bool:	YES 	{bool_val = 1;}
	|
	NO	{bool_val = 0;}
;


