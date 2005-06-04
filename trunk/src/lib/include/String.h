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
 
#ifndef _RTSP_STRING_H_ // Modified to avoid conflicts with standard library
#define _RTSP_STRING_H_
 
#include <string>
#include <sstream>
#include <list>
 
/**
 * \brief Represents a string.
 */
class String
{
public:
 	/** 
 	 * \brief Constructor
 	 *
 	 * Builds an empty string.
 	 */
 	String();
 	
 	/** Destructor */
 	virtual ~String();
 	
 	/** 
	 * Add content to the string.
	 *
	 * @param str \a std::string that holds the content
	 */
	String & operator<<( const std::string& str );
	
	/** 
	 * Add content to the stream.
	 *
	 * @param str String that holds the content
	 */
	String & operator<<( const String& str );
	
	/** 
	 * Add content to the stream.
	 *
	 * @param str string that holds the content
	 */
	String & operator<<( const char* str );
	
	/**
	 * Returns a constant reference to internal \a std::string object.
	 */
	const std::string& stdString() const { return m_string; }
	
	/**
	 * Returns a reference to internal \a std::string object.
	 */
	std::string& stdString() { return m_string; }
	
	struct Null {};
	static const Null null;
 	
private:
	std::string m_string;
};

/** List of Strings */
typedef std::list<String> StringList;
/** Iterator for a list of strings */
typedef std::list<String>::iterator StringListIterator;
 
#endif // _RTSP_STRING_H_
 