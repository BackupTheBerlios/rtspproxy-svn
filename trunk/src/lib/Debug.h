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

#ifndef _DEBUG_H_
#define _DEBUG_H_

#include <fstream>
#include <iostream>
#include <time.h>
#include <string>
#include <sstream>
 
/** @internal */
struct internal_threadsafe_log_stream
{
	internal_threadsafe_log_stream( std::string strFileName)
		: m_out( strFileName.c_str() ) {}
		
	internal_threadsafe_log_stream( std::ofstream out )
		: m_out( out ) {}  
    
	void threadsafe_write_str( const std::string & str)
	{
		m_cs.lock();
		m_out << str; 
		m_out.flush();
		m_cs.unlock();
	}
private:
	std::ofstream m_out;
	critical_section m_cs;
};

class Debug
{
public:
	Debug();
	virtual ~Debug();
	
	static void _warning() 
};

#endif //_DEBUG_H_
