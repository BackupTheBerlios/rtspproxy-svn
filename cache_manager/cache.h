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
 
#ifndef CACHE_H
#define CACHE_H

#include <sys/types.h>
#include "cache_items.h"
#include "cache_status.h"
#include "sdp.h"

/**
 * Conveniency constants.
 */
enum {
	CACHE_HIT,
	CACHE_MISS
};

/* This is the default maximum size for the cache.
 * Can be changed with a command line parameter.
 */
#define CACHE_MAX_SIZE 52428800 // 50 Mb


/**
 * Cache is the "cache manager" layer for the proxy.
 * All the cache related actions are taken using this class
 */
class Cache :
	public CacheItems//, public CacheStatus
{

 public:
	/**
	 * Constructor
	 */
	Cache();
	/**
	 * Destructor
	 */
	~Cache();



	/**
	 * This is a debug function that print to screen all the element in the item list.
	 */
	void print_list();

	/*!
	 * Sets the maximum size of the cache.
	 */
	void set_size(size_t size);

	/*! Returns the instance of a previously cached item identfied by url. */
	CacheItem * get_item(const char* url);

	/*! Completely empties the cache, removing all items and
	    unlinking the cache segments associated.
	 */
	void empty();

	size_t get_max_size() {return m_max_size;}

	/*! Returns a reference to the SDP description manager. */
	SDP* sdp() {return m_sdp;}

 protected:

	 /*! Chache Maximum Size */
	 size_t m_max_size;

	 /*! Cache status instance */
	 CacheStatus *m_cache_status;

	 /*! SDP manager. */
	 SDP *m_sdp;
};

#endif


