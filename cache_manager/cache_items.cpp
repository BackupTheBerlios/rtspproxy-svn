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
 
#include <string.h>
#include <types.h>
#include <dbg.h>

#include "cache_items.h"
#include "cache.h"

#include "rtspproxy.h"

extern pthread_mutex_t g_cache_mutex;

uint16_t checksum(void *vbuf, size_t nbytes)
{
	uint16_t *buf = (uint16_t *) vbuf;
	uint32_t sum;
	uint16_t oddbyte;

	sum = 0;
	while ( nbytes > 1 ) {
		sum += *buf++;
		nbytes -= 2;
	}

	if ( nbytes == 1 ) {
		oddbyte = 0;
		*( (uint16_t *) &oddbyte ) = *(uint8_t *) buf;
		sum += oddbyte;
	}

	sum = ( sum >> 16 ) + (sum & 0xffff );
	sum += ( sum >> 16 );

	return (uint16_t) ~sum;
}

// ************************************************

CacheItem::CacheItem() : 
	m_url( NULL ),
	m_segment( NULL )
{
	pthread_mutex_init( &m_read_mutex, NULL );
	pthread_mutex_init( &m_write_mutex, NULL );
}

CacheItem::~CacheItem()
{
	/* Here we should clean out the allocated objects
	 * in the CacheItem.
	 */
	if ( m_url )
		free( m_url );
	if ( m_segment )
		delete m_segment;
}

u_int32_t CacheItem::get_size()
{
	return m_segment->get_size();
}

bool CacheItem::is_locked_read()
{
	int s = pthread_mutex_trylock( &m_read_mutex );
	if ( s == EBUSY )
		return true;
	
	pthread_mutex_unlock( &m_read_mutex );
	return false;
}

bool CacheItem::is_locked_write()
{
	int s = pthread_mutex_trylock( &m_write_mutex );
	if ( s == EBUSY ) 
		return true;
	
	pthread_mutex_unlock( &m_write_mutex );
	return false;
}

void CacheItem::set_url(const char* url)
{
	m_url = strdup( url );
}

void CacheItem::set_segment(CacheItem *parent)
{
	if ( m_segment ) 
		delete m_segment;
	m_segment = new CacheSegment( parent );
}

// ************************************************

CacheItems::CacheItems()
{
	// dbg("CacheItems...\n");
	m_item_list = new cache_item_list_t();
	assert( m_item_list );
}

CacheItems::~CacheItems()
{
	delete m_item_list;
}


uint8_t CacheItems::check(const char* url)
{
	if ( !url ) {
		dbg("Searching for a NULL Url...\n");
		return CACHE_MISS;
	}
	
	uint16_t chk_sum = checksum( (void *)url, strlen(url) );
	
	assert( m_item_list );

	if ( m_item_list->GetCount() == 0 ) {
		/* Cache is empty... */
		dbg("Cache is empty....\n");
		return CACHE_MISS;
	}

	CacheItem *item;

	pthread_mutex_lock( & g_cache_mutex );
	cache_item_list_t::Iterator itr = m_item_list->Begin();
	while ( itr ) {
		item = *itr;
		if ( item->id() == chk_sum ) {
			/*! Ok, this is the seeked Item..
			 *  we have to check if it's empty
			 */
			if ( ! (item->get_size()) ) {
				/* The item is empty!!
				 * It's better to get rid of it!
				 */
				cache_item_list_t::Iterator it( m_item_list->Begin() );
				for (; it != m_item_list->End(); ++it ) {
					if ( (*it) == item ) {
						m_item_list->Remove( it );
						break;
					}
				}
				pthread_mutex_unlock( & g_cache_mutex );
				return CACHE_MISS;
			}
			pthread_mutex_unlock( & g_cache_mutex );
			return CACHE_HIT;
		}
		itr++;
	}
	pthread_mutex_unlock( & g_cache_mutex );

	/* Nothing found ... */
	return CACHE_MISS;
}


CacheSegment* CacheItems::add(const char* url)
{
	if ( !url )
		return NULL;

	dbg("Adding an item to the cache: %s", url);

	CacheItem *item = new CacheItem();
	item->set_id( checksum( (void *) url, strlen(url) ) );
	item->set_url( url );

	item->set_segment( item );
	
	pthread_mutex_lock( & g_cache_mutex );
	m_item_list->InsertHead( item );
	pthread_mutex_unlock( & g_cache_mutex );

	return item->segment();
}

CacheItem * CacheItems::get_item(const char *url)
{
	if ( !url ) {
		return NULL;
	}

	uint16_t chk_sum = checksum( (void *)url, strlen(url) );

	CacheItem *item;

	pthread_mutex_lock( & g_cache_mutex );
	cache_item_list_t::Iterator itr( m_item_list->Begin() );
	while ( itr ) {
		item = *itr;
		if ( item->id() == chk_sum ) {
			pthread_mutex_unlock( & g_cache_mutex );
			return item;
		}
		itr++;
	}
	
	pthread_mutex_unlock( & g_cache_mutex );
	return NULL;
}


/** LOG **
 *
 * $Log: cache_items.cpp,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

