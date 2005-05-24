/****************************************************************************
 *
 *  Copyright (C) 2000-2001 RealNetworks, Inc. All rights reserved.
 *
 *  This program is free software.  It may be distributed under the terms
 *  in the file LICENSE, found in the top level of the source distribution.
 *
 */

#include <stdarg.h>
#include <time.h>

#include "dbg.h"
#include "../config/config_parser.h"

static FILE *log;

void dbg(const char *fmt, ...)
{
	if ( ! global_config.debug )
		return;

	char str[4096];
	va_list v;
	va_start(v, fmt);
	vsprintf(str, fmt, v);

	if ( global_config.log_to_file ) {
		fputs(str, log);
		fflush( log );
	} else 
		fputs(str, stderr);
}

void open_log_file()
{
	log = fopen( global_config.log_file, "w" );
	if ( log == NULL ) {
		fprintf(stderr, "Failed to open log file... log to file disabled..\n");
		global_config.log_to_file = false;
		return;
	}
}

void close_log_file()
{
	if ( ! log )
		return;
		
	fclose( log );
}


/** LOG **
 *
 * $Log: dbg.cpp,v $
 * Revision 1.2  2003/11/17 16:14:02  mat
 * make-up
 *
 *
 */

