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

#include "Mutex.h"

/** @internal */
struct internal_threadsafe_log_stream
{
	// internal_threadsafe_log_stream( std::string strFileName)
	// 	: m_out( strFileName.c_str() ) {}
		
	internal_threadsafe_log_stream( std::ostream & out )
		{ m_out = & out;  }

	void threadsafe_write_str( const std::string & str)
	{
		/// m_cs.lock();
		(*m_out) << str; 
		(*m_out).flush();
		/// m_cs.unlock();
	}
private:
	std::ostream *m_out;
	/// Mutex m_cs;
};

struct log_stream
{
	log_stream( std::ostream & out )
		: m_underlyingStream( out ) {}
	
	// log_stream( internal_threadsafe_log_stream & underlyingStream )
	// 	: m_underlyingStream( underlyingStream ) {}
    
	// log_stream( const log_stream & from)
	//	: m_underlyingStream( from.m_underlyingStream ) {}

	template< class type> log_stream & operator<<( type val ) { 
		m_buffer << val << std::endl; 
		return *this; 
	}
	
	void flush() { 
		m_underlyingStream.threadsafe_write_str( m_buffer.str() ); 
	}
private:
    internal_threadsafe_log_stream m_underlyingStream;
    std::ostringstream m_buffer;
};

/** 
 * Debug stream 
 * Ex. 
 *   Debug << "Error with .. " << item;
 */
extern log_stream Debug;

// extern 

/*
class Debug
{
public:
	Debug();
	virtual ~Debug();
	
	static void _warning(); 
};
*/
#endif //_DEBUG_H_
