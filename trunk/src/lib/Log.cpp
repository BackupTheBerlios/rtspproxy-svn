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
 
#include "Log.h"

#include <iostream>

// LogBase ProxyLog();

LogBase Debug( LogBase::STD_COUT );
LogBase Info( LogBase::STD_COUT );
LogBase Warning( LogBase::STD_COUT );
LogBase Error( LogBase::STD_COUT );

LogBase::LogBase( Output out )
{
	m_mutex = new Mutex();
}

LogBase::~LogBase()
{
	delete m_mutex;
}

LogBase & LogBase::operator<<( const std::string& str ) 
{ 
	m_mutex->lock();
	std::cout << str;
	std::cout.flush();
	m_mutex->unlock();
	return *this; 
}

LogBase & LogBase::operator<<( const char* str )
{ 
	m_mutex->lock();
	std::cout << str;
	std::cout.flush();
	m_mutex->unlock();
	return *this; 
}


