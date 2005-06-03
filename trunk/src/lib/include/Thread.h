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
 
#ifndef _THREAD_H_
#define _THREAD_H_

#include <pthread.h>

/**
 * \brief Thread abstraction Class.
 * 
 * This class implements all the thread related utilities.
 * Classes that whish to execute some method in other thread, 
 * should subclass the \a Thread class implementing the \a run()
 * method.
 *
 * This method will be executed in the newly created thread using 
 * the \a start() method.
 */
class Thread
{
public:
	/*! Constructor */
	Thread();
	/*! Destructor */
	virtual ~Thread();
	
	/**
	 * Start the execution of the thread calling the
	 * run() virtual method.
	 */
	bool start();
	
	/**
	 * Terminate the execution of the thread.
	 */
	void terminate();
	
	/**
	 * Returns the internal thread id
	 */
	int getId();
	
	
	/**
	 * Forces the current thread to sleep for \a secs seconds.
	 *
	 * @param secs Number of seconds to sleep
	 */
	void sleep(unsigned long secs);
	
	/**
	 * Forces the current thread to sleep for \a msecs milli-seconds.
	 *
	 * @param msecs Number of milli-seconds to sleep
	 */
	void msleep( unsigned long msecs );
	
	/**
	 * Forces the current thread to sleep for \a usecs micro-seconds.
	 *
	 * @param usecs Number of micro-seconds to sleep
	 */
	void usleep( unsigned long usecs );
	
protected:
	/**
	 * Virtual function that will be called on thread starting.
	 * Subclasses of \a Thread should implement this method. 
	 */
	virtual void run() = 0;
	
private:
	
	pthread_t m_thread;
	
	pthread_attr_t m_attr;
	
	bool m_finished;
	
	bool m_running;
	
	bool m_terminated;
	
};

#endif //_THREAD_H_
