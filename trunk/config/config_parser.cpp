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
#include "config_parser.h"

#include <stdlib.h>
#include <string.h>
#include "cache.h"
#include "rtspprot.h"

#include "iniparser.h"

// extern FILE* yyin;

// extern "C" int yyparse();

config_t global_config;

/* Voices of the configuration file */

ConfigParser::ConfigParser()
{
	/*********************************
	 * DEFAULT VALUES
	 *********************************/
	global_config.debug = false;
	global_config.log_to_file = false;
	global_config.log_file = "./rtspproxy.log";

	global_config.proxy_port = RTSP_DEFAULT_PORT;
	global_config.deamon_mode = false;

	global_config.cache_enable = false;
	global_config.cache_max_size = uint(CACHE_MAX_SIZE) / (1024*1024);
	global_config.cache_dir = "./cache";
	
	global_config.hierarchical_proxy = false;
	global_config.hierarchical_proxy_addr = "proxy.server.com";
	global_config.hierarchical_proxy_port = 554;


	m_file_list = new file_list_t();
	
	m_file_list->InsertTail( "./rtspproxy.conf" );
	
	char *home = getenv("HOME");
	if ( home ) {
		const char *home_path = "/.rtspproxy.conf";
		char *str = (char *)malloc( strlen(home) + strlen(home_path) + 1);
		strncpy(str, home, strlen(home) );
		strncat(str, home_path, strlen(home_path) );
		m_file_list->InsertTail( str ); /* ~/.rtspproxy */
	}
	m_file_list->InsertTail( "/etc/rtspproxy.conf" );
	
	if ( !open_file() ) {
		return;
	}

  struct t_command *cmd;

  while ( 1 ) {
    cmd = parse_file( m_file );
    if ( cmd == NULL ) {
      break;
    }

    if ( cmd->type == NUMERIC )
      printf("\t%s = [%u] \n", cmd->name, cmd->value_num );
    else
      printf("\t%s = \"%s\" \n", cmd->name, cmd->value_str );

    if ( ! strcasecmp( "debug", cmd->name ) )
      global_config.debug = cmd->value_num;
    else if ( ! strcasecmp( "log_to_file", cmd->name ) )
      global_config.log_to_file = cmd->value_num;
    else if ( ! strcasecmp( "log_file", cmd->name ) )
      global_config.log_file = cmd->value_str;
    else if ( ! strcasecmp( "proxy_port", cmd->name ) )
      global_config.proxy_port = cmd->value_num;
    else if ( ! strcasecmp( "deamon_mode", cmd->name ) )
      global_config.deamon_mode = cmd->value_num;
    else if ( ! strcasecmp( "cache_enable", cmd->name ) )
      global_config.cache_enable = cmd->value_num;
    else if ( ! strcasecmp( "cache_max_size", cmd->name ) )
      global_config.cache_max_size = cmd->value_num;
    else if ( ! strcasecmp( "cache_dir", cmd->name ) )
      global_config.cache_dir = cmd->value_str;
    else if ( ! strcasecmp( "hierarchical_proxy", cmd->name ) )
      global_config.hierarchical_proxy = cmd->value_num;
    else if ( ! strcasecmp( "hierarchical_proxy_addr", cmd->name ) )
      global_config.hierarchical_proxy_addr = cmd->value_str;
    else if ( ! strcasecmp( "hierarchical_proxy_port", cmd->name ) )
      global_config.hierarchical_proxy_port = cmd->value_num;
  }

	// yyin = m_file;
	// yyparse();
  fclose( m_file );
}

ConfigParser::~ConfigParser()
{
	delete m_file_list;
}

bool ConfigParser::open_file()
{
	char *file_name;
	FILE *retval;
	
	file_list_t::Iterator it = m_file_list->Begin();
	for ( ; it; it++ ) {
		file_name = *(it);
		retval = fopen( file_name, "r");
		if ( retval == NULL ) {
			continue;
		}
		
		m_file = retval;
		fprintf(stderr, "Using configuration file : '%s'\n", file_name);
		return true;
	}
	
	/* Config file not found... */
	fprintf(stderr, 
		"\nConfiguration file not found.. using default settings..\n"
		"Paths for configuration file are (in sequence order) : \n");
	it = m_file_list->Begin();
	for ( ; it; it++ ) {
		fprintf(stderr, "'%s' ", (*it) );
	}
	fprintf(stderr, "\n\n");
	return false;
}


/** LOG **
 *
 * $Log: config_parser.cpp,v $
 * Revision 1.3  2003/11/17 16:13:56  mat
 * make-up
 *
 *
 */

