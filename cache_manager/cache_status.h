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

#ifndef CACHE_STATUS_H
#define CACHE_STATUS_H

#include "pthread.h"

/*!
 * Class CacheStatus runs in a separate thread and periodically
 * checks the status of the cache. It can do things like swap out
 * or cleaning out unused resources...
 */

class CacheStatus
{
public:
	/*! Contructor */
	CacheStatus();
	/*! Destructor */
	~CacheStatus();

	/*! Main function of the CacheStatus thread. */
	void run();

	/*! Verifies the status of the cache and eventually does a swap-out to
	 *  remove items in cache.
	 */
	void check_status();

	/*!
	 * Returns the pid of the cache status thread.
	 */
	int pid() {return m_pid;}

protected:

	/*! Extracts the LRU item from the cache.
         * @return The size of extracted item.
	 */
	int swap_out();

	pthread_t m_thread;

	/*! Pid of the cache status thread */
	int m_pid;
};

// static void *thread_main(void *);

/*! signal handler.. */
void handler(int sig);

#endif


/** LOG **
 *
 * $Log: cache_status.h,v $
 * Revision 1.3  2003/11/17 16:13:45  mat
 * make-up
 *
 *
 */

