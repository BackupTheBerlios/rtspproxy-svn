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
 
#include <string.h>
#include <errno.h>

#include "Thread.h"
#include "Log.h"

static void thread_sleep(struct timespec *ti);



Thread::Thread()
{
	m_finished = false;
	m_running = false;
	m_terminated = false;
}

Thread::~Thread()
{
	if ( m_running && ! m_finished ) {
		Warning << "~Thread: Thread object destroyed while thread still running\n";
	}
}

bool Thread::start()
{	
	pthread_attr_init( &m_attr );
	pthread_attr_setdetachstate( &m_attr, PTHREAD_CREATE_JOINABLE );
	
	int rc = pthread_create( &m_thread, &m_attr, (void*)&run, NULL);
	if ( rc ) {
		Warning << "Thread::start() Error creating new thread" << strerror(errno) << "\n";
		m_running = false;
        m_finished = false;
        m_thread = 0;
		return false;
	}
	
	return true;
}

void Thread::terminate()
{
	if ( ! m_thread )
		return;
	
	int r = pthread_cancel( m_thread );
	if ( r ) {
		Warning << "Thread::terminate() Error terminating thread.\n";
	} else {
		m_terminated = true;
	}
}

inline int Thread::getId()
{
	return (int)m_thread;
}

void Thread::sleep(unsigned long secs)
{
    struct timeval tv;
    gettimeofday(&tv, 0);
    struct timespec ti;
    ti.tv_sec = tv.tv_sec + secs;
    ti.tv_nsec = (tv.tv_usec * 1000);
    thread_sleep(&ti);
}

void Thread::msleep(unsigned long msecs)
{
    struct timeval tv;
    gettimeofday(&tv, 0);
    struct timespec ti;
	
    ti.tv_nsec = (tv.tv_usec + (msecs % 1000) * 1000) * 1000;
    ti.tv_sec = tv.tv_sec + (msecs / 1000) + (ti.tv_nsec / 1000000000);
    ti.tv_nsec %= 1000000000;
    thread_sleep(&ti);
}

void Thread::usleep(unsigned long usecs)
{
    struct timeval tv;
    gettimeofday(&tv, 0);
    struct timespec ti;
	
    ti.tv_nsec = (tv.tv_usec + (usecs % 1000000)) * 1000;
    ti.tv_sec = tv.tv_sec + (usecs / 1000000) + (ti.tv_nsec / 1000000000);
    ti.tv_nsec %= 1000000000;
    thread_sleep(&ti);
}


/** @internal
 *  helper function to do thread sleeps, since usleep()/nanosleep() 
 *  aren't reliable enough (in terms of behavior and availability)
 */
static void thread_sleep(struct timespec *ti)
{
    pthread_mutex_t mtx;
    pthread_cond_t cnd;
	
    pthread_mutex_init(&mtx, 0);
    pthread_cond_init(&cnd, 0);
	
    pthread_mutex_lock(&mtx);
    (void) pthread_cond_timedwait(&cnd, &mtx, ti);
    pthread_mutex_unlock(&mtx);
	
    pthread_cond_destroy(&cnd);
    pthread_mutex_destroy(&mtx);
}
