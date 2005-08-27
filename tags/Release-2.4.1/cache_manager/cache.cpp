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
#include <sys/types.h>
#include <dirent.h>

#include "../libapp/dbg.h"
#include "cache.h"
#include "../config/config_parser.h"

extern pthread_mutex_t g_cache_mutex;
extern pthread_mutex_t g_cache_status_mutex;

/* Global pointer to the class instance. It is legal cause we have only
 * one instance of this class.
 */
Cache *g_cache = NULL;

Cache::Cache()
	: CacheItems()
{
	dbg("Cache initialisation.\n");
	g_cache = this;
	m_max_size = CACHE_MAX_SIZE;
	
	DIR* dir = opendir( global_config.cache_dir );
	if ( dir == NULL ) {
		char s[50];
		/* We try to create the directory. */
		snprintf(s, 50, "/bin/mkdir -p %s", global_config.cache_dir );
		int result = system( s );
	
		if ( result != 0 ) {
			snprintf(s, 50, "Failed to open cache directory '%s' ",
			 global_config.cache_dir);
			perror(s);
			exit(1);
		}
	}
	closedir( dir );
	
	pthread_mutex_init( &g_cache_mutex, NULL );
		
	m_cache_status = new CacheStatus();

	m_sdp = new SDP();
}


Cache::~Cache()
{

}

void Cache::set_size(size_t size)
{
	if (size <= 0) {
		/* The size should be > 0 */
		m_max_size = CACHE_MAX_SIZE;
		return;
	}

	m_max_size = size*1024*1024; // Translating from Megabytes => bytes
}


CacheItem * Cache::get_item(const char* url)
{
	if ( !url ) {
		return NULL;
	}

	u_int16_t chk_sum = checksum( (void *)url, strlen(url) );

	CacheItem *item;
	
	pthread_mutex_lock( & g_cache_mutex );
	cache_item_list_t::Iterator itr( m_item_list->Begin() );
	while (itr) {
		item = *itr;
		if ( item->id() == chk_sum ) {
			pthread_mutex_unlock( & g_cache_mutex );
			return item; // Ok, item found
		}
		itr++;
	}
	pthread_mutex_unlock( & g_cache_mutex );

	return NULL;
}


void Cache::print_list()
{
	CacheItem *item;
	CacheSegment *cs;

	pthread_mutex_lock( & g_cache_mutex );
	dbg("============================\n"
			"Cache Item List (%d items): \n", m_item_list->GetCount());
	cache_item_list_t::Iterator itr(m_item_list->Begin());
	int idx = m_item_list->GetCount();
	while (itr && idx--) {
		item = *itr;

		dbg("\t%d\t%s\t", item->id(), item->url());
		cs = item->segment();
		dbg("(%0.1f Kb - %d packets) ",
		       cs->get_size()/1024.0 ,
		       cs->get_num_packets()
		       );
		dbg("\n");
		++itr;
	}
	pthread_mutex_unlock( & g_cache_mutex );

	dbg("============================\n");
}

void Cache::empty()
{
	pthread_mutex_lock( &g_cache_status_mutex );
	if ( m_item_list->GetCount() )
		dbg("Emptying the cache...\n");
	else return;

	CacheItem *item = NULL;
		
	pthread_mutex_lock( & g_cache_mutex );
	while ( m_item_list->GetCount() ) {
		item = m_item_list->RemoveTail();
		dbg("  Removing %d - '%s'\n", item->id(), item->url() );
		if ( ! item ) {
			dbg("Warning !! Item is NULL.\n");
			continue;
		} 
		delete item;
	}
	pthread_mutex_unlock( & g_cache_mutex );
}



