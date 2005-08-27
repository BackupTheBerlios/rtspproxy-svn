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
 
#ifndef CACHE_ITEMS_H
#define CACHE_ITEMS_H

#ifdef _BSD
	#include <sys/types.h>
#else
	#include <types.h>
#endif
#include <pthread.h>

#include "../libapp/tlist.h"
#include "cache_segment.h"


/*! Compute the 16 bit checksum of a buffer. */
uint16_t checksum(void *vbuf, size_t nbytes);

/*!
 *  Data structure for the cached streams
 */
class CacheItem
{
 public:
	/*! Constructor */
	CacheItem();
	/*! Destructor  */
	~CacheItem();
	
	/*! Returns the size of the CacheItem. */
	uint32_t get_size();

	/*! Return whether the item is locked by an other object..*/
	bool is_locked_read();
	
	/*! Return whether the item is locked by an other object..*/
	bool is_locked_write();

	/*! Locks the segment to avoid it can be swapped out. */
	void lock_read() {pthread_mutex_lock( &m_read_mutex ); }
	
	/*! Locks the segment to avoid other write accesses. */
	void lock_write() {pthread_mutex_lock( &m_write_mutex ); }

	/*! Free the resource. */
	void unlock_read() {pthread_mutex_unlock( &m_read_mutex );}
	
	/*! Free the resource. */
	void unlock_write() {pthread_mutex_unlock( &m_write_mutex );}
	
	uint16_t id() {return m_id;}
	void set_id(uint16_t id) {m_id = id;}
	
	const char* url() {return (const char*)m_url;}
	void set_url(const char* url);
	
	CacheSegment* segment() {return m_segment;}
	void set_segment(CacheItem *parent);
	
	uint8_t transport() {return m_transport;}
	void set_transport(uint8_t tran) {m_transport = tran;}

private:
	/*! Resource identificator */
	uint16_t m_id;
  
        /*! Resource URL */
	char *m_url;
 
	/*! Packet format */
	uint8_t m_transport;
	
	/*! Segment */
	CacheSegment *m_segment;
	
	/*! Mutual exclusion controller for write operations. */
	pthread_mutex_t m_write_mutex;
	
	/*! Mutual exclusion controller for read operations. */
	pthread_mutex_t m_read_mutex;
	
};

typedef TDoubleList< CacheItem * > cache_item_list_t;


/**
 * Class CacheItems contains the references to all the cached objects, and
 * perform operarations like checking if a resource is cached or adding a 
 * new resource to the cache index.
 *
 * The replacement policy used in the cache is LRU (Last Recently Used). To do
 * this, we keep the LRU items on top of the list. This way we have always the 
 * list ordered by usage time.
 * 
 * New added items will be stored on top of the list. When there is a replacement
 * need, the last item should be removed from the list.
 */
class CacheItems 
{

 public:
	/*! Constructor  */
	CacheItems();
	/*! Destructor   */
	~CacheItems();
	
        /*!
	 * Checks whether the resource associated with the given url 
	 * is present in the cache.
	 * @param url string representing the complete url (eg: "rtsp://host:port/file")
	 * @return CACHE_HIT if the resource is found in cache, CACHE_MISS elsewhere.
	 */
	uint8_t check(const char *url);

        /*!
	 * Add, to the top of the list,  the resource identified by 
	 * the given url the cache index.
	 */
	CacheSegment * add(const char *url);

	/*!
	 * Returns the CacheItem given the URL
	 */
	CacheItem * get_item(const char *url);

	/*! Returns the cache item list. */
	cache_item_list_t *get_item_list() {return m_item_list;}


 protected:
	/*! List of the item in cache. */
	cache_item_list_t *m_item_list;
	
};


#endif

/** LOG **
 *
 * $Log: cache_items.h,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

