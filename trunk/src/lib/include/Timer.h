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

#ifndef _TIMER_H_
#define _TIMER_H_

#include "Thread.h"

/**
 * Implements a standard timer.
 */
class Timer : public Thread
{
public:
	enum Mode {
		Disabled, 
		Single, 
		Repeated
	};
	
	Timer( uint32_t msecs, Mode mode );
	virtual ~Timer();
	
	void setTimeout( uint32_t msecs ) { m_timeout = msecs; }
	uint32_t getTimeout() const { return m_timeout; }
	
	void setMode( Mode mode ) { m_mode = mode; }
	uint32_t getMode() { return m_mode; }
	
	/**
	 * Set a callback function to be called when timeout expires.
	 */
	void setCallback( void (*function)(void) ) { m_callback = function; }
	
	/**
	 * This virtual method can be reimplemented in a subclass. It will 
	 * be called when Timer stops counting.
	 */
	virtual void action() = 0;
	
protected:
	
	/**
	 * Implements the virtual run() from Thread.
	 */
	void run();
	
private:
	Mode m_mode;
	uint32_t m_timeout;
	
	void (* m_callback)(void);
};

#endif // _TIMER_H_
