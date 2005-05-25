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

#include "Mutex.h"

Mutex::Mutex( RecursionMode mode )
{
	m_recursive = ( mode == Recursive );
	pthread_mutex_init( &m_mutex, NULL );
	pthread_cond_init( &m_cond, NULL );
	
	m_count = 0;
}

Mutex::~Mutex()
{
	pthread_mutex_destroy( &m_mutex );
	pthread_cond_destroy( &m_cond );
}

void Mutex::lock()
{
	void* self = (void*) pthread_self();
	
	pthread_mutex_lock( &m_mutex );
	if ( m_owner == self ) {
		// Warning: Deadlock detected, mutex locked two times 
		// by same thread.
	}
	m_owner = self;
}

void Mutex::unlock()
{
	if ( m_owner != pthread_self() ) {
		// Error: Mutex must be released by the same thread that locked it
	}
	pthread_mutex_unlock( &m_mutex );
}

bool Mutex::tryLock()
{
	int r;
	r = pthread_mutex_trylock( &m_mutex );
	if ( r == EBUSY ) {
		// Mutex is already locked
		return false;
	}
	
}


