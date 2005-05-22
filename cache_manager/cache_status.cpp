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

#include "cache_status.h"
#include "cache.h"
#include "rtspproxy.h"

#include <stdio.h>
#include <unistd.h>
#include <signal.h>
#include <dbg.h>

/*! Seconds beetween every check of the cache status */
#define CHECK_PERIOD 10

/*! Reference of the instance of the class. */
static CacheStatus *g_cache_status;

/*! Global cache mutex */
pthread_mutex_t g_cache_mutex;
pthread_mutex_t g_cache_status_mutex;

static void *thread_main(void *);

extern Cache *g_cache;

CacheStatus::CacheStatus()
{
	g_cache_status = this;
	
	pthread_mutex_init( &g_cache_mutex, NULL );
	pthread_mutex_init( &g_cache_status_mutex, NULL );
	
	pthread_attr_t attr;
	pthread_attr_init(&attr);
	pthread_create( &m_thread, &attr , &thread_main, NULL);
	pthread_detach( m_thread );
}

CacheStatus::~CacheStatus()
{
}

void CacheStatus::run()
{
	struct timeval timeout;
	timeout.tv_sec = CHECK_PERIOD;
	timeout.tv_usec = 0;
	

	while (1) {
		timeout.tv_sec = CHECK_PERIOD;
		timeout.tv_usec = 0;
		select( 0, NULL, NULL, NULL, &timeout );
		check_status();
	}
}

void CacheStatus::check_status()
{
	/*! This is a trick to disable the check_status 
	 *  when we are working on the item list.
	 */
	pthread_mutex_lock( &g_cache_status_mutex );
	pthread_mutex_unlock( &g_cache_status_mutex );

	CacheItem *c_item;
        size_t total_size = 0;

	pthread_mutex_lock( &g_cache_mutex );
	cache_item_list_t::Iterator itr( g_cache->get_item_list()->Begin() );
	pthread_mutex_unlock( &g_cache_mutex );
	
	int idx = g_cache->get_item_list()->GetCount();
	while (itr && idx--) {
		c_item = *itr;
		++itr;

		if ( !c_item->segment() )
			continue;

		total_size += c_item->get_size();
	}

	// FIXME: Enable this!
	// dbg("\nTotal Cache Size: %0.1f Kbytes\n", total_size/1024.0);

	int s = 0;
	int t;

	if ( total_size > g_cache->get_max_size()) {
		dbg("Cache size exceeded the maximum allowed size..(%d Mb)\n",
			g_cache->get_max_size()/(1024*1024));
		do {
			t = 0;
			t = swap_out();
			if (t == -1)
				return;
			s += t;
			dbg("\nNew Cache Size: %0.1f Kbytes\n", (total_size -s)/1024.0);

		} while (total_size - s > g_cache->get_max_size() );
	}

}


int CacheStatus::swap_out()
{
	size_t size;
	
	pthread_mutex_lock( &g_cache_mutex );
	CacheItem *item = g_cache->get_item_list()->RemoveTail();
	pthread_mutex_unlock( &g_cache_mutex );

	if ( item->is_locked_read() || item->is_locked_write() ) {
		// We don't want to touch object in use...
		dbg("Item is locked, we cannot remove it now...\n");
		g_cache->get_item_list()->InsertTail( item );
		return -1;
	}
	item->lock_write();

	size = item->get_size();
	dbg("Removing %d - '%s'\n", item->id(), item->url());

	/* Here we should do an unlink() to the segments files...
	 * Anyway all the segment files should be deleted when
	 * shutting down the proxy.
	 */
	delete item;

	dbg("Swap Out: %d bytes\n", size);

	return size;
}


void * thread_main(void *p)
{
 	g_cache_status->run();

 	pthread_exit(0);
}





/** LOG **
 *
 * $Log: cache_status.cpp,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

