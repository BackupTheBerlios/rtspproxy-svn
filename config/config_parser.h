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
 
#ifndef _CONFIG_PARSER_H_
#define _CONFIG_PARSER_H_

#include <stdio.h>
#include <stdlib.h>

typedef struct {
	u_int8_t debug;
	u_int8_t log_to_file;
	u_int8_t deamon_mode;
	u_int16_t proxy_port;
	char * log_file;
	u_int8_t cache_enable;
	u_int16_t cache_max_size;
	char * cache_dir;
	u_int8_t hierarchical_proxy;
	char * hierarchical_proxy_addr;
	u_int16_t hierarchical_proxy_port;
} config_t;

extern config_t global_config;

#ifdef __cplusplus

#include "../libapp/tlist.h"
typedef TSingleList< char* > file_list_t;

class ConfigParser 
{
public:
	ConfigParser();
	~ConfigParser();
	
protected:
	bool open_file();

	FILE *m_file;
	
	file_list_t* m_file_list;

};

#endif // __cplusplus

#endif



/** LOG **
 *
 * $Log: config_parser.h,v $
 * Revision 1.3  2003/11/17 16:13:56  mat
 * make-up
 *
 *
 */

