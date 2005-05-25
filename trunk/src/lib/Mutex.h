/**************************************************************************
 *   Copyright (C) 2005 Matteo Merli <matteo.merli@gmail.com>
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
 * 
 *   $URL$
 * 
 *****************************************************************************/

#ifndef _MUTEX_H_
#define _MUTEX_H_

#include <pthread.h>

class Mutex
{
public:
	enum RecursionMode { NonRecursive, Recursive };

	Mutex( RecursionMode = NonRecursive );
	virtual ~Mutex();
	
	/** 
	 * Obtain the lock. Can block execution if the mutex is already 
	 * locked.
	 */
	void lock();
	
	/** 
	 * Release the lock.
	 */
	void unlock();
	
	/**
	 * Try to obtain the lock. If the mutex is already locked 
	 * return immediately.
	 * 
	 * @return true if lock was obtained
	 * @return false if mutex was already locked
	 */
	bool tryLock();
	 
	 
	
private:
	Mutex( const Mutex& other );
	Mutex & operator=(const Mutex& other);
	
	pthread_mutex_t m_mutex;
	pthread_cond_t m_cond;
	
	void* m_owner;
	
	bool m_recursive;
	uint8_t m_count;
};

#endif //_MUTEX_H_
