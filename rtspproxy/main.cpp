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

#include <stdlib.h>
#include <stdio.h>
#include <stdarg.h>
#include <signal.h>
#include <getopt.h>
#include <pthread.h>

#include "app.h"
#include "rtspproxy.h"
#include "rtspprot.h"
#include "cache.h"
#include "dbg.h"
#include "config_parser.h"

#include <rtp_session.h>

extern config_t global_config;

/* Pointer to the CApp object */
CRtspProxyApp *p_app;

pthread_mutex_t s_main_mutex;

void sigHandler(int n)
{
	if ( global_config.cache_enable ) {
		/* This mutex will never be released.. */
		pthread_mutex_lock( &s_main_mutex );
		p_app->cache()->print_list();
		p_app->cache()->empty();
	}
	
	delete CResolver::GetResolver();
	
	if ( global_config.log_to_file )
		close_log_file();

	exit(0);
}

static void usage(char * progname)
{
	printf(
		"\nUsage: %s [options]\n\n"
		"\t--help [-h]\t\tPrint this help message.\n"
		"\t--version [-v]\t\tPrint version informations.\n"
		"\t--debug [-d]\t\tEnable debug messages.\n"
		"\t--log-to-file [-l]\tRedirect debugging output to a file.\n"
		"\n"
		"\t--port [-p]\t\tUse a different port for proxy. Default is %d.\n"
		"\t--deamon \t\tRun in deamon mode (Release the terminal)\n"
		"\t--enable-cache [-c]\tEnable stream caching.\n\n"
		"*** Cache Parameters *** \n\n"
		"\t--cache-maxsize n\tSpecify the max size of cache in Mb. Default is %d Mb.\n"
		"\t--cache-dir dir\t\tSpecify the directory where to store cached streams.\n"
		"\n",
		progname,
		RTSP_DEFAULT_PORT,
		CACHE_MAX_SIZE / 1048576
	);
}



int main(int argc, char **argv)
{
  /* Define default handlers (exits) for common signals */
	signal(SIGINT, sigHandler);
	signal(SIGTERM, sigHandler);
	signal(SIGQUIT, sigHandler);
	signal(SIGABRT, sigHandler);
	signal(SIGUSR1, sigHandler);
	signal(SIGPIPE, SIG_IGN);

	CRtspProxyApp *app = new CRtspProxyApp(argc, argv);

	p_app = app;

	int c;
	INT16 port;
	int result = 0;

	enum {
		MAX_SIZE,
		DEAMON,
		DIR
	};

	new ConfigParser();

	static struct option long_options[] = {
		{"help",          0, 0, 'h'},
		{"version",       0, 0, 'v'},
		{"port",          1, 0, 'p'},
		{"debug",         0, 0, 'd'},
		{"enable-cache",  0, 0, 'c'},
		{"cache-maxsize", 1, &result, MAX_SIZE},
		{"log-to-file",   1, 0, 'l'},
		{"deamon",	  0, 0, DEAMON},
		{"cache-dir",	  1, 0, DIR},
		{0, 0, 0, 0}
	};

	while (1) {
		int option_index = 0;


		c = getopt_long (argc, argv, "hvp:cdl",
				 long_options, &option_index);
		if (c == -1)
			break;

		switch ( c ) {

		case  0 :

			switch ( result ) {

			case DEAMON:
				global_config.deamon_mode = true;
				break;

			case DIR:
				global_config.cache_dir = strdup( optarg );

			case MAX_SIZE:
				dbg("Setting cache size to %d Mb.\n", atoi(optarg) );
				global_config.cache_max_size = atoi(optarg);
				break;
			}
			break;

			
		case 'h':
		  usage( argv[0] );
		  exit(0);


		case 'p':
			port = atoi( optarg );
			if (port > 0) {
				global_config.proxy_port = port;
			} else {
				usage(argv[0]);
				exit(1);
			}
			break;

		case 'l':
			global_config.log_to_file = true;
			global_config.log_file = strdup(optarg);

		case 'c':
			global_config.cache_enable = true;
			break;

		case 'd':
			global_config.debug = true;
			break;

		case 'v':
			printf( "\nRTSP Caching Proxy version 2.4 \n"
			    "(c) 2003 Matteo Merli, http://merlimat.net/rtsp-proxy\n"
				  "Based on : \n"
				"\nRTSP Proxy Reference Implementation Version 2.0 \n"
				  "(c) 2001 RealNetworks, Inc. All Rights Reserved\n\n");
			exit(0);

		default:
			printf("\n");
			exit(1);
		}
	}

	if (optind < argc) {
		printf ("Malformed arguments: ");
		while (optind < argc)
			printf ("%s ", argv[optind++]);
		printf("\n");
		usage( argv[0] );
		exit(1);
	}

	/* Set up the options */
	if ( global_config.log_to_file )
		open_log_file();
	app->UseCache( global_config.cache_enable );
	if ( global_config.cache_enable ) {
		app->cache()->set_size( global_config.cache_max_size );
	}
	app->SetPort( global_config.proxy_port );

	app->Run();

	if ( global_config.log_to_file )
		close_log_file();

	exit(0);
}

/** LOG **
 *
 * $Log: main.cpp,v $
 * Revision 1.3  2003/11/17 16:14:16  mat
 * make-up
 *
 *
 */

