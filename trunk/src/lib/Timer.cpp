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

#include "Timer.h"

Timer::Timer( uint32_t timeout, Mode mode )
	: Thread()
{
	m_timeout = timeout;
	m_mode = mode;
	m_callback = NULL;
}

Timer::~Timer()
{
}

void Timer::run()
{
	// calling Thread::sleep()
	msleep( m_timeout );
	
	// Call actions
	if ( m_callback )
		m_callback();
	
	if ( &action != 0 ) 
		action();
}


